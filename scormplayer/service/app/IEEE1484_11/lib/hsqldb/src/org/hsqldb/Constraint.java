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
import org.hsqldb.index.RowIterator;
import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.lib.Iterator;

// fredt@users 20020225 - patch 1.7.0 by boucherb@users - named constraints
// fredt@users 20020320 - doc 1.7.0 - update
// tony_lai@users 20020820 - patch 595156 - violation of Integrity constraint name

/**
 * Implementation of a table constraint with references to the indexes used
 * by the constraint.<p>
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.8.0
 * @since Hypersonic SQL
 */
class Constraint {

    /*
     SQL CLI codes

     Referential Constraint 0 CASCADE
     Referential Constraint 1 RESTRICT
     Referential Constraint 2 SET NULL
     Referential Constraint 3 NO ACTION
     Referential Constraint 4 SET DEFAULT
     */
    static final int CASCADE        = 0,
                     SET_NULL       = 2,
                     NO_ACTION      = 3,
                     SET_DEFAULT    = 4,
                     INIT_DEFERRED  = 5,
                     INIT_IMMEDIATE = 6,
                     NOT_DEFERRABLE = 7;
    static final int FOREIGN_KEY    = 0,
                     MAIN           = 1,
                     UNIQUE         = 2,
                     CHECK          = 3,
                     PRIMARY_KEY    = 4;
    ConstraintCore   core;
    HsqlName         constName;
    int              constType;

    /**
     *  Constructor declaration for PK and UNIQUE
     */
    Constraint(HsqlName name, Table t, Index index, int type) {

        core           = new ConstraintCore();
        constName      = name;
        constType      = type;
        core.mainTable = t;
        core.mainIndex = index;
        /* fredt - in unique constraints column list for iColMain is the
           visible columns of iMain
         */
        core.mainColArray = ArrayUtil.arraySlice(index.getColumns(), 0,
                index.getVisibleColumns());
        core.colLen = core.mainColArray.length;
    }

    /**
     *  Constructor for main constraints (foreign key references in PK table)
     */
    Constraint(HsqlName name, Constraint fkconstraint) {

        constName = name;
        constType = MAIN;
        core      = fkconstraint.core;
    }

    /**
     *  Constructor for foreign key constraints.
     *
     * @param  pkname name in the main (referenced) table, used internally
     * @param  name name in the referencing table, public name of the constraint
     * @param  mainTable referenced table
     * @param  refTable referencing talbe
     * @param  mainCols array of column indexes in main table
     * @param  refCols array of column indexes in referencing table
     * @param  mainIndex index on the main table
     * @param  refIndex index on the referencing table
     * @param  deleteAction triggered action on delete
     * @param  updateAction triggered action on update
     * @exception  HsqlException
     */
    Constraint(HsqlName pkname, HsqlName name, Table mainTable,
               Table refTable, int[] mainCols, int[] refCols,
               Index mainIndex, Index refIndex, int deleteAction,
               int updateAction) throws HsqlException {

        core           = new ConstraintCore();
        core.pkName    = pkname;
        core.fkName    = name;
        constName      = name;
        constType      = FOREIGN_KEY;
        core.mainTable = mainTable;
        core.refTable  = refTable;
        /* fredt - in FK constraints column lists for iColMain and iColRef have
           identical sets to visible columns of iMain and iRef respectively
           but the order of columns can be different and must be preserved
         */
        core.mainColArray = mainCols;
        core.colLen       = core.mainColArray.length;
        core.refColArray  = refCols;
        core.mainIndex    = mainIndex;
        core.refIndex     = refIndex;
        core.deleteAction = deleteAction;
        core.updateAction = updateAction;
    }

    /**
     * temp constraint constructor
     */
    Constraint(HsqlName name, int[] mainCols, Table refTable, int[] refCols,
               int type, int deleteAction, int updateAction) {

        core              = new ConstraintCore();
        constName         = name;
        constType         = type;
        core.mainColArray = mainCols;
        core.refTable     = refTable;
        core.refColArray  = refCols;
        core.deleteAction = deleteAction;
        core.updateAction = updateAction;
    }

    private Constraint() {}

    /**
     * Returns the HsqlName.
     */
    HsqlName getName() {
        return constName;
    }

    /**
     * Changes constraint name.
     */
    private void setName(String name, boolean isquoted) throws HsqlException {
        constName.rename(name, isquoted);
    }

    /**
     *  probably a misnomer, but DatabaseMetaData.getCrossReference specifies
     *  it this way (I suppose because most FKs are declared against the PK of
     *  another table)
     *
     *  @return name of the index refereneced by a foreign key
     */
    String getPkName() {
        return core.pkName == null ? null
                                   : core.pkName.name;
    }

    /**
     *  probably a misnomer, but DatabaseMetaData.getCrossReference specifies
     *  it this way (I suppose because most FKs are declared against the PK of
     *  another table)
     *
     *  @return name of the index for the referencing foreign key
     */
    String getFkName() {
        return core.fkName == null ? null
                                   : core.fkName.name;
    }

    /**
     *  Returns the type of constraint
     */
    int getType() {
        return constType;
    }

    /**
     *  Returns the main table
     */
    Table getMain() {
        return core.mainTable;
    }

    /**
     *  Returns the main index
     */
    Index getMainIndex() {
        return core.mainIndex;
    }

    /**
     *  Returns the reference table
     */
    Table getRef() {
        return core.refTable;
    }

    /**
     *  Returns the reference index
     */
    Index getRefIndex() {
        return core.refIndex;
    }

    /**
     *  The ON DELETE triggered action of (foreign key) constraint
     */
    int getDeleteAction() {
        return core.deleteAction;
    }

    /**
     *  The ON UPDATE triggered action of (foreign key) constraint
     */
    int getUpdateAction() {
        return core.updateAction;
    }

    /**
     *  Returns the main table column index array
     */
    int[] getMainColumns() {
        return core.mainColArray;
    }

    /**
     *  Returns the reference table column index array
     */
    int[] getRefColumns() {
        return core.refColArray;
    }

    /**
     *  Returns true if an index is part this constraint and the constraint is set for
     *  a foreign key. Used for tests before dropping an index.
     */
    boolean isIndexFK(Index index) {

        if (constType == FOREIGN_KEY || constType == MAIN) {
            if (core.mainIndex == index || core.refIndex == index) {
                return true;
            }
        }

        return false;
    }

    /**
     *  Returns true if an index is part this constraint and the constraint is set for
     *  a unique constraint. Used for tests before dropping an index.
     */
    boolean isIndexUnique(Index index) {
        return (constType == UNIQUE && core.mainIndex == index);
    }

    /**
     * Only for check constraints
     */
    boolean hasColumn(Table table, String colname) {

        if (constType != CHECK) {
            return false;
        }

        Expression.Collector coll = new Expression.Collector();

        coll.addAll(core.check, Expression.COLUMN);

        Iterator it = coll.iterator();

        for (; it.hasNext(); ) {
            Expression e = (Expression) it.next();

            if (e.getColumnName().equals(colname)
                    && table.tableName.name.equals(e.getTableName())) {
                return true;
            }
        }

        return false;
    }

    boolean hasColumn(int colIndex) {

        if (constType == MAIN) {
            return ArrayUtil.find(core.mainColArray, colIndex) != -1;
        } else if (constType == FOREIGN_KEY) {
            return ArrayUtil.find(core.refColArray, colIndex) != -1;
        }

        return false;
    }

// fredt@users 20020225 - patch 1.7.0 by fredt - duplicate constraints

    /**
     * Compares this with another constraint column set. This implementation
     * only checks UNIQUE constraints.
     */
    boolean isEquivalent(int[] col, int type) {

        if (type != constType || constType != UNIQUE
                || core.colLen != col.length) {
            return false;
        }

        return ArrayUtil.haveEqualSets(core.mainColArray, col, core.colLen);
    }

    /**
     * Compares this with another constraint column set. This implementation
     * only checks FOREIGN KEY constraints.
     */
    boolean isEquivalent(Table tablemain, int[] colmain, Table tableref,
                         int[] colref) {

        if (constType != Constraint.MAIN
                && constType != Constraint.FOREIGN_KEY) {
            return false;
        }

        if (tablemain != core.mainTable || tableref != core.refTable) {
            return false;
        }

        return ArrayUtil.areEqualSets(core.mainColArray, colmain)
               && ArrayUtil.areEqualSets(core.refColArray, colref);
    }

    /**
     *  Used to update constrains to reflect structural changes in a table.
     *  Prior checks must ensure that this method does not throw.
     *
     * @param  oldt reference to the old version of the table
     * @param  newt referenct to the new version of the table
     * @param  colindex index at which table column is added or removed
     * @param  adjust -1, 0, +1 to indicate if column is added or removed
     * @throws  HsqlException
     */
    void replaceTable(Table oldt, Table newt, int colindex,
                      int adjust) throws HsqlException {

        if (oldt == core.mainTable) {
            core.mainTable = newt;

            // exclude CHECK
            if (core.mainIndex != null) {
                core.mainIndex =
                    core.mainTable.getIndex(core.mainIndex.getName().name);
                core.mainColArray =
                    ArrayUtil.toAdjustedColumnArray(core.mainColArray,
                                                    colindex, adjust);
            }
        }

        if (oldt == core.refTable) {
            core.refTable = newt;

            if (core.refIndex != null) {
                core.refIndex =
                    core.refTable.getIndex(core.refIndex.getName().name);

                if (core.refIndex != core.mainIndex) {
                    core.refColArray =
                        ArrayUtil.toAdjustedColumnArray(core.refColArray,
                                                        colindex, adjust);
                }
            }
        }
    }

    /**
     * Checks for foreign key or check constraint violation when
     * inserting a row into the child table.
     */
    void checkInsert(Session session, Object[] row) throws HsqlException {

        if (constType == Constraint.MAIN || constType == Constraint.UNIQUE
                || constType == Constraint.PRIMARY_KEY) {

            // inserts in the main table are never a problem
            // unique constraints are checked by the unique index
            return;
        }

        if (constType == Constraint.CHECK) {
            checkCheckConstraint(session, row);

            return;
        }

        if (Index.isNull(row, core.refColArray)) {
            return;
        }

        // a record must exist in the main table
        boolean exists = core.mainIndex.exists(session, row,
                                               core.refColArray);

        if (!exists) {

            // special case: self referencing table and self referencing row
            if (core.mainTable == core.refTable) {
                boolean match = true;

                for (int i = 0; i < core.colLen; i++) {
                    if (!row[core.refColArray[i]].equals(
                            row[core.mainColArray[i]])) {
                        match = false;

                        break;
                    }
                }

                if (match) {
                    return;
                }
            }

            throw Trace.error(Trace.INTEGRITY_CONSTRAINT_VIOLATION_NOPARENT,
                              Trace.Constraint_violation, new Object[] {
                core.fkName.name, core.mainTable.getName().name
            });
        }
    }

    /*
     * Tests a row against this CHECK constraint.
     */
    void checkCheckConstraint(Session session,
                              Object[] row) throws HsqlException {

        core.checkFilter.currentData = row;

        boolean nomatch = Boolean.FALSE.equals(core.check.test(session));

        core.checkFilter.currentData = null;

        if (nomatch) {
            throw Trace.error(Trace.CHECK_CONSTRAINT_VIOLATION,
                              Trace.Constraint_violation, new Object[] {
                constName.name, core.mainTable.tableName.name
            });
        }
    }

// fredt@users 20020225 - patch 1.7.0 - cascading deletes

    /**
     * New method to find any referencing row for a
     * foreign key (finds row in child table). If ON DELETE CASCADE is
     * supported by this constraint, then the method finds the first row
     * among the rows of the table ordered by the index and doesn't throw.
     * Without ON DELETE CASCADE, the method attempts to finds any row that
     * exists, in which case it throws an exception. If no row is found,
     * null is returned.
     * (fredt@users)
     *
     * @param  row array of objects for a database row
     * @param  forDelete should we allow 'ON DELETE CASCADE' or 'ON UPDATE CASCADE'
     * @return Node object or null
     * @throws  HsqlException
     */
    RowIterator findFkRef(Session session, Object[] row,
                          boolean delete) throws HsqlException {

        if (row == null || Index.isNull(row, core.mainColArray)) {
            return core.refIndex.emptyIterator();
        }

        return delete
               ? core.refIndex.findFirstRowForDelete(session, row,
                   core.mainColArray)
               : core.refIndex.findFirstRow(session, row, core.mainColArray);
    }

    /**
     * For the candidate table row, finds any referring node in the main table.
     * This is used to check referential integrity when updating a node. We
     * have to make sure that the main table still holds a valid main record.
     * If a valid row is found the corresponding <code>Node</code> is returned.
     * Otherwise a 'INTEGRITY VIOLATION' Exception gets thrown.
     */
    boolean hasMainRef(Session session, Object[] row) throws HsqlException {

        if (Index.isNull(row, core.refColArray)) {
            return false;
        }

        boolean exists = core.mainIndex.exists(session, row,
                                               core.refColArray);

        // -- there has to be a valid node in the main table
        // --
        if (!exists) {
            throw Trace.error(Trace.INTEGRITY_CONSTRAINT_VIOLATION_NOPARENT,
                              Trace.Constraint_violation, new Object[] {
                core.fkName.name, core.refTable.getName().name
            });
        }

        return exists;
    }

    /**
     * Test used before adding a new foreign key constraint. This method
     * returns true if the given row has a corresponding row in the main
     * table. Also returns true if any column covered by the foreign key
     * constraint has a null value.
     */
    private static boolean hasReferencedRow(Session session,
            Object[] rowdata, int[] rowColArray,
            Index mainIndex) throws HsqlException {

        if (Index.isNull(rowdata, rowColArray)) {
            return true;
        }

        // else a record must exist in the main index
        return mainIndex.exists(session, rowdata, rowColArray);
    }

    /**
     * Check used before creating a new foreign key cosntraint, this method
     * checks all rows of a table to ensure they all have a corresponding
     * row in the main table.
     */
    static void checkReferencedRows(Session session, Table table,
                                    int[] rowColArray,
                                    Index mainIndex) throws HsqlException {

        RowIterator it = table.getPrimaryIndex().firstRow(session);

        while (true) {
            Row row = it.next();

            if (row == null) {
                break;
            }

            Object[] rowdata = row.getData();

            if (!Constraint.hasReferencedRow(session, rowdata, rowColArray,
                                             mainIndex)) {
                String colvalues = "";

                for (int i = 0; i < rowColArray.length; i++) {
                    Object o = rowdata[rowColArray[i]];

                    colvalues += o;
                    colvalues += ",";
                }

                throw Trace.error(
                    Trace.INTEGRITY_CONSTRAINT_VIOLATION_NOPARENT,
                    Trace.Constraint_violation, new Object[] {
                    colvalues, table.getName().name
                });
            }
        }
    }
}
