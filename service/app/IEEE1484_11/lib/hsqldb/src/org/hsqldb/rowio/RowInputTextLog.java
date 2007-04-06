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


package org.hsqldb.rowio;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.hsqldb.Column;
import org.hsqldb.HsqlDateTime;
import org.hsqldb.HsqlException;
import org.hsqldb.Token;
import org.hsqldb.Tokenizer;
import org.hsqldb.Types;
import org.hsqldb.scriptio.ScriptReaderBase;
import org.hsqldb.store.ValuePool;
import org.hsqldb.types.Binary;
import org.hsqldb.types.JavaObject;
import org.hsqldb.lib.java.JavaSystem;

/**
 *  Class for reading the data for a database row from the script file.
 *
 * @author fredt@users
 * @version 1.8.0
 * @since 1.7.3
 */
public class RowInputTextLog extends RowInputBase
implements RowInputInterface {

    Tokenizer tokenizer;
    String    tableName  = null;
    String    schemaName = null;
    int       statementType;

    public RowInputTextLog() {

        super(new byte[0]);

        tokenizer = new Tokenizer();
    }

    public void setSource(String text) throws HsqlException {

        tokenizer.reset(text);

        statementType = ScriptReaderBase.ANY_STATEMENT;

        String s = tokenizer.getString();

        if (s.equals(Token.T_INSERT)) {
            statementType = ScriptReaderBase.INSERT_STATEMENT;

            tokenizer.getString();

            tableName = tokenizer.getString();

            tokenizer.getString();
        } else if (s.equals(Token.T_DELETE)) {
            statementType = ScriptReaderBase.DELETE_STATEMENT;

            tokenizer.getString();

            tableName = tokenizer.getString();
        } else if (s.equals(Token.T_COMMIT)) {
            statementType = ScriptReaderBase.COMMIT_STATEMENT;
        } else if (s.equals(Token.T_SET)) {
            if (tokenizer.isGetThis(Token.T_SCHEMA)) {
                schemaName    = tokenizer.getSimpleName();
                statementType = ScriptReaderBase.SCHEMA_STATEMENT;
            }
        }
    }

    public int getStatementType() {
        return statementType;
    }

    public String getTableName() {
        return tableName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    protected String readField() throws IOException {

        try {
            tokenizer.getString();

            if (statementType == ScriptReaderBase.DELETE_STATEMENT) {
                tokenizer.getString();
                tokenizer.getString();
            }

            String s = tokenizer.getString();

            if (tokenizer.getType() == Types.NULL) {
                s = null;
            }

            return s;
        } catch (HsqlException e) {
            throw new IOException(e.getMessage());
        }
    }

    protected String readNumberField() throws IOException {

        try {
            tokenizer.getString();

            if (statementType == ScriptReaderBase.DELETE_STATEMENT) {
                tokenizer.getString();
                tokenizer.getString();
            }

            String s = tokenizer.getString();

            if ("-".equals(s)) {
                s = s + tokenizer.getString();
            } else if (tokenizer.getType() == Types.NULL) {
                s = null;
            }

            return s;
        } catch (HsqlException e) {
            throw new IOException(e.getMessage());
        }
    }

    public String readString() throws IOException {

        String s = readField();

        return ValuePool.getString(s);
    }

    public short readShortData() throws IOException {

        String s = readNumberField();

        if (s == null) {
            return 0;
        }

        return Short.parseShort(s);
    }

    public int readIntData() throws IOException {

        String s = readNumberField();

        if (s == null) {
            return 0;
        }

        return Integer.parseInt(s);
    }

    public long readLongData() throws IOException {

        String s = readNumberField();

        if (s == null) {
            return 0;
        }

        return Long.parseLong(s);
    }

    public int readType() throws IOException {
        return 0;
    }

    protected boolean checkNull() {

        // Return null on each column read instead.
        return false;
    }

    protected String readChar(int type) throws IOException {
        return readString();
    }

    protected Integer readSmallint() throws IOException, HsqlException {

        String s = readNumberField();

        if (s == null) {
            return null;
        }

        int i = Integer.parseInt(s);

        return ValuePool.getInt(i);
    }

    protected Integer readInteger() throws IOException, HsqlException {

        String s = readNumberField();

        if (s == null) {
            return null;
        }

        int i = Integer.parseInt(s);

        return ValuePool.getInt(i);
    }

    protected Long readBigint() throws IOException, HsqlException {

        String s = readNumberField();

        if (s == null) {
            return null;
        }

        long i = Long.parseLong(s);

        return ValuePool.getLong(i);
    }

    protected Double readReal(int type) throws IOException, HsqlException {

        String s = readNumberField();

        if (s == null) {
            return null;
        }

        double i = JavaSystem.parseDouble(s);

        if (tokenizer.isGetThis(Token.T_DIVIDE)) {
            s = tokenizer.getString();

            // parse simply to ensure it's a number
            double ii = JavaSystem.parseDouble(s);

            if (i == 0E0) {
                i = Double.NaN;
            } else if (i == -1E0) {
                i = Double.NEGATIVE_INFINITY;
            } else if (i == 1E0) {
                i = Double.POSITIVE_INFINITY;
            }
        }

        return ValuePool.getDouble(Double.doubleToLongBits(i));
    }

    protected BigDecimal readDecimal() throws IOException, HsqlException {

        String s = readNumberField();

        if (s == null) {
            return null;
        }

        BigDecimal i = new BigDecimal(s);

        return ValuePool.getBigDecimal(i);
    }

    protected Time readTime() throws IOException, HsqlException {

        String s = readField();

        if (s == null) {
            return null;
        }

        return HsqlDateTime.timeValue(s);
    }

    protected Date readDate() throws IOException, HsqlException {

        String s = readField();

        if (s == null) {
            return null;
        }

        return HsqlDateTime.dateValue(s);
    }

    protected Timestamp readTimestamp() throws IOException, HsqlException {

        String s = readField();

        if (s == null) {
            return null;
        }

        return HsqlDateTime.timestampValue(s);
    }

    protected Boolean readBit() throws IOException, HsqlException {

        String s = readField();

        if (s == null) {
            return null;
        }

        return s.equalsIgnoreCase("TRUE") ? Boolean.TRUE
                                          : Boolean.FALSE;
    }

    protected Object readOther() throws IOException, HsqlException {

        byte[] data;
        String s = readField();

        if (s == null) {
            return null;
        }

        data = Column.hexToByteArray(s);

        return new JavaObject(data);
    }

    protected Binary readBinary(int type) throws IOException, HsqlException {

        String s = readField();

        if (s == null) {
            return null;
        }

        return new Binary(Column.hexToByteArray(s), false);
    }
}
