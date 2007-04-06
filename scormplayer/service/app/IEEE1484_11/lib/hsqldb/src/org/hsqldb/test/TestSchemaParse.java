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
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class TestSchemaParse extends junit.framework.TestCase
implements org.hsqldb.Trigger {

    Connection                  con = null;
    Statement                   statement;
    private static final String ipref = "INFORMATION_SCHEMA.";

    protected void setUp() throws Exception {

        org.hsqldb.jdbcDriver.class.getName();

        con = DriverManager.getConnection("jdbc:hsqldb:mem:parsetest", "sa",
                                          "");
        statement = con.createStatement();

        execSQL("SET AUTOCOMMIT false", 0);
        execSQL("CREATE TABLE tsttbl (i INT, vc VARCHAR)", 0);
        execSQL("CREATE TABLE bigtbl (i INT, vc VARCHAR, i101 INT, i102 INT, "
                + "i103 INT, i104 INT, i105 INT, i106 INT, i107 INT, "
                + "i108 INT, i109 INT, i110 INT, i111 INT, i112 INT, "
                + "i113 INT, i114 INT, i115 INT, i116 INT, i117 INT, "
                + "i118 INT, i119 INT)", 0);
        execSQL("INSERT INTO tsttbl VALUES (1, 'one')", 1);
        execSQL("INSERT INTO tsttbl VALUES (2, 'two')", 1);
        execSQL("CREATE TABLE joinedtbl (i2 INT, vc2 VARCHAR)", 0);
        execSQL("INSERT INTO joinedtbl VALUES (2, 'zwei')", 1);
        execSQL("CREATE TABLE indexedtbl (i3 INT, vc3 VARCHAR)", 0);
        execSQL("INSERT INTO indexedtbl VALUES (3, 'tres')", 1);
        execSQL("CREATE TABLE triggedtbl (i4 INT, vc4 VARCHAR)", 0);

        // Can't test text tables in memory-only DB.
        //execSQL("CREATE TEXT TABLE texttbl (i5 INT, vc5 VARCHAR)", 0);
        execSQL("INSERT INTO triggedtbl VALUES (4, 'quatro')", 1);
        execSQL("CREATE ALIAS tstali FOR "
                + "\"org.hsqldb.test.BlaineTrig.capitalize\"", 0);
        execSQL("CREATE UNIQUE INDEX tstind ON indexedtbl (i3)", 0);
        execSQL("CREATE SEQUENCE tstseq", 0);
        execSQL("CREATE TRIGGER tsttrig AFTER INSERT ON triggedtbl CALL \""
                + "org.hsqldb.test.BlaineTrig\"", 0);
        execSQL("CREATE USER tstuser PASSWORD fake", 0);
        execSQL("CREATE TABLE constrainedtbl (i6 INT, vc6 VARCHAR, "
                + "CONSTRAINT ucons UNIQUE(i6))", 0);
        execSQL("CREATE TABLE primarytbl (i8 INT, i18 INT, vc8 VARCHAR, "
                + "UNIQUE(i8), UNIQUE(i18))", 0);
        execSQL(
            "CREATE TABLE foreigntbl (i7 INT, vc7 VARCHAR, "
            + "CONSTRAINT tstfk FOREIGN KEY (i7) REFERENCES primarytbl (i8))", 0);
        execSQL("CREATE TABLE playtbl (i9 INT, vc9 VARCHAR)", 0);
        execSQL("CREATE TABLE toindextbl (i10 INT, vc10 VARCHAR)", 0);
        execSQL("INSERT INTO toindextbl VALUES (10, 'zehn')", 1);

        // Do the view last since it can cause dependendies with indexes, etc.
        execSQL("CREATE VIEW tstview AS SELECT * FROM tsttbl WHERE i < 10",
                0);
        execSQL("COMMIT", 0);
    }

    // Want to permit the SHUTDOWN SQL command in tearDown() to fail iff
    // the test method run has tested SHUTDOWN.
    private boolean shutdownTested = false;

    protected void tearDown() throws Exception {

        // Shut down to destroy all of the DB objects (only works because
        // it's an in-memory instance.
        execSQL("SHUTDOWN", shutdownTested);

        if (con != null) {
            con.close();
        }
    }

    public void test2pTables() throws Exception {

        String prefix = "public.";

        execSQL("DROP VIEW tstview", 0);    // Just so deps don't cause problems

        // Select commands
        assertEquals(2, queryRowCount("SELECT i FROM " + prefix
                                      + "tsttbl WHERE i IN (1, 2, 3)"));
        execSQL("SELECT * INTO " + prefix + "newtbl FROM tsttbl", 2);
        assertEquals(2, queryRowCount("SELECT admin FROM " + ipref
                                      + "system_users"));
        assertEquals("Sub-query", 1,
                     queryRowCount("SELECT vc FROM " + prefix
                                   + "tsttbl WHERE i = (\n"
                                   + "    SELECT i2 FROM " + prefix
                                   + "joinedtbl\n" + ")"));
        assertEquals("Join", 1,
                     queryRowCount("SELECT vc FROM " + prefix + "tsttbl, "
                                   + prefix + "joinedtbl\n"
                                   + "WHERE tsttbl.i = joinedtbl.i2\n"
                                   + "AND joinedtbl.vc2 = 'zwei'"));

        // Selects using Labels/Aliases
        assertEquals(
            2, queryRowCount(
                "SELECT ali.i FROM " + prefix
                + "tsttbl ali WHERE ali.i IN (1, 2, 3)"));
        execSQL("SELECT * INTO " + prefix + "newtbl2 FROM tsttbl ali", 2);
        execSQL("SELECT * INTO newtbl3 FROM " + prefix + "tsttbl ali", 2);
        execSQL("SELECT * INTO " + prefix + "newtbl4 FROM " + prefix
                + "tsttbl ali", 2);
        assertEquals(2, queryRowCount("SELECT ali.admin FROM " + ipref
                                      + "system_users ali"));
        assertEquals("Sub-query", 1,
                     queryRowCount("SELECT ali.vc FROM " + prefix
                                   + "tsttbl ali WHERE i = (\n"
                                   + "    SELECT bali.i2 FROM " + prefix
                                   + "joinedtbl bali\n" + ")"));
        assertEquals("Join", 1,
                     queryRowCount("SELECT ali.vc FROM " + prefix
                                   + "tsttbl ali, " + prefix
                                   + "joinedtbl bali\n"
                                   + "WHERE ali.i = bali.i2\n"
                                   + "AND bali.vc2 = 'zwei'"));
        /* Mixed aliases not working yet
        assertEquals("Join", 1, queryRowCount(
          "SELECT ali.vc FROM " + prefix + "tsttbl ali, " + prefix
          + "joinedtbl bali\nWHERE tsttbl.i = joinedtbl.i2\n"
            + "AND bali.vc2 = 'zwei'"));
        */

        // Alter Table commands
        execSQL("ALTER TABLE " + prefix + "playtbl RENAME TO " + prefix
                + "renamedtbl", 0);
        execSQL("ALTER TABLE " + prefix + "renamedtbl RENAME TO " + prefix
                + "playtbl", 0);
        execSQL("ALTER TABLE " + prefix
                + "constrainedtbl ADD CONSTRAINT con1 CHECK (i6 > 4)", 0);
        execSQL("ALTER TABLE " + prefix + "tsttbl ADD COLUMN vco1 VARCHAR",
                0);
        execSQL("ALTER TABLE " + prefix + "tsttbl DROP COLUMN vco1", 0);
        execSQL("ALTER TABLE " + prefix + "tsttbl ADD COLUMN vco1 VARCHAR",
                0);
        execSQL("ALTER TABLE " + prefix
                + "tsttbl ALTER COLUMN vco1 RENAME TO j1", 0);
        execSQL("ALTER TABLE " + prefix
                + "constrainedtbl DROP CONSTRAINT con1", 0);
        execSQL("ALTER TABLE " + prefix + "foreigntbl DROP CONSTRAINT tstfk",
                0);
        execSQL("ALTER TABLE " + prefix
                + "foreigntbl ADD CONSTRAINT tstfk FOREIGN KEY "
                + "(i7) REFERENCES primarytbl (i8)", 0);
        execSQL("ALTER TABLE " + prefix
                + "playtbl ADD CONSTRAINT ucons9 UNIQUE (i9)", 0);

        // Drop table command
        execSQL("DROP TABLE " + prefix + "playtbl", 0);

        // Set table readonly command
        execSQL("SET TABLE " + prefix + "tsttbl READONLY true", 0);
        execSQL("SET TABLE " + prefix + "tsttbl READONLY false", 0);

        // Create table commands
        execSQL("CREATE TABLE " + prefix + "tsttbly (i INT, vc VARCHAR)", 0);
        execSQL("CREATE CACHED TABLE " + prefix
                + "tsttblx (i INT, vc VARCHAR)", 0);
        execSQL("CREATE TABLE constrz (i6 INT, vc6 VARCHAR, "
                + "CONSTRAINT uconsz UNIQUE(i6))", 0);
        execSQL(
            "CREATE TABLE forztbl (i7 INT, vc7 VARCHAR, "
            + "CONSTRAINT tstfkz FOREIGN KEY (i7) REFERENCES primarytbl (i8))", 0);

        // Update command
        execSQL("UPDATE " + prefix + "tsttbl SET vc = 'eleven' WHERE i = 1",
                1);

        // delete
        execSQL("DELETE FROM  " + prefix + "tsttbl WHERE i = 1", 1);

        // grant, revoke
        execSQL("GRANT ALL ON " + prefix + "tsttbl TO tstuser", 0);
        execSQL("REVOKE ALL ON " + prefix + "tsttbl FROM tstuser", 0);
    }

    public void test2pViews() throws Exception {

        String prefix = "public.";

        assertEquals(2, queryRowCount("SELECT i FROM " + prefix
                                      + "tstview WHERE i IN (1, 2, 3)"));
        assertEquals(2, queryRowCount("SELECT i FROM tstview"));
        assertEquals(2, queryRowCount("SELECT ali.i FROM tstview ali"));
        assertEquals("Sub-query", 1,
                     queryRowCount("SELECT vc FROM " + prefix
                                   + "tstview WHERE i = (\n"
                                   + "    SELECT i2 FROM " + prefix
                                   + "joinedtbl\n" + ")"));
        assertEquals("Join", 1,
                     queryRowCount("SELECT vc FROM " + prefix + "tstview, "
                                   + prefix + "joinedtbl\n"
                                   + "WHERE tstview.i = joinedtbl.i2\n"
                                   + "AND joinedtbl.vc2 = 'zwei'"));
        assertEquals(
            2, queryRowCount(
                "SELECT i FROM " + prefix
                + "tstview ali WHERE ali.i IN (1, 2, 3)"));

        // view
        execSQL("CREATE VIEW " + prefix
                + "tstview2 AS SELECT * FROM tsttbl WHERE i < 10", 0);

        // grant, revoke
        execSQL("GRANT ALL ON " + prefix + "tstview TO tstuser", 0);
        execSQL("REVOKE ALL ON " + prefix + "tstview FROM tstuser", 0);

        // drop
        execSQL("DROP VIEW tstview", 0);
    }

    public void test2pSequences() throws Exception {

        String prefix = "public.";

        execSQL("CREATE SEQUENCE " + prefix + "tstseq2", 0);
        execSQL("ALTER SEQUENCE " + prefix + "tstseq RESTART WITH 23", 0);
        assertEquals(1, queryRowCount("SELECT next value FOR " + prefix
                                      + "tstseq FROM tsttbl WHERE i = 1"));
        execSQL("DROP SEQUENCE " + prefix + "tstseq", 0);
    }

    public void test2pConstraints() throws Exception {

        String prefix = "public.";

        // Some named constraints
        execSQL("CREATE TABLE constbl1 (i11 INT, vc12 VARCHAR, "
                + "CONSTRAINT " + prefix + "uconsw UNIQUE(vc12))", 0);
        execSQL("CREATE TABLE constbl2 (i11 INT, vc12 VARCHAR, "
                + "CONSTRAINT " + prefix + "chk CHECK (i11 > 4))", 0);
        execSQL("CREATE TABLE for2tbl (i7 INT, vc7 VARCHAR, " + "CONSTRAINT "
                + prefix
                + "tstfk2 FOREIGN KEY (i7) REFERENCES primarytbl (i8))", 0);
        execSQL("CREATE TABLE for3tbl (i7 INT, vc7 VARCHAR, " + "CONSTRAINT "
                + prefix + "tstpk2 PRIMARY KEY (i7))", 0);
        execSQL("ALTER TABLE constrainedtbl ADD CONSTRAINT " + prefix
                + "con1 CHECK (i6 > 4)", 0);
        execSQL("ALTER TABLE foreigntbl ADD CONSTRAINT " + prefix
                + "tstfkm FOREIGN KEY "
                + "(i7) REFERENCES primarytbl (i18)", 0);
        execSQL("ALTER TABLE for3tbl DROP CONSTRAINT " + prefix + "tstpk2",
                0);
    }

    public void test2pIndexes() throws Exception {

        String prefix = "public.";

        execSQL("CREATE UNIQUE INDEX playind ON playtbl (i9)", 0);
        execSQL("CREATE UNIQUE INDEX bigind ON bigtbl (i)", 0);
        execSQL("CREATE UNIQUE INDEX " + prefix + "tstind2 ON tsttbl (i)", 0);
        execSQL("ALTER INDEX " + prefix + "playind RENAME TO renamedind", 0);
        execSQL("ALTER INDEX " + prefix + "renamedind RENAME TO " + prefix
                + "tstind22", 0);
        execSQL("ALTER INDEX tstind RENAME TO " + prefix + "renamedind", 0);
        execSQL("DROP INDEX " + prefix + "bigind", 0);
    }

    public void test2pAliases() throws Exception {

        String prefix = "public.";

        // All occurrences of "expect" in this method indicate bugs.
        // When fixed, don't change the value of "expect" in the method body.
        int expect = 0;

        expect = SQL_ABORT;

        execSQL("CREATE ALIAS " + prefix + "tstalias "
                + "FOR \"org.hsqldb.test.BlaineTrig.capitalize\"", expect);

        // Following should not throw an exception:
        /*
        assertEquals(
            expect, queryRowCount(
                "SELECT " + prefix + "tstalias('helo') FROM tsttbl WHERE i = 1"));
        */
    }

    public void test2pTriggers() throws Exception {

        String prefix = "public.";

        execSQL("CREATE TRIGGER " + prefix
                + "tsttrig2 AFTER INSERT ON triggedtbl "
                + "CALL \"org.hsqldb.test.BlaineTrig\"", 0);
        execSQL("DROP TRIGGER " + prefix + "tsttrig", 0);
    }

    public void testSanityCheck() throws Exception {

        // All occurrences of "expect" in this method indicate bugs.
        // When fixed, change the value of "expect" to 0:
        int expect = SQL_ABORT;

        // The most basic CREATEs and INSERTs would have already failed
        // in the setup method.
        // Get rid of view early so it doesn't cause dependency problems.
        assertEquals(2, queryRowCount("SELECT i FROM tstview"));
        execSQL("DROP VIEW tstview", 0);
        execSQL("CREATE CACHED TABLE cachtbl (i INT, vc VARCHAR)", 0);
        execSQL("SET TABLE tsttbl READONLY true", 0);
        execSQL("SET TABLE tsttbl READONLY false", 0);
        execSQL("INSERT INTO tsttbl VALUES (11, 'eleven')", 1);
        assertEquals(1, queryRowCount("SELECT i FROM tsttbl WHERE i = 1"));
        assertEquals(
            2, queryRowCount("SELECT i FROM tsttbl WHERE i IN (1, 2, 3)"));
        execSQL("ALTER SEQUENCE tstseq RESTART WITH 13", 0);
        execSQL("ALTER TABLE playtbl RENAME TO renamedtbl", 0);
        execSQL("ALTER TABLE renamedtbl RENAME TO playtbl", 0);
        execSQL("DROP INDEX tstind", 0);
        execSQL("DROP TABLE bigtbl", 0);
        execSQL("DROP SEQUENCE tstseq", 0);
        execSQL("SET LOGSIZE 5", 0);

        // Following syntax is now obsolete.
        execSQL("SET PROPERTY \"hsqldb.first_identity\" 4", SQL_ABORT);
        execSQL("UPDATE tsttbl SET vc = 'eleven' WHERE i = 1", 1);
        execSQL(
            "ALTER TABLE constrainedtbl ADD CONSTRAINT con1 CHECK (i6 > 4)",
            0);

        // Can't test text tables in in-memory DB.
        execSQL("COMMIT", 0);
        execSQL("DELETE FROM tsttbl WHERE i < 10", 2);
        assertEquals(1, queryRowCount("SELECT i FROM tsttbl"));
        execSQL("ROLLBACK", 0);
        assertEquals(3, queryRowCount("SELECT i FROM tsttbl"));

        // Remember that inserts must change after adding a column.
        execSQL("ALTER TABLE tsttbl ADD COLUMN vco1 VARCHAR", 0);
        execSQL("ALTER TABLE tsttbl DROP COLUMN vco1", 0);
        execSQL("CREATE UNIQUE INDEX tstind ON tsttbl (i)", 0);
        execSQL("SET AUTOCOMMIT true", 0);
        execSQL("SET AUTOCOMMIT false", 0);
        execSQL("SET IGNORECASE true", 0);
        execSQL("SET IGNORECASE false", 0);
        execSQL("SET PASSWORD blah", 0);
        execSQL("SET PASSWORD 'blah'", 0);
        execSQL("SET REFERENTIAL_INTEGRITY true", 0);
        execSQL("GRANT ALL ON playtbl TO tstuser", 0);
        execSQL("REVOKE ALL ON playtbl FROM tstuser", 0);

// TODO:  These should not throw a Null Pointer exception.
        execSQL("ALTER INDEX tstind RENAME TO renamedind", 0);
        execSQL("ALTER INDEX renamedind RENAME TO tstind", 0);
        execSQL("ALTER USER tstuser SET PASSWORD frank", 0);
        execSQL("ALTER USER tstuser SET PASSWORD 'frank'", 0);
        execSQL("ALTER TABLE tsttbl ADD COLUMN vco1 VARCHAR", 0);
        execSQL("ALTER TABLE tsttbl ALTER COLUMN vco1 RENAME TO j1", 0);
        execSQL("ALTER TABLE constrainedtbl DROP CONSTRAINT con1", 0);
        execSQL("ALTER TABLE foreigntbl DROP CONSTRAINT tstfk", 0);
        execSQL("ALTER TABLE foreigntbl ADD CONSTRAINT tstfk FOREIGN KEY "
                + "(i7) REFERENCES primarytbl (i8)", 0);
        assertEquals("Sub-query", 1,
                     queryRowCount("SELECT vc FROM tsttbl WHERE i = (\n"
                                   + "    SELECT i2 FROM joinedtbl\n" + ")"));
        assertEquals(
            "Join", 1,
            queryRowCount(
                "SELECT vc FROM tsttbl, joinedtbl WHERE tsttbl.i = joinedtbl.i2\n"
                + "AND joinedtbl.vc2 = 'zwei'"));

        // Over-specified table names
        assertEquals(
            "Over-specified Query 1", 1,
            queryRowCount("SELECT tsttbl.i FROM tsttbl WHERE tsttbl.i = 1"));
        assertEquals(
            "Over-specified Query 2", 1,
            queryRowCount("SELECT tsttbl.i FROM tsttbl WHERE i = 1"));
        assertEquals(
            "Over-specified Query 3", 1,
            queryRowCount("SELECT i FROM tsttbl WHERE tsttbl.i = 1"));

        // HSQLDB labels, Oracle aliases
        assertEquals("Trivial Label/alias 1", 1,
                     queryRowCount("SELECT i FROM tsttbl ali WHERE i = 1"));
        assertEquals(
            "Trivial Label/alias 2", 1,
            queryRowCount("SELECT i FROM tsttbl AS ali WHERE i = 1"));
        assertEquals(
            "Trivial Label/alias 3", 1,
            queryRowCount("SELECT ali.i FROM tsttbl ali WHERE i = 1"));
        assertEquals(
            "Trivial Label/alias 4", 1,
            queryRowCount("SELECT i FROM tsttbl ali WHERE ali.i = 1"));
        assertEquals(
            "Trivial Label/alias 5", 1,
            queryRowCount("SELECT ali.i FROM tsttbl ali WHERE ali.i = 1"));

        /**
         * Uncomment when this mixing of aliases and real names is fixed.
         *
         * assertEquals("Mixed Label/aliases 1", 1, queryRowCount(
         *       "SELECT tsttbl.i FROM tsttbl ali WHERE i = 1"));
         * assertEquals("Mixed Label/aliases 2", 1, queryRowCount(
         *       "SELECT i FROM tsttbl ali WHERE tsttbl.i = 1"));
         * assertEquals("Mixed Label/aliases 3", 1, queryRowCount(
         *       "SELECT tsttbl.i FROM tsttbl ali WHERE tsttbl.i = 1"));
         * assertEquals("Mixed Label/aliases 4", 1, queryRowCount(
         *       "SELECT tsttbl.i FROM tsttbl ali WHERE ali.i = 1"));
         * assertEquals("Mixed Label/aliases 5", 1, queryRowCount(
         *       "SELECT ali.i FROM tsttbl ali WHERE tsttbl.i = 1"));
         */
        assertEquals(
            "Join w/Labels/aliases 1", 1,
            queryRowCount(
                "SELECT vc FROM tsttbl ali1, joinedtbl ali2\n"
                + "WHERE i = i2 AND vc2 = 'zwei'"));
        assertEquals(
            "Join w/Labels/aliases 2", 1,
            queryRowCount(
                "SELECT vc FROM tsttbl ali1, joinedtbl ali2\n"
                + "WHERE ali1.i = i2 AND ali2.vc2 = 'zwei'"));
        assertEquals(
            "Join w/Labels/aliases 3", 1,
            queryRowCount(
                "SELECT ali1.vc FROM tsttbl ali1, joinedtbl ali2\n"
                + "WHERE ali1.i = i2 AND ali2.vc2 = 'zwei'"));
        assertEquals(
            "Join w/Labels/aliases 4", 1,
            queryRowCount(
                "SELECT ali1.vc FROM tsttbl ali1, joinedtbl ali2\n"
                + "WHERE i = i2 AND vc2 = 'zwei'"));

        /**
         * Uncomment when this mixing of aliases and real names is fixed.
         * assertEquals("Join w/Mixed Labels/aliases 1", 1, queryRowCount(
         * "SELECT vc FROM tsttbl ali1, joinedtbl ali2\n"
         * + "WHERE tsttbl.i = i2 AND vc2 = 'zwei'"));
         * assertEquals("Join w/Mixed Labels/aliases 2", 1, queryRowCount(
         * "SELECT vc FROM tsttbl ali1, joinedtbl ali2\n"
         * + "WHERE tsttbl.i = i2 AND joinedtbl.vc2 = 'zwei'"));
         * assertEquals("Join w/Mixed Labels/aliases 3", 1, queryRowCount(
         * "SELECT ali1.vc FROM tsttbl ali1, joinedtbl ali2\n"
         * + "WHERE ali1.i = i2 AND joinedtbl.vc2 = 'zwei'"));
         * assertEquals("Join w/Mixed Labels/aliases 4", 1, queryRowCount(
         * "SELECT tsttbl.vc FROM tsttbl ali1, joinedtbl ali2\n"
         * + "WHERE ali1.i = i2 AND ali2.vc2 = 'zwei'"));
         * assertEquals("Join w/Mixed Labels/aliases 5", 1, queryRowCount(
         * "SELECT tsttbl.vc FROM tsttbl ali1, joinedtbl ali2\n"
         * + "WHERE i = i2 AND vc2 = 'zwei'"));
         * assertEquals("Join w/Mixed Labels/aliases 6", 1, queryRowCount(
         * "SELECT tsttbl.vc FROM tsttbl ali1, joinedtbl ali2\n"
         * + "WHERE i = i2 AND joinedtbl.vc2 = 'zwei'"));
         */
        execSQL("SET PROPERTY \"hsqldb.first_identity\" 5 bad", expect);
        execSQL("CHECKPOINT bad", expect);
        execSQL("INSERT INTO tsttbl(i, vc) VALUES (12, 'twelve')", 1);
        execSQL("SELECT * INTO newtbl FROM tsttbl", 4);
    }

    public void testTwoPartKeywords() throws Exception {
        multiPartKeywords("public.");
    }

    public void testThreePartKeywords() throws Exception {
        multiPartKeywords("alpha.public.");
    }

    public void multiPartKeywords(String pref) throws Exception {

        /*
         *  Search for "expect =".  This indicates a bug that needs fixing.
         */
        /*
         * IMPORTANT!!!!  When fixed, the method should NOT change the
         * expect value from SQL_ABORT.
         * Where "expect" is used there is always a real error.
         */
        int expect = SQL_ABORT;

        // If > 2 name parts.  E.g. "x.y.z".
        boolean manyParter = (pref.lastIndexOf('.') != pref.indexOf('.'));

        // Prep for we will attempt to drop later
        execSQL("DROP VIEW tstview", 0);                              // Don't want dep. problems
        execSQL("CREATE TABLE adroptbl (i INT, vc VARCHAR)", 0);
        execSQL("CREATE TABLE bdroptbl (i INT, vc VARCHAR)", 0);
        execSQL("CREATE UNIQUE INDEX adropind ON adroptbl (i)", 0);
        execSQL("CREATE UNIQUE INDEX bdropind ON bdroptbl (i)", 0);
        execSQL("CREATE SEQUENCE bdropseq", 0);
        execSQL("CREATE SEQUENCE adropseq", 0);
        execSQL("CREATE TRIGGER adroptrig AFTER INSERT ON adroptbl CALL \""
                + "org.hsqldb.test.BlaineTrig\"", 0);
        execSQL("CREATE TRIGGER bdroptrig AFTER INSERT ON bdroptbl CALL \""
                + "org.hsqldb.test.BlaineTrig\"", 0);
        execSQL("CREATE VIEW adropviewx AS SELECT * FROM adroptbl", 0);
        execSQL("CREATE VIEW bdropviewx AS SELECT * FROM bdroptbl", 0);
        execSQL("ALTER TABLE playtbl ADD COLUMN newc VARCHAR", 0);    // prep
        execSQL("SET TABLE tsttbl READONLY false", 0);                // reset
        execSQL("SET TABLE tsttbl READONLY " + pref + "true", expect);
        execSQL(pref + "CREATE SEQUENCE tstseqa", expect);
        execSQL(pref + "SET PROPERTY \"hsqldb.first_identity\" 4", expect);
        execSQL("SET " + pref + "PROPERTY \"hsqldb.first_identity\" 4",
                expect);

        /* This block not keywords, but other non-Strings */
        execSQL("SELECT i FROM tsttbl WHERE i = " + pref + "1", expect);
        execSQL("SELECT i FROM tsttbl WHERE vc = " + pref + "'1.3'", expect);
        execSQL("SELECT i FROM tsttbl WHERE vc = " + pref + "1", expect);
        execSQL("SELECT i FROM tsttbl WHERE i = " + pref + "'1.3'", expect);
        execSQL("SELECT i FROM tsttbl WHERE " + pref + "1 = " + pref + "1",
                expect);
        execSQL("SELECT i FROM tsttbl WHERE " + pref + "'1.3' = " + pref
                + "'1.3'", expect);
        execSQL("SELECT i FROM tsttbl WHERE " + pref + "true = " + pref
                + "true", expect);
        execSQL("SELECT i FROM tsttbl WHERE i " + pref + "IN (2, 4)", expect);
        execSQL("SELECT i FROM tsttbl WHERE i < 3 y.AND i > 0", expect);
        execSQL("SELECT i FROM tsttbl WHERE i < y.3 AND i > 0", expect);
        execSQL("INSERT INTO tsttbl VALUES (" + pref + "1, 'one')", expect);
        execSQL("CREATE VIEW tstviewx AS SELECT " + pref
                + "* FROM tsttbl WHERE i < 10", expect);
        execSQL("DROP VIEW tstviewx IF EXISTS", 0);                   // reset
        execSQL("INSERT INTO tsttbl VALUES (1, " + pref + "'one')", expect);
        execSQL("CREATE UNIQUE INDEX tstinda ON toindextbl (" + pref
                + "i10)", expect);
        execSQL("DROP INDEX tstinda IF EXISTS", 0);                   // reset
        execSQL("CREATE VIEW tstviewx AS SELECT * FROM tsttbl WHERE i < "
                + pref + "10", expect);
        execSQL("DROP VIEW tstviewx IF EXISTS", 0);                   // reset
        execSQL("xDROP VIEW adropview", expect);
        execSQL("DROP xVIEW bdropview", expect);
        execSQL("xDROP TRIGGER adroptrig", expect);
        execSQL("DROP xTRIGGER bdroptrig", expect);
        execSQL("xDROP INDEX adropind", expect);
        execSQL("DROP xINDEX bdropind", expect);
        execSQL("xDROP TABLE adroptbl", expect);
        execSQL("DROP xTABLE bdroptbl", expect);
        execSQL("xDROP SEQUENCE adropseq", expect);
        execSQL("DROP xSEQUENCE bdropseq", expect);
        execSQL("SET LOGSIZE " + pref + "5", expect);

        // Can't test text tables in in-memory DB.
        execSQL(pref + "SET TABLE texttbl SOURCE \"test.csv;fs=|\"", expect);
        execSQL("SET " + pref + "TABLE texttbl SOURCE \"test.csv;fs=|\"",
                expect);
        execSQL("SET TABLE texttbl " + pref + "SOURCE \"test.csv;fs=|\"",
                expect);
        execSQL("SET TABLE texttbl SOURCE " + pref + "\"test.csv;fs=|\"",
                expect);
        execSQL("UPDATE tsttbl SET vc = " + pref + "'eleven' WHERE i = 1",
                expect);
        execSQL("UPDATE tsttbl SET vc = 'eleven' WHERE i = " + pref + "1",
                expect);
        execSQL("ALTER SEQUENCE tstseq RESTART WITH " + pref + "13", expect);
        execSQL("ALTER TABLE constrainedtbl ADD CONSTRAINT con1 CHECK (i6 > "
                + pref + "4)", expect);
        execSQL(pref + "INSERT INTO tsttbl VALUES (1, 'one')", expect);
        execSQL("INSERT " + pref + "INTO tsttbl VALUES (1, 'one')", expect);

        if (!manyParter) {
            expect = 1;
        }

        execSQL("INSERT INTO " + pref + "tsttbl VALUES (1, 'one')", expect);

        expect = SQL_ABORT;

        execSQL(pref + "DELETE FROM tsttbl WHERE i < 10", expect);
        execSQL("SELECT vc FROM " + pref + "tsttbl, " + pref
                + "joinedtbl WHERE tsttbl.i = joinedtbl.i2\n"
                + "AND joinedtbl.vc2 = 'zwei'", (manyParter ? SQL_ABORT
                                                            : SQL_FAIL));
        execSQL(pref + "SELECT i FROM tsttbl", expect);
        execSQL("SELECT i " + pref + "FROM tsttbl", expect);
        execSQL("SELECT i FROM tsttbl " + pref + "WHERE i > 0", expect);
        execSQL(pref + "CREATE ALIAS alpha.tstalia "
                + "FOR \"org.hsqldb.test.BlaineTrig.capitalize\"", expect);
        execSQL("CREATE " + pref + "ALIAS tstalib "
                + "FOR \"org.hsqldb.test.BlaineTrig.capitalize\"", expect);
        execSQL("CREATE ALIAS tstalic " + pref
                + "FOR \"org.hsqldb.test.BlaineTrig.capitalize\"", expect);
        execSQL("CREATE ALIAS tstalid " + "FOR " + pref
                + "\"org.hsqldb.test.BlaineTrig.capitalize\"", expect);
        execSQL("ALTER " + pref + "TABLE playtbl DROP COLUMN newc", expect);
        execSQL("CREATE " + pref + "SEQUENCE tstseqb", expect);
        execSQL("CREATE " + pref
                + "TRIGGER tsttrigx AFTER INSERT ON triggedtbl CALL '"
                + "org.hsqldb.test.BlaineTrig'", expect);
        execSQL("CREATE " + pref + "USER tstusera PASSWORD fake", expect);
        execSQL("CREATE VIEW tstviewx " + pref
                + "AS SELECT * FROM tsttbl WHERE i < 10", expect);
        execSQL("DROP VIEW tstviewx IF EXISTS", 0);    // reset
        execSQL("CREATE UNIQUE " + pref
                + "INDEX tstinda ON toindextbl (i10)", expect);
        execSQL("DROP INDEX tstinda IF EXISTS", 0);    // reset
        execSQL("CREATE " + pref + "INDEX tstinda ON toindextbl (i10)",
                expect);
        execSQL("DROP INDEX tstinda IF EXISTS", 0);    // reset
        execSQL("CREATE TRIGGER tsttrigy " + pref
                + "AFTER INSERT ON triggedtbl CALL \""
                + "org.hsqldb.test.BlaineTrig\"", expect);
        execSQL("CREATE USER tstuserb " + pref + "PASSWORD fake", expect);
        execSQL("CREATE VIEW tstviewx AS " + pref
                + "SELECT * FROM tsttbl WHERE i < 10", expect);
        execSQL("DROP VIEW tstviewx IF EXISTS", 0);    // reset
        execSQL("CREATE UNIQUE INDEX tstinda " + pref
                + "ON toindextbl (i10)", expect);
        execSQL("DROP INDEX tstinda IF EXISTS", 0);    // reset
        execSQL("CREATE TRIGGER tsttrigz AFTER " + pref
                + "INSERT ON triggedtbl CALL \""
                + "org.hsqldb.test.BlaineTrig\"", expect);
        execSQL("CREATE VIEW tstviewx AS SELECT * " + pref
                + "FROM tsttbl WHERE i < 10", expect);

        if (!manyParter) {
            expect = 0;
        }

        execSQL("CREATE USER tstuserc PASSWORD " + pref + "fake", expect);

        expect = SQL_ABORT;

        execSQL("DROP VIEW tstviewx IF EXISTS", 0);    // reset
        execSQL("CREATE TRIGGER tsttriga AFTER INSERT " + pref
                + "ON triggedtbl CALL \""
                + "org.hsqldb.test.BlaineTrig\"", expect);
        execSQL("CREATE TRIGGER tsttrigb AFTER INSERT ON triggedtbl " + pref
                + "CALL \"" + "org.hsqldb.test.BlaineTrig\"", expect);
        execSQL("CREATE VIEW tstviewx AS SELECT * FROM tsttbl " + pref
                + "WHERE i < 10", expect);
        execSQL("DROP VIEW tstviewx IF EXISTS", 0);    // reset
        execSQL("CREATE TRIGGER tsttrigc AFTER INSERT ON triggedtbl CALL "
                + pref + "\"org.hsqldb.test.BlaineTrig'", expect);
        execSQL("CREATE " + pref
                + "UNIQUE INDEX tstindx ON toindextbl (i10)", expect);
        execSQL("DROP INDEX tstinda IF EXISTS", 0);    // reset
        execSQL(
            "CREATE " + pref
            + "VIEW tstviewx AS SELECT * FROM tsttbl WHERE i < 10", expect);
        execSQL("DROP VIEW tstviewx IF EXISTS", 0);    // reset
        execSQL(pref + "CREATE USER tstuserd PASSWORD fake", expect);
        execSQL(pref
                + "CREATE TRIGGER tsttrigd AFTER INSERT ON triggedtbl CALL \""
                + "org.hsqldb.test.BlaineTrig\"", expect);
        execSQL(
            pref
            + "CREATE VIEW tstviewx AS SELECT * FROM tsttbl WHERE i < 10", expect);
        execSQL("DROP VIEW tstviewx IF EXISTS", 0);    // reset
        execSQL(pref + "CREATE UNIQUE INDEX tstinda ON toindextbl (i10)",
                expect);
        execSQL("DROP INDEX tstinda IF EXISTS", 0);    // reset
        execSQL("CREATE TABLE t1 (i " + pref + "INT, vc VARCHAR)", expect);
        execSQL("DROP TABLE t1 IF EXISTS", 0);         // reset
        execSQL("CREATE TABLE t1 (i INT, vc " + pref + "VARCHAR)", expect);
        execSQL("DROP TABLE t1 IF EXISTS", 0);         // reset
        execSQL(pref + "CREATE TABLE t1 (i INT, vc VARCHAR)", expect);
        execSQL("DROP TABLE t1 IF EXISTS", 0);         // reset
        execSQL("CREATE " + pref + "TABLE t1 (i INT, vc VARCHAR)", expect);
        execSQL("DROP TABLE t1 IF EXISTS", 0);         // reset
        execSQL("CREATE TABLE t1 (i " + pref + "INT, vc VARCHAR)", expect);
        execSQL("DROP TABLE t1 IF EXISTS", 0);         // reset
        execSQL("CREATE TABLE t1 (i INT, vc " + pref + "VARCHAR)", expect);
        execSQL("DROP TABLE t1 IF EXISTS", 0);         // reset
        execSQL("DELETE " + pref + "FROM tsttbl WHERE i < 10", expect);

        if (!manyParter) {
            expect = 3;
        }

        execSQL("DELETE FROM tsttbl " + pref + "WHERE i < 10", expect);

        expect = SQL_ABORT;

        execSQL(pref + "SET AUTOCOMMIT true", expect);
        execSQL("SET " + pref + "AUTOCOMMIT true", expect);
        execSQL("SET AUTOCOMMIT false", 0);               // reset
        execSQL(pref + "SET IGNORECASE true", expect);
        execSQL("SET " + pref + "IGNORECASE true", expect);
        execSQL(pref + "SET LOGSIZE 5", expect);
        execSQL("SET " + pref + "LOGSIZE 5", expect);
        execSQL(pref + "SET PASSWORD blah", expect);
        execSQL("SET " + pref + "PASSWORD blah", expect);
        execSQL(pref + "SET REFERENTIAL_INTEGRITY true", expect);
        execSQL("SET " + pref + "REFERENTIAL_INTEGRITY true", expect);

        // Can't test text tables in in-memory DB.
        execSQL(pref + "SET SCRIPTFORMAT text", expect);
        execSQL("SET " + pref + "SCRIPTFORMAT text", expect);
        execSQL(pref + "SET TABLE tsttbl READONLY true", expect);
        execSQL("SET " + pref + "TABLE tsttbl READONLY true", expect);
        execSQL("SET TABLE tsttbl READONLY false", 0);    // reset
        execSQL(pref + "GRANT ALL ON playtbl TO tstuser", expect);
        execSQL("GRANT " + pref + "ALL ON playtbl TO tstuser", expect);
        execSQL("GRANT ALL " + pref + "ON playtbl TO tstuser", expect);
        execSQL("GRANT ALL ON playtbl " + pref + "TO tstuser", expect);

        if (!manyParter) {
            expect = 0;
        }

        execSQL("GRANT ALL ON playtbl TO " + pref + "tstuser", expect);

        expect = SQL_ABORT;

        execSQL(pref + "REVOKE ALL ON playtbl FROM tstuser", expect);
        execSQL("REVOKE " + pref + "ALL ON playtbl FROM tstuser", expect);
        execSQL("REVOKE ALL " + pref + "ON playtbl FROM tstuser", expect);
        execSQL("REVOKE ALL ON playtbl " + pref + "FROM tstuser", expect);

        if (!manyParter) {
            expect = 0;
        }

        execSQL("REVOKE ALL ON playtbl FROM " + pref + "tstuser", expect);

        expect = SQL_ABORT;

        execSQL("GRANT ALL ON playtbl TO tstuser", 0);    // reset
        execSQL(pref + "COMMIT", expect);
        execSQL(pref + "ROLLBACK", expect);
        execSQL(pref + "UPDATE tsttbl SET vc = 'eleven' WHERE i = 1", expect);
        execSQL("UPDATE tsttbl " + pref + "SET vc = 'eleven' WHERE i = 1",
                expect);
        execSQL("UPDATE tsttbl SET vc = 'eleven' " + pref + "WHERE i = 1",
                expect);
        execSQL(pref + "ALTER INDEX tstind RENAME TO renamedind", expect);
        execSQL("ALTER INDEX tstind " + pref + "RENAME TO renamedind",
                expect);
        execSQL("ALTER " + pref + "INDEX tstind RENAME TO renamedind",
                expect);
        execSQL("ALTER INDEX tstind RENAME " + pref + "TO renamedind",
                expect);
        execSQL(pref + "ALTER SEQUENCE tstseq RESTART WITH 13", expect);
        execSQL("ALTER " + pref + "SEQUENCE tstseq RESTART WITH 13", expect);
        execSQL("ALTER SEQUENCE tstseq " + pref + "RESTART WITH 13", expect);
        execSQL("ALTER SEQUENCE tstseq RESTART " + pref + "WITH 13", expect);

        if (!manyParter) {
            expect = 0;
        }

        execSQL("ALTER USER tstuser SET PASSWORD " + pref + "frank", expect);

        expect = SQL_ABORT;

        execSQL(pref + "ALTER USER tstuser SET PASSWORD frank", expect);
        execSQL("ALTER " + pref + "USER tstuser SET PASSWORD frank", expect);
        execSQL("ALTER USER tstuser " + pref + "SET PASSWORD frank", expect);
        execSQL("ALTER USER tstuser SET " + pref + "PASSWORD frank", expect);
        execSQL(pref + "ALTER TABLE tsttbl ADD COLUMN vco1 VARCHAR", expect);
        execSQL("ALTER " + pref + "TABLE tsttbl ADD COLUMN vco2 VARCHAR",
                expect);
        execSQL("ALTER TABLE tsttbl " + pref + "ADD COLUMN vco3 VARCHAR",
                expect);
        execSQL("ALTER TABLE tsttbl ADD " + pref + "COLUMN vco4 VARCHAR",
                expect);
        execSQL("ALTER TABLE tsttbl ADD " + pref + "COLUMN vco5 " + pref
                + "VARCHAR", expect);
        execSQL("ALTER TABLE bigtbl DROP " + pref + "COLUMN i103", expect);
        execSQL("ALTER TABLE bigtbl " + pref + "DROP COLUMN i102", expect);
        execSQL(pref + "ALTER TABLE bigtbl DROP COLUMN i101", expect);
        execSQL(pref + "ALTER TABLE bigtbl ALTER COLUMN i104 RENAME TO j1",
                expect);
        execSQL("ALTER " + pref
                + "TABLE bigtbl ALTER COLUMN i105 RENAME TO j2", expect);
        execSQL("ALTER TABLE bigtbl " + pref
                + "ALTER COLUMN i106 RENAME TO j3", expect);
        execSQL("ALTER TABLE bigtbl ALTER " + pref
                + "COLUMN i107 RENAME TO j4", expect);
        execSQL("ALTER TABLE bigtbl ALTER COLUMN i108 " + pref
                + "RENAME TO j5", expect);
        execSQL("ALTER TABLE bigtbl ALTER COLUMN i109 RENAME " + pref
                + "TO j6", expect);
        execSQL(
            pref
            + "ALTER TABLE constrainedtbl ADD CONSTRAINT con2 CHECK (i6 > 4)", expect);
        execSQL(
            "ALTER " + pref
            + "TABLE constrainedtbl ADD CONSTRAINT con3 CHECK (i6 > 4)", expect);
        execSQL("ALTER TABLE constrainedtbl " + pref
                + "ADD CONSTRAINT con4 CHECK (i6 > 4)", expect);
        execSQL(
            "ALTER TABLE constrainedtbl ADD CONSTRAINT con1 CHECK (i6 > 4)",
            true);                                                            // setup
        execSQL(
            "ALTER TABLE constrainedtbl ADD CONSTRAINT con2 CHECK (i6 > 4)",
            true);                                                            // setup
        execSQL(
            "ALTER TABLE constrainedtbl ADD CONSTRAINT con3 CHECK (i6 > 4)",
            true);                                                            // setup
        execSQL(
            "ALTER TABLE constrainedtbl ADD CONSTRAINT con4 CHECK (i6 > 4)",
            true);                                                            // setup
        execSQL("ALTER TABLE constrainedtbl ADD " + pref
                + "CONSTRAINT con5 CHECK (i6 > 4)", expect);
        execSQL("ALTER TABLE constrainedtbl ADD CONSTRAINT con6 " + pref
                + "CHECK (i6 > 4)", expect);
        execSQL("ALTER TABLE constrainedtbl DROP CONSTRAINT ucons", true);    // reset
        execSQL(
            pref
            + "ALTER TABLE constrainedtbl ADD CONSTRAINT ucons UNIQUE (i6)", expect);
        execSQL("ALTER TABLE constrainedtbl DROP CONSTRAINT ucons", true);    // reset
        execSQL(
            "ALTER " + pref
            + "TABLE constrainedtbl ADD CONSTRAINT ucons UNIQUE (i6)", expect);
        execSQL("ALTER TABLE constrainedtbl DROP CONSTRAINT ucons", true);    // reset
        execSQL("ALTER TABLE constrainedtbl " + pref
                + "ADD CONSTRAINT ucons UNIQUE (i6)", expect);
        execSQL("ALTER TABLE constrainedtbl DROP CONSTRAINT ucons", true);    // reset
        execSQL("ALTER TABLE constrainedtbl ADD " + pref
                + "CONSTRAINT ucons UNIQUE (i6)", expect);
        execSQL("ALTER TABLE constrainedtbl DROP CONSTRAINT ucons", true);    // reset
        execSQL("ALTER TABLE constrainedtbl ADD CONSTRAINT ucons " + pref
                + "UNIQUE (i6)", expect);
        execSQL("ALTER TABLE constrainedtbl ADD CONSTRAINT ucons UNIQUE (i6)",
                true);                                                        // reset
        execSQL(pref + "ALTER TABLE playtbl RENAME TO renamedtbl", expect);
        execSQL("ALTER TABLE renamedtbl RENAME TO playtbl", true);            // reset
        execSQL("ALTER " + pref + "TABLE playtbl RENAME TO renamedtbl",
                expect);
        execSQL("ALTER TABLE renamedtbl RENAME TO playtbl", true);            // reset
        execSQL("ALTER TABLE playtbl " + pref + "RENAME TO renamedtbl",
                expect);
        execSQL("ALTER TABLE renamedtbl RENAME TO playtbl", true);            // reset
        execSQL("ALTER TABLE playtbl RENAME " + pref + "TO renamedtbl",
                expect);
        execSQL(pref + "ALTER TABLE constrainedtbl DROP CONSTRAINT con1",
                expect);
        execSQL("ALTER " + pref
                + "TABLE constrainedtbl DROP CONSTRAINT con2", expect);
        execSQL("ALTER TABLE constrainedtbl " + pref
                + "DROP CONSTRAINT con3", expect);
        execSQL("ALTER TABLE constrainedtbl DROP " + pref
                + "CONSTRAINT con4", expect);
        execSQL("ALTER TABLE foreigntbl DROP CONSTRAINT tstfk", true);        // reset
        execSQL(pref
                + "ALTER TABLE foreigntbl ADD CONSTRAINT tstfk FOREIGN KEY "
                + "(i7) REFERENCES primarytbl (i8)", expect);
        execSQL("ALTER TABLE foreigntbl DROP CONSTRAINT tstfk", true);        // reset
        execSQL("ALTER " + pref
                + "TABLE foreigntbl ADD CONSTRAINT tstfk FOREIGN KEY "
                + "(i7) REFERENCES primarytbl (i8)", expect);
        execSQL("ALTER TABLE foreigntbl DROP CONSTRAINT tstfk", true);        // reset
        execSQL("ALTER TABLE foreigntbl " + pref
                + "ADD CONSTRAINT tstfk FOREIGN KEY "
                + "(i7) REFERENCES primarytbl (i8)", expect);
        execSQL("ALTER TABLE foreigntbl DROP CONSTRAINT tstfk", true);        // reset
        execSQL("ALTER TABLE foreigntbl ADD " + pref
                + "CONSTRAINT tstfk FOREIGN KEY "
                + "(i7) REFERENCES primarytbl (i8)", expect);
        execSQL("ALTER TABLE foreigntbl DROP CONSTRAINT tstfk", true);        // reset
        execSQL("ALTER TABLE foreigntbl ADD CONSTRAINT tstfk " + pref
                + "FOREIGN KEY " + "(i7) REFERENCES primarytbl (i8)", expect);
        execSQL("ALTER TABLE foreigntbl DROP CONSTRAINT tstfk", true);        // reset
        execSQL("ALTER TABLE foreigntbl ADD CONSTRAINT tstfk FOREIGN " + pref
                + "KEY " + "(i7) REFERENCES primarytbl (i8)", expect);
        execSQL("ALTER TABLE foreigntbl DROP CONSTRAINT tstfk", true);        // reset
        execSQL("ALTER TABLE foreigntbl ADD CONSTRAINT tstfk FOREIGN KEY "
                + "(i7) " + pref + "REFERENCES primarytbl (i8)", expect);

        /*
        // KEEP THESE TEST CASES AT THE BOTTOM!!!!  Can wreck all following
        // tests in current method, even when this test succeeds.
        // Can only run one successful SHUTDOWN command in one test case.
        execSQL(pref + "SHUTDOWN", SQL_ABORT);
        execSQL(pref + "SHUTDOWN IMMEDIATELY", SQL_ABORT);
        */
        shutdownTested = true;

        /* Failing
        execSQL(pref + "SHUTDOWN BADARG", SQL_ABORT);
        execSQL("Bad SHUTDOWN command did shut down database",
                "SET LOGSIZE " + pref + "5", 0);
        */
        execSQL("SHUTDOWN IMMEDIATELY", 0);
    }

    public void testThreePartNames() throws Exception {
        execSQL("SELECT public.tsttbl.i FROM public.beta.tsttbl\n"
                + "WHERE public.tsttbl.i = 1", SQL_ABORT);
    }

    /**
     * This method seems to be obsolete.
     */
    public void testBasicQueries() throws Exception {

        String prefix = "public.";

        assertEquals(2, queryRowCount("SELECT i FROM " + prefix + "tsttbl"));
        assertEquals(1, queryRowCount("SELECT vc FROM " + prefix
                                      + "tsttbl WHERE i = 1"));
        assertEquals(1, queryRowCount("SELECT vc FROM " + prefix
                                      + "tsttbl WHERE i = (\n"
                                      + "    SELECT i2 FROM " + prefix
                                      + "joinedtbl\n" + ")"));
    }

/** @todo fredt - need to define additional identifiers to use for all cases of expect */
    private static final int SQL_ABORT   = -1234;
    private static final int SQL_INITIAL = -1233;
    private static final int SQL_FAIL    = -1;

    private void execSQL(String s, boolean ignoreError) throws SQLException {

        try {
            statement.execute(s);
            statement.getUpdateCount();
        } catch (SQLException se) {
            if (!ignoreError) {
                throw se;
            }

//else System.err.println("FAILURE of (" + s + ')');
        }
    }

    private void execSQL(String m, String s, int expect) {

        int retval = SQL_INITIAL;

        try {
            statement.execute(s);

            retval = statement.getUpdateCount();
        } catch (SQLException se) {
            retval = SQL_ABORT;
        }

        assertEquals(m, expect, retval);
    }

/** @todo fredt - this method body seems to be incorrect */
    private void execSQL(String s, int expect) {
        execSQL(s, s, expect);
    }

    private int queryRowCount(String query) throws SQLException {

        int count = 0;

        if (!statement.execute(query)) {
            return count;
        }

        ResultSet rs = statement.getResultSet();

        try {
            while (rs.next()) {
                count++;
            }
        } finally {
            rs.close();
        }

        return count;
    }

    private int tableRowCount(String tableName) throws SQLException {

        String query = "SELECT count(*) FROM " + tableName;

        if (!statement.execute(query)) {
            return 0;
        }

        ResultSet rs = statement.getResultSet();

        try {
            if (!rs.next()) {
                throw new SQLException("0 rows returned by (" + query + ')');
            }

            int count = rs.getInt(1);

            if (rs.next()) {
                throw new SQLException("> 1 row returned by (" + query + ')');
            }

            return count;
        } finally {
            rs.close();
        }

        //throw new Exception("Failed to get rowcount for " + tableName);
    }

    public TestSchemaParse() {
        super();
    }

    public TestSchemaParse(String s) {
        super(s);
    }

    public static void main(String[] sa) {

        if (sa.length > 0 && sa[0].startsWith("-g")) {
            junit.swingui.TestRunner.run(TestSchemaParse.class);
        } else {
            junit.textui.TestRunner runner = new junit.textui.TestRunner();

            System.exit(
                runner.run(
                    runner.getTest(
                        TestSchemaParse.class.getName())).wasSuccessful() ? 0
                                                                          : 1);
        }
    }

    public static junit.framework.Test suite() {

        junit.framework.TestSuite newSuite = new junit.framework.TestSuite();

        newSuite.addTest(new TestSchemaParse("testSanityCheck"));
        newSuite.addTest(new TestSchemaParse("testTwoPartKeywords"));
        newSuite.addTest(new TestSchemaParse("testThreePartKeywords"));
        newSuite.addTest(new TestSchemaParse("testThreePartNames"));
        newSuite.addTest(new TestSchemaParse("testBasicQueries"));
        newSuite.addTest(new TestSchemaParse("test2pTables"));
        newSuite.addTest(new TestSchemaParse("test2pViews"));
        newSuite.addTest(new TestSchemaParse("test2pSequences"));
        newSuite.addTest(new TestSchemaParse("test2pIndexes"));
        newSuite.addTest(new TestSchemaParse("test2pAliases"));
        newSuite.addTest(new TestSchemaParse("test2pConstraints"));
        newSuite.addTest(new TestSchemaParse("test2pTriggers"));

        return newSuite;
    }
    ;

    public void fire(int i, String name, String table, Object[] row1,
                     Object[] row2) {}

    public static String capitalize(String inString) {
        return inString.toUpperCase();
    }
}
