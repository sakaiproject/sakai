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
import org.hsqldb.lib.HashMap;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.WrapperIterator;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.store.ValuePool;

// fredt@users - 1.7.2 - structural modifications to allow inheritance
// boucherb@users - 1.7.2 - 20020225
// - factored out all reusable code into DIXXX support classes
// - completed Fred's work on allowing inheritance
// boucherb@users - 1.7.2 - 20020304 - bug fixes, refinements, better java docs
// fredt@users - 1.8.0 - updated to report latest enhancements and changes

/**
 * Produces a collection of tables that form the system data dictionary. <p>
 *
 * Implementations use a group of arrays of equal size to store various
 * attributes or cached instances of system tables.<p>
 *
 * Two fixed static lists of reserved table names are kept in String[] and
 * HsqlName[] forms. These are shared by all implementations of
 * DatabaseInformtion.<p>
 *
 * Each implementation keeps a lookup set of names for those tables whose
 * contents are never cached (nonCachedTablesSet). <p>
 *
 * An instance of this class uses three lists named sysTablexxxx for caching
 * system tables.<p>
 *
 * sysTableSessionDependent indicates which tables contain data that is
 * dependent on the user rights of the User associatiod with the Session.<p>
 *
 * sysTableSessions contains the Session with whose rights each cached table
 * was built.<p>
 *
 * sysTables contains the cached tables.<p>
 *
 * At the time of instantiation, which is part of the Database.open() method
 * invocation, an empty table is created and placed in sysTables with calls to
 * generateTable(int) for each name in sysTableNames. Some of these
 * table entries may be null if an implementation does not produce them.<p>
 *
 * Calls to getSystemTable(String, Session) return a cached table if various
 * caching rules are met (see below), or it will delete all rows of the table
 * and rebuild the contents via generateTable(int).<p>
 *
 * generateTable(int) calls the appropriate single method for each table.
 * These methods either build and return an empty table (if sysTables
 * contains null for the table slot) or populate the table with up-to-date
 * rows. <p>
 *
 * When the setDirty() call is made externally, the internal isDirty flag
 * is set. This flag is used next time a call to
 * getSystemTable(String, Session) is made. <p>
 *
 * Rules for caching are applied as follows: <p>
 *
 * When a call to getSystemTable(String, Session) is made, if the isDirty flag
 * is true, then the contents of all cached tables are cleared and the
 * sysTableUsers slot for all tables is set to null. This also has the
 * effect of clearing the isDirty and isDirtyNextIdentity flags<p>
 *
 * if the isDirtyNextIdentity flag is true at this point, then the contents
 * of all next identity value dependent cached tables are cleared and the
 * sysTableUsers slot for these tables are set to null.  Currently,
 * the only member of this set is the SYSTEM_TABLES system table.
 *
 * If a table has non-cached contents, its contents are cleared and
 * rebuilt. <p>
 *
 * For the rest of the tables, if the sysTableSessions slot is null or if the
 * Session parameter is not the same as the Session object
 * in that slot, the table contents are cleared and rebuilt. <p>
 *
 * (fredt@users) <p>
 * @author boucherb@users
 * @version 1.8.0
 * @since 1.7.2
 */
class DatabaseInformationMain extends DatabaseInformation {

    // HsqlName objects for the system tables

    /** The HsqlNames of the system tables. */
    protected static final HsqlName[] sysTableHsqlNames;

    /** Current user for each cached system table */
    protected final int[] sysTableSessions = new int[sysTableNames.length];

    /** true if the contents of a cached system table depends on the session */
    protected final boolean[] sysTableSessionDependent =
        new boolean[sysTableNames.length];

    /** cache of system tables */
    protected final Table[] sysTables = new Table[sysTableNames.length];

    /** Set: { names of system tables that are not to be cached } */
    protected static final HashSet nonCachedTablesSet;

    /**
     * Map: simple <code>Column</code> name <code>String</code> object =>
     * <code>HsqlName</code> object.
     */
    protected static final HashMap columnNameMap;

    /**
     * The <code>Session</code> object under consideration in the current
     * executution context.
     */
    protected Session session;

    /** The table types HSQLDB supports. */
    protected static final String[] tableTypes = new String[] {
        "GLOBAL TEMPORARY", "SYSTEM TABLE", "TABLE", "VIEW"
    };

    /** Provides naming support. */
    protected DINameSpace ns;

    static {
        columnNameMap      = new HashMap();
        nonCachedTablesSet = new HashSet();
        sysTableHsqlNames  = new HsqlName[sysTableNames.length];

        for (int i = 0; i < sysTableNames.length; i++) {
            sysTableHsqlNames[i] =
                HsqlNameManager.newHsqlSystemObjectName(sysTableNames[i]);
            sysTableHsqlNames[i].schema =
                SchemaManager.INFORMATION_SCHEMA_HSQLNAME;
        }

        // build the set of non-cached tables
        nonCachedTablesSet.add("SYSTEM_CACHEINFO");
        nonCachedTablesSet.add("SYSTEM_SESSIONINFO");
        nonCachedTablesSet.add("SYSTEM_SESSIONS");
        nonCachedTablesSet.add("SYSTEM_PROPERTIES");
        nonCachedTablesSet.add("SYSTEM_SEQUENCES");
    }

    /**
     * Constructs a table producer which provides system tables
     * for the specified <code>Database</code> object. <p>
     *
     * <b>Note:</b> before 1.7.2 Alpha N, it was important to observe that
     * by specifying an instance of this class or one of its descendents to
     * handle system table production, the new set of builtin permissions
     * and aliases would overwrite those of an existing database, meaning that
     * metadata reporting might have been rendered less secure if the same
     * database were then opened again using a lower numbered system table
     * producer instance (i.e. one in a 1.7.1 or earlier distribution).
     * As of 1.7.2 Alpha N, system-generated permissions and aliases are no
     * longer recorded in the checkpoint script, obseleting this issue.
     * Checkpointing of system-generated grants and aliases was removed
     * because their existence is very close to a core requirment for correct
     * operation and they are reintroduced to the system at each startup.
     * In a future release, it may even be an exception condition to attempt
     * to remove or alter system-generated grants and aliases,
     * respectvely. <p>
     *
     * @param db the <code>Database</code> object for which this object
     *      produces system tables
     * @throws HsqlException if a database access error occurs
     */
    DatabaseInformationMain(Database db) throws HsqlException {

        super(db);

        init();
    }

    /**
     * Adds a <code>Column</code> object with the specified name, data type,
     * data size and nullability to the specified <code>Table</code>
     * object. <p>
     *
     * @param t the table to which to add the specified column
     * @param name the name of the column
     * @param type the data type of the column
     * @param size the precision/length of the column
     * @param nullable <code>true</code> if the column is to allow null values,
     *      else <code>false</code>
     * @throws HsqlException if a problem occurs when adding the
     *      column (e.g. duplicate name)
     */
    protected final void addColumn(Table t, String name, int type, int size,
                                   boolean nullable) throws HsqlException {

        HsqlName cn;
        Column   c;

        cn = (HsqlName) columnNameMap.get(name);

        if (cn == null) {
            cn = database.nameManager.newHsqlName(name, false);

            columnNameMap.put(name, cn);
        }

        c = new Column(cn, nullable, type, size, 0, false, null);

        t.addColumn(c);
    }

    /**
     * Adds a <code>Column</code> object with the specified name, data type
     * and nullability to the specified <code>Table</code> object. <p>
     *
     * @param t the table to which to add the specified column
     * @param name the name of the column
     * @param type the data type of the column
     * @param nullable <code>true</code> if the column is to allow null values,
     *      else <code>false</code>
     * @throws HsqlException if a problem occurs when adding the
     *      column (e.g. duplicate name)
     */
    protected final void addColumn(Table t, String name, int type,
                                   boolean nullable) throws HsqlException {
        addColumn(t, name, type, 0, nullable);
    }

    /**
     * Adds a nullable <code>Column</code> object with the specified name and
     * data type to the specified <code>Table</code> object. <p>
     *
     * @param t the table to which to add the specified column
     * @param name the name of the column
     * @param type the data type of the column
     * @throws HsqlException if a problem occurs when adding the
     *      column (e.g. duplicate name)
     */
    protected final void addColumn(Table t, String name,
                                   int type) throws HsqlException {
        addColumn(t, name, type, true);
    }

    /**
     * Retrieves an enumeration over all of the tables in this database.
     * This means all user tables, views, system tables, system views,
     * including temporary and text tables. <p>
     *
     * @return an enumeration over all of the tables in this database
     */
    protected final Iterator allTables() {
        return new WrapperIterator(database.schemaManager.allTablesIterator(),
                                   new WrapperIterator(sysTables, true));
    }

    /**
     * Clears the contents of cached system tables and resets user slots
     * to null. <p>
     *
     * @throws HsqlException if a database access error occurs
     */
    protected final void cacheClear() throws HsqlException {

        int i = sysTables.length;

        while (i-- > 0) {
            Table t = sysTables[i];

            if (t != null) {
                t.clearAllRows(session);
            }

            sysTableSessions[i] = -1;
        }

        isDirty = false;
    }

    /**
     * Retrieves the system table corresponding to the specified
     * tableIndex value. <p>
     *
     * @param tableIndex int value identifying the system table to generate
     * @throws HsqlException if a database access error occurs
     * @return the system table corresponding to the specified tableIndex value
     */
    protected Table generateTable(int tableIndex) throws HsqlException {

        Table t = sysTables[tableIndex];

//        Please note that this class produces non-null tables for
//        just those absolutely essential to the JDBC 1 spec and the
//        HSQLDB core.  Also, all table producing methods except
//        SYSTEM_PROCEDURES() and SYSTEM_PROCEDURECOLUMNS() are declared final;
//        this class produces only an empty table for each, as per previous
//        DatabaseInformation implementations, whereas
//        DatabaseInformationFull produces comprehensive content for
//        them).
//
//        This break down of inheritance allows DatabaseInformation and
//        DatabaseInformationMain (this class) to be made as small as possible
//        while still meeting their mandates:
//
//        1.) DatabaseInformation prevents use of reserved system table names
//            for user tables and views, meaning that even under highly
//            constrained use cases where the notion of DatabaseMetaData can
//            be discarded (i.e. the engine operates in a distribution where
//            DatabaseInforationMain/Full and jdbcDatabaseMetaData have been
//            dropped from the JAR), it is still impossible to produce a
//            database which will be incompatible in terms of system table <=>
//            user table name clashes, if/when imported into a more
//            capable operating environment.
//
//        2.) DatabaseInformationMain builds on DatabaseInformation, providing
//            at minimum what is needed for comprehensive operation under
//            JDK 1.1/JDBC 1 and provides, at minimum, what was provided under
//            earlier implementations.
//
//        3.) descendents of DatabaseInformationMain (such as the current
//            DatabaseInformationFull) need not (and indeed: now cannot)
//            override most of the DatabaseInformationMain table producing
//            methods, as for the most part they are expected to be already
//            fully comprehensive, security aware and accessible to all users.
        switch (tableIndex) {

            case SYSTEM_BESTROWIDENTIFIER :
                return SYSTEM_BESTROWIDENTIFIER();

            case SYSTEM_CATALOGS :
                return SYSTEM_CATALOGS();

            case SYSTEM_COLUMNPRIVILEGES :
                return SYSTEM_COLUMNPRIVILEGES();

            case SYSTEM_COLUMNS :
                return SYSTEM_COLUMNS();

            case SYSTEM_CROSSREFERENCE :
                return SYSTEM_CROSSREFERENCE();

            case SYSTEM_INDEXINFO :
                return SYSTEM_INDEXINFO();

            case SYSTEM_PRIMARYKEYS :
                return SYSTEM_PRIMARYKEYS();

            case SYSTEM_PROCEDURECOLUMNS :
                return SYSTEM_PROCEDURECOLUMNS();

            case SYSTEM_PROCEDURES :
                return SYSTEM_PROCEDURES();

            case SYSTEM_SCHEMAS :
                return SYSTEM_SCHEMAS();

            case SYSTEM_TABLEPRIVILEGES :
                return SYSTEM_TABLEPRIVILEGES();

            case SYSTEM_TABLES :
                return SYSTEM_TABLES();

            case SYSTEM_TABLETYPES :
                return SYSTEM_TABLETYPES();

            case SYSTEM_TYPEINFO :
                return SYSTEM_TYPEINFO();

            case SYSTEM_USERS :
                return SYSTEM_USERS();

            // required by SYSTEM_TYPEINFRO
            case SYSTEM_ALLTYPEINFO :
                return SYSTEM_ALLTYPEINFO();

            case SYSTEM_CHECK_CONSTRAINTS :
                return SYSTEM_CHECK_CONSTRAINTS();

            case SYSTEM_SEQUENCES :
                return SYSTEM_SEQUENCES();

            default :
                return null;
        }
    }

    /**
     * One time initialisation of instance attributes
     * at construction time. <p>
     *
     * @throws HsqlException if a database access error occurs
     */
    protected final void init() throws HsqlException {

        ns = new DINameSpace(database);

        // flag the Session-dependent cached tables
        sysTableSessionDependent[SYSTEM_ALIASES] =
            sysTableSessionDependent[SYSTEM_CLASSPRIVILEGES] =
            sysTableSessionDependent[SYSTEM_BESTROWIDENTIFIER] =
            sysTableSessionDependent[SYSTEM_COLUMNPRIVILEGES] =
            sysTableSessionDependent[SYSTEM_COLUMNS] =
            sysTableSessionDependent[SYSTEM_CROSSREFERENCE] =
            sysTableSessionDependent[SYSTEM_INDEXINFO] =
            sysTableSessionDependent[SYSTEM_PRIMARYKEYS] =
            sysTableSessionDependent[SYSTEM_PROCEDURES] =
            sysTableSessionDependent[SYSTEM_PROCEDURECOLUMNS] =
            sysTableSessionDependent[SYSTEM_TABLEPRIVILEGES] =
            sysTableSessionDependent[SYSTEM_TABLES] =
            sysTableSessionDependent[SYSTEM_TRIGGERCOLUMNS] =
            sysTableSessionDependent[SYSTEM_TRIGGERS] =
            sysTableSessionDependent[SYSTEM_VIEWS] =
            sysTableSessionDependent[SYSTEM_TEXTTABLES] =
            sysTableSessionDependent[SYSTEM_CHECK_CONSTRAINTS] =
            sysTableSessionDependent[SYSTEM_SEQUENCES] =
            sysTableSessionDependent[SYSTEM_USAGE_PRIVILEGES] =
            sysTableSessionDependent[SYSTEM_TABLE_CONSTRAINTS] =
            sysTableSessionDependent[SYSTEM_CHECK_COLUMN_USAGE] =
            sysTableSessionDependent[SYSTEM_CHECK_ROUTINE_USAGE] =
            sysTableSessionDependent[SYSTEM_CHECK_TABLE_USAGE] =
            sysTableSessionDependent[SYSTEM_VIEW_COLUMN_USAGE] =
            sysTableSessionDependent[SYSTEM_VIEW_TABLE_USAGE] =
            sysTableSessionDependent[SYSTEM_VIEW_ROUTINE_USAGE] =
            sysTableSessionDependent[SYSTEM_AUTHORIZATIONS] = true;

        Table t;

/*
        Session oldSession = session;

        session = database.sessionManager.getSysSession(
            database.schemaManager.INFORMATION_SCHEMA);
*/
        for (int i = 0; i < sysTables.length; i++) {
            t = sysTables[i] = generateTable(i);

            if (t != null) {
                t.setDataReadOnly(true);
            }
        }

        GranteeManager gm = database.getGranteeManager();

        for (int i = 0; i < sysTableHsqlNames.length; i++) {
            if (sysTables[i] != null) {
                gm.grant(GranteeManager.PUBLIC_ROLE_NAME,
                         sysTableHsqlNames[i], UserManager.SELECT);
            }
        }
/*
        session = oldSession;
*/
    }

    /**
     * Retrieves whether any form of SQL access is allowed against the
     * the specified table w.r.t the database access rights
     * assigned to current Session object's User. <p>
     *
     * @return true if the table is accessible, else false
     * @param table the table for which to check accessibility
     * @throws HsqlException if a database access error occurs
     */
    protected final boolean isAccessibleTable(Table table)
    throws HsqlException {
        return session.isAccessible(table.getName());
    }

    /**
     * Creates a new primoidal system table with the specified name. <p>
     *
     * @return a new system table
     * @param name of the table
     * @throws HsqlException if a database access error occurs
     */
    protected final Table createBlankTable(HsqlName name)
    throws HsqlException {
        return new Table(database, name, Table.SYSTEM_TABLE);
    }

    /**
     * Retrieves the system <code>Table</code> object corresponding to
     * the given <code>name</code> and <code>session</code> arguments. <p>
     *
     * @param session the Session object requesting the table
     * @param name a String identifying the desired table
     * @throws HsqlException if there is a problem producing the table or a
     *      database access error occurs
     * @return a system table corresponding to the <code>name</code> and
     *      <code>session</code> arguments
     */
    final Table getSystemTable(Session session,
                               String name) throws HsqlException {

        Table t;
        int   tableIndex;

        // must come first...many methods depend on this being set properly
        this.session = session;

        if (!isSystemTable(name)) {
            return null;
        }

        tableIndex = getSysTableID(name);
        t          = sysTables[tableIndex];

        // fredt - any system table that is not supported will be null here
        if (t == null) {
            return t;
        }

        // At the time of opening the database, no content is needed
        // at present.  However, table structure is required at this
        // point to allow processing logged View defn's against system
        // tables.  Returning tables without content speeds the database
        // open phase under such cases.
        if (!withContent) {
            return t;
        }

        if (isDirty) {
            cacheClear();
        }

        int     oldSessionId = sysTableSessions[tableIndex];
        boolean tableValid   = oldSessionId != -1;

        // user has changed and table is user-dependent
        if (session.getId() != oldSessionId
                && sysTableSessionDependent[tableIndex]) {
            tableValid = false;
        }

        if (nonCachedTablesSet.contains(name)) {
            tableValid = false;
        }

        // any valid cached table will be returned here
        if (tableValid) {
            return t;
        }

        // fredt - clear the contents of table and set new User
        t.clearAllRows(session);

        sysTableSessions[tableIndex] = session.getId();

        // match and if found, generate.
        t = generateTable(tableIndex);

        // t will be null at this point, if the implementation
        // does not support the particular table
        // send back what we found or generated
        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the optimal
     * set of visible columns that uniquely identifies a row
     * for each accessible table defined within this database. <p>
     *
     * Each row describes a single column of the best row indentifier column
     * set for a particular table.  Each row has the following
     * columns: <p>
     *
     * <pre class="SqlCodeExample">
     * SCOPE          SMALLINT  scope of applicability
     * COLUMN_NAME    VARCHAR   simple name of the column
     * DATA_TYPE      SMALLINT  SQL data type from Types
     * TYPE_NAME      VARCHAR   canonical type name
     * COLUMN_SIZE    INTEGER   precision
     * BUFFER_LENGTH  INTEGER   transfer size in bytes, if definitely known
     * DECIMAL_DIGITS SMALLINT  scale  - fixed # of decimal digits
     * PSEUDO_COLUMN  SMALLINT  is this a pseudo column like an Oracle ROWID?
     * TABLE_CAT      VARCHAR   table catalog
     * TABLE_SCHEM    VARCHAR   simple name of table schema
     * TABLE_NAME     VARCHAR   simple table name
     * NULLABLE       SMALLINT  is column nullable?
     * IN_KEY         BOOLEAN   column belongs to a primary or alternate key?
     * </pre> <p>
     *
     * <b>Notes:</b><p>
     *
     * <code>jdbcDatabaseMetaData.getBestRowIdentifier</code> uses its
     * nullable parameter to filter the rows of this table in the following
     * manner: <p>
     *
     * If the nullable parameter is <code>false</code>, then rows are reported
     * only if, in addition to satisfying the other specified filter values,
     * the IN_KEY column value is TRUE. If the nullable parameter is
     * <code>true</code>, then the IN_KEY column value is ignored. <p>
     *
     * There is not yet infrastructure in place to make some of the ranking
     * descisions described below, and it is anticipated that mechanisms
     * upon which cost descisions could be based will change significantly over
     * the next few releases.  Hence, in the interest of simplicity and of not
     * making overly complex dependency on features that will almost certainly
     * change significantly in the near future, the current implementation,
     * while perfectly adequate for all but the most demanding or exacting
     * purposes, is actually sub-optimal in the strictest sense. <p>
     *
     * A description of the current implementation follows: <p>
     *
     * <b>DEFINTIONS:</b>  <p>
     *
     * <b>Alternate key</b> <p>
     *
     *  <UL>
     *   <LI> An attribute of a table that, by virtue of its having a set of
     *        columns that are both the full set of columns participating in a
     *        unique constraint or index and are all not null, yeilds the same
     *        selectability characteristic that would obtained by declaring a
     *        primary key on those same columns.
     *  </UL> <p>
     *
     * <b>Column set performance ranking</b> <p>
     *
     *  <UL>
     *  <LI> The ranking of the expected average performance w.r.t a subset of
     *       a table's columns used to select and/or compare rows, as taken in
     *       relation to all other distinct candidate subsets under
     *       consideration. This can be estimated by comparing each cadidate
     *       subset in terms of total column count, relative peformance of
     *       comparisons amongst the domains of the columns and differences
     *       in other costs involved in the execution plans generated using
     *       each subset under consideration for row selection/comparison.
     *  </UL> <p>
     *
     *
     * <b>Rules:</b> <p>
     *
     * Given the above definitions, the rules currently in effect for reporting
     * best row identifier are as follows, in order of precedence: <p>
     *
     * <OL>
     * <LI> if the table under consideration has a primary key contraint, then
     *      the columns of the primary key are reported, with no consideration
     *      given to the column set performance ranking over the set of
     *      candidate keys. Each row has its IN_KEY column set to TRUE.
     *
     * <LI> if 1.) does not hold, then if there exits one or more alternate
     *      keys, then the columns of the alternate key with the lowest column
     *      count are reported, with no consideration given to the column set
     *      performance ranking over the set of candidate keys. If there
     *      exists a tie for lowest column count, then the columns of the
     *      first such key encountered are reported.
     *      Each row has its IN_KEY column set to TRUE.
     *
     * <LI> if both 1.) and 2.) do not hold, then, if possible, a unique
     *      contraint/index is selected from the set of unique
     *      contraints/indices containing at least one column having
     *      a not null constraint, with no consideration given to the
     *      column set performance ranking over the set of all such
     *      candidate column sets. If there exists a tie for lowest non-zero
     *      count of columns having a not null constraint, then the columns
     *      of the first such encountered candidate set are reported. Each
     *      row has its IN_KEY column set to FALSE. <p>
     *
     * <LI> Finally, if the set of candidate column sets in 3.) is the empty,
     *      then no column set is reported for the table under consideration.
     * </OL> <p>
     *
     * The scope reported for a best row identifier column set is determined
     * thus: <p>
     *
     * <OL>
     * <LI> if the database containing the table under consideration is in
     *      read-only mode or the table under consideration is GLOBAL TEMPORARY
     *      (a TEMP or TEMP TEXT table, in HSQLDB parlance), then the scope
     *      is reported as
     *      <code>java.sql.DatabaseMetaData.bestRowSession</code>.
     *
     * <LI> if 1.) does not hold, then the scope is reported as
     *      <code>java.sql.DatabaseMetaData.bestRowTemporary</code>.
     * </OL> <p>
     *
     * @return a <code>Table</code> object describing the optimal
     * set of visible columns that uniquely identifies a row
     * for each accessible table defined within this database
     * @throws HsqlException if an error occurs while producing the table
     */
    final Table SYSTEM_BESTROWIDENTIFIER() throws HsqlException {

        Table t = sysTables[SYSTEM_BESTROWIDENTIFIER];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_BESTROWIDENTIFIER]);

            addColumn(t, "SCOPE", Types.SMALLINT, false);            // not null
            addColumn(t, "COLUMN_NAME", Types.VARCHAR, false);       // not null
            addColumn(t, "DATA_TYPE", Types.SMALLINT, false);        // not null
            addColumn(t, "TYPE_NAME", Types.VARCHAR, 32, false);     // not null
            addColumn(t, "COLUMN_SIZE", Types.INTEGER);
            addColumn(t, "BUFFER_LENGTH", Types.INTEGER);
            addColumn(t, "DECIMAL_DIGITS", Types.SMALLINT);
            addColumn(t, "PSEUDO_COLUMN", Types.SMALLINT, false);    // not null
            addColumn(t, "TABLE_CAT", Types.VARCHAR);
            addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            addColumn(t, "TABLE_NAME", Types.VARCHAR, false);        // not null
            addColumn(t, "NULLABLE", Types.SMALLINT, false);         // not null
            addColumn(t, "IN_KEY", Types.BOOLEAN, false);            // not null

            // order: SCOPE
            // for unique:  TABLE_CAT, TABLE_SCHEM, TABLE_NAME, COLUMN_NAME
            // false PK, as TABLE_CAT and/or TABLE_SCHEM may be null
            t.createPrimaryKey(null, new int[] {
                0, 8, 9, 10, 1
            }, false);

            return t;
        }

        // calculated column values
        Integer scope;           // { temp, transaction, session }
        Integer pseudo;

        //-------------------------------------------
        // required for restriction of results via
        // DatabaseMetaData filter parameters, but
        // not actually required to be included in
        // DatabaseMetaData.getBestRowIdentifier()
        // result set
        //-------------------------------------------
        String  tableCatalog;    // table calalog
        String  tableSchema;     // table schema
        String  tableName;       // table name
        Boolean inKey;           // column participates in PK or AK?

        //-------------------------------------------
        // TODO:  Maybe include:
        //        - backing index (constraint) name?
        //        - column sequence in index (constraint)?
        //-------------------------------------------
        // Intermediate holders
        Iterator       tables;
        Table          table;
        DITableInfo    ti;
        int[]          cols;
        Object[]       row;
        HsqlProperties p;

        // Column number mappings
        final int iscope          = 0;
        final int icolumn_name    = 1;
        final int idata_type      = 2;
        final int itype_name      = 3;
        final int icolumn_size    = 4;
        final int ibuffer_length  = 5;
        final int idecimal_digits = 6;
        final int ipseudo_column  = 7;
        final int itable_cat      = 8;
        final int itable_schem    = 9;
        final int itable_name     = 10;
        final int inullable       = 11;
        final int iinKey          = 12;

        // Initialization
        ti     = new DITableInfo();
        p      = database.getProperties();
        tables = p.isPropertyTrue("hsqldb.system_table_bri") ? allTables()
                                                             : database
                                                             .schemaManager
                                                                 .allTablesIterator();

        // Do it.
        while (tables.hasNext()) {
            table = (Table) tables.next();

            if (table.isView() ||!isAccessibleTable(table)) {
                continue;
            }

            cols = table.getBestRowIdentifiers();

            if (cols == null) {
                continue;
            }

            ti.setTable(table);

            inKey = ValuePool.getBoolean(table.isBestRowIdentifiersStrict());
            tableCatalog = ns.getCatalogName(table);
            tableSchema  = table.getSchemaName();
            tableName    = ti.getName();
            scope        = ti.getBRIScope();
            pseudo       = ti.getBRIPseudo();

            for (int i = 0; i < cols.length; i++) {
                row                  = t.getEmptyRowData();
                row[iscope]          = scope;
                row[icolumn_name]    = ti.getColName(i);
                row[idata_type]      = ti.getColDataType(i);
                row[itype_name]      = ti.getColDataTypeName(i);
                row[icolumn_size]    = ti.getColSize(i);
                row[ibuffer_length]  = ti.getColBufLen(i);
                row[idecimal_digits] = ti.getColScale(i);
                row[ipseudo_column]  = pseudo;
                row[itable_cat]      = tableCatalog;
                row[itable_schem]    = tableSchema;
                row[itable_name]     = tableName;
                row[inullable]       = ti.getColNullability(i);
                row[iinKey]          = inKey;

                t.insertSys(row);
            }
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object naming the accessible catalogs
     * defined within this database. <p>
     *
     * Each row is a catalog name description with the following column: <p>
     *
     * <pre class="SqlCodeExample">
     * TABLE_CAT   VARCHAR   catalog name
     * </pre> <p>
     *
     * @return a <code>Table</code> object naming the accessible
     *        catalogs defined within this database
     * @throws HsqlException if an error occurs while producing the table
     */
    final Table SYSTEM_CATALOGS() throws HsqlException {

        Table t = sysTables[SYSTEM_CATALOGS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_CATALOGS]);

            addColumn(t, "TABLE_CAT", Types.VARCHAR, false);    // not null

            // order:  TABLE_CAT
            // true PK
            t.createPrimaryKey(null, new int[]{ 0 }, true);

            return t;
        }

        Object[] row;
        Iterator catalogs;

        catalogs = ns.iterateCatalogNames();

        while (catalogs.hasNext()) {
            row    = t.getEmptyRowData();
            row[0] = (String) catalogs.next();

            t.insertSys(row);
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the visible
     * access rights for all visible columns of all accessible
     * tables defined within this database.<p>
     *
     * Each row is a column privilege description with the following
     * columns: <p>
     *
     * <pre class="SqlCodeExample">
     * TABLE_CAT    VARCHAR   table catalog
     * TABLE_SCHEM  VARCHAR   table schema
     * TABLE_NAME   VARCHAR   table name
     * COLUMN_NAME  VARCHAR   column name
     * GRANTOR      VARCHAR   grantor of access
     * GRANTEE      VARCHAR   grantee of access
     * PRIVILEGE    VARCHAR   name of access
     * IS_GRANTABLE VARCHAR   grantable?: "YES" - grant to others, else "NO"
     * </pre>
     *
     * <b>Note:</b> As of 1.7.2, HSQLDB does not support column level
     * privileges. However, it does support table-level privileges, so they
     * are reflected here.  That is, the content of this table is equivalent
     * to a projection of SYSTEM_TABLEPRIVILEGES and SYSTEM_COLUMNS joined by
     * full table identifier. <p>
     *
     * @return a <code>Table</code> object describing the visible
     *        access rights for all visible columns of
     *        all accessible tables defined within this
     *        database
     * @throws HsqlException if an error occurs while producing the table
     */
    final Table SYSTEM_COLUMNPRIVILEGES() throws HsqlException {

        Table t = sysTables[SYSTEM_COLUMNPRIVILEGES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_COLUMNPRIVILEGES]);

            addColumn(t, "TABLE_CAT", Types.VARCHAR);
            addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            addColumn(t, "TABLE_NAME", Types.VARCHAR, false);         // not null
            addColumn(t, "COLUMN_NAME", Types.VARCHAR, false);        // not null
            addColumn(t, "GRANTOR", Types.VARCHAR, false);            // not null
            addColumn(t, "GRANTEE", Types.VARCHAR, false);            // not null
            addColumn(t, "PRIVILEGE", Types.VARCHAR, 10, false);      // not null
            addColumn(t, "IS_GRANTABLE", Types.VARCHAR, 3, false);    // not null

            // order: COLUMN_NAME, PRIVILEGE
            // for unique: GRANTEE, GRANTOR, TABLE_NAME, TABLE_SCHEM, TABLE_CAT
            // false PK, as TABLE_SCHEM and/or TABLE_CAT may be null
            t.createPrimaryKey(null, new int[] {
                3, 6, 5, 4, 2, 1, 0
            }, false);

            return t;
        }

        Result rs;

        rs = session.sqlExecuteDirectNoPreChecks(
            "select a.TABLE_CAT, a.TABLE_SCHEM, a.TABLE_NAME, b.COLUMN_NAME, "
            + "a.GRANTOR, a.GRANTEE, a.PRIVILEGE, a.IS_GRANTABLE "
            + "from  INFORMATION_SCHEMA.SYSTEM_TABLEPRIVILEGES a, "
            + "INFORMATION_SCHEMA.SYSTEM_COLUMNS b "
            + "where a.TABLE_NAME = b.TABLE_NAME and "
            + "a.TABLE_SCHEM = b.TABLE_SCHEM;");

        t.insertSys(rs);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the
     * visible columns of all accessible tables defined
     * within this database.<p>
     *
     * Each row is a column description with the following columns: <p>
     *
     * <pre class="SqlCodeExample">
     * TABLE_CAT         VARCHAR   table catalog
     * TABLE_SCHEM       VARCHAR   table schema
     * TABLE_NAME        VARCHAR   table name
     * COLUMN_NAME       VARCHAR   column name
     * DATA_TYPE         SMALLINT  SQL type from DITypes
     * TYPE_NAME         VARCHAR   canonical type name
     * COLUMN_SIZE       INTEGER   column size (length/precision)
     * BUFFER_LENGTH     INTEGER   transfer size in bytes, if definitely known
     * DECIMAL_DIGITS    INTEGER   # of fractional digits (scale)
     * NUM_PREC_RADIX    INTEGER   Radix
     * NULLABLE          INTEGER   is NULL allowed? (from DatabaseMetaData)
     * REMARKS           VARCHAR   comment describing column
     * COLUMN_DEF        VARCHAR   default value (possibly expression)
     * SQL_DATA_TYPE     VARCHAR   type code as expected in the SQL CLI SQLDA
     * SQL_DATETIME_SUB  INTEGER   the SQL CLI subtype for DATETIME types
     * CHAR_OCTET_LENGTH INTEGER   for char types, max # of bytes in column
     * ORDINAL_POSITION  INTEGER   1-based index of column in table
     * IS_NULLABLE       VARCHAR   is column nullable? ("YES"|"NO"|""}
     * SCOPE_CATLOG      VARCHAR   catalog of REF attribute scope table
     * SCOPE_SCHEMA      VARCHAR   schema of REF attribute scope table
     * SCOPE_TABLE       VARCHAR   name of REF attribute scope table
     * SOURCE_DATA_TYPE  VARCHAR   source type of REF attribute
     * TYPE_SUB          INTEGER   HSQLDB data subtype code
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing the
     *        visible columns of all accessible
     *        tables defined within this database.<p>
     * @throws HsqlException if an error occurs while producing the table
     */
    final Table SYSTEM_COLUMNS() throws HsqlException {

        Table t = sysTables[SYSTEM_COLUMNS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_COLUMNS]);

            addColumn(t, "TABLE_CAT", Types.VARCHAR);
            addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            addColumn(t, "TABLE_NAME", Types.VARCHAR, false);          // not null
            addColumn(t, "COLUMN_NAME", Types.VARCHAR, false);         // not null
            addColumn(t, "DATA_TYPE", Types.SMALLINT, false);          // not null
            addColumn(t, "TYPE_NAME", Types.VARCHAR, 32, false);       // not null
            addColumn(t, "COLUMN_SIZE", Types.INTEGER);
            addColumn(t, "BUFFER_LENGTH", Types.INTEGER);
            addColumn(t, "DECIMAL_DIGITS", Types.INTEGER);
            addColumn(t, "NUM_PREC_RADIX", Types.INTEGER);
            addColumn(t, "NULLABLE", Types.INTEGER, false);            // not null
            addColumn(t, "REMARKS", Types.VARCHAR);
            addColumn(t, "COLUMN_DEF", Types.VARCHAR);
            addColumn(t, "SQL_DATA_TYPE", Types.INTEGER);
            addColumn(t, "SQL_DATETIME_SUB", Types.INTEGER);
            addColumn(t, "CHAR_OCTET_LENGTH", Types.INTEGER);
            addColumn(t, "ORDINAL_POSITION", Types.INTEGER, false);    // not null
            addColumn(t, "IS_NULLABLE", Types.VARCHAR, 3, false);      // not null
            addColumn(t, "SCOPE_CATLOG", Types.VARCHAR);
            addColumn(t, "SCOPE_SCHEMA", Types.VARCHAR);
            addColumn(t, "SCOPE_TABLE", Types.VARCHAR);
            addColumn(t, "SOURCE_DATA_TYPE", Types.VARCHAR);
            addColumn(t, "TYPE_SUB", Types.INTEGER, false);            // not null

            // order: TABLE_SCHEM, TABLE_NAME, ORDINAL_POSITION
            // added for unique: TABLE_CAT
            // false PK, as TABLE_SCHEM and/or TABLE_CAT may be null
            t.createPrimaryKey(null, new int[] {
                1, 2, 16, 0
            }, false);

            return t;
        }

        // calculated column values
        String tableCatalog;
        String tableSchema;
        String tableName;

        // intermediate holders
        int         columnCount;
        Iterator    tables;
        Table       table;
        Object[]    row;
        DITableInfo ti;

        // column number mappings
        final int itable_cat         = 0;
        final int itable_schem       = 1;
        final int itable_name        = 2;
        final int icolumn_name       = 3;
        final int idata_type         = 4;
        final int itype_name         = 5;
        final int icolumn_size       = 6;
        final int ibuffer_length     = 7;
        final int idecimal_digits    = 8;
        final int inum_prec_radix    = 9;
        final int inullable          = 10;
        final int iremark            = 11;
        final int icolumn_def        = 12;
        final int isql_data_type     = 13;
        final int isql_datetime_sub  = 14;
        final int ichar_octet_length = 15;
        final int iordinal_position  = 16;
        final int iis_nullable       = 17;
        final int itype_sub          = 22;

        // Initialization
        tables = allTables();
        ti     = new DITableInfo();

        // Do it.
        while (tables.hasNext()) {
            table = (Table) tables.next();

            if (!isAccessibleTable(table)) {
                continue;
            }

            ti.setTable(table);

            tableCatalog = ns.getCatalogName(table);
            tableSchema  = table.getSchemaName();
            tableName    = ti.getName();
            columnCount  = table.getColumnCount();

            for (int i = 0; i < columnCount; i++) {
                row                     = t.getEmptyRowData();
                row[itable_cat]         = tableCatalog;
                row[itable_schem]       = tableSchema;
                row[itable_name]        = tableName;
                row[icolumn_name]       = ti.getColName(i);
                row[idata_type]         = ti.getColDataType(i);
                row[itype_name]         = ti.getColDataTypeName(i);
                row[icolumn_size]       = ti.getColSize(i);
                row[ibuffer_length]     = ti.getColBufLen(i);
                row[idecimal_digits]    = ti.getColScale(i);
                row[inum_prec_radix]    = ti.getColPrecRadix(i);
                row[inullable]          = ti.getColNullability(i);
                row[iremark]            = ti.getColRemarks(i);
                row[icolumn_def]        = ti.getColDefault(i);
                row[isql_data_type]     = ti.getColSqlDataType(i);
                row[isql_datetime_sub]  = ti.getColSqlDateTimeSub(i);
                row[ichar_octet_length] = ti.getColCharOctLen(i);
                row[iordinal_position]  = ValuePool.getInt(i + 1);
                row[iis_nullable]       = ti.getColIsNullable(i);
                row[itype_sub]          = ti.getColDataTypeSub(i);

                t.insertSys(row);
            }
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing, for each
     * accessible referencing and referenced table, how the referencing
     * tables import, for the purposes of referential integrity,
     * the columns of the referenced tables.<p>
     *
     * Each row is a foreign key column description with the following
     * columns: <p>
     *
     * <pre class="SqlCodeExample">
     * PKTABLE_CAT   VARCHAR   referenced table catalog
     * PKTABLE_SCHEM VARCHAR   referenced table schema
     * PKTABLE_NAME  VARCHAR   referenced table name
     * PKCOLUMN_NAME VARCHAR   referenced column name
     * FKTABLE_CAT   VARCHAR   referencing table catalog
     * FKTABLE_SCHEM VARCHAR   referencing table schema
     * FKTABLE_NAME  VARCHAR   referencing table name
     * FKCOLUMN_NAME VARCHAR   referencing column
     * KEY_SEQ       SMALLINT  sequence number within foreign key
     * UPDATE_RULE   SMALLINT
     *    { Cascade | Set Null | Set Default | Restrict (No Action)}?
     * DELETE_RULE   SMALLINT
     *    { Cascade | Set Null | Set Default | Restrict (No Action)}?
     * FK_NAME       VARCHAR   foreign key constraint name
     * PK_NAME       VARCHAR   primary key or unique constraint name
     * DEFERRABILITY SMALLINT
     *    { initially deferred | initially immediate | not deferrable }
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing how accessible tables
     *      import other accessible tables' primary key and/or unique
     *      constraint columns
     * @throws HsqlException if an error occurs while producing the table
     */
    final Table SYSTEM_CROSSREFERENCE() throws HsqlException {

        Table t = sysTables[SYSTEM_CROSSREFERENCE];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_CROSSREFERENCE]);

            addColumn(t, "PKTABLE_CAT", Types.VARCHAR);
            addColumn(t, "PKTABLE_SCHEM", Types.VARCHAR);
            addColumn(t, "PKTABLE_NAME", Types.VARCHAR, false);      // not null
            addColumn(t, "PKCOLUMN_NAME", Types.VARCHAR, false);     // not null
            addColumn(t, "FKTABLE_CAT", Types.VARCHAR);
            addColumn(t, "FKTABLE_SCHEM", Types.VARCHAR);
            addColumn(t, "FKTABLE_NAME", Types.VARCHAR, false);      // not null
            addColumn(t, "FKCOLUMN_NAME", Types.VARCHAR, false);     // not null
            addColumn(t, "KEY_SEQ", Types.SMALLINT, false);          // not null
            addColumn(t, "UPDATE_RULE", Types.SMALLINT, false);      // not null
            addColumn(t, "DELETE_RULE", Types.SMALLINT, false);      // not null
            addColumn(t, "FK_NAME", Types.VARCHAR);
            addColumn(t, "PK_NAME", Types.VARCHAR);
            addColumn(t, "DEFERRABILITY", Types.SMALLINT, false);    // not null

            // order: FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME, and KEY_SEQ
            // added for unique: FK_NAME
            // false PK, as FKTABLE_CAT, FKTABLE_SCHEM and/or FK_NAME
            // may be null
            t.createPrimaryKey(null, new int[] {
                4, 5, 6, 8, 11
            }, false);

            return t;
        }

        // calculated column values
        String  pkTableCatalog;
        String  pkTableSchema;
        String  pkTableName;
        String  pkColumnName;
        String  fkTableCatalog;
        String  fkTableSchema;
        String  fkTableName;
        String  fkColumnName;
        Integer keySequence;
        Integer updateRule;
        Integer deleteRule;
        String  fkName;
        String  pkName;
        Integer deferrability;

        // Intermediate holders
        Iterator      tables;
        Table         table;
        Table         fkTable;
        Table         pkTable;
        int           columnCount;
        int[]         mainCols;
        int[]         refCols;
        Constraint[]  constraints;
        Constraint    constraint;
        int           constraintCount;
        HsqlArrayList fkConstraintsList;
        Object[]      row;
        DITableInfo   pkInfo;
        DITableInfo   fkInfo;

        // column number mappings
        final int ipk_table_cat   = 0;
        final int ipk_table_schem = 1;
        final int ipk_table_name  = 2;
        final int ipk_column_name = 3;
        final int ifk_table_cat   = 4;
        final int ifk_table_schem = 5;
        final int ifk_table_name  = 6;
        final int ifk_column_name = 7;
        final int ikey_seq        = 8;
        final int iupdate_rule    = 9;
        final int idelete_rule    = 10;
        final int ifk_name        = 11;
        final int ipk_name        = 12;
        final int ideferrability  = 13;

        tables = database.schemaManager.allTablesIterator();
        pkInfo = new DITableInfo();
        fkInfo = new DITableInfo();

        // the only deferrability rule currently supported by hsqldb is:
        deferrability = ValuePool.getInt(Constraint.NOT_DEFERRABLE);

        // We must consider all the constraints in all the user tables, since
        // this is where reference relationships are recorded.  However, we
        // are only concerned with Constraint.FOREIGN_KEY constraints here
        // because their corresponing Constraint.MAIN entries are essentially
        // duplicate data recorded in the referenced rather than the
        // referencing table.  Also, we skip constraints where either
        // the referenced, referencing or both tables are not accessible
        // relative to the session of the calling context
        fkConstraintsList = new HsqlArrayList();

        while (tables.hasNext()) {
            table = (Table) tables.next();

            if (!isAccessibleTable(table)) {
                continue;
            }

            constraints     = table.getConstraints();
            constraintCount = constraints.length;

            for (int i = 0; i < constraintCount; i++) {
                constraint = (Constraint) constraints[i];

                if (constraint.getType() == Constraint.FOREIGN_KEY
                        && isAccessibleTable(constraint.getRef())) {
                    fkConstraintsList.add(constraint);
                }
            }
        }

        // Now that we have all of the desired constraints, we need to
        // process them, generating one row in our ouput table for each
        // imported/exported column pair of each constraint.
        // Do it.
        for (int i = 0; i < fkConstraintsList.size(); i++) {
            constraint = (Constraint) fkConstraintsList.get(i);
            pkTable    = constraint.getMain();

            pkInfo.setTable(pkTable);

            pkTableName = pkInfo.getName();
            fkTable     = constraint.getRef();

            fkInfo.setTable(fkTable);

            fkTableName    = fkInfo.getName();
            pkTableCatalog = ns.getCatalogName(pkTable);
            pkTableSchema  = pkTable.getSchemaName();
            fkTableCatalog = ns.getCatalogName(fkTable);
            fkTableSchema  = fkTable.getSchemaName();
            mainCols       = constraint.getMainColumns();
            refCols        = constraint.getRefColumns();
            columnCount    = refCols.length;
            fkName         = constraint.getFkName();
            pkName         = constraint.getPkName();

            //pkName = constraint.getMainIndex().getName().name;
            deleteRule = ValuePool.getInt(constraint.getDeleteAction());
            updateRule = ValuePool.getInt(constraint.getUpdateAction());

            for (int j = 0; j < columnCount; j++) {
                keySequence          = ValuePool.getInt(j + 1);
                pkColumnName         = pkInfo.getColName(mainCols[j]);
                fkColumnName         = fkInfo.getColName(refCols[j]);
                row                  = t.getEmptyRowData();
                row[ipk_table_cat]   = pkTableCatalog;
                row[ipk_table_schem] = pkTableSchema;
                row[ipk_table_name]  = pkTableName;
                row[ipk_column_name] = pkColumnName;
                row[ifk_table_cat]   = fkTableCatalog;
                row[ifk_table_schem] = fkTableSchema;
                row[ifk_table_name]  = fkTableName;
                row[ifk_column_name] = fkColumnName;
                row[ikey_seq]        = keySequence;
                row[iupdate_rule]    = updateRule;
                row[idelete_rule]    = deleteRule;
                row[ifk_name]        = fkName;
                row[ipk_name]        = pkName;
                row[ideferrability]  = deferrability;

                t.insertSys(row);
            }
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the visible
     * <code>Index</code> objects for each accessible table defined
     * within this database.<p>
     *
     * Each row is an index column description with the following
     * columns: <p>
     *
     * <pre class="SqlCodeExample">
     * TABLE_CAT        VARCHAR   table's catalog
     * TABLE_SCHEM      VARCHAR   simple name of table's schema
     * TABLE_NAME       VARCHAR   simple name of the table using the index
     * NON_UNIQUE       BOOLEAN   can index values be non-unique?
     * INDEX_QUALIFIER  VARCHAR   catalog in which the index is defined
     * INDEX_NAME       VARCHAR   simple name of the index
     * TYPE             SMALLINT  index type: { Clustered | Hashed | Other }
     * ORDINAL_POSITION SMALLINT  column sequence number within index
     * COLUMN_NAME      VARCHAR   simple column name
     * ASC_OR_DESC      VARCHAR   col. sort sequence: {"A" (Asc) | "D" (Desc)}
     * CARDINALITY      INTEGER   # of unique values in index (not implemented)
     * PAGES            INTEGER   index page use (not implemented)
     * FILTER_CONDITION VARCHAR   filter condition, if any (not implemented)
     * // HSQLDB-extension
     * ROW_CARDINALITY  INTEGER   total # of rows in index (not implemented)
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing the visible
     *        <code>Index</code> objects for each accessible
     *        table defined within this database.
     * @throws HsqlException if an error occurs while producing the table
     */
    final Table SYSTEM_INDEXINFO() throws HsqlException {

        Table t = sysTables[SYSTEM_INDEXINFO];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_INDEXINFO]);

            // JDBC
            addColumn(t, "TABLE_CAT", Types.VARCHAR);
            addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            addColumn(t, "TABLE_NAME", Types.VARCHAR, false);           // NOT NULL
            addColumn(t, "NON_UNIQUE", Types.BOOLEAN, false);           // NOT NULL
            addColumn(t, "INDEX_QUALIFIER", Types.VARCHAR);
            addColumn(t, "INDEX_NAME", Types.VARCHAR);
            addColumn(t, "TYPE", Types.SMALLINT, false);                // NOT NULL
            addColumn(t, "ORDINAL_POSITION", Types.SMALLINT, false);    // NOT NULL
            addColumn(t, "COLUMN_NAME", Types.VARCHAR);
            addColumn(t, "ASC_OR_DESC", Types.VARCHAR, 1, true);
            addColumn(t, "CARDINALITY", Types.INTEGER);
            addColumn(t, "PAGES", Types.INTEGER);
            addColumn(t, "FILTER_CONDITION", Types.VARCHAR);

            // HSQLDB extension
            addColumn(t, "ROW_CARDINALITY", Types.INTEGER);

            // order: NON_UNIQUE, TYPE, INDEX_NAME, and ORDINAL_POSITION.
            // added for unique: INDEX_QUALIFIER, TABLE_NAME
            // false PK, as INDEX_QUALIFIER may be null
            t.createPrimaryKey(null, new int[] {
                3, 6, 5, 7, 4, 2
            }, false);

            return t;
        }

        // calculated column values
        String  tableCatalog;
        String  tableSchema;
        String  tableName;
        Boolean nonUnique;
        String  indexQualifier;
        String  indexName;
        Integer indexType;

        //Integer ordinalPosition;
        //String  columnName;
        //String  ascOrDesc;
        Integer cardinality;
        Integer pages;
        String  filterCondition;
        Integer rowCardinality;

        // Intermediate holders
        Iterator       tables;
        Table          table;
        int            indexCount;
        int[]          cols;
        int            col;
        int            colCount;
        Object[]       row;
        DITableInfo    ti;
        HsqlProperties p;

        // column number mappings
        final int itable_cat        = 0;
        final int itable_schem      = 1;
        final int itable_name       = 2;
        final int inon_unique       = 3;
        final int iindex_qualifier  = 4;
        final int iindex_name       = 5;
        final int itype             = 6;
        final int iordinal_position = 7;
        final int icolumn_name      = 8;
        final int iasc_or_desc      = 9;
        final int icardinality      = 10;
        final int ipages            = 11;
        final int ifilter_condition = 12;
        final int irow_cardinality  = 13;

        // Initialization
        ti = new DITableInfo();
        p  = database.getProperties();
        tables = p.isPropertyTrue("hsqldb.system_table_indexinfo")
                 ? allTables()
                 : database.schemaManager.allTablesIterator();

        // Do it.
        while (tables.hasNext()) {
            table = (Table) tables.next();

            if (table.isView() ||!isAccessibleTable(table)) {
                continue;
            }

            ti.setTable(table);

            tableCatalog = ns.getCatalogName(table);
            tableSchema  = table.getSchemaName();
            tableName    = ti.getName();

            // not supported yet
            filterCondition = null;

            // different cat for index not supported yet
            indexQualifier = tableCatalog;
            indexCount     = table.getIndexCount();

            // process all of the visible indices for this table
            for (int i = 0; i < indexCount; i++) {
                colCount = ti.getIndexVisibleColumns(i);

                if (colCount < 1) {
                    continue;
                }

                indexName      = ti.getIndexName(i);
                nonUnique      = ti.isIndexNonUnique(i);
                cardinality    = ti.getIndexCardinality(i);
                pages          = ValuePool.getInt(0);
                rowCardinality = ti.getIndexRowCardinality(i);
                cols           = ti.getIndexColumns(i);
                indexType      = ti.getIndexType(i);

                for (int k = 0; k < colCount; k++) {
                    col                    = cols[k];
                    row                    = t.getEmptyRowData();
                    row[itable_cat]        = tableCatalog;
                    row[itable_schem]      = tableSchema;
                    row[itable_name]       = tableName;
                    row[inon_unique]       = nonUnique;
                    row[iindex_qualifier]  = indexQualifier;
                    row[iindex_name]       = indexName;
                    row[itype]             = indexType;
                    row[iordinal_position] = ValuePool.getInt(k + 1);
                    row[icolumn_name]      = ti.getColName(col);
                    row[iasc_or_desc]      = ti.getIndexColDirection(i, col);
                    row[icardinality]      = cardinality;
                    row[ipages]            = pages;
                    row[irow_cardinality]  = rowCardinality;
                    row[ifilter_condition] = filterCondition;

                    t.insertSys(row);
                }
            }
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the visible
     * primary key columns of each accessible table defined within
     * this database. <p>
     *
     * Each row is a PRIMARY KEY column description with the following
     * columns: <p>
     *
     * <pre class="SqlCodeExample">
     * TABLE_CAT   VARCHAR   table catalog
     * TABLE_SCHEM VARCHAR   table schema
     * TABLE_NAME  VARCHAR   table name
     * COLUMN_NAME VARCHAR   column name
     * KEY_SEQ     SMALLINT  sequence number within primary key
     * PK_NAME     VARCHAR   primary key constraint name
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing the visible
     *        primary key columns of each accessible table
     *        defined within this database.
     * @throws HsqlException if an error occurs while producing the table
     */
    final Table SYSTEM_PRIMARYKEYS() throws HsqlException {

        Table t = sysTables[SYSTEM_PRIMARYKEYS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_PRIMARYKEYS]);

            addColumn(t, "TABLE_CAT", Types.VARCHAR);
            addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            addColumn(t, "TABLE_NAME", Types.VARCHAR, false);     // not null
            addColumn(t, "COLUMN_NAME", Types.VARCHAR, false);    // not null
            addColumn(t, "KEY_SEQ", Types.SMALLINT, false);       // not null
            addColumn(t, "PK_NAME", Types.VARCHAR);

            // order: COLUMN_NAME
            // added for unique: TABLE_NAME, TABLE_SCHEM, TABLE_CAT
            // false PK, as  TABLE_SCHEM and/or TABLE_CAT may be null
            t.createPrimaryKey(null, new int[] {
                3, 2, 1, 0
            }, false);

            return t;
        }

        // calculated column values
        String tableCatalog;
        String tableSchema;
        String tableName;

        //String  columnName;
        //Integer keySequence;
        String primaryKeyName;

        // Intermediate holders
        Iterator       tables;
        Table          table;
        Object[]       row;
        Index          index;
        int[]          cols;
        int            colCount;
        DITableInfo    ti;
        HsqlProperties p;

        // column number mappings
        final int itable_cat   = 0;
        final int itable_schem = 1;
        final int itable_name  = 2;
        final int icolumn_name = 3;
        final int ikey_seq     = 4;
        final int ipk_name     = 5;

        // Initialization
        ti = new DITableInfo();
        p  = database.getProperties();
        tables = p.isPropertyTrue("hsqldb.system_table_primarykeys")
                 ? allTables()
                 : database.schemaManager.allTablesIterator();

        while (tables.hasNext()) {
            table = (Table) tables.next();

            if (table.isView() ||!isAccessibleTable(table)
                    ||!table.hasPrimaryKey()) {
                continue;
            }

            index = table.getPrimaryIndex();

            ti.setTable(table);

            tableCatalog   = ns.getCatalogName(table);
            tableSchema    = table.getSchemaName();
            tableName      = ti.getName();
            primaryKeyName = index.getName().name;
            cols           = index.getColumns();
            colCount       = cols.length;

            for (int j = 0; j < colCount; j++) {
                row               = t.getEmptyRowData();
                row[itable_cat]   = tableCatalog;
                row[itable_schem] = tableSchema;
                row[itable_name]  = tableName;
                row[icolumn_name] = ti.getColName(cols[j]);
                row[ikey_seq]     = ValuePool.getInt(j + 1);
                row[ipk_name]     = primaryKeyName;

                t.insertSys(row);
            }
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the
     * return, parameter and result columns of the accessible
     * routines defined within this database.<p>
     *
     * Each row is a procedure column description with the following
     * columns: <p>
     *
     * <pre class="SqlCodeExample">
     * PROCEDURE_CAT   VARCHAR   routine catalog
     * PROCEDURE_SCHEM VARCHAR   routine schema
     * PROCEDURE_NAME  VARCHAR   routine name
     * COLUMN_NAME     VARCHAR   column/parameter name
     * COLUMN_TYPE     SMALLINT  kind of column/parameter
     * DATA_TYPE       SMALLINT  SQL type from DITypes
     * TYPE_NAME       VARCHAR   SQL type name
     * PRECISION       INTEGER   precision (length) of type
     * LENGTH          INTEGER   transfer size, in bytes, if definitely known
     *                           (roughly equivalent to BUFFER_SIZE for table
     *                           columns)
     * SCALE           SMALLINT  scale
     * RADIX           SMALLINT  radix
     * NULLABLE        SMALLINT  can column contain NULL?
     * REMARKS         VARCHAR   explanatory comment on column
     * SPECIFIC_NAME   VARCHAR   typically (but not restricted to) a
     *                           fully qualified Java Method name and signature
     * SEQ             INTEGER   The JDBC-specified order within
     *                           runs of PROCEDURE_SCHEM, PROCEDURE_NAME,
     *                           SPECIFIC_NAME, which is:
     *
     *                           return value (0), if any, first, followed
     *                           by the parameter descriptions in call order
     *                           (1..n1), followed by the result column
     *                           descriptions in column number order
     *                           (n1 + 1..n1 + n2)
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing the
     *        return, parameter and result columns
     *        of the accessible routines defined
     *        within this database.
     * @throws HsqlException if an error occurs while producing the table
     */
    Table SYSTEM_PROCEDURECOLUMNS() throws HsqlException {

        Table t = sysTables[SYSTEM_PROCEDURECOLUMNS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_PROCEDURECOLUMNS]);

            // ----------------------------------------------------------------
            // required
            // ----------------------------------------------------------------
            addColumn(t, "PROCEDURE_CAT", Types.VARCHAR);
            addColumn(t, "PROCEDURE_SCHEM", Types.VARCHAR);
            addColumn(t, "PROCEDURE_NAME", Types.VARCHAR, false);    // not null
            addColumn(t, "COLUMN_NAME", Types.VARCHAR, false);       // not null
            addColumn(t, "COLUMN_TYPE", Types.SMALLINT, false);      // not null
            addColumn(t, "DATA_TYPE", Types.SMALLINT, false);        // not null
            addColumn(t, "TYPE_NAME", Types.VARCHAR, 32, false);     // not null
            addColumn(t, "PRECISION", Types.INTEGER);
            addColumn(t, "LENGTH", Types.INTEGER);
            addColumn(t, "SCALE", Types.SMALLINT);
            addColumn(t, "RADIX", Types.SMALLINT);
            addColumn(t, "NULLABLE", Types.SMALLINT, false);         // not null
            addColumn(t, "REMARKS", Types.VARCHAR);

            // ----------------------------------------------------------------
            // extended (and required for JDBC sort contract w.r.t. overloading)
            // ----------------------------------------------------------------
            addColumn(t, "SPECIFIC_NAME", Types.VARCHAR, false);     // not null

            // ----------------------------------------------------------------
            // just required for JDBC sort contract
            // ----------------------------------------------------------------
            addColumn(t, "SEQ", Types.INTEGER, false);               // not null

            // ----------------------------------------------------------------
            // order: PROCEDURE_SCHEM, PROCEDURE_NAME, SPECIFIC_NAME, SEQ
            // added for unique: PROCEDURE_CAT
            // false PK, as PROCEDURE_SCHEM and/or PROCEDURE_CAT may be null
            t.createPrimaryKey(null, new int[] {
                1, 2, 13, 14, 0
            }, false);

            return t;
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * routines defined within this database.
     *
     * Each row is a procedure description with the following
     * columns: <p>
     *
     * <pre class="SqlCodeExample">
     * PROCEDURE_CAT     VARCHAR   catalog in which routine is defined
     * PROCEDURE_SCHEM   VARCHAR   schema in which routine is defined
     * PROCEDURE_NAME    VARCHAR   simple routine identifier
     * NUM_INPUT_PARAMS  INTEGER   number of input parameters
     * NUM_OUTPUT_PARAMS INTEGER   number of output parameters
     * NUM_RESULT_SETS   INTEGER   number of result sets returned
     * REMARKS           VARCHAR   explanatory comment on the routine
     * PROCEDURE_TYPE    SMALLINT  { Unknown | No Result | Returns Result }
     * ORIGIN            VARCHAR   {ALIAS |
     *                             [BUILTIN | USER DEFINED] ROUTINE |
     *                             [BUILTIN | USER DEFINED] TRIGGER |
     *                              ...}
     * SPECIFIC_NAME     VARCHAR   typically (but not restricted to) a
     *                             fully qualified Java Method name
     *                             and signature
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing the accessible
     *        routines defined within the this database
     * @throws HsqlException if an error occurs while producing the table
     */
    Table SYSTEM_PROCEDURES() throws HsqlException {

        Table t = sysTables[SYSTEM_PROCEDURES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_PROCEDURES]);

            // ----------------------------------------------------------------
            // required
            // ----------------------------------------------------------------
            addColumn(t, "PROCEDURE_CAT", Types.VARCHAR);
            addColumn(t, "PROCEDURE_SCHEM", Types.VARCHAR);
            addColumn(t, "PROCEDURE_NAME", Types.VARCHAR, false);     // not null
            addColumn(t, "NUM_INPUT_PARAMS", Types.INTEGER);
            addColumn(t, "NUM_OUTPUT_PARAMS", Types.INTEGER);
            addColumn(t, "NUM_RESULT_SETS", Types.INTEGER);
            addColumn(t, "REMARKS", Types.VARCHAR);

            // basically: function (returns result), procedure (no return value)
            // or unknown (say, a trigger callout routine)
            addColumn(t, "PROCEDURE_TYPE", Types.SMALLINT, false);    // not null

            // ----------------------------------------------------------------
            // extended
            // ----------------------------------------------------------------
            addColumn(t, "ORIGIN", Types.VARCHAR, 32, false);         // not null
            addColumn(t, "SPECIFIC_NAME", Types.VARCHAR, false);      // not null

            // ----------------------------------------------------------------
            // order: PROCEDURE_SCHEM and PROCEDURE_NAME.
            // added for uniqe: SPECIFIC_NAME, PROCEDURE_CAT
            // false PK, as PROCEDURE_SCHEM and/or PROCEDURE_CAT may be null
            t.createPrimaryKey(null, new int[] {
                1, 2, 9, 0
            }, false);

            return t;
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible schemas
     * defined within this database. <p>
     *
     * Each row is a schema description with the following
     * columns: <p>
     *
     * <pre class="SqlCodeExample">
     * TABLE_SCHEM      VARCHAR   simple schema name
     * TABLE_CATALOG    VARCHAR   catalog in which schema is defined
     * IS_DEFAULT       BOOLEAN   is the schema the default for new sessions
     * </pre> <p>
     *
     * @return table containing information about schemas defined
     *      within this database
     * @throws HsqlException if an error occurs while producing the table
     */
    final Table SYSTEM_SCHEMAS() throws HsqlException {

        Table t = sysTables[SYSTEM_SCHEMAS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_SCHEMAS]);

            addColumn(t, "TABLE_SCHEM", Types.VARCHAR, false);    // not null
            addColumn(t, "TABLE_CATALOG", Types.VARCHAR);
            addColumn(t, "IS_DEFAULT", Types.BOOLEAN);

            // order: TABLE_SCHEM
            // true PK, as rows never have null TABLE_SCHEM
            t.createPrimaryKey(null, new int[]{ 0 }, true);

            return t;
        }

        Iterator schemas;
        Object[] row;

        // Initialization
        schemas = database.schemaManager.fullSchemaNamesIterator();

        String defschema = database.schemaManager.getDefaultSchemaName();

        // Do it.
        while (schemas.hasNext()) {
            row = t.getEmptyRowData();

            String schema = (String) schemas.next();

            row[0] = schema;
            row[1] = ns.getCatalogName(row[0]);
            row[2] = schema.equals(defschema) ? Boolean.TRUE
                                              : Boolean.FALSE;

            t.insertSys(row);
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the visible access
     * rights for each accessible table definied within this database. <p>
     *
     * Each row is a table privilege description with the following columns: <p>
     *
     * <pre class="SqlCodeExample">
     * TABLE_CAT    VARCHAR   table catalog
     * TABLE_SCHEM  VARCHAR   table schema
     * TABLE_NAME   VARCHAR   table name
     * GRANTOR      VARCHAR   grantor of access
     * GRANTEE      VARCHAR   grantee of access
     * PRIVILEGE    VARCHAR   { "SELECT" | "INSERT" | "UPDATE" | "DELETE" }
     * IS_GRANTABLE VARCHAR   { "YES" | "NO" |  NULL (unknown) }
     * </pre>
     *
     * <b>Note:</b> Up to and including HSQLDB 1.7.2, the access rights granted
     * on a table apply to all of the columns of that table as well. <p>
     *
     * @return a <code>Table</code> object describing the visible
     *        access rights for each accessible table
     *        defined within this database
     * @throws HsqlException if an error occurs while producing the table
     */
    final Table SYSTEM_TABLEPRIVILEGES() throws HsqlException {

        Table t = sysTables[SYSTEM_TABLEPRIVILEGES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_TABLEPRIVILEGES]);

            addColumn(t, "TABLE_CAT", Types.VARCHAR);
            addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            addColumn(t, "TABLE_NAME", Types.VARCHAR, false);         // not null
            addColumn(t, "GRANTOR", Types.VARCHAR, false);            // not null
            addColumn(t, "GRANTEE", Types.VARCHAR, false);            // not null
            addColumn(t, "PRIVILEGE", Types.VARCHAR, 10, false);      // not null
            addColumn(t, "IS_GRANTABLE", Types.VARCHAR, 3, false);    // not null

            // order: TABLE_SCHEM, TABLE_NAME, and PRIVILEGE,
            // added for unique:  GRANTEE, GRANTOR, TABLE_CAT
            // false PK, as TABLE_SCHEM and/or TABLE_CAT may be null
            t.createPrimaryKey(null, new int[] {
                1, 2, 5, 4, 3, 0
            }, false);

            return t;
        }

        // calculated column values
        String tableCatalog;
        String tableSchema;
        String tableName;
        String grantorName;
        String granteeName;
        String privilege;
        String isGrantable;

        // intermediate holders
        HsqlArrayList users;
        User          user;
        String[]      tablePrivileges;
        Iterator      tables;
        Table         table;
        HsqlName      accessKey;
        Object[]      row;

        // column number mappings
        final int itable_cat    = 0;
        final int itable_schem  = 1;
        final int itable_name   = 2;
        final int igrantor      = 3;
        final int igrantee      = 4;
        final int iprivilege    = 5;
        final int iis_grantable = 6;

        // Initialization
        grantorName = GranteeManager.DBA_ADMIN_ROLE_NAME;
        users = database.getUserManager().listVisibleUsers(session, true);
        tables      = allTables();

        // Do it.
        while (tables.hasNext()) {
            table     = (Table) tables.next();
            accessKey = table.getName();

            // Only show table grants if session user is admin, has some
            // right, or the special PUBLIC user has some right.
            if (!isAccessibleTable(table)) {
                continue;
            }

            tableName    = table.getName().name;
            tableCatalog = ns.getCatalogName(table);
            tableSchema  = table.getSchemaName();

            for (int i = 0; i < users.size(); i++) {
                user        = (User) users.get(i);
                granteeName = user.getName();

                if (user.isAdmin()) {
                    tablePrivileges =
                        UserManager.getRightsArray(UserManager.ALL);
                } else {
                    tablePrivileges =
                        user.listGrantedTablePrivileges(accessKey);
                }

                isGrantable = (user.isAdmin()) ? "YES"
                                               : "NO";

                for (int j = 0; j < tablePrivileges.length; j++) {
                    privilege          = (String) tablePrivileges[j];
                    row                = t.getEmptyRowData();
                    row[itable_cat]    = tableCatalog;
                    row[itable_schem]  = tableSchema;
                    row[itable_name]   = tableName;
                    row[igrantor]      = grantorName;
                    row[igrantee]      = granteeName;
                    row[iprivilege]    = privilege;
                    row[iis_grantable] = isGrantable;

                    t.insertSys(row);
                }
            }
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * tables defined within this database. <p>
     *
     * Each row is a table description with the following columns: <p>
     *
     * <pre class="SqlCodeExample">
     * TABLE_CAT                 VARCHAR   table catalog
     * TABLE_SCHEM               VARCHAR   table schema
     * TABLE_NAME                VARCHAR   table name
     * TABLE_TYPE                VARCHAR   {"TABLE" | "VIEW" |
     *                                      "SYSTEM TABLE" | "GLOBAL TEMPORARY"}
     * REMARKS                   VARCHAR   comment on the table.
     * TYPE_CAT                  VARCHAR   table type catalog (not implemented).
     * TYPE_SCHEM                VARCHAR   table type schema (not implemented).
     * TYPE_NAME                 VARCHAR   table type name (not implemented).
     * SELF_REFERENCING_COL_NAME VARCHAR   designated "identifier" column of
     *                                     typed table (not implemented).
     * REF_GENERATION            VARCHAR   {"SYSTEM" | "USER" |
     *                                      "DERIVED" | NULL } (not implemented)
     * HSQLDB_TYPE               VARCHAR   HSQLDB-specific type:
     *                                     {"MEMORY" | "CACHED" | "TEXT" | ...}
     * READ_ONLY                 BOOLEAN   TRUE if table is read-only,
     *                                     else FALSE.
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing the accessible
     *      tables defined within this database
     * @throws HsqlException if an error occurs while producing the table
     */
    final Table SYSTEM_TABLES() throws HsqlException {

        Table t = sysTables[SYSTEM_TABLES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_TABLES]);

            // -------------------------------------------------------------
            // required
            // -------------------------------------------------------------
            addColumn(t, "TABLE_CAT", Types.VARCHAR);
            addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            addColumn(t, "TABLE_NAME", Types.VARCHAR, false);        // not null
            addColumn(t, "TABLE_TYPE", Types.VARCHAR, 16, false);    // not null
            addColumn(t, "REMARKS", Types.VARCHAR);

            // -------------------------------------------------------------
            // JDBC3
            // -------------------------------------------------------------
            addColumn(t, "TYPE_CAT", Types.VARCHAR);
            addColumn(t, "TYPE_SCHEM", Types.VARCHAR);
            addColumn(t, "TYPE_NAME", Types.VARCHAR);
            addColumn(t, "SELF_REFERENCING_COL_NAME", Types.VARCHAR);
            addColumn(t, "REF_GENERATION", Types.VARCHAR);

            // -------------------------------------------------------------
            // extended
            // ------------------------------------------------------------
            addColumn(t, "HSQLDB_TYPE", Types.VARCHAR, 6, true);
            addColumn(t, "READ_ONLY", Types.BOOLEAN, false);         // not null

            // ------------------------------------------------------------
            // order TABLE_TYPE, TABLE_SCHEM and TABLE_NAME
            // added for unique: TABLE_CAT
            // false PK, as TABLE_SCHEM and/or TABLE_CAT may be null
            t.createPrimaryKey(null, new int[] {
                3, 1, 2, 0
            }, false);

            return t;
        }

        // intermediate holders
        Iterator    tables;
        Table       table;
        Object[]    row;
        HsqlName    accessKey;
        DITableInfo ti;

        // column number mappings
        // jdbc 1
        final int itable_cat   = 0;
        final int itable_schem = 1;
        final int itable_name  = 2;
        final int itable_type  = 3;
        final int iremark      = 4;

        // jdbc 3
        final int itype_cat   = 5;
        final int itype_schem = 6;
        final int itype_name  = 7;
        final int isref_cname = 8;
        final int iref_gen    = 9;

        // hsqldb ext
        final int ihsqldb_type = 10;
        final int iread_only   = 11;

        // Initialization
        tables = allTables();
        ti     = new DITableInfo();

        // Do it.
        while (tables.hasNext()) {
            table = (Table) tables.next();

            if (!isAccessibleTable(table)) {
                continue;
            }

            ti.setTable(table);

            row               = t.getEmptyRowData();
            row[itable_cat]   = ns.getCatalogName(table);
            row[itable_schem] = table.getSchemaName();
            row[itable_name]  = ti.getName();
            row[itable_type]  = ti.getStandardType();
            row[iremark]      = ti.getRemark();
            row[ihsqldb_type] = ti.getHsqlType();
            row[iread_only]   = ti.isReadOnly();

            t.insertSys(row);
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the table types
     * available in this database. <p>
     *
     * In general, the range of values that may be commonly encounted across
     * most DBMS implementations is: <p>
     *
     * <UL>
     *   <LI><FONT color='#FF00FF'>"TABLE"</FONT>
     *   <LI><FONT color='#FF00FF'>"VIEW"</FONT>
     *   <LI><FONT color='#FF00FF'>"SYSTEM TABLE"</FONT>
     *   <LI><FONT color='#FF00FF'>"GLOBAL TEMPORARY"</FONT>
     *   <LI><FONT color='#FF00FF'>"LOCAL TEMPORARY"</FONT>
     *   <LI><FONT color='#FF00FF'>"ALIAS"</FONT>
     *   <LI><FONT color='#FF00FF'>"SYNONYM"</FONT>
     * </UL> <p>
     *
     * As of HSQLDB 1.7.2, the engine supports and thus this method reports
     * only a subset of the range above: <p>
     *
     * <UL>
     *   <LI><FONT color='#FF00FF'>"TABLE"</FONT>
     *    (HSQLDB MEMORY, CACHED and TEXT tables)
     *   <LI><FONT color='#FF00FF'>"VIEW"</FONT>  (Views)
     *   <LI><FONT color='#FF00FF'>"SYSTEM TABLE"</FONT>
     *    (The tables generated by this object)
     *   <LI><FONT color='#FF00FF'>"GLOBAL TEMPORARY"</FONT>
     *    (HSQLDB TEMP and TEMP TEXT tables)
     * </UL> <p>
     *
     * @return a <code>Table</code> object describing the table types
     *        available in this database
     * @throws HsqlException if an error occurs while producing the table
     */
    Table SYSTEM_TABLETYPES() throws HsqlException {

        Table t = sysTables[SYSTEM_TABLETYPES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_TABLETYPES]);

            addColumn(t, "TABLE_TYPE", Types.VARCHAR, 16, false);    // not null

            // order: TABLE_TYPE
            // true PK
            t.createPrimaryKey(null, new int[]{ 0 }, true);

            return t;
        }

        Object[] row;

        for (int i = 0; i < tableTypes.length; i++) {
            row    = t.getEmptyRowData();
            row[0] = tableTypes[i];

            t.insertSys(row);
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the
     * result expected by the JDBC DatabaseMetaData interface implementation
     * for system-defined SQL types supported as table columns.
     *
     * <pre class="SqlCodeExample">
     * TYPE_NAME          VARCHAR   the canonical name for DDL statements.
     * DATA_TYPE          SMALLINT  data type code from DITypes.
     * PRECISION          INTEGER   max column size.
     *                              number => max precision.
     *                              character => max characters.
     *                              datetime => max chars incl. frac. component.
     * LITERAL_PREFIX     VARCHAR   char(s) prefixing literal of this type.
     * LITERAL_SUFFIX     VARCHAR   char(s) terminating literal of this type.
     * CREATE_PARAMS      VARCHAR   Localized syntax-order list of domain
     *                              create parameter keywords.
     *                              - for human consumption only
     * NULLABLE           SMALLINT  {No Nulls | Nullable | Unknown}
     * CASE_SENSITIVE     BOOLEAN   case-sensitive in collations/comparisons?
     * SEARCHABLE         SMALLINT  {None | Char (Only WHERE .. LIKE) |
     *                               Basic (Except WHERE .. LIKE) |
     *                               Searchable (All forms)}
     * UNSIGNED_ATTRIBUTE BOOLEAN   {TRUE  (unsigned) | FALSE (signed) |
     *                               NULL (non-numeric or not applicable)}
     * FIXED_PREC_SCALE   BOOLEAN   {TRUE (fixed) | FALSE (variable) |
     *                               NULL (non-numeric or not applicable)}
     * AUTO_INCREMENT     BOOLEAN   automatic unique value generated for
     *                              inserts and updates when no value or
     *                              NULL specified?
     * LOCAL_TYPE_NAME    VARCHAR   localized name of data type;
     *                              - NULL if not supported.
     *                              - for human consuption only
     * MINIMUM_SCALE      SMALLINT  minimum scale supported.
     * MAXIMUM_SCALE      SMALLINT  maximum scale supported.
     * SQL_DATA_TYPE      INTEGER   value expected in SQL CLI SQL_DESC_TYPE
     *                              field of the SQLDA.
     * SQL_DATETIME_SUB   INTEGER   SQL CLI datetime/interval subcode.
     * NUM_PREC_RADIX     INTEGER   numeric base w.r.t # of digits reported in
     *                              PRECISION column (typically 10).
     * TYPE_SUB           INTEGER   From DITypes:
     *                              {TYPE_SUB_DEFAULT | TYPE_SUB_IDENTITY |
     *                               TYPE_SUB_IGNORECASE}
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing the
     *      system-defined SQL types supported as table columns
     * @throws HsqlException if an error occurs while producing the table
     */
    final Table SYSTEM_TYPEINFO() throws HsqlException {

        Table t = sysTables[SYSTEM_TYPEINFO];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_TYPEINFO]);

            //-------------------------------------------
            // required by JDBC:
            // ------------------------------------------
            addColumn(t, "TYPE_NAME", Types.VARCHAR, 32, false);
            addColumn(t, "DATA_TYPE", Types.SMALLINT, false);
            addColumn(t, "PRECISION", Types.INTEGER);
            addColumn(t, "LITERAL_PREFIX", Types.VARCHAR, 2, true);
            addColumn(t, "LITERAL_SUFFIX", Types.VARCHAR, 2, true);
            addColumn(t, "CREATE_PARAMS", Types.VARCHAR);
            addColumn(t, "NULLABLE", Types.SMALLINT);
            addColumn(t, "CASE_SENSITIVE", Types.BOOLEAN);
            addColumn(t, "SEARCHABLE", Types.SMALLINT);
            addColumn(t, "UNSIGNED_ATTRIBUTE", Types.BOOLEAN);
            addColumn(t, "FIXED_PREC_SCALE", Types.BOOLEAN);
            addColumn(t, "AUTO_INCREMENT", Types.BOOLEAN);
            addColumn(t, "LOCAL_TYPE_NAME", Types.VARCHAR);
            addColumn(t, "MINIMUM_SCALE", Types.SMALLINT);
            addColumn(t, "MAXIMUM_SCALE", Types.SMALLINT);
            addColumn(t, "SQL_DATA_TYPE", Types.INTEGER);
            addColumn(t, "SQL_DATETIME_SUB", Types.INTEGER);
            addColumn(t, "NUM_PREC_RADIX", Types.INTEGER);

            //-------------------------------------------
            // for JDBC sort contract:
            //-------------------------------------------
            addColumn(t, "TYPE_SUB", Types.INTEGER);

            // order: DATA_TYPE, TYPE_SUB
            // true PK
            t.createPrimaryKey(null, new int[] {
                1, 18
            }, true);

            return t;
        }

        Result rs;

        rs = session.sqlExecuteDirectNoPreChecks(
            "select TYPE_NAME, DATA_TYPE, PRECISION, LITERAL_PREFIX, LITERAL_SUFFIX, CREATE_PARAMS, NULLABLE, CASE_SENSITIVE, SEARCHABLE,"
            + "UNSIGNED_ATTRIBUTE, FIXED_PREC_SCALE, AUTO_INCREMENT, LOCAL_TYPE_NAME, MINIMUM_SCALE, "
            + "MAXIMUM_SCALE, SQL_DATA_TYPE, SQL_DATETIME_SUB, NUM_PREC_RADIX, TYPE_SUB "
            + "from INFORMATION_SCHEMA.SYSTEM_ALLTYPEINFO  where AS_TAB_COL = true;");

        t.insertSys(rs);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing, in an extended
     * fashion, all of the system or formal specification SQL types known to
     * this database, including its level of support for them (which may
     * be no support at all) in various capacities. <p>
     *
     * <pre class="SqlCodeExample">
     * TYPE_NAME          VARCHAR   the canonical name used in DDL statements.
     * DATA_TYPE          SMALLINT  data type code from Types
     * PRECISION          INTEGER   max column size.
     *                              number => max. precision.
     *                              character => max characters.
     *                              datetime => max chars incl. frac. component.
     * LITERAL_PREFIX     VARCHAR   char(s) prefixing literal of this type.
     * LITERAL_SUFFIX     VARCHAR   char(s) terminating literal of this type.
     * CREATE_PARAMS      VARCHAR   Localized syntax-order list of domain
     *                              create parameter keywords.
     *                              - for human consumption only
     * NULLABLE           SMALLINT  { No Nulls | Nullable | Unknown }
     * CASE_SENSITIVE     BOOLEAN   case-sensitive in collations/comparisons?
     * SEARCHABLE         SMALLINT  { None | Char (Only WHERE .. LIKE) |
     *                                Basic (Except WHERE .. LIKE) |
     *                                Searchable (All forms) }
     * UNSIGNED_ATTRIBUTE BOOLEAN   { TRUE  (unsigned) | FALSE (signed) |
     *                                NULL (non-numeric or not applicable) }
     * FIXED_PREC_SCALE   BOOLEAN   { TRUE (fixed) | FALSE (variable) |
     *                                NULL (non-numeric or not applicable) }
     * AUTO_INCREMENT     BOOLEAN   automatic unique value generated for
     *                              inserts and updates when no value or
     *                              NULL specified?
     * LOCAL_TYPE_NAME    VARCHAR   Localized name of data type;
     *                              - NULL => not supported (no resource avail).
     *                              - for human consumption only
     * MINIMUM_SCALE      SMALLINT  minimum scale supported.
     * MAXIMUM_SCALE      SMALLINT  maximum scale supported.
     * SQL_DATA_TYPE      INTEGER   value expected in SQL CLI SQL_DESC_TYPE
     *                              field of the SQLDA.
     * SQL_DATETIME_SUB   INTEGER   SQL CLI datetime/interval subcode
     * NUM_PREC_RADIX     INTEGER   numeric base w.r.t # of digits reported
     *                              in PRECISION column (typically 10)
     * INTERVAL_PRECISION INTEGER   interval leading precision (not implemented)
     * AS_TAB_COL         BOOLEAN   type supported as table column?
     * AS_PROC_COL        BOOLEAN   type supported as procedure column?
     * MAX_PREC_ACT       BIGINT    like PRECISION unless value would be
     *                              truncated using INTEGER
     * MIN_SCALE_ACT      INTEGER   like MINIMUM_SCALE unless value would be
     *                              truncated using SMALLINT
     * MAX_SCALE_ACT      INTEGER   like MAXIMUM_SCALE unless value would be
     *                              truncated using SMALLINT
     * COL_ST_CLS_NAME    VARCHAR   Java Class FQN of in-memory representation
     * COL_ST_IS_SUP      BOOLEAN   is COL_ST_CLS_NAME supported under the
     *                              hosting JVM and engine build option?
     * STD_MAP_CLS_NAME   VARCHAR   Java class FQN of standard JDBC mapping
     * STD_MAP_IS_SUP     BOOLEAN   Is STD_MAP_CLS_NAME supported under the
     *                              hosting JVM?
     * CST_MAP_CLS_NAME   VARCHAR   Java class FQN of HSQLDB-provided JDBC
     *                              interface representation
     * CST_MAP_IS_SUP     BOOLEAN   is CST_MAP_CLS_NAME supported under the
     *                              hosting JVM and engine build option?
     * MCOL_JDBC          INTEGER   maximum character octet length representable
     *                              via JDBC interface
     * MCOL_ACT           BIGINT    like MCOL_JDBC unless value would be
     *                              truncated using INTEGER
     * DEF_OR_FIXED_SCALE INTEGER   default or fixed scale for numeric types
     * REMARKS            VARCHAR   localized comment on the data type
     * TYPE_SUB           INTEGER   From Types:
     *                              {TYPE_SUB_DEFAULT | TYPE_SUB_IGNORECASE}
     *                              deprecated: TYPE_SUB_IDENTITY
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing all of the
     *        standard SQL types known to this database
     * @throws HsqlException if an error occurs while producing the table
     */
    final Table SYSTEM_ALLTYPEINFO() throws HsqlException {

        Table t = sysTables[SYSTEM_ALLTYPEINFO];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_ALLTYPEINFO]);

            //-------------------------------------------
            // same as SYSTEM_TYPEINFO:
            // ------------------------------------------
            addColumn(t, "TYPE_NAME", Types.VARCHAR, 32, false);
            addColumn(t, "DATA_TYPE", Types.SMALLINT, false);
            addColumn(t, "PRECISION", Types.INTEGER);
            addColumn(t, "LITERAL_PREFIX", Types.VARCHAR, 2, true);
            addColumn(t, "LITERAL_SUFFIX", Types.VARCHAR, 2, true);
            addColumn(t, "CREATE_PARAMS", Types.VARCHAR);
            addColumn(t, "NULLABLE", Types.SMALLINT);
            addColumn(t, "CASE_SENSITIVE", Types.BOOLEAN);
            addColumn(t, "SEARCHABLE", Types.SMALLINT);
            addColumn(t, "UNSIGNED_ATTRIBUTE", Types.BOOLEAN);
            addColumn(t, "FIXED_PREC_SCALE", Types.BOOLEAN);
            addColumn(t, "AUTO_INCREMENT", Types.BOOLEAN);
            addColumn(t, "LOCAL_TYPE_NAME", Types.VARCHAR);
            addColumn(t, "MINIMUM_SCALE", Types.SMALLINT);
            addColumn(t, "MAXIMUM_SCALE", Types.SMALLINT);
            addColumn(t, "SQL_DATA_TYPE", Types.INTEGER);
            addColumn(t, "SQL_DATETIME_SUB", Types.INTEGER);
            addColumn(t, "NUM_PREC_RADIX", Types.INTEGER);

            //-------------------------------------------
            // SQL CLI / ODBC - not in JDBC spec
            // ------------------------------------------
            addColumn(t, "INTERVAL_PRECISION", Types.INTEGER);

            //-------------------------------------------
            // extended:
            //-------------------------------------------
            // level of support
            //-------------------------------------------
            addColumn(t, "AS_TAB_COL", Types.BOOLEAN);

            // for instance, some executable methods take Connection
            // or return non-serializable Object such as ResultSet, neither
            // of which maps to a supported table column type but which
            // we show as JAVA_OBJECT in SYSTEM_PROCEDURECOLUMNS.
            // Also, triggers take Object[] row, which we show as ARRAY
            // presently, although STRUCT would probably be better in the
            // future, as the row can actually contain mixed data types.
            addColumn(t, "AS_PROC_COL", Types.BOOLEAN);

            //-------------------------------------------
            // actual values for attributes that cannot be represented
            // within the limitations of the SQL CLI / JDBC interface
            //-------------------------------------------
            addColumn(t, "MAX_PREC_ACT", Types.BIGINT);
            addColumn(t, "MIN_SCALE_ACT", Types.INTEGER);
            addColumn(t, "MAX_SCALE_ACT", Types.INTEGER);

            //-------------------------------------------
            // how do we store this internally as a column value?
            //-------------------------------------------
            addColumn(t, "COL_ST_CLS_NAME", Types.VARCHAR);
            addColumn(t, "COL_ST_IS_SUP", Types.BOOLEAN);

            //-------------------------------------------
            // what is the standard Java mapping for the type?
            //-------------------------------------------
            addColumn(t, "STD_MAP_CLS_NAME", Types.VARCHAR);
            addColumn(t, "STD_MAP_IS_SUP", Types.BOOLEAN);

            //-------------------------------------------
            // what, if any, custom mapping do we provide?
            // (under the current build options and hosting VM)
            //-------------------------------------------
            addColumn(t, "CST_MAP_CLS_NAME", Types.VARCHAR);
            addColumn(t, "CST_MAP_IS_SUP", Types.BOOLEAN);

            //-------------------------------------------
            // what is the max representable and actual
            // character octet length, if applicable?
            //-------------------------------------------
            addColumn(t, "MCOL_JDBC", Types.INTEGER);
            addColumn(t, "MCOL_ACT", Types.BIGINT);

            //-------------------------------------------
            // what is the default or fixed scale, if applicable?
            //-------------------------------------------
            addColumn(t, "DEF_OR_FIXED_SCALE", Types.INTEGER);

            //-------------------------------------------
            // Any type-specific, localized remarks can go here
            //-------------------------------------------
            addColumn(t, "REMARKS", Types.VARCHAR);

            //-------------------------------------------
            // required for JDBC sort contract:
            //-------------------------------------------
            addColumn(t, "TYPE_SUB", Types.INTEGER);

            // order:  DATA_TYPE, TYPE_SUB
            // true primary key
            t.createPrimaryKey(null, new int[] {
                1, 34
            }, true);

            return t;
        }

        Object[]   row;
        int        type;
        DITypeInfo ti;

        //-----------------------------------------
        // Same as SYSTEM_TYPEINFO
        //-----------------------------------------
        final int itype_name          = 0;
        final int idata_type          = 1;
        final int iprecision          = 2;
        final int iliteral_prefix     = 3;
        final int iliteral_suffix     = 4;
        final int icreate_params      = 5;
        final int inullable           = 6;
        final int icase_sensitive     = 7;
        final int isearchable         = 8;
        final int iunsigned_attribute = 9;
        final int ifixed_prec_scale   = 10;
        final int iauto_increment     = 11;
        final int ilocal_type_name    = 12;
        final int iminimum_scale      = 13;
        final int imaximum_scale      = 14;
        final int isql_data_type      = 15;
        final int isql_datetime_sub   = 16;
        final int inum_prec_radix     = 17;

        //------------------------------------------
        // Extentions
        //------------------------------------------
        // not in JDBC, but in SQL CLI SQLDA / ODBC
        //------------------------------------------
        final int iinterval_precision = 18;

        //------------------------------------------
        // HSQLDB/Java-specific:
        //------------------------------------------
        final int iis_sup_as_tcol = 19;
        final int iis_sup_as_pcol = 20;

        //------------------------------------------
        final int imax_prec_or_len_act = 21;
        final int imin_scale_actual    = 22;
        final int imax_scale_actual    = 23;

        //------------------------------------------
        final int ics_cls_name         = 24;
        final int ics_cls_is_supported = 25;

        //------------------------------------------
        final int ism_cls_name         = 26;
        final int ism_cls_is_supported = 27;

        //------------------------------------------
        final int icm_cls_name         = 28;
        final int icm_cls_is_supported = 29;

        //------------------------------------------
        final int imax_char_oct_len_jdbc = 30;
        final int imax_char_oct_len_act  = 31;

        //------------------------------------------
        final int idef_or_fixed_scale = 32;

        //------------------------------------------
        final int iremarks = 33;

        //------------------------------------------
        final int itype_sub = 34;

        ti = new DITypeInfo();

        for (int i = 0; i < Types.ALL_TYPES.length; i++) {
            ti.setTypeCode(Types.ALL_TYPES[i][0]);
            ti.setTypeSub(Types.ALL_TYPES[i][1]);

            row                      = t.getEmptyRowData();
            row[itype_name]          = ti.getTypeName();
            row[idata_type]          = ti.getDataType();
            row[iprecision]          = ti.getPrecision();
            row[iliteral_prefix]     = ti.getLiteralPrefix();
            row[iliteral_suffix]     = ti.getLiteralSuffix();
            row[icreate_params]      = ti.getCreateParams();
            row[inullable]           = ti.getNullability();
            row[icase_sensitive]     = ti.isCaseSensitive();
            row[isearchable]         = ti.getSearchability();
            row[iunsigned_attribute] = ti.isUnsignedAttribute();
            row[ifixed_prec_scale]   = ti.isFixedPrecisionScale();
            row[iauto_increment]     = ti.isAutoIncrement();
            row[ilocal_type_name]    = ti.getLocalName();
            row[iminimum_scale]      = ti.getMinScale();
            row[imaximum_scale]      = ti.getMaxScale();
            row[isql_data_type]      = ti.getSqlDataType();
            row[isql_datetime_sub]   = ti.getSqlDateTimeSub();
            row[inum_prec_radix]     = ti.getNumPrecRadix();

            //------------------------------------------
            row[iinterval_precision] = ti.getIntervalPrecision();

            //------------------------------------------
            row[iis_sup_as_tcol] = ti.isSupportedAsTCol();
            row[iis_sup_as_pcol] = ti.isSupportedAsPCol();

            //------------------------------------------
            row[imax_prec_or_len_act] = ti.getPrecisionAct();
            row[imin_scale_actual]    = ti.getMinScaleAct();
            row[imax_scale_actual]    = ti.getMaxScaleAct();

            //------------------------------------------
            row[ics_cls_name]         = ti.getColStClsName();
            row[ics_cls_is_supported] = ti.isColStClsSupported();

            //------------------------------------------
            row[ism_cls_name]         = ti.getStdMapClsName();
            row[ism_cls_is_supported] = ti.isStdMapClsSupported();

            //------------------------------------------
            row[icm_cls_name] = ti.getCstMapClsName();

            try {
                ns.classForName((String) row[icm_cls_name]);

                row[icm_cls_is_supported] = Boolean.TRUE;
            } catch (Exception e) {
                row[icm_cls_is_supported] = Boolean.FALSE;
            }

            //------------------------------------------
            row[imax_char_oct_len_jdbc] = ti.getCharOctLen();
            row[imax_char_oct_len_act]  = ti.getCharOctLenAct();

            //------------------------------------------
            row[idef_or_fixed_scale] = ti.getDefaultScale();

            //------------------------------------------
            row[iremarks] = ti.getRemarks();

            //------------------------------------------
            row[itype_sub] = ti.getDataTypeSub();

            t.insertSys(row);
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the
     * visible <code>Users</code> defined within this database.
     * @return table containing information about the users defined within
     *      this database
     * @throws HsqlException if an error occurs while producing the table
     */
    Table SYSTEM_USERS() throws HsqlException {

        Table t = sysTables[SYSTEM_USERS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_USERS]);

            addColumn(t, "USER", Types.VARCHAR, false);
            addColumn(t, "ADMIN", Types.BOOLEAN, false);

            // order: USER
            // true PK
            t.createPrimaryKey(null, new int[]{ 0 }, true);

            return t;
        }

        // Intermediate holders
        HsqlArrayList users;
        User          user;
        int           userCount;
        Object[]      row;

        // Initialization
        users = database.getUserManager().listVisibleUsers(session, false);

        // Do it.
        for (int i = 0; i < users.size(); i++) {
            row    = t.getEmptyRowData();
            user   = (User) users.get(i);
            row[0] = user.getName();
            row[1] = ValuePool.getBoolean(user.isAdmin());

            t.insertSys(row);
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * The SYSTEM_CHECK_CONSTRAINTS table has one row for each domain
     * constraint, table check constraint, and assertion. <p>
     *
     * <b>Definition:</b><p>
     *
     * <pre class="SqlCodeExample">
     * CREATE TABLE SYSTEM_CHECK_CONSTRAINTS (
     *      CONSTRAINT_CATALOG  VARCHAR NULL,
     *      CONSTRAINT_SCHEMA   VARCHAR NULL,
     *      CONSTRAINT_NAME     VARCHAR NOT NULL,
     *      CHECK_CLAUSE        VARCHAR NOT NULL,
     *
     *      UNIQUE ( CONSTRAINT_CATALOG, CONSTRAINT_SCHEMA, CONSTRAINT_NAME )
     * )
     * </pre>
     *
     * <b>Description:</b><p>
     *
     * <ol>
     * <li> The values of CONSTRAINT_CATALOG, CONSTRAINT_SCHEMA and
     *      CONSTRAINT_NAME are the catalog name, unqualified schema name,
     *      and qualified identifier, respectively, of the constraint being
     *      described. <p>
     *
     * <li> Case: <p>
     *
     *      <table>
     *          <tr>
     *               <td valign="top" halign="left">a)</td>
     *               <td> If the character representation of the
     *                    &lt;search condition&gt; contained in the
     *                    &lt;check constraint definition&gt;,
     *                    &lt;domain constraint definition&gt;, or
     *                    &lt;assertion definition&gt; that defined
     *                    the check constraint being described can be
     *                    represented without truncation, then the
     *                    value of CHECK_CLAUSE is that character
     *                    representation. </td>
     *          </tr>
     *          <tr>
     *              <td align="top" halign="left">b)</td>
     *              <td>Otherwise, the value of CHECK_CLAUSE is the
     *                  null value.</td>
     *          </tr>
     *      </table>
     * </ol>
     */
    final Table SYSTEM_CHECK_CONSTRAINTS() throws HsqlException {

        Table t = sysTables[SYSTEM_CHECK_CONSTRAINTS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_CHECK_CONSTRAINTS]);

            addColumn(t, "CONSTRAINT_CATALOG", Types.VARCHAR);
            addColumn(t, "CONSTRAINT_SCHEMA", Types.VARCHAR);
            addColumn(t, "CONSTRAINT_NAME", Types.VARCHAR, false);    // not null
            addColumn(t, "CHECK_CLAUSE", Types.VARCHAR, false);       // not null

            // false PK, as FKTABLE_CAT, FKTABLE_SCHEM and/or FK_NAME
            // may be null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2
            }, false);

            return t;
        }

        // calculated column values
        // Intermediate holders
        Iterator      tables;
        Table         table;
        Constraint[]  tableConstraints;
        int           constraintCount;
        Constraint    constraint;
        HsqlArrayList constraintList;
        Object[]      row;

        // column number mappings
        final int icons_cat    = 0;
        final int icons_schem  = 1;
        final int icons_name   = 2;
        final int icons_clause = 3;

        tables         = database.schemaManager.allTablesIterator();
        constraintList = new HsqlArrayList();

        while (tables.hasNext()) {
            table = (Table) tables.next();

            if (!isAccessibleTable(table)) {
                continue;
            }

            tableConstraints = table.getConstraints();
            constraintCount  = tableConstraints.length;

            for (int i = 0; i < constraintCount; i++) {
                constraint = (Constraint) tableConstraints[i];

                if (constraint.getType() == Constraint.CHECK) {
                    constraintList.add(constraint);
                }
            }
        }

        for (int i = 0; i < constraintList.size(); i++) {
            row              = t.getEmptyRowData();
            constraint       = (Constraint) constraintList.get(i);
            table            = constraint.getMain();
            row[icons_cat]   = ns.getCatalogName(table);
            row[icons_schem] = table.getSchemaName();
            row[icons_name]  = constraint.constName.name;

            try {
                row[icons_clause] = constraint.core.check.getDDL();
            } catch (Exception e) {}

            t.insertSys(row);
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * The SYSTEM_SEQUENCES table has one row for each external sequence
     * generator. <p>
     *
     * <b>Definition:</b> <p>
     *
     * <pre class="SqlCodeExample">
     * CREATE TABLE SYSTEM_SEQUENCES (
     *      SEQUENCE_CATALOG     VARCHAR NULL,
     *      SEQUENCE_SCHEMA      VARCHAR NULL,
     *      SEQUENCE_NAME        VARCHAR NOT NULL,
     *      DTD_IDENTIFIER       VARCHAR NOT NULL,
     *      MAXIMUM_VALUE        VARCHAR NOT NULL,
     *      MINIMUM_VALUE        VARCHAR NOT NULL,
     *      INCREMENT            VARCHAR NOT NULL,
     *      CYCLE_OPTION         VARCHAR NOT NULL,
     *      START_WITH           VARCHAR NOT NULL,
     *
     *      CHECK(CYCLE_OPTION IN('YES', 'NO')),
     *
     *      CHECK(CAST(START_WITH AS BIGINT)
     *          BETWEEN CAST(MINIMUM_VALUE AS BIGINT)
     *              AND CAST(MAXIMUM_VALUE AS BIGINT)),
     *
     *      UNIQUE(SEQUENCE_CATALOG, SEQUENCE_SCHEMA, SEQUENCE_NAME)
     * )
     * </pre>
     *
     * <b>DESCRIPTION:</b><p>
     *
     * <ol>
     * <li> The values of SEQUENCE_CATALOG, SEQUENCE_SCHEMA, and
     *      SEQUENCE_NAME are the catalog name, unqualified schema name,
     *      and qualified identifier, respectively, of the sequence generator
     *      being described. <p>
     *
     * <li> The values of SEQUENCE_CATALOG, SEQUENCE_SCHEMA, SEQUENCE_NAME, and
     *      DTD_IDENTIFIER are the values of OBJECT_CATALOG, OBJECT_SCHEMA,
     *      OBJECT_NAME, and DTD_IDENTIFIER, respectively, of the row in
     *      DATA_TYPE_DESCRIPTOR (not yet implemented) that describes the data
     *      type of the sequence generator. <p>
     *
     * <li> The values of MAXIMUM_VALUE, MINIMUM_VALUE, and INCREMENT are the
     *      character representations of maximum value, minimum value,
     *      and increment, respectively, of the sequence generator being
     *      described. <p>
     *
     * <li> The values of CYCLE_OPTION have the following meanings: <p>
     *
     *      <table border cellpadding="3">
     *          <tr>
     *              <td nowrap>YES</td>
     *              <td nowrap>The cycle option of the sequence generator
     *                         is CYCLE.</td>
     *          <tr>
     *              <td nowrap>NO</td>
     *              <td nowrap>The cycle option of the sequence generator is
     *                         NO CYCLE.</td>
     *          </tr>
     *      </table> <p>
     *
     * <li> The value of START_WITH is HSQLDB-specific (not in the SQL 200n
     *      spec).  <p>
     *
     *      It is the character representation of the START WITH clause
     *      value that would be required to recreate or ALTER RESET START WITH
     *      the described SEQUENCE, such that its behaviour would be identical
     *      to that exhibited at the instant this table is materialized in the
     *      session context. <p>
     *
     *      In short, this is the character representation of value that
     *      would be generated by NEXT VALUE FOR at the instant this table
     *      is materialized in session context. <p>
     *
     */
    final Table SYSTEM_SEQUENCES() throws HsqlException {

        Table t = sysTables[SYSTEM_SEQUENCES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_SEQUENCES]);

            addColumn(t, "SEQUENCE_CATALOG", Types.VARCHAR);
            addColumn(t, "SEQUENCE_SCHEMA", Types.VARCHAR);
            addColumn(t, "SEQUENCE_NAME", Types.VARCHAR, true);        // not null
            addColumn(t, "DTD_IDENTIFIER", Types.VARCHAR, true);       // not null
            addColumn(t, "MAXIMUM_VALUE", Types.VARCHAR, 20, true);    // not null
            addColumn(t, "MINIMUM_VALUE", Types.VARCHAR, 20, true);    // not null
            addColumn(t, "INCREMENT", Types.VARCHAR, 20, true);        // not null
            addColumn(t, "CYCLE_OPTION", Types.VARCHAR, 3, true);      // not null

            // HSQLDB-specific
            addColumn(t, "START_WITH", Types.VARCHAR, 20, true);       // not null

            // order SEQUENCE_CATALOG, SEQUENCE_SCHEMA, SEQUENCE_NAME
            // false PK, as SCHEMA and/or CATALOG may be null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2
            }, false);

            return t;
        }

        //
        final int iseq_cat    = 0;
        final int iseq_schem  = 1;
        final int iseq_name   = 2;
        final int iseq_dtdid  = 3;
        final int iseq_max    = 4;
        final int iseq_min    = 5;
        final int iseq_incr   = 6;
        final int iseq_cycopt = 7;
        final int iseq_start  = 8;

        //
        final String min = ValuePool.getString("0");
        final String intMax =
            ValuePool.getString(String.valueOf(Integer.MAX_VALUE));

        // 19 is longer than max value pool string length (15)
        final String longMax     = String.valueOf(Long.MAX_VALUE);
        final String cycleOption = ValuePool.getString("NO");

        //
        Iterator       it;
        Object[]       row;
        String         sequenceName;
        NumberSequence sequence;
        int            dataType;

        it = database.schemaManager.allSequencesIterator();

        while (it.hasNext()) {
            row              = t.getEmptyRowData();
            sequence         = (NumberSequence) it.next();
            dataType         = sequence.getType();
            sequenceName     = sequence.getName().name;
            row[iseq_cat]    = ns.getCatalogName(sequence);
            row[iseq_schem]  = sequence.getSchemaName();
            row[iseq_name]   = sequenceName;
            row[iseq_dtdid]  = Types.getTypeString(dataType);
            row[iseq_min]    = min;
            row[iseq_incr]   = String.valueOf(sequence.getIncrement());
            row[iseq_cycopt] = cycleOption;
            row[iseq_start]  = String.valueOf(sequence.peek());

            switch (dataType) {

                case Types.INTEGER : {
                    row[iseq_max] = intMax;

                    break;
                }
                case Types.BIGINT :
                default : {
                    row[iseq_max] = longMax;

                    break;
                }
            }

            t.insertSys(row);
        }

        t.setDataReadOnly(true);

        return t;
    }
}
