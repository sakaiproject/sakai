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
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Random;

/**
 * Test with small cache and very large row inserts
 */
public class TestStressInsert {

    private Connection        con;
    private PreparedStatement insertStmt;
    private static final int  MAX_SIZE = 800000;
    private final Random      random   = new Random(0);
    byte[]                    data     = getRandomBytes(MAX_SIZE);

    public void init() throws Exception {

        String driver = "org.hsqldb.jdbcDriver";
        String url    = "jdbc:hsqldb:file:testing/test";

        Class.forName(driver);

        con = DriverManager.getConnection(url, "sa", "");

        con.setAutoCommit(true);

        // set cache sizes
        Statement stmt = con.createStatement();

        try {
//            stmt.execute("set property \"hsqldb.nio_data_file\" false");
            stmt.execute("set property \"hsqldb.cache_scale\" 8");
            stmt.execute("set property \"hsqldb.cache_size_scale\" 10");
            stmt.execute("set write_delay 0");
            stmt.execute("set logsize " + 100);

            DatabaseMetaData metaData = con.getMetaData();
            ResultSet        rs = metaData.getTables(null, null, "A", null);
            boolean          schemaExists;

            try {
                schemaExists = rs.next();
            } finally {
                rs.close();
            }

            if (!schemaExists) {
                stmt.execute(
                    "create cached table A (ID binary(16) PRIMARY KEY, DATA varbinary not null)");
            }

            stmt.execute("checkpoint");
        } finally {
            stmt.close();
        }

        // prepare statements
        insertStmt =
            con.prepareStatement("insert into A (DATA, ID) values (?, ?)");
    }

    public void shutdown() throws Exception {
        insertStmt.close();
        con.close();
    }

    public void insert(byte[] id) throws Exception {

        try {
            insertStmt.setBytes(1, data);
            insertStmt.setBytes(2, id);
            insertStmt.execute();
        } finally {
            insertStmt.clearParameters();
            insertStmt.clearWarnings();
        }
    }

    public static void main(String[] args) {

        try {
            TestStressInsert test = new TestStressInsert();
            long     t1   = System.currentTimeMillis();

            System.out.print("Initializing...");
            test.init();

            long t2 = System.currentTimeMillis();

            System.out.println("done " + (t2 - t1));

            for (int i = 0; i < MAX_SIZE; i++) {
                test.insert(test.getRandomBytes(16));


                if (i %100 == 0 ) {
                    long t3 = System.currentTimeMillis();
                    System.out.println("inserted " + i + " in " + (t3 - t2));
                    t2 = t3;
                }
            }

            test.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] getRandomBytes(int length) {

        byte[] ret = new byte[length];

        random.nextBytes(ret);

        return ret;
    }
}
