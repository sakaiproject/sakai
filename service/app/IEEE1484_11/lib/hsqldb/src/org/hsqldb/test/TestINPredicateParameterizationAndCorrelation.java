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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import junit.framework.TestCase;
import junit.framework.TestResult;

/**
 *  HSQLDB TestINPredicate Junit test case. <p>
 *
 * @author  boucherb@users
 * @version 1.7.2
 * @since 1.7.2
 */
public class TestINPredicateParameterizationAndCorrelation extends TestBase {

    public TestINPredicateParameterizationAndCorrelation(String name) {
        super(name);
    }

    /* Implements the TestINPredicate test */
    public void test() throws Exception {

        Connection        conn = newConnection();
        Statement         stmt = conn.createStatement();
        PreparedStatement pstmt;
        ResultSet         rs;
        int               actualCount;
        int               expectedCount;
        String            sql;

        stmt.execute("drop table test if exists");

        sql = "create table test(id int)";

        stmt.execute(sql);

        sql   = "insert into test values(?)";
        pstmt = conn.prepareStatement(sql);

        for (int i = 0; i < 10; i++) {
            pstmt.setInt(1, i);
            pstmt.addBatch();
        }

        pstmt.executeBatch();

        sql   = "select count(*) from test where id in(?,?)";
        pstmt = conn.prepareStatement(sql);

        pstmt.setInt(1, 0);
        pstmt.setInt(2, 9);

        rs = pstmt.executeQuery();

        rs.next();

        expectedCount = 2;
        actualCount   = rs.getInt(1);
        sql           = "\"select count(*) from test where id in(0,9)\"";

        assertEquals(sql, expectedCount, actualCount);

        sql = "select count(*) from test a, test b where 0 in(a.id, b.id)";
        rs  = stmt.executeQuery(sql);

        rs.next();

        expectedCount = rs.getInt(1);
        sql = "select count(*) from test a, test b where ? in (a.id, b.id)";
        pstmt         = conn.prepareStatement(sql);

        pstmt.setInt(1, 0);

        rs = pstmt.executeQuery();

        rs.next();

        actualCount = rs.getInt(1);
        sql = "\"select count(*) from test a, test b where 0 in (a.id, b.id)\"";

        assertEquals(sql, expectedCount, actualCount);

        try {
            sql   = "select count(*) from test a, test b where ? in(?, b.id)";
            pstmt = conn.prepareStatement(sql);

            assertTrue("expected exception preparing \"" + sql + "\"", false);
        } catch (Exception e) {

            // this is the expected result
            assertTrue(e.toString(), true);
        }

        try {
            sql   = "select count(*) from test a, test b where a.id in(?, ?)";
            pstmt = conn.prepareStatement(sql);
        } catch (Exception e) {
            assertTrue("unexpected exception preparing \"" + sql + "\":" + e,
                       false);
        }

        sql = "select count(*) from "
              + "(select * from test where id in (1,2)) a,"
              + "(select * from test where id in (3,4)) b "
              + "where a.id < 2 and b.id < 4";
        rs = stmt.executeQuery(sql);

        rs.next();

        expectedCount = rs.getInt(1);
        sql = "select count(*) from "
              + "(select * from test where id in (?,?)) a,"
              + "(select * from test where id in (?,?)) b "
              + "where a.id < ? and b.id < ?";
        pstmt = conn.prepareStatement(sql);

        pstmt.setInt(1, 1);
        pstmt.setInt(2, 2);
        pstmt.setInt(3, 3);
        pstmt.setInt(4, 4);
        pstmt.setInt(5, 2);
        pstmt.setInt(6, 4);

        rs = pstmt.executeQuery();

        rs.next();

        actualCount = rs.getInt(1);

        assertEquals("row count: ", expectedCount, actualCount);
    }

    /* Runs TestINPredicate test from the command line*/
    public static void main(String[] args) throws Exception {

        TestResult            result;
        TestCase              test;
        java.util.Enumeration failures;
        int                   count;

        result = new TestResult();
        test   = new TestINPredicateParameterizationAndCorrelation("test");

        test.run(result);

        count = result.failureCount();

        System.out.println(
            "TestINPredicateParameterizationAndCorrelation failure count: "
            + count);

        failures = result.failures();

        while (failures.hasMoreElements()) {
            System.out.println(failures.nextElement());
        }
    }
}
