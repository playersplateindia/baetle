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

import org.openrdf.model.*;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.nativerdf.NativeStore;

import java.io.File;
import java.io.OutputStream;
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
    private String foaf = "http://xmlns.com/foaf/0.1/";
    private String  sioc = "http://rdfs.org/sioc/ns#";


    public RefactorDB(boolean doit) throws RepositoryException {
        this.doit = doit;
        NativeStore store = new NativeStore(netbeansDB);
        store.setTripleIndexes("spoc,sopc,posc,psoc,opsc,ospc");
        Repository myRepository = new SailRepository(store);

        //Repository myRepository = new HTTPRepository(server, id);
        myRepository.initialize();
        lc = myRepository.getConnection();
        f = myRepository.getValueFactory();
    }

    public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, RDFHandlerException {
//      String sesameServer = " http://localhost:8080/openrdf/";
        String repositoryID = "native";
        RefactorDB work = new RefactorDB(true);

        //work.removeBrokenMboxes();
        //work.cleanMBoxStrings();
        /*refactorDuplicatePeople(lc);
        work.findAllPropertiesFor(new String[]{"http://netbeans.org/people/1020",
                "http://netbeans.org/people/14202",
                "http://netbeans.org/people/14203",
                "http://netbeans.org/people/67227",
                "http://netbeans.org/people/67610",
                "http://netbeans.org/people/67611",
                "http://netbeans.org/people/68748"});*/
        //work.renameUSers();
        //work.deletelAllUserInfo();
        //work.nameBlankUserNodes();
        //work.renameFoafNicToFoafAccount();
        work.extract(System.out);
        work.close();
    }

    private void close() throws RepositoryException {
        if (doit) {
            try {
                lc.setAutoCommit(false);
                lc.add(addStatements);
                lc.remove(deleteStatements);
                lc.commit();
            } finally {
                lc.setAutoCommit(true);
            }
        } else {//talk about it
            for (Statement st : deleteStatements)
                System.out.println("delete: " + st);
            for (Statement st : addStatements)
                System.out.println("add:" + st);
        }
        lc.close();
    }

    private void extract(OutputStream out) throws RDFHandlerException, RepositoryException {
        lc.export(new NTriplesWriter(out) );
    }
    

    private void findAllPropertiesFor(String[] ids) throws MalformedQueryException, RepositoryException, TupleQueryResultHandlerException, QueryEvaluationException, RDFHandlerException {
        String propsForQ = "CONSTRUCT { ?sub ?rel ?obj . } WHERE { ?sub ?rel ?obj .}";
        TurtleWriter tw = new TurtleWriter(System.out);
        for (String id : ids) {
            String q = propsForQ.replaceAll("\\?sub", "<" + id + ">");
            GraphQuery query = lc.prepareGraphQuery(QueryLanguage.SPARQL, q);

//            query.addBinding("sub",f.createURI(id));
            query.evaluate(tw);
            System.out.println("===");

        }

    }


    private void renameFoafNicToFoafAccount() throws RepositoryException {
         renameRelation(f.createURI(foaf+"nick"),f.createURI(foaf+"accountName"));
    }


    /**
     * the commits are linked to sioc users, but I did not name them initially. I could have done this easily
     * at the time since the nick can be used to create the username.
     */
    private void nameBlankUserNodes() throws RepositoryException {
        RepositoryResult<Statement> res = lc.getStatements(null, f.createURI(foaf + "nick"), null, false);
        while(res.hasNext()) {
            Statement user = res.next();
            String nick = user.getObject().toString();
            URI newname = f.createURI("http://netbeans.org/people/" + nick);
            smush(newname,user.getSubject());            
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
            System.out.println(ms);
            if (ms.matches("mailto:\\s.*")) {
                System.out.println("toreplace " + ms);
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
            System.out.println(statement);
            if (!lc.hasStatement(null, null, statement.getSubject(), false)) {
                System.out.println("removing");
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
        RepositoryResult<Statement> accounts = lc.getStatements(null, f.createURI(foaf+"accountName"), null, false);
        while (accounts.hasNext()) {
            Statement account = accounts.next();
            String aurl = account.getSubject().toString();
            System.out.print("replaceing " + aurl);
            if (aurl.matches("http://.*/[0-9]+")) {
                String aname = account.getObject().toString();
                int atloc = aname.indexOf('@');
                if (atloc < 0) atloc = aname.length();
                aname = aname.substring(0, atloc);
                int idloc = aurl.lastIndexOf('/') + 1;
                aurl = aurl.substring(0, idloc) + aname;
                System.out.println(" with " + aurl);
                smush(f.createURI(aurl), account.getSubject());
            }
        }

    }

    /**
     * we don't need to keep email addresses in the rdf db
     */
    void deletelAllUserInfo() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        String allUserInfo =""+
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
            addStatements.add(f.createStatement(s.getSubject(),RDF.TYPE, f.createURI(sioc+"User")));
        }
    }

    /**
     * Replace all resources with the one specified by keep
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
        while(res.hasNext()) {
            Statement statement = res.next();
            deleteStatements.add(statement);
            addStatements.add(f.createStatement(statement.getSubject(),newRelation,statement.getObject()));
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
            System.out.println("creator p=" + bs.getBinding("p") + " nick = " + nick.getValue());
            String query = usersQS1 + nick.getValue() + usersQS2;
            usersQ = lc.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult usersRes = usersQ.evaluate();
            while (usersRes.hasNext()) {
                BindingSet bsu = usersRes.next();
                Binding nameB = bsu.getBinding("nm");
                System.out.println("user=" + bsu.getBinding("p2") + " name=" + nameB.getValue());
                if (nameB.getValue().toString().endsWith("neatbeans.org.netbeans.org")) {

                }
            }


        }
    }
}
