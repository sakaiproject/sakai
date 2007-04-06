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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.hsqldb.lib.HashMap;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.WrapperIterator;

// boucherb@users - 2004xxxx - patch 1.7.2
// -- canonical database uri for catalog name reporting
// -- enumXXX methods to iterateXXX
// -- simple support for SEQUENCE schema reporting
// -- report built-in procedures/procedure columns without dependency on user grants;

/**
 * Provides catalog and schema related definitions and functionality. <p>
 *
 * Additional features include accessibility tests, class loading, filtered
 * iteration and inverted alias mapping functionality regarding Java Classes
 * and Methods defined within the context of this database name space support
 * object. <p>
 *
 * @author  boucherb@users
 * @version 1.8.0
 * @since 1.7.2
 */
final class DINameSpace {

    /** The Database for which the name space functionality is provided */
    private Database database;

    /** The catalog name reported by this namespace */
    private String catalogName;

    /**
     * Set { <code>Class</code> FQN <code>String</code> objects }. <p>
     *
     * The Set contains the names of the classes providing the public static
     * methods that are automatically made accessible to the PUBLIC user in
     * support of the expected SQL CLI scalar functions and other core
     * HSQLDB SQL functions and stored procedures. <p>
     */
    private static HashSet builtin = new HashSet();

    // procedure columns
    // make temporary ad-hoc spec a little more "official"
    // until better system in place
    static {
        builtin.add("org.hsqldb.Library");
        builtin.add("java.lang.Math");
    }

    /**
     * Constructs a new name space support object for the
     * specified Database object. <p>
     *
     * @param database The Database object for which to provide name
     *      space support
     * @throws HsqlException if a database access error occurs
     */
    public DINameSpace(Database database) throws HsqlException {

        try {
            this.database    = database;
            this.catalogName = database.getURI();
        } catch (Exception e) {
            Trace.throwerror(Trace.GENERAL_ERROR, e.toString());
        }
    }

    /**
     * Retrieves the declaring <code>Class</code> object for the specified
     * fully qualified method name, using (if possible) the classLoader
     * attribute of this object's database. <p>
     *
     * @param fqn the fully qualified name of the method for which to
     *        retrieve the declaring <code>Class</code> object.
     * @return the declaring <code>Class</code> object for the
     *        specified fully qualified method name
     */
    Class classForMethodFQN(String fqn) {

        try {
            return classForName(fqn.substring(0, fqn.lastIndexOf('.')));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Retrieves the <code>Class</code> object specified by the
     * <code>name</code> argument, using, if possible, the
     * classLoader attribute of the database. <p>
     *
     * @param name the fully qualified name of the <code>Class</code>
     *      object to retrieve.
     * @throws ClassNotFoundException if the specified class object
     *      cannot be found in the context of this name space
     * @return the <code>Class</code> object specified by the
     *      <code>name</code> argument
     */
    Class classForName(String name) throws ClassNotFoundException {

        try {
            if (database.classLoader == null) {
                return Class.forName(name);
            } else {
                if (name != null) {
                    return database.classLoader.loadClass(name);
                } else {
                    throw new ClassNotFoundException();
                }
            }
        } catch (NoClassDefFoundError err) {
            throw new ClassNotFoundException(err.toString());
        }
    }

    /**
     * Retrieves an <code>Iterator</code> whose elements form the set of
     * distinct names of all visible catalogs, relative to this object's
     * database. <p>
     *
     * If catalog reporting is turned off, then the empty Iterator is
     * returned. <p>
     *
     * <b>Note:</b> in the present implementation, if catalog reporting is
     * turned on, then the iteration consists of a single element that is the
     * uri of this object's database; HSQLDB  currently does not support the
     * concept a single engine hosting multiple catalogs. <p>
     *
     * @return An Iterator whose elements are <code>String</code> objects
     *      naming all visible catalogs, relative to this object's database.
     * @throws HsqlException never (reserved for future use)
     */
    Iterator iterateCatalogNames() throws HsqlException {
        return isReportCatalogs() ? new WrapperIterator(catalogName)
                                  : new WrapperIterator();
    }

    /**
     * Retrieves the name of the catalog corresponding to the indicated
     * object. <p>
     *
     * <B>Note:</B> the uri of this object's database is returned whenever
     * catalog reporting is turned on. <p>
     *
     * This a stub that will be used until such time (if ever) that the
     * engine actually supports the concept of multiple hosted
     * catalogs. <p>
     *
     * @return the name of specified object's qualifying catalog, or null if
     *      catalog reporting is turned off.
     * @param o the object for which the name of its qualifying catalog
     *      is to be retrieved
     */
    String getCatalogName(Object o) {
        return isReportCatalogs() ? catalogName
                                  : null;
    }

    /**
     * Retrieves a map from each distinct value of this object's database
     * SQL routine CALL alias map to the list of keys in the input map
     * mapping to that value. <p>
     *
     * @return The requested map
     */
    HashMap getInverseAliasMap() {

        HashMap       mapIn;
        HashMap       mapOut;
        Iterator      keys;
        Object        key;
        Object        value;
        HsqlArrayList keyList;

        // TODO:
        // update Database to dynamically maintain its own
        // inverse alias map.  This will make things *much*
        // faster for our  purposes here, without appreciably
        // slowing down Database
        mapIn  = database.getAliasMap();
        mapOut = new HashMap();
        keys   = mapIn.keySet().iterator();

        while (keys.hasNext()) {
            key     = keys.next();
            value   = mapIn.get(key);
            keyList = (HsqlArrayList) mapOut.get(value);

            if (keyList == null) {
                keyList = new HsqlArrayList();

                mapOut.put(value, keyList);
            }

            keyList.add(key);
        }

        return mapOut;
    }

    /**
     * Retrieves the fully qualified name of the given Method object. <p>
     *
     * @param m The Method object for which to retreive the fully
     *      qualified name
     * @return the fully qualified name of the specified Method object.
     */
    static String getMethodFQN(Method m) {

        return m == null ? null
                         : m.getDeclaringClass().getName() + '.'
                           + m.getName();
    }

    /**
     * Retrieves the specific name of the given Method object. <p>
     *
     * @param m The Method object for which to retreive the specific name
     * @return the specific name of the specified Method object.
     */
    static String getMethodSpecificName(Method m) {

        return m == null ? null
                         : m.getDeclaringClass().getName() + '.'
                           + getSignature(m);
    }

    static String getSignature(Method method) {

        StringBuffer sb;
        String       signature;
        Class[]      parmTypes;
        int          len;
        int          last;

        sb        = new StringBuffer();
        parmTypes = method.getParameterTypes();
        len       = parmTypes.length;
        last      = len - 1;

        sb.append(method.getName()).append('(');

        for (int i = 0; i < len; i++) {
            sb.append(parmTypes[i].getName());

            if (i < last) {
                sb.append(',');
            }
        }

        sb.append(')');

        signature = sb.toString();

        return signature;
    }

    /**
     * Deprecated
     */
    String getSchemaName(Object o) {
        return database.schemaManager.PUBLIC_SCHEMA;
    }

    /**
     * Adds to the given Set the fully qualified names of the Class objects
     * internally granted to PUBLIC in support of core operation.
     *
     * @param the HashSet to which to add the fully qualified names of
     * the Class objects internally granted to PUBLIC in support of
     * core operation.
     */
    void addBuiltinToSet(HashSet set) {
        set.addAll(builtin.toArray(new String[builtin.size()]));
    }

    /**
     * Retrieves whether the indicated Class object is systematically
     * granted to PUBLIC in support of core operation. <p>
     *
     * @return whether the indicated Class object is systematically
     * granted to PUBLIC in support of core operation
     * @param clazz The Class object for which to make the determination
     */
    boolean isBuiltin(Class clazz) {
        return clazz == null ? false
                             : builtin.contains(clazz.getName());
    }

    /**
     * Retrieves whether the Class object indicated by the fully qualified
     * class name is systematically granted to PUBLIC in support of
     * core operation. <p>
     *
     * @return true if system makes grant, else false
     * @param name fully qualified name of a Class
     */
    boolean isBuiltin(String name) {
        return (name == null) ? false
                              : builtin.contains(name);
    }

    /**
     * Retrieves an <code>Iterator</code> object describing the Java
     * <code>Method</code> objects that are both the entry points
     * to executable SQL database objects (such as SQL functions and
     * stored procedures) within the context of this name space. <p>
     *
     * Each element of the <code>Iterator</code> is an Object[3] array
     * whose elements are: <p>
     *
     * <ol>
     * <li>a <code>Method</code> object.
     * <li>an <code>HsqlArrayList</code> object whose elements are the SQL call
     *     aliases for the method.
     * <li>the <code>String</code> "ROUTINE"
     * </ol>
     *
     * <b>Note:</b> Admin users are actually free to invoke *any* public
     * static non-abstract Java Method that can be found through the database
     * class loading process, either as a SQL stored procedure or SQL function,
     * as long as its parameters and return type are compatible with the
     * engine's supported SQL type / Java <code>Class</code> mappings. <p>
     *
     * @return An <code>Iterator</code> object whose elements form the set
     *        of distinct <code>Method</code> objects accessible as
     *        executable as SQL routines within the current execution
     *        context.<p>
     *
     *        Elements are <code>Object[3]</code> instances, with [0] being a
     *        <code>Method</code> object, [1] being an alias list object and
     *        [2] being the <code>String</code> "ROUTINE"<p>
     *
     *        If the <code>Method</code> object at index [0] has aliases,
     *        and the <code>andAliases</code> parameter is specified
     *        as <code>true</code>, then there is an HsqlArrayList
     *        at index [1] whose elements are <code>String</code> objects
     *        whose values are the SQL call aliases for the method.
     *        Otherwise, the value of index [1] is <code>null</code>.
     * @param className The fully qualified name of the class for which to
     *        retrieve the iteration
     * @param andAliases if <code>true</code>, alias lists for qualifying
     *        methods are additionally retrieved.
     * @throws HsqlException if a database access error occurs
     *
     */
    Iterator iterateRoutineMethods(String className,
                                   boolean andAliases) throws HsqlException {

        Class         clazz;
        Method[]      methods;
        Method        method;
        int           mods;
        Object[]      info;
        HsqlArrayList aliasList;
        HsqlArrayList methodList;
        HashMap       invAliasMap;

        try {
            clazz = classForName(className);
        } catch (ClassNotFoundException e) {
            return new WrapperIterator();
        }

        invAliasMap = andAliases ? getInverseAliasMap()
                                 : null;

        // we are interested in inherited methods too,
        // so we use getDeclaredMethods() first.
        // However, under Applet execution or
        // under restrictive SecurityManager policies
        // this may fail, so we use getMethods()
        // if getDeclaredMethods() fails.
        try {
            methods = clazz.getDeclaredMethods();
        } catch (Exception e) {
            methods = clazz.getMethods();
        }

        methodList = new HsqlArrayList(methods.length);

        // add all public static methods to the set
        for (int i = 0; i < methods.length; i++) {
            method = methods[i];
            mods   = method.getModifiers();

            if (!(Modifier.isPublic(mods) && Modifier.isStatic(mods))) {
                continue;
            }

            info = new Object[] {
                method, null, "ROUTINE"
            };

            if (andAliases) {
                info[1] = invAliasMap.get(getMethodFQN(method));
            }

            methodList.add(info);
        }

        // return the iterator
        return methodList.iterator();
    }

    /**
     * Retrieves an <code>Iterator</code> object describing the
     * fully qualified names of all Java <code>Class</code> objects
     * that are both trigger body implementations and that are accessible
     * (whose fire method can potentially be invoked) by actions upon this
     * object's database by the specified <code>User</code>. <p>
     *
     * @param user the <code>User</code> for which to retrieve the
     *      <code>Iterator</code>
     * @throws HsqlException if a database access error occurs
     * @return an <code>Iterator</code> object describing the
     *        fully qualified names of all Java <code>Class</code>
     *        objects that are both trigger body implementations
     *        and that are accessible (whose fire method can
     *        potentially be invoked) by actions upon this object's database
     *        by the specified <code>User</code>.
     */
    Iterator iterateAccessibleTriggerClassNames(User user)
    throws HsqlException {

        Table           table;
        Class           clazz;
        HashSet         classSet;
        TriggerDef      triggerDef;
        HsqlArrayList[] triggerLists;
        HsqlArrayList   triggerList;
        HsqlArrayList   tableList;
        int             listSize;

        classSet = new HashSet();

        Iterator schemas = database.schemaManager.userSchemaNameIterator();

        while (schemas.hasNext()) {
            String   schema = (String) schemas.next();
            Iterator tables = database.schemaManager.tablesIterator(schema);

            while (tables.hasNext()) {
                table = (Table) tables.next();

                if (!user.isAccessible(table.getName())) {
                    continue;
                }

                triggerLists = table.triggerLists;

                if (triggerLists == null) {
                    continue;
                }

                for (int j = 0; j < triggerLists.length; j++) {
                    triggerList = triggerLists[j];

                    if (triggerList == null) {
                        continue;
                    }

                    listSize = triggerList.size();

                    for (int k = 0; k < listSize; k++) {
                        triggerDef = (TriggerDef) triggerList.get(k);

                        if (triggerDef == null ||!triggerDef.valid
                                || triggerDef.trigger == null
                                ||!user.isAccessible(
                                    table.getName(),
                                    TriggerDef.indexToRight(k))) {
                            continue;
                        }

                        classSet.add(triggerDef.trigger.getClass().getName());
                    }
                }
            }
        }

        return classSet.iterator();
    }

    /**
     * Retrieves a composite <code>Iterator</code> consisting of the elements
     * from {@link #iterateRoutineMethods} for each Class granted to the
     * specified session. <p>
     *
     * @return a composite <code>Iterator</code> consisting of the elements
     *      from {@link #iterateRoutineMethods} and
     *      {@link #iterateAccessibleTriggerMethods}
     * @param session The context in which to produce the iterator
     * @param andAliases true if the alias lists for the "ROUTINE" type method
     *      elements are to be generated.
     * @throws HsqlException if a database access error occurs
     */
    Iterator iterateAllAccessibleMethods(Session session,
                                         boolean andAliases)
                                         throws HsqlException {

        Iterator out;
        HashSet  classNameSet;
        Iterator classNames;
        Iterator methods;
        String   className;

        out          = new WrapperIterator();
        classNameSet = session.getUser().getGrantedClassNames(true);

        addBuiltinToSet(classNameSet);

        classNames = classNameSet.iterator();

        while (classNames.hasNext()) {
            className = (String) classNames.next();
            methods   = iterateRoutineMethods(className, andAliases);
            out       = new WrapperIterator(out, methods);
        }

        return out;
    }

    /**
     * Retrieves the set of distinct, visible sessions connected to this
     * object's database, as a list. <p>
     *
     * @param session The context in which to produce the list
     * @return the set of distinct, visible sessions connected
     *        to this object's database, as a list.
     */
    Session[] listVisibleSessions(Session session) {
        return database.sessionManager.getVisibleSessions(session);
    }

    /**
     * Retrieves whether this object is reporting catalog qualifiers.
     * @return true if this object is reporting catalog qualifiers, else false.
     */
    boolean isReportCatalogs() {
        return database.getProperties().isPropertyTrue("hsqldb.catalogs");
    }
}
