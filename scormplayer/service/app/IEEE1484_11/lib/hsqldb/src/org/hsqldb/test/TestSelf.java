/* Copyright (c) 1995-2000, The Hypersonic SQL Group.
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
 * Neither the name of the Hypersonic SQL Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HYPERSONIC SQL GROUP,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many individuals 
 * on behalf of the Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2005, The HSQL Development Group
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

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

import org.hsqldb.lib.Sort;

/**
 *  Main test class, containing several JDBC and script based tests to
 *  verify correct operation of the engine.<p>
 *
 *  The tests consist of the following:
 * <ul>
 * <li>
 *  Built-in tests for operations, especially those relating to JDBC.
 *</li>
 * <li>
 *  Speed tests using insert / delete / update on a simple table.<p>
 *</li>
 * <li>
 *  Script based SQL tests consisting of:<p>
 *  <code>TestSelf.txt</code> : the main test script.<p>
 *  <code>TestSelfXXXX.txt</code> : specialised test scripts that
 *  will be run in alphabetical filename order.<p>
 *</li>
 * </ul>
 *
 *  Tests can be added by writing new scripts in the standard format described
 *  in <code>TestSelf.txt</code> and naming the script in the correct format,
 *  <code>TestSelfXXXX.txt</code>, where XXXX is the description of the new
 *  test.<p>
 *  The database can be shutdown at the end of each script (using the
 *  SHUTDOWN command). This allows a test to be divided into two or more
 *  scripts in order to test the persistence mechanism for both objects
 *  created via DDL or data stored in the database. An example of this
 *  is the set of supplied scripts, <code>TestSelfCreate.txt</code>,
 *  <code>TestSelfModify.txt</code> and <code>TestSelfVerify.txt</code>.
 *  (fredt@users)
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
  * @version 1.7.2
  * @since Hypersonic SQL
 */
class TestSelf extends TestUtil {

    /**
     *  This test in invoked from the command line using:
     * <pre>
     * TestSelf [records [-m]]
     *
     * </pre>
     *
     * -m means run the tests in-memory only
     *
     * @param  argv
     */
    public static void main(String[] argv) {

        print("Usage: TestSelf [records [-m]] (-m means in-memory only)");

        int max = 500;

        if (argv.length >= 1) {
            max = Integer.parseInt(argv[0]);
        }

        boolean persistent = true;
        boolean update     = false;

        if (argv.length >= 2) {
            String a1 = argv[1];

            if (a1.equals("-m")) {
                persistent = false;
            }
        }

        test(max, persistent);
    }

    /**
     *  Method declaration
     *
     * @param  max
     * @param  persistent
     */
    static void test(int max, boolean persistent) {

        // DriverManager.setLogStream(System.out);
        try {
            DriverManager.registerDriver(new org.hsqldb.jdbcDriver());

            if (persistent) {
                testPersistence();
                deleteDatabase("test2");
                test("jdbc:hsqldb:test2", "sa", "", true);
                testPerformance("jdbc:hsqldb:test2", "sa", "", max, true);
            }

            test("jdbc:hsqldb:.", "sa", "", false);
            testPerformance("jdbc:hsqldb:.", "sa", "", max, false);
        } catch (Exception e) {
            print("TestSelf error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static void delete(String file) {

        try {
            new File(file).delete();
        } catch (Exception e) {}
    }

    static void deleteDatabase(String path) {

        delete(path + ".backup");
        delete(path + ".properties");
        delete(path + ".script");
        delete(path + ".data");
        delete(path + ".log");
    }

    static void test(String url, String user, String password,
                     boolean persistent) throws Exception {

        String name = persistent ? "Persistent"
                                 : "Memory";

        print(name);

        Connection cConnection = null;

        try {
            cConnection = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
            print("TestSelf init error: " + e.getMessage());
        }

        testMainScript(cConnection, persistent);
        testTabProfile(cConnection, persistent);
        testMarotest(cConnection, persistent);
        cConnection.createStatement().execute("SHUTDOWN");
        cConnection.close();
    }

    static void testPersistence() {

        deleteDatabase("test1");

        try {
            String     url = "jdbc:hsqldb:test1;sql.enforce_strict_size=true";
            String     user        = "sa";
            String     password    = "";
            Connection cConnection = null;
            String[]   filelist;
            String     absolute = new File("TestSelf.txt").getAbsolutePath();

            filelist = new File(new File(absolute).getParent()).list();

            Sort.sort((Object[]) filelist, new Sort.StringComparator(), 0,
                      filelist.length - 1);

            for (int i = 0; i < filelist.length; i++) {
                String fname = filelist[i];

                if (fname.startsWith("TestSelf") && fname.endsWith(".txt")
                        &&!fname.equals("TestSelf.txt")) {
                    print("Openning DB");

                    cConnection = DriverManager.getConnection(url, user,
                            password);

                    testScript(cConnection, fname);
                    cConnection.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            print("TestSelf init error: " + e.getMessage());
        }
    }

    static void testMainScript(Connection cConnection, boolean persistent) {

        String name = persistent ? "Persistent"
                                 : "Memory";

        print(name + " TestScript");

        // location of TestSelf.txt relative to the development environment
        String path = "TestSelf.txt";

        testScript(cConnection, path);
    }

    static byte[] b1 = {
        0, 1, -128, 44, 12
    };
    static byte[] b2 = {
        10, 127
    };

    static void testTabProfile(Connection cConnection, boolean persistent) {

        Statement sStatement = null;
        ResultSet r;
        String    s = "";
        long      start;
        boolean   bDropError = false;
        String    name       = persistent ? "Persistent"
                                          : "Memory";

        print(name + " TabProfile");

        try {
            sStatement = cConnection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
            print("TabProfile init error: " + e.getMessage());

            return;
        }

        try {

            // prepared statements
            s = "create table TabProfile(id int primary key,"
                + "car char,won bit,licence varbinary,"
                + "name char,sex char,chance double,birthday date,temp char)";

            sStatement.execute(s);

            s = "insert into TabProfile values ( ?, ?, ?, ?,"
                + "'\"John\" the bird''s best friend', 'M',?,?,'')";

            PreparedStatement p = cConnection.prepareStatement(s);

            p.clearParameters();
            p.setInt(1, 10);
            p.setString(2, "Matchcartoon");
            p.setBoolean(3, true);
            p.setBytes(4, b1);
            p.setDouble(5, 50.5);
            p.setNull(6, Types.DATE);
            p.executeUpdate();
            p.clearParameters();
            p.setInt(1, -2);
            p.setString(2, "\"Birdie\"'s car ?");
            p.setBoolean(3, false);

            byte[] b2 = {
                10, 127
            };

            p.setBytes(4, b2);
            p.setDouble(5, -3.1415e-20);

            java.util.Calendar cal = java.util.Calendar.getInstance();

            cal.set(2000, 2, 29);

            // fredt@users - who designed the java.util.Calendar API?
            p.setDate(6, new Date(cal.getTime().getTime()));
            p.executeUpdate();
            readTabProfileTest(sStatement);

            byte[]  b2n;
            byte[]  b1n;
            boolean mismatch;

            s = "select \"org.hsqldb.lib.ArrayUtil.containsAt\"(licence,0, ?) "
                + "from TabProfile";
            p = cConnection.prepareStatement(s);

            p.setBytes(1, b2);

            r = p.executeQuery();

            r.next();

            boolean boo1 = r.getBoolean(1);

            r.next();

            boolean boo2 = r.getBoolean(1);

            // test boo1 != boo2

/** @todo fredt - nested procedure call doesn't parse  */
/*
            s = "select \"org.hsqldb.lib.StringConverter.hexToByte\""
                + "(\"org.hsqldb.lib.StringConverter.byteToHex\"(car)) "
                + "from TabProfile";
            r = sStatement.executeQuery(s);

            r.next();

            b1n = r.getBytes(1);

            r.next();

            b1n = r.getBytes(1);
*/

/** @todo fredt - alias does not resolve */
/*
            s = "select \"org.hsqldb.lib.StringConverter.byteToHex\"(car) temp, " +
                "\"org.hsqldb.lib.StringConverter.hexToByte\"(temp) "
                + "from TabProfile";
            r = sStatement.executeQuery(s);

            r.next();

            b1n = r.getBytes(2);

            r.next();

            b1n = r.getBytes(2);
*/
            s = "update tabprofile set temp = \"org.hsqldb.lib.StringConverter.byteToHex\"(licence)";

            sStatement.executeUpdate(s);

            s = "select \"org.hsqldb.lib.StringConverter.hexToByte\"(temp) "
                + "from TabProfile order by id desc";
            r = sStatement.executeQuery(s);

            r.next();

            b1n = r.getBytes(1);

            for (int i = 0; i < b1n.length; i++) {
                if (b1[i] != b1n[i]) {
                    mismatch = true;
                }
            }

            r.next();

            b2n = r.getBytes(1);

            for (int i = 0; i < b2n.length; i++) {
                if (b2[i] != b2n[i]) {
                    mismatch = true;
                }
            }

//            s = "drop table TabProfile";
//            sStatement.execute(s);
            s = "create table obj(id int,o object)";

            sStatement.execute(s);

            s = "insert into obj values(?,?)";
            p = cConnection.prepareStatement(s);

            p.setInt(1, 1);

            int[] ia1 = {
                1, 2, 3
            };

            p.setObject(2, ia1);
            p.executeUpdate();
            p.clearParameters();
            p.setInt(1, 2);

            java.awt.Rectangle r1 = new java.awt.Rectangle(10, 11, 12, 13);

            p.setObject(2, r1);
            p.executeUpdate();

            r = sStatement.executeQuery("SELECT o FROM obj ORDER BY id DESC");

            r.next();

            java.awt.Rectangle r2 = (java.awt.Rectangle) r.getObject(1);

            if (r2.x != 10 || r2.y != 11 || r2.width != 12
                    || r2.height != 13) {
                throw new Exception("Object data error: Rectangle");
            }

            r.next();

            int[] ia2 = (int[]) (r.getObject(1));

            if (ia2[0] != 1 || ia2[1] != 2 || ia2[2] != 3
                    || ia2.length != 3) {
                throw new Exception("Object data error: int[]");
            }

//            s = "drop table obj";
//            sStatement.execute(s);
            sStatement.close();
        } catch (Exception e) {
            print("");
            print("TabProfile error: " + e);
            print("with SQL command: " + s);
            e.printStackTrace();
        }
    }

    static void readTabProfileTest(Statement sStatement) throws Exception {

        String    s = "select * from TabProfile where id=-2";
        ResultSet r = sStatement.executeQuery(s);

        r.next();

        if (!r.getString(2).equals("\"Birdie\"'s car ?")) {
            throw new Exception("Unicode error.");
        }

        boolean mismatch = false;
        byte[]  b2n      = r.getBytes(4);

        for (int i = 0; i < b2n.length; i++) {
            if (b2[i] != b2n[i]) {
                mismatch = true;
            }
        }

        r.close();

        s = "select * from TabProfile where id=10";
        r = sStatement.executeQuery(s);

        r.next();

        byte[] b1n = r.getBytes(4);

        for (int i = 0; i < b1n.length; i++) {
            if (b1[i] != b1n[i]) {
                mismatch = true;
            }
        }

        r.close();
    }

    static void testMarotest(Connection cConnection, boolean persistent) {

        Statement sStatement = null;
        ResultSet r;
        String    s = "";
        long      start;
        boolean   bDropError = false;
        String    name       = persistent ? "Persistent"
                                          : "Memory";

        print(name + " Marotest");

        try {
            sStatement = cConnection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
            print("Marotest init error: " + e.getMessage());
        }

        try {

            // test duplicate keys & small transaction rollback
            s = "CREATE TABLE marotest (id int PRIMARY KEY, dat int);"
                + "INSERT INTO marotest VALUES (1,0);"
                + "INSERT INTO marotest VALUES (2,0);"
                + "INSERT INTO marotest VALUES (2,0);";

            try {
                sStatement.execute(s);

                s = "";
            } catch (Exception e) {}

            if (s.equals("")) {
                throw new Exception("Duplicate key gave no error on insert");
            }

            try {
                s = "UPDATE marotest SET id=1, dat=-1 WHERE dat=0";

                sStatement.execute(s);

                s = "";
            } catch (Exception e) {}

            if (s.equals("")) {
                throw new Exception("Duplicate key gave no error on update");
            }

            int count = 0;

            s = "SELECT *, id as marotest_id FROM marotest";
            r = sStatement.executeQuery(s);

            while (r.next()) {
                r.getFloat(1);
                r.getString("ID");
                r.getInt("DAT");
                r.getInt("MAROTEST_ID");

                if (r.getShort("DAT") != 0) {
                    throw new Exception("Bad update worked");
                }

                r.getLong("DAT");
                r.getString(2);
                r.getObject("ID");
                r.clearWarnings();

                try {

                    // this must throw an error
                    r.getTimestamp("Timestamp?");

                    count = 99;
                } catch (Exception e) {}

                count++;
            }

            r.close();

            if (count != 2) {
                throw new Exception("Should have 2 but has " + count
                                    + " rows");
            }

            // test database meta data
            DatabaseMetaData dbMeta = cConnection.getMetaData();

            r = dbMeta.getColumns(null, "DBO", "MAROTEST", "%");

            while (r.next()) {
                s = r.getString(4).trim();    // COLUMN_NAME

                int i = r.getInt(5);          // DATA_TYPE

                s += i + r.getString("TYPE_NAME");
                i = r.getInt(7);              // COLUMN_SIZE
                i = r.getInt(9);              // "Decimal_Digits"
                i = r.getInt(11);             // NULLABLE
                s = s.toUpperCase();

                if (!s.equals("ID4INTEGER") &&!s.equals("DAT4INTEGER")) {
                    throw new Exception("Wrong database meta data");
                }
            }

            s = "DROP TABLE marotest";

            sStatement.execute(s);
            sStatement.close();
        } catch (Exception e) {
            print("");
            print("Marotest error: " + e);
            print("with SQL command: " + s);
            e.printStackTrace();
        }
    }

    static void testPerformance(String url, String user, String password,
                                int max,
                                boolean persistent) throws Exception {

        if (persistent) {
            deleteDatabase("test2");
        }

        Statement  sStatement  = null;
        Connection cConnection = null;
        ResultSet  r;
        String     s = "";
        long       start;
        boolean    bDropError = false;
        String     name       = persistent ? "Persistent"
                                           : "Memory";

        print(name + " Performance");

        try {
            cConnection = DriverManager.getConnection(url, user, password);
            sStatement  = cConnection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
            print("TestSelf init error: " + e.getMessage());
        }

        try {

            // cache, index and performance tests
            s = "CREATE CACHED TABLE Addr(ID INT PRIMARY KEY,First CHAR,"
                + "Name CHAR,ZIP INT)";

            sStatement.execute(s);

            s = "CREATE INDEX iName ON Addr(Name)";

            sStatement.execute(s);

            s = "SET WRITE_DELAY TRUE";

            sStatement.execute(s);

            start = System.currentTimeMillis();

            for (int i = 0; i < max; i++) {
                s = "INSERT INTO Addr VALUES(" + i + ",'Marcel" + i + "',"
                    + "'Renggli" + (max - i - (i % 31)) + "',"
                    + (3000 + i % 100) + ")";

                if (sStatement.executeUpdate(s) != 1) {
                    throw new Exception("Insert failed");
                }

                if (i % 100 == 0) {
                    printStatus("insert   ", i, max, start);
                }
            }

            printStatus("insert   ", max, max, start);
            print("");

            s = "SELECT COUNT(*) FROM Addr";
            r = sStatement.executeQuery(s);

            r.next();

            int c = r.getInt(1);

            if (c != max) {
                throw new Exception("Count should be " + (max) + " but is "
                                    + c);
            }

            if (persistent) {

                // close & reopen to test backup
                cConnection.close();

                cConnection = DriverManager.getConnection(url, user,
                        password);
                sStatement = cConnection.createStatement();
            }

            start = System.currentTimeMillis();

            for (int i = 0; i < max; i++) {
                s = "UPDATE Addr SET Name='Robert" + (i + (i % 31))
                    + "' WHERE ID=" + i;

                if (sStatement.executeUpdate(s) != 1) {
                    throw new Exception("Update failed");
                }

                if (i % 100 == 0) {
                    printStatus("updated  ", i, max, start);

                    // s="SELECT COUNT(*) FROM Addr";
                    // r=sStatement.executeQuery(s);
                    // r.next();
                    // int c=r.getInt(1);
                    // if(c!=max) {
                    // throw new Exception("Count should be "+max+" but is "+c);
                    // }
                }
            }

            printStatus("update   ", max, max, start);
            print("");

            if (persistent) {
                s = "SHUTDOWN IMMEDIATELY";

                sStatement.execute(s);

                // open the database; it must be restored after shutdown
                cConnection.close();

                cConnection = DriverManager.getConnection(url, user,
                        password);
                sStatement = cConnection.createStatement();
            }

            start = System.currentTimeMillis();

            for (int i = 0; i < max; i++) {
                s = "DELETE FROM Addr WHERE ID=" + (max - 1 - i);

                if (sStatement.executeUpdate(s) != 1) {
                    throw new Exception("Delete failed");
                }

                if (i % 100 == 0) {
                    printStatus("deleting ", i, max, start);

                    // s="SELECT COUNT(*) FROM Addr";
                    // r=sStatement.executeQuery(s);
                    // r.next();
                    // int c=r.getInt(1);
                    // if(c!=max-i-1) {
                    // throw new Exception("Count should be "+(max-i-1)+" but is "+c);
                    // }
                }
            }

            printStatus("delete   ", max, max, start);
            print("");
            sStatement.execute("DROP TABLE Addr");
        } catch (Exception e) {
            print("");
            print("TestSelf error: " + e);
            print("with SQL command: " + s);
            e.printStackTrace();
        }

        cConnection.close();
        print("Test finished");
    }

    /**
     *  Method declaration
     *
     * @param  s
     * @param  i
     * @param  max
     * @param  start
     */
    static void printStatus(String s, int i, int max, long start) {

        System.out.print(s + ": " + i + "/" + max + " " + (100 * i / max)
                         + "% ");

        long now = System.currentTimeMillis();

        if (now > start) {
            System.out.print((i * 1000 / (now - start)));
        }

        System.out.print(" rows/s                \r");
    }
}
