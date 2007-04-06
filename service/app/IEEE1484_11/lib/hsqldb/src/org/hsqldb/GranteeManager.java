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

import org.hsqldb.lib.HashMappedList;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.IntKeyHashMap;
import org.hsqldb.lib.IntValueHashMap;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.StringUtil;
import org.hsqldb.lib.Collection;
import org.hsqldb.lib.Set;

/**
 * Contains a set of Grantee objects, and supports operations for creating,
 * finding, modifying and deleting Grantee objects for a Database; plus
 * Administrative privileges.
 *
 * @author boucherb@users
 * @author fredt@users
 * @author unsaved@users
 *
 * @version 1.8.0
 * @since 1.8.0
 * @see Grantee
 */
class GranteeManager implements GrantConstants {

    /**
     * The role name reserved for authorization of INFORMATION_SCHEMA and
     * system objects.
     */
    static final String SYSTEM_AUTHORIZATION_NAME = "_SYSTEM";

    /** The role name reserved for ADMIN users. */
    static final String DBA_ADMIN_ROLE_NAME = "DBA";

    /** The role name reserved for the special PUBLIC pseudo-user. */
    static final String PUBLIC_ROLE_NAME = "PUBLIC";

    /**
     * An empty list that is returned from
     * {@link #listTablePrivileges listTablePrivileges} when
     * it is detected that neither this <code>User</code> object or
     * its <code>PUBLIC</code> <code>User</code> object attribute have been
     * granted any rights on the <code>Table</code> object identified by
     * the specified <code>HsqlName</code> object.
     *
     */
    static final String[] emptyRightsList = new String[0];

    /**
     * MAP:  int => HsqlArrayList. <p>
     *
     * This map caches the lists of <code>String</code> objects naming the rights
     * corresponding to each valid set of rights flags, as returned by
     * {@link #listRightNames listRightNames}
     *
     */
    static final IntKeyHashMap hRightsLists = new IntKeyHashMap();

    /**
     * Used to provide access to the RoleManager for Grantee.isAccessible()
     * lookups
     */
    /*
     * Our map here has the same keys as the UserManager map
     * EXCEPT that we include all roles, including the SYSTEM_AUTHORIZATION_NAME
     * because we need o keep track of those permissions, but not his identity.
     * I.e., our list here is all-inclusive, whether the User or Role is
     * visible to database users or not.
     */

    /**
     * Map of String-to-Grantee-objects.<p>
     * Primary object maintained by this class
     */
    private HashMappedList map = new HashMappedList();

    /**
     * This object's set of Role objects. <p>
     * role-Strings-to-Grantee-object
     */
    private HashMappedList roleMap = new HashMappedList();

    /**
     * Construct the GranteeManager for a Database.
     *
     * Construct special Grantee objects for PUBLIC and SYS, and add them
     * to the Grantee map.
     * We depend on the corresponding User accounts being created
     * independently so as to remove a dependency to the UserManager class.
     *
     * @param inDatabase Only needed to link to the RoleManager later on.
     */
    public GranteeManager(Database inDatabase) throws HsqlException {
        addRole(GranteeManager.DBA_ADMIN_ROLE_NAME);
        getRole(GranteeManager.DBA_ADMIN_ROLE_NAME).setAdminDirect();
    }

    static final IntValueHashMap rightsStringLookup = new IntValueHashMap(7);

    static {
        rightsStringLookup.put(S_R_ALL, ALL);
        rightsStringLookup.put(S_R_SELECT, SELECT);
        rightsStringLookup.put(S_R_UPDATE, UPDATE);
        rightsStringLookup.put(S_R_DELETE, DELETE);
        rightsStringLookup.put(S_R_INSERT, INSERT);
    }

    /**
     * Grants the rights represented by the rights argument on
     * the database object identified by the dbobject argument
     * to the Grantee object identified by name argument.<p>
     *
     *  Note: For the dbobject argument, Java Class objects are identified
     *  using a String object whose value is the fully qualified name
     *  of the Class, while Table and other objects are
     *  identified by an HsqlName object.  A Table
     *  object identifier must be precisely the one obtained by calling
     *  table.getName(); if a different HsqlName
     *  object with an identical name attribute is specified, then
     *  rights checks and tests will fail, since the HsqlName
     *  class implements its {@link HsqlName#hashCode hashCode} and
     *  {@link HsqlName#equals equals} methods based on pure object
     *  identity, rather than on attribute values. <p>
     */
    void grant(String name, Object dbobject,
               int rights) throws HsqlException {

        Grantee g = get(name);

        if (g == null) {
            throw Trace.error(Trace.NO_SUCH_GRANTEE, name);
        }

        if (isImmutable(name)) {
            throw Trace.error(Trace.NONMOD_GRANTEE, name);
        }

        g.grant(dbobject, rights);
        g.updateAllRights();

        if (g.isRole) {
            updateAllRights(g);
        }
    }

    /**
     * Grant a role to this Grantee.
     */
    void grant(String name, String role) throws HsqlException {

        Grantee grantee = get(name);

        if (grantee == null) {
            throw Trace.error(Trace.NO_SUCH_GRANTEE, name);
        }

        if (isImmutable(name)) {
            throw Trace.error(Trace.NONMOD_GRANTEE, name);
        }

        Grantee r = get(role);

        if (r == null) {
            throw Trace.error(Trace.NO_SUCH_ROLE, role);
        }

        if (role.equals(name)) {
            throw Trace.error(Trace.CIRCULAR_GRANT, name);
        }

        // boucherb@users 20050515
        // SQL 2003 Foundation, 4.34.3
        // No cycles of role grants are allowed.
        if (r.hasRole(name)) {

            // boucherb@users
            // TODO: Correct reporting of actual grant path
            throw Trace.error(Trace.CIRCULAR_GRANT,
                              Trace.getMessage(Trace.ALREADY_HAVE_ROLE)
                              + " GRANT " + name + " TO " + role);
        }

        if (grantee.getDirectRoles().contains(role)) {
            throw Trace.error(Trace.ALREADY_HAVE_ROLE, role);
        }

        grantee.grant(role);
        grantee.updateAllRights();

        if (grantee.isRole) {
            updateAllRights(grantee);
        }
    }

    /**
     * Revoke a role from a Grantee
     */
    void revoke(String name, String role) throws HsqlException {

        Grantee g = get(name);

        if (g == null) {
            throw Trace.error(Trace.NO_SUCH_GRANTEE, name);
        }

        g.revoke(role);
        g.updateAllRights();

        if (g.isRole) {
            updateAllRights(g);
        }
    }

    /**
     * Revokes the rights represented by the rights argument on
     * the database object identified by the dbobject argument
     * from the User object identified by the name
     * argument.<p>
     * @see #grant
     */
    void revoke(String name, Object dbobject,
                int rights) throws HsqlException {

        Grantee g = get(name);

        g.revoke(dbobject, rights);
        g.updateAllRights();

        if (g.isRole) {
            updateAllRights(g);
        }
    }

    /**
     * Removes a role without any privileges from all grantees
     */
    void removeEmptyRole(Grantee role) {

        String name = role.getName();

        for (int i = 0; i < map.size(); i++) {
            Grantee grantee = (Grantee) map.get(i);

            grantee.roles.remove(name);
        }
    }

    /**
     * Removes all rights mappings for the database object identified by
     * the dbobject argument from all Grantee objects in the set.
     */
    void removeDbObject(Object dbobject) {

        for (int i = 0; i < map.size(); i++) {
            Grantee g = (Grantee) map.get(i);

            g.revokeDbObject(dbobject);
        }
    }

    /**
     * First updates all ROLE Grantee objects. Then updates all USER Grantee
     * Objects.
     */
    void updateAllRights(Grantee role) {

        String name = role.getName();

        for (int i = 0; i < map.size(); i++) {
            Grantee grantee = (Grantee) map.get(i);

            if (grantee.isRole) {
                grantee.updateNestedRoles(name);
            }
        }

        for (int i = 0; i < map.size(); i++) {
            Grantee grantee = (Grantee) map.get(i);

            if (!grantee.isRole) {
                grantee.updateAllRights();
            }
        }
    }

    /**
     */
    public boolean removeGrantee(String name) {

        /*
         * Explicitly can't remove PUBLIC_USER_NAME and system grantees.
         */
        if (isReserved(name)) {
            return false;
        }

        Grantee g = (Grantee) map.remove(name);

        if (g == null) {
            return false;
        }

        g.clearPrivileges();
        updateAllRights(g);

        if (g.isRole) {
            roleMap.remove(name);
            removeEmptyRole(g);
        }

        return true;
    }

    /**
     * We don't have to worry about anything manually creating a reserved
     * account, because the reserved accounts are created upon DB
     * initialization.  If somebody tries to create one of these accounts
     * after that, it will fail because the account will already exist.
     * (We do prevent them from being removed, elsewhere!)
     */
    public Grantee addGrantee(String name) throws HsqlException {

        if (map.containsKey(name)) {
            throw Trace.error(Trace.GRANTEE_ALREADY_EXISTS, name);
        }

        Grantee pubGrantee = null;

        if (!isReserved(name)) {
            pubGrantee = get(PUBLIC_ROLE_NAME);

            if (pubGrantee == null) {
                Trace.doAssert(
                    false, Trace.getMessage(Trace.MISSING_PUBLIC_GRANTEE));
            }
        }

        Grantee g = new Grantee(name, pubGrantee, this);

        map.put(name, g);

        return g;
    }

    /**
     * Returns true if named Grantee object exists.
     * This will return true for reserved Grantees
     * SYSTEM_AUTHORIZATION_NAME, ADMIN_ROLE_NAME, PUBLIC_USER_NAME.
     */
    boolean isGrantee(String name) {
        return (map.containsKey(name));
    }

    static int getCheckRight(String right) throws HsqlException {

        int r = getRight(right);

        if (r != 0) {
            return r;
        }

        throw Trace.error(Trace.NO_SUCH_RIGHT, right);
    }

    /**
     * Translate a string representation or right(s) into its numeric form.
     */
    static int getRight(String right) {
        return rightsStringLookup.get(right, 0);
    }

    /**
     * Returns a comma separated list of right names corresponding to the
     * right flags set in the right argument. <p>
     */
    static String getRightsList(int rights) {

//        checkValidFlags(right);
        if (rights == 0) {
            return null;
        }

        if (rights == ALL) {
            return S_R_ALL;
        }

        return StringUtil.getList(getRightsArray(rights), ",", "");
    }

    /**
     * Retrieves the list of right names represented by the right flags
     * set in the specified <code>Integer</code> object's <code>int</code>
     * value. <p>
     *
     * @param rights An Integer representing a set of right flags
     * @return an empty list if the specified <code>Integer</code> object is
     *        null, else a list of rights, as <code>String</code> objects,
     *        represented by the rights flag bits set in the specified
     *        <code>Integer</code> object's int value.
     *
     */
    static String[] getRightsArray(int rights) {

        if (rights == 0) {
            return emptyRightsList;
        }

        String[] list = (String[]) hRightsLists.get(rights);

        if (list != null) {
            return list;
        }

        list = getRightsArraySub(rights);

        hRightsLists.put(rights, list);

        return list;
    }

    private static String[] getRightsArraySub(int right) {

//        checkValidFlags(right);
        if (right == 0) {
            return emptyRightsList;
        }

        HsqlArrayList a  = new HsqlArrayList();
        Iterator      it = rightsStringLookup.keySet().iterator();

        for (; it.hasNext(); ) {
            String rightString = (String) it.next();

            if (rightString.equals(S_R_ALL)) {
                continue;
            }

            int i = rightsStringLookup.get(rightString, 0);

            if ((right & i) != 0) {
                a.add(rightString);
            }
        }

        return (String[]) a.toArray(new String[a.size()]);
    }

    /**
     * Retrieves the set of distinct, fully qualified Java <code>Class</code>
     * names upon which any grants currently exist to elements in
     * this collection. <p>
     * @return the set of distinct, fully qualified Java Class names, as
     *        <code>String</code> objects, upon which grants currently exist
     *        to the elements of this collection
     *
     */
    HashSet getGrantedClassNames() throws HsqlException {

        int      size;
        Grantee  grantee;
        HashSet  out;
        Iterator e;

        size = map.size();
        out  = new HashSet();

        for (int i = 0; i < size; i++) {
            grantee = (Grantee) map.get(i);

            if (grantee == null) {
                continue;
            }

            e = grantee.getGrantedClassNames(false).iterator();

            while (e.hasNext()) {
                out.add(e.next());
            }
        }

        return out;
    }

    public Grantee get(String name) {
        return (Grantee) map.get(name);
    }

    public Collection getGrantees() {
        return map.values();
    }

    public static boolean validRightString(String rightString) {
        return getRight(rightString) != 0;
    }

    public static boolean isImmutable(String name) {
        return name.equals(SYSTEM_AUTHORIZATION_NAME)
               || name.equals(DBA_ADMIN_ROLE_NAME);
    }

    public static boolean isReserved(String name) {

        return name.equals(SYSTEM_AUTHORIZATION_NAME)
               || name.equals(DBA_ADMIN_ROLE_NAME)
               || name.equals(PUBLIC_ROLE_NAME);
    }

    /**
     * Creates a new Role object under management of this object. <p>
     *
     *  A set of constraints regarding user creation is imposed: <p>
     *
     *  <OL>
     *    <LI>Can't create a role with name same as any right.
     *
     *    <LI>If the specified name is null, then an
     *        ASSERTION_FAILED exception is thrown stating that
     *        the name is null.
     *
     *    <LI>If this object's collection already contains an element whose
     *        name attribute equals the name argument, then
     *        a GRANTEE_ALREADY_EXISTS or ROLE_ALREADY_EXISTS Trace
     *        is thrown.
     *        (This will catch attempts to create Reserved grantee names).
     *  </OL>
     */
    String addRole(String name) throws HsqlException {

        /*
         * Role names can't be right names because that would cause
         * conflicts with "GRANT name TO...".  This doesn't apply to
         * User names or Grantee names in general, since you can't
         * "GRANT username TO...".  That's why this check is only here.
         */
        if (name == null) {
            Trace.doAssert(false, Trace.getMessage(Trace.NULL_NAME));
        }

        Grantee g = null;

        if (GranteeManager.validRightString(name)) {
            throw Trace.error(Trace.ILLEGAL_ROLE_NAME, name);
        }

        g        = addGrantee(name);
        g.isRole = true;

        boolean result = roleMap.add(name, g);

        if (!result) {
            throw Trace.error(Trace.ROLE_ALREADY_EXISTS, name);
        }

        // I don't think can get this trace since every roleMap element
        // will have a Grantee element which was already verified
        // above.  Easier to leave this check here than research it.
        return name;
    }

    /**
     * Attempts to drop a Role with the specified name
     *  from this object's set. <p>
     *
     *  A successful drop action consists of: <p>
     *
     *  <UL>
     *
     *    <LI>removing the Grantee object with the specified name
     *        from the set.
     *
     *    <LI>revoking all rights from the removed object<br>
     *        (this ensures that in case there are still references to the
     *        just dropped Grantee object, those references
     *        cannot be used to erronously access database objects).
     *
     *  </UL> <p>
     *
     */
    void dropRole(String name) throws HsqlException {

        if (name.equals(GranteeManager.DBA_ADMIN_ROLE_NAME)) {
            throw Trace.error(Trace.ACCESS_IS_DENIED);
        }

        if (!isRole(name)) {
            throw Trace.error(Trace.NO_SUCH_ROLE, name);
        }

        removeGrantee(name);
        roleMap.remove(name);
    }

    public Set getRoleNames() {
        return roleMap.keySet();
    }

    /**
     * Returns Grantee for the named Role
     */
    Grantee getRole(String name) throws HsqlException {

        if (!isRole(name)) {
            Trace.doAssert(false, "No role '" + name + "'");
        }

        Grantee g = (Grantee) roleMap.get(name);

        if (g == null) {
            throw Trace.error(Trace.MISSING_GRANTEE, name);
        }

        return g;
    }

    boolean isRole(String name) throws HsqlException {
        return roleMap.containsKey(name);
    }
}
