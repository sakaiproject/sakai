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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.archive.api.ArchiveService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.util.Xml;
import org.sakaiproject.util.MergeConfig;

@Slf4j
@Setter
public class SiteMerger {

	protected static HashMap userIdTrans = new HashMap();

	/**********************************************/
	/* Injected Dependencies                      */
	/**********************************************/

	private LTIService ltiService;
	private ContentHostingService contentHostingService;
	private AuthzGroupService authzGroupService;
	private SiteService siteService;
	private SecurityService securityService;
	private EntityManager entityManager;
	private ServerConfigurationService serverConfigurationService;
	private UserDirectoryService userDirectoryService;

    //	 only the resources created by the followinng roles will be imported
	// role sets are different to different system
	//public String[] SAKAI_roles = m_filteredSakaiRoles; //= {"Affiliate", "Assistant", "Instructor", "Maintain", "Organizer", "Owner"};

	// tool id updates
	private String old_toolId_prefix = "chef.";
	private String new_toolId_prefix = "sakai.";
	private String[] old_toolIds = {"sakai.noti.prefs", "sakai.presence", "sakai.siteinfogeneric", "sakai.sitesetupgeneric", "sakai.threadeddiscussion"};
	private String[] new_toolIds = {"sakai.preferences", "sakai.online", "sakai.siteinfo", "sakai.sitesetup", "sakai.discussion"};

	private String DEV_MERGE_KEEP_ATTACHMENTS = "dev.merge.keep.attachments";
	private Boolean DEV_MERGE_KEEP_ATTACHMENTS_DEFAULT = false;

	/**
	 * Process a merge for the file, or if it's a directory, for all contained files (one level deep).
	 *
	 * @param fileName The site name (for the archive file) to read from.
	 * @param mergeId The id string to use to make ids in the merge consistent and unique.
	 * @param creatorId The creator id
	 * If null or blank, the date/time string of the merge is used.
	 */
	public String merge(String fileName, String siteId, String creatorId, String m_storagePath,
						boolean filterSakaiServices, String[] filteredSakaiServices, boolean filterSakaiRoles, String[] filteredSakaiRoles)
	{
		StringBuilder results = new StringBuilder();

		File[] files = null;

		// See if the name is a rooted directory in tomcat.home
		if ( ! fileName.startsWith("/") ) {
			fileName = m_storagePath + fileName;
		}
		File file = new File(fileName);
		if ((file == null) || (!file.exists()))
		{
			results.append("file: " + fileName + " not found.\n");
			log.warn("merge(): file not found: {}", file.getPath());
			return results.toString();
		} else {
			try {
				// Path must be within tomcat.home (one up from SakaiHome)
				// TODO: This seems arbitrary. Why should the archive files be within tomcat's
				// directory structure?
				File baseLocation = new File(serverConfigurationService.getSakaiHomePath());
				if (!file.getCanonicalPath().startsWith(baseLocation.getParentFile().getCanonicalPath())) {
					throw new Exception();
				}
			} catch (Exception ex) {
				// TODO; this exception will either be thrown by the home path file creation, or
				// explicitly because the archive is not hosted inside Tomcat's directory structure.
				// Like, why bother having this block here?
				results.append("file: " + fileName + " not permitted.\n");
				log.warn("merge(): file not permitted: {}", file.getPath());
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

		MergeConfig mcx = new MergeConfig();
		mcx.creatorId = creatorId;

		List<File> fileList = List.of(files);

		// This list is the set of xml files which are either omitted here, or have their own merge
		// method. archive.xml is really a debug log, not actual archive data and web.xml is now
		// merged in the site merger.
		List<String> specialCases = List.of("archive.xml", "web.xml", "user.xml", "site.xml", "attachment.xml");

		// Pull in the ltiContentItems for use in later merges
		fileList.stream().filter(f -> f.getName().equals("basiclti.xml")).findAny().ifPresent(f -> {
			processLti(f.getPath(), siteId, results, mcx);
		});

		// track old to new attachment names
		Map attachmentNames = new HashMap();

		// firstly, merge the users
		fileList.stream().filter(f -> f.getName().equals("user.xml")).findAny().ifPresent(f -> {
			processMerge(f.getPath(), siteId, results, mcx, filterSakaiServices, filteredSakaiServices, filterSakaiRoles, filteredSakaiRoles);
		});

		// see if there's a site definition which we will process at the end.
		Optional<File> siteFile = fileList.stream().filter(f -> f.getName().equals("site.xml")).findAny();

		fileList.stream().filter(f -> f.getName().equals("attachment.xml")).findAny().ifPresent(f -> {
			processMerge(f.getPath(), siteId, results, mcx, filterSakaiServices, filteredSakaiServices, filterSakaiRoles, filteredSakaiRoles);
		});

		fileList.stream().filter(f -> !specialCases.contains(f.getName()) && f.getName().endsWith(".xml")).forEach(f -> {
			processMerge(f.getPath(), siteId, results, mcx, filterSakaiServices, filteredSakaiServices, filterSakaiRoles, filteredSakaiRoles);
		});

		siteFile.ifPresent(f -> {
			processMerge(f.getPath(), siteId, results, mcx, filterSakaiServices, filteredSakaiServices, filterSakaiRoles, filteredSakaiRoles);
		});

		// Attachments are of the form
		// /attachment/TA-8a7a04de-77e1-497c-9ed3-79f4daa0dfd3/Assignments/fbf7609c-e289-459c-951e-187e70b653e4/ietf-jon-postel-07.png
		// The first two tokens are a folder we need to remove
		boolean keepAttachments = serverConfigurationService.getBoolean(DEV_MERGE_KEEP_ATTACHMENTS, DEV_MERGE_KEEP_ATTACHMENTS_DEFAULT);
		for (String key : mcx.attachmentNames.keySet()) {
			String value = mcx.attachmentNames.get(key);
			if (!StringUtils.startsWith(value, "/attachment/TA-")) {
				continue;
			}
			if ( keepAttachments ) {
				log.warn(DEV_MERGE_KEEP_ATTACHMENTS+" is true, keeping attachment "+value);
				continue;
			}

			int secondSlash = value.indexOf('/', 1);
			if (secondSlash == -1) continue;
			int thirdSlash = value.indexOf('/', secondSlash + 1);
			if (thirdSlash == -1) continue;
			String collectionId = value.substring(0, thirdSlash+1);
			try {
				log.debug("Removing attachment collection {}", collectionId);
				contentHostingService.removeCollection(collectionId);
			} catch (Exception e) {
				log.warn("Error removing collection {}: {}", collectionId, e.getMessage());
			}
		}

		return results.toString();
	}	// merge

	/**
	* Read in an archive file and merge the entries into the specified site.
    *
	* @param filePath The file path (for the archive file) to read from.
	* @param siteId The id of the site to merge the content into.
	* @param results A buffer to accumulate result messages.
	* @param mcx The MergeConfig for this import
	*/
	private void processMerge(String filePath, String siteId, StringBuilder results, MergeConfig mcx, boolean filterSakaiService, String[] filteredSakaiService, boolean filterSakaiRoles, String[] filteredSakaiRoles)
	{
		// correct for windows backslashes
		filePath = filePath.replace('\\', '/');

		log.debug("merge(): processing file: {}", filePath);

		Site theSite = null;
		try
		{
			theSite = siteService.getSite(siteId);
		}
		catch (IdUnusedException ignore) {
			log.info("Site not found for id:"+siteId+". New site will be created.");
		}

		// read the whole file into a DOM
		Document doc = Xml.readDocument(filePath);
		if (doc == null)
		{
			results.append("Error reading xml from: " + filePath + "\n");
			return;
		}

		// verify the root element
		Element root = doc.getDocumentElement();
		if (!root.getTagName().equals("archive"))
		{
			results.append("File: " + filePath + " does not contain archive xml.  Found this root tag: " + root.getTagName() + "\n");
			return;
		}

		// get the from site id
		String fromSite = root.getAttribute("source");
		String system = root.getAttribute("system");

		// the children
		NodeList children = root.getChildNodes();
		for(int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element = (Element)child;

			// look for site stuff
			if (element.getTagName().equals(SiteService.APPLICATION_ID))
			{	
				mergeSite(siteId, fromSite, element, new HashMap()/*empty userIdMap */, mcx, filterSakaiRoles, filteredSakaiRoles);
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


                Collection<EntityProducer> entityProducers = entityManager.getEntityProducers();
				// find the service using the EntityManager
                EntityProducer service = entityProducers.stream()
                        .filter(ep -> serviceName.equals(ep.getClass().getName()) || serviceName.equals(ep.getLabel()))
                        .findFirst()
                        .orElse(null);

				try {
					String msg = "";
					if (service != null) {
						if ((system.equalsIgnoreCase(ArchiveService.FROM_SAKAI) || system.equalsIgnoreCase(ArchiveService.FROM_SAKAI_2_8))) {
							if (checkSakaiService(filterSakaiService, filteredSakaiService, serviceName)) {
								// checks passed so now we attempt to do the merge
								Node parent = element.getParentNode();
								if (parent.getNodeType() == Node.ELEMENT_NODE) {
									Element parentEl = (Element) parent;
									mcx.archiveContext = parentEl.getAttribute("source");
									mcx.archiveServerUrl = parentEl.getAttribute("serverurl");
								}
								log.debug("Merging archive data for {} ({}) to site {} archive from context {} and server {}", serviceName, filePath, siteId, mcx.archiveContext, mcx.archiveServerUrl);
								msg = service.merge(siteId, element, filePath, fromSite, mcx);
							} else {
								log.warn("Skipping merge archive data for {} ({}) to site {}, checked filter failed (filtersOn={}, filters={})", serviceName, filePath, siteId, filterSakaiService, Arrays.toString(filteredSakaiService));
							}
						} else {
							log.warn("Skipping archive data for for {} ({}) to site {}, this does not appear to be a sakai archive", serviceName, filePath, siteId);
						}
					} else {
						log.warn("Skipping archive data for for {} ({}) to site {}, no service (EntityProducer) could be found to deal with this data", serviceName, filePath, siteId);
					}
					results.append(msg);
				} catch (Throwable t) {
					results.append("Error merging: " + serviceName + " in file: " + filePath + " : " + t.toString() + "\n");
					log.warn("Error merging: {} in file: {} : {}", serviceName, filePath, t.toString());
				}
			}
		}
	}	// processMerge

	/**
	* Read in the archive file and pull out Content Items
	* @param fileName The site name (for the archive file) to read from.
	* @param siteId The id of the site to merge the content into.
	* @param results A buffer to accumulate result messages.
	* @param ltiContentItems A map of LTI Content Items associated with this import
	*/
	protected void processLti(String fileName, String siteId, StringBuilder results, MergeConfig mcx)
	{
		// correct for windows backslashes
		fileName = fileName.replace('\\', '/');

		if (log.isDebugEnabled())
			log.debug("merge(): processing file: " + fileName);

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
		// the children lti tools ARCHIVE_LTI_CONTENT_TAG
		NodeList contentNodes = root.getElementsByTagName(LTIService.ARCHIVE_LTI_CONTENT_TAG);
		final int length = contentNodes.getLength();
		for(int i = 0; i < length; i++)
		{
			Node contentNode = contentNodes.item(i);
			if (contentNode.getNodeType() != Node.ELEMENT_NODE) continue;
			Element contentElement = (Element)contentNode;

			Map<String, Object> content = new HashMap();
			Map<String, Object> tool = new HashMap();
			ltiService.mergeContent(contentElement, content, tool);
			String contentErrors = ltiService.validateContent(content);
			if ( contentErrors != null ) {
				log.warn("import found invalid content tag {}", contentErrors);
				continue;
			}

			String toolErrors = ltiService.validateTool(tool);
			if ( toolErrors != null ) {
				log.warn("import found invalid tool tag {}", toolErrors);
				continue;
			}
			Long contentId = ltiService.getId(content);
			if ( contentId > 0 ) {
				content.put(LTIService.TOOL_IMPORT_MAP, tool);
				mcx.ltiContentItems.put(contentId, content);
			}
		}

	}	// processLti

	/**
	* Merge the site definition from the site part of the archive file into the site service.
	* Translate the id to the siteId.
	* @param siteId The id of the site getting imported into.
	* @param fromSiteId The id of the site the archive was made from.
	* @param element The XML DOM tree of messages to merge.
	* @param mcx The MergeConfig for this import
	*/
	private void mergeSite(String siteId, String fromSiteId, Element element, HashMap useIdTrans, MergeConfig mcx, boolean filterSakaiRoles, String[] filteredSakaiRoles)
	{
		String source = "";

		Node parent = element.getParentNode();
		if (parent.getNodeType() == Node.ELEMENT_NODE)
		{
			Element parentEl = (Element)parent;
			source = parentEl.getAttribute("system");
		}

		NodeList children = element.getChildNodes();
		for(int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element2 = (Element)child;
			if (!element2.getTagName().equals("site")) continue;

			NodeList toolChildren = element2.getElementsByTagName("tool");
			for(int i2 = 0; i2 < toolChildren.getLength(); i2++)
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

				// If this is a sakai.web.168 that launches an LTI url
				// import the associated content item and tool and re-link them
				// to the tool placement
				if ( LTIService.WEB_PORTLET.equals(toolId) ) {
					Element foundProperty = null;
					NodeList propertyChildren = element3.getElementsByTagName("property");
					for(int i3 = 0; i3 < propertyChildren.getLength(); i3++)
					{
						Element propElement = (Element)propertyChildren.item(i3);
						String propname = propElement.getAttribute("name");
						if ( "source".equals(propname) ) {
							String propvalue = Xml.decodeAttribute(propElement, "value");
							if ( ltiService.getContentKeyFromLaunch(propvalue) > 0 ) {
								foundProperty = propElement;
								break;
							}
						}
					}

					if ( foundProperty != null ) {
						Long contentKey = ltiService.mergeContentFromImport(element, siteId);
						if ( contentKey != null ) {
							Map<String, Object> theContent = ltiService.getContent(contentKey, siteId);
							String launchUrl = ltiService.getContentLaunch(theContent);
							Xml.encodeAttribute(foundProperty, "value", launchUrl);
						}
					}
				}

			}

			// merge the site info first
			try
			{
				siteService.merge(siteId, element2, mcx.creatorId);
				mergeSiteInfo(element2, siteId);
			}
			catch(Exception any)
			{
				log.warn(any.getMessage(), any.toString());
			}

			Site site = null;
			try
			{
				site = siteService.getSite(siteId);
			}
			catch (IdUnusedException e) 
			{
				log.warn("The site with id {} doesn't exist: {}", siteId, e.toString());
				return;
			}

			if (site != null)
			{
				NodeList children2 = element2.getChildNodes();
				for(int i2 = 0; i2 < children2.getLength(); i2++)
				{
					Node child2 = children2.item(i2);
					if (child2.getNodeType() != Node.ELEMENT_NODE) continue;
					Element element3 = (Element)child2;
					if (!element3.getTagName().equals("roles")) continue;

					try  {	
						mergeSiteRoles(element3, siteId, mcx, filterSakaiRoles, filteredSakaiRoles);
					} 
					catch (PermissionException e1) {
						log.warn(e1.getMessage(), e1.toString());
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
	private void mergeSiteInfo(Element el, String siteId)
		throws IdInvalidException, IdUsedException, PermissionException, IdUnusedException, InUseException 
	{
		// heck security (throws if not permitted)
		unlock(SiteService.SECURE_UPDATE_SITE, siteService.siteReference(siteId));

		Site edit = siteService.getSite(siteId);
		String desc = el.getAttribute("description-enc");

		try
		{
			byte[] decoded = Base64.decodeBase64(desc.getBytes("UTF-8"));
			byte[] filteredDecoded = decoded;
			for(int i = 0; i < decoded.length; i++)
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
			log.warn("mergeSiteInfo(): exception caught: {}", any.toString());
		}							
		edit.setDescription(desc);

		siteService.save(edit);

		return;

	} // mergeSiteInfo	

	/**
	* Merge the the permission-roles settings into the site
	* @param element The XML DOM tree of messages to merge.
	* @param siteId The id of the site getting imported into.
	*/
	private void mergeSiteRoles(Element el, String siteId, MergeConfig mcx, boolean filterSakaiRoles, String[] filteredSakaiRoles) throws PermissionException
	{
		// heck security (throws if not permitted)
		unlock(SiteService.SECURE_UPDATE_SITE, siteService.siteReference(siteId));

		String source = "";

		// el: <roles> node			
		Node parent0 = el.getParentNode(); // parent0: <site> node
		Node parent1 = parent0.getParentNode(); // parent1: <service> node
		Node parent = parent1.getParentNode(); // parent: <archive> node containing "system"

		if (parent.getNodeType() == Node.ELEMENT_NODE)
		{
			source = ((Element) parent).getAttribute("system");
		}

		Vector roles = new Vector();

		// to add this user with this role inito this realm
		String realmId = siteService.siteReference(siteId); //SWG "/site/" + siteId;
		try
		{
			AuthzGroup realm = authzGroupService.getAuthzGroup(realmId);
			roles.addAll(realm.getRoles());

			NodeList children = el.getChildNodes();
			for(int i = 0; i < children.getLength(); i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element element2 = (Element) child;
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
				for(int i2 = 0; i2 < children2.getLength(); i2++)
				{
					Node child2 = children2.item(i2);
					if (child2.getNodeType() != Node.ELEMENT_NODE) continue;
					Element element3 = (Element)child2;
					if (!element3.getTagName().equals("ability")) continue;

					String userId = element3.getAttribute("userId");	
					// this user has a qualified role, his/her resource will be imported
					mcx.userListAllowImport.add(userId);
				}
			} // for
		}
		catch(Exception err)
		{
			log.warn("()mergeSiteRoles realm {} edit exception caught: {}" + realmId, err.toString());
		}
		return;

	} // mergeSiteRoles

	/**
	 * Old archives have the old CHEF 1.2 service names...
	 */
	private String translateServiceName(String name)
	{
		if ("org.chefproject.service.GenericContentHostingService".equals(name))
		{
			return ContentHostingService.class.getName();
		}

		return name;
	}

	private boolean checkSakaiService (boolean m_filterSakaiServices, String[] m_filteredSakaiServices, String serviceName)
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
	 *
	 * @param lock The lock id string.
	 * @param reference The resource's reference string, or null if no resource is involved.
	 * @exception PermissionException thrown if the user does not have access
	 */
	private void unlock(String lock, String reference) throws PermissionException
	{
		if (!securityService.unlock(lock, reference))
		{
			if (!securityService.unlock(lock, reference))
			{
				// needs to bring back: where is sessionService
				// throw new PermissionException(UsageSessionService.getSessionUserId(), lock, reference);
			}
		}
	} // unlock

	/**
	 * When Sakai is importing a role in site.xml, check if it is a qualified role.
	 *
	 * @param roleId
	 * @return boolean value - true: the role is accepted for importing; otherwise, not;
	 */
	private boolean checkSystemRole(String system, String roleId, boolean filterSakaiRoles, String[] filteredSakaiRoles) {
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
