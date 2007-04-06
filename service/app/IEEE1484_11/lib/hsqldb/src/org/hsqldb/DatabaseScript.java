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
import org.hsqldb.lib.HashMappedList;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.IntValueHashMap;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.StringConverter;

/**
 * Script generation.
 *
 * The core functionality of this class was inherited from Hypersonic and
 * extensively rewritten and extended in successive versions of HSQLDB.<p>
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @author fredt@users
 * @version 1.8.0
 * @since 1.7.0
 */
public class DatabaseScript {

    /**
     * Returns the DDL and all other statements for the database excluding
     * INSERT and SET <tablename> READONLY statements.
     * cachedData == true indicates that SET <tablename> INDEX statements should
     * also be included.
     *
     * This class should not have any dependencies on metadata reporting.
     */
    public static Result getScript(Database database, boolean indexRoots) {

        Iterator it;
        Result   r = Result.newSingleColumnResult("COMMAND", Types.VARCHAR);

        r.metaData.tableNames[0] = "SYSTEM_SCRIPT";

        // collation for database
        if (database.collation.name != null) {
            String name =
                StringConverter.toQuotedString(database.collation.name, '"',
                                               true);

            addRow(r, "SET DATABASE COLLATION " + name);
        }

        // Role definitions
        it = database.getGranteeManager().getRoleNames().iterator();

        String role;

        while (it.hasNext()) {
            role = (String) it.next();

            // ADMIN_ROLE_NAME is not persisted
            if (!GranteeManager.DBA_ADMIN_ROLE_NAME.equals(role)) {
                addRow(r, "CREATE ROLE " + role);
            }
        }

        // aliases
        HashMap h       = database.getAliasMap();
        HashMap builtin = Library.getAliasMap();

        it = h.keySet().iterator();

        while (it.hasNext()) {
            String alias  = (String) it.next();
            String java   = (String) h.get(alias);
            String biJava = (String) builtin.get(alias);

            if (biJava != null && biJava.equals(java)) {
                continue;
            }

            StringBuffer buffer = new StringBuffer(64);

            buffer.append(Token.T_CREATE).append(' ').append(
                Token.T_ALIAS).append(' ');
            buffer.append(alias);
            buffer.append(" FOR \"");
            buffer.append(java);
            buffer.append('"');
            addRow(r, buffer.toString());
        }

        addSchemaStatements(database, r, indexRoots);

        // rights for classes, tables and views
        addRightsStatements(database, r);

        if (database.logger.hasLog()) {
            int     delay  = database.logger.getWriteDelay();
            boolean millis = delay < 1000;

            if (millis) {
                if (delay != 0 && delay < 20) {
                    delay = 20;
                }
            } else {
                delay /= 1000;
            }

            String statement = "SET WRITE_DELAY " + delay
                               + (millis ? " MILLIS"
                                         : "");

            addRow(r, statement);
        }

        return r;
    }

    static void addSchemaStatements(Database database, Result r,
                                    boolean indexRoots) {

        Iterator schemas = database.schemaManager.userSchemaNameIterator();

        while (schemas.hasNext()) {
            String schemaKey = (String) schemas.next();
            HsqlName schema =
                database.schemaManager.toSchemaHsqlName(schemaKey);
            HashMappedList tTable =
                database.schemaManager.getTables(schema.name);
            HsqlArrayList forwardFK = new HsqlArrayList();

            // schema creation
            {
                String ddl = getSchemaCreateDDL(database, schema);

                addRow(r, ddl);
            }

            // sequences
            /*
                     CREATE SEQUENCE <name>
                     [AS {INTEGER | BIGINT}]
                     [START WITH <value>]
                     [INCREMENT BY <value>]
             */
            Iterator it =
                database.schemaManager.sequenceIterator(schema.name);

            while (it.hasNext()) {
                NumberSequence seq = (NumberSequence) it.next();
                StringBuffer   a   = new StringBuffer(128);

                a.append(Token.T_CREATE).append(' ');
                a.append(Token.T_SEQUENCE).append(' ');
                a.append(seq.getName().statementName).append(' ');
                a.append(Token.T_AS).append(' ');
                a.append(Types.getTypeString(seq.getType())).append(' ');
                a.append(Token.T_START).append(' ');
                a.append(Token.T_WITH).append(' ');
                a.append(seq.peek()).append(' ');

                if (seq.getIncrement() != 1) {
                    a.append(Token.T_INCREMENT).append(' ');
                    a.append(Token.T_BY).append(' ');
                    a.append(seq.getIncrement()).append(' ');
                }

                addRow(r, a.toString());
            }

            // tables
            for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
                Table t = (Table) tTable.get(i);

                if (t.isView()) {
                    continue;
                }

                StringBuffer a = new StringBuffer(128);

                getTableDDL(database, t, i, forwardFK, false, a);
                addRow(r, a.toString());

                // indexes for table
                for (int j = 1; j < t.getIndexCount(); j++) {
                    Index index = t.getIndex(j);

                    if (HsqlName.isReservedIndexName(index.getName().name)) {

                        // the following are autocreated with the table
                        // indexes for primary keys
                        // indexes for unique constraints
                        // own table indexes for foreign keys
                        continue;
                    }

                    a = new StringBuffer(64);

                    a.append(Token.T_CREATE).append(' ');

                    if (index.isUnique()) {
                        a.append(Token.T_UNIQUE).append(' ');
                    }

                    a.append(Token.T_INDEX).append(' ');
                    a.append(index.getName().statementName);
                    a.append(' ').append(Token.T_ON).append(' ');
                    a.append(t.getName().statementName);

                    int[] col = index.getColumns();
                    int   len = index.getVisibleColumns();

                    getColumnList(t, col, len, a);
                    addRow(r, a.toString());
                }

                // readonly for TEXT tables only
                if (t.isText() && t.isDataReadOnly()) {
                    a = new StringBuffer(64);

                    a.append(Token.T_SET).append(' ').append(
                        Token.T_TABLE).append(' ');
                    a.append(t.getName().statementName);
                    a.append(' ').append(Token.T_READONLY).append(' ').append(
                        Token.T_TRUE);
                    addRow(r, a.toString());
                }

                // data source
                String dataSource = getDataSource(t);

                if (dataSource != null) {
                    addRow(r, dataSource);
                }

                // header
                String header = getDataSourceHeader(t);

                if (!indexRoots && header != null) {
                    addRow(r, header);
                }

                // triggers
                int numTrigs = TriggerDef.NUM_TRIGS;

                for (int tv = 0; tv < numTrigs; tv++) {
                    HsqlArrayList trigVec = t.triggerLists[tv];

                    if (trigVec == null) {
                        continue;
                    }

                    int trCount = trigVec.size();

                    for (int k = 0; k < trCount; k++) {
                        a = ((TriggerDef) trigVec.get(k)).getDDL();

                        addRow(r, a.toString());
                    }
                }
            }

            // forward referencing foreign keys
            for (int i = 0, tSize = forwardFK.size(); i < tSize; i++) {
                Constraint   c = (Constraint) forwardFK.get(i);
                StringBuffer a = new StringBuffer(128);

                a.append(Token.T_ALTER).append(' ').append(
                    Token.T_TABLE).append(' ');
                a.append(c.getRef().getName().statementName);
                a.append(' ').append(Token.T_ADD).append(' ');
                getFKStatement(c, a);
                addRow(r, a.toString());
            }

            // SET <tablename> INDEX statements
            Session sysSession = database.sessionManager.getSysSession();

            for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
                Table t = (Table) tTable.get(i);

                if (indexRoots && t.isIndexCached()
                        &&!t.isEmpty(sysSession)) {
                    addRow(r, getIndexRootsDDL((Table) tTable.get(i)));
                }
            }

            // RESTART WITH <value> statements
            for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
                Table t = (Table) tTable.get(i);

                if (!t.isTemp()) {
                    String ddl = getIdentityUpdateDDL(t);

                    addRow(r, ddl);
                }
            }

            // views
            for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
                Table t = (Table) tTable.get(i);

                if (t.isView()) {
                    View         v = (View) tTable.get(i);
                    StringBuffer a = new StringBuffer(128);

                    a.append(Token.T_CREATE).append(' ').append(
                        Token.T_VIEW).append(' ');
                    a.append(v.getName().statementName).append(' ').append(
                        '(');

                    int count = v.getColumnCount();

                    for (int j = 0; j < count; j++) {
                        a.append(v.getColumn(j).columnName.statementName);

                        if (j < count - 1) {
                            a.append(',');
                        }
                    }

                    a.append(')').append(' ').append(Token.T_AS).append(' ');
                    a.append(v.getStatement());
                    addRow(r, a.toString());
                }
            }
        }
    }

    static String getIdentityUpdateDDL(Table t) {

        if (t.identityColumn == -1) {
            return "";
        } else {
            String tablename = t.getName().statementName;
            String colname =
                t.getColumn(t.identityColumn).columnName.statementName;
            long         idval = t.identitySequence.peek();
            StringBuffer a     = new StringBuffer(128);

            a.append(Token.T_ALTER).append(' ').append(Token.T_TABLE).append(
                ' ').append(tablename).append(' ').append(
                Token.T_ALTER).append(' ').append(Token.T_COLUMN).append(
                ' ').append(colname).append(' ').append(
                Token.T_RESTART).append(' ').append(Token.T_WITH).append(
                ' ').append(idval);

            return a.toString();
        }
    }

    static String getIndexRootsDDL(Table t) {

        StringBuffer a = new StringBuffer(128);

        a.append(Token.T_SET).append(' ').append(Token.T_TABLE).append(' ');
        a.append(t.getName().statementName);
        a.append(' ').append(Token.T_INDEX).append('\'');
        a.append(t.getIndexRoots());
        a.append('\'');

        return a.toString();
    }

    static String getSchemaCreateDDL(Database database, HsqlName schemaName) {

        StringBuffer ab = new StringBuffer(128);

        ab.append(Token.T_CREATE).append(' ');
        ab.append(Token.T_SCHEMA).append(' ');
        ab.append(schemaName.statementName).append(' ');
        ab.append(Token.T_AUTHORIZATION).append(' ');
        ab.append(GranteeManager.DBA_ADMIN_ROLE_NAME);

        return ab.toString();
    }

    static void getTableDDL(Database database, Table t, int i,
                            HsqlArrayList forwardFK, boolean useSchema,
                            StringBuffer a) {

        a.append(Token.T_CREATE).append(' ');

        if (t.isTemp) {
            a.append(Token.T_GLOBAL).append(' ');
            a.append(Token.T_TEMPORARY).append(' ');
        }

        if (t.isText()) {
            a.append(Token.T_TEXT).append(' ');
        } else if (t.isCached()) {
            a.append(Token.T_CACHED).append(' ');
        } else {
            a.append(Token.T_MEMORY).append(' ');
        }

        a.append(Token.T_TABLE).append(' ');

        if (useSchema) {
            a.append(t.getName().schema.statementName).append('.');
        }

        a.append(t.getName().statementName);
        a.append('(');

        int        columns = t.getColumnCount();
        int[]      pk      = t.getPrimaryKey();
        HsqlName   pkName  = null;
        Constraint pkConst = t.getPrimaryConstraint();

        if (pkConst != null &&!pkConst.getName().isReservedIndexName()) {
            pkName = pkConst.getName();
        }

        for (int j = 0; j < columns; j++) {
            Column column  = t.getColumn(j);
            String colname = column.columnName.statementName;

            a.append(colname);
            a.append(' ');

            String sType = Types.getTypeString(column.getType());

            a.append(sType);

            // append the size and scale if > 0
            boolean hasSize = false;

            if (column.getType() == Types.TIMESTAMP) {
                if (column.getSize() != 6) {
                    hasSize = true;
                }
            } else {
                hasSize = column.getSize() > 0;
            }

            if (hasSize) {
                a.append('(');
                a.append(column.getSize());

                if (column.getScale() > 0) {
                    a.append(',');
                    a.append(column.getScale());
                }

                a.append(')');
            }

            String defaultString = column.getDefaultDDL();

            if (defaultString != null) {
                a.append(' ').append(Token.T_DEFAULT).append(' ');
                a.append(defaultString);
            }

            if (j == t.getIdentityColumn()) {
                a.append(" GENERATED BY DEFAULT AS IDENTITY(START WITH ");
                a.append(column.identityStart);

                if (column.identityIncrement != 1) {
                    a.append(Token.T_COMMA).append(Token.T_INCREMENT).append(
                        ' ').append(Token.T_BY).append(' ');
                    a.append(column.identityIncrement);
                }

                a.append(")");
            }

            if (!column.isNullable()) {
                a.append(' ').append(Token.T_NOT).append(' ').append(
                    Token.T_NULL);
            }

            if ((pk.length == 1) && (j == pk[0]) && pkName == null) {
                a.append(' ').append(Token.T_PRIMARY).append(' ').append(
                    Token.T_KEY);
            }

            if (j < columns - 1) {
                a.append(',');
            }
        }

        if (pk.length > 1 || (pk.length == 1 && pkName != null)) {
            a.append(',');

            if (pkName != null) {
                a.append(Token.T_CONSTRAINT).append(' ');
                a.append(pkName.statementName).append(' ');
            }

            a.append(Token.T_PRIMARY).append(' ').append(Token.T_KEY);
            getColumnList(t, pk, pk.length, a);
        }

        Constraint[] v = t.getConstraints();

        for (int j = 0, vSize = v.length; j < vSize; j++) {
            Constraint c = v[j];

            switch (c.getType()) {

                case Constraint.UNIQUE :
                    a.append(',').append(Token.T_CONSTRAINT).append(' ');
                    a.append(c.getName().statementName);
                    a.append(' ').append(Token.T_UNIQUE);

                    int[] col = c.getMainColumns();

                    getColumnList(c.getMain(), col, col.length, a);
                    break;

                case Constraint.FOREIGN_KEY :

                    // forward referencing FK
                    Table maintable = c.getMain();
                    int maintableindex =
                        database.schemaManager.getTableIndex(maintable);

                    if (maintableindex > i) {
                        forwardFK.add(c);
                    } else {
                        a.append(',');
                        getFKStatement(c, a);
                    }
                    break;

                case Constraint.CHECK :
                    try {
                        a.append(',').append(Token.T_CONSTRAINT).append(' ');
                        a.append(c.getName().statementName);
                        a.append(' ').append(Token.T_CHECK).append('(');
                        a.append(c.core.check.getDDL());
                        a.append(')');
                    } catch (HsqlException e) {

                        // should not throw as it is already tested OK
                    }
                    break;
            }
        }

        a.append(')');

        if (t.onCommitPreserve) {
            a.append(' ').append(Token.T_ON).append(' ');
            a.append(Token.T_COMMIT).append(' ').append(Token.T_PRESERVE);
            a.append(' ').append(Token.T_ROWS);
        }
    }

    /**
     * Generates the SET TABLE <tablename> SOURCE <string> statement for a
     * text table;
     */
    static String getDataSource(Table t) {

        String dataSource = t.getDataSource();

        if (dataSource == null) {
            return null;
        }

        boolean      isDesc = t.isDescDataSource();
        StringBuffer a      = new StringBuffer(128);

        a.append(Token.T_SET).append(' ').append(Token.T_TABLE).append(' ');
        a.append(t.getName().statementName);
        a.append(' ').append(Token.T_SOURCE).append(' ').append('"');
        a.append(dataSource);
        a.append('"');

        if (isDesc) {
            a.append(' ').append(Token.T_DESC);
        }

        return a.toString();
    }

    /**
     * Generates the SET TABLE <tablename> SOURCE HEADER <string> statement for a
     * text table;
     */
    static String getDataSourceHeader(Table t) {

        String header = t.getHeader();

        if (header == null) {
            return null;
        }

        StringBuffer a = new StringBuffer(128);

        a.append(Token.T_SET).append(' ').append(Token.T_TABLE).append(' ');
        a.append(t.getName().statementName);
        a.append(' ').append(Token.T_SOURCE).append(' ');
        a.append(Token.T_HEADER).append(' ');
        a.append(header);

        return a.toString();
    }

    /**
     * Generates the column definitions for a table.
     */
    private static void getColumnList(Table t, int[] col, int len,
                                      StringBuffer a) {

        a.append('(');

        for (int i = 0; i < len; i++) {
            a.append(t.getColumn(col[i]).columnName.statementName);

            if (i < len - 1) {
                a.append(',');
            }
        }

        a.append(')');
    }

    /**
     * Generates the foreign key declaration for a given Constraint object.
     */
    private static void getFKStatement(Constraint c, StringBuffer a) {

        a.append(Token.T_CONSTRAINT).append(' ');
        a.append(c.getName().statementName);
        a.append(' ').append(Token.T_FOREIGN).append(' ').append(Token.T_KEY);

        int[] col = c.getRefColumns();

        getColumnList(c.getRef(), col, col.length, a);
        a.append(' ').append(Token.T_REFERENCES).append(' ');
        a.append(c.getMain().getName().statementName);

        col = c.getMainColumns();

        getColumnList(c.getMain(), col, col.length, a);

        if (c.getDeleteAction() != Constraint.NO_ACTION) {
            a.append(' ').append(Token.T_ON).append(' ').append(
                Token.T_DELETE).append(' ');
            a.append(getFKAction(c.getDeleteAction()));
        }

        if (c.getUpdateAction() != Constraint.NO_ACTION) {
            a.append(' ').append(Token.T_ON).append(' ').append(
                Token.T_UPDATE).append(' ');
            a.append(getFKAction(c.getUpdateAction()));
        }
    }

    /**
     * Returns the foreign key action rule.
     */
    private static String getFKAction(int action) {

        switch (action) {

            case Constraint.CASCADE :
                return Token.T_CASCADE;

            case Constraint.SET_DEFAULT :
                return Token.T_SET + ' ' + Token.T_DEFAULT;

            case Constraint.SET_NULL :
                return Token.T_SET + ' ' + Token.T_NULL;

            default :
                return Token.T_NO + ' ' + Token.T_ACTION;
        }
    }

    /**
     * Adds a script line to the result.
     */
    private static void addRow(Result r, String sql) {

        if (sql == null || sql.length() == 0) {
            return;
        }

        String[] s = new String[1];

        s[0] = sql;

        r.add(s);
    }

    /**
     * Generates the GRANT statements for grantees.
     *
     * When views is true, generates rights for views only. Otherwise generates
     * rights for tables and classes.
     *
     * Does not generate script for:
     *
     * grant on builtin classes to public
     * grant select on system tables
     *
     */
    private static void addRightsStatements(Database dDatabase, Result r) {

        StringBuffer   a;
        HashMappedList userlist = dDatabase.getUserManager().getUsers();
        Iterator       users    = userlist.values().iterator();
        GranteeManager gm       = dDatabase.getGranteeManager();
        Iterator       grantees = gm.getGrantees().iterator();

        for (; users.hasNext(); ) {
            User   u    = (User) users.next();
            String name = u.getName();

            // PUBLIC user is not persisted.  (However, his
            // grants/revokes are).  _SYSTEM user not in user list.
            if (!name.equals(Token.T_PUBLIC)) {
                addRow(r, u.getCreateUserDDL());
            }
        }

        // grantees has ALL Users and Roles, incl. hidden and reserved ones.
        // Therefore, we filter out the non-persisting ones.
        for (; grantees.hasNext(); ) {
            Grantee g    = (Grantee) grantees.next();
            String  name = g.getName();

            // _SYSTEM user, DBA Role grants/revokes not persisted
            if (name.equals("_SYSTEM") || name.equals("DBA")) {
                continue;
            }

            String roleString = g.allRolesString();

            if (roleString != null) {
                addRow(r, "GRANT " + roleString + " TO " + name);
            }

            IntValueHashMap rightsmap = g.getRights();

            if (rightsmap == null) {
                continue;
            }

            Iterator dbobjects = rightsmap.keySet().iterator();

            while (dbobjects.hasNext()) {
                Object nameobject = dbobjects.next();
                int    right      = rightsmap.get(nameobject, 0);

                a = new StringBuffer(64);

                a.append(Token.T_GRANT).append(' ');
                a.append(GranteeManager.getRightsList(right));
                a.append(' ').append(Token.T_ON).append(' ');

                if (nameobject instanceof String) {
                    if (nameobject.equals("java.lang.Math")
                            || nameobject.equals("org.hsqldb.Library")) {
                        continue;
                    }

                    a.append("CLASS \"");
                    a.append((String) nameobject);
                    a.append('\"');
                } else {
                    HsqlName hsqlname = (HsqlName) nameobject;

                    // assumes all non String objects are table names
                    Table table = dDatabase.schemaManager.findUserTable(null,
                        hsqlname.name, hsqlname.schema.name);

                    // either table != null or is system table
                    if (table != null) {
                        a.append(hsqlname.schema.statementName).append(
                            '.').append(hsqlname.statementName);
                    } else {
                        continue;
                    }
                }

                a.append(' ').append(Token.T_TO).append(' ');
                a.append(g.getName());
                addRow(r, a.toString());
            }
        }
    }
}
