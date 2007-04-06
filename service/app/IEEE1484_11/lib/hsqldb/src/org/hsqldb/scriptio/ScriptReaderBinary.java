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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.hsqldb.Database;
import org.hsqldb.HsqlException;
import org.hsqldb.Result;
import org.hsqldb.ResultConstants;
import org.hsqldb.Session;
import org.hsqldb.Table;
import org.hsqldb.Trace;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.SimpleLog;
import org.hsqldb.rowio.RowInputBase;
import org.hsqldb.rowio.RowInputBinary;

/**
 * Reader corresponding to BinaryDatabaseScritReader.
 *
 * @author fredt@users
 * @version 1.7.2
 * @since 1.7.2
 */
class ScriptReaderBinary extends ScriptReaderBase {

    private RowInputBinary    rowIn;
    protected DataInputStream dataStreamIn;

    ScriptReaderBinary(Database db,
                       String file) throws HsqlException, IOException {

        super(db, file);

        rowIn = new RowInputBinary();
    }

    protected void openFile() throws IOException {

        InputStream d = db.isFilesInJar()
                        ? getClass().getResourceAsStream(fileName)
                        : db.getFileAccess().openInputStreamElement(fileName);

        dataStreamIn = new DataInputStream(new BufferedInputStream(d,
                1 << 13));
    }

    protected void readDDL(Session session)
    throws IOException, HsqlException {

        Result   r  = Result.read(rowIn, dataStreamIn);
        Iterator it = r.iterator();

        while (it.hasNext()) {
            Object[] data   = (Object[]) it.next();
            String   s      = (String) data[0];
            Result   result = session.sqlExecuteDirectNoPreChecks(s);

            if (result.isError()) {
                db.logger.appLog.logContext(SimpleLog.LOG_ERROR,
                                            result.getMainString());

                /** @todo fredt - trap if unavaialble external functions are to be ignored */
                throw Trace.error(result);
            }
        }
    }

    protected void readExistingData(Session session)
    throws IOException, HsqlException {

        for (;;) {
            String s = readTableInit();

            if (s == null) {
                break;
            }

            String schema = session.getSchemaName(currentSchema);
            Table  t      = db.schemaManager.getUserTable(session, s, schema);
            int    j      = 0;

            for (j = 0; ; j++) {
                if (readRow(t) == false) {
                    break;
                }
            }

            int checkCount = readTableTerm();

            if (j != checkCount) {
                throw Trace.error(Trace.ERROR_IN_SCRIPT_FILE,
                                  Trace.ERROR_IN_BINARY_SCRIPT_1,
                                  new Object[] {
                    s, new Integer(j), new Integer(checkCount)
                });
            }
        }
    }

    // int : row size (0 if no more rows) ,
    // BinaryServerRowInput : row (column values)
    protected boolean readRow(Table t) throws IOException, HsqlException {

        boolean more = readRow(rowIn, 0);

        if (!more) {
            return false;
        }

        Object[] data = rowIn.readData(t.getColumnTypes());

        t.insertFromScript(data);

        return true;
    }

    // int : rowcount
    protected int readTableTerm() throws IOException, HsqlException {
        return dataStreamIn.readInt();
    }

    // int : headersize (0 if no more tables), String : tablename, int : operation,
    // String : schemaname
    protected String readTableInit() throws IOException, HsqlException {

        boolean more = readRow(rowIn, 0);

        if (!more) {
            return null;
        }

        String s = rowIn.readString();

        // operation is always INSERT
        int checkOp = rowIn.readIntData();

        if (checkOp == ScriptWriterBase.INSERT_WITH_SCHEMA) {
            currentSchema = rowIn.readString();
        } else {
            currentSchema = null;
        }

        if (checkOp != ScriptWriterBase.INSERT
                && checkOp != ScriptWriterBase.INSERT_WITH_SCHEMA) {
            throw Trace.error(Trace.ERROR_IN_SCRIPT_FILE,
                              Trace.ERROR_IN_BINARY_SCRIPT_2);
        }

        return s;
    }

    boolean readRow(RowInputBase rowin, int pos) throws IOException {

        try {
            int length = dataStreamIn.readInt();
            int count  = 4;

            if (length == 0) {
                return false;
            }

            rowin.resetRow(pos, length);
            dataStreamIn.readFully(rowin.getBuffer(), count, length - count);

            return true;
        } catch (EOFException e) {
            return false;
        }
    }

    public boolean readLoggedStatement(Session session) throws IOException {
        return false;
    }

    public void close() {

        try {
            dataStreamIn.close();
        } catch (IOException e) {}
    }
}
