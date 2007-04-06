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
 * Interface to Session and its remote proxy objects. Used by the
 * implementations of JDBC interfaces to communicate with the database at
 * the session level.
 *
 * @author fredt@users
 * @version 1.7.2
 * @since 1.7.2
 */
public interface SessionInterface {

    // fredt@users - marked as used
    int INFO_DATABASE            = 0;
    int INFO_USER                = 1;
    int INFO_SESSION_ID          = 2;
    int INFO_ISOLATION           = 3;
    int INFO_AUTOCOMMIT          = 4;    // used
    int INFO_DATABASE_READONLY   = 5;
    int INFO_CONNECTION_READONLY = 6;    // used

    //
    int TX_READ_UNCOMMITTED = 1;
    int TX_READ_COMMITTED   = 2;
    int TX_REPEATABLE_READ  = 4;
    int TX_SERIALIZABLE     = 8;

    Result execute(Result r) throws HsqlException;

    void close();

    boolean isClosed();

    boolean isReadOnly() throws HsqlException;

    void setReadOnly(boolean readonly) throws HsqlException;

    boolean isAutoCommit() throws HsqlException;

    void setAutoCommit(boolean autoCommit) throws HsqlException;

    void setIsolation(int level) throws HsqlException;

    int getIsolation() throws HsqlException;

    void startPhasedTransaction() throws HsqlException;

    void prepareCommit() throws HsqlException;

    void commit() throws HsqlException;

    void rollback() throws HsqlException;

    int getId();

    void resetSession() throws HsqlException;
}
