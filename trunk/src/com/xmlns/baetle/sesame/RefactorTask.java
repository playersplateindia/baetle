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
package com.xmlns.baetle.sesame;

import com.xmlns.baetle.svn.BaetleUtil;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;

import static java.lang.System.out;
import static java.lang.System.exit;
import java.util.ArrayList;

/**
 * @author Henry Story
 */
public abstract class RefactorTask {
    Repository sesmeRepo;
    private String format;
    private boolean doit;
    RepositoryConnection lc;
    protected ValueFactory f;
    private ArrayList<Statement> deleteStatements = new ArrayList<Statement>();
    private ArrayList<Statement> addStatements = new ArrayList<Statement>();
    private boolean debug = false;

    public RefactorTask() {
    }

    String getArgs() { return "\n"; }

    String getMessage() { return ""; }
    
    protected void message(String message) {
         message(message,null);
    }

    void message(String message, Exception e) {
         out.println(message);
         out.println(getArgs()+getMessage());
         if (e != null) e.printStackTrace(System.err);
         exit(-1);
     }
    

    void setSesmeRepo(Repository rep) throws RepositoryException {
        this.sesmeRepo = rep;
        sesmeRepo.initialize();
        lc = sesmeRepo.getConnection();
        f = sesmeRepo.getValueFactory();
    }

    void setFormat(String format) {
        this.format = format;
    }

    void setDoit(boolean doit) {
        this.doit = doit;
    }

    public void run() throws Exception {
        runtask();
        close();
    }

    /**
     * extend this task with your code
     */
    protected abstract void runtask() throws Exception;

    /**
     * put statement on list to be added
     *
     * @param s
     */
    protected void addStatement(Statement s) {
        if (debug) System.out.println(s);
        addStatements.add(s);
    }

    /**
     * put statement on list to be deleted
     *
     * @param s
     */
    protected void delStatement(Statement s) {
        addStatements.add(s);
    }


    private void close() throws RepositoryException, SailException, RDFHandlerException {
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

            RDFWriter writer = getWriter();
            MemoryStore mem = new MemoryStore();
            mem.initialize();
            BaetleUtil.setPrefixes(lc);
            SailRepository sail = new SailRepository(mem);
            SailRepositoryConnection sailconn = sail.getConnection();
            BaetleUtil.setPrefixes(sailconn);
            sailconn.add(addStatements);
            out.println();
            out.println();
            writer.handleComment("Add Statements");
            out.println();
            out.println();
            sailconn.export(writer);


            sailconn.clear();
            sailconn.add(deleteStatements);
            out.println();
            out.println();
            writer.handleComment("Delete STatements");
            out.println();
            out.println();
            sailconn.export(writer);

        }
        lc.close();
        sesmeRepo.shutDown();
    }

    private RDFWriter getWriter() {
        if ("ntriples".equals(format)) {
            return new NTriplesWriter(System.out);
        } else if ("rdfxml".equals(format)) {
            return new RDFXMLWriter(System.out);
        }
        return new TurtleWriter(System.out);

    }

    /**
     * Do nothing by default
     * extend if needed
     * @param strings
     */
    public void setArgs(String[] strings) {
    }

    public void setDebug(boolean val) {
        debug = val;
    }
}
