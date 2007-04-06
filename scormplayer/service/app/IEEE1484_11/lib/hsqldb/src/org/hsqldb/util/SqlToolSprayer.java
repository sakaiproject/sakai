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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

/* $Id: SqlToolSprayer.java,v 1.15 2005/10/23 19:25:14 fredt Exp $ */

/**
 * Sql Tool Sprayer.
 * Invokes SqlTool.main() multiple times with the same SQL.
 * Invokes for multiple urlids and/or retries.
 *
 * See JavaDocs for the main method for syntax of how to run.
 *
 * System properties used if set:
 * <UL>
 *      <LI>sqltoolsprayer.period (in ms.)</LI>
 *      <LI>sqltoolsprayer.maxtime (in ms.)</LI>
 *      <LI>sqltoolsprayer.rcfile (filepath)</LI>
 * </UL>
 *
 * @see @main()
 * @version $Revision: 1.15 $
 * @author Blaine Simpson unsaved@users
 */
public class SqlToolSprayer {

    private static final String SYNTAX_MSG =
        "SYNTAX:  java [-D...] SqlToolSprayer 'SQL;' [urlid1 urlid2...]\n"
        + "System properties you may use [default values]:\n"
        + "    sqltoolsprayer.period (in ms.) [500]\n"
        + "    sqltoolsprayer.maxtime (in ms.) [0]\n"
        + "    sqltoolsprayer.monfile (filepath) [none]\n"
        + "    sqltoolsprayer.rcfile (filepath) [none.  SqlTool default used.]\n"
        + "    sqltoolsprayer.propfile (filepath) [none]";

    public static void main(String[] sa) {

        if (sa.length < 1) {
            System.err.println(SYNTAX_MSG);
            System.exit(4);
        }

        System.setProperty("sqltool.noexit", "true");

        long period = ((System.getProperty("sqltoolsprayer.period") == null)
                       ? 500
                       : Integer.parseInt(
                           System.getProperty("sqltoolsprayer.period")));
        long maxtime =
            ((System.getProperty("sqltoolsprayer.maxtime") == null) ? 0
                                                                    : Integer.parseInt(
                                                                        System.getProperty(
                                                                            "sqltoolsprayer.maxtime")));
        String rcFile   = System.getProperty("sqltoolsprayer.rcfile");
        String propfile = System.getProperty("sqltoolsprayer.propfile");
        File monitorFile =
            (System.getProperty("sqltoolsprayer.monfile") == null) ? null
                                                                   : new File(
                                                                       System.getProperty(
                                                                           "sqltoolsprayer.monfile"));
        ArrayList urlids = new ArrayList();

        if (propfile != null) {
            try {
                getUrlsFromPropFile(propfile, urlids);
            } catch (Exception e) {
                System.err.println("Failed to load property file '"
                                   + propfile + "':  " + e);
                System.exit(3);
            }
        }

        for (int i = 1; i < sa.length; i++) {
            urlids.add(sa[i]);
        }

        boolean[] status = new boolean[urlids.size()];

        for (int i = 0; i < status.length; i++) {
            status[i] = false;
        }

        String[] withRcArgs    = {
            "--sql", sa[0], "--rcfile", rcFile, null
        };
        String[] withoutRcArgs = {
            "--sql", sa[0], null
        };
        String[] sqlToolArgs   = (rcFile == null) ? withoutRcArgs
                                                  : withRcArgs;
        boolean  onefailed     = false;
        long     startTime     = (new Date()).getTime();

        while (true) {
            if (monitorFile != null &&!monitorFile.exists()) {
                System.err.println("Required file is gone:  " + monitorFile);
                System.exit(2);
            }

            onefailed = false;

            for (int i = 0; i < status.length; i++) {
                if (status[i]) {
                    continue;
                }

                sqlToolArgs[sqlToolArgs.length - 1] = (String) urlids.get(i);

                try {
                    SqlTool.main(sqlToolArgs);

                    status[i] = true;

                    System.err.println("Success for instance '"
                                       + urlids.get(i) + "'");
                } catch (Exception e) {
                    onefailed = true;
                }
            }

            if (!onefailed) {
                break;
            }

            if (maxtime == 0
                    || (new Date()).getTime() > startTime + maxtime) {
                break;
            }

            try {
                Thread.sleep(period);
            } catch (InterruptedException ie) {}
        }

        ArrayList failedUrlids = new ArrayList();

        // If all statuses true, then System.exit(0);
        for (int i = 0; i < status.length; i++) {
            if (status[i] != true) {
                failedUrlids.add((String) urlids.get(i));
            }
        }

        if (failedUrlids.size() > 0) {
            System.err.println("Failed instances:   " + failedUrlids);
            System.exit(1);
        }

        System.exit(0);
    }

    private static void getUrlsFromPropFile(String fileName,
            ArrayList al) throws Exception {

        Properties p = new Properties();

        p.load(new FileInputStream(fileName));

        int    i = -1;
        String val;

        while (true) {
            i++;

            val = p.getProperty("server.urlid." + i);

            if (val == null) {
                return;
            }

            al.add(val);
        }
    }
}
