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
 * An <code>IndexOutput</code> implementation that initially writes the data to a memory buffer. Once it exceeds
 * the configured threshold ({@link #INDEX_OUTPUT_THRESHOLD_SETTING}, will start working with a temporary file,
 * releasing the previous buffer.
 *
 * @author kimchy
 */
public class RAMAndFileJdbcIndexOutput extends IndexOutput implements JdbcIndexConfigurable {

    /**
     * The threshold setting name. See {@link JdbcFileEntrySettings#setLongSetting(String, long)}.
     * Should be set in bytes.
     */
    public static final String INDEX_OUTPUT_THRESHOLD_SETTING = "indexOutput.threshold";

    /**
     * The default value for the threshold (in bytes). Currently 16K.
     */
    public static final long DEFAULT_THRESHOLD = 16 * 1024;

    private long threshold;

    private RAMJdbcIndexOutput ramIndexOutput;

    private FileJdbcIndexOutput fileIndexOutput;

    private JdbcDirectory jdbcDirectory;

    private String name;

    private JdbcFileEntrySettings settings;

    private long position;

    public void configure(String name, JdbcDirectory jdbcDirectory, JdbcFileEntrySettings settings) throws IOException {
        this.jdbcDirectory = jdbcDirectory;
        this.name = name;
        this.settings = settings;
        this.threshold = settings.getSettingAsLong(INDEX_OUTPUT_THRESHOLD_SETTING, DEFAULT_THRESHOLD);
        ramIndexOutput = new RAMJdbcIndexOutput();
        ramIndexOutput.configure(name, jdbcDirectory, settings);
    }

    public void writeByte(byte b) throws IOException {
        switchIfNeeded(1).writeByte(b);
    }

    public void writeBytes(byte[] b, int length) throws IOException {
        switchIfNeeded(length).writeBytes(b, length);
    }

    public void flush() throws IOException {
        actualOutput().flush();
    }

    public void close() throws IOException {
        actualOutput().close();
    }

    public long getFilePointer() {
        return actualOutput().getFilePointer();
    }

    public void seek(long pos) throws IOException {
        position = pos;
        actualOutput().seek(pos);
    }

    public long length() throws IOException {
        return actualOutput().length();
    }

    private IndexOutput actualOutput() {
        if (fileIndexOutput != null) {
            return fileIndexOutput;
        }
        return ramIndexOutput;
    }

    private IndexOutput switchIfNeeded(int length) throws IOException {
        if (fileIndexOutput != null) {
            return fileIndexOutput;
        }
        position += length;
        if (position < threshold) {
            return ramIndexOutput;
        }
        fileIndexOutput = new FileJdbcIndexOutput();
        fileIndexOutput.configure(name, jdbcDirectory, settings);
        ramIndexOutput.flushToIndexOutput(fileIndexOutput);
        // let it be garbage collected
        ramIndexOutput = null;

        return fileIndexOutput;
    }
}
