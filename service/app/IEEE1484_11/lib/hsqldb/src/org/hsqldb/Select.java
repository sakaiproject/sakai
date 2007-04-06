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

import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.lib.HashMap;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.Iterator;

// fredt@users 20010701 - patch 1.6.1 by hybris
// basic implementation of LIMIT n m
// fredt@users 20020130 - patch 471710 by fredt - LIMIT rewritten
// for SELECT LIMIT n m DISTINCT
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// type and logging attributes of sIntotable
// fredt@users 20020230 - patch 495938 by johnhobs@users - GROUP BY order
// fred@users 20020522 - patch 1.7.0 - aggregate functions with DISTINCT
// rougier@users 20020522 - patch 552830 - COUNT(DISTINCT)
// fredt@users 20020804 - patch 580347 by dkkopp - view speedup
// tony_lai@users 20021020 - patch 1.7.2 - improved aggregates and HAVING
// boucherb@users 20030811 - patch 1.7.2 - prepared statement support
// fredt@users 20031012 - patch 1.7.2 - better OUTER JOIN implementation
// fredt@users 20031012 - patch 1.7.2 - SQL standard ORDER BY with UNION and other set queries
// fredt@users 200408xx - patch 1.7.2 - correct evaluation of the precedence of nested UNION and other set query

/**
 * The compiled representation of an SQL SELECT.
 *
 * Extended in successive versions of HSQLDB.
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.8.0
 * @since Hypersonic SQL
 */
class Select {

    boolean               isDistinctSelect;
    boolean               isAggregated;
    private boolean       isGrouped;
    private HashSet       groupColumnNames;
    TableFilter[]         tFilter;
    Expression            limitCondition;
    Expression            queryCondition;     // null means no condition
    Expression            havingCondition;    // null means none
    Expression[]          exprColumns;        // 'result', 'group' and 'order' columns
    int                   iResultLen;         // number of columns that are 'result'
    int                   iGroupLen;          // number of columns that are 'group'
    int                   iHavingLen;         // number of columns that are 'group'
    int                   iOrderLen;          // number of columns that are 'order'
    int[]                 sortOrder;
    int[]                 sortDirection;
    boolean               sortUnion;          // if true, sort the result of the full union
    HsqlName              sIntoTable;         // null means not select..into
    int                   intoType;
    Select[]              unionArray;         // only set in the first Select in a union chain
    int                   unionMaxDepth;      // max unionDepth in chain
    Select                unionSelect;        // null means no union select
    int                   unionType;
    int                   unionDepth;
    static final int      NOUNION   = 0,
                          UNION     = 1,
                          UNIONALL  = 2,
                          INTERSECT = 3,
                          EXCEPT    = 4;
    private boolean       simpleLimit;        // true if maxrows can be uses as is
    Result.ResultMetaData resultMetaData;

    /**
     * Experimental.
     *
     * Map the column aliases to expressions in order to resolve alias names
     * in WHERE clauses
     *
     */
    HashMap getColumnAliases() {

        HashMap aliasMap = new HashMap();

        for (int i = 0; i < iResultLen; i++) {
            String alias = exprColumns[i].getAlias();

            if (alias != null) {
                aliasMap.put(alias, exprColumns[i]);
            }
        }

        return aliasMap;
    }

    /**
     * Method declaration
     *
     *
     * @throws HsqlException
     */
    void resolve(Session session) throws HsqlException {

        resolveTables();
        resolveTypes(session);
        setFilterConditions(session);
    }

    /**
     * Method declaration
     *
     *
     * @throws HsqlException
     */
    private void resolveTables() throws HsqlException {

        // replace the aliases with expressions
        for (int i = iResultLen; i < exprColumns.length; i++) {
            if (exprColumns[i].getType() == Expression.COLUMN) {
                if (exprColumns[i].joinedTableColumnIndex == -1) {
                    boolean descending = exprColumns[i].isDescending();

                    exprColumns[i] =
                        exprColumns[i].getExpressionForAlias(exprColumns,
                            iResultLen);

                    if (descending) {
                        exprColumns[i].setDescending();
                    }
                }
            } else {
                exprColumns[i].replaceAliases(exprColumns, iResultLen);
            }
        }

        if (queryCondition != null) {
            queryCondition.replaceAliases(exprColumns, iResultLen);
        }

        int len = tFilter.length;

        for (int i = 0; i < len; i++) {
            resolveTables(tFilter[i]);
        }
    }

    /**
     * Converts the types of the columns in set operations to those in the first
     * Select.
     */
    void resolveUnionColumnTypes() throws HsqlException {

        if (unionSelect != null) {
            if (unionSelect.iResultLen != iResultLen) {
                throw Trace.error(Trace.COLUMN_COUNT_DOES_NOT_MATCH);
            }

            for (int i = 0; i < iResultLen; i++) {
                Expression e = exprColumns[i];

                if (!e.isTypeEqual(unionSelect.exprColumns[i])) {
                    unionSelect.exprColumns[i] =
                        new Expression(unionSelect.exprColumns[i],
                                       e.getDataType(), e.getColumnSize(),
                                       e.getColumnScale());
                }
            }
        }
    }

    /**
     * Sets the types of all the expressions that have so far resolved.
     *
     * @throws HsqlException
     */
    void resolveTypes(Session session) throws HsqlException {

        int len = exprColumns.length;

        for (int i = 0; i < len; i++) {
            exprColumns[i].resolveTypes(session);
        }

        if (queryCondition != null) {
            queryCondition.resolveTypes(session);
        }
    }

    void resolveTablesUnion(TableFilter f) throws HsqlException {

        if (unionArray == null) {
            resolveTables(f);
        } else {
            for (int i = 0; i < unionArray.length; i++) {
                unionArray[i].resolveTables(f);
            }
        }
    }

    /**
     * Resolves the tables for all the Expression in the Select object
     * if it is possible to do so with the given TableFilter.
     *
     * @param f
     *
     * @throws HsqlException
     */
    void resolveTables(TableFilter f) throws HsqlException {

        int len = exprColumns.length;

        for (int i = 0; i < len; i++) {
            exprColumns[i].resolveTables(f);
        }

        if (queryCondition != null) {
            queryCondition.resolveTables(f);
        }
    }

    private void setFilterConditions(Session session) throws HsqlException {

        if (queryCondition == null) {
            return;
        }

        for (int i = 0; i < tFilter.length; i++) {
            tFilter[i].setConditions(session, queryCondition);
        }
    }

    /**
     * Check all Expression have resolved. Return true or false as a result.
     * Throw if false and check parameter is true.
     *
     * @throws HsqlException
     */
    boolean checkResolved(boolean check) throws HsqlException {

        boolean result = true;
        int     len    = exprColumns.length;

        for (int i = 0; i < len; i++) {
            result = result && exprColumns[i].checkResolved(check);
        }

        if (queryCondition != null) {
            result = result && queryCondition.checkResolved(check);
        }

        if (havingCondition != null) {
            result = result && havingCondition.checkResolved(check);
        }

        for (int i = 0; i < tFilter.length; i++) {
            if (tFilter[i].filterIndex == null) {
                tFilter[i].filterIndex =
                    tFilter[i].filterTable.getPrimaryIndex();
            }
        }

        return result;
    }

    /**
     * Removes all the TableFilters from the Expressions.
     *
     * @throws HsqlException
     */
/*
    void removeFilters() throws HsqlException {

        int len = eColumn.length;

        for (int i = 0; i < len; i++) {
            eColumn[i].removeFilters();
        }

        if (eCondition != null) {
            eCondition.removeFilters();
        }
    }
*/

    /**
     * Returns a single value result or throws if the result has more than
     * one row with one value.
     *
     * @param type data type
     * @param session context
     * @return the single valued result
     * @throws HsqlException
     */
    Object getValue(Session session, int type) throws HsqlException {

        resolve(session);

        Result r    = getResult(session, 2);    // 2 records are required for test
        int    size = r.getSize();
        int    len  = r.getColumnCount();

        if (len == 1) {
            if (size == 0) {
                return null;
            } else if (size == 1) {
                Object o = r.rRoot.data[0];

                return r.metaData.colTypes[0] == type ? o
                                                      : Column.convertObject(
                                                      o, type);
            } else {
                throw Trace.error(Trace.CARDINALITY_VIOLATION_NO_SUBCLASS);
            }
        }

        HsqlException e =
            Trace.error(Trace.CARDINALITY_VIOLATION_NO_SUBCLASS);

        throw new HsqlInternalException(e);
    }

    /**
     * Resolves expressions and pepares thre metadata for the result.
     */
    void prepareResult(Session session) throws HsqlException {

        resolveAll(session, true);

        if (iGroupLen > 0) {    // has been set in Parser
            isGrouped        = true;
            groupColumnNames = new HashSet();

            for (int i = iResultLen; i < iResultLen + iGroupLen; i++) {

//              MarcH: this is wrong for a CASE WHEN statement in a SELECT CASE WHEN ...,<something aggregate> statement
//              collectColumnName collects no columns if exprColumns[i]'s expressiontype is Expression.CASEWHEN
//              collectAllColumnNames collects all columns used in the CASE WHEN statement
//              exprColumns[i].collectColumnName(groupColumnNames);
                exprColumns[i].collectAllColumnNames(groupColumnNames);
            }
        }

        int len = exprColumns.length;

        resultMetaData = new Result.ResultMetaData(len);

        Result.ResultMetaData rmd = resultMetaData;

        // tony_lai@users having
        int groupByStart = iResultLen;
        int groupByEnd   = groupByStart + iGroupLen;
        int orderByStart = groupByEnd + iHavingLen;
        int orderByEnd   = orderByStart + iOrderLen;

        for (int i = 0; i < len; i++) {
            Expression e = exprColumns[i];

            rmd.colTypes[i]  = e.getDataType();
            rmd.colSizes[i]  = e.getColumnSize();
            rmd.colScales[i] = e.getColumnScale();

            if (e.isAggregate()) {
                isAggregated = true;
            }

            if (i >= groupByStart && i < groupByEnd
                    &&!exprColumns[i].canBeInGroupBy()) {
                Trace.error(Trace.INVALID_GROUP_BY, exprColumns[i]);
            }

            if (i >= groupByEnd && i < groupByEnd + iHavingLen
                    &&!exprColumns[i].isConditional()) {
                Trace.error(Trace.INVALID_HAVING, exprColumns[i]);
            }

            if (i >= orderByStart && i < orderByEnd
                    &&!exprColumns[i].canBeInOrderBy()) {
                Trace.error(Trace.INVALID_ORDER_BY, exprColumns[i]);
            }

            if (i < iResultLen) {
                rmd.colLabels[i]     = e.getAlias();
                rmd.isLabelQuoted[i] = e.isAliasQuoted();
                rmd.schemaNames[i]   = e.getTableSchemaName();
                rmd.tableNames[i]    = e.getTableName();
                rmd.colNames[i]      = e.getColumnName();

                if (rmd.isTableColumn(i)) {
                    rmd.colNullable[i] = e.nullability;
                    rmd.isIdentity[i]  = e.isIdentity;
                    rmd.isWritable[i]  = e.isWritable;
                }

                rmd.classNames[i] = e.getValueClassName();
            }
        }

        // selected columns
        checkAggregateOrGroupByColumns(0, iResultLen);

        // having columns
        checkAggregateOrGroupByColumns(groupByEnd, orderByStart);

        // order by columns
        checkAggregateOrGroupByOrderColumns(orderByStart, orderByEnd);
        prepareSort();

        simpleLimit = (isDistinctSelect == false && isGrouped == false
                       && unionSelect == null && iOrderLen == 0);
    }

    /**
     * This is called externally only on the first Select in a UNION chain.
     */
    void prepareUnions() throws HsqlException {

        int count = 0;

        for (Select current = this; current != null;
                current = current.unionSelect, count++) {}

        if (count == 1) {
            if (unionDepth != 0) {
                throw Trace.error(Trace.MISSING_CLOSEBRACKET);
            }

            return;
        }

        unionArray = new Select[count];
        count      = 0;

        for (Select current = this; current != null;
                current = current.unionSelect, count++) {
            unionArray[count] = current;
            unionMaxDepth = current.unionDepth > unionMaxDepth
                            ? current.unionDepth
                            : unionMaxDepth;
        }

        if (unionArray[unionArray.length - 1].unionDepth != 0) {
            throw Trace.error(Trace.MISSING_CLOSEBRACKET);
        }
    }

    /**
     * Returns the result of executing this Select.
     *
     * @param maxrows may be 0 to indicate no limit on the number of rows.
     * Positive values limit the size of the result set.
     * @return the result of executing this Select
     * @throws HsqlException if a database access error occurs
     */
    Result getResult(Session session, int maxrows) throws HsqlException {

        Result r;

        if (unionArray == null) {
            r = getSingleResult(session, maxrows);
        } else {
            r = getResultMain(session);

            if (sortUnion) {
                sortResult(session, r);
                r.trimResult(getLimitStart(session),
                             getLimitCount(session, maxrows));
            }
        }

        // fredt - now there is no need for the sort and group columns
        r.setColumnCount(iResultLen);

        return r;
    }

    private Result getResultMain(Session session) throws HsqlException {

        Result[] unionResults = new Result[unionArray.length];

        for (int i = 0; i < unionArray.length; i++) {
            unionResults[i] = unionArray[i].getSingleResult(session,
                    Integer.MAX_VALUE);
        }

        for (int depth = unionMaxDepth; depth >= 0; depth--) {
            for (int pass = 0; pass < 2; pass++) {
                for (int i = 0; i < unionArray.length - 1; i++) {
                    if (unionResults[i] != null
                            && unionArray[i].unionDepth >= depth) {
                        if (pass == 0
                                && unionArray[i].unionType
                                   != Select.INTERSECT) {
                            continue;
                        }

                        if (pass == 1
                                && unionArray[i].unionType
                                   == Select.INTERSECT) {
                            continue;
                        }

                        int nextIndex = i + 1;

                        for (; nextIndex < unionArray.length; nextIndex++) {
                            if (unionResults[nextIndex] != null) {
                                break;
                            }
                        }

                        if (nextIndex == unionArray.length) {
                            break;
                        }

                        unionArray[i].mergeResults(session, unionResults[i],
                                                   unionResults[nextIndex]);

                        unionResults[nextIndex] = unionResults[i];
                        unionResults[i]         = null;
                    }
                }
            }
        }

        return unionResults[unionResults.length - 1];
    }

    /**
     * Merges the second result into the first using the unionMode
     * set operation.
     */
    private void mergeResults(Session session, Result first,
                              Result second) throws HsqlException {

        switch (unionType) {

            case UNION :
                first.append(second);
                first.removeDuplicates(session, iResultLen);
                break;

            case UNIONALL :
                first.append(second);
                break;

            case INTERSECT :
                first.removeDifferent(session, second, iResultLen);
                break;

            case EXCEPT :
                first.removeSecond(session, second, iResultLen);
                break;
        }
    }

    int getLimitStart(Session session) throws HsqlException {

        if (limitCondition != null) {
            Integer limit =
                (Integer) limitCondition.getArg().getValue(session);

            if (limit != null) {
                return limit.intValue();
            }
        }

        return 0;
    }

    /**
     * For SELECT LIMIT n m ....
     * finds cases where the result does not have to be fully built and
     * returns an adjusted rowCount with LIMIT params.
     */
    int getLimitCount(Session session, int rowCount) throws HsqlException {

        int limitCount = 0;

        if (limitCondition != null) {
            Integer limit =
                (Integer) limitCondition.getArg2().getValue(session);

            if (limit != null) {
                limitCount = limit.intValue();
            }
        }

        if (rowCount != 0 && (limitCount == 0 || rowCount < limitCount)) {
            limitCount = rowCount;
        }

        return limitCount;
    }

    /**
     * translate the rowCount into total number of rows needed from query,
     * including any rows skipped at the beginning
     */
    int getMaxRowCount(Session session, int rowCount) throws HsqlException {

        int limitStart = getLimitStart(session);
        int limitCount = getLimitCount(session, rowCount);

        if (!simpleLimit) {
            rowCount = Integer.MAX_VALUE;
        } else {
            if (rowCount == 0) {
                rowCount = limitCount;
            }

            if (rowCount == 0 || rowCount > Integer.MAX_VALUE - limitStart) {
                rowCount = Integer.MAX_VALUE;
            } else {
                rowCount += limitStart;
            }
        }

        return rowCount;
    }

    private Result getSingleResult(Session session,
                                   int rowCount) throws HsqlException {

        if (resultMetaData == null) {
            prepareResult(session);
        }

        Result r = buildResult(session, getMaxRowCount(session, rowCount));

        // the result is perhaps wider (due to group and order by)
        // so use the visible columns to remove duplicates
        if (isDistinctSelect) {
            r.removeDuplicates(session, iResultLen);
        }

        if (!sortUnion) {
            sortResult(session, r);
            r.trimResult(getLimitStart(session),
                         getLimitCount(session, rowCount));
        }

        return r;
    }

    private void prepareSort() {

        if (iOrderLen == 0) {
            return;
        }

        sortOrder     = new int[iOrderLen];
        sortDirection = new int[iOrderLen];

        int startCol = iResultLen + iGroupLen + iHavingLen;

        for (int i = startCol, j = 0; j < iOrderLen; i++, j++) {
            int colindex = i;

            // fredt - when a union, use the visible select columns for sort comparison
            // also whenever a column alias is used
            if (exprColumns[i].joinedTableColumnIndex != -1) {
                colindex = exprColumns[i].joinedTableColumnIndex;
            }

            sortOrder[j]     = colindex;
            sortDirection[j] = exprColumns[i].isDescending() ? -1
                                                             : 1;
        }
    }

    private void sortResult(Session session, Result r) throws HsqlException {

        if (iOrderLen == 0) {
            return;
        }

        r.sortResult(session, sortOrder, sortDirection);
    }

    /**
     * Check result columns for aggregate or group by violation.
     * If any result column is aggregated, then all result columns need to be
     * aggregated, unless it is included in the group by clause.
     */
    private void checkAggregateOrGroupByColumns(int start,
            int end) throws HsqlException {

        if (start < end) {
            HsqlArrayList colExps = new HsqlArrayList();

            for (int i = start; i < end; i++) {
                exprColumns[i].collectInGroupByExpressions(colExps);
            }

            for (int i = 0, size = colExps.size(); i < size; i++) {
                Expression exp = (Expression) colExps.get(i);

                if (inAggregateOrGroupByClause(exp)) {
                    continue;
                }

                throw Trace.error(Trace.NOT_IN_AGGREGATE_OR_GROUP_BY, exp);
            }
        }
    }

    private void checkAggregateOrGroupByOrderColumns(int start,
            int end) throws HsqlException {

        checkAggregateOrGroupByColumns(start, end);

        if (start < end && isDistinctSelect) {
            HsqlArrayList colExps = new HsqlArrayList();

            for (int i = start; i < end; i++) {
                exprColumns[i].collectInGroupByExpressions(colExps);
            }

            for (int i = 0, size = colExps.size(); i < size; i++) {
                Expression exp = (Expression) colExps.get(i);

                if (isSimilarIn(exp, 0, iResultLen)) {
                    continue;
                }

                throw Trace.error(Trace.INVALID_ORDER_BY_IN_DISTINCT_SELECT,
                                  exp);
            }
        }
    }

    /**
     * Check if the given expression is acceptable in a select that may
     * include aggregate function and/or group by clause.
     * <p>
     * The expression is acceptable if:
     * <UL>
     * <LI>The select does not containt any aggregate function;
     * <LI>The expression itself can be included in an aggregate select;
     * <LI>The expression is defined in the group by clause;
     * <LI>All the columns in the expression are defined in the group by clause;
     * </UL)
     */
    private boolean inAggregateOrGroupByClause(Expression exp) {

        if (isGrouped) {
            return isSimilarIn(exp, iResultLen, iResultLen + iGroupLen)
                   || allColumnsAreDefinedIn(exp, groupColumnNames);
        } else if (isAggregated) {
            return exp.canBeInAggregate();
        } else {
            return true;
        }
    }

    /**
     * Check if the given expression is similar to any of the eColumn
     * expressions within the given range.
     */
    private boolean isSimilarIn(Expression exp, int start, int end) {

        for (int i = start; i < end; i++) {
            if (exp.similarTo(exprColumns[i])) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if all the column names used in the given expression are defined
     * in the given defined column names.
     */
    static boolean allColumnsAreDefinedIn(Expression exp,
                                          HashSet definedColumns) {

        HashSet colNames = new HashSet();

        exp.collectAllColumnNames(colNames);

        if ((colNames.size() > 0) && (definedColumns == null)) {
            return false;
        }

        Iterator i = colNames.iterator();

        while (i.hasNext()) {
            if (!definedColumns.contains(i.next())) {
                return false;
            }
        }

        return true;
    }

// fredt@users 20030810 - patch 1.7.2 - OUTER JOIN rewrite
    private Result buildResult(Session session,
                               int limitcount) throws HsqlException {

        GroupedResult gResult   = new GroupedResult(this, resultMetaData);
        final int     len       = exprColumns.length;
        final int     filter    = tFilter.length;
        boolean[]     first     = new boolean[filter];
        boolean[]     outerused = new boolean[filter];
        int           level     = 0;

        // fredt - shortcut needed by OpenOffice to speed up empty query processing for metadata
        boolean notempty = !(queryCondition != null
                             && queryCondition.isFixedConditional()
                             &&!queryCondition.testCondition(session));

        while (notempty && level >= 0) {

            // perform a join
            TableFilter t = tFilter[level];
            boolean     found;
            boolean     outerfound;

            if (!first[level]) {
                found = t.findFirst(session);

                // if outer join, and no inner result, get next outer row
                // nonJoinIsNull disallows getting the next outer row in some circumstances
                outerused[level] = outerfound = t.isOuterJoin &&!found
                                                &&!outerused[level]
                                                &&!t.nonJoinIsNull
                                                && t.nextOuter(session);
                first[level] = found;
            } else {
                found = t.next(session);
                outerused[level] = outerfound = t.isOuterJoin &&!found
                                                &&!first[level]
                                                &&!outerused[level]
                                                &&!t.nonJoinIsNull
                                                && t.nextOuter(session);
                first[level] = found;
            }

            if (!found &&!outerfound) {
                level--;

                continue;
            }

            if (level < filter - 1) {
                level++;

                continue;
            } else {
                while (outerused[level]) {
                    outerused[level--] = false;
                }
            }

            // apply condition
            if (queryCondition == null
                    || queryCondition.testCondition(session)) {
                try {
                    Object[] row = new Object[len];

                    // gets the group by column values first.
                    for (int i = gResult.groupBegin; i < gResult.groupEnd;
                            i++) {
                        row[i] = exprColumns[i].getValue(session);
                    }

                    row = gResult.getRow(row);

                    // Get all other values
                    for (int i = 0; i < gResult.groupBegin; i++) {
                        row[i] =
                            isAggregated && exprColumns[i].isAggregate()
                            ? exprColumns[i].updateAggregatingValue(session,
                                row[i])
                            : exprColumns[i].getValue(session);
                    }

                    for (int i = gResult.groupEnd; i < len; i++) {
                        row[i] =
                            isAggregated && exprColumns[i].isAggregate()
                            ? exprColumns[i].updateAggregatingValue(session,
                                row[i])
                            : exprColumns[i].getValue(session);
                    }

                    gResult.addRow(row);

                    if (gResult.size() >= limitcount) {
                        break;
                    }
                } catch (HsqlInternalException e) {
                    continue;
                }
            }
        }

        if (isAggregated &&!isGrouped && gResult.size() == 0) {
            Object[] row = new Object[len];

            for (int i = 0; i < len; i++) {
                row[i] = exprColumns[i].isAggregate() ? null
                                                      : exprColumns[i]
                                                      .getValue(session);
            }

            gResult.addRow(row);
        }

        Iterator it = gResult.iterator();

        while (it.hasNext()) {
            Object[] row = (Object[]) it.next();

            if (isAggregated) {
                for (int i = 0; i < len; i++) {
                    if (exprColumns[i].isAggregate()) {
                        row[i] = exprColumns[i].getAggregatedValue(session,
                                row[i]);
                    }
                }
            }

            if (iHavingLen > 0) {

                // The test value, either aggregate or not, is set already.
                // Removes the row that does not satisfy the HAVING
                // condition.
                if (!Boolean.TRUE.equals(row[iResultLen + iGroupLen])) {
                    it.remove();
                }
            }
        }

        return gResult.getResult();
    }

    /**
     * Skeleton under development. Needs a lot of work.
     */
    public StringBuffer getDDL() throws HsqlException {

        StringBuffer sb = new StringBuffer();

        sb.append(Token.T_SELECT).append(' ');

        //limitStart;
        //limitCount;
        for (int i = 0; i < iResultLen; i++) {
            sb.append(exprColumns[i].getDDL());

            if (i < iResultLen - 1) {
                sb.append(',');
            }
        }

        sb.append(Token.T_FROM);

        for (int i = 0; i < tFilter.length; i++) {

            // find out if any expression in any of the filters isInJoin then use this form
            TableFilter filter = tFilter[i];

            // if any expression isInJoin
            if (i != 0) {
                if (filter.isOuterJoin) {
                    sb.append(Token.T_FROM).append(' ');
                    sb.append(Token.T_JOIN).append(' ');
                }

                // eStart and eEnd expressions
            }

            // otherwise use a comma delimited table list
            sb.append(',');
        }

        // if there are any expressions that are not isInJoin
        sb.append(' ').append(Token.T_WHERE).append(' ');

        for (int i = 0; i < tFilter.length; i++) {
            TableFilter filter = tFilter[i];

            // eStart and eEnd expressions that are not isInJoin
        }

        // if has GROUP BY
        sb.append(' ').append(Token.T_GROUP).append(' ');

        for (int i = iResultLen; i < iResultLen + iGroupLen; i++) {
            sb.append(exprColumns[i].getDDL());

            if (i < iResultLen + iGroupLen - 1) {
                sb.append(',');
            }
        }

        // if has HAVING
        sb.append(' ').append(Token.T_HAVING).append(' ');

        for (int i = iResultLen + iGroupLen;
                i < iResultLen + iGroupLen + iHavingLen; i++) {
            sb.append(exprColumns[i].getDDL());

            if (i < iResultLen + iGroupLen - 1) {
                sb.append(',');
            }
        }

        if (unionSelect != null) {
            switch (unionType) {

                case EXCEPT :
                    sb.append(' ').append(Token.T_EXCEPT).append(' ');
                    break;

                case INTERSECT :
                    sb.append(' ').append(Token.T_INTERSECT).append(' ');
                    break;

                case UNION :
                    sb.append(' ').append(Token.T_UNION).append(' ');
                    break;

                case UNIONALL :
                    sb.append(' ').append(Token.T_UNION).append(' ').append(
                        Token.T_ALL).append(' ');
                    break;
            }
        }

        // if has ORDER BY
        int groupByEnd   = iResultLen + iGroupLen;
        int orderByStart = groupByEnd + iHavingLen;
        int orderByEnd   = orderByStart + iOrderLen;

        sb.append(' ').append(Token.T_ORDER).append(Token.T_BY).append(' ');

        for (int i = orderByStart; i < orderByEnd; i++) {
            sb.append(exprColumns[i].getDDL());

            if (i < iResultLen + iGroupLen - 1) {
                sb.append(',');
            }
        }

        return sb;
    }

    boolean isResolved = false;

    /**
     * @todo - post 1.8.0 - review resolve and check resolve -
     * determine if isResolved is specific to main query or the full set including UNION
     *
     */
    boolean resolveAll(Session session, boolean check) throws HsqlException {

        if (isResolved) {
            return true;
        }

        resolve(session);

        isResolved = checkResolved(check);

        if (unionSelect != null) {
            if (unionSelect.iResultLen != iResultLen) {
                throw Trace.error(Trace.COLUMN_COUNT_DOES_NOT_MATCH);
            }

            for (int i = 0; i < iResultLen; i++) {
                Expression e = exprColumns[i];

                if (!e.isTypeEqual(unionSelect.exprColumns[i])) {
                    unionSelect.exprColumns[i] =
                        new Expression(unionSelect.exprColumns[i],
                                       e.getDataType(), e.getColumnSize(),
                                       e.getColumnScale());
                }
            }

            isResolved &= unionSelect.resolveAll(session, check);
        }

        return isResolved;
    }

    boolean isResolved() {
        return isResolved;
    }

    public String describe(Session session) {

        StringBuffer sb;
        String       temp;

        // temporary :  it is currently unclear whether this may affect
        // later attempts to retrieve an actual result (calls getResult(1)
        // in preProcess mode).  Thus, toString() probably should not be called
        // on Select objects that will actually be used to retrieve results,
        // only on Select objects used by EXPLAIN PLAN FOR
        try {
            getResult(session, 1);
        } catch (HsqlException e) {}

        sb = new StringBuffer();

        sb.append(super.toString()).append("[\n");

        if (sIntoTable != null) {
            sb.append("into table=[").append(sIntoTable.name).append("]\n");
        }

        if (limitCondition != null) {
            sb.append("offset=[").append(
                limitCondition.getArg().describe(session)).append("]\n");
            sb.append("limit=[").append(
                limitCondition.getArg2().describe(session)).append("]\n");
        }

        sb.append("isDistinctSelect=[").append(isDistinctSelect).append(
            "]\n");
        sb.append("isGrouped=[").append(isGrouped).append("]\n");
        sb.append("isAggregated=[").append(isAggregated).append("]\n");
        sb.append("columns=[");

        int columns = exprColumns.length - iOrderLen;

        for (int i = 0; i < columns; i++) {
            sb.append(exprColumns[i].describe(session));
        }

        sb.append("\n]\n");
        sb.append("tableFilters=[\n");

        for (int i = 0; i < tFilter.length; i++) {
            sb.append("[\n");
            sb.append(tFilter[i].describe(session));
            sb.append("\n]");
        }

        sb.append("]\n");

        temp = queryCondition == null ? "null"
                                      : queryCondition.describe(session);

        sb.append("eCondition=[").append(temp).append("]\n");

        temp = havingCondition == null ? "null"
                                       : havingCondition.describe(session);

        sb.append("havingCondition=[").append(temp).append("]\n");
        sb.append("groupColumns=[").append(groupColumnNames).append("]\n");

        if (unionSelect != null) {
            switch (unionType) {

                case EXCEPT :
                    sb.append(" EXCEPT ");
                    break;

                case INTERSECT :
                    sb.append(" INTERSECT ");
                    break;

                case UNION :
                    sb.append(" UNION ");
                    break;

                case UNIONALL :
                    sb.append(" UNION ALL ");
                    break;

                default :
                    sb.append(" UNKNOWN SET OPERATION ");
            }

            sb.append("[\n").append(unionSelect.describe(session)).append(
                "]\n");
        }

        return sb.toString();
    }

    Result describeResult() {

        Result                r;
        Result.ResultMetaData rmd;
        Expression            e;

        r   = new Result(ResultConstants.DATA, iResultLen);
        rmd = r.metaData;

        for (int i = 0; i < iResultLen; i++) {
            e                    = exprColumns[i];
            rmd.colTypes[i]      = e.getDataType();
            rmd.colSizes[i]      = e.getColumnSize();
            rmd.colScales[i]     = e.getColumnScale();
            rmd.colLabels[i]     = e.getAlias();
            rmd.isLabelQuoted[i] = e.isAliasQuoted();
            rmd.tableNames[i]    = e.getTableName();
            rmd.colNames[i]      = e.getColumnName();

            if (rmd.isTableColumn(i)) {
                rmd.colNullable[i] = e.nullability;
                rmd.isIdentity[i]  = e.isIdentity;
                rmd.isWritable[i]  = e.isWritable;
            }
        }

        return r;
    }
}
