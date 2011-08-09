/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.poll.logic.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.model.EmailTemplate;
import org.sakaiproject.emailtemplateservice.model.RenderedTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.PollRolePerms;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ExternalLogicImpl implements ExternalLogic {

	 private static Log log = LogFactory.getLog(ExternalLogicImpl.class);
	
	 private static final String
	 	/* Email template constants */
	 	EMAIL_TEMPLATE_NOTIFY_DELETED_OPTION = "polls.notifyDeletedOption",
	 	FILE_NOTIFY_DELETED_OPTION_TEMPLATE = "notifyDeletedOption.xml",
	 	
	 	/* Other constants */
	 	USER_ADMIN_ID = "admin",
	 	USER_ADMIN_EID = "admin";
	 
	private static final String USER_ENTITY_PREFIX = "/user/";
	
	/**
	 * Injected services
	 */
	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(
			DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}
	
	
    private AuthzGroupService authzGroupService;
    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }
	
    private EntityManager entityManager;
    public void setEntityManager(EntityManager em) {
        entityManager = em;
    }

    private EmailService emailService;
    public void setEmailService(EmailService emailService) {
    	this.emailService = emailService;
    }
    
    private EmailTemplateService emailTemplateService;
    public void setEmailTemplateService(EmailTemplateService emailTemplateService) {
    	this.emailTemplateService = emailTemplateService;
    }
    
    private EventTrackingService eventTrackingService;
    public void setEventTrackingService(EventTrackingService ets) {
        eventTrackingService = ets;
    }
    
    private FunctionManager functionManager;
    public void setFunctionManager(FunctionManager fm) {
        functionManager = fm;
    }
    
	private TimeService timeService;
	public void setTimeService(TimeService ts) {
		timeService = ts;
	}

    private SiteService siteService;
    public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}


    private SecurityService securityService;
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	private String fromEmailAddress;
	public void setFromEmailAddress(String fromEmailAddress) {
		this.fromEmailAddress = fromEmailAddress;
	}
	
	private String replyToEmailAddress;
	public void setReplyToEmailAddress(String replyToEmailAddress) {
		this.replyToEmailAddress = replyToEmailAddress;
	}
	
	/**
     * Methods
     */
	public String getCurrentLocationId() {
		return developerHelperService.getCurrentLocationId();
	}

	public boolean isUserAdmin(String userId) {
		return developerHelperService.isUserAdmin(USER_ENTITY_PREFIX + userId);
	}

	public boolean isUserAdmin() {
		return isUserAdmin(getCurrentUserId());
	}

	public String getCurrentUserId() {
		return developerHelperService.getCurrentUserId();
	}

	public String getCurrentuserReference() {
		return developerHelperService.getCurrentUserReference();
	} 

	public String getUserEidFromId(String userId) {
		try {
			return userDirectoryService.getUserEid(userId);
		} catch (UserNotDefinedException e) {
			log.debug("Looked up non-existant user id: "+userId, e);
		}
		
		return null;
	}
	
	public String getCurrentLocationReference() {
		log.debug("getCurrentLocationReference");
        return developerHelperService.getCurrentLocationReference();
	}

	public boolean isAllowedInLocation(String permission, String locationReference, String userReference) {
		log.debug("isAllowed in location( " + permission + " , " + locationReference + " , " + userReference);
		return developerHelperService.isUserAllowedInEntityReference(userReference, permission, locationReference);
	}

	public boolean isAllowedInLocation(String permission,
			String locationReference) {
		log.debug("isAllowed in location( " + permission + " , " + locationReference);
		return isAllowedInLocation(permission, locationReference, developerHelperService.getCurrentUserReference());
	}

    private static final String SAKAI_SITE_TYPE = SiteService.SITE_SUBTYPE;
   
    public void init() {
    	log.info("init()");
    	
    	try {
    		//Load the "notify deleted option" template
			loadMailTemplate(EMAIL_TEMPLATE_NOTIFY_DELETED_OPTION, FILE_NOTIFY_DELETED_OPTION_TEMPLATE);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Could not load an XML parser.", e);
		} catch (IOException e) {
			throw new RuntimeException("Could not read from XML template.");
		} catch (InvalidEmailTemplateException e) {
			throw new RuntimeException("Could not parse email template: "+e.getKey()+" from "+e.getFileName(), e);
		}
    }
    
    public List<String> getSitesForUser(String userId, String permission) {
        log.debug("userId: " + userId + ", permission: " + permission);

        List<String> l = new ArrayList<String>();

        // get the groups from Sakai
        Set<String> authzGroupIds = 
           authzGroupService.getAuthzGroupsIsAllowed(userId, permission, null);
        Iterator<String> it = authzGroupIds.iterator();
        while (it.hasNext()) {
           String authzGroupId = it.next();
           Reference r = entityManager.newReference(authzGroupId);
           if (r.isKnownType()) {
              // check if this is a Sakai Site or Group
              if (r.getType().equals(SiteService.APPLICATION_ID)) {
                 String type = r.getSubType();
                 if (SAKAI_SITE_TYPE.equals(type)) {
                    // this is a Site
                    String siteId = r.getId();
                    l.add(siteId);
                 }
              }
           }
        }

        if (l.isEmpty()) log.info("Empty list of siteIds for user:" + userId + ", permission: " + permission);
        return l;
     }


	public void postEvent(String eventId, String reference, boolean modify) {
		 eventTrackingService.post(eventTrackingService.newEvent(eventId, reference, modify));
		
	}

	public void registerFunction(String function) {
		functionManager.registerFunction(function);
		
	}

	public TimeZone getLocalTimeZone() {
		return timeService.getLocalTimeZone();
	}


	public List<String> getRoleIdsInRealm(String realmId) {
		AuthzGroup group;
		
		try {
			group = authzGroupService.getAuthzGroup(realmId);
			List<String> ret = new ArrayList<String>();
			Set<Role> roles = group.getRoles();
			Iterator<Role> i = roles.iterator();
			while (i.hasNext()) {
				Role role = (Role)i.next();
				ret.add(role.getId());
			}
			return ret;
		} catch (GroupNotDefinedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		return null;
	}


	public boolean isRoleAllowedInRealm(String roleId, String realmId, String permission) {
		try {
			AuthzGroup group = authzGroupService.getAuthzGroup(realmId);
			Role role = group.getRole(roleId);
			return  role.isAllowed(permission);
		} catch (GroupNotDefinedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}


	public String getSiteTile(String siteId) {
		Site site;
		
		try {
			site = siteService.getSite(siteId);
			return site.getTitle();
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return null;
	}

	public void setToolPermissions(Map<String, PollRolePerms> permMap,
			String locationReference) throws SecurityException, IllegalArgumentException {
		
		AuthzGroup authz = null;
		try {
			 authz = authzGroupService.getAuthzGroup(locationReference);
		}
		catch (GroupNotDefinedException e) {
			
			throw new IllegalArgumentException(e);
			
		}
		Set<Entry<String, PollRolePerms>> entrySet = permMap.entrySet(); 
		for (Iterator<Entry<String, PollRolePerms>> i = entrySet.iterator(); i.hasNext();)
		{	
			Entry<String, PollRolePerms> entry = i.next(); 
			String key = entry.getKey();
			Role role = authz.getRole(key);
			//try {
			  PollRolePerms rp = (PollRolePerms) entry.getValue();
			  if (rp.add != null )
				  setFunc(role,PollListManager.PERMISSION_ADD,rp.add);
			  if (rp.deleteAny != null )
				  setFunc(role,PollListManager.PERMISSION_DELETE_ANY, rp.deleteAny);
			  if (rp.deleteOwn != null )
				  setFunc(role,PollListManager.PERMISSION_DELETE_OWN,rp.deleteOwn);
			  if (rp.editAny != null )
				  setFunc(role,PollListManager.PERMISSION_EDIT_ANY,rp.editAny);
			  if (rp.editOwn != null )
				  setFunc(role,PollListManager.PERMISSION_EDIT_OWN,rp.editOwn);
			  if (rp.vote != null )
				  setFunc(role,PollListManager.PERMISSION_VOTE,rp.vote);
			  
			  log.info(" Key: " + key + " Vote: " + rp.vote + " New: " + rp.add );
			/*}
			  catch(Exception e)
			{
			log.error(" ClassCast Ex PermKey: " + key);
				e.printStackTrace();
				return "error";
			}*/
		}
		try {
			authzGroupService.save(authz);
		}
		catch (GroupNotDefinedException e) {
			throw new IllegalArgumentException(e);
		}
		catch (AuthzPermissionException e) {
			throw new SecurityException(e);
		}
		
	}

	
	public Map<String, PollRolePerms> getRoles(String locationReference)
	{
		log.debug("Getting permRoles");
		Map<String, PollRolePerms>  perms = new HashMap<String, PollRolePerms>();
		try {
			AuthzGroup group = authzGroupService.getAuthzGroup(locationReference);
			Set<Role> roles = group.getRoles();
			Iterator<Role> i = roles.iterator();
			
			while (i.hasNext())
			{
				Role role = (Role)i.next();
				String name = role.getId();
				log.debug("Adding element for " + name); 
				perms.put(name, new PollRolePerms(name, 
						role.isAllowed(PollListManager.PERMISSION_VOTE),
						role.isAllowed(PollListManager.PERMISSION_ADD),
						role.isAllowed(PollListManager.PERMISSION_DELETE_OWN),
						role.isAllowed(PollListManager.PERMISSION_DELETE_ANY),
						role.isAllowed(PollListManager.PERMISSION_EDIT_OWN),
						role.isAllowed(PollListManager.PERMISSION_EDIT_ANY)
						));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return perms;
	}

	
	private void setFunc(Role role, String function, Boolean allow)
	{
		
			//m_log.debug("Setting " + function + " to " + allow.toString() + " for " + rolename + " in /site/" + ToolManager.getCurrentPlacement().getContext());
			if (allow.booleanValue())
				role.allowFunction(function);
			else
				role.disallowFunction(function);
			
	}

	public String getSiteRefFromId(String siteId) {
		return siteService.siteReference(siteId);
	}



	public boolean userIsViewingAsRole() {
		String effectiveRole = securityService.getUserEffectiveRole(developerHelperService.getCurrentLocationReference());
		if (effectiveRole != null)
					return true;
		
		return false;
	}

	public void notifyDeletedOption(List<String> userEids, String siteTitle, String pollQuestion) {
		if (siteTitle == null)
			throw new IllegalArgumentException("Site title cannot be null");
		else if (pollQuestion == null)
			throw new IllegalArgumentException("Poll Question cannot be null");
		
		Map<String, String> replacementValues = new HashMap<String, String>();

		String from = (fromEmailAddress == null || fromEmailAddress.equals("")) ?
					serverConfigurationService.getString("smtpFrom@org.sakaiproject.email.api.EmailService") : fromEmailAddress;
					
		for (String userEid : userEids) {
			User user = null;
			try {
				user = userDirectoryService.getUserByEid(userEid);
				replacementValues.put("localSakaiName",
						developerHelperService.getConfigurationSetting("ui.service", "Sakai"));
				replacementValues.put("recipientFirstName",user.getFirstName());
				replacementValues.put("pollQuestion", pollQuestion);
				replacementValues.put("siteTitle", siteTitle); 

				RenderedTemplate template = emailTemplateService.getRenderedTemplateForUser(EMAIL_TEMPLATE_NOTIFY_DELETED_OPTION,
						user.getReference(), replacementValues);
				
				if (template == null)
					return;
					
				String
					content = template.getRenderedMessage(),
					subject = template.getRenderedSubject();
				
				emailService.send(from, user.getEmail(), subject, content, user.getEmail(), from,
						null);
			} catch (UserNotDefinedException e) {
				log.warn("Attempted to send email to unknown user (eid): '"+userEid+"'", e);
			}
		}
	}
	
	/**
	 * Load the mail template described by the XML in file 'fileName' into the emailTemplateService,
	 * identified by 'key'
	 * 
	 * @param key
	 * 	The key that identifies the template
	 * @param fileName
	 * 	The filename that holds the template information
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws InvalidEmailTemplateException
	 * 	Thrown if the email template is not a valid format.
	 */
	private void loadMailTemplate(String key, String fileName) throws ParserConfigurationException,
			IOException, InvalidEmailTemplateException {
		Session session = null;
		try {
			session = sessionManager.getCurrentSession();
			session.setUserId(USER_ADMIN_ID);
			session.setUserEid(USER_ADMIN_EID);
			
			try {
				NodeList templates = getEmailTemplates(FILE_NOTIFY_DELETED_OPTION_TEMPLATE);
				int n = templates.getLength();
				for (int i = 0; i < n; i++) {
					xmlToTemplate((Element)templates.item(i), key);
				}
			} catch (SAXException e) {
				throw new InvalidEmailTemplateException(key, fileName, e);
			} catch (InvalidEmailTemplateException e) {
				//'e' doesn't have all the information to throw here.
				throw new InvalidEmailTemplateException(key, fileName);
			}
		} finally {
			if (session != null) {
				session.setUserId(null);
				session.setUserEid(null);
			}
		}
	}
	
	/**
	 * Given the XML template node, load it into the Email Template Service as a template
	 * identified by 'key'
	 * 
	 * @param xmlTemplate
	 * 	The valid XML template to load
	 * @param key
	 * 	The key that should identify the template
	 */
	private void xmlToTemplate(Element xmlTemplate, String key) {
		String
			subject = getTagValue(xmlTemplate, "subject", ""),
			body = getTagValue(xmlTemplate, "message", ""),
			locale = getTagValue(xmlTemplate, "locale", ""),
			versionString = getTagValue(xmlTemplate, "version", "");
	
		
		if (!emailTemplateService.templateExists(key, new Locale(locale)))
		{
			EmailTemplate template = new EmailTemplate();
			template.setSubject(subject);
			template.setMessage(body);
			template.setLocale(locale);
			template.setKey(key);
			template.setVersion(Integer.valueOf(1));
			template.setOwner(USER_ADMIN_ID);
			template.setLastModified(new Date());
			this.emailTemplateService.saveTemplate(template);
			log.debug("Added email template: '"+key+"'");
		}
		else
		{
			EmailTemplate existingTemplate = this.emailTemplateService.getEmailTemplate(key, new Locale(locale));
			String oVersionString = existingTemplate.getVersion() != null ? existingTemplate.getVersion().toString():null;
			if ((oVersionString == null && versionString != null) || (oVersionString != null && versionString != null && !oVersionString.equals(versionString)))
			{
				Integer version = (versionString != null && !versionString.equals("")) ? Integer.valueOf(versionString) : Integer.valueOf(0);
				existingTemplate.setSubject(subject);
				existingTemplate.setMessage(body);
				existingTemplate.setLocale(locale);
				existingTemplate.setKey(key);
				existingTemplate.setVersion(version);
				existingTemplate.setOwner(USER_ADMIN_ID);
				existingTemplate.setLastModified(new Date());
				this.emailTemplateService.updateTemplate(existingTemplate);
				log.debug("Updated email template: '"+key+"' to version "+version);
			}
		}
			
	}

	/**
	 * Load a list of all the XML DOM elements that represent 'emailTemplate's.
	 * 
	 * @param file
	 * 	The file to parse
	 * @return
	 * 	A list of nodes (NodeList) that represent all the email templates within the file
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws InvalidEmailTemplateException
	 * 	Thrown if the XML template is not a valid email template definition
	 * 
	 * TODO: Validate the XML email templates against a DTD to validate correctness
	 */
	private NodeList getEmailTemplates(String file) throws SAXException, IOException,
			ParserConfigurationException, InvalidEmailTemplateException {
		InputStream in = getClass().getClassLoader().getResourceAsStream(file);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(in);
		
		Element emailTemplates = doc.getDocumentElement();
		if ("emailTemplates".equals(emailTemplates.getNodeName())) {
			return emailTemplates.getElementsByTagName("emailTemplate");
		} else {
			throw new InvalidEmailTemplateException(null, file);
		}
	}
	
	/**
	 * Convenience method to get the value of a particular tag within an XML element.
	 * @param e
	 * @param tagName
	 * @return
	 */
	private String getTagValue(Element e, String tagName, String failover) {
		String value = failover;
		NodeList l = e.getElementsByTagName(tagName);
		if (l != null && l.getLength() > 0) {
			Element tag = (Element) l.item(0);
			if (tag != null) {
				Node n = tag.getFirstChild();
				if (n != null) {
					value = n.getNodeValue();
				}		
			}
		}
		return value;
	}

	public ToolSession getCurrentToolSession() {
		return sessionManager.getCurrentToolSession();
	}
	
	public boolean isResultsChartEnabled() {
		return serverConfigurationService.getBoolean("poll.results.chart.enabled", false);
	}
	
	
	public boolean isMobileBrowser() {
		Session session = sessionManager.getCurrentSession();
		if (session.getAttribute("is_wireless_device") != null && ((Boolean) session.getAttribute("is_wireless_device")).booleanValue()) {
			return true;
		}
		return false;
		
	}
	
	
	public List<String> getPermissionKeys() {
		
		String[] perms = new String[]{
				"poll.vote",
			    "poll.add",
			    "poll.deleteown",
			    "poll.deleteAny",
			    "poll.editAny",
			    "poll.editOwn",
		}; 
		List<String> ret = Arrays.asList(perms);
		return ret;
	}
	
}
