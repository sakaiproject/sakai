/*
 * Copyright 2004-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.store.jdbc.index;

import java.io.IOException;

import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.jdbc.JdbcDirectory;
import org.apache.lucene.store.jdbc.JdbcFileEntrySettings;

/**
 * A simple base class that performs index input memory based buffering. The buffer size can be configured
 * under the {@link #BUFFER_SIZE_SETTING} name.
 *
 * @author kimchy
 */
public abstract class JdbcBufferedIndexInput extends IndexInput implements JdbcIndexConfigurable {

    /**
     * The buffer size setting name. See {@link JdbcFileEntrySettings#setIntSetting(String, int)}.
     * Should be set in bytes.
     */
    public static final String BUFFER_SIZE_SETTING = "indexInput.bufferSize";

    /**
     * The default value for the buffer size (in bytes). Currently 1024.
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    protected byte[] buffer;

    protected long bufferStart = 0;              // position in file of buffer
    protected int bufferLength = 0;              // end of valid bytes
    protected int bufferPosition = 0;          // next byte to read

    protected int bufferSize;

    public void configure(String name, JdbcDirectory jdbcDirectory, JdbcFileEntrySettings settings) throws IOException {
        bufferSize = settings.getSettingAsInt(BUFFER_SIZE_SETTING, DEFAULT_BUFFER_SIZE);
    }

    public byte readByte() throws IOException {
        if (bufferPosition >= bufferLength)
            refill();
        return buffer[bufferPosition++];
    }

    public void readBytes(byte[] b, int offset, int len)
            throws IOException {
        if (len < bufferSize) {
            for (int i = 0; i < len; i++)          // read byte-by-byte
                b[i + offset] = readByte();
        } else {                      // read all-at-once
            long start = getFilePointer();
            seekInternal(start);
            readInternal(b, offset, len);

            bufferStart = start + len;          // adjust stream variables
            bufferPosition = 0;
            bufferLength = 0;                  // trigger refill() on read
        }
    }

    protected void refill() throws IOException {
        long start = bufferStart + bufferPosition;
        long end = start + bufferSize;
        if (end > length())                  // don't read past EOF
            end = length();
        bufferLength = (int) (end - start);
        if (bufferLength <= 0)
            throw new IOException("read past EOF");

        if (buffer == null)
            buffer = new byte[bufferSize];          // allocate buffer lazily
        readInternal(buffer, 0, bufferLength);

        bufferStart = start;
        bufferPosition = 0;
    }

    /**
     * Expert: implements buffer refill.  Reads bytes from the current position
     * in the input.
     *
     * @param b      the array to read bytes into
     * @param offset the offset in the array to start storing bytes
     * @param length the number of bytes to read
     */
    protected abstract void readInternal(byte[] b, int offset, int length)
            throws IOException;

    public long getFilePointer() {
        return bufferStart + bufferPosition;
    }

    public void seek(long pos) throws IOException {
        if (pos >= bufferStart && pos < (bufferStart + bufferLength))
            bufferPosition = (int) (pos - bufferStart);  // seek within buffer
        else {
            bufferStart = pos;
            bufferPosition = 0;
            bufferLength = 0;                  // trigger refill() on read()
            seekInternal(pos);
        }
    }

    /**
     * Expert: implements seek.  Sets current position in this file, where the
     * next {@link #readInternal(byte[],int,int)} will occur.
     *
     * @see #readInternal(byte[],int,int)
     */
    protected abstract void seekInternal(long pos) throws IOException;

    public Object clone() {
        JdbcBufferedIndexInput clone = (JdbcBufferedIndexInput) super.clone();

        if (buffer != null) {
            clone.buffer = new byte[bufferSize];
            System.arraycopy(buffer, 0, clone.buffer, 0, bufferLength);
        }

        return clone;
    }


}
