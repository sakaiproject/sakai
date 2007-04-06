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


/*
 * TestSubQueriesInPreparedStatements.java
 *
 * Created on July 9, 2003, 4:03 PM
 */
package org.hsqldb.test;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author boucherb@users
 */
public class TestSubQueriesInPreparedStatements {

    public static void main(String[] args) throws Exception {
        test();
    }

    public static void test() throws Exception {

        Connection        conn;
        Statement         stmnt;
        PreparedStatement pstmnt;
        Driver            driver;

        driver =
            (Driver) Class.forName("org.hsqldb.jdbcDriver").newInstance();

        DriverManager.registerDriver(driver);

        conn = DriverManager.getConnection("jdbc:hsqldb:mem:test", "sa", "");
        stmnt  = conn.createStatement();
        pstmnt = conn.prepareStatement("drop table t if exists");

        boolean result = pstmnt.execute();

        pstmnt = conn.prepareStatement("create table t(i decimal)");

        int updatecount = pstmnt.executeUpdate();

        pstmnt = conn.prepareStatement("insert into t values(?)");

        for (int i = 0; i < 100; i++) {
            pstmnt.setInt(1, i);
            pstmnt.executeUpdate();
        }

        pstmnt = conn.prepareStatement(
            "select * from (select * from t where i < ?)");

        System.out.println("Expecting: 0..3");
        pstmnt.setInt(1, 4);

        ResultSet rs = pstmnt.executeQuery();

        while (rs.next()) {
            System.out.println(rs.getInt(1));
        }

        System.out.println("Expecting: 0..4");
        pstmnt.setInt(1, 5);

        rs = pstmnt.executeQuery();

        while (rs.next()) {
            System.out.println(rs.getInt(1));
        }

        pstmnt = conn.prepareStatement(
            "select sum(i) from (select i from t where i between ? and ?)");

        System.out.println("Expecting: 9");
        pstmnt.setInt(1, 4);
        pstmnt.setInt(2, 5);

        rs = pstmnt.executeQuery();

        while (rs.next()) {
            System.out.println(rs.getInt(1));
        }

        System.out.println("Expecting: 15");
        pstmnt.setInt(2, 6);

        rs = pstmnt.executeQuery();

        while (rs.next()) {
            System.out.println(rs.getInt(1));
        }

        pstmnt = conn.prepareStatement(
            "select * from (select i as c1 from t where i < ?) a, (select i as c2 from t where i < ?) b");

        System.out.println("Expecting: (0,0)");
        pstmnt.setInt(1, 1);
        pstmnt.setInt(2, 1);

        rs = pstmnt.executeQuery();

        while (rs.next()) {
            System.out.println("(" + rs.getInt(1) + "," + rs.getInt(2) + ")");
        }

        System.out.println("Expecting: ((0,0), (0,1), (1,0), (1,1)");
        pstmnt.setInt(1, 2);
        pstmnt.setInt(2, 2);

        rs = pstmnt.executeQuery();

        while (rs.next()) {
            System.out.println("(" + rs.getInt(1) + "," + rs.getInt(2) + ")");
        }

        System.out.println("Expecting: ((0,0) .. (3,3)");
        pstmnt.setInt(1, 4);
        pstmnt.setInt(2, 4);

        rs = pstmnt.executeQuery();

        while (rs.next()) {
            System.out.println("(" + rs.getInt(1) + "," + rs.getInt(2) + ")");
        }
    }
}
