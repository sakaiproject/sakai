/* Copyright (c) 2001-2005, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// $Id: SqlToolHarness.java,v 1.13 2005/10/23 19:25:13 fredt Exp $

/**
 * Runs SqlTool tests based upon metacommands embedded in comments in SQL
 * files.
 */
public class SqlToolHarness {

    private static final int MAX_SQLFILE_LEN = 10240;
    private static final String SYNTAX_MSG =
        "SYNTAX:  java org.hsqldb.test.SqlToolHarness file1.sql [file2.sq...]";

    /**
     * To test the SqlToolHarness class itself.
     * (Basically, a sanity check).
     *
     * @param sa Each argument is a SQL file to process.
     * @returns Exits with 0 or 1 depending on whether the last
     * SqlToolHarness.execute() returned true or false (correspondingly).
     */
    public static void main(String[] sa)
    throws IOException, InterruptedException {

        if (sa.length > 0 && sa[0].equals("-v")) {
            sa = ExecHarness.shift(sa);

            System.setProperty("VERBOSE", "true");
        }

        if (sa.length < 1) {
            System.err.println(SYNTAX_MSG);
            System.exit(1);
        }

        SqlToolHarness harness = new SqlToolHarness();
        boolean        result  = true;

        for (int i = 0; i < sa.length; i++) {
            result = harness.execute(new File(sa[i]));

            System.err.println(sa[i] + " ==> " + result);
        }

        System.exit(result ? 0
                           : 1);
    }

    ExecHarness execHarness;

    public SqlToolHarness() {

        execHarness = new ExecHarness("java");

        String tmp = System.getProperty("VERBOSE");

        Verbose = (tmp != null) && (tmp.trim().length() > 0);
    }

    private boolean Verbose = false;

    /**
     * Run SqlTool according to metacommands embedded in given SQL file.
     *
     * @param file SQL file
     */
    public boolean execute(File file)
    throws IOException, InterruptedException {

        Metadata md = new Metadata(file);

        if (Verbose) {
            System.err.println("HARNESS METADATA:\n" + md);
        }

        execHarness.clear();

        String[] args =
            new String[md.jvmargs.length + 1 + md.toolargs.length + (md.inputAsFile ? 1
                                                                                    : 0)];
        int argIndex = 0;

        for (int i = 0; i < md.jvmargs.length; i++) {
            args[argIndex++] = md.jvmargs[i];
        }

        args[argIndex++] = org.hsqldb.util.SqlTool.class.getName();

        for (int i = 0; i < md.toolargs.length; i++) {
            args[argIndex++] = md.toolargs[i];
        }

        if (md.inputAsFile) {
            args[argIndex++] = file.toString();
        } else {
            execHarness.setInput(file);
        }

        if (Verbose) {
            System.err.println("ALL ARGS: "
                               + ExecHarness.stringArrayToString(args));
        }

        execHarness.setArgs(args);
        execHarness.exec();

        if (Verbose) {
            System.err.println(
                "STDOUT ******************************************");
            System.out.print(execHarness.getStdout());
            System.err.println(
                "ERROUT ******************************************");
            System.err.print(execHarness.getErrout());
            System.err.println(
                "*************************************************");
        }

        if (md.exitValue != null) {
            if (md.exitValue.intValue() != execHarness.getExitValue()) {
                if (Verbose) {
                    System.err.println("Failed exit value test");
                }

                return false;
            }
        }

        String stdout = execHarness.getStdout();
        String errout = execHarness.getErrout();

        for (int i = 0; i < md.rejectErroutPatterns.length; i++) {
            if (md.rejectErroutPatterns[i].matcher(errout).find()) {
                if (Verbose) {
                    System.err.println("Failed rejectErrOut regex '"
                                       + md.rejectErroutPatterns[i].pattern()
                                       + "'");
                }

                return false;
            }
        }

        for (int i = 0; i < md.rejectStdoutPatterns.length; i++) {
            if (md.rejectStdoutPatterns[i].matcher(stdout).find()) {
                if (Verbose) {
                    System.err.println("Failed rejectStdout regex '"
                                       + md.rejectStdoutPatterns[i].pattern()
                                       + "'");
                }

                return false;
            }
        }

        for (int i = 0; i < md.requireErroutPatterns.length; i++) {
            if (!md.requireErroutPatterns[i].matcher(errout).find()) {
                if (Verbose) {
                    System.err.println("Failed requireErrorOut regex '"
                                       + md.requireErroutPatterns[i].pattern()
                                       + "'");
                }

                return false;
            }
        }

        for (int i = 0; i < md.requireStdoutPatterns.length; i++) {
            if (!md.requireStdoutPatterns[i].matcher(stdout).find()) {
                if (Verbose) {
                    System.err.println("Failed requireStdOut regex '"
                                       + md.requireStdoutPatterns[i].pattern()
                                       + "'");
                }

                return false;
            }
        }

        return true;
    }

    private static String[]  mtString  = {};
    private static Pattern[] mtPattern = {};

    private class Metadata {

        private byte[] ba = new byte[MAX_SQLFILE_LEN + 1];

        public Metadata(File inFile)
        throws FileNotFoundException, IOException {

            String name, val;
            String metaBlock = getHarnessMetaBlock(inFile);
            /*  This really only needed for debugging this class itself
             *  (SqlToolHarness).
            if (Verbose) {
                System.err.println("METABLOCK {\n" + metaBlock + "}");
            }
            */
            Pattern directivePattern =
                Pattern.compile("(?m)^\\s*(\\w+)\\s+(.*\\S+)?");
            Matcher directiveMatcher = directivePattern.matcher(metaBlock);

            while (directiveMatcher.find()) {
                if (directiveMatcher.groupCount() != 2) {
                    throw new RuntimeException("Pattern '" + directivePattern
                                               + " matched "
                                               + directiveMatcher.groupCount()
                                               + " groups.");
                }

                name = directiveMatcher.group(1);
                val = ((directiveMatcher.groupCount() == 2)
                       ? directiveMatcher.group(2)
                       : null);

                if (name.equals("arg")) {
                    toolargs = ExecHarness.push(val, toolargs);
                } else if (name.equals("jvmarg")) {
                    jvmargs = ExecHarness.push(val, jvmargs);
                } else if (name.equals("requireStdoutRegex")) {
                    requireStdoutPatterns = push(Pattern.compile(val),
                                                 requireStdoutPatterns);
                } else if (name.equals("rejectStdoutRegex")) {
                    rejectStdoutPatterns = push(Pattern.compile(val),
                                                rejectStdoutPatterns);
                } else if (name.equals("requireErroutRegex")) {
                    requireErroutPatterns = push(Pattern.compile(val),
                                                 requireErroutPatterns);
                } else if (name.equals("rejectErroutRegex")) {
                    rejectErroutPatterns = push(Pattern.compile(val),
                                                rejectErroutPatterns);
                } else if (name.equals("inputAsFile")) {
                    inputAsFile = Boolean.valueOf(val).booleanValue();
                } else if (name.equals("exitValue")) {
                    exitValue = ((val == null) ? null
                                               : Integer.valueOf(val));
                } else {

                    // TODO:  Use a custom Exception class.
                    throw new IOException("Unknown Metadata directive: "
                                          + name);
                }
            }
        }

        private String[]  toolargs              = mtString;
        private String[]  jvmargs               = mtString;
        private Pattern[] requireStdoutPatterns = mtPattern;
        private Pattern[] rejectStdoutPatterns  = mtPattern;
        private Pattern[] requireErroutPatterns = mtPattern;
        private Pattern[] rejectErroutPatterns  = mtPattern;
        private boolean   inputAsFile           = false;
        private Integer   exitValue             = new Integer(0);

        public String toString() {

            StringBuffer sb = new StringBuffer();

            sb.append("    TOOLARGS: "
                      + ExecHarness.stringArrayToString(toolargs) + '\n');
            sb.append("    JVMARGS: "
                      + ExecHarness.stringArrayToString(jvmargs) + '\n');
            sb.append("    REQUIRE_STDOUT_PATTERNS: "
                      + patternArrayToString(requireStdoutPatterns) + '\n');
            sb.append("    REJECT_STDOUT_PATTERNS: "
                      + patternArrayToString(rejectStdoutPatterns) + '\n');
            sb.append("    REQUIRE_ERROUT_PATTERNS: "
                      + patternArrayToString(requireErroutPatterns) + '\n');
            sb.append("    REJECT_ERROUT_PATTERNS: "
                      + patternArrayToString(rejectErroutPatterns) + '\n');
            sb.append("    INPUTASFILE: " + inputAsFile + '\n');
            sb.append("    EXITVALUE: " + exitValue + '\n');

            return sb.toString();
        }

        private String getHarnessMetaBlock(File inFile)
        throws FileNotFoundException, IOException {

            // The extra 1 is so we can request 1 more byte than we want.
            // If that read is satisfied, we know that we read > MAX_SQLFILE_LEN
            int i;
            int writePointer = 0;
            BufferedInputStream bis =
                new BufferedInputStream(new FileInputStream(inFile));

            while ((i = bis.read(ba, writePointer, ba.length - writePointer))
                    > 0) {
                writePointer += i;
            }

            if (i > -1) {
                throw new IOException(inFile.toString() + " is larger than "
                                      + (ba.length - 1) + " bytes.");
            }

            StringBuffer sb1 = new StringBuffer();
            StringBuffer sb2 = new StringBuffer();
            Pattern commentPattern =
                Pattern.compile("(?s)(?<=/\\*).*?(?=\\*/)");
            Pattern mdPattern = Pattern.compile(
                "(?m)(^\\s*HARNESS_METADATA\\s+BEGIN\\s*^)(?s)(.*?$)(?-s)"
                + "(\\s*HARNESS_METADATA\\s+END\\s*$)");
            Matcher commentMatcher = commentPattern.matcher(new String(ba, 0,
                writePointer));

            while (commentMatcher.find()) {
                sb1.append(commentMatcher.group() + '\n');
            }

            Matcher mdMatcher = mdPattern.matcher(sb1);

            while (mdMatcher.find()) {
                if (mdMatcher.groupCount() != 3) {
                    continue;
                }

                sb2.append(mdMatcher.group(2) + '\n');
            }

            return sb2.toString();
        }
    }

    public static Pattern[] push(Pattern newTail, Pattern[] pataIn) {

        Pattern[] pataOut = new Pattern[pataIn.length + 1];

        for (int i = 0; i < pataIn.length; i++) {
            pataOut[i] = pataIn[i];
        }

        pataOut[pataOut.length - 1] = newTail;

        return pataOut;
    }

    public static String patternArrayToString(Pattern[] pata) {

        StringBuffer sb = new StringBuffer("{");

        for (int i = 0; i < pata.length; i++) {
            if (i > 0) {
                sb.append(',');
            }

            sb.append(pata[i].pattern());
        }

        return sb.toString() + '}';
    }
}
