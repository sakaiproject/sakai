package org.sakaiproject.sitemembers;

/**
 * Represents the roles used in the site. Users are categorised to one of these.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public enum SiteRole {

    // Note that these strings are actually the *permission* that determines whether the user
    // has the given role.  Not all schools call these roles "student", "ta", "instructor", but
    // the underlying permission cannot be changed
    STUDENT("section.role.student"),
    TA("section.role.ta"),
    INSTRUCTOR("section.role.instructor");

    private String permissionName;

    SiteRole(final String permissionName) {
        this.permissionName = permissionName;
    }

    /**
     * Get the Sakai permissionName for the role
     *
     * @return
     */
    public String getPermissionName() {
        return this.permissionName;
    }
}
