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


package org.hsqldb.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.StringTokenizer;

/* $Id: RCData.java,v 1.13 2006/07/27 20:04:31 fredt Exp $ */

/**
 * All the info we need to connect up to a database.
 *
 * @author Blaine Simpson unsaved@users
 */
public class RCData {

    public static final String DEFAULT_JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    /**
     * Just for testing and debugging.
     */
    public void report() {
        System.err.println("urlid: " + id + ", url: " + url + ", username: "
                           + username + ", password: " + password);
    }

    /**
     * Creates a RCDataObject by looking up the given key in the
     * given authentication file.
     *
     * @param dbKey Key to look up in the file.
     * @param file File containing the authentication information.
     */
    public RCData(File file, String dbKey) throws Exception {

        if (file == null) {
            throw new IllegalArgumentException("RC file name not specified");
        }

        if (!file.canRead()) {
            throw new IOException("Please set up authentication file '"
                                  + file + "'");
        }

        // System.err.println("Using RC file '" + file + "'");
        StringTokenizer tokenizer = null;
        boolean         thisone   = false;
        String          s;
        String          keyword, value;
        int             linenum = 0;
        BufferedReader  br      = new BufferedReader(new FileReader(file));

        while ((s = br.readLine()) != null) {
            ++linenum;

            s = s.trim();

            if (s.length() == 0) {
                continue;
            }

            if (s.charAt(0) == '#') {
                continue;
            }

            tokenizer = new StringTokenizer(s);

            if (tokenizer.countTokens() == 1) {
                keyword = tokenizer.nextToken();
                value   = "";
            } else if (tokenizer.countTokens() > 1) {
                keyword = tokenizer.nextToken();
                value   = tokenizer.nextToken("").trim();
            } else {
                try {
                    br.close();
                } catch (IOException e) {}

                throw new Exception("Corrupt line " + linenum + " in '"
                                    + file + "':  " + s);
            }

            if (dbKey == null) {
                if (keyword.equals("urlid")) {
                    System.out.println(value);
                }

                continue;
            }

            if (keyword.equals("urlid")) {
                if (value.equals(dbKey)) {
                    if (id == null) {
                        id      = dbKey;
                        thisone = true;
                    } else {
                        try {
                            br.close();
                        } catch (IOException e) {}

                        throw new Exception("Key '" + dbKey + " redefined at"
                                            + " line " + linenum + " in '"
                                            + file);
                    }
                } else {
                    thisone = false;
                }

                continue;
            }

            if (thisone) {
                if (keyword.equals("url")) {
                    url = value;
                } else if (keyword.equals("username")) {
                    username = value;
                } else if (keyword.equals("driver")) {
                    driver = value;
                } else if (keyword.equals("charset")) {
                    charset = value;
                } else if (keyword.equals("truststore")) {
                    truststore = value;
                } else if (keyword.equals("password")) {
                    password = value;
                } else {
                    try {
                        br.close();
                    } catch (IOException e) {}

                    throw new Exception("Bad line " + linenum + " in '"
                                        + file + "':  " + s);
                }
            }
        }

        try {
            br.close();
        } catch (IOException e) {}

        if (dbKey == null) {
            return;
        }

        if (url == null || username == null || password == null) {
            throw new Exception("url or username or password not set "
                                + "for '" + dbKey + "' in file '" + file
                                + "'");
        }
    }

    /**
     * <p>Creates a new <code>RCData</code> object.
     *
     * <p>The parameters driver, charset, and truststore are optional. Setting
     * these parameters to <code>NULL</code> will set them to their default
     * values.
     *
     * @param id The identifier for these connection settings
     * @param url The URL of the database to connect to
     * @param username The username to log in as
     * @param password The password of the username
     * @param driver The JDBC driver to use
     * @param charset The character set to use
     * @param truststore The trust store to use
     * @throws Exception if the a non-optional parameter is set to <code>NULL</code>
     */
    public RCData(String id, String url, String username, String password,
                  String driver, String charset,
                  String truststore) throws Exception {

        this.id         = id;
        this.url        = url;
        this.username   = username;
        this.password   = password;
        this.driver     = driver;
        this.charset    = charset;
        this.truststore = truststore;

        if (id == null || url == null || username == null
                || password == null) {
            throw new Exception("id, url, username, or password was not set");
        }
    }

    String id         = null;
    String url        = null;
    String username   = null;
    String password   = null;
    String driver     = null;
    String charset    = null;
    String truststore = null;

    /**
     * Gets a JDBC Connection using the data of this RCData object.
     *
     * @return New JDBC Connection
     */
    public Connection getConnection()
    throws ClassNotFoundException, InstantiationException,
           IllegalAccessException, SQLException {
        return getConnection(null, null, null);
    }

    /**
     * Gets a JDBC Connection using the data of this RCData object with
     * specified override elements
     *
     * @return New JDBC Connection
     */
    public Connection getConnection(String curDriver, String curCharset,
                                    String curTrustStore)
                                    throws ClassNotFoundException,
                                           InstantiationException,
                                           IllegalAccessException,
                                           SQLException {

        Properties sysProps = System.getProperties();

        if (curDriver == null) {

            // If explicit driver not specified
            curDriver = ((driver == null) ? DEFAULT_JDBC_DRIVER
                                          : driver);
        }

        if (curCharset == null && charset != null) {
            curCharset = charset;
        }

        if (curTrustStore == null && truststore != null) {
            curTrustStore = truststore;
        }

        if (curCharset == null) {
            sysProps.remove("sqlfile.charset");
        } else {
            sysProps.put("sqlfile.charset", curCharset);
        }

        if (curTrustStore == null) {
            sysProps.remove("javax.net.ssl.trustStore");
        } else {
            sysProps.put("javax.net.ssl.trustStore", curTrustStore);
        }

        // As described in the JDBC FAQ:
        // http://java.sun.com/products/jdbc/jdbc-frequent.html;
        // Why doesn't calling class.forName() load my JDBC driver?
        // There is a bug in the JDK 1.1.x that can cause Class.forName()
        // to fail. // new org.hsqldb.jdbcDriver();
        Class.forName(curDriver).newInstance();

        return DriverManager.getConnection(url, username, password);
    }
}
