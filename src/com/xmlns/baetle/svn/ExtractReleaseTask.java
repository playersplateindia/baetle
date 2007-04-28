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

import static com.xmlns.baetle.svn.BaetleUtil.awol;
import static com.xmlns.baetle.svn.BaetleUtil.baetle;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.io.SVNFileRevision;

import static java.lang.System.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Henry Story
 */
public class ExtractReleaseTask extends SvnTask {
    private String svnBaseUrl;
    private long latestRevision = -1;
    String repoRoot;
    private URI releaseURI;
    private URI modifiedRel;
    private URI originRel;
    private URI containsRel;
    private URI collectionClass;
    private URI entryClass;
    private URI previousRel;
    private URI entryRel;


    /**
     * extend this task with your code
     */
    public void runtask() throws Exception {
        try {
            /*
             * Checks up if the specified path/to/repository part of the URL
             * really corresponds to a directory. If doesn't the program exits.
             * SVNNodeKind is that one who says what is located at a path in a
             * revision. -1 means the latest revision.
             */
            SVNNodeKind nodeKind = svnRepo.checkPath("", -1);
            if (nodeKind == SVNNodeKind.NONE) {
                message("There is no entry at '" + svnRepo.getLocation() + "'.");
            } else if (nodeKind == SVNNodeKind.FILE) {
                message("The entry at '" + svnRepo.getLocation() + "' is a file while a directory was expected.");
            }
            /*
            * Gets the latest revision number of the repository
            */
            try {
                latestRevision = svnRepo.getLatestRevision();
            } catch (SVNException svne) {
                message("error while fetching the latest repository revision: "
                        + svne.getMessage());
            }

            repoRoot = svnRepo.getRepositoryRoot(true).toString();
            releaseURI = f.createURI(svnRepo.getLocation().toString());
            modifiedRel = f.createURI(baetle, "modified");
            originRel = f.createURI(baetle, "origin");
            containsRel = f.createURI(baetle, "contains");
            entryRel = f.createURI(awol, "entry");
            previousRel = f.createURI(baetle, "previous");
            collectionClass = f.createURI(awol, "Collection");
            entryClass = f.createURI(awol, "Entry");

            /*
            * getRepositoryRoot() returns the actual root directory where the
            * repository was created. 'true' forces to connect to the repository
            * if the root svnBaseUrl is not cached yet.
            */
            out.println("# Repository Root: " + svnRepo.getRepositoryRoot(true));
            svnBaseUrl = svnRepo.getRepositoryRoot(true).toString();
            /*
             * getRepositoryUUID() returns Universal Unique IDentifier (UUID) of the
             * repository. 'true' forces to connect to the repository
             * if the UUID is not cached yet.
             */
            out.println("# Repository UUID: " + svnRepo.getRepositoryUUID(true));
            out.println();

            URI svnRepoUri = f.createURI(svnRepo.getLocation().toString());
            addStatement(f.createStatement(svnRepoUri,RDF.TYPE,f.createURI(baetle,"Release")));
            listEntries(svnRepoUri,"");
        } catch (SVNException svne) {
            err.println("error while listing entries: "
                    + svne.getMessage());
            svne.printStackTrace(err);
            exit(1);
        }

    }


    /**
     * Displays the repository tree at the current path - "" (means
     * the path/to/repository directory)
     */
    private void listEntries(URI collection, String path) throws SVNException {
        Collection entries = svnRepo.getDir(path, -1, null,
                                            (Collection) null);
        Iterator iterator = entries.iterator();
        while (iterator.hasNext()) {


            SVNDirEntry entry = (SVNDirEntry) iterator.next();
            URI entryUri = f.createURI(entry.getURL().toString());

            addStatement(f.createStatement(releaseURI,
                                           containsRel,
                                           entryUri));

            String entryPath = (path.equals("")) ? entry.getName()
                    : path + "/" + entry.getName();

            /*
            * Checking up if the entry is a directory.
            */
            if (entry.getKind() == SVNNodeKind.DIR) {
                addStatement(f.createStatement(entryUri, RDF.TYPE, collectionClass));
                listEntries(entryUri,entryPath);
                //todo: how to I specify moved directories here...
            } else {
                Collection revisions = svnRepo.getFileRevisions(entryPath, null, 1, latestRevision);
                addStatement(f.createStatement(entryUri, RDF.TYPE, entryClass));
                ArrayList<URI> history = new ArrayList<URI>();
                for (Object revo : revisions) {
                    SVNFileRevision rev = (SVNFileRevision) revo;
                    URI revUri = f.createURI(repoRoot + "/!svn/ver/" + rev.getRevision() + rev.getPath());
                    addStatement(f.createStatement(revUri, RDF.TYPE, entryClass));
                    history.add(revUri);
                    URI commitUri = f.createURI(repoRoot + "/!svn/bc/" + rev.getRevision()+"/");
                    addStatement(f.createStatement(commitUri, modifiedRel, revUri));
                }
                URI origin = history.get(0);
                for (int i = 0; i < history.size(); i++) {
                    if (collection !=null) addStatement(f.createStatement(collection, entryRel, history.get(i)));
                    addStatement(f.createStatement(releaseURI, entryRel, history.get(i)));
                    if (i + 1 < history.size())
                        addStatement(f.createStatement(history.get(i+1), previousRel, history.get(i)));
                    addStatement(f.createStatement(history.get(i), originRel, origin));

                }

            }
        }
    }
}
