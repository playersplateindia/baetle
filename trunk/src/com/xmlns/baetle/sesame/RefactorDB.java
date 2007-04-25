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

import static com.xmlns.baetle.svn.BaetleUtil.*;
import com.xmlns.baetle.svn.BaetleUtil;
import org.openrdf.model.*;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.nativerdf.NativeStore;

import java.io.File;
import java.io.OutputStream;
import static java.lang.System.exit;
import static java.lang.System.out;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A little class to do refactorings on the local database
 *
 * @author Henry Story
 */
public class RefactorDB {
    RepositoryConnection lc;
    ValueFactory f;
    File netbeansDB = new File("/Users/hjs/Programming/DataBases/NetBeans/sesame/openrdf/server/native");
    ArrayList<Statement> deleteStatements = new ArrayList<Statement>();
    ArrayList<Statement> addStatements = new ArrayList<Statement>();
    boolean doit = false;
    private NativeStore store;


    static void message(String message) {
        message(message,null);
    }

    static void message(String message, Exception e) {
        out.println(message);
        out.println("com.xmlns.baetle.sesame.RefactorDB [-s serverurl] [-d datadir] [-h] -doit refactorAlgorithm");
        out.println("if no option specified it will use -Daduna.platform.applicationdata.dir property");
        out.println("-s url of sesame sparql endpoint ");
        out.println("-d datadirectory if connecting to a local native store");
        out.println("-doit really apply the changes. Otherwise just write them to standard out");
        out.println("-f format of output (default turtle)");
        out.println("refactorAlgorithm just the class name relative to this package");
        out.println("-h print this message");
        if (e != null) e.printStackTrace(System.err);
        exit(-1);
    }


    public static void main(String[] args)  {
        Repository chosenRepository = null;
        String format = "turtle";
        boolean doit = false;
        Class<RefactorTask> executor = null;
        try {
            if (args.length==0) message("arguments required");
            for (int i = 0; i < args.length; i++) {
                if ("-s".equals(args[i].trim())) { //create http server
                    String url = args[++i].trim();
                    String endpoint = url.substring(0, url.lastIndexOf('/') - "/repositories".length());
                    System.out.println("# endpoint=" + endpoint);
                    String repid = url.substring(url.lastIndexOf('/') + 1);
                    System.out.println("# repid=" + repid);
                    chosenRepository = new HTTPRepository(endpoint, repid);
                    out.println("# using repository at " + args[i].trim());
                } else if ("-h".equals(args[i])) {
                    message("help:");
                } else if ("-format".equals(args[i])) {
                    format = args[++i];
                    //test the format is ok
                } else if ("-d".equals(args[i].trim())) {
                    chosenRepository = createFileRep(new File(args[++i].trim()));
                } else if ("-doit".equals(args[i].trim())) {
                    doit = true;
                } else {
                    String clazz = RefactorDB.class.getPackage().getName() + "." + args[i];
                    executor = (Class<RefactorTask>) RefactorDB.class.getClassLoader().loadClass(clazz);
                }
            }

            Constructor<RefactorTask> cons = executor.getConstructor();
            RefactorTask refactorTask = cons.newInstance();
            refactorTask.setRepository(chosenRepository);
            refactorTask.setFormat(format);
            refactorTask.setDoit(doit);
            refactorTask.run();
        } catch (Exception e) {
            message(e.getMessage(), e);
        }

    }


    private void extract(OutputStream out) throws RDFHandlerException, RepositoryException {
        lc.export(new NTriplesWriter(out));
    }


    private void findAllPropertiesFor(String[] ids) throws MalformedQueryException, RepositoryException, TupleQueryResultHandlerException, QueryEvaluationException, RDFHandlerException {
        String propsForQ = "CONSTRUCT { ?sub ?rel ?obj . } WHERE { ?sub ?rel ?obj .}";
        TurtleWriter tw = new TurtleWriter(out);
        for (String id : ids) {
            String q = propsForQ.replaceAll("\\?sub", "<" + id + ">");
            GraphQuery query = lc.prepareGraphQuery(QueryLanguage.SPARQL, q);

//            query.addBinding("sub",f.createURI(id));
            query.evaluate(tw);
            out.println("===");

        }

    }


    private void renameFoafNicToFoafAccount() throws RepositoryException {
        renameRelation(f.createURI(foaf + "nick"), f.createURI(foaf + "accountName"));
    }


    /**
     * the commits are linked to sioc users, but I did not name them initially. I could have done this easily
     * at the time since the nick can be used to create the username.
     */
    private void nameBlankUserNodes() throws RepositoryException {
        RepositoryResult<Statement> res = lc.getStatements(null, f.createURI(foaf + "nick"), null, false);
        while (res.hasNext()) {
            Statement user = res.next();
            String nick = user.getObject().toString();
            URI newname = f.createURI("http://netbeans.org/people/" + nick);
            smush(newname, user.getSubject());
        }

    }


    String findAllAccountsWithSameEmail = "PREFIX sioc: <http://rdfs.org/sioc/ns#>\n" +
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
            "PREFIX baetle: <http://xmlns.com/baetle/#>\n" +
            "SELECT DISTINCT *\n" +
            "WHERE {\n" +
            "   ?p1 sioc:email ?email .\n" +
            "   ?p2 sioc:email ?email .\n" +
            "   FILTER ( ?p1 != ?p2 && str(?p1) < str(?p2))\n" +
            "} LIMIT 50";


    private void cleanMBoxStrings() throws MalformedQueryException, RepositoryException, QueryEvaluationException {
        String cleanMBoxStrings = "" +
                "PREFIX sioc: <http://rdfs.org/sioc/ns#>\n" +
                "CONSTRUCT { ?s sioc:email ?m . }\n" +
                "WHERE { \n" +
                "    ?s sioc:email ?m ;\n" +
                "    FILTER ( ! REGEX(str(?m), \"mailto:[a-z,A-Z,0-9].*@.*\") )\n" +
                "} \n";
        GraphQuery gq = lc.prepareGraphQuery(QueryLanguage.SPARQL, cleanMBoxStrings);
        GraphQueryResult res = gq.evaluate();
        ArrayList<Statement> delete = new ArrayList<Statement>(100);
        ArrayList<Statement> add = new ArrayList<Statement>(100);
        while (res.hasNext()) {
            Statement statement = res.next();
            Resource mailto = (Resource) statement.getObject();
            String ms = mailto.toString();
            out.println(ms);
            if (ms.matches("mailto:\\s.*")) {
                out.println("toreplace " + ms);
                ms = "mailto:" + ms.substring(7).trim();
                add.add(f.createStatement(statement.getSubject(), statement.getPredicate(), f.createURI(ms)));
                delete.add(statement);
            }
        }
        lc.remove(delete);
        lc.add(add);

    }

    private void removeBrokenMboxes() throws MalformedQueryException, RepositoryException, QueryEvaluationException {
        String cleanMBoxStrings = "" +
                "PREFIX sioc: <http://rdfs.org/sioc/ns#>\n" +
                "CONSTRUCT { ?s sioc:email ?m . }\n" +
                "WHERE { \n" +
                "    ?s sioc:email ?m .\n" +
                "    FILTER ( ! REGEX(str(?m), \"mailto:.*@.*\") )\n" +
                "} \n";
        GraphQuery gq = lc.prepareGraphQuery(QueryLanguage.SPARQL, cleanMBoxStrings);
        GraphQueryResult res = gq.evaluate();
        while (res.hasNext()) {
            Statement statement = res.next();
            out.println(statement);
            if (!lc.hasStatement(null, null, statement.getSubject(), false)) {
                out.println("removing");
                RepositoryResult<Statement> remove = lc.getStatements(statement.getSubject(), null, null, false);
                while (remove.hasNext())
                    deleteStatements.add(remove.next());

            }
        }
    }

    /**
     * I had initally named users with urls such as http://netbeans.org/people/69020, but it will be easier to
     * merge different people if one just uses the account of the person
     * eg http://netbeans.org/people/rgreig
     */
    void renameUSers() throws MalformedQueryException, RepositoryException, QueryEvaluationException {
        String query = "" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "CONSTRUCT { ?s foaf:accountName ?o . }\n" +
                "WHERE { \n" +
                "    ?s foaf:accountName ?o .\n" +
                "}";
        RepositoryResult<Statement> accounts = lc.getStatements(null, f.createURI(foaf + "accountName"), null, false);
        while (accounts.hasNext()) {
            Statement account = accounts.next();
            String aurl = account.getSubject().toString();
            out.print("replaceing " + aurl);
            if (aurl.matches("http://.*/[0-9]+")) {
                String aname = account.getObject().toString();
                int atloc = aname.indexOf('@');
                if (atloc < 0) atloc = aname.length();
                aname = aname.substring(0, atloc);
                int idloc = aurl.lastIndexOf('/') + 1;
                aurl = aurl.substring(0, idloc) + aname;
                out.println(" with " + aurl);
                smush(f.createURI(aurl), account.getSubject());
            }
        }

    }

    /**
     * we don't need to keep email addresses in the rdf db
     */
    void deletelAllUserInfo() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        String allUserInfo = "" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX sioc: <http://rdfs.org/sioc/ns#>\n" +
                "CONSTRUCT { ?s ?r ?o . }\n" +
                "WHERE { \n" +
                "    ?s foaf:accountName ?m ;\n" +
                "       ?r ?o . \n" +
                "}";
        GraphQuery gq = lc.prepareGraphQuery(QueryLanguage.SPARQL, allUserInfo);
        GraphQueryResult res = gq.evaluate();
        while (res.hasNext()) {
            Statement s = res.next();
            deleteStatements.add(s);
            addStatements.add(f.createStatement(s.getSubject(), RDF.TYPE, f.createURI(sioc + "User")));
        }
    }

    /**
     * Replace all resources with the one specified by keep
     *
     * @param keep
     * @param remove
     * @throws RepositoryException
     */
    void smush(Resource keep, Resource... remove) throws RepositoryException {
        for (Resource kill : remove) {
            RepositoryResult<Statement> relsTo = lc.getStatements(null, null, (Value) kill, false);
            while (relsTo.hasNext()) {
                Statement s = relsTo.next();
                deleteStatements.add(s);
                addStatements.add(f.createStatement(s.getSubject(), s.getPredicate(), keep));
            }
            RepositoryResult<Statement> relsFrom = lc.getStatements(kill, null, null, false);
            while (relsFrom.hasNext()) {
                Statement s = relsFrom.next();
                deleteStatements.add(s);
                addStatements.add(f.createStatement(keep, s.getPredicate(), s.getObject()));
            }
        }

    }

    private void renameRelation(URI oldRelation, URI newRelation) throws RepositoryException {
        RepositoryResult<Statement> res = lc.getStatements(null, oldRelation, null, false);
        while (res.hasNext()) {
            Statement statement = res.next();
            deleteStatements.add(statement);
            addStatements.add(f.createStatement(statement.getSubject(), newRelation, statement.getObject()));
        }
    }


    public void renameSiocAccounts() throws RepositoryException {
        RepositoryResult<Statement> accounts = lc.getStatements(null, f.createURI("http://rdfs.org/sioc/ns#accountName"), null, false);
        HashMap<Resource, Resource> replaceMap = new HashMap();

        while (accounts.hasNext()) {
            Statement rel = accounts.next();
            rel.getObject();

        }


    }

    String removeAllAccountsThatAreNotLinkedTo;


    private void refactorDuplicateAccounts() {
        String usersQS1 = "" +
                "PREFIX sioc: <http://rdfs.org/sioc/ns#>\n" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT * " +
                "WHERE {" +
                "    ?p2 a sioc:User;\n" +
                "        foaf:accountName ?nm;" +
                "        foaf:mbox ?mbox ." +
                "}";


    }


    private void linkCommitsToPeople() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        String duplicateCommitsQ = "PREFIX sioc: <http://rdfs.org/sioc/ns#>\n" +
                "PREFIX dc: <http://purl.org/dc/elements/1.1/> \n" +
                "PREFIX baetle: <http://xmlns.com/baetle/#>\n" +
                "SELECT * WHERE {\n" +
                "  ?cmt1  a   baetle:Committing;\n" +
                "         sioc:content ?c1.\n" +
                "  ?cmt2  a   baetle:Committing;\n" +
                "         sioc:content ?c1.\n" +
                "} " +
                "LIMIT 5";
        String commitersQS = "" +
                "PREFIX sioc: <http://rdfs.org/sioc/ns#>\n" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX baetle: <http://xmlns.com/baetle/#>\n" +
                "SELECT * \n" +
                "WHERE {\n" +
                "    [] a baetle:Committing;\n" +
                "       sioc:has_creator ?p .\n" +
                "    ?p foaf:nick ?nick .\n" +
                "}\n" +
                "LIMIT 100";
        String usersQS1 = "" +
                "PREFIX sioc: <http://rdfs.org/sioc/ns#>\n" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT * " +
                "WHERE {" +
                "    ?p2 a sioc:User;\n" +
                "        foaf:accountName ?nm .\n" +
                "     FILTER (REGEX(?nm,\"^";
        String usersQS2 = "\"))\n" +
                "} LIMIT 10 ";


        TupleQuery commitersQ = lc.prepareTupleQuery(QueryLanguage.SPARQL, commitersQS);
        TupleQuery usersQ;

        TupleQueryResult commtersRes = commitersQ.evaluate();
        while (commtersRes.hasNext()) {
            BindingSet bs = commtersRes.next();
            Binding nick = bs.getBinding("nick");
            out.println("creator p=" + bs.getBinding("p") + " nick = " + nick.getValue());
            String query = usersQS1 + nick.getValue() + usersQS2;
            usersQ = lc.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult usersRes = usersQ.evaluate();
            while (usersRes.hasNext()) {
                BindingSet bsu = usersRes.next();
                Binding nameB = bsu.getBinding("nm");
                out.println("user=" + bsu.getBinding("p2") + " name=" + nameB.getValue());
                if (nameB.getValue().toString().endsWith("neatbeans.org.netbeans.org")) {

                }
            }


        }
    }
}


/**
 * go through a tree of ?a :previous ?b relations, starting at the root
 * and give each element in the tree the same id.
 */
class LinkToOrigin extends RefactorTask {
    private URI previousRel;

    public LinkToOrigin() {
        super();
    }
    URI originrel;

    /**
     * extend this task with your code
     */
    public void runtask() throws Exception {
        originrel = f.createURI(baetle + "origin");
        previousRel = f.createURI(baetle, "previous");
        ArrayList<Resource> origins = findOrigins();
        for (Resource o: origins) {
            recurseOrigin(o,o);
        }
    }

    private ArrayList<Resource> findOrigins() throws MalformedQueryException, RepositoryException, QueryEvaluationException {
        String q = BaetleUtil.getPrefixes()+
                "SELECT ?o \n" +
                "WHERE {\n" +
                "   ?x :previous ?o .\n" +
                "   OPTIONAL { ?o :previous ?none . }\n" +
                "   FILTER (! bound(?none)) \n" +
                "}";
        TupleQuery query = lc.prepareTupleQuery(QueryLanguage.SPARQL, q);
        TupleQueryResult results = query.evaluate();
        ArrayList<Resource> result = new ArrayList<Resource>();
        while(results.hasNext()) {
            BindingSet bindSet = results.next();
            Binding binding = bindSet.getBinding("o");
            Value value = binding.getValue();
            result.add((Resource) value);
        }
        return result;
    }

    /* this does not work because the data in the repository is not complete. That is there is not always a link
      from the origina through to the next versions
    public xxx findOrigins() throws RepositoryException {
        originrel = f.createURI(baetle + "origin");
        previousRel = f.createURI(baetle, "previous");
        RepositoryResult<Statement> originstatmts = lc.getStatements(null, f.createURI(baetle + "added") , null, false);
        while (originstatmts.hasNext()) {
            //for every origin move up the graph and have it point to its origin
            Resource origin = (Resource) originstatmts.next().getObject();
            recurseOrigin(origin, origin);
        }
    }
    */

    private void recurseOrigin(Value origin, Resource version) throws RepositoryException {
        addStatement(f.createStatement(version, originrel, origin));
        RepositoryResult<Statement> nextQuery = lc.getStatements(null, previousRel, version, false);
        while (nextQuery.hasNext()) {
            Resource next = nextQuery.next().getSubject();
            recurseOrigin(origin, next);
        }
    }


}
