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


package org.hsqldb;

import java.io.IOException;

import org.hsqldb.rowio.RowInputInterface;
import org.hsqldb.rowio.RowOutputInterface;

// fredt@users 20021205 - path 1.7.2 - enhancements
// fredt@users 20021215 - doc 1.7.2 - javadoc comments

/**
 * Implementation of rows for tables with memory resident indexes and
 * disk-based data, such as TEXT tables.
 *
 * @version 1.7.2
 */
class CachedDataRow extends CachedRow {

    /**
     *  Constructor for new rows.
     */
    CachedDataRow(Table t, Object[] o) throws HsqlException {

        super(t, o);

        hasDataChanged = true;
    }

    /**
     *  Constructor when read from the disk into the Cache. The link with
     *  the Nodes is made separetly.
     */
    CachedDataRow(Table t,
                  RowInputInterface in) throws IOException, HsqlException {

        tTable         = t;
        iPos           = in.getPos();
        storageSize    = in.getSize();
        oData          = in.readData(tTable.getColumnTypes());
        hasDataChanged = false;
    }

    /**
     *  As the indexes are in-memory, this passes the existing primary node
     *  for the construction of the new Row
     */
    Row getUpdatedRow() throws HsqlException {
        return tTable.getRow(iPos, nPrimaryNode);
    }

    /**
     *  Used when data is read from the disk into the Cache the first time.
     *  New Nodes are created which are then indexed.
     */
    void setNewNodes() {

        int index = tTable.getIndexCount();

        nPrimaryNode = Node.newNode(this, 0, tTable);

        Node n = nPrimaryNode;

        for (int i = 1; i < index; i++) {
            n.nNext = Node.newNode(this, i, tTable);
            n       = n.nNext;
        }
    }

    /**
     *  Used when data is re-read from the disk into the Cache. The Row is
     *  already indexed so it is linked with the Node in the primary index.
     *  the Nodes is made separetly.
     */
    void setPrimaryNode(Node primary) {
        nPrimaryNode = primary;
    }

    /**
     * returned size does not include the row size written at the beginning
     */
    public int getRealSize(RowOutputInterface out) {
        return out.getSize(this);
    }

    /**
     *  Writes the data to disk. Unlike CachedRow, hasChanged is never set
     *  to true when changes are made to the Nodes. (Nodes are in-memory).
     *  The only time this is used is when a new Row is added to the Caches.
     */
    public void write(RowOutputInterface out) {

        out.writeSize(storageSize);
        out.writeData(oData, tTable);
        out.writeEnd();

        hasDataChanged = false;
    }

    public boolean hasChanged() {
        return hasDataChanged;
    }

    /**
     * Sets the file position for the row and registers the row with
     * the table.
     *
     * @param pos position in data file
     */
    public void setPos(int pos) {

        iPos = pos;

        Node n = nPrimaryNode;

        while (n != null) {
            ((PointerNode) n).iData = iPos;
            n                       = n.nNext;
        }
    }

    /**
     * With the current implementation of TEXT table updates and inserts,
     * the lifetime scope of this method extends until redefinition of table
     * data source or shutdown.
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj argument;
     *   <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }

        if (obj instanceof CachedDataRow) {
            return ((CachedDataRow) obj).iPos == iPos
                   && ((CachedDataRow) obj).tTable == tTable;
        }

        return false;
    }
}
