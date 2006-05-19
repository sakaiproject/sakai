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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.sql.PreparedStatement;

import org.apache.lucene.store.jdbc.JdbcDirectory;
import org.apache.lucene.store.jdbc.JdbcFileEntrySettings;
import org.apache.lucene.store.jdbc.support.InputStreamBlob;
import org.apache.lucene.store.jdbc.support.JdbcTemplate;

/**
 * An <code>IndexOutput</code> implemenation that writes all the data to a temporary file, and when closed, flushes
 * the file to the database.
 * <p/>
 * Usefull for large files that are known in advance to be larger then the acceptable threshold configured in
 * {@link RAMAndFileJdbcIndexOutput}.
 *
 * @author kimchy
 */
public class FileJdbcIndexOutput extends JdbcBufferedIndexOutput {

    private String name;

    private JdbcDirectory jdbcDirectory;

    private RandomAccessFile file = null;

    public void configure(String name, JdbcDirectory jdbcDirectory, JdbcFileEntrySettings settings) throws IOException {
        super.configure(name, jdbcDirectory, settings);
        File tempFile = File.createTempFile(jdbcDirectory.getTable().getName() + "_" + name + "_" + System.currentTimeMillis(), ".ljt");
        tempFile.deleteOnExit();
        this.file = new RandomAccessFile(tempFile, "rw");
        this.jdbcDirectory = jdbcDirectory;
        this.name = name;
    }

    /**
     * output methods:
     */
    public void flushBuffer(byte[] b, int size) throws IOException {
        file.write(b, 0, size);
    }

    /**
     * Random-access methods
     */
    public void seek(long pos) throws IOException {
        super.seek(pos);
        file.seek(pos);
    }

    public long length() throws IOException {
        return file.length();
    }

    protected void finalize() throws Throwable {
        super.finalize();
        file.close();          // close the file
    }

    public void close() throws IOException {
        super.close();
        final long length = length();
        file.seek(0);
        jdbcDirectory.getJdbcTemplate().executeUpdate(jdbcDirectory.getTable().sqlInsert(), new JdbcTemplate.PrepateStatementAwareCallback() {
            public void fillPrepareStatement(PreparedStatement ps) throws Exception {
                ps.setFetchSize(1);
                ps.setString(1, name);
                //TODO most driver should probably have internal buffers, so adding the BufferInputStream might be redundant
                InputStream is = new BufferedInputStream(new FileInputStream(file.getFD()));
                if (jdbcDirectory.getDialect().useInputStreamToInsertBlob()) {
                    ps.setBinaryStream(2, is, (int) length());
                } else {
                    ps.setBlob(2, new InputStreamBlob(is, length));
                }
                ps.setLong(3, length);
                ps.setBoolean(4, false);
            }
        });
        file.close();
    }
}
