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


package org.hsqldb.persist;

import org.hsqldb.Database;
import org.hsqldb.HsqlException;
import org.hsqldb.NumberSequence;
import org.hsqldb.Session;
import org.hsqldb.Table;
import org.hsqldb.Trace;
import org.hsqldb.lib.SimpleLog;

// boucherb@users 20030510 - patch 1.7.2 - added cooperative file locking

/**
 *  The public interface of logging and cache classes.<p>
 *
 *  Implements a storage manager wrapper that provides a consistent,
 *  always available interface to storage management for the Database
 *  class, despite the fact not all Database objects actually use file
 *  storage.<p>
 *
 *  The Logger class makes it possible to avoid testing for a
 *  null Log Database attribute again and again, in many different places,
 *  and generally avoids tight coupling between Database and Log, opening
 *  the doors for multiple logs/caches in the future. In this way, the
 *  Database class does not need to know the details of the Logging/Cache
 *  implementation, lowering its breakability factor and promoting
 *  long-term code flexibility.
 *
 * @author fredt@users
 * @version 1.8.0
 * @since 1.7.0
 */
public class Logger {

    public SimpleLog appLog;

    /**
     *  The Log object this Logger object wraps
     */
    private Log log;

    /**
     *  The LockFile object this Logger uses to cooperatively lock
     *  the database files
     */
    private LockFile lf;
    boolean          needsCheckpoint;
    private boolean  logStatements;
    private boolean  syncFile = false;

    public Logger() {
        appLog = new SimpleLog(null, SimpleLog.LOG_NONE, false);
    }

    /**
     *  Opens the specified Database object's database files and starts up
     *  the logging process. <p>
     *
     *  If the specified Database object is a new database, its database
     *  files are first created.
     *
     * @param  db the Database
     * @throws  HsqlException if there is a problem, such as the case when
     *      the specified files are in use by another process
     */
    public void openLog(Database db) throws HsqlException {

        needsCheckpoint = false;

        String path = db.getPath();
        int loglevel = db.getProperties().getIntegerProperty(
            HsqlDatabaseProperties.hsqldb_applog, 0);

        if (loglevel != SimpleLog.LOG_NONE) {
            appLog = new SimpleLog(path + ".app.log", loglevel,
                                   !db.isFilesReadOnly());
        }

        appLog.sendLine(SimpleLog.LOG_ERROR, "Database (re)opened");

        logStatements = false;

        if (!db.isFilesReadOnly()) {
            acquireLock(path);
        }

        log = new Log(db);

        log.open();

        logStatements = !db.isFilesReadOnly();
    }

// fredt@users 20020130 - patch 495484 by boucherb@users

    /**
     *  Shuts down the logging process using the specified mode. <p>
     *
     * @param  closemode The mode in which to shut down the logging
     *      process
     *      <OL>
     *        <LI> closemode -1 performs SHUTDOWN IMMEDIATELY, equivalent
     *        to  a poweroff or crash.
     *        <LI> closemode 0 performs a normal SHUTDOWN that
     *        checkpoints the database normally.
     *        <LI> closemode 1 performs a shutdown compact that scripts
     *        out the contents of any CACHED tables to the log then
     *        deletes the existing *.data file that contains the data
     *        for all CACHED table before the normal checkpoint process
     *        which in turn creates a new, compact *.data file.
     *        <LI> closemode 2 performs a SHUTDOWN SCRIPT.
     *      </OL>
     *
     * @return  true if closed with no problems or false if a problem was
     *        encountered.
     */
    public boolean closeLog(int closemode) {

        if (log == null) {
            appLog.sendLine(SimpleLog.LOG_ERROR, "Database closed");
            appLog.close();

            return true;
        }

        try {
            switch (closemode) {

                case Database.CLOSEMODE_IMMEDIATELY :
                    log.shutdown();
                    break;

                case Database.CLOSEMODE_NORMAL :
                    log.close(false);
                    break;

                case Database.CLOSEMODE_COMPACT :
                case Database.CLOSEMODE_SCRIPT :
                    log.close(true);
                    break;
            }
        } catch (Throwable e) {
            appLog.logContext(e, "error closing log");
            appLog.close();

            log = null;

            return false;
        }

        appLog.sendLine(SimpleLog.LOG_ERROR, "Database closed");
        appLog.close();

        log = null;

        return true;
    }

    /**
     *  Determines if the logging process actually does anything. <p>
     *
     *  In-memory Database objects do not need to log anything. This
     *  method is essentially equivalent to testing whether this logger's
     *  database is an in-memory mode database.
     *
     * @return  true if this object encapsulates a non-null Log instance,
     *      else false
     */
    public boolean hasLog() {
        return log != null;
    }

    /**
     *  Returns the Cache object or null if one doesn't exist.
     */
    public DataFileCache getCache() throws HsqlException {

        if (log == null) {
            return null;
        } else {
            return log.getCache();
        }
    }

    /**
     *  Returns the Cache object or null if one doesn't exist.
     */
    public boolean hasCache() {

        if (log == null) {
            return false;
        } else {
            return log.hasCache();
        }
    }

    /**
     *  Records a Log entry representing a new connection action on the
     *  specified Session object.
     *
     * @param  session the Session object for which to record the log
     *      entry
     * @throws  HsqlException if there is a problem recording the Log
     *      entry
     */
    public synchronized void logConnectUser(Session session)
    throws HsqlException {

        if (logStatements) {
            writeToLog(session, session.getUser().getConnectStatement());
        }
    }

    /**
     *  Records a Log entry for the specified SQL statement, on behalf of
     *  the specified Session object.
     *
     * @param  session the Session object for which to record the Log
     *      entry
     * @param  statement the SQL statement to Log
     * @throws  HsqlException if there is a problem recording the entry
     */
    public synchronized void writeToLog(Session session,
                                        String statement)
                                        throws HsqlException {

        if (logStatements && log != null) {
            log.writeStatement(session, statement);
        }
    }

    public synchronized void writeInsertStatement(Session session,
            Table table, Object[] row) throws HsqlException {

        if (logStatements) {
            log.writeInsertStatement(session, table, row);
        }
    }

    public synchronized void writeDeleteStatement(Session session, Table t,
            Object[] row) throws HsqlException {

        if (logStatements) {
            log.writeDeleteStatement(session, t, row);
        }
    }

    public synchronized void writeSequenceStatement(Session session,
            NumberSequence s) throws HsqlException {

        if (logStatements) {
            log.writeSequenceStatement(session, s);
        }
    }

    public synchronized void writeCommitStatement(Session session)
    throws HsqlException {

        if (logStatements) {
            log.writeCommitStatement(session);
            synchLog();
        }
    }

    /**
     * Called after commits or after each statement when autocommit is on
     */
    public synchronized void synchLog() {

        if (logStatements && syncFile) {
            log.synchLog();
        }
    }

    public synchronized void synchLogForce() {

        if (logStatements) {
            log.synchLog();
        }
    }

    /**
     *  Checkpoints the database. <p>
     *
     *  The most important effect of calling this method is to cause the
     *  log file to be rewritten in the most efficient form to
     *  reflect the current state of the database, i.e. only the DDL and
     *  insert DML required to recreate the database in its present state.
     *  Other house-keeping duties are performed w.r.t. other database
     *  files, in order to ensure as much as possible the ACID properites
     *  of the database.
     *
     * @throws  HsqlException if there is a problem checkpointing the
     *      database
     */
    public synchronized void checkpoint(boolean mode) throws HsqlException {

        if (logStatements) {
            appLog.logContext(appLog.LOG_NORMAL, "start");

            needsCheckpoint = false;

            log.checkpoint(mode);
            appLog.logContext(appLog.LOG_NORMAL, "end");
        }
    }

    /**
     *  Sets the maximum size to which the log file can grow
     *  before being automatically checkpointed.
     *
     * @param  megas size in MB
     */
    public synchronized void setLogSize(int megas) {

        if (log != null) {
            log.setLogSize(megas);
        }
    }

    /**
     *  Sets the type of script file, currently 0 for text (default)
     *  1 for binary and 3 for compressed
     *
     * @param  i The type
     */
    public synchronized void setScriptType(int i) throws HsqlException {

        if (log != null) {
            log.setScriptType(i);
        }
    }

    /**
     *  Sets the log write delay mode to number of seconds. By default
     *  executed commands written to the log are committed fully at most
     *  60 second after they are executed. This improves performance for
     *  applications that execute a large number
     *  of short running statements in a short period of time, but risks
     *  failing to log some possibly large number of statements in the
     *  event of a crash. A small value improves recovery.
     *  A value of 0 will severly slow down logging when autocommit is on,
     *  or many short transactions are committed.
     *
     * @param  delay in seconds
     */
    public synchronized void setWriteDelay(int delay) {

        if (log != null) {
            syncFile = (delay == 0);

            log.setWriteDelay(delay);
        }
    }

    public int getWriteDelay() {
        return log != null ? log.getWriteDelay()
                           : 0;
    }

    public int getLogSize() {
        return log != null ? log.getLogSize()
                           : 0;
    }

    public int getScriptType() {
        return log != null ? log.getScriptType()
                           : 0;
    }

    /**
     *  Opens the TextCache object.
     */
    public DataFileCache openTextCache(Table table, String source,
                                       boolean readOnlyData,
                                       boolean reversed)
                                       throws HsqlException {
        return log.openTextCache(table, source, readOnlyData, reversed);
    }

    /**
     *  Closes the TextCache object.
     */
    public void closeTextCache(Table table) throws HsqlException {
        log.closeTextCache(table);
    }

    public boolean needsCheckpoint() {
        return needsCheckpoint;
    }

    /**
     * Attempts to aquire a cooperative lock condition on the database files
     */
    public void acquireLock(String path) throws HsqlException {

        if (lf != null) {
            return;
        }

        lf = LockFile.newLockFileLock(path);
    }

    public void releaseLock() {

        try {
            if (lf != null) {
                lf.tryRelease();
            }
        } catch (Exception e) {
            if (Trace.TRACE) {
                Trace.printSystemOut(e.toString());
            }
        }

        lf = null;
    }
}
