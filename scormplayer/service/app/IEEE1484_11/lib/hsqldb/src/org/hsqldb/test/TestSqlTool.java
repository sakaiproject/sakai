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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class TestSqlTool extends junit.framework.TestCase {

    /**
     * Trivial utility class (for use like x.a dna x.b.
     * Does not have getters/setters.  No purpose would be served by
     * getters and setters, other than over-engineering.
     */
    private class TestSqlFile {

        public File   file;
        public String description;

        public TestSqlFile(String filename,
                           String inDescript) throws IOException {

            file = new File(filename);

            if (!file.isFile()) {
                throw new IOException("'" + file + "' is not a file");
            }

            description = inDescript;
        }
    }

    /**
     * List of SQL files, with a description of the purpose.
     */
    private class SqlFileList extends ArrayList {

        /**
         * Loads a list of SQL files and descriptions for the specified
         * test * method.
         */
        public SqlFileList(String filename) throws IOException {

            BufferedReader  br = new BufferedReader(new FileReader(filename));
            String          s, trimmed;
            StringTokenizer st;
            int             ctr = 0;

            while ((s = br.readLine()) != null) {
                ctr++;

                trimmed = s.replaceFirst("#.*", "").trim();    // Remove comments.

                if (trimmed.length() < 1) {
                    continue;                                  // Skip blank and comment lines
                }

                st = new StringTokenizer(trimmed);

                if (st.countTokens() < 2) {
                    throw new IOException("Bad line no. " + ctr
                                          + " in list file '" + filename
                                          + "'");
                }

                add(new TestSqlFile(st.nextToken(), st.nextToken("")));
            }

            br.close();
        }

        public TestSqlFile getSqlFile(int i) {
            return (TestSqlFile) get(i);
        }
    }

    SqlToolHarness harness = new SqlToolHarness();

    private void runTestsInList(String testList) throws Exception {

        SqlFileList fileList = new SqlFileList(testList);
        TestSqlFile sqlFile;

        for (int i = 0; i < fileList.size(); i++) {
            sqlFile = fileList.getSqlFile(i);

            assertTrue(sqlFile.description + " (" + sqlFile.file + ')',
                       harness.execute(sqlFile.file));
        }
    }

    public void testHistory() throws Exception {
        runTestsInList("testHistory.list");
    }

    public void testEditing() throws Exception {
        runTestsInList("testEditing.list");
    }

    public void testArgs() throws Exception {
        runTestsInList("testArgs.list");
    }

    public void testComments() throws Exception {
        runTestsInList("testComments.list");
    }

    public void testPL() throws Exception {
        runTestsInList("testPL.list");
    }

    public void testSpecials() throws Exception {
        runTestsInList("testSpecials.list");
    }

    public void testSQL() throws Exception {
        runTestsInList("testSQL.list");
    }

    // public TestSqlTool() { super(); } necessary?
    public TestSqlTool(String s) {
        super(s);
    }

    public static void main(String[] sa) {

        if (sa.length > 0 && sa[0].startsWith("--gui")) {
            junit.swingui.TestRunner.run(TestSqlTool.class);
        } else {
            junit.textui.TestRunner runner = new junit.textui.TestRunner();

            System.exit(
                runner.run(
                    runner.getTest(
                        TestSqlTool.class.getName())).wasSuccessful() ? 0
                                                                      : 1);
        }
    }

    public static junit.framework.Test suite() {

        junit.framework.TestSuite newSuite = new junit.framework.TestSuite();

        newSuite.addTest(new TestSqlTool("testHistory"));
        newSuite.addTest(new TestSqlTool("testEditing"));
        newSuite.addTest(new TestSqlTool("testArgs"));
        newSuite.addTest(new TestSqlTool("testComments"));
        newSuite.addTest(new TestSqlTool("testPL"));
        newSuite.addTest(new TestSqlTool("testSpecials"));
        newSuite.addTest(new TestSqlTool("testSQL"));

        return newSuite;
    }
    ;
}
