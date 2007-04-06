/* Copyright (c) 2001-2004, The HSQL Development Group
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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// boucherb@users 20030705 - patch 1.7.2 - renamed methods to allow sp/sf calls
// under HSQLDB's current inability to resolve method name overloads.

/**
 * Provides a static utility interface to an MD5 digest algorithm
 * obtained through the java.security.MessageDigest spi. <p>
 *
 * Database end-users may wish to access the services of this class
 * to provide, for instance, application user lookup tables with
 * one-way password encryption.  For example: <p>
 *
 * <pre>
 * -- DDL
 * CREATE TABLE USERS(UID INTEGER IDENTITY, UNAME VARCHAR, UPASS VARCHAR, UNIQUE(UNAME))
 * CREATE ALIAS MD5 FOR "org.hsqldb.lib.MD5.encodeString"
 *
 * -- DML & DQL
 * INSERT INTO USERS(UNAME, UPASS) VALUES('joe', MD5('passwd'))
 * UPDATE USERS SET UPASS = MD5('newpasswd') WHERE UNAME = 'joe' AND UPASS = MD5('oldpasswd')
 * SELECT UID FROM USERS WHERE UNAME = 'joe' AND UPASS = MD5('logonpasswd')
 * </pre>
 *
 * <b>NOTE:</b> <p>
 *
 * Although it is possible that a particular JVM / application installation may
 * encounter NoSuchAlgorithmException when attempting to get a jce MD5 message
 * digest generator, the likelyhood is very small for almost all JDK/JRE 1.1
 * and later  JVM implementations, as the Sun java.security package has come,
 * by default, with a jce MD5 message digest generator since JDK 1.1 was
 * released.  The HSLQLDB project could have provided an MD5 implementation to
 * guarantee presence, but this class is much more lightweight and still allows
 * clients to install / use  custom implementations through the
 * java.security.MessageDigest spi, for instance if there is no service
 * provided by default under the target JVM of choice or if a client has
 * developed / provides, say, a faster MD5 message digest implementation.
 * In short, this class is a convenience that allows HSQLDB SQL Function and
 * Stored Procedure style access to any underlying MD5 message digest algorithm
 * obtained via the java.security.MessageDigest spi
 *
 * @author boucherb@users.sourceforge.net
 * @version 1.7.2
 * @since 1.7.2
 */
public final class MD5 {

    /**
     * The jce MD5 message digest generator.
     */
    private static MessageDigest md5;

    /**
     * Retrieves a hexidecimal character sequence representing the MD5
     * digest of the specified character sequence, using the specified
     * encoding to first convert the character sequence into a byte sequence.
     * If the specified encoding is null, then ISO-8859-1 is assumed
     *
     * @param string the string to encode.
     * @param encoding the encoding used to convert the string into the
     *      byte sequence to submit for MD5 digest
     * @return a hexidecimal character sequence representing the MD5
     *      digest of the specified string
     * @throws HsqlUnsupportedOperationException if an MD5 digest
     *      algorithm is not available through the
     *      java.security.MessageDigest spi or the requested
     *      encoding is not available
     */
    public static final String encodeString(String string,
            String encoding) throws RuntimeException {
        return StringConverter.byteToHex(digestString(string, encoding));
    }

    /**
     * Retrieves a byte sequence representing the MD5 digest of the
     * specified character sequence, using the specified encoding to
     * first convert the character sequence into a byte sequence.
     * If the specified encoding is null, then ISO-8859-1 is
     * assumed.
     *
     * @param string the string to digest.
     * @param encoding the character encoding.
     * @return the digest as an array of 16 bytes.
     * @throws HsqlUnsupportedOperationException if an MD5 digest
     *      algorithm is not available through the
     *      java.security.MessageDigest spi or the requested
     *      encoding is not available
     */
    public static byte[] digestString(String string,
                                      String encoding)
                                      throws RuntimeException {

        byte[] data;

        if (encoding == null) {
            encoding = "ISO-8859-1";
        }

        try {
            data = string.getBytes(encoding);
        } catch (UnsupportedEncodingException x) {
            throw new RuntimeException(x.toString());
        }

        return digestBytes(data);
    }

    /**
     * Retrieves a byte sequence representing the MD5 digest of the
     * specified byte sequence.
     *
     * @param data the data to digest.
     * @return the MD5 digest as an array of 16 bytes.
     * @throws HsqlUnsupportedOperationException if an MD5 digest
     *       algorithm is not available through the
     *       java.security.MessageDigest spi
     */
    public static final byte[] digestBytes(byte[] data)
    throws RuntimeException {

        synchronized (MD5.class) {
            if (md5 == null) {
                try {
                    md5 = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e.toString());
                }
            }

            return md5.digest(data);
        }
    }
}
