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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

// $Id: ExecHarness.java,v 1.10 2005/10/23 19:25:13 fredt Exp $

/**
 * Utilities that test classes can call to execute a specified command and to
 * evaluate the exit status and output of said execution.
 * harnessInstance.exec() executes the given program (Java or not).
 * Any time thereafter, harnessInstance can be interrogated for exit
 * status and text output.
 *
 * ExecHarness can emulate user interaction with SqlTool, but you can
 * not use ExecHarness interactively.
 *
 * To execute java classes, you can either give the classpath by setting the
 * environmental var before running this program, or by giving the classpath
 * switch to the target program.  Classpath switches used for invoking
 * this ExecHarness class WILL NOT EFFECT java executions by ExecHarness.
 * E.g. the java invocation
 * "java org.hsqldb.test.ExecHarness java -cp newcp Cname" will give Cname
 * classpath of 'newcp', but the following WILL NOT:
 * "java -cp newcp org.hsqldb.test.ExecHarness java Cname".
 * It's often easier to just set (and export if necessary) CLASSPATH before
 * invoking ExecHarness.
 *
 * Same applies to java System Properties.  You must supply them after the
 * 2nd "java".
 *
 * @see main() for an example of use.
 */
public class ExecHarness {

    /*
     * In general, for output from the program, we use Strings so that we can
     * use regexes with them.
     * For my current needs, I just need to be able to supply stdin to the
     * target program by a file, so that's all I'm implementing for stdin
     * right now.
     */
    private static final String SYNTAX_MSG =
        "SYNTAX:  java org.hsqldb.test.ExecHarness targetprogram [args...]";
    private static final int MAX_PROG_OUTPUT = 10240;

    /**
     * To test the ExecHarness class itself.
     * (Basically, a sanity check).
     *
     * Note that we always exec another process.  This makes it safe to
     * execute Java classes which may call System.exit().
     *
     * @param sa sa[0] is the program to be run.
     *           Remaining arguments will be passed as command-line args
     *           to the sa[0] program.
     */
    public static void main(String[] sa)
    throws IOException, FileNotFoundException, InterruptedException {

        byte[] localBa = new byte[10240];

        if (sa.length < 1) {
            System.err.println(SYNTAX_MSG);
            System.exit(1);
        }

        String progname = sa[0];

        System.err.println(
            "Enter any input that you want passed to SqlTool via stdin\n"
            + "(end with EOF, like Ctrl-D or Ctrl-Z+ENTER):");

        File tmpFile = File.createTempFile("ExecHarness-", ".input");
        String specifiedCharSet = System.getProperty("harness.charset");
        String charset = ((specifiedCharSet == null) ? DEFAULT_CHARSET
                                                     : specifiedCharSet);
        FileOutputStream fos    = new FileOutputStream(tmpFile);
        int              i;

        while ((i = System.in.read(localBa)) > 0) {
            fos.write(localBa, 0, i);
        }

        fos.close();

        ExecHarness harness = new ExecHarness(progname);

        harness.setArgs(shift(sa));
        harness.setInput(tmpFile);
        harness.exec();
        tmpFile.delete();

        int retval = harness.getExitValue();

        System.err.println(
            "STDOUT ******************************************");
        System.out.print(harness.getStdout());
        System.err.println(
            "ERROUT ******************************************");
        System.err.print(harness.getErrout());
        System.err.println(
            "*************************************************");
        System.err.println(progname + " exited with value " + retval);
        harness.clear();
        System.exit(retval);
    }

    File    input     = null;
    String  program   = null;
    int     exitValue = 0;
    boolean executed  = false;

    // I'm sure there's a better way to do this.  Can't think of it right now.
    String[] mtStringArray = {};
    String[] args          = mtStringArray;

    // The extra 1 is so we can request 1 more byte than we want.
    // If that read is satisfied, we know that we read > MAX_PROG_OUTPUT.
    private byte[]              ba = new byte[MAX_PROG_OUTPUT + 1];
    private String              stdout          = null;
    private String              errout          = null;
    private static final String DEFAULT_CHARSET = "US-ASCII";

    /*
     * Execute associated program synchronously, but in a separate process.
     * Would be easy to run it asynchronously, but I think this is more
     * useful as-is for unit testing purposes.
     *           To run a Java class, give args like
     *           <PRE>{ "java", "org.hsqldb.util.SqlTool", "mem" }</PRE>
     *
     * Intentionally passes through many exceptions so that the unit testing
     * tool may properly process these are errors of the testing procedure,
     * not a failed test.
     *
     * In addition to passed-through exceptions, this method will throw
     * an IOException if the invoked program generates > 10 k of output
     * to either stdout or errout.
     */
    public void exec() throws IOException, InterruptedException {

        InputStream stream;
        int         i;
        int         writePointer;

        if (executed) {
            throw new IllegalStateException("You have already executed '"
                                            + program + "'.  Run clear().");
        }

        Process      proc = Runtime.getRuntime().exec(unshift(program, args));
        OutputStream outputStream = proc.getOutputStream();

        if (input != null) {
            BufferedInputStream bis =
                new BufferedInputStream(new FileInputStream(input));

            while ((i = bis.read(ba)) > 0) {
                outputStream.write(ba, 0, i);
            }
        }

        outputStream.close();

        stream       = proc.getInputStream();
        writePointer = 0;

        while ((i = stream.read(ba, writePointer, ba.length - writePointer))
                > 0) {
            writePointer += i;
        }

        if (i > -1) {
            throw new IOException(program + " generated > " + (ba.length - 1)
                                  + " bytes of standard output");
        }

        stream.close();

        executed = true;    // At this point we are changing state.  No going back.
        stdout       = new String(ba, 0, writePointer);
        stream       = proc.getErrorStream();
        writePointer = 0;

        while ((i = stream.read(ba, writePointer, ba.length - writePointer))
                > 0) {
            writePointer += i;
        }

        if (i > -1) {
            throw new IOException(program + " generated > " + (ba.length - 1)
                                  + " bytes of error output");
        }

        stream.close();

        errout    = new String(ba, 0, writePointer);
        exitValue = proc.waitFor();
    }

    /*
     * You must run this method before preparing an ExecHarness for re-use
     * (I.e. to exec() again).
     */
    public void clear() {

        // TODO:  Release output buffers.
        args     = mtStringArray;
        executed = false;
        stdout   = errout = null;
        input    = null;
    }

    public String getStdout() {
        return stdout;
    }

    public String getErrout() {
        return errout;
    }

    /**
     * @param inFile  There is no size limit on the input file.
     */
    public void setInput(File inFile) throws IllegalStateException {

        if (executed) {
            throw new IllegalStateException("You have already executed '"
                                            + program + "'.  Run clear().");
        }

        input = inFile;
    }

    public void setArgs(String[] inArgs) throws IllegalStateException {

        if (executed) {
            throw new IllegalStateException("You have already executed '"
                                            + program + "'.  Run clear().");
        }

        args = inArgs;
    }

    public void setArgs(List list) throws IllegalStateException {
        setArgs(listToPrimitiveArray(list));
    }

    int getExitValue() throws IllegalStateException {

        if (!executed) {
            throw new IllegalStateException("You have not executed '"
                                            + program + "' yet");
        }

        return exitValue;
    }

    /**
     * Create an ExecHarness instance which can invoke the given program.
     *
     * @param inName Name of the external program (like "cat" or "java").
     */
    public ExecHarness(String inName) {
        program = inName;
    }

    /**
     * These utility methods really belong in a class in the util package.
     */
    public static String[] unshift(String newHead, String[] saIn) {

        String[] saOut = new String[saIn.length + 1];

        saOut[0] = newHead;

        for (int i = 1; i < saOut.length; i++) {
            saOut[i] = saIn[i - 1];
        }

        return saOut;
    }

    public static String[] shift(String[] saIn) {

        String[] saOut = new String[saIn.length - 1];

        for (int i = 0; i < saOut.length; i++) {
            saOut[i] = saIn[i + 1];
        }

        return saOut;
    }

    public static String[] listToPrimitiveArray(List list) {

        String[] saOut = new String[list.size()];

        for (int i = 0; i < list.size(); i++) {
            saOut[i] = (String) list.get(i);
        }

        return saOut;
    }

    public static String[] push(String newTail, String[] saIn) {

        String[] saOut = new String[saIn.length + 1];

        for (int i = 0; i < saIn.length; i++) {
            saOut[i] = saIn[i];
        }

        saOut[saOut.length - 1] = newTail;

        return saOut;
    }

    public static String[] pop(String[] saIn) {

        String[] saOut = new String[saIn.length - 1];

        for (int i = 0; i < saOut.length; i++) {
            saOut[i] = saIn[i];
        }

        return saOut;
    }

    public static String stringArrayToString(String[] sa) {

        StringBuffer sb = new StringBuffer("{");

        for (int i = 0; i < sa.length; i++) {
            if (i > 0) {
                sb.append(',');
            }

            sb.append(sa[i]);
        }

        return sb.toString() + '}';
    }
}
