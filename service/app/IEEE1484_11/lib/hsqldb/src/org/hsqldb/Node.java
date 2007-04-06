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

import org.hsqldb.rowio.RowInputInterface;
import org.hsqldb.rowio.RowOutputInterface;

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// fredt@users 20020920 - path 1.7.1 - refactoring to cut mamory footprint
// fredt@users 20021205 - path 1.7.2 - enhancements
// fredt@users 20021215 - doc 1.7.2 - javadoc comments

/**
 *  The parent for all AVL node implementations, features factory methods for
 *  its subclasses. Subclasses of Node vary in the way they hold
 *  references to other Nodes in the AVL tree, or to their Row data.<br>
 *
 *  nNext links the Node objects belonging to different indexes for each
 *  table row. It is used solely by Row to locate the node belonging to a
 *  particular index.
 *
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.7.2
 * @since Hypersonic SQL
 */
abstract class Node {

    static final int NO_POS = CachedRow.NO_POS;
    int              iBalance;    // currently, -2 means 'deleted'
    Node             nNext;       // node of next index (nNext==null || nNext.iId=iId+1)

    static final Node newNode(Row r, int id, Table t) {

        switch (t.getIndexType()) {

            case Index.MEMORY_INDEX :
                return new MemoryNode(r);

            case Index.POINTER_INDEX :
                return new PointerNode((CachedRow) r, id);

            case Index.DISK_INDEX :
            default :
                return new DiskNode((CachedRow) r, id);
        }
    }

    static final Node newNode(Row r, RowInputInterface in, int id,
                              Table t) throws IOException, HsqlException {

        switch (t.getIndexType()) {

            case Index.MEMORY_INDEX :
                return new MemoryNode(r);

            case Index.POINTER_INDEX :
                return new PointerNode((CachedRow) r, id);

            case Index.DISK_INDEX :
            default :
                return new DiskNode((CachedRow) r, in, id);
        }
    }

    /**
     *  This method unlinks the Node from the other Nodes in the same Index
     *  and from the Row.
     *
     *  It must keep the links between the Nodes in different Indexes.
     */
    abstract void delete();

    /**
     *  File offset of Node. Used with CachedRow objects only
     */
    abstract int getKey();

    /**
     *  Return the Row Object that is linked to this Node.
     */
    abstract Row getRow() throws HsqlException;

    /**
     *  Getters and setters for AVL index operations.
     */
    abstract boolean isLeft(Node node) throws HsqlException;

    abstract boolean isRight(Node node) throws HsqlException;

    abstract Node getLeft() throws HsqlException;

    abstract void setLeft(Node n) throws HsqlException;

    abstract Node getRight() throws HsqlException;

    abstract void setRight(Node n) throws HsqlException;

    abstract Node getParent() throws HsqlException;

    abstract void setParent(Node n) throws HsqlException;

    final int getBalance() throws HsqlException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        return iBalance;
    }

    abstract void setBalance(int b) throws HsqlException;

    abstract boolean isRoot();

    abstract boolean isFromLeft() throws HsqlException;

    /**
     *  Returns the database table data for this Node
     *
     */
    abstract Object[] getData() throws HsqlException;

    abstract boolean equals(Node n);

    /**
     *  Returns the Node Object that currently represents this Node in the
     *  AVL index structure. In current implementations of Node this is
     *  always the same as the this Object for MEMORY and TEXT tables but can
     *  be a different Object for CACHED tables, where DiskNode Objects may
     *  be freed from the Cache. Calling this method returns a Node with
     *  currently valid pointers to its linked AVL Nodes.
     *
     */
    Node getUpdatedNode() throws HsqlException {
        return this;
    }

    /**
     *  Writes out the node in an implementation dependent way.
     */
    abstract void write(RowOutputInterface out) throws IOException;

    boolean isDeleted() {
        return iBalance == -2;
    }
}
