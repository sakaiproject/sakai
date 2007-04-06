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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

import org.hsqldb.Trace;

// boucherb@users 2004-04-xx - patch 1.7.2 - position and truncate methods
//                             implemented; minor changes for moderate thread
//                             safety and optimal performance
// boucherb@users 2004-04-xx - doc 1.7.2 - javadocs updated; methods put in
//                             correct (historical, interface declared) order

/**
 * The representation (mapping) in the Java<sup><font size=-2>TM</font></sup>
 * programming language of an SQL BLOB value. <p>
 *
 * Provides methods for getting the length of an SQL BLOB (Binary Large Object)
 * value, for materializing a BLOB value on the client, and for determining the
 * position of an octet sequence (byte pattern) within a BLOB value. <p>
 *
 * <!-- start Release-specific documentation -->
 * <div class="ReleaseSpecificDocumentation">
 * <h3>HSQLDB-Specific Information:</h3> <p>
 *
 * Including 1.8.x, the HSQLDB driver does not implement Blob using an SQL
 * locator(BLOB).  That is, an HSQLDB Blob object does not contain a logical
 * pointer to SQL BLOB data; rather it directly contains a representation of
 * the data (a byte array). As a result, an HSQLDB Blob object is itself
 * valid beyond the duration of the transaction in which is was created,
 * although it does not necessarily represent a corresponding value
 * on the database. <p>
 *
 * Currently, the interface methods for updating a BLOB value are
 * unsupported. However, the truncate method is supported for local use.
 * </div>
 * <!-- start Release-specific documentation -->
 *
 * @author james house jhouse@part.net
 * @author boucherb@users
 * @version 1.7.2
 * @since JDK 1.2, HSQLDB 1.7.2
 */
public class jdbcBlob implements Blob {

    volatile byte[] data;

    /**
     * Constructs a new jdbcBlob instance wrapping the given octet sequence. <p>
     *
     * This constructor is used internally to retrieve result set values as
     * Blob objects, yet it must be public to allow access from other packages.
     * As such (in the interest of efficiency) this object maintains a reference
     * to the given octet sequence rather than making a copy; special care
     * should be taken by extenal clients never to use this constructor with a
     * byte array object that may later be modified extenally.
     *
     * @param data the octet sequence representing the Blob value
     * @throws SQLException if the argument is null
     */
    public jdbcBlob(final byte[] data) throws SQLException {

        if (data == null) {
            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT, "null");
        }

        this.data = data;    // (byte[]) data.clone();
    }

    /**
     * Returns the number of bytes in the <code>BLOB</code> value
     * designated by this <code>Blob</code> object.
     *
     * @return length of the <code>BLOB</code> in bytes
     * @exception SQLException if there is an error accessing the
     *      length of the <code>BLOB</code>
     *
     * @since JDK 1.2, HSQLDB 1.7.2
     */
    public long length() throws SQLException {

        final byte[] ldata = data;

        return ldata.length;
    }

    /**
     * Retrieves all or part of the <code>BLOB</code> value that this
     * <code>Blob</code> object represents, as an array of bytes.  This
     * <code>byte</code> array contains up to <code>length</code>
     * consecutive bytes starting at position <code>pos</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * The official specification above is ambiguous in that it does not
     * precisely indicate the policy to be observed when
     * pos > this.length() - length.  One policy would be to retrieve the
     * octets from pos to this.length().  Another would be to throw an
     * exception.  HSQLDB observes the later policy.
     * </div>
     *
     * @param pos the ordinal position of the first byte in the
     *        <code>BLOB</code> value to be extracted; the first byte is at
     *        position 1
     * @param length the number of consecutive bytes to be copied
     * @return a byte array containing up to <code>length</code>
     *         consecutive bytes from the <code>BLOB</code> value designated
     *         by this <code>Blob</code> object, starting with the
     *         byte at position <code>pos</code>
     * @exception SQLException if there is an error accessing the
     *            <code>BLOB</code> value
     * @see #setBytes
     *
     * @since JDK 1.2, HSQLDB 1.7.2
     */
    public byte[] getBytes(long pos, final int length) throws SQLException {

        final byte[] ldata = data;
        final int    dlen  = ldata.length;

        pos--;

        if (pos < 0 || pos > dlen) {
            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT,
                                    "pos: " + (pos + 1));
        }

        if (length < 0 || length > dlen - pos) {
            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT,
                                    "length: " + length);
        }

        final byte[] out = new byte[length];

        System.arraycopy(ldata, (int) pos, out, 0, length);

        return out;
    }

    /**
     * Retrieves the <code>BLOB</code> value designated by this
     * <code>Blob</code> instance as a stream.
     *
     * @return a stream containing the <code>BLOB</code> data
     * @exception SQLException if there is an error accessing the
     *            <code>BLOB</code> value
     * @see #setBinaryStream
     *
     * @since JDK 1.2, HSQLDB 1.7.2
     */
    public InputStream getBinaryStream() throws SQLException {

        final byte[] ldata = data;

        return new ByteArrayInputStream(ldata);
    }

    /**
     * Retrieves the byte position at which the specified byte array
     * <code>pattern</code> begins within the <code>BLOB</code>
     * value that this <code>Blob</code> object represents.  The
     * search for <code>pattern</code> begins at position
     * <code>start</code>. <p>
     *
     * @param pattern the byte array for which to search
     * @param start the position at which to begin searching; the
     *        first position is 1
     * @return the position at which the pattern appears, else -1
     * @exception SQLException if there is an error accessing the
     *        <code>BLOB</code>
     *
     * @since JDK 1.2, HSQLDB 1.7.2
     */
    public long position(final byte[] pattern,
                         long start) throws SQLException {

        final byte[] ldata = data;
        final int    dlen  = ldata.length;

        if (start > dlen || pattern == null) {
            return -1;
        } else if (start < 1) {
            start = 0;
        } else {
            start--;
        }

        final int plen = pattern.length;

        if (plen == 0 || start > dlen - plen) {
            return -1;
        }

        final int  stop = dlen - plen;
        final byte b0   = pattern[0];

        outer_loop:
        for (int i = (int) start; i <= stop; i++) {
            if (ldata[i] != b0) {
                continue;
            }

            int     len     = plen;
            int     doffset = i;
            int     poffset = 0;
            boolean match   = true;

            while (len-- > 0) {
                if (ldata[doffset++] != pattern[poffset++]) {
                    continue outer_loop;
                }
            }

            return i + 1;
        }

        return -1;
    }

    /**
     * Retrieves the byte position in the <code>BLOB</code> value
     * designated by this <code>Blob</code> object at which
     * <code>pattern</code> begins.  The search begins at position
     * <code>start</code>.
     *
     * @param pattern the <code>Blob</code> object designating
     *      the <code>BLOB</code> value for which to search
     * @param start the position in the <code>BLOB</code> value
     *        at which to begin searching; the first position is 1
     * @return the position at which the pattern begins, else -1
     * @exception SQLException if there is an error accessing the
     *        <code>BLOB</code> value
     *
     * @since JDK 1.2, HSQLDB 1.7.2
     */
    public long position(final Blob pattern, long start) throws SQLException {

        final byte[] ldata = data;
        final int    dlen  = ldata.length;

        if (start > dlen || pattern == null) {
            return -1;
        } else if (start < 1) {
            start = 0;
        } else {
            start--;
        }

        final long plen = pattern.length();

        if (plen == 0 || start > ((long) dlen) - plen) {
            return -1;
        }

        // by now, we know plen <= Integer.MAX_VALUE
        final int iplen = (int) plen;
        byte[]    bap;

        if (pattern instanceof jdbcBlob) {
            bap = ((jdbcBlob) pattern).data;
        } else {
            bap = pattern.getBytes(1, iplen);
        }

        final int  stop = dlen - iplen;
        final byte b0   = bap[0];

        outer_loop:
        for (int i = (int) start; i <= stop; i++) {
            if (ldata[i] != b0) {
                continue;
            }

            int len     = iplen;
            int doffset = i;
            int poffset = 0;

            while (len-- > 0) {
                if (ldata[doffset++] != bap[poffset++]) {
                    continue outer_loop;
                }
            }

            return i + 1;
        }

        return -1;
    }

    // -------------------------- JDBC 3.0 -----------------------------------

    /**
     * Writes the given array of bytes to the <code>BLOB</code> value that
     * this <code>Blob</code> object represents, starting at position
     * <code>pos</code>, and returns the number of bytes written. <p>
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
     * @param pos the position in the <code>BLOB</code> object at which
     *        to start writing
     * @param bytes the array of bytes to be written to the <code>BLOB</code>
     *        value that this <code>Blob</code> object represents
     * @return the number of bytes written
     * @exception SQLException if there is an error accessing the
     *            <code>BLOB</code> value
     * @see #getBytes
     *
     * @since JDK 1.4, HSQLDB 1.7.2
     */
    public int setBytes(long pos, byte[] bytes) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * Writes all or part of the given <code>byte</code> array to the
     * <code>BLOB</code> value that this <code>Blob</code> object represents
     * and returns the number of bytes written.
     * Writing starts at position <code>pos</code> in the <code>BLOB</code>
     * value; <code>len</code> bytes from the given byte array are written. <p>
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
     * @param pos the position in the <code>BLOB</code> object at which
     *        to start writing
     * @param bytes the array of bytes to be written to this <code>BLOB</code>
     *        object
     * @param offset the offset into the array <code>bytes</code> at which
     *        to start reading the bytes to be set
     * @param len the number of bytes to be written to the <code>BLOB</code>
     *        value from the array of bytes <code>bytes</code>
     * @return the number of bytes written
     * @exception SQLException if there is an error accessing the
     *            <code>BLOB</code> value
     * @see #getBytes
     *
     * @since JDK 1.4, HSQLDB 1.7.2
     */
    public int setBytes(long pos, byte[] bytes, int offset,
                        int len) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * Retrieves a stream that can be used to write to the <code>BLOB</code>
     * value that this <code>Blob</code> object represents.  The stream begins
     * at position <code>pos</code>. <p>
     *
     * <!-- start release-specific documentation -->
     * <div class="ReleaseSpecificDocumentation">
     * <h3>HSQLDB-Specific Information:</h3> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws an <code>SQLException</code>.
     * </div>
     * <!-- end release-specific documentation -->
     *
     * @param pos the position in the <code>BLOB</code> value at which
     *        to start writing
     * @return a <code>java.io.OutputStream</code> object to which data can
     *         be written
     * @exception SQLException if there is an error accessing the
     *            <code>BLOB</code> value
     * @see #getBinaryStream
     *
     * @since JDK 1.4, HSQLDB 1.7.2
     */
    public OutputStream setBinaryStream(long pos) throws SQLException {
        throw Util.notSupported();
    }

    /**
     * Truncates the <code>BLOB</code> value that this <code>Blob</code>
     * object represents to be <code>len</code> bytes in length.
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
     * @param len the length, in bytes, to which the <code>BLOB</code> value
     *        that this <code>Blob</code> object represents should be truncated
     * @exception SQLException if there is an error accessing the
     *            <code>BLOB</code> value
     *
     * @since JDK 1.4, HSQLDB 1.7.2
     */
    public void truncate(final long len) throws SQLException {

        final byte[] ldata = data;

        if (len < 0 || len > ldata.length) {
            throw Util.sqlException(Trace.INVALID_JDBC_ARGUMENT,
                                    Long.toString(len));
        }

        if (len == ldata.length) {
            return;
        }

        byte[] newData = new byte[(int) len];

        System.arraycopy(ldata, 0, newData, 0, (int) len);

        data = newData;
    }

//    public static void main(String[] args) throws Exception {
//
//        System.out.println("--------------------------------");
//        System.out.println((new jdbcBlob(new byte[0])).position(new byte[]{1}, 1));
//        System.out.println((new jdbcBlob(new byte[]{1})).position(new byte[0], 1));
//        System.out.println((new jdbcBlob(new byte[]{1})).position((byte[])null, 1));
//
//        System.out.println("--------------------------------");
//        byte[] data1 = new byte[]{0,1,2,1,2,3,2,3,4,2,3,4,5,2,3,4,5,0,1,2,
//                                  1,2,3,2,3,4,2,3,4,5,2,3,4};
//        byte[] pattern = new byte[]{2,3,4,5};
//
//        jdbcBlob blob1 = new jdbcBlob(data1);
//        jdbcBlob blob2 = new jdbcBlob(pattern);
//
//        for (int i = -1; i <= data1.length + 1; i++) {
//            System.out.println(blob1.position(pattern, i));
//        }
//
//        System.out.println("--------------------------------");
//
//        for (int i = -1; i <= data1.length + 1; i++) {
//            System.out.println(blob1.position(blob2, i));
//        }
//
//        System.out.println("--------------------------------");
//
//        new jdbcBlob(null);
//    }
}
