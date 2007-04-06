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
import java.sql.ResultSet;
import java.sql.Statement;

public class TestMultipleConnections {

    public TestMultipleConnections() {}

    public static void main(String[] args) throws Exception {

        // test for bug itme 500105 commit does not work with multiple con. FIXED
        TestMultipleConnections hs   = new TestMultipleConnections();
        Connection              con1 = hs.createObject();
        Connection              con2 = hs.createObject();
        Connection              con3 = hs.createObject();

        con1.setAutoCommit(false);

        //connection1.commit();
        con2.setAutoCommit(false);

        //connection1.commit();
        con3.setAutoCommit(false);

        //connection1.commit();
        Statement st = con3.createStatement();

        st.execute("DROP TABLE T IF EXISTS");
        st.execute("CREATE TABLE T (I INT)");
        st.execute("INSERT INTO T VALUES (2)");

        ResultSet rs = st.executeQuery("SELECT * FROM T");

        rs.next();

        int value = rs.getInt(1);

        con2.commit();
        con3.commit();
        con1.commit();

        rs = st.executeQuery("SELECT * FROM T");

        rs.next();

        if (value != rs.getInt(1)) {
            throw new Exception("value doesn't exist");
        }
    }

    /**
     * create a connection and wait
     */
    protected Connection createObject() {

        try {
            Class.forName("org.hsqldb.jdbcDriver");

            return DriverManager.getConnection("jdbc:hsqldb:/hsql/test/test",
                                               "sa", "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
