/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.archive.impl;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.archive.api.ArchiveService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
* <p>...</p>
*/
public class BasicArchiveService
	implements ArchiveService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(BasicArchiveService.class);

	/** A full path and file name to the storage file. */
	protected String m_storagePath = "/";
	
	protected final static HashMap userIdTrans = new HashMap();
	
	protected HashSet usersListAllowImport = new HashSet(); 

	// only the resources created by the followinng roles will be imported
	// role sets are different to different system
	public String[] SAKAI_roles = {"Affiliate", "Assistant", "Instructor", "Maintain", "Organizer", "Owner"};
	public String[] CT_roles = {"Affiliate", "Assistant", "Instructor", "Maintain", "Organizer", "Owner"};
	public String[] WT_roles = {};
	
	// tool id updates
	private String old_toolId_prefix = "chef.";
	private String new_toolId_prefix = "sakai.";
	private String[] old_toolIds = {"sakai.noti.prefs", "sakai.presence", "sakai.siteinfogeneric", "sakai.sitesetupgeneric", "sakai.threadeddiscussion"};
	private String[] new_toolIds = {"sakai.preferences", "sakai.online", "sakai.siteinfo", "sakai.sitesetup", "sakai.discussion"};
	
	// only these Sakai tools will be imported
	public String[] sakaiServicesToImport = 
		{"AnnouncementService", 
		"AssignmentService", 
		"ContentHostingService", 
		"CalendarService",
		"DiscussionService",
		"MailArchiveService",
		"SyllabusService",
		"RWikiObjectService",
		"DiscussionForumService",
		"WebService"
		};
		
	public String[] CT_tools_toImport = {};
	public String[] WT_tools_toImport = {};
	
	//TODO: the draft flag settings for WT and CT
	
	public HashMap tool_draft_flag = new HashMap();

	/*******************************************************************************
	* Dependencies and their setter methods
	*******************************************************************************/

	/** Dependency: ServerConfigurationService. */
	protected ServerConfigurationService m_serverConfigurationService = null;

	/**
	 * Dependency: ServerConfigurationService.
	 * @param service The ServerConfigurationService.
	 */
	public void setServerConfigurationService(ServerConfigurationService service)
	{
		m_serverConfigurationService = service;
	}

	/**
	 * Configuration: Set the Storage Path.
	 * @param path The storage path.
	 */
	public void setStoragePath(String path)
	{
		m_storagePath = path;
	}

	/** Dependency: EntityManager. */
	protected EntityManager m_entityManager = null;

	private ContentHostingService contentHostingService;

	/**
	 * Dependency: EntityManager.
	 * 
	 * @param service
	 *        The EntityManager.
	 */
	public void setEntityManager(EntityManager service)
	{
		m_entityManager = service;
	}

	/*******************************************************************************
	* Init and Destroy
	*******************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		contentHostingService = (ContentHostingService) ComponentManager.get(ContentHostingService.class.getName());
		if ((m_storagePath != null) && (!m_storagePath.endsWith("/")))
		{
			m_storagePath = m_storagePath + "/";
		}

		M_log.info("init(): storage path: " + m_storagePath);
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/*******************************************************************************
	* ArchiveService implementation
	*******************************************************************************/

	/**
	* Create an archive for the resources of a site.
	* @param siteId The id of the site to archive.
	* @return A log of messages from the archive.
	*/
	public String archive(String siteId)
	{
		StringBuilder results = new StringBuilder();

		if (M_log.isDebugEnabled())
			M_log.debug("archive(): site: " + siteId);

		Site theSite = null;
		try
		{
			theSite = SiteService.getSite(siteId);
		}
		catch (IdUnusedException e)
		{
			results.append("Site: " + siteId + " not found.\n");
			M_log.warn("archive(): site not found: " + siteId);
			return results.toString();
		}

		// collect all the attachments we need
		List attachments = m_entityManager.newReferenceList();

		Time now = TimeService.newTime();

		// this is the folder we are writing files to
		String storagePath = m_storagePath + siteId + "-archive/";

		// create the directory for the archive
		File dir = new File(m_storagePath + siteId + "-archive/");
		dir.mkdirs();

		// for each registered ResourceService, give it a chance to archve
		List services = m_entityManager.getEntityProducers();
		for (Iterator iServices = services.iterator(); iServices.hasNext();)
		{
			EntityProducer service = (EntityProducer) iServices.next();
			if (service == null) continue;
			if (!service.willArchiveMerge()) continue;

			Document doc = Xml.createDocument();
			Stack stack = new Stack();
			Element root = doc.createElement("archive");
			doc.appendChild(root);
			root.setAttribute("source", siteId);
			root.setAttribute("server", m_serverConfigurationService.getServerId());
			root.setAttribute("date", now.toString());
			root.setAttribute("system", FROM_SAKAI_2_8);
			
			stack.push(root);

			try
			{
				String msg = service.archive(siteId, doc, stack, storagePath, attachments);
				results.append(msg);
			}
			catch (Throwable t)
			{
				results.append(t.toString() + "\n");
			}

			stack.pop();
			
			String fileName = storagePath + service.getLabel() + ".xml";
			Xml.writeDocument(doc, fileName);
		}

		// archive the collected attachments
		if (attachments.size() > 0)
		{
			Document doc = Xml.createDocument();
			Stack stack = new Stack();
			Element root = doc.createElement("archive");
			doc.appendChild(root);
			root.setAttribute("source", siteId);
			root.setAttribute("server", m_serverConfigurationService.getServerId());
			root.setAttribute("date", now.toString());
			root.setAttribute("system", FROM_SAKAI_2_8);
			
			stack.push(root);

			String msg = contentHostingService.archiveResources(attachments, doc, stack, storagePath);
			results.append(msg);

			stack.pop();

			String fileName = storagePath + "attachment.xml";
			Xml.writeDocument(doc, fileName);
		}

		// *** Site

		
		Document doc = Xml.createDocument();
		Stack stack = new Stack();
		Element root = doc.createElement("archive");
		doc.appendChild(root);
		root.setAttribute("site", siteId);
		root.setAttribute("date", now.toString());
		root.setAttribute("system", FROM_SAKAI_2_8);
		
		stack.push(root);

		String msg = archiveSite(theSite, doc, stack);
		results.append(msg);
		
		stack.pop();
		Xml.writeDocument(doc, m_storagePath + siteId + "-archive/site.xml");


		// *** Users
		doc = Xml.createDocument();
		stack = new Stack();
		root = doc.createElement("archive");
		doc.appendChild(root);
		root.setAttribute("site", siteId);
		root.setAttribute("date", now.toString());
		root.setAttribute("system", FROM_SAKAI_2_8);
		
		stack.push(root);
		
		msg = archiveUsers(theSite, doc, stack);
		results.append(msg);

		stack.pop();
		Xml.writeDocument(doc, m_storagePath + siteId + "-archive/user.xml");


		return results.toString();

	}	// archive


	/**
	* Archive the site definition.
	* @param site the site.
	* @param doc The document to contain the xml.
	* @param stack The stack of elements, the top of which will be the containing
	* element of the "site" element.
	*/
	
	protected static String archiveSite(Site site, Document doc, Stack stack)
	{
		Element element = doc.createElement(SiteService.APPLICATION_ID);
		((Element)stack.peek()).appendChild(element);
		stack.push(element);
		
		Element siteNode = site.toXml(doc, stack);
		stack.push(siteNode);	
		
		// to add the realm node with user list into site
		List roles = new Vector();
		String realmId = "/site/" + site.getId();
		try
		{
			Role role = null;
			AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
			
			Element realmNode = doc.createElement("roles");
			((Element)stack.peek()).appendChild(realmNode);
			stack.push(realmNode);
			
			roles.addAll(realm.getRoles());
			
			for (int i = 0; i< roles.size(); i++)
			{
				role = (Role) roles.get(i);
				String roleId = role.getId();
				Element node = doc.createElement(roleId);
				realmNode.appendChild(node);
				
				List users = new Vector();
				users.addAll(realm.getUsersHasRole(role.getId()));
				for (int j = 0; j < users.size(); j++)
				{			
					Element abilityNode = doc.createElement("ability");
					abilityNode.setAttribute("roleId", roleId);
					abilityNode.setAttribute("userId", ((String)users.get(j)));
					node.appendChild(abilityNode);
				}
			}
		}
		catch(Exception any)
		{
			// M_log.warn("archve: exception archiving site: "+ site.getId() + ": ", any);
		}
	
		stack.pop();
		
		return "archiving Site: " + site.getId() + "\n";
	
	}	// archiveSite

	/**
	* Archive the users defined in this site (internal users only).
	* @param site the site.
	* @param doc The document to contain the xml.
	* @param stack The stack of elements, the top of which will be the containing
	* element of the "site" element.
	*/
	protected static String archiveUsers(Site site, Document doc, Stack stack)
	{
		Element element = doc.createElement(UserDirectoryService.APPLICATION_ID);
		((Element)stack.peek()).appendChild(element);
		stack.push(element);
	
		try
		{
			// get the site's user list
			List users = new Vector();
			String realmId = "/site/" + site.getId();
			try
			{
				AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
				users.addAll(UserDirectoryService.getUsers(realm.getUsers()));
				Collections.sort(users);
				for (int i = 0; i < users.size(); i++)
				{
					User user = (User) users.get(i);
					user.toXml(doc, stack);
				}
			}
			catch (GroupNotDefinedException e)
			{
				//Log.warn("chef", "SiteAction.updateParticipantList  IdUnusedException " + realmId);
			}
			catch (Exception any) {}
	
		}
		catch (Exception any)
		{
			//.M_log.warn("archve: exception archiving users: "
					//	+ site.getId() + ": ", any);
		}
	
		stack.pop();
		
		return "archiving the users for Site: " + site.getId() + "\n";
	
	
	}	// archiveUsers


	/**
	* Process a merge for the file, or if it's a directory, for all contained files (one level deep).
	* @param fileName The site name (for the archive file) to read from.
	* @param mergeId The id string to use to make ids in the merge consistent and unique.
	* @param creatorId The creator id
	* If null or blank, the date/time string of the merge is used.
	*/
	public String merge(String fileName, String siteId, String creatorId)
	{
		StringBuilder results = new StringBuilder();

		File[] files = null;

		// see if the name is a directory
		File file = new File(m_storagePath + fileName);
		if ((file == null) || (!file.exists()))
		{
			results.append("file: " + file.getPath() + " not found.\n");
			M_log.warn("merge(): file not found: " + file.getPath());
			return results.toString();
		}

		if (file.isDirectory())
		{
			files = file.listFiles();
		}
		else
		{
			files = new File[1];
			files[0] = file;
		}

		// track old to new attachment names
		Map attachmentNames = new HashMap();		
		
		// firstly, merge the users
		for (int i = 0; i < files.length; i++)
		{
			if ((files[i] != null) && (files[i].getPath().indexOf("user.xml") != -1))
			{
				processMerge(files[i].getPath(), siteId, results, attachmentNames, null);
				files[i] = null;
				break;
			}
		}
		
		// see if there's a site definition
		for (int i = 0; i < files.length; i++)
		{
			if ((files[i] != null) && (files[i].getPath().indexOf("site.xml") != -1))
			{
				processMerge(files[i].getPath(), siteId, results, attachmentNames, creatorId);
				files[i] = null;
				break;
			}
		}

		// see if there's an attachments definition
		for (int i = 0; i < files.length; i++)
		{
			if ((files[i] != null) && (files[i].getPath().indexOf("attachment.xml") != -1))
			{
				processMerge(files[i].getPath(), siteId, results, attachmentNames, null);
				files[i] = null;
				break;
			}
		}

		// process each remaining file that is an .xml file
		for (int i = 0; i < files.length; i++)
		{
			if (files[i] != null)
				if (files[i].getPath().endsWith(".xml"))
					processMerge(files[i].getPath(), siteId, results, attachmentNames, null);
		}

		return results.toString();

	}	// merge

	/**
	 * When Sakai is importing an item archived by Sakai, check the creator's role first.
	 * The item is imported when the role is in the acceptance list,
	 * @param siteId
	 * @param userId
	 * @return boolean value - true: the role is accepted for importing; otherwise, not;
	 */
	public boolean checkSakaiRole(String siteId, String userId)
	{
		// Check - In sakai, if this tool accept this role during importing
		// currently, all the tools allowed to be imported, are using the same role set
		try
		{
			AuthzGroup realm = AuthzGroupService.getAuthzGroup(siteId);
			
			// get the role of the user as this realm
			Role role = realm.getRole(userId);
			
			for (int i = 0; i <SAKAI_roles.length; i++)
			{
				if (!SAKAI_roles[i].equalsIgnoreCase(role.getId())) continue;
				return true;
			}
		}
		catch (GroupNotDefinedException e) 
		{
		}
		
		return false;
		
	}//checkSakaiRole
	
	/**
	 * When Sakai is importing a role in site.xml, check if it is a qualified role.
	 * @param roleId
	 * @return boolean value - true: the role is accepted for importing; otherwise, not;
	 */
	protected boolean checkSystemRole(String system, String roleId)
	{
		if (system.equalsIgnoreCase(FROM_CT))
		{
			// Check - if CT classic accepts the resource made by this role during importing
			for (int i = 0; i <CT_roles.length; i++)
			{
				if (!CT_roles[i].equalsIgnoreCase(roleId)) continue;
				return true;
			}
		}
		else if (system.equalsIgnoreCase(FROM_SAKAI) || system.equalsIgnoreCase(FROM_SAKAI_2_8))
		{
			// Check - if CTools accepts the resource made by this role during importing
			for (int i = 0; i <SAKAI_roles.length; i++)
			{
				if (!SAKAI_roles[i].equalsIgnoreCase(roleId)) continue;
				return true;
			}
		}
		
		return false;
		
	}//checkSystemRole
	
	/*
	 * 
	 */
	 protected boolean checkSakaiService (String serviceName)
	 {
	 	for (int i = 0; i < sakaiServicesToImport.length; i ++)
	 	{
	 		if (serviceName.endsWith(sakaiServicesToImport[i]))
	 		{
	 			return true;
	 		}
	 	}
	 	return false;
	 }
	 
	
	/**
	 * When importing an item, check if it needs to be set as a draft
	 * @param system The system by which this file was archived
	 * @param toolService The service name of the tool calling this function
	 * @return boolean value - true: draft requested; false: as it is.
	 */
	/*
	public boolean checkToolDraftFlag(String system, String toolService)
	{
		if (system.equalsIgnoreCase(FROM_SAKAI))
		{
			if (toolService.equalsIgnoreCase(AnnouncementService.SERVICE_NAME))
			{
				return SAKAI_annc_draft_import;
			}
			else if (toolService.equalsIgnoreCase(AssignmentService.SERVICE_NAME))
			{
				return SAKAI_assign_draft_import;
			}
			else if (toolService.equalsIgnoreCase(ContentHostingService.SERVICE_NAME))
			{
				return SAKAI_rsc_draft_import;
			}
			else if (toolService.equalsIgnoreCase(CalendarService.SERVICE_NAME))
			{
				return SAKAI_schedule_draft_import;
			}
			else if (toolService.equalsIgnoreCase(DiscussionService.SERVICE_NAME))
			{
				return SAKAI_disc_draft_import;
			}
			else
				return false;
		}
		else
		{
			// TODO: WT or CT may use different rules
			return false;
		}
		
	} //checkToolDraftFlag
	*/

	/**
	* Read in an archive file and merge the entries into the specified site.
	* @param fileName The site name (for the archive file) to read from.
	* @param siteId The id of the site to merge the content into.
	* @param results A buffer to accumulate result messages.
	* @param attachmentNames A map of old to new attachment names.
	* @param useIdTrans A map of old WorkTools id to new Ctools id
	* @param creatorId The creator id
	*/
	protected void processMerge(String fileName, String siteId, StringBuilder results, Map attachmentNames, String creatorId)
	{
		// correct for windows backslashes
		fileName = fileName.replace('\\', '/');

		if (M_log.isDebugEnabled())
			M_log.debug("merge(): processing file: " + fileName);

		Site theSite = null;
		try
		{
			theSite = SiteService.getSite(siteId);
		}
		catch (IdUnusedException ignore) {}

		// read the whole file into a DOM
		Document doc = Xml.readDocument(fileName);
		if (doc == null)
		{
			results.append("Error reading xml from: " + fileName + "\n");
			return;
		}

		// verify the root element
		Element root = doc.getDocumentElement();
		if (!root.getTagName().equals("archive"))
		{
			results.append("File: " + fileName + " does not contain archive xml.  Found this root tag: " + root.getTagName() + "\n");
			return;
		}

		// get the from site id
		String fromSite = root.getAttribute("source");
		String system = root.getAttribute("system");

		// the children
		NodeList children = root.getChildNodes();
		final int length = children.getLength();
		for(int i = 0; i < length; i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element = (Element)child;

			// look for site stuff
			if (element.getTagName().equals(SiteService.APPLICATION_ID))
			{	
				//if the xml file is from WT site, merge it with the translated user ids
				if (system.equalsIgnoreCase(FROM_WT))
					mergeSite(siteId, fromSite, element, userIdTrans, creatorId);
				else
					mergeSite(siteId, fromSite, element, new HashMap()/*empty userIdMap */, creatorId);
			}
			else if (element.getTagName().equals(UserDirectoryService.APPLICATION_ID))
			{
				try
				{
					// merge the users in only when they are from WorkTools
					if (system.equalsIgnoreCase(FROM_WT))
					{
						String msg = mergeUsers(element, userIdTrans);
						results.append(msg);
					}
				}
				catch (Exception any)
				{
				}
			}

			else
			{
				// we need a site now
				if (theSite == null)
				{
					results.append("Site: " + siteId + " not found.\n");
					return;
				}
				
				// get the service name
				String serviceName = translateServiceName(element.getTagName());

				// get the service
				try
				{
					EntityProducer service = (EntityProducer) ComponentManager.get(serviceName);
					
					try
					{
						String msg = "";
						// if the xml file is from WT site, merge it with user id translation
						if (system.equalsIgnoreCase(FROM_WT))
							msg = service.merge(siteId, element, fileName, fromSite, attachmentNames, userIdTrans, new HashSet());
						else if ((system.equalsIgnoreCase(FROM_SAKAI) || system.equalsIgnoreCase(FROM_SAKAI_2_8))
                                 && (checkSakaiService(serviceName)))
							msg = service.merge(siteId, element, fileName, fromSite, attachmentNames, new HashMap() /* empty userIdTran map */, usersListAllowImport);
						else if (system.equalsIgnoreCase(FROM_CT))
							msg = service.merge(siteId, element, fileName, fromSite, attachmentNames, new HashMap() /* empty userIdTran map */, usersListAllowImport);
							
						results.append(msg);
					}
					catch (Throwable t)
					{
						results.append("Error merging: " + serviceName + " in file: " + fileName + " : " + t.toString() + "\n");
					}
				}
				catch (Throwable t)
				{
					results.append("Did not recognize the resource service: " + serviceName + " in file: " + fileName + "\n");
				}
			}
		}

	}	// processMerge
	
	/**
	* Check security permission.
	* @param lock The lock id string.
	* @param reference The resource's reference string, or null if no resource is involved.
	* @exception PermissionException thrown if the user does not have access
	*/
	protected void unlock(String lock, String reference) throws PermissionException
	{
		if (!SecurityService.unlock(lock, reference))
		{
			// needs to bring back: where is sessionService
			// throw new PermissionException(UsageSessionService.getSessionUserId(), lock, reference);
		}
	} // unlock
	
	
	/**
	* Merge the site info like description from the site part of the archive file into the site service.
	* @param element The XML DOM tree of messages to merge.
	* @param siteId The id of the site getting imported into.
	*/
	protected void mergeSiteInfo(Element el, String siteId)
		throws IdInvalidException, IdUsedException, PermissionException, IdUnusedException, InUseException 
	{
		// heck security (throws if not permitted)
		unlock(SiteService.SECURE_UPDATE_SITE, SiteService.siteReference(siteId));
	
		Site edit = SiteService.getSite(siteId);
		String desc = el.getAttribute("description-enc");
			
		try
		{
		byte[] decoded = Base64.decodeBase64(desc.getBytes("UTF-8"));
		byte[] filteredDecoded = decoded;
		for(int i=0; i<decoded.length;i++)
		{
			byte b = decoded[i];
			if (b == (byte) -109 || b == (byte) -108)
			{
				// smart quotes, open/close double quote
				filteredDecoded[i] = (byte) 34;
			}
			else if (b == (byte) -111 || b == (byte) -110)
			{
				// smart quotes, open/close double quote
				filteredDecoded[i] = (byte) 39;
			}
			else if (b == (byte) -106)
			{
				// dash
				filteredDecoded[i] = (byte) 45;
			}
		}
		desc = new String(decoded, "UTF-8");
		}
		catch(Exception any)
		{
			M_log.warn("mergeSiteInfo(): exception caught");				
		}							
		//edit.setTitle(title);
		edit.setDescription(desc);
		
		SiteService.save(edit);
			 
		return;
		
	} // mergeSiteInfo
	
	/**
	* Merge the the permission-roles settings into the site
	* @param element The XML DOM tree of messages to merge.
	* @param siteId The id of the site getting imported into.
	*/
	protected void mergeSiteRoles(Element el, String siteId, HashMap useIdTrans) throws PermissionException
	{
		// heck security (throws if not permitted)
		unlock(SiteService.SECURE_UPDATE_SITE, SiteService.siteReference(siteId));
		
		String source = "";
		
		// el: <roles> node			
		Node parent0 = el.getParentNode(); // parent0: <site> node
		Node parent1 = parent0.getParentNode(); // parent1: <service> node
		Node parent = parent1.getParentNode(); // parent: <archive> node containing "system"
		
		if (parent.getNodeType() == Node.ELEMENT_NODE)
		{
			Element parentEl = (Element)parent;
			source = parentEl.getAttribute("system");
		}

		List roles = new Vector();
		// to add this user with this role inito this realm
		String realmId = "/site/" + siteId;
		try
		{
			//AuthzGroup realmEdit = AuthzGroupService.getRealm(realmId);
			AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
			
			//roles.addAll(realmEdit.getRoles());
			roles.addAll(realm.getRoles());

			NodeList children = el.getChildNodes();
			final int length = children.getLength();
			for(int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element element2 = (Element)child;
				
				if (source.equalsIgnoreCase(FROM_WT))
				{
					// from WT, merge the users with roles into the new site
					
					NodeList children2 = element2.getChildNodes();
					final int length2 = children2.getLength();
					for(int i2 = 0; i2 < length2; i2++)
					{
						Node child2 = children2.item(i2);
						if (child2.getNodeType() != Node.ELEMENT_NODE) continue;
						Element element3 = (Element)child2;
						if (!element3.getTagName().equals("ability")) continue;
	
						String userId = element3.getAttribute("userId");
						
						// under 2 conditions the userIdTrans would be empty
						// 1st is, even from WT, no user id has been processed
						// 2nd is, this user is not from WT. userIdTrans is always empty
						if (!userIdTrans.isEmpty())
						{
							// user the new id if the old one from WT has been replaced
							String newId = (String)useIdTrans.get(userId);
							if (newId != null)
								userId = newId;
						}
						try 
						{
							User user = UserDirectoryService.getUser(userId);
							String roleId = element3.getAttribute("roleId");
							//Role role = realmEdit.getRole(roleId);
							Role role = realm.getRole(roleId);
							if (role != null)
							{
								AuthzGroup realmEdit = AuthzGroupService.getAuthzGroup(realmId);
								realmEdit.addMember(user.getId(), role.getId(), true, false);
								AuthzGroupService.save(realmEdit);
							}
						} 
						catch (UserNotDefinedException e) {
							M_log.warn("UserNotDefined in mergeSiteRoles: " + userId);
						} catch (AuthzPermissionException e) {
							M_log.warn("AuthzPermissionException in mergeSiteRoles", e);
						}
					}
				}
				else 
				{
					// for both CT classic and Sakai CTools
					
					// check is this roleId is a qualified one
					if (!checkSystemRole(source, element2.getTagName())) continue;
					
					NodeList children2 = element2.getChildNodes();
					final int length2 = children2.getLength();
					for(int i2 = 0; i2 < length2; i2++)
					{
						Node child2 = children2.item(i2);
						if (child2.getNodeType() != Node.ELEMENT_NODE) continue;
						Element element3 = (Element)child2;
						if (!element3.getTagName().equals("ability")) continue;

						String userId = element3.getAttribute("userId");
					
						// this user has a qualified role, his/her resource will be imported
						usersListAllowImport.add(userId);
					}
				} // if - elseif - elseif
			} // for
		}
		catch(GroupNotDefinedException err)
		{
			M_log.warn("()mergeSiteRoles realm edit exception caught GroupNotDefinedException " + realmId);
		}
		return;
	
	} // mergeSiteRoles
	
	
	/**
	* Merge the user list into the the system.
	* Translate the id to the siteId.
	* @param element The XML DOM tree of messages to merge.
	*/

	protected String mergeUsers(Element element, HashMap useIdTrans) 
	throws IdInvalidException, IdUsedException, PermissionException
	{
		String msg = "";
		int count = 0;
		
		// The flag showing from WT
		boolean fromWT = false;
		boolean fromCTclassic = false;
		boolean fromCTools = false;
		String source = "";
					
		Node parent = element.getParentNode();
		if (parent.getNodeType() == Node.ELEMENT_NODE)
		{
			Element parentEl = (Element)parent;
			source = parentEl.getAttribute("system");
		}
		
		if (source != null)
		{
			if (source.equalsIgnoreCase(FROM_CT))
				fromCTclassic = true;
			else if (source.equalsIgnoreCase(FROM_WT))
				fromWT = true;
			else
				fromCTools = true;
		}
		else
			fromCTools = true;
		
		NodeList children = element.getChildNodes();
		final int length = children.getLength();
		for(int i = 0; i < length; i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element2 = (Element)child;
			if (!element2.getTagName().equals("user")) continue;

			//for WorkTools
			if (fromWT)
			{
				// Worktools use email address as Id
				String wtId = element2.getAttribute("id");	
				// check if this is an umich address
				if (wtId.endsWith("umich.edu"))
				{
					// if this id is a UM unique name
					// trim the first part of the email as the id
					String userId = wtId.substring(0, wtId.indexOf("@"));
					// change the id to be the first part of wtId
					element2.setAttribute("id", userId);
										
					// add the entry (wtEmail <-> userId) into map
					useIdTrans.put(wtId, userId);
										
					try
					{
						User user = UserDirectoryService.getUser(userId);
						// if this user id exists, do nothing.
					}
					catch(UserNotDefinedException e)
					{
						// this umich id does not exit, merging will create a new user with this id
						try
						{
							UserEdit userEdit = UserDirectoryService.mergeUser(element2);
							UserDirectoryService.commitEdit(userEdit);
							count++;
							// because it is an UM id, report it in the merge log.
							msg = msg.concat("The user id " + userId + "(" + wtId + ") doesn't exist, and was just created. \n");
						}
						catch(UserIdInvalidException error)
						{
							msg = msg.concat("This user with id -" + wtId + ", can't be merged because of the invalid email address.\n");
						}
						catch(UserAlreadyDefinedException error)
						{
						}
						catch(UserPermissionException error)
						{
						}
					}
					
				}
				else
				{
					// for non-UM email, process as a friend account id
					try
					{
						// test if it is a friend account id
						User user = UserDirectoryService.getUser(wtId);
						// if the user exists, do nothing.
					}
					catch(UserNotDefinedException e)
					{
						try
						{
							// if this isn't such a friend email address,
							// create a new friend account for it
							UserEdit userEdit = UserDirectoryService.mergeUser(element2);
							UserDirectoryService.commitEdit(userEdit);
							count++;
						}
						catch(UserIdInvalidException error)
						{
							msg = msg.concat("This user with id -" + wtId + ", can't be merged because of the invalid email address.\n");
						}
						catch(UserAlreadyDefinedException error)
						{
						}
						catch(UserPermissionException error)
						{
						}					
					}
					
				}
			}
			// not allowing merging users form CT classic or CTools.
			/*
			else if (fromCTclassic)
			{
				String ctId = element2.getAttribute("id");

				try
				{
					User user = UserDirectoryService.getUser(ctId);
				}
				catch(IdUnusedException e)
				{
					// if this umich id does not exit, report it in importing log.
					msg.concat("The user id " + ctId + " doesn't exist, and was just created. \n");
				}
					
				try
				{
					// override the old one or create a new user with this id
					UserEdit userEdit = UserDirectoryService.mergeUser(element2);
					UserDirectoryService.commitEdit(userEdit);
					count++;
				}
				catch(IdInvalidException error)
				{
					msg.concat("This user with id -" + ctId + ", can't be merged because of the invalid email address.\n");
				}
				catch(IdUsedException error)
				{
				}
				catch(PermissionException error)
				{
				}
								
			}
			else if (fromCTools)
			{
				try
				{
					UserEdit userEdit = UserDirectoryService.mergeUser(element2);
					UserDirectoryService.commitEdit(userEdit);
					count++;
				}
				catch(IdInvalidException error)
				{
					msg.concat("This user with id -" + element2.getAttribute("id") + ", can't be merged because of the invalid email address.\n");
				}
				catch(IdUsedException error)
				{
				}
				catch(PermissionException error)
				{
				}
			}
			*/
		}

		msg = msg + "merging user" + "(" + count +") users\n";
		return msg;
		
	} // mergeUsers
		
	/**
	* Merge the site definition from the site part of the archive file into the site service.
	* Translate the id to the siteId.
	* @param siteId The id of the site getting imported into.
	* @param fromSiteId The id of the site the archive was made from.
	* @param element The XML DOM tree of messages to merge.
	* @param creatorId The creator id
	*/
	
	protected void mergeSite(String siteId, String fromSiteId, Element element, HashMap useIdTrans, String creatorId)
	{		
		NodeList children = element.getChildNodes();
		final int length = children.getLength();
		for(int i = 0; i < length; i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element2 = (Element)child;
			if (!element2.getTagName().equals("site")) continue;
			
			NodeList toolChildren = element2.getElementsByTagName("tool");
			final int tLength = toolChildren.getLength();
			for(int i2 = 0; i2 < tLength; i2++)
			{
				Element element3 = (Element) toolChildren.item(i2);
				String toolId = element3.getAttribute("toolId");
				if (toolId != null)
				{
					toolId = toolId.replaceAll(old_toolId_prefix, new_toolId_prefix);
					for (int j = 0; j < old_toolIds.length; j++)
					{
						toolId = toolId.replaceAll(old_toolIds[i], new_toolIds[i]);
					}
				}
				element3.setAttribute("toolId", toolId);
			}
				
			// merge the site info first
			try
			{
				SiteService.merge(siteId, element2, creatorId);
				mergeSiteInfo(element2, siteId);
			}
			catch(Exception any)
			{
			}
			
			Site site = null;
			try
			{
				site = SiteService.getSite(siteId);
			}
			catch (IdUnusedException e) 
			{
				M_log.warn(this + "The site with id " + siteId + " doesn't exit");
				return;
			}
		
			if (site != null)
			{
				NodeList children2 = element2.getChildNodes();
				final int length2 = children2.getLength();
				for(int i2 = 0; i2 < length2; i2++)
				{
					Node child2 = children2.item(i2);
					if (child2.getNodeType() != Node.ELEMENT_NODE) continue;
					Element element3 = (Element)child2;
					if (!element3.getTagName().equals("roles")) continue;
	
					// only merge roles when from Worktools
					//if (source.equalsIgnoreCase(FROM_WT))
					//{
						// merge the permission-roles
						
						
						// mergeSiteRoles will merge users from WT into the new site
						// it also creates a set for the users from CT or CTools having a certain role
						try 
						{	
							mergeSiteRoles(element3, siteId, useIdTrans);
						} 
						catch (PermissionException e1) 
						{
						//}
					}
				}	
			}
		}
	}	// mergeSite


	/**
	* Merge the content attachment resources from the attachments part of the
	* archive file into the content hosting service.
	* Map the attachment folder to something new based on the mergeId.
	* @param mergeId The value pre-pended to the numeric attachment folder id from the archive.
	* to make it unique here.
	* @param root The XML DOM tree of content to merge.
	*/
/*
	protected void mergeAttachments(String mergeId, Element root)
	{
		try
		{
			ContentHostingService service = (ContentHostingService)TurbineServices.getInstance()
									.getService(ContentHostingService.SERVICE_NAME);

			NodeList children = root.getChildNodes();
			final int length = children.getLength();
			for(int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE)
				{
					Element element = (Element)child;

					// for "resource" kids
					if (element.getTagName().equals("resource"))
					{
						// map the attachment area folder name
						String oldId = element.getAttribute("id");
						if (oldId.startsWith("/attachment/"))
						{
							String newId = "/attachment/"
								+ mergeId + "-"
								+ oldId.substring(12);
							element.setAttribute("id", newId);
						}

						// 	resource: add if missing
						service.mergeResource(element);
					}
				}
			}
		}
		catch (Exception any)
		{
			M_log.warn("mergeAttachments(): exception: ", any);
		}
	}	// mergeAttachments
*/

	/**
	 * Old archives have the old CHEF 1.2 service names...
	 */
	protected String translateServiceName(String name)
	{
		if ("org.chefproject.service.GenericContentHostingService".equals(name))
		{
			return ContentHostingService.class.getName();
		}
		
		return name;
	}

}	// BasicArchiveService



