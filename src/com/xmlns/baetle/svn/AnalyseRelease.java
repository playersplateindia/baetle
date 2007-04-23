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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import static java.lang.System.exit;
import static java.lang.System.out;
import java.math.BigInteger;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * extract the info from a binary release
 * find all the jars it contains, the md5 sum of those jars, and the classes they contain
 * given the base url of the jar write out all this information in ntriples
 * todo: the binary is currently downloaded and extracted by hand in a directory, automate this
 * todo: the jars are each given fictitious urls close to the location of the extraction point.
 * todo: one could use blank nodes for this. Decide what is best
 * @author Henry Story
 */
public class AnalyseRelease {
    static final String baetle = "http://baetle.googlecode.com/svn/ns/#";
    final static String xsd = "http://www.w3.org/2001/XMLSchema#";
    final static String rdfs = "http://www.w3.org/2000/01/rdf-schema#";


    File base = new File("/Users/hjs/Programming/Sesame/openrdf-sesame-2.0-beta3");
    String packageLocation = "http://downloads.sourceforge.net/sesame/openrdf-sesame-2.0-beta3-onejar.jar";
    String packageBase = "http://downloads.sourceforge.net/sesame/openrdf-sesame-2.0-beta3"; // we will imagine that the jars are inside this directory


    public static void main(String[] args) {
        new AnalyseRelease(args).run();
    }

    public void usage(String error, Exception e) {
        out.println(error);
        out.println();
        out.println("AnalyseRelease [-base basedir] [-h] packageURL ");
        out.println("Description:");
        out.println(" -base: dir to start searching for jars");
        out.println(" -h help: print this out.");
        out.println(" packageUrl: name of package as a url (could download it and extract it if no basedir)");
        if (e != null) e.printStackTrace();
        exit(-1);

    }

    public AnalyseRelease(String[] args) {
        analyseCommandLineArgs(args);
    }


    private void analyseCommandLineArgs(String[] args) {
        try {
            for (int i = 0; i < args.length; i++) {
                if ("-base".equals(args[i])) {
                    base = new File(args[++i]);
                    if (!base.isDirectory()) usage(args[i] + " should point to a directory ", null);
                } else if ("-h".equals(args[i])) {
                    usage("help", null);
                } else {
                    packageLocation = new URL(args[i]).toString();
                    packageBase = packageLocation.substring(packageLocation.lastIndexOf('/'), packageLocation.length());
                }
            }
        } catch (Exception e) {
            usage("could not parse command line arguments", e);
        }
    }

    public void run() {
        out.println("<" + packageLocation + "> <" + rdfs + "type> <" + baetle + "Package> .");
        ArrayList<File> jars = findAllJarFiles(base);
        for (File jar : jars) {
            try {
                analyseJar(jar);
            } catch (IOException e) {
                e.printStackTrace();  //todo: decide what exception to throw
            }
        }

    }

    private void analyseJar(File jar) throws IOException {
        String jarurl = packageBase + jar.getAbsolutePath().substring(base.getAbsolutePath().length());
        out.println("<" + packageLocation + "> <" + baetle + "contains> <" + jarurl + "> .");
        FileChannel channel = new RandomAccessFile(jar, "r").getChannel();
        ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int) channel.size());
        try {
            System.out.println("<" + jarurl + "> <" + baetle + "md5sum> \"" + digest("MD5", buffer) + "\" ."); //find well known namespace for md5sun
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //should not happen
        }
        System.out.println("<" + jarurl + "> <" + rdfs + "label> \"" + jar.getName() + "\" ."); // can one use rdfs here?

        JarFile jarf = new JarFile(jar);
        for (Enumeration<JarEntry> en = jarf.entries(); en.hasMoreElements();) {
            JarEntry je = en.nextElement();
            String name = je.getName();
            if (name.endsWith(".class")) {
                String classurl = "<jar:" + jarurl + "!/" + name + "> ";
                System.out.println("<" + jarurl + "> <" + baetle + "contains> " + classurl + " .");
                System.out.println(classurl + "<" + rdfs + "type> <" + baetle + "JavaClassFile> .");
                //ok I want to say it contains a file named 'name', but this should do for the moment
                //I should really have it point to a bnode for the class, and give it a name property
                //then later when I find a url for the jar, I can simulateneously find a url for the class
                //(relative to the jar of course)
                //I later want to say that the jar contains a class, and that that class was build from some source file.
            }

        }

    }

    /**
     * copied from http://blogs.sun.com/andreas/entry/hashing_a_file_in_3
     *
     * @param algorithm
     * @param buffer
     * @throws Exception
     */
    private static String digest(String algorithm, ByteBuffer buffer) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(buffer.duplicate());
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        new Formatter(sb).format
                ("%0" + (digest.length * 2) + "x",
                 new BigInteger(1, digest));
        return sb.toString();
    }

    private ArrayList<File> findAllJarFiles(File base) {
        ArrayList<File> result = new ArrayList<File>();
        for (File f : base.listFiles()) {
            if (f.isDirectory())
                result.addAll(findAllJarFiles(f));
            else {
                if (f.toString().endsWith(".jar")) {
                    result.add(f);
                }
            }
        }
        return result;
    }


}
