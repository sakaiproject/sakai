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
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import org.sakaiproject.archive.api.ArchiveService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.util.Xml;

@Slf4j
public class SiteArchiver {
	
	/** Dependency: ServerConfigurationService. */
	protected ServerConfigurationService m_serverConfigurationService = null;
	public void setServerConfigurationService(ServerConfigurationService service) {
		m_serverConfigurationService = service;
	}
	/** Dependency: EntityManager. */
	protected EntityManager m_entityManager = null;
	public void setEntityManager(EntityManager service) {
		m_entityManager = service;
	}
	
	/** Dependency: SiteService */
	protected SiteService m_siteService = null;
	public void setSiteService(SiteService service) {
		m_siteService = service;
	}
	
	/** Dependency: AuthzService */
	protected AuthzGroupService m_authzGroupService = null;
	public void setAuthzGroupService(AuthzGroupService service) {
		m_authzGroupService = service;
	}
	
	/** Dependency: UserDirectoryService */
	protected UserDirectoryService m_userDirectoryService = null;
	public void setUserDirectoryService(UserDirectoryService service) {
		m_userDirectoryService = service;
	}
	
	/** Dependency: TimeService */
	protected TimeService m_timeService = null;
	public void setTimeService(TimeService service) {
		m_timeService = service;
	}
	
	/** Dependency: ContentHosting */
	protected ContentHostingService m_contentHostingService = null;
	public void setContentHostingService(ContentHostingService service) {
		m_contentHostingService = service;
	}

	@Setter private TransactionTemplate transactionTemplate;

	public String archive(String siteId, String m_storagePath, String fromSystem)
	{
		StringBuilder results = new StringBuilder();

		log.info("archive(): site: {}", siteId);

		Site theSite = null;
		try
		{
			theSite = m_siteService.getSite(siteId);
		}
		catch (IdUnusedException e)
		{
			log.warn("archive(): site {} not found: ", siteId);
			throw new RuntimeException("Site not found");
		}

		// collect all the attachments we need
		List attachments = m_entityManager.newReferenceList();

		Time now = m_timeService.newTime();

		// this is the folder we are writing files to
		String storagePath = m_storagePath + siteId + "-archive/";

		// create the directory for the archive
		File dir = new File(m_storagePath + siteId + "-archive/");

		// clear the directory (if site already archived) so resources are not duplicated
		try {
			FileUtils.deleteDirectory(dir);
		} catch (IOException e) {
			log.warn("Could not clear existing archive: {}: {}", dir, e.toString());
		}

		dir.mkdirs();

		// for each registered ResourceService, give it a chance to archve
		Collection<EntityProducer> producers = m_entityManager.getEntityProducers();
        for (EntityProducer producer : producers) {
            if (producer == null) continue;
            if (!producer.willArchiveMerge()) continue;

            Document doc = Xml.createDocument();
            Stack stack = new Stack();
            Element root = doc.createElement("archive");
            doc.appendChild(root);
            root.setAttribute("source", siteId);
            root.setAttribute("server", m_serverConfigurationService.getServerId());
            root.setAttribute("date", now.toString());
            root.setAttribute("system", fromSystem);
            root.setAttribute("xmlns:sakai", ArchiveService.SAKAI_ARCHIVE_NS);
            root.setAttribute("xmlns:CHEF", ArchiveService.SAKAI_ARCHIVE_NS.concat("CHEF"));
            root.setAttribute("xmlns:DAV", ArchiveService.SAKAI_ARCHIVE_NS.concat("DAV"));

            stack.push(root);

            final String serviceName = producer.getClass().getCanonicalName();
            try {
                transactionTemplate.executeWithoutResult(
                        transactionStatus -> results
                                .append("<===== Start ")
                                .append(producer.getLabel())
                                .append(" [").append(serviceName).append("]")
                                .append(" =====>\n")
                                .append(producer.archive(siteId, doc, stack, storagePath, attachments))
                                .append("<===== End ")
                                .append(serviceName)
                                .append(" =====>\n\n"));
            } catch (Throwable t) {
                String failure = String.format("Failure archiving site %s from service %s [%s]: %s", siteId, producer.getLabel(), serviceName, t.getMessage());
                log.warn(failure, t);
                throw new RuntimeException(failure);
            }

            stack.pop();

            String fileName = storagePath + producer.getLabel() + ".xml";

            // fileName
            log.debug("fileName => {}", fileName);

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
			root.setAttribute("system", fromSystem);
			root.setAttribute("xmlns:sakai", ArchiveService.SAKAI_ARCHIVE_NS);
			root.setAttribute("xmlns:CHEF", ArchiveService.SAKAI_ARCHIVE_NS.concat("CHEF"));
			root.setAttribute("xmlns:DAV", ArchiveService.SAKAI_ARCHIVE_NS.concat("DAV"));
			
			stack.push(root);

			results.append("<===== Attachments =====>\n");
			results.append(m_contentHostingService.archiveResources(attachments, doc, stack, storagePath));
			results.append("<===== End =====>\n\n");

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
		root.setAttribute("system", fromSystem);
		root.setAttribute("xmlns:sakai", ArchiveService.SAKAI_ARCHIVE_NS);
		
		stack.push(root);

		results.append("<===== Site =====>\n");
		results.append(archiveSite(theSite, doc, stack, fromSystem));
		results.append("<===== End =====>\n\n");
		
		stack.pop();
		Xml.writeDocument(doc, m_storagePath + siteId + "-archive/site.xml");


		// *** Users
		doc = Xml.createDocument();
		stack = new Stack();
		root = doc.createElement("archive");
		doc.appendChild(root);
		root.setAttribute("site", siteId);
		root.setAttribute("date", now.toString());
		root.setAttribute("system", fromSystem);
		root.setAttribute("xmlns:sakai", ArchiveService.SAKAI_ARCHIVE_NS);
		
		stack.push(root);

		results.append("<===== Users =====>\n");
		results.append(archiveUsers(theSite, doc, stack));
		results.append("<===== End =====>\n\n");

		stack.pop();
		Xml.writeDocument(doc, m_storagePath + siteId + "-archive/user.xml");

		// Write an archive.xml file with status about the export
		doc = Xml.createDocument();
		stack = new Stack();
		root = doc.createElement("archive");
		doc.appendChild(root);
		root.setAttribute("site", siteId);
		root.setAttribute("date", now.toString());
		root.setAttribute("system", fromSystem);
		root.setAttribute("xmlns:sakai", ArchiveService.SAKAI_ARCHIVE_NS);

		stack.push(root);
		archiveArchive(doc, stack, results.toString());
		stack.pop();

		Xml.writeDocument(doc, m_storagePath + siteId + "-archive/archive.xml");

		log.info("Completed archive of site {}", siteId);

		return results.toString();
	}	// archive


	/**
	* Archive the site definition.
	* @param site the site.
	* @param doc The document to contain the xml.
	* @param stack The stack of elements, the top of which will be the containing
	* element of the "site" element.
	*/
	
	protected String archiveSite(Site site, Document doc, Stack stack, String fromSystem)
	{
		Element element = doc.createElement(SiteService.APPLICATION_ID);
		((Element)stack.peek()).appendChild(element);
		stack.push(element);
		
		Element siteNode = site.toXml(doc, stack);

		// By default, do not include fields that have secret or password in the name
                String filter = m_serverConfigurationService.getString("archive.toolproperties.excludefilter","password|secret");
		Pattern pattern = null;
                if ( ( ! "none".equals(filter) ) && filter.length() > 0 ) {
			try { 
				pattern = Pattern.compile(filter);
			}
			catch (Exception e) {
				pattern = null;
			}
		}

                if ( pattern != null ) {
			NodeList nl = siteNode.getElementsByTagName("property");
			List<Element> toRemove = new ArrayList<Element>();

			for(int i = 0; i < nl.getLength(); i++) {
				Element proptag = (Element)nl.item(i);
				String propname = proptag.getAttribute("name");
				if ( propname == null ) continue;
				propname = propname.toLowerCase();
				Matcher matcher = pattern.matcher(propname);
				if ( matcher.find() ) {
					toRemove.add(proptag);
				}
			}
			for(Element proptag : toRemove ) {
				proptag.getParentNode().removeChild(proptag);
			}
		}
	
		stack.push(siteNode);	
		
		// to add the realm node with user list into site
		List roles = new Vector();
		String realmId = m_siteService.siteReference(site.getId()); //SWG "/site/" + site.getId();
		try
		{
			Role role = null;
			AuthzGroup realm = m_authzGroupService.getAuthzGroup(realmId);
			
			Element realmNode = doc.createElement("roles");
			((Element)stack.peek()).appendChild(realmNode);
			stack.push(realmNode);
			
			roles.addAll(realm.getRoles());

            for (int i = 0; i< roles.size(); i++)
            {
                role = (Role) roles.get(i);
                String roleId = role.getId();
                Element node = null;
                if (ArchiveService.FROM_SAKAI_2_8.equals(fromSystem))
                {
                    node = doc.createElement("role");
                    node.setAttribute("roleId", roleId);
                }
                else
                {
                    node = doc.createElement(roleId);
                }
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
			log.warn("archve: exception archiving site: {}: {}", site.getId(), any);
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
	protected String archiveUsers(Site site, Document doc, Stack stack)
	{
		Element element = doc.createElement(UserDirectoryService.APPLICATION_ID);
		((Element)stack.peek()).appendChild(element);
		stack.push(element);
	
		try
		{
			// get the site's user list
			List users = new Vector();
			String realmId = m_siteService.siteReference(site.getId()); //SWG "/site/" + site.getId();
			try
			{
				AuthzGroup realm = m_authzGroupService.getAuthzGroup(realmId);
				users.addAll(m_userDirectoryService.getUsers(realm.getUsers()));
				Collections.sort(users);
				for (int i = 0; i < users.size(); i++)
				{
					User user = (User) users.get(i);
					user.toXml(doc, stack);
				}
			}
			catch (GroupNotDefinedException e)
			{
				log.warn(e.getMessage(), e);
			}
			catch (Exception any) {
				log.warn(any.getMessage(), any);
			}
	
		}
		catch (Exception any)
		{
			log.warn(any.getMessage(), any);
		}
	
		stack.pop();
		
		return "archiving the users for Site: " + site.getId() + "\n";
	
	}	// archiveUsers

	/**
	* Archive the archive results
	* @param doc The document to contain the xml.
	* @param stack The stack of elements
	* @param result The results of the archive operation
	*/
	protected String archiveArchive(Document doc, Stack stack, String results)
	{
		Element element = doc.createElement("log");
		((Element)stack.peek()).appendChild(element);
		stack.push(element);

		// Write log as CDATA
		CDATASection cdata = doc.createCDATASection(results);
		element.appendChild(cdata);

		stack.pop();

		return "archived the archive operation log\n";
	}	// archiveArchive
}
