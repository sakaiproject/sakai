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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.hsqldb.CachedRow;
import org.hsqldb.Column;
import org.hsqldb.HsqlDateTime;
import org.hsqldb.lib.StringConverter;
import org.hsqldb.types.Binary;
import org.hsqldb.types.JavaObject;

/**
 * @author fredt@users
 * @since 1.7.2
 * @version 1.7.2
 */
public class RowOutputTextLog extends RowOutputBase {

    static final byte[]     BYTES_NULL  = "NULL".getBytes();
    static final byte[]     BYTES_TRUE  = "TRUE".getBytes();
    static final byte[]     BYTES_FALSE = "FALSE".getBytes();
    static final byte[]     BYTES_AND   = " AND ".getBytes();
    static final byte[]     BYTES_IS    = " IS ".getBytes();
    public static final int MODE_DELETE = 1;
    public static final int MODE_INSERT = 0;
    private boolean         isWritten;
    private int             logMode;

    public void setMode(int mode) {
        logMode = mode;
    }

    protected void writeFieldPrefix() {

        if (logMode == MODE_DELETE && isWritten) {
            write(BYTES_AND);
        }
    }

    protected void writeChar(String s, int t) {

        write('\'');
        StringConverter.unicodeToAscii(this, s, true);
        write('\'');
    }

    protected void writeReal(Double o, int type) {
        writeBytes(Column.createSQLString(((Number) o).doubleValue()));
    }

    protected void writeSmallint(Number o) {
        this.writeBytes(o.toString());
    }

    public void writeEnd() {}

    protected void writeTime(Time o) {

        write('\'');
        writeBytes(o.toString());
        write('\'');
    }

    protected void writeBinary(Binary o, int t) {

        ensureRoom(o.getBytesLength() * 2 + 2);
        write('\'');
        StringConverter.writeHex(getBuffer(), count, o.getBytes());

        count += o.getBytesLength() * 2;

        write('\'');
    }

    public void writeType(int type) {}

    public void writeSize(int size) {}

    protected void writeDate(Date o) {

        write('\'');
        this.writeBytes(o.toString());
        write('\'');
    }

    public int getSize(CachedRow row) {
        return 0;
    }

    protected void writeInteger(Number o) {
        this.writeBytes(o.toString());
    }

    protected void writeBigint(Number o) {
        this.writeBytes(o.toString());
    }

//fredt@users - patch 1108647 by nkowalcz@users (NataliaK) fix for IS NULL
    protected void writeNull(int type) {

        if (logMode == MODE_DELETE) {
            write(BYTES_IS);
        } else if (isWritten) {
            write(',');
        }

        isWritten = true;

        write(BYTES_NULL);
    }

    protected void writeOther(JavaObject o) {

        ensureRoom(o.getBytesLength() * 2 + 2);
        write('\'');
        StringConverter.writeHex(getBuffer(), count, o.getBytes());

        count += o.getBytesLength() * 2;

        write('\'');
    }

    public void writeString(String value) {
        StringConverter.unicodeToAscii(this, value, false);
    }

    protected void writeBit(Boolean o) {
        write(o.booleanValue() ? BYTES_TRUE
                               : BYTES_FALSE);
    }

    protected void writeDecimal(BigDecimal o) {
        this.writeBytes(o.toString());
    }

    protected void writeFieldType(int type) {

        if (logMode == MODE_DELETE) {
            write('=');
        } else if (isWritten) {
            write(',');
        }

        isWritten = true;
    }

    public void writeLongData(long value) {
        this.writeBytes(Long.toString(value));
    }

    public void writeIntData(int i, int position) {}

    protected void writeTimestamp(Timestamp o) {

        write('\'');
        this.writeBytes(HsqlDateTime.getTimestampString(o));
        write('\'');
    }

    public void writeShortData(short i) {
        writeBytes(Integer.toString(i));
    }

    public void writeIntData(int i) {
        writeBytes(Integer.toString(i));
    }

    public void reset() {

        super.reset();

        isWritten = false;
    }
}
