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

import org.hsqldb.lib.DoubleIntIndex;
import org.hsqldb.lib.HashMappedList;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.LongKeyIntValueHashMap;

/**
 * Manages rows involved in transactions
 *
 * @author fredt@users
 * @version  1.8.0
 * @since 1.8.0
 */
public class TransactionManager {

    LongKeyIntValueHashMap rowSessionMap;
    boolean                reWriteProtect;
    Database               database;

    TransactionManager(Database db) {
        database      = db;
        rowSessionMap = new LongKeyIntValueHashMap(true);
    }

    public void setReWriteProtection(boolean value) {
        reWriteProtect = value;
    }

    void checkDelete(Session session, Row row) throws HsqlException {}

    void checkDelete(Session session,
                     HashMappedList rowSet) throws HsqlException {

        if (!reWriteProtect) {
            return;
        }

        int sessionid = session.getId();

        for (int i = 0, size = rowSet.size(); i < size; i++) {
            Row  row   = (Row) rowSet.getKey(i);
            long rowid = row.getId();

            if (rowSessionMap.get(rowid, sessionid) != sessionid) {
                throw Trace.error(Trace.INVALID_TRANSACTION_STATE_NO_SUBCLASS,
                                  Trace.ITSNS_OVERWRITE);
            }
        }
    }

    void checkDelete(Session session,
                     HsqlArrayList rowSet) throws HsqlException {

        if (!reWriteProtect) {
            return;
        }

        int sessionid = session.getId();

        for (int i = 0, size = rowSet.size(); i < size; i++) {
            Row  row   = (Row) rowSet.get(i);
            long rowid = row.getId();

            if (rowSessionMap.get(rowid, sessionid) != sessionid) {
                throw Trace.error(Trace.INVALID_TRANSACTION_STATE_NO_SUBCLASS,
                                  Trace.ITSNS_OVERWRITE);
            }
        }
    }

    void commit(Session session) {

        Object[] list = session.rowActionList.getArray();
        int      size = session.rowActionList.size();

        for (int i = 0; i < size; i++) {
            Transaction tx    = (Transaction) list[i];
            long        rowid = tx.row.getId();

            tx.commit(session);
            rowSessionMap.remove(rowid);
        }

        session.rowActionList.clear();
        session.savepoints.clear();
    }

    synchronized void rollback(Session session) {
        rollbackTransactions(session, 0, false);
        session.savepoints.clear();
    }

    void rollbackSavepoint(Session session,
                           String name) throws HsqlException {

        int index = session.savepoints.getIndex(name);

        if (index < 0) {
            throw Trace.error(Trace.SAVEPOINT_NOT_FOUND, name);
        }

        Integer oi    = (Integer) session.savepoints.get(index);
        int     limit = oi.intValue();

        rollbackTransactions(session, limit, false);

        while (session.savepoints.size() > index) {
            session.savepoints.remove(session.savepoints.size() - 1);
        }
    }

    void rollbackTransactions(Session session, int limit, boolean log) {

        Object[] list = session.rowActionList.getArray();
        int      size = session.rowActionList.size();

        for (int i = size - 1; i >= limit; i--) {
            Transaction tx = (Transaction) list[i];

            tx.rollback(session, log);
        }

        for (int i = limit; i < size; i++) {
            Transaction tx    = (Transaction) list[i];
            long        rowid = tx.row.getId();

            rowSessionMap.remove(rowid);
        }

        session.rowActionList.setSize(limit);
    }

    void addTransaction(Session session, Transaction transaction) {

        if (reWriteProtect) {
            rowSessionMap.put(transaction.row.getId(), session.getId());
        }
    }

    private long globalActionTimestamp = 0;

    /**
     * gets the next timestamp for an action
     */
    long nextActionTimestamp() {

        globalActionTimestamp++;

        return globalActionTimestamp;
    }

    /**
     * Return an array of all transactions sorted by System Change No.
     */
    Transaction[] getTransactionList() {

        Session[]     sessions = database.sessionManager.getAllSessions();
        int[]         tIndex   = new int[sessions.length];
        Transaction[] transactions;
        int           transactionCount = 0;

        {
            int actioncount = 0;

            for (int i = 0; i < sessions.length; i++) {
                actioncount += sessions[i].getTransactionSize();
            }

            transactions = new Transaction[actioncount];
        }

        while (true) {
            boolean found        = false;
            long    minChangeNo  = Long.MAX_VALUE;
            int     sessionIndex = 0;

            // find the lowest available SCN across all sessions
            for (int i = 0; i < sessions.length; i++) {
                int tSize = sessions[i].getTransactionSize();

                if (tIndex[i] < tSize) {
                    Transaction current =
                        (Transaction) sessions[i].rowActionList.get(
                            tIndex[i]);

                    if (current.SCN < minChangeNo) {
                        minChangeNo  = current.SCN;
                        sessionIndex = i;
                    }

                    found = true;
                }
            }

            if (!found) {
                break;
            }

            HsqlArrayList currentList = sessions[sessionIndex].rowActionList;

            for (; tIndex[sessionIndex] < currentList.size(); ) {
                Transaction current =
                    (Transaction) currentList.get(tIndex[sessionIndex]);

                // if the next change no is in this session, continue adding
                if (current.SCN == minChangeNo + 1) {
                    minChangeNo++;
                }

                if (current.SCN == minChangeNo) {
                    transactions[transactionCount++] = current;

                    tIndex[sessionIndex]++;
                } else {
                    break;
                }
            }
        }

        return transactions;
    }

    /**
     * Return a lookup of all transactions ids for cached tables.
     */
    public DoubleIntIndex getTransactionIDList() {

        Session[]      sessions = database.sessionManager.getAllSessions();
        DoubleIntIndex lookup   = new DoubleIntIndex(10, false);

        lookup.setKeysSearchTarget();

        for (int i = 0; i < sessions.length; i++) {
            HsqlArrayList tlist = sessions[i].rowActionList;

            for (int j = 0, size = tlist.size(); j < size; j++) {
                Transaction tx = (Transaction) tlist.get(j);

                if (tx.tTable.getTableType() == Table.CACHED_TABLE) {
                    lookup.addUnique(tx.row.getPos(), 0);
                }
            }
        }

        return lookup;
    }

    /**
     * Convert row ID's for cached table rows in transactions
     */
    public void convertTransactionIDs(DoubleIntIndex lookup) {

        Session[] sessions = database.sessionManager.getAllSessions();

        for (int i = 0; i < sessions.length; i++) {
            HsqlArrayList tlist = sessions[i].rowActionList;

            for (int j = 0, size = tlist.size(); j < size; j++) {
                Transaction tx = (Transaction) tlist.get(j);

                if (tx.tTable.getTableType() == Table.CACHED_TABLE) {
                    int pos = lookup.lookupFirstEqual(tx.row.getPos());

                    tx.row.setPos(pos);
                }
            }
        }
    }
}
