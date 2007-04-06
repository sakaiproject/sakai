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
import java.sql.Statement;

/**
 * @author kloska@users
 */
class TestPreparedSubQueries {

    private Connection con = null;

    private class sqlStmt {

        boolean prepare;
        boolean update;
        String  command;

        sqlStmt(String c, boolean p, boolean u) {

            prepare = p;
            command = c;
            update  = u;
        }
    }
    ;

    private sqlStmt[] stmtArray = {
        new sqlStmt("drop table a if exists", false, false),
        new sqlStmt("create cached table a (a int identity,b int)", false,
                    false),
        new sqlStmt("create index bIdx on a(b)", false, false),
        new sqlStmt("insert into a(b) values(1)", true, true),
        new sqlStmt("insert into a(b) values(2)", true, true),
        new sqlStmt("insert into a(b) values(3)", true, true),
        new sqlStmt("insert into a(b) values(4)", true, true),
        new sqlStmt("insert into a(b) values(5)", true, true),
        new sqlStmt("insert into a(b) values(6)", true, true),
        new sqlStmt(
            "update a set b=100 where b>(select b from a X where X.a=2)",
            true, true),
        new sqlStmt("update a set b=200 where b>(select b from a where a=?)",
                    true, true),
        new sqlStmt(
            "update a set b=300 where b>(select b from a X where X.a=?)",
            true, true)
    };
    private Object[][] stmtArgs = {
        {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, { new Integer(2) },
        { new Integer(2) }
    };

    public static void main(String[] argv) {

        Connection con = null;

        try {
            String url = "jdbc:hsqldb:test";

            Class.forName("org.hsqldb.jdbcDriver");

            con = java.sql.DriverManager.getConnection(url, "sa", "");

            System.out.println("SciSelect::connect -- connected to '" + url
                               + "'");
        } catch (Exception e) {
            System.out.println(" ?? main: Caught Exception " + e);
            System.out.println(" - FAILED - ");

            return;
        }

        TestPreparedSubQueries t = new TestPreparedSubQueries(con);
        boolean                b = t.test();

        System.out.println(b ? " -- OK -- "
                             : " ?? FAILED ?? ");
        System.exit(0);
    }

    public TestPreparedSubQueries(Connection c) {
        con = c;
    }

    public boolean test() {

        try {
            int i = 0;

            for (i = 0; i < stmtArray.length; i++) {
                int j;

                System.out.println(" -- #" + i + " ----------------------- ");

                if (stmtArray[i].prepare) {
                    PreparedStatement ps = null;

                    System.out.println(" -- preparing\n<<<\n"
                                       + stmtArray[i].command + "\n>>>\n");

                    ps = con.prepareStatement(stmtArray[i].command);

                    System.out.print(" -- setting " + stmtArgs[i].length
                                     + " Args [");

                    for (j = 0; j < stmtArgs[i].length; j++) {
                        System.out.print((j > 0 ? "; "
                                                : "") + stmtArgs[i][j]);
                        ps.setObject(j + 1, stmtArgs[i][j]);
                    }

                    System.out.println("]");
                    System.out.println(" -- executing ");

                    if (stmtArray[i].update) {
                        int r = ps.executeUpdate();

                        System.out.println(" ***** ps.executeUpdate gave me "
                                           + r);
                    } else {
                        boolean b = ps.execute();

                        System.out.print(" ***** ps.execute gave me " + b);
                    }
                } else {
                    System.out.println(" -- executing directly\n<<<\n"
                                       + stmtArray[i].command + "\n>>>\n");

                    Statement s = con.createStatement();
                    boolean   b = s.execute(stmtArray[i].command);

                    System.out.println(" ***** st.execute gave me " + b);
                }
            }
        } catch (Exception e) {
            System.out.println(" ?? Caught Exception " + e);

            return false;
        }

        return true;
    }
}
