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


package org.hsqldb.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides a site for conditional compilation of stub classes supporting
 * the JDBC 2 imterface implementation under the JDBC 1 runtime. <p>
 */
class jdbcStubs {}

//#ifdef JAVA2
//#else
/*
// surrogate for java.util.Map interface
interface Map {

    int size();

    boolean isEmpty();

    boolean containsKey(Object key);

    boolean containsValue(Object value);

    Object get(Object key);

    Object put(Object key, Object value);

    Object remove(Object key);

    void putAll(Map t);

    void clear();

//    public Set keySet();

//    public Collection values();

//    public Set entrySet();

    interface Entry {

        Object getKey();

        Object getValue();

        Object setValue(Object value);

        boolean equals(Object o);

        int hashCode();
    }

    boolean equals(Object o);

    int hashCode();
}

// surrogates for java.SQL type interfaces
interface Array {

    String getBaseTypeName() throws SQLException;

    int getBaseType() throws SQLException;

    Object getArray() throws SQLException;

    Object getArray(Map map) throws SQLException;

    Object getArray(long index, int count) throws SQLException;

    Object getArray(long index, int count, Map map) throws SQLException;

    ResultSet getResultSet() throws SQLException;

    ResultSet getResultSet(Map map) throws SQLException;

    ResultSet getResultSet(long index, int count) throws SQLException;

    ResultSet getResultSet(long index, int count,
                           Map map) throws SQLException;
}

interface Blob {

    long length() throws SQLException;

    byte[] getBytes(long pos, int length) throws SQLException;

    java.io.InputStream getBinaryStream() throws SQLException;

    long position(byte pattern[], long start) throws SQLException;

    long position(Blob pattern, long start) throws SQLException;
}

interface Clob {

    long length() throws SQLException;

    String getSubString(long pos, int length) throws SQLException;

    java.io.Reader getCharacterStream() throws SQLException;

    java.io.InputStream getAsciiStream() throws SQLException;

    long position(String searchstr, long start) throws SQLException;

    long position(Clob searchstr, long start) throws SQLException;
}

interface Ref {
    String getBaseTypeName() throws SQLException;
}
*/

//#endif JAVA2
