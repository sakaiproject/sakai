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

import org.hsqldb.lib.HashMappedList;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.SchemaManager.Schema;

// fredt@users 20020130 - patch 497872 by Nitin Chauhan - loop optimisation
// fredt@users 20020320 - doc 1.7.0 - update
// fredt@users 20021103 - patch 1.7.2 - allow for drop table, etc.
// fredt@users 20030613 - patch 1.7.2 - simplified data structures and reporting
// unsaved@users - patch 1.8.0 moved right managament to new classes

/**
 *
 * Manages the User objects for a Database instance.
 * The special users PUBLIC_USER_NAME and SYSTEM_AUTHORIZATION_NAME
 * are created and managed here.  SYSTEM_AUTHORIZATION_NAME is also
 * special in that the name is not kept in the user "list"
 * (PUBLIC_USER_NAME is kept in the list because it's needed by MetaData
 * routines via "listVisibleUsers(x, true)").
 *
 * Partly based on Hypersonic code.
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @author boucherb@users
 * @author fredt@users
 *
 * @version  1.8.0
 * @since  1.7.2
 * @see  User
 */
class UserManager implements GrantConstants {

    /**
     * We keep a link to the SYSTEM_AUTHORIZATION_NAME user because it is
     * the only User with no entry in the User map.
     */
    User sysUser = null;

    /**
     * This object's set of User objects. <p>
     *
     * Note: The special _SYSTEM  role
     * is not included in this list but the special PUBLIC
     * User object is kept in the list because it's needed by MetaData
     * routines via "listVisibleUsers(x, true)".
     */
    private HashMappedList userList;
    private GranteeManager granteeManager;

    /**
     * Construction happens once for each Database object.
     *
     * Creates special users PUBLIC_USER_NAME and SYSTEM_AUTHORIZATION_NAME.
     * Sets up association with the GranteeManager for this database.
     */
    UserManager(Database database) throws HsqlException {

        granteeManager = database.getGranteeManager();
        userList       = new HashMappedList();

        createUser(GranteeManager.PUBLIC_ROLE_NAME, null);

        sysUser = createUser(GranteeManager.SYSTEM_AUTHORIZATION_NAME, null);

        // Don't know whether to grant ADMIN to SYS directly, or to grant
        // role DBA.  The former seems safer as it doesn't depend on any role.
        //granteeManager.grant(SYSTEM_AUTHORIZATION_NAME, RoleManager.ADMIN_ROLE_NAME);
        sysUser.getGrantee().setAdminDirect();
    }

    /**
     * Creates a new User object under management of this object. <p>
     *
     *  A set of constraints regarding user creation is imposed: <p>
     *
     *  <OL>
     *    <LI>If the specified name is null, then an
     *        ASSERTION_FAILED exception is thrown stating that
     *        the name is null.
     *
     *    <LI>If this object's collection already contains an element whose
     *        name attribute equals the name argument, then
     *        a GRANTEE_ALREADY_EXISTS exception is thrown.
     *        (This will catch attempts to create Reserved grantee names).
     *  </OL>
     */
    User createUser(String name, String password) throws HsqlException {

        if (name == null) {
            Trace.doAssert(false, Trace.getMessage(Trace.NULL_NAME));
        }

        // TODO:
        // checkComplexity(password);
        // requires special: createSAUser(), createPublicUser()
        // boucherb@users 20020815 - disallow user-land creation of SYS user
        // -------------------------------------------------------
        // This will throw an appropriate Trace if grantee already exists,
        // regardless of whether the name is in any User, Role, etc. list.
        Grantee g = granteeManager.addGrantee(name);
        User    u = new User(name, password, g);

        // ONLY!! SYSTEM_AUTHORIZATION_NAME is not stored in our User list.
        if (GranteeManager.SYSTEM_AUTHORIZATION_NAME.equals(name)) {
            return u;
        }

        boolean success = userList.add(name, u);

        if (!success) {
            throw Trace.error(Trace.USER_ALREADY_EXISTS, name);
        }

        return u;
    }

    /**
     * Attempts to drop a User object with the specified name
     *  from this object's set. <p>
     *
     *  A successful drop action consists of: <p>
     *
     *  <UL>
     *
     *    <LI>removing the User object with the specified name
     *        from the set.
     *
     *    <LI>revoking all rights from the removed object<br>
     *        (this ensures that in case there are still references to the
     *        just dropped User object, those references
     *        cannot be used to erronously access database objects).
     *
     *  </UL> <p>
     *
     */
    void dropUser(String name) throws HsqlException {

        boolean reservedUser = GranteeManager.isReserved(name);

        Trace.check(!reservedUser, Trace.NONMOD_ACCOUNT, name);

        boolean result = granteeManager.removeGrantee(name);

        Trace.check(result, Trace.NO_SUCH_GRANTEE, name);

        User u = (User) userList.remove(name);

        Trace.check(u != null, Trace.USER_NOT_FOUND, name);
    }

    /**
     * Returns the User object with the specified name and
     * password from this object's set.
     */
    User getUser(String name, String password) throws HsqlException {

        if (name == null) {
            name = "";
        }

        if (password == null) {
            password = "";
        }

        // Don't have to worry about SYSTEM_AUTHORIZATION_NAME, since get()
        // will fail below (because it's not in the list).
        if (name.equals(GranteeManager.PUBLIC_ROLE_NAME)) {
            throw Trace.error(Trace.ACCESS_IS_DENIED);
        }

        name     = name.toUpperCase();
        password = password.toUpperCase();

        User u = get(name);

        u.checkPassword(password);

        return u;
    }

    /**
     * Retrieves this object's set of User objects as
     *  an HsqlArrayList. <p>
     */
    HashMappedList getUsers() {
        return userList;
    }

    boolean exists(String name) {
        return userList.get(name) == null ? false
                                          : true;
    }

    /**
     * Returns the User object identified by the
     * name argument. <p>
     */
    User get(String name) throws HsqlException {

        User u = (User) userList.get(name);

        if (u == null) {
            throw Trace.error(Trace.USER_NOT_FOUND, name);
        }

        return u;
    }

    /**
     * Retrieves the <code>User</code> objects representing the database
     * users that are visible to the <code>User</code> object
     * represented by the <code>session</code> argument. <p>
     *
     * If the <code>session</code> argument's <code>User</code> object
     * attribute has isAdmin() true (directly or by virtue of a Role),
     * then all of the
     * <code>User</code> objects in this collection are considered visible.
     * Otherwise, only this object's special <code>PUBLIC</code>
     * <code>User</code> object attribute and the session <code>User</code>
     * object, if it exists in this collection, are considered visible. <p>
     *
     * @param session The <code>Session</code> object used to determine
     *          visibility
     * @param andPublicUser whether to include the special <code>PUBLIC</code>
     *          <code>User</code> object in the retrieved list
     * @return a list of <code>User</code> objects visible to
     *          the <code>User</code> object contained by the
     *         <code>session</code> argument.
     *
     */
    HsqlArrayList listVisibleUsers(Session session, boolean andPublicUser) {

        HsqlArrayList list;
        User          user;
        boolean       isAdmin;
        String        sessName;
        String        userName;

        list     = new HsqlArrayList();
        isAdmin  = session.isAdmin();
        sessName = session.getUsername();

        if (userList == null || userList.size() == 0) {
            return list;
        }

        for (int i = 0; i < userList.size(); i++) {
            user = (User) userList.get(i);

            if (user == null) {
                continue;
            }

            userName = user.getName();

            if (GranteeManager.PUBLIC_ROLE_NAME.equals(userName)) {
                if (andPublicUser) {
                    list.add(user);
                }
            } else if (isAdmin) {
                list.add(user);
            } else if (sessName.equals(userName)) {
                list.add(user);
            }
        }

        return list;
    }

    // Legacy wrappers
    static String[] getRightsArray(int rights) {
        return GranteeManager.getRightsArray(rights);
    }

    /**
     * Removes all rights mappings for the database object identified by
     * the dbobject argument from all Grantee objects in the set.
     */
    void removeDbObject(Object dbobject) {
        granteeManager.removeDbObject(dbobject);
    }

    /**
     * Returns the specially constructed
     * <code>SYSTEM_AUTHORIZATION_NAME</code>
     * <code>User</code> object for the current <code>Database</code> object.
     *
     * @throws HsqlException - if the specified <code>Database</code>
     *          has no <code>SYS_AUTHORIZATION_NAME</code>
     *          <code>User</code> object.
     * @return the <code>SYS_AUTHORIZATION_NAME</code>
     *          <code>User</code> object
     *
     */
    User getSysUser() {
        return sysUser;
    }

    public synchronized void removeSchemaReference(Schema schema) {

        for (int i = 0; i < userList.size(); i++) {
            User user = (User) userList.get(i);

            if (user.getInitialSchema() == schema.name) {
                user.setInitialSchema(null);
            }
        }
    }
}
