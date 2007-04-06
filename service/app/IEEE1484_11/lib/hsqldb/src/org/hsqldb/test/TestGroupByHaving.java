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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.hsqldb.Trace;

import junit.framework.TestCase;

/**
 * Test cases for HSQL aggregates and HAVING clause.
 *
 * @author Tony Lai
 */

// fredt@users - modified to remove dependecy on DBUnit
public class TestGroupByHaving extends TestCase {

    //------------------------------------------------------------
    // Class variables
    //------------------------------------------------------------
    private static final String databaseDriver   = "org.hsqldb.jdbcDriver";
    private static final String databaseURL      = "jdbc:hsqldb:mem:.";
    private static final String databaseUser     = "sa";
    private static final String databasePassword = "";

    //------------------------------------------------------------
    // Instance variables
    //------------------------------------------------------------
    private Connection conn;
    private Statement  stmt;

    //------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------

    /**
     * Constructs a new SubselectTest.
     */
    public TestGroupByHaving(String s) {
        super(s);
    }

    //------------------------------------------------------------
    // Class methods
    //------------------------------------------------------------
    protected static Connection getJDBCConnection() throws SQLException {
        return DriverManager.getConnection(databaseURL, databaseUser,
                                           databasePassword);
    }

    protected void setUp() throws Exception {

        super.setUp();

        if (conn != null) {
            return;
        }

        Class.forName(databaseDriver);

        conn = getJDBCConnection();
        stmt = conn.createStatement();

        // I decided not the use the "IF EXISTS" clause since it is not a
        // SQL standard.
        try {

//            stmt.execute("drop table employee");
            stmt.execute("drop table employee if exists");
        } catch (Exception x) {}

        stmt.execute("create table employee(id int, "
                     + "firstname VARCHAR(50), " + "lastname VARCHAR(50), "
                     + "salary decimal(10, 2), " + "superior_id int, "
                     + "CONSTRAINT PK_employee PRIMARY KEY (id), "
                     + "CONSTRAINT FK_superior FOREIGN KEY (superior_id) "
                     + "REFERENCES employee(ID))");
        addEmployee(1, "Mike", "Smith", 160000, -1);
        addEmployee(2, "Mary", "Smith", 140000, -1);

        // Employee under Mike
        addEmployee(10, "Joe", "Divis", 50000, 1);
        addEmployee(11, "Peter", "Mason", 45000, 1);
        addEmployee(12, "Steve", "Johnson", 40000, 1);
        addEmployee(13, "Jim", "Hood", 35000, 1);

        // Employee under Mike
        addEmployee(20, "Jennifer", "Divis", 60000, 2);
        addEmployee(21, "Helen", "Mason", 50000, 2);
        addEmployee(22, "Daisy", "Johnson", 40000, 2);
        addEmployee(23, "Barbara", "Hood", 30000, 2);
    }

    protected void tearDown() throws Exception {

        super.tearDown();

        // I decided not the use the "IF EXISTS" clause since it is not a
        // SQL standard.
        try {

//            stmt.execute("drop table employee");
            stmt.execute("drop table employee if exists");
        } catch (Exception x) {}

        if (stmt != null) {
            stmt.close();

            stmt = null;
        }

        if (conn != null) {
            conn.close();

            conn = null;
        }
    }

    private void addEmployee(int id, String firstName, String lastName,
                             double salary, int superiorId) throws Exception {

        stmt.execute("insert into employee values(" + id + ", '" + firstName
                     + "', '" + lastName + "', " + salary + ", "
                     + (superiorId <= 0 ? "null"
                                        : ("" + superiorId)) + ")");
    }

    /**
     * Tests aggregated selection with a <b>GROUP_BY</b> clause.  This is
     * a normal use of the <b>GROUP_BY</b> clause.  The first two employees
     * do not have a superior, and must be grouped within the same group,
     * according to <b>GROUP_BY</b> standard.
     */
    public void testAggregatedGroupBy() throws SQLException {

        String sql = "select avg(salary), max(id) from employee "
                     + "group by superior_id " + "order by superior_id " + "";
        Object[][] expected = new Object[][] {
            {
                new Double(150000), new Integer(2)
            }, {
                new Double(42500), new Integer(13)
            }, {
                new Double(45000), new Integer(23)
            },
        };

        compareResults(sql, expected, 0);
    }

    /**
     * Tests aggregated selection with a <b>GROUP_BY</b> clause and a
     * <b>HAVING</b> clause.
     * <p>
     * This is a typical use of the <b>GROUP_BY</b> + <b>HAVING</b> clause.
     * The first two employees are eliminated due to the <b>HAVING</b>
     * condition.
     * <p>
     * This test uses aggregated function to eliminate first group.
     */
    public void testAggregatedGroupByHaving1() throws SQLException {

        String sql = "select avg(salary), max(id) from employee "
                     + "group by superior_id " + "having max(id) > 5 "
                     + "order by superior_id " + "";
        Object[][] expected = new Object[][] {
            {
                new Double(42500), new Integer(13)
            }, {
                new Double(45000), new Integer(23)
            },
        };

        compareResults(sql, expected, 0);
    }

    /**
     * Tests aggregated selection with a <b>GROUP_BY</b> clause and a
     * <b>HAVING</b> clause.
     * <p>
     * This is a typical use of the <b>GROUP_BY</b> + <b>HAVING</b> clause.
     * The first two employees are eliminated due to the <b>HAVING</b>
     * condition.
     * <p>
     * This test uses <b>GROUP_BY</b> column to eliminate first group.
     */
    public void testAggregatedGroupByHaving2() throws SQLException {

        String sql = "select avg(salary), max(id) from employee "
                     + "group by superior_id "
                     + "having superior_id is not null "
                     + "order by superior_id " + "";
        Object[][] expected = new Object[][] {
            {
                new Double(42500), new Integer(13)
            }, {
                new Double(45000), new Integer(23)
            },
        };

        compareResults(sql, expected, 0);
    }

    /**
     * Tests an unusual usage of the <b>HAVING</b> clause, without a
     * <b>GROUP BY</b> clause.
     * <p>
     * Only one row is returned by the aggregate selection without a
     * <b>GROUP BY</b> clause.  The <b>HAVING</b> clause is applied to the
     * only returned row.  In this case, the <b>HAVING</b> condition is
     * satisfied.
     */
    public void testHavingWithoutGroupBy1() throws SQLException {

        String sql = "select avg(salary), max(id) from employee "
                     + "having avg(salary) > 1000 " + "";
        Object[][] expected = new Object[][] {
            {
                new Double(65000), new Integer(23)
            },
        };

        compareResults(sql, expected, 0);
    }

    /**
     * Tests an unusual usage of the <b>HAVING</b> clause, without a
     * <b>GROUP BY</b> clause.
     * <p>
     * Only one row is returned by the aggregate selection without a
     * <b>GROUP BY</b> clause.  The <b>HAVING</b> clause is applied to the
     * only returned row.  In this case, the <b>HAVING</b> condition is
     * NOT satisfied.
     */
    public void testHavingWithoutGroupBy2() throws SQLException {

        String sql = "select avg(salary), max(id) from employee "
                     + "having avg(salary) > 1000000 " + "";
        Object[][] expected = new Object[][]{};

        compareResults(sql, expected, 0);
    }

    /**
     * Tests an invalid <b>HAVING</b> clause that contains columns not in
     * the <b>GROUP BY</b> clause.  A SQLException should be thrown.
     */
    public void testInvalidHaving() throws SQLException {

        String sql = "select avg(salary), max(id) from employee "
                     + "group by lastname "
                     + "having (max(id) > 1) and (superior_id > 1) " + "";
        Object[][] expected = new Object[][]{};

        compareResults(sql, expected, -Trace.NOT_IN_AGGREGATE_OR_GROUP_BY);
    }

    //------------------------------------------------------------
    // Helper methods
    //------------------------------------------------------------
    private void compareResults(String sql, Object[][] rows,
                                int errorCode) throws SQLException {

        ResultSet rs = null;

        try {
            rs = stmt.executeQuery(sql);

            assertTrue("Statement <" + sql + "> \nexpecting error code: "
                       + errorCode, (0 == errorCode));
        } catch (SQLException sqlx) {
            if (sqlx.getErrorCode() != errorCode) {
                sqlx.printStackTrace();
            }

            assertTrue("Statement <" + sql + "> \nthrows wrong error code: "
                       + sqlx.getErrorCode() + " expecting error code: "
                       + errorCode, (sqlx.getErrorCode() == errorCode));

            return;
        }

        int rowCount = 0;
        int colCount = rows.length > 0 ? rows[0].length
                                       : 0;

        while (rs.next()) {
            assertTrue("Statement <" + sql + "> \nreturned too many rows.",
                       (rowCount < rows.length));

            Object[] columns = rows[rowCount];

            for (int col = 1, i = 0; i < colCount; i++, col++) {
                Object result   = null;
                Object expected = columns[i];

                if (expected == null) {
                    result = rs.getString(col);
                    result = rs.wasNull() ? null
                                          : result;
                } else if (expected instanceof String) {
                    result = rs.getString(col);
                } else if (expected instanceof Double) {
                    result = new Double(rs.getString(col));
                } else if (expected instanceof Integer) {
                    result = new Integer(rs.getInt(col));
                }

                assertEquals("Statement <" + sql
                             + "> \nreturned wrong value.", columns[i],
                                 result);
            }

            rowCount++;
        }

        assertEquals("Statement <" + sql
                     + "> \nreturned wrong number of rows.", rows.length,
                         rowCount);
    }

    //------------------------------------------------------------
    // Main program
    //------------------------------------------------------------
    public static void main(String[] args) throws IOException {

//        junit.swingui.TestRunner.run(TestGroupByHaving.class);
        junit.textui.TestRunner.run(TestGroupByHaving.class);
    }
}
