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
import java.sql.SQLException;

import junit.framework.TestCase;
import junit.framework.TestResult;

/**
 * HSQLDB TestBug778213 Junit test case. <p>
 *
 * Test to ensure that DDL can be executed through the
 * HSQLDB PreparedStatement interface implementation and
 * that the behaviour of the prepared statement object is
 * nominally correct under "prepared" DDL.
 *
 * @author  boucherb@users
 * @version 1.7.2
 * @since 1.7.2
 */
public class TestBug778213 extends TestBase {

    public TestBug778213(String name) {
        super(name);
    }

    /* Implements the TestBug778213_Part3 test */
    public void test() throws Exception {

        Connection        conn = newConnection();
        PreparedStatement pstmt;
        int               updateCount;

        try {
            pstmt = conn.prepareStatement("drop table test if exists");

            pstmt.executeUpdate();

            pstmt       = conn.prepareStatement("create table test(id int)");
            updateCount = pstmt.executeUpdate();

            assertTrue("expected update count of zero", updateCount == 0);

            pstmt       = conn.prepareStatement("drop table test");
            updateCount = pstmt.executeUpdate();

            assertTrue("expected update count of zero", updateCount == 0);
        } catch (Exception e) {
            assertTrue("unable to prepare or execute DDL", false);
        } finally {
            conn.close();
        }

        conn = newConnection();

        try {
            pstmt = conn.prepareStatement("create table test(id int)");

            assertTrue("got data expecting update count", !pstmt.execute());
        } catch (Exception e) {
            assertTrue("unable to prepare or execute DDL", false);
        } finally {
            conn.close();
        }

        conn = newConnection();

        boolean exception = true;

        try {
            pstmt = conn.prepareStatement("drop table test");

            pstmt.executeQuery();
        } catch (SQLException e) {
            exception = false;
        } finally {
            conn.close();
        }

        if (exception) {
            assertTrue("no exception thrown for executeQuery(DDL)", false);
        }

        conn = newConnection();

        try {
            pstmt = conn.prepareStatement("call identity()");

            pstmt.execute();
        } catch (Exception e) {
            assertTrue("unable to prepare or execute call", false);
        } finally {
            conn.close();
        }

        exception = false;
        conn      = newConnection();

        try {
            pstmt = conn.prepareStatement("create table test(id int)");

            pstmt.addBatch();
        } catch (SQLException e) {
            exception = true;
        } finally {
            conn.close();
        }

        if (exception) {
            assertTrue("not expected exception batching prepared DDL", false);
        }

        conn = newConnection();

        try {
            pstmt = conn.prepareStatement("create table test(id int)");

            assertTrue("expected null ResultSetMetadata for prepared DDL",
                       null == pstmt.getMetaData());
        } finally {
            conn.close();
        }

        conn = newConnection();

//#ifdef  JDBC3
/*

        try {
            pstmt = conn.prepareStatement("create table test(id int)");

            assertTrue("expected zero parameter for prepared DDL",
                       0 == pstmt.getParameterMetaData().getParameterCount());

        } finally {
            conn.close();
        }

*/

//#endif JDBC3
    }

    /* Runs TestBug778213_Part3 test from the command line*/
    public static void main(String[] args) throws Exception {

        TestResult            result;
        TestCase              test;
        java.util.Enumeration failures;
        int                   count;

        result = new TestResult();
        test   = new TestBug778213("test");

        test.run(result);

        count = result.failureCount();

        System.out.println("TestBug778213 failure count: " + count);

        failures = result.failures();

        while (failures.hasMoreElements()) {
            System.out.println(failures.nextElement());
        }
    }
}
