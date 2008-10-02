/**
 * $Id: EntityMember.java 51323 2008-08-24 07:24:19Z csev@umich.edu $
 * $URL: https://source.sakaiproject.org/svn/entitybroker/trunk/impl/src/java/org/sakaiproject/entitybroker/providers/EntityMember.java $
 * EntityMember.java - entity-broker - Aug 15, 2008 2:02:20 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.providers.model;

import org.azeckoski.reflectutils.annotations.ReflectIgnoreClassFields;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityOwner;

/**
 * Represents a membership in something (probably a site or group)
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@ReflectIgnoreClassFields({"role"})
public class EntityMember implements Member {

    @EntityId
    private String id;
    private String userId;
    private String locationReference;
    private String memberRole;
    private String userEid;
    private boolean active = true;
    private boolean provided = false;

    private transient Member member;

    public EntityMember() {
    }

    /**
     * @param userId a unique user id (not the username), e.g. 59307d75-7863-4560-9abc-6d1c4e62a63e or 'admin'
     * @param locationReference the reference to the location (e.g. site or group) this is a membership in (e.g. '/site/mysite', '/site/mysite/group/mygroup')
     * @param memberRole the id of the membership role (e.g. maintain, access, etc.)
     * @param active true if this membership should be active, false otherwise
     */
    public EntityMember(String userId, String locationReference, String memberRole, boolean active) {
        this.id = makeId(userId, locationReference);
        this.userId = userId;
        this.locationReference = locationReference;
        this.memberRole = memberRole;
        this.active = active;
    }

    /**
     * @param member a legacy Member object
     * @param locationReference the reference to the location (e.g. site or group) this is a membership in (e.g. '/site/mysite', '/site/mysite/group/mygroup')
     */
    public EntityMember(Member member, String locationReference) {
        this.userId = member.getUserId();
        this.id = makeId(this.userId, locationReference);
        this.userEid = member.getUserEid();
        this.memberRole = member.getRole() == null ? null : member.getRole().getId();
        this.locationReference = locationReference;
        this.member = member;
        this.active = member.isActive();
        this.provided = member.isProvided();
    }

    /**
     * Constructs a membership id from the user and member references
     * @param userId a unique user id (not the username), e.g. 59307d75-7863-4560-9abc-6d1c4e62a63e or 'admin'
     * @param locationReference the reference to the thing this is a membership in, site or group (e.g. '/site/mysite', '/site/mysite/group/mygroup')
     * @return a membership id which is unique
     */
    public static String makeId(String userId, String locationReference) {
        if (userId == null || locationReference == null || "".equals(locationReference)) {
            throw new IllegalArgumentException("userId ("+userId+") and locationReference ("+locationReference+") cannot be null when creating an id for a membership");
        }
        if (locationReference.charAt(0) != '/') {
            throw new IllegalArgumentException("locationReference ("+locationReference+") must be a reference like '/site/siteId'");
        }
        if (userId.charAt(0) == '/') {
            userId = userId.substring(1);
        }
        if (userId.indexOf('/') > 0) {
            userId = userId.replace('/', ':');
        }
        if (locationReference.charAt(0) == '/') {
            locationReference = locationReference.substring(1);
        }
        if (locationReference.indexOf('/') > 0) {
            locationReference = locationReference.replace('/', ':');
        }
        String id = userId + "::" + locationReference;
        return id;
    }

    /**
     * Tries to parse a membershipId into three parts:
     * 1) the userId
     * 2) the location reference
     * 3) the location entity prefix
     * @param membershipId an id structured like 'userId::site:siteId'
     * @return an array with the 3 parts in order
     */
    public static String[] parseId(String membershipId) {
        if (membershipId == null || "".equals(membershipId)) {
            throw new IllegalArgumentException("membershipId cannot be null");
        }
        int splitter = membershipId.indexOf("::");
        if (splitter == -1 
                || membershipId.length() < splitter + 3) {
            return null; // invalid
        }
        String userId = membershipId.substring(0, splitter);
        if (userId.startsWith("user:")) {
            userId = userId.substring(5);
        }
        String locationRef = '/' + membershipId.substring(splitter+2).replace(':', '/');
        EntityReference ref = new EntityReference(locationRef);
        if (ref.getId() == null) {
            return null; // invalid
        }
        String locType = ref.getPrefix();
        String[] togo = new String[] {userId, locationRef, locType};
        return togo;
    }

    @EntityId
    public String getId() {
        return id;
    }

    @EntityOwner
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocationReference() {
        return locationReference;
    }

    public void setLocationReference(String locationReference) {
        this.locationReference = locationReference;
    }

    public String getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(String memberRole) {
        this.memberRole = memberRole;
    }

    public String getUserEid() {
        return userEid;
    }

    public boolean isProvided() {
        return provided;
    }

    public void setProvided(boolean provided) {
        this.provided = provided;
    }

    public boolean isActive() {
        return active;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.authz.api.Member#getRole()
     */
    public Role getRole() {
        if (member != null) {
            return member.getRole();
        }
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.authz.api.Member#getUserDisplayId()
     */
    public String getUserDisplayId() {
        if (member != null) {
            return member.getUserDisplayId();
        }
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.authz.api.Member#setActive(boolean)
     */
    public void setActive(boolean arg0) {
        if (member != null) {
            member.setActive(arg0);
        }
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {
        if (!(obj instanceof Member))
            throw new ClassCastException();
        if (obj == this)
            return 0;
        int compare = getUserId().compareTo(((Member) obj).getUserId());
        return compare;
    }

}
