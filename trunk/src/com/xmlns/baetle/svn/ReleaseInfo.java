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

import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNLocationEntry;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashSet;
import static java.lang.System.*;

/**
 * Extract all the info for a particular release.
 *
 * It is easy to crawl the release path for all the files contained in it.
 *
 * More problematic, but I think a more general problem, is keeping track of copy actions. Without this it is not
 * going to be easy to work out what the history of a file was, and without that history it is not possible to link
 * bugs to jars. I may know which source code version lead to which jars, but if I can't find the history of that
 * source code, then I won't either be able to find out which bugs were fixed in it, nor which bugs have later been
 * associated with successors of that code.
 *
 * There is an interesting extra problem in that a release is itself a branch of the code in subversion, so that there
 * may never be successors to it. To find the successor to a release, one has to look first at its ancestor. This
 * indicates that it may be necessary to later give every entry in a successor/predecessor linked list an id, to help
 * search for versions with the same history.
 *
 * @author Henry Story
 */
public class ReleaseInfo {

    static DatatypeFactory xmldf;
    static final String baetle = "http://baetle.googlecode.com/svn/ns/#";
    final static String xsd = "http://www.w3.org/2001/XMLSchema#";
    final static String rdfs = "http://www.w3.org/2000/01/rdf-schema#";

    static {
        try {
            xmldf = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();  //todo: decide what exception to throw
        }
    }

    String svnReleaseUrl = "https://src.aduna-software.org/svn/org.openrdf/releases/sesame2/2.0-beta3";
    String svnBaseUrl;
    String name = "anonymous";
    String password = "anonymous";
    SVNRepository repository;
    HashSet newAfterRevision = new HashSet();
    long copyRevision;

    String releasePath, copiedFrom;


    public void usage(String error, Exception e) {
        out.println(error);
        out.println();
        out.println("You can use this class with the following command line arguments.");
        out.println("Warning. this is exceedingly slow, as we have to connect for every file to find its ancestor." +
                " Subclass this if you have more information. ");
        out.println("ReleaseInfo [-url releaseUrl] [-map versionPath] [-u account] [-p password] [-h]");
        out.println("Description:");
        out.println(" -url: Url of the base of the repository to scan. Defaults to '" + svnReleaseUrl + "'");
        out.println(" -u: account name (default: anonymous)");
        out.println(" -p: password of account (default: anonymous)");
        out.println(" -h: print this message and exit");
        out.println();
        if (e != null) e.printStackTrace();
        exit(-1);

    }

    /*
    * args parameter is used to obtain a repository location URL, user's
    * account name & password to authenticate him to the server.
    */
    public static void main(String[] args) {
        new ReleaseInfo(args).run();


    }

    public ReleaseInfo(String[] args) {
        analyseCommandLineArgs(args);
        init();
    }

    private void init() {

        /*
         * initializes the library (it must be done before ever using the
         * library itself)
         */
        setupLibrary();

        repository = null;
        try {
            /*
             * Creates an instance of SVNRepository to work with the repository.
             * All user's requests to the repository are relative to the
             * repository location used to create this SVNRepository.
             * SVNURL is a wrapper for URL strings that refer to repository locations.
             */
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnReleaseUrl));
        } catch (SVNException svne) {
            /*
             * Perhaps a malformed URL is the cause of this exception
             */
            err.println("error while creating an SVNRepository for location '"
                            + svnReleaseUrl + "': " + svne.getMessage());
            exit(1);
        }

        /*
         * User's authentication information (name/password) is provided via  an
         * ISVNAuthenticationManager  instance.  SVNWCUtil  creates  a   default
         * authentication manager given user's name and password.
         *
         * Default authentication manager first attempts to use provided user name
         * and password and then falls back to the credentials stored in the
         * default Subversion credentials storage that is located in Subversion
         * configuration area. If you'd like to use provided user name and password
         * only you may use BasicAuthenticationManager class instead of default
         * authentication manager:
         *
         *  authManager = new BasicAuthenticationsManager(userName, userPassword);
         *
         * You may also skip this point - anonymous access will be used.
         */
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(name, password);
        repository.setAuthenticationManager(authManager);

    }


    HashSet modifiedAfterRevision = new HashSet();

    void run() {
        try {
            /*
             * Checks up if the specified path/to/repository part of the URL
             * really corresponds to a directory. If doesn't the program exits.
             * SVNNodeKind is that one who says what is located at a path in a
             * revision. -1 means the latest revision.
             */
            SVNNodeKind nodeKind = repository.checkPath("", -1);
            if (nodeKind == SVNNodeKind.NONE) {
                err.println("There is no entry at '" + svnReleaseUrl + "'.");
                exit(1);
            } else if (nodeKind == SVNNodeKind.FILE) {
                err.println("The entry at '" + svnReleaseUrl + "' is a file while a directory was expected.");
                exit(1);
            }
            /*
             * getRepositoryRoot() returns the actual root directory where the
             * repository was created. 'true' forces to connect to the repository
             * if the root svnBaseUrl is not cached yet.
             */
            out.println("# Repository Root: " + repository.getRepositoryRoot(true));
            svnBaseUrl = repository.getRepositoryRoot(true).toString();
            /*
             * getRepositoryUUID() returns Universal Unique IDentifier (UUID) of the
             * repository. 'true' forces to connect to the repository
             * if the UUID is not cached yet.
             */
            out.println("# Repository UUID: " + repository.getRepositoryUUID(true));
            out.println();

            out.println("<"+ svnReleaseUrl +"> <"+rdfs+"type> <"+baetle+"Release> .");

            releasePath = svnReleaseUrl.substring(svnBaseUrl.length(),svnReleaseUrl.length());

            //get all the logs for the revision up until it was copied
            changesAfterRelease();

            /*
             * Displays the repository tree at the current path - "" (means
             * the path/to/repository directory)
             */
            listEntries(repository, "");
        } catch (SVNException svne) {
            err.println("error while listing entries: "
                    + svne.getMessage());
            svne.printStackTrace(err);
            exit(1);
        }
        /*
         * Gets the latest revision number of the repository
         */
        long latestRevision = -1;
        try {
            latestRevision = repository.getLatestRevision();
        } catch (SVNException svne) {
            err
                    .println("error while fetching the latest repository revision: "
                            + svne.getMessage());
            exit(1);
        }
        out.println("");
        out.println("---------------------------------------------");
        out.println("Repository latest revision: " + latestRevision);
        exit(0);
    }


    /**
     * This is a bit of a hack.
     *
     * There may be changes to a release directory after the release copy is made. Paths that are changed after the release
     * copy will map differently to all the other paths.
     * Files that have not changed since the copy will map back like this
     *
     *   /releases/sesame2/2.0-beta3/{file} => /!svn/ver/{revision}/projects/sesame2/{file}
     *
     * files changed after the copy will map like this
     *
     *  /releases/sesame2/2.0-beta3/{file} => /!svn/ver/{revision}/releases/sesame2/2.0-beta3/{file}
     *
     * Now releases in subversion are meant to be frozen trees. It would be better perhaps to use the
     * version tree urls to clarify this.
     *
     * This history of these files can be captured in a full log on the repository. What is important here is
     * to capture the copy event and its release number to be able to correctly track the history of all the files
     * that were copied. 
     *
     *
     * @throws SVNException
     */
    private void changesAfterRelease() throws SVNException {
        Collection logs = repository.log(new String[]{""}, (Collection) null, -1, 0, true, false);
        for(Object e: logs) {
            SVNLogEntry entry = (SVNLogEntry) e;
            Map changedPaths = entry.getChangedPaths();

            for(Iterator i = changedPaths.entrySet().iterator(); i.hasNext();){
                Map.Entry ie = (Map.Entry) i.next();
                String key = (String) ie.getKey();
                SVNLogEntryPath path = (SVNLogEntryPath) ie.getValue();
                if (key.equals(releasePath)) { //we are at the release copy change
                    copiedFrom = path.getCopyPath();
                    copyRevision = path.getCopyRevision();
                    return;
                }
                switch (path.getType()) {
                    case SVNLogEntryPath.TYPE_MODIFIED:
                        modifiedAfterRevision.add(key);
                        break;
                    case SVNLogEntryPath.TYPE_ADDED:
                    case SVNLogEntryPath.TYPE_DELETED:
                    case SVNLogEntryPath.TYPE_REPLACED:
                        newAfterRevision.remove(key);

                }
            }
        }
    }

    private void analyseCommandLineArgs(String[] args) {
        try {
            for (int i = 0; i < args.length; i++) {
                if ("-url".equals(args[i])) {
                    svnReleaseUrl = args[++i].trim();
                } else if ("-p".equals(args[i])) {
                    password = args[++i];
                } else if ("-h".equals(args[i])) {
                    usage("help desired", null);
                }
            }
        } catch (Exception e) {
            usage("could not parse command line arguments", e);
        }
    }


    /*
     * Initializes the library to work with a repository via
     * different protocols.
     */
    private static void setupLibrary() {
        /*
         * For using over http:// and https://
         */
        DAVRepositoryFactory.setup();
        /*
         * For using over svn:// and svn+xxx://
         */
        SVNRepositoryFactoryImpl.setup();

        /*
         * For using over file:///
         */
        FSRepositoryFactory.setup();
    }

    /*
     * Called recursively to obtain all entries that make up the repository tree
     * repository - an SVNRepository which interface is used to carry out the
     * request, in this case it's a request to get all entries in the directory
     * located at the path parameter;
     *
     * path is a directory path relative to the repository location path (that
     * is a part of the URL used to create an SVNRepository instance);
     *
     */
    public void listEntries(SVNRepository repository, String path)
            throws SVNException {
        /*
         * Gets the contents of the directory specified by path at the latest
         * revision (for this purpose -1 is used here as the revision number to
         * mean HEAD-revision) getDir returns a Collection of SVNDirEntry
         * elements. SVNDirEntry represents information about the directory
         * entry. Here this information is used to get the entry name, the name
         * of the person who last changed this entry, the number of the revision
         * when it was last changed and the entry type to determine whether it's
         * a directory or a file. If it's a directory listEntries steps into a
         * next recursion to display the contents of this directory. The third
         * parameter of getDir is null and means that a user is not interested
         * in directory properties. The fourth one is null, too - the user
         * doesn't provide its own Collection instance and uses the one returned
         * by getDir.
         */
        Collection entries = repository.getDir(path, -1, null,
                                               (Collection) null);
        Iterator iterator = entries.iterator();
        while (iterator.hasNext()) {
            SVNDirEntry entry = (SVNDirEntry) iterator.next();
            out.println("<"+ svnReleaseUrl +"> <"+baetle+"contains> <"+entry.getURL()+"> .");

            String basepath = entry.getURL().toString().substring(svnReleaseUrl.length());
            if (newAfterRevision.contains(basepath)) {
            } else if (modifiedAfterRevision.contains(basepath)) {
                //todo: here to find the history we should look at the copy path
            } else {
                out.println("<"+ entry.getURL() +"> <"+baetle+"previous> "+getRevision(copiedFrom+basepath,entry.getRevision())+".");
            }
           /*
            * Checking up if the entry is a directory.
            */
            if (entry.getKind() == SVNNodeKind.DIR) {
                listEntries(repository, (path.equals("")) ? entry.getName()
                        : path + "/" + entry.getName());
            }/** else {
                this is a certain way to find a good url for the previous version, but it is dead slow
                findPreviousRelation(repository, entry);

            }   */
        }

    }

    /**
     * Add type information about the file. Is it java source, a build file? Guess from name.
     */
    void typeIt(String url) {
        if (url.endsWith(".java")) {
            System.out.println("<"+url+"> "+"<"+rdfs+"type> <"+baetle+"JavaSource> .");
        }
    }
    


    /**
     * this works, but is way too slow. Plus it does not really do the right thing for files changed or copied.
     * Files changed after a release, need looking into, until they hook up with the main change tree. Otherwise we will just
     * get the last version.
     *
     * @param repository
     * @param entry
     */
    void findPreviousRelation(SVNRepository repository, SVNDirEntry entry) {
        try {
            String filepath =  entry.getURL().toString().substring(svnBaseUrl.length());

            Collection locs = repository.getLocations(filepath, (Collection) null, repository.getLatestRevision(), new long[]{entry.getRevision()});
            for (Object l: locs) {
                SVNLocationEntry e = (SVNLocationEntry) l;
                out.println("<"+entry.getURL()+"> <"+baetle+"previous> "+getRevision(e.getPath(),e.getRevision())+" .");
            }
        } catch (SVNException e) {
            out.println("# problem with getting previous location of "+entry.getURL());
            e.printStackTrace(err);  //todo: decide what exception to throw
        }
    }

    /**
     * get the url for the revision of file at path
     * @param path
     * @param revision
     * @return
     */
    String getRevision(String path, long revision) {
        return "<"+svnBaseUrl+"/!svn/ver/"+revision+path+">";
    }

}
