/**
 * Copyright (c) 2005 The Apereo Foundation
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
package org.sakaiproject.webservices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Date;
import java.util.Collections;
import java.util.Collection;
import java.util.Map.Entry;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.LocaleUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEdit;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.RecurrenceRule;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.EntityTransferrerRefMigrator;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.util.ArrayUtil;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.Xml;
import org.sakaiproject.util.Web;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * SakaiScript.jws
 * <p/>
 * A set of administrative web services for Sakai
 */

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
@Slf4j
public class SakaiScript extends AbstractWebService {

    private static final String ADMIN_SITE_REALM = "/site/!admin";
    private static final String SESSION_ATTR_NAME_ORIGIN = "origin";
    private static final String SESSION_ATTR_VALUE_ORIGIN_WS = "sakai-axis";

    /**
     * Check if a session is active
     *
     * @param sessionid the id of the session to check
     * @return the sessionid if active, or "null" if not.
     */
    @WebMethod
    @Path("/checkSession")
    @Produces("text/plain")
    @GET
    public String checkSession(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid) {


        Session s = sessionManager.getSession(sessionid);

        if (s == null) {
            return "";
        } else {
            return sessionid;
        }
    }

    /**
     * Create a new user account
     *
     * @param sessionid the id of a valid session
     * @param eid       the login username (ie jsmith26) of the new user
     * @param firstname the new user's first name
     * @param lastname  the new user's last name
     * @param email     the new user's email address
     * @param type      the type of account (ie registered, guest etc). Should either match one of the !user.template.XXX realms (where XXX is the type) or be blank to inherit the !user.template permission
     * @param password  the password for the new user
     * @return success or exception message
     * @throws RuntimeException This is the preferred method of adding user accounts whereby their internal ID is automatically assigned a UUID.
     */
    @WebMethod
    @Path("/addNewUser")
    @Produces("text/plain")
    @GET
    public String addNewUser(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "firstname", partName = "firstname") @QueryParam("firstname") String firstname,
            @WebParam(name = "lastname", partName = "lastname") @QueryParam("lastname") String lastname,
            @WebParam(name = "email", partName = "email") @QueryParam("email") String email,
            @WebParam(name = "type", partName = "type") @QueryParam("type") String type,
            @WebParam(name = "password", partName = "password") @QueryParam("password") String password) {
        Session session = establishSession(sessionid);

        if (!securityService.isSuperUser()) {
            log.warn("NonSuperUser trying to add accounts: " + session.getUserId());
            throw new RuntimeException("NonSuperUser trying to add accounts: " + session.getUserId());
        }
        try {

            User addeduser = null;
            addeduser = userDirectoryService.addUser(null, eid, firstname, lastname, email, password, type, null);

        } catch (Exception e) {
            log.warn("WS addNewUser(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Create a new user account
     *
     * @param sessionid the id of a valid session
     * @param id        the id of the new user that will be used internally by Sakai
     * @param eid       the login username (ie jsmith26) of the new user
     * @param firstname the new user's first name
     * @param lastname  the new user's last name
     * @param email     the new user's email address
     * @param type      the type of account (ie registered, guest etc). Should either match one of the !user.template.XXX realms (where XXX is the type) or be blank to inherit the !user.template permission
     * @param password  the password for the new user
     * @return success or exception message
     * @throws RuntimeException This form of addUser() should only be used when you need control over the user's internal ID. Otherwise use the other form.
     */
    @WebMethod
    @Path("/addNewUserWithInternalId")
    @Produces("text/plain")
    @GET
    public String addNewUserWithInternalId(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "id", partName = "id") @QueryParam("id") String id,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "firstname", partName = "firstname") @QueryParam("firstname") String firstname,
            @WebParam(name = "lastname", partName = "lastname") @QueryParam("lastname") String lastname,
            @WebParam(name = "email", partName = "email") @QueryParam("email") String email,
            @WebParam(name = "type", partName = "type") @QueryParam("type") String type,
            @WebParam(name = "password", partName = "password") @QueryParam("password") String password) {
        Session session = establishSession(sessionid);

        if (!securityService.isSuperUser()) {
            log.warn("NonSuperUser trying to add accounts: " + session.getUserId());
            throw new RuntimeException("NonSuperUser trying to add accounts: " + session.getUserId());
        }
        try {

            User addeduser = null;
            addeduser = userDirectoryService.addUser(id, eid, firstname, lastname, email, password, type, null);

        } catch (Exception e) {
            log.warn("WS addNewUser(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Remove a user account
     *
     * @param sessionid the id of a valid session
     * @param eid       the login username (ie jsmith26) of the user whose account you want to remove
     * @return success or exception message
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/removeUser")
    @Produces("text/plain")
    @GET
    public String removeUser(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid) {
        Session session = establishSession(sessionid);

        try {
            UserEdit userEdit = null;
            String userid = userDirectoryService.getUserByEid(eid).getId();
            userEdit = userDirectoryService.editUser(userid);
            userDirectoryService.removeUser(userEdit);
        } catch (Exception e) {
            log.error("WS removeUser(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Edit a user's account details
     *
     * @param sessionid the id of a valid session
     * @param eid       the login username (ie jsmith26) of the user you want to edit
     * @param firstname the updated firstname for the user
     * @param lastname  the updated last name for the user
     * @param email     the updated email address for the user
     * @param type      the updated user type
     * @param password  the updated password for the user
     * @return success or exception message
     * @throws RuntimeException Note that if you only want to change individual properties of a user's account like their email address or password, see the related web services.
     */
    @WebMethod
    @Path("/changeUserInfo")
    @Produces("text/plain")
    @GET
    public String changeUserInfo(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "firstname", partName = "firstname") @QueryParam("firstname") String firstname,
            @WebParam(name = "lastname", partName = "lastname") @QueryParam("lastname") String lastname,
            @WebParam(name = "email", partName = "email") @QueryParam("email") String email,
            @WebParam(name = "type", partName = "type") @QueryParam("type") String type,
            @WebParam(name = "password", partName = "password") @QueryParam("password") String password) {
        Session session = establishSession(sessionid);

        UserEdit userEdit = null;
        try {
            String userid = userDirectoryService.getUserByEid(eid).getId();
            userEdit = userDirectoryService.editUser(userid);
            userEdit.setFirstName(firstname);
            userEdit.setLastName(lastname);
            userEdit.setEmail(email);
            userEdit.setType(type);
            userEdit.setPassword(password);
            userDirectoryService.commitEdit(userEdit);
        } catch (Exception e) {
            userDirectoryService.cancelEdit(userEdit);
            log.error("WS removeUser(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Edit a user's eid
     * Commonly needed when a user changes legal name and username changes at institution
     *
     * @param sessionid the id of a valid session
     * @param eid       the current username (ie jsmith26) of the user you want to edit
     * @param mewEid    the new username
     * @return success or exception message
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/changeUserEid")
    @Produces("text/plain")
    @GET
    public String changeUserEid(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "newEid", partName = "newEid") @QueryParam("newEid") String newEid) {
        Session session = establishSession(sessionid);

        try {
            String userid = userDirectoryService.getUserByEid(eid).getId();
            boolean success = userDirectoryService.updateUserEid(userid, newEid);
            return success ? "success" : "failure";
        } catch (Exception e) {
            log.error("WS changeUserEid(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
    }

    /**
     * Edit a user's firstname/lastname
     *
     * @param sessionid the id of a valid session
     * @param eid       the login username (ie jsmith26) of the user you want to edit
     * @param firstname the updated firstname for the user
     * @param lastname  the updated last name for the user
     * @return success or exception message
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/changeUserName")
    @Produces("text/plain")
    @GET
    public String changeUserName(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "firstname", partName = "firstname") @QueryParam("firstname") String firstname,
            @WebParam(name = "lastname", partName = "lastname") @QueryParam("lastname") String lastname) {
        Session session = establishSession(sessionid);

        UserEdit userEdit = null;
        try {
            String userid = userDirectoryService.getUserByEid(eid).getId();
            userEdit = userDirectoryService.editUser(userid);
            userEdit.setFirstName(firstname);
            userEdit.setLastName(lastname);
            userDirectoryService.commitEdit(userEdit);
        } catch (Exception e) {
            userDirectoryService.cancelEdit(userEdit);
            log.error("WS changeUserName(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Edit a user's email address
     *
     * @param sessionid the id of a valid session
     * @param eid       the login username (ie jsmith26) of the user you want to edit
     * @param email     the updated email address for the user
     * @return success or exception message
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/changeUserEmail")
    @Produces("text/plain")
    @GET
    public String changeUserEmail(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "email", partName = "email") @QueryParam("email") String email) {
        Session session = establishSession(sessionid);

        UserEdit userEdit = null;
        try {
            String userid = userDirectoryService.getUserByEid(eid).getId();
            userEdit = userDirectoryService.editUser(userid);
            userEdit.setEmail(email);
            userDirectoryService.commitEdit(userEdit);
        } catch (Exception e) {
            userDirectoryService.cancelEdit(userEdit);
            log.error("WS changeUserEmail(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Edit a user's user type
     *
     * @param sessionid the id of a valid session
     * @param eid       the login username (ie jsmith26) of the user you want to edit
     * @param type      the updated user type. See addNewUser() for an explanation of what this field means
     * @return success or exception message
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/changeUserType")
    @Produces("text/plain")
    @GET
    public String changeUserType(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "type", partName = "type") @QueryParam("type") String type) {
        Session session = establishSession(sessionid);

        UserEdit userEdit = null;
        try {
            String userid = userDirectoryService.getUserByEid(eid).getId();
            userEdit = userDirectoryService.editUser(userid);
            userEdit.setType(type);
            userDirectoryService.commitEdit(userEdit);
        } catch (Exception e) {
            userDirectoryService.cancelEdit(userEdit);
            log.error("WS changeUserType(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Edit a user's password
     *
     * @param sessionid the id of a valid session
     * @param eid       the login username (ie jsmith26) of the user you want to edit
     * @param password  the password for the user
     * @return success or exception message
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/changeUserPassword")
    @Produces("text/plain")
    @GET
    public String changeUserPassword(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "password", partName = "password") @QueryParam("password") String password) {
        Session session = establishSession(sessionid);

        UserEdit userEdit = null;
        try {
            String userid = userDirectoryService.getUserByEid(eid).getId();
            userEdit = userDirectoryService.editUser(userid);
            userEdit.setPassword(password);
            userDirectoryService.commitEdit(userEdit);
        } catch (Exception e) {
            userDirectoryService.cancelEdit(userEdit);
            log.error("WS changeUserPassword(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Edit a user's locale
     *
     * @param sessionid the id of a valid session
     * @param eid       the login username (ie jsmith26) of the user you want to edit
     * @param locale  the locale for the user
     * @return success or exception message
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/changeUserLocale")
    @Produces("text/plain")
    @GET
    public String changeUserLocale(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "locale", partName = "locale") @QueryParam("locale") String locale) {
        Session session = establishSession(sessionid);

        try{
            Locale localeParam = LocaleUtils.toLocale(locale);
            if(!LocaleUtils.isAvailableLocale(localeParam)){
                log.warn("WS changeUserLocale(): Locale not available");
                return "";
            }
        } catch(Exception e){
            log.error("WS changeUserLocale(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }

        UserEdit userEdit = null;
        PreferencesEdit prefs = null;
        try {
            User user = userDirectoryService.getUserByEid(eid);
            
            try {
                prefs = (PreferencesEdit) preferencesService.edit(user.getId());
            } catch (IdUnusedException e1) {
                prefs = (PreferencesEdit) preferencesService.add(user.getId());
            }
            ResourcePropertiesEdit props = prefs.getPropertiesEdit(ResourceLoader.APPLICATION_ID);
            props.addProperty(ResourceLoader.LOCALE_KEY, locale);
            preferencesService.commit(prefs);
        } catch (Exception e) {
            preferencesService.cancel(prefs);
            log.error("WS changeUserLocale(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Get a user's email address based on their session id
     *
     * @param sessionid the session id of the user who's email address you wish to retrieve
     * @return the email address for the user
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/getUserEmailForCurrentUser")
    @Produces("text/plain")
    @GET
    public String getUserEmailForCurrentUser(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid) {
        Session session = establishSession(sessionid);
        User user = userDirectoryService.getCurrentUser();
        return user.getEmail();
    }

    /**
     * Gets the email address for a given user
     * <p/>
     * Differs from original above as that one uses the session to get the email address hence you must know this in advance or be logged in to the web services
     * with that user. This uses a userid as well so we could be logged in as admin and retrieve the email address for any user.
     *
     * @param sessionid the id of a valid session
     * @param userid    the login username (ie jsmith26) of the user you want the email address for
     * @return the email address for the user
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/getUserEmail")
    @Produces("text/plain")
    @GET
    public String getUserEmail(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "userid", partName = "userid") @QueryParam("userid") String userid) {
        Session session = establishSession(sessionid);
        try {
            User user = userDirectoryService.getUserByEid(userid);
            return user.getEmail();
        } catch (Exception e) {
            log.error("WS getUserEmail() failed for user: " + userid + " : " + e.getClass().getName() + " : " + e.getMessage());
            return "";
        }
    }

    /**
     * Get a user's display name based on their session id
     *
     * @param sessionid the session id of the user who's display name you wish to retrieve
     * @return success or exception message
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/getUserDisplayNameForCurrentUser")
    @Produces("text/plain")
    @GET
    public String getUserDisplayNameForCurrentUser(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid) {
        Session session = establishSession(sessionid);
        User user = userDirectoryService.getCurrentUser();
        return user.getDisplayName();
    }

    /**
     * Gets the display name for a given user
     * <p/>
     * Differs from original above as that one uses the session to get the displayname hence you must know this in advance or be logged in to the web services
     * with that user. This uses a userid as well so we could be logged in as admin and retrieve the display name for any user.
     *
     * @param sessionid the id of a valid session
     * @param userid    the login username (ie jsmith26) of the user you want the display name for
     * @return the display name for the user
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/getUserDisplayName")
    @Produces("text/plain")
    @GET
    public String getUserDisplayName(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "userid", partName = "userid") @QueryParam("userid") String userid) {
        Session session = establishSession(sessionid);
        try {
            User user = userDirectoryService.getUserByEid(userid);
            return user.getDisplayName();
        } catch (Exception e) {
            log.error("WS getUserDisplayName() failed for user: " + userid + " : " + e.getClass().getName() + " : " + e.getMessage());
            return "";
        }
    }

    /**
     * Create user-group to specified worksite (as if it had been added in Worksite Setup)
     *
     * @param sessionid    the id of a valid session
     * @param siteid        the id of the site you want the group created in
     * @param grouptitle    the name of the new group
     * @param groupdesc    the description of the new group
     * @return groupid        if successful/exception
     */
    private static final String GROUP_PROP_WSETUP_CREATED = "group_prop_wsetup_created";

    @WebMethod
    @Path("/addGroupToSite")
    @Produces("text/plain")
    @GET
    public String addGroupToSite(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "grouptitle", partName = "grouptitle") @QueryParam("grouptitle") String grouptitle,
            @WebParam(name = "groupdesc", partName = "groupdesc") @QueryParam("groupdesc") String groupdesc) {
        Session session = establishSession(sessionid);
        try {
            Site site = siteService.getSite(siteid);
            Group group = site.addGroup();
            group.setTitle(grouptitle);
            group.setDescription(groupdesc);
            group.getProperties().addProperty(GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());

            // SAK-22849 clear the provider id
            group.setProviderGroupId(null);

            // SAK-22849 clear the role provider id property
            group.getProperties().removeProperty("group_prop_role_providerid");

            siteService.save(site);
            return group.getId();
        } catch (Exception e) {
            log.error("WS addGroupToSite(): " + e.getClass().getName() + " : " + e.getMessage());
            return "";

        }
    }

    /**
     * Add member to specified worksite group
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site that the group is in
     * @param groupid   the id of the group you want to add the user to
     * @param userid    the internal userid of the member to add
     * @return true        if successful/exception
     * <p/>
     * TODO: This is not returning false if it fails (ie if user isn't in site to begin with). SAK-15334
     */
    @WebMethod
    @Path("/addMemberToGroup")
    @Produces("text/plain")
    @GET
    public boolean addMemberToGroup(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "groupid", partName = "groupid") @QueryParam("groupid") String groupid,
            @WebParam(name = "userid", partName = "userid") @QueryParam("userid") String userid) {
        Session session = establishSession(sessionid);
        try {
            Site site = siteService.getSite(siteid);
            Group group = site.getGroup(groupid);
            if (group == null) {
                log.error("addMemberToGroup called with group that does not exist: " + groupid);
                return false;
            }

            Role r = site.getUserRole(userid);
            Member m = site.getMember(userid);
            try {
                group.insertMember(userid, r != null ? r.getId() : "", m != null ? m.isActive() : true, false);
                siteService.saveGroupMembership(site);
            } catch (IllegalStateException e) {
                log.error(".addMemberToGroup: User with id {} cannot be inserted in group with id {} because the group is locked", userid, group.getId());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("WS addMemberToGroup(): " + e.getClass().getName() + " : " + e.getMessage());
            return false;
        }
    }

    /**
     * Get list of groups in a site
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site to retrieve the group list for
     * @return xml doc of the list of groups, title and description
     * @throws RuntimeException returns <exception /> string if exception encountered and logs it
     */
    @WebMethod
    @Path("/getGroupsInSite")
    @Produces("text/plain")
    @GET
    public String getGroupsInSite(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid) {

        Session s = establishSession(sessionid);

        try {

            Site site = siteService.getSite(siteid);

            Document dom = Xml.createDocument();
            Node list = dom.createElement("list");
            dom.appendChild(list);


            for (Iterator iter = site.getGroups().iterator(); iter.hasNext(); ) {
                Group group = (Group) iter.next();

                Node groupNode = dom.createElement("group");

                Node groupId = dom.createElement("id");
                groupId.appendChild(dom.createTextNode(group.getId()));

                Node groupTitle = dom.createElement("title");
                groupTitle.appendChild(dom.createTextNode(group.getTitle()));

                Node groupDesc = dom.createElement("description");
                groupDesc.appendChild(dom.createTextNode(group.getDescription()));

                groupNode.appendChild(groupId);
                groupNode.appendChild(groupTitle);
                groupNode.appendChild(groupDesc);

                Node propertiesNode = dom.createElement("properties");
                groupNode.appendChild(propertiesNode);
                ResourceProperties groupProperties = group.getProperties();
                if (groupProperties != null) {
                    for (Iterator propertiesIter = groupProperties.getPropertyNames(); propertiesIter.hasNext(); ) {
                        Node propertyNode = dom.createElement("property");

                        String propertyName = (String) propertiesIter.next();
                        Object propertyValue = (Object) groupProperties.get(propertyName);

                        Node propertyNameNode = dom.createElement("propertyName");
                        propertyNameNode.appendChild(dom.createTextNode(propertyName));
                        Node propertyValueNode = dom.createElement("propertyValue");
                        propertyValueNode.appendChild(dom.createTextNode(propertyValue.toString()));

                        propertyNode.appendChild(propertyNameNode);
                        propertyNode.appendChild(propertyValueNode);
                        propertiesNode.appendChild(propertyNode);
                    }
                }

                list.appendChild(groupNode);


            }
            return Xml.writeDocumentToString(dom);
        } catch (Exception e) {
            log.error("WS getGroupsInSite(): " + e.getClass().getName() + " : " + e.getMessage());
            return "<exception/>";

        }
    }

    /**
     * Create a new authzgroup (realm)
     *
     * @param sessionid    the id of a valid session
     * @param authzgroupid the id of the new authzgroup
     * @return success or exception message
     */

    @WebMethod
    @Path("/addNewAuthzGroup")
    @Produces("text/plain")
    @GET
    public String addNewAuthzGroup(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid) {
        Session session = establishSession(sessionid);

        try {

            AuthzGroup authzgroup = null;
            authzgroup = authzGroupService.addAuthzGroup(authzgroupid);
            authzGroupService.save(authzgroup);

        } catch (Exception e) {
            log.error("WS addNewAuthzGroup(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Remove an authzgroup (realm)
     *
     * @param sessionid    the id of a valid session
     * @param authzgroupid the id of the authzgroup to remove
     * @return success or exception message
     */
    @WebMethod
    @Path("/removeAuthzGroup")
    @Produces("text/plain")
    @GET
    public String removeAuthzGroup(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid) {
        Session session = establishSession(sessionid);

        try {

            AuthzGroup authzgroup = authzGroupService.getAuthzGroup(authzgroupid);
            authzGroupService.removeAuthzGroup(authzgroup);

        } catch (Exception e) {
            log.error("WS removeAuthzGroup(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Add a role to an authzgroup (realm)
     *
     * @param sessionid    the id of a valid session
     * @param authzgroupid the id of the authzgroup to add the role to
     * @param roleid       the id of the role to add
     * @param description  the description for the new role
     * @return success or exception message
     */
    @WebMethod
    @Path("/addNewRoleToAuthzGroup")
    @Produces("text/plain")
    @GET
    public String addNewRoleToAuthzGroup(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid,
            @WebParam(name = "roleid", partName = "roleid") @QueryParam("roleid") String roleid,
            @WebParam(name = "description", partName = "description") @QueryParam("description") String description) {
        Session session = establishSession(sessionid);

        try {

            AuthzGroup authzgroup = authzGroupService.getAuthzGroup(authzgroupid);
            Role role = authzgroup.addRole(roleid);
            role.setDescription(description);
            authzGroupService.save(authzgroup);

        } catch (Exception e) {
            log.error("WS addNewRoleToAuthzGroup(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Remove all roles that exist in an authzgroup (realm)
     *
     * @param sessionid    the id of a valid session
     * @param authzgroupid the id of the authzgroup to remove the roles from
     * @return success or exception message
     */
    @WebMethod
    @Path("/removeAllRolesFromAuthzGroup")
    @Produces("text/plain")
    @GET
    public String removeAllRolesFromAuthzGroup(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid) {
        Session session = establishSession(sessionid);

        try {

            AuthzGroup authzgroup = authzGroupService.getAuthzGroup(authzgroupid);
            authzgroup.removeRoles();
            authzGroupService.save(authzgroup);

        } catch (Exception e) {
            log.error("WS removeAllRolesFromAuthzGroup(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Remove a role from an authzgroup (realm)
     *
     * @param sessionid    the id of a valid session
     * @param authzgroupid the id of the authzgroup to remove the role from
     * @param roleid       the id of the role to remove
     * @return success or exception message
     * <p/>
     * Note: This web service has been modified, see SAK-15334
     */
    @WebMethod
    @Path("/removeRoleFromAuthzGroup")
    @Produces("text/plain")
    @GET
    public String removeRoleFromAuthzGroup(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid,
            @WebParam(name = "roleid", partName = "roleid") @QueryParam("roleid") String roleid) {
        Session session = establishSession(sessionid);

        try {

            AuthzGroup authzgroup = authzGroupService.getAuthzGroup(authzgroupid);
            //check Role exists
            Role role = authzgroup.getRole(roleid);
            if (role == null) {
                //log warning, but still continue so as not to break any existing implementations
                log.warn("WS removeRoleFromAuthzGroup(): authzgroup: " + authzgroupid + " does not contain role: " + roleid);
            }
            authzgroup.removeRole(roleid);
            authzGroupService.save(authzgroup);

        } catch (Exception e) {
            log.error("WS removeRoleFromAuthzGroup(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Add a function to a role in an authzgroup (realm)
     *
     * @param sessionid    the id of a valid session
     * @param authzgroupid the id of the authzgroup that the role is in
     * @param roleid       the id of the role to add a function to
     * @param functionname the name of the new function eg content.new
     * @return success or exception message
     * <p/>
     * TODO: fix for if the functionname doesn't exist, it is still returning success - SAK-15334
     */
    @WebMethod
    @Path("/allowFunctionForRole")
    @Produces("text/plain")
    @GET
    public String allowFunctionForRole(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid,
            @WebParam(name = "roleid", partName = "roleid") @QueryParam("roleid") String roleid,
            @WebParam(name = "functionname", partName = "functionname") @QueryParam("functionname") String functionname) {
        Session session = establishSession(sessionid);

        // check that ONLY super user's are accessing this (see SAK-18494)
        if (!securityService.isSuperUser(session.getUserId())) {
            log.warn("WS allowFunctionForRole(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS allowFunctionForRole(): Permission denied. Restricted to super users.");
        }

        try {
            AuthzGroup authzgroup = authzGroupService.getAuthzGroup(authzgroupid);
            Role role = authzgroup.getRole(roleid);
            role.allowFunction(functionname);
            authzGroupService.save(authzgroup);
        } catch (Exception e) {
            log.error("WS allowFunctionForRole(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Remove all functions from a role in an authzgroup (realm)
     *
     * @param sessionid    the id of a valid session
     * @param authzgroupid the id of the authzgroup that the role is in
     * @param roleid       the id of the role to remove the functions from
     * @return success or exception message
     */
    @WebMethod
    @Path("/disallowAllFunctionsForRole")
    @Produces("text/plain")
    @GET
    public String disallowAllFunctionsForRole(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid,
            @WebParam(name = "roleid", partName = "roleid") @QueryParam("roleid") String roleid) {
        Session session = establishSession(sessionid);

        try {

            AuthzGroup authzgroup = authzGroupService.getAuthzGroup(authzgroupid);
            Role role = authzgroup.getRole(roleid);
            role.disallowAll();
            authzGroupService.save(authzgroup);

        } catch (Exception e) {
            log.error("WS disallowAllFunctionsForRole(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Remove a function from a role in an authzgroup (realm)
     *
     * @param sessionid    the id of a valid session
     * @param authzgroupid the id of the authzgroup that the role is in
     * @param roleid       the id of the role to remove the function from
     * @param functionname the name of the function to remove
     * @return success or exception message
     * <p/>
     * TODO: fix for if the functionname doesn't exist, it is still returning success - SAK-15334
     */
    @WebMethod
    @Path("/disallowFunctionForRole")
    @Produces("text/plain")
    @GET
    public String disallowFunctionForRole(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid,
            @WebParam(name = "roleid", partName = "roleid") @QueryParam("roleid") String roleid,
            @WebParam(name = "functionname", partName = "functionname") @QueryParam("functionname") String functionname) {
        Session session = establishSession(sessionid);

        try {

            AuthzGroup authzgroup = authzGroupService.getAuthzGroup(authzgroupid);
            Role role = authzgroup.getRole(roleid);
            role.disallowFunction(functionname);
            authzGroupService.save(authzgroup);

        } catch (Exception e) {
            log.error("WS disallowFunctionForRole(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Edit a role's description
     *
     * @param sessionid    the id of a valid session
     * @param authzgroupid the id of the authzgroup that the role exists in
     * @param roleid       the id of the role to edit
     * @param description  the updated description for the role
     * @return success or exception message
     */
    @WebMethod
    @Path("/setRoleDescription")
    @Produces("text/plain")
    @GET
    public String setRoleDescription(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid,
            @WebParam(name = "roleid", partName = "roleid") @QueryParam("roleid") String roleid,
            @WebParam(name = "description", partName = "description") @QueryParam("description") String description) {
        Session session = establishSession(sessionid);

        try {

            AuthzGroup authzgroup = authzGroupService.getAuthzGroup(authzgroupid);
            Role role = authzgroup.getRole(roleid);
            role.setDescription(description);
            authzGroupService.save(authzgroup);

        } catch (Exception e) {
            log.error("WS setRoleDescription(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Add a user to an authgroup with the given role
     *
     * @param sessionid    the id of a valid session
     * @param eid          the login username (ie jsmith26) of the user you want to add
     * @param authzgroupid the id of the authzgroup to add the user to
     * @param roleid       the id of the role to add the user to in the authzgroup
     * @return success or exception message
     * <p/>
     * Note: This web service has been modified, see SAK-15334
     */
    @WebMethod
    @Path("/addMemberToAuthzGroupWithRole")
    @Produces("text/plain")
    @GET
    public String addMemberToAuthzGroupWithRole(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid,
            @WebParam(name = "roleid", partName = "roleid") @QueryParam("roleid") String roleid) {
        Session session = establishSession(sessionid);

        try {

            AuthzGroup authzgroup = authzGroupService.getAuthzGroup(authzgroupid);
            //check Role exists
            Role role = authzgroup.getRole(roleid);
            if (role == null) {
                //log warning and return error as it would return success even if it failed
                log.error("WS addMemberToAuthzGroupWithRole(): authzgroup: " + authzgroupid + " does not contain role: " + roleid);
                return "WS addMemberToAuthzGroupWithRole(): authzgroup: " + authzgroupid + " does not contain role: " + roleid;
            }

            String userid = userDirectoryService.getUserByEid(eid).getId();
            authzgroup.addMember(userid, roleid, true, false);
            authzGroupService.save(authzgroup);

        } catch (Exception e) {
            log.error("WS addMemberToAuthzGroupWithRole(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Remove a user from an authgroup
     *
     * @param sessionid    the id of a valid session
     * @param eid          the login username (ie jsmith26) of the user you want to remove
     * @param authzgroupid the id of the authzgroup to remove the user from
     * @return success or exception message
     */
    @WebMethod
    @Path("/removeMemberFromAuthzGroup")
    @Produces("text/plain")
    @GET
    public String removeMemberFromAuthzGroup(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid) {
        Session session = establishSession(sessionid);

        try {

            AuthzGroup authzgroup = authzGroupService.getAuthzGroup(authzgroupid);
            String userid = userDirectoryService.getUserByEid(eid).getId();
            authzgroup.removeMember(userid);
            authzGroupService.save(authzgroup);

        } catch (Exception e) {
            log.error("WS removeMemberFromAuthzGroup(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Remove all users from an authgroup
     *
     * @param sessionid    the id of a valid session
     * @param authzgroupid the id of the authzgroup to remove the users from
     * @return success or exception message
     */
    @WebMethod
    @Path("/removeAllMembersFromAuthzGroup")
    @Produces("text/plain")
    @GET
    public String removeAllMembersFromAuthzGroup(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid) {
        Session session = establishSession(sessionid);

        try {

            AuthzGroup realmEdit = authzGroupService.getAuthzGroup(authzgroupid);
            realmEdit.removeMembers();
            authzGroupService.save(realmEdit);

        } catch (Exception e) {
            log.error("WS removeAllMembersFromAuthzGroup(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Set the role that allows maintenance on the given authgroup
     *
     * @param sessionid    the id of a valid session
     * @param authzgroupid the id of the authzgroup to edit
     * @param roleid       the id of the role to to set
     * @return success or exception message
     * <p/>
     * Note: This web service has been modified, see SAK-15334
     */
    @WebMethod
    @Path("/setRoleForAuthzGroupMaintenance")
    @Produces("text/plain")
    @GET
    public String setRoleForAuthzGroupMaintenance(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid,
            @WebParam(name = "roleid", partName = "roleid") @QueryParam("roleid") String roleid) {
        Session session = establishSession(sessionid);

        try {

            AuthzGroup authzgroup = authzGroupService.getAuthzGroup(authzgroupid);

            //check Role exists
            Role role = authzgroup.getRole(roleid);
            if (role == null) {
                log.warn("WS setRoleForAuthzGroupMaintenance(): authzgroup: " + authzgroupid + " does not contain role: " + roleid);
                return "WS setRoleForAuthzGroupMaintenance(): authzgroup: " + authzgroupid + " does not contain role: " + roleid;
            }

            authzgroup.setMaintainRole(roleid);
            authzGroupService.save(authzgroup);

        } catch (Exception e) {
            log.error("WS setRoleForAuthzGroupMaintenance(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Add a user to a site with a given role
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site to add the user to
     * @param eid       the login username (ie jsmith26) of the user you want to add to the site
     * @param roleid    the id of the role to to give the user in the site
     * @return success or exception message
     * <p/>
     * TODO: fix for if the role doesn't exist in the site, it is still returning success - SAK-15334
     */
    @WebMethod
    @Path("/addMemberToSiteWithRole")
    @Produces("text/plain")
    @GET
    public String addMemberToSiteWithRole(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "roleid", partName = "roleid") @QueryParam("roleid") String roleid) {
        Session session = establishSession(sessionid);

        try {
            Site site = siteService.getSite(siteid);
            String userid = userDirectoryService.getUserByEid(eid).getId();
            site.addMember(userid, roleid, true, false);
            siteService.saveSiteMembership(site);
        } catch (Exception e) {
            log.error("WS addMemberToSiteWithRole(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Add a user to a site with a given role
     *
     * @param 	sessionid 	the id of a valid session
     * @param 	siteid 		the id of the site to add the user to
     * @param 	eids		the login usernames (ie jsmith26) separated by commas of the user you want to add to the site
     * @param 	roleid		the id of the role to to give the user in the site
     * @return				success or exception message
     *
     * TODO: fix for if the role doesn't exist in the site, it is still returning success - SAK-15334
     */
    @WebMethod
    @Path("/addMemberToSiteWithRoleBatch")
    @Produces("text/plain")
    @GET
    public String addMemberToSiteWithRoleBatch(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "eids", partName = "eids") @QueryParam("eids") String eids,
            @WebParam(name = "roleid", partName = "roleid") @QueryParam("roleid") String roleid) {

        Session session = establishSession(sessionid);

        if (!securityService.isSuperUser(session.getUserId())) {
            log.warn("NonSuperUser trying to addMemberToSiteWithRoleBatch: " + session.getUserId());
            throw new RuntimeException("NonSuperUser trying to addMemberToSiteWithRoleBatch: " + session.getUserId());
        }

        try {
            Site site = siteService.getSite(siteid);
            List<String> eidsList = Arrays.asList(eids.split(","));
            for (String eid : eidsList) {
                String userid = userDirectoryService.getUserByEid(eid).getId();
                site.addMember(userid,roleid,true,false);
            }
            siteService.save(site);
        }
        catch (Exception e) {
            log.error("WS addMemberToSiteWithRoleBatch(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Create a new site
     *
     * @param sessionid   the id of a valid session
     * @param siteid      the id of the new site (ie test123)
     * @param title       the title of the new site
     * @param description the full description for the new site
     * @param shortdesc   the short description for the new site
     * @param iconurl     the url to an icon for the site (on the default skin should not be more than 100px wide)
     * @param infourl     the url to a page of information about the site (this is added to the Site Information portlet)
     * @param joinable    should this site be joinable?
     * @param joinerrole  if joinable, the role to assign users that join this site
     * @param published   should this site be made available to participants of the site now?
     * @param publicview  should this site be shown on the public list of sites?
     * @param skin        the id of the skin for this site, from the list in /library/skin/SKIN
     * @param type        the type of site ie project, course, etc, or any type defined as !site.template.TYPE. If blank will inherit !site.template roles/permissions
     * @return success or exception message
     * <p/>
     * Note that this will create an empty site with no tools. If you would like to create a site from a template, ie inherit its tool structure (not content), see copySite()
     */
    @WebMethod
    @Path("/addNewSite")
    @Produces("text/plain")
    @GET
    public String addNewSite(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "title", partName = "title") @QueryParam("title") String title,
            @WebParam(name = "description", partName = "description") @QueryParam("description") String description,
            @WebParam(name = "shortdesc", partName = "shortdesc") @QueryParam("shortdesc") String shortdesc,
            @WebParam(name = "iconurl", partName = "iconurl") @QueryParam("iconurl") String iconurl,
            @WebParam(name = "infourl", partName = "infourl") @QueryParam("infourl") String infourl,
            @WebParam(name = "joinable", partName = "joinable") @QueryParam("joinable") boolean joinable,
            @WebParam(name = "joinerrole", partName = "joinerrole") @QueryParam("joinerrole") String joinerrole,
            @WebParam(name = "published", partName = "published") @QueryParam("published") boolean published,
            @WebParam(name = "publicview", partName = "publicview") @QueryParam("publicview") boolean publicview,
            @WebParam(name = "skin", partName = "skin") @QueryParam("skin") String skin,
            @WebParam(name = "type", partName = "type") @QueryParam("type") String type) {
        Session session = establishSession(sessionid);

        try {

            // check description
            if (description != null) {
                StringBuilder alertMsg = new StringBuilder();
                description = FormattedText.processFormattedText(description, alertMsg);
                if (description == null) {
                    throw new RuntimeException("Site description markup rejected: " + alertMsg.toString());
                }
            }

            Site siteEdit = null;
            siteEdit = siteService.addSite(siteid, type);
            siteEdit.setTitle(title);
            siteEdit.setDescription(description);
            siteEdit.setShortDescription(shortdesc);
            siteEdit.setIconUrl(iconurl);
            siteEdit.setInfoUrl(infourl);
            siteEdit.setJoinable(joinable);
            siteEdit.setJoinerRole(joinerrole);
            siteEdit.setPublished(published);
            siteEdit.setPubView(publicview);
            siteEdit.setSkin(skin);
            siteEdit.setType(type);
            siteService.save(siteEdit);

        } catch (Exception e) {
            log.error("WS addNewSite(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Remove a site
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site to remove
     * @return success or exception message
     */
    @WebMethod
    @Path("/removeSite")
    @Produces("text/plain")
    @GET
    public String removeSite(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid) {
        Session session = establishSession(sessionid);

        try {

            Site siteEdit = null;
            siteEdit = siteService.getSite(siteid);
            siteService.removeSite(siteEdit);

        } catch (Exception e) {
            log.error("WS removeSite(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Create a new site based on another site. This will copy its tool structure, but not its content
     *
     * @param sessionid    the id of a valid session
     * @param siteidtocopy the id of the site to base this new site on
     * @param newsiteid    the id of the new site (ie test123)
     * @param title        the title of the new site
     * @param description  the full description for the new site
     * @param shortdesc    the short description for the new site
     * @param iconurl      the url to an icon for the site (on the default skin should not be more than 100px wide)
     * @param infourl      the url to a page of information about the site (this is added to the Site Information portlet)
     * @param joinable     should this site be joinable?
     * @param joinerrole   if joinable, the role to assign users that join this site
     * @param published    should this site be made available to participants of the site now?
     * @param publicview   should this site be shown on the public list of sites?
     * @param skin         the id of the skin for this site, from the list in /library/skin/SKIN
     * @param type         the type of site ie project, course, etc, or any type defined as !site.template.TYPE. If blank will inherit !site.template roles/permissions
     * @return success or exception message
     */
    @WebMethod
    @Path("/copySite")
    @Produces("text/plain")
    @GET
    public String copySite(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteidtocopy", partName = "siteidtocopy") @QueryParam("siteidtocopy") String siteidtocopy,
            @WebParam(name = "newsiteid", partName = "newsiteid") @QueryParam("newsiteid") String newsiteid,
            @WebParam(name = "title", partName = "title") @QueryParam("title") String title,
            @WebParam(name = "description", partName = "description") @QueryParam("description") String description,
            @WebParam(name = "shortdesc", partName = "shortdesc") @QueryParam("shortdesc") String shortdesc,
            @WebParam(name = "iconurl", partName = "iconurl") @QueryParam("iconurl") String iconurl,
            @WebParam(name = "infourl", partName = "infourl") @QueryParam("infourl") String infourl,
            @WebParam(name = "joinable", partName = "joinable") @QueryParam("joinable") boolean joinable,
            @WebParam(name = "joinerrole", partName = "joinerrole") @QueryParam("joinerrole") String joinerrole,
            @WebParam(name = "published", partName = "published") @QueryParam("published") boolean published,
            @WebParam(name = "publicview", partName = "publicview") @QueryParam("publicview") boolean publicview,
            @WebParam(name = "skin", partName = "skin") @QueryParam("skin") String skin,
            @WebParam(name = "type", partName = "type") @QueryParam("type") String type) {
        Session session = establishSession(sessionid);

        try {

            Site site = siteService.getSite(siteidtocopy);

            // If not admin, check maintainer membership in the source site
            if (!securityService.isSuperUser(session.getUserId()) &&
                    !securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference())) {
                log.warn("WS copySite(): Permission denied. Must be super user to copy a site in which you are not a maintainer.");
                throw new RuntimeException("WS copySite(): Permission denied. Must be super user to copy a site in which you are not a maintainer.");
            }

            // check description
            if (description != null) {
                StringBuilder alertMsg = new StringBuilder();
                description = FormattedText.processFormattedText(description, alertMsg);
                if (description == null) {
                    throw new RuntimeException("Site description markup rejected: " + alertMsg.toString());
                }
            }

            Site siteEdit = siteService.addSite(newsiteid, site);
            siteEdit.setTitle(title);
            siteEdit.setDescription(description);
            siteEdit.setShortDescription(shortdesc);
            siteEdit.setIconUrl(iconurl);
            siteEdit.setInfoUrl(infourl);
            siteEdit.setJoinable(joinable);
            siteEdit.setJoinerRole(joinerrole);
            siteEdit.setPublished(published);
            siteEdit.setPubView(publicview);
            siteEdit.setSkin(skin);
            siteEdit.setType(type);
            siteService.save(siteEdit);

        } catch (Exception e) {
            log.error("WS copySite(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Create a new page in a site. A page holds one or more tools and is shown in the main navigation section. You will still need to add tools to this page.
     *
     * @param sessionid  the id of a valid session
     * @param siteid     the id of the site to add the page to
     * @param pagetitle  the title of the new page
     * @param pagelayout single or double column (0 or 1). Any other value will revert to 0.
     * @return success or exception message
     */
    @WebMethod
    @Path("/addNewPageToSite")
    @Produces("text/plain")
    @GET
    public String addNewPageToSite(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "pagetitle", partName = "pagetitle") @QueryParam("pagetitle") String pagetitle,
            @WebParam(name = "pagelayout", partName = "pagelayout") @QueryParam("pagelayout") int pagelayout) {
        Session session = establishSession(sessionid);

        try {

            Site siteEdit = null;
            SitePage sitePageEdit = null;
            siteEdit = siteService.getSite(siteid);
            sitePageEdit = siteEdit.addPage();
            sitePageEdit.setTitle(pagetitle);
            sitePageEdit.setLayout(pagelayout);
            siteService.save(siteEdit);

        } catch (Exception e) {
            log.error("WS addNewPageToSite(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Remove a page from a site
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site to remove the page from
     * @param pagetitle the title of the page to remove
     * @return success or exception message
     * <p/>
     * TODO: fix for if the page title is blank it removes nothing and is still returning success - SAK-15334
     * TODO: fix for ConcurrentModficationException being thrown - SAK-15337. Is this because it removes via pagetitle but can allow multiple page titles of the same name?
     */
    @WebMethod
    @Path("/removePageFromSite")
    @Produces("text/plain")
    @GET
    public String removePageFromSite(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "pagetitle", partName = "pagetitle") @QueryParam("pagetitle") String pagetitle) {
        Session session = establishSession(sessionid);

        try {

            Site siteEdit = null;
            siteEdit = siteService.getSite(siteid);
            ArrayList remPages = new ArrayList();
            List pageEdits = siteEdit.getPages();
            for (Iterator i = pageEdits.iterator(); i.hasNext(); ) {
                SitePage pageEdit = (SitePage) i.next();
                if (pageEdit.getTitle().equals(pagetitle)) {
                    remPages.add(pageEdit);
                }
            }

            for (Iterator i = remPages.iterator(); i.hasNext(); ) {
                SitePage pageEdit = (SitePage) i.next();
                siteEdit.removePage(pageEdit);
            }

            siteService.save(siteEdit);

        } catch (Exception e) {
            log.error("WS removePageFromSite(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }


    /**
     * Add a new tool to a page in a site
     *
     * @param sessionid   the id of a valid session
     * @param siteid      the id of the site to add the page to
     * @param pagetitle   the title of the page to add the tool to
     * @param tooltitle   the title of the new tool (ie Resources)
     * @param toolid      the id of the new tool (ie sakai.resources)
     * @param layouthints where on the page this tool should be added, in 'row, col' and 0 based, ie first column, first tool='0,0'; Second column third tool = '1,2'
     * @return success or exception message
     * <p/>
     * TODO: fix for if any values (except sessionid and siteid) are blank or invalid, it is still returning success - SAK-15334
     */
    @WebMethod
    @Path("/addNewToolToPage")
    @Produces("text/plain")
    @GET
    public String addNewToolToPage(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "pagetitle", partName = "pagetitle") @QueryParam("pagetitle") String pagetitle,
            @WebParam(name = "tooltitle", partName = "tooltitle") @QueryParam("tooltitle") String tooltitle,
            @WebParam(name = "toolid", partName = "toolid") @QueryParam("toolid") String toolid,
            @WebParam(name = "layouthints", partName = "layouthints") @QueryParam("layouthints") String layouthints) {
        Session session = establishSession(sessionid);

        try {

            Site siteEdit = siteService.getSite(siteid);

            // Check that the tool is visible (not stealthed) and available for this site type (category)
            if (!securityService.isSuperUser(session.getUserId())) {

                Set categories = new HashSet<String>();
                Set<Tool> visibleTools = toolManager.findTools(categories, null);

                boolean toolVisible = false;
                for (Tool tool : visibleTools) {
                    if (tool.getId().equals(toolid)) {
                        toolVisible = true;
                    }
                }

                if (!toolVisible) {
                    log.warn("WS addNewToolToPage(): Permission denied. Must be super user to add a stealthed tool to a site.");
                    throw new RuntimeException("WS addNewToolToPage(): Permission denied. Must be super user to add a stealthed tool to a site.");
                }

                categories.add(siteEdit.getType());
                Set<Tool> availableTools = toolManager.findTools(categories, null);

                boolean toolAvailable = false;
                for (Tool tool : availableTools) {
                    if (tool.getId().equals(toolid)) {
                        toolAvailable = true;
                    }
                }

                if (!toolAvailable) {
                    log.warn("WS addNewToolToPage(): Permission denied. Must be super user to add a tool which is not available for this site type.");
                    throw new RuntimeException("WS addNewToolToPage(): Permission denied. Must be super user to add a tool which is not available for this site type.");
                }
            }

            List pageEdits = siteEdit.getPages();
            for (Iterator i = pageEdits.iterator(); i.hasNext(); ) {
                SitePage pageEdit = (SitePage) i.next();
                if (pageEdit.getTitle().equals(pagetitle)) {
                    ToolConfiguration tool = pageEdit.addTool();
                    Tool t = tool.getTool();

                    tool.setTool(toolid, toolManager.getTool(toolid));
                    tool.setTitle(tooltitle);
                    //toolEdit.setTitle(tooltitle);
                    //toolEdit.setToolId(toolid);
                    tool.setLayoutHints(layouthints);
                }
            }
            siteService.save(siteEdit);

        } catch (Exception e) {
            log.error("WS addNewToolToPage(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }


    /**
     * Add a property to a tool on a page in a site
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site to add the page to
     * @param pagetitle the title of the page the tool exists in
     * @param tooltitle the title of the tool to add the property to
     * @param propname  the name of the property
     * @param propvalue the value of the property
     * @return success or exception message
     * <p/>
     * TODO: fix for if any values (except sessionid and siteid) are blank or invalid, it is still returning success - SAK-15334
     */
    @WebMethod
    @Path("/addConfigPropertyToTool")
    @Produces("text/plain")
    @GET
    public String addConfigPropertyToTool(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "pagetitle", partName = "pagetitle") @QueryParam("pagetitle") String pagetitle,
            @WebParam(name = "tooltitle", partName = "tooltitle") @QueryParam("tooltitle") String tooltitle,
            @WebParam(name = "propname", partName = "propname") @QueryParam("propname") String propname,
            @WebParam(name = "propvalue", partName = "propvalue") @QueryParam("propvalue") String propvalue) {
        Session session = establishSession(sessionid);

        try {

            Site siteEdit = siteService.getSite(siteid);
            List pageEdits = siteEdit.getPages();
            for (Iterator i = pageEdits.iterator(); i.hasNext(); ) {
                SitePage pageEdit = (SitePage) i.next();
                if (pageEdit.getTitle().equals(pagetitle)) {
                    List toolEdits = pageEdit.getTools();
                    for (Iterator j = toolEdits.iterator(); j.hasNext(); ) {
                        ToolConfiguration tool = (ToolConfiguration) j.next();
                        Tool t = tool.getTool();
                        if (tool.getTitle().equals(tooltitle)) {
                            Properties propsedit = tool.getPlacementConfig();
                            propsedit.setProperty(propname, propvalue);
                        }
                    }
                }
            }
            siteService.save(siteEdit);

        } catch (Exception e) {
            log.error("WS addConfigPropertyToTool(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Add a property to a page in a site
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site to add the page to
     * @param pagetitle the title of the page the tool exists in
     * @param propname  the name of the property
     * @param propvalue the value of the property
     * @return success or exception message
     * <p/>
     * TODO: fix for if any values (except sessionid and siteid) are blank or invalid, it is still returning success - SAK-15334
     */
    @WebMethod
    @Path("/addConfigPropertyToPage")
    @Produces("text/plain")
    @GET
    public String addConfigPropertyToPage(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "pagetitle", partName = "pagetitle") @QueryParam("pagetitle") String pagetitle,
            @WebParam(name = "propname", partName = "propname") @QueryParam("propname") String propname,
            @WebParam(name = "propvalue", partName = "propvalue") @QueryParam("propvalue") String propvalue) {
        Session session = establishSession(sessionid);

        try {

            Site siteEdit = siteService.getSite(siteid);
            List pageEdits = siteEdit.getPages();
            for (Iterator i = pageEdits.iterator(); i.hasNext(); ) {
                SitePage pageEdit = (SitePage) i.next();
                if (pageEdit.getTitle().equals(pagetitle)) {
                    ResourcePropertiesEdit propsedit = pageEdit.getPropertiesEdit();
                    propsedit.addProperty(propname, propvalue); // is_home_page = true
                }
            }
            siteService.save(siteEdit);

        } catch (Exception e) {
            log.error("WS addConfigPropertyToPage(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }


    /**
     * Check if a user exists (either as an account in Sakai or in any external provider)
     *
     * @param sessionid the id of a valid session
     * @param eid       the login username (ie jsmith26) of the user to check for
     * @return true/false
     */
    @WebMethod
    @Path("/checkForUser")
    @Produces("text/plain")
    @GET
    public boolean checkForUser(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid) {
        Session s = establishSession(sessionid);

        try {
            User u = null;
            String userid = userDirectoryService.getUserByEid(eid).getId();
            u = userDirectoryService.getUser(userid);
            if (u != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("WS checkForUser(): " + e.getClass().getName() + " : " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a site exists
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site to check for
     * @return true/false
     */
    @WebMethod
    @Path("/checkForSite")
    @Produces("text/plain")
    @GET
    public boolean checkForSite(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid) {
        Session s = establishSession(sessionid);

        try {
            Site site = null;
            site = siteService.getSite(siteid);
            if (site != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("WS checkForSite(): " + e.getClass().getName() + " : " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a user exists in the authzgroup (or site) with the given role
     *
     * @param sessionid    the id of a valid session
     * @param eid          the login username (ie jsmith26) of the user to check for
     * @param authzgroupid the id of the authzgroup to check in. If this is a site it should be of the form /site/SITEID
     * @param role         the id of the role for the user in the site
     * @return true/false
     */
    @WebMethod
    @Path("/checkForMemberInAuthzGroupWithRole")
    @Produces("text/plain")
    @GET
    public boolean checkForMemberInAuthzGroupWithRole(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid,
            @WebParam(name = "role", partName = "role") @QueryParam("role") String role) {
        Session s = establishSession(sessionid);

        if (ADMIN_SITE_REALM.equalsIgnoreCase(authzgroupid) && !securityService.isSuperUser(s.getUserId())) {
            log.warn("WS checkForMemberInAuthzGroupWithRole(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS checkForMemberInAuthzGroupWithRole(): Permission denied. Restricted to super users.");
        }

        try {
            AuthzGroup authzgroup = null;
            authzgroup = authzGroupService.getAuthzGroup(authzgroupid);
            if (authzgroup == null) {
                return false;
            } else {
                String userid = userDirectoryService.getUserByEid(eid).getId();
                return authzgroup.hasRole(userid, role);
            }
        } catch (Exception e) {
            log.error("WS checkForMemberInAuthzGroupWithRole(): " + e.getClass().getName() + " : " + e.getMessage());
            return false;
        }
    }

    /**
     * Return XML document listing all sites user has read or write access based on their session id.
     *
     * @param sessionid the session id of a user who's list of sites you want to retrieve
     * @return xml or an empty list <list/>. The return XML format is below:
     * <list>
     * <item>
     * <siteId>!admin</siteId>
     * <siteTitle>Administration Workspace</siteTitle>
     * </item>
     * <item>
     * ...
     * </item>
     * ...
     * </list>
     */
    @WebMethod
    @Path("/getSitesCurrentUserCanAccess")
    @Produces("text/plain")
    @GET
    public String getSitesCurrentUserCanAccess(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid) {
        Session s = establishSession(sessionid);

        try {
            List allSites = siteService.getSites(SelectionType.ACCESS, null, null,
                    null, SortType.TITLE_ASC, null);
            List moreSites = siteService.getSites(SelectionType.UPDATE, null, null,
                    null, SortType.TITLE_ASC, null);

            if ((allSites == null || moreSites == null) || (allSites.size() == 0 && moreSites.size() == 0)) {
                return "<list/>";
            }

            // Remove duplicates and combine two lists
            allSites.removeAll(moreSites);
            allSites.addAll(moreSites);

            return getSiteListXml(allSites);
            
        } catch (Exception e) {
            log.error("WS getSitesCurrentUserCanAccess(): " + e.getClass().getName() + " : " + e.getMessage());
            return "<exception/>";
        }
    }

    /**
     * Return XML document listing all sites the given user has read or write access to.
     *
     * @param sessionid the session id of a super user
     * @param userid    eid (eg jsmith26) if the user you want the list for
     * @return
     * @		if not super user or any other error occurs from main method
     */
    @WebMethod
    @Path("/getSitesUserCanAccess")
    @Produces("text/plain")
    @GET
    public String getSitesUserCanAccess(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "userid", partName = "userid") @QueryParam("userid") String userid) {

        //get a session for the other user, reuse if possible
        String newsessionid = getSessionForUser(sessionid, userid, true);

        //might be an exception that was returned, so check session is valid.
        Session session = establishSession(newsessionid);

        //ok, so hand over to main method to get the list for us
        return getSitesCurrentUserCanAccess(newsessionid);

    }

    /**
     * Return XML document listing all sites user has read or write access based on their session id, including My Workspace sites
     *
     * @param sessionid the session id of a user who's list of sites you want to retrieve
     * @return xml or an empty list <list/>. The return XML format is below:
     * <list>
     * <item>
     * <siteId>!admin</siteId>
     * <siteTitle>Administration Workspace</siteTitle>
     * </item>
     * <item>
     * ...
     * </item>
     * ...
     * </list>
     */

    @WebMethod
    @Path("/getAllSitesForCurrentUser")
    @Produces("text/plain")
    @GET
    public String getAllSitesForCurrentUser(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid) {
        Session s = establishSession(sessionid);

        try {
            List<Site> allSites = siteService.getSites(SelectionType.ACCESS, null, null, null, SortType.TITLE_ASC, null);
            List<Site> moreSites = siteService.getSites(SelectionType.UPDATE, null, null, null, SortType.TITLE_ASC, null);

            // Remove duplicates and combine
            allSites.removeAll(moreSites);
            allSites.addAll(moreSites);

            try {
                Site myWorkspace = siteService.getSiteVisit(siteService.getUserSiteId(s.getUserId()));
                allSites.add(myWorkspace);
            } catch (Exception e) {
                log.error("WS getAllSitesForCurrentUser(): cannot add My Workspace site: " + e.getClass().getName() + " : " + e.getMessage());
            }

            if (allSites == null || (allSites.size() == 0)) {
                return "<list/>";
            }

            return this.getSiteListXml(allSites);
        } catch (Exception e) {
            log.error("WS getAllSitesForCurrentUser(): " + e.getClass().getName() + " : " + e.getMessage());
            return "<exception/>";
        }
    }


    /**
     * Return XML document listing all sites user has read or write access based on their session id, including My Workspace sites
     *
     * @param sessionid the session id of a super user
     * @param userid    eid (eg jsmith26) if the user you want the list for
     * @return
     * @		if not super user or any other error occurs from main method
     */
    @WebMethod
    @Path("/getAllSitesForUser")
    @Produces("text/plain")
    @GET
    public String getAllSitesForUser(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "userid", partName = "userid") @QueryParam("userid") String userid) {

        //get a session for the other user, reuse if possible
        String newsessionid = getSessionForUser(sessionid, userid, true);

        //might be an exception that was returned, so check session is valid.
        Session session = establishSession(newsessionid);

        //ok, so hand over to main method to get the list for us
        return getAllSitesForCurrentUser(newsessionid);
    }


    /**
     * Get a site's title
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site you want the title of
     * @return title of the site or string containing error
     * @
     */
    @WebMethod
    @Path("/getSiteTitle")
    @Produces("text/plain")
    @GET
    public String getSiteTitle(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid) {

        Session s = establishSession(sessionid);

        String siteTitle = "";

        try {
            Site site = siteService.getSite(siteid);
            siteTitle = site.getTitle();
        } catch (Exception e) {
            log.error("WS getSiteTitle(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }

        return siteTitle;
    }

    /**
     * Get a site's description
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site you want the description of
     * @return description of the site or string containing error
     * @throws RuntimeException
     */

    @WebMethod
    @Path("/getSiteDescription")
    @Produces("text/plain")
    @GET
    public String getSiteDescription(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid) {

        Session s = establishSession(sessionid);

        String siteDescription = "";

        try {
            Site site = siteService.getSite(siteid);
            siteDescription = site.getDescription();
        } catch (Exception e) {
            log.error("WS getSiteDescription(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }

        return siteDescription;
    }


    /**
     * Get a site's skin
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site you want the skin of
     * @return description of the site or string containing error
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/getSiteSkin")
    @Produces("text/plain")
    @GET
    public String getSiteSkin(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid) {

        Session s = establishSession(sessionid);

        String siteSkin = "";

        try {
            Site site = siteService.getSite(siteid);
            siteSkin = site.getSkin();
        } catch (Exception e) {
            log.error("WS getSiteSkin(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }

        return siteSkin;
    }


    /**
     * Get a site's joinable status
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site you want the joinable status of
     * @return true if joinable, false if not or error (and logs any errors)
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/isSiteJoinable")
    @Produces("text/plain")
    @GET
    public boolean isSiteJoinable(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid) {
        Session s = establishSession(sessionid);

        try {
            Site site = siteService.getSite(siteid);
            if (site.isJoinable()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("WS isSiteJoinable(): " + e.getClass().getName() + " : " + e.getMessage());
            return false;
        }
    }


    /**
     * Change the title of a site
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site you want to change the title of
     * @param title     the new title
     * @return success or string containing error
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/changeSiteTitle")
    @Produces("text/plain")
    @GET
    public String changeSiteTitle(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "title", partName = "title") @QueryParam("title") String title) {
        Session session = establishSession(sessionid);

        try {

            Site siteEdit = null;
            siteEdit = siteService.getSite(siteid);
            siteEdit.setTitle(title);
            siteService.save(siteEdit);

        } catch (Exception e) {
            log.error("WS changeSiteTitle(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Change the skin of a site
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site you want to change the skin of
     * @param skin      the new skin value (make sure its in /library/skin/<yourskin>)
     * @return success or string containing error
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/changeSiteSkin")
    @Produces("text/plain")
    @GET
    public String changeSiteSkin(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "skin", partName = "skin") @QueryParam("skin") String skin) {
        Session session = establishSession(sessionid);

        try {

            Site siteEdit = null;
            siteEdit = siteService.getSite(siteid);
            siteEdit.setSkin(skin);
            siteService.save(siteEdit);

        } catch (Exception e) {
            log.error("WS changeSiteSkin(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Make a site joinable or not, depending on the params sent
     *
     * @param sessionid  the id of a valid session
     * @param siteid     the id of the site you want to change the status of
     * @param joinable   boolean if its joinable or not
     * @param joinerrole the role that users who join the site will be given
     * @param @WebMethod publicview        boolean if the site is to be public or not. if its joinable it should probably be public so people can find it, but if its public it doesnt necessarily need to be joinable.
     * @return success or string containing error
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/changeSiteJoinable")
    @Produces("text/plain")
    @GET
    public String changeSiteJoinable(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "joinable", partName = "joinable") @QueryParam("joinable") boolean joinable,
            @WebParam(name = "joinerrole", partName = "joinerrole") @QueryParam("joinerrole") String joinerrole,
            @WebParam(name = "publicview", partName = "publicview") @QueryParam("publicview") boolean publicview) {
        Session session = establishSession(sessionid);

        try {

            Site siteEdit = null;
            siteEdit = siteService.getSite(siteid);
            siteEdit.setJoinable(joinable);
            siteEdit.setJoinerRole(joinerrole);
            siteEdit.setPubView(publicview);
            siteService.save(siteEdit);

        } catch (Exception e) {
            log.error("WS changeSiteJoinable(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }


    /**
     * Change the icon of a site (top left hand corner of site)
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site you want to change the icon of
     * @param iconurl   the new icon value ( @WebMethod publically accessible url - suggest its located in Resources for the site or another  @WebMethod public location)
     * @return success or string containing error
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/changeSiteIconUrl")
    @Produces("text/plain")
    @GET
    public String changeSiteIconUrl(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "iconurl", partName = "iconurl") @QueryParam("iconurl") String iconurl) {
        Session session = establishSession(sessionid);

        try {

            Site siteEdit = null;
            siteEdit = siteService.getSite(siteid);
            siteEdit.setIconUrl(iconurl);
            siteService.save(siteEdit);

        } catch (Exception e) {
            log.error("WS changeSiteIconUrl(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }


    /**
     * Change the description of a site
     *
     * @param sessionid   the id of a valid session
     * @param siteid      the id of the site you want to change the title of
     * @param description the new description
     * @return success or string containing error
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/changeSiteDescription")
    @Produces("text/plain")
    @GET
    public String changeSiteDescription(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "description", partName = "description") @QueryParam("description") String description) {
        Session session = establishSession(sessionid);

        try {

            // check description
            if (description != null) {
                StringBuilder alertMsg = new StringBuilder();
                description = FormattedText.processFormattedText(description, alertMsg);
                if (description == null) {
                    throw new RuntimeException("Site description markup rejected: " + alertMsg.toString());
                }
            }

            Site siteEdit = null;
            siteEdit = siteService.getSite(siteid);
            siteEdit.setDescription(description);
            siteService.save(siteEdit);

        } catch (Exception e) {
            log.error("WS changeSiteDescription(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }


    /**
     * Get a custom property of a site
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site you want to get the property from
     * @param propname  the name of the property you want
     * @return the property or blank if not found/property is blank
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/getSiteProperty")
    @Produces("text/plain")
    @GET
    public String getSiteProperty(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "propname", partName = "propname") @QueryParam("propname") String propname) {
        Session session = establishSession(sessionid);

        try {
            //get site handle
            Site site = siteService.getSite(siteid);

            //get list of properties for this site
            ResourceProperties props = site.getProperties();

            //get the property that we wanted, as a string. this wont return multi valued ones
            //would need to use getPropertyList() for that, but then need to return XML since its a list.
            String propvalue = props.getProperty(propname);
            return propvalue;

        } catch (Exception e) {
            log.error("WS getSiteProperty(): " + e.getClass().getName() + " : " + e.getMessage());
            return "";
        }
    }


    /**
     * Set a custom property for a site
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site you want to set the property for
     * @param propname  the name of the property you want to set
     * @param propvalue the name of the property you want to set
     * @return success if true or exception
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/setSiteProperty")
    @Produces("text/plain")
    @GET
    public String setSiteProperty(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "propname", partName = "propname") @QueryParam("propname") String propname,
            @WebParam(name = "propvalue", partName = "propvalue") @QueryParam("propvalue") String propvalue) {
        Session session = establishSession(sessionid);

        if (!securityService.isSuperUser()) {
            log.warn("WS setSiteProperty(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS setSiteProperty(): Permission denied. Restricted to super users.");
        }

        try {
            //get site handle
            Site site = siteService.getSite(siteid);

            //get properties in edit mode
            ResourcePropertiesEdit props = site.getPropertiesEdit();

            //add property
            props.addProperty(propname, propvalue);

            //save site
            siteService.save(site);

        } catch (Exception e) {
            log.error("WS setSiteProperty(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }


    /**
     * Remove a custom property for a site
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site you want to remove the property from
     * @param propname  the name of the property you want to remove
     * @return success if true or exception
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/removeSiteProperty")
    @Produces("text/plain")
    @GET
    public String removeSiteProperty(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "propname", partName = "propname") @QueryParam("propname") String propname) {
        Session session = establishSession(sessionid);

        try {
            //get site handle
            Site site = siteService.getSite(siteid);

            //get properties in edit mode
            ResourcePropertiesEdit props = site.getPropertiesEdit();

            //remove property
            //if the property doesn't exist it will still return success. this is fine.
            props.removeProperty(propname);

            //save site
            siteService.save(site);

        } catch (Exception e) {
            log.error("WS removeSiteProperty(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }


    /**
     * Check if a role exists in a given authzgroup
     *
     * @param sessionid    the id of a valid session
     * @param authzgroupid the id of the authzgroup you want to check
     * @param roleid       the id of the role you want to check for
     * @return true/false
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/checkForRoleInAuthzGroup")
    @Produces("text/plain")
    @GET
    public boolean checkForRoleInAuthzGroup(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid,
            @WebParam(name = "roleid", partName = "roleid") @QueryParam("roleid") String roleid) {
        Session session = establishSession(sessionid);

        try {
            //open authzgroup
            AuthzGroup authzgroup = authzGroupService.getAuthzGroup(authzgroupid);

            //see if we can get the role in this authzgroup. will either return the Role, or null
            Role role = authzgroup.getRole(roleid);
            if (role != null) {
                return true;
            }
            return false;

        } catch (Exception e) {
            log.error("WS checkForRoleInAuthzGroup(): " + e.getClass().getName() + " : " + e.getMessage());
            return false;
        }
    }


    /**
     * Check if a given authzgroup lacks roles
     *
     * @param sessionid    the id of a valid session
     * @param authzgroupid the id of the authzgroup you want to check
     * @return true/false
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/checkForEmptyRolesInAuthzGroup")
    @Produces("text/plain")
    @GET
    public boolean checkForEmptyRolesInAuthzGroup(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid) {
        Session session = establishSession(sessionid);

        try {
            //open authzgroup
            AuthzGroup authzgroup = authzGroupService.getAuthzGroup(authzgroupid);

            boolean roleEmpty = authzgroup.isEmpty();
            return roleEmpty;

        } catch (Exception e) {
            log.error("WS checkForEmptyRolesInAuthzGroup(): " + e.getClass().getName() + " : " + e.getMessage());
            return false;
        }
    }


    /**
     * Search all the users that match this criteria in id or email, first or last name, returning
     * a subset of records within the record range given (sorted by sort name).
     * As of September 2008, the Sakai API that does the searching is not searching provided users
     * nor limiting the returned results (ie first and last are ignored)
     * <p/>
     * This web service is returning everything it receives correctly though, so when
     * userDirectoryService.searchUsers() is amended, this will be even more complete.
     * <p/>
     * See: SAK-6792 and SAK-14268 for the relevant JIRA tickets.
     *
     * @param sessionid the id of a valid session
     * @param criteria  the search criteria.
     * @param first     the first record position to return.
     * @param last      the last record position to return.
     * @return xml doc of list of records
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/searchForUsers")
    @Produces("text/plain")
    @GET
    public String searchForUsers(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "criteria", partName = "criteria") @QueryParam("criteria") String criteria,
            @WebParam(name = "first", partName = "first") @QueryParam("first") int first,
            @WebParam(name = "last", partName = "last") @QueryParam("last") int last) {
        Session session = establishSession(sessionid);


        try {

            //validate input
            if (("").equals(criteria)) {
                log.warn("WS searchForUsers(): no search criteria");
                return "<exception/>";
            }

            if (first == 0 || last == 0) {
                log.warn("WS searchForUsers(): invalid ranges");
                return "<exception/>";
            }


            List users = userDirectoryService.searchUsers(criteria, first, last);
            List externalUsers = userDirectoryService.searchExternalUsers(criteria, first, last);
            users.addAll(externalUsers);

            Document dom = Xml.createDocument();
            Node list = dom.createElement("list");
            dom.appendChild(list);


            for (Iterator i = users.iterator(); i.hasNext(); ) {
                User user = (User) i.next();

                try {

                    Node userNode = dom.createElement("user");

                    Node userId = dom.createElement("id");
                    userId.appendChild(dom.createTextNode(user.getId()));

                    Node userEid = dom.createElement("eid");
                    userEid.appendChild(dom.createTextNode(user.getEid()));

                    Node userName = dom.createElement("name");
                    userName.appendChild(dom.createTextNode(user.getDisplayName()));

                    Node userEmail = dom.createElement("email");
                    userEmail.appendChild(dom.createTextNode(user.getEmail()));

                    userNode.appendChild(userId);
                    userNode.appendChild(userEid);
                    userNode.appendChild(userName);
                    userNode.appendChild(userEmail);
                    list.appendChild(userNode);

                } catch (Exception e) {
                    //log this error and continue to the next user, otherwise we get nothing
                    log.warn("WS searchForUsers(): " + e.getClass().getName() + " : " + e.getMessage());
                }

            }

            //add total size node (nice attribute to save the end user doing an XSLT count every time)
            Node total = dom.createElement("total");
            total.appendChild(dom.createTextNode(Integer.toString(users.size())));
            list.appendChild(total);

            return Xml.writeDocumentToString(dom);

        } catch (Exception e) {
            log.error("WS searchForUsers(): " + e.getClass().getName() + " : " + e.getMessage());
            return "<exception/>";

        }
    }


    /**
     * Check if an authzgroup exists, similar to checkForSite, but does authzgroup instead
     * (e.g. might be used to check if !site.template exists which checkForSite() cannot do.)
     *
     * @param sessionid    the id of a valid session
     * @param authzgroupid the id of the authzgroup you want to check
     * @return true if exists, false if not or error.
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/checkForAuthzGroup")
    @Produces("text/plain")
    @GET
    public boolean checkForAuthzGroup(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid) {
        Session s = establishSession(sessionid);

        try {

            AuthzGroup authzgroup = null;
            authzgroup = authzGroupService.getAuthzGroup(authzgroupid);
            if (authzgroup != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("WS checkForAuthzGroup(): " + e.getClass().getName() + " : " + e.getMessage());
            return false;
        }
    }


    /**
     * Removes a member from a given site, similar to removeMembeForAuthzGroup but acts on Site directly
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site you want to remove the user from
     * @return success or string containing error
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/removeMemberFromSite")
    @Produces("text/plain")
    @GET
    public String removeMemberFromSite(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid) {

        Session session = establishSession(sessionid);

        try {
            Site site = siteService.getSite(siteid);
            String userid = userDirectoryService.getUserByEid(eid).getId();
            site.removeMember(userid);
            siteService.saveSiteMembership(site);
        } catch (Exception e) {
            log.error("WS removeMemberFromSite(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Removes a member from a given site, similar to removeMembeForAuthzGroup but acts on Site directly and uses a
     * list of users
     *
     * @param	sessionid	the id of a valid session
     * @param	siteid		the id of the site you want to remove the users from
     * @param   eids         comma separated list of users eid
     * @return				success or string containing error
     * @throws	AxisFault
     *
     */
    @Path("/removeMemberFromSiteBatch")
    @Produces("text/plain")
    @GET
    public String removeMemberFromSiteBatch(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "eids", partName = "eid") @QueryParam("eids") String eids) {

        Session session = establishSession(sessionid);

        if (!securityService.isSuperUser(session.getUserId())) {
            log.warn("NonSuperUser trying to removeMemberFromSiteBatch: " + session.getUserId());
            throw new RuntimeException("NonSuperUser trying to removeMemberFromSiteBatch: " + session.getUserId());
        }

        try {
            Site site = siteService.getSite(siteid);
            List<String> eidsList = Arrays.asList(eids.split(","));
            for (String eid : eidsList) {
                String userid = userDirectoryService.getUserByEid(eid).getId();
                site.removeMember(userid);
            }
            siteService.save(site);
        } catch (Exception e) {
            log.error("WS removeMemberFromSiteBatch(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }


    /**
     * Check if a user is in a particular authzgroup
     *
     * @param sessionid    the id of a valid session, generally the admin user
     * @param authzgroupid the id of the authzgroup or site you want to check (if site: /site/SITEID)
     * @param eid          the userid of the person you want to check
     * @return true if in site, false if not or error.
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/checkForUserInAuthzGroup")
    @Produces("text/plain")
    @GET
    public boolean checkForUserInAuthzGroup(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid) {
        Session s = establishSession(sessionid);

        if (ADMIN_SITE_REALM.equalsIgnoreCase(authzgroupid) && !securityService.isSuperUser(s.getUserId())) {
            log.warn("WS checkForUserInAuthzGroup(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS checkForUserInAuthzGroup(): Permission denied. Restricted to super users.");
        }

        try {
            AuthzGroup azg = authzGroupService.getAuthzGroup(authzgroupid);
            for (Iterator i = azg.getUsers().iterator(); i.hasNext(); ) {
                String id = (String) i.next();
                User user = userDirectoryService.getUser(id);
                if (user.getEid().equals(eid)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("WS checkForUserInAuthzGroup(): " + e.getClass().getName() + " : " + e.getMessage());
            return false;
        }
    }


    /**
     * Get list of users in an authzgroup with the given role(s)
     *
     * @param sessionid       the id of a valid session
     * @param authzgroupid    the id of the authzgroup, site or group you want to get the users in (if site: /site/SITEID, if group: /site/SITEID/group/GROUPID)
     * @param authzgrouproles the roles that you want to filter on (string with commas as delimiters)
     * @return xml doc of the list of users, display name and roleid
     * @throws RuntimeException returns <exception /> string if exception encountered and logs it
     */
    @WebMethod
    @Path("/getUsersInAuthzGroupWithRole")
    @Produces("text/plain")
    @GET
    public String getUsersInAuthzGroupWithRole(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid,
            @WebParam(name = "authzgrouproles", partName = "authzgrouproles") @QueryParam("authzgrouproles") String authzgrouproles) {

        Session s = establishSession(sessionid);

        if (ADMIN_SITE_REALM.equalsIgnoreCase(authzgroupid) && !securityService.isSuperUser(s.getUserId())) {
            log.warn("WS getUsersInAuthzGroupWithRole(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS getUsersInAuthzGroupWithRole(): Permission denied. Restricted to super users.");
        }

        try {

            AuthzGroup azg = authzGroupService.getAuthzGroup(authzgroupid);

            Document dom = Xml.createDocument();
            Node list = dom.createElement("list");
            dom.appendChild(list);

            //get the authzgroup role(s)
            String[] authzgrouprolesArr = StringUtils.split(authzgrouproles, ',');
            List<String> authzgrouprolesList = Arrays.asList(authzgrouprolesArr);

            //iterate over each role in the list...
            for (Iterator j = authzgrouprolesList.iterator(); j.hasNext(); ) {
                String role = (String) j.next();

                //now get all the users in the authzgroup with this role and add to xml doc
                for (Iterator k = azg.getUsersHasRole(role).iterator(); k.hasNext(); ) {
                    String id = (String) k.next();

                    try {
                        User user = userDirectoryService.getUser(id);
                        Node userNode = dom.createElement("user");
                        Node userId = dom.createElement("id");
                        userId.appendChild(dom.createTextNode(user.getEid()));
                        Node userName = dom.createElement("name");
                        userName.appendChild(dom.createTextNode(user.getDisplayName()));
                        Node userRole = dom.createElement("role");
                        userRole.appendChild(dom.createTextNode(role));

                        userNode.appendChild(userId);
                        userNode.appendChild(userName);
                        userNode.appendChild(userRole);
                        list.appendChild(userNode);

                    } catch (Exception e) {
                        //Exception with this user, log the error, skip this user and continue to the next
                        log.warn("WS getUsersInAuthzGroupWithRole(): error processing user " + id + " : " + e.getClass().getName() + " : " + e.getMessage());
                    }
                }
            }
            return Xml.writeDocumentToString(dom);
        } catch (Exception e) {
            log.error("WS getUsersInAuthzGroupWithRole(): " + e.getClass().getName() + " : " + e.getMessage());
            return "<exception/>";

        }
    }


    /**
     * Gets list of ALL users in an authzgroup
     *
     * @param sessionid    the id of a valid session
     * @param authzgroupid the id of the authzgroup, site or group you want to get the users in (if site: /site/SITEID, if group: /site/SITEID/group/GROUPID)
     * @return xml doc of the list of users, display name and roleid
     * @throws RuntimeException returns <exception /> string if exception encountered
     */
    @WebMethod
    @Path("/getUsersInAuthzGroup")
    @Produces("text/plain")
    @GET
    public String getUsersInAuthzGroup(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid) {

        Session s = establishSession(sessionid);

        if (ADMIN_SITE_REALM.equalsIgnoreCase(authzgroupid) && !securityService.isSuperUser(s.getUserId())) {
            log.warn("WS getUsersInAuthzGroup(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS getUsersInAuthzGroup(): Permission denied. Restricted to super users.");
        }

        try {

            AuthzGroup azg = authzGroupService.getAuthzGroup(authzgroupid);

            Document dom = Xml.createDocument();
            Node list = dom.createElement("list");
            dom.appendChild(list);

            for (Iterator i = azg.getUsers().iterator(); i.hasNext(); ) {
                String id = (String) i.next();
                try {
                    User user = userDirectoryService.getUser(id);

                    //wrapping user node
                    Node userNode = dom.createElement("user");

                    //id child node
                    Node userId = dom.createElement("id");
                    userId.appendChild(dom.createTextNode(user.getEid()));

                    //name child node
                    Node userName = dom.createElement("name");
                    userName.appendChild(dom.createTextNode(user.getDisplayName()));

                    //role child node
                    Node userRole = dom.createElement("role");
                    String role = azg.getUserRole(id).getId();
                    userRole.appendChild(dom.createTextNode(role));

                    //add all clicd nodes into the parent node
                    userNode.appendChild(userId);
                    userNode.appendChild(userName);
                    userNode.appendChild(userRole);
                    list.appendChild(userNode);

                } catch (Exception e) {
                    //Exception with this user, log the error, skip this user and continue to the next
                    log.warn("WS getUsersInAuthzGroup(): error processing user " + id + " : " + e.getClass().getName() + " : " + e.getMessage());
                }
            }
            return Xml.writeDocumentToString(dom);
        } catch (Exception e) {
            log.error("WS getUsersInAuthzGroup(): " + e.getClass().getName() + " : " + e.getMessage());
            return "<exception/>";
        }
    }

   /**
     * Add a new single calendar event
     * 
     * @param sessionid    			the id of a valid session
     * @param sourceSiteId 			the id of the site containing the calendar entries you want copied from
     * @param startTime 			start time in java milliseconds
     * @param endTime		    	end time in java milliseconds
     * @param startIncluded 		to include start in range
     * @param endIncluded 			to include end in range
     * @param displayName 			display name for the calendar
     * @param description			description for the calendar
     * @param type					calendar type (must match defined types)
     * @param location				calendar location
     * @param descriptionFormatted	formatted description
     * @param recurrenceFrequency 	recurrence frequency, must match a recurrence rule defined in RecurrenceRule.java 
     * @param recurrenceInterval	recurrence interval
     * @return success or exception
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/addCalendarEvent")
    @Produces("text/plain")
    @GET
    public String addCalendarEvent(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "sourceSiteId", partName = "sourceSiteId") @QueryParam("sourceSiteId") String sourceSiteId,
            @WebParam(name = "startTime", partName = "startTime") @QueryParam("startTime") long startTime,
            @WebParam(name = "endTime", partName = "endTime") @QueryParam("endTime") long endTime, 
            @WebParam(name = "startIncluded", partName = "startIncluded") @QueryParam("startIncluded") boolean startIncluded,
            @WebParam(name = "endIncluded", partName = "endIncluded") @QueryParam("endIncluded") boolean endIncluded,
            @WebParam(name = "displayName", partName = "displayName") @QueryParam("displayName") String displayName,
            @WebParam(name = "description", partName = "description") @QueryParam("description") String description,
            @WebParam(name = "type", partName = "type") @QueryParam("type") String type,
            @WebParam(name = "location", partName = "location") @QueryParam("location") String location,
            @WebParam(name = "descriptionFormatted", partName = "descriptionFormatted") @QueryParam("descriptionFormatted") String descriptionFormatted,
            @WebParam(name = "recurrenceFrequency", partName = "recurrenceFrequency") @QueryParam("recurrenceFrequency") String recurrenceFrequency,
            @WebParam(name = "recurrenceInterval", partName = "recurrenceInterval") @QueryParam("recurrenceInterval") int recurrenceInterval){

        Session session = establishSession(sessionid);

        //setup source and target calendar strings
        String calId = "/calendar/calendar/" + sourceSiteId + "/main";

        CalendarEdit calendar = null;

    	try {
    		//get calendars
    		calendar = calendarService.editCalendar(calId);

    		CalendarEventEdit cedit = calendar.addEvent();
    		TimeRange timeRange = timeService.newTimeRange(timeService.newTime(startTime), timeService.newTime(endTime), startIncluded, endIncluded);
    		cedit.setRange(timeRange);
    		cedit.setDisplayName(displayName);
    		cedit.setDescription(description);
    		cedit.setType(type);
    		cedit.setLocation(location);
    		cedit.setDescriptionFormatted(descriptionFormatted);
    		if (recurrenceFrequency != null) {
    			RecurrenceRule rule = calendarService.newRecurrence(recurrenceFrequency, recurrenceInterval);
    			cedit.setRecurrenceRule(rule);
    		}
    		calendar.commitEvent(cedit);
    		calendarService.commitCalendar(calendar);

    	} catch (Exception e) {
    		calendarService.cancelCalendar(calendar);
    		log.error("WS addCalendarEvent(): error " + e.getClass().getName() + " : " + e.getMessage());
    		return e.getClass().getName() + " : " + e.getMessage();
    	}
    	return "success";
    }

    /**
     * Copy the calendar events from one site to another
     *
     * @param sessionid    the id of a valid session
     * @param sourceSiteId the id of the site containing the calendar entries you want copied from
     * @param targetSiteId the id of the site you want to copy the calendar entries to
     * @return success or exception
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/copyCalendarEvents")
    @Produces("text/plain")
    @GET
    public String copyCalendarEvents(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "sourceSiteId", partName = "sourceSiteId") @QueryParam("sourceSiteId") String sourceSiteId,
            @WebParam(name = "targetSiteId", partName = "targetSiteId") @QueryParam("targetSiteId") String targetSiteId) {

        Session session = establishSession(sessionid);

        //setup source and target calendar strings
        String calId1 = "/calendar/calendar/" + sourceSiteId + "/main";
        String calId2 = "/calendar/calendar/" + targetSiteId + "/main";

        Calendar calendar1 = null;
        CalendarEdit calendar2 = null;
        try {
            //get calendars
            calendar1 = calendarService.getCalendar(calId1);
            calendar2 = calendarService.editCalendar(calId2);

            //for every event in calendar1, add it to calendar2
            List eventsList = calendar1.getEvents(null, null);

            for (Iterator i = eventsList.iterator(); i.hasNext(); ) {
                CalendarEvent cEvent = (CalendarEvent) i.next();
                CalendarEventEdit cedit = calendar2.addEvent();
                cedit.setRange(cEvent.getRange());
                cedit.setDisplayName(cEvent.getDisplayName());
                cedit.setDescription(cEvent.getDescription());
                cedit.setType(cEvent.getType());
                cedit.setLocation(cEvent.getLocation());
                cedit.setDescriptionFormatted(cEvent.getDescriptionFormatted());
                cedit.setRecurrenceRule(cEvent.getRecurrenceRule());
                calendar2.commitEvent(cedit);
                //log.warn(cEvent.getDisplayName()); 
            }
            //save calendar 2
            calendarService.commitCalendar(calendar2);

        } catch (Exception e) {
            calendarService.cancelCalendar(calendar2);
            log.error("WS copyCalendarEvents(): error " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Get a user's type (for their account)
     *
     * @param sessionid the id of a valid session
     * @param userid    the userid of the person you want the type for
     * @return type if set or blank
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/getUserType")
    @Produces("text/plain")
    @GET
    public String getUserType(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "userid", partName = "userid") @QueryParam("userid") String userid) {
        Session session = establishSession(sessionid);
        try {
            User user = userDirectoryService.getUserByEid(userid);
            return user.getType();
        } catch (Exception e) {
            log.warn("WS getUserType() failed for user: " + userid);
            return "";
        }

    }


    /**
     * Adds a tool to all My Workspace sites
     *
     * @param pagelayout single or double column (0 or 1). Any other value will revert to 0.
     * @param sessionid  the id of a valid session for the admin user
     * @param toolid     the id of the tool you want to add (ie sakai.profile2)
     * @param pagetitle  the title of the page shown in the site navigation
     * @param tooltitle  the title of the tool shown in the main portlet
     * @param position   integer specifying the position within other pages on the site (0 means top, for right at the bottom a large enough number, ie 99)
     * @param popup      boolean for if it should be a popup window or not
     * @return success or exception
     * @throws RuntimeException Sakai properties:
     *                          #specify the list of users to ignore separated by a comma, no spaces. Defaults to 'admin,postmaster'.
     *                          webservice.specialUsers=admin,postmaster
     */
    @WebMethod
    @Path("/addNewToolToAllWorkspaces")
    @Produces("text/plain")
    @GET
    public String addNewToolToAllWorkspaces(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "toolid", partName = "toolid") @QueryParam("toolid") String toolid,
            @WebParam(name = "pagetitle", partName = "pagetitle") @QueryParam("pagetitle") String pagetitle,
            @WebParam(name = "tooltitle", partName = "tooltitle") @QueryParam("tooltitle") String tooltitle,
            @WebParam(name = "pagelayout", partName = "pagelayout") @QueryParam("pagelayout") int pagelayout,
            @WebParam(name = "position", partName = "position") @QueryParam("position") int position,
            @WebParam(name = "popup", partName = "popup") @QueryParam("popup") boolean popup) {
        Session session = establishSession(sessionid);

        //check that ONLY admin is accessing this	
        if (!securityService.isSuperUser(session.getUserId())) {
            log.warn("WS addNewToolToAllWorkspaces() failed. Restricted to admin users.");
            throw new RuntimeException("WS failed. Restricted to admin users.");
        }

        try {

            //get special users
            String config = serverConfigurationService.getString("webservice.specialUsers", "admin,postmaster");
            String[] items = StringUtils.split(config, ',');
            List<String> specialUsers = Arrays.asList(items);

            //now get all users
            List<String> allUsers = new ArrayList<String>();
            List<User> users = userDirectoryService.getUsers();
            for (Iterator i = users.iterator(); i.hasNext(); ) {
                User user = (User) i.next();
                allUsers.add(user.getId());
            }

            //remove special users
            allUsers.removeAll(specialUsers);

            //now add a page to each site, and the tool to that page
            for (Iterator j = allUsers.iterator(); j.hasNext(); ) {
                String userid = StringUtils.trim((String) j.next());

                log.info("Processing user:" + userid);

                String myWorkspaceId = siteService.getUserSiteId(userid);

                Site siteEdit = null;
                SitePage sitePageEdit = null;

                try {
                    siteEdit = siteService.getSite(myWorkspaceId);
                } catch (IdUnusedException e) {
                    log.error("No workspace for user: " + myWorkspaceId + ", skipping...");
                    continue;
                }

                sitePageEdit = siteEdit.addPage();
                sitePageEdit.setTitle(pagetitle);
                sitePageEdit.setLayout(pagelayout);

                sitePageEdit.setPosition(position);
                sitePageEdit.setPopup(popup);

                ToolConfiguration tool = sitePageEdit.addTool();
                Tool t = tool.getTool();

                tool.setTool(toolid, toolManager.getTool(toolid));
                tool.setTitle(tooltitle);

                siteService.save(siteEdit);
                log.info("Page added for user:" + userid);

            }
            return "success";
        } catch (Exception e) {
            log.error("WS addNewToolToAllWorkspaces(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
    }

    /**
     * Copies a role from one authzgroup to another. Useful for mass population/synchronisation
     * <p/>
     * The sessionid argument must be a valid session for a super user ONLY otherwise it will fail.
     * The authzgroup arguments for sites should start with /site/SITEID, likewise for site groups etc
     *
     * @param sessionid        the sessionid of a valid session for the admin user
     * @param authzgroupid1    the authgroupid of the site you want to copy the role FROM
     * @param authzgroupid2    the authgroupid of the site you want to copy the role TO
     * @param roleid           the id of the role you want to copy
     * @param description      the description of the new role
     * @param removeBeforeSync if synchronising roles, whether or not to remove the functions from the target role before adding the set in. This will
     *                         mean that no additional permissions will remain, if they already exist in that role
     * @return success or RuntimeException
     * @throws RuntimeException if not a super user, if new role cannot be created, if functions differ after the new role is made
     */
    @WebMethod
    @Path("/copyRole2")
    @Produces("text/plain")
    @GET
    public String copyRole2(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid1", partName = "authzgroupid1") @QueryParam("authzgroupid1") String authzgroupid1,
            @WebParam(name = "authzgroupid2", partName = "authzgroupid2") @QueryParam("authzgroupid2") String authzgroupid2,
            @WebParam(name = "roleid", partName = "roleid") @QueryParam("roleid") String roleid,
            @WebParam(name = "description", partName = "description") @QueryParam("description") String description,
            @WebParam(name = "removeBeforeSync", partName = "removeBeforeSync") @QueryParam("removeBeforeSync") boolean removeBeforeSync) {
        Session session = establishSession(sessionid);

        Set existingfunctions;
        Set newfunctions;
        Set existingroles;
        ArrayList existingroleids;
        Iterator iRoles;
        boolean createRole = false;
        Role role2;

        //check that ONLY super user's are accessing this	
        if (!securityService.isSuperUser(session.getUserId())) {
            log.warn("WS copyRole2(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS copyRole(): Permission denied. Restricted to super users.");
        }

        try {
            //open authzgroup1
            AuthzGroup authzgroup1 = authzGroupService.getAuthzGroup(authzgroupid1);
            //get role that we want to copy
            Role role1 = authzgroup1.getRole(roleid);
            //get functions that are in this role
            existingfunctions = role1.getAllowedFunctions();

            log.warn("WS copyRole(): existing functions in role " + roleid + " in " + authzgroupid1 + ": " + new ArrayList(existingfunctions).toString());

            //open authzgroup2
            AuthzGroup authzgroup2 = authzGroupService.getAuthzGroup(authzgroupid2);
            //get roles in authzgroup2
            existingroles = authzgroup2.getRoles();

            existingroleids = new ArrayList();

            //iterate over roles, get the roleId from the role, add to arraylist for checking
            for (iRoles = existingroles.iterator(); iRoles.hasNext(); ) {
                Role existingrole = (Role) iRoles.next();
                existingroleids.add(existingrole.getId());
            }
            log.warn("WS copyRole2(): existing roles in " + authzgroupid2 + ": " + existingroleids.toString());


            //if this roleid exists in the authzgroup already...
            if (existingroleids.contains(roleid)) {
                log.warn("WS copyRole2(): role " + roleid + " exists in " + authzgroupid2 + ". This role will updated.");
            } else {
                log.warn("WS copyRole2(): role " + roleid + " does not exist in " + authzgroupid2 + ". This role will be created.");

                //create this role in authzgroup2
                role2 = authzgroup2.addRole(roleid);
                //save authzgroup change
                authzGroupService.save(authzgroup2);

                //reopen authzgroup2 for checking
                authzgroup2 = authzGroupService.getAuthzGroup(authzgroupid2);

                //check the role was actually created by getting set again and iterating
                existingroles = authzgroup2.getRoles();

                existingroleids = new ArrayList();

                //iterate over roles, get the roleId from the role, add to arraylist for checking
                for (iRoles = existingroles.iterator(); iRoles.hasNext(); ) {
                    Role existingrole = (Role) iRoles.next();
                    existingroleids.add(existingrole.getId());
                }

                log.warn("WS copyRole2(): existing roles in " + authzgroupid2 + " after addition: " + existingroleids.toString());

                //if role now exists, ok, else fault.
                if (existingroleids.contains(roleid)) {
                    log.warn("WS copyRole2(): role " + roleid + " was created in " + authzgroupid2 + ".");
                } else {
                    log.warn("WS copyRole2(): role " + roleid + " could not be created in " + authzgroupid2 + ".");
                    throw new RuntimeException("WS copyRole2(): role " + roleid + " could not be created in " + authzgroupid2 + ".");
                }

            }

            //get this role
            role2 = authzgroup2.getRole(roleid);

            //if removing permissions before syncing (SAK-18019)
            if (removeBeforeSync) {
                role2.disallowAll();
            }

            //add Set of functions to this role
            role2.allowFunctions(existingfunctions);

            //set description
            role2.setDescription(description);

            //save authzgroup change
            authzGroupService.save(authzgroup2);

            //reopen authzgroup2 for checking
            authzgroup2 = authzGroupService.getAuthzGroup(authzgroupid2);

            //get role we want to check
            role2 = authzgroup2.getRole(roleid);
            //get Set of functions that are now in this role
            newfunctions = role2.getAllowedFunctions();

            //compare existingfunctions with newfunctions to see that they match
            if (newfunctions.containsAll(existingfunctions)) {
                log.warn("WS copyRole2(): functions added successfully to role " + roleid + " in " + authzgroupid2 + ".");
            } else {
                log.warn("WS copyRole2(): functions in roles differ after addition.");
                throw new RuntimeException("WS copyRole(): functions in roles differ after addition.");
            }

        } catch (Exception e) {
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Copies a role from one authzgroup to another. Useful for mass population/synchronisation
     * <p/>
     * The sessionid argument must be a valid session for a super user ONLY otherwise it will fail.
     * The authzgroup arguments for sites should start with /site/SITEID, likewise for site groups etc
     *
     * @param sessionid     the sessionid of a valid session for the admin user
     * @param authzgroupid1 the authgroupid of the site you want to copy the role FROM
     * @param authzgroupid2 the authgroupid of the site you want to copy the role TO
     * @param roleid        the id of the role you want to copy
     * @param description   the description of the new role
     * @return success or RuntimeException
     * @throws RuntimeException if not a super user, if new role cannot be created, if functions differ after the new role is made
     */
    @WebMethod
    @Path("/copyRole")
    @Produces("text/plain")
    @GET
    public String copyRole(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid1", partName = "authzgroupid1") @QueryParam("authzgroupid1") String authzgroupid1,
            @WebParam(name = "authzgroupid2", partName = "authzgroupid2") @QueryParam("authzgroupid2") String authzgroupid2,
            @WebParam(name = "roleid", partName = "roleid") @QueryParam("roleid") String roleid,
            @WebParam(name = "description", partName = "description") @QueryParam("description") String description) {
        return copyRole2(sessionid, authzgroupid1, authzgroupid2, roleid, description, false);

    }


    /**
     * Gets all user accounts as XML. Currently returns userId, eid, displayName and type.
     *
     * @param sessionid the id of a valid session for the admin user
     * @return XML or exception
     * @throws RuntimeException Optional sakai.properties:
     *                          #specify the list of users to ignore separated by a comma, no spaces. Defaults to 'admin,postmaster'.
     *                          webservice.specialUsers=admin,postmaster
     */
    @WebMethod
    @Path("/getAllUsers")
    @Produces("text/plain")
    @GET
    public String getAllUsers(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid) {
        Session session = establishSession(sessionid);

        //check that ONLY admin is accessing this	
        if (!securityService.isSuperUser(session.getUserId())) {
            log.warn("WS getAllUsers() failed. Restricted to admin users.");
            throw new RuntimeException("WS failed. Restricted to admin users.");
        }

        try {

            //get special users
            String config = serverConfigurationService.getString("webservice.specialUsers", "admin,postmaster");
            String[] items = StringUtils.split(config, ',');
            List<String> specialUsers = Arrays.asList(items);

            //get all users
            List<User> allUsers = userDirectoryService.getUsers();

            //check size
            if (allUsers == null || allUsers.size() == 0) {
                return "<list/>";
            }

            Document dom = Xml.createDocument();
            Node list = dom.createElement("list");
            dom.appendChild(list);
            for (Iterator i = allUsers.iterator(); i.hasNext(); ) {
                User user = (User) i.next();

                //skip if this user is in the specialUser list
                if (specialUsers.contains(user.getEid())) {
                    continue;
                }

                Node item = dom.createElement("item");
                Node userId = dom.createElement("userId");
                userId.appendChild(dom.createTextNode(user.getId()));
                Node eid = dom.createElement("eid");
                eid.appendChild(dom.createTextNode(user.getEid()));
                Node displayName = dom.createElement("displayName");
                displayName.appendChild(dom.createTextNode(user.getDisplayName()));
                Node type = dom.createElement("type");
                type.appendChild(dom.createTextNode(user.getType()));

                item.appendChild(userId);
                item.appendChild(eid);
                item.appendChild(displayName);
                item.appendChild(type);
                list.appendChild(item);
            }

            return Xml.writeDocumentToString(dom);

        } catch (Exception e) {
            log.error("WS getAllUsers(): " + e.getClass().getName() + " : " + e.getMessage());
            return "<exception/>";
        }

    }

    /**
     * Creates and returns the session ID for a given user.
     * <p/>
     * The sessionid argument must be a valid session for a super user ONLY otherwise it will fail.
     * The userid argument must be the EID (ie jsmith) of a valid user.
     * This new sessionid can then be used with getSitesUserCanAccess() to get the sites for the given user.
     *
     * @param sessionid the sessionid of a valid session for a super user
     * @param eid       the eid of the user you want to create a session for
     * @param wsonly    should the session created be tied to the web services only?
     *                  the initial implementation of this will just set an attribute on the Session that identifies it as originating from the web services.
     *                  but in essence it will be just like a normal session. However this attribute is used elsewhere for filtering for these web service sessions.
     * @return the sessionid for the user specified
     * @throws RuntimeException if any data is missing,
     *                          not super user,
     *                          or session cannot be established
     */
    @WebMethod
    @Path("/getSessionForUser")
    @Produces("text/plain")
    @GET
    public String getSessionForUser(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "wsonly", partName = "wsonly") @QueryParam("wsonly") boolean wsonly) {

        Session session = establishSession(sessionid);

        //check that ONLY super user's are accessing this	
        if (!securityService.isSuperUser(session.getUserId())) {
            log.warn("WS getSessionForUser(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS getSessionForUser(): Permission denied. Restricted to super users.");
        }

        try {

            //check for empty userid
            if (StringUtils.isBlank(eid)) {
                log.warn("WS getSessionForUser() failed. Param eid empty.");
                throw new RuntimeException("WS failed. Param eid empty.");
            }

            //if dealing with web service sessions, re-use is ok
            if (wsonly) {
                //do we already have a web service session for the given user? If so, reuse it.
                List<Session> existingSessions = sessionManager.getSessions();
                for (Session existingSession : existingSessions) {
                    if (StringUtils.equals(existingSession.getUserEid(), eid)) {

                        //check if the origin attribute, if set, is set for web services
                        String origin = (String) existingSession.getAttribute(SESSION_ATTR_NAME_ORIGIN);
                        if (StringUtils.equals(origin, SESSION_ATTR_VALUE_ORIGIN_WS)) {
                            log.warn("WS getSessionForUser() reusing existing session for: " + eid + ", session=" + existingSession.getId());
                            return existingSession.getId();
                        }
                    }
                }
            }

            //get ip address for establishing session
            Message message = PhaseInterceptorChain.getCurrentMessage();
            HttpServletRequest request = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
            String ipAddress = request.getRemoteAddr();

            //start a new session
            Session newsession = sessionManager.startSession();
            sessionManager.setCurrentSession(newsession);

            //inject this session with new user values
            User user = userDirectoryService.getUserByEid(eid);
            newsession.setUserEid(eid);
            newsession.setUserId(user.getId());

            //if wsonly, inject the origin attribute
            if (wsonly) {
                newsession.setAttribute(SESSION_ATTR_NAME_ORIGIN, SESSION_ATTR_VALUE_ORIGIN_WS);
                log.warn("WS getSessionForUser() set origin attribute on session: " + newsession.getId());
            }

            //register the session with presence
            UsageSession usagesession = usageSessionService.startSession(user.getId(), ipAddress, "SakaiScript.jws getSessionForUser()");

            // update the user's externally provided realm definitions
            authzGroupService.refreshUser(user.getId());

            // post the login event
            eventTrackingService.post(eventTrackingService.newEvent(UsageSessionService.EVENT_LOGIN_WS, null, true));

            if (newsession == null) {
                log.warn("WS getSessionForUser() failed. Unable to establish session for userid=" + eid + ", ipAddress=" + ipAddress);
                throw new RuntimeException("WS failed. Unable to establish session");
            } else {
                log.warn("WS getSessionForUser() OK. Established session for userid=" + eid + ", session=" + newsession.getId() + ", ipAddress=" + ipAddress);
                return newsession.getId();
            }
        } catch (Exception e) {
            return e.getClass().getName() + " : " + e.getMessage();
        }

    }

    /**
     * Alternate (original) form of getSessionForUser that creates normal sessions for users.
     *
     * @param sessionid the sessionid of a valid session for a super user
     * @param eid       the eid of the user you want to create a session for
     * @return the sessionid for the user specified
     * @throws RuntimeException if any data is missing,
     *                          not super user,
     *                          or session cannot be established
     */
    @WebMethod
    @Path("/getSessionForCurrentUser")
    @Produces("text/plain")
    @GET
    public String getSessionForCurrentUser(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid) {
        return getSessionForUser(sessionid, eid, false);
    }


    /**
     * Create a new page in a site. A page holds one or more tools and is shown in the main navigation section. You will still need to add tools to this page.
     *
     * @param sessionid  the id of a valid session
     * @param siteid     the id of the site to add the page to
     * @param pagetitle  the title of the new page
     * @param pagelayout single or double column (0 or 1). Any other value will revert to 0.
     * @param position   and integer specifying the position within other pages on the site (0 means top, for right at the bottom a large enough number, ie 99)
     * @param popup      boolean for if it should be a popup window or not
     * @return success or exception message
     */
    @WebMethod
    @Path("/addNewPageToSiteWithPosition")
    @Produces("text/plain")
    @GET
    public String addNewPageToSiteWithPosition(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "pagetitle", partName = "pagetitle") @QueryParam("pagetitle") String pagetitle,
            @WebParam(name = "pagelayout", partName = "pagelayout") @QueryParam("pagelayout") int pagelayout,
            @WebParam(name = "position", partName = "position") @QueryParam("position") int position,
            @WebParam(name = "popup", partName = "popup") @QueryParam("popup") boolean popup) {
        Session session = establishSession(sessionid);

        try {

            Site siteEdit = null;
            SitePage sitePageEdit = null;
            siteEdit = siteService.getSite(siteid);
            sitePageEdit = siteEdit.addPage();
            sitePageEdit.setTitle(pagetitle);
            sitePageEdit.setLayout(pagelayout);

            sitePageEdit.setPosition(position);
            sitePageEdit.setPopup(popup);
            siteService.save(siteEdit);

        } catch (Exception e) {
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Gets the user ID for a given eid.
     *
     * @param sessionid the sessionid of a valid session for a super user or the sessionid for the user making the request for their own user ID
     * @param eid       the login username (ie jsmith26) of the user you want the user ID for
     * @return the user ID (generally a UUID) for the user specified or an empty string ""
     * @throws RuntimeException if not a super user and the eid supplied does not match the eid of the session, if user does not exist
     */
    @WebMethod
    @Path("/getUserId")
    @Produces("text/plain")
    @GET
    public String getUserId(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid) {
        Session session = establishSession(sessionid);

        //if eids don't match and we aren't a super user, abort	
        if (!StringUtils.equals(eid, session.getUserEid()) && !securityService.isSuperUser(session.getUserId())) {
            log.warn("WS getUserId(): Permission denied. Restricted to super users or own user.");
            throw new RuntimeException("WS getUserId(): Permission denied. Restricted to super users or own user.");
        }

        try {
            return userDirectoryService.getUserId(eid);
        } catch (Exception e) {
            log.warn("WS getUserId() failed for user: " + eid);
            return "";
        }
    }

    /**
     * Gets the user ID associated with the sessionid
     *
     * @param sessionid sessionid for a valid session.
     * @return the user ID (generally a UUID) for the user associated with the given session, or an empty string ""
     * @
     */
    @WebMethod
    @Path("/getUserIdForCurrentUser")
    @Produces("text/plain")
    @GET
    public String getUserIdForCurrentUser(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid) {
        Session session = establishSession(sessionid);

        try {
            return session.getUserId();
        } catch (Exception e) {
            log.warn("WS getUserId() failed for session: " + sessionid);
            return "";
        }
    }

    /**
     * Return XML document listing all pages and tools in those pages for a given site.
     * The session id must be of a valid, active user in that site, or a super user, or it will throw an exception.
     * If a page is hidden in a site, the page and all tools in that page will be skipped from the returned document, as they are in the portal.
     * Super user's can request any site to retrieve the full list.
     *
     * @param sessionid the session id of a user in a site, or a super user
     * @param siteid    the site to retrieve the information for
     * @return xml or an empty list <site/>. The return XML format is below:
     * <site id="9ec48d9e-b690-4090-a300-10a44ed7656e">
     * <pages>
     * <page id="ec1b0ab8-90e8-4d4d-bf64-1e586035f08f">
     * <page-title>Home</page-title>
     * <tools>
     * <tool id="dafd2a4d-8d3f-4f4c-8e12-171968b259cd">
     * <tool-id>sakai.iframe.site</tool-id>
     * <tool-title>Welcome</tool-title>
     * </tool>
     * ...
     * </tools>
     * </page>
     * <page>
     * ...
     * </page>
     * ...
     * </pages>
     * </site>
     * @throws RuntimeException if not a super user and the user attached to the session is not in the site, if site does not exist
     */
    @WebMethod
    @Path("/getPagesAndToolsForSiteForCurrentUser")
    @Produces("text/plain")
    @GET
    public String getPagesAndToolsForSiteForCurrentUser(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid) {
        Session session = establishSession(sessionid);

        //check if site exists
        Site site;
        try {
            site = siteService.getSite(siteid);
        } catch (Exception e) {
            log.warn("WS getPagesAndToolsForSiteForCurrentUser(): Error looking up site: " + siteid + ":" + e.getClass().getName() + " : " + e.getMessage());
            throw new RuntimeException("WS getPagesAndToolsForSiteForCurrentUser(): Error looking up site: " + siteid + ":" + e.getClass().getName() + " : " + e.getMessage());
        }

        String userId = session.getUserId();

        //check if super user
        boolean isSuperUser = false;
        if (securityService.isSuperUser(userId)) {
            isSuperUser = true;
        }

        //if not super user, check user is a member of the site, and get their Role
        Role role;
        if (!isSuperUser) {
            Member member = site.getMember(userId);
            if (member == null || !member.isActive()) {
                log.warn("WS getPagesAndToolsForSiteForCurrentUser(): User: " + userId + " does not exist in site : " + siteid);
                throw new RuntimeException("WS getPagesAndToolsForSiteForCurrentUser(): User: " + userId + " does not exist in site : " + siteid);
            }
            role = member.getRole();
        }


        //get list of pages in the site, if none, return empty list
        List<SitePage> pages = site.getPages();
        if (pages.isEmpty()) {
            return "<site id=\"" + site.getId() + "\"/>";
        }

        //site node
        Document dom = Xml.createDocument();
        Element siteNode = dom.createElement("site");
        Attr siteIdAttr = dom.createAttribute("id");
        siteIdAttr.setNodeValue(site.getId());
        siteNode.setAttributeNode(siteIdAttr);

        //pages node
        Element pagesNode = dom.createElement("pages");

        for (SitePage page : pages) {

            //page node
            Element pageNode = dom.createElement("page");
            Attr pageIdAttr = dom.createAttribute("id");
            pageIdAttr.setNodeValue(page.getId());
            pageNode.setAttributeNode(pageIdAttr);

            //pageTitle
            Element pageTitleNode = dom.createElement("page-title");
            pageTitleNode.appendChild(dom.createTextNode(page.getTitle()));

            //get tools in page
            List<ToolConfiguration> tools = page.getTools();

            Element toolsNode = dom.createElement("tools");

            boolean includePage = true;
            for (ToolConfiguration toolConfig : tools) {
                //if we not a superAdmin, check the page properties
                //if any tool on this page is hidden, skip the rest of the tools and exclude this page from the output
                //this makes the behaviour consistent with the portal

                //if not superUser, process  tool function requirements
                if (!isSuperUser) {

                    //skip processing tool if we've skipped tools previously on this page
                    if (!includePage) {
                        continue;
                    }

                    //skip this tool if not visible, ultimately hiding the whole page
                    if (!toolManager.isVisible(site, toolConfig)) {
                        includePage = false;
                        break;
                    }
                }

                //if we got this far, add the details about the tool to the document
                Element toolNode = dom.createElement("tool");

                //tool uuid
                Attr toolIdAttr = dom.createAttribute("id");
                toolIdAttr.setNodeValue(toolConfig.getId());
                toolNode.setAttributeNode(toolIdAttr);

                //registration (eg sakai.profile2)
                Element toolIdNode = dom.createElement("tool-id");
                toolIdNode.appendChild(dom.createTextNode(toolConfig.getToolId()));
                toolNode.appendChild(toolIdNode);

                Element toolTitleNode = dom.createElement("tool-title");
                toolTitleNode.appendChild(dom.createTextNode(toolConfig.getTitle()));
                toolNode.appendChild(toolTitleNode);

                toolsNode.appendChild(toolNode);

            }

            //if the page is not hidden, add the elements
            if (includePage) {
                pageNode.appendChild(pageTitleNode);
                pageNode.appendChild(toolsNode);
                pagesNode.appendChild(pageNode);
            }
        }

        //add the main nodes
        siteNode.appendChild(pagesNode);
        dom.appendChild(siteNode);

        return Xml.writeDocumentToString(dom);
    }

    /**
     * Alternative method signature which will first get a session for the given user,
     * and then get the list of pages & tools visible to that user in the site.
     *
     * @param sessionid must be a valid session for a superuser
     * @param userid    eid, eg jsmith26
     * @param siteid    site to get the list for.
     * @return
     * @
     */
    @WebMethod
    @Path("/getPagesAndToolsForSite")
    @Produces("text/plain")
    @GET
    public String getPagesAndToolsForSite(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "userid", partName = "userid") @QueryParam("userid") String userid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid) {

        //get a session for the other user, reuse if possible
        String newsessionid = getSessionForUser(sessionid, userid, true);

        //might be an exception that was returned, so check session is valid.
        Session session = establishSession(newsessionid);

        //ok, so hand over to main method to get the list for us
        return getPagesAndToolsForSiteForCurrentUser(newsessionid, siteid);

    }

    /**
     * Copy the resources from a site to another site.
     *
     * @param sessionid         the id of a valid session
     * @param sourcesiteid      the id of the source site
     * @param destinationsiteid the id of the destiny site
     * @return success or exception message
     */
    @WebMethod
    @Path("/copyResources")
    @Produces("text/plain")
    @GET
    public String copyResources(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "sourcesiteid", partName = "sourcesiteid") @QueryParam("sourcesiteid") String sourcesiteid,
            @WebParam(name = "destinationsiteid", partName = "destinationsiteid") @QueryParam("destinationsiteid") String destinationsiteid) {

        Session session = establishSession(sessionid);

        try {

            //check if both sites exist
            Site site = siteService.getSite(sourcesiteid);
            site = siteService.getSite(destinationsiteid);

            // If not admin, check maintainer membership in the source site
            if (!securityService.isSuperUser(session.getUserId()) &&
                    !securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference())) {
                log.warn("WS copyResources(): Permission denied. Must be super user to copy a site in which you are not a maintainer.");
                throw new RuntimeException("WS copyResources(): Permission denied. Must be super user to copy a site in which you are not a maintainer.");
            }

            //transfer content
            transferCopyEntities(
                    "sakai.resources",
                    contentHostingService.getSiteCollection(sourcesiteid),
                    contentHostingService.getSiteCollection(destinationsiteid));

        } catch (Exception e) {
            log.error("WS copyResources(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Change the short description of a site
     *
     * @param sessionid        the id of a valid session
     * @param siteid           the id of the site you want to change the title of
     * @param shortDescription the new short description
     * @return success or string containing error
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/changeSiteShortDescription")
    @Produces("text/plain")
    @GET
    public String changeSiteShortDescription(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "shortDescription", partName = "shortDescription") @QueryParam("shortDescription") String shortDescription) {

        Session session = establishSession(sessionid);

        try {

            // check short description
            if (shortDescription != null) {
                StringBuilder alertMsg = new StringBuilder();
                shortDescription = FormattedText.processFormattedText(shortDescription, alertMsg);
                if (shortDescription == null) {
                    throw new RuntimeException("Site short description markup rejected: " + alertMsg.toString());
                }
            }

            Site siteEdit = null;
            siteEdit = siteService.getSite(siteid);
            siteEdit.setShortDescription(shortDescription);
            siteService.save(siteEdit);

        } catch (Exception e) {
            log.error("WS changeSiteShortDescription(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }


    /**
     * Adds a tool to a site, includes adding a page.
     *
     * @param pagelayout if you want the page to be single or double column (0 or 1). Any other value will revert to 0.
     * @param sessionid  the id of a valid session for the admin user
     * @param siteid     the id of the site to add the page to
     * @param toolid     the id of the tool you want to add (ie sakai.profile2)
     * @param pagetitle  the desired title for the page shown in the site navigation. If null, will use default from tool configuration.
     * @param tooltitle  the desired title for the tool shown in the main portlet.  If null, will use default from tool configuration. Custom tool titles only respected if you also have a custom page title.
     * @param position   integer specifying the position of this page within other pages on the site (0 means top, for right at the bottom a large enough number, ie 99)
     * @param popup      boolean for if it should be a popup window or not
     * @return success or exception
     * @throws RuntimeException
     */
    @WebMethod
    @Path("/addToolAndPageToSite")
    @Produces("text/plain")
    @GET
    public String addToolAndPageToSite(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "toolid", partName = "toolid") @QueryParam("toolid") String toolid,
            @WebParam(name = "pagetitle", partName = "pagetitle") @QueryParam("pagetitle") String pagetitle,
            @WebParam(name = "tooltitle", partName = "tooltitle") @QueryParam("tooltitle") String tooltitle,
            @WebParam(name = "pagelayout", partName = "pagelayout") @QueryParam("pagelayout") int pagelayout,
            @WebParam(name = "position", partName = "position") @QueryParam("position") int position,
            @WebParam(name = "popup", partName = "popup") @QueryParam("popup") boolean popup) {
        Session session = establishSession(sessionid);

        //do we want a custom title, or use the defaults from the tool?
        boolean customTitle = false;
        if (StringUtils.isNotBlank(pagetitle)) {
            customTitle = true;
        }

        try {

            //get site
            Site siteEdit = siteService.getSite(siteid);

            //add the page
            SitePage sitePageEdit = siteEdit.addPage();

            if (customTitle) {
                sitePageEdit.setTitle(pagetitle);
                sitePageEdit.setTitleCustom(true);
            }
            sitePageEdit.setLayout(pagelayout);

            sitePageEdit.setPosition(position);
            sitePageEdit.setPopup(popup);

            // Check that the tool is visible (not stealthed) and available for this site type (category)
            if (!securityService.isSuperUser(session.getUserId())) {

                Set categories = new HashSet<String>();
                Set<Tool> visibleTools = toolManager.findTools(categories, null);

                boolean toolVisible = false;
                for (Tool tool : visibleTools) {
                    if (tool.getId().equals(toolid)) {
                        toolVisible = true;
                    }
                }

                if (!toolVisible) {
                    log.warn("WS addToolAndPageToSite(): Permission denied. Must be super user to add a stealthed tool to a site.");
                    throw new RuntimeException("WS addToolAndPageToSite(): Permission denied. Must be super user to add a stealthed tool to a site.");
                }

                categories.add(siteEdit.getType());
                Set<Tool> availableTools = toolManager.findTools(categories, null);

                boolean toolAvailable = false;
                for (Tool tool : availableTools) {
                    if (tool.getId().equals(toolid)) {
                        toolAvailable = true;
                    }
                }

                if (!toolAvailable) {
                    log.warn("WS addToolAndPageToSite(): Permission denied. Must be super user to add a tool which is not available for this site type.");
                    throw new RuntimeException("WS addToolAndPageToSite(): Permission denied. Must be super user to add a tool which is not available for this site type.");
                }
            }

            //add the tool
            ToolConfiguration tool = sitePageEdit.addTool();

            tool.setTool(toolid, toolManager.getTool(toolid));

            //set custom tool title if we have a custom page title and custom tool title is sent, otherwise set default from tool config
            if (customTitle && StringUtils.isNotBlank(tooltitle)) {
                tool.setTitle(tooltitle);
            } else {
                tool.setTitle(toolManager.getTool(toolid).getTitle());
            }

            siteService.save(siteEdit);
            log.info("Page and tool added for site:" + siteid);

            return "success";
        } catch (Exception e) {
            log.error("WS addToolAndPageToSite(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
    }


    /**
     * Get a user property.
     *
     * @param sessionid    valid session
     * @param eid          id of the user to query
     * @param propertyName name of the property to retrieve
     * @return
     * @
     */
    @WebMethod
    @Path("/getUserProperty")
    @Produces("text/plain")
    @GET
    public String getUserProperty(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "propertyName", partName = "propertyName") @QueryParam("propertyName") String propertyName) {
        Session session = establishSession(sessionid);
        try {
            User user = userDirectoryService.getUserByEid(eid);
            return user.getProperties().getProperty(propertyName);
        } catch (Exception e) {
            log.error("WS getUserProperty() failed for user: " + eid + " : " + e.getClass().getName() + " : " + e.getMessage());
            return "";
        }
    }

    /**
     * Sets a property for the user
     *
     * @param sessionid
     *            The session id.
     * @param eid
     *            The user eid.
     * @param key
     *            The property key.
     * @param value
     *             The property value.
     * @return
     *			  Success or exception message
     */
    @WebMethod
    @Path("/setUserProperty")
    @Produces("text/plain")
    @GET
    public String setUserProperty(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "key", partName = "key") @QueryParam("key") String key,
            @WebParam(name = "value", partName = "value") @QueryParam("value") String value){
        Session session = establishSession(sessionid);

        if (!securityService.isSuperUser(session.getUserId())) {
            log.warn("WS setUserProperty(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS setUserProperty(): Permission denied. Restricted to super users.");
        }

        try {
            String userid = userDirectoryService.getUserByEid(eid).getId();
            UserEdit user = userDirectoryService.editUser(userid);
            user.getPropertiesEdit().addProperty(key, value);
            userDirectoryService.commitEdit(user);
        }
        catch (Exception e) {
            log.warn("WS setUserProperty(): " + e.getClass().getName() + " : " + e.getMessage(), e);
            return "failure";
        }
        return "success";
    }

    /**
     * Find Sites that have a particular propertySet regardless of the value - Returns empty <list/> if not found
     *
     * @param sessionid    valid session
     * @param propertyName name of the property to search for.
     * @return Sites as xml
     */
    @WebMethod
    @Path("/findSitesByProperty")
    @Produces("text/plain")
    @GET
    public String findSitesByProperty(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "propertyName", partName = "propertyName") @QueryParam("propertyName") String propertyName) {
        //register the session with presence
        Session s = establishSession(sessionid);

        Map propertyCriteria = new HashMap();

        // search for anything that has propertyName set, "%" will do it
        propertyCriteria.put(propertyName, "%");
        return findSites(propertyCriteria);
    }

    /**
     * Find Sites with a property set to a specified value - Returns empty <list/> if not found
     *
     * @param sessionid     valid session
     * @param propertyName  name of the property to search for
     * @param propertyValue value that the property much have to satisfy the query
     * @return Sites as xml
     */
    @WebMethod
    @Path("/findSitesByPropertyValue")
    @Produces("text/plain")
    @GET
    public String findSitesByPropertyValue(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "propertyName", partName = "propertyName") @QueryParam("propertyName") String propertyName,
            @WebParam(name = "propertyValue", partName = "propertyValue") @QueryParam("propertyValue") String propertyValue) {
        //register the session with presence
        Session s = establishSession(sessionid);

        Map propertyCriteria = new HashMap();

        propertyCriteria.put(propertyName, propertyValue);
        return findSites(propertyCriteria);
    }

    /**
     * Helper method to find sites based on a property criteria
     *
     * @param propertyCriteria site properties with values that wil lbe used in the search
     * @returnSites as xml
     */
    private String findSites(Map propertyCriteria) {

        Document dom = Xml.createDocument();
        Node list = dom.createElement("list");
        dom.appendChild(list);

        try {
            List siteList = siteService.getSites(SelectionType.ANY, null, null,
                    propertyCriteria, SortType.NONE, null);

            if (siteList != null && siteList.size() > 0) {
                for (Iterator i = siteList.iterator(); i.hasNext(); ) {
                    Site site = (Site) i.next();
                    Node item = dom.createElement("site");
                    Node siteId = dom.createElement("siteId");
                    siteId.appendChild(dom.createTextNode(site.getId()));
                    Node siteTitle = dom.createElement("siteTitle");
                    siteTitle.appendChild(dom.createTextNode(site.getTitle()));
                    item.appendChild(siteId);
                    item.appendChild(siteTitle);
                    if (site.getProperties() != null) {
                        for (Iterator j = site.getProperties().getPropertyNames(); j.hasNext(); ) {
                            String name = (String) j.next();
                            Node siteProperty = dom.createElement(name);
                            siteProperty.appendChild(dom.createTextNode((String) site.getProperties().get(name)));
                            item.appendChild(siteProperty);
                        }
                    }
                    list.appendChild(item);
                }
            }
        } catch (Throwable t) {
            log.warn(this + ".findSite: Error encountered" + t.getMessage(), t);
        }

        return Xml.writeDocumentToString(dom);
    }


    /**
     * Get the placement ID for a given tool in the given site
     *
     * @param sessionid valid session
     * @param siteId    ID of the site we are looking at
     * @param toolId    tool id eg. "sakai.gradebook.gwt.rpc", we are looking for
     * @return first placement id found in the site for the given tool. If the tool/site is not found, the response will be empty.
     * @
     */
    @WebMethod
    @Path("/getPlacementId")
    @Produces("text/plain")
    @GET
    public String getPlacementId(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteId", partName = "siteId") @QueryParam("siteId") String siteId,
            @WebParam(name = "toolId", partName = "toolId") @QueryParam("toolId") String toolId) {
        // register the session with presence
        Session s = establishSession(sessionid);
        try {
            Site site = siteService.getSite(siteId);
            // log.warn("found site" + siteId);
            if (site != null) {

                ToolConfiguration toolConfiguration = site.getToolForCommonId(toolId);
                if (toolConfiguration != null) {
                    return toolConfiguration.getId();
                }

            }
        } catch (Throwable t) {
            log.warn(this + "getPlacementId(): Error encountered: " + t.getMessage(), t);
        }
        return "";
    }

    /**
     * Copy the content from a site to another site. It creates a list of tools in the source site and transfers that content
     * to the destination site.
     *
     * @param sessionid         the id of a valid session
     * @param sourcesiteid      the id of the source site
     * @param destinationsiteid the id of the destiny site
     * @return success or exception message
     */
    @WebMethod
    @Path("/copySiteContent")
    @Produces("text/plain")
    @GET
    public String copySiteContent(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "sourcesiteid", partName = "sourcesiteid") @QueryParam("sourcesiteid") String sourcesiteid,
            @WebParam(name = "destinationsiteid", partName = "destinationsiteid") @QueryParam("destinationsiteid") String destinationsiteid) {

        Session session = establishSession(sessionid);

        try {

            //check if both sites exist
            Site site = siteService.getSite(sourcesiteid);
            site = siteService.getSite(destinationsiteid);

            //check if super user
            boolean isSuperUser = false;
            if (securityService.isSuperUser(session.getUserId())) {
                isSuperUser = true;
            }

            // If not admin, check maintainer membership in the source site
            if (!isSuperUser && !securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference())) {
                log.warn("WS copySiteContent(): Permission denied. Must be super user to copy a site in which you are not a maintainer.");
                throw new RuntimeException("WS copySiteContent(): Permission denied. Must be super user to copy a site in which you are not a maintainer.");
            }

            List<SitePage> pages = site.getPages();
            Set<String> toolIds = new HashSet();
            for (SitePage page : pages) {

                //get tools in page
                List<ToolConfiguration> tools = page.getTools();
                boolean includePage = true;
                for (ToolConfiguration toolConfig : tools) {
                    //if we not a superAdmin, check the page properties
                    //if any tool on this page is hidden, skip the rest of the tools and exclude this page from the output
                    //this makes the behaviour consistent with the portal

                    //if not superUser, process  tool function requirements
                    if (!isSuperUser) {

                        //skip processing tool if we've skipped tools previously on this page
                        if (!includePage) {
                            continue;
                        }

                        //skip this tool if not visible, ultimately hiding the whole page
                        if (!toolManager.isVisible(site, toolConfig)) {
                            includePage = false;
                            break;
                        }
                    }
                    toolIds.add(toolConfig.getToolId());
                }
            }

            for (String toolId : toolIds)
            {
                Map<String,String> entityMap;
                Map transversalMap = new HashMap();
                
        		if (!toolId.equalsIgnoreCase("sakai.resources"))
        		{
        			entityMap = transferCopyEntities(toolId, sourcesiteid, destinationsiteid);
        		}
        		else
        		{
        			entityMap = transferCopyEntities(toolId, contentHostingService.getSiteCollection(sourcesiteid), contentHostingService.getSiteCollection(destinationsiteid));
        		}
        		
        		if(entityMap != null)
        		{
        			transversalMap.putAll(entityMap);
        		}

        		updateEntityReferences(toolId, sourcesiteid, transversalMap, site);
            }
        } catch (Exception e) {
            log.error("WS copySiteContent(): " + e.getClass().getName() + " : " + e.getMessage(), e);
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Copy the content from a site to another site for only the content of the specified tool
     *
     * @param sessionid         the id of a valid session
     * @param sourcesiteid      the id of the source site
     * @param destinationsiteid the id of the destiny site
     * @param toolid            the tool id for which content should be copied
     * @return success or exception message
     */
    @WebMethod
    @Path("/copySiteContentForTool")
    @Produces("text/plain")
    @GET
    public String copySiteContentForTool(
    		@WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
    		@WebParam(name = "sourcesiteid", partName = "sourcesiteid") @QueryParam("sourcesiteid") String sourcesiteid,
    		@WebParam(name = "destinationsiteid", partName = "destinationsiteid") @QueryParam("destinationsiteid") String destinationsiteid,
    		@WebParam(name = "toolid", partName = "toolid") @QueryParam("toolid") String toolid)
    {
    	Session session = establishSession(sessionid);
    	Set<String> toolsCopied = new HashSet<String>();
    	Map transversalMap = new HashMap();

    	try
    	{
    		//check if both sites exist
    		Site site = siteService.getSite(sourcesiteid);
    		site = siteService.getSite(destinationsiteid);

    		// If not admin, check maintainer membership in the source site
    		if (!securityService.isSuperUser(session.getUserId()) && !securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference()))
    		{
    			log.warn("WS copySiteContentForTool(): Permission denied. Must be super user to copy a site in which you are not a maintainer.");
    			throw new RuntimeException("WS copySiteContentForTool(): Permission denied. Must be super user to copy a site in which you are not a maintainer.");
    		}

    		Map<String,String> entityMap;
    		if (!toolid.equalsIgnoreCase("sakai.resources"))
    		{
    			entityMap = transferCopyEntities(toolid, sourcesiteid, destinationsiteid);
    		}
    		else
    		{
    			entityMap = transferCopyEntities(toolid, contentHostingService.getSiteCollection(sourcesiteid), contentHostingService.getSiteCollection(destinationsiteid));
    		}
    		
    		if(entityMap != null)
    		{
    			transversalMap.putAll(entityMap);
    		}

    		updateEntityReferences(toolid, sourcesiteid, transversalMap, site);
    	}
    	catch (Exception e)
    	{
    		log.error("WS copySiteContentForTool(): " + e.getClass().getName() + " : " + e.getMessage(), e);
    		return e.getClass().getName() + " : " + e.getMessage();
    	}
    	return "success";
    }

    /**
     * Transfer a copy of all entites from another context for any entity
     * producer that claims this tool id.
     *
     * @param toolId      The tool id.
     * @param fromContext The context to import from.
     * @param toContext   The context to import into.
     */
    protected Map transferCopyEntities(String toolId, String fromContext, String toContext)
    {
    	Map transversalMap = new HashMap();

    	// offer to all EntityProducers
    	for (Iterator i = entityManager.getEntityProducers().iterator(); i.hasNext();)
    	{
    		EntityProducer ep = (EntityProducer) i.next();
    		if (ep instanceof EntityTransferrer)
    		{
    			try
    			{
    				EntityTransferrer et = (EntityTransferrer) ep;

    				// if this producer claims this tool id
    				if (ArrayUtil.contains(et.myToolIds(), toolId))
    				{
    					if(ep instanceof EntityTransferrerRefMigrator)
    					{
    						EntityTransferrerRefMigrator etMp = (EntityTransferrerRefMigrator) ep;
    						Map<String,String> entityMap = etMp.transferCopyEntitiesRefMigrator(fromContext, toContext, new ArrayList(), true);
    						if(entityMap != null)
    						{
    							transversalMap.putAll(entityMap);
    						}
    					}
    					else
    					{
    						et.transferCopyEntities(fromContext, toContext,	new ArrayList(), true);
    					}
    				}
    			}
    			catch (Throwable t)
    			{
    				log.warn("Error encountered while asking EntityTransfer to transferCopyEntities from: " + fromContext + " to: " + toContext, t);
    			}
    		}
    	}
    	
    	// record direct URL for this tool in old and new sites, so anyone using the URL in HTML text will 
		// get a proper update for the HTML in the new site
		// Some tools can have more than one instance. Because getTools should always return tools
		// in order, we can assume that if there's more than one instance of a tool, the instances
		// correspond

		Site fromSite = null;
		Site toSite = null;
		Collection<ToolConfiguration> fromTools = null;
		Collection<ToolConfiguration> toTools = null;
		try
		{
		    fromSite = siteService.getSite(fromContext);
		    toSite = siteService.getSite(toContext);
		    fromTools = fromSite.getTools(toolId);
		    toTools = toSite.getTools(toolId);
		}
		catch (Exception e)
		{
			log.warn("transferCopyEntities: can't get site:" + e.getMessage());
		}

		// getTools appears to return tools in order. So we should be able to match them
		if (fromTools != null && toTools != null)
		{
		    Iterator<ToolConfiguration> toToolIt = toTools.iterator();
		    for (ToolConfiguration fromTool: fromTools)
		    {
				if (toToolIt.hasNext())
				{
				    ToolConfiguration toTool = toToolIt.next();
				    String fromUrl = serverConfigurationService.getPortalUrl() + "/directtool/" + Web.escapeUrl(fromTool.getId()) + "/";
				    String toUrl = serverConfigurationService.getPortalUrl() + "/directtool/" + Web.escapeUrl(toTool.getId()) + "/";
				    if (transversalMap.get(fromUrl) == null)
				    {
				    	transversalMap.put(fromUrl, toUrl);
				    }
				    if (shortenedUrlService.shouldCopy(fromUrl))
				    {
						fromUrl = shortenedUrlService.shorten(fromUrl, false);
						toUrl = shortenedUrlService.shorten(toUrl, false);
						if (fromUrl != null && toUrl != null)
						{
						    transversalMap.put(fromUrl, toUrl);
						}
				    }
				}
				else
				{
				    break;
				}
		    }
		}

    	return transversalMap;
    }
    
    
    protected void updateEntityReferences(String toolId, String toContext, Map transversalMap, Site newSite)
    {
		if (toolId.equalsIgnoreCase("sakai.iframe.site"))
		{
			updateSiteInfoToolEntityReferences(transversalMap, newSite);
		}
		else
		{		
			for (Iterator i = entityManager.getEntityProducers().iterator(); i.hasNext();)
			{
				EntityProducer ep = (EntityProducer) i.next();
				if (ep instanceof EntityTransferrerRefMigrator && ep instanceof EntityTransferrer)
				{
					try
					{
						EntityTransferrer et = (EntityTransferrer) ep;
						EntityTransferrerRefMigrator etRM = (EntityTransferrerRefMigrator) ep;

						// if this producer claims this tool id
						if (ArrayUtil.contains(et.myToolIds(), toolId))
						{
							etRM.updateEntityReferences(toContext, transversalMap);
						}
					}
					catch (Throwable t)
					{
						log.error("Error encountered while asking EntityTransfer to updateEntityReferences at site: " + toContext, t);
					}
				}
			}
		}
	}
    
	private void updateSiteInfoToolEntityReferences(Map transversalMap, Site newSite)
	{
		if(transversalMap != null && transversalMap.size() > 0 && newSite != null)
		{
			Set<Entry<String, String>> entrySet = (Set<Entry<String, String>>) transversalMap.entrySet();
			
			String msgBody = newSite.getDescription();
			if(msgBody != null && !"".equals(msgBody))
			{
				boolean updated = false;
				Iterator<Entry<String, String>> entryItr = entrySet.iterator();
				while(entryItr.hasNext())
				{
					Entry<String, String> entry = (Entry<String, String>) entryItr.next();
					String fromContextRef = entry.getKey();
					if(msgBody.contains(fromContextRef))
					{
						msgBody = msgBody.replace(fromContextRef, entry.getValue());
						updated = true;
					}
				}	
				if(updated)
				{
					//update the site b/c some tools (Lessonbuilder) updates the site structure (add/remove pages) and we don't want to
					//over write this
					try
					{
						newSite = siteService.getSite(newSite.getId());
						newSite.setDescription(msgBody);
						siteService.save(newSite);
					}
					catch (IdUnusedException e) {
						// TODO:
					}
					catch (PermissionException p)
					{
						// TODO:
					}
				}
			}
		}
	}
    
    /**
     * Get any subsites for a given site
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site to retrieve the list of subsites for
     * @return xml doc of the list of sites
     * @throws RuntimeException returns <exception /> string if exception encountered and logs it
     */
    @WebMethod
    @Path("/getSubSites")
    @Produces("text/plain")
    @GET
    public String getSubSites(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid) {

        Session s = establishSession(sessionid);

        try {
            List<Site> subSites = siteService.getSubSites(siteid);
            String xml = getSiteListXml(subSites);
            return xml;
        } catch (Exception e) {
            log.error("WS getSubSites(): " + e.getClass().getName() + " : " + e.getMessage());
            return "<exception/>";

        }
    }
    
    /**
     * Get the parent siteId for a site, if it is a child site.
     *
     * @param sessionid the id of a valid session
     * @param siteid    the id of the site to retrieve the parent site id for
     * @return the siteId of the parent site, if there is one
     */
    @WebMethod
    @Path("/getParentSite")
    @Produces("text/plain")
    @GET
    public String getParentSite(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid) {

        Session s = establishSession(sessionid);
        String parent = siteService.getParentSite(siteid);
        if (parent == null) {
            parent = "";
        }
        return parent;
    }
    
    /**
     * Renders a list of sites as XML to ensure consistency amongst webservice requests
     * 
     * @param sites List of sites
     * @return XML string
     */
    private String getSiteListXml(List<Site> sites) {
    	Document dom = Xml.createDocument();
        Node list = dom.createElement("list");
        dom.appendChild(list);

        for (Site site: sites) {
            Node item = dom.createElement("item");
            Node siteId = dom.createElement("siteId");
            siteId.appendChild(dom.createTextNode(site.getId()));
            Node siteTitle = dom.createElement("siteTitle");
            siteTitle.appendChild(dom.createTextNode(site.getTitle()));

            item.appendChild(siteId);
            item.appendChild(siteTitle);
            list.appendChild(item);
        }

        return Xml.writeDocumentToString(dom);
    }

/**
     * Adds LTI tool to a site 
     *
     * @param    sessionid    a valid session id
     * @param    siteId        site identifier where to add the LTI tool
     * @param    toolTitle    custom title for the tool. May be empty for default value 
     * @param    properties    comma separated list of LTI properties. Example : final.allowlori:false,final.allowroster:false,imsti.allowlori:,imsti.allowroster:on,imsti.allowoutcomes:,final.allowoutcomes:false,imsti.allowsettings:,final.allowsettings:false,imsti.contentlink:,final.contentlink:false,imsti.custom:,final.custom:false,final.debug:false,imsti.encryptedsecret:,imsti.frameheight:,final.frameheight:false,imsti.key:KEY,final.key:false,imsti.launch:http://MYURL,final.launch:false,final.maximize:false,final.newpage:false,imsti.pagetitle:Virtual Meeting,final.pagetitle:false,final.releaseemail:false,final.releasename:false,imsti.secret:SECRETKEY,final.secret:false,imsti.splash:,final.splash:false,imsti.tooltitle:Virtual Meeting,final.tooltitle:false,imsti.xml:,final.xml:false,imsti.maximize:,imsti.newpage:,imsti.debug:,imsti.releaseemail:on,imsti.releasename:on
     * @return    Success or exception message    
     *
     */
    @WebMethod
    @Path("/addLTITool")
    @Produces("text/plain")
    @GET
    public String addLTITool(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteId", partName = "siteId") @QueryParam("siteId") String siteId,
            @WebParam(name = "toolTitle", partName = "toolTitle") @QueryParam("toolTitle") String toolTitle,
            @WebParam(name = "properties", partName = "properties") @QueryParam("properties") String properties
        ){
        
        Session session = establishSession(sessionid);

        boolean customTitle = false;

        String toolid = "sakai.basiclti";

        try {

            //get site
            Site siteEdit = siteService.getSite(siteId);

            //add the page
            SitePage sitePageEdit = siteEdit.addPage();
            sitePageEdit.setTitle(toolTitle);
            sitePageEdit.setTitleCustom(true);
            sitePageEdit.setLayout(0);
            sitePageEdit.setPosition(0);
            sitePageEdit.setPopup(false);

            // Check that the tool is visible (not stealthed) and available for this site type (category)
            if (!securityService.isSuperUser(session.getUserId())) {

                Set categories = new HashSet<String>();
                Set<Tool> visibleTools = toolManager.findTools(categories, null);

                boolean toolVisible = false;
                for (Tool tool : visibleTools) {
                    if (tool.getId().equals(toolid)) {
                        toolVisible = true;
                    }
                }

                if (!toolVisible) {
                    log.warn("WS addLTITool(): Permission denied. Must be super user to add a stealthed tool to a site.");
                    throw new RuntimeException("WS addLTITool(): Permission denied. Must be super user to add a stealthed tool to a site.");
                }

                categories.add(siteEdit.getType());
                Set<Tool> availableTools = toolManager.findTools(categories, null);

                boolean toolAvailable = false;
                for (Tool tool : availableTools) {
                    if (tool.getId().equals(toolid)) {
                        toolAvailable = true;
                    }
                }

                if (!toolAvailable) {
                    log.warn("WS addLTITool(): Permission denied. Must be super user to add a tool which is not available for this site type.");
                    throw new RuntimeException("WS addLTITool(): Permission denied. Must be super user to add a tool which is not available for this site type.");
                }
            }

            //add the tool
            ToolConfiguration tool = sitePageEdit.addTool();
            
            //set LTI properties
            setToolProperties(tool, properties);

            tool.setTool(toolid, toolManager.getTool(toolid));

            if (StringUtils.isNotBlank(toolTitle)) {
                tool.setTitle(toolTitle);
            } else {
                tool.setTitle(toolManager.getTool(toolid).getTitle());
            }

            siteService.save(siteEdit);
            log.info("WS addLTITool(): LTI tool added for site:" + siteId);

            return "success";
        } catch (Exception e) {
            log.error("WS addLTITool(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
    }
    
    private void setToolProperties(ToolConfiguration tool, String propList) {
        if(propList != null) {
            for(String prop : propList.split(",")) {
                if(StringUtils.isNotEmpty(prop)) {
                    int index = prop.indexOf(":");
                    if(index >= 0) {
                        try {
                            String propName = prop.substring(0, index);
                            String propValue = prop.substring(index+1);
                            
                            if(StringUtils.isNotEmpty(propValue)) {
                                Properties propsedit = tool.getPlacementConfig();
                                propsedit.setProperty(propName, propValue);
                            }
                        } catch(Exception e){
                            log.error("SakaiScript: setToolProperties(): " + e.getClass().getName() + " : " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    @WebMethod
    @Path("/getSessionCountForServer")
    @Produces("text/plain")
    @GET
    public Integer getSessionCountForServer(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "serverid", partName = "serverid") @QueryParam("serverid") String serverid,
            @WebParam(name = "millisBeforeExpire", partName = "millisBeforeExpire") @QueryParam("millisBeforeExpire") int millisBeforeExpire) {
        //register the session with presence
        Session s = establishSession(sessionid);

        if (!securityService.isSuperUser()) {
            log.warn("NonSuperUser trying to get Session Count For Server: " + s.getUserId());
            throw new RuntimeException("NonSuperUser trying to get Session Count For Server: " + s.getUserId());
        }
        try {
            Map servers = usageSessionService.getOpenSessionsByServer();
            List matchingServers = (List) getServersByServerId(servers).get(serverid);

            if (matchingServers.size() == 0) {
                log.warn("can't find any sessions for server with id=" + serverid);
                return new Integer(0);
            }

            Collections.sort(matchingServers);
            // find the latest started server with matching id
            String serverKey = (String) matchingServers.get(matchingServers.size() - 1);

            return getSessionCountForServer(servers, serverKey, millisBeforeExpire);
        } catch (Exception e) {
            log.error("error in getSessionsForServer() ws call:" + e.getMessage(), e);
        }
        return new Integer(0);
    }


    @WebMethod
    @Path("/getSessionTotalCount")
    @Produces("text/plain")
    @GET
    public int getSessionTotalCount(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "millisBeforeExpire", partName = "millisBeforeExpire") @QueryParam("millisBeforeExpire") int millisBeforeExpire) {
        //register the session with presence
        Session s = establishSession(sessionid);
        if (!securityService.isSuperUser()) {
            log.warn("NonSuperUser trying to get Total Session Count: " + s.getUserId());
            throw new RuntimeException("NonSuperUser trying to get Total Session Count: " + s.getUserId());
        }
        int count = 0;

        try {
            Map servers = usageSessionService.getOpenSessionsByServer();
            Map serversByServerId = getServersByServerId(servers);


            for (Iterator i = serversByServerId.keySet().iterator(); i.hasNext(); ) {
                String serverKey = (String) i.next();
                List matchingServers = (List) serversByServerId.get(serverKey);
                Collections.sort(matchingServers);
                // find the latest started server, and add to count
                count += getSessionCountForServer(servers, (String) matchingServers.get(matchingServers.size() - 1), millisBeforeExpire);
            }
        } catch (Exception e) {
            log.error("error in getSessionsForServer() ws call:" + e.getMessage(), e);
        }
        return new Integer(count);
    }

    private Integer getSessionCountForServer(Map servers, String serverKey, int millisBeforeExpire) {
        int count = 0;
        List selectedServer = (List) servers.get(serverKey);

        if (selectedServer != null) {
            for (Iterator i = selectedServer.iterator(); i.hasNext(); ) {
                UsageSession session = (UsageSession) i.next();
                Long lastActivityTime = activityService.getLastEventTimeForUser(session.getUserId());
                if (lastActivityTime != null &&
                        ((new Date().getTime() - lastActivityTime) < millisBeforeExpire)) {
                    log.warn("adding count for " + serverKey);
                    count++;
                } else {
                    log.warn("not including user:" + session.getUserEid() +
                            " in active session count last activity was more than " +
                            millisBeforeExpire + " ago or no activity detected.");
                }
            }
        } else {
            log.warn("can't find any sessions for server with id=" + serverKey);
        }
        return new Integer(count);

    }

    protected Map getServersByServerId(Map servers) {
        Map serverByServerId = new HashMap();

        // create Map of servers key'd by serverId only
        for (Iterator i = servers.keySet().iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            List matchingServers;
            String serverKey = key.split("-")[0];
            if (!serverByServerId.containsKey(serverKey)) {
                matchingServers = new ArrayList();
                serverByServerId.put(serverKey, matchingServers);
            } else {
                matchingServers = (List) serverByServerId.get(serverKey);
            }

            log.warn("adding " + key + " to " + serverKey + " list");
            matchingServers.add(key);
        }
        return serverByServerId;
    }

    /**
     * Check if a user exists (either as an account in Sakai or in any external provider)
     *
     * @param sessionid the id of a valid session
     * @param userid    the internal user id
     * @return true/false
     */
    @WebMethod
    @Path("/checkForUserById")
    @Produces("text/plain")
    @GET
    public boolean checkForUserById(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "userid", partName = "userid") @QueryParam("userid") String userid) {
        Session s = establishSession(sessionid);
        if (!securityService.isSuperUser()) {
            log.warn("NonSuperUser trying to checkForUserById: " + s.getUserId());
            throw new RuntimeException("NonSuperUser trying to checkForUserById: " + s.getUserId());
        }

        try {
            User u = null;
            u = userDirectoryService.getUser(userid);
            if (u != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("WS checkForUserById(): " + e.getClass().getName() + " : " + e.getMessage());
            return false;
        }
    }


    @WebMethod
    @Path("/isSuperUser")
    @Produces("text/plain")
    @GET
    public boolean isSuperUser(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid) {
        Session s = establishSession(sessionid);

        return securityService.isSuperUser(s.getUserId());

    }

    @WebMethod
    @Path("/resetAllUserWorkspace")
    @Produces("text/plain")
    @GET
    public boolean resetAllUserWorkspace(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid) {
        Session session = establishSession(sessionid);
        //check that ONLY super user's are accessing this
        if (!securityService.isSuperUser(session.getUserId())) {
            log.warn("WS resetAllUserWorkspace(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS resetAllUserWorkspace(): Permission denied. Restricted to super users.");
        }

        try {
            List<String> siteList = siteService.getSiteIds(SelectionType.ANY, null, null,
                    null, SortType.NONE, null);
            if (siteList != null && siteList.size() > 0) {
                for (Iterator i = siteList.iterator(); i.hasNext(); ) {
                    String siteId =  (String) i.next();
                    if (siteService.isUserSite(siteId) && !(siteId.equals("~admin"))){
                        siteService.removeSite(siteService.getSite(siteId));
                    }
                }
            }
        } catch (Throwable t) {
            log.warn(this + ".resetAllUserWorkspace: Error encountered" + t.getMessage(), t);
            return false;
        }

        return true;
    }


    @Path("/changeSitePublishStatus")
    @Produces("text/plain")
    @GET
    public String changeSitePublishStatus(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "published", partName = "published") @QueryParam("published") boolean published) {
        Session session = establishSession(sessionid);

        try {

            Site siteEdit = null;
            siteEdit = siteService.getSite(siteid);
            siteEdit.setPublished(published);
            siteService.save(siteEdit);

        } catch (Exception e) {
            log.error("WS changeSitePublishStatus(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    @WebMethod
    @Path("/checkForMemberInSite")
    @Produces("text/plain")
    @GET
    public boolean checkForMemberInSite(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid) {
        Session session = establishSession(sessionid);

        if (!securityService.isSuperUser(session.getUserId())) {
            log.warn("WS checkForMemberInSite(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS checkForMemberInSite(): Permission denied. Restricted to super users.");
        }

        try {
            Site site = siteService.getSite(siteid);
            String userid = userDirectoryService.getUserByEid(eid).getId();
            Member member = site.getMember(userid);
            if (member == null) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @WebMethod
    @Path("/getAvailableRoles")
    @Produces("text/plain")
    @GET
    public String getAvailableRoles(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid) {
        Session session = establishSession(sessionid);

        if (!securityService.isSuperUser(session.getUserId())) {
            log.warn("WS getAvailableRoles(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS getAvailableRoles(): Permission denied. Restricted to super users.");
        }

        Set roles;
        Iterator iRoles;

        Document dom = Xml.createDocument();
        Node list = dom.createElement("list");
        dom.appendChild(list);
        try {
            AuthzGroup authzgroup = authzGroupService.getAuthzGroup(authzgroupid);
            roles = authzgroup.getRoles();
            for (iRoles = roles.iterator(); iRoles.hasNext(); ) {
                Role r = (Role) iRoles.next();
                Node item = dom.createElement("role");
                Node roleId = dom.createElement("roleId");
                roleId.appendChild(dom.createTextNode(r.getId()));
                Node roleDescription = dom.createElement("roleDescription");
                roleDescription.appendChild(dom.createTextNode(r.getDescription()));
                item.appendChild(roleId);
                item.appendChild(roleDescription);
                list.appendChild(item);
            }
        } catch (Exception e) {
            log.error("WS getAvailableRoles(): " + e.getClass().getName() + " : " + e.getMessage(), e);
            return "";
        }
        return Xml.writeDocumentToString(dom);
    }

    @WebMethod
    @Path("/getExistingFunctions")
    @Produces("text/plain")
    @GET
    public String getExistingFunctions(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionId,
            @WebParam(name = "authzgroupid", partName = "authzgroupid") @QueryParam("authzgroupid") String authzgroupid,
            @WebParam(name = "roleid", partName = "roleid") @QueryParam("roleid") String roleid) {
        Session session = establishSession(sessionId);

        if (!securityService.isSuperUser(session.getUserId())) {
            log.warn("WS getAvailableRoles(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS getAvailableRoles(): Permission denied. Restricted to super users.");
        }

        Document dom = Xml.createDocument();
        Node list = dom.createElement("list");
        dom.appendChild(list);

        try {
            AuthzGroup authzgroup = authzGroupService.getAuthzGroup(authzgroupid);
            Role role = authzgroup.getRole(roleid);

            //get functions that are in this role
            Set existingfunctions = role.getAllowedFunctions();
            Iterator it = existingfunctions.iterator();

            Node item = dom.createElement("functions");
            while (it.hasNext()) {
                Node function = dom.createElement("function");
                function.appendChild(dom.createTextNode((String) it.next()));
                item.appendChild(function);
            }
            list.appendChild(item);
        } catch (Exception e) {
            log.error("WS getAvailableRoles(): " + e.getClass().getName() + " : " + e.getMessage(), e);
            return "";
        }
        return Xml.writeDocumentToString(dom);
    }

    @WebMethod
    @Path("/getSiteDefaultJoinerRole")
    @Produces("text/plain")
    @GET
    public String getSiteDefaultJoinerRole(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid) {

        Session session = establishSession(sessionid);

        if (!securityService.isSuperUser(session.getUserId())) {
            log.warn("WS getSiteDefaultJoinerRole(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS getSiteDefaultJoinerRole(): Permission denied. Restricted to super users.");
        }

        try {
            Site site = siteService.getSite(siteid);
            if (site != null) {
                return site.getJoinerRole();
            } else {
                log.warn("WS getSiteDefaultJoinerRole() failed. Unable to find site:" + siteid);
                throw new RuntimeException("WS failed. Unable to find site:" + siteid);
            }
        } catch (Exception e) {
            log.warn("WS getSiteDefaultJoinerRole():"+ e.getClass().getName() + " : " + e.getMessage(), e);
            return e.getClass().getName() + " : " + e.getMessage();
        }
    }

    /**
     * Sets the TimeZone for the user
     *
     * @param sessionid
     *            The session id.
     * @param eid
     *            The user eid.
     * @param timeZoneId
     *            The TimeZone id.
     * @return
     *			  Success or exception message
     */

    @WebMethod
    @Path("/setUserTimeZone")
    @Produces("text/plain")
    @GET
    public String setUserTimeZone(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "timeZoneId", partName = "timeZoneId") @QueryParam("timeZoneId") String timeZoneId){

        Session session = establishSession(sessionid);

        if (!securityService.isSuperUser(session.getUserId())) {
            log.warn("WS setUserTimeZone(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS setUserTimeZone(): Permission denied. Restricted to super users.");
        }

        try {
            User user = userDirectoryService.getUserByEid(eid);
            PreferencesEdit prefs = null;
            try {
                prefs = preferencesService.edit(user.getId());
            } catch (Exception e1) {
                prefs = preferencesService.add(user.getId());
            }

            ResourcePropertiesEdit props = prefs.getPropertiesEdit(timeService.APPLICATION_ID);
            props.addProperty(timeService.TIMEZONE_KEY, timeZoneId);
            preferencesService.commit(prefs);

        } catch (Exception e) {
            log.error("WS setUserTimeZone(): " + e.getClass().getName() + " : " + e.getMessage(), e);
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    /**
     * Activate/Deactivate an user in a site
     *
     * @param 	sessionid 	a valid session id
     * @param 	siteid 		the id of the site
     * @param 	eid 		the id of the user to activate/deactivate
     * @param 	active 		true for activate, false to deactivate
     * @return	true if all went ok or exception otherwise
     * @return	Success or exception message
     */

    @WebMethod
    @Path("/changeSiteMemberStatus")
    @Produces("text/plain")
    @GET
    public String changeSiteMemberStatus(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "active", partName = "active") @QueryParam("active") boolean active){

        Session session = establishSession(sessionid);

        try {
            User user = userDirectoryService.getUserByEid(eid);
            String realmId = siteService.siteReference(siteid);
            if (!authzGroupService.allowUpdate(realmId) || !siteService.allowUpdateSiteMembership(siteid)) {
                String errorMessage = "WS changeSiteMemberStatus(): Site : " + siteid +" membership not updatable ";
                log.warn(errorMessage);
                return errorMessage;
            }
            AuthzGroup realmEdit = authzGroupService.getAuthzGroup(realmId);
            Member userMember = realmEdit.getMember(user.getId());
            if(userMember == null) {
                String errorMessage = "WS changeSiteMemberStatus(): User: " + user.getId() + " does not exist in site : " + siteid ;
                log.warn(errorMessage);
                return errorMessage;
            }
            userMember.setActive(active);
            authzGroupService.save(realmEdit);
        } catch (Exception e) {
            log.error("WS changeSiteMemberStatus(): " + e.getClass().getName() + " : " + e.getMessage(), e);
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";

    }
}
