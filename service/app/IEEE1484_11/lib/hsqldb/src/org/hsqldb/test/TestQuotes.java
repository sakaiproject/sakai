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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *  Test handling of quote characters in strings
 *
 * @author <a href="mailto:david@walend.net">David Walend</a>
 * @author <a href="mailto:jvanzyl@zenplex.com">Jason van Zyl</a>
 */
public class TestQuotes extends TestCase {

    private static final String CREATETABLE =
        "create table quotetest (test varchar)";
    private static final String DELETE = "delete from quotetest";
    private static final String TESTSTRING =
        "insert into quotetest (test) values (?)";
    private static final String NOQUOTES = "the house of the dog of kevin";
    private static final String QUOTES   = "kevin's dog's house";
    private static final String RESULT   = "select * from quotetest";

    public TestQuotes(String testName) {
        super(testName);
    }

    /**
     * Run all related test methods
     */
    public static Test suite() {
        return new TestSuite(org.hsqldb.test.TestQuotes.class);
    }

    public void testSetString() {

        Connection        connection = null;
        Statement         statement  = null;
        PreparedStatement pStatement = null;
        ResultSet         rs1        = null;
        ResultSet         rs2        = null;

        try {
            DriverManager.registerDriver(new org.hsqldb.jdbcDriver());

            connection = DriverManager.getConnection("jdbc:hsqldb:.", "sa",
                    "");
            statement = connection.createStatement();

            statement.executeUpdate(CREATETABLE);

            pStatement = connection.prepareStatement(TESTSTRING);

            pStatement.setString(1, NOQUOTES);
            pStatement.executeUpdate();

            rs1 = statement.executeQuery(RESULT);

            rs1.next();

            String result1 = rs1.getString(1);

            assertTrue("result1 is -" + result1 + "- not -" + NOQUOTES + "-",
                       NOQUOTES.equals(result1));
            statement.executeUpdate(DELETE);
            pStatement.setString(1, QUOTES);
            pStatement.executeUpdate();

            rs2 = statement.executeQuery(RESULT);

            rs2.next();

            String result2 = rs2.getString(1);

            assertTrue("result2 is " + result2, QUOTES.equals(result2));
        } catch (SQLException sqle) {
            fail(sqle.getMessage());
        } finally {
            if (rs2 != null) {
                try {
                    rs2.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }

            if (rs1 != null) {
                try {
                    rs1.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }

            if (pStatement != null) {
                try {
                    pStatement.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }
        }
    }
}
