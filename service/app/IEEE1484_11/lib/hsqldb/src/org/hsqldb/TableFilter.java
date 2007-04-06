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

import org.hsqldb.index.RowIterator;
import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.lib.HashMappedList;

// fredt@users 20030813 - patch 1.7.2 - fix for column comparison within same table bugs #572075 and 722443
// fredt@users 20031012 - patch 1.7.2 - better OUTER JOIN implementation
// fredt@users 20031026 - patch 1.7.2 - more efficient findfirst - especially for multi-column equijoins
// implemented optimisations similart to patch 465542 by hjbush@users

/**
 * This class iterates over table rows to select the rows that fulfil join
 * or other conditions. It uses indexes if they are availabe.
 *
 * Extended in successive versions of HSQLDB.
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.8.0
 * @since Hypersonic SQL
 */
final class TableFilter {

    static final int CONDITION_NONE      = -1;     // not a condition expression
    static final int CONDITION_UNORDERED = 0;      // not candidate for eStart or eEnd
    static final int   CONDITION_START_END = 1;    // candidate for eStart and eEnd
    static final int   CONDITION_START     = 2;    // candidate for eStart
    static final int   CONDITION_END       = 3;    // candidate for eEnd
    static final int   CONDITION_OUTER     = 4;    // add to this
    Table              filterTable;
    private String     tableAlias;
    HashMappedList     columnAliases;
    Index              filterIndex;
    private Object[]   emptyData;
    boolean[]          usedColumns;
    private Expression eStart, eEnd;

    //
    Expression eAnd;

    //
    boolean      isOuterJoin;                      // table joined with OUTER JOIN
    boolean      isAssigned;                       // conditions have been assigned to this
    boolean      isMultiFindFirst;                 // findFirst() uses multi-column index
    Expression[] findFirstExpressions;             // expressions for column values

    //
    private RowIterator it;
    Object[]            currentData;
    Row                 currentRow;

    //
    Object[] currentJoinData;

    // addendum to the result of findFirst() and next() with isOuterJoin==true
    // when the result is false, it indicates if a non-join condition caused the failure
    boolean nonJoinIsNull;

    // indicates current data is empty data produced for an outer join
    boolean isCurrentOuter;

    /**
     * Constructor declaration
     *
     *
     * @param t
     * @param alias
     * @param outerjoin
     */
    TableFilter(Table t, String alias, HashMappedList columnList,
                boolean outerjoin) {

        filterTable   = t;
        tableAlias    = alias == null ? t.getName().name
                                      : alias;
        columnAliases = columnList;
        isOuterJoin   = outerjoin;
        emptyData     = filterTable.getEmptyRowData();
        usedColumns   = filterTable.getNewColumnCheckList();
    }

    /**
     * Returns the alias or the table name.
     * Never returns null;
     * @return
     */
    String getName() {
        return tableAlias;
    }

    /**
     * Retrieves this object's filter Table object.
     *
     * @return this object's filter Table object
     */
    Table getTable() {
        return filterTable;
    }

    /**
     * Retrieves a CONDITION_XXX code indicating how a condition
     * expression can be used for a TableFilter.
     *
     * @param exprType an expression type code
     * @return
     */
    static int getConditionType(Expression e) {

        int exprType = e.getType();

        switch (exprType) {

            case Expression.NOT_EQUAL :
            case Expression.LIKE :
                return CONDITION_UNORDERED;

            case Expression.IN : {
                return e.isQueryCorrelated ? CONDITION_NONE
                                           : CONDITION_UNORDERED;
            }
            case Expression.IS_NULL :
            case Expression.EQUAL : {
                return CONDITION_START_END;
            }
            case Expression.BIGGER :
            case Expression.BIGGER_EQUAL : {
                return CONDITION_START;
            }
            case Expression.SMALLER :
            case Expression.SMALLER_EQUAL : {
                return CONDITION_END;
            }
            default : {

                // not a condition so forget it
                return CONDITION_NONE;
            }
        }
    }

    // TODO: Optimize
    //
    // The current way always chooses eStart, eEnd conditions
    // using first encountered eligible index
    //
    // We should check if current index offers better selectivity/access
    // path than previously assigned iIndex.
    //
    // EXAMPLE 1:
    //
    // CREATE TABLE t (c1 int, c2 int primary key)
    // CREATE INDEX I1 ON t(c1)
    // SELECT
    //      *
    // FROM
    //      t
    // WHERE
    //     c1 = | < | <= | >= | > ...
    // AND
    //     c2 = | < | <= | >= | > ...
    //
    // currently always chooses iIndex / condition (c1/I1), over
    // index / condition (c2/pk), whereas index / condition (c2/pk)
    // may well be better, especially if condition on c2 is equality
    // (condition_start_end) and conditionon(s) on c1 involve range
    // (condition_start, condition_end, or some composite).
    //
    // Currently, the developer/client software must somehow know facts
    // both about the table, the query and the way HSQLDB forms its
    // plans and, based on this knowlege, perhaps decide to reverse
    // order by explicitly issuing instead:
    //
    // SELECT
    //      *
    // FROM
    //      t
    // WHERE
    //     c2 = | < | <= | >= | > ...
    // AND
    //     c1 = | < | <= | >= | > ...
    //
    // to get optimal index choice.
    //
    // The same thing applies to and is even worse for joins.
    //
    // Consider the following (highly artificial, but easy to
    // understand) case:
    //
    // CREATE TABLE T1(ID INTEGER PRIMARY KEY, C1 INTEGER)
    // CREATE INDEX I1 ON T1(C1)
    // CREATE TABLE T2(ID INTEGER PRIMARY KEY, C1 INTEGER)
    // CREATE INDEX I2 ON T2(C1)
    //
    // select * from t1, t2 where t1.c1 = t2.c1 and t1.id = t2.id
    //
    // Consider the worst value distribution where t1 and t2 are both
    // 10,000 rows, c1 selectivity is nil (all values are identical)
    // for both tables, and, say, id values span the range 0..9999
    // for both tables.
    //
    // Then time to completion on 500 MHz Athlon testbed using memory
    // tables is:
    //
    // 10000 row(s) in 309114 ms
    //
    // whereas for:
    //
    // select * from t1, t2 where t1.id = t2.id and t1.c1 = t2.c1
    //
    // time to completion is:
    //
    // 10000 row(s) in 471 ms
    //
    // Hence, the unoptimized query takes 656 times as long as the
    // optimized one!!!
    //
    // EXAMPLE 2:
    //
    // If there are, say, two non-unique candidate indexes,
    // and some range or equality predicates against
    // them, preference should be given to the one with
    // better selectivity (if the total row count of the
    // table is large, otherwise the overhead of making
    // the choice is probably large w.r.t. any possible
    // savings).  Might require maintaining some basic
    // statistics or performing appropriate index probes
    // at the time the plan is being generated.

    /**
     * Chooses certain query conditions and assigns a copy of them to this
     * filter. The original condition is set to Expression.TRUE once assigned.
     *
     * @param condition
     *
     * @throws HsqlException
     */
    void setConditions(Session session,
                       Expression condition) throws HsqlException {

        setCondition(session, condition);

        if (filterIndex == null) {
            filterIndex = filterTable.getPrimaryIndex();
        }

        if (filterIndex.getVisibleColumns() == 1 || eStart == null
                || eAnd == null || eStart.exprType != Expression.EQUAL) {
            return;
        }

        boolean[]    check    = filterTable.getNewColumnCheckList();
        Expression[] expr     = new Expression[check.length];
        int          colindex = eStart.getArg().getColumnNr();

        check[colindex] = true;
        expr[colindex]  = eStart.getArg2();

        eAnd.getEquiJoinColumns(this, check, expr);

        if (ArrayUtil.containsAllTrueElements(check, filterIndex.colCheck)) {
            isMultiFindFirst     = true;
            findFirstExpressions = expr;
        }
    }

    private void setCondition(Session session,
                              Expression e) throws HsqlException {

        int        type = e.getType();
        Expression e1   = e.getArg();
        Expression e2   = e.getArg2();

        isAssigned = true;

        if (type == Expression.AND) {
            setCondition(session, e1);
            setCondition(session, e2);

            return;
        }

        if (type == Expression.OR && isOuterJoin && e.isInJoin
                && e.outerFilter == this) {
            addAndCondition(e);
            e.setTrue();

            return;
        }

        int conditionType = getConditionType(e);

        if (conditionType == CONDITION_NONE) {

            // not a condition expression
            return;
        }

// fredt@users 20030813 - patch 1.7.2 - fix for column comparison within same table bugs #572075 and 722443
        if (e1.getFilter() == this && e2.getFilter() == this) {
            conditionType = CONDITION_UNORDERED;
        } else if (e1.getFilter() == this) {
            if (!e.isInJoin && isOuterJoin) {

                // do not use a where condition on the second table in outer joins
                return;
            }

            // ok include this
        } else if ((e2.getFilter() == this)
                   && (conditionType != CONDITION_UNORDERED)) {

            // swap and try again to allow index usage
            e.swapCondition();
            setCondition(session, e);

            return;
        } else if (e1.outerFilter == this) {

            // fredt - this test is last to allow swapping the terms above
            conditionType = CONDITION_OUTER;
        } else {

            // unrelated: don't include
            return;
        }

//        Trace.doAssert(e1.getFilter() == this, "setCondition");
        if (!e2.isResolved()) {
            return;
        }

        // fredt - condition defined in outer but not this one
        if (e1.outerFilter != null && e1.outerFilter != this) {
            return;
        }

        if (conditionType == CONDITION_UNORDERED) {
            addAndCondition(e);

            return;
        }

        if (conditionType == CONDITION_OUTER) {
            addAndCondition(e);

            return;
        }

        int   i     = e1.getColumnNr();
        Index index = filterTable.getIndexForColumn(session, i);

        if (index == null || (filterIndex != index && filterIndex != null)) {
            addAndCondition(e);

            return;
        }

        filterIndex = index;

        switch (conditionType) {

            case CONDITION_START_END : {

                // candidate for both start and end
                if ((eStart != null) || (eEnd != null)) {
                    addAndCondition(e);

                    return;
                }

                eStart = new Expression(e);
                eEnd   = eStart;

                break;
            }
            case CONDITION_START : {

                // candidate for start
                if (eStart != null) {
                    addAndCondition(e);

                    return;
                }

                eStart = new Expression(e);

                break;
            }
            case CONDITION_END : {

                // candidate for end
                if (eEnd != null) {
                    addAndCondition(e);

                    return;
                }

                eEnd = new Expression(e);

                break;
            }
        }

        e.setTrue();
    }

    /**
     * Finds the first row in the table (using an index if there is one) and
     * checks it against the eEnd (range) and eAnd (other conditions)
     * Expression objects. (fredt)
     *
     * @return true if first row was found, else false
     */
    boolean findFirst(Session session) throws HsqlException {

        nonJoinIsNull  = false;
        isCurrentOuter = false;

        if (filterIndex == null) {
            filterIndex = filterTable.getPrimaryIndex();
        }

        if (isMultiFindFirst) {
            boolean convertible = true;
            int[]   types       = filterTable.getColumnTypes();

            currentJoinData = filterTable.getEmptyRowData();

            for (int i = 0; i < findFirstExpressions.length; i++) {
                Expression e = findFirstExpressions[i];

                if (e != null) {
                    Object value = e.getValue(session);

                    if (Column.compareToTypeRange(value, types[i]) != 0) {
                        convertible = false;

                        break;
                    }

                    value = Column.convertObject(value, types[i]);
                    currentJoinData[i] = e.getValue(session, types[i]);
                }
            }

            it = convertible
                 ? filterIndex.findFirstRow(session, currentJoinData)
                 : filterIndex.emptyIterator();

            if (!it.hasNext()) {
                ArrayUtil.clearArray(ArrayUtil.CLASS_CODE_OBJECT,
                                     currentJoinData, 0,
                                     currentJoinData.length);
            }
        } else if (eStart == null) {
            it = eEnd == null ? filterIndex.firstRow(session)
                              : filterIndex.findFirstRowNotNull(session);
        } else {
            Object value      = eStart.getArg2().getValue(session);
            int    valuetype  = eStart.getArg2().getDataType();
            int    targettype = eStart.getArg().getDataType();

            it = getFirstIterator(session, eStart.getType(), value,
                                  valuetype, filterIndex, targettype);
        }

        while (true) {
            currentRow = it.next();

            if (currentRow == null) {
                break;
            }

            currentData = currentRow.getData();

            if (!(eEnd == null || eEnd.testCondition(session))) {
                break;
            }

            if (eAnd == null || eAnd.testCondition(session)) {
                return true;
            }
        }

        currentRow  = null;
        currentData = emptyData;

        return false;
    }

    static RowIterator getFirstIterator(Session session, int eType,
                                        Object value, int valueType,
                                        Index index,
                                        int targetType) throws HsqlException {

        RowIterator it;
        int         range = 0;

        if (targetType != valueType) {
            range = Column.compareToTypeRange(value, targetType);
        }

        if (range == 0) {
            value = Column.convertObject(value, targetType);
            it    = index.findFirstRow(session, value, eType);
        } else {
            switch (eType) {

                case Expression.BIGGER_EQUAL :
                case Expression.BIGGER :
                    if (range < 0) {
                        it = index.findFirstRowNotNull(session);

                        break;
                    }
                default :
                    it = index.emptyIterator();
            }
        }

        return it;
    }

    /**
     * Advances to the next available value. <p>
     *
     * @return true if a next value is available upon exit
     *
     * @throws HsqlException if a database access error occurs
     */
    boolean next(Session session) throws HsqlException {

        boolean result = false;

        nonJoinIsNull  = false;
        isCurrentOuter = false;

        while (true) {
            currentRow = it.next();

            if (currentRow == null) {
                break;
            }

            currentData = currentRow.getData();

            if (!(eEnd == null || eEnd.testCondition(session))) {
                break;
            }

            if (eAnd == null || eAnd.testCondition(session)) {
                result = true;

                break;
            }
        }

        if (result) {
            return true;
        }

        currentRow  = null;
        currentData = emptyData;

        return false;
    }

    boolean nextOuter(Session session) throws HsqlException {

        nonJoinIsNull  = false;
        isCurrentOuter = true;
        currentData    = emptyData;
        currentRow     = null;

        return eAnd == null || (eAnd.getFilter() != this && eAnd.isInJoin)
               || eAnd.testCondition(session);
    }

    /**
     * Forms a new conjunction using the given condition and this filter's
     * pre-existing AND condition, or sets the given condition as this filter's
     * AND condition when there is no such pre-exisiting object.
     *
     * @param e the condition to add
     */
    private void addAndCondition(Expression e) {

        Expression e2 = new Expression(e);

        if (eAnd == null) {
            eAnd = e2;
        } else {
            Expression and = new Expression(Expression.AND, eAnd, e2);

            eAnd = and;
        }

        e.setTrue();
    }

    /**
     * Removes reference to Index to avoid possible memory leaks after alter
     * table or drop index
     */
    void setAsCheckFilter() {
        filterIndex = null;
    }

// boucheb@users 20030415 - added for debugging support

    /**
     * Retreives a String representation of this obejct. <p>
     *
     * The returned String describes this object's table, alias
     * access mode, index, join mode, Start, End and And conditions.
     *
     * @return a String representation of this object
     */
    public String describe(Session session) {

        StringBuffer sb;
        String       temp;
        Index        index;
        Index        primaryIndex;
        int[]        primaryKey;
        boolean      hidden;
        boolean      fullScan;

        sb           = new StringBuffer();
        index        = filterIndex;
        primaryIndex = filterTable.getPrimaryIndex();
        primaryKey   = filterTable.getPrimaryKey();
        hidden       = false;
        fullScan     = (eStart == null && eEnd == null);

        if (index == null) {
            index = primaryIndex;
        }

        if (index == primaryIndex && primaryKey.length == 0) {
            hidden   = true;
            fullScan = true;
        }

        sb.append(super.toString()).append('\n');
        sb.append("table=[").append(filterTable.getName().name).append("]\n");
        sb.append("alias=[").append(tableAlias).append("]\n");
        sb.append("access=[").append(fullScan ? "FULL SCAN"
                                              : "INDEX PRED").append("]\n");
        sb.append("index=[");
        sb.append(index == null ? "NONE"
                                : index.getName() == null ? "UNNAMED"
                                                          : index.getName()
                                                          .name);
        sb.append(hidden ? "[HIDDEN]]\n"
                         : "]\n");
        sb.append("isOuterJoin=[").append(isOuterJoin).append("]\n");

        temp = eStart == null ? "null"
                              : eStart.describe(session);

        sb.append("eStart=[").append(temp).append("]\n");

        temp = eEnd == null ? "null"
                            : eEnd.describe(session);

        sb.append("eEnd=[").append(temp).append("]\n");

        temp = eAnd == null ? "null"
                            : eAnd.describe(session);

        sb.append("eAnd=[").append(temp).append("]");

        return sb.toString();
    }
}
