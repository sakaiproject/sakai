/* Copyright (c) 1995-2000, The Hypersonic SQL Group.
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
 * Neither the name of the Hypersonic SQL Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HYPERSONIC SQL GROUP,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many individuals 
 * on behalf of the Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2005, The HSQL Development Group
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
 * Represents a single row table operation.
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.8.0
 * @since Hypersonic SQL
 */
class Transaction {

    boolean isDelete;
    Table   tTable;
    Row     row;
    long    SCN;

    /**
     * Constructor. <p>
     *
     * @param delete if true, this represents a single row delete action, else
     *      a single row insert action
     * @param nested true if this action is part of a transaction initiated
     *  within an INSERT INTO or UPDATE statement
     * @param table the Table object against which the operation occured
     * @param row the row data that iis inserted or deleted
     */
    Transaction(boolean delete, Table table, Row row, long SCN) {

        isDelete = delete;
        tTable   = table;
        this.row = row;
    }

    /**
     * Undoes the single row delete or insert represented by this object.
     *
     * @param session the session context in which to perform the undo
     * @param log if true log the work
     * @throws HsqlException if a database access error occurs
     */
    void rollback(Session session, boolean log) {

        try {
            if (isDelete) {
                tTable.insertNoCheckRollback(session, row, log);
            } else {
                tTable.deleteNoCheckRollback(session, row, log);
            }
        } catch (Exception e) {

//            System.out.println("rollback error: isDelete " + isDelete);
        }
    }

    void commit(Session session) {

        try {
            if (isDelete) {
                tTable.removeRowFromStore(row);
            } else {
                tTable.commitRowToStore(row);
            }
        } catch (Exception e) {

//            System.out.println("rollback error: isDelete " + isDelete);
        }
    }

    void logRollback(Session session) {

        try {
            if (isDelete) {
                tTable.database.logger.writeInsertStatement(session, tTable,
                        row.getData());
            } else {
                tTable.database.logger.writeDeleteStatement(session, tTable,
                        row.getData());
            }
        } catch (Exception e) {}
    }

    void logAction(Session session) {

        try {
            if (isDelete) {
                tTable.database.logger.writeDeleteStatement(session, tTable,
                        row.getData());
            } else {
                tTable.database.logger.writeInsertStatement(session, tTable,
                        row.getData());
            }
        } catch (Exception e) {}
    }
}
