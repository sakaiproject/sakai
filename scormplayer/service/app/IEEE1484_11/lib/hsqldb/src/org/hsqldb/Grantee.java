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
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.IntValueHashMap;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.Set;

/**
 * A Grantee Object holds the name, access and administrative rights for a
 * particular grantee.<p>
 * It supplies the methods used to grant, revoke, test
 * and check a grantee's access rights to other database objects.
 * It also holds a reference to the common PUBLIC User Object,
 * which represent the special user refered to in
 * GRANT ... TO PUBLIC statements.<p>
 * The check(), isAccessible() and getGrantedClassNames() methods check the
 * rights granted to the PUBLIC User Object, in addition to individually
 * granted rights, in order to decide which rights exist for the user.
 *
 * Method names ending in Direct indicate methods which do not recurse
 * to look through Roles which "this" object is a member of.
 *
 * We use the word "Admin" (e.g., in private variable "admin" and method
 * "isAdmin()) to mean this Grantee has admin priv by any means.
 * We use the word "adminDirect" (e.g., in private variable "adminDirect"
 * and method "isAdminDirect()) to mean this Grantee has admin priv
 * directly.
 *
 * @author boucherb@users
 * @author fredt@usrs
 * @author unsaved@users
 *
 * @version 1.8.0
 * @since 1.8.0
 */
public class Grantee {

    boolean isRole;

    /**
     * true if this grantee has database administrator priv directly
     *  (ie., not by membership in any role)
     */
    private boolean isAdminDirect = false;

    /** true if this grantee has database administrator priv by any means. */
    private boolean isAdmin = false;

    /** contains righs granted direct, or via roles, expept those of PUBLIC */
    private IntValueHashMap fullRightsMap = new IntValueHashMap();

    /**
     * Grantee name.
     */
    private String granteeName;

    /** map with database object identifier keys and access privileges values */
    private IntValueHashMap rightsMap;

    /** These are the DIRECT roles.  Each of these may contain nested roles */
    HashSet roles = new HashSet();

    /**
     * The special PUBLIC Grantee object. <p>
     *
     * Note: All Grantee objects except the special
     * SYS and PUBLIC Grantee objects contain a reference to this object
     */
    private Grantee pubGrantee;

    /** Needed only to give access to the roles for this database */
    private GranteeManager granteeManager;

    /**
     * Constructor, with a argument reference to the PUBLIC User Object which
     * is null if this is the SYS or PUBLIC user.
     *
     * The dependency upon a GranteeManager is undesirable.  Hopefully we
     * can get rid of this dependency with an IOC or Listener re-design.
     */
    Grantee(String name, Grantee inGrantee,
            GranteeManager man) throws HsqlException {

        rightsMap      = new IntValueHashMap();
        granteeName    = name;
        granteeManager = man;
        pubGrantee     = inGrantee;
    }

    String getName() {
        return granteeName;
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

        // necessary to create the script
        return rightsMap;
    }

    /**
     * Grant a role
     */
    public void grant(String role) throws HsqlException {
        roles.add(role);
    }

    /**
     * Revoke a direct role only
     */
    public void revoke(String role) throws HsqlException {

        if (!hasRoleDirect(role)) {
            throw Trace.error(Trace.DONT_HAVE_ROLE, role);
        }

        roles.remove(role);
    }

    /**
     * Gets direct roles, not roles nested within them.
     */
    public HashSet getDirectRoles() {
        return roles;
    }

    String getDirectRolesString() {
        return setToString(roles);
    }

    String getAllRolesString() {
        return setToString(getAllRoles());
    }

    public String setToString(Set set) {

        // Should be sorted
        // Iterator it = (new java.util.TreeSet(roles)).iterator();
        Iterator     it = set.iterator();
        StringBuffer sb = new StringBuffer();

        while (it.hasNext()) {
            if (sb.length() > 0) {
                sb.append(',');
            }

            sb.append(it.next());
        }

        return sb.toString();
    }

    /**
     * Gets direct and nested roles.
     */
    public HashSet getAllRoles() {

        HashSet newSet = new HashSet();

        addGranteeAndRoles(newSet);

        // Since we added "Grantee" in addition to Roles, need to remove self.
        newSet.remove(granteeName);

        return newSet;
    }

    /**
     * Adds to given Set this.sName plus all roles and nested roles.
     *
     * @return Given role with new elements added.
     */
    private HashSet addGranteeAndRoles(HashSet set) {

        String candidateRole;

        set.add(granteeName);

        Iterator it = roles.iterator();

        while (it.hasNext()) {
            candidateRole = (String) it.next();

            if (!set.contains(candidateRole)) {
                try {
                    granteeManager.getRole(candidateRole).addGranteeAndRoles(
                        set);
                } catch (HsqlException he) {
                    throw new RuntimeException(he.getMessage());
                }
            }
        }

        return set;
    }

    public boolean hasRoleDirect(String role) {
        return roles.contains(role);
    }

    public boolean hasRole(String role) {
        return getAllRoles().contains(role);
    }

    public String allRolesString() {

        HashSet allRoles = getAllRoles();

        if (allRoles.size() < 1) {
            return null;
        }

        Iterator     it = getAllRoles().iterator();
        StringBuffer sb = new StringBuffer();

        while (it.hasNext()) {
            if (sb.length() > 0) {
                sb.append(',');
            }

            sb.append((String) it.next());
        }

        return sb.toString();
    }

    /**
     * Grants the specified rights on the specified database object. <p>
     *
     * Keys stored in rightsMap for database tables are their HsqlName
     * attribute. This allows rights to persist when a table is renamed. <p>
     */
    void grant(Object dbobject, int rights) {

        if (rights == 0) {
            return;
        }

        int n = rightsMap.get(dbobject, 0);

        n |= rights;

        rightsMap.put(dbobject, n);
    }

    /**
     * Revokes the specified rights on the specified database object. <p>
     *
     * If, after removing the specified rights, no rights remain on the
     * database object, then the key/value pair for that object is removed
     * from the rights map
     */
    void revoke(Object dbobject, int rights) {

        if (rights == 0) {
            return;
        }

        int n = rightsMap.get(dbobject, 0);

        if (n == 0) {
            return;
        }

        rights = n & (GranteeManager.ALL - rights);

        if (rights == 0) {
            rightsMap.remove(dbobject);
        } else {
            rightsMap.put(dbobject, rights);
        }
    }

    /**
     * Revokes all rights on the specified database object.<p>
     *
     * This method removes any existing mapping from the rights map
     */
    void revokeDbObject(Object dbobject) {
        rightsMap.remove(dbobject);
        fullRightsMap.remove(dbobject);
    }

    /**
     * Revokes all rights from this Grantee object.  The map is cleared and
     * the database administrator role attribute is set false.
     */
    void clearPrivileges() {

        roles.clear();
        rightsMap.clear();
        fullRightsMap.clear();

        isAdminDirect = false;
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

        if (!isAccessible(dbobject, rights)) {
            throw Trace.error(Trace.ACCESS_IS_DENIED);
        }
    }

    void check(String dbobject) throws HsqlException {

        if (!isAccessible(dbobject)) {
            throw Trace.error(Trace.ACCESS_IS_DENIED);
        }
    }

    /**
     * Returns true if any of the rights represented by the
     * rights argument has been granted on the database object identified
     * by the dbobject argument. <p>
     *
     * This is done by checking that a mapping exists in the rights map
     * from the dbobject argument for at least one of the rights
     * contained in the rights argument.
     *
     * Only does one level of recursion to check the PUBLIC role.
     */
    boolean isAccessible(HsqlName dbObject, int rights) throws HsqlException {

        if (isAdmin) {
            return true;
        }

        if (pubGrantee != null && pubGrantee.isAccessible(dbObject, rights)) {
            return true;
        }

        int n = fullRightsMap.get(dbObject, 0);

        if (n != 0) {
            return (n & rights) != 0;
        }

        return false;
    }

    /**
     * Returns true if any right at all has been granted to this User object
     * on the database object identified by the dbObject argument.
     */
    boolean isAccessible(String functionName) throws HsqlException {

        if (functionName.startsWith("org.hsqldb.Library")
                || functionName.startsWith("java.lang.Math")) {
            return true;
        }

        if (isAdmin) {
            return true;
        }

        if (pubGrantee != null && pubGrantee.isAccessible(functionName)) {
            return true;
        }

        int n = fullRightsMap.get(functionName, 0);

        return n != 0;
    }

    /**
     * Returns true if any of the rights represented by the
     * rights argument has been granted on the database object identified
     * by the dbObject argument. <p>
     *
     * This is done by checking that a mapping exists in the rights map
     * from the dbObject argument for at least one of the rights
     * contained in the rights argument.
     *
     * Considers none of pubGranee, nested roles, admin privs, globally
     * available Class object.
     */
    protected boolean isDirectlyAccessible(Object dbObject,
                                           int rights) throws HsqlException {

        int n = rightsMap.get(dbObject, 0);

        if (n != 0) {
            return (n & rights) != 0;
        }

        return false;
    }

    /**
     * Returns true if any right at all has been granted to this User object
     * on the database object identified by the dbObject argument.
     */
    boolean isAccessible(HsqlName dbObject) throws HsqlException {
        return isAccessible(dbObject, GranteeManager.ALL);
    }

    /**
     * Checks whether this Grantee has administrative privs either directly
     * or indirectly. Otherwise it throws.
     */
    void checkAdmin() throws HsqlException {

        if (!isAdmin()) {
            throw Trace.error(Trace.ACCESS_IS_DENIED);
        }
    }

    /**
     * Returns true if this Grantee has administrative privs either directly
     * or indirectly.
     */
    boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Returns true if this grantee object is for a user with Direct
     * database administrator privileges.
     * I.e., if this User/Role has Admin priv. directly, not via a
     * nested Role.
     */
    boolean isAdminDirect() {
        return isAdminDirect;
    }

    /**
     * Retrieves the distinct set of Java <code>Class</code> FQNs
     * for which this <code>User</code> object has been
     * granted <code>ALL</code> (the Class execution privilege). <p>
     * @param andToPublic if <code>true</code>, then the set includes the
     *        names of classes accessible to this <code>User</code> object
     *        through grants to its Roles + <code>PUBLIC</code>
     *        <code>User</code> object attribute, else only role grants
     *        + direct grants are included.
     * @return the distinct set of Java Class FQNs for which this
     *        this <code>User</code> object has been granted
     *        <code>ALL</code>.
     */
    HashSet getGrantedClassNames(boolean andToPublic) throws HsqlException {

        IntValueHashMap rights;
        Object          key;
        int             right;
        Iterator        i;

        rights = rightsMap;

        HashSet out = getGrantedClassNamesDirect();

        if (andToPublic && pubGrantee != null) {
            rights = pubGrantee.rightsMap;
            i      = rights.keySet().iterator();

            while (i.hasNext()) {
                key = i.next();

                if (key instanceof String) {
                    right = rights.get(key, 0);

                    if (right == GranteeManager.ALL) {
                        out.add(key);
                    }
                }
            }
        }

        Iterator it = getAllRoles().iterator();

        while (it.hasNext()) {
            out.addAll(
                ((Grantee) granteeManager.getRole(
                    (String) it.next())).getGrantedClassNamesDirect());
        }

        return out;
    }

    /**
     * Retrieves the distinct set of Java <code>Class</code> FQNs
     * for which this <code>User</code> object has directly been
     * granted <code>ALL</code> (the Class execution privilege).
     *
     * Does NOT check nested the pubGrantee nor nested roles.
     * @return the distinct set of Java Class FQNs for which this
     *        this <code>User</code> object has been granted
     *        <code>ALL</code>.
     *
     */
    HashSet getGrantedClassNamesDirect() throws HsqlException {

        IntValueHashMap rights;
        HashSet         out;
        Object          key;
        int             right;
        Iterator        i;

        rights = rightsMap;
        out    = new HashSet();
        i      = rightsMap.keySet().iterator();

        while (i.hasNext()) {
            key = i.next();

            if (key instanceof String) {
                right = rights.get(key, 0);

                if (right == GranteeManager.ALL) {
                    out.add(key);
                }
            }
        }

        return out;
    }

    /**
     * Retrieves a string[] whose elements are the names of the rights
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
        return GranteeManager.getRightsArray(rightsMap.get(name, 0));
    }

    /**
     * Violates naming convention (for backward compatibility).
     * Should be "setAdminDirect(boolean").
     */
    void setAdminDirect() {
        isAdmin = isAdminDirect = true;
    }

    /**
     * Recursive method used with ROLE Grantee objects to set the fullRightsMap
     * and admin flag for all the roles.
     *
     * If a new ROLE is granted to a ROLE Grantee object, the ROLE should first
     * be added to the Set of ROLE Grantee objects (roles) for the grantee.
     * The grantee will be the parameter.
     *
     * If the direct permissions granted to an existing ROLE Grentee is
     * modified no extra initial action is necessary.
     * The existing Grantee will b the parameter.
     *
     * If an existing ROLE is REVOKEed from a ROLE, it should first be removed
     * from the set of ROLE Grantee objects in the containing ROLE.
     * The containing ROLE will be the parameter.
     *
     * If an existing ROLE is DROPped, all its privileges should be cleared
     * first. The ROLE will be the parameter. After calling this method on
     * all other roles, the DROPped role should be removed from all grantees.
     *
     * After the initial modification, this method should be called iteratively
     * on all the ROLE Grantee objects contained in RoleManager.
     *
     * The updateAllRights() method is then called iteratively on all the
     * USER Grantee objects contained in UserManager.
     * @param role a modified, revoked or dropped role.
     * @return true if this Grantee has possibly changed as a result
     */
    boolean updateNestedRoles(String role) {

        boolean hasNested = false;
        boolean isSelf    = role.equals(granteeName);

        if (!isSelf) {
            Iterator it = roles.iterator();

            while (it.hasNext()) {
                String roleName = (String) it.next();

                try {
                    Grantee currentRole = granteeManager.getRole(roleName);

                    hasNested |= currentRole.updateNestedRoles(role);
                } catch (HsqlException e) {}
            }
        }

        if (hasNested) {
            updateAllRights();
        }

        return hasNested || isSelf;
    }

    /**
     * Method used with all Grantee objects to set the full set of rights
     * according to those inherited form ROLE Grantee objects and those
     * granted to the object itself.
     */
    void updateAllRights() {

        fullRightsMap.clear();

        isAdmin = isAdminDirect;

        Iterator it = roles.iterator();

        while (it.hasNext()) {
            String roleName = (String) it.next();

            try {
                Grantee currentRole = granteeManager.getRole(roleName);

                fullRightsMap.putAll(currentRole.fullRightsMap);

                isAdmin |= currentRole.isAdmin();
            } catch (HsqlException e) {}
        }

        fullRightsMap.putAll(rightsMap);
    }
}
