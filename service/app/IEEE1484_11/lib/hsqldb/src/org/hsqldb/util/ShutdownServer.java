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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.hsqldb.persist.HsqlProperties;

/*
 *  Shutdown class for Server, used by hsqldbserver to shutdown the server
 *
 *  Send any questions to fchoong@user
 */
public class ShutdownServer {

    public static void main(String[] arg) {

        boolean webserver;
        String  driver = "org.hsqldb.jdbcDriver";
        String  url;
        String  user;
        String  password;
        int     port;
        String  defaulturl;
        String  shutdownarg;

        if (arg.length > 0) {
            String p = arg[0];

            if ((p != null) && p.startsWith("-?")) {
                printHelp();

                return;
            }
        }

        HsqlProperties props = HsqlProperties.argArrayToProps(arg, "server");

        webserver  = props.isPropertyTrue("server.webserver", false);
        defaulturl = webserver ? "jdbc:hsqldb:http://localhost"
                               : "jdbc:hsqldb:hsql://localhost";

        int defaultport = webserver ? 80
                                    : 9001;

        port        = props.getIntegerProperty("server.port", defaultport);
        url = props.getProperty("server.url", defaulturl + ":" + port);
        user        = props.getProperty("server.user", "sa");
        password    = props.getProperty("server.password", "");
        shutdownarg = props.getProperty("server.shutdownarg", "");

        try {
            Class.forName(driver);       // Load the driver

            Connection connection = DriverManager.getConnection(url, user,
                password);
            Statement statement = connection.createStatement();

            // can use SHUTDOWN COMPACT or SHUTDOWN IMMEDIATELY
            statement.execute("SHUTDOWN " + shutdownarg);
        } catch (ClassNotFoundException e) {
            System.err.println(e);    // Driver not found
        } catch (SQLException e) {
            System.err.println(e);    // error connection to database
        }
    }

    static void printHelp() {

        System.out.print(
            "Usage: java ShutdownServer [-options]\n"
            + "where options include:\n"
            + "    -port <nr>               port where the server is listening\n"
            + "    -user <name>             username of admin user\n"
            + "    -password <value>        password of admin user\n"
            + "    -webserver <true/false>  whether it's a web server\n"
            + "    -url <value>             server url (overrides -webserver and -port options)\n"
            + "    -shutdownarg <value>     IMMEDIATELY or COMPACT are allowed\n"
            + "The command line arguments override the default values.");
    }
}
