/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
