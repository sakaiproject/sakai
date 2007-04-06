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

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.lib.StringConverter;
import org.hsqldb.store.ValuePool;
import org.hsqldb.types.Binary;
import org.hsqldb.types.JavaObject;
import org.hsqldb.lib.java.JavaSystem;

// fredt@users 20020130 - patch 491987 by jimbag@users
// fredt@users 20020320 - doc 1.7.0 - update
// fredt@users 20020401 - patch 442993 by fredt - arithmetic expressions
// to allow mixed type arithmetic expressions beginning with a narrower type
// changes applied to several lines of code and not marked separately
// consists of changes to arithmatic functions to allow promotion of
// java.lang.Number values and new functions to choose type for promotion
// fredt@users 20020401 - patch 455757 by galena@users (Michiel de Roo)
// interpretation of TINYINT as Byte instead of Short
// fredt@users 20020130 - patch 491987 by jimbag@users
// support for sql standard char and varchar. size is maintained as
// defined in the DDL and trimming and padding takes place accordingly
// modified by fredt - trimming and padding are turned off by default but
// can be applied accross the database by defining sql.enforce_size=true in
// database.properties file
// fredt@users 20020215 - patch 1.7.0 by fredt - quoted identifiers
// applied to different parts to support the sql standard for
// naming of columns and tables (use of quoted identifiers as names)
// fredt@users 20020328 - patch 1.7.0 by fredt - change REAL to Double
// fredt@users 20020402 - patch 1.7.0 by fredt - type conversions
// frequently used type conversions are done without creating temporary
// Strings to reduce execution time and garbage collection
// fredt@users 20021013 - patch 1.7.1 by fredt - type conversions
// scripting of Double.Nan and infinity values
// fredt@users 20030715 - patch 1.7.2 - type narrowing for numeric values
// fredt@users - patch 1.8.0 - enforcement of precision and scale

/**
 *  Implementation of SQL table columns as defined in DDL statements with
 *  static methods to process their values.<p>
 *
 *  Enhanced type checking and conversion by fredt@users
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @author fredt@users
 * @version    1.8.0
 * @since Hypersonic SQL
 */
public class Column {

// --------------------------------------------------
    // DDL name, size, scale, null, identity and default values
    // most variables are final but not declared so because of a bug in
    // JDK 1.1.8 compiler
    public HsqlName         columnName;
    private int             colType;
    private int             colSize;
    private int             colScale;
    private boolean         isNullable;
    private boolean         isIdentity;
    private boolean         isPrimaryKey;
    private Expression      defaultExpression;
    long                    identityStart;
    long                    identityIncrement;
    static final BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
    static final BigInteger MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);
    static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);
    static final BigInteger MIN_INT = BigInteger.valueOf(Integer.MIN_VALUE);
    static final BigDecimal BIG_DECIMAL_0 = new BigDecimal(0.0);
    static final BigDecimal BIG_DECIMAL_1 = new BigDecimal(1.0);

    /**
     *  Creates a column defined in DDL statement.
     *
     * @param  name
     * @param  nullable
     * @param  type
     * @param  size
     * @param  scale
     * @param  identity
     * @param  startvalue
     * @param  increment
     * @param  primarykey
     * @param  defstring
     */
    Column(HsqlName name, boolean nullable, int type, int size, int scale,
            boolean primarykey,
            Expression defexpression) throws HsqlException {

        columnName        = name;
        isNullable        = nullable;
        colType           = type;
        colSize           = size;
        colScale          = scale;
        isPrimaryKey      = primarykey;
        defaultExpression = defexpression;
    }

    void setIdentity(boolean identity, long startvalue,
                     long increment) throws HsqlException {

        isIdentity        = identity;
        identityStart     = startvalue;
        identityIncrement = increment;

        if (isIdentity) {
            if (colType == Types.INTEGER) {
                if (identityStart > Integer.MAX_VALUE
                        || identityIncrement > Integer.MAX_VALUE) {
                    throw Trace.error(Trace.NUMERIC_VALUE_OUT_OF_RANGE,
                                      columnName.statementName);
                }
            }
        }
    }

    private Column() {}

    /**
     * Used for primary key changes.
     */
    Column duplicate(boolean withIdentity) throws HsqlException {

        Column newCol = new Column();

        newCol.columnName        = columnName;
        newCol.isNullable        = isNullable;
        newCol.colType           = colType;
        newCol.colSize           = colSize;
        newCol.colScale          = colScale;
        newCol.defaultExpression = defaultExpression;

        if (withIdentity) {
            newCol.setIdentity(isIdentity, identityStart, identityIncrement);
        }

        return newCol;
    }

    void setType(Column other) {

        isNullable = other.isNullable;
        colType    = other.colType;
        colSize    = other.colSize;
        colScale   = other.colScale;
    }

    /**
     *  Is this the identity column in the table.
     *
     * @return boolean
     */
    boolean isIdentity() {
        return isIdentity;
    }

    /**
     *  Is column nullable.
     *
     * @return boolean
     */
    boolean isNullable() {
        return isNullable;
    }

    /**
     *  Set nullable.
     *
     */
    void setNullable(boolean value) {
        isNullable = value;
    }

    /**
     *  Is this single column primary key of the table.
     *
     * @return boolean
     */
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    /**
     *  Set primary key.
     *
     */
    void setPrimaryKey(boolean value) {
        isPrimaryKey = value;
    }

    /**
     *  Returns default value in the session context.
     */
    Object getDefaultValue(Session session) throws HsqlException {

        return defaultExpression == null ? null
                                         : defaultExpression.getValue(session,
                                         colType);
    }

    /**
     *  Returns DDL for default value.
     */
    String getDefaultDDL() {

        String ddl = null;

        try {
            ddl = defaultExpression == null ? null
                                            : defaultExpression.getDDL();
        } catch (HsqlException e) {}

        return ddl;
    }

    /**
     *  Returns default expression for the column.
     */
    Expression getDefaultExpression() {
        return defaultExpression;
    }

    void setDefaultExpression(Expression expr) {
        defaultExpression = expr;
    }

    /**
     *  Type of the column.
     *
     * @return java.sql.Types int value for the column
     */
    int getType() {
        return colType;
    }

    int getDIType() {
        return colType == Types.VARCHAR_IGNORECASE ? Types.VARCHAR
                                                   : colType;
    }

    int getDITypeSub() {

        if (colType == Types.VARCHAR_IGNORECASE) {
            return Types.TYPE_SUB_IGNORECASE;
        }

        return Types.TYPE_SUB_DEFAULT;
    }

    /**
     *  Size of the column in DDL (0 if not defined).
     *
     * @return DDL size of column
     */
    int getSize() {
        return colSize;
    }

    /**
     *  Scale of the column in DDL (0 if not defined).
     *
     * @return DDL scale of column
     */
    int getScale() {
        return colScale;
    }

    /**
     *  Add two object of a given type
     *
     * @param  a
     * @param  b
     * @param  type
     * @return result
     * @throws  HsqlException
     */
    static Object add(Object a, Object b, int type) throws HsqlException {

        if (a == null || b == null) {
            return null;
        }

        switch (type) {

            case Types.NULL :
                return null;

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE : {
                double ad = ((Number) a).doubleValue();
                double bd = ((Number) b).doubleValue();

                return ValuePool.getDouble(Double.doubleToLongBits(ad + bd));

//                return new Double(ad + bd);
            }
            case Types.VARCHAR :
            case Types.CHAR :
            case Types.LONGVARCHAR :
            case Types.VARCHAR_IGNORECASE :
                return (String) a + (String) b;

            case Types.NUMERIC :
            case Types.DECIMAL : {
                BigDecimal abd = (BigDecimal) a;
                BigDecimal bbd = (BigDecimal) b;

                return abd.add(bbd);
            }
            case Types.TINYINT :
            case Types.SMALLINT :
            case Types.INTEGER : {
                int ai = ((Number) a).intValue();
                int bi = ((Number) b).intValue();

                return ValuePool.getInt(ai + bi);
            }
            case Types.BIGINT : {
                long longa = ((Number) a).longValue();
                long longb = ((Number) b).longValue();

                return ValuePool.getLong(longa + longb);
            }
            default :
                throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,
                                  Types.getTypeString(type));
        }
    }

    /**
     *  Concat two objects by turning them into strings first.
     *
     * @param  a
     * @param  b
     * @return result
     * @throws  HsqlException
     */
    static Object concat(Object a, Object b) throws HsqlException {

        if (a == null || b == null) {
            return null;
        }

        return a.toString() + b.toString();
    }

    /**
     *  Negate a numeric object.
     *
     * @param  a
     * @param  type
     * @return result
     * @throws  HsqlException
     */
    static Object negate(Object a, int type) throws HsqlException {

        if (a == null) {
            return null;
        }

        switch (type) {

            case Types.NULL :
                return null;

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE : {
                double ad = -((Number) a).doubleValue();

                return ValuePool.getDouble(Double.doubleToLongBits(ad));
            }
            case Types.NUMERIC :
            case Types.DECIMAL :
                return ((BigDecimal) a).negate();

            case Types.TINYINT :
            case Types.SMALLINT :
            case Types.INTEGER :
                return ValuePool.getInt(-((Number) a).intValue());

            case Types.BIGINT :
                return ValuePool.getLong(-((Number) a).longValue());

            default :
                throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,
                                  Types.getTypeString(type));
        }
    }

    /**
     *  Multiply two numeric objects.
     *
     * @param  a
     * @param  b
     * @param  type
     * @return result
     * @throws  HsqlException
     */
    static Object multiply(Object a, Object b,
                           int type) throws HsqlException {

        if (a == null || b == null) {
            return null;
        }

// fredt@users - type conversion - may need to apply to other arithmetic operations too
        if (!(a instanceof Number && b instanceof Number)) {
            a = Column.convertObject(a, type);
            b = Column.convertObject(b, type);
        }

        switch (type) {

            case Types.NULL :
                return null;

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE : {
                double ad = ((Number) a).doubleValue();
                double bd = ((Number) b).doubleValue();

                return ValuePool.getDouble(Double.doubleToLongBits(ad * bd));
            }
            case Types.NUMERIC :
            case Types.DECIMAL : {
                BigDecimal abd = (BigDecimal) a;
                BigDecimal bbd = (BigDecimal) b;

                return abd.multiply(bbd);
            }
            case Types.TINYINT :
            case Types.SMALLINT :
            case Types.INTEGER : {
                int ai = ((Number) a).intValue();
                int bi = ((Number) b).intValue();

                return ValuePool.getInt(ai * bi);
            }
            case Types.BIGINT : {
                long longa = ((Number) a).longValue();
                long longb = ((Number) b).longValue();

                return ValuePool.getLong(longa * longb);
            }
            default :
                throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,
                                  Types.getTypeString(type));
        }
    }

    /**
     *  Divide numeric object a by object b.
     *
     * @param  a
     * @param  b
     * @param  type
     * @return result
     * @throws  HsqlException
     */
    static Object divide(Object a, Object b, int type) throws HsqlException {

        if (a == null || b == null) {
            return null;
        }

        switch (type) {

            case Types.NULL :
                return null;

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE : {
                double ad = ((Number) a).doubleValue();
                double bd = ((Number) b).doubleValue();

                return ValuePool.getDouble(Double.doubleToLongBits(ad / bd));
            }
            case Types.NUMERIC :
            case Types.DECIMAL : {
                BigDecimal abd   = (BigDecimal) a;
                BigDecimal bbd   = (BigDecimal) b;
                int        scale = abd.scale() > bbd.scale() ? abd.scale()
                                                             : bbd.scale();

                return (bbd.signum() == 0) ? null
                                           : abd.divide(bbd, scale,
                                           BigDecimal.ROUND_HALF_DOWN);
            }
            case Types.TINYINT :
            case Types.SMALLINT :
            case Types.INTEGER : {
                int ai = ((Number) a).intValue();
                int bi = ((Number) b).intValue();

                if (bi == 0) {
                    throw Trace.error(Trace.DIVISION_BY_ZERO);
                }

                return ValuePool.getInt(ai / bi);
            }
            case Types.BIGINT : {
                long longa = ((Number) a).longValue();
                long longb = ((Number) b).longValue();

                return (longb == 0) ? null
                                    : ValuePool.getLong(longa / longb);
            }
            default :
                throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,
                                  Types.getTypeString(type));
        }
    }

    /**
     *  Subtract numeric object b from object a.
     *
     * @param  a
     * @param  b
     * @param  type
     * @return result
     * @throws  HsqlException
     */
    static Object subtract(Object a, Object b,
                           int type) throws HsqlException {

        if (a == null || b == null) {
            return null;
        }

        switch (type) {

            case Types.NULL :
                return null;

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE : {
                double ad = ((Number) a).doubleValue();
                double bd = ((Number) b).doubleValue();

                return ValuePool.getDouble(Double.doubleToLongBits(ad - bd));
            }
            case Types.NUMERIC :
            case Types.DECIMAL : {
                BigDecimal abd = (BigDecimal) a;
                BigDecimal bbd = (BigDecimal) b;

                return abd.subtract(bbd);
            }
            case Types.TINYINT :
            case Types.SMALLINT :
            case Types.INTEGER : {
                int ai = ((Number) a).intValue();
                int bi = ((Number) b).intValue();

                return ValuePool.getInt(ai - bi);
            }
            case Types.BIGINT : {
                long longa = ((Number) a).longValue();
                long longb = ((Number) b).longValue();

                return ValuePool.getLong(longa - longb);
            }
            default :
                throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,
                                  Types.getTypeString(type));
        }
    }

// boucherb@users 2003-09-25
// TODO:  Maybe use //#ifdef tag or reflective static method attribute
// instantiation to take advantage of String.compareToIgnoreCase when
// available (JDK 1.2 and greater) during ANT build. That or perhaps
// consider using either local character-wise comparison or first converting
// to lower case and then to upper case. Sun states that the JDK 1.2 introduced
// String.compareToIngnorCase() comparison involves calling
// Character.toLowerCase(Character.toUpperCase()) on compared characters,
// to correctly handle some caveats concering using only the one operation or
// the other outside the ascii character range.
// fredt@users 20020130 - patch 418022 by deforest@users
// use of rtrim() to mimic SQL92 behaviour

    /**
     *  Compare a with b and return int value as result.
     *
     * @param  a instance of Java wrapper, depending on type, but always same for a & b (can be null)
     * @param  b instance of Java wrapper, depending on type, but always same for a & b (can be null)
     * @param  type one of the java.sql.Types
     * @return result 1 if a>b, 0 if a=b, -1 if b>a
     * @throws  HsqlException
     */
    static int compare(Collation collation, Object a, Object b, int type) {

        int i = 0;

        if (a == b) {
            return 0;
        }

        // Current null handling here: null==null and smaller any value
        // Note, standard SQL null handling is handled by Expression.test() calling testNull() instead of this!
        // Attention, this is also used for grouping ('null' is one group)
        if (a == null) {
            return -1;
        }

        if (b == null) {
            return 1;
        }

        switch (type) {

            case Types.NULL :
                return 0;

            case Types.VARCHAR :
            case Types.LONGVARCHAR :
                return collation.compare((String) a, (String) b);

            case Types.CHAR :
                return collation.compare(Library.rtrim((String) a),
                                         Library.rtrim((String) b));

            case Types.VARCHAR_IGNORECASE :
                return collation.compareIgnoreCase(((String) a),
                                                   ((String) b));

            case Types.TINYINT :
            case Types.SMALLINT :
            case Types.INTEGER : {
                int ai = ((Number) a).intValue();
                int bi = ((Number) b).intValue();

                return (ai > bi) ? 1
                                 : (bi > ai ? -1
                                            : 0);
            }
            case Types.BIGINT : {
                long longa = ((Number) a).longValue();
                long longb = ((Number) b).longValue();

                return (longa > longb) ? 1
                                       : (longb > longa ? -1
                                                        : 0);
            }
            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE : {
                double ad = ((Number) a).doubleValue();
                double bd = ((Number) b).doubleValue();

                return (ad > bd) ? 1
                                 : (bd > ad ? -1
                                            : 0);
            }
            case Types.NUMERIC :
            case Types.DECIMAL :
                i = ((BigDecimal) a).compareTo((BigDecimal) b);
                break;

            case Types.DATE :
                return HsqlDateTime.compare((Date) a, (Date) b);

            case Types.TIME :
                return HsqlDateTime.compare((Time) a, (Time) b);

            case Types.TIMESTAMP :
                return HsqlDateTime.compare((Timestamp) a, (Timestamp) b);

            case Types.BOOLEAN : {
                boolean boola = ((Boolean) a).booleanValue();
                boolean boolb = ((Boolean) b).booleanValue();

                return (boola == boolb) ? 0
                                        : (boolb ? -1
                                                 : 1);
            }
            case Types.BINARY :
            case Types.VARBINARY :
            case Types.LONGVARBINARY :
                if (a instanceof Binary && b instanceof Binary) {
                    i = compareTo(((Binary) a).getBytes(),
                                  ((Binary) b).getBytes());
                }
                break;

            case Types.OTHER :
                return 0;
        }

        return (i == 0) ? 0
                        : (i < 0 ? -1
                                 : 1);
    }

    /**
     *  Convert an object into a Java object that represents its SQL type.<p>
     *  All internal type conversion operations start with
     *  this method. If a direct conversion doesn't take place, the object
     *  is converted into a string first and an attempt is made to convert
     *  the string into the target type.<br>
     *
     *  One objective of this mehod is to ensure the Object can be converted
     *  to the given SQL type. For example, a very large BIGINT
     *  value cannot be narrowed down to an INTEGER or SMALLINT.<br>
     *
     *  Type conversion performed by this method has gradually evolved in 1.7.2
     *  to allow narrowing of numeric types in all cases according to the SQL
     *  standard.<br>
     *
     *  Another new objective is to normalize DATETIME values.<br>
     *
     *  Objects set via JDBC PreparedStatement use this method to convert
     *  the data to the Java type that is required for custom serialization
     *  by the engine. <br>
     *
     * @param  o
     * @param  type
     * @return result
     * @throws  HsqlException
     */
    public static Object convertObject(Object o,
                                       int type) throws HsqlException {

        try {
            if (o == null) {
                return null;
            }

            switch (type) {

                case Types.NULL :
                    return null;

                case Types.TINYINT :
                    if (o instanceof java.lang.String) {
                        o = Library.trim((String) o, " ", true, true);

                        int val = Integer.parseInt((String) o);

                        o = ValuePool.getInt(val);
                    }

                    if (o instanceof java.lang.Integer) {
                        int temp = ((Number) o).intValue();

                        if (Byte.MAX_VALUE < temp || temp < Byte.MIN_VALUE) {
                            throw Trace.error(
                                Trace.NUMERIC_VALUE_OUT_OF_RANGE);
                        }

                        return o;
                    }

                    if (o instanceof java.lang.Long) {
                        long temp = ((Number) o).longValue();

                        if (Byte.MAX_VALUE < temp || temp < Byte.MIN_VALUE) {
                            throw Trace.error(
                                Trace.NUMERIC_VALUE_OUT_OF_RANGE);
                        }

                        return ValuePool.getInt(((Number) o).intValue());
                    }

                    // fredt@users - direct conversion to optimise JDBC setObject(Byte)
                    if (o instanceof java.lang.Byte) {
                        return ValuePool.getInt(((Number) o).intValue());
                    }

                    // fredt@users - returns to this method for range checking
                    if (o instanceof java.lang.Number) {
                        return convertObject(convertToInt(o), type);
                    }

                    if (o instanceof java.lang.Boolean) {
                        return ((Boolean) o).booleanValue()
                               ? ValuePool.getInt(1)
                               : ValuePool.getInt(0);
                    }
                    break;

                case Types.SMALLINT :
                    if (o instanceof java.lang.String) {
                        o = Library.trim((String) o, " ", true, true);

                        int val = Integer.parseInt((String) o);

                        o = ValuePool.getInt(val);
                    }

                    if (o instanceof java.lang.Integer) {
                        int temp = ((Number) o).intValue();

                        if (Short.MAX_VALUE < temp
                                || temp < Short.MIN_VALUE) {
                            throw Trace.error(
                                Trace.NUMERIC_VALUE_OUT_OF_RANGE);
                        }

                        return o;
                    }

                    if (o instanceof java.lang.Long) {
                        long temp = ((Number) o).longValue();

                        if (Short.MAX_VALUE < temp
                                || temp < Short.MIN_VALUE) {
                            throw Trace.error(
                                Trace.NUMERIC_VALUE_OUT_OF_RANGE);
                        }

                        return ValuePool.getInt(((Number) o).intValue());
                    }

                    // fredt@users - direct conversion for JDBC setObject(Short), etc.
                    if (o instanceof Byte || o instanceof Short) {
                        return ValuePool.getInt(((Number) o).intValue());
                    }

                    // fredt@users - returns to this method for range checking
                    if (o instanceof Number) {
                        return convertObject(convertToInt(o), type);
                    }

                    if (o instanceof java.lang.Boolean) {
                        return ((Boolean) o).booleanValue()
                               ? ValuePool.getInt(1)
                               : ValuePool.getInt(0);
                    }
                    break;

                case Types.INTEGER :
                    if (o instanceof java.lang.Integer) {
                        return o;
                    }

                    if (o instanceof java.lang.String) {
                        o = Library.trim((String) o, " ", true, true);

                        int val = Integer.parseInt((String) o);

                        return ValuePool.getInt(val);
                    }

                    if (o instanceof java.lang.Long) {
                        long temp = ((Number) o).longValue();

                        if (Integer.MAX_VALUE < temp
                                || temp < Integer.MIN_VALUE) {
                            throw Trace.error(
                                Trace.NUMERIC_VALUE_OUT_OF_RANGE);
                        }

                        return ValuePool.getInt(((Number) o).intValue());
                    }

                    if (o instanceof Byte || o instanceof Short) {
                        return ValuePool.getInt(((Number) o).intValue());
                    }

                    if (o instanceof java.lang.Number) {
                        return convertToInt(o);
                    }

                    if (o instanceof java.lang.Boolean) {
                        return ((Boolean) o).booleanValue()
                               ? ValuePool.getInt(1)
                               : ValuePool.getInt(0);
                    }
                    break;

                case Types.BIGINT :
                    if (o instanceof java.lang.Long) {
                        return o;
                    }

                    if (o instanceof java.lang.String) {
                        o = Library.trim((String) o, " ", true, true);

                        long val = Long.parseLong((String) o);

                        return ValuePool.getLong(val);
                    }

                    if (o instanceof java.lang.Integer) {
                        return ValuePool.getLong(((Integer) o).longValue());
                    }

                    if (o instanceof Byte || o instanceof Short) {
                        return ValuePool.getLong(((Number) o).intValue());
                    }

                    if (o instanceof java.lang.Number) {
                        return convertToLong(o);
                    }

                    if (o instanceof java.lang.Boolean) {
                        return ((Boolean) o).booleanValue()
                               ? ValuePool.getLong(1)
                               : ValuePool.getLong(0);
                    }
                    break;

                case Types.REAL :
                case Types.FLOAT :
                case Types.DOUBLE :
                    if (o instanceof java.lang.Double) {
                        return o;
                    }

                    if (o instanceof java.lang.String) {
                        o = Library.trim((String) o, " ", true, true);

                        double d = JavaSystem.parseDouble((String) o);
                        long   l = Double.doubleToLongBits(d);

                        return ValuePool.getDouble(l);
                    }

                    if (o instanceof java.lang.Number) {
                        return convertToDouble(o);
                    }

                    if (o instanceof java.lang.Boolean) {
                        return ((Boolean) o).booleanValue()
                               ? ValuePool.getDouble(1)
                               : ValuePool.getDouble(0);
                    }
                    break;

                case Types.NUMERIC :
                case Types.DECIMAL :
                    if (o instanceof BigDecimal) {
                        return o;
                    }

                    if (o instanceof Long) {
                        return BigDecimal.valueOf(((Long) o).longValue());
                    }

                    if (o instanceof java.lang.Boolean) {
                        return ((Boolean) o).booleanValue() ? BIG_DECIMAL_1
                                                            : BIG_DECIMAL_0;
                    }
                    break;

                case Types.BOOLEAN :
                    if (o instanceof java.lang.Boolean) {
                        return (Boolean) o;
                    }

                    if (o instanceof java.lang.String) {
                        o = Library.trim((String) o, " ", true, true);

                        return ((String) o).equalsIgnoreCase("TRUE")
                               ? Boolean.TRUE
                               : Boolean.FALSE;
                    }

                    if (o instanceof Integer) {
                        return ((Integer) o).intValue() == 0 ? Boolean.FALSE
                                                             : Boolean.TRUE;
                    }

                    if (o instanceof Long) {
                        return ((Long) o).longValue() == 0L ? Boolean.FALSE
                                                            : Boolean.TRUE;
                    }

                    if (o instanceof java.lang.Double) {
                        return ((Double) o).doubleValue() == 0.0
                               ? Boolean.FALSE
                               : Boolean.TRUE;
                    }

                    if (o instanceof BigDecimal) {
                        return ((BigDecimal) o).equals(BIG_DECIMAL_0)
                               ? Boolean.FALSE
                               : Boolean.TRUE;
                    }

                    throw Trace.error(Trace.WRONG_DATA_TYPE);
                case Types.VARCHAR_IGNORECASE :
                case Types.VARCHAR :
                case Types.CHAR :
                case Types.LONGVARCHAR :
                    if (o instanceof java.lang.String) {
                        return o;
                    }

                    if (o instanceof Time) {
                        return HsqlDateTime.getTimeString((Time) o, null);
                    }

                    if (o instanceof Timestamp) {
                        return HsqlDateTime.getTimestampString((Timestamp) o,
                                                               null);
                    }

                    if (o instanceof Date) {
                        return HsqlDateTime.getDateString((Date) o, null);
                    }

                    if (o instanceof byte[]) {
                        return StringConverter.byteToHex((byte[]) o);
                    }
                    break;

                case Types.TIME :
                    if (o instanceof Time) {
                        return HsqlDateTime.getNormalisedTime((Time) o);
                    }

                    if (o instanceof Timestamp) {
                        return HsqlDateTime.getNormalisedTime((Timestamp) o);
                    }

                    if (o instanceof String) {
                        return HsqlDateTime.timeValue((String) o);
                    }

                    if (o instanceof Date) {
                        throw Trace.error(Trace.INVALID_CONVERSION,
                                          Types.getTypeString(type));
                    }
                    break;

                case Types.TIMESTAMP :
                    if (o instanceof Timestamp) {
                        return o;
                    }

                    if (o instanceof Time) {
                        return HsqlDateTime.getNormalisedTimestamp((Time) o);
                    }

                    if (o instanceof Date) {
                        return HsqlDateTime.getNormalisedTimestamp((Date) o);
                    }

                    if (o instanceof String) {
                        return HsqlDateTime.timestampValue((String) o);
                    }
                    break;

                case Types.DATE :
                    if (o instanceof Date) {
                        return HsqlDateTime.getNormalisedDate((Date) o);
                    }

                    if (o instanceof Timestamp) {
                        return HsqlDateTime.getNormalisedDate((Timestamp) o);
                    }

                    if (o instanceof String) {
                        return HsqlDateTime.dateValue((String) o);
                    }

                    if (o instanceof Time) {
                        throw Trace.error(Trace.INVALID_CONVERSION,
                                          Types.getTypeString(type));
                    }
                    break;

                case Types.BINARY :
                case Types.VARBINARY :
                case Types.LONGVARBINARY :
                    if (o instanceof Binary) {
                        return o;
                    } else if (o instanceof byte[]) {
                        return new Binary((byte[]) o, false);
                    } else if (o instanceof String) {

                        /**
                         * @todo fredt - we need this for script processing only
                         *  handle the script separately and process normal
                         *  conversion according to rules in SQL
                         *  standard
                         */
                        return new Binary(
                            StringConverter.hexToByte((String) o), false);
                    }

                    throw Trace.error(Trace.INVALID_CONVERSION,
                                      Types.getTypeString(type));

// fredt@users 20030708 -  patch 1.7.2 - OBJECT handling - superseded
                case Types.OTHER :
                    if (o instanceof JavaObject) {
                        return o;
                    } else if (o instanceof String) {

                        /**
                         * @todo fredt - we need this for script processing only
                         *  handle the script separately and allow normal Sting
                         *  objects to be stored as JavaObject
                         */
                        return new JavaObject(
                            StringConverter.hexToByte((String) o));
                    } else if (o instanceof Binary) {
                        return new JavaObject(((Binary) o).getBytes());
                    }

                    return new JavaObject((Serializable) o);

                default :
            }

            if (o instanceof JavaObject) {
                o = ((JavaObject) o).getObject();

                return convertObject(o, type);
            }

            return convertString(o.toString(), type);
        } catch (HsqlException e) {
            throw e;
        } catch (Exception e) {
            throw Trace.error(Trace.WRONG_DATA_TYPE, e.toString());
        }
    }

    /**
     *  Return a java object based on a SQL string. This is called from
     *  convertObject(Object o, int type).
     *
     * @param  s
     * @param  type
     * @return
     * @throws  HsqlException
     */
    private static Object convertString(String s,
                                        int type) throws HsqlException {

        switch (type) {

            case Types.TINYINT :
            case Types.SMALLINT :

                // fredt - do maximumm / minimum checks on each type
                return convertObject(s, type);

            case Types.INTEGER :
                int val = Integer.parseInt(s);

                return ValuePool.getInt(val);

            case Types.BIGINT :
                return ValuePool.getLong(Long.parseLong(s));

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE :
                double d = JavaSystem.parseDouble(s);
                long   l = Double.doubleToLongBits(d);

                return ValuePool.getDouble(l);

            case Types.VARCHAR_IGNORECASE :
            case Types.VARCHAR :
            case Types.CHAR :
            case Types.LONGVARCHAR :
                return s;

            case Types.DATE :
                return HsqlDateTime.dateValue(s);

            case Types.TIME :
                return HsqlDateTime.timeValue(s);

            case Types.TIMESTAMP :
                return HsqlDateTime.timestampValue(s);

            case Types.NUMERIC :
            case Types.DECIMAL :
                s = Library.trim(s, " ", true, true);

                return new BigDecimal(s);

            case Types.BOOLEAN :
                return s.equalsIgnoreCase("TRUE") ? Boolean.TRUE
                                                  : Boolean.FALSE;

            case Types.BINARY :
            case Types.VARBINARY :
            case Types.LONGVARBINARY :
            case Types.OTHER :
            default :
                throw Trace.error(Trace.INVALID_CONVERSION,
                                  Types.getTypeString(type));
        }
    }

    /**
     *  Return an SQL representation of an object. Strings will be quoted
     *  with single quotes, other objects will represented as in a SQL
     *  statement.
     *
     * @param  o
     * @param  type
     * @return result
     * @throws  HsqlException
     */
    static String createSQLString(Object o, int type) throws HsqlException {

        if (o == null) {
            return "NULL";
        }

        switch (type) {

            case Types.NULL :
                return "NULL";

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE :
                return createSQLString(((Number) o).doubleValue());

            case Types.DATE :
            case Types.TIME :
            case Types.TIMESTAMP :
                return StringConverter.toQuotedString(o.toString(), '\'',
                                                      false);

            case Types.BINARY :
            case Types.VARBINARY :
            case Types.LONGVARBINARY :
                if (!(o instanceof Binary)) {
                    throw Trace.error(Trace.INVALID_CONVERSION);
                }

                return StringConverter.toQuotedString(
                    StringConverter.byteToHex(((Binary) o).getBytes()), '\'',
                    false);

            case Types.OTHER :
                if (!(o instanceof JavaObject)) {
                    throw Trace.error(Trace.SERIALIZATION_FAILURE);
                }

                return StringConverter.toQuotedString(
                    StringConverter.byteToHex(((JavaObject) o).getBytes()),
                    '\'', false);

            case Types.VARCHAR_IGNORECASE :
            case Types.VARCHAR :
            case Types.CHAR :
            case Types.LONGVARCHAR :
                return createSQLString((String) o);

            default :
                return o.toString();
        }
    }

    public static String createSQLString(double x) {

        if (x == Double.NEGATIVE_INFINITY) {
            return "-1E0/0";
        }

        if (x == Double.POSITIVE_INFINITY) {
            return "1E0/0";
        }

        if (Double.isNaN(x)) {
            return "0E0/0E0";
        }

        String s = Double.toString(x);

        // ensure the engine treats the value as a DOUBLE, not DECIMAL
        if (s.indexOf('E') < 0) {
            s = s.concat("E0");
        }

        return s;
    }

    /**
     *  Turns a java string into a quoted SQL string
     *
     * @param  s java string
     * @return quoted SQL string
     */
    public static String createSQLString(String s) {

        if (s == null) {
            return "NULL";
        }

        return StringConverter.toQuotedString(s, '\'', true);
    }

    /**
     * Explicit casts are handled here. This is separate from the implicit
     * casts carried out when inserting/updating rows.
     * SQL standard 6.12 rules for enforcement of size, precision and scale
     * are implemented here are as follows:
     *
     * For no size, precision or scale, default to convertObject(Object)
     *
     * Right truncation is allowed only for CHAR to CHAR casts
     * (CHAR is generic for all string types).
     *
     * For other casts to CHAR, right truncation is not allowed.
     *
     * For numeric conversions, scale is always converted to target first,
     * then precision is imposed. No truncation is allowed. (fredt)
     */
    public static Object convertObject(Session session, Object o, int type,
                                       int precision,
                                       int scale) throws HsqlException {

        if (o == null) {
            return o;
        }

        if (precision == 0) {
            return convertObject(o, type);
        }

        boolean check = true;

        switch (type) {

            case Types.VARCHAR_IGNORECASE :
            case Types.LONGVARCHAR :
                type = Types.VARCHAR;
            case Types.VARCHAR :
            case Types.CHAR :
                if (o instanceof String) {
                    check = false;
                } else {
                    o = convertObject(o, Types.VARCHAR);
                }

                return enforceSize(o, type, precision, scale, check);

            case Types.NUMERIC :
            case Types.DECIMAL :
                if (!(o instanceof BigDecimal)) {
                    o = convertObject(o, type);
                }

                return enforceSize(o, type, precision, scale, true);

            case Types.TIMESTAMP :
                if (o instanceof Time) {
                    long millis = session.currentDate.getTime()
                                  + ((Time) o).getTime();

                    o = HsqlDateTime.getTimestamp(millis);
                }

                if (o instanceof Timestamp) {
                    return enforceSize(o, type, precision, scale, false);
                }
        }

        return convertObject(o, type);
    }

    static int[] tenPower = {
        1000000000, 100000000, 10000000, 1000000, 100000, 10000, 1000
    };

    /**
     *  Check an object for type CHAR and VARCHAR and truncate/pad based on
     *  the  size
     *
     * @param  obj   object to check
     * @param  type  the object type
     * @param  size  size to enforce
     * @param check  throw if too long
     * @return       the altered object if the right type, else the object
     *      passed in unaltered
     * @throws HsqlException if data too long
     */
    static Object enforceSize(Object obj, int type, int size, int scale,
                              boolean check) throws HsqlException {

        if (obj == null) {
            return obj;
        }

        if (size == 0 && type != Types.TIMESTAMP) {
            return obj;
        }

        // todo: need to handle BINARY like this as well
        switch (type) {

            case Types.CHAR :
                return checkChar((String) obj, size, check);

            case Types.VARCHAR :
            case Types.VARCHAR_IGNORECASE :
                return checkVarchar((String) obj, size, check);

            case Types.NUMERIC :
            case Types.DECIMAL :
                BigDecimal dec = (BigDecimal) obj;

                dec = dec.setScale(scale, BigDecimal.ROUND_HALF_DOWN);

                BigInteger big  = JavaSystem.getUnscaledValue(dec);
                int        sign = big.signum() == -1 ? 1
                                                     : 0;

                if (big.toString().length() - sign > size) {
                    throw Trace.error(Trace.STRING_DATA_TRUNCATION);
                }

                return dec;

            case Types.TIMESTAMP :
                if (size == 6) {
                    return obj;
                }

                Timestamp ts       = (Timestamp) obj;
                int       nanos    = ts.getNanos();
                int       divisor  = tenPower[size];
                int       newNanos = (nanos / divisor) * divisor;

                ts.setNanos(newNanos);

                return ts;

            default :
                return obj;
        }
    }

    /**
     *  Checks the length of a VARCHAR string.
     *
     * @param s     the string to pad to truncate
     * @param len   the len to make the string
     * @param check if true, throw an exception if truncation takes place
     * @return      the string of size len
     */
    static String checkVarchar(String s, int len,
                               boolean check) throws HsqlException {

        int slen = s.length();

        if (slen > len) {
            if (check) {
                throw Trace.error(Trace.STRING_DATA_TRUNCATION);
            }

            return s.substring(0, len);
        }

        return s;
    }

    /**
     *  Checks and pads a CHARACTER string to len size
     *
     * @param s     the string to pad to truncate
     * @param len   the len to make the string
     * @param check if true, throw an exception if truncation takes place
     * @return      the string of size len
     */
    static String checkChar(String s, int len,
                            boolean check) throws HsqlException {

        int slen = s.length();

        if (slen == len) {
            return s;
        }

        if (slen > len) {
            if (check) {
                throw Trace.error(Trace.STRING_DATA_TRUNCATION);
            }

            return s.substring(0, len);
        }

        char[] b = new char[len];

        s.getChars(0, slen, b, 0);

        for (int i = slen; i < len; i++) {
            b[i] = ' ';
        }

        return new String(b);
    }

    /**
     * Type narrowing from DOUBLE/DECIMAL/NUMERIC to BIGINT / INT / SMALLINT / TINYINT
     * following the SQL rules. When conversion is from a non-integral type,
     * digits to the right of the decimal point are lost.
     */

    /**
     * Converter from a numeric object to Integer. Input is checked to be
     * within range represented by Integer.
     */
    static Integer convertToInt(Object o) throws HsqlException {

        if (o instanceof BigDecimal) {
            BigInteger bi = ((BigDecimal) o).toBigInteger();

            if (bi.compareTo(MAX_INT) > 0 || bi.compareTo(MIN_INT) < 0) {
                throw Trace.error(Trace.NUMERIC_VALUE_OUT_OF_RANGE);
            }

            return ValuePool.getInt(bi.intValue());
        }

        if (o instanceof Double || o instanceof Float) {
            double d = ((Number) o).doubleValue();

            if (Double.isNaN(d) || d >= (double) Integer.MAX_VALUE + 1
                    || d <= (double) Integer.MIN_VALUE - 1) {
                throw Trace.error(Trace.NUMERIC_VALUE_OUT_OF_RANGE);
            }

            return ValuePool.getInt((int) d);
        }

        throw Trace.error(Trace.INVALID_CONVERSION);
    }

    /**
     * Converter from a numeric object to Long. Input is checked to be
     * within range represented by Long.
     */
    static Long convertToLong(Object o) throws HsqlException {

        if (o instanceof BigDecimal) {
            BigInteger bi = ((BigDecimal) o).toBigInteger();

            if (bi.compareTo(MAX_LONG) > 0 || bi.compareTo(MIN_LONG) < 0) {
                throw Trace.error(Trace.NUMERIC_VALUE_OUT_OF_RANGE);
            }

            return ValuePool.getLong(bi.longValue());
        }

        if (o instanceof Double || o instanceof Float) {
            double d = ((Number) o).doubleValue();

            if (Double.isNaN(d) || d >= (double) Long.MAX_VALUE + 1
                    || d <= (double) Long.MIN_VALUE - 1) {
                throw Trace.error(Trace.NUMERIC_VALUE_OUT_OF_RANGE);
            }

            return ValuePool.getLong((long) d);
        }

        throw Trace.error(Trace.INVALID_CONVERSION);
    }

    /**
     * Converter from a numeric object to Double. Input is checked to be
     * within range represented by Double
     */
    static Double convertToDouble(Object o) throws HsqlException {

        double val;

        if (o instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) o;

            val = bd.doubleValue();

            int        signum = bd.signum();
            BigDecimal bo     = new BigDecimal(val + signum);

            if (bo.compareTo(bd) != signum) {
                throw Trace.error(Trace.NUMERIC_VALUE_OUT_OF_RANGE);
            }
        } else {
            val = ((Number) o).doubleValue();
        }

        return ValuePool.getDouble(Double.doubleToLongBits(val));
    }

// fredt@users 20020408 - patch 442993 by fredt - arithmetic expressions

    /**
     *  Arithmetic expression terms are promoted to a type that can
     *  represent the resulting values and avoid incorrect results.<p>
     *  When the result or the expression is converted to the
     *  type of the target column for storage, an exception is thrown if the
     *  resulting value cannot be stored in the column<p>
     *  Returns a SQL type "wide" enough to represent the result of the
     *  expression.<br>
     *  A type is "wider" than the other if it can represent all its
     *  numeric values.<BR>
     *  Types narrower than INTEGER (int) are promoted to
     *  INTEGER. The order is as follows<p>
     *
     *  INTEGER, BIGINT, DOUBLE, DECIMAL<p>
     *
     *  TINYINT and SMALLINT in any combination return INTEGER<br>
     *  INTEGER and INTEGER return BIGINT<br>
     *  BIGINT and INTEGER return NUMERIC/DECIMAL<br>
     *  BIGINT and BIGINT return NUMERIC/DECIMAL<br>
     *  DOUBLE and INTEGER return DOUBLE<br>
     *  DOUBLE and BIGINT return DOUBLE<br>
     *  NUMERIC/DECIMAL and any type returns NUMERIC/DECIMAL<br>
     *
     * @author fredt@users
     * @param  type1  java.sql.Types value for the first numeric type
     * @param  type2  java.sql.Types value for the second numeric type
     * @return        either type1 or type2 on the basis of the above order
     */
    static int getCombinedNumberType(int type1, int type2, int expType) {

        int typeWidth1 = getNumTypeWidth(type1);
        int typeWidth2 = getNumTypeWidth(type2);

        if (typeWidth1 == 16 || typeWidth2 == 16) {
            return Types.DOUBLE;
        }

        switch (expType) {

            case Expression.EQUAL :
            case Expression.BIGGER :
            case Expression.BIGGER_EQUAL :
            case Expression.SMALLER_EQUAL :
            case Expression.SMALLER :
            case Expression.NOT_EQUAL :
            case Expression.ALTERNATIVE :
            case Expression.DIVIDE :
                return (typeWidth1 > typeWidth2) ? type1
                                                 : type2;

            default :
                int sum = typeWidth1 + typeWidth2;

                if (sum <= 4) {
                    return Types.INTEGER;
                }

                if (sum <= 8) {
                    return Types.BIGINT;
                }

                return Types.NUMERIC;
        }
    }

    /**
     * @param  type java.sql.Types int for a numeric type
     * @return relative width
     */
    static int getNumTypeWidth(int type) {

        switch (type) {

            case Types.TINYINT :
                return 1;

            case Types.SMALLINT :
                return 2;

            case Types.INTEGER :
                return 4;

            case Types.BIGINT :
                return 8;

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE :
                return 16;

            case Types.NUMERIC :
            case Types.DECIMAL :
                return 32;

            default :
                return 32;
        }
    }

    /**
     * returns -1, 0 , +1
     */
    static int compareToTypeRange(Object o, int targettype) {

        if (!(o instanceof Number)) {
            return 0;
        }

        if (o instanceof Integer || o instanceof Long) {
            long temp = ((Number) o).longValue();
            int  min;
            int  max;

            switch (targettype) {

                case Types.TINYINT :
                    min = Byte.MIN_VALUE;
                    max = Byte.MAX_VALUE;
                    break;

                case Types.SMALLINT :
                    min = Short.MIN_VALUE;
                    max = Short.MAX_VALUE;
                    break;

                case Types.INTEGER :
                    min = Integer.MIN_VALUE;
                    max = Integer.MAX_VALUE;
                    break;

                case Types.BIGINT :
                case Types.DECIMAL :
                case Types.NUMERIC :
                default :
                    return 0;
            }

            if (max < temp) {
                return 1;
            }

            if (temp < min) {
                return -1;
            }

            return 0;
        } else {
            try {
                o = convertToLong(o);

                return compareToTypeRange(o, targettype);
            } catch (HsqlException e) {
                if (e.getErrorCode() == -Trace.NUMERIC_VALUE_OUT_OF_RANGE) {
                    if (o instanceof BigDecimal) {
                        return ((BigDecimal) o).signum();
                    } else if (o instanceof Double) {
                        return ((Double) o).doubleValue() > 0 ? 1
                                                              : -1;
                    }
                }
            }
        }

        return 0;
    }

    /**
     * Converts the specified hexadecimal digit <CODE>String</CODE>
     * to an equivalent array of bytes.
     *
     * @param hexString a <CODE>String</CODE> of hexadecimal digits
     * @throws HsqlException if the specified string contains non-hexadecimal digits.
     * @return a byte array equivalent to the specified string of hexadecimal digits
     */
    public static byte[] hexToByteArray(String hexString)
    throws HsqlException {

        try {
            return StringConverter.hexToByte(hexString);
        } catch (IOException e) {
            throw Trace.error(Trace.INVALID_CHARACTER_ENCODING);
        }
    }

    /**
     * Compares a <CODE>byte[]</CODE> with another specified
     * <CODE>byte[]</CODE> for order.  Returns a negative integer, zero,
     * or a positive integer as the first object is less than, equal to, or
     * greater than the specified second <CODE>byte[]</CODE>.<p>
     *
     * @param o1 the first byte[] to be compared
     * @param o2 the second byte[] to be compared
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     */
    static int compareTo(byte[] o1, byte[] o2) {

        int len  = o1.length;
        int lenb = o2.length;

        for (int i = 0; ; i++) {
            int a = 0;
            int b = 0;

            if (i < len) {
                a = ((int) o1[i]) & 0xff;
            } else if (i >= lenb) {
                return 0;
            }

            if (i < lenb) {
                b = ((int) o2[i]) & 0xff;
            }

            if (a > b) {
                return 1;
            }

            if (b > a) {
                return -1;
            }
        }
    }
}
