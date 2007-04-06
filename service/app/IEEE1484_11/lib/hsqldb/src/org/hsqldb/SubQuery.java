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

import org.hsqldb.lib.ObjectComparator;

/**
 * Represents an SQL view or anonymous subquery (inline virtual table
 * descriptor) nested within an SQL statement. <p>
 *
 * Implements {@link org.hsqldb.lib.ObjectComparator ObjectComparator} to
 * provide the correct order of materialization for nested views / subqueries.
 *
 * @author boucherb@users
 * @author fredt@users
 */
class SubQuery implements ObjectComparator {

    int     level;
    boolean hasParams;
    boolean isResolved;
    boolean isExistsPredicate;
    boolean uniqueRows;
    Select  select;
    Table   table;
    View    view;
    boolean isMaterialised;

    void populateTable(Session session) throws HsqlException {

        Result r = select.getResult(session, isExistsPredicate ? 1
                                                               : 0);

        if (uniqueRows) {
            r.removeDuplicates(session, select.iResultLen);
        }

        table.insertResult(session, r);
    }

    /**
     * This results in the following sort order:
     *
     * view subqueries, then other subqueries
     *
     *    view subqueries:
     *        views sorted by creation order (earlier declaration first)
     *
     *    other subqueries:
     *        subqueries sorted by depth within select query (deep == higher level)
     *
     */
    public int compare(Object a, Object b) {

        SubQuery sqa = (SubQuery) a;
        SubQuery sqb = (SubQuery) b;

        if (sqa.view == null && sqb.view == null) {
            return sqb.level - sqa.level;
        } else if (sqa.view != null && sqb.view != null) {
            Database db = sqa.view.database;
            int      ia = db.schemaManager.getTableIndex(sqa.view);
            int      ib = db.schemaManager.getTableIndex(sqb.view);

            if (ia == -1) {
                ia = db.schemaManager.getTables(
                    sqa.view.getSchemaName()).size();
            }

            if (ib == -1) {
                ib = db.schemaManager.getTables(
                    sqb.view.getSchemaName()).size();
            }

            int diff = ia - ib;

            return diff == 0 ? sqb.level - sqa.level
                             : diff;
        } else {
            return sqa.view == null ? 1
                                    : -1;
        }
    }
}
