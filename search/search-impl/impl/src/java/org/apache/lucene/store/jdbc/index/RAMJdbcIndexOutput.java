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

import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.jdbc.JdbcDirectory;
import org.apache.lucene.store.jdbc.JdbcFileEntrySettings;
import org.apache.lucene.store.jdbc.support.InputStreamBlob;
import org.apache.lucene.store.jdbc.support.JdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.util.ArrayList;

/**
 * An <code>IndexOutput</code> implemenation that stores all the data written to it in memory, and flushes it
 * to the database when the output is closed.
 * <p/>
 * Useful for small file entries like the segment file.
 *
 * @author kimchy
 */
public class RAMJdbcIndexOutput extends JdbcBufferedIndexOutput {

    private class RAMFile {
        ArrayList buffers = new ArrayList();
        long length;
    }

    private class RAMInputStream extends InputStream {

        private long position;

        private int buffer;

        private int bufferPos;

        private long markedPosition;

        public synchronized void reset() throws IOException {
            position = markedPosition;
        }

        public boolean markSupported() {
            return true;
        }

        public void mark(int readlimit) {
            this.markedPosition = position;
        }

        public int read(byte[] dest, int destOffset, int len) throws IOException {
            if (position == file.length) {
                return -1;
            }
            int remainder = (int) ((position + len > file.length) ? file.length - position : len);
            long oldPosition = position;
            while (remainder != 0) {
                if (bufferPos == bufferSize) {
                    bufferPos = 0;
                    buffer++;
                }
                int bytesToCopy = bufferSize - bufferPos;
                bytesToCopy = bytesToCopy >= remainder ? remainder : bytesToCopy;
                byte[] buf = (byte[]) file.buffers.get(buffer);
                System.arraycopy(buf, bufferPos, dest, destOffset, bytesToCopy);
                destOffset += bytesToCopy;
                position += bytesToCopy;
                bufferPos += bytesToCopy;
                remainder -= bytesToCopy;
            }
            return (int) (position - oldPosition);
        }

        public int read() throws IOException {
            if (position == file.length) {
                return -1;
            }
            if (bufferPos == bufferSize) {
                bufferPos = 0;
                buffer++;
            }
            byte[] buf = (byte[]) file.buffers.get(buffer);
            position++;
            return buf[bufferPos++] & 0xFF;
        }
    }

    private RAMFile file;

    private int pointer = 0;

    private String name;

    private JdbcDirectory jdbcDirectory;

    public void configure(String name, JdbcDirectory jdbcDirectory, JdbcFileEntrySettings settings) throws IOException {
        super.configure(name, jdbcDirectory, settings);
        this.file = new RAMFile();
        this.name = name;
        this.jdbcDirectory = jdbcDirectory;
    }

    public void flushBuffer(byte[] src, int len) {
        byte[] buffer;
        int bufferPos = 0;
        while (bufferPos != len) {
            int bufferNumber = pointer / bufferSize;
            int bufferOffset = pointer % bufferSize;
            int bytesInBuffer = bufferSize - bufferOffset;
            int remainInSrcBuffer = len - bufferPos;
            int bytesToCopy = bytesInBuffer >= remainInSrcBuffer ? remainInSrcBuffer : bytesInBuffer;

            if (bufferNumber == file.buffers.size()) {
                buffer = new byte[bufferSize];
                file.buffers.add(buffer);
            } else {
                buffer = (byte[]) file.buffers.get(bufferNumber);
            }

            System.arraycopy(src, bufferPos, buffer, bufferOffset, bytesToCopy);
            bufferPos += bytesToCopy;
            pointer += bytesToCopy;
        }

        if (pointer > file.length)
            file.length = pointer;
    }

    public void close() throws IOException {
        super.close();
        jdbcDirectory.getJdbcTemplate().executeUpdate(jdbcDirectory.getTable().sqlInsert(),
                new JdbcTemplate.PrepateStatementAwareCallback() {
                    public void fillPrepareStatement(PreparedStatement ps) throws Exception {
                        ps.setFetchSize(1);
                        ps.setString(1, name);
                        if (jdbcDirectory.getDialect().useInputStreamToInsertBlob()) {
                            ps.setBinaryStream(2, new RAMInputStream(), (int) length());
                        } else {
                            ps.setBlob(2, new InputStreamBlob(new RAMInputStream(), length()));
                        }
                        ps.setLong(3, length());
                        ps.setBoolean(4, false);
                    }
                });
        file = null;
    }

    public void seek(long pos) throws IOException {
        super.seek(pos);
        pointer = (int) pos;
    }

    public long length() {
        return file.length;
    }

    public void flushToIndexOutput(IndexOutput indexOutput) throws IOException {
        super.flush();
        if (file.buffers.size() == 0) {
            return;
        }
        if (file.buffers.size() == 1) {
            indexOutput.writeBytes((byte[]) file.buffers.get(0), (int) file.length);
            return;
        }
        int tempSize = file.buffers.size() - 1;
        int i;
        for (i = 0; i < tempSize; i++) {
            indexOutput.writeBytes((byte[]) file.buffers.get(i), bufferSize);
        }
        int leftOver = (int) (file.length % bufferSize);
        if (leftOver == 0) {
            indexOutput.writeBytes((byte[]) file.buffers.get(i), bufferSize);
        } else {
            indexOutput.writeBytes((byte[]) file.buffers.get(i), leftOver);
        }
    }
}
