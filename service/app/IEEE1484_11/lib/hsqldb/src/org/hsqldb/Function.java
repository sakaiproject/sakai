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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;

import org.hsqldb.lib.HashMap;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.StringConverter;
import org.hsqldb.types.Binary;
import org.hsqldb.types.JavaObject;

// fredt@users 20020912 - patch 1.7.1 - shortcut treatment of identity() call
// fredt@users 20020912 - patch 1.7.1 - cache java.lang.reflect.Method objects
// fredt@users 20021013 - patch 1.7.1 - ignore non-static methods
// boucherb@users 20030201 - patch 1.7.2 - direct calls for org.hsqldb.Library
// fredt@users 20030621 - patch 1.7.2 - shortcut treatment of session calls
// boucherb@users 200404xx - doc 1.7.2 - updates toward 1.7.2 final

/**
 * Provides services to evaluate SQL function and stored procedure calls,
 * by invoking Java methods.
 *
 * Extended in successive versions of HSQLDB.
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.8.0
 * @since Hypersonic SQL
 */
class Function {

    private String         sFunction;
    private Method         mMethod;
    private String         returnClassName;
    private Class[]        aArgClasses;
    private int            iReturnType;
    private int            iArgCount;
    private int            iSqlArgCount;
    private int            iSqlArgStart;
    private int[]          iArgType;
    private boolean[]      bArgNullable;
    Expression[]           eArg;
    private boolean        bConnection;
    private static HashMap methodCache = new HashMap();
    private int            fID;
    String                 name;        // name used to call function
    boolean                isSimple;    //CURRENT_TIME, NOW etc.
    boolean                hasAggregate;

    /**
     * Constructs a new Function object with the given function call name
     * and using the specified Session context. <p>
     *
     * The call name is the fully qualified name of a static Java method, in
     * the form "package.class.method."  This implies that Java
     * methods with the same fully qualified name but different signatures
     * cannot be used properly as HSQLDB SQL functions or stored procedures.
     * For instance, it is impossible to call both System.getProperty(String)
     * and System.getProperty(String,String) under this arrangement, because
     * the HSQLDB Function object is unable to differentiate between the two;
     * it simply chooses the first method matching the FQN in the array of
     * methods obtained from calling getMethods() on an instance of the
     * Class indicated in the FQN, hiding all other methods with the same
     * FQN. <p>
     *
     * The function FQN must match at least one static Java method FQN in the
     * specified class or construction cannot procede and an HsqlException is
     * thrown. <p>
     *
     * The isSimple parameter is true when certain SQL standard functions
     * that are used without brackets are invokded.
     *
     * @param name this Function object's call name
     * @param fqn the fully qualified name of a Java method
     * @param isSimple if true, used to evalate CURRENT_TIME, NOW etc.
     *      evaluate
     * @throws HsqlException if the specified function FQN corresponds to no
     *      Java method
     */
    Function(String name, String fqn, boolean isSimple) throws HsqlException {

        this.name     = name;
        this.isSimple = isSimple;

//        cSession      = session;
        sFunction = fqn;
        fID       = Library.functionID(fqn);

        int i = fqn.lastIndexOf('.');

        Trace.check(i != -1, Trace.UNEXPECTED_TOKEN, fqn);

        String classname = fqn.substring(0, i);

        mMethod = (Method) methodCache.get(fqn);

        if (mMethod == null) {
            String methodname    = fqn.substring(i + 1);
            Class  classinstance = null;

            try {
                classinstance = Class.forName(classname);
            } catch (Exception e) {
                throw Trace.error(Trace.FUNCTION_NOT_FOUND,
                                  Trace.Message_Pair, new Object[] {
                    classname, e
                });
            }

            // public only, but includes those inherited from
            // superclasses and superinterfaces.  List is unordered.
            Method[] methods = classinstance.getMethods();

            for (i = 0; i < methods.length; i++) {
                Method m = methods[i];

                if (m.getName().equals(methodname)
                        && Modifier.isStatic(m.getModifiers())) {
                    mMethod = m;

                    break;
                }
            }

            Trace.check(mMethod != null, Trace.UNKNOWN_FUNCTION, methodname);
            methodCache.put(fqn, mMethod);
        }

        Class returnClass = mMethod.getReturnType();

        if (returnClass.equals(org.hsqldb.Result.class)) {

            // For now, we can write stored procedures whose
            // descriptor explicitly specifies the above return type.
            // Later, this will be modified or replaced to provide proper
            // support for jdbcCallableStatement OUT mode return parameter,
            // multiple results (Result.MULTI etc.)
            iReturnType = Types.OTHER;
        } else {

            // Now we support the following construction-time return type
            // Classes, as specified by the method descriptor:
            //
            // 1.) any primitive or primitive wrapper type, except Byte(.TYPE),
            //     Short(.TYPE) and Float(.TYPE) (TBD; narrow if no truncation)
            //
            // 2.) any primitive array type
            //
            // 3.) any non-primitive array whose base component implements
            // java.io.Serializable
            //
            // 4.) any class implementing java.io.Serializable, except those
            //     described in 1.) as currently unsupported
            //
            // 5.) java.lang.Object
            //
            // For java.lang.Object, checking is deferred from the construction
            // stage to the evaluation stage.  In general, for the evaluation
            // to succeed, the runtime class of the retrieved Object must be
            //
            // 1.) any primitive or primitive wrapper type, except Byte(.TYPE),
            //     Short(.TYPE) and Float(.TYPE) (TBD; narrow if no trunction)
            //
            // 2.) any primitive array type
            // 3.) any non-primitive array whose base component implements
            //     java.io.Serializable
            //
            // 4.) any class implementing java.io.Serializable, except those
            //     described in 1.) as currently unsupported
            //
            // Additionally, it is possible for the evaluation to succeed under
            // an SQL CALL if the runtime Class of the returned Object is not
            // from the list above but is from the list below:
            //
            // 1.) is org.hsqldb.Result
            // 2.) is org.hsqldb.jdbc.jdbcResultSet
            //
            // In these special cases, the statement executor notices the
            // types and presents the client with a view the underlying result
            // rather than with a view of the object as an opaque scalar value
            //
            iReturnType = Types.getParameterTypeNr(returnClass);
        }

        returnClassName =
            Types.getFunctionReturnClassName(returnClass.getName());
        aArgClasses  = mMethod.getParameterTypes();
        iArgCount    = aArgClasses.length;
        iArgType     = new int[iArgCount];
        bArgNullable = new boolean[iArgCount];

        for (i = 0; i < aArgClasses.length; i++) {
            Class  a    = aArgClasses[i];
            String type = a.getName();

            if ((i == 0) && a.equals(Connection.class)) {

                // TODO: provide jdbc:default:connection url functionality
                //
                // only the first parameter can be a Connection
                bConnection = true;
            } else {

                // see discussion above for iReturnType
                iArgType[i]     = Types.getParameterTypeNr(a);
                bArgNullable[i] = !a.isPrimitive();
            }
        }

        iSqlArgCount = iArgCount;

        if (bConnection) {
            iSqlArgCount--;

            iSqlArgStart = 1;
        } else {
            iSqlArgStart = 0;
        }

        eArg = new Expression[iArgCount];
    }

    /**
     * Evaluates and returns this Function in the context of the session.<p>
     */
    Object getValue(Session session) throws HsqlException {

        switch (fID) {

            case Library.curtime :
                return session.getCurrentTime();

            case Library.curdate :
                return session.getCurrentDate();

            case Library.database :
                return session.getDatabase().getPath();

            case Library.getAutoCommit :
                return session.isAutoCommit() ? Boolean.TRUE
                                              : Boolean.FALSE;

            case Library.identity :
                return session.getLastIdentity();

            case Library.isReadOnlyDatabase :
                return session.getDatabase().databaseReadOnly ? Boolean.TRUE
                                                              : Boolean.FALSE;

            case Library.isReadOnlyConnection :
                return session.isReadOnly() ? Boolean.TRUE
                                            : Boolean.FALSE;

            case Library.isReadOnlyDatabaseFiles :
                return session.getDatabase().isFilesReadOnly() ? Boolean.TRUE
                                                               : Boolean
                                                               .FALSE;

            case Library.now :
                return session.getCurrentTimestamp();

            case Library.user :
                return session.getUsername();
        }

        Object[] oArg = getArguments(session);

        if (oArg == null) {
            return null;
        }

        return getValue(session, oArg);
    }

    /**
     * Evaluates the Function with the given arguments in the session context.
     */
    Object getValue(Session session,
                    Object[] arguments) throws HsqlException {

        if (bConnection) {
            arguments[0] = session.getInternalConnection();
        }

        try {
            Object ret = (fID >= 0) ? Library.invoke(fID, arguments)
                                    : mMethod.invoke(null, arguments);

            return Column.convertObject(ret, iReturnType);
        } catch (InvocationTargetException e) {

            // thrown by user functions
            Throwable t = e.getTargetException();
            String    s = sFunction + " : " + t.toString();

            throw Trace.error(Trace.FUNCTION_CALL_ERROR, s);
        } catch (IllegalAccessException e) {

            // never thrown in this method
            throw Trace.error(Trace.FUNCTION_CALL_ERROR);
        }

        // Library function throw HsqlException
    }

    private Object[] getArguments(Session session) throws HsqlException {

        int      i    = bConnection ? 1
                                    : 0;
        Object[] oArg = new Object[iArgCount];

        for (; i < iArgCount; i++) {
            Expression e = eArg[i];
            Object     o = null;

            if (e != null) {

                // no argument: null
                o = e.getValue(session, iArgType[i]);
            }

            if ((o == null) &&!bArgNullable[i]) {

                // null argument for primitive datatype: don't call
                return null;
            }

            if (o instanceof JavaObject) {
                o = ((JavaObject) o).getObject();
            } else if (o instanceof Binary) {
                o = ((Binary) o).getBytes();
            }

            oArg[i] = o;
        }

        return oArg;
    }

    /**
     * returns null if any non-nullable element of values is null
     */
    private Object[] getNotNull(Object[] values) throws HsqlException {

        int i = bConnection ? 1
                            : 0;

        for (; i < iArgCount; i++) {
            Object o = values[i];

            if (o == null &&!bArgNullable[i]) {

                // null argument for primitive datatype: don't call
                return null;
            }
        }

        return values;
    }

    void collectInGroupByExpressions(HsqlArrayList colExps) {

        for (int i = 0; i < iArgCount; i++) {
            Expression e = eArg[i];

            if (e != null) {
                e.collectInGroupByExpressions(colExps);
            }
        }
    }

    Object getAggregatedValue(Session session,
                              Object currValue) throws HsqlException {

        Object[] valueArray = (Object[]) currValue;

        if (valueArray == null) {
            valueArray = new Object[iArgCount];
        }

        for (int i = 0; i < iArgCount; i++) {
            Expression e = eArg[i];

            if (eArg[i] != null) {
                if (eArg[i].isAggregate()) {
                    valueArray[i] = Column.convertObject(
                        e.getAggregatedValue(session, valueArray[i]),
                        iArgType[i]);
                } else {
                    valueArray[i] = e.getValue(session, iArgType[i]);
                }
            }
        }

        valueArray = getNotNull(valueArray);

        if (valueArray == null) {
            return null;
        }

        return getValue(session, valueArray);
    }

    Object updateAggregatingValue(Session session,
                                  Object currValue) throws HsqlException {

        Object[] valueArray = (Object[]) currValue;

        if (valueArray == null) {
            valueArray = new Object[iArgCount];
        }

        for (int i = 0; i < iArgCount; i++) {
            Expression e = eArg[i];

            if (eArg[i] != null) {
                valueArray[i] = e.updateAggregatingValue(session,
                        valueArray[i]);
            }
        }

        return valueArray;
    }

    /**
     * Returns the number of parameters that must be supplied to evaluate
     * this Function object from SQL.  <p>
     *
     * This value may be different than the number of parameters of the
     * underlying Java method.  This is because HSQLDB automatically detects
     * if the first parameter is of type java.sql.Connection, and supplies a
     * live Connection object constructed from the evaluating session context
     * if so.
     */
    int getArgCount() {
        return iSqlArgCount;
    }

    /**
     * Remnoves the Table filters from Expression parameters to this Function.
     *
     * @throws HsqlException if there is a problem resolving a parameter
     * against the specified TableFilter
     */
/*
    void removeFilters() throws HsqlException {

        Expression e;

        for (int i = iSqlArgStart; i < iArgCount; i++) {
            e = eArg[i];

            if (e != null) {
                e.removeFilters();
            }
        }
    }
*/
    void replaceAliases(Expression[] columns,
                        int length) throws HsqlException {

        Expression e;

        for (int i = iSqlArgStart; i < iArgCount; i++) {
            e = eArg[i];

            if (e != null) {
                if (e.exprType == Expression.COLUMN) {
                    eArg[i] = e.getExpressionForAlias(columns, length);
                } else {
                    e.replaceAliases(columns, length);
                }
            }
        }
    }

    /**
     * Checks the Expresion parameters to this Function object against the
     * set of TableFilter.
     */
    void checkTables(HsqlArrayList fa) throws HsqlException {

        Expression e;

        for (int i = iSqlArgStart; i < iArgCount; i++) {
            e = eArg[i];

            if (e != null) {
                e.checkTables(fa);
            }
        }
    }

    /**
     * Resolves the Expression parameters to this Function object against the
     * specified TableFilter.
     */
    void resolveTables(TableFilter f) throws HsqlException {

        Expression e;

        for (int i = iSqlArgStart; i < iArgCount; i++) {
            e = eArg[i];

            if (e != null) {
                e.resolveTables(f);
            }
        }
    }

    /**
     * Resolves the type of this expression and performs certain
     * transformations and optimisations of the expression tree.
     */
    void resolveType(Session session) throws HsqlException {

        Expression e;

        for (int i = iSqlArgStart; i < iArgCount; i++) {
            e = eArg[i];

            if (e != null) {
                if (e.isParam()) {
                    e.setDataType(iArgType[i]);

                    e.nullability    = getArgNullability(i);
                    e.valueClassName = getArgClass(i).getName();
                } else {
                    e.resolveTypes(session);
                }
            }
        }
    }

    /**
     * Checks each of this object's arguments for resolution, throwing an
     * HsqlException if any arguments have not yet been resolved. <p>
     * The check boolean argument is passed on to further check calls.<p>
     */
    boolean checkResolved(boolean check) throws HsqlException {

        boolean result = true;

        for (int i = iSqlArgStart; i < iArgCount; i++) {
            if (eArg[i] != null) {
                result = result && eArg[i].checkResolved(check);
            }
        }

        return result;
    }

    /**
     * Returns the type of the argument at the specified
     * offset in this Function object's paramter list. <p>
     */
    int getArgType(int i) {
        return iArgType[i];
    }

    /**
     * Returns the type of this Function
     * object's return type. <p>
     */
    int getReturnType() {
        return iReturnType;
    }

    /**
     * Binds the specified expression to the specified position in this
     * Function object's parameter list. <p>
     */
    void setArgument(int i, Expression e) {

        if (bConnection) {
            i++;
        }

        eArg[i]      = e;
        hasAggregate = hasAggregate || (e != null && e.isAggregate());
    }

    /**
     * Returns a DDL representation of this object. <p>
     */
    String getDLL() throws HsqlException {

        StringBuffer sb = new StringBuffer();

        // get the name as used by the CHECK statement
        String ddlName = name;

        if (isSimple) {
            return name;
        } else if (Token.T_TRIM.equals(name)) {

            // special case for TRIM
            sb.append(name).append('(');

            boolean leading  = eArg[2].testCondition(null);
            boolean trailing = eArg[3].testCondition(null);

            if (leading && trailing) {
                sb.append(Token.T_BOTH);
            } else {
                sb.append(leading ? Token.T_LEADING
                                  : Token.T_TRAILING);
            }

            // to do change to string
            sb.append(' ');

            String charval = (String) eArg[1].getValue(null);

            sb.append(Column.createSQLString(charval)).append(' ');
            sb.append(Token.T_FROM).append(' ');
            sb.append(eArg[0].getDDL()).append(')');

            return sb.toString();
        }

        if (sFunction.equals(name)) {
            ddlName = StringConverter.toQuotedString(name, '"', true);
        }

        sb.append(ddlName).append('(');

        for (int i = iSqlArgStart; i < eArg.length; i++) {
            sb.append(eArg[i].getDDL());

            if (i < eArg.length - 1) {
                sb.append(',');
            }
        }

        sb.append(')');

        return sb.toString();
    }

    /**
     * Returns a String representation of this object. <p>
     */
    public String describe(Session session) {

        StringBuffer sb = new StringBuffer();

        sb.append(super.toString()).append("=[\n");
        sb.append(sFunction).append("(");

        for (int i = iSqlArgStart; i < eArg.length; i++) {
            sb.append("[").append(eArg[i].describe(session)).append("]");
        }

        sb.append(") returns ").append(Types.getTypeString(getReturnType()));
        sb.append("]\n");

        return sb.toString();
    }

    /**
     * Returns the Java Class of the object returned by getValue(). <p>
     */
    String getReturnClassName() {
        return returnClassName;
    }

    /**
     * Returns the Java Class of the i'th argument. <p>
     */
    Class getArgClass(int i) {
        return aArgClasses[i];
    }

    /**
     * Returns the SQL nullability code of the i'th argument. <p>
     */
    int getArgNullability(int i) {
        return bArgNullable[i] ? Expression.NULLABLE
                               : Expression.NO_NULLS;
    }

    Method getMethod() {
        return mMethod;
    }
}
