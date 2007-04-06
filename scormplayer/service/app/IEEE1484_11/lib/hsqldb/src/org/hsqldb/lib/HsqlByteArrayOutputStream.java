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


package org.hsqldb.lib;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.io.UnsupportedEncodingException;

/**
 * This class is a replacement for both java.io.ByteArrayOuputStream
 * (without synchronization) and java.io.DataOutputStream
 *
 * @author fredt@users
 * @version 1.7.2
 * @since 1.7.0
 */
public class HsqlByteArrayOutputStream extends java.io.OutputStream
implements DataOutput {

    protected byte[] buf;
    protected int    count;

    public HsqlByteArrayOutputStream() {
        this(128);
    }

    public HsqlByteArrayOutputStream(int size) {

        if (size < 128) {
            size = 128;
        }

        buf = new byte[size];
    }

    public HsqlByteArrayOutputStream(byte[] buffer) {
        buf = buffer;
    }

    // methods that implement dataOutput
    public final void writeShort(int v) {

        ensureRoom(2);

        buf[count++] = (byte) (v >>> 8);
        buf[count++] = (byte) v;
    }

    public final void writeInt(int v) {

        if (count + 4 > buf.length) {
            ensureRoom(4);
        }

        buf[count++] = (byte) (v >>> 24);
        buf[count++] = (byte) (v >>> 16);
        buf[count++] = (byte) (v >>> 8);
        buf[count++] = (byte) v;
    }

    public final void writeLong(long v) {
        writeInt((int) (v >>> 32));
        writeInt((int) v);
    }

    public final void writeBytes(String s) {

        int len = s.length();

        ensureRoom(len);

        for (int i = 0; i < len; i++) {
            buf[count++] = (byte) s.charAt(i);
        }
    }

    public final void writeFloat(float v) {
        writeInt(Float.floatToIntBits(v));
    }

    public final void writeDouble(double v) {
        writeLong(Double.doubleToLongBits(v));
    }

    public void writeBoolean(boolean v) throws IOException {

        ensureRoom(1);

        buf[count++] = (byte) (v ? 1
                                 : 0);
    }

    public void writeByte(int v) throws IOException {

        ensureRoom(1);

        buf[count++] = (byte) (v);
    }

    public void writeChar(int v) throws IOException {

        ensureRoom(2);

        buf[count++] = (byte) (v >>> 8);
        buf[count++] = (byte) v;
    }

    public void writeChars(String s) throws IOException {

        int len = s.length();

        ensureRoom(len * 2);

        for (int i = 0; i < len; i++) {
            int v = s.charAt(i);

            buf[count++] = (byte) (v >>> 8);
            buf[count++] = (byte) v;
        }
    }

    public void writeUTF(String str) throws IOException {

        int len = str.length();

        if (len > 0xffff) {
            throw new UTFDataFormatException();
        }

        ensureRoom(len * 3 + 2);

        //
        int initpos = count;

        count += 2;

        StringConverter.writeUTF(str, this);

        int bytecount = count - initpos - 2;

        if (bytecount > 0xffff) {
            count = initpos;

            throw new UTFDataFormatException();
        }

        buf[initpos++] = (byte) (bytecount >>> 8);
        buf[initpos]   = (byte) bytecount;
    }

    /**
     * does nothing
     */
    public void flush() throws java.io.IOException {
        super.flush();
    }

    // methods that extend java.io.OutputStream
    public void write(int b) {

        ensureRoom(1);

        buf[count++] = (byte) b;
    }

    public void write(byte[] b) {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) {

        ensureRoom(len);
        System.arraycopy(b, off, buf, count, len);

        count += len;
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(buf, 0, count);
    }

    public void reset() {
        count = 0;
    }

    public byte[] toByteArray() {

        byte[] newbuf = new byte[count];

        System.arraycopy(buf, 0, newbuf, 0, count);

        return newbuf;
    }

    public int size() {
        return count;
    }

    public String toString() {
        return new String(buf, 0, count);
    }

    public String toString(String enc) throws UnsupportedEncodingException {
        return new String(buf, 0, count, enc);
    }

    public void close() throws IOException {}

    // additional public methods not in similar java.util classes
    public void fill(int b, int len) {

        ensureRoom(len);

        for (int i = 0; i < len; i++) {
            buf[count++] = (byte) b;
        }
    }

    public byte[] getBuffer() {
        return this.buf;
    }

    protected void ensureRoom(int extra) {

        int newcount = count + extra;
        int newsize  = buf.length;

        if (newcount > newsize) {
            while (newcount > newsize) {
                newsize *= 2;
            }

            byte[] newbuf = new byte[newsize];

            System.arraycopy(buf, 0, newbuf, 0, count);

            buf = newbuf;
        }
    }

    protected void reset(int newSize) {

        count = 0;

        if (newSize > buf.length) {
            buf = new byte[newSize];
        }
    }
}
