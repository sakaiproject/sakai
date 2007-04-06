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
import org.hsqldb.rowio.RowInputInterface;
import org.hsqldb.rowio.RowOutputInterface;

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// fredt@users 20020920 - path 1.7.1 - refactoring to cut mamory footprint
// fredt@users 20021205 - path 1.7.2 - enhancements

/**
 *  Cached table Node implementation.<p>
 *  Only integral references to left, right and parent nodes in the AVL tree
 *  are held and used as pointers data.<p>
 *
 *  iId is a reference to the Index object that contains this node.<br>
 *  This fields can be eliminated in the future, by changing the
 *  method signatures to take a Index parameter from Index.java (fredt@users)
 *
 *  New class derived from the Hypersonic code
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version    1.7.2
 * @since Hypersonic SQL
 */
class DiskNode extends Node {

    protected Row    rData;
    private int      iLeft   = NO_POS;
    private int      iRight  = NO_POS;
    private int      iParent = NO_POS;
    private int      iId;    // id of Index object for this Node
    static final int SIZE_IN_BYTE = 4 * 4;

    DiskNode(CachedRow r, RowInputInterface in,
             int id) throws IOException, HsqlException {

        iId      = id;
        rData    = r;
        iBalance = in.readIntData();
        iLeft    = in.readIntData();

        if (iLeft <= 0) {
            iLeft = NO_POS;
        }

        iRight = in.readIntData();

        if (iRight <= 0) {
            iRight = NO_POS;
        }

        iParent = in.readIntData();

        if (iParent <= 0) {
            iParent = NO_POS;
        }

        if (Trace.DOASSERT) {

            // fredt - assert not correct - row can be deleted from one index but
            // not yet deleted from other indexes while the process of finding
            // the node is in progress which may require saving the row
            // to make way for new rows in the cache and loading it back
            // Trace.doAssert(iBalance != -2);
        }
    }

    DiskNode(CachedRow r, int id) {
        iId   = id;
        rData = r;
    }

    void delete() {
        rData    = null;
        iBalance = -2;
    }

    int getKey() {

        if (rData != null) {
            return ((CachedRow) rData).iPos;
        }

        return NO_POS;
    }

    Row getRow() throws HsqlException {

        if (Trace.DOASSERT) {
            Trace.doAssert(rData != null);
        }

        return rData;
    }

    private Node findNode(int pos) throws HsqlException {

        Node ret = null;
        Row  r   = ((CachedRow) rData).getTable().getRow(pos);

        if (r != null) {
            ret = r.getNode(iId);
        }

        return ret;
    }

    boolean isLeft(Node node) throws HsqlException {

        if (node == null) {
            return iLeft == NO_POS;
        }

        return iLeft == ((DiskNode) node).getKey();
    }

    boolean isRight(Node node) throws HsqlException {

        if (node == null) {
            return iRight == NO_POS;
        }

        return iRight == ((DiskNode) node).getKey();
    }

    Node getLeft() throws HsqlException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        if (iLeft == NO_POS) {
            return null;
        }

        return findNode(iLeft);
    }

    Node getRight() throws HsqlException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        if (iRight == NO_POS) {
            return null;
        }

        return findNode(iRight);
    }

    Node getParent() throws HsqlException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        if (iParent == NO_POS) {
            return null;
        }

        return findNode(iParent);
    }

    boolean isRoot() {
        return iParent == Node.NO_POS;
    }

    boolean isFromLeft() throws HsqlException {

        if (this.isRoot()) {
            return true;
        }

        if (Trace.DOASSERT) {
            Trace.doAssert(getParent() != null);
        }

        DiskNode parent = (DiskNode) getParent();

        return getKey() == parent.iLeft;
    }

    Object[] getData() throws HsqlException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        return rData.getData();
    }

    void setParent(Node n) throws HsqlException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        ((CachedRow) rData).setChanged();

        iParent = n == null ? NO_POS
                            : n.getKey();
    }

    void setBalance(int b) throws HsqlException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        if (iBalance != b) {
            ((CachedRow) rData).setChanged();

            iBalance = b;
        }
    }

    void setLeft(Node n) throws HsqlException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        ((CachedRow) rData).setChanged();

        iLeft = n == null ? NO_POS
                          : n.getKey();
    }

    void setRight(Node n) throws HsqlException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        ((CachedRow) rData).setChanged();

        iRight = n == null ? NO_POS
                           : n.getKey();
    }

    boolean equals(Node n) {

/*
        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);

            if (n != this) {
                boolean test = (getKey() == NO_POS) || (n == null)
                               || (n.getKey() != getKey());

                if (test == false) {
                    test = iParent == ((DiskNode) n).iParent
                           && iLeft == ((DiskNode) n).iLeft
                           && iRight == ((DiskNode) n).iRight;

                    if (test == false) {
                        int aA = ((CachedRow) getRow()).iLastAccess;
                        int bA = ((CachedRow) n.getRow()).iLastAccess;

                        Trace.doAssert(test,
                                       "a: " + aA + ", " + iParent + ", "
                                       + iLeft + ", " + iRight + " b: " + bA
                                       + ", " + ((DiskNode) n).iParent + ", "
                                       + ((DiskNode) n).iLeft + ", "
                                       + ((DiskNode) n).iRight);
                    }
                }
            }
        }
*/
        return this == n
               || (n != null && getKey() == ((DiskNode) n).getKey());
    }

    void write(RowOutputInterface out) throws IOException {

        if (Trace.DOASSERT) {

            // fredt - assert not correct - row can be deleted from one index but
            // not yet deleted from other indexes while the process of finding
            // the node is in progress which may require saving the row
            // to make way for new rows in the cache
            // Trace.doAssert(iBalance != -2);
        }

        out.writeIntData(iBalance);
        out.writeIntData((iLeft == NO_POS) ? 0
                                           : iLeft);
        out.writeIntData((iRight == NO_POS) ? 0
                                            : iRight);
        out.writeIntData((iParent == NO_POS) ? 0
                                             : iParent);
    }

    Node getUpdatedNode() throws HsqlException {

        Row row = rData.getUpdatedRow();

        return row == null ? null
                           : row.getNode(iId);
    }

    void writeTranslate(RowOutputInterface out, IntLookup lookup) {

        out.writeIntData(iBalance);
        writeTranslatePointer(iLeft, out, lookup);
        writeTranslatePointer(iRight, out, lookup);
        writeTranslatePointer(iParent, out, lookup);
    }

    private void writeTranslatePointer(int pointer, RowOutputInterface out,
                                       IntLookup lookup) {

        int newPointer = 0;

        if (pointer != Node.NO_POS) {
            newPointer = lookup.lookupFirstEqual(pointer);
        }

        out.writeIntData(newPointer);
    }
}
