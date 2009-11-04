package org.sakaiproject.site.tool.helper.participant.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.EmailValidator;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.util.Participant;
import org.sakaiproject.sitemanage.api.UserNotificationProvider;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.sakaiproject.util.StringUtil;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
/**
 * 
 * @author 
 *
 */
public class SiteAddParticipantHandler {
	
    /** Our log (commons). */
    private static Log M_log = LogFactory.getLog(SiteAddParticipantHandler.class);

	private static final String EMAIL_CHAR = "@";
    public SiteService siteService = null;
    public AuthzGroupService authzGroupService = null;
    public ToolManager toolManager = null;
    public SessionManager sessionManager = null;
    public ServerConfigurationService serverConfigurationService;
    private final String HELPER_ID = "sakai.tool.helper.id";

    public MessageLocator messageLocator;
    
    private UserNotificationProvider notiProvider;
    
    // Tool session attribute name used to schedule a whole page refresh.
    public static final String ATTR_TOP_REFRESH = "sakai.vppa.top.refresh"; 
	
    public TargettedMessageList targettedMessageList;
	public void setTargettedMessageList(TargettedMessageList targettedMessageList) {
		this.targettedMessageList = targettedMessageList;
	}
    
    public Site site = null;
    
	public String officialAccountParticipant = null;
	public String getOfficialAccountParticipant() {
		return officialAccountParticipant;
	}

	private UserDirectoryService userDirectoryService;	
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public void setOfficialAccountParticipant(String officialAccountParticipant) {
		this.officialAccountParticipant = officialAccountParticipant;
	}

	public String nonOfficialAccountParticipant = null;
	
	public String getNonOfficialAccountParticipant() {
		return nonOfficialAccountParticipant;
	}

	public void setNonOfficialAccountParticipant(
			String nonOfficialAccountParticipant) {
		this.nonOfficialAccountParticipant = nonOfficialAccountParticipant;
	}
	
	// for eids inside this list, don't look them up as email ids
	public List<String> officialAccountEidOnly = new Vector<String>();
	public List<String> getOfficialAccountEidOnly()
	{
		return officialAccountEidOnly;
	}
	public void setOfficialAccountEidOnly(List<String> officialAccountEidOnly)
	{
		this.officialAccountEidOnly = officialAccountEidOnly;
	}
	
	
	/*whether the role choice is for same role or different role */
	public String roleChoice = "sameRole";
	
    public String getRoleChoice() {
		return roleChoice;
	}

	public void setRoleChoice(String roleChoice) {
		this.roleChoice = roleChoice;
	}
	
	/*whether the same role used for all users */
	public String sameRoleChoice = null;
	
    public String getSameRoleChoice() {
		return sameRoleChoice;
	}

	public void setSameRoleChoice(String sameRoleChoice) {
		this.sameRoleChoice = sameRoleChoice;
	}
	
	/* the email notification setting */
	public String emailNotiChoice = Boolean.FALSE.toString();
	
    public String getEmailNotiChoice() {
		return emailNotiChoice;
	}

	public void setEmailNotiChoice(String emailNotiChoice) {
		this.emailNotiChoice = emailNotiChoice;
	}

        /** realm for the site **/
        public AuthzGroup realm = null;
        public String siteId = null;

	/** the role set for the site **/
	public List<Role> roles = new Vector<Role>();
	public List<Role> getRoles()
	{
		if (roles.isEmpty())
			init();
		return roles;
	}
	public void setRoles (List<Role> roles)
	{
		this.roles = roles;
	}
	
	/** the user selected */
	public List<UserRoleEntry> userRoleEntries = new Vector<UserRoleEntry>();
	
	public String getUserRole(String userId)
	{
		String rv = "";
		if (userRoleEntries != null)
		{
			for (UserRoleEntry entry:userRoleEntries)
			{
				if (entry.userEId.equals(userId))
				{
					rv = entry.role;
				}
			}
		}
		
		return rv;
	}
	
	public List<String> getUsers()
	{
		List<String> rv = new Vector<String>();
		if (userRoleEntries != null)
		{
			for (UserRoleEntry entry:userRoleEntries)
			{
				rv.add(entry.userEId);
			}
		}
		return rv;
		
	}
	/**
     * Initialization method, just gets the current site in preparation for other calls
	 * @throws  
     *
     */
    public void init() {
        if (site == null) {
            try {
                siteId = sessionManager.getCurrentToolSession()
                        .getAttribute(HELPER_ID + ".siteId").toString();
            }
            catch (java.lang.NullPointerException npe) {
                // Site ID wasn't set in the helper call!!
            }
            
            if (siteId == null) {
                siteId = toolManager.getCurrentPlacement().getContext();
            }
            
            try {    
                site = siteService.getSite(siteId);
                realm = authzGroupService.getAuthzGroup(siteService.siteReference(siteId));
                for(Iterator<Role> i = realm.getRoles().iterator(); i.hasNext();)
                { 
                	Role r = (Role) i.next();
                	roles.add(r);
                }
            
            } catch (IdUnusedException e) {
                // The siteId we were given was bogus
                e.printStackTrace();
            } catch (GroupNotDefinedException e) {
                // The siteId we were given was bogus
                e.printStackTrace();
            }
            
        }
    }
    
    /**
     * get the site title
     * @return
     */
    public String getSiteTitle()
    {
    	String rv = "";
    	if (site == null)
    		init();
    	if (site != null)
    		rv = site.getTitle();
    	
    	return rv;
    }
    
    /**
     * is current site a course site?
     * @return
     */
    public boolean isCourseSite()
    {
    	boolean rv = false;
		String courseSiteType = getServerConfigurationString("courseSiteType", "course");
		if (site == null)
			init();
		if (site != null && courseSiteType.equals(site.getType()))
		{
			rv = true;
		}
		return rv;
    }
    
    /**
     * get the configuration string value
     * @param param
     * @return
     */
    public String getServerConfigurationString(String param)
    {
    	return getServerConfigurationString(param, null);
    }
    
    /**
     * get the configuration string value
     * @param param
     * @param defaultValue
     * @return
     */
    public String getServerConfigurationString(String param, String defaultValue)
    {
    	return serverConfigurationService.getString(param, defaultValue);
    }
    
    /**
     * Allows the Cancel button to return control to the tool calling this helper
     * @return
     */
    public String processCancel() {
        ToolSession session = sessionManager.getCurrentToolSession();
        session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);
        resetTargettedMessageList();
        reset();

        return "done";
    }
    
    /**
     * get role choice and go to difference html page based on that
     * @return
     */
    public String processGetParticipant() {
    	// reset errors
    	resetTargettedMessageList();
    	// reset user list
    	resetUserRolesEntries();
    	checkAddParticipant();
    	if (targettedMessageList != null && targettedMessageList.size() > 0 && targettedMessageList.isError())
    	{
    		
    		// there is error, remain on the same page
    		return "";
    	}
    	else
    	{
    		// go to next step
    		return roleChoice;
    	}
    }
    
    private void resetTargettedMessageList()
    {
    	targettedMessageList.clear();
    }
    
    private void resetUserRolesEntries()
    {
    	userRoleEntries = new Vector<UserRoleEntry>(); 
    }
    
    /**
     * get the same role choice and continue
     * @return
     */
    public String processSameRoleContinue() {
    	targettedMessageList.clear();
    	if (sameRoleChoice == null)
    	{
    		targettedMessageList.addMessage(new TargettedMessage("java.pleasechoose", null, TargettedMessage.SEVERITY_ERROR));
    		return null;
    	}
    	else
    	{
    		resetTargettedMessageList();

	        // if user doesn't have full rights, don't let him add one with site update
	    	if (!authzGroupService.allowUpdate("/site/" + siteId)) {
	    	if (realm == null)
	    		init();
		    Role r = realm.getRole(sameRoleChoice);
		    if (r != null && r.isAllowed("site.upd")) {
			targettedMessageList.addMessage(new TargettedMessage("java.roleperm", new Object[] { sameRoleChoice }, TargettedMessage.SEVERITY_ERROR));
			return null;
		    }
		}

	    	if (userRoleEntries != null)
			{
				for (UserRoleEntry entry:userRoleEntries)
				{
					entry.role = sameRoleChoice;
				}
			}
	    	
	        return "continue";
    	}
    }
    
    /**
     * back to the first add participant page
     * @return
     */
    public String processSameRoleBack() {
    	resetTargettedMessageList();
        return "back";
    }
    
    /**
     * get the different role choice and continue
     * @return
     */
    public String processDifferentRoleContinue() {
	
		resetTargettedMessageList();
		if (!authzGroupService.allowUpdate("/site/" + siteId)) {
		    Set<String> roles = new HashSet<String>();
		    for (UserRoleEntry entry : userRoleEntries)
		    	roles.add(entry.role);
		    for (String rolename: roles) {
				Role r = realm.getRole(rolename);
				if (r != null && r.isAllowed("site.upd")) {
				    targettedMessageList.addMessage(new TargettedMessage("java.roleperm", new Object[] { rolename }, TargettedMessage.SEVERITY_ERROR));
				    return null;
				}
		    }
		}
        return "continue";
    }
    
    /**
     * back to the first add participant page
     * @return
     */
    public String processDifferentRoleBack() {
    	resetTargettedMessageList();
        return "back";
    }
    
    /**
     * get the email noti choice and continue
     * @return
     */
    public String processEmailNotiContinue() {

    	resetTargettedMessageList();
        return "continue";
    }
    
    /**
     * back to the previous role choice page
     * @return
     */
    public String processEmailNotiBack() {
    	resetTargettedMessageList();
    	if (roleChoice.equals("sameRole"))
    	{
    		return "backSameRole";
    	}
    	else
    	{
    		return "backDifferentRole";
    	}
    }
    
	/**
	 * whether the eId is considered of official account
	 * @param eId
	 * @return
	 */
	private boolean isOfficialAccount(String eId) {
		return eId.indexOf(EMAIL_CHAR) == -1;
	}
	
	/*
	 * Given a list of user eids, add users to realm If the user account does
	 * not exist yet inside the user directory, assign role to it @return A list
	 * of eids for successfully added users
	 */
	private List<String> addUsersRealm( boolean notify) {
		// return the list of user eids for successfully added user
		List<String> addedUserEIds = new Vector<String>();

		if (userRoleEntries != null && !userRoleEntries.isEmpty()) {
			if (site == null)
				init();
			if (site != null) {
				// get realm object
				String realmId = site.getReference();
				try {
					AuthzGroup realmEdit = authzGroupService.getAuthzGroup(realmId);
					boolean allowUpdate = authzGroupService.allowUpdate(realmId);
					Set<String>okRoles = new HashSet<String>();
					for (UserRoleEntry entry: userRoleEntries) {
						String eId = entry.userEId;
						String role =entry.role;
						// this check should never trigger, as we check it earlier
						// however I'm worried about users manually calling this page directly
						if (!allowUpdate && !okRoles.contains(role)) {
						    Role r = realmEdit.getRole(role);
						    if (r != null && r.isAllowed("site.upd")) {
							targettedMessageList.addMessage(new TargettedMessage("java.roleperm",
					                 new Object[] { role }, 
						          TargettedMessage.SEVERITY_ERROR));
							continue;
						    }
						    okRoles.add(role);
						}

						try {
							User user = userDirectoryService.getUserByEid(eId);
							if (authzGroupService.allowUpdate(realmId)
									|| siteService.allowUpdateSiteMembership(site.getId())) 
							{
								realmEdit.addMember(user.getId(), role, true,
										false);
								addedUserEIds.add(eId);

								// send notification
								if (notify) {
									// send notification email
									notiProvider.notifyAddedParticipant(!isOfficialAccount(eId), user, site.getTitle());
									
								}
							}
						} catch (UserNotDefinedException e) {
							targettedMessageList.addMessage(new TargettedMessage("java.account",
					                new Object[] { eId }, 
					                TargettedMessage.SEVERITY_INFO));
							M_log.warn(this  + ".addUsersRealm: cannot find user with eid= " + eId);
						} // try
					} // for

					try {
						// post event about adding participant
						EventTrackingService.post(EventTrackingService.newEvent(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP, realmEdit.getId(),false));
						authzGroupService.save(realmEdit);
					} catch (GroupNotDefinedException ee) {
						targettedMessageList.addMessage(new TargettedMessage("java.realm",new Object[] { realmId }, TargettedMessage.SEVERITY_INFO));
						M_log.warn(this + ".addUsersRealm: cannot find realm for" + realmId);
					} catch (AuthzPermissionException ee) {
						targettedMessageList.addMessage(new TargettedMessage("java.permeditsite",new Object[] { realmId }, TargettedMessage.SEVERITY_INFO));
						M_log.warn(this + ".addUsersRealm: don't have permission to edit realm " + realmId);
					}
				} catch (GroupNotDefinedException eee) {
					targettedMessageList.addMessage(new TargettedMessage("java.realm",new Object[] { realmId }, TargettedMessage.SEVERITY_INFO));
					M_log.warn(this + ".addUsersRealm: cannot find realm for " + realmId);
				} catch (Exception eee) {
					M_log.warn(this + ".addUsersRealm: " + eee.getMessage() + " realmId=" + realmId);
				}
			}
		}

		return addedUserEIds;

	} // addUsersRealm
	
    /**
     * get the confirm choice and continue
     * @return
     */
    public String processConfirmContinue() {

    	resetTargettedMessageList();
    	if (site == null)
    		init();
    	for (UserRoleEntry entry:userRoleEntries) {
			String eId = entry.userEId;
			
			if (isOfficialAccount(eId)) {
				// if this is a officialAccount
			} else {
				// if this is an nonOfficialAccount
				try {
					userDirectoryService.getUserByEid(eId);
				} catch (UserNotDefinedException e) {
					// if there is no such user yet, add the user
					try {
						UserEdit uEdit = userDirectoryService
								.addUser(null, eId);

						// set email address
						uEdit.setEmail(eId);

						// set the guest user type
						uEdit.setType("guest");
						
						// set the guest first name
						String firstName = entry.firstName;
						if (firstName != null  && firstName.length() > 0)
							
							uEdit.setFirstName(entry.firstName);
						
						// set the guest last name
						String lastName = entry.firstName;
						if (lastName != null  && lastName.length() > 0)
							uEdit.setLastName(entry.lastName);

						// set password to a positive random number
						Random generator = new Random(System
								.currentTimeMillis());
						Integer num = Integer.valueOf(generator
								.nextInt(Integer.MAX_VALUE));
						if (num.intValue() < 0)
							num = Integer.valueOf(num.intValue() * -1);
						String pw = num.toString();
						uEdit.setPassword(pw);

						// and save
						userDirectoryService.commitEdit(uEdit);

						boolean notifyNewUserEmail = (getServerConfigurationString("notifyNewUserEmail", Boolean.TRUE.toString()))
								.equalsIgnoreCase(Boolean.TRUE.toString());
						if (notifyNewUserEmail) {    						
								notiProvider.notifyNewUserEmail(uEdit, pw, site != null ? site.getTitle():"");
						}
					} catch (UserIdInvalidException ee) {
						targettedMessageList.addMessage(new TargettedMessage("java.isinval",new Object[] { eId }, TargettedMessage.SEVERITY_INFO));
						M_log.warn(this + ".doAdd_participant: id " + eId + " is invalid");
					} catch (UserAlreadyDefinedException ee) {
						targettedMessageList.addMessage(new TargettedMessage("java.beenused",new Object[] { eId }, TargettedMessage.SEVERITY_INFO));
						M_log.warn(this + ".doAdd_participant: id " + eId + " has been used");
					} catch (UserPermissionException ee) {
						targettedMessageList.addMessage(new TargettedMessage("java.haveadd",new Object[] { eId }, TargettedMessage.SEVERITY_INFO));
						M_log.warn(this + ".doAdd_participant: You don't have permission to add " + eId);
					}
				}
			}
		}

		// batch add and updates the successful added list
		List<String> addedParticipantEIds = addUsersRealm(Boolean.parseBoolean(emailNotiChoice));

		// update the not added user list
		String notAddedOfficialAccounts = "";
		String notAddedNonOfficialAccounts = "";
		for (UserRoleEntry entry:userRoleEntries)
		{
			String iEId = entry.userEId;
			if (!addedParticipantEIds.contains(iEId)) {
				if (isOfficialAccount(iEId)) {
					// no email in eid
					notAddedOfficialAccounts = notAddedOfficialAccounts
							.concat(iEId + "\n");
				} else {
					// email in eid
					notAddedNonOfficialAccounts = notAddedNonOfficialAccounts
							.concat(iEId + "\n");
				}
			}
		}

		if (addedParticipantEIds.size() != 0
				&& (!notAddedOfficialAccounts.equals("") || !notAddedNonOfficialAccounts.equals(""))) {
			// at lease one officialAccount account or an nonOfficialAccount
			// account added, and there are also failures
			targettedMessageList.addMessage(new TargettedMessage("java.allusers", null, TargettedMessage.SEVERITY_INFO));
		}
    		
		// time to reset user inputs
		reset();
		
        return "done";
    }
    
    /**
     * back to the email notification page
     * @return
     */
    public String processConfirmBack() {
    	resetTargettedMessageList();
        return "back";
    }
    
    /**
     * Gets the current tool
     * @return Tool
     */
    public Tool getCurrentTool() {
        return toolManager.getCurrentTool();
    }
    
    /** check the participant input **/
    private void checkAddParticipant() {
		// get the participants to be added
		int i;
		if (site == null)
			init();
		Vector<Participant> pList = new Vector<Participant>();
		HashSet<String> existingUsers = new HashSet<String>();

		// accept officialAccounts and/or nonOfficialAccount account names
		String officialAccounts = "";
		String nonOfficialAccounts = "";

		// check that there is something with which to work
		officialAccounts = StringUtil.trimToNull(officialAccountParticipant);
		nonOfficialAccounts = StringUtil.trimToNull(nonOfficialAccountParticipant);

		// if there is no eid or nonOfficialAccount entered
		if (officialAccounts == null && nonOfficialAccounts == null) {
			targettedMessageList.addMessage(new TargettedMessage("java.guest", null, TargettedMessage.SEVERITY_ERROR));
		}

		String at = "@";

		if (officialAccounts != null) {
			// adding officialAccounts
			String[] officialAccountArray = officialAccounts
					.split("\r\n");

			for (i = 0; i < officialAccountArray.length; i++) {
				String officialAccount = StringUtil.trimToNull(officialAccountArray[i].replaceAll("[\t\r\n]", ""));
				// if there is some text, try to use it
				if (officialAccount != null) {
					// automatically add nonOfficialAccount account
					Participant participant = new Participant();
					User u = null;
					String eidsForAllMatches = "";
					String eidsForAllMatchesAlert = "";
					
					if (officialAccount.indexOf(at) == -1)
					{
						// is not of email format, then look up by eid only
						try {
							// look for user based on eid first
							u = userDirectoryService.getUserByEid(officialAccount);
						} catch (UserNotDefinedException e) {
							M_log.debug(this + ".checkAddParticipant: " + officialAccount + " " + messageLocator.getMessage("java.username") + " ");
						}
					}
					else
					{
						// is email. Need to lookup by both eid and email address
						try {
							// look for user based on eid first
							u = userDirectoryService.getUserByEid(officialAccount);
						} catch (UserNotDefinedException e) {
							M_log.debug(this + ".checkAddParticipant: " + officialAccount + " " + messageLocator.getMessage("java.username") + " ");
						}
						
						//Changed user lookup to satisfy BSP-1010 (jholtzman)
						// continue to look for the user by their email address
						// if the email address is not marked as eid only
						if (!officialAccountEidOnly.contains(officialAccount))
						{
							Collection<User> usersWithEmail = userDirectoryService.findUsersByEmail(officialAccount);
							
							if(usersWithEmail != null) {
								if(usersWithEmail.size() == 0) {
									// If the collection is empty, we didn't find any users with this email address
									M_log.debug("Unable to find users with email " + officialAccount);
								} else if (usersWithEmail.size() == 1 && u == null) {
									// We found one user with this email address.  Use it.
									u = (User)usersWithEmail.iterator().next();
								} else if (!usersWithEmail.isEmpty()) {
									// If we have multiple users with this email address, expand the list with all matching user's eids and let the instructor choose from them
									M_log.debug("Found multiple user with email " + officialAccount);
									
									// multiple matches
									for (User user : usersWithEmail)
									{
										String eid = user.getEid();
										eidsForAllMatches += eid + "\n";
										eidsForAllMatchesAlert += eid + ", ";
										
										// this is to mark the eid so that it won't be used again for email lookup in the future
										officialAccountEidOnly.add(eid);
									}
									
									// update ui input
									updateOfficialAccountParticipant(officialAccount, u, eidsForAllMatches);
									
									// show alert message
									targettedMessageList.addMessage(new TargettedMessage("java.username.multiple", 
											new Object[] { officialAccount, eidsForAllMatchesAlert }, 
							                TargettedMessage.SEVERITY_ERROR));
								}
							}
						}
					}
						
					if (u != null)
					{
						M_log.debug("found user with eid " + u.getEid());
						if (site != null && site.getUserRole(u.getId()) != null) {
							// user already exists in the site, cannot be added
							// again
							existingUsers.add(officialAccount);
						} else {
							participant.name = u.getDisplayName();
							participant.uniqname = u.getEid();
							participant.active = true;
							pList.add(participant);
						}
						
						// update the userRoleTable
						if (!getUsers().contains(officialAccount))
						{
							userRoleEntries.add(new UserRoleEntry(u.getEid(), ""));
						}
					}
					else if (eidsForAllMatches.length() == 0)
					{
						// not valid user
						targettedMessageList.addMessage(new TargettedMessage("java.username",
				                new Object[] { officialAccount }, 
				                TargettedMessage.SEVERITY_ERROR));
					}
				}
			}
		} // officialAccounts

		if (nonOfficialAccounts != null) {
			String[] nonOfficialAccountArray = nonOfficialAccounts.split("\r\n");
			for (i = 0; i < nonOfficialAccountArray.length; i++) {
				String nonOfficialAccountAll = StringUtil.trimToNull(nonOfficialAccountArray[i].replaceAll("[\t\r\n]", ""));
				// the format of per user entry is: email address,first name,last name
				// comma separated
				String[] nonOfficialAccountParts  = nonOfficialAccountAll.split(",");
				if (nonOfficialAccountParts.length > 3)
				{
					// if the input contains more fields than "email address,first name,last name", show an alert
					targettedMessageList.addMessage(new TargettedMessage("add.multiple.nonofficial.alert.more",
			                new Object[] {nonOfficialAccountAll}, 
			                TargettedMessage.SEVERITY_ERROR));
					break;
				}
				String userEid = nonOfficialAccountParts[0].trim();
				// get last name, if any
				String userLastName = "";
				if (nonOfficialAccountParts.length > 1)
				{
					userLastName = nonOfficialAccountParts[1].trim();
				}
				// get first name, if any
				String userFirstName = "";
				if (nonOfficialAccountParts.length > 2)
				{
					userFirstName = nonOfficialAccountParts[2].trim();
				}
				// remove the trailing dots
				while (userEid != null && userEid.endsWith(".")) {
					userEid = userEid.substring(0, userEid.length() - 1);
				}

				if (userEid != null && userEid.length() > 0) {
					String[] parts = userEid.split(at);

					if (userEid.indexOf(at) == -1) {
						// must be a valid email address
						targettedMessageList.addMessage(new TargettedMessage("java.emailaddress",
				                new Object[] { userEid }, 
				                TargettedMessage.SEVERITY_ERROR));
					} else if ((parts.length != 2) || (parts[0].length() == 0)) {
						// must have both id and address part
						targettedMessageList.addMessage(new TargettedMessage("java.notemailid", 
				                new Object[] { userEid }, 
				                TargettedMessage.SEVERITY_ERROR));
					} else if (!EmailValidator.getInstance().isValid(userEid)) { 
						targettedMessageList.addMessage(new TargettedMessage("java.emailaddress",
				                new Object[] { userEid }, 
				                TargettedMessage.SEVERITY_ERROR));
						targettedMessageList.addMessage(new TargettedMessage("java.theemail", "no text"));
					} else if (userEid != null
							&& !isValidDomain(userEid)) {
						// wrong string inside nonOfficialAccount id
						targettedMessageList.addMessage(new TargettedMessage("java.emailbaddomain",
								new Object[] { userEid, messageLocator.getMessage("nonOfficialAccountSectionTitle")}, 
								TargettedMessage.SEVERITY_ERROR));
					} else if (!isValidMail(userEid)) {
						// must be a valid email address
						targettedMessageList.addMessage(new TargettedMessage("java.emailaddress",
				                new Object[] { userEid }, 
				                TargettedMessage.SEVERITY_ERROR));
					} else {
						Participant participant = new Participant();
						try {
							// if the nonOfficialAccount user already exists
							User u = userDirectoryService
									.getUserByEid(userEid);
							if (site != null && site.getUserRole(u.getId()) != null) {
								// user already exists in the site, cannot be
								// added again
								existingUsers.add(userEid);
							} else {
								participant.name = u.getDisplayName();
								participant.uniqname = userEid;
								participant.active = true;
								pList.add(participant);
							}
						} catch (UserNotDefinedException e) {
							M_log.debug("no user with eid: " + userEid);
							
							/*
							 * The account may exist with a different eid
							 */
							User u = null;
							Collection<User> usersWithEmail = userDirectoryService.findUsersByEmail(userEid);
							if(usersWithEmail != null) {
								M_log.debug("found a collection of matching email users:  " + usersWithEmail.size());
								if(usersWithEmail.size() == 0) {
									// If the collection is empty, we didn't find any users with this email address
									M_log.info("Unable to find users with email " + userEid);
								} else if (usersWithEmail.size() == 1) {
									// We found one user with this email address.  Use it.
									u = (User)usersWithEmail.iterator().next();
								} else if (usersWithEmail.size() > 1) {
									// If we have multiple users with this email address, pick one and log this error condition
									// TODO Should we not pick a user?  Throw an exception?
									M_log.warn("Found multiple user with email " + userEid);
									u = (User)usersWithEmail.iterator().next();
								}
							}
							
							if (u == null) {
							
							// if the nonOfficialAccount user is not in the system
							// yet
							participant.name = userEid;
							participant.uniqname = userEid; // TODO:
							// what
							// would
							// the
							// UDS
							// case
							// this
							// name
							// to?
							// -ggolden
							participant.active = true;
							} else  {
								M_log.debug("adding: " + u.getDisplayName() + ", " + u.getEid());
								participant.name = u.getDisplayName();
								participant.uniqname = u.getEid();
								participant.active = true;
								userEid = u.getEid();
							}
							pList.add(participant);
						}
						
						// update the userRoleTable
						if (!getUsers().contains(userEid))
						{
							userRoleEntries.add(new UserRoleEntry(userEid, "", userFirstName, userLastName));
						}
					}
				} // if
			} // 	
		} // nonOfficialAccounts

		if (roleChoice.equals("same_role")) {
			targettedMessageList.addMessage(new TargettedMessage("java.roletype", null, TargettedMessage.SEVERITY_ERROR));
		}

		// remove duplicate or existing user from participant list
		pList = removeDuplicateParticipants(pList);

		// if the add participant list is empty after above removal, stay in the
		// current page
		// add alert for attempting to add existing site user(s)
		if (!existingUsers.isEmpty()) {
			int count = 0;
			String accounts = "";
			for (Iterator<String> eIterator = existingUsers.iterator(); eIterator
					.hasNext();) {
				if (count == 0) {
					accounts = (String) eIterator.next();
				} else {
					accounts = accounts + ", " + (String) eIterator.next();
				}
				count++;
			}

			targettedMessageList.addMessage(new TargettedMessage("add.existingpart.1", new Object[]{accounts}, TargettedMessage.SEVERITY_ERROR));
			targettedMessageList.addMessage(new TargettedMessage("add.existingpart.2", null, TargettedMessage.SEVERITY_ERROR));
		}

		return;

	} // checkAddParticipant
    

	private boolean isValidDomain(String email) {
		String invalidNonOfficialAccountString = getServerConfigurationString("invalidNonOfficialAccountString", null);

		if (invalidNonOfficialAccountString != null) {
			String[] invalidDomains = invalidNonOfficialAccountString.split(",");

			for (int i = 0; i < invalidDomains.length; i++) {
				String domain = invalidDomains[i].trim();

				if (email.toLowerCase().indexOf(domain.toLowerCase()) != -1) {
					return false;
				}
			}
		}
		return true;
		
		
	}
    
	private boolean isValidMail(String email) {
		if (email == null || email.equals(""))
			return false;
		
		email = email.trim();
		
		EmailValidator ev = EmailValidator.getInstance();
		return ev.isValid(email);
		
	}
	
	private Vector<Participant> removeDuplicateParticipants(List<Participant> pList) {
		// check the uniqueness of list member
		Set<String> s = new HashSet<String>();
		Set<String> uniqnameSet = new HashSet<String>();
		Vector<Participant> rv = new Vector<Participant>();
		for (int i = 0; i < pList.size(); i++) {
			Participant p = (Participant) pList.get(i);
			if (!uniqnameSet.contains(p.getUniqname())) {
				// no entry for the account yet
				rv.add(p);
				uniqnameSet.add(p.getUniqname());
			} else {
				// found duplicates
				s.add(p.getUniqname());
			}
		}

		if (!s.isEmpty()) {
			int count = 0;
			String accounts = "";
			for (Iterator<String> i = s.iterator(); i.hasNext();) {
				if (count == 0) {
					accounts = (String) i.next();
				} else {
					accounts = accounts + ", " + (String) i.next();
				}
				count++;
			}
			if (count == 1) {
				targettedMessageList.addMessage(new TargettedMessage("add.duplicatedpart.single",new Object[]{accounts}, TargettedMessage.SEVERITY_INFO));
			} else {
				targettedMessageList.addMessage(new TargettedMessage("add.duplicatedpart", new Object[]{accounts}, TargettedMessage.SEVERITY_INFO));
			}
		}

		return rv;
	}
	
	private void reset()
	{
		site = null;
		siteId = null;
		realm = null;
		roles.clear();
		officialAccountParticipant = null;
		officialAccountEidOnly = new Vector<String>();
		nonOfficialAccountParticipant = null;
		roleChoice = "sameRole";
		sameRoleChoice = null;
		emailNotiChoice = Boolean.FALSE.toString();
		userRoleEntries = new Vector<UserRoleEntry>();
	}

	public void setNotiProvider(UserNotificationProvider notiProvider) {
		this.notiProvider = notiProvider;
	}
	
	/**
	 * This is to update the handler's officialAccountParticipant attribute when encountering multiple users with same email address.
	 * The visual result is that the official account list will be expanded to include eids from all matches
	 * 
	 * @param officialAccount
	 * @param u
	 */
	protected void updateOfficialAccountParticipant(String officialAccount, User u, String eidsForAllMatches)
	{
		
		if (u != null && !eidsForAllMatches.contains(u.getEid()))
		{
			eidsForAllMatches = u.getEid() + "\n" + eidsForAllMatches;
		}
		
		// replace the original official account entry with eids from all matches.
		officialAccountParticipant = officialAccountParticipant.replaceAll(officialAccount, eidsForAllMatches);
	}
}

