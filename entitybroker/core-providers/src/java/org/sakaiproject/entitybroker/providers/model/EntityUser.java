/**
 * $Id$
 * $URL$
 * EntityUser.java - entity-broker - Jun 28, 2008 5:24:57 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
import java.util.Stack;

import org.azeckoski.reflectutils.annotations.ReflectIgnoreClassFields;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityFieldRequired;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityLastModified;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityOwner;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityTitle;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This class is needed to allow input and output since the User/UserEdit classes are too hard to work with,
 * it is disappointing that this is needed, very disappointing indeed<br/>
 * They seem to already be wrapped in a proxy as well for some reason based on the failure from xstream when
 * it tries to work with them
 *
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@SuppressWarnings("unchecked")
@ReflectIgnoreClassFields({"createdBy","modifiedBy","properties"})
public class EntityUser implements User {

    @EntityId
    private String id;
    @EntityFieldRequired
    private String eid;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String displayName;
    private String displayId;
    private String type;
    private String owner;
    private long lastModified;
    public Map<String, String> props;

    private transient User user;

    public EntityUser() {}

    /**
     * Construct an EntityUser from a legacy user object
     * @param user a legacy user or user edit
     */
    public EntityUser(User user) {
        this.user = user;
        this.id = user.getId();
        this.eid = user.getEid();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.displayName = user.getDisplayName();
        this.displayId = user.getDisplayId();
        this.owner = user.getCreatedBy() == null ? null : "/user/" + user.getCreatedBy().getId();
        this.lastModified = user.getModifiedTime() == null ? System.currentTimeMillis() : user.getModifiedTime().getTime();
        this.type = user.getType();
        ResourceProperties rp = user.getProperties();
        for (Iterator<String> iterator = rp.getPropertyNames(); iterator.hasNext();) {
            String name = iterator.next();
            String value = rp.getProperty(name);
            this.setProperty(name, value);
        }
    }

    public EntityUser(String eid, String email, String firstName, String lastName,
            String displayName, String displayId, String password, String type) {
        this.eid = eid;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.displayName = displayName;
        this.displayId = displayId;
        this.type = type;
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

    @EntityOwner
    public String getOwner() {
        return owner;
    }

    @EntityLastModified
    public long getLastModified() {
        return lastModified;
    }

    @EntityId
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @EntityTitle
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getProps() {
        if (props == null) {
            props = new HashMap<String,String>();
        }
        return props;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    // extra User junk below here
    // TODO set these so they are ignored by reflection

    public boolean checkPassword(String arg0) {
        if (user != null) {
            return user.checkPassword(arg0);
        }
        return false;
    }

    public User getCreatedBy() {
        if (user != null) {
            return user.getCreatedBy();
        }
        throw new UnsupportedOperationException();
    }

    public Time getCreatedTime() {
        if (user != null) {
            return user.getCreatedTime();
        }
        throw new UnsupportedOperationException();
    }

    public Date getCreatedDate() {
        if (user != null) {
            return user.getCreatedDate();
        }
        return new Date(lastModified);
    }

    public String getDisplayId() {
        return displayId;
    }

    public User getModifiedBy() {
        if (user != null) {
            return user.getModifiedBy();
        }
        throw new UnsupportedOperationException();
    }

    public Time getModifiedTime() {
        if (user != null) {
            return user.getModifiedTime();
        }
        throw new UnsupportedOperationException();
    }

    public Date getModifiedDate() {
        if (user != null) {
            return user.getModifiedDate();
        }
        return new Date(lastModified);
    }

    public String getSortName() {
        String sortName = null;
        if (user != null) {
            sortName = user.getSortName();
        } else {
            // generate the sortName
            StringBuilder sb = new StringBuilder(128);
            if (lastName != null) sb.append(lastName);
            if (firstName != null) {
                sb.append(", ");
                sb.append(firstName);
            }
            if (sb.length() == 0) sb.append(email);
            if (sb.length() == 0) sb.append(eid);
            sortName = sb.toString();
        }
        return sortName;
    }

    public ResourceProperties getProperties() {
        if (user != null) {
            return user.getProperties();
        }
        throw new UnsupportedOperationException();
    }

    public String getReference() {
        return "/user/" + id;
    }

    public String getReference(String arg0) {
        return getReference();
    }

    public String getUrl() {
        if (user != null) {
            return user.getUrl();
        }
        throw new UnsupportedOperationException();
    }

    public String getUrl(String arg0) {
        if (user != null) {
            return user.getUrl(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public Element toXml(Document arg0, Stack arg1) {
        if (user != null) {
            return user.toXml(arg0, arg1);
        }
        throw new UnsupportedOperationException();
    }

    public int compareTo(Object o) {
        if (user != null) {
            return user.compareTo(o);
        }
        throw new UnsupportedOperationException();
    }
}
