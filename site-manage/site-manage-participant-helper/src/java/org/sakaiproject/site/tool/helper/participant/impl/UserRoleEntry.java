/**
 * Copyright (c) 2003-2009 The Apereo Foundation
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
package org.sakaiproject.site.tool.helper.participant.impl;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class UserRoleEntry implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String firstName;
    private final String lastName;
    private final String role;
    private final String eid;

    public UserRoleEntry(String firstName, String lastName, String role, String eid) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.eid = eid;
    }

    public UserRoleEntry(String eid, String role) {
        this(null, null, role, eid);
    }

    public UserRoleEntry withRole(String role) {
        return new UserRoleEntry(firstName, lastName, role, eid);
    }
}
