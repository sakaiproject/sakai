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

import java.lang.reflect.Constructor;

import org.hsqldb.lib.IntValueHashMap;

// fredt@users - 1.7.2 - structural modifications to allow inheritance
// boucherB@users 20020305 - completed inheritance work, including final access
// boucherB@users 20020305 - javadoc updates/corrections
// boucherB@users 20020305 - SYSTEM_VIEWS brought in line with SQL 200n
// boucherb@users 20050514 - further SQL 200n metdata support

/**
 * Base class for system tables. Includes a factory method which returns the
 * most complete implementation available in the jar. This base implementation
 * knows the names of all system tables but returns null for any system table.
 * <p>
 * This class has been developed from scratch to replace the previous
 * DatabaseInformation implementations. <p>
 *
 * @author boucherb@users
 * @version 1.8.0
 * @since 1.7.2
 */
class DatabaseInformation {

    // ids for system table names strictly in order of sysTableNames[]
    protected static final int SYSTEM_BESTROWIDENTIFIER = 0;
    protected static final int SYSTEM_CATALOGS          = 1;
    protected static final int SYSTEM_COLUMNPRIVILEGES  = 2;
    protected static final int SYSTEM_COLUMNS           = 3;
    protected static final int SYSTEM_CROSSREFERENCE    = 4;
    protected static final int SYSTEM_INDEXINFO         = 5;
    protected static final int SYSTEM_PRIMARYKEYS       = 6;
    protected static final int SYSTEM_PROCEDURECOLUMNS  = 7;
    protected static final int SYSTEM_PROCEDURES        = 8;
    protected static final int SYSTEM_SCHEMAS           = 9;
    protected static final int SYSTEM_SUPERTABLES       = 10;
    protected static final int SYSTEM_SUPERTYPES        = 11;
    protected static final int SYSTEM_TABLEPRIVILEGES   = 12;
    protected static final int SYSTEM_TABLES            = 13;
    protected static final int SYSTEM_TABLETYPES        = 14;
    protected static final int SYSTEM_TYPEINFO          = 15;
    protected static final int SYSTEM_UDTATTRIBUTES     = 16;
    protected static final int SYSTEM_UDTS              = 17;
    protected static final int SYSTEM_USERS             = 18;
    protected static final int SYSTEM_VERSIONCOLUMNS    = 19;

    // HSQLDB-specific
    protected static final int SYSTEM_ALIASES         = 20;
    protected static final int SYSTEM_BYTECODE        = 21;
    protected static final int SYSTEM_CACHEINFO       = 22;
    protected static final int SYSTEM_CLASSPRIVILEGES = 23;
    protected static final int SYSTEM_SESSIONINFO     = 24;
    protected static final int SYSTEM_PROPERTIES      = 25;
    protected static final int SYSTEM_SESSIONS        = 26;
    protected static final int SYSTEM_TRIGGERCOLUMNS  = 27;
    protected static final int SYSTEM_TRIGGERS        = 28;
    protected static final int SYSTEM_ALLTYPEINFO     = 29;

// boucherb@users 20030305 - brought in line with SQL 200n
    protected static final int SYSTEM_VIEWS = 30;

// boucherb@users 20030403 - isolated and improved text table reporting
    protected static final int SYSTEM_TEXTTABLES = 31;

// boucherb@users 20040107 - metadata support for sequences
    protected static final int SYSTEM_SEQUENCES        = 32;
    protected static final int SYSTEM_USAGE_PRIVILEGES = 33;

// boucherb@users 20040107 - metadata support for constraints
    protected static final int SYSTEM_CHECK_CONSTRAINTS = 34;
    protected static final int SYSTEM_TABLE_CONSTRAINTS = 35;

// boucherb@users 20040107 - metadata support for view usage breakdown- SQL 200n
    protected static final int SYSTEM_CHECK_COLUMN_USAGE  = 36;
    protected static final int SYSTEM_CHECK_ROUTINE_USAGE = 37;
    protected static final int SYSTEM_CHECK_TABLE_USAGE   = 38;
    protected static final int SYSTEM_VIEW_COLUMN_USAGE   = 39;
    protected static final int SYSTEM_VIEW_TABLE_USAGE    = 40;
    protected static final int SYSTEM_VIEW_ROUTINE_USAGE  = 41;

// boucherb@users 20050514 - further SQL 200n metdata support
    protected static final int SYSTEM_AUTHORIZATIONS                 = 42;
    protected static final int SYSTEM_COLLATIONS                     = 43;
    protected static final int SYSTEM_ROLE_AUTHORIZATION_DESCRIPTORS = 44;
    protected static final int SYSTEM_SCHEMATA                       = 45;

    /** system table names strictly in order of their ids */
    protected static final String[] sysTableNames = {
        "SYSTEM_BESTROWIDENTIFIER",                 //
        "SYSTEM_CATALOGS",                          //
        "SYSTEM_COLUMNPRIVILEGES",                  //
        "SYSTEM_COLUMNS",                           //
        "SYSTEM_CROSSREFERENCE",                    //
        "SYSTEM_INDEXINFO",                         //
        "SYSTEM_PRIMARYKEYS",                       //
        "SYSTEM_PROCEDURECOLUMNS",                  //
        "SYSTEM_PROCEDURES",                        //
        "SYSTEM_SCHEMAS",                           //
        "SYSTEM_SUPERTABLES",                       //
        "SYSTEM_SUPERTYPES",                        //
        "SYSTEM_TABLEPRIVILEGES",                   //
        "SYSTEM_TABLES",                            //
        "SYSTEM_TABLETYPES",                        //
        "SYSTEM_TYPEINFO",                          //
        "SYSTEM_UDTATTRIBUTES",                     //
        "SYSTEM_UDTS",                              //
        "SYSTEM_USERS",                             //
        "SYSTEM_VERSIONCOLUMNS",                    //

        // HSQLDB-specific
        "SYSTEM_ALIASES",                           //
        "SYSTEM_BYTECODE",                          //
        "SYSTEM_CACHEINFO",                         //
        "SYSTEM_CLASSPRIVILEGES",                   //
        "SYSTEM_SESSIONINFO",                       //
        "SYSTEM_PROPERTIES",                        //
        "SYSTEM_SESSIONS",                          //
        "SYSTEM_TRIGGERCOLUMNS",                    //
        "SYSTEM_TRIGGERS",                          //
        "SYSTEM_ALLTYPEINFO",                       //

        // boucherb@users 20030305 - brought in line with SQL 200n
        "SYSTEM_VIEWS",

        // boucherb@users 20030403 - isolated and improved text table reporting
        "SYSTEM_TEXTTABLES",

        // boucherb@users 20040107 - metadata support for sequences - SQL 200n
        "SYSTEM_SEQUENCES",                         //
        "SYSTEM_USAGE_PRIVILEGES",

        // boucherb@users 20040107 - metadata support for constraints - SQL 200n
        "SYSTEM_CHECK_CONSTRAINTS",                 //
        "SYSTEM_TABLE_CONSTRAINTS",                 //

        // boucherb@users 20040107 - metadata support for usage - SQL 200n
        "SYSTEM_CHECK_COLUMN_USAGE",                //
        "SYSTEM_CHECK_ROUTINE_USAGE",               //
        "SYSTEM_CHECK_TABLE_USAGE",                 //
        "SYSTEM_VIEW_COLUMN_USAGE",                 //
        "SYSTEM_VIEW_TABLE_USAGE",                  //
        "SYSTEM_VIEW_ROUTINE_USAGE",                //

        // boucherb@users 20050514 - further SQL 200n metadata support
        "SYSTEM_AUTHORIZATIONS",                    //
        "SYSTEM_COLLATIONS",                        //
        "SYSTEM_ROLE_AUTHORIZATION_DESCRIPTORS",    //
        "SYSTEM_SCHEMATA"                           //

        // Future use
//        "SYSTEM_ASSERTIONS",
//        "SYSTEM_ATTRIBUTES",
//        "SYSTEM_AUTHORIZATIONS",                 // boucherb@users 20050514 - implemented
//        "SYSTEM_CHARACTER_ENCODING_FORMS",
//        "SYSTEM_CHARACTER_REPERTOIRES",
//        "SYSTEM_CHARACTER_SETS",
//        "SYSTEM_CHECK_COLUMN_USAGE",             // boucherb@users 20040107 - implemented
//        "SYSTEM_CHECK_ROUTINE_USAGE",            // boucherb@users 20040107 - implemented
//        "SYSTEM_CHECK_CONSTRAINTS",              // boucherb@users 20040107 - implemented
//        "SYSTEM_CHECK_TABLE_USAGE",              // boucherb@users 20040107 - implemented
//        "SYSTEM_COLLATION_CHARACTER_SET_APPLICABILITY",
//        "SYSTEM_COLLATIONS",                     // boucherb@users 20050514 - implemented
//        "SYSTEM_COLUMN_COLUMN_USAGE",
//        "SYSTEM_COLUMN_OPTIONS",
//        "SYSTEM_COLUMN_PRIVILEGES",
//        "SYSTEM_COLUMNS",
//        "SYSTEM_DATA_TYPE_DESCRIPTOR",
//        "SYSTEM_DIRECT_SUPERTABLES",
//        "SYSTEM_DIRECT_SUPERTYPES",
//        "SYSTEM_DOMAIN_CONSTRAINTS",
//        "SYSTEM_DOMAINS",
//        "SYSTEM_ELEMENT_TYPES",
//        "SYSTEM_FIELDS",
//        "SYSTEM_FOREIGN_DATA_WRAPPER_OPTIONS",
//        "SYSTEM_FOREIGN_DATA_WRAPPERS",
//        "SYSTEM_FOREIGN_SERVER_OPTIONS",
//        "SYSTEM_FOREIGN_SERVERS",
//        "SYSTEM_FOREIGN_TABLE_OPTIONS",
//        "SYSTEM_FOREIGN_TABLES",
//        "SYSTEM_JAR_JAR_USAGE",
//        "SYSTEM_JARS",
//        "SYSTEM_KEY_COLUMN_USAGE",
//        "SYSTEM_METHOD_SPECIFICATION_PARAMETERS",
//        "SYSTEM_METHOD_SPECIFICATIONS",
//        "SYSTEM_MODULE_COLUMN_USAGE",
//        "SYSTEM_MODULE_PRIVILEGES",
//        "SYSTEM_MODULE_TABLE_USAGE",
//        "SYSTEM_MODULES",
//        "SYSTEM_PARAMETERS",
//        "SYSTEM_REFERENCED_TYPES",
//        "SYSTEM_REFERENTIAL_CONSTRAINTS",
//        "SYSTEM_ROLE_AUTHORIZATION_DESCRIPTORS", // boucherb@users 20050514 - implemented
//        "SYSTEM_ROLES",
//        "SYSTEM_ROUTINE_COLUMN_USAGE",
//        "SYSTEM_ROUTINE_JAR_USAGE",
//        "SYSTEM_ROUTINE_MAPPING_OPTIONS",
//        "SYSTEM_ROUTINE_MAPPINGS",
//        "SYSTEM_ROUTINE_PRIVILEGES",
//        "SYSTEM_ROUTINE_ROUTINE_USAGE",
//        "SYSTEM_ROUTINE_SEQUENCE_USAGE",
//        "SYSTEM_ROUTINE_TABLE_USAGE",
//        "SYSTEM_ROUTINES",
//        "SYSTEM_SCHEMATA",                       // boucherb@users 20050514 - implemented
//        "SYSTEM_SEQUENCES",                      // boucherb@users 20040107 - implemented
//        "SYSTEM_SQL_FEATURES",
//        "SYSTEM_SQL_IMPLEMENTATION_INFO",
//        "SYSTEM_SQL_LANGUAGES",
//        "SYSTEM_SQL_SIZING",
//        "SYSTEM_SQL_SIZING_PROFILES",
//        "SYSTEM_TABLE_CONSTRAINTS",              // boucherb@users 20040107 - implemented
//        "SYSTEM_TABLE_METHOD_PRIVILEGES",
//        "SYSTEM_TABLE_PRIVILEGES",
//        "SYSTEM_TABLES",
//        "SYSTEM_TRANSFORMS",
//        "SYSTEM_TRANSLATIONS",
//        "SYSTEM_TRIGGER_COLUMN_USAGE",
//        "SYSTEM_TRIGGER_ROUTINE_USAGE",
//        "SYSTEM_TRIGGER_SEQUENCE_USAGE",
//        "SYSTEM_TRIGGER_TABLE_USAGE",
//        "SYSTEM_TRIGGERED_UPDATE_COLUMNS",
//        "SYSTEM_TRIGGERS",
//        "SYSTEM_TYPE_JAR_USAGE",
//        "SYSTEM_USAGE_PRIVILEGES",               // boucherb@users 20040107 - implemented
//        "SYSTEM_USER_DEFINED_TYPE_PRIVILEGES",
//        "SYSTEM_USER_DEFINED_TYPES",
//        "SYSTEM_USER_MAPPING_OPTIONS",
//        "SYSTEM_USER_MAPPINGS",
//        "SYSTEM_USERS",
//        "SYSTEM_VIEW_COLUMN_USAGE",              // boucherb@users 20040107 - implemented
//        "SYSTEM_VIEW_ROUTINE_USAGE",             // boucherb@users 20040107 - implemented
//        "SYSTEM_VIEW_TABLE_USAGE",               // boucherb@users 20040107 - implemented
//        "SYSTEM_VIEWS", // boucherb@users 20030305 - implemented
    };

    /** Map: table name => table id */
    protected static final IntValueHashMap sysTableNamesMap;

    static {
        sysTableNamesMap = new IntValueHashMap(47);

        for (int i = 0; i < sysTableNames.length; i++) {
            sysTableNamesMap.put(sysTableNames[i], i);
        }
    }

    static int getSysTableID(String token) {
        return sysTableNamesMap.get(token, -1);
    }

    /** Database for which to produce tables */
    protected final Database database;

    /**
     * Simple object-wide flag indicating that all of this object's cached
     * data is dirty.
     */
    protected boolean isDirty = true;

    /**
     * state flag -- if true, contentful tables are to be produced, else
     * empty (surrogate) tables are to be produced.  This allows faster
     * database startup where user views reference system tables and faster
     * system table structural reflection for table metadata.
     */
    protected boolean withContent = false;

    /**
     * Factory method retuns the fullest system table producer
     * implementation available.  This instantiates implementations beginning
     * with the most complete, finally choosing an empty table producer
     * implemenation (this class) if no better instance can be constructed.
     * @param db The Database object for which to produce system tables
     * @return the fullest system table producer
     *      implementation available
     * @throws HsqlException never - required by constructor
     */
    static final DatabaseInformation newDatabaseInformation(Database db)
    throws HsqlException {

        Class clazz = null;

        try {
            clazz = Class.forName("org.hsqldb.DatabaseInformationFull");
        } catch (Exception e) {
            try {
                clazz = Class.forName("org.hsqldb.DatabaseInformationMain");
            } catch (Exception e2) {}
        }

        try {
            Class[]     ctorParmTypes = new Class[]{ Database.class };
            Object[]    ctorParms     = new Object[]{ db };
            Constructor ctor = clazz.getDeclaredConstructor(ctorParmTypes);

            return (DatabaseInformation) ctor.newInstance(ctorParms);
        } catch (Exception e) {}

        return new DatabaseInformation(db);
    }

    /**
     * Constructs a new DatabaseInformation instance which knows the names of
     * all system tables (isSystemTable()) but simpy returns null for all
     * getSystemTable() requests. <p>
     *
     * @param db The Database object for which to produce system tables
     * @throws HsqlException never (required for descendents)
     */
    DatabaseInformation(Database db) throws HsqlException {
        database = db;
    }

    /**
     * Tests if the specified name is that of a system table. <p>
     *
     * @param name the name to test
     * @return true if the specified name is that of a system table
     */
    final boolean isSystemTable(String name) {
        return sysTableNamesMap.containsKey(name);
    }

    /**
     * Retrieves a table with the specified name whose content may depend on
     * the execution context indicated by the session argument as well as the
     * current value of <code>withContent</code>. <p>
     *
     * @param session the context in which to produce the table
     * @param name the name of the table to produce
     * @throws HsqlException if a database access error occurs
     * @return a table corresponding to the name and session arguments, or
     *      <code>null</code> if there is no such table to be produced
     */
    Table getSystemTable(Session session, String name) throws HsqlException {
        return null;
    }

    /**
     * Controls caching of all tables produced by this object. <p>
     *
     * Subclasses are free to ignore this, since they may choose an
     * implementation that does not dynamically generate and/or cache
     * table content on an as-needed basis. <p>
     *
     * If not ignored, this call indicates to this object that all cached
     * table data may be dirty, requiring a complete cache clear at some
     * point.<p>
     *
     * Subclasses are free to delay cache clear until next getSystemTable().
     * However, subclasses may have to be aware of additional methods with
     * semantics similar to getSystemTable() and act accordingly (e.g.
     * clearing earlier than next invocation of getSystemTable()).
     */
    final void setDirty() {
        isDirty = true;
    }

    /**
     * Switches this table producer between producing empty (surrogate)
     * or contentful tables. <p>
     *
     * @param withContent if true, then produce contentful tables, else
     *        produce emtpy (surrogate) tables
     */
    final void setWithContent(boolean withContent) {
        this.withContent = withContent;
    }
}
