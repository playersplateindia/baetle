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

/**
 * @author Henry Story
 */
public class Query {
    RepositoryConnection lc;
    ValueFactory f;

    private static void message(String message) {
        System.out.println(message);
        System.out.println("org.com.xmlns.baetle.sesame.Query [-s serverurl] [-d datadir]");
        System.out.println("if no option specified it will use -Daduna.platform.applicationdata.dir property");
        System.exit(-1);
    }

    public Query(Repository repQ) throws RepositoryException {

//        Repository repQ = new HTTPRepository(server, id);
        repQ.initialize();
        lc = repQ.getConnection();
        f = repQ.getValueFactory();
    }


    public static void main(String[] args) throws Exception {
        String datadir = System.getProperty("aduna.platform.applicationdata.dir","~/.aduna") + "/openrdf/server/native";
        Repository chosenRep = null;

        if (args.length > 0 && "-s".equals(args[0].trim())) { //create http server
            chosenRep = new HTTPRepository(args[1].trim(), "native");
            System.out.println("using repository at " + args[1].trim());
        } else if (args.length > 0 && "-h".equals(args[0])) {
            message("help:");
        } else {  // a file repository

            File dataDir;
            if (args.length > 0 && "-d".equals(args[0].trim())) {
                dataDir = new File(args[1].trim());
            } else {
                dataDir = new File(datadir);
            }
            if (!dataDir.exists()) message("dir does not exist" + dataDir);
            if (!dataDir.isDirectory()) message("can't find " + dataDir);

            NativeStore store = new NativeStore(dataDir);
            store.setTripleIndexes("spoc,sopc,posc,psoc,opsc,ospc");
            chosenRep = new SailRepository(store);

            System.out.println("using store in directory " + dataDir);

        }


        Query q = new Query(chosenRep);

        String query = q.getQuery();
        if (query.contains("SELECT")) {
            q.evalTupleQuery(query);
        } else {
            q.evalGraphQuery(query);
        }
    }

    private void evalGraphQuery(String query)
            throws MalformedQueryException, RepositoryException, TupleQueryResultHandlerException, QueryEvaluationException, RDFHandlerException {
        GraphQuery gq = lc.prepareGraphQuery(QueryLanguage.SPARQL, query);
        TurtleWriter res = new TurtleWriter();
        res.setOutputStream(System.out);
        gq.evaluate(res);
    }


    private void evalTupleQuery(String query) throws MalformedQueryException, RepositoryException, TupleQueryResultHandlerException, QueryEvaluationException {
        TupleQuery tq = lc.prepareTupleQuery(QueryLanguage.SPARQL, query);
        SPARQLResultsXMLWriter res = new SPARQLResultsXMLWriter();
        res.setOutputStream(System.out);
        tq.evaluate(res);
    }

    private String getQuery() throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(System.in));
        String line;
        String query = "";
        while ((line = rd.readLine()) != null && line.length() != 0) {
            query += line;
        }
        return query;
    }

}
