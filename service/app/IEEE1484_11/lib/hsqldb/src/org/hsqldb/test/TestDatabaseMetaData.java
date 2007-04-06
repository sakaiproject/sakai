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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestResult;

public class TestDatabaseMetaData extends TestBase {

    public TestDatabaseMetaData(String name) {
        super(name);
    }

    public void test() throws Exception {

        Connection        conn = newConnection();
        PreparedStatement pstmt;
        int               updateCount;

        try {
            pstmt = conn.prepareStatement(
                "SET PROPERTY \"sql.enforce_strict_size\" true");

            pstmt.executeUpdate();
            pstmt.close();

            pstmt = conn.prepareStatement("DROP TABLE t1 IF EXISTS");

            pstmt.executeUpdate();
            pstmt.close();

            pstmt = conn.prepareStatement(
                "CREATE TABLE t1 (cha CHARACTER, dec DECIMAL, doub DOUBLE, lon BIGINT, \"IN\" INTEGER, sma SMALLINT, tin TINYINT, "
                + "dat DATE DEFAULT CURRENT_DATE, tim TIME DEFAULT CURRENT_TIME, timest TIMESTAMP DEFAULT CURRENT_TIMESTAMP );");
            updateCount = pstmt.executeUpdate();

            assertTrue("expected update count of zero", updateCount == 0);

            pstmt = conn.prepareStatement("CREATE INDEX t1 ON t1 (cha );");
            updateCount = pstmt.executeUpdate();
            pstmt       = conn.prepareStatement("DROP TABLE t2 IF EXISTS");
            updateCount = pstmt.executeUpdate();
            pstmt = conn.prepareStatement(
                "CREATE TABLE t2 (cha CHARACTER, dec DECIMAL, doub DOUBLE, lon BIGINT, \"IN\" INTEGER, sma SMALLINT, tin TINYINT, "
                + "dat DATE DEFAULT CURRENT_DATE, tim TIME DEFAULT CURRENT_TIME, timest TIMESTAMP DEFAULT CURRENT_TIMESTAMP );");
            updateCount = pstmt.executeUpdate();
            pstmt = conn.prepareStatement("CREATE INDEX t2 ON t2 (cha );");
            updateCount = pstmt.executeUpdate();

            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet        rsp  = dbmd.getTablePrivileges(null, null, "T1");

            while (rsp.next()) {
                System.out.println("Table: " + rsp.getString(3) + " priv: "
                                   + rsp.getString(6));
            }

            rsp = dbmd.getIndexInfo(null, null, "T1", false, false);

            while (rsp.next()) {
                System.out.println("Table: " + rsp.getString(3)
                                   + " IndexName: " + rsp.getString(6));
            }

            rsp = dbmd.getIndexInfo(null, null, "T2", false, false);

            while (rsp.next()) {
                System.out.println("Table: " + rsp.getString(3)
                                   + " IndexName: " + rsp.getString(6));
            }

            pstmt       = conn.prepareStatement("DROP INDEX t2 ON t2;");
            updateCount = pstmt.executeUpdate();
            rsp         = dbmd.getIndexInfo(null, null, "T2", false, false);

            assertTrue("expected getIndexInfo returns empty resultset",
                       rsp.next() == false);

            ResultSet rs = dbmd.getTables(null, null, "T1",
                                          new String[]{ "TABLE" });
            ArrayList tablesarr = new ArrayList();
            int       i;

            for (i = 0; rs.next(); i++) {
                String tempstr =
                    rs.getString("TABLE_NAME").trim().toLowerCase();

                tablesarr.add(tempstr);
            }

            rs.close();
            assertTrue("expected table t1 count of 1", i == 1);

            Iterator it = tablesarr.iterator();

            for (; it.hasNext(); ) {

                // create new ArrayList and HashMap for the table
                String tablename = ((String) it.next()).trim();
                List   collist   = new ArrayList(30);

                rs = dbmd.getColumns(null, null, tablename.toUpperCase(),
                                     null);

                for (i = 0; rs.next(); i++) {
                    collist.add(
                        rs.getString("COLUMN_NAME").trim().toLowerCase());
                }

                rs.close();
            }

            pstmt = conn.prepareStatement("DROP TABLE t_1 IF EXISTS");

            pstmt.executeUpdate();
            pstmt.close();

            pstmt = conn.prepareStatement(
                "CREATE TABLE t_1 (cha CHARACTER(10), dec DECIMAL(10,2), doub DOUBLE, lon BIGINT, \"IN\" INTEGER, sma SMALLINT, tin TINYINT, "
                + "dat DATE DEFAULT CURRENT_DATE, tim TIME DEFAULT CURRENT_TIME, timest TIMESTAMP DEFAULT CURRENT_TIMESTAMP, bool BOOLEAN );");
            updateCount = pstmt.executeUpdate();

            assertTrue("expected update count of zero", updateCount == 0);

            rs = dbmd.getTables(null, null, "T\\_1", new String[]{ "TABLE" });

            for (i = 0; rs.next(); i++) {
                String tempstr =
                    rs.getString("TABLE_NAME").trim().toLowerCase();

                tablesarr.add(tempstr);
            }

            rs.close();
            assertTrue("expected table t_1 count of 1", i == 1);

            // test various methods
            dbmd.getPrimaryKeys(null, null, "T_1");
            dbmd.getImportedKeys(null, null, "T_1");
            dbmd.getCrossReference(null, null, "T_1", null, null, "T_1");

            // test ResultSetMetaData
            pstmt = conn.prepareStatement(
                "INSERT INTO T_1 (cha, dec, doub) VALUES ('name', 10.23, 0)");

            pstmt.executeUpdate();
            pstmt.close();

            pstmt = conn.prepareStatement("SELECT * FROM T_1");
            rs    = pstmt.executeQuery();

            ResultSetMetaData md = rs.getMetaData();
            int               x  = md.getColumnDisplaySize(1);
            int               y  = md.getColumnDisplaySize(2);
            int               b  = md.getPrecision(2);
            int               c  = md.getScale(1);
            int               d  = md.getScale(2);
            String            e  = md.getColumnClassName(10);
            boolean testresult = (x == 10) && (y == 13) && (b == 10)
                                 && (c == 0) && (d == 2)
                                 && e.equals("java.sql.Timestamp");

            assertTrue("wrong result metadata", testresult);

            e          = md.getColumnClassName(11);
            testresult = e.equals("java.lang.Boolean");

            assertTrue("wrong result metadata", testresult);
            pstmt.close();
            conn.close();
        } catch (Exception e) {
            assertTrue("unable to prepare or execute DDL", false);
        } finally {
            conn.close();
        }
    }

    public static void main(String[] args) throws Exception {

        TestResult            result;
        TestCase              test;
        java.util.Enumeration failures;
        int                   count;

        result = new TestResult();
        test   = new TestDatabaseMetaData("test");

        test.run(result);

        count = result.failureCount();

        System.out.println("TestDatabaseMetaData failure count: " + count);

        failures = result.failures();

        while (failures.hasMoreElements()) {
            System.out.println(failures.nextElement());
        }
    }
}
