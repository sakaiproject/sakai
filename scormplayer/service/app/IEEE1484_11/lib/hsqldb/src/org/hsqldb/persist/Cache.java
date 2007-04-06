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


package org.hsqldb.persist;

import java.io.IOException;

import org.hsqldb.Trace;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.ObjectComparator;
import org.hsqldb.lib.Sort;
import org.hsqldb.lib.StopWatch;
import org.hsqldb.store.ObjectCacheHashMap;

/**
 * New implementation of row caching for CACHED tables.<p>
 *
 * Manages memory for the cache map and its contents based on least recently
 * used clearup.<p>
 * Also provides services for selecting rows to be saved and passing them
 * to DataFileCache.<p>
 *
 * @author fredt@users
 * @version 1.8.0
 */
public class Cache {

    final DataFileCache                  dataFileCache;
    private int                          capacity;         // number of Rows
    private long                         bytesCapacity;    // number of bytes
    private final CachedObjectComparator rowComparator;

//
    private CachedObject[] rowTable;

//
    private final ObjectCacheHashMap cacheMap;
    long                             cacheBytesLength;

    // for testing
    StopWatch saveAllTimer = new StopWatch(false);
    StopWatch makeRowTimer = new StopWatch(false);
    StopWatch sortTimer    = new StopWatch(false);
    int       makeRowCount = 0;
    int       saveRowCount = 0;

    Cache(DataFileCache dfc) {

        dataFileCache    = dfc;
        capacity         = dfc.capacity();
        bytesCapacity    = dfc.bytesCapacity();
        rowComparator    = new CachedObjectComparator();
        rowTable         = new CachedObject[capacity];
        cacheMap         = new ObjectCacheHashMap(capacity);
        cacheBytesLength = 0;
    }

    /**
     *  Structural initialisations take place here. This allows the Cache to
     *  be resized while the database is in operation.
     */
    void init(int capacity, long bytesCapacity) {}

    int size() {
        return cacheMap.size();
    }

    long getTotalCachedBlockSize() {
        return cacheBytesLength;
    }

    /**
     * Returns a row if in memory cache.
     */
    synchronized CachedObject get(int pos) {
        return (CachedObject) cacheMap.get(pos);
    }

    /**
     * Adds a row to the cache.
     */
    synchronized void put(int key,
                                 CachedObject row) throws IOException {

        int storageSize = row.getStorageSize();

        if (cacheMap.size() >= capacity
                || storageSize + cacheBytesLength > bytesCapacity) {
            cleanUp();
        }

        cacheMap.put(key, row);

        cacheBytesLength += storageSize;
    }

    /**
     * Removes an object from memory cache. Does not release the file storage.
     */
    synchronized CachedObject release(int i) {

        CachedObject r = (CachedObject) cacheMap.remove(i);

        if (r == null) {
            return null;
        }

        cacheBytesLength -= r.getStorageSize();

        return r;
    }

    /**
     * Reduces the number of rows held in this Cache object. <p>
     *
     * Cleanup is done by checking the accessCount of the Rows and removing
     * the rows with the lowest access count.
     *
     * Index operations require that up to 5 recently accessed rows remain
     * in the cache.
     *
     */
    private synchronized void cleanUp() throws IOException {

        int removeCount = cacheMap.size() / 2;
        int accessTarget = cacheMap.getAccessCountCeiling(removeCount,
            removeCount / 8);
        ObjectCacheHashMap.ObjectCacheIterator it = cacheMap.iterator();
        int                                    savecount = 0;

        for (; it.hasNext(); ) {
            CachedObject r = (CachedObject) it.next();

            if (it.getAccessCount() <= accessTarget) {
                if (!r.isKeepInMemory()) {
                    if (r.hasChanged()) {
                        rowTable[savecount++] = r;
                    }

                    it.remove();

                    cacheBytesLength -= r.getStorageSize();
                }
            }
        }

        cacheMap.setAccessCountFloor(accessTarget);
        saveRows(savecount);
    }

    private synchronized void saveRows(int count) throws IOException {

        if (count == 0) {
            return;
        }

        rowComparator.setType(rowComparator.COMPARE_POSITION);
        sortTimer.start();
        Sort.sort(rowTable, rowComparator, 0, count - 1);
        sortTimer.stop();
        saveAllTimer.start();
        dataFileCache.saveRows(rowTable, 0, count);

        saveRowCount += count;

        /*
                // not necessary if the full storage size of each object is written out
                try {
                    dataFile.file.seek(fileFreePosition);
                } catch (IOException e){}
        */
        saveAllTimer.stop();
    }

    /**
     * Writes out all modified cached Rows.
     */
    synchronized void saveAll() throws IOException {

        Iterator it        = cacheMap.iterator();
        int      savecount = 0;

        for (; it.hasNext(); ) {
            CachedObject r = (CachedObject) it.next();

            if (r.hasChanged()) {
                rowTable[savecount++] = r;
            }
        }

        saveRows(savecount);
        Trace.printSystemOut(
            saveAllTimer.elapsedTimeToMessage(
                "Cache.saveRow() total row save time"));
        Trace.printSystemOut("Cache.saveRow() total row save count = "
                             + saveRowCount);
        Trace.printSystemOut(
            makeRowTimer.elapsedTimeToMessage(
                "Cache.makeRow() total row load time"));
        Trace.printSystemOut("Cache.makeRow() total row load count = "
                             + makeRowCount);
        Trace.printSystemOut(
            sortTimer.elapsedTimeToMessage("Cache.sort() total time"));
    }

    /**
     * clears out the memory cache
     */
    synchronized void clear() {

        cacheMap.clear();

        cacheBytesLength = 0;
    }

    static class CachedObjectComparator implements ObjectComparator {

        static final int COMPARE_LAST_ACCESS = 0;
        static final int COMPARE_POSITION    = 1;
        static final int COMPARE_SIZE        = 2;
        private int      compareType;

        CachedObjectComparator() {}

        void setType(int type) {
            compareType = type;
        }

        public int compare(Object a, Object b) {

            switch (compareType) {

                case COMPARE_POSITION :
                    return ((CachedObject) a).getPos()
                           - ((CachedObject) b).getPos();

                case COMPARE_SIZE :
                    return ((CachedObject) a).getStorageSize()
                           - ((CachedObject) b).getStorageSize();

                default :
                    return 0;
            }
        }
    }
}
