/*
 New BSD license: http://opensource.org/licenses/bsd-license.php

 Copyright (c) 2007 Sun Microsystems, Inc.
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
package com.xmlns.baetle.svn;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

import java.util.Map;
import java.util.LinkedHashMap;
import java.io.File;
import static java.lang.System.out;

/**
 * @author Henry Story
 */
public class BaetleUtil {
    public static final String baetle = "http://baetle.googlecode.com/svn/ns/#";
    public final static String xsd = "http://www.w3.org/2001/XMLSchema#";
    public final static String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
    public final static String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    public final static String doap = "http://usefulinc.com/ns/doap#";
    public final static String sioc = "http://rdfs.org/sioc/ns#";
    public final static String dct= "http://purl.org/dc/terms/";
    public final static String foaf = "http://xmlns.com/foaf/0.1/";

    static LinkedHashMap<String, String> nameSpaces = new LinkedHashMap<String, String>();

    static {
        nameSpaces.put("", baetle);
        nameSpaces.put("sioc", sioc);
        nameSpaces.put("doap", doap);
        nameSpaces.put("rdfs", rdfs);
        nameSpaces.put("rdf", rdf);
        nameSpaces.put("xsd", xsd);
        nameSpaces.put("dct", dct);
    }

    /**
     * get the SPARQL PREFIX for all defined prefixes
     * @return
     * @throws RepositoryException
     */
    public static String getPrefixes()  {
        String query = "";
        for(Map.Entry<String,String> ns: nameSpaces.entrySet()) {
            query += "PREFIX "+ns.getKey()+": <"+ns.getValue()+">\n";
        }
        return query;
    }

    /**
     * get the SPARQL PREFIX for the gives prefixes
     * @param prefixes
     * @return
     */
    public static String getPrefixes(String... prefixes)  {
        String query = "";
        for(String prefix: prefixes) {
            String val = nameSpaces.get(prefix);
            if (val == null) throw new NullPointerException("no such prefix defined");
            query += "PREFIX "+prefix+": <"+val+">\n";
        }
        return query;
    }

    /**
     * set the prefixes on the connection. This can affect the output received
     * @param repositoryConnection
     * @throws RepositoryException
     */
    public static void setPrefixes(RepositoryConnection repositoryConnection) throws RepositoryException {
        for(Map.Entry<String,String> ns: nameSpaces.entrySet()) {
            repositoryConnection.setNamespace(ns.getKey(),ns.getValue());
        }
    }

    public static SailRepository createFileRep(File dataDir) throws Exception {
        if (!dataDir.exists()) throw new Exception("dir does not exist" + dataDir);
        if (!dataDir.isDirectory()) throw new Exception("can't find " + dataDir);

        NativeStore store = new NativeStore(dataDir);
        store.setTripleIndexes("spoc,sopc,posc,psoc,opsc,ospc");
        out.println("using store in directory " + dataDir);
        return new SailRepository(store);

    }


}
