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

import org.hsqldb.lib.IntLookup;
import org.hsqldb.lib.java.JavaSystem;
import org.hsqldb.persist.CachedObject;
import org.hsqldb.rowio.RowOutputInterface;

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// fredt@users 20020920 - patch 1.7.1 - refactoring to cut mamory footprint
// fredt@users 20021215 - doc 1.7.2 - javadoc comments

/**
 * Base class for a database row object implementing rows for
 * memory resident tables.<p>
 *
 * Subclass CachedRow implements rows for CACHED and TEXT tables
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.8.0
 * @since Hypersonic SQL
 */
public class Row implements CachedObject {

    int                tableId;
    int                iPos;
    protected Object[] oData;
    protected Node     nPrimaryNode;

    /**
     *  Default constructor used only in subclasses.
     */
    protected Row() {}

    /**
     *  Constructor for MEMORY table Row. The result is a Row with Nodes that
     *  are not yet linked with other Nodes in the AVL indexes.
     */
    Row(Table t, Object[] o) throws HsqlException {

        int index = t.getIndexCount();

        nPrimaryNode = Node.newNode(this, 0, t);

        Node n = nPrimaryNode;

        for (int i = 1; i < index; i++) {
            n.nNext = Node.newNode(this, i, t);
            n       = n.nNext;
        }

        tableId = t.getId();
        oData   = o;
    }

    /**
     * Returns the Node for a given Index, using the ordinal position of the
     * Index within the Table Object.
     */
    Node getNode(int index) {

        Node n = nPrimaryNode;

        while (index-- > 0) {
            n = n.nNext;
        }

        return n;
    }

    /**
     *  Returns the Node for the next Index on this database row, given the
     *  Node for any Index.
     */
    Node getNextNode(Node n) {

        if (n == null) {
            n = nPrimaryNode;
        } else {
            n = n.nNext;
        }

        return n;
    }

    /**
     * Returns the Row Object that currently represents the same database row.
     * In current implementations of Row, this is always the same as the this
     * Object for MEMORY tables, but could be a different Object for CachedRow
     * or CachedDataRow implementation. For example the Row Object that
     * represents a given database row can be freed from the Cache when other
     * rows need to be loaded into the Cache. getUpdatedRow() returns a
     * currently valid Row object that is in the Cache.
     */
    Row getUpdatedRow() throws HsqlException {
        return this;
    }

    /**
     * Returns the array of fields in the database row.
     */
    public Object[] getData() {
        return oData;
    }

    /**
     *  Is used only when the database row is deleted, not when it is freed
     *  from the Cache.
     */
    void delete() throws HsqlException {

        JavaSystem.memoryRecords++;

        nPrimaryNode = null;
    }

    void clearNodeLinks() {

        Node last;
        Node temp;

        last = nPrimaryNode;

        while (last.nNext != null) {
            temp       = last.nNext;
            last.nNext = null;
            last       = temp;
        }

        nPrimaryNode = null;
    }

    boolean isCascadeDeleted() {
        return nPrimaryNode == null;
    }

    public int getRealSize(RowOutputInterface out) {
        return 0;
    }

    public void setStorageSize(int size) {
        ;
    }

    public int getStorageSize() {
        return 0;
    }

    public long getId() {
        return ((long) tableId << 32) + ((long) iPos);
    }

    public static long getId(Table table, int pos) {
        return ((long) table.getId() << 32) + ((long) pos);
    }

    public int getPos() {
        return iPos;
    }

    public void setPos(int pos) {
        iPos = pos;
    }

    public boolean hasChanged() {
        return false;
    }

    public boolean isKeepInMemory() {
        return true;
    }

    public void keepInMemory(boolean keep) {}

    public boolean isInMemory() {
        return true;
    }

    public void setInMemory(boolean in) {}

    public void write(RowOutputInterface out) {}

    public void write(RowOutputInterface out, IntLookup lookup) {}

    /**
     * Lifetime scope of this method depends on the operations performed on
     * any cached tables since this row or the parameter were constructed.
     * If only deletes or only inserts have been performed, this method
     * remains valid. Otherwise it can return invalid results.
     *
     * @param obj row to compare
     * @return boolean
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }

        if (obj instanceof Row) {
            return ((Row) obj).iPos == iPos;
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
