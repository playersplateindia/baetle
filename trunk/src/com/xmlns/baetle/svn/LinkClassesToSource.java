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

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.parser.ParseException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.*;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.SailInitializationException;
import org.openrdf.sail.memory.MemoryStore;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Given a list of ntriples files which together describe
 *  - the release jars and classes
 *  - the source file locations
 * work out how the classes in the jars relate to the java source by analysing the source files
 * downloaded from the remote repository, and working on patter matching names of files
 *
 * todo: there are hardcoded urls in here which means this will only work with sesame2 beta 3
 * todo: make this more generic
 *
 * for some background info on the algorithm see the thread "Extracting class info from source, and relating them"
 * on the google groups mailing list
 * http://groups.google.com/group/baetle/browse_thread/thread/fdf0f8419d8f603a
 *
 * Overview of algorithm (might no longer be in sync with what is happening
 *  1. find all java source files that belong to a release
 *  2. find the binary that belongs to that release,
 *           all the jars and all the class files
 *
 *   3. analyse the source files and find out which classes they contain
 *   4. link the classes to the source files:
 *       <xxx.class> baetle:built_from <xxx.java> .
 *
 *
 * @author Henry Story
 */
public class LinkClassesToSource {
    static final String baetle = "http://baetle.googlecode.com/svn/ns/#";
    final static String xsd = "http://www.w3.org/2001/XMLSchema#";
    final static String rdfs = "http://www.w3.org/2000/01/rdf-schema#";

    RepositoryConnection lc;
    ValueFactory f;
    TupleQueryResultWriter tupleWriter;
    Repository repQ;
    private MemoryStore store;
    private RepositoryConnection conn;
    String releaseUrl = "https://src.aduna-software.org/svn/org.openrdf/releases/sesame2/2.0-beta3";
    String baseSvnUrl = "https://src.aduna-software.org/svn/org.openrdf/";
    String releaseJar = "http://downloads.sourceforge.net/sesame/openrdf-sesame-2.0-beta3-onejar.jar";

    private SVNRepository repository;
    private String regex;

    void usage(String err, Exception e) {
        System.out.println(err);
        System.exit(-1);
    }

    public static void main(String[] args) throws RepositoryException, SailInitializationException, IOException, RDFParseException, MalformedQueryException, QueryEvaluationException {
        LinkClassesToSource linker = new LinkClassesToSource();
        linker.load(args);
        System.out.println("finished loading");
        linker.run();
    }

    private void run() throws MalformedQueryException, RepositoryException, QueryEvaluationException, MalformedURLException {
        //note: only using subversion here to avoid https issues. URLConnections if properly set up should work just as well
        try {
            DAVRepositoryFactory.setup();
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(baseSvnUrl));
        } catch (SVNException svne) {
            usage("error while creating an SVNRepository for the location " + baseSvnUrl, svne);
        }
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager("anonymous", "anonymous");
        repository.setAuthenticationManager(authManager);

        ArrayList<String> classUrls = getAllClassFilesForRelease(releaseJar);
        System.out.println("found results " + classUrls.size());

        ArrayList<URL> jsources = getAllJavaSourceFilesForRelease(releaseUrl);
        System.out.println("found results " + jsources.size());
        for (URL source : jsources) {
            ArrayList<String> clist = getClassesIn(source);

            classlist:
            for (String cpath : clist) {
                for (ListIterator<String> curli = classUrls.listIterator(); curli.hasNext();) {
                    String curl = curli.next();
                    if (curl.endsWith(cpath)) {
                        System.out.println("<" + curl + "> <" + baetle + "builtFrom> <" + source.toString() + "> .");
                        curli.remove();
                        continue classlist;
                    }
                }
                System.out.println("#no mapping for " + cpath);
            }
        }
        System.out.println("# number of class urls unmapped:" + classUrls.size());
        System.out.println("# probably anonymous ones. Trying now");
        for (ListIterator<String> curli = classUrls.listIterator(); curli.hasNext();) {
            String classurl = curli.next();
            for (Map.Entry<String, URL> path2source : topClassPathToSourceUrl.entrySet()) {
                regex = ".*"+path2source.getKey() + "[$][0-9]+.class$";
                if (classurl.matches(regex)) {
                    System.out.println("<" + classurl + "> <" + baetle + "builtFrom> <" + path2source.getValue() + "> .");
                    curli.remove();
                }
            }
        }
        System.out.println("# number of class urls unmapped:" + classUrls.size());
        for (String left : classUrls) {
            System.out.println("# no map for: " + left);
        }
    }


    JavaDocBuilder builder = new JavaDocBuilder();

    HashMap<String, URL> topClassPathToSourceUrl;

    /**
     * Find all the class paths+names that are defined in the java source file
     *
     * @return a list of paths such as "com.xmlns.baetle.svn.LinkClassesToSource"
     */
    private ArrayList<String> getClassesIn(URL javaSource) {
        ArrayList<String> result = new ArrayList<String>();
        builder = new JavaDocBuilder();
        try {
            ByteArrayOutputStream outarr = new ByteArrayOutputStream();
            //just using repository here because I don't want to waste time looking for libraries to deal with https
            String s = javaSource.toString().substring(baseSvnUrl.length() - 1);
            repository.getFile(s, -1, null, outarr);
            try {
                builder.addSource(new StringReader(outarr.toString()));
            } catch (ParseException e) {
                System.out.println("#oops parse exception for "+javaSource);
                e.printStackTrace(System.err);
                return result;
            }
            for (JavaClass cl : builder.getClasses()) {
                String cn = cl.asType().toString().replace('.', '/') + ".class";
                result.add(cn);
                if (s.endsWith(cl.asType().toString().replace('.', '/') + ".java")) {
                    topClassPathToSourceUrl.put(cl.asType().toString(), javaSource);
                }
            }
        } catch (SVNException e) {
            e.printStackTrace(System.err);  //todo: decide what exception to throw
        }
        return result;
    }

    private ArrayList<String> getAllClassFilesForRelease(String releaseJar) throws MalformedQueryException, RepositoryException, QueryEvaluationException, MalformedURLException {
        String sparql = "" +
                "PREFIX baetle: <" + baetle + ">\n" +
                "SELECT ?class\n" +
                "WHERE {\n" +
                "        <" + releaseJar + "> baetle:contains ?jar .\n" +
                "        ?jar baetle:contains ?class .\n" +
                "        FILTER REGEX( STR(?class), '[.]class$') \n" +
                "}";
        System.err.println("sparql=" + sparql);
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
        TupleQueryResult qr = query.evaluate();
        ArrayList<String> results = new ArrayList<String>();
        System.out.println("query returned results in the number of " + results.size());
        while (qr.hasNext()) {
            BindingSet bindingSet = qr.next();
            Binding binding = bindingSet.getBinding("class");
            Value value = binding.getValue();
            if (value instanceof URI) {
                results.add(value.toString()); //we assume all of these are URIs, no blank nodes.
            } else {
                System.err.println("oops a blank node:" + value);
            }
        }
        return results;

    }

    private ArrayList<URL> getAllJavaSourceFilesForRelease(String releaseUrl) throws MalformedQueryException, RepositoryException, QueryEvaluationException, MalformedURLException {
        String sparql = "" +
                "PREFIX baetle: <" + baetle + ">\n" +
                "SELECT ?java\n" +
                "WHERE {\n" +
                "        <" + releaseUrl + "> baetle:contains ?java .\n" +
                "        FILTER REGEX( STR(?java), '[.]java$') \n" +
                "}";
        System.err.println("sparql=" + sparql);
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
        TupleQueryResult qr = query.evaluate();
        ArrayList<URL> results = new ArrayList<URL>();
        System.out.println("query returned results in the number of " + results.size());
        while (qr.hasNext()) {
            BindingSet bindingSet = qr.next();
            Binding binding = bindingSet.getBinding("java");
            Value value = binding.getValue();
            if (value instanceof URI) {
                results.add(new URL(value.toString())); //we assume all of these are URIs, no blank nodes.
            }
        }
        return results;
    }

    public LinkClassesToSource() throws SailInitializationException, RepositoryException {
        store = new MemoryStore();
        repQ = new SailRepository(store);
        store.initialize();
        conn = repQ.getConnection();

        topClassPathToSourceUrl = new HashMap<String, URL>();
    }

    void load(String... files) throws IOException, RDFParseException, RepositoryException {
        for (String fs : files) {
            File f = new File(fs);
            conn.add(f, (String) null, RDFFormat.NTRIPLES);
        }
        System.err.println("now graph contains " + conn.size());
    }


}
