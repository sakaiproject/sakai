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

import java.io.StringReader;
import java.sql.Clob;
import java.sql.SQLException;

import org.hsqldb.Trace;
import org.hsqldb.lib.AsciiStringInputStream;

// boucherb@users 2004-03/04-xx - doc 1.7.2 - javadocs updated; methods put in
//                                            correct (historical, interface
//                                            declared) order
// boucherb@users 2004-03/04-xx - patch 1.7.2 - null check for constructor (a
//                                              null CLOB value is Java null,
//                                              not a Clob object with null
//                                              data);moderate thread safety;
//                                              simplification; optimization
//                                              of operations between jdbcClob
//                                              instances

/**
 * The mapping in the Java<sup><font size=-2>TM</font></sup> programming
 * language for the SQL CLOB type. <p>
 *
 * Provides methods for getting the length of an SQL CLOB (Character Large
 * Object) value, for materializing a CLOB value on the client, and for
 * searching for a substring or CLOB object within a CLOB value. <p>
 *
 * <!-- start Release-specific documentation -->
 * <div class="ReleaseSpecificDocumentation">
 * <h3>HSQLDB-Specific Information:</h3> <p>
 *
 * Including 1.8.x, the HSQLDB driver does not implement Clob using an SQL
 * locator(CLOB).  That is, an HSQLDB Clob object does not contain a logical
 * pointer to SQL CLOB data; rather it directly contains an immutable
 * representation of the data (a String object). As a result, an HSQLDB
 * Clob object itself is valid beyond the duration of the transaction in which
 * is was created, although it does not necessarily represent a corresponding
 * value on the database. <p>
 *
 * Currently, the interface methods for updating a CLOB value are
 * unsupported. However, the truncate method is supported for local use.
 * </div>
 * <!-- end release-specific documentation -->
 *
 * @author  boucherb@users
 * @version 1.7.2
 * @since JDK 1.2, HSQLDB 1.7.2
 */
public final class jdbcClob implements Clob {

    volatile String data;

    /**
     * Constructs a new jdbcClob object wrapping the given character
     * sequence. <p>
     *
     * This constructor is used internally to retrieve result set values as
     * Clob objects, yet it must be public to allow access from other packages.
     * As such (in the interest of efficiency) this object maintains a reference
     * to the given String object rather than making a copy and so it is
     * gently suggested (in the interest of effective memory management) that
     * extenal clients using this constructor either take pause to consider
     * the implications or at least take care to provide a String object whose
     * internal character buffer is not much larger than required to represent
     * the value.
     *
     * @param data the character sequence representing the Clob value
     * @throws SQLException if the argument is null
     */
    public jdbcClob(final String data) throws SQLException {

        if (data == null) {
            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT, "null");
        }

        this.data = data;
    }

    /**
     * Retrieves the number of characters in the <code>CLOB</code> value
     * designated by this <code>Clob</code> object.
     *
     * @return length of the <code>CLOB</code> in characters
     * @exception SQLException if there is an error accessing the
     *            length of the <code>CLOB</code> value
     *
     * @since JDK 1.2, HSQLDB 1.7.2
     */
    public long length() throws SQLException {

        final String ldata = data;

        return ldata.length();
    }

    /**
     * Retrieves a copy of the specified substring in the <code>CLOB</code>
     * value designated by this <code>Clob</code> object. The substring begins
     * at position <code>pos</code> and has up to <code>length</code>
     * consecutive characters. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * The official specification above is ambiguous in that it does not
     * precisely indicate the policy to be observed when
     * pos > this.length() - length.  One policy would be to retrieve the
     * characters from pos to this.length().  Another would be to throw
     * an exception.  HSQLDB observes the later policy.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param pos the first character of the substring to be extracted.
     *            The first character is at position 1.
     * @param length the number of consecutive characters to be copied
     * @return a <code>String</code> that is the specified substring in
     *         the <code>CLOB</code> value designated by this
     *         <code>Clob</code> object
     * @exception SQLException if there is an error accessing the
     *            <code>CLOB</code> value
     *
     * @since JDK 1.2, HSQLDB 1.7.2
     */
    public String getSubString(long pos,
                               final int length) throws SQLException {

        final String ldata = data;
        final int    dlen  = ldata.length();

        pos--;

        if (pos < 0 || pos > dlen) {
            Util.sqlException(Trace.INVALID_JDBC_ARGUMENT,
                              "pos: " + (pos + 1L));
        }

        if (length < 0 || length > dlen - pos) {
            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT,
                                    "length: " + length);
        }

        if (pos == 0 && length == dlen) {
            return ldata;
        }

        return ldata.substring((int) pos, (int) pos + length);
    }

    /**
     * Retrieves the <code>CLOB</code> value designated by this
     * <code>Clob</code> object as a <code>java.io.Reader</code> object
     * (or as a stream of characters).
     *
     * @return a <code>java.io.Reader</code> object containing the
     *         <code>CLOB</code> data
     * @exception SQLException if there is an error accessing the
     *            <code>CLOB</code> value
     * @see #setCharacterStream
     *
     * @since JDK 1.2, HSQLDB 1.7.2
     */
    public java.io.Reader getCharacterStream() throws SQLException {

        final String ldata = data;

        return new StringReader(ldata);
    }

    /**
     * Retrieves the <code>CLOB</code> value designated by this
     * <code>Clob</code> object as an ascii stream.
     *
     * @return a <code>java.io.InputStream</code> object containing the
     *         <code>CLOB</code> data
     * @exception SQLException if there is an error accessing the
     *            <code>CLOB</code> value
     * @see #setAsciiStream
     *
     * @since JDK 1.2, HSQLDB 1.7.2
     */
    public java.io.InputStream getAsciiStream() throws SQLException {

        final String ldata = data;

        return new AsciiStringInputStream(ldata);
    }

    /**
     * Retrieves the character position at which the specified substring
     * <code>searchstr</code> appears in the SQL <code>CLOB</code> value
     * represented by this <code>Clob</code> object.  The search
     * begins at position <code>start</code>.
     *
     * @param searchstr the substring for which to search
     * @param start the position at which to begin searching; the
     *          first position is 1
     * @return the position at which the substring appears or -1 if it is not
     *          present; the first position is 1
     * @exception SQLException if there is an error accessing the
     *          <code>CLOB</code> value
     *
     * @since JDK 1.2, HSQLDB 1.7.2
     */
    public long position(final String searchstr,
                         long start) throws SQLException {

        if (searchstr == null || start > Integer.MAX_VALUE) {
            return -1;
        }

        final String ldata = data;
        final int    pos   = ldata.indexOf(searchstr, (int) --start);

        return (pos < 0) ? -1
                         : pos + 1;
    }

    /**
     * Retrieves the character position at which the specified
     * <code>Clob</code> object <code>searchstr</code> appears in this
     * <code>Clob</code> object.  The search begins at position
     * <code>start</code>.
     *
     * @param searchstr the <code>Clob</code> object for which to search
     * @param start the position at which to begin searching; the first
     *              position is 1
     * @return the position at which the <code>Clob</code> object appears
     *              or -1 if it is not present; the first position is 1
     * @exception SQLException if there is an error accessing the
     *            <code>CLOB</code> value
     *
     * @since JDK 1.2, HSQLDB 1.7.2
     */
    public long position(final Clob searchstr,
                         long start) throws SQLException {

        if (searchstr == null) {
            return -1;
        }

        final String ldata = data;
        final long   dlen  = ldata.length();
        final long   sslen = searchstr.length();

        start--;    //***** FOIRGOT THIS *******

// This is potentially much less expensive than materializing a large
// substring from some other vendor's CLOB.  Indeed, we should probably
// do the comparison piecewise, using an in-memory buffer (or temp-files
// when available), if it is detected that the input CLOB is very long.
        if (start > dlen - sslen) {
            return -1;
        }

        // by now, we know sslen and start are both < Integer.MAX_VALUE
        String s;

        if (searchstr instanceof jdbcClob) {
            s = ((jdbcClob) searchstr).data;
        } else {
            s = searchstr.getSubString(1L, (int) sslen);
        }

        final int pos = ldata.indexOf(s, (int) start);

        return (pos < 0) ? -1
                         : pos + 1;
    }

    //---------------------------- jdbc 3.0 -----------------------------------

    /**
     * Writes the given Java <code>String</code> to the <code>CLOB</code>
     * value that this <code>Clob</code> object designates at the position
     * <code>pos</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSLQDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param pos the position at which to start writing to the
     *          <code>CLOB</code> value that this <code>Clob</code> object
     *          represents
     * @param str the string to be written to the <code>CLOB</code>
     *          value that this <code>Clob</code> designates
     * @return the number of characters written
     * @exception SQLException if there is an error accessing the
     *            <code>CLOB</code> value
     *
     * @since JDK 1.4, HSQLDB 1.7.2
     */
    public int setString(long pos, String str) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * Writes <code>len</code> characters of <code>str</code>, starting
     * at character <code>offset</code>, to the <code>CLOB</code> value
     * that this <code>Clob</code> represents. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSLQDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param pos the position at which to start writing to this
     *          <code>CLOB</code> object
     * @param str the string to be written to the <code>CLOB</code>
     *          value that this <code>Clob</code> object represents
     * @param offset the offset into <code>str</code> to start reading
     *          the characters to be written
     * @param len the number of characters to be written
     * @return the number of characters written
     * @exception SQLException if there is an error accessing the
     *          <code>CLOB</code> value
     *
     * @since JDK 1.4, HSQLDB 1.7.2
     */
    public int setString(long pos, String str, int offset,
                         int len) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * Retrieves a stream to be used to write Ascii characters to the
     * <code>CLOB</code> value that this <code>Clob</code> object represents,
     * starting at position <code>pos</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSLQDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param pos the position at which to start writing to this
     *        <code>CLOB</code> object
     * @return the stream to which ASCII encoded characters can be written
     * @exception SQLException if there is an error accessing the
     *            <code>CLOB</code> value
     * @see #getAsciiStream
     *
     * @since JDK 1.4, HSQLDB 1.7.2
     */
    public java.io.OutputStream setAsciiStream(long pos) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * Retrieves a stream to be used to write a stream of Unicode characters
     * to the <code>CLOB</code> value that this <code>Clob</code> object
     * represents, at position <code>pos</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSLQDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param  pos the position at which to start writing to the
     *        <code>CLOB</code> value
     *
     * @return a stream to which Unicode encoded characters can be written
     * @exception SQLException if there is an error accessing the
     *            <code>CLOB</code> value
     * @see #getCharacterStream
     *
     * @since JDK 1.4, HSQLDB 1.7.2
     */
    public java.io.Writer setCharacterStream(long pos) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * Truncates the <code>CLOB</code> value that this <code>Clob</code>
     * designates to have a length of <code>len</code>
     * characters. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * This operation affects only the client-side value; it has no effect upon
     * the value as it is stored in the database.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param len the length, in bytes, to which the <code>CLOB</code> value
     *        should be truncated
     * @exception SQLException if there is an error accessing the
     *            <code>CLOB</code> value
     *
     * @since JDK 1.4, HSQLDB 1.7.2
     */
    public void truncate(final long len) throws SQLException {

        final String ldata = data;
        final long   dlen  = ldata.length();
        final long   chars = len >> 1;

        if (chars == dlen) {

            // nothing has changed, so there's nothing to be done
        } else if (len < 0 || chars > dlen) {
            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT,
                                    Long.toString(len));
        } else {

            // use new String() to ensure we get rid of slack
            data = new String(ldata.substring(0, (int) chars));
        }
    }
}
