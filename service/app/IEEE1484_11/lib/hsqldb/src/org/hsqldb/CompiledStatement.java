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

import org.hsqldb.HsqlNameManager.HsqlName;

/**
 * A simple structure class for holding the products of
 * statement compilation for later execution.
 *
 * @author  boucherb@users
 * @version 1.7.2
 * @since 1.7.2
 */

// fredt@users 20040404 - patch 1.7.2 - fixed type resolution for parameters
// boucherb@users 200404xx - patch 1.7.2 - changed parameter naming scheme for SQLCI client usability/support
// fredt@users 20050609 - 1.8.0 - fixed EXPLAIN PLAN by implementing describe(Session)
final class CompiledStatement {

    static final String PCOL_PREFIX        = "@p";
    static final String RETURN_COLUMN_NAME = "@p0";
    static final int    UNKNOWN            = 0;

    // enumeration of allowable CompiledStatement types
    static final int INSERT_VALUES = 1;
    static final int INSERT_SELECT = 2;
    static final int UPDATE        = 3;
    static final int DELETE        = 4;
    static final int SELECT        = 5;
    static final int SELECT_INTO   = 6;
    static final int CALL          = 7;

    // enumeration of catagories
    static final int DML = 7;
    static final int DQL = 8;
    static final int DDL = 9;

    /** id in CompiledStatementManager */
    int id;

    /** false when cleared */
    boolean isValid = true;

    /** target table for INSERT_XXX, UPDATE and DELETE */
    Table targetTable;

    /** table filter for UPDATE and DELETE */
    TableFilter targetFilter;

    /** condition expression for UPDATE and DELETE */
    Expression condition;

    /** column map for INSERT_XXX, UPDATE */
    int[] columnMap;

    /** Column value Expressions for INSERT_VALUES and UPDATE. */
    Expression[] columnValues;

    /**
     * Flags indicating which columns' values will/will not be
     * explicitly set.
     */
    boolean[] checkColumns;

    /** Expression to be evaluated when this is a CALL statement. */
    Expression expression;

    /**
     * Select to be evaluated when this is an INSERT_SELECT or
     * SELECT statement
     */
    Select select;

    /**
     * Parse-order array of Expression objects, all of iType == PARAM ,
     * involved in some way in any INSERT_XXX, UPDATE, DELETE, SELECT or
     * CALL CompiledStatement
     */
    Expression[] parameters;

    /**
     * int[] contains type of each parameter
     */
    int[] paramTypes;

    /**
     * Subqueries inverse parse depth order
     */
    SubQuery[] subqueries;

    /**
     * The type of this CompiledStatement. <p>
     *
     * One of: <p>
     *
     * <ol>
     *  <li>UNKNOWN
     *  <li>INSERT_VALUES
     *  <li>INSERT_SELECT
     *  <li>UPDATE
     *  <li>DELETE
     *  <li>SELECT
     *  <li>CALL
     *  <li>DDL
     * </ol>
     */
    int type;

    /**
     * The SQL string that produced this compiled statement
     */
    String sql;

    /**
     * The default schema name used to resolve names in the sql
     */
    final HsqlName schemaHsqlName;

    /**
     * Creates a new instance of CompiledStatement for DDL
     *
     */
    CompiledStatement(HsqlName schema) {

        parameters     = new Expression[0];
        paramTypes     = new int[0];
        subqueries     = new SubQuery[0];
        type           = DDL;
        schemaHsqlName = schema;
    }

    /**
     * Initializes this as a DELETE statement
     *
     * @param targetFilter
     * @param deleteCondition
     * @param parameters
     */
    CompiledStatement(Session session, Database database, HsqlName schema,
                      TableFilter targetFilter, Expression deleteCondition,
                      SubQuery[] subqueries,
                      Expression[] params) throws HsqlException {

        schemaHsqlName    = schema;
        this.targetFilter = targetFilter;
        targetTable       = targetFilter.filterTable;

        if (deleteCondition != null) {
            condition = new Expression(deleteCondition);

            condition.resolveTables(targetFilter);
            condition.resolveTypes(session);
            targetFilter.setConditions(session, condition);
        }

        setParameters(params);
        setSubqueries(subqueries);

        type = DELETE;
    }

    /**
     * Instantiate this as an UPDATE statement.
     *
     * @param targetTable
     * @param columnMap
     * @param columnValues
     * @param updateCondition
     * @param params
     */
    CompiledStatement(Session session, Database database, HsqlName schema,
                      TableFilter targetFilter, int[] columnMap,
                      Expression[] columnValues, Expression updateCondition,
                      SubQuery[] subqueries,
                      Expression[] params) throws HsqlException {

        schemaHsqlName    = schema;
        this.targetFilter = targetFilter;
        targetTable       = targetFilter.filterTable;
        this.columnMap    = columnMap;
        this.columnValues = columnValues;

        for (int i = 0; i < columnValues.length; i++) {
            Expression cve = columnValues[i];

            if (cve.isParam()) {
                cve.setTableColumnAttributes(targetTable, columnMap[i]);
            } else {
                cve.resolveTables(targetFilter);
                cve.resolveTypes(session);
            }
        }

        if (updateCondition != null) {
            condition = new Expression(updateCondition);

            condition.resolveTables(targetFilter);
            condition.resolveTypes(session);
            targetFilter.setConditions(session, condition);
        }

        setParameters(params);
        setSubqueries(subqueries);

        type = UPDATE;
    }

    /**
     * Instantiate this as an INSERT_VALUES statement.
     *
     * @param targetTable
     * @param columnMap
     * @param columnValues
     * @param checkColumns
     * @param params
     */
    CompiledStatement(HsqlName schema, Table targetTable, int[] columnMap,
                      Expression[] columnValues, boolean[] checkColumns,
                      SubQuery[] subqueries,
                      Expression[] params) throws HsqlException {

        schemaHsqlName    = schema;
        this.targetTable  = targetTable;
        this.columnMap    = columnMap;
        this.checkColumns = checkColumns;
        this.columnValues = columnValues;

        for (int i = 0; i < columnValues.length; i++) {
            Expression cve = columnValues[i];

            // If its not a param, it's already been resolved in
            // Parser.getColumnValueExpressions
            if (cve.isParam()) {
                cve.setTableColumnAttributes(targetTable, columnMap[i]);
            }
        }

        setParameters(params);
        setSubqueries(subqueries);

        type = INSERT_VALUES;
    }

    /**
     * Instantiate this as an INSERT_SELECT statement.
     *
     * @param targetTable
     * @param columnMap
     * @param checkColumns
     * @param select
     * @param params
     */
    CompiledStatement(Session session, Database database, HsqlName schema,
                      Table targetTable, int[] columnMap,
                      boolean[] checkColumns, Select select,
                      SubQuery[] subqueries,
                      Expression[] params) throws HsqlException {

        schemaHsqlName    = schema;
        this.targetTable  = targetTable;
        this.columnMap    = columnMap;
        this.checkColumns = checkColumns;
        this.select       = select;

        // resolve any parameters in SELECT
        resolveInsertParameterTypes();

        // set select result metadata etc.
        select.prepareResult(session);
        setParameters(params);
        setSubqueries(subqueries);

        type = INSERT_SELECT;
    }

    /**
     * Instantiate this as a SELECT statement.
     *
     * @param select
     * @param params
     */
    CompiledStatement(Session session, Database database, HsqlName schema,
                      Select select, SubQuery[] subqueries,
                      Expression[] params) throws HsqlException {

        schemaHsqlName = schema;
        this.select    = select;

        // resolve any parameters in SELECT as VARCHAR
        for (int i = 0; i < select.iResultLen; i++) {
            Expression colexpr = select.exprColumns[i];

            if (colexpr.getDataType() == Types.NULL) {
                colexpr.setDataType(Types.VARCHAR);
            }
        }

        // set select result metadata etc.
        select.prepareResult(session);
        setParameters(params);
        setSubqueries(subqueries);

        type = SELECT;
    }

    /**
     * Instantiate this as a CALL statement.
     *
     * @param expression
     * @param params
     */
    CompiledStatement(Session session, Database database, HsqlName schema,
                      Expression expression, SubQuery[] subqueries,
                      Expression[] params) throws HsqlException {

        schemaHsqlName  = schema;
        this.expression = expression;

        expression.resolveTypes(session);

        expression.paramMode = Expression.PARAM_OUT;

        setParameters(params);
        setSubqueries(subqueries);

        type = CALL;
    }

    /**
     * For parameters in INSERT_VALUES and INSERT_SELECT lists
     */
    private void resolveInsertParameterTypes() {

        for (int i = 0; i < select.iResultLen; i++) {
            Expression colexpr = select.exprColumns[i];

            if (colexpr.getDataType() == Types.NULL) {
                Column col = targetTable.getColumn(columnMap[i]);

                colexpr.setDataType(col.getType());
            }
        }
    }

    private void setParameters(Expression[] params) {

        this.parameters = params;

        int[] types = new int[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            types[i] = parameters[i].getDataType();
        }

        this.paramTypes = types;
    }

    private void setSubqueries(SubQuery[] subqueries) {
        this.subqueries = subqueries;
    }

    void materializeSubQueries(Session session) throws HsqlException {

        for (int i = 0; i < subqueries.length; i++) {
            SubQuery sq = subqueries[i];

            // VIEW working table contents are filled only once per query and reused
            if (sq.isMaterialised) {
                continue;
            }

            if (sq.isResolved) {
                sq.populateTable(session);

                sq.isMaterialised = true;
            }
        }
    }

    void dematerializeSubQueries(Session session) {

        if (subqueries == null) {
            return;
        }

        for (int i = 0; i < subqueries.length; i++) {
            subqueries[i].table.clearAllRows(session);

            subqueries[i].isMaterialised = false;
        }
    }

    void clearVariables() {

        isValid      = false;
        targetTable  = null;
        targetFilter = null;
        condition    = null;
        columnMap    = null;
        columnValues = null;
        checkColumns = null;
        expression   = null;
        select       = null;
        parameters   = null;
        paramTypes   = null;
        subqueries   = null;
    }

    boolean canExecute(Session session) throws HsqlException {

        switch (type) {

            case CALL : {}
            case SELECT :
                for (int i = 0; i < select.tFilter.length; i++) {
                    HsqlName name = select.tFilter[i].filterTable.getName();

                    session.check(name, UserManager.SELECT);
                }
                break;

            case INSERT_SELECT :
                break;

            case DELETE :
                session.check(targetTable.getName(), UserManager.DELETE);
                break;

            case INSERT_VALUES :
                session.check(targetTable.getName(), UserManager.INSERT);
                break;

            case UPDATE :
                session.check(targetTable.getName(), UserManager.UPDATE);
                break;

            case DDL :
        }

        return true;
    }

    void checkTableWriteAccess(Session session,
                               Table table) throws HsqlException {

        // session level user rights
        session.checkReadWrite();

        // object type
        if (table.isView()) {
            throw Trace.error(Trace.NOT_A_TABLE, table.getName().name);
        }

        // object readonly
        table.checkDataReadOnly();
    }

    private static final Result updateCountResult =
        new Result(ResultConstants.UPDATECOUNT);

    Result describeResult() {

        switch (type) {

            case CALL : {

                // TODO:
                //
                // 1.) standard to register metadata for columns of
                // the primary result set, if any, generated by call
                //
                // 2.) Represent the return value, if any (which is
                // not, in truth, a result set), as an OUT parameter
                //
                // For now, I've reverted a bunch of code I had in place
                // and instead simply reflect things as the are, describing
                // a single column result set that communicates
                // the return value.  If the expression generating the
                // return value has a void return type, a result set
                // is described whose single column is of type NULL
                Expression e;
                Result     r;

                e = expression;
                r = Result.newSingleColumnResult(
                    CompiledStatement.RETURN_COLUMN_NAME, e.getDataType());
                r.metaData.classNames[0] = e.getValueClassName();

                // no more setup for r; all the defaults apply
                return r;
            }
            case SELECT :
                return select.sIntoTable == null ? select.describeResult()
                                                 : updateCountResult;

            case DELETE :
            case INSERT_SELECT :
            case INSERT_VALUES :
            case UPDATE :
            case DDL :

                // will result in
                return updateCountResult;

            default :
                return new Result(
                    Trace.runtimeError(
                        Trace.UNSUPPORTED_INTERNAL_OPERATION,
                        "CompiledStatement.describeResult()"), null);
        }
    }

    Result describeParameters() {

        Result     out;
        Expression e;
        int        outlen;
        int        offset;
        int        idx;
        boolean    hasReturnValue;

        outlen = parameters.length;
        offset = 0;

// NO:  Not yet
//        hasReturnValue = (type == CALL && !expression.isProcedureCall());
//
//        if (hasReturnValue) {
//            outlen++;
//            offset = 1;
//        }
        out = Result.newParameterDescriptionResult(outlen);

// NO: Not yet
//        if (hasReturnValue) {
//            e = expression;
//            out.sName[0]       = DIProcedureInfo.RETURN_COLUMN_NAME;
//            out.sClassName[0]  = e.getValueClassName();
//            out.colType[0]     = e.getDataType();
//            out.colSize[0]     = e.getColumnSize();
//            out.colScale[0]    = e.getColumnScale();
//            out.nullability[0] = e.nullability;
//            out.isIdentity[0]  = false;
//            out.paramMode[0]   = expression.PARAM_OUT;
//        }
        for (int i = 0; i < parameters.length; i++) {
            e   = parameters[i];
            idx = i + offset;

            // always i + 1.  We currently use the convention of @p0 to name the
            // return value OUT parameter
            out.metaData.colNames[idx] = CompiledStatement.PCOL_PREFIX
                                         + (i + 1);

            // sLabel is meaningless in this context.
            out.metaData.classNames[idx]  = e.getValueClassName();
            out.metaData.colTypes[idx]    = e.getDataType();
            out.metaData.colSizes[idx]    = e.getColumnSize();
            out.metaData.colScales[idx]   = e.getColumnScale();
            out.metaData.colNullable[idx] = e.nullability;
            out.metaData.isIdentity[idx]  = e.isIdentity;

            // currently will always be Expression.PARAM_IN
            out.metaData.paramMode[idx] = e.paramMode;
        }

        return out;
    }

    /**
     * Retrieves a String representation of this object.
     *
     * @return  the String representation of this object
     */
    public String describe(Session session) {

        try {
            return describeImpl(session);
        } catch (Exception e) {
            return e.toString();
        }
    }

    /**
     * Provides the toString() implementation.
     *
     * @throws Exception if a database access or io error occurs
     * @return the String representation of this object
     */
    private String describeImpl(Session session) throws Exception {

        StringBuffer sb;

        sb = new StringBuffer();

        switch (type) {

            case SELECT : {
                sb.append(select.describe(session));
                appendParms(sb).append('\n');
                appendSubqueries(sb);

                return sb.toString();
            }
            case INSERT_VALUES : {
                sb.append("INSERT VALUES");
                sb.append('[').append('\n');
                appendColumns(sb).append('\n');
                appendTable(sb).append('\n');
                appendParms(sb).append('\n');
                appendSubqueries(sb).append(']');

                return sb.toString();
            }
            case INSERT_SELECT : {
                sb.append("INSERT SELECT");
                sb.append('[').append('\n');
                appendColumns(sb).append('\n');
                appendTable(sb).append('\n');
                sb.append(select.describe(session)).append('\n');
                appendParms(sb).append('\n');
                appendSubqueries(sb).append(']');

                return sb.toString();
            }
            case UPDATE : {
                sb.append("UPDATE");
                sb.append('[').append('\n');
                appendColumns(sb).append('\n');
                appendTable(sb).append('\n');
                appendCondition(session, sb);
                sb.append(targetFilter.describe(session)).append('\n');
                appendParms(sb).append('\n');
                appendSubqueries(sb).append(']');

                return sb.toString();
            }
            case DELETE : {
                sb.append("DELETE");
                sb.append('[').append('\n');
                appendTable(sb).append('\n');
                appendCondition(session, sb);
                sb.append(targetFilter.describe(session)).append('\n');
                appendParms(sb).append('\n');
                appendSubqueries(sb).append(']');

                return sb.toString();
            }
            case CALL : {
                sb.append("CALL");
                sb.append('[');
                sb.append(expression.describe(session)).append('\n');
                appendParms(sb).append('\n');
                appendSubqueries(sb).append(']');

                return sb.toString();
            }
            default : {
                return "UNKNOWN";
            }
        }
    }

    private StringBuffer appendSubqueries(StringBuffer sb) {

        sb.append("SUBQUERIES[");

        for (int i = 0; i < subqueries.length; i++) {
            sb.append("\n[level=").append(subqueries[i].level).append(
                '\n').append("hasParams=").append(
                subqueries[i].hasParams).append('\n');

            if (subqueries[i].select != null) {
                sb.append("org.hsqldb.Select@").append(
                    Integer.toHexString(subqueries[i].select.hashCode()));
            }

            sb.append("]");
        }

        sb.append(']');

        return sb;
    }

    private StringBuffer appendTable(StringBuffer sb) {

        sb.append("TABLE[").append(targetTable.getName().name).append(']');

        return sb;
    }

    private StringBuffer appendColumns(StringBuffer sb) {

        sb.append("COLUMNS=[");

        for (int i = 0; i < columnMap.length; i++) {
            sb.append('\n').append(columnMap[i]).append(':').append(
                ' ').append(
                targetTable.getColumn(columnMap[i]).columnName.name).append(
                '[').append(columnValues[i]).append(']');
        }

        sb.append(']');

        return sb;
    }

    private StringBuffer appendParms(StringBuffer sb) {

        sb.append("PARAMETERS=[");

        for (int i = 0; i < parameters.length; i++) {
            sb.append('\n').append('@').append(i).append('[').append(
                parameters[i]).append(']');
        }

        sb.append(']');

        return sb;
    }

    private StringBuffer appendCondition(Session session, StringBuffer sb) {

        return condition == null ? sb.append("CONDITION[]\n")
                                 : sb.append("CONDITION[").append(
                                     condition.describe(session)).append(
                                     "]\n");
    }
}
