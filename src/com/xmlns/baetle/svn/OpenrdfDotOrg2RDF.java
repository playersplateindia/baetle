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

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

/**
 * extract all the info from the Sesame openrdf.org repository
 * @author Henry Story
 */
public class OpenrdfDotOrg2RDF extends Subversion2RDF {

    Pattern  bugpattern = Pattern.compile("\\[([a-zA-Z]+-[0-9]+)\\]");

    public static void main(String[] args) {

        new OpenrdfDotOrg2RDF(args).extract();
    }

    void init() {
        svnRepBaseUrl = "https://src.aduna-software.org/svn/org.openrdf/";
        peopleBaseUrl  = "http://openrdf.org/issues/secure/ViewProfile.jspa?name=";        
    }

    public OpenrdfDotOrg2RDF(String[] args) {
        super(args);
    }

    public ArrayList<String> extractBugs(String comment) {
        ArrayList<String> answers = new ArrayList<String>(2);
        Matcher match = bugpattern.matcher(comment);
        while (match.find()) {
            String bug = match.group(1);
            answers.add("<http://openrdf.org/issues/browse/"+bug+"#issue>");
        }
        return  answers;
    }
}
