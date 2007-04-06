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

import java.util.NoSuchElementException;

import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.index.RowIterator;
import org.hsqldb.lib.ArrayUtil;

// fredt@users 20020221 - patch 513005 by sqlbob@users - corrections
// fredt@users 20020225 - patch 1.7.0 - changes to support cascading deletes
// tony_lai@users 20020820 - patch 595052 - better error message
// fredt@users 20021205 - patch 1.7.2 - changes to method signature
// fredt@users - patch 1.80 - reworked the interface and comparison methods

/**
 * Implementation of an AVL tree with parent pointers in nodes. Subclasses
 * of Node implement the tree node objects for memory or disk storage. An
 * Index has a root Node that is linked with other nodes using Java Object
 * references or file pointers, depending on Node implementation.<p>
 * An Index object also holds information on table columns (in the form of int
 * indexes) that are covered by it.(fredt@users)
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.8.0
 * @since Hypersonic SQL
 */
public class Index {

    // types of index
    static final int MEMORY_INDEX  = 0;
    static final int DISK_INDEX    = 1;
    static final int POINTER_INDEX = 2;

    // fields
    private final HsqlName indexName;
    final boolean[]        colCheck;
    private final int[]    colIndex;
    private final int[]    colTypes;
    final int[]            pkCols;
    final int[]            pkTypes;
    private final boolean  isUnique;    // DDL uniqueness
    private final boolean  useRowId;
    final boolean          isConstraint;
    final boolean          isForward;
    final boolean          isTemp;
    private Node           root;
    private int            depth;
    final Collation        collation;
    static IndexRowIterator emptyIterator = new IndexRowIterator(null, null,
        null);
    IndexRowIterator updatableIterators;
    final boolean    onCommitPreserve;
    final Table      table;

    /**
     * Constructor declaration
     *
     *
     * @param name HsqlName of the index
     * @param table table of the index
     * @param column array of column indexes
     * @param type array of column types
     * @param unique is this a unique index
     * @param constraint does this index belonging to a constraint
     * @param forward is this an auto-index for an FK that refers to a table defined after this table
     * @param visColumns count of visible columns
     */
    Index(Database database, HsqlName name, Table table, int[] column,
            int[] colTypes, boolean isPk, boolean unique, boolean constraint,
            boolean forward, int[] pkcols, int[] pktypes, boolean temp) {

        this.table     = table;
        this.indexName = name;
        this.colIndex  = column;
        this.colTypes  = colTypes;
        this.pkCols    = pkcols;
        this.pkTypes   = pktypes;
        isUnique       = unique;
        isConstraint   = constraint;
        isForward      = forward;
        useRowId = (!isUnique && pkCols.length == 0)
                   || (colIndex.length == 0);
        colCheck = table.getNewColumnCheckList();

        ArrayUtil.intIndexesToBooleanArray(colIndex, colCheck);

        updatableIterators = new IndexRowIterator(null, null, null);
        updatableIterators.next = updatableIterators.last =
            updatableIterators;
        collation        = database.collation;
        isTemp           = temp;
        onCommitPreserve = table.onCommitPreserve;
    }

    /**
     * Returns the HsqlName object
     */
    HsqlName getName() {
        return indexName;
    }

    /**
     * Changes index name. Used by 'alter index rename to'. Argument isquoted
     * is true if the name was quoted in the DDL.
     */
    void setName(String name, boolean isquoted) throws HsqlException {
        indexName.rename(name, isquoted);
    }

    /**
     * Returns the count of visible columns used
     */
    int getVisibleColumns() {
        return colIndex.length;
    }

    /**
     * Is this a UNIQUE index?
     */
    boolean isUnique() {
        return isUnique;
    }

    /**
     * Does this index belong to a constraint?
     */
    boolean isConstraint() {
        return isConstraint;
    }

    /**
     * Returns the array containing column indexes for index
     */
    int[] getColumns() {
        return colIndex;    // todo: this gives back also primary key field(s)!
    }

    /**
     * Returns the array containing column indexes for index
     */
    int[] getColumnTypes() {
        return colTypes;    // todo: this gives back also primary key field(s)!
    }

    String getColumnNameList() {

        String columnNameList = "";

        for (int j = 0; j < colIndex.length; ++j) {
            columnNameList +=
                table.getColumn(colIndex[j]).columnName.statementName;

            if (j < colIndex.length - 1) {
                columnNameList += ",";
            }
        }

        return columnNameList;
    }

    /**
     * Returns the node count.
     */
    int size(Session session) throws HsqlException {

        int         count = 0;
        RowIterator it    = firstRow(session);

        while (it.hasNext()) {
            it.next();

            count++;
        }

        return count;
    }

    boolean isEmpty(Session session) {
        return getRoot(session) == null;
    }

    public int sizeEstimate() throws HsqlException {

        firstRow(null);

        return (int) (1L << depth);
    }

    void clearAll(Session session) {

        setRoot(session, null);

        depth = 0;
        updatableIterators.next = updatableIterators.last =
            updatableIterators;
    }

    void clearIterators() {
        updatableIterators.next = updatableIterators.last =
            updatableIterators;
    }

    void setRoot(Session session, Node node) {

        if (isTemp) {
            session.setIndexRoot(indexName, onCommitPreserve, node);
        } else {
            root = node;
        }
    }

    int getRoot() {
        return (root == null) ? -1
                              : root.getKey();
    }

    private Node getRoot(Session session) {

        if (isTemp && session != null) {
            return session.getIndexRoot(indexName, onCommitPreserve);
        } else {
            return root;
        }
    }

    /**
     * Insert a node into the index
     */
    void insert(Session session, Row row, int offset) throws HsqlException {

        Node    n       = getRoot(session);
        Node    x       = n;
        boolean isleft  = true;
        int     compare = -1;

        while (true) {
            if (n == null) {
                if (x == null) {
                    setRoot(session, row.getNode(offset));

                    return;
                }

                set(x, isleft, row.getNode(offset));

                break;
            }

            compare = compareRowForInsert(session, row, n.getRow());

            if (compare == 0) {
                int    errorCode = Trace.VIOLATION_OF_UNIQUE_INDEX;
                String name      = indexName.statementName;

                if (isConstraint) {
                    Constraint c =
                        table.getUniqueOrPKConstraintForIndex(this);

                    if (c != null) {
                        name      = c.getName().name;
                        errorCode = Trace.VIOLATION_OF_UNIQUE_CONSTRAINT;
                    }
                }

                throw Trace.error(errorCode, new Object[] {
                    name, getColumnNameList()
                });
            }

            isleft = compare < 0;
            x      = n;
            n      = child(x, isleft);
        }

        balance(session, x, isleft);
    }

    /**
     * Balances part of the tree after an alteration to the index.
     */
    private void balance(Session session, Node x,
                         boolean isleft) throws HsqlException {

        while (true) {
            int sign = isleft ? 1
                              : -1;

            x = x.getUpdatedNode();

            switch (x.getBalance() * sign) {

                case 1 :
                    x.setBalance(0);

                    return;

                case 0 :
                    x.setBalance(-sign);
                    break;

                case -1 :
                    Node l = child(x, isleft);

                    if (l.getBalance() == -sign) {
                        replace(session, x, l);
                        set(x, isleft, child(l, !isleft));
                        set(l, !isleft, x);

                        x = x.getUpdatedNode();

                        x.setBalance(0);

                        l = l.getUpdatedNode();

                        l.setBalance(0);
                    } else {
                        Node r = child(l, !isleft);

                        replace(session, x, r);
                        set(l, !isleft, child(r.getUpdatedNode(), isleft));
                        set(r, isleft, l);
                        set(x, isleft, child(r.getUpdatedNode(), !isleft));
                        set(r, !isleft, x);

                        int rb = r.getUpdatedNode().getBalance();

                        x.getUpdatedNode().setBalance((rb == -sign) ? sign
                                                                    : 0);
                        l.getUpdatedNode().setBalance((rb == sign) ? -sign
                                                                   : 0);
                        r.getUpdatedNode().setBalance(0);
                    }

                    return;
            }

            x = x.getUpdatedNode();

            if (x.isRoot()) {
                return;
            }

            isleft = x.isFromLeft();
            x      = x.getParent();
        }
    }

    /**
     * Delete a node from the index
     */
    void delete(Session session, Node x) throws HsqlException {

        if (x == null) {
            return;
        }

        for (IndexRowIterator it = updatableIterators.next;
                it != updatableIterators; it = it.next) {
            it.updateForDelete(x);
        }

        Node n;

        if (x.getLeft() == null) {
            n = x.getRight();
        } else if (x.getRight() == null) {
            n = x.getLeft();
        } else {
            Node d = x;

            x = x.getLeft();

/*
            // todo: this can be improved

            while (x.getRight() != null) {
                if (Trace.STOP) {
                    Trace.stop();
                }

                x = x.getRight();
            }
*/
            for (Node temp = x; (temp = temp.getRight()) != null; ) {
                x = temp;
            }

            // x will be replaced with n later
            n = x.getLeft();

            // swap d and x
            int b = x.getBalance();

            x = x.getUpdatedNode();

            x.setBalance(d.getBalance());

            d = d.getUpdatedNode();

            d.setBalance(b);

            // set x.parent
            Node xp = x.getParent();
            Node dp = d.getParent();

            x = x.getUpdatedNode();

            if (d.isRoot()) {
                setRoot(session, x);
            }

            x.setParent(dp);

            if (dp != null) {
                dp = dp.getUpdatedNode();

                if (dp.isRight(d)) {
                    dp.setRight(x);
                } else {
                    dp.setLeft(x);
                }
            }

            // relink d.parent, x.left, x.right
            d = d.getUpdatedNode();

            if (d.equals(xp)) {
                d.setParent(x);

                if (d.isLeft(x)) {
                    x = x.getUpdatedNode();

                    x.setLeft(d);

                    Node dr = d.getRight();

                    x = x.getUpdatedNode();

                    x.setRight(dr);
                } else {
                    x.setRight(d);

                    Node dl = d.getLeft();

                    x = x.getUpdatedNode();

                    x.setLeft(dl);
                }
            } else {
                d.setParent(xp);

                xp = xp.getUpdatedNode();

                xp.setRight(d);

                Node dl = d.getLeft();
                Node dr = d.getRight();

                x = x.getUpdatedNode();

                x.setLeft(dl);
                x.setRight(dr);
            }

            x.getRight().setParent(x);
            x.getLeft().setParent(x);

            // set d.left, d.right
            d = d.getUpdatedNode();

            d.setLeft(n);

            if (n != null) {
                n = n.getUpdatedNode();

                n.setParent(d);
            }

            d = d.getUpdatedNode();

            d.setRight(null);

            x = d;
        }

        boolean isleft = x.isFromLeft();

        replace(session, x, n);

        n = x.getParent();
        x = x.getUpdatedNode();

        x.delete();

        while (n != null) {
            x = n;

            int sign = isleft ? 1
                              : -1;

            x = x.getUpdatedNode();

            switch (x.getBalance() * sign) {

                case -1 :
                    x.setBalance(0);
                    break;

                case 0 :
                    x.setBalance(sign);

                    return;

                case 1 :
                    Node r = child(x, !isleft);
                    int  b = r.getBalance();

                    if (b * sign >= 0) {
                        replace(session, x, r);
                        set(x, !isleft, child(r, isleft));
                        set(r, isleft, x);

                        if (b == 0) {
                            x = x.getUpdatedNode();

                            x.setBalance(sign);

                            r = r.getUpdatedNode();

                            r.setBalance(-sign);

                            return;
                        }

                        x = x.getUpdatedNode();

                        x.setBalance(0);

                        r = r.getUpdatedNode();

                        r.setBalance(0);

                        x = r;
                    } else {
                        Node l = child(r, isleft);

                        replace(session, x, l);

                        l = l.getUpdatedNode();
                        b = l.getBalance();

                        set(r, isleft, child(l, !isleft));
                        set(l, !isleft, r);
                        set(x, !isleft, child(l, isleft));
                        set(l, isleft, x);

                        x = x.getUpdatedNode();

                        x.setBalance((b == sign) ? -sign
                                                 : 0);

                        r = r.getUpdatedNode();

                        r.setBalance((b == -sign) ? sign
                                                  : 0);

                        l = l.getUpdatedNode();

                        l.setBalance(0);

                        x = l;
                    }
            }

            isleft = x.isFromLeft();
            n      = x.getParent();
        }
    }

    RowIterator findFirstRow(Session session, Object[] rowdata,
                             int[] rowColMap) throws HsqlException {

        Node node = findNotNull(session, rowdata, rowColMap, true);

        return getIterator(session, node);
    }

    RowIterator findFirstRowForDelete(Session session, Object[] rowdata,
                                      int[] rowColMap) throws HsqlException {

        Node node           = findNotNull(session, rowdata, rowColMap, true);
        IndexRowIterator it = getIterator(session, node);

        if (node != null) {
            updatableIterators.link(it);
        }

        return it;
    }

    /**
     * Finds an existing row
     */
    Row findRow(Session session, Row row) throws HsqlException {

        Node node = search(session, row);

        return node == null ? null
                            : node.getRow();
    }

    boolean exists(Session session, Object[] rowdata,
                   int[] rowColMap) throws HsqlException {
        return findNotNull(session, rowdata, rowColMap, true) != null;
    }

    RowIterator emptyIterator() {
        return emptyIterator;
    }

    /**
     * Finds a foreign key referencing rows (in child table)
     *
     * @param rowdata array containing data for the index columns
     * @param rowColMap map of the data to columns
     * @param first true if the first matching node is required, false if any node
     * @return matching node or null
     * @throws HsqlException
     */
    private Node findNotNull(Session session, Object[] rowdata,
                             int[] rowColMap,
                             boolean first) throws HsqlException {

        Node x = getRoot(session);
        Node n;
        Node result = null;

        if (isNull(rowdata, rowColMap)) {
            return null;
        }

        while (x != null) {
            int i = this.compareRowNonUnique(session, rowdata, rowColMap,
                                             x.getData());

            if (i == 0) {
                if (first == false) {
                    result = x;

                    break;
                } else if (result == x) {
                    break;
                }

                result = x;
                n      = x.getLeft();
            } else if (i > 0) {
                n = x.getRight();
            } else {
                n = x.getLeft();
            }

            if (n == null) {
                break;
            }

            x = n;
        }

        return result;
    }

    /**
     * Finds any row that matches the rowdata. Use rowColMap to map index
     * columns to rowdata. Limit to visible columns of data.
     *
     * @param rowdata array containing data for the index columns
     * @param rowColMap map of the data to columns
     * @return node matching node
     * @throws HsqlException
     */
/*
    Node find(Object[] rowdata, int[] rowColMap) throws HsqlException {

        Node x = root;

        while (x != null) {
            int c = compareRowNonUnique(rowdata, rowColMap, x.getData());

            if (c == 0) {
                return x;
            } else if (c < 0) {
                x = x.getLeft();
            } else {
                x = x.getRight();
            }
        }

        return null;
    }
*/

    /**
     * Determines if a table row has a null column for any of the columns given
     * in the rowColMap array.
     */
    static boolean isNull(Object[] row, int[] rowColMap) {

        int count = rowColMap.length;

        for (int i = 0; i < count; i++) {
            if (row[rowColMap[i]] == null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if a table row has a null column for any of the indexed
     * columns.
     */
    boolean isNull(Object[] row) {

        int count = colIndex.length;

        for (int i = 0; i < count; i++) {
            int j = colIndex[i];

            if (row[j] == null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return the first node equal to the rowdata object. Use visible columns
     * only. The rowdata has the same column mapping as this table.
     *
     * @param rowdata array containing table row data
     * @return iterator
     * @throws HsqlException
     */
    RowIterator findFirstRow(Session session,
                             Object[] rowdata) throws HsqlException {

        Node    x      = getRoot(session);
        Node    found  = null;
        boolean unique = isUnique &&!isNull(rowdata);

        while (x != null) {
            int c = compareRowNonUnique(session, rowdata, colIndex,
                                        x.getData());

            if (c == 0) {
                found = x;

                if (unique) {
                    break;
                }

                x = x.getLeft();
            } else if (c < 0) {
                x = x.getLeft();
            } else {
                x = x.getRight();
            }
        }

        return getIterator(session, found);
    }

    /**
     * Finds the first node that is larger or equal to the given one based
     * on the first column of the index only.
     *
     * @param value value to match
     * @param compare comparison Expression type
     *
     * @return iterator
     *
     * @throws HsqlException
     */
    RowIterator findFirstRow(Session session, Object value,
                             int compare) throws HsqlException {

        boolean isEqual = compare == Expression.EQUAL
                          || compare == Expression.IS_NULL;
        Node x     = getRoot(session);
        int  iTest = 1;

        if (compare == Expression.BIGGER) {
            iTest = 0;
        }

        if (value == null &&!isEqual) {
            return emptyIterator;
        }

/*
        // this method returns the correct node only with the following conditions
        boolean check = compare == Expression.BIGGER
                        || compare == Expression.EQUAL
                        || compare == Expression.BIGGER_EQUAL;

        if (!check) {
            Trace.doAssert(false, "Index.findFirst");
        }
*/
        while (x != null) {
            boolean t =
                Column.compare(
                    collation, value, x.getData()[colIndex[0]],
                    colTypes[0]) >= iTest;

            if (t) {
                Node r = x.getRight();

                if (r == null) {
                    break;
                }

                x = r;
            } else {
                Node l = x.getLeft();

                if (l == null) {
                    break;
                }

                x = l;
            }
        }

/*
        while (x != null
                && Column.compare(value, x.getData()[colIndex_0], colType_0)
                   >= iTest) {
            x = next(x);
        }
*/
        while (x != null) {
            Object colvalue = x.getData()[colIndex[0]];
            int result = Column.compare(collation, value, colvalue,
                                        colTypes[0]);

            if (result >= iTest) {
                x = next(x);
            } else {
                if (isEqual) {
                    if (result != 0) {
                        x = null;
                    }
                } else if (colvalue == null) {
                    x = next(x);

                    continue;
                }

                break;
            }
        }

        return getIterator(session, x);
    }

    /**
     * Finds the first node where the data is not null.
     *
     * @return iterator
     *
     * @throws HsqlException
     */
    RowIterator findFirstRowNotNull(Session session) throws HsqlException {

        Node x = getRoot(session);

        while (x != null) {
            boolean t = Column.compare(
                collation, null, x.getData()[colIndex[0]], colTypes[0]) >= 0;

            if (t) {
                Node r = x.getRight();

                if (r == null) {
                    break;
                }

                x = r;
            } else {
                Node l = x.getLeft();

                if (l == null) {
                    break;
                }

                x = l;
            }
        }

        while (x != null) {
            Object colvalue = x.getData()[colIndex[0]];

            if (colvalue == null) {
                x = next(x);
            } else {
                break;
            }
        }

        return getIterator(session, x);
    }

    /**
     * Returns the row for the first node of the index
     *
     * @return Iterator for first row
     *
     * @throws HsqlException
     */
    RowIterator firstRow(Session session) throws HsqlException {

        depth = 0;

        Node x = getRoot(session);
        Node l = x;

        while (l != null) {
            x = l;
            l = x.getLeft();

            depth++;
        }

        return getIterator(session, x);
    }

    /**
     * Returns the row for the last node of the index
     *
     * @return last row
     *
     * @throws HsqlException
     */
    Row lastRow(Session session) throws HsqlException {

        Node x = getRoot(session);
        Node l = x;

        while (l != null) {
            x = l;
            l = x.getRight();
        }

        return x == null ? null
                         : x.getRow();
    }

    /**
     * Returns the node after the given one
     *
     * @param x node
     *
     * @return next node
     *
     * @throws HsqlException
     */
    Node next(Node x) throws HsqlException {

        if (x == null) {
            return null;
        }

        Node r = x.getRight();

        if (r != null) {
            x = r;

            Node l = x.getLeft();

            while (l != null) {
                x = l;
                l = x.getLeft();
            }

            return x;
        }

        Node ch = x;

        x = x.getParent();

        while (x != null && ch.equals(x.getRight())) {
            ch = x;
            x  = x.getParent();
        }

        return x;
    }

    /**
     * Returns either child node
     *
     * @param x node
     * @param isleft boolean
     *
     * @return child node
     *
     * @throws HsqlException
     */
    private Node child(Node x, boolean isleft) throws HsqlException {
        return isleft ? x.getLeft()
                      : x.getRight();
    }

    /**
     * Replace two nodes
     *
     * @param x node
     * @param n node
     *
     * @throws HsqlException
     */
    private void replace(Session session, Node x,
                         Node n) throws HsqlException {

        if (x.isRoot()) {
            if (n != null) {
                n = n.getUpdatedNode();

                n.setParent(null);
            }

            setRoot(session, n);
        } else {
            set(x.getParent(), x.isFromLeft(), n);
        }
    }

    /**
     * Set a node as child of another
     *
     * @param x parent node
     * @param isleft boolean
     * @param n child node
     *
     * @throws HsqlException
     */
    private void set(Node x, boolean isleft, Node n) throws HsqlException {

        x = x.getUpdatedNode();

        if (isleft) {
            x.setLeft(n);
        } else {
            x.setRight(n);
        }

        if (n != null) {
            n = n.getUpdatedNode();

            n.setParent(x);
        }
    }

    /**
     * Find a node with matching data
     *
     * @param row row data
     *
     * @return matching node
     *
     * @throws HsqlException
     */
    private Node search(Session session, Row row) throws HsqlException {

        Object[] d = row.getData();
        Node     x = getRoot(session);

        while (x != null) {
            int c = compareRowForInsert(session, row, x.getRow());

            if (c == 0) {
                return x;
            } else if (c < 0) {
                x = x.getLeft();
            } else {
                x = x.getRight();
            }
        }

        return null;
    }

    /**
     * Compares two table rows based on the columns of this index. The rowColMap
     * parameter specifies which columns of the other table are to be compared
     * with the colIndex columns of this index. The rowColMap can cover all
     * or only some columns of this index.
     *
     * @param a row from another table
     * @param rowColMap column indexes in the other table
     * @param b a full row in this table
     *
     * @return comparison result, -1,0,+1
     * @throws HsqlException
     */
    int compareRowNonUnique(Session session, Object[] a, int[] rowColMap,
                            Object[] b) throws HsqlException {

        int i = Column.compare(collation, a[rowColMap[0]], b[colIndex[0]],
                               colTypes[0]);

        if (i != 0) {
            return i;
        }

        int fieldcount = rowColMap.length;

        for (int j = 1; j < fieldcount; j++) {
            i = Column.compare(collation, a[rowColMap[j]], b[colIndex[j]],
                               colTypes[j]);

            if (i != 0) {
                return i;
            }
        }

        return 0;
    }

    /**
     * compares two full table rows based on a set of columns
     *
     * @param a a full row
     * @param b a full row
     * @param cols array of column indexes to compare
     *
     * @return comparison result, -1,0,+1
     * @throws HsqlException
     */
    static int compareRows(Session session, Object[] a, Object[] b,
                           int[] cols, int[] coltypes) throws HsqlException {

        int fieldcount = cols.length;

        for (int j = 0; j < fieldcount; j++) {
            int i = Column.compare(session.database.collation, a[cols[j]],
                                   b[cols[j]], coltypes[cols[j]]);

            if (i != 0) {
                return i;
            }
        }

        return 0;
    }

    /**
     * Compare two rows of the table for inserting rows into unique indexes
     *
     * @param a data
     * @param b data
     *
     * @return comparison result, -1,0,+1
     *
     * @throws HsqlException
     */
    private int compareRowForInsert(Session session, Row newRow,
                                    Row existingRow) throws HsqlException {

        Object[] a       = newRow.getData();
        Object[] b       = existingRow.getData();
        int      j       = 0;
        boolean  hasNull = false;

        for (; j < colIndex.length; j++) {
            Object currentvalue = a[colIndex[j]];
            int i = Column.compare(collation, currentvalue, b[colIndex[j]],
                                   colTypes[j]);

            if (i != 0) {
                return i;
            }

            if (currentvalue == null) {
                hasNull = true;
            }
        }

        if (isUnique &&!useRowId &&!hasNull) {
            return 0;
        }

        for (j = 0; j < pkCols.length; j++) {
            Object currentvalue = a[pkCols[j]];
            int i = Column.compare(collation, currentvalue, b[pkCols[j]],
                                   pkTypes[j]);

            if (i != 0) {
                return i;
            }
        }

        if (useRowId) {
            int difference = newRow.getPos() - existingRow.getPos();

            if (difference < 0) {
                difference = -1;
            } else if (difference > 0) {
                difference = 1;
            }

            return difference;
        }

        return 0;
    }

    /**
     * Returns a value indicating the order of different types of index in
     * the list of indexes for a table. The position of the groups of Indexes
     * in the list in ascending order is as follows:
     *
     * primary key index
     * unique constraint indexes
     * autogenerated foreign key indexes for FK's that reference this table or
     *  tables created before this table
     * user created indexes (CREATE INDEX)
     * autogenerated foreign key indexes for FK's that reference tables created
     *  after this table
     *
     * Among a group of indexes, the order is based on the order of creation
     * of the index.
     *
     * @return ordinal value
     */
    int getIndexOrderValue() {

        int value = 0;

        if (isConstraint) {
            return isForward ? 4
                             : isUnique ? 0
                                        : 1;
        } else {
            return 2;
        }
    }

    private IndexRowIterator getIterator(Session session, Node x) {

        if (x == null) {
            return emptyIterator;
        } else {
            IndexRowIterator it = new IndexRowIterator(session, this, x);

            return it;
        }
    }

    static class IndexRowIterator implements RowIterator {

        Session                    session;
        Index                      index;
        Node                       nextnode;
        protected IndexRowIterator last;
        protected IndexRowIterator next;

        /**
         * When session == null, rows from all sessions are returned
         */
        private IndexRowIterator(Session session, Index index, Node node) {

            if (index == null) {
                return;
            }

            this.session  = session;
            this.index    = index;
            this.nextnode = node;
        }

        public boolean hasNext() {
            return nextnode != null;
        }

        public Row next() {

            if (hasNext()) {
                try {
                    Row row = nextnode.getRow();

                    nextnode = index.next(nextnode);

                    return row;
                } catch (Exception e) {
                    throw new NoSuchElementException(e.getMessage());
                }
            } else {
                return null;
            }
        }

        void updateForDelete(Node node) {

            try {
                if (node.equals(nextnode)) {
                    nextnode = index.next(node);
                }
            } catch (Exception e) {}
        }

        void link(IndexRowIterator other) {

            other.next = next;
            other.last = this;
            next.last  = other;
            next       = other;
        }

        public void release() {

            if (last != null) {
                last.next = next;
            }

            if (next != null) {
                next.last = last;
            }
        }
    }
}
