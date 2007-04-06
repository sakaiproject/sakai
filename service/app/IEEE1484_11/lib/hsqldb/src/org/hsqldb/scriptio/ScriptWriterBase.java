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


package org.hsqldb.scriptio;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.Database;
import org.hsqldb.DatabaseManager;
import org.hsqldb.DatabaseScript;
import org.hsqldb.HsqlException;
import org.hsqldb.NumberSequence;
import org.hsqldb.Result;
import org.hsqldb.Session;
import org.hsqldb.Table;
import org.hsqldb.Token;
import org.hsqldb.Trace;
import org.hsqldb.index.RowIterator;
import org.hsqldb.lib.FileAccess;
import org.hsqldb.lib.FileUtil;
import org.hsqldb.lib.HsqlTimer;
import org.hsqldb.lib.Iterator;

//import org.hsqldb.lib.StopWatch;
// todo - can lock the database engine as readonly in a wrapper for this when
// used at checkpoint

/**
 * Handles all logging to file operations. A log consists of three blocks:<p>
 *
 * DDL BLOCK: definition of DB objects, users and rights at startup time<br>
 * DATA BLOCK: all data for MEMORY tables at startup time<br>
 * LOG BLOCK: SQL statements logged since startup or the last CHECKPOINT<br>
 *
 * The implementation of this class and its subclasses support the formats
 * used for writing the data. In versions up to 1.7.2, this data is written
 * to the *.script file for the database. Since 1.7.2 the data can also be
 * written as binray in order to speed up shutdown and startup.<p>
 *
 * In 1.7.2, two separate files are used, one for the DDL + DATA BLOCK and
 * the other for the LOG BLOCK.<p>
 *
 * A related use for this class is for saving a current snapshot of the
 * database data to a user-defined file. This happens in the SHUTDOWN COMPACT
 * process or done as a result of the SCRIPT command. In this case, the
 * DATA block contains the CACHED table data as well.<p>
 *
 * DatabaseScriptReader and its subclasses read back the data at startup time.
 *
 * @author fredt@users
 * @version 1.8.0
 * @since 1.7.2
 */
public abstract class ScriptWriterBase implements Runnable {

    Database            database;
    String              outFile;
    OutputStream        fileStreamOut;
    FileAccess.FileSync outDescriptor;
    int                 tableRowCount;
    HsqlName            schemaToLog;

    /**
     * this determines if the script is the normal script (false) used
     * internally by the engine or a user-initiated snapshot of the DB (true)
     */
    boolean          isDump;
    boolean          includeCachedData;
    long             byteCount;
    volatile boolean needsSync;
    volatile boolean forceSync;
    volatile boolean busyWriting;
    private int      syncCount;
    static final int INSERT             = 0;
    static final int INSERT_WITH_SCHEMA = 1;

    /** the last schema for last sessionId */
    Session                      currentSession;
    public static final String[] LIST_SCRIPT_FORMATS      = new String[] {
        Token.T_TEXT, Token.T_BINARY, null, Token.T_COMPRESSED
    };
    public static final int      SCRIPT_TEXT_170          = 0;
    public static final int      SCRIPT_BINARY_172        = 1;
    public static final int      SCRIPT_ZIPPED_BINARY_172 = 3;

    public static ScriptWriterBase newScriptWriter(Database db, String file,
            boolean includeCachedData, boolean newFile,
            int scriptType) throws HsqlException {

        if (scriptType == SCRIPT_TEXT_170) {
            return new ScriptWriterText(db, file, includeCachedData, newFile,
                                        false);
        } else if (scriptType == SCRIPT_BINARY_172) {
            return new ScriptWriterBinary(db, file, includeCachedData,
                                          newFile);
        } else {
            return new ScriptWriterZipped(db, file, includeCachedData,
                                          newFile);
        }
    }

    ScriptWriterBase() {}

    ScriptWriterBase(Database db, String file, boolean includeCachedData,
                     boolean isNewFile, boolean isDump) throws HsqlException {

        this.isDump = isDump;

        initBuffers();

        boolean exists = false;

        if (isDump) {
            exists = FileUtil.exists(file);
        } else {
            exists = db.getFileAccess().isStreamElement(file);
        }

        if (exists && isNewFile) {
            throw Trace.error(Trace.FILE_IO_ERROR, file);
        }

        this.database          = db;
        this.includeCachedData = includeCachedData;
        outFile                = file;
        currentSession         = database.sessionManager.getSysSession();

        // start with neutral schema - no SET SCHEMA to log
        schemaToLog = currentSession.loggedSchema =
            currentSession.currentSchema;

        openFile();
    }

    public void reopen() throws HsqlException {
        openFile();
    }

    protected abstract void initBuffers();

    /**
     *  Called internally or externally in write delay intervals.
     */
    public synchronized void sync() {

        if (needsSync && fileStreamOut != null) {
            if (busyWriting) {
                forceSync = true;

                return;
            }

            try {
                fileStreamOut.flush();
                outDescriptor.sync();

                syncCount++;
            } catch (IOException e) {
                Trace.printSystemOut("flush() or sync() error: "
                                     + e.toString());
            }

            needsSync = false;
            forceSync = false;
        }
    }

    public void close() throws HsqlException {

        stop();

        try {
            if (fileStreamOut != null) {
                fileStreamOut.flush();
                fileStreamOut.close();

                fileStreamOut = null;
            }
        } catch (IOException e) {
            throw Trace.error(Trace.FILE_IO_ERROR);
        }

        byteCount = 0;
    }

    public long size() {
        return byteCount;
    }

    public void writeAll() throws HsqlException {

        try {
            writeDDL();
            writeExistingData();
            finishStream();
        } catch (IOException e) {
            throw Trace.error(Trace.FILE_IO_ERROR);
        }
    }

    /**
     *  File is opened in append mode although in current usage the file
     *  never pre-exists
     */
    protected void openFile() throws HsqlException {

        try {
            FileAccess   fa  = isDump ? FileUtil.getDefaultInstance()
                                      : database.getFileAccess();
            OutputStream fos = fa.openOutputStreamElement(outFile);

            outDescriptor = fa.getFileSync(fos);
            fileStreamOut = new BufferedOutputStream(fos, 2 << 12);
        } catch (IOException e) {
            throw Trace.error(Trace.FILE_IO_ERROR, Trace.Message_Pair,
                              new Object[] {
                e.toString(), outFile
            });
        }
    }

    /**
     * This is not really useful in the current usage but may be if this
     * class is used in a different way.
     */
    protected void finishStream() throws IOException {}

    protected void writeDDL() throws IOException, HsqlException {

        Result ddlPart = DatabaseScript.getScript(database,
            !includeCachedData);

        writeSingleColumnResult(ddlPart);
    }

    protected void writeExistingData() throws HsqlException, IOException {

        // start with blank schema - SET SCHEMA to log
        currentSession.loggedSchema = null;

        Iterator schemas = database.schemaManager.userSchemaNameIterator();

        while (schemas.hasNext()) {
            String   schema = (String) schemas.next();
            Iterator tables = database.schemaManager.tablesIterator(schema);

            while (tables.hasNext()) {
                Table t = (Table) tables.next();

                // write all memory table data
                // write cached table data unless index roots have been written
                // write all text table data apart from readonly text tables
                // unless index roots have been written
                boolean script = false;

                switch (t.getTableType()) {

                    case Table.MEMORY_TABLE :
                        script = true;
                        break;

                    case Table.CACHED_TABLE :
                        script = includeCachedData;
                        break;

                    case Table.TEXT_TABLE :
                        script = includeCachedData &&!t.isReadOnly();
                        break;
                }

                try {
                    if (script) {
                        schemaToLog = t.getName().schema;

                        writeTableInit(t);

                        RowIterator it = t.rowIterator(currentSession);

                        while (it.hasNext()) {
                            writeRow(currentSession, t, it.next().getData());
                        }

                        writeTableTerm(t);
                    }
                } catch (Exception e) {
                    throw Trace.error(Trace.ASSERT_FAILED, e.toString());
                }
            }
        }

        writeDataTerm();
    }

    protected void writeTableInit(Table t)
    throws HsqlException, IOException {}

    protected void writeTableTerm(Table t) throws HsqlException, IOException {

        if (t.isDataReadOnly() &&!t.isTemp() &&!t.isText()) {
            StringBuffer a = new StringBuffer("SET TABLE ");

            a.append(t.getName().statementName);
            a.append(" READONLY TRUE");
            writeLogStatement(currentSession, a.toString());
        }
    }

    protected void writeSingleColumnResult(Result r)
    throws HsqlException, IOException {

        Iterator it = r.iterator();

        while (it.hasNext()) {
            Object[] data = (Object[]) it.next();

            writeLogStatement(currentSession, (String) data[0]);
        }
    }

    abstract void writeRow(Session session, Table table,
                           Object[] data) throws HsqlException, IOException;

    protected abstract void writeDataTerm() throws IOException;

    protected abstract void addSessionId(Session session) throws IOException;

    public abstract void writeLogStatement(Session session,
                                           String s)
                                           throws IOException, HsqlException;

    public abstract void writeInsertStatement(Session session, Table table,
            Object[] data) throws HsqlException, IOException;

    public abstract void writeDeleteStatement(Session session, Table table,
            Object[] data) throws HsqlException, IOException;

    public abstract void writeSequenceStatement(Session session,
            NumberSequence seq) throws HsqlException, IOException;

    public abstract void writeCommitStatement(Session session)
    throws HsqlException, IOException;

    //
    private Object timerTask;

    // long write delay for scripts : 60s
    protected volatile int writeDelay = 60000;

    public void run() {

        try {
            if (writeDelay != 0) {
                sync();
            }

            // todo: try to do Cache.cleanUp() here, too
        } catch (Exception e) {

            // ignore exceptions
            // may be InterruptedException or IOException
            if (Trace.TRACE) {
                Trace.printSystemOut(e.toString());
            }
        }
    }

    public void setWriteDelay(int delay) {

        writeDelay = delay;

        int period = writeDelay == 0 ? 1000
                                     : writeDelay;

        HsqlTimer.setPeriod(timerTask, period);
    }

    public void start() {

        int period = writeDelay == 0 ? 1000
                                     : writeDelay;

        timerTask = DatabaseManager.getTimer().schedulePeriodicallyAfter(0,
                period, this, false);
    }

    public void stop() {

        if (timerTask != null) {
            HsqlTimer.cancel(timerTask);

            timerTask = null;
        }
    }

    public int getWriteDelay() {
        return writeDelay;
    }
}
