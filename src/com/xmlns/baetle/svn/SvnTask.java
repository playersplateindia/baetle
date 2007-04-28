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

import com.xmlns.baetle.sesame.RefactorTask;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * The class of tasks that connect themselves to svn.
 * Sets up the connections.
 *
 * @author Henry Story
 */
public abstract class SvnTask extends RefactorTask {
    SVNRepository svnRepo;

    static {
        setupLibrary();
    }

    String getArgs() {
       return "[-name name -pass pass] [-u repoRoot]";
    }

    String getMessage() {
         return "-name: user name\n" +
                "-pass: user password\n" +
                "-u repository root url\n" +
                "-debug";
    }

    /**
     * Do nothing by default
     * extend if needed
     *
     * @param args arguments passed from the command line
     */
    public void setArgs(String[] args) {
        String name = "anonymous", pass = "anonymous";
        for (int i = 0; i < args.length; i++) {
            if ("-name".equals(args[i])) {
                name = args[++i];
            } else if ("-pass".equals(args[i])) {
                pass = args[++i];
            } else if ("-u".equals(args[i])) {
                setSVNRepositoryRoot(args[++i]);
            } 
        }
        if (name != null && pass != null) {
            setAuthenticationManager(name, pass);
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

    String getSVNRepositoryRoot() {
        return svnRepo.getLocation().toString();
    }

    /*
      * Creates an instance of SVNRepository to work with the repository.
      * All user's requests to the repository are relative to the
      * repository location used to create this SVNRepository.
      * SVNURL is a wrapper for URL strings that refer to repository locations.
      */  
    void setSVNRepositoryRoot(String root) {
        try {
            svnRepo = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(root));
        } catch (SVNException svne) {
               message("error while creating an SVNRepository for the location '"
                    + getSVNRepositoryRoot() + "': " + svne.getMessage());
        }
    }

    /**
     * User's authentication information (name/password) is provided via  an
     * ISVNAuthenticationManager  instance.  SVNWCUtil  creates  a   default
     * authentication manager given user's name and password.
     * <p/>
     * Default authentication manager first attempts to use provided user name
     * and password and then falls back to the credentials stored in the
     * default Subversion credentials storage that is located in Subversion
     * configuration area. If you'd like to use provided user name and password
     * only you may use BasicAuthenticationManager class instead of default
     * authentication manager:
     * <p/>
     * authManager = new BasicAuthenticationsManager(userName, userPassword);
     * <p/>
     * You may also skip this point - anonymous access will be used.
     */
    void setAuthenticationManager(String user, String password) {
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(user, password);
        svnRepo.setAuthenticationManager(authManager);

    }


}
