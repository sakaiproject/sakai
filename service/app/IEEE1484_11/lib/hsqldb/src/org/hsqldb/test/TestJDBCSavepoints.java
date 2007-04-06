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
import java.sql.Savepoint;
import java.sql.Statement;

import org.hsqldb.WebServer;

import junit.framework.TestCase;
import junit.framework.TestResult;

/**
 * Tests JDBC java.sql.Savepoint support in context of new engine SQL-savepoint
 * support and new HSQL protocol extensions for savepoint support. <p>
 *
 * @author boucher@users
 * @version 1.7.2
 * @since 1.7.2
 */
public class TestJDBCSavepoints extends TestCase {

//  You change the url and serverProps to reflect your preferred settings
    // String serverProps = "database.0=mem:test;silent=false;trace=true" // debugging
    String serverProps = "database.0=mem:test;silent=true;trace=false";

    //String     url         = "jdbc:hsqldb:hsql://localhost";
    String     url = "jdbc:hsqldb:http://localhost";
    String     user;
    String     password;
    Statement  stmt;
    Connection conn1;
    Connection conn2;

    // Server server;
    // this exercises everything:
    // the engine and JDBC savepoint support,
    // the new HSQL protocol and tunneling HSQL protocol over HTTP
    WebServer server;

    public TestJDBCSavepoints(String name) {
        super(name);
    }

    protected void setUp() {

        user     = "sa";
        password = "";
        stmt     = null;
        conn1    = null;
        conn2    = null;

        // server   = new Server();
        server = new WebServer();

        server.putPropertiesFromString(serverProps);
        server.setLogWriter(null);
        server.start();

        try {
            Class.forName("org.hsqldb.jdbcDriver");

            conn1 = DriverManager.getConnection(url, user, password);
            conn2 = DriverManager.getConnection(url, user, password);
            stmt  = conn1.createStatement();
        } catch (Exception e) {

            //e.printStackTrace();
            System.out.println(this + ".setUp() error: " + e.getMessage());
        }
    }

    protected void tearDown() {

        try {
            conn1.close();
        } catch (Exception e) {

            //e.printStackTrace();
            System.out.println(this + ".tearDown() error: " + e.getMessage());
        }

        try {
            conn2.close();
        } catch (Exception e) {

            //e.printStackTrace();
            System.out.println(this + ".tearDown() error: " + e.getMessage());
        }

        server.stop();
    }

    public void testJDBCSavepoints() throws Exception {

        String            sql;
        String            msg;
        int               i;
        PreparedStatement ps;
        ResultSet         rs;
        Savepoint         sp1;
        Savepoint         sp2;
        Savepoint         sp3;
        Savepoint         sp4;
        Savepoint         sp5;
        Savepoint         sp6;
        Savepoint         sp7;
        int               rowcount = 0;

        sql = "drop table t if exists";

        stmt.executeUpdate(sql);

        sql = "create table t(id int, fn varchar, ln varchar, zip int)";

        stmt.executeUpdate(sql);
        conn1.setAutoCommit(true);

        //-- Test 1 : The execution of an SQL savepoint statement shall
        //            raise an exception in the absence of an active
        //            enclosing transaction
        // fredt@users - there is always an active transaction when autocommit
        // is true. The transaction is committed automatically if the next
        // Statement.execute() or similar call is performed successfully.
/*
        msg = "savepoint set successfully in the abscence of an active transaction";
        try {
            conn.setSavepoint("savepoint1");
            assertTrue(msg,false);
        } catch (Exception e) {}

*/

        //-- setup for following tests
        conn1.setAutoCommit(false);

        sql = "insert into t values(?,?,?,?)";
        ps  = conn1.prepareStatement(sql);

        ps.setString(2, "Mary");
        ps.setString(3, "Peterson-Clancy");

        i = 0;

        for (; i < 10; i++) {
            ps.setInt(1, i);
            ps.setInt(4, i);
            ps.executeUpdate();
        }

        sp1 = conn1.setSavepoint("savepoint1");

        for (; i < 20; i++) {
            ps.setInt(1, i);
            ps.setInt(4, i);
            ps.executeUpdate();
        }

        sp2 = conn1.setSavepoint("savepoint2");

        for (; i < 30; i++) {
            ps.setInt(1, i);
            ps.setInt(4, i);
            ps.executeUpdate();
        }

        sp3 = conn1.setSavepoint("savepoint3");

        for (; i < 40; i++) {
            ps.setInt(1, i);
            ps.setInt(4, i);
            ps.executeUpdate();
        }

        sp4 = conn1.setSavepoint("savepoint4");

        for (; i < 50; i++) {
            ps.setInt(1, i);
            ps.setInt(4, i);
            ps.executeUpdate();
        }

        sp5 = conn1.setSavepoint("savepoint5");
        sp6 = conn1.setSavepoint("savepoint6");
        sp7 = conn1.setSavepoint("savepoint7");
        rs  = stmt.executeQuery("select count(*) from t");

        rs.next();

        rowcount = rs.getInt(1);

        rs.close();

        //-- Test 2 : count of rows matches # rows inserted (assertion req'd by
        //            following tests, but not directly related to the feature
        //            being tested)
        msg = "select count(*) from t value";

        try {
            assertEquals(msg, 50, rowcount);
        } catch (Exception e) {}

        conn2.setAutoCommit(false);
        conn2.setSavepoint("savepoint1");
        conn2.setSavepoint("savepoint2");

        //-- test 3 : A JDBC Savepoint shall be considered invalid if used to
        //            release an SQL-savepoint in an SQL-session other than that
        //            of the originating Connection object
        msg = "savepoint released succesfully on non-originating connection";

        try {
            conn2.releaseSavepoint(sp2);
            assertTrue(msg, false);
        } catch (Exception e) {}

        //-- test 4 : A JDBC Savepoint shall be invalid if used to roll back to
        //            an SQL-savepoint in an SQL-session other than that of the
        //            originating Connection object
        try {
            conn2.rollback(sp1);

            msg = "succesful rollback to savepoint on "
                  + "non-originating connection";

            assertTrue(msg, false);
        } catch (Exception e) {}

        //-- test 5 : Direct execution of a <release savepoint> statement shall
        //            not fail to release an existing indicated savepoint,
        //            regardless of how the indicated savepoint was created
        msg = "direct execution of <release savepoint> statement failed to "
              + "release JDBC-created SQL-savepoint with identical savepoint name";

        try {
            conn2.createStatement().executeUpdate(
                "release savepoint \"savepoint2\"");
        } catch (Exception e) {
            try {
                assertTrue(msg, false);
            } catch (Exception e2) {}
        }

        //-- test 6 : Direct execution of a <rollback to savepoint> statement
        //            shall not fail to roll back to an existing indicated
        //            savepoint due and only due to how the indicated savepoint
        //            was created
        msg = "direct execution of <rollback to savepoint> statement failed to "
              + "roll back to existing JDBC-created SQL-savepoint with identical "
              + "savepoint name";

        try {
            conn2.createStatement().executeUpdate(
                "rollback to savepoint \"savepoint1\"");
        } catch (Exception e) {
            e.printStackTrace();

            try {
                assertTrue(msg, false);
            } catch (Exception e2) {}
        }

        conn1.releaseSavepoint(sp6);

        //-- test 7 : Releasing an SQL-savepoint shall destroy that savepoint
        msg = "savepoint released succesfully > 1 times";

        try {
            conn1.releaseSavepoint(sp6);
            assertTrue(msg, false);
        } catch (Exception e) {}

        //-- test 8 : Releasing an SQL-savepoint shall destroy all subsequent SQL-
        //            savepoints in the same savepoint level
        msg = "savepoint released successfully after preceding savepoint released";

        try {
            conn1.releaseSavepoint(sp7);
            assertTrue(msg, false);
        } catch (Exception e) {}

        //-- test 9 : Releasing an SQL-savepoint shall not affect preceding
        //            savepoints
        msg = "preceding same-point savepoint destroyed by following savepoint release";

        try {
            conn1.releaseSavepoint(sp5);
        } catch (Exception e) {
            try {
                assertTrue(msg, false);
            } catch (Exception e2) {}
        }

        conn1.rollback(sp4);

        rs = stmt.executeQuery("select count(*) from t");

        rs.next();

        rowcount = rs.getInt(1);

        rs.close();

        //-- Test 10 : count of rows matches # rows inserted less the number
        //             of insertions rolled back
        msg = "select * rowcount after 50 inserts - 10 rolled back:";

        try {
            assertEquals(msg, 40, rowcount);
        } catch (Exception e) {}

        //-- test 11 : An SQL-savepoint shall be destroyed in the
        //            process of rolling back to that savepoint
        msg = "savepoint rolled back succesfully > 1 times";

        try {
            conn1.rollback(sp4);
            assertTrue(msg, false);
        } catch (Exception e) {}

        conn1.rollback(sp3);

        rs = stmt.executeQuery("select count(*) from t");

        rs.next();

        rowcount = rs.getInt(1);

        rs.close();

        //-- Test 12 : count of rows matches # rows inserted less the number
        //             of insertions rolled back
        msg = "select count(*) after 50 inserts - 20 rolled back:";

        try {
            assertEquals(msg, 30, rowcount);
        } catch (Exception e) {}

        //-- test 13 : An SQL-savepoint shall be destroyed in the
        //            process of rolling back to that savepoint
        msg = "savepoint released succesfully after use in rollback";

        try {
            conn1.releaseSavepoint(sp3);
            assertTrue(msg, false);
        } catch (Exception e) {}

        conn1.rollback(sp1);

        //-- test 14 : All subsequent savepoints (in a savepoint level)
        //            shall be destroyed by the process of rolling back to
        //            a preceeding savepoint (in the same savepoint level)
        msg = "savepoint rolled back without raising an exception after "
              + "rollback to a preceeding savepoint";

        try {
            conn1.rollback(sp2);
            assertTrue(msg, false);
        } catch (Exception e) {}

        conn1.rollback();

        //-- test 15 : All subsequent savepoints (in a savepoint level)
        //            shall be destroyed by the process of
        //            rolling back the active transaction
        msg = "savepoint released succesfully when it should have been "
              + "destroyed by a full rollback";

        try {
            conn1.releaseSavepoint(sp1);
            assertTrue(msg, false);
        } catch (Exception e) {}

        conn1.setAutoCommit(false);

        sp1 = conn1.setSavepoint("savepoint1");

        conn1.rollback();
        conn1.setAutoCommit(false);
        conn1.createStatement().executeUpdate("savepoint \"savepoint1\"");

        //-- test 16 : A JDBC Savepoint shall be considered invalid if used to
        //             release an SQL-savepoint other than precisely the
        //             one created in correspondence to the creation of that
        //             JDBC Savepoint object
        // fredt@users - we allow this if the name is valid
/*
        msg = "JDBC Savepoint used to successfully release an identically named "
              + "savepoint in a transaction distinct from the originating "
              + "transaction";
        try {
            conn1.releaseSavepoint(sp1);
            assertTrue(msg, false);
        } catch (Exception e) {}
*/
        conn1.setAutoCommit(false);

        sp1 = conn1.setSavepoint("savepoint1");

        conn1.createStatement().executeUpdate("savepoint \"savepoint1\"");

        //-- test 17 : A JDBC Savepoint shall be considered invalid if used to
        //             release an SQL-savepoint other than precisely the
        //             one created in correspondence to the creation of that
        //             JDBC Savepoint object
        // fredt@users - we allow this if the name is valid
/*
        msg = "JDBC Savepoint used to successfully release an identically named "
              + "savepoint in a transaction other than the originating "
              + "transaction";
        try {
            conn1.releaseSavepoint(sp1);
            assertTrue(msg, false);
        } catch (Exception e) {}
*/

        //-- test 18 : A JDBC Savepoint shall be considered invalid if used to
        //             roll back to an SQL-savepoint other than precisely the
        //             one created in correspondence to the creation of that
        //             JDBC Savepoint object
        // fredt@users - we allow this if the name is valid
/*
        msg = "JDBC Savepoint used to successfully to roll back to an "
              + "identically named savepoint in a transaction distinct "
              + "from the originating transaction";
        try {
            conn1.rollback(sp1);
            assertTrue(msg, false);
        } catch (Exception e) {}
*/
        conn1.setAutoCommit(false);

        sp1 = conn1.setSavepoint("savepoint1");

        conn1.createStatement().executeUpdate("savepoint \"savepoint1\"");

        //-- test 19 : A JDBC Savepoint shall be considered invalid if used to
        //             roll back to an SQL-savepoint other than precisely the
        //             one created in correspondence to the creation of that
        //             JDBC Savepoint object
        // fredt@users - we allow this if the name is valid
/*
        msg = "JDBC Savepoint used to successfully release an identically named "
              + "savepoint in a transaction other than the originating "
              + "transaction";
        try {
            conn1.releaseSavepoint(sp1);
            assertTrue(msg, false);
        } catch (Exception e) {}
*/
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        TestResult            result;
        TestCase              test;
        java.util.Enumeration failures;
        int                   count;

        result = new TestResult();
        test   = new TestJDBCSavepoints("testJDBCSavepoints");

        test.run(result);

        count = result.failureCount();

        System.out.println("TestJDBCSavepoints failure count: " + count);

        failures = result.failures();

        while (failures.hasMoreElements()) {
            System.out.println(failures.nextElement());
        }
    }
}
