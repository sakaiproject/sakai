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

package org.apache.lucene.store.jdbc.support;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * A helper class that can wrap an <code>InputStream</code> as a Jdbc <code>Blob<code>.
 * <p>
 * Some jdbc drivers do not support the {@link java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, int)}
 * method, but require using {@link java.sql.PreparedStatement#setBlob(int, java.sql.Blob)}. For code that already has
 * an <code>InputStream<code> ready, this <code>Blob</code> implementation can help.
 *
 * @see org.apache.lucene.store.jdbc.dialect.Dialect#useInputStreamToInsertBlob()  
 *
 * @author kimchy
 */
public class InputStreamBlob implements Blob {

    private InputStream is;

    private long length;

    public InputStreamBlob(InputStream is, long length) {
        this.is = is;
        this.length = length;
    }

    public long length() throws SQLException {
        return this.length;
    }

    public void truncate(long len) throws SQLException {
        throw new UnsupportedOperationException("");
    }

    public byte[] getBytes(long pos, int length) throws SQLException {
        throw new UnsupportedOperationException("");
    }

    public int setBytes(long pos, byte[] bytes) throws SQLException {
        throw new UnsupportedOperationException("");
    }

    public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
        throw new UnsupportedOperationException("");
    }

    public long position(byte pattern[], long start) throws SQLException {
        throw new UnsupportedOperationException("");
    }

    public InputStream getBinaryStream() throws SQLException {
        return is;
    }

    public OutputStream setBinaryStream(long pos) throws SQLException {
        throw new UnsupportedOperationException("");
    }

    public long position(Blob pattern, long start) throws SQLException {
        throw new UnsupportedOperationException("");
    }
}
