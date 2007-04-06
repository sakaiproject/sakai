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

import java.math.BigDecimal;
import java.sql.Date;

/**
  * Supports pooling of Integer, Long, Double, BigDecimal, String and Date
  * Java Objects. Leads to reduction in memory use when an Object is used more
  * then twice in the database.
  *
  * getXXX methods are used for retrival of values. If a value is not in
  * the pool, it is added to the pool and returned. When the pool gets
  * full, half the contents that have been accessed less recently are purged.
  *
  * @author fredt@users
  * @version 1.8.0
  * @since 1.7.2
  */
public class ValuePool {

    //
    static ValuePoolHashMap intPool;
    static ValuePoolHashMap longPool;
    static ValuePoolHashMap doublePool;
    static ValuePoolHashMap bigdecimalPool;
    static ValuePoolHashMap stringPool;
    static ValuePoolHashMap datePool;
    static final int        DEFAULT_VALUE_POOL_SIZE = 10000;
    static final int[]      defaultPoolLookupSize   = new int[] {
        DEFAULT_VALUE_POOL_SIZE, DEFAULT_VALUE_POOL_SIZE,
        DEFAULT_VALUE_POOL_SIZE, DEFAULT_VALUE_POOL_SIZE,
        DEFAULT_VALUE_POOL_SIZE, DEFAULT_VALUE_POOL_SIZE
    };
    static final int POOLS_COUNT            = defaultPoolLookupSize.length;
    static final int defaultSizeFactor      = 2;
    static final int defaultMaxStringLength = 16;

    //
    static ValuePoolHashMap[] poolList;

    //
    static int maxStringLength;

    //
    static {
        initPool();
    }

    private static void initPool() {

        int[] sizeArray  = defaultPoolLookupSize;
        int   sizeFactor = defaultSizeFactor;

        synchronized (ValuePool.class) {
            maxStringLength = defaultMaxStringLength;
            poolList        = new ValuePoolHashMap[POOLS_COUNT];

            for (int i = 0; i < POOLS_COUNT; i++) {
                int size = sizeArray[i];

                poolList[i] = new ValuePoolHashMap(size, size * sizeFactor,
                                                   BaseHashMap.PURGE_HALF);
            }

            intPool        = poolList[0];
            longPool       = poolList[1];
            doublePool     = poolList[2];
            bigdecimalPool = poolList[3];
            stringPool     = poolList[4];
            datePool       = poolList[5];
        }
    }

    public static void resetPool(int[] sizeArray, int sizeFactor) {

        synchronized (ValuePool.class) {
            for (int i = 0; i < POOLS_COUNT; i++) {
                poolList[i].resetCapacity(sizeArray[i] * sizeFactor,
                                          BaseHashMap.PURGE_HALF);
            }
        }
    }

    public static void resetPool() {

        synchronized (ValuePool.class) {
            resetPool(defaultPoolLookupSize, defaultSizeFactor);
        }
    }

    public static void clearPool() {

        synchronized (ValuePool.class) {
            for (int i = 0; i < POOLS_COUNT; i++) {
                poolList[i].clear();
            }
        }
    }

    public static Integer getInt(int val) {

        synchronized (intPool) {
            return intPool.getOrAddInteger(val);
        }
    }

    public static Long getLong(long val) {

        synchronized (longPool) {
            return longPool.getOrAddLong(val);
        }
    }

    public static Double getDouble(long val) {

        synchronized (doublePool) {
            return doublePool.getOrAddDouble(val);
        }
    }

    public static String getString(String val) {

        if (val == null || val.length() > maxStringLength) {
            return val;
        }

        synchronized (stringPool) {
            return stringPool.getOrAddString(val);
        }
    }

    public static Date getDate(long val) {

        synchronized (datePool) {
            return datePool.getOrAddDate(val);
        }
    }

    public static BigDecimal getBigDecimal(BigDecimal val) {

        if (val == null) {
            return val;
        }

        synchronized (bigdecimalPool) {
            return (BigDecimal) bigdecimalPool.getOrAddObject(val);
        }
    }

    public static Boolean getBoolean(boolean b) {
        return b ? Boolean.TRUE
                 : Boolean.FALSE;
    }
}
