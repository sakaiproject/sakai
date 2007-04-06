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


package org.hsqldb.store;

/**
 * Implementation of a bit map of any size, together with static methods to
 * manipulate int values as bit maps.
 *
* @author fredt@users
* @version 1.8.0
* @since 1.8.0
*/
public class BitMap {

    int   defaultCapacity;
    int   capacity;
    int[] map;

    public BitMap(int initialCapacity) {

        int words = initialCapacity / 32;

        if (initialCapacity % 32 != 0) {
            words++;
        }

        defaultCapacity = capacity = words * 32;
        map             = new int[words];
    }

    /**
     * Resets to blank with original capacity
     */
    public void reset() {
        map      = new int[defaultCapacity / 32];
        capacity = defaultCapacity;
    }

    /**
     * Sets pos and returns old value
     */
    public int set(int pos) {

        while (pos >= capacity) {
            doubleCapacity();
        }

        int windex = pos >> 5;
        int mask   = 0x80000000 >>> (pos & 0x1F);
        int word   = map[windex];
        int result = (word & mask) == 0 ? 0
                                        : 1;

        map[windex] = (word | mask);

        return result;
    }

    /**
     * Unsets pos and returns old value
     */
    public int unset(int pos) {

        if (pos >= capacity) {
            return 0;
        }

        int windex = pos >> 5;
        int mask   = 0x80000000 >>> (pos & 0x1F);
        int word   = map[windex];
        int result = (word & mask) == 0 ? 0
                                        : 1;

        mask        = ~mask;
        map[windex] = (word & mask);

        return result;
    }

    public int get(int pos) {

        while (pos >= capacity) {
            doubleCapacity();
        }

        int windex = pos >> 5;
        int mask   = 0x80000000 >>> (pos & 0x1F);
        int word   = map[windex];

        return (word & mask) == 0 ? 0
                                  : 1;
    }

    public static int set(int map, int pos) {

        int mask = 0x80000000 >>> pos;

        return (map | mask);
    }

    public static int unset(int map, int pos) {

        int mask = 0x80000000 >>> pos;

        mask = ~mask;

        return (map & mask);
    }

    public static boolean isSet(int map, int pos) {

        int mask = 0x80000000 >>> pos;

        return (map & mask) == 0 ? false
                                 : true;
    }

    private void doubleCapacity() {

        int[] newmap = new int[map.length * 2];

        capacity *= 2;

        System.arraycopy(map, 0, newmap, 0, map.length);

        map = newmap;
    }
}
