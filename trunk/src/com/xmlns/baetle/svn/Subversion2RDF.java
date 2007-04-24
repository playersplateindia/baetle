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
package com.xmlns.baetle.svn;

import static com.xmlns.baetle.svn.BaetleUtil.xsd;
import static com.xmlns.baetle.svn.BaetleUtil.baetle;
import static com.xmlns.baetle.svn.BaetleUtil.rdf;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import static java.text.MessageFormat.format;
import java.text.MessageFormat;
import java.util.*;

/*
 * The following  program extracts NTriples from a subversion repository using the baetle ontology.
 * Example output:
 *
 * #---------------------------------------------
 * <http://baetle.googlecode.com/svn/!svn/bc/118/> <http://www.w3.org/2000/01/rdf-schema#type> <http://baetle.googlecode.com/svn/ns/#Revision> .
 * <http://baetle.googlecode.com/svn/!svn/bc/118/> <http://baetle.googlecode.com/svn/ns/#author> <http://openrdf.org/issues/secure/ViewProfile.jspa?name=henry.story> .
 * <http://baetle.googlecode.com/svn/!svn/bc/118/> <http://baetle.googlecode.com/svn/ns/#date> "2007-04-10T13:50:44.049Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> .
 * <http://baetle.googlecode.com/svn/!svn/bc/118/> <http://baetle.googlecode.com/svn/ns/#summary> "Update to sesame map and uml to fit\n" .
 * <http://baetle.googlecode.com/svn/!svn/bc/118/> <http://baetle.googlecode.com/svn/ns/#modified> <http://baetle.googlecode.com/svn/!svn/ver/118/trunk/mappings/sesame/sesame-d2rqmap.n3> .
 * <http://baetle.googlecode.com/svn/!svn/ver/118/trunk/mappings/sesame/sesame-d2rqmap.n3> <http://baetle.googlecode.com/svn/ns/#previous> <http://baetle.googlecode.com/svn/!svn/ver/117/trunk/mappings/sesame/sesame-d2rqmap.n3> .
 * <http://baetle.googlecode.com/svn/!svn/bc/118/> <http://baetle.googlecode.com/svn/ns/#modified> <http://baetle.googlecode.com/svn/!svn/ver/118/trunk/uml/BaetleUML.graffle> .
 * <http://baetle.googlecode.com/svn/!svn/ver/118/trunk/uml/BaetleUML.graffle> <http://baetle.googlecode.com/svn/ns/#previous> <http://baetle.googlecode.com/svn/!svn/ver/117/trunk/uml/BaetleUML.graffle> .
 * <http://baetle.googlecode.com/svn/!svn/bc/118/> <http://baetle.googlecode.com/svn/ns/#modified> <http://baetle.googlecode.com/svn/!svn/ver/118/trunk/uml/BaetleUML.jpg> .
 * <http://baetle.googlecode.com/svn/!svn/ver/118/trunk/uml/BaetleUML.jpg> <http://baetle.googlecode.com/svn/ns/#previous> <http://baetle.googlecode.com/svn/!svn/ver/117/trunk/uml/BaetleUML.jpg> .
 * #---------------------------------------------
 *
 * Note. A lot of this code and comments is taken from the examples from the tmatsoft softaware. So this code should perhaps be kept under their
 * licence. Not sure how much code has to change to be considered different.
 */
public class Subversion2RDF {


    private static final TimeZone TZ = TimeZone.getTimeZone("UTC");
    static DatatypeFactory xmldf = null;
    static String name = "anonymous";
    static String password = "anonymous";
    static long startRevision = 0;
    static long endRevision = -1;//HEAD (the latest) revision

    String svnBaseUrl;
    String peopleBaseUrl;

    static {
        try {
            xmldf = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();  //todo: decide what exception to throw
        }
    }


    public void usage(String error, Exception e) {
        System.out.println(error);
        System.out.println();
        System.out.println("You can use this class with the following command line arguments.\n" +
                " You may get more functionality by overriding the class though, especially if you need to extract bug data from the comments.");
        System.out.println("Subversion2RDF [-url repositoryUrl] [-ub userBaseUrl] [-st start] [-end end] [-u account] [-p password] [-h]");
        System.out.println("Description:");
        System.out.println(" -url: Url of the base of the repository to scan. Defaults to '" + svnBaseUrl + "'");
        System.out.println(" -ub: base url of all users. defaults to '" + peopleBaseUrl + "' ");
        System.out.println(" -st: number greater than 0, start version (default 0)");
        System.out.println(" -end: revision number to end, -1 if last (default -1)");
        System.out.println(" -u: account name (default: anonymous)");
        System.out.println(" -p: password of account (default: anonymous)");
        System.out.println(" -h: print this message and exit");
        System.out.println();
        if (e != null) e.printStackTrace();
        System.exit(-1);

    }

    /*
    * args parameter is used to obtain a repository location URL, a start
    * revision number, an end revision number, user's account name & password
    * to authenticate him to the server.
    */
    public static void main(String[] args) {

        new Subversion2RDF(args).extract();
    }

    Subversion2RDF(String[] args) {
         /*
          * Initializes the library (it must be done before ever using the
          * library itself)
          */
        setupLibrary();
        init();
        analyseCommandLineArgs(args);


    }

    /**
     * subclasses override this
     */
    void init() {
        svnBaseUrl = "http://baetle.googlecode.com/svn/";
        peopleBaseUrl  = "http://code.google.com/u/";
    }

    private void analyseCommandLineArgs(String[] args) {
        try {
            for (int i = 0; i < args.length; i++) {
                if ("-url".equals(args[i])) {
                    svnBaseUrl = args[++i];
                } else if ("-ppl".equals(args[i])) {
                    peopleBaseUrl = args[++i];
                } else if ("-st".equals(args[i])) {
                    startRevision = Long.parseLong(args[++i]);
                } else if ("-end".equals(args[i])) {
                    endRevision = Long.parseLong(args[++i]);
                } else if ("-u".equals(args[i])) {
                    name = args[++i];
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


    void extract() {
        SVNRepository repository = null;

        try {
            /*
             * Creates an instance of SVNRepository to work with the repository.
             * All user's requests to the repository are relative to the
             * repository location used to create this SVNRepository.
             * SVNURL is a wrapper for URL strings that refer to repository locations.
             */
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnBaseUrl));
        } catch (SVNException svne) {
            /*
             * Perhaps a malformed URL is the cause of this exception.
             */
            usage("error while creating an SVNRepository for the location " + svnBaseUrl, svne);
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

        /*
         * Gets the latest revision number of the repository
         */
        try {
            endRevision = repository.getLatestRevision();
        } catch (SVNException svne) {
            System.err.println("error while fetching the latest repository revision: " + svne.getMessage());
            System.exit(1);
        }

        Collection logEntries = null;
        try {
            /*
             * Collects SVNLogEntry objects for all revisions in the range
             * defined by its start and end points [startRevision, endRevision].
             * For each revision commit information is represented by
             * SVNLogEntry.
             *
             * the 1st parameter (targetPaths - an array of path strings) is set
             * when restricting the [startRevision, endRevision] range to only
             * those revisions when the paths in targetPaths were changed.
             *
             * the 2nd parameter if non-null - is a user's Collection that will
             * be filled up with found SVNLogEntry objects; it's just another
             * way to reach the scope.
             *
             * startRevision, endRevision - to define a range of revisions you are
             * interested in; by default in this program - startRevision=0, endRevision=
             * the latest (HEAD) revision of the repository.
             *
             * the 5th parameter - a boolean flag changedPath - if true then for
             * each revision a corresponding SVNLogEntry will contain a map of
             * all paths which were changed in that revision.
             *
             * the 6th parameter - a boolean flag strictNode - if false and a
             * changed path is a copy (branch) of an existing one in the repository
             * then the history for its origin will be traversed; it means the
             * history of changes of the target URL (and all that there's in that
             * URL) will include the history of the origin path(s).
             * Otherwise if strictNode is true then the origin path history won't be
             * included.
             *
             * The return value is a Collection filled up with SVNLogEntry Objects.
             */
            logEntries = repository.log(new String[]{""}, null,
                                        startRevision, endRevision, true, false);

        } catch (SVNException svne) {
            System.out.println("error while collecting log information for '"
                    + svnBaseUrl + "': " + svne.getMessage());
            System.exit(1);
        }
        for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
            /*
             * gets a next SVNLogEntry
             */
            SVNLogEntry logEntry = (SVNLogEntry) entries.next();
            System.out.println("#---------------------------------------------");
            long revision = logEntry.getRevision();
            String rev = bcRevisionUrl(revision);
            System.out.println(rev + " <" + rdf + "type> <" + baetle + "Committing> .");
            String auth = authorUrl(logEntry.getAuthor());
            if (auth != null)
                System.out.println(format("{0} <" + baetle + "author> {1} .", rev, auth));
            System.out.println(format("{0} <" + baetle + "date> \"{1}\"^^<" + xsd + "dateTime> .", rev, dateToXsdString(logEntry.getDate())));
            String comment = logEntry.getMessage();
            if (comment != null) {
                System.out.println(format("{0} <" + baetle + "summary> {1} .", rev, n3quote(comment)));
                for(String bug: extractBugs(comment)) {
                    System.out.println(MessageFormat.format("{0} <"+baetle+"fixes> {1} .", rev, bug));
                }
            }
            if (logEntry.getChangedPaths().size() > 0) {
                Set changedPathsSet = logEntry.getChangedPaths().keySet();

                for (Iterator changedPaths = changedPathsSet.iterator(); changedPaths
                        .hasNext();) {
                    /*
                     * obtains a next SVNLogEntryPath
                     */
                    SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry
                            .getChangedPaths().get(changedPaths.next());
                    /*
                     * SVNLogEntryPath.getPath returns the changed path itself;
                     *
                     * SVNLogEntryPath.getType returns a charecter describing
                     * how the path was changed ('A' - added, 'D' - deleted or
                     * 'M' - modified);
                     *
                     * If the path was copied from another one (branched) then
                     * SVNLogEntryPath.getCopyPath &
                     * SVNLogEntryPath.getCopyRevision tells where it was copied
                     * from and what revision the origin path was at.
                     */
                    switch (entryPath.getType()) {
                        case 'A':
                            System.out.println(format("{0} <" + baetle + "added> {1} .",
                                                      rev, fileRevision(entryPath.getPath(), revision)));
                            break;
                        case 'D':
                            System.out.println(format("{0} <" + baetle + "deleted> {1} .",
                                                      rev, fileRevision(entryPath.getPath(), revision - 1)));
                            break;
                        case 'M':
                            String newres = fileRevision(entryPath.getPath(), revision);
                            System.out.println(format("{0} <" + baetle + "modified> {1} .", rev, newres));
                            System.out.println(format("{0} <" + baetle + "previous> {1} .",
                                                      newres, fileRevision(entryPath.getPath(), revision - 1)));
                            break;
                        case 'R':
                            System.out.println(format("{0} <" + baetle + "added> {1} .",
                                                      rev, fileRevision(entryPath.getPath(), revision)));
                            System.out.println(format("{0} <" + baetle + "deleted> {1} .",
                                                      rev, fileRevision(entryPath.getPath(), revision - 1)));
                        default:
                            System.out.println("# ?? type: " + entryPath.getType());

                    }
                    typeIt(entryPath.getPath(),revision);
                    /*
                    System.out.println(" "
                            + entryPath.getType()
                            + "	"
                            + entryPath.getPath()
                            + ((entryPath.getCopyPath() != null) ? " (from "
                            + entryPath.getCopyPath() + " revision "
                            + entryPath.getCopyRevision() + ")" : ""));
                    */
                }
            }
        }
    }

    /**
     * Add type information about the file. Is it java source, a build file? Guess from name.
     * @param path
     * @param revision
     */
    void typeIt(String path, long revision) {
        if (path.endsWith(".java")) {
            System.out.println(fileRevision(path,revision)+" <"+rdf+"type> <"+baetle+"JavaSource> .");
        }
    }

    /**
     * link issue to bug
     * Well maintained repositories have some conventional way of linking a commit message to a bug
     * If there is such a convention, a BugExtractor can be implemented.
     * <p/>
     * Override in subclasses
     *
     * @param comment the comment to analyse for a pattern.
     * @return the urls of bugs referred to in the comment
     */
    public ArrayList<String> extractBugs(String comment) {
        return new ArrayList<String>(0);
    }


    /**
     * get the revision url for the file at path in this repository
     * note: this same function appears in different places: refactor.
     * @param path
     * @param revision
     * @return the resource
     */
    String fileRevision(String path, long revision) {
        return format("<" + svnBaseUrl + "!svn/ver/"+ revision+path+">");
    }

    private String bcRevisionUrl(long revision) {
        return new StringBuilder(50).append("<").append(svnBaseUrl).append("!svn/bc/").append(revision).append("/>").toString();
    }

    /**
     * create an author url
     *
     * @param author the string given by subversion to identify the author
     * @return
     */
    private String authorUrl(String author) {
        return "<" + peopleBaseUrl + author + ">";
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

    /**
     * warning: code is not complete, it does not take account of unicode chars
     * see http://www.w3.org/TR/rdf-testcases/#ntriples
     *
     * @param string to be quoted
     * @return
     */
    private static String n3quote(String string) {
        StringBuffer trans = new StringBuffer(string.length() + (int) (string.length() / 0.1));
        trans.append('"');
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
                case '\\':
                    trans.append("\\\\");
                    break;
                case '\"':
                    trans.append("\\\"");
                    break;
                case '\n':
                    trans.append("\\n");
                    break;
                case '\r':
                    trans.append("\\r");
                    break;
                case '\t':
                    trans.append("\\t");
                    break;
                default:
                    trans.append(c);
            }
        }
        trans.append('"');
        return trans.toString();
    }

    public static String dateToXsdString(Date date) {
        GregorianCalendar calendar = new GregorianCalendar();
        //todo: warning: due to a bug in Sesmae 2 alpha 3 we have to set the time zone to TZ and remove millisecond precision
        //todo: see http://www.openrdf.org/forum/mvnforum/viewthread?thread=956
        calendar.setTimeZone(TZ);
        calendar.setTime(date);
        XMLGregorianCalendar xmlGregorianCalendar = xmldf.newXMLGregorianCalendar(calendar);
        // I won't do this here. Hopefully bug has been fixed in last version
        // xmlGregorianCalendar.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
        return xmlGregorianCalendar.toXMLFormat();
    }

}