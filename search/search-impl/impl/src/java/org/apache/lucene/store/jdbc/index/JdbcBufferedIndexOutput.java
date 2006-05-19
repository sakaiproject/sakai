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

import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.jdbc.JdbcDirectory;
import org.apache.lucene.store.jdbc.JdbcFileEntrySettings;

/**
 * A simple base class that performs index output memory based buffering. The buffer size can be configured
 * under the {@link #BUFFER_SIZE_SETTING} name.
 *
 * @author kimchy
 */
public abstract class JdbcBufferedIndexOutput extends IndexOutput implements JdbcIndexConfigurable {

    /**
     * The buffer size setting name. See {@link JdbcFileEntrySettings#setIntSetting(String, int)}.
     * Should be set in bytes.
     */
    public static final String BUFFER_SIZE_SETTING = "indexOutput.bufferSize";

    /**
     * The default value for the buffer size (in bytes). Currently 1024.
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    private byte[] buffer;
    private long bufferStart = 0;              // position in file of buffer
    private int bufferPosition = 0;          // position in buffer

    protected int bufferSize;

    public void configure(String name, JdbcDirectory jdbcDirectory, JdbcFileEntrySettings settings) throws IOException {
        bufferSize = settings.getSettingAsInt(BUFFER_SIZE_SETTING, DEFAULT_BUFFER_SIZE);
        buffer = new byte[bufferSize];
    }

    /**
     * Writes a single byte.
     *
     * @see org.apache.lucene.store.IndexInput#readByte()
     */
    public void writeByte(byte b) throws IOException {
        if (bufferPosition >= bufferSize)
            flush();
        buffer[bufferPosition++] = b;
    }

    /**
     * Writes an array of bytes.
     *
     * @param b      the bytes to write
     * @param length the number of bytes to write
     * @see org.apache.lucene.store.IndexInput#readBytes(byte[],int,int)
     */
    public void writeBytes(byte[] b, int length) throws IOException {
        int bytesLeft = bufferSize - bufferPosition;
        // is there enough space in the buffer?
        if (bytesLeft >= length) {
            // we add the data to the end of the buffer
            System.arraycopy(b, 0, buffer, bufferPosition, length);
            bufferPosition += length;
            // if the buffer is full, flush it
            if (bufferSize - bufferPosition == 0)
                flush();
        } else {
            // is data larger then buffer?
            if (length > bufferSize) {
                // we flush the buffer
                if (bufferPosition > 0)
                    flush();
                // and write data at once
                flushBuffer(b, length);
                bufferStart += length;
            } else {
                // we fill/flush the buffer (until the input is written)
                int pos = 0; // position in the input data
                int pieceLength;
                while (pos < length) {
                    pieceLength = (length - pos < bytesLeft) ? length - pos : bytesLeft;
                    System.arraycopy(b, pos, buffer, bufferPosition, pieceLength);
                    pos += pieceLength;
                    bufferPosition += pieceLength;
                    // if the buffer is full, flush it
                    bytesLeft = bufferSize - bufferPosition;
                    if (bytesLeft == 0) {
                        flush();
                        bytesLeft = bufferSize;
                    }
                }
            }
        }
    }

    /**
     * Forces any buffered output to be written.
     */
    public void flush() throws IOException {
        flushBuffer(buffer, bufferPosition);
        bufferStart += bufferPosition;
        bufferPosition = 0;
    }

    /**
     * Expert: implements buffer write.  Writes bytes at the current position in
     * the output.
     *
     * @param b   the bytes to write
     * @param len the number of bytes to write
     */
    protected abstract void flushBuffer(byte[] b, int len) throws IOException;

    /**
     * Closes this stream to further operations.
     */
    public void close() throws IOException {
        flush();
    }

    /**
     * Returns the current position in this file, where the next write will
     * occur.
     *
     * @see #seek(long)
     */
    public long getFilePointer() {
        return bufferStart + bufferPosition;
    }

    /**
     * Sets current position in this file, where the next write will occur.
     *
     * @see #getFilePointer()
     */
    public void seek(long pos) throws IOException {
        flush();
        bufferStart = pos;
    }

    /**
     * The number of bytes in the file.
     */
    public abstract long length() throws IOException;


}
