/***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.archive.impl;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.archive.api.ArchiveService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
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
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.Xml;

@Slf4j
public class SiteMerger {
	protected static HashMap userIdTrans = new HashMap();
	
	/**********************************************/
	/* Injected Dependencies                      */
	/**********************************************/
	protected AuthzGroupService m_authzGroupService = null;
	public void setAuthzGroupService(AuthzGroupService service) {
		m_authzGroupService = service;
	}
	
	protected UserDirectoryService m_userDirectoryService = null;
	public void setUserDirectoryService(UserDirectoryService service) {
		m_userDirectoryService = service;
	}
	
	protected SiteService m_siteService = null;
	public void setSiteService(SiteService service) {
		m_siteService = service;
	}
	
	protected SecurityService m_securityService = null;
	public void setSecurityService(SecurityService service) {
		m_securityService = service;
	}
	
    protected EntityManager m_entityManager = null;
    public void setEntityManager(EntityManager m_entityManager) {
        this.m_entityManager = m_entityManager;
    }

    //	 only the resources created by the followinng roles will be imported
	// role sets are different to different system
	//public String[] SAKAI_roles = m_filteredSakaiRoles; //= {"Affiliate", "Assistant", "Instructor", "Maintain", "Organizer", "Owner"};
	
	// tool id updates
	private String old_toolId_prefix = "chef.";
	private String new_toolId_prefix = "sakai.";
	private String[] old_toolIds = {"sakai.noti.prefs", "sakai.presence", "sakai.siteinfogeneric", "sakai.sitesetupgeneric", "sakai.threadeddiscussion"};
	private String[] new_toolIds = {"sakai.preferences", "sakai.online", "sakai.siteinfo", "sakai.sitesetup", "sakai.discussion"};
	
	//SWG TODO I have a feeling this is a bug
	protected HashSet<String> usersListAllowImport = new HashSet<String>(); 
	/**
	* Process a merge for the file, or if it's a directory, for all contained files (one level deep).
	* @param fileName The site name (for the archive file) to read from.
	* @param mergeId The id string to use to make ids in the merge consistent and unique.
	* @param creatorId The creator id
	* If null or blank, the date/time string of the merge is used.
	*/
	//TODO Javadoc this
	public String merge(String fileName, String siteId, String creatorId, String m_storagePath,
						boolean filterSakaiServices, String[] filteredSakaiServices, boolean filterSakaiRoles, String[] filteredSakaiRoles)
	{
		StringBuilder results = new StringBuilder();

		File[] files = null;

		// see if the name is a directory
		File file = new File(m_storagePath + fileName);
		if ((file == null) || (!file.exists()))
		{
			results.append("file: " + fileName + " not found.\n");
			log.warn("merge(): file not found: " + file.getPath());
			return results.toString();
		} else {
			try {
				// Path outside archive location, discard !
				File baseLocation = new File(m_storagePath);
				if (!file.getCanonicalPath().startsWith(baseLocation.getCanonicalPath())) {
					throw new Exception();
		}
			} catch (Exception ex) {
				results.append("file: " + fileName + " not permitted.\n");
				log.warn("merge(): file not permitted: " + file.getPath());
				return results.toString();
			}
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
				processMerge(files[i].getPath(), siteId, results, attachmentNames, null, filterSakaiServices, filteredSakaiServices, filterSakaiRoles, filteredSakaiRoles);
				files[i] = null;
				break;
			}
		}
		
		// see if there's a site definition
		for (int i = 0; i < files.length; i++)
		{
			if ((files[i] != null) && (files[i].getPath().indexOf("site.xml") != -1))
			{
				processMerge(files[i].getPath(), siteId, results, attachmentNames, creatorId, filterSakaiServices, filteredSakaiServices, filterSakaiRoles, filteredSakaiRoles);
				files[i] = null;
				break;
			}
		}

		// see if there's an attachments definition
		for (int i = 0; i < files.length; i++)
		{
			if ((files[i] != null) && (files[i].getPath().indexOf("attachment.xml") != -1))
			{
				processMerge(files[i].getPath(), siteId, results, attachmentNames, null, filterSakaiServices, filteredSakaiServices, filterSakaiRoles, filteredSakaiRoles);
				files[i] = null;
				break;
			}
		}

		// process each remaining file that is an .xml file
		for (int i = 0; i < files.length; i++)
		{
			if (files[i] != null)
				if (files[i].getPath().endsWith(".xml"))
					processMerge(files[i].getPath(), siteId, results, attachmentNames, null, filterSakaiServices, filteredSakaiServices, filterSakaiRoles, filteredSakaiRoles);
		}

		return results.toString();

	}	// merge
	
	/**
	* Read in an archive file and merge the entries into the specified site.
	* @param fileName The site name (for the archive file) to read from.
	* @param siteId The id of the site to merge the content into.
	* @param results A buffer to accumulate result messages.
	* @param attachmentNames A map of old to new attachment names.
	* @param useIdTrans A map of old WorkTools id to new Ctools id
	* @param creatorId The creator id
	*/
	protected void processMerge(String fileName, String siteId, StringBuilder results, Map attachmentNames, String creatorId, boolean filterSakaiService, String[] filteredSakaiService, boolean filterSakaiRoles, String[] filteredSakaiRoles)
	{
		// correct for windows backslashes
		fileName = fileName.replace('\\', '/');

		if (log.isDebugEnabled())
			log.debug("merge(): processing file: " + fileName);

		Site theSite = null;
		try
		{
			theSite = m_siteService.getSite(siteId);
		}
		catch (IdUnusedException ignore) {
			log.info("Site not found for id:"+siteId+". New site will be created.");
		}

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
				//if (system.equalsIgnoreCase(ArchiveService.FROM_WT))
				//	mergeSite(siteId, fromSite, element, userIdTrans, creatorId);
				//else
				mergeSite(siteId, fromSite, element, new HashMap()/*empty userIdMap */, creatorId, filterSakaiRoles, filteredSakaiRoles);
			}
			else if (element.getTagName().equals(UserDirectoryService.APPLICATION_ID))
			{	;
				// Apparently, users have only been merged in they are from WorkTools...
				// Is this every going to be wanted in Sakai?
				//	String msg = mergeUsers(element, userIdTrans);
				//	results.append(msg);
			}

			else
			{
				// we need a site now
				if (theSite == null)
				{
					results.append("Site: " + siteId + " not found.\n");
					return;
				}
				
				String serviceName;
				//Support for attachment.xml created prior to SAK-29456 
				if (element.getTagName().equals(ContentHostingService.APPLICATION_ID)) 
				{
				    serviceName = "org.sakaiproject.content.api.ContentHostingService";
				}
				else {
				// get the service name
				    serviceName = translateServiceName(element.getTagName());
				}

				// get the service
				try
				{
					EntityProducer service = (EntityProducer) ComponentManager.get(serviceName);
                    if (service == null) {
                        // find the service using the EntityManager
                        List<EntityProducer> entityProducers = m_entityManager.getEntityProducers();
                        for (EntityProducer entityProducer : entityProducers) {
                            if (serviceName.equals(entityProducer.getClass().getName())
                                    || serviceName.equals(entityProducer.getLabel())
                            ) {
                                service = entityProducer;
                                break;
                            }
                        }
                    }

                    try
					{
						String msg = "";
						if (service != null) {
						    if ((system.equalsIgnoreCase(ArchiveService.FROM_SAKAI) || system.equalsIgnoreCase(ArchiveService.FROM_SAKAI_2_8))) {
						        if (checkSakaiService(filterSakaiService, filteredSakaiService, serviceName)) {
						            // checks passed so now we attempt to do the merge
		                            if (log.isDebugEnabled()) log.debug("Merging archive data for "+serviceName+" ("+fileName+") to site "+siteId);
		                            msg = service.merge(siteId, element, fileName, fromSite, attachmentNames, new HashMap() /* empty userIdTran map */, usersListAllowImport);
						        } else {
						            log.warn("Skipping merge archive data for "+serviceName+" ("+fileName+") to site "+siteId+", checked filter failed (filtersOn="+filterSakaiService+", filters="+Arrays.toString(filteredSakaiService)+")");
						        }
						    } else {
						        log.warn("Skipping archive data for for "+serviceName+" ("+fileName+") to site "+siteId+", this does not appear to be a sakai archive");
						    }
						} else {
                            log.warn("Skipping archive data for for "+serviceName+" ("+fileName+") to site "+siteId+", no service (EntityProducer) could be found to deal with this data");
						}
						results.append(msg);
					}
					catch (Throwable t)
					{
						results.append("Error merging: " + serviceName + " in file: " + fileName + " : " + t.toString() + "\n");
						log.warn("Error merging: " + serviceName + " in file: " + fileName + " : " + t.toString(),t);
					}
				}
				catch (Throwable t)
				{
					results.append("Did not recognize the resource service: " + serviceName + " in file: " + fileName + "\n");
					log.warn("Did not recognize the resource service: " + serviceName + " in file: " + fileName, t);
				}
			}
		}

	}	// processMerge
	
	/**
	* Merge the site definition from the site part of the archive file into the site service.
	* Translate the id to the siteId.
	* @param siteId The id of the site getting imported into.
	* @param fromSiteId The id of the site the archive was made from.
	* @param element The XML DOM tree of messages to merge.
	* @param creatorId The creator id
	*/
	protected void mergeSite(String siteId, String fromSiteId, Element element, HashMap useIdTrans, String creatorId, boolean filterSakaiRoles, String[] filteredSakaiRoles)
	{
		String source = "";
					
		Node parent = element.getParentNode();
		if (parent.getNodeType() == Node.ELEMENT_NODE)
		{
			Element parentEl = (Element)parent;
			source = parentEl.getAttribute("system");
		}
					
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
				m_siteService.merge(siteId, element2, creatorId);
				mergeSiteInfo(element2, siteId);
			}
			catch(Exception any)
			{
				log.warn(any.getMessage(), any);
			}
			
			Site site = null;
			try
			{
				site = m_siteService.getSite(siteId);
			}
			catch (IdUnusedException e) 
			{
				log.warn(this + "The site with id " + siteId + " doesn't exit", e);
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
	
					try  {	
						mergeSiteRoles(element3, siteId, useIdTrans, filterSakaiRoles, filteredSakaiRoles);
					} 
					catch (PermissionException e1) {
						log.warn(e1.getMessage(), e1);
					}
				}	
			}
		}
	}	// mergeSite
	
	/**
	* Merge the site info like description from the site part of the archive file into the site service.
	* @param element The XML DOM tree of messages to merge.
	* @param siteId The id of the site getting imported into.
	*/
	protected void mergeSiteInfo(Element el, String siteId)
		throws IdInvalidException, IdUsedException, PermissionException, IdUnusedException, InUseException 
	{
		// heck security (throws if not permitted)
		unlock(SiteService.SECURE_UPDATE_SITE, m_siteService.siteReference(siteId));
	
		Site edit = m_siteService.getSite(siteId);
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
			log.warn("mergeSiteInfo(): exception caught", any);	
		}							
		//edit.setTitle(title);
		edit.setDescription(desc);
		
		m_siteService.save(edit);
			 
		return;
		
	} // mergeSiteInfo	
	
	/**
	* Merge the the permission-roles settings into the site
	* @param element The XML DOM tree of messages to merge.
	* @param siteId The id of the site getting imported into.
	*/
	protected void mergeSiteRoles(Element el, String siteId, HashMap useIdTrans, boolean filterSakaiRoles, String[] filteredSakaiRoles) throws PermissionException
	{
		// heck security (throws if not permitted)
		unlock(SiteService.SECURE_UPDATE_SITE, m_siteService.siteReference(siteId));
		
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
		//List maintainUsers = new Vector();
		//List accessUsers = new Vector();
		
		// to add this user with this role inito this realm
		String realmId = m_siteService.siteReference(siteId); //SWG "/site/" + siteId;
		try
		{
			AuthzGroup realm = m_authzGroupService.getAuthzGroup(realmId);
			roles.addAll(realm.getRoles());

			NodeList children = el.getChildNodes();
			final int length = children.getLength();
			for(int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element element2 = (Element)child;
                String roleId = null;

                if (ArchiveService.FROM_SAKAI_2_8.equals(source))
                {
                    if (!"role".equals(element2.getTagName())) continue;

                    roleId = element2.getAttribute("roleId");
                }
                else
                {
                    roleId = element2.getTagName();
                }

				//SWG Getting rid of WT part above, this was previously the else branch labeled "for both CT classic and Sakai CTools"
				// check is this roleId is a qualified one
				if (!checkSystemRole(source, roleId, filterSakaiRoles, filteredSakaiRoles)) continue;
					
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
			} // for
		}
		catch(Exception err)
		{
			log.warn("()mergeSiteRoles realm edit exception caught" + realmId,err);
		}
		return;
	
	} // mergeSiteRoles
	
	/**
	* Merge the user list into the the system.
	* Translate the id to the siteId.
	* @param element The XML DOM tree of messages to merge.
	*/
	//SWG This seems to have been abandoned for anything for WorkTools.
	//    If we need the ability to import users again, see ArchiveServiceImpl.java
	//    for the implementation of this method.
	//protected String mergeUsers(Element element, HashMap useIdTrans) 
	//throws IdInvalidException, IdUsedException, PermissionException
	
	
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
	
	/*
	 * 
	 */
	protected boolean checkSakaiService (boolean m_filterSakaiServices, String[] m_filteredSakaiServices, String serviceName)
	{
		if (m_filterSakaiServices)
		{
			for (int i = 0; i < m_filteredSakaiServices.length; i ++)
			{
				if (serviceName.endsWith(m_filteredSakaiServices[i].toString()))
				{
					return true;
				}
			}
			return false;
		}
		else 
		{
			return true;
		}
	}
	 
	 /**
		* Check security permission.
		* @param lock The lock id string.
		* @param reference The resource's reference string, or null if no resource is involved.
		* @exception PermissionException thrown if the user does not have access
		*/
		protected void unlock(String lock, String reference) throws PermissionException
		{
			if (!m_securityService.unlock(lock, reference))
			{
				// needs to bring back: where is sessionService
				// throw new PermissionException(UsageSessionService.getSessionUserId(), lock, reference);
			}
		} // unlock
		
	/**
	* When Sakai is importing a role in site.xml, check if it is a qualified role.
	* @param roleId
	* @return boolean value - true: the role is accepted for importing; otherwise, not;
	*/
	protected boolean checkSystemRole(String system, String roleId, boolean filterSakaiRoles, String[] filteredSakaiRoles) {
		if (system.equalsIgnoreCase(ArchiveService.FROM_SAKAI) || system.equalsIgnoreCase(ArchiveService.FROM_SAKAI_2_8)) {
			if (filterSakaiRoles)
			{
				for (int i = 0; i <filteredSakaiRoles.length; i++)
				{
					if (!filteredSakaiRoles[i].equalsIgnoreCase(roleId))
					return true;
				}
			}
			else {
				return true;
			}
		}	
		return false;
	}
}
