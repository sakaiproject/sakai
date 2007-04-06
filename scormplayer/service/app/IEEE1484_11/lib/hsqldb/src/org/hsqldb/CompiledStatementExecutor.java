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

import org.hsqldb.jdbc.jdbcResultSet;
import org.hsqldb.lib.HashMappedList;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.java.JavaSystem;

// boucherb@users 200404xx - fixed broken CALL statement result set unwrapping;
//                           fixed broken support for prepared SELECT...INTO

/**
 * Provides execution of CompiledStatement objects. <p>
 *
 * If multiple threads access a CompiledStatementExecutor.execute()
 * concurrently, they must be synchronized externally, relative to both
 * this object's Session and the Session's Database object. Internally, this
 * is accomplished in Session.execute() by synchronizing on the Session
 * object's Database object.
 *
 * @author  boucherb@users
 * @version 1.7.2
 * @since 1.7.2
 */
final class CompiledStatementExecutor {

    private Session session;
    private Result  updateResult;
    private static Result emptyZeroResult =
        new Result(ResultConstants.UPDATECOUNT);
    private static Result updateOneResult =
        new Result(ResultConstants.UPDATECOUNT);

    static {
        updateOneResult.updateCount = 1;
    }

    /**
     * Creates a new instance of CompiledStatementExecutor.
     *
     * @param session the context in which to perform the execution
     */
    CompiledStatementExecutor(Session session) {
        this.session = session;
        updateResult = new Result(ResultConstants.UPDATECOUNT);
    }

    /**
     * Executes a generic CompiledStatement. Execution includes first building
     * any subquery result dependencies and clearing them after the main result
     * is built.
     *
     * @return the result of executing the statement
     * @param cs any valid CompiledStatement
     */
    Result execute(CompiledStatement cs, Object[] paramValues) {

        Result result = null;

        JavaSystem.gc();

        for (int i = 0; i < cs.parameters.length; i++) {
            cs.parameters[i].bind(paramValues[i]);
        }

        try {
            cs.materializeSubQueries(session);

            result = executeImpl(cs);
        } catch (Throwable t) {
            result = new Result(t, cs.sql);
        }

        // clear redundant data
        cs.dematerializeSubQueries(session);

        if (result == null) {
            result = emptyZeroResult;
        }

        return result;
    }

    /**
     * Executes a generic CompiledStatement. Execution excludes building
     * subquery result dependencies and clearing them after the main result
     * is built.
     *
     * @param cs any valid CompiledStatement
     * @throws HsqlException if a database access error occurs
     * @return the result of executing the statement
     */
    private Result executeImpl(CompiledStatement cs) throws HsqlException {

        switch (cs.type) {

            case CompiledStatement.SELECT :
                return executeSelectStatement(cs);

            case CompiledStatement.INSERT_SELECT :
                return executeInsertSelectStatement(cs);

            case CompiledStatement.INSERT_VALUES :
                return executeInsertValuesStatement(cs);

            case CompiledStatement.UPDATE :
                return executeUpdateStatement(cs);

            case CompiledStatement.DELETE :
                return executeDeleteStatement(cs);

            case CompiledStatement.CALL :
                return executeCallStatement(cs);

            case CompiledStatement.DDL :
                return executeDDLStatement(cs);

            default :
                throw Trace.runtimeError(
                    Trace.UNSUPPORTED_INTERNAL_OPERATION,
                    "CompiledStatementExecutor.executeImpl()");
        }
    }

    /**
     * Executes a CALL statement.  It is assumed that the argument is
     * of the correct type.
     *
     * @param cs a CompiledStatement of type CompiledStatement.CALL
     * @throws HsqlException if a database access error occurs
     * @return the result of executing the statement
     */
    private Result executeCallStatement(CompiledStatement cs)
    throws HsqlException {

        Expression e = cs.expression;          // representing CALL
        Object     o = e.getValue(session);    // expression return value
        Result     r;

        if (o instanceof Result) {
            return (Result) o;
        } else if (o instanceof jdbcResultSet) {
            return ((jdbcResultSet) o).rResult;
        }

        r = Result.newSingleColumnResult(CompiledStatement.RETURN_COLUMN_NAME,
                                         e.getDataType());

        Object[] row = new Object[1];

        row[0]                   = o;
        r.metaData.classNames[0] = e.getValueClassName();

        r.add(row);

        return r;
    }

// fredt - currently deletes that fail due to referential constraints are caught prior to
// actual delete operation, so no nested transaction is required

    /**
     * Executes a DELETE statement.  It is assumed that the argument is
     * of the correct type.
     *
     * @param cs a CompiledStatement of type CompiledStatement.DELETE
     * @throws HsqlException if a database access error occurs
     * @return the result of executing the statement
     */
    private Result executeDeleteStatement(CompiledStatement cs)
    throws HsqlException {

        Table       table  = cs.targetTable;
        TableFilter filter = cs.targetFilter;
        int         count  = 0;

        if (filter.findFirst(session)) {
            Expression    c = cs.condition;
            HsqlArrayList del;

            del = new HsqlArrayList();

            do {
                if (c == null || c.testCondition(session)) {
                    del.add(filter.currentRow);
                }
            } while (filter.next(session));

            count = table.delete(session, del);
        }

        updateResult.updateCount = count;

        return updateResult;
    }

    /**
     * Executes an INSERT_SELECT statement.  It is assumed that the argument
     * is of the correct type.
     *
     * @param cs a CompiledStatement of type CompiledStatement.INSERT_SELECT
     * @throws HsqlException if a database access error occurs
     * @return the result of executing the statement
     */
    private Result executeInsertSelectStatement(CompiledStatement cs)
    throws HsqlException {

        Table     t   = cs.targetTable;
        Select    s   = cs.select;
        int[]     ct  = t.getColumnTypes();    // column types
        Result    r   = s.getResult(session, Integer.MAX_VALUE);
        Record    rc  = r.rRoot;
        int[]     cm  = cs.columnMap;          // column map
        boolean[] ccl = cs.checkColumns;       // column check list
        int       len = cm.length;
        Object[]  row;
        int       count;
        boolean   success = false;

        session.beginNestedTransaction();

        try {
            while (rc != null) {
                row = t.getNewRowData(session, ccl);

                for (int i = 0; i < len; i++) {
                    int j = cm[i];

                    if (ct[j] != r.metaData.colTypes[i]) {
                        row[j] = Column.convertObject(rc.data[i], ct[j]);
                    } else {
                        row[j] = rc.data[i];
                    }
                }

                rc.data = row;
                rc      = rc.next;
            }

            count   = t.insert(session, r);
            success = true;
        } finally {
            session.endNestedTransaction(!success);
        }

        updateResult.updateCount = count;

        return updateResult;
    }

    /**
     * Executes an INSERT_VALUES statement.  It is assumed that the argument
     * is of the correct type.
     *
     * @param cs a CompiledStatement of type CompiledStatement.INSERT_VALUES
     * @throws HsqlException if a database access error occurs
     * @return the result of executing the statement
     */
    private Result executeInsertValuesStatement(CompiledStatement cs)
    throws HsqlException {

        Table        t    = cs.targetTable;
        Object[]     row  = t.getNewRowData(session, cs.checkColumns);
        int[]        cm   = cs.columnMap;        // column map
        Expression[] acve = cs.columnValues;
        Expression   cve;
        int[]        ct = t.getColumnTypes();    // column types
        int          ci;                         // column index
        int          len = acve.length;

        for (int i = 0; i < len; i++) {
            cve     = acve[i];
            ci      = cm[i];
            row[ci] = cve.getValue(session, ct[ci]);
        }

        t.insert(session, row);

        return updateOneResult;
    }

    /**
     * Executes a SELECT statement.  It is assumed that the argument
     * is of the correct type.
     *
     * @param cs a CompiledStatement of type CompiledStatement.SELECT
     * @throws HsqlException if a database access error occurs
     * @return the result of executing the statement
     */
    private Result executeSelectStatement(CompiledStatement cs)
    throws HsqlException {

        Select select = cs.select;
        Result result;

        if (select.sIntoTable != null) {

            // session level user rights
            session.checkDDLWrite();

            boolean exists =
                session.database.schemaManager.findUserTable(
                    session, select.sIntoTable.name,
                    select.sIntoTable.schema.name) != null;

            if (exists) {
                throw Trace.error(Trace.TABLE_ALREADY_EXISTS,
                                  select.sIntoTable.name);
            }

            result = select.getResult(session, Integer.MAX_VALUE);
            result = session.dbCommandInterpreter.processSelectInto(result,
                    select.sIntoTable, select.intoType);

            session.getDatabase().setMetaDirty(false);
        } else {
            result = select.getResult(session, session.getMaxRows());
        }

        return result;
    }

    /**
     * Executes an UPDATE statement.  It is assumed that the argument
     * is of the correct type.
     *
     * @param cs a CompiledStatement of type CompiledStatement.UPDATE
     * @throws HsqlException if a database access error occurs
     * @return the result of executing the statement
     */
    private Result executeUpdateStatement(CompiledStatement cs)
    throws HsqlException {

        Table       table  = cs.targetTable;
        TableFilter filter = cs.targetFilter;
        int         count  = 0;

        if (filter.findFirst(session)) {
            int[]          colmap    = cs.columnMap;    // column map
            Expression[]   colvalues = cs.columnValues;
            Expression     condition = cs.condition;    // update condition
            int            len       = colvalues.length;
            HashMappedList rowset    = new HashMappedList();
            int            size      = table.getColumnCount();
            int[]          coltypes  = table.getColumnTypes();
            boolean        success   = false;

            do {
                if (condition == null || condition.testCondition(session)) {
                    try {
                        Row      row = filter.currentRow;
                        Object[] ni  = table.getEmptyRowData();

                        System.arraycopy(row.getData(), 0, ni, 0, size);

                        for (int i = 0; i < len; i++) {
                            int ci = colmap[i];

                            ni[ci] = colvalues[i].getValue(session,
                                                           coltypes[ci]);
                        }

                        rowset.add(row, ni);
                    } catch (HsqlInternalException e) {}
                }
            } while (filter.next(session));

            session.beginNestedTransaction();

            try {
                count   = table.update(session, rowset, colmap);
                success = true;
            } finally {

                // update failed (constraint violation) or succeeded
                session.endNestedTransaction(!success);
            }
        }

        updateResult.updateCount = count;

        return updateResult;
    }

    /**
     * Executes a DDL statement.  It is assumed that the argument
     * is of the correct type.
     *
     * @param cs a CompiledStatement of type CompiledStatement.DDL
     * @throws HsqlException if a database access error occurs
     * @return the result of executing the statement
     */
    private Result executeDDLStatement(CompiledStatement cs)
    throws HsqlException {
        return session.sqlExecuteDirectNoPreChecks(cs.sql);
    }
}
