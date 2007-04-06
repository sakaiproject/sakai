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

import java.sql.SQLException;

import org.hsqldb.jdbc.Util;
import org.hsqldb.persist.HsqlProperties;

// fredt@users 20020215 - patch 461556 by paul-h@users - modified
// minor changes to support the new HsqlServerProperties class
// boucherb@users 20030501 - Server now implements HsqlSocketRequestHandler

/**
 * HsqlServerFactory
 *
 * @author paul-h@users
 * @version 1.7.2
 * @since 1.7.0
 */
public class HsqlServerFactory {

    private HsqlServerFactory() {}

    public static HsqlSocketRequestHandler createHsqlServer(String dbFilePath,
            boolean debugMessages, boolean silentMode) throws SQLException {

        HsqlProperties props = new HsqlProperties();

        props.setProperty("server.database.0", dbFilePath);
        props.setProperty("server.trace", debugMessages);
        props.setProperty("server.silent", silentMode);

        Server server = new Server();

        server.setProperties(props);

        if (server.openDatabases() == false) {
            Throwable t = server.getServerError();

            if (t != null && t instanceof HsqlException) {
                throw Util.sqlException((HsqlException) t);
            } else {
                throw new SQLException(Trace.getMessage(Trace.GENERAL_ERROR));
            }
        }

        server.setState(ServerConstants.SERVER_STATE_ONLINE);

        // Server now implements HsqlSocketRequestHandler,
        // so there's really no need for HsqlSocketRequestHandlerImpl
        return (HsqlSocketRequestHandler) server;
    }
}
