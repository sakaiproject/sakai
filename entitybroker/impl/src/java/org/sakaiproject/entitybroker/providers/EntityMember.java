/**
 * $Id$
 * $URL$
 * EntityMember.java - entity-broker - Aug 15, 2008 2:02:20 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.providers;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityOwner;

/**
 * Represents a membership in something (probably a site or group)
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityMember implements Member {

    @EntityId
    private String id;
    private String userId;
    private String locationReference;
    private String memberRole;
    private String userEid;
    private boolean active;
    private boolean provided;

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
    public String makeId(String userId, String locationReference) {
        if (userId == null || locationReference == null) {
            throw new IllegalArgumentException("userId ("+userId+") and locationReference ("+locationReference+") cannot be null when creating an id for a membership");
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

    public String getMemberInRef() {
        return locationReference;
    }

    public void setMemberInRef(String locationReference) {
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
