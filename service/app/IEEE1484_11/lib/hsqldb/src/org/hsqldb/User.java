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
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.IntValueHashMap;

// fredt@users 20021103 - patch 1.7.2 - fix bug in revokeAll()
// fredt@users 20021103 - patch 1.7.2 - allow for drop table, etc.
// when tables are dropped or renamed, changes are reflected in the
// permissions held in User objects.
// boucherb@users 200208-200212 - doc 1.7.2 - update
// boucherb@users 200208-200212 - patch 1.7.2 - metadata
// unsaved@users - patch 1.8.0 moved right managament to new classes

/**
 * A User Object holds the name, password for a
 * particular database user.<p>
 *
 * Enhanced in successive versions of HSQLDB.
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.8.0
 * @since Hypersonic SQL
 */
public class User {

    /** true if this user is the sys user. */
    private boolean isSys;

    /** true if this user is the public user. */
    private boolean isPublic;

    /** user name. */
    private String sName;

    /** password. */
    private String sPassword;

    /** default schema when new Sessions started (defaults to PUBLIC schema) */
    private HsqlName initialSchema = null;

    /** grantee object. */
    private Grantee grantee;

    /**
     * Constructor
     */
    User(String name, String password,
            Grantee inGrantee) throws HsqlException {

        sName   = name;
        grantee = inGrantee;

        boolean granteeOk = grantee != null
                            || GranteeManager.isReserved(name);

        if (!granteeOk) {
            Trace.doAssert(false,
                           Trace.getMessage(Trace.MISSING_GRANTEE) + ": "
                           + name);
        }

        setPassword(password);

        isSys    = name.equals(GranteeManager.SYSTEM_AUTHORIZATION_NAME);
        isPublic = name.equals(GranteeManager.PUBLIC_ROLE_NAME);
    }

    String getName() {
        return sName;
    }

    void setPassword(String password) throws HsqlException {

        // TODO:
        // checkComplexity(password);
        // requires: UserManager.createSAUser(), UserManager.createPublicUser()
        sPassword = password;
    }

    /**
     * Checks if this object's password attibute equals
     * specified argument, else throws.
     */
    void checkPassword(String test) throws HsqlException {
        Trace.check(test.equals(sPassword), Trace.ACCESS_IS_DENIED);
    }

    /**
     * Returns true if this User object is for a user with the
     * database administrator role.
     */
    boolean isSys() {
        return isSys;
    }

    /**
     * Returns the initial schema for the user
     */
    HsqlName getInitialSchema() {
        return initialSchema;
    }

    /**
     * This class does not have access to the SchemaManager, therefore
     * caller should verify that the given schemaName exists.
     *
     * @param schemaName Name of an existing schema.  Null value allowed,
     *                   which means use the DB default session schema.
     */
    void setInitialSchema(HsqlName schema) {
        initialSchema = schema;
    }

    /**
     * Returns true if this User object represents the PUBLIC user
     */
    boolean isPublic() {
        return isPublic;
    }

    /**
     * Returns the ALTER USER DDL character sequence that preserves the
     * this user's current password value and mode. <p>
     *
     * @return  the DDL
     */
    String getAlterUserDDL() {

        StringBuffer sb = new StringBuffer();

        sb.append(Token.T_ALTER).append(' ');
        sb.append(Token.T_USER).append(' ');
        sb.append(sName).append(' ');
        sb.append(Token.T_SET).append(' ');
        sb.append(Token.T_PASSWORD).append(' ');
        sb.append('"').append(sPassword).append('"');

        return sb.toString();
    }

    /**
     * returns the DDL string
     * sequence that creates this user.
     *
     */
    String getCreateUserDDL() {

        StringBuffer sb = new StringBuffer(64);

        sb.append(Token.T_CREATE).append(' ');
        sb.append(Token.T_USER).append(' ');
        sb.append(sName).append(' ');
        sb.append(Token.T_PASSWORD).append(' ');
        sb.append('"').append(sPassword).append('"');

        return sb.toString();
    }

    /**
     * Retrieves the redo log character sequence for connecting
     * this user
     *
     * @return the redo log character sequence for connecting
     *      this user
     */
    public String getConnectStatement() {

        StringBuffer sb = new StringBuffer();

        sb.append(Token.T_CONNECT).append(' ');
        sb.append(Token.T_USER).append(' ');
        sb.append(sName);

        return sb.toString();
    }

    /**
     * Retrieves the Grantee object for this User.
     */
    Grantee getGrantee() {
        return grantee;
    }

    /**
     * Sets the Grantee object for this User.
     * This is done in the constructor for all users except the special
     * users SYSTEM and PUBLIC, which have to be set up before the
     * Managers are initialized.
     */
    void setGrantee(Grantee inGrantee) throws HsqlException {

        if (grantee != null) {
            Trace.doAssert(false,
                           Trace.getMessage(Trace.CHANGE_GRANTEE) + ": "
                           + sName);
        }

        grantee = inGrantee;
    }

    // Legacy wrappers

    /**
     * Returns true if this User object is for a user with the
     * database administrator role.
     */
    boolean isAdmin() {
        return grantee.isAdmin();
    }

    /**
     * Retrieves a string[] whose elements are the names, of the rights
     * explicitly granted with the GRANT command to this <code>User</code>
     * object on the <code>Table</code> object identified by the
     * <code>name</code> argument.
     * * @return array of Strings naming the rights granted to this
     *        <code>User</code> object on the <code>Table</code> object
     *        identified by the <code>name</code> argument.
     * @param name a <code>Table</code> object identifier
     *
     */
    String[] listGrantedTablePrivileges(HsqlName name) {
        return grantee.listGrantedTablePrivileges(name);
    }

    /**
     * Retrieves the distinct set of Java <code>Class</code> FQNs
     * for which this <code>User</code> object has been
     * granted <code>ALL</code> (the Class execution privilege). <p>
     * @param andToPublic if <code>true</code>, then the set includes the
     *        names of classes accessible to this <code>User</code> object
     *        through grants to its <code>PUBLIC</code> <code>User</code>
     *        object attribute, else only direct grants are inlcuded.
     * @return the distinct set of Java Class FQNs for which this
     *        this <code>User</code> object has been granted
     *        <code>ALL</code>.
     *
     */
    HashSet getGrantedClassNames(boolean andToPublic) throws HsqlException {
        return grantee.getGrantedClassNames(andToPublic);
    }

    /**
     * Retrieves the map object that represents the rights that have been
     * granted on database objects.  <p>
     *
     * The map has keys and values with the following interpretation: <P>
     *
     * <UL>
     * <LI> The keys are generally (but not limited to) objects having
     *      an attribute or value equal to the name of an actual database
     *      object.
     *
     * <LI> Specifically, the keys act as database object identifiers.
     *
     * <LI> The values are always Integer objects, each formed by combining
     *      a set of flags, one for each of the access rights defined in
     *      UserManager: {SELECT, INSERT, UPDATE and DELETE}.
     * </UL>
     */
    IntValueHashMap getRights() {
        return grantee.getRights();
    }

    /**
     * Checks that this User object is for a user with the
     * database administrator role. Otherwise it throws.
     */
    void checkAdmin() throws HsqlException {
        grantee.checkAdmin();
    }

    /**
     * Checks if any of the rights represented by the rights
     * argument have been granted on the specified database object. <p>
     *
     * This is done by checking that a mapping exists in the rights map
     * from the dbobject argument for at least one of the rights
     * contained in the rights argument. Otherwise, it throws.
     */
    void check(HsqlName dbobject, int rights) throws HsqlException {
        grantee.check(dbobject, rights);
    }

    void check(String dbobject) throws HsqlException {
        grantee.check(dbobject);
    }

    /**
     * Returns true if any of the rights represented by the
     * rights argument has been granted on the database object identified
     * by the dbobject argument. <p>
     *
     * This is done by checking that a mapping exists in the rights map
     * from the dbobject argument for at least one of the rights
     * contained in the rights argument.
     */
    boolean isAccessible(HsqlName dbobject, int rights) throws HsqlException {
        return grantee.isAccessible(dbobject, rights);
    }

    /**
     * Returns true if any right at all has been granted to this User object
     * on the database object identified by the dbobject argument.
     */
    boolean isAccessible(String dbobject) throws HsqlException {
        return grantee.isAccessible(dbobject);
    }

    boolean isAccessible(HsqlName dbobject) throws HsqlException {
        return grantee.isAccessible(dbobject);
    }
}
