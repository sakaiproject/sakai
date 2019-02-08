/**
 * $Id$
 * $URL$
 * EntitySite.java - entity-broker - Jun 29, 2008 9:31:10 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.providers.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.azeckoski.reflectutils.annotations.ReflectIgnoreClassFields;
import org.azeckoski.reflectutils.annotations.ReflectTransient;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityFieldRequired;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityLastModified;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityOwner;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntitySummary;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityTitle;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is needed to allow RESTful access to the group data
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@SuppressWarnings("unchecked")
@ReflectIgnoreClassFields({"createdBy","modifiedBy","containingSite","members","properties","propertiesEdit","roles"})
public class EntityGroup implements Group {

    private static final long serialVersionUID = 7526472295622776147L;

    @EntityId
    private String id;
    @EntityFieldRequired
    private String siteId;
    @EntityFieldRequired
    private String title;
    private String description;
    private String joinerRole;
    private String maintainRole;
    private String providerGroupId;
    private String owner;
    private long lastModified;
    private String[] userRoles;

    public Map<String, String> props;
    public Map<String, String> getProps() {
        if (props == null) {
            props = new HashMap<String, String>();
        }
        return props;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    public void setProperty(String key, String value) {
        if (props == null) {
            props = new HashMap<String, String>();
        }
        props.put(key, value);
    }

    public String getProperty(String key) {
        if (props == null) {
            return null;
        }
        return props.get(key);
    }

    private transient Group group;

    public EntityGroup() {
    }

    
    public EntityGroup(String siteId, String title, String description, String maintainRole,
            String providerGroupId, String owner) {
        super();
        this.siteId = siteId;
        this.title = title;
        this.description = description;
        this.maintainRole = maintainRole;
        this.providerGroupId = providerGroupId;
        this.owner = owner;
        this.lastModified = System.currentTimeMillis();
        getUserRoles(); // populate the user roles
    }

    public EntityGroup(Group group) {
        this.group = group;
        Site site = group.getContainingSite();
        this.siteId = site.getId();
        this.id = group.getId();
        this.title = group.getTitle();
        this.description = group.getDescription();
        this.joinerRole = site.getJoinerRole();
        this.maintainRole = group.getMaintainRole();
        this.providerGroupId = group.getProviderGroupId();
        this.owner = group.getCreatedBy() == null ? null : group.getCreatedBy().getId();
        this.lastModified = group.getModifiedTime() == null ? System.currentTimeMillis() : group.getModifiedTime().getTime();
        getUserRoles(); // populate the user roles
        // properties
        ResourceProperties rp = group.getProperties();
        for (Iterator<String> iterator = rp.getPropertyNames(); iterator.hasNext();) {
            String name = iterator.next();
            String value = rp.getProperty(name);
            this.setProperty(name, value);
        }
    }


    @EntityId
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    /**
     * @return the id of the owner of this group (will match the created by user id)
     */
    @EntityOwner
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Special method
     * @return the owner data for the current site owner
     */
    public Owner getSiteOwner() {
        Owner owner = null;
        if (this.group != null) {
            // TODO handle the contact info?
            User user = group.getCreatedBy();
            owner = new Owner(user.getId(), user.getDisplayName());
        } else {
            owner = new Owner(this.owner, this.owner);
        }
        return owner;
    }

    @EntityLastModified
    public long getLastModified() {
        if (group != null) {
            this.lastModified = group.getModifiedTime() == null ? lastModified : group.getModifiedTime().getTime();
        }
        return lastModified;
    }

	
    
    public void setLastModified(long lastModified) {
        throw new UnsupportedOperationException("Cannot set the last modified time manually");
    }

    @EntityTitle
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @EntitySummary
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJoinerRole() {
        return joinerRole;
    }

    public void setJoinerRole(String joinerRole) {
        this.joinerRole = joinerRole;
    }

    public String getMaintainRole() {
        return maintainRole;
    }

    public void setMaintainRole(String maintainRole) {
        this.maintainRole = maintainRole;
    }

    public String getProviderGroupId() {
        return providerGroupId;
    }

    public void setProviderGroupId(String providerGroupId) {
        this.providerGroupId = providerGroupId;
    }

    public String[] getUserRoles() {
        if (userRoles == null) {
            if (group == null) {
                userRoles = new String[] {maintainRole, joinerRole};
            } else {
                Set<Role> roles = (Set<Role>) group.getRoles();
                userRoles = new String[roles.size()];
                int i = 0;
                for (Role role : roles) {
                    userRoles[i] = role.getId();
                    i++;
                }
            }
        }
        return userRoles;
    }
    
    public void setUserRoles(String[] userRoles) {
        this.userRoles = userRoles;
    }
    
    
    // Site operations

    public User getCreatedBy() {
        if (group != null) {
            return group.getCreatedBy();
        }
        throw new UnsupportedOperationException();
    }

    public Time getCreatedTime() {
        if (group != null) {
            return group.getCreatedTime();
        }
        throw new UnsupportedOperationException();
    }
    
    public Date getCreatedDate() {
        if (group != null) {
            return group.getCreatedDate();
        }
        throw new UnsupportedOperationException();
    }
    public User getModifiedBy() {
        if (group != null) {
            return group.getModifiedBy();
        }
        throw new UnsupportedOperationException();
    }

    public Time getModifiedTime() {
        if (group != null) {
            return group.getModifiedTime();
        }
        throw new UnsupportedOperationException();
    }
    
    
    public Date getModifiedDate() {
    	if (group != null) {
            return group.getModifiedDate();
        }
        throw new UnsupportedOperationException();
	}

    public ResourcePropertiesEdit getPropertiesEdit() {
        if (group != null) {
            return group.getPropertiesEdit();
        }
        throw new UnsupportedOperationException();
    }

    public boolean isActiveEdit() {
        if (group != null) {
            return group.isActiveEdit();
        }
        throw new UnsupportedOperationException();
    }

    public ResourceProperties getProperties() {
        if (group != null) {
            return group.getProperties();
        }
        throw new UnsupportedOperationException();
    }

    public String getReference() {
        return "/site/" + siteId + "/group/" + id;
    }

    public String getReference(String arg0) {
        return this.getReference();
    }

    public String getUrl() {
        if (group != null) {
            return group.getUrl();
        }
        throw new UnsupportedOperationException();
    }

    public String getUrl(String arg0) {
        if (group != null) {
            return group.getUrl(arg0);
        }
        throw new UnsupportedOperationException();
    }

    @ReflectTransient
    public Element toXml(Document arg0, Stack arg1) {
        if (group != null) {
            return group.toXml(arg0, arg1);
        }
        throw new UnsupportedOperationException();
    }

    public int compareTo(Object o) {
        if (group != null) {
            return group.compareTo(o);
        }
        throw new UnsupportedOperationException();
    }

    public void addMember(String arg0, String arg1, boolean arg2, boolean arg3) {
        if (group != null) {
            group.addMember(arg0, arg1, arg2, arg3);
            return;
        }
        throw new UnsupportedOperationException();
    }

    public void insertMember(String arg0, String arg1, boolean arg2, boolean arg3) throws IllegalStateException {
        if (group != null) {
            try {
                group.insertMember(arg0, arg1, arg2, arg3);
                return;
            } catch (IllegalStateException e) {
                throw e;
            }
        }
        throw new UnsupportedOperationException();
    }

    public Role addRole(String arg0) throws RoleAlreadyDefinedException {
        if (group != null) {
            return group.addRole(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public Role addRole(String arg0, Role arg1) throws RoleAlreadyDefinedException {
        if (group != null) {
            return group.addRole(arg0, arg1);
        }
        throw new UnsupportedOperationException();
    }

    public Member getMember(String arg0) {
        if (group != null) {
            return group.getMember(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public Set getMembers() {
        if (group != null) {
            return group.getMembers();
        }
        throw new UnsupportedOperationException();
    }

    public Role getRole(String arg0) {
        if (group != null) {
            return group.getRole(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public Set getRoles() {
        if (group != null) {
            return group.getRoles();
        }
        throw new UnsupportedOperationException();
    }

    public Set getRolesIsAllowed(String arg0) {
        if (group != null) {
            return group.getRolesIsAllowed(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public Role getUserRole(String arg0) {
        if (group != null) {
            return group.getUserRole(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public Set getUsers() {
        if (group != null) {
            return group.getUsers();
        }
        throw new UnsupportedOperationException();
    }

    public Set getUsersHasRole(String arg0) {
        if (group != null) {
            return group.getUsersHasRole(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public Set getUsersIsAllowed(String arg0) {
        if (group != null) {
            return group.getUsersIsAllowed(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public boolean hasRole(String arg0, String arg1) {
        if (group != null) {
            return group.hasRole(arg0, arg1);
        }
        throw new UnsupportedOperationException();
    }

    public boolean isAllowed(String arg0, String arg1) {
        if (group != null) {
            return group.isAllowed(arg0, arg1);
        }
        return false;
    }

    public boolean isEmpty() {
        if (group != null) {
            return group.isEmpty();
        }
        return false;
    }

    public boolean keepIntersection(AuthzGroup arg0) {
        if (group != null) {
            return group.keepIntersection(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public void removeMember(String arg0) {
        if (group != null) {
            group.removeMember(arg0);
            return;
        }
        throw new UnsupportedOperationException();
    }

    public void deleteMember(String arg0) throws IllegalStateException {
        if (group != null) {
            try {
                group.deleteMember(arg0);
                return;
            } catch (IllegalStateException e) {
                throw e;
            }
        }
        throw new UnsupportedOperationException();
    }

    public void removeMembers() {
        if (group != null) {
            group.removeMembers();
            return;
        }
        throw new UnsupportedOperationException();
    }

    public void deleteMembers() throws IllegalStateException {
        if (group != null) {
            try {
                group.deleteMembers();
                return;
            } catch (IllegalStateException e) {
                throw e;
            }
        }
        throw new UnsupportedOperationException();
    }

    public void removeRole(String arg0) {
        if (group != null) {
            group.removeRole(arg0);
            return;
        }
        throw new UnsupportedOperationException();
    }

    public void removeRoles() {
        if (group != null) {
            group.removeRoles();
            return;
        }
        throw new UnsupportedOperationException();
    }

    public Site getContainingSite() {
        if (group != null) {
            return group.getContainingSite();
        }
        throw new UnsupportedOperationException();
    }

    public void lockGroup(Entity entity) {
        if (group != null) {
            group.lockGroup(entity);
            return;
        }
        throw new UnsupportedOperationException();
    }

    public void lockGroup(String lock) {
        if (group != null) {
            group.lockGroup(lock);
            return;
        }
        throw new UnsupportedOperationException();
    }

    public void unlockGroup(Entity entity) {
        if (group != null) {
            group.unlockGroup(entity);
            return;
        }
        throw new UnsupportedOperationException();
    }

    public void unlockGroup(String lock) {
        if (group != null) {
            group.unlockGroup(lock);
            return;
        }
        throw new UnsupportedOperationException();
    }

    public void unlockGroup() {
        if (group != null) {
            group.unlockGroup();
            return;
        }
        throw new UnsupportedOperationException();
    }

    public boolean isLocked(String lock) {
        if (group != null) {
            return group.isLocked(lock);
        }
        throw new UnsupportedOperationException();
    }

    public boolean isLocked() {
        if (group != null) {
            return group.isLocked();
        }
        throw new UnsupportedOperationException();
    }

}
