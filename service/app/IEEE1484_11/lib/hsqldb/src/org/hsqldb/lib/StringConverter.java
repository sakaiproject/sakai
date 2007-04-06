/* Copyright (c) 1995-2000, The Hypersonic SQL Group.
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
 * Neither the name of the Hypersonic SQL Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HYPERSONIC SQL GROUP,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many individuals 
 * on behalf of the Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2005, The HSQL Development Group
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UTFDataFormatException;

/**
 * Collection of static methods for converting strings between different
 * formats and to and from byte arrays.<p>
 *
 * New class, with extensively enhanced and rewritten Hypersonic code.
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @author fredt@users
 * @version 1.8.0
 * @since 1.7.2
 */

// fredt@users 20020328 - patch 1.7.0 by fredt - error trapping
public class StringConverter {

    private static final byte[] HEXBYTES = {
        (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4',
        (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9',
        (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
    };
    private static final String HEXINDEX = "0123456789abcdef0123456789ABCDEF";

    /**
     * Converts a String into a byte array by using a big-endian two byte
     * representation of each char value in the string.
     */
    byte[] stringToFullByteArray(String s) {

        int    length = s.length();
        byte[] buffer = new byte[length * 2];
        int    c;

        for (int i = 0; i < length; i++) {
            c                 = s.charAt(i);
            buffer[i * 2]     = (byte) ((c & 0x0000ff00) >> 8);
            buffer[i * 2 + 1] = (byte) (c & 0x000000ff);
        }

        return buffer;
    }

    /**
     * Compacts a hexadecimal string into a byte array
     *
     *
     * @param s hexadecimal string
     *
     * @return byte array for the hex string
     * @throws IOException
     */
    public static byte[] hexToByte(String s) throws IOException {

        int    l    = s.length() / 2;
        byte[] data = new byte[l];
        int    j    = 0;

        if (s.length() % 2 != 0) {
            throw new IOException(
                "hexadecimal string with odd number of characters");
        }

        for (int i = 0; i < l; i++) {
            char c = s.charAt(j++);
            int  n, b;

            n = HEXINDEX.indexOf(c);

            if (n == -1) {
                throw new IOException(
                    "hexadecimal string contains non hex character");
            }

            b       = (n & 0xf) << 4;
            c       = s.charAt(j++);
            n       = HEXINDEX.indexOf(c);
            b       += (n & 0xf);
            data[i] = (byte) b;
        }

        return data;
    }

    /**
     * Converts a byte array into a hexadecimal string
     *
     *
     * @param b byte array
     *
     * @return hex string
     */
    public static String byteToHex(byte[] b) {

        int    len = b.length;
        char[] s   = new char[len * 2];

        for (int i = 0, j = 0; i < len; i++) {
            int c = ((int) b[i]) & 0xff;

            s[j++] = (char) HEXBYTES[c >> 4 & 0xf];
            s[j++] = (char) HEXBYTES[c & 0xf];
        }

        return new String(s);
    }

    /**
     * Converts a byte array into hexadecimal characters
     * which are written as ASCII to the given output stream.
     *
     * @param o output stream
     * @param b byte array
     */
    public static void writeHex(byte[] o, int from, byte[] b) {

        int len = b.length;

        for (int i = 0; i < len; i++) {
            int c = ((int) b[i]) & 0xff;

            o[from++] = HEXBYTES[c >> 4 & 0xf];
            o[from++] = HEXBYTES[c & 0xf];
        }
    }

    public static String byteToString(byte[] b, String charset) {

        try {
            return (charset == null) ? new String(b)
                                     : new String(b, charset);
        } catch (Exception e) {}

        return null;
    }

    /**
     * Converts a Unicode string into UTF8 then convert into a hex string
     *
     *
     * @param s normal Unicode string
     *
     * @return hex string representation of UTF8 encoding of the input
     */
    public static String unicodeToHexString(String s) {

        HsqlByteArrayOutputStream bout = new HsqlByteArrayOutputStream();

        writeUTF(s, bout);

        return byteToHex(bout.toByteArray());
    }

// fredt@users 20011120 - patch 450455 by kibu@users - modified
// method return type changed to HsqlStringBuffer with spare
// space for end-of-line characters -- to reduce String concatenation

    /**
     * Hsqldb specific encoding used only for log files.
     *
     * The SQL statements that need to be written to the log file (input) are
     * Java Unicode strings. input is converted into a 7bit escaped ASCII
     * string (output)with the following transformations.
     * All characters outside the 0x20-7f range are converted to a
     * escape sequence and added to output.
     * If a backslash character is immdediately followed by 'u', the
     * backslash character is converted to escape sequence and
     * added to output.
     * All the remaining characters in input are added to output without
     * conversion.
     *
     * The escape sequence is backslash, letter u, xxxx, where xxxx
     * is the hex representation of the character code.
     * (fredt@users)
     *
     * @param b output stream to wite to
     * @param s Java Unicode string
     *
     * @return number of bytes written out
     *
     */
    public static int unicodeToAscii(HsqlByteArrayOutputStream b, String s,
                                     boolean doubleSingleQuotes) {

        int count = 0;

        if ((s == null) || (s.length() == 0)) {
            return 0;
        }

        int len = s.length();

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);

            if (c == '\\') {
                if ((i < len - 1) && (s.charAt(i + 1) == 'u')) {
                    b.write(c);    // encode the \ as unicode, so 'u' is ignored
                    b.write('u');
                    b.write('0');
                    b.write('0');
                    b.write('5');
                    b.write('c');

                    count += 6;
                } else {
                    b.write(c);

                    count++;
                }
            } else if ((c >= 0x0020) && (c <= 0x007f)) {
                b.write(c);        // this is 99%

                count++;

                if (c == '\'' && doubleSingleQuotes) {
                    b.write(c);

                    count++;
                }
            } else {
                b.write('\\');
                b.write('u');
                b.write(HEXBYTES[(c >> 12) & 0xf]);
                b.write(HEXBYTES[(c >> 8) & 0xf]);
                b.write(HEXBYTES[(c >> 4) & 0xf]);
                b.write(HEXBYTES[c & 0xf]);

                count += 6;
            }
        }

        return count;
    }

// fredt@users 20020522 - fix for 557510 - backslash bug
// this legacy bug resulted from forward reading the input when a backslash
// was present and manifested itself when a backslash was followed
// immdediately by a character outside the 0x20-7f range in a database field.

    /**
     * Hsqldb specific decoding used only for log files.
     *
     * This method converts the 7 bit escaped ASCII strings in a log file
     * back into Java Unicode strings. See unicodeToAccii() above,
     *
     * @param s encoded ASCII string in byte array
     * @param offset position of first byte
     * @param length number of bytes to use
     *
     * @return Java Unicode string
     */
    public static String asciiToUnicode(byte[] s, int offset, int length) {

        if (length == 0) {
            return "";
        }

        char[] b = new char[length];
        int    j = 0;

        for (int i = 0; i < length; i++) {
            byte c = s[offset + i];

            if (c == '\\' && i < length - 5) {
                byte c1 = s[offset + i + 1];

                if (c1 == 'u') {
                    i++;

                    // 4 characters read should always return 0-15
                    int k = HEXINDEX.indexOf(s[offset + (++i)]) << 12;

                    k      += HEXINDEX.indexOf(s[offset + (++i)]) << 8;
                    k      += HEXINDEX.indexOf(s[offset + (++i)]) << 4;
                    k      += HEXINDEX.indexOf(s[offset + (++i)]);
                    b[j++] = (char) k;
                } else {
                    b[j++] = (char) c;
                }
            } else {
                b[j++] = (char) c;
            }
        }

        return new String(b, 0, j);
    }

    public static String asciiToUnicode(String s) {

        if ((s == null) || (s.indexOf("\\u") == -1)) {
            return s;
        }

        int    len = s.length();
        char[] b   = new char[len];
        int    j   = 0;

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);

            if (c == '\\' && i < len - 5) {
                char c1 = s.charAt(i + 1);

                if (c1 == 'u') {
                    i++;

                    // 4 characters read should always return 0-15
                    int k = HEXINDEX.indexOf(s.charAt(++i)) << 12;

                    k      += HEXINDEX.indexOf(s.charAt(++i)) << 8;
                    k      += HEXINDEX.indexOf(s.charAt(++i)) << 4;
                    k      += HEXINDEX.indexOf(s.charAt(++i));
                    b[j++] = (char) k;
                } else {
                    b[j++] = c;
                }
            } else {
                b[j++] = c;
            }
        }

        return new String(b, 0, j);
    }

    public static String readUTF(byte[] bytearr, int offset,
                                 int length) throws IOException {

        char[] buf = new char[length];

        return readUTF(bytearr, offset, length, buf);
    }

    public static String readUTF(byte[] bytearr, int offset, int length,
                                 char[] buf) throws IOException {

        int bcount = 0;
        int c, char2, char3;
        int count = 0;

        while (count < length) {
            c = (int) bytearr[offset + count];

            if (bcount == buf.length) {
                buf = (char[]) ArrayUtil.resizeArray(buf, length);
            }

            if (c > 0) {

                /* 0xxxxxxx*/
                count++;

                buf[bcount++] = (char) c;

                continue;
            }

            c &= 0xff;

            switch (c >> 4) {

                case 12 :
                case 13 :

                    /* 110x xxxx   10xx xxxx*/
                    count += 2;

                    if (count > length) {
                        throw new UTFDataFormatException();
                    }

                    char2 = (int) bytearr[offset + count - 1];

                    if ((char2 & 0xC0) != 0x80) {
                        throw new UTFDataFormatException();
                    }

                    buf[bcount++] = (char) (((c & 0x1F) << 6)
                                            | (char2 & 0x3F));
                    break;

                case 14 :

                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;

                    if (count > length) {
                        throw new UTFDataFormatException();
                    }

                    char2 = (int) bytearr[offset + count - 2];
                    char3 = (int) bytearr[offset + count - 1];

                    if (((char2 & 0xC0) != 0x80)
                            || ((char3 & 0xC0) != 0x80)) {
                        throw new UTFDataFormatException();
                    }

                    buf[bcount++] = (char) (((c & 0x0F) << 12)
                                            | ((char2 & 0x3F) << 6)
                                            | ((char3 & 0x3F) << 0));
                    break;

                default :

                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException();
            }
        }

        // The number of chars produced may be less than length
        return new String(buf, 0, bcount);
    }

    /**
     * Writes a string to the specified DataOutput using UTF-8 encoding in a
     * machine-independent manner.
     * <p>
     * @param      str   a string to be written.
     * @param      out   destination to write to
     * @return     The number of bytes written out.
     */
    public static int writeUTF(String str, HsqlByteArrayOutputStream out) {

        int strlen = str.length();
        int c,
            count  = 0;

        for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);

            if (c >= 0x0001 && c <= 0x007F) {
                out.write(c);

                count++;
            } else if (c > 0x07FF) {
                out.write(0xE0 | ((c >> 12) & 0x0F));
                out.write(0x80 | ((c >> 6) & 0x3F));
                out.write(0x80 | ((c >> 0) & 0x3F));

                count += 3;
            } else {
                out.write(0xC0 | ((c >> 6) & 0x1F));
                out.write(0x80 | ((c >> 0) & 0x3F));

                count += 2;
            }
        }

        return count;
    }

    public static int getUTFSize(String s) {

        int len = (s == null) ? 0
                              : s.length();
        int l   = 0;

        for (int i = 0; i < len; i++) {
            int c = s.charAt(i);

            if ((c >= 0x0001) && (c <= 0x007F)) {
                l++;
            } else if (c > 0x07FF) {
                l += 3;
            } else {
                l += 2;
            }
        }

        return l;
    }

    /**
     * Using a Reader and a Writer, returns a String from an InputStream.
     */
    public static String inputStreamToString(InputStream x,
            int length) throws IOException {

        InputStreamReader in        = new InputStreamReader(x);
        StringWriter      writer    = new StringWriter();
        int               blocksize = 8 * 1024;
        char[]            buffer    = new char[blocksize];

        for (int left = length; left > 0; ) {
            int read = in.read(buffer, 0, left > blocksize ? blocksize
                                                           : left);

            if (read == -1) {
                break;
            }

            writer.write(buffer, 0, read);

            left -= read;
        }

        writer.close();

        return writer.toString();
    }

// fredt@users 20020130 - patch 497872 by Nitin Chauhan - use byte[] of exact size

    /**
     * Returns the quoted version of the string using the quotechar argument.
     * doublequote argument indicates whether each instance of quotechar
     * inside the string is doubled.<p>
     *
     * null string argument returns null. If the caller needs the literal
     * "NULL" it should created it itself <p>
     *
     * The reverse conversion is handled in Tokenizer.java
     */
    public static String toQuotedString(String s, char quoteChar,
                                        boolean extraQuote) {

        if (s == null) {
            return null;
        }

        int    count = extraQuote ? count(s, quoteChar)
                                  : 0;
        int    len   = s.length();
        char[] b     = new char[2 + count + len];
        int    i     = 0;
        int    j     = 0;

        b[j++] = quoteChar;

        for (; i < len; i++) {
            char c = s.charAt(i);

            b[j++] = c;

            if (extraQuote && c == quoteChar) {
                b[j++] = c;
            }
        }

        b[j] = quoteChar;

        return new String(b);
    }

    /**
     * Counts Character c in String s
     *
     * @param String s
     *
     * @return int count
     */
    static int count(final String s, final char c) {

        int pos   = 0;
        int count = 0;

        if (s != null) {
            while ((pos = s.indexOf(c, pos)) > -1) {
                count++;
                pos++;
            }
        }

        return count;
    }
}
