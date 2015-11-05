/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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
 *
 **********************************************************************************/
package org.sakaiproject.user.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.Stack;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserEditHelper;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * BaseUserEdit is an implementation of the UserEdit object.
 * </p>
 */
public class BaseUserEdit implements UserEdit, SessionBindingListener, Serializable {
    private static Logger M_log = LoggerFactory.getLogger(BaseUserDirectoryService.class);

    /** The event code for this edit. */
    protected String m_event = null;

    /** Active flag. */
    protected boolean m_active = false;

    /** The user id. */
    protected String m_id = null;

    /** The user eid. */
    protected String m_eid = null;

    /** The user first name. */
    protected String m_firstName = null;

    /** The user last name. */
    protected String m_lastName = null;

    /** The user email address. */
    protected String m_email = null;

    /** The user password. */
    protected String m_pw = null;

    /** The properties. */
    protected ResourcePropertiesEdit m_properties = null;

    /** The user type. */
    protected String m_type = null;

    /** The created user id. */
    protected String m_createdUserId = null;

    /** The last modified user id. */
    protected String m_lastModifiedUserId = null;

    /** The time created. */
    protected Time m_createdTime = null;

    /** The time last modified. */
    protected Time m_lastModifiedTime = null;

    /** If editing the first name is restricted **/
    protected boolean m_restrictedFirstName = false;

    /** If editing the last name is restricted **/
    protected boolean m_restrictedLastName = false;

    /** If editing the email is restricted **/
    protected boolean m_restrictedEmail = false;

    /** If editing the password is restricted **/
    protected boolean m_restrictedPassword = false;

    /** If editing the type is restricted **/
    protected boolean m_restrictedType = false;

    /** if editing the eid is restricted **/
    protected boolean m_restrictedEid = false;

    // in object cache of the sort name.
    private transient String m_sortName;

    private transient UserEditHelper userEditHelper;

    /**
     * Construct.
     *
     * @param id
     *        The user id.
     */
    public BaseUserEdit(String id, String eid) {
        m_id = id;
        m_eid = eid;

        // setup for properties
        BaseResourcePropertiesEdit props = new BaseResourcePropertiesEdit();
        m_properties = props;

        // if the id is not null (a new user, rather than a reconstruction)
        // and not the anon (id == "") user,
        // add the automatic (live) properties
        if ((m_id != null) && (m_id.length() > 0))
            getUserEditHelper().addLiveProperties(this);

        //KNL-567 lazy set the properties to be lazy so they get loaded
        props.setLazy(true);
    }

    public BaseUserEdit(String id) {
        this(id, null);
    }

    public BaseUserEdit() {
        this(null, null);
    }

    /**
     * Construct from another User object.
     *
     * @param user
     *        The user object to use for values.
     */
    public BaseUserEdit(User user) {
        setAll(user);
    }

    /**
     * Construct from information in XML.
     *
     * @param el
     *        The XML DOM Element definining the user.
     */
    public BaseUserEdit(Element el) {
        // setup for properties
        m_properties = new BaseResourcePropertiesEdit();

        m_id = getUserEditHelper().cleanId(el.getAttribute("id"));
        m_eid = getUserEditHelper().cleanEid(el.getAttribute("eid"));
        m_firstName = trimToNull(el.getAttribute("first-name"));
        m_lastName = trimToNull(el.getAttribute("last-name"));
        setEmail(trimToNull(el.getAttribute("email")));
        m_pw = el.getAttribute("pw");
        m_type = trimToNull(el.getAttribute("type"));
        m_createdUserId = trimToNull(el.getAttribute("created-id"));
        m_lastModifiedUserId = trimToNull(el.getAttribute("modified-id"));

        String time = trimToNull(el.getAttribute("created-time"));
        if (time != null) {
            m_createdTime = getUserEditHelper().timeService().newTimeGmt(time);
        }

        time = trimToNull(el.getAttribute("modified-time"));
        if (time != null) {
            m_lastModifiedTime = getUserEditHelper().timeService().newTimeGmt(time);
        }

        // the children (roles, properties)
        NodeList children = el.getChildNodes();
        final int length = children.getLength();
        for (int i = 0; i < length; i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element element = (Element) child;

            // look for properties
            if (element.getTagName().equals("properties")) {
                // re-create properties
                m_properties = new BaseResourcePropertiesEdit(element);

                // pull out some properties into fields to convert old (pre 1.38) versions
                if (m_createdUserId == null) {
                    m_createdUserId = m_properties.getProperty("CHEF:creator");
                }
                if (m_lastModifiedUserId == null) {
                    m_lastModifiedUserId = m_properties.getProperty("CHEF:modifiedby");
                }
                if (m_createdTime == null) {
                    try {
                        m_createdTime = m_properties.getTimeProperty("DAV:creationdate");
                    } catch (Exception ignore) {
                    }
                }
                if (m_lastModifiedTime == null) {
                    try {
                        m_lastModifiedTime = m_properties.getTimeProperty("DAV:getlastmodified");
                    } catch (Exception ignore) {
                    }
                }
                m_properties.removeProperty("CHEF:creator");
                m_properties.removeProperty("CHEF:modifiedby");
                m_properties.removeProperty("DAV:creationdate");
                m_properties.removeProperty("DAV:getlastmodified");
            }
        }
    }

    /**
     * ReConstruct.
     *
     * @param id
     *        The id.
     * @param eid
     *        The eid.
     * @param email
     *        The email.
     * @param firstName
     *        The first name.
     * @param lastName
     *        The last name.
     * @param type
     *        The type.
     * @param pw
     *        The password.
     * @param createdBy
     *        The createdBy property.
     * @param createdOn
     *        The createdOn property.
     * @param modifiedBy
     *        The modified by property.
     * @param modifiedOn
     *        The modified on property.
     */
    public BaseUserEdit(String id, String eid, String email, String firstName, String lastName, String type, String pw,
            String createdBy, Time createdOn, String modifiedBy, Time modifiedOn) {
        m_id = id;
        m_eid = eid;
        m_firstName = firstName;
        m_lastName = lastName;
        m_type = type;
        setEmail(email);
        m_pw = pw;
        m_createdUserId = createdBy;
        m_lastModifiedUserId = modifiedBy;
        m_createdTime = createdOn;
        m_lastModifiedTime = modifiedOn;

        // setup for properties, but mark them lazy since we have not yet established them from data
        BaseResourcePropertiesEdit props = new BaseResourcePropertiesEdit();
        props.setLazy(true);
        m_properties = props;
    }

    /**
     * Take all values from this object.
     *
     * @param user
     *        The user object to take values from.
     */
    protected void setAll(User user) {
        m_id = user.getId();
        m_eid = user.getEid();
        m_firstName = user.getFirstName();
        m_lastName = user.getLastName();
        m_type = user.getType();
        setEmail(user.getEmail());
        m_pw = ((BaseUserEdit) user).m_pw;
        m_createdUserId = ((BaseUserEdit) user).m_createdUserId;
        m_lastModifiedUserId = ((BaseUserEdit) user).m_lastModifiedUserId;
        if (((BaseUserEdit) user).m_createdTime != null)
            m_createdTime = (Time) ((BaseUserEdit) user).m_createdTime.clone();
        if (((BaseUserEdit) user).m_lastModifiedTime != null)
            m_lastModifiedTime = (Time) ((BaseUserEdit) user).m_lastModifiedTime.clone();

        m_properties = new BaseResourcePropertiesEdit();
        m_properties.addAll(user.getProperties());
        ((BaseResourcePropertiesEdit) m_properties).setLazy(((BaseResourceProperties) user.getProperties()).isLazy());
    }

    /**
     * @inheritDoc
     */
    public Element toXml(Document doc, Stack stack) {
        Element user = doc.createElement("user");

        if (stack.isEmpty()) {
            doc.appendChild(user);
        } else {
            ((Element) stack.peek()).appendChild(user);
        }

        stack.push(user);

        user.setAttribute("id", getId());
        user.setAttribute("eid", getEid());
        if (m_firstName != null)
            user.setAttribute("first-name", m_firstName);
        if (m_lastName != null)
            user.setAttribute("last-name", m_lastName);
        if (m_type != null)
            user.setAttribute("type", m_type);
        user.setAttribute("email", getEmail());
        user.setAttribute("created-id", m_createdUserId);
        user.setAttribute("modified-id", m_lastModifiedUserId);

        if (m_createdTime != null) {
            user.setAttribute("created-time", m_createdTime.toString());
        }

        if (m_lastModifiedTime != null) {
            user.setAttribute("modified-time", m_lastModifiedTime.toString());
        }

        // properties
        getProperties().toXml(doc, stack);

        stack.pop();

        return user;
    }

    /**
     * @inheritDoc
     */
    public String getId() {
        return m_id;
    }

    /**
     * @inheritDoc
     */
    public String getEid() {
        return m_eid;
    }

    /**
     * @inheritDoc
     */
    public String getUrl() {
        return getUserEditHelper().getAccessPoint(false) + m_id;
    }

    /**
     * @inheritDoc
     */
    public String getReference() {
        return getUserEditHelper().userReference(m_id);
    }

    /**
     * @inheritDoc
     */
    public String getReference(String rootProperty) {
        return getReference();
    }

    /**
     * @inheritDoc
     */
    public String getUrl(String rootProperty) {
        return getUrl();
    }

    /**
     * @inheritDoc
     */
    public ResourceProperties getProperties() {
        // if lazy, resolve
        if (((BaseResourceProperties) m_properties).isLazy()) {
            ((BaseResourcePropertiesEdit) m_properties).setLazy(false);
            getUserEditHelper().readProperties(this, m_properties);
        }

        return m_properties;
    }

    /**
     * @inheritDoc
     */
    public User getCreatedBy() {
        try {
            return getUserEditHelper().getUser(m_createdUserId);
        } catch (Exception e) {
            return getUserEditHelper().getAnonymousUser();
        }
    }

    /**
     * @inheritDoc
     */
    public User getModifiedBy() {
        try {
            return getUserEditHelper().getUser(m_lastModifiedUserId);
        } catch (Exception e) {
            return getUserEditHelper().getAnonymousUser();
        }
    }

    /**
     * @inheritDoc
     */
    public Time getCreatedTime() {
        return m_createdTime;
    }

    /**
     * @inheritDoc
     */
    public Date getCreatedDate() {
        return new Date(m_createdTime.getTime());
    }

    /**
     * @inheritDoc
     */
    public Time getModifiedTime() {
        return m_lastModifiedTime;
    }

    /**
     * @inheritDoc
     */
    public Date getModifiedDate() {
        return new Date(m_lastModifiedTime.getTime());
    }

    /**
     * @inheritDoc
     */
    public String getDisplayName() {
        // If a contextual aliasing service exists, let it have the first try.
        String rv = getUserEditHelper().getUserDisplayName(this);
        if (rv != null) {
            return rv;
        }

        // let the provider handle it, if we have that sort of provider, and it wants to handle this
        rv = getUserEditHelper().getDisplayName(this);

        if (rv == null) {
            // or do it this way
            StringBuilder buf = new StringBuilder(128);
            if (m_firstName != null)
                buf.append(m_firstName);
            if (m_lastName != null) {
                if (buf.length() > 0)
                    buf.append(" ");
                buf.append(m_lastName);
            }

            if (buf.length() == 0) {
                rv = getEid();
            }

            else {
                rv = buf.toString();
            }
        }

        return rv;
    }

    /**
     * @inheritDoc
     */
    public String getDisplayId() {
        // If a contextual aliasing service exists, let it have the first try.
        String rv = getUserEditHelper().getUserDisplayId(this);
        if (rv != null) {
            return rv;
        }

        // let the provider handle it, if we have that sort of provider, and it wants to handle this
        rv = getUserEditHelper().getDisplayId(this);

        // use eid if not
        if (rv == null) {
            rv = getEid();
        }

        return rv;
    }

    /**
     * @inheritDoc
     */
    public String getFirstName() {
        if (m_firstName == null)
            return "";
        return m_firstName;
    }

    /**
     * @inheritDoc
     */
    public String getLastName() {
        if (m_lastName == null)
            return "";
        return m_lastName;
    }

    /**
     * @inheritDoc
     */
    public String getSortName() {
        if (m_sortName == null) {
            String result = getUserEditHelper().getSortName(this);
            if (result != null) {
                m_sortName = result;
                return result;
            }

            // Cache this locally in the object as otherwise when sorting users we generate lots of objects.
            StringBuilder buf = new StringBuilder(128);
            if (m_lastName != null)
                buf.append(m_lastName);
            if (m_firstName != null) {
                //KNL-524 no comma if the last name is null
                if (m_lastName != null) {
                    buf.append(", ");
                }
                buf.append(m_firstName);
            }

            m_sortName = (buf.length() == 0) ? getEid() : buf.toString();
        }

        return m_sortName;
    }

    /**
     * @inheritDoc
     */
    public String getEmail() {
        if (m_email == null)
            return "";
        return m_email;
    }

    /**
     * @inheritDoc
     */
    public String getType() {
        return m_type;
    }

    /**
     * @inheritDoc
     */
    public boolean checkPassword(String pw) {
        pw = trimToNull(pw);
        return getUserEditHelper().checkPassword(pw, m_pw);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BaseUserEdit that = (BaseUserEdit) o;

        if (m_id != null ? !m_id.equals(that.m_id) : that.m_id != null)
            return false;
        if (m_eid != null ? !m_eid.equals(that.m_eid) : that.m_eid != null)
            return false;

        return true;
    }

    /**
     * @inheritDoc
     */
    public int hashCode() {
        String id = getId();
        if (id == null) {
            // Maintains consistency with Sakai 2.4.x behavior.
            id = "";
        }
        return id.hashCode();
    }

    /**
     * @inheritDoc
     */
    public int compareTo(Object obj) {
        if (!(obj instanceof User))
            throw new ClassCastException();

        // if the object are the same, say so
        if (obj == this)
            return 0;

        // start the compare by comparing their sort names
        int compare = getSortName().compareTo(((User) obj).getSortName());

        // if these are the same
        if (compare == 0) {
            // sort based on (unique) eid
            compare = getEid().compareTo(((User) obj).getEid());
        }

        return compare;
    }

    /**
     * Clean up.
     */
    protected void finalize() {
        // catch the case where an edit was made but never resolved
        if (m_active) {
            getUserEditHelper().cancelEdit(this);
        }
    }

    /**
     * @inheritDoc
     */
    public void setId(String id) {
        // set once only!
        if (m_id == null) {
            m_id = id;
        } else
            throw new UnsupportedOperationException("Tried to change user ID from " + m_id + " to " + id);
    }

    /**
     * @inheritDoc
     */
    public void setEid(String eid) {
        if (!m_restrictedEid) {
            m_eid = eid;
            m_sortName = null;
        }
    }

    /**
     * @inheritDoc
     */
    public void setFirstName(String name) {
        if (!m_restrictedFirstName) {
            // https://jira.sakaiproject.org/browse/SAK-20226 - removed html from name
            m_firstName = getUserEditHelper().formattedText().convertFormattedTextToPlaintext(name);
            m_sortName = null;
        }
    }

    /**
     * @inheritDoc
     */
    public void setLastName(String name) {
        if (!m_restrictedLastName) {
            // https://jira.sakaiproject.org/browse/SAK-20226 - removed html from name
            m_lastName = getUserEditHelper().formattedText().convertFormattedTextToPlaintext(name);
            m_sortName = null;
        }
    }

    /**
     * @inheritDoc
     */
    public void setEmail(String email) {
        if (!m_restrictedEmail) {
            m_email = email;
        }
    }

    /**
     * @inheritDoc
     */
    public void setPassword(String pw) {
        if (!m_restrictedPassword) {
            m_pw = (pw == null) ? null : getUserEditHelper().encodePassword(pw);
        }
    }

    /**
     * @inheritDoc
     */
    public void setType(String type) {
        if (!m_restrictedType) {

            m_type = type;

        }
    }

    public void restrictEditFirstName() {

        m_restrictedFirstName = true;

    }

    public void restrictEditLastName() {

        m_restrictedLastName = true;

    }

    public void restrictEditEmail() {

        m_restrictedEmail = true;

    }

    public void restrictEditPassword() {

        m_restrictedPassword = true;

    }

    public void restrictEditEid() {
        m_restrictedEid = true;
    }

    public void restrictEditType() {

        m_restrictedType = true;

    }

    /**
     * Take all values from this object.
     *
     * @param user
     *        The user object to take values from.
     */
    protected void set(User user) {
        setAll(user);
    }

    /**
     * Access the event code for this edit.
     *
     * @return The event code for this edit.
     */
    protected String getEvent() {
        return m_event;
    }

    /**
     * Set the event code for this edit.
     *
     * @param event
     *        The event code for this edit.
     */
    protected void setEvent(String event) {
        m_event = event;
    }

    /**
     * @inheritDoc
     */
    public ResourcePropertiesEdit getPropertiesEdit() {
        // if lazy, resolve
        if (((BaseResourceProperties) m_properties).isLazy()) {
            ((BaseResourcePropertiesEdit) m_properties).setLazy(false);
            getUserEditHelper().readProperties(this, m_properties);
        }

        return m_properties;
    }

    /**
     * Enable editing.
     */
    protected void activate() {
        m_active = true;
    }

    /**
     * @inheritDoc
     */
    public boolean isActiveEdit() {
        return m_active;
    }

    /**
     * Close the edit object - it cannot be used after this.
     */
    protected void closeEdit() {
        m_active = false;
    }

    /**
     * Check this User object to see if it is selected by the criteria.
     *
     * @param criteria
     *        The critera.
     * @return True if the User object is selected by the criteria, false if not.
     */
    protected boolean selectedBy(String criteria) {
        if (containsIgnoreCase(getSortName(), criteria) || containsIgnoreCase(getDisplayName(), criteria)
                || containsIgnoreCase(getEid(), criteria) || containsIgnoreCase(getEmail(), criteria)) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "BaseUserEdit{" + "m_id='" + m_id + '\'' + ", m_eid='" + m_eid + '\'' + '}';
    }

    private UserEditHelper getUserEditHelper() {
        if (userEditHelper == null) {
            userEditHelper = (UserEditHelper) ComponentManager.get(UserDirectoryService.class.getName());
            userEditHelper.init();
        }
        return userEditHelper;
    }

    void setBaseUserDirectoryService(BaseUserDirectoryService uds) {
        userEditHelper = uds;
    }

    public void setLastModifiedUserId(String id) {
        m_lastModifiedUserId = id;
    }

    /**
     * Set the last modified time
     */
    public void setLastModifiedTime(Time lastModifiedTime) {
        m_lastModifiedTime = lastModifiedTime;
    }

    public void setCreatedUserId(String id) {
        m_createdUserId = id;
    }

    public void setCreatedTime(Time createdTime) {
        m_createdTime = createdTime;
    }

    /**
     * The preferred method is to use common StringUtils, but this is here to reduce the dependencies on other libs.
     * @param value
     * @return
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        return (value.length() == 0) ? null : value;
    }

    /**
     * The preferred method is to use StringUtil, but this is here to reduce the dependencies on other libs.
     * @param target
     * @param substring
     * @return
     */
    private boolean containsIgnoreCase(String target, String substring) {
        if ((target == null) || (substring == null)) {
            return false;
        }
        target = target.toLowerCase();
        substring = substring.toLowerCase();
        int pos = target.indexOf(substring);
        return (pos != -1);
    }

    /******************************************************************************************************************************************************************************************************************************************************
     * SessionBindingListener implementation
     *****************************************************************************************************************************************************************************************************************************************************/

    /**
     * @inheritDoc
     */
    public void valueBound(SessionBindingEvent event) {
    }

    /**
     * @inheritDoc
     */
    public void valueUnbound(SessionBindingEvent event) {
        if (M_log.isDebugEnabled())
            M_log.debug("valueUnbound()");

        // catch the case where an edit was made but never resolved
        if (m_active) {
            getUserEditHelper().cancelEdit(this);
        }
    }
}
