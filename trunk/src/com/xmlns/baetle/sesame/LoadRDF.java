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

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import java.io.File;
import java.io.IOException;

/**
 * @author Henry Story
 */
public class LoadRDF {
    static File dataDir;
    static File db;

    public static void main(String[] args) throws RepositoryException, IOException, RDFParseException {
        for (int i = 0; i < args.length; i++) {
            if (args[i].trim().equals("-d")) {
                dataDir = new File(args[++i]);
                if (!dataDir.isDirectory() || !dataDir.exists()) message("can't find data dir "+dataDir);
            } else {
                db = new File(args[i]);
                if (!db.exists()) message("missing ntriples file "+db);
                if (!db.isFile()) message("should be a file "+db);
            }
        }
        if (db == null) message("missing triples file to import");

        NativeStore store = new NativeStore(dataDir);
        store.setTripleIndexes("spoc,sopc,posc,psoc,opsc,ospc");
        Repository netb = new SailRepository(store);
        netb.initialize();

        RepositoryConnection nc = netb.getConnection();
        System.out.println("starting import of "+db);
        nc.add(db,null, RDFFormat.NTRIPLES);
        System.out.println("finished checkins");

        nc.close();
        netb.shutDown();
    }

    private static void message(String message) {
        System.out.println(message);
        System.out.println("LoadRDF -d databaseDir/ file.ntriples");
        System.exit(-1);
    }
}
