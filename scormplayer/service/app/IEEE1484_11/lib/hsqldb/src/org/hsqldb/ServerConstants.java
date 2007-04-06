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

/**
 * An enumeration of the property keys and default property values used by
 * HSQLDB servers
 *
 * @author  boucherb@users
 * @version 1.7.2
 * @since 1.7.2
 */
public interface ServerConstants {

    // server states
    int SERVER_STATE_ONLINE   = 1;
    int SERVER_STATE_OPENING  = 4;
    int SERVER_STATE_CLOSING  = 8;
    int SERVER_STATE_SHUTDOWN = 16;
    int SC_DATABASE_SHUTDOWN  = 0;

    // use default address for server socket
    String SC_DEFAULT_ADDRESS = "0.0.0.0";

    // default database name if non specified
    String SC_DEFAULT_DATABASE = "test";

    // default port for each protocol
    int SC_DEFAULT_HSQL_SERVER_PORT  = 9001;
    int SC_DEFAULT_HSQLS_SERVER_PORT = 554;
    int SC_DEFAULT_HTTP_SERVER_PORT  = 80;
    int SC_DEFAULT_HTTPS_SERVER_PORT = 443;
    int SC_DEFAULT_BER_SERVER_PORT   = 9101;

    // operation modes
    boolean SC_DEFAULT_SERVER_AUTORESTART = false;
    boolean SC_DEFAULT_NO_SYSTEM_EXIT     = true;
    boolean SC_DEFAULT_SILENT             = true;
    boolean SC_DEFAULT_TLS                = false;
    boolean SC_DEFAULT_TRACE              = false;
    boolean SC_DEFAULT_REMOTE_OPEN_DB     = false;

    // type of server
    int SC_PROTOCOL_HTTP = 0;
    int SC_PROTOCOL_HSQL = 1;
    int SC_PROTOCOL_BER  = 2;

    // keys to properties
    String SC_KEY_PREFIX             = "server";
    String SC_KEY_ADDRESS            = SC_KEY_PREFIX + ".address";
    String SC_KEY_AUTORESTART_SERVER = SC_KEY_PREFIX + ".restart_on_shutdown";
    String SC_KEY_DATABASE           = SC_KEY_PREFIX + ".database";
    String SC_KEY_DBNAME             = SC_KEY_PREFIX + ".dbname";
    String SC_KEY_NO_SYSTEM_EXIT     = SC_KEY_PREFIX + ".no_system_exit";
    String SC_KEY_PORT               = SC_KEY_PREFIX + ".port";
    String SC_KEY_SILENT             = SC_KEY_PREFIX + ".silent";
    String SC_KEY_TLS                = SC_KEY_PREFIX + ".tls";
    String SC_KEY_TRACE              = SC_KEY_PREFIX + ".trace";
    String SC_KEY_WEB_DEFAULT_PAGE   = SC_KEY_PREFIX + ".default_page";
    String SC_KEY_WEB_ROOT           = SC_KEY_PREFIX + ".root";
    String SC_KEY_MAX_CONNECTIONS    = SC_KEY_PREFIX + ".maxconnections";
    String SC_KEY_REMOTE_OPEN_DB     = SC_KEY_PREFIX + ".remote_open";

    // web server page defaults
    String SC_DEFAULT_WEB_MIME = "text/html";
    String SC_DEFAULT_WEB_PAGE = "index.html";
    String SC_DEFAULT_WEB_ROOT = ".";
}
