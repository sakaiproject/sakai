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


package org.hsqldb;

import java.util.Locale;

import org.hsqldb.persist.HsqlProperties;

/*
 * Parses a connection URL into parts.
 *
 * @author fredt@users
 * @version 1.8.0
 * @since 1.8.0
 */
public class DatabaseURL {

    static final String        S_DOT        = ".";
    public static final String S_MEM        = "mem:";
    public static final String S_FILE       = "file:";
    public static final String S_RES        = "res:";
    public static final String S_ALIAS      = "alias:";
    public static final String S_HSQL       = "hsql://";
    public static final String S_HSQLS      = "hsqls://";
    public static final String S_HTTP       = "http://";
    public static final String S_HTTPS      = "https://";
    public static final String S_URL_PREFIX = "jdbc:hsqldb:";

    /**
     * Returns true if type represents an in-process connection to a file backed
     * database.
     */
    public static boolean isFileBasedDatabaseType(String url) {

        if (url == S_FILE || url == S_RES) {
            return true;
        }

        return false;
    }

    /**
     * Returns true if type represents an in-process connection to database.
     */
    public static boolean isInProcessDatabaseType(String url) {

        if (url == S_FILE || url == S_RES || url == S_MEM) {
            return true;
        }

        return false;
    }

    /**
     * Parses the url into components that are returned in a properties
     * object. <p>
     *
     * The following components are isolated: <p>
     *
     * <ul>
     * url: the original url<p>
     * connection_type: a static string that indicate the protocol. If the
     * url does not begin with a valid protocol, null is returned by this
     * method instead of the properties object.<p>
     * host: name of host in networked modes in lowercase<p>
     * port: port number in networked mode, or 0 if not present<p>
     * path: path of the resource on server in networked modes,
     * / (slash) in all cases apart from
     * servlet path which is / (slash) plus the name of the servlet<p>
     * database: database name. For memory, resource and networked modes,
     * this is returned in lowercase, for file databases the original
     * case of characters is preserved. Returns empty string if name is not
     * present in the url.<p>
     * for each protocol if port number is not in the url<p>
     * Additional connection properties specified as key/value pairs.
     * </ul>
     * @return null returned if the part that should represent the port is not
     * an integer or the part for database name is empty.
     * Empty HsqlProperties returned if if url does not begin with valid
     * protocol and could refer to another JDBC driver.
     *
     */
    public static HsqlProperties parseURL(String url, boolean hasPrefix) {

        String         urlImage   = url.toLowerCase(Locale.ENGLISH);
        HsqlProperties props      = new HsqlProperties();
        HsqlProperties extraProps = null;
        String         arguments  = null;
        int            pos        = 0;

        if (hasPrefix) {
            if (urlImage.startsWith(S_URL_PREFIX)) {
                pos = S_URL_PREFIX.length();
            } else {
                return props;
            }
        }

        String  type = null;
        String  host;
        int     port = 0;
        String  database;
        String  path;
        boolean isNetwork = false;

        props.setProperty("url", url);

        int semicolpos = url.indexOf(';', pos);

        if (semicolpos < 0) {
            semicolpos = url.length();
        } else {
            arguments = urlImage.substring(semicolpos + 1, urlImage.length());
            extraProps = HsqlProperties.delimitedArgPairsToProps(arguments,
                    "=", ";", null);

            //todo - check if properties have valid names / values
            props.addProperties(extraProps);
        }

        if (semicolpos == pos + 1 && urlImage.startsWith(S_DOT, pos)) {
            type = S_DOT;
        } else if (urlImage.startsWith(S_MEM, pos)) {
            type = S_MEM;
        } else if (urlImage.startsWith(S_FILE, pos)) {
            type = S_FILE;
        } else if (urlImage.startsWith(S_RES, pos)) {
            type = S_RES;
        } else if (urlImage.startsWith(S_ALIAS, pos)) {
            type = S_ALIAS;
        } else if (urlImage.startsWith(S_HSQL, pos)) {
            type      = S_HSQL;
            port      = ServerConstants.SC_DEFAULT_HSQL_SERVER_PORT;
            isNetwork = true;
        } else if (urlImage.startsWith(S_HSQLS, pos)) {
            type      = S_HSQLS;
            port      = ServerConstants.SC_DEFAULT_HSQLS_SERVER_PORT;
            isNetwork = true;
        } else if (urlImage.startsWith(S_HTTP, pos)) {
            type      = S_HTTP;
            port      = ServerConstants.SC_DEFAULT_HTTP_SERVER_PORT;
            isNetwork = true;
        } else if (urlImage.startsWith(S_HTTPS, pos)) {
            type      = S_HTTPS;
            port      = ServerConstants.SC_DEFAULT_HTTPS_SERVER_PORT;
            isNetwork = true;
        }

        if (type == null) {
            type = S_FILE;
        } else if (type == S_DOT) {
            type = S_MEM;

            // keep pos
        } else {
            pos += type.length();
        }

        props.setProperty("connection_type", type);

        if (isNetwork) {
            int slashpos = url.indexOf('/', pos);

            if (slashpos < pos || slashpos > semicolpos) {
                slashpos = semicolpos;
            }

            int colonpos = url.indexOf(':', pos);

            if (colonpos < pos || colonpos > slashpos) {
                colonpos = slashpos;
            } else {
                try {
                    port = Integer.parseInt(url.substring(colonpos + 1,
                                                          slashpos));
                } catch (NumberFormatException e) {
                    return null;
                }
            }

            host = urlImage.substring(pos, colonpos);

            int secondslashpos = url.lastIndexOf('/', semicolpos);

            if (secondslashpos < pos) {
                path     = "/";
                database = "";
            } else if (secondslashpos == slashpos) {
                path     = "/";
                database = urlImage.substring(secondslashpos + 1, semicolpos);
            } else {
                path     = url.substring(slashpos, secondslashpos);
                database = urlImage.substring(secondslashpos + 1, semicolpos);
            }

            props.setProperty("port", port);
            props.setProperty("host", host);
            props.setProperty("path", path);

            if (extraProps != null) {
                String filePath = extraProps.getProperty("filepath");

                if (filePath != null && database.length() != 0) {
                    database += ";" + filePath;
                }
            }
        } else {
            if (type == S_MEM || type == S_RES) {
                database = urlImage.substring(pos, semicolpos).toLowerCase();

                if (type == S_RES) {
                    if (database.indexOf('/') != 0) {
                        database = '/' + database;
                    }
                }
            } else {
                database = url.substring(pos, semicolpos);
            }

            if (database.length() == 0) {
                return null;
            }
        }

        props.setProperty("database", database);

        return props;
    }
/*
    public static void main(String[] argv) {

        parseURL(
            "JDBC:hsqldb:hsql://myhost:1777/mydb;filepath=c:/myfile/database/db",
            true);
        parseURL("JDBC:hsqldb:../data/mydb.db", true);
        parseURL("JDBC:hsqldb:../data/mydb.db;ifexists=true", true);
        parseURL("JDBC:hsqldb:HSQL://localhost:9000/mydb", true);
        parseURL(
            "JDBC:hsqldb:Http://localhost:8080/servlet/org.hsqldb.Servlet/mydb;ifexists=true",
            true);
        parseURL("JDBC:hsqldb:Http://localhost/servlet/org.hsqldb.Servlet/",
                 true);
        parseURL("JDBC:hsqldb:hsql://myhost", true);
    }
*/
}
