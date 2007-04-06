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


package org.hsqldb.types;

import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.lib.StringConverter;

/**
 * Java representation of a BINARY field value. <p>
 *
 * A Binary object instance always wraps a non-null byte[] object; all
 * NULL SQL field values are represented internally by HSQLDB as Java null.
 *
 * @author fredt@users
 * @version 1.7.2
 * @since 1.7.2
 */
public class Binary {

    private byte[] data;
    int            hash;

    /**
     * This constructor is used inside the engine when an already serialized
     * byte[] is read from a file (.log, .script, .data or text table source).
     * In this case clone is false.
     *
     * When a byte[] is submitted as a parameter of PreparedStatement then
     * clone is true.
     */
    public Binary(byte[] data, boolean clone) {

        if (clone) {
            data = (byte[]) ArrayUtil.duplicateArray(data);
        }

        this.data = data;
    }

    public byte[] getBytes() {
        return data;
    }

    public byte[] getClonedBytes() {
        return (byte[]) data.clone();
    }

    public int getBytesLength() {
        return data.length;
    }

    public boolean equals(Object other) {

        if (other == this) {
            return true;
        }

        if (!(other instanceof Binary)) {
            return false;
        }

        if (data.length != ((Binary) other).data.length) {
            return false;
        }

        return ArrayUtil.containsAt(data, 0, ((Binary) other).data);
    }

    public int hashCode() {

        int h = 0;

        if (hash == 0) {
            for (int i = 0; i < data.length; i++) {
                h = 31 * h + data[i];
            }

            hash = h;
        }

        return hash;
    }

    public String toString() {
        return StringConverter.byteToHex(data);
    }
}
