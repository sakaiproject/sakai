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


package org.hsqldb;

import java.io.IOException;

import org.hsqldb.lib.IntLookup;
import org.hsqldb.lib.java.JavaSystem;
import org.hsqldb.rowio.RowInputInterface;
import org.hsqldb.rowio.RowOutputInterface;

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// fredt@users 20020920 - patch 1.7.1 - refactoring to cut memory footprint
// fredt@users 20021205 - patch 1.7.2 - enhancements
// fredt@users 20021215 - doc 1.7.2 - javadoc comments
// boucherb@users - 20040411 - doc 1.7.2 - javadoc comments

/**
 *  In-memory representation of a disk-based database row object with  methods
 *  for serialization and de-serialization. <p>
 *
 *  A CachedRow is normally part of a circular double linked list which
 *  contains all of the Rows currently in the Cache for the database. It is
 *  unlinked from this list when it is freed from the Cache to make way for
 *  other rows.
 *
 *  New class from the Hypersonic Original
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version    1.7.2
 * @since Hypersonic SQL
 */
public class CachedRow extends Row {

    static final int NO_POS = -1;

    //
    protected Table tTable;
    int             storageSize;

    /**
     *  Flag indicating unwritten data.
     */
    protected boolean hasDataChanged;

    /**
     *  Flag indicating Node data has changed.
     */
    boolean hasNodesChanged;

    /**
     *  Default constructor used only in subclasses.
     */
    CachedRow() {}

    public static CachedRow newCachedRow(Table t,
                                         Object[] o) throws HsqlException {

        if (t.isText) {
            return new CachedDataRow(t, o);
        } else {
            return new CachedRow(t, o);
        }
    }

    /**
     *  Constructor for new Rows.  Variable hasDataChanged is set to true in
     *  order to indicate the data needs saving.
     *
     * @param t table
     * @param o row data
     * @throws HsqlException if a database access error occurs
     */
    CachedRow(Table t, Object[] o) throws HsqlException {

        tTable = t;

        int indexcount = t.getIndexCount();

        nPrimaryNode = Node.newNode(this, 0, t);

        Node n = nPrimaryNode;

        for (int i = 1; i < indexcount; i++) {
            n.nNext = Node.newNode(this, i, t);
            n       = n.nNext;
        }

        oData          = o;
        hasDataChanged = hasNodesChanged = true;
    }

    /**
     *  Constructor when read from the disk into the Cache.
     *
     * @param t table
     * @param in data source
     * @throws IOException
     * @throws HsqlException
     */
    public CachedRow(Table t,
                     RowInputInterface in) throws IOException, HsqlException {

        tTable      = t;
        iPos        = in.getPos();
        storageSize = in.getSize();

        int indexcount = t.getIndexCount();

        nPrimaryNode = Node.newNode(this, in, 0, t);

        Node n = nPrimaryNode;

        for (int i = 1; i < indexcount; i++) {
            n.nNext = Node.newNode(this, in, i, t);
            n       = n.nNext;
        }

        oData = in.readData(tTable.getColumnTypes());
    }

    private void readRowInfo(RowInputInterface in)
    throws IOException, HsqlException {

        // for use when additional transaction info is attached to rows
    }

    /**
     *  This method is called only when the Row is deleted from the database
     *  table. The links with all the other objects apart from the data
     *  are removed.
     *
     * @throws HsqlException
     */
    public void delete() throws HsqlException {

        super.delete();

        hasNodesChanged = hasDataChanged = false;
        tTable          = null;
    }

    public int getStorageSize() {
        return storageSize;
    }

    /**
     * Sets the file position for the row
     *
     * @param pos position in data file
     */
    public void setPos(int pos) {
        iPos = pos;
    }

    /**
     * Sets flag for Node data change.
     */
    void setChanged() {
        hasNodesChanged = true;
    }

    /**
     * Returns true if Node data has changed.
     *
     * @return boolean
     */
    public boolean hasChanged() {
        return hasNodesChanged;
    }

    /**
     * Returns the Table to which this Row belongs.
     *
     * @return Table
     */
    public Table getTable() {
        return tTable;
    }

    /**
     * returned size does not include the row size written at the beginning
     */
    public int getRealSize(RowOutputInterface out) {
        return tTable.getIndexCount() * DiskNode.SIZE_IN_BYTE
               + out.getSize(this);
    }

    public void setStorageSize(int size) {
        storageSize = size;
    }

    /**
     * Returns true if any of the Nodes for this row is a root node.
     * Used only in Cache.java to avoid removing the row from the cache.
     *
     * @return boolean
     * @throws HsqlException
     */
    synchronized public boolean isKeepInMemory() {

        Node n = nPrimaryNode;

        while (n != null) {
            if (n.isRoot()) {
                return true;
            }

            n = n.nNext;
        }

        return false;
    }

    /**
     *  Using the internal reference to the Table, returns the current cached
     *  Row. Valid for deleted rows only before any subsequent insert or
     *  update on any cached table.<p>
     *
     *  Access to tables while performing the internal operations for an
     *  SQL statement result in CachedRow objects to be cleared from the cache.
     *  This method returns the CachedRow, loading it to the cache if it is not
     *  there.
     * @return the current Row in Cache for this Object
     * @throws HsqlException
     */
    synchronized Row getUpdatedRow() throws HsqlException {
        return tTable == null ? null
                              : (CachedRow) tTable.rowStore.get(iPos);
    }

    /**
     * used in CachedDataRow
     */
    void setNewNodes() {}

    /**
     *  Used exclusively by Cache to save the row to disk. New implementation
     *  in 1.7.2 writes out only the Node data if the table row data has not
     *  changed. This situation accounts for the majority of invocations as
     *  for each row deleted or inserted, the Nodes for several other rows
     *  will change.
     *
     * @param output data source
     * @throws IOException
     * @throws HsqlException
     */
    public void write(RowOutputInterface out) {

        try {
            writeNodes(out);

            if (hasDataChanged) {
                out.writeData(oData, tTable);
                out.writeEnd();

                hasDataChanged = false;
            }
        } catch (IOException e) {}
    }

    private void writeRowInfo(RowOutputInterface out) {

        // for use when additional transaction info is attached to rows
    }

    public void write(RowOutputInterface out, IntLookup lookup) {

        out.writeSize(storageSize);

        Node rownode = nPrimaryNode;

        while (rownode != null) {
            ((DiskNode) rownode).writeTranslate(out, lookup);

            rownode = rownode.nNext;
        }

        out.writeData(getData(), getTable());
        out.writeEnd();
    }

    /**
     *  Writes the Nodes, immediately after the row size.
     *
     * @param out
     *
     * @throws IOException
     * @throws HsqlException
     */
    private void writeNodes(RowOutputInterface out) throws IOException {

        out.writeSize(storageSize);

        Node n = nPrimaryNode;

        while (n != null) {
            n.write(out);

            n = n.nNext;
        }

        hasNodesChanged = false;
    }

    /**
     * With CACHED tables there may possibly exist two copies of the row.
     * All copies will have the same iPos.
     *
     * @param obj row to compare
     * @return boolean
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }

        if (obj instanceof CachedRow) {
            return ((CachedRow) obj).iPos == iPos;
        }

        return false;
    }

    /**
     * Hash code is valid only until a modification to the cache
     *
     * @return file position of row
     */
    public int hashCode() {
        return iPos;
    }
}
