/*
 New BSD license: http://opensource.org/licenses/bsd-license.php

 Copyright (c) 2003, 2004, 2005 Sun Microsystems, Inc.
 901 San Antonio Road, Palo Alto, CA 94303 USA. 
 All rights reserved.


 Redistribution and use in source and binary forms, with or without 
 modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, 
  this list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice, 
  this list of conditions and the following disclaimer in the documentation 
  and/or other materials provided with the distribution.
 - Neither the name of Sun Microsystems, Inc. nor the names of its contributors
  may be used to endorse or promote products derived from this software 
  without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 POSSIBILITY OF SUCH DAMAGE.
*/
package com.xmlns.baetle.sesame;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.*;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.nativerdf.NativeStore;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.System.out;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Henry Story
 */
public class Query {
    RepositoryConnection lc;
    ValueFactory f;
    TupleQueryResultWriter tupleWriter;
    LinkedHashMap<String, String> nameSpaces = new LinkedHashMap<String, String>();

    private static void message(String message) {
        out.println(message);
        out.println("com.xmlns.baetle.sesame.Query [-s serverurl] [-d datadir] [-f format] [-h]");
        out.println("format is one of json, (todo default is xml for tuples, turtle for graphs)");
        out.println("if no option specified it will use -Daduna.platform.applicationdata.dir property");
        out.println("-s url of sesame sparql endpoint ");
        out.println("-d datadirectory if connecting to a local native store");
        out.println("-f format of results. ");
        System.out.println("-h print this message");
        System.exit(-1);
    }

    public Query(Repository repQ, String format) throws RepositoryException {

        repQ.initialize();
        if ("json".equals(format)) {
            tupleWriter = new SPARQLResultsJSONWriter(out);
        } else {
            tupleWriter = new SPARQLResultsXMLWriter(out);
        }
        lc = repQ.getConnection();
        f = repQ.getValueFactory();
        nameSpaces.put("", "http://baetle.googlecode.com/svn/ns/#");
        nameSpaces.put("sioc", "http://rdfs.org/sioc/ns#");
        nameSpaces.put("doap", "http://usefulinc.com/ns/doap#");
        nameSpaces.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        nameSpaces.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        nameSpaces.put("dct", "http://purl.org/dc/terms/");
    }


    public static void main(String[] args) throws Exception {
        String datadir = System.getProperty("aduna.platform.applicationdata.dir", "~/.aduna") + "/openrdf/server/native";
        Repository chosenRep = null;
        String format = null;

        for (int i = 0; i < args.length; i++) {
            if ("-s".equals(args[i].trim())) { //create http server
                String url = args[++i].trim();
                String endpoint = url.substring(0, url.lastIndexOf('/') - "/repositories".length());
                System.out.println("enpoint=" + endpoint);
                String repid = url.substring(url.lastIndexOf('/') + 1);
                System.out.println("repid=" + repid);
                chosenRep = new HTTPRepository(endpoint, repid) {
                    public void shutDown() throws RepositoryException {
                        //don't do anything on shutdown. We don't want to affect the server from here.
                    }
                };
                out.println("using repository at " + args[i].trim());
            } else if ("-h".equals(args[i])) {
                message("help:");
            } else if ("-f".equals(args[i])) {
                format = args[++i];
                //test the format is ok
            } else if ("-d".equals(args[i].trim())) {
                chosenRep = createFileRep(new File(args[++i].trim()));
            }
        }
        if (chosenRep == null) {  // try the defaults from the system properties
            chosenRep = createFileRep(new File(datadir));
        }


        Query q = new Query(chosenRep, format);

        String query = q.getQuery();
        if (query.contains("SELECT")) {
            q.evalTupleQuery(query);
        } else {
            q.evalGraphQuery(query);
        }

        q.close();
    }

    private void close() throws RepositoryException {
        lc.close();
    }

    private static SailRepository createFileRep(File dataDir) {
        if (!dataDir.exists()) message("dir does not exist" + dataDir);
        if (!dataDir.isDirectory()) message("can't find " + dataDir);

        NativeStore store = new NativeStore(dataDir);
        store.setTripleIndexes("spoc,sopc,posc,psoc,opsc,ospc");
        out.println("using store in directory " + dataDir);
        return new SailRepository(store);

    }

    private void evalGraphQuery(String query)
            throws MalformedQueryException, RepositoryException, TupleQueryResultHandlerException, QueryEvaluationException, RDFHandlerException {
        GraphQuery gq = lc.prepareGraphQuery(QueryLanguage.SPARQL, query);
        TurtleWriter res = new TurtleWriter(out);
        gq.evaluate(res);
    }


    private void evalTupleQuery(String query) throws MalformedQueryException, RepositoryException, TupleQueryResultHandlerException, QueryEvaluationException {
        TupleQuery tq = lc.prepareTupleQuery(QueryLanguage.SPARQL, query);
        tq.evaluate(tupleWriter);
    }

    private String getQuery() throws IOException, RepositoryException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(System.in));
        String line;
        String query = "";
        for(Map.Entry<String,String> ns: nameSpaces.entrySet()) {
            query += "PREFIX "+ns.getKey()+": <"+ns.getValue()+">\n";
            lc.setNamespace(ns.getKey(),ns.getValue());
        }
        System.out.println(query);
        while ((line = rd.readLine()) != null && line.length() != 0) {
            query += line + "\n";
        }
        return query;
    }

}
