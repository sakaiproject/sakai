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

import org.hsqldb.lib.IntKeyHashMap;
import org.hsqldb.lib.IntValueHashMap;
import org.hsqldb.lib.HashMap;

/**
 * Defines the constants that are used to identify SQL types for HSQLDB JDBC
 * inteface type reporting. The actual type constant values are equivalent
 * to those defined in the latest java.sql.Types, where available,
 * or those defined by ansi/iso SQL 200n otherwise. A type sub-identifer
 * has been added to differentiate HSQLDB-specific type specializations.
 *
 * @author  boucherb@users
 * @version 1.7.2
 * @since 1.7.2
 */
public class Types {

    /**
     * Names of types.
     * Used for external, JDBC reporting
     * Used for library and user function arguments
     */
    public static final String DecimalClassName   = "java.math.BigDecimal";
    public static final String DateClassName      = "java.sql.Date";
    public static final String TimeClassName      = "java.sql.Time";
    public static final String TimestampClassName = "java.sql.Timestamp";

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>ARRAY</code>.
     *
     * @since JDK 1.2
     */
    public static final int ARRAY = 2003;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>BIGINT</code>.
     */
    public static final int BIGINT = -5;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>BINARY</code>.
     */
    public static final int BINARY = -2;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>BLOB</code>.
     *
     * @since JDK 1.2
     */
    public static final int BLOB = 2004;

    /**
     * The constant in the Java programming language, somtimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>BOOLEAN</code>.
     *
     * @since JDK 1.4
     */
    public static final int BOOLEAN = 16;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>CHAR</code>.
     */
    public static final int CHAR = 1;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>CLOB</code>
     *
     * @since JDK 1.2
     */
    public static final int CLOB = 2005;

    /**
     * The constant in the Java programming language, somtimes referred to
     * as a type code, that identifies the generic SQL type <code>DATALINK</code>.
     *
     * @since JDK 1.4
     */
    public static final int DATALINK = 70;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>DATE</code>.
     */
    public static final int DATE = 91;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>DECIMAL</code>.
     */
    public static final int DECIMAL = 3;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>DISTINCT</code>.
     *
     * @since JDK 1.2
     */
    public static final int DISTINCT = 2001;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>DOUBLE</code>.
     */
    public static final int DOUBLE = 8;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>FLOAT</code>.
     */
    public static final int FLOAT = 6;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>INTEGER</code>.
     */
    public static final int INTEGER = 4;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>JAVA_OBJECT</code>.
     *
     * @since JDK 1.2
     */
    public static final int JAVA_OBJECT = 2000;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>LONGVARBINARY</code>.
     */
    public static final int LONGVARBINARY = -4;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>LONGVARCHAR</code>.
     */
    public static final int LONGVARCHAR = -1;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>NULL</code>.
     */
    public static final int NULL = 0;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>NUMERIC</code>.
     */
    public static final int NUMERIC = 2;

    /**
     * The constant in the Java programming language that indicates
     * that the SQL type is database-specific and
     * gets mapped to a Java object that can be accessed via
     * the methods <code>getObject</code> and <code>setObject</code>.
     */
    public static final int OTHER = 1111;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>REAL</code>.
     */
    public static final int REAL = 7;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>REF</code>.
     *
     * @since JDK 1.2
     */
    public static final int REF = 2006;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>SMALLINT</code>.
     */
    public static final int SMALLINT = 5;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>STRUCT</code>.
     *
     * @since JDK 1.2
     */
    public static final int STRUCT = 2002;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>TIME</code>.
     */
    public static final int TIME = 92;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>TIMESTAMP</code>.
     */
    public static final int TIMESTAMP = 93;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>TINYINT</code>.
     */
    public static final int TINYINT = -6;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>VARBINARY</code>.
     */
    public static final int VARBINARY = -3;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>VARCHAR</code>.
     */
    public static final int VARCHAR = 12;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the recent SQL 200n SQL type
     * <code>XML</code>.
     *
     * @since SQL 200n
     */
    public static final int XML = 137;

    /**
     * The default HSQLODB type sub-identifier. This indicates that an
     * HSQLDB type with this sub-type, if supported, is the very closest
     * thing HSQLDB offerers to the JDBC/SQL200n type
     */
    public static final int TYPE_SUB_DEFAULT = 1;

    /**
     * The IGNORECASE type sub-identifier. This indicates that an HSQLDB type
     * with this sub-type, if supported,  is the closest thing HSQLDB offerers
     * to the JDBC/SQL200n type, except that case is ignored in comparisons
     */
    public static final int TYPE_SUB_IGNORECASE = TYPE_SUB_DEFAULT << 2;

    /**
     * Every (type,type-sub) combination known in the HSQLDB context.
     * Not every combination need be supported as a table or procedure
     * column type -- such determinations are handled in DITypeInfo.
     */
    static final int[][] ALL_TYPES = {
        {
            ARRAY, TYPE_SUB_DEFAULT
        }, {
            BIGINT, TYPE_SUB_DEFAULT
        }, {
            BINARY, TYPE_SUB_DEFAULT
        }, {
            BLOB, TYPE_SUB_DEFAULT
        }, {
            BOOLEAN, TYPE_SUB_DEFAULT
        }, {
            CHAR, TYPE_SUB_DEFAULT
        }, {
            CLOB, TYPE_SUB_DEFAULT
        }, {
            DATALINK, TYPE_SUB_DEFAULT
        }, {
            DATE, TYPE_SUB_DEFAULT
        }, {
            DECIMAL, TYPE_SUB_DEFAULT
        }, {
            DISTINCT, TYPE_SUB_DEFAULT
        }, {
            DOUBLE, TYPE_SUB_DEFAULT
        }, {
            FLOAT, TYPE_SUB_DEFAULT
        }, {
            INTEGER, TYPE_SUB_DEFAULT
        }, {
            JAVA_OBJECT, TYPE_SUB_DEFAULT
        }, {
            LONGVARBINARY, TYPE_SUB_DEFAULT
        }, {
            LONGVARCHAR, TYPE_SUB_DEFAULT
        }, {
            NULL, TYPE_SUB_DEFAULT
        }, {
            NUMERIC, TYPE_SUB_DEFAULT
        }, {
            OTHER, TYPE_SUB_DEFAULT
        }, {
            REAL, TYPE_SUB_DEFAULT
        }, {
            REF, TYPE_SUB_DEFAULT
        }, {
            SMALLINT, TYPE_SUB_DEFAULT
        }, {
            STRUCT, TYPE_SUB_DEFAULT
        }, {
            TIME, TYPE_SUB_DEFAULT
        }, {
            TIMESTAMP, TYPE_SUB_DEFAULT
        }, {
            TINYINT, TYPE_SUB_DEFAULT
        }, {
            VARBINARY, TYPE_SUB_DEFAULT
        }, {
            VARCHAR, TYPE_SUB_DEFAULT
        }, {
            VARCHAR, TYPE_SUB_IGNORECASE
        }, {
            XML, TYPE_SUB_DEFAULT
        }
    };
    /*
     SQL specifies predefined data types named by the following <key word>s:
     CHARACTER, CHARACTER VARYING, CHARACTER LARGE OBJECT, BINARY LARGE OBJECT,
     NUMERIC, DECIMAL, SMALLINT, INTEGER, BIGINT, FLOAT, REAL, DOUBLE PRECISION,
     BOOLEAN, DATE, TIME, TIMESTAMP, and INTERVAL.
     SQL 200n adds DATALINK in Part 9: Management of External Data (SQL/MED)
     and adds XML in Part 14: XML-Related Specifications (SQL/XML)
     */

    // CLI type list from Table 37
    static final int SQL_CHARACTER                 = 1;
    static final int SQL_CHAR                      = 1;
    static final int SQL_NUMERIC                   = 2;
    static final int SQL_DECIMAL                   = 3;
    static final int SQL_DEC                       = 3;
    static final int SQL_INTEGER                   = 4;
    static final int SQL_INT                       = 4;
    static final int SQL_SMALLINT                  = 5;
    static final int SQL_FLOAT                     = 6;
    static final int SQL_REAL                      = 7;
    static final int SQL_DOUBLE                    = 8;
    static final int SQL_CHARACTER_VARYING         = 12;
    static final int SQL_CHAR_VARYING              = 12;
    static final int SQL_VARCHAR                   = 12;
    static final int SQL_BOOLEAN                   = 16;
    static final int SQL_USER_DEFINED_TYPE         = 17;
    static final int SQL_ROW                       = 19;
    static final int SQL_REF                       = 20;
    static final int SQL_BIGINT                    = 25;
    static final int SQL_BINARY_LARGE_OBJECT       = 30;
    static final int SQL_BLOB                      = 30;
    static final int SQL_CHARACTER_LARGE_OBJECT    = 40;
    static final int SQL_CLOB                      = 40;
    static final int SQL_ARRAY                     = 50;     // not predefined
    static final int SQL_MULTISET                  = 55;     //
    static final int SQL_DATE                      = 91;
    static final int SQL_TIME                      = 92;
    static final int SQL_TIMESTAMP                 = 93;     //
    static final int SQL_TIME_WITH_TIME_ZONE       = 94;
    static final int SQL_TIMESTAMP_WITH_TIME_ZONE  = 95;     //
    static final int SQL_INTERVAL_YEAR             = 101;    //
    static final int SQL_INTERVAL_MONTH            = 102;
    static final int SQL_INTERVAL_DAY              = 103;
    static final int SQL_INTERVAL_HOUR             = 104;
    static final int SQL_INTERVAL_MINUTE           = 105;
    static final int SQL_INTERVAL_SECOND           = 106;
    static final int SQL_INTERVAL_YEAR_TO_MONTH    = 107;
    static final int SQL_INTERVAL_DAY_TO_HOUR      = 108;
    static final int SQL_INTERVAL_DAY_TO_MINUTE    = 109;
    static final int SQL_INTERVAL_DAY_TO_SECOND    = 110;
    static final int SQL_INTERVAL_HOUR_TO_MINUTE   = 111;
    static final int SQL_INTERVAL_HOUR_TO_SECOND   = 112;
    static final int SQL_INTERVAL_MINUTE_TO_SECOND = 113;

    // These values are not in table 37 of the SQL CLI 200n FCD, but some
    // are found in tables 6-9 and some are found in Annex A1:
    // c Header File SQLCLI.H and/or addendums in other documents,
    // such as:
    // SQL 200n Part 9: Management of External Data (SQL/MED) : DATALINK
    // SQL 200n Part 14: XML-Related Specifications (SQL/XML) : XML
    static final int SQL_BIT_VARYING      = 15;              // is in SQL99 but removed from 200n
    static final int SQL_DATALINK         = 70;
    static final int SQL_UDT              = 17;
    static final int SQL_UDT_LOCATOR      = 18;
    static final int SQL_BLOB_LOCATOR     = 31;
    static final int SQL_CLOB_LOCATOR     = 41;
    static final int SQL_ARRAY_LOCATOR    = 51;
    static final int SQL_MULTISET_LOCATOR = 56;
    static final int SQL_ALL_TYPES        = 0;
    static final int SQL_DATETIME         = 9;               // collective name
    static final int SQL_INTERVAL         = 10;              // collective name
    static final int SQL_XML              = 137;

    // SQL_UDT subcodes
    static final int SQL_DISTINCT    = 1;
    static final int SQL_SCTRUCTURED = 2;

    // non-standard type not in JDBC or SQL CLI
    public static final int VARCHAR_IGNORECASE = 100;

// lookup for types
// boucherb@users - access changed for metadata 1.7.2
    static IntValueHashMap typeAliases;
    static IntKeyHashMap   typeNames;
    static HashMap         javaTypeNames;

//  boucherb@users - We can't handle method invocations in
//                   Function.java or user-defined methods whose parameters
//                   number class is
//                   narrower than the corresponding internal
//                   wrapper
    private static org.hsqldb.lib.HashSet illegalParameterClasses;

    static {
        typeAliases = new IntValueHashMap(50);

        typeAliases.put("INTEGER", Types.INTEGER);
        typeAliases.put("INT", Types.INTEGER);
        typeAliases.put("int", Types.INTEGER);
        typeAliases.put("java.lang.Integer", Types.INTEGER);
        typeAliases.put("IDENTITY", Types.INTEGER);
        typeAliases.put("DOUBLE", Types.DOUBLE);
        typeAliases.put("double", Types.DOUBLE);
        typeAliases.put("java.lang.Double", Types.DOUBLE);
        typeAliases.put("FLOAT", Types.FLOAT);
        typeAliases.put("REAL", Types.REAL);
        typeAliases.put("VARCHAR", Types.VARCHAR);
        typeAliases.put("java.lang.String", Types.VARCHAR);
        typeAliases.put("CHAR", Types.CHAR);
        typeAliases.put("CHARACTER", Types.CHAR);
        typeAliases.put("LONGVARCHAR", Types.LONGVARCHAR);
        typeAliases.put("VARCHAR_IGNORECASE", VARCHAR_IGNORECASE);
        typeAliases.put("DATE", Types.DATE);
        typeAliases.put(DateClassName, Types.DATE);
        typeAliases.put("TIME", Types.TIME);
        typeAliases.put(TimeClassName, Types.TIME);
        typeAliases.put("TIMESTAMP", Types.TIMESTAMP);
        typeAliases.put(TimestampClassName, Types.TIMESTAMP);
        typeAliases.put("DATETIME", Types.TIMESTAMP);
        typeAliases.put("DECIMAL", Types.DECIMAL);
        typeAliases.put(DecimalClassName, Types.DECIMAL);
        typeAliases.put("NUMERIC", Types.NUMERIC);
        typeAliases.put("BIT", Types.BOOLEAN);
        typeAliases.put("BOOLEAN", Types.BOOLEAN);
        typeAliases.put("boolean", Types.BOOLEAN);
        typeAliases.put("java.lang.Boolean", Types.BOOLEAN);
        typeAliases.put("TINYINT", Types.TINYINT);
        typeAliases.put("byte", Types.TINYINT);
        typeAliases.put("java.lang.Byte", Types.TINYINT);
        typeAliases.put("SMALLINT", Types.SMALLINT);
        typeAliases.put("short", Types.SMALLINT);
        typeAliases.put("java.lang.Short", Types.SMALLINT);
        typeAliases.put("BIGINT", Types.BIGINT);
        typeAliases.put("long", Types.BIGINT);
        typeAliases.put("java.lang.Long", Types.BIGINT);
        typeAliases.put("BINARY", Types.BINARY);
        typeAliases.put("[B", Types.BINARY);
        typeAliases.put("VARBINARY", Types.VARBINARY);
        typeAliases.put("LONGVARBINARY", Types.LONGVARBINARY);
        typeAliases.put("OTHER", Types.OTHER);
        typeAliases.put("OBJECT", Types.OTHER);
        typeAliases.put("java.lang.Object", Types.OTHER);
        typeAliases.put("NULL", Types.NULL);
        typeAliases.put("void", Types.NULL);
        typeAliases.put("java.lang.Void", Types.NULL);

        //
        typeNames = new IntKeyHashMap();

        typeNames.put(Types.NULL, "NULL");
        typeNames.put(Types.INTEGER, "INTEGER");
        typeNames.put(Types.DOUBLE, "DOUBLE");
        typeNames.put(VARCHAR_IGNORECASE, "VARCHAR_IGNORECASE");
        typeNames.put(Types.VARCHAR, "VARCHAR");
        typeNames.put(Types.CHAR, "CHAR");
        typeNames.put(Types.LONGVARCHAR, "LONGVARCHAR");
        typeNames.put(Types.DATE, "DATE");
        typeNames.put(Types.TIME, "TIME");
        typeNames.put(Types.DECIMAL, "DECIMAL");
        typeNames.put(Types.BOOLEAN, "BOOLEAN");
        typeNames.put(Types.TINYINT, "TINYINT");
        typeNames.put(Types.SMALLINT, "SMALLINT");
        typeNames.put(Types.BIGINT, "BIGINT");
        typeNames.put(Types.REAL, "REAL");
        typeNames.put(Types.FLOAT, "FLOAT");
        typeNames.put(Types.NUMERIC, "NUMERIC");
        typeNames.put(Types.TIMESTAMP, "TIMESTAMP");
        typeNames.put(Types.BINARY, "BINARY");
        typeNames.put(Types.VARBINARY, "VARBINARY");
        typeNames.put(Types.LONGVARBINARY, "LONGVARBINARY");
        typeNames.put(Types.OTHER, "OBJECT");

        //
        illegalParameterClasses = new org.hsqldb.lib.HashSet();

        illegalParameterClasses.add(Byte.TYPE);
        illegalParameterClasses.add(Short.TYPE);
        illegalParameterClasses.add(Float.TYPE);
        illegalParameterClasses.add(Byte.class);
        illegalParameterClasses.add(Short.class);
        illegalParameterClasses.add(Float.class);

        //
        javaTypeNames = new HashMap();

        javaTypeNames.put(DateClassName, "java.sql.Date");
        javaTypeNames.put(TimeClassName, "java.sql.Time");
        javaTypeNames.put(TimestampClassName, "java.sql.Timestamp");
        javaTypeNames.put(DecimalClassName, "java.math.BigDecimal");
        javaTypeNames.put("byte", "java.lang.Integer");
        javaTypeNames.put("java.lang.Byte", "java.lang.Integer");
        javaTypeNames.put("short", "java.lang.Integer");
        javaTypeNames.put("java.lang.Short", "java.lang.Integer");
        javaTypeNames.put("int", "java.lang.Integer");
        javaTypeNames.put("java.lang.Integer", "java.lang.Integer");
        javaTypeNames.put("long", "java.lang.Long");
        javaTypeNames.put("java.lang.Long", "java.lang.Long");
        javaTypeNames.put("double", "java.lang.Double");
        javaTypeNames.put("java.lang.Double", "java.lang.Double");
        javaTypeNames.put("boolean", "java.lang.Boolean");
        javaTypeNames.put("java.lang.Boolean", "java.lang.Boolean");
        javaTypeNames.put("java.lang.String", "java.lang.String");
        javaTypeNames.put("void", "java.lang.Void");
        javaTypeNames.put("[B", "[B");
    }

    /**
     * Translates a type name returned from a method into the name of type
     * returned in a ResultSet
     */
    static String getFunctionReturnClassName(String methodReturnType) {

        String name = (String) javaTypeNames.get(methodReturnType);

        return name == null ? methodReturnType
                            : name;
    }

    /**
     * `
     *
     * @param type string
     * @return java.sql.Types int value
     * @throws  HsqlException
     */
    static int getTypeNr(String type) throws HsqlException {

        int i = typeAliases.get(type, Integer.MIN_VALUE);

        Trace.check(i != Integer.MIN_VALUE, Trace.WRONG_DATA_TYPE, type);

        return i;
    }

    /**
     * Returns SQL type string for a java.sql.Types int value
     */
    public static String getTypeString(int type) {
        return (String) typeNames.get(type);
    }

    /**
     * Returns SQL type string for a java.sql.Types int value
     */
    public static String getTypeString(int type, int precision, int scale) {

        String s = (String) typeNames.get(type);

        if (precision != 0 && acceptsPrecisionCreateParam(type)) {
            StringBuffer sb = new StringBuffer(s);

            sb.append(Token.T_OPENBRACKET);
            sb.append(precision);

            if (scale != 0 && acceptsScaleCreateParam(type)) {
                sb.append(Token.T_COMMA);
                sb.append(scale);
            }

            sb.append(Token.T_CLOSEBRACKET);

            return sb.toString();
        }

        return s;
    }

    /**
     * Retieves the type number corresponding to the class
     * of an IN, IN OUT or OUT parameter.  <p>
     *
     * This method extends getTypeNr to return OTHER for
     * primitive arrays, classes that directly implement
     * java.io.Serializable and non-primitive arrays whose
     * base component implements java.io.Serializable,
     * allowing, for instance, arguments and return types of
     * primitive arrays, Serializable objects and arrays,
     * of Serializable objects.  Direct primitive types
     * other than those mapping directly to the internal
     * wrapper form are not yet handled.  That is, HSQLDB
     * cannot yet properly deal with CALLs involving methods
     * with primitive byte, short, float or their
     * corresponding wrappers, due to the way internal
     * conversion works and lack of detection and narrowing
     * code in Function to allow this.  In other words,
     * passing in or retrieving any of the mentioned types
     * always causes conversion to a wider internal wrapper
     * which is genrally incompatible under reflective
     * invocation, resulting in an IllegalArgumentException.
     *
     * @param  c a Class instance
     * @return java.sql.Types int value
     * @throws  HsqlException
     */
    static int getParameterTypeNr(Class c) throws HsqlException {

        String name;
        int    type;

        if (c == null) {
            Trace.doAssert(false, "c is null");
        }

        if (Void.TYPE.equals(c)) {
            return Types.NULL;
        }

        if (illegalParameterClasses.contains(c)) {
            throw Trace.error(Trace.WRONG_DATA_TYPE,
                              Trace.UNSUPPORTED_PARAM_CLASS, c.getName());
        }

        name = c.getName();
        type = typeAliases.get(name, Integer.MIN_VALUE);

        if (type == Integer.MIN_VALUE) {

            // ensure all nested types are serializable
            // byte[] is already covered as BINARY in typeAliases
            if (c.isArray()) {
                while (c.isArray()) {
                    c = c.getComponentType();
                }

                if (c.isPrimitive()
                        || java.io.Serializable.class.isAssignableFrom(c)) {
                    type = OTHER;
                }
            } else if (java.io.Serializable.class.isAssignableFrom(c)) {
                type = OTHER;
            }
        }

        Trace.check(type != Integer.MIN_VALUE, Trace.WRONG_DATA_TYPE, name);

        return type;
    }

/*
    static boolean areSimilar(int t1, int t2) {

        if (t1 == t2) {
            return true;
        }

        if (isNumberType(t1)) {
            return isNumberType(t2);
        }

        if (isCharacterType(t1)) {
            return isCharacterType(t2);
        }

        if (isBinaryType(t1)) {
            return isBinaryType(t2);
        }

        return false;
    }

    static boolean haveSameInternalRepresentation(int t1, int t2) {

        if (t1 == t2) {
            return true;
        }

        if (isCharacterType(t1)) {
            return isCharacterType(t2);
        }

        if (isBinaryType(t1)) {
            return isBinaryType(t2);
        }

        switch (t1) {

            case TINYINT :
            case SMALLINT :
            case INTEGER : {
                switch (t2) {

                    case TINYINT :
                    case SMALLINT :
                    case INTEGER : {
                        return true;
                    }
                    default : {
                        return false;
                    }
                }
            }
            case FLOAT :
            case REAL :
            case DOUBLE : {
                switch (t2) {

                    case FLOAT :
                    case REAL :
                    case DOUBLE : {
                        return true;
                    }
                    default : {
                        return false;
                    }
                }
            }
            case DECIMAL :
            case NUMERIC : {
                switch (t2) {

                    case DECIMAL :
                    case NUMERIC : {
                        return true;
                    }
                    default : {
                        return false;
                    }
                }
            }
            default : {
                return false;
            }
        }
    }

    static boolean isExactNumberType(int type) {

        switch (type) {

            case BIGINT :
            case DECIMAL :
            case INTEGER :
            case NUMERIC :
            case SMALLINT :
            case TINYINT :
                return true;

            default :
                return false;
        }
    }

    static boolean isStrictlyIntegralNumberType(int type) {

        switch (type) {

            case BIGINT :
            case INTEGER :
            case SMALLINT :
            case TINYINT :
                return true;

            default :
                return false;
        }
    }

    static boolean isApproximateNumberType(int type) {

        switch (type) {

            case DOUBLE :
            case FLOAT :
            case REAL :
                return true;

            default :
                return false;
        }
    }

    public static boolean isBinaryType(int type) {

        switch (type) {

            case BINARY :

            case BLOB :
            case LONGVARBINARY :
            case VARBINARY :
                return true;

            default :
                return false;
        }
    }

*/
    static boolean isDatetimeType(int type) {

        switch (type) {

            case DATE :
            case TIME :
            case TIMESTAMP :
                return true;

            default :
                return false;
        }
    }

    /**
     * Types that accept precition params in column definition or casts.
     * We ignore the parameter in many cases but accept it for compatibility
     * with other engines. CHAR, VARCHAR and VARCHAR_IGNORECASE params
     * are used when the sql.enforce_strict_types is true.
     *
     */
    public static boolean acceptsPrecisionCreateParam(int type) {

        switch (type) {

            case BINARY :
            case BLOB :
            case CHAR :
            case CLOB :

            //            case LONGVARBINARY :
            //            case LONGVARCHAR :
            case VARBINARY :
            case VARCHAR :
            case VARCHAR_IGNORECASE :
            case DECIMAL :
            case NUMERIC :
            case FLOAT :
            case TIMESTAMP :
            case TIME :
                return true;

            default :
                return false;
        }
    }

    public static int numericPrecisionCreateParamRadix(int type) {

        switch (type) {

            case Types.DECIMAL :
            case Types.NUMERIC :
                return 10;

            case FLOAT :
                return 2;

            default :

                // to mean NOT APPLICABLE (i.e. NULL)
                return 0;
        }
    }

    public static boolean acceptsScaleCreateParam(int type) {

        switch (type) {

            case Types.DECIMAL :
            case Types.NUMERIC :
                return true;

            default :
                return false;
        }
    }

    public static boolean isNumberType(int type) {

        switch (type) {

            case BIGINT :
            case DECIMAL :
            case DOUBLE :
            case FLOAT :
            case INTEGER :
            case NUMERIC :
            case REAL :
            case SMALLINT :
            case TINYINT :
                return true;

            default :
                return false;
        }
    }

    public static boolean isCharacterType(int type) {

        switch (type) {

            case CHAR :
            case CLOB :
            case LONGVARCHAR :
            case VARCHAR :
            case VARCHAR_IGNORECASE :
                return true;

            default :
                return false;
        }
    }

    public static String getTypeName(int type) {

        switch (type) {

            case Types.ARRAY :
                return "ARRAY";

            case Types.BIGINT :
                return "BIGINT";

            case Types.BINARY :
                return "BINARY";

            case Types.BLOB :
                return "BLOB";

            case Types.BOOLEAN :
                return "BOOLEAN";

            case Types.CHAR :
                return "CHAR";

            case Types.CLOB :
                return "CLOB";

            case Types.DATALINK :
                return "DATALINK";

            case Types.DATE :
                return "DATE";

            case Types.DECIMAL :
                return "DECIMAL";

            case Types.DISTINCT :
                return "DISTINCT";

            case Types.DOUBLE :
                return "DOUBLE";

            case Types.FLOAT :
                return "FLOAT";

            case Types.INTEGER :
                return "INTEGER";

            case Types.JAVA_OBJECT :
                return "JAVA_OBJECT";

            case Types.LONGVARBINARY :
                return "LONGVARBINARY";

            case Types.LONGVARCHAR :
                return "LONGVARCHAR";

            case Types.NULL :
                return "NULL";

            case Types.NUMERIC :
                return "NUMERIC";

            case Types.OTHER :
                return "OTHER";

            case Types.REAL :
                return "REAL";

            case Types.REF :
                return "REF";

            case Types.SMALLINT :
                return "SMALLINT";

            case Types.STRUCT :
                return "STUCT";

            case Types.TIME :
                return "TIME";

            case Types.TIMESTAMP :
                return "TIMESTAMP";

            case Types.TINYINT :
                return "TINYINT";

            case Types.VARBINARY :
                return "VARBINARY";

            case Types.VARCHAR :
                return "VARCHAR";

            case Types.VARCHAR_IGNORECASE :
                return "VARCHAR_IGNORECASE";

            case Types.XML :
                return "XML";

            default :
                return null;
        }
    }

    /**
     * A reasonable/customizable number to avoid the shortcomings/defects
     * associated with doing a dynamic scan of results to determine
     * the value.  In practice, it turns out that single query yielding
     * widely varying values for display size of CHAR and VARCHAR columns
     * on repeated execution results in patently poor usability, as some fairly
     * high-profile, otherwise "enterprise-quality" RAD tools depend on
     * on the first value returned to lay out forms and limit the size of
     * single line edit controls, set corresponding local datastore storage
     * sizes, etc. In practice, It also turns out that many tools (due to
     * the original lack of PreparedStatement.getMetaData() in JDK 1.1) emulate
     * a SQL_DESCRIBE by executing a query hopefully guaranteed to return no
     * or very few rows for example: select ... from ... where 1=0.
     * Using the dynamic scan of 1.7.2 RC5 and previous, therefore, the
     * minimum display size value (1) was often being generated during
     * a tool's describe phase.  Upon subsequent "real" retrievals, some
     * tools complain that CHAR and VARCHAR result values exceeded the
     * originally reported display size and refused to fetch further values.
     */
    public static final int MAX_CHAR_OR_VARCHAR_DISPLAY_SIZE =
        MAX_CHAR_OR_VARCHAR_DISPLAY_SIZE();

    // So that the variable can be both public static final and
    // customizable through system properties if required.
    //
    // 32766 (0x7ffe) seems to be a magic number over which several
    // rather high-profile RAD tools start to have problems
    // regarding layout and allocation stress.  It is gently
    // recommended that LONGVARCHAR be used for larger values in RAD
    // tool layout & presentation use cases until such time as we provide
    // true BLOB support (at which point, LONGVARCHAR will most likely become
    // an alias for CLOB).
    //
    // Most GUI tools seem to handle LONGVARCHAR gracefully by:
    //
    // 1.) refusing to directly display such columns in graphical query results
    // 2.) providing other means to retrieve and display such values
    private static int MAX_CHAR_OR_VARCHAR_DISPLAY_SIZE() {

        try {
            return Integer.getInteger(
                "hsqldb.max_char_or_varchar_display_size", 32766).intValue();
        } catch (SecurityException e) {
            return 32766;
        }
    }

    public static int getMaxDisplaySize(int type) {

        switch (type) {

            case Types.BINARY :
            case Types.LONGVARBINARY :
            case Types.LONGVARCHAR :
            case Types.OTHER :
            case Types.VARBINARY :
            case Types.XML :
                return Integer.MAX_VALUE;    // max string length

            case Types.CHAR :
            case Types.VARCHAR :
                return MAX_CHAR_OR_VARCHAR_DISPLAY_SIZE;

            case Types.BIGINT :              // PowerBuilder barfs, wants 19

                // ...not our problem, tho,
                // according to JDBC
                return 20;                   // precision + "-".length();

            case Types.BOOLEAN :
                return 5;                    // Math.max("true".length(),"false".length);

            case Types.DATALINK :
                return 20004;                // same as precision

            case Types.DECIMAL :
            case Types.NUMERIC :
                return 646456995;            // precision + "-.".length()

            case Types.DATE :
                return 10;                   // same as precision

            case Types.INTEGER :
                return 11;                   // precision + "-".length();

            case Types.FLOAT :
            case Types.REAL :
            case Types.DOUBLE :
                return 23;                   // String.valueOf(-Double.MAX_VALUE).length();

            case Types.TIME :
                return 8;                    // same as precision

            case Types.SMALLINT :
                return 6;                    // precision + "-".length();

            case Types.TIMESTAMP :
                return 29;                   // same as precision

            case Types.TINYINT :
                return 4;                    // precision + "-".length();

            default :
                return 0;                    // unknown
        }
    }

    public static boolean isSearchable(int type) {

        switch (type) {

            case Types.ARRAY :
            case Types.BLOB :
            case Types.CLOB :
            case Types.JAVA_OBJECT :
            case Types.STRUCT :
            case Types.OTHER :
                return false;

            default :
                return true;
        }
    }

    public static Boolean isCaseSensitive(int type) {

        switch (type) {

            case Types.ARRAY :
            case Types.BLOB :
            case Types.CLOB :
            case Types.DISTINCT :
            case Types.JAVA_OBJECT :
            case Types.NULL :
            case Types.REF :
            case Types.STRUCT :
                return null;

            case Types.CHAR :
            case Types.DATALINK :
            case Types.LONGVARCHAR :
            case Types.OTHER :
            case Types.XML :
                return Boolean.TRUE;

            case Types.VARCHAR_IGNORECASE :
            default :
                return Boolean.FALSE;
        }
    }

    public static Boolean isUnsignedAttribute(int type) {

        switch (type) {

            case Types.BIGINT :
            case Types.DECIMAL :
            case Types.DOUBLE :
            case Types.FLOAT :
            case Types.INTEGER :
            case Types.NUMERIC :
            case Types.REAL :
            case Types.SMALLINT :
            case Types.TINYINT :
                return Boolean.FALSE;

            default :
                return null;
        }
    }

    public static int getPrecision(int type) {

        switch (type) {

            case Types.BINARY :
            case Types.CHAR :
            case Types.LONGVARBINARY :
            case Types.LONGVARCHAR :
            case Types.OTHER :
            case Types.VARBINARY :
            case Types.VARCHAR :
            case Types.XML :
                return Integer.MAX_VALUE;

            case Types.BIGINT :
                return 19;

            case Types.BOOLEAN :
                return 1;

            case Types.DATALINK :

                // from SQL CLI spec.  TODO:  Interpretation?
                return 20004;

            case Types.DECIMAL :
            case Types.NUMERIC :

// Integer.MAX_VALUE bit 2's complement number:
// (Integer.MAX_VALUE-1) / ((ln(10)/ln(2)) bits per decimal digit)
// See:  java.math.BigInteger
// - the other alternative is that we could report the numprecradix as 2 and
// report Integer.MAX_VALUE here
                return 646456993;

            case Types.DATE :
            case Types.INTEGER :
                return 10;

            case Types.FLOAT :
            case Types.REAL :
            case Types.DOUBLE :
                return 17;

            case Types.TIME :
                return 8;

            case Types.SMALLINT :
                return 5;

            case Types.TIMESTAMP :
                return 29;

            case Types.TINYINT :
                return 3;

            default :
                return 0;
        }
    }

    public static String getColStClsName(int type) {

        switch (type) {

            case Types.BIGINT :
                return "java.lang.Long";

            case Types.BINARY :
            case Types.LONGVARBINARY :
            case Types.VARBINARY :

                // but wrapped by org.hsqldb.Binary
                return "[B";

            case Types.OTHER :

                // but wrapped by org.hsqldb.JavaObject
                return "java.lang.Object";

            case Types.BOOLEAN :
                return "java.lang.Boolean";

            case Types.CHAR :
            case Types.LONGVARCHAR :
            case Types.VARCHAR :
            case Types.XML :    //?
                return "java.lang.String";

            case Types.DATALINK :
                return "java.net.URL";

            case Types.DATE :
                return DateClassName;

            case Types.DECIMAL :
            case Types.NUMERIC :
                return DecimalClassName;

            case Types.DOUBLE :
            case Types.FLOAT :
            case Types.REAL :
                return "java.lang.Double";

            case Types.INTEGER :
            case Types.SMALLINT :
            case Types.TINYINT :
                return "java.lang.Integer";

            case Types.TIME :
                return TimeClassName;

            case Types.TIMESTAMP :
                return TimestampClassName;

            default :
                return null;
        }
    }
}
