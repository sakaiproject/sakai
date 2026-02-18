/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *			 http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Charles Hedrick, hedrick@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.sakaiproject.authz.api.AuthzRealmLockException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.lti.util.SakaiLTIUtil;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.InputTranslatable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Statisticable;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lessonbuildertool.LessonBuilderAccessAPI;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageGroup;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.ToolApi;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderConstants;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderEvents;
import org.sakaiproject.lessonbuildertool.cc.CartridgeLoader;
import org.sakaiproject.lessonbuildertool.cc.Parser;
import org.sakaiproject.lessonbuildertool.cc.PrintHandler;
import org.sakaiproject.lessonbuildertool.cc.ZipLoader;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.OrphanPageFinder;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.util.api.LinkMigrationHelper;
import org.sakaiproject.grading.api.ConflictingAssignmentNameException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Xml;
import org.springframework.context.MessageSource;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.extern.slf4j.Slf4j;
import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.util.MergeConfig;

/**
 * @author hedrick
 * The goal is to get sites to save and copy. However there's actually no data
 * involved in this tool. The only configuration is the URL, which is a tool
 * configuration property. That's handled separately in site.xml
 *
 */
@Slf4j
public class LessonBuilderEntityProducer extends AbstractEntityProvider
	implements EntityProducer, EntityTransferrer, Serializable,
			   CoreEntityProvider, AutoRegisterEntityProvider, Statisticable, InputTranslatable, Createable, ToolApi  {
	private static final String ARCHIVE_VERSION = "2.4"; // in case new features are added in future exports
	private static final String VERSION_ATTR = "version";
	private static final String NAME = "name";
	private static final String VALUE = "value";

	private static final String PROPERTIES = "properties";
	private static final String PROPERTY = "property";
	public static final String REFERENCE_ROOT = "/lessonbuilder";
	public static final String LESSONBUILDER = "lessonbuilder";
	public static final String ATTR_TOP_REFRESH = "sakai.vppa.top.refresh";

	public final static String ENTITY_PREFIX = "lessonbuilder";

	public final static String REF_LB = "lessonbuilder/fix/";
	public final static String REF_LB_ASSIGNMENT = "lessonbuilder/fix/assignment/";
	public final static String REF_LB_ASSESSMENT = "lessonbuilder/fix/assessment/";
	public final static String REF_LB_FORUM = "lessonbuilder/fix/forum/";


	// other tools don't copy group access restrictions, so I think we probably shouldn't. The data is
	// there in the archive
	public final boolean RESTORE_GROUPS = false;

	private ToolManager toolManager;
	private SecurityService securityService;
	private SessionManager sessionManager;
	private SiteService siteService;
	private ContentHostingService contentHostingService;
	private MemoryService memoryService;
	private SimplePageToolDao simplePageToolDao;
	private LessonEntity forumEntity;
	private LessonEntity quizEntity;
	private LessonEntity assignmentEntity;
	private LessonEntity bltiEntity;
	private GradebookIfc gradebookIfc;
	private LessonBuilderAccessAPI lessonBuilderAccessAPI;
	private MessageSource messageSource;
	private LTIService ltiService;
	private TimeService timeService;
	private LinkMigrationHelper linkMigrationHelper;
	public void setLessonBuilderAccessAPI(LessonBuilderAccessAPI l) {
		lessonBuilderAccessAPI = l;
	}


	private Set<String> servers;

	/*
	 * There are several types of updating when we move a lesson from one site to another:
	 * Fixing HTML text:
	 *  fixItems - during load, called on the XML structure for each page
	 *	for each item object in the new page, if it's text, fixup the URLs (fixUrls)
	 *  fixUrls - for a piece of HTML, fixup urls on it, with convertHtmlContent
	 *  convertHtmlContent - for a piece of HTML, fixup urls on it
	 *	  finds all URLs in a text and calls processUrl
	 *  processUrl
	 *	  for special dummy "http://lessonbuilder.sakaiproject.org/ITEMID update the ID
	 *	  for /access/content, etc, update site ID
	 *	  see migrateEmbeddedlinks below for updating references to other sakai objects
	 *
	 * Fixing sakaiid's so that Sakai items point to the assignment, test, etc. in the new site
	 *   updateEntityReferences - called by Sakai as part of load with map of old and new references
	 *		  one-argument version called from tool to get anything that couldn't be done during load
	 *	  if the kernel supports migrateAllLinks, call migrateEmbeddedLinks
	 *	  look up the all items in the map, and update the sakaiId to the new assignment, test, etc, id
	 *		  Sakai supplies a map for objects in old site to new site. However not all tools support it
	 *		  the one-argument version constructs a map when the entry is an "objectid". This is returned
	 *			 from the tool-specific Lessons code, and may be different for assignments, test, quizes, etc.
	 *			 but normally the objectid uses the title of the object in the tool since titles of quizes, etc
	 *			 are unique in a given site. the update operation calls findobject in the tool-specific interface
	 *			 to locate the quiz with that title
	 *   migrateEmbedded links - for all text items in site, call kernel linkMigrationHelper
	 */

	// The attributes in HTML that should have their values looked at and possibly re-written
	private Collection<String> attributes = new HashSet<String>(
			Arrays.asList(new String[] { "href", "src", "background", "action",
				"pluginspage", "pluginurl", "classid", "code", "codebase",
				"data", "usemap" }));

	private Pattern attributePattern;

	private Pattern pathPattern;
	private Pattern dummyPattern;

	final String ITEMDUMMY = "http://lessonbuilder.sakaiproject.org/";


	public void init() {
		log.info("init()");

		try {
			EntityManager.registerEntityProducer(this, REFERENCE_ROOT);
		}
		catch (Exception e) {
			log.warn("Error registering Link Tool Entity Producer", e);
		}

		lessonBuilderAccessAPI.setToolApi(this);

		linkMigrationHelper = (LinkMigrationHelper) ComponentManager.get("org.sakaiproject.util.api.LinkMigrationHelper");

		// Builds a Regexp selector.
		StringBuilder regexp = new StringBuilder("(");
		for (String attribute : attributes) {
			regexp.append(attribute);
			regexp.append("|");
		}
		if (regexp.length() > 1) {
			regexp.deleteCharAt(regexp.length() - 1);
		}
		regexp.append(")[\\s]*=[\\s]*([\"'|])([^\"']*)(\\2|#)");
		attributePattern = Pattern.compile(regexp.toString(),
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		pathPattern = Pattern
			.compile("/(?:access/content/group|web|dav|xsl-portal/site|portal/site)/([^/]+)/.*");

		dummyPattern = Pattern.compile(ITEMDUMMY + "\\d+");

		// Add the server name to the list of servers
		String serverName = ServerConfigurationService.getString("serverName", null);
		String serverId = ServerConfigurationService.getString("serverId", null);

		servers = new HashSet<String>();
		// prefer servername to serverid, by doing it first
		if (serverName != null)
			servers.add(serverName);
		if (serverId != null)
			servers.add(serverId);
		try {
			String hostName = InetAddress.getLocalHost().getHostName();
			servers.add(hostName);
			hostName = InetAddress.getLocalHost().getCanonicalHostName();
			servers.add(hostName);
		} catch (Exception ignore) {
		}
		servers.add("localhost");
		// if neither is defined we're in trouble;
		if (servers.size() == 0)
			log.info("LessonBuilderEntityProducer ERROR: neither servername nor serverid defined in sakai.properties");

		try {
			ComponentManager.loadComponent("org.sakaiproject.lessonbuildertool.service.LessonBuilderEntityProducer", this);
		} catch (Exception e) {
			log.warn("Error registering Lesson Builder Entity Producer with Spring. Lessonbuilder will work, but Lesson Builder instances won't be imported from site archives. This normally happens only if you redeploy Lessonbuilder. Suggest restarting Sakai", e);
		}

	}

	/**
	 * Destroy
	 */
	public void destroy()
	{
		log.info("destroy()");
	}

	@Override
	public String[] myToolIds()
	{
		String[] toolIds = {LessonBuilderConstants.TOOL_ID};
		return toolIds;
	}

	public List<String> myToolList()
	{
		List<String> toolList = new ArrayList<String>();
		toolList.add(LessonBuilderConstants.TOOL_ID);
		return toolList;
	}

	@Override
	public Optional<List<String>> getTransferOptions() {
		return Optional.of(Arrays.asList(new String[] { EntityTransferrer.COPY_PERMISSIONS_OPTION }));
	}

	/**
	 * Get the service name for this class
	 * @return
	 */
	protected String serviceName() {
		return LessonBuilderEntityProducer.class.getName();
	}

	protected void addAttr(Document doc, Element element, String name, String value) {
		if (value == null)
			return;
		Attr attr = doc.createAttribute(name);
		attr.setValue(value);
		element.setAttributeNode(attr);
	}

	protected void addPage(Document doc, Element element, SimplePage page, Site site, List attachments) {

		long pageId = page.getPageId();

		Element pageElement = doc.createElement("page");

		addAttr(doc, pageElement, "pageid", Long.valueOf(pageId).toString());
		addAttr(doc, pageElement, "toolid", page.getToolId());
		addAttr(doc, pageElement, "siteid", page.getSiteId());
		addAttr(doc, pageElement, "title", page.getTitle());

		Long parent = page.getParent();
		if (parent != null) {
			addAttr(doc, pageElement, "parent", parent.toString());
		}
		else {
			// Get some settings for a top level page that are stored not in SimplePage but in a corresponding SimplePageItem
			SimplePageItem spi = simplePageToolDao.findTopLevelPageItemBySakaiId(Long.toString(pageId));
			if (spi != null) {
				addAttr(doc, pageElement, "required", spi.isRequired() ? "true" : "false");
				addAttr(doc, pageElement, "prerequisite", spi.isPrerequisite() ? "true" : "false");
			}
			else {
				log.error("Cannot find SimplePageItem for top level page id={}", pageId);
			}
		}

		parent = page.getTopParent();
		if (parent != null)
			addAttr(doc, pageElement, "topparent", parent.toString());

		addAttr(doc, pageElement, "hidden", page.isHidden() || page.isHiddenFromNavigation() ? "true" : "false");
		addAttr(doc, pageElement, "hiddenfromnavigation", page.isHiddenFromNavigation() ? "true" : "false");
		// we don't read this on input, as copying typically assumes you'll want new release dates
		Date releaseDate = page.getReleaseDate();
		if (releaseDate != null)
			addAttr(doc, pageElement, "releasedate", releaseDate.toString());
		Double gradebookPoints = page.getGradebookPoints();
		if (gradebookPoints != null)
			addAttr(doc, pageElement, "gradebookpoints", gradebookPoints.toString());
		String cssSheet = page.getCssSheet();
		if (cssSheet != null && !cssSheet.equals(""))
			addAttr(doc, pageElement, "csssheet", cssSheet);

		String folder = page.getFolder();
		if (folder != null && !folder.equals(""))
			addAttr(doc, pageElement, "folder", folder);

		List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(pageId);

		if (items != null) {
			for (SimplePageItem item: items) {

				Element itemElement = doc.createElement("item");
				addAttr(doc, itemElement, "id", Long.valueOf(item.getId()).toString());
				addAttr(doc, itemElement, "pageId", Long.valueOf(item.getPageId()).toString());
				addAttr(doc, itemElement, "sequence", Integer.valueOf(item.getSequence()).toString());
				addAttr(doc, itemElement, "type", Integer.valueOf(item.getType()).toString());
				addAttr(doc, itemElement, "sakaiid", item.getSakaiId());
				if (!(SimplePageItem.DUMMY).equals(item.getSakaiId())) {
					if (item.getType() == SimplePageItem.FORUM || item.getType() == SimplePageItem.ASSESSMENT || item.getType() == SimplePageItem.ASSIGNMENT) {
						LessonEntity e = null;
						if (item.getType() == SimplePageItem.FORUM)
							e = forumEntity;
						else if (item.getType() == SimplePageItem.ASSESSMENT)
							e = quizEntity;
						else
							e = assignmentEntity;
						e = e.getEntity(item.getSakaiId());
						if (e != null) {
							String title = e.getTitle();
							if (title != null && !title.equals(""))
								addAttr(doc, itemElement, "sakaititle", title);
						}
					}

				}

				String html = item.getHtml();

				// References to assets in html text content (type 5) that aren't in this site's Resources
				if ((attachments != null) && (item.getType() == SimplePageItem.TEXT) && (html != null) && html.contains("/access/content/")) {
					org.jsoup.nodes.Document htmlDoc = Jsoup.parse(html);

					// Typically audio or video <source> or <img>
					Elements media = htmlDoc.select("[src]");
					for (org.jsoup.nodes.Element src : media) {
						String link = src.attr("abs:src");

						// embedded fckeditor attachments
						if (link.contains("/access/content/attachment/")) {
							String linkRef = link.replace(link.substring(0, link.indexOf("/attachment/")), "");
							Reference ref = EntityManager.newReference(contentHostingService.getReference(linkRef));
							attachments.add(ref);
							log.info("Found attachment asset: {} adding to attachment list as: {}", link, linkRef);
						}

						// cross-site references
						if (link.contains("/access/content/group/") && !link.contains(site.getId())) {
							// URLDecode this to turn it back into a Sakai content ID
							try {
								String linkRef = URLDecoder.decode(link.replace(link.substring(0, link.indexOf("/group/")), ""), "UTF-8");
								Reference ref = EntityManager.newReference(contentHostingService.getReference(linkRef));
								attachments.add(ref);
								log.info("Found cross-site asset: {} adding to attachment list as: {}", link, linkRef);
							} catch (UnsupportedEncodingException e) {
								log.error("Unable to add link {} to attachment list, {}", link, e.toString());
							}
						}
					}
				}

				// check for cross-site video resources (type 7)
				if ((attachments != null) && ((item.getType() == SimplePageItem.MULTIMEDIA) || (item.getType() == SimplePageItem.RESOURCE))) {
					if (item.getSakaiId().startsWith("/group/") && (item.getSakaiId().split("/").length >=2)) {
						String groupId = item.getSakaiId().split("/")[2];
						log.debug("Lessons item in siteid {} with resource group id {}", site.getId(), groupId);
						if (!site.getId().equals(groupId)) {
							// Append to the attachment reference list for archive
							Reference ref = EntityManager.newReference(contentHostingService.getReference(item.getSakaiId()));
							attachments.add(ref);
							log.info("Found cross-site item, adding to attachment list: {}", item.getSakaiId());
						}
					}
				}

				// the Sakai ID is good enough for other object types
				addAttr(doc, itemElement, "name", item.getName());
				addAttr(doc, itemElement, "html", html);
				addAttr(doc, itemElement, "description", item.getDescription());
				addAttr(doc, itemElement, "height", item.getHeight());
				addAttr(doc, itemElement, "width", item.getWidth());
				addAttr(doc, itemElement, "alt", item.getAlt());
				addAttr(doc, itemElement, "required", item.isRequired() ? "true" : "false");
				addAttr(doc, itemElement, "prerequisite", item.isPrerequisite() ? "true" : "false");
				addAttr(doc, itemElement, "subrequirement", item.getSubrequirement() ? "true" : "false");
				addAttr(doc, itemElement, "requirementtext", item.getRequirementText());
				addAttr(doc, itemElement, "nextpage", item.getNextPage() ? "true" : "false");
				addAttr(doc, itemElement, "format", item.getFormat());
				addAttr(doc, itemElement, "anonymous", item.isAnonymous() ? "true" : "false");
				addAttr(doc, itemElement, "showComments", item.getShowComments() ? "true" : "false");
				addAttr(doc, itemElement, "forcedCommentsAnonymous", item.getForcedCommentsAnonymous() ? "true" : "false");
				addAttr(doc, itemElement, "groups", item.getGroups());
				addAttr(doc, itemElement, "gradebookId", item.getGradebookId());
				addAttr(doc, itemElement, "gradebookPoints", String.valueOf(item.getGradebookPoints()));
				addAttr(doc, itemElement, "gradebookTitle", item.getGradebookTitle());
				addAttr(doc, itemElement, "altGradebook", item.getAltGradebook());
				addAttr(doc, itemElement, "altPoints", String.valueOf(item.getAltPoints()));
				addAttr(doc, itemElement, "altGradebookTitle", item.getAltGradebookTitle());
				addAttr(doc, itemElement, "groupOwned", item.isGroupOwned() ? "true" : "false");

				Collection<Group> siteGroups = site.getGroups();
				addGroup(doc, itemElement, item.getOwnerGroups(), "ownerGroup", siteGroups);

				if (item.getType() == SimplePageItem.FORUM || item.getType() == SimplePageItem.ASSESSMENT || item.getType() == SimplePageItem.ASSIGNMENT) {
					LessonEntity e = null;
					if (item.getType() == SimplePageItem.FORUM)
						e = forumEntity;
					else if (item.getType() == SimplePageItem.ASSESSMENT)
						e = quizEntity;
					else
						e = assignmentEntity;
					e = e.getEntity(item.getSakaiId());
					if (e != null) {
						String objectid = e.getObjectId();  // this is something like assignment/ID/TITLE. It's used to find the object in the new site if necessary
						if (objectid!= null)
							addAttr(doc, itemElement, "objectid", objectid);
					}
				}

				if (item.isSameWindow() != null)
					addAttr(doc, itemElement, "samewindow", item.isSameWindow() ? "true" : "false");

				String attrString = item.getAttributeString(); //json encoded attributes

				if (attrString != null) {
					Element attributeElement = doc.createElement("attributes");
					attributeElement.setTextContent(attrString);
					itemElement.appendChild(attributeElement);
				}

				pageElement.appendChild(itemElement);
			}
		}
		element.appendChild(pageElement);
	}

	void addGroup(Document doc, Element itemElement, String groupString, String attr, Collection<Group>siteGroups) {
		if (groupString != null && !groupString.equals("") && siteGroups != null) {
			String [] groups = groupString.split(",");
			for (int i = 0; i < groups.length ; i++) {
				Element groupElement = doc.createElement(attr);
				addAttr(doc, groupElement, "id", groups[i]);
				Group group = null;
				for (Group g: siteGroups)
					if (g.getId().equals(groups[i])) {
						group = g;
						break;
					}
				if (group != null)
					addAttr(doc, groupElement, "title", group.getTitle());
				itemElement.appendChild(groupElement);
			}
		}
	}


	@Override
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		return archive(siteId, doc, stack, archivePath, attachments, null);
	}

	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments, List<String> selectedIds)
	{
		// prepare the buffer for the results log
		StringBuilder results = new StringBuilder();

		// Orphaned pages need not apply!
		SimplePageBean simplePageBean = makeSimplePageBean(siteId);
		OrphanPageFinder orphanFinder = simplePageBean.getOrphanFinder(siteId);
		
		Map<Long, List<Long>> pageToReferencedPages = findReferencedPagesByItems(siteId);

		Set<Long> originalSelectedPageIds = new HashSet<>();
		Set<Long> selectedPageIds = new HashSet<>();
		boolean hasSelection = selectedIds != null && !selectedIds.isEmpty();
		if (hasSelection) {
			for (String idStr : selectedIds) {
				try {
					Long pageId = Long.valueOf(idStr);
					originalSelectedPageIds.add(pageId);
					selectedPageIds.add(pageId);
				} catch (NumberFormatException e) {
					log.warn("Invalid page Id: {}", idStr);
				}
			}
			// Expand selection to include all descendant pages
			selectedPageIds = expandSelectionToIncludeDescendants(selectedPageIds, siteId, orphanFinder, pageToReferencedPages);
		}

		try
		{
			Site site = siteService.getSite(siteId);
			// start with an element with our very own (service) name
			Element element = doc.createElement(serviceName());
			element.setAttribute(VERSION_ATTR, ARCHIVE_VERSION);
			((Element) stack.peek()).appendChild(element);
			stack.push(element);

			Element lessonbuilder = doc.createElement(LESSONBUILDER);

			int orphansSkipped = 0;
			int selectionSkipped = 0;

			List<SimplePage> sitePages = simplePageToolDao.getSitePages(siteId);
			if (sitePages != null && !sitePages.isEmpty()) {
				for (SimplePage page: sitePages) {
					// Skip orphaned pages unless they are in our selected set
					boolean isSelectedOrExpanded = hasSelection && selectedPageIds.contains(Long.valueOf(page.getPageId()));
					if (orphanFinder.isOrphan(page.getPageId()) && !isSelectedOrExpanded) {
						orphansSkipped++;
						continue;
					}
					if (hasSelection && !selectedPageIds.contains(Long.valueOf(page.getPageId()))) {
						selectionSkipped++;
						continue;
					}
					addPage(doc, lessonbuilder, page, site, attachments);
				}
			}

			log.info("Skipped {} orphaned pages and {} non-selected pages while archiving site {}", orphansSkipped, selectionSkipped, siteId);

			int count = 0;
			if (hasSelection) {
				Set<Long> topLevelSelectedPages = findTopLevelSelectedPages(originalSelectedPageIds, selectedPageIds, siteId, pageToReferencedPages);
				// Filter out top-level selections that are orphans (not exported as pages)
				List<Long> orderedTopLevelPages = new ArrayList<>();
				for (Long id : topLevelSelectedPages) {
					boolean wasOriginallySelected = originalSelectedPageIds.contains(id);
					if (orphanFinder.isOrphan(id) && !wasOriginallySelected) {
						selectionSkipped++;
						continue;
					}
					orderedTopLevelPages.add(id);
				}
				// Ensure deterministic ordering of placements
				Collections.sort(orderedTopLevelPages);

				for (Long topLevelPageId : orderedTopLevelPages) {
					SimplePage topLevelPage = simplePageToolDao.getPage(topLevelPageId);
					if (topLevelPage != null) {
						element = doc.createElement(LESSONBUILDER);
						addAttr(doc, element, "toolid", topLevelPageId.toString());
						addAttr(doc, element, "name", topLevelPage.getTitle());
						addAttr(doc, element, "pagePosition", String.valueOf(count));
						addAttr(doc, element, "functions.require", "");
						addAttr(doc, element, "pageVisibility", "true");
						addAttr(doc, element, "pageId", Long.toString(topLevelPageId));

						lessonbuilder.appendChild(element);
						count++;
					}
				}
			} else {
				Collection<ToolConfiguration> tools = site.getTools(myToolIds());
				if (tools != null && !tools.isEmpty())
				{
					for (ToolConfiguration config: tools) {
						element = doc.createElement(LESSONBUILDER);

						addAttr(doc, element, "toolid", config.getPageId());
						addAttr(doc, element, "name" , config.getContainingPage().getTitle());
						addAttr(doc, element, "pagePosition" , config.getContainingPage().getPosition() + "");

						Properties props = config.getPlacementConfig();
						String roleList = StringUtils.trimToEmpty(props.getProperty("functions.require"));
						String pageVisibility = StringUtils.trimToEmpty(props.getProperty(ToolManager.PORTAL_VISIBLE));

						addAttr(doc, element, "functions.require", roleList);
						addAttr(doc, element, "pageVisibility" , pageVisibility);

						// should be impossible for these nulls, but we've seen it
						if (simplePageToolDao.getTopLevelPageId(config.getPageId()) != null)
							addAttr(doc, element, "pageId", Long.toString(simplePageToolDao.getTopLevelPageId(config.getPageId())));
						else
							log.warn("archive site {} tool page {} null lesson", siteId, config.getPageId());

						lessonbuilder.appendChild(element);
						count++;
					}
				}
			}

			results.append("archiving " + count + " LessonBuilder instances.\n");

			((Element) stack.peek()).appendChild(lessonbuilder);
			stack.push(lessonbuilder);

			stack.pop();
		}
		catch (Exception any)
		{
			log.warn("archive: exception archiving service: {} {}", any.toString(), serviceName());
		}

		stack.pop();

		return results.toString();
	}

	/**
	 * Expands the selected page IDs to include all descendant pages
	 * Only includes referenced subpages if the parent page is valid (not orphaned)
	 */
	private Set<Long> expandSelectionToIncludeDescendants(Set<Long> selectedPageIds, String siteId, OrphanPageFinder orphanFinder, Map<Long, List<Long>> pageToReferencedPages) {
		if (selectedPageIds.isEmpty()) return selectedPageIds;

		Set<Long> expandedIds = new HashSet<>(selectedPageIds);
		List<SimplePage> allPages = simplePageToolDao.getSitePages(siteId);

		if (allPages == null || allPages.isEmpty()) return expandedIds;

		// Build parent-child relationships based on the parent field
		Map<Long, List<Long>> parentToChildren = new HashMap<>();
		for (SimplePage page : allPages) {
			Long parentId = page.getParent();
			if (parentId != null) {
				parentToChildren.computeIfAbsent(parentId, k -> new ArrayList<>())
					.add(page.getPageId());
			}
		}

		Queue<Long> toProcess = new LinkedList<>(selectedPageIds);

		while (!toProcess.isEmpty()) {
			Long currentPageId = toProcess.poll();

			// Only process this page if it's not an orphan
			boolean isExplicitlySelected = selectedPageIds.contains(currentPageId);
			boolean isOrphan = orphanFinder.isOrphan(currentPageId);

			if (isOrphan && !isExplicitlySelected) {
				log.debug("Skipping orphan page {} during expansion", currentPageId);
				continue;
			}

			// Add direct children (based on parent field)
			List<Long> children = parentToChildren.get(currentPageId);
			if (children != null) {
				for (Long childId : children) {
					if (!expandedIds.contains(childId) && !orphanFinder.isOrphan(childId)) {
						expandedIds.add(childId);
						toProcess.offer(childId);
					}
				}
			}

			// Add pages referenced by items of type page (subpages)
			if (!isOrphan) {
				List<Long> referencedPages = pageToReferencedPages.get(currentPageId);
				if (referencedPages != null) {
					for (Long referencedPageId : referencedPages) {
						if (!expandedIds.contains(referencedPageId)) {
							// Only include the referenced page if it exists and is valid
							SimplePage referencedPage = simplePageToolDao.getPage(referencedPageId);
							if (referencedPage != null && siteId.equals(referencedPage.getSiteId())) {
								expandedIds.add(referencedPageId);
								toProcess.offer(referencedPageId);
								log.debug("Including referenced subpage {} from valid page {}", referencedPageId, currentPageId);
							}
						}
					}
				}
			}
		}

		return expandedIds;
	}

	/**
	 * Handles orphaned subpages that weren't exported due to missing parent field
	 * but are referenced by SimplePageItems of type PAGE.
	 */
	private Long handleOrphanedSubpage(Long oldSubpageId, String fromSiteId, String toSiteId, Map<Long, Long> pageMap, Long parentPageId) {
		try {
			SimplePage orphanedPage = simplePageToolDao.getPage(oldSubpageId);
			if (orphanedPage == null || !fromSiteId.equals(orphanedPage.getSiteId())) {
				log.debug("Orphaned subpage {} not found in source site {}", oldSubpageId, fromSiteId);
				return null;
			}

			// Create a new page in the destination site
			SimplePage newPage = simplePageToolDao.makePage(toSiteId, null, orphanedPage.getTitle(), parentPageId, null);

			// Copy essential properties
			if (orphanedPage.getCssSheet() != null) {
				// Update CSS sheet paths to reference the new site
				String newCssSheet = orphanedPage.getCssSheet().replace("/group/" + fromSiteId + "/", "/group/" + toSiteId + "/");
				newPage.setCssSheet(newCssSheet);
			}

			// Copy other properties
			newPage.setHidden(orphanedPage.isHidden());
			newPage.setHiddenFromNavigation(orphanedPage.isHiddenFromNavigation());
			newPage.setReleaseDate(orphanedPage.getReleaseDate());
			newPage.setGradebookPoints(orphanedPage.getGradebookPoints());
			if (orphanedPage.getFolder() != null) {
				String newFolder = orphanedPage.getFolder().replace("/group/" + fromSiteId + "/", "/group/" + toSiteId + "/");
				newPage.setFolder(newFolder);
			}

			// Set the parent to the current page being processed to establish hierarchy
			newPage.setParent(parentPageId);

			// Find the top-level page for topparent
			SimplePage parentPage = simplePageToolDao.getPage(parentPageId);
			Long topParentId = parentPageId;
			if (parentPage != null) {
				// Traverse up to find the real top parent
				while (parentPage.getParent() != null) {
					parentPage = simplePageToolDao.getPage(parentPage.getParent());
					if (parentPage != null) {
						topParentId = parentPage.getPageId();
					} else {
						break;
					}
				}
				newPage.setTopParent(topParentId);

				// Set the toolId from the top-level page
				SimplePage topParentPage = simplePageToolDao.getPage(topParentId);
				if (topParentPage != null && topParentPage.getToolId() != null) {
					newPage.setToolId(topParentPage.getToolId());
				}
			}

			// Save the page
			simplePageToolDao.quickSaveItem(newPage);

			// Add to the pageMap so other references to this page work
			pageMap.put(oldSubpageId, newPage.getPageId());

			log.info("Created orphaned subpage recovery: old page {} -> new page {} with parent {} in site {}", 
					oldSubpageId, newPage.getPageId(), parentPageId, toSiteId);

			return newPage.getPageId();

		} catch (Exception e) {
			log.error("Failed to handle orphaned subpage {}: {}", oldSubpageId, e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Finds pages referenced by SimplePageItems of type PAGE (subpages)
	 * This helps identify orphaned subpages that should be included in exports
	 * but lack proper parent/topparent/toolid fields
	 */
	private Map<Long, List<Long>> findReferencedPagesByItems(String siteId) {
		Map<Long, List<Long>> pageToReferencedPages = new HashMap<>();
		
		List<SimplePage> allPages = simplePageToolDao.getSitePages(siteId);
		if (allPages == null || allPages.isEmpty()) {
			return pageToReferencedPages;
		}
		
		for (SimplePage page : allPages) {
			List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(page.getPageId());
			if (items != null) {
				for (SimplePageItem item : items) {
					if (item.getType() == SimplePageItem.PAGE) {
						try {
							Long referencedPageId = Long.valueOf(item.getSakaiId());
							// Verify that the referenced page exists
							SimplePage referencedPage = simplePageToolDao.getPage(referencedPageId);
							if (referencedPage != null && siteId.equals(referencedPage.getSiteId())) {
								pageToReferencedPages.computeIfAbsent(page.getPageId(), k -> new ArrayList<>())
									.add(referencedPageId);
								log.debug("Found subpage reference: page {} references subpage {}", page.getPageId(), referencedPageId);
							}
						} catch (NumberFormatException e) {
							// Invalid sakaiId, skip this item
							log.debug("Invalid sakaiId for PAGE item: {}", item.getSakaiId());
						}
					}
				}
			}
		}
		
		return pageToReferencedPages;
	}

	/**
	 * Finds the top-level pages that should become new Lessons from the selected pages
	 * Only considers pages that were originally selected by the user, not those added automatically as references
	 */
	private Set<Long> findTopLevelSelectedPages(Set<Long> originalSelectedPageIds, Set<Long> allSelectedPageIds, String siteId, Map<Long, List<Long>> pageToReferencedPages) {
		Set<Long> topLevelPages = new HashSet<>();
		Set<Long> referencedBySelectedPages = new HashSet<>();
		
		// Find all pages that are referenced by originally selected pages
		for (Long selectedPageId : originalSelectedPageIds) {
			List<Long> referencedPages = pageToReferencedPages.get(selectedPageId);
			if (referencedPages != null) {
				referencedBySelectedPages.addAll(referencedPages);
			}
		}

		for (Long pageId : originalSelectedPageIds) {
			SimplePage page = simplePageToolDao.getPage(pageId);
			if (page == null) continue;

			// Check if this page has a parent relationship or is referenced by another selected page
			Long parentId = page.getParent();
			boolean hasSelectedParent = parentId != null && originalSelectedPageIds.contains(parentId);
			boolean isReferencedBySelected = referencedBySelectedPages.contains(pageId);
			
			// It's a top-level page if it has no selected parent AND is not referenced by a selected page
			if (!hasSelectedParent && !isReferencedBySelected) {
				topLevelPages.add(pageId);
			}
		}

		return topLevelPages;
	}

	@Override
	public String getEntityUrl(Reference ref)
	{
		String URL = "";
		long id = idFromRef(ref.getReference());
		if ("page".equals(ref.getSubType())) {
			SimplePage currentPage = simplePageToolDao.getPage(id);
			URL = String.format("%s/site/%s/page/%s", ServerConfigurationService.getPortalUrl(), currentPage.getSiteId(), currentPage.getToolId());
			return URL;
		}
		//Just return a URL to the top of the page based on the item's pageId and toolId
		SimplePageItem item = simplePageToolDao.findItem(id);
		if (item != null) {
			URL = item.getURL();
			if (URL == null || "".equals(URL) ) {
				//Return a default as portal tool page for now
				SimplePage currentPage = simplePageToolDao.getPage(item.getPageId());
				URL = ServerConfigurationService.getPortalUrl()+"/site/"+currentPage.getSiteId()+"/page/"+currentPage.getToolId();
			}
		}
		return URL;
	}

	@Override
	public HttpAccess getHttpAccess()
	{
		// not for now
		return lessonBuilderAccessAPI.getHttpAccess();
	}

	@Override
	public String getLabel() {
		return LESSONBUILDER;
	}

	// the pages are already made. this adds the elements
	private boolean mergePage(Element element, String oldServer, String siteId, String fromSiteId, Map<Long,Long> pageMap,
			Map<Long,Long> itemMap, Map<String,String> entityMap, MergeConfig mcx) {

		log.debug("mergePage: element {} siteId {} fromSiteId {}", element, siteId, fromSiteId);
		String oldSiteId = element.getAttribute("siteid");
		String oldPageIdString = element.getAttribute("pageid");
		Long oldPageId = Long.valueOf(oldPageIdString);
		Long pageId = pageMap.get(oldPageId);
		Site site = null;
		Collection<Group> siteGroups = null;
		boolean needFix = false;

		// not currently doing this
		if (RESTORE_GROUPS) {
			try {
				site = siteService.getSite(siteId);
				siteGroups = site.getGroups();
			} catch (Exception impossible) {};
		}

		Site fromSite = null;
		try {
			fromSite = siteService.getSite(fromSiteId);
		} catch (Exception impossible) {
			fromSite = null;
		};

		boolean isSameServer = fromSite != null;

		NodeList allChildrenNodes = element.getChildNodes();
		int length = allChildrenNodes.getLength();
		for (int i = 0; i < length; i++) {

			Node node = allChildrenNodes.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;
			Element itemElement = (Element) node;
			if (!itemElement.getTagName().equals("item")) continue;

			String s = itemElement.getAttribute("sequence");
			int sequence = Integer.valueOf(s);
			s = itemElement.getAttribute("type");
			int type = Integer.valueOf(s);
			String sakaiId = itemElement.getAttribute("sakaiid");
			String name = itemElement.getAttribute("name");
			String explanation = null;
			String sakaiTitle = itemElement.getAttribute("sakaititle");
			log.debug("Processing item {}", sakaiTitle);
			String id = itemElement.getAttribute("id");
			Long itemId = Long.valueOf(id);

			// URL is probably no longer used, but if it is, it probably doesn't need mapping
			if (type == SimplePageItem.RESOURCE || type == SimplePageItem.MULTIMEDIA) {
				String prefix = "/group/" + oldSiteId + "/";
				if (sakaiId.startsWith(prefix))
					sakaiId = "/group/" + siteId + "/" + sakaiId.substring(prefix.length());
				else
					log.warn("sakaiId not recognized: {}", sakaiId);
			} else if (type == SimplePageItem.BLTI) {
				// We need to import the BLTI tool to the new site and update the sakaiid
				if (sakaiId == null || !sakaiId.startsWith("/blti/")) {
					log.warn("Invalid BLTI sakaiId format: {}", sakaiId);
					continue;
				}
				String[] bltiId = sakaiId.split("/");
				Long ltiContentId = NumberUtils.toLong(bltiId[2]);
				if (ltiContentId < 1) {
					log.warn("Invalid BLTI sakaiId format: {}", sakaiId);
					continue;
				}
				if ( isSameServer ) {
					try {
						Map<String, Object> ltiContent = ltiService.getContentDao(ltiContentId, oldSiteId, securityService.isSuperUser());
						String newSakaiId = copyLTIContent(ltiContent, siteId, oldSiteId);
						if ( newSakaiId != null ) sakaiId = newSakaiId;
					} catch (Exception e) {
						log.warn("Unable to import LTI tool to new site: {}", e);
						continue;
					}
				} else {
					if ( mcx.ltiContentItems == null ) {
						log.warn("Unable to look up LTI content item with ID: {}", ltiContentId);
						continue;
					}
					Map<String, Object> ltiContentItem = mcx.ltiContentItems.get(ltiContentId);
					if (ltiContentItem == null) {
						log.warn("Unable to find LTI content item with ID: {}", ltiContentId);
						continue;
					}

					// Lets find the right tool to assiociate with if it is already installed
					Long ltiToolId = null;
					String launchUrl = ltiContentItem.get(LTIService.LTI_LAUNCH).toString();
					String toolBaseUrl = SakaiLTIUtil.stripOffQuery(launchUrl);
					List<Map<String,Object>> tools = ltiService.getTools(null,null,0,0, siteId);
					Map<String, Object> ltiTool = SakaiLTIUtil.findBestToolMatch(launchUrl, null, tools);
					if ( ltiTool != null ) ltiToolId = ltiService.getId(ltiTool);

					// If no matching tool, lets get a tool from the import XML if provided
					// or make a stub tool from the content data
					if ( ltiToolId == null ) {
						ltiTool = (Map<String, Object>) ltiContentItem.get(LTIService.TOOL_IMPORT_MAP);
						if (ltiTool == null) {
							log.debug("Creating LTI11 Stub Tool for {}", toolBaseUrl);
							ltiTool = ltiService.createStubLTI11Tool(toolBaseUrl, ltiContentItem.get(LTIService.LTI_TITLE).toString());
						}
						Object toolResult = ltiService.insertTool(ltiTool, siteId);
						if  (!(toolResult instanceof Long)) {
							log.warn("Unable to add LTI tool to new site: {}", toolResult);
							continue;
						} else {
							ltiToolId = (Long) toolResult;
						}
					}
					// Now store the content item with the toolId
					ltiContentItem.put(LTIService.LTI_TOOL_ID, ltiToolId);
					Object contentResult = ltiService.insertContent(ltiContentItem, siteId);
					if (!(contentResult instanceof Long)) {
						log.warn("Unable to import LTI content to new site: {}", contentResult);
						continue;
					} else {
						ltiContentId = (Long) contentResult;
						sakaiId = "/blti/" + ltiContentId;
						log.debug("Created new content item: {}", sakaiId);
					}
				}
			} else if (type == SimplePageItem.TEXT) {
				String html = itemElement.getAttribute("html");
				explanation = ltiService.fixLtiLaunchUrls(html, siteId, mcx);
				explanation = linkMigrationHelper.migrateLinksInMergedRTE(siteId, mcx, explanation);
				Pattern idPattern = Pattern.compile("(https?://[^/]+/access/(?:basic)?lti/site)/" + Pattern.quote(oldSiteId) + "/content:([0-9]+)");
				Matcher matcher = idPattern.matcher(html);
				StringBuffer sb = new StringBuffer();
				boolean foundLtiLink = false;
				while(matcher.find()) {
					String urlFirstPart = matcher.group(1);
					Long ltiContentId = Long.valueOf(matcher.group(2));
					log.debug("Updating reference: {}", matcher.group(0));
					foundLtiLink = true;
					try {
						Map<String, Object> ltiContent = ltiService.getContentDao(ltiContentId, oldSiteId, securityService.isSuperUser());
						String newSakaiId = copyLTIContent(ltiContent, siteId, oldSiteId);
						if ( newSakaiId != null ) sakaiId = newSakaiId;
						String[] bltiId = sakaiId.split("/");
						ltiContentId = Long.valueOf(bltiId[2]);
					} catch (Exception e) {
						log.warn("Failed to import LTI tool [contentId: {}, fromSite: {}, toSite: {}]: {}. Tool will be skipped.", ltiContentId, oldSiteId, siteId, e.toString());
					} finally {
						String updatedReference = urlFirstPart + "/" + siteId + "/content:" + ltiContentId;
						log.debug("New reference: {}", updatedReference);
						matcher.appendReplacement(sb, Matcher.quoteReplacement(updatedReference));
					}
				}

				if(foundLtiLink) {
					matcher.appendTail(sb);
					explanation = sb.toString();
					log.debug("Updated at least one LTI reference lesson HTML");
				}
			} else if (type == SimplePageItem.PAGE) {
				// sakaiId should be the new page ID
				Long oldSubpageId = Long.valueOf(sakaiId);
				Long newPageId = pageMap.get(oldSubpageId);
				// we've seen a few cases where sakaiId of a subpage is 0. It won't be
				// in the map, so this leaves it zero.
				if (newPageId != null) {
					sakaiId = newPageId.toString();
				} else {
					// Try to find and import the orphaned subpage from the source site
					newPageId = handleOrphanedSubpage(oldSubpageId, fromSiteId, siteId, pageMap, pageId);
					if (newPageId != null) {
						sakaiId = newPageId.toString();
						log.info("Successfully recovered orphaned subpage {} as new page {}", oldSubpageId, newPageId);
					} else {
						log.warn("Could not recover orphaned subpage {} referenced from page {}", oldSubpageId, pageId);
					}
				}
			}

			if (type == SimplePageItem.ASSIGNMENT || type == SimplePageItem.ASSESSMENT || type == SimplePageItem.FORUM) {
				sakaiId = SimplePageItem.DUMMY;
				needFix = true;
			}

			log.debug("makePage.makeItem: pageId {} type {} sakaiId {} name {}", pageId, type, sakaiId, name);
			SimplePageItem item = simplePageToolDao.makeItem(pageId, sequence, type, sakaiId, name);

			if (explanation != null) {
				item.setHtml(explanation);
			} else {
				item.setHtml(itemElement.getAttribute("html"));
			}
			s = itemElement.getAttribute("description");
			if (s != null) item.setDescription(s);
			s = itemElement.getAttribute("height");
			if (s != null) item.setHeight(s);
			s = itemElement.getAttribute("width");
			if (s != null) item.setWidth(s);
			s = itemElement.getAttribute("alt");
			if (s != null) item.setAlt(s);
			s = itemElement.getAttribute("required");
			if (s != null) item.setRequired(s.equals("true"));
			s = itemElement.getAttribute("prerequisite");
			if (s != null) item.setPrerequisite(s.equals("true"));
			s = itemElement.getAttribute("subrequirement");
			if (s != null) item.setSubrequirement(s.equals("true"));
			s = itemElement.getAttribute("requirementtext");
			if (s != null)item.setRequirementText(s);
			s = itemElement.getAttribute("nextpage");
			if (s != null) item.setNextPage(s.equals("true"));
			s = itemElement.getAttribute("format");
			if (s != null) item.setFormat(s);
			s = itemElement.getAttribute("samewindow");
			if (s != null) item.setSameWindow(s.equals("true"));
			s = itemElement.getAttribute("anonymous");
			if (s != null) item.setAnonymous(s.equals("true"));
			s = itemElement.getAttribute("showComments");
			if (s != null) item.setShowComments(s.equals("true"));
			s = itemElement.getAttribute("forcedCommentsAnonymous");
			if (s != null)item.setForcedCommentsAnonymous(s.equals("true"));
			s = itemElement.getAttribute("gradebookTitle");
			if (s != null) item.setGradebookTitle(s);
			s = itemElement.getAttribute("altGradebookTitle");
			if (s != null) item.setAltGradebookTitle(s);
			s = itemElement.getAttribute("gradebookPoints");
			if (s != null && !s.equals("null")) item.setGradebookPoints(Integer.valueOf(s));
			s = itemElement.getAttribute("altPoints");
			if (s != null && !s.equals("null")) item.setAltPoints(Integer.valueOf(s));
			s = itemElement.getAttribute("groupOwned");
			if (s != null) item.setGroupOwned(s.equals("true"));

			if (RESTORE_GROUPS) {
				String groupString = mergeGroups(itemElement, "ownerGroup", siteGroups, fromSiteId);
				if (groupString != null)
					item.setOwnerGroups(groupString);
			}

			// save objectid for dummy items so we can do mapping; alt isn't otherwise used for these items
			if (type == SimplePageItem.ASSIGNMENT || type == SimplePageItem.ASSESSMENT || type == SimplePageItem.FORUM) {
				item.setAlt(itemElement.getAttribute("objectid"));
			}

		   List<String> gradebookUids = Arrays.asList(siteId);

			// not currently doing this, although the code has been tested.
			// The problem is that other tools don't do it. Since much of our group
			// awareness comes from the other tools, enabling this produces
			// inconsistent results
			if (RESTORE_GROUPS) {
				String groupString = mergeGroups(itemElement, "groups", siteGroups, fromSiteId);
				if (groupString != null)
					item.setGroups(groupString);

				if (gradebookIfc.isGradebookGroupEnabled(siteId)) {
					gradebookUids = gradebookIfc.getGradebookGroupInstances(siteId);
				}
			}

			NodeList attributes = itemElement.getElementsByTagName("attributes");
			if (attributes != null && attributes.getLength() > 0) {
				Node attributesNode = attributes.item(0); // only one
				String attributeString = attributesNode.getTextContent();
				item.setAttributeString(attributeString);
			}

			simplePageToolDao.quickSaveItem(item);
			log.debug("itemMap: itemId {} item.getId() {}", itemId, item.getId());
			itemMap.put(itemId, item.getId());

			boolean needupdate = false;

			// these need the item number, so do after save
			s = itemElement.getAttribute("gradebookId");
			if (s != null && !s.equals("null") && !s.equals("")) {
				// update item number in both gradebook id and title
				String title = item.getGradebookTitle();
				if(title == null || title.equals("null") || title.equals("")) {
					title = s;
				}
				// update gb id
				int ii = s.lastIndexOf(":");
				s = s.substring(0, ii+1) + item.getId();
				// update title
				// can't do this, because Gradebook 2 will create items with the original name.
				// the plan is to use user-defined names so we avoid this whole problem.
				if (false) {
					ii = title.lastIndexOf(":");
					title = title.substring(0, ii+1) + item.getId() + ")";
				}

				try {
					gradebookIfc.addExternalAssessment(gradebookUids, siteId, s, null, title, Double.valueOf(itemElement.getAttribute("gradebookPoints")), null, LessonBuilderConstants.TOOL_ID);
					needupdate = true;
					item.setGradebookId(s);
				} catch(ConflictingAssignmentNameException cane){
					log.error("ConflictingAssignmentNameException for title {} and attribute {}.", title, "gradebookId");
				}
			}

			s = itemElement.getAttribute("altGradebook");
			if (s != null && !s.equals("null") && !s.equals("")) {
				// update item number in both gradebook id and title
				String title = item.getAltGradebookTitle();
				if(title == null || title.equals("null") || title.equals("")) {
					title = s;
				}
				// update gb id
				int ii = s.lastIndexOf(":");
				s = s.substring(0, ii+1) + item.getId();
				// update title
				// can't do this, because Gradebook 2 will create items with the original name.
				// the plan is to use user-defined names so we avoid this whole problem.
				if (false) {
					ii = title.lastIndexOf(":");
					title = title.substring(0, ii+1) + item.getId() + ")";
				}
				try {
					gradebookIfc.addExternalAssessment(gradebookUids, siteId, s, null, title, Double.valueOf(itemElement.getAttribute("altPoints")), null, LessonBuilderConstants.TOOL_ID);
					needupdate = true;
					item.setAltGradebook(s);
				} catch(ConflictingAssignmentNameException cane){
					log.error("ConflictingAssignmentNameException for title {} and attribute {}.", title, "altGradebook");
				}
			}

			// have to save again, I believe
			if (needupdate) {
				log.debug("updating item: {}", item);
				simplePageToolDao.quickUpdate(item);
			}

			// these needs item id, so it has to be done here
			// save item ID to object id. This will allow references to be fixed up.
			// object id identifies the Sakai object in the old site. The fixup will
			// find the object in the new site and fix up the item. Hence we need
			// a mapping of item ID to object id.

			simplePageToolDao.syncQRTotals(item);

			if (type == SimplePageItem.ASSIGNMENT || type == SimplePageItem.ASSESSMENT || type == SimplePageItem.FORUM) {
				String objectid = itemElement.getAttribute("objectid");
				if (objectid != null) {
					String entityid = null;
					if (type == SimplePageItem.ASSIGNMENT)
						entityid = REF_LB_ASSIGNMENT + item.getId();
					else if (type == SimplePageItem.ASSESSMENT)
						entityid = REF_LB_ASSESSMENT + item.getId();
					else
						entityid = REF_LB_FORUM + item.getId();
					if (entityMap != null)
						entityMap.put(entityid, objectid);
				}
			}

		}
		return needFix;
	}

	String mergeGroups(Element itemElement, String attr, Collection<Group> siteGroups, String fromSiteId) {

		// not currently doing this, although the code has been tested.
		// The problem is that other tools don't do it. Since much of our group
		// awareness comes from the other tools, enabling this produces
		// inconsistent results

		String groups = itemElement.getAttribute(attr);
		String groupString = null;
		Site oldSite = null;
		try {
			oldSite = siteService.getSite(fromSiteId);
		} catch (Exception e) {
			log.debug("site {} not found.", fromSiteId);
		}
		// For each old group, get the corresponding new group id
		if (!groups.isEmpty() && siteGroups != null && oldSite != null) {
			final String[] parts = groups.split(",");
			for (int n = 0; n < parts.length; n ++) {
				final Group group = oldSite.getGroup(parts[n]);
				final String providerID = group.getProviderGroupId();
				if(providerID == null ) { // only transfer groups which are not old rosters
					final String title = group.getTitle();
					if (title != null && !title.equals("")) {
						for (Group g : siteGroups) {
							if (title.equals(g.getTitle())) {
								if (groupString == null) {
									groupString = g.getId();
								} else {
									groupString = groupString + "," + g.getId();
								}
							}
						}
					}
				}
			}
		}

		return groupString;

	}

	// fix up items on page. does any updates that need the whole page and item map
	private void fixItems(Element element, String oldServer, String siteId, String fromSiteId, Map<Long,Long> pageMap, Map<Long,Long> itemMap, MergeConfig mcx) {

		String oldPageIdString = element.getAttribute("pageid");
		Long oldPageId = Long.valueOf(oldPageIdString);
		Long pageId = pageMap.get(oldPageId);
		log.debug("fixing items pageId: {} oldPageId: {} pageMap: {} itemMap: {}", pageId, oldPageId, pageMap, itemMap);

		List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(pageId);
		if (items == null) return;

		String attribute;
		final JSONParser jsonParser = new JSONParser();
		for (SimplePageItem item : items) {
			boolean itemUpdated = false;
			switch (item.getType()) {
				case SimplePageItem.TEXT:
					String html = item.getHtml();
					if (StringUtils.isNotBlank(html)) {
						String fixed = fixUrls(html, oldServer, siteId, fromSiteId, itemMap);
						if (!StringUtils.equals(html, fixed)) {
							log.debug("Fixing HTML {} {}", item.getId(), html);
							item.setHtml(fixed);
							itemUpdated = true;
						}
					}
					break;
				case SimplePageItem.CHECKLIST:
					attribute = item.getAttributeString(); // json encoded attributes
					if (StringUtils.isNotBlank(attribute)) {
						try {
							Object parsedAttributeObj = jsonParser.parse(attribute);
							if (parsedAttributeObj instanceof JSONObject) {
								JSONObject parsedAttribute = (JSONObject) parsedAttributeObj;
								Object checklistItemsObj = parsedAttribute.get("checklistItems");
								if (checklistItemsObj instanceof JSONArray) {
									JSONArray checklistItems = (JSONArray) checklistItemsObj;
									for (Object checklistItemObj : checklistItems) {
										if (checklistItemObj instanceof JSONObject) {
											JSONObject checklistItem = (JSONObject) checklistItemObj;
											Object link = checklistItem.get("link");
											if (link != null) {
												Long linkId = NumberUtils.toLong(link.toString(), -1L);
												if (linkId != -1L) {
													Long newLink = itemMap.get(linkId);
													if (newLink != null) {
														linkId = newLink;
													}
												}
												checklistItem.put("link", linkId);
												parsedAttribute.put("checklistItems", checklistItems); // Update the JSON object for the JSON attribute with the modified checklist
												item.setAttributeString(parsedAttribute.toJSONString());
												itemUpdated = true;
												log.debug("fixing checklist {} {}", item.getId(), parsedAttribute.toJSONString());
											}
										}
									}
								}
							}
						} catch (ParseException pe) {
							log.warn("Exception caught while parsing checklist array: {}", pe.toString());
							break;
						}

					}
					break;

				case SimplePageItem.QUESTION:
					attribute = item.getAttributeString(); // json encoded attributes
					if (StringUtils.isBlank(attribute)) break;
					try {
						Object parsedAttributeObj = jsonParser.parse(attribute);
						if (parsedAttributeObj instanceof JSONObject) {
							JSONObject parsedAttribute = (JSONObject) parsedAttributeObj;
							Object questionTextObj = parsedAttribute.get("questionText");
							if (questionTextObj instanceof String) {
								String questionText = (String) questionTextObj;
								if ( StringUtils.isNotBlank(questionText) ) {
									questionText = ltiService.fixLtiLaunchUrls(questionText, siteId, mcx);
									questionText = linkMigrationHelper.migrateLinksInMergedRTE(siteId, mcx, questionText);
									parsedAttribute.put("questionText", questionText);
									item.setAttributeString(parsedAttribute.toJSONString());
									itemUpdated = true;
								}
							}
						}
					} catch (ParseException pe) {
						log.warn("Exception caught while parsing simple question: {}", pe.toString());
						break;
					}
					break;

				case SimplePageItem.RESOURCE_FOLDER:
					attribute = item.getAttributeString(); // json encoded attributes
					if (StringUtils.isBlank(attribute)) break;
					try {
						Object parsedAttributeObj = jsonParser.parse(attribute);
						if (parsedAttributeObj instanceof JSONObject) {
							JSONObject parsedAttribute = (JSONObject) parsedAttributeObj;
							Object dataDirectoryObj = parsedAttribute.get("dataDirectory");
							if (dataDirectoryObj instanceof String) {
								String dataDirectory = (String) dataDirectoryObj;
								if ( StringUtils.isNotBlank(dataDirectory) ) {
									dataDirectory = StringUtils.replace(dataDirectory, fromSiteId, siteId);
									parsedAttribute.put("dataDirectory", dataDirectory);
									item.setAttributeString(parsedAttribute.toJSONString());
									itemUpdated = true;
								}
							}
						}
					} catch (ParseException pe) {
						log.warn("Exception caught while parsing resource folder: {}", pe.toString());
						break;
					}
					break;

				default:
			}
			if (itemUpdated) {
				log.debug("Updating item...");
				simplePageToolDao.quickUpdate(item);
			}
		}
	}

	public String fixUrls(String s, String oldServer, String siteId, String fromSiteId, Map<Long,Long> itemMap) {
		ContentCopyContext context = new ContentCopyContext(fromSiteId, siteId, oldServer);
		String htmlWithAttachments = transferAttachmentFiles(s, fromSiteId, siteId);
		return convertHtmlContent(context, htmlWithAttachments, null, itemMap);
	}

	// Externally used for zip import
	@Override
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, MergeConfig mcx) {
		Map<String, String> entityMap = null;
		boolean isTransferCopy = false;
		return mergeInternal(siteId, root, archivePath, fromSiteId, mcx, entityMap, isTransferCopy);
	}

	/*
	 * To give some context in why merge is so complex, here is a description of the merge process.
	 *
	 * An archive of a site is an XML document that describes the site's Lessons content.
	 * It is created by the export tool.  It contains one or more trees of pages and
	 * the items within those pages.  Trees in lessons are single parent.  Each page belongs to one
	 * and only one parent page.  The root page is the only page that does not have a parent.
	 *
	 * There is also a list of tool placements in the site left nav that link to the root of each of the
	 * page trees.  All the pages and items should be accessible by starting at the placement,
	 * navigating to the top page for the placement and then going down to each of the
	 * pages in the tree.  There should be one left nav lessons placement in the archive for each tree of pages.
	 *
	 * This code is called in one of two ways: (1) as part of the second half of transferCopyEntities or
	 * (2) for an import from a CC+ zip file.  They are processed somewhat differently.  For (1), the user
	 * is presented with the option to replace or merge data and for (2) there is an expectation that
	 * the same data is not to be imported twice (i.e. duplicate removal).
	 *
	 * For use case (1), the user choice is handled before we are called.  If the user asks to replace
	 * Lessons data, all of the lessons placements from the new site are deleted before we are
	 * called and the expectation that we will create new top level Lessons placements for each
	 * placement in the "from" site with all the pages and items.  If on the other hand, the user
	 * requested "merge", then this code makes brand new lessons placements and imports (perhaps a
	 * second copy) of the lessons placements from the archive.  This means that if a "merge" copy
	 * is done from a site with two Lessons placements, the new site will have two, four, six etc..
	 * placements with a new pair of placements appearing for each succssive import.
	 *
	 * This makes things simple for (1) transferCopyEntities use case.  In the code below, we always
	 * make a new Lessons placement for each lesson placement in the import content.  There is no
	 * duplicate removal at all for transferCopyEntities calls.
	 *
	 * On the other hand, for scenario (2 - import from ZIP) we want the first import into an
	 * empty site to clone the exported site and the second and following imports to not import
	 * anything due to duplicate removal processing as follows:
	 *
	 * For (2), the site may or may not already have tool placements.  For each of the tool placements in
	 * the archive, the placement in the site can be in one of three states:
	 *
	 * 1.  The placement exists in the site and contains content.  In this case, the placement is ignored
	 *     in the import as a duplicate import.
	 *
	 * 2.  The placement does not exist in the site.  In this case, the placement is created by
	 *     adding the placement to the site and adding a vestigial page and item for the placement.
	 *     Then the root page is linked to the placement.
	 *
	 * 3.  The placement exists in the site but is empty.  In this case the placement is reused and
	 *     the ultimate root node for the placement is linked to the placement.
	 *
	 * Empty placements are those added to the site that have no content.  Because the creation of the
	 * vestigial page and item
	 * are triggered when you navigate to the Lessons placement in the Sakai UI after adding the
	 * placement in Site Info.  If you add a Lessons placement without navigating to it in the UI first
	 * and go straight to a merge(), the merge process detects the lack of the vestigial page and
	 * item are creates it before reusing the placement for the imported content.
	 *
	 * To properly process empty placements and duplicate page removal, the merge process must scan the XML
	 * content for pages and items and build a transitive closure of the page tree.  It then looks
	 * through the Site structures to make an inventory of existing Lessons placements ans assess them
	 * to see if they are full or empty or non-existent.
	 *
	 * Then it imports the non-duplicate pages and items from the archive into the site.
	 *
	 * At the end it looks at the placements in the XML and creates new placements with a vestigial page and item
	 * and links the newly created root page and its tree of pages and items into the left navigation of the site.
	 *
	 * One note on duplicate removal is that it is done based on the name of the tool in the left navigation.
	 * If you have a tool labeled "Week 1" with content and you import a site with a tool labeled "Week 1"
	 * and a set of pages, the imported pages will not be imported a second time. However if you rename the
	 * link to be "Week 1X" and do another import, a new "Week 1" will be imported and the site will have both
	 * a "Week 1" and "Week 1X " Lessons placements.
	 *
	 * One note on the wonderfully named "vestigial" page and item.  They are essential to the proper functioning
	 * of Lessons.  Not having them leads to the data model getting messed up.  In the Lessons UI if there are not
	 * in place, lessons will freak out and make new ones.  The result in this situation is that a bunch of pages
	 * might disappear.
	 *
	 * As Lessons tries to recover from the bad situation, it tends to add more pages and point the new page at
	 * the placement in the left nav.  This is not good.  It means that there are multiple pages that are somewhat
	 * non-deterministically chosen as *the* page when a user navigates to a placement in the left nav.  Lessons
	 * is very careful not to create this situation and we need to make very sure we don't create the
	 * "multiple page to one placement situation" during import / merge.
	 */

	// Internally used for both site copy and zip import
	public String mergeInternal(String siteId, Element root, String archivePath, String fromSiteId, MergeConfig mcx,
			Map<String, String> entityMap, boolean isTransferCopy)
	{

		log.debug("Lessons Merge siteId={} fromSiteId={} creatorId={} archiveContext={} archiveServerUrl={} isTransferCopy={}",
				siteId, fromSiteId, mcx.creatorId, mcx.archiveContext, mcx.archiveServerUrl, isTransferCopy);

		StringBuilder results = new StringBuilder();

		if (StringUtils.isBlank(siteId) ) {
			log.warn("Lessons merge stopped siteId is not provided");
			return "Lessons merge stopped siteId is not provided";
		}

		// Check if there is nothing to import and build trees of pages in the import
		NodeList lessonBuilderTools = root.getElementsByTagName("lessonbuilder");
		boolean lessonHasContent = false;
		Map<Long, String> placementPageMap = new HashMap<>();
		Map<Long, Long> parentPage = new HashMap<>();

		for (int toolIndex = 0; toolIndex < lessonBuilderTools.getLength(); toolIndex++) {
			Node lessonBuilderNode = lessonBuilderTools.item(toolIndex);
			if (lessonBuilderNode.getNodeType() == Node.ELEMENT_NODE) {
				Element lessonBuilderElement = (Element) lessonBuilderNode;
				Long rootPageId = NumberUtils.toLong(lessonBuilderElement.getAttribute("pageId"), 0L);  // Camel case is correct
				String rootPageName = trimToNull(lessonBuilderElement.getAttribute("name"));
				if (rootPageId > 0 && StringUtils.isNotBlank(rootPageName)) {
					log.debug("Found root lessonbuilder {} {}", rootPageName, rootPageId);
					placementPageMap.put(rootPageId, rootPageName);
				}

				NodeList lessonPages = lessonBuilderElement.getElementsByTagName("page");
				for (int pageIndex = 0; pageIndex < lessonPages.getLength(); pageIndex++) {
					Element currentPage = (Element) lessonPages.item(pageIndex);
					Long pageId = NumberUtils.toLong(currentPage.getAttribute("pageid"), 0L); // Lower case is correct
					Long pageParentId = NumberUtils.toLong(currentPage.getAttribute("parent"), 0L);
					if ( pageId > 0 && pageParentId > 0 ) {
						parentPage.put(pageId, pageParentId);
					} else if ( pageId > 0 ) {
						parentPage.put(pageId, pageId);  // Top level page is its own parent
					}

					NodeList pageItems = currentPage.getElementsByTagName("item");

					if (pageItems != null && pageItems.getLength() > 0) {
						lessonHasContent = true;
					}
				}
			}
		}

		if (!lessonHasContent) {
			log.debug("No lessonbuilder pages to import");
			return "No lessonbuilder pages to import";
		}

		// Do the transitive closure
		log.debug("Pre-transitive closure {} {}", placementPageMap, parentPage);
		for(int i=0; i< 1000; i++ ) {
			boolean changed = false;
			for (Map.Entry<Long, Long> entry : parentPage.entrySet()) {
				Long page = entry.getKey();
				Long parent = entry.getValue();
				if ( parent < 1 ) continue;
				Long parentOfParent = parentPage.getOrDefault(parent, 0L);
				if ( parentOfParent < 1 ) continue;
				log.debug("Walking page {} up tree from {} to {}", page, parent, parentOfParent);
				changed = true;
				parentPage.put(page, parentOfParent);
			}
			if ( changed ) break;
		}
		log.debug("Post-transitive closure {} {}", placementPageMap, parentPage);

		// If this is a transferCopy operation, cleanup is handled based on the user's
		// selection before we are called and we put items into existing or new placements
		// with the same name.  If the user requested "merge" - there is no real duplicate
		// handling and the user does the cleanup if needed afterwards

		// If this is an import from file, we will do our best to not overwrite any existing
		// content (i.e. avoid importing the same content twice) by only importing into empty
		// Lessons placements or creating new Lessons placements.

		Site site;
		try {
			site = siteService.getSite(siteId);
		} catch(IdUnusedException e) {
			log.warn("Lessons merge stopped site {} could not be loaded {}", siteId, e.toString());
			return "Lessons merge stopped site "+siteId+" could not be loaded "+e.toString();
		}

		Map<String, String> emptySakaiIds = new HashMap<>();
		Map<String, Long> emptyTopLevelPageIds = new HashMap<>();
		Map<String, Long> fullPlacements = new HashMap<>();

		// some code in site action creates the vestigial page and item for new placements and some doesn't
		// so we loop through and figure out which placements we already have and patch any existing placements
		// missing their vestigial page and/or item

		log.debug("Looping through {} placements in {}", LessonBuilderConstants.TOOL_ID, siteId);
		Collection<ToolConfiguration> toolConfs = site.getTools(myToolIds());
		if (toolConfs != null && !toolConfs.isEmpty())  {
			for (ToolConfiguration config: toolConfs) {
				if (!config.getToolId().equals(LessonBuilderConstants.TOOL_ID)) continue;
				SitePage p = config.getContainingPage();
				if (p == null ) continue;
				String title = p.getTitle();
				Long topLevelPageId = simplePageToolDao.getTopLevelPageId(config.getPageId());
				log.debug("Looking at tool placement {} page placement {} {} topLevelPageId {}", config.getId(), p.getId(), title, topLevelPageId);

				// If there is no top level page associated with a lessonbuilder placement
				// we need to create a vestigial page and item so it can have its tree of pages
				// linked into it later
				if (topLevelPageId == null) {
					log.debug("Creating top level vestigial page for placement site {} page {} {}",siteId, p.getId(), title);
					SimplePage page = simplePageToolDao.makePage(p.getId(), siteId, title, null, null);

					List<String>elist = new ArrayList<>();
					boolean requiresEditPermission = false;
					if ( !simplePageToolDao.saveItem(page,  elist, messageLocator.getMessage("simplepage.nowrite"), requiresEditPermission) ) {
						log.error("Failure creating top level vestigial page for placement site {} page {} {}",siteId, p.getId(), title);
						results.append("Failure creating top level vestigial page for placement site " + siteId + " page " + p.getId() + " " + title);
						continue;
					}
					topLevelPageId = page.getPageId();

					// create the vestigial item for this top level page
					log.debug("creating vestigial item for top level page: {} type: {}", topLevelPageId, SimplePageItem.PAGE);
					SimplePageItem item = simplePageToolDao.makeItem(0, 0, SimplePageItem.PAGE, Long.toString(topLevelPageId), title);
					simplePageToolDao.quickSaveItem(item);

					emptyTopLevelPageIds.put(title, topLevelPageId);
					emptySakaiIds.put(title, p.getId());
					continue;
				}

				SimplePage topLevelPage = simplePageToolDao.getPage(topLevelPageId);
				if ( topLevelPage == null ) continue;
				List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(topLevelPageId);
				if ( isTransferCopy ) { // If we are doing transferCopyEntities, pretend it is empty :)
					log.debug("merging page into lesson placement: {} {} {} ", p.getId(), title, topLevelPageId);
					emptyTopLevelPageIds.put(title, topLevelPageId);
					emptySakaiIds.put(title, p.getId());
				} else if (items.isEmpty()) {
					log.debug("found empty lesson placement: {} {} {} ", p.getId(), title, topLevelPageId);
					emptyTopLevelPageIds.put(title, topLevelPageId);
					emptySakaiIds.put(title, p.getId());
				} else {
					log.debug("found existing lesson placement: {} {} {} ", p.getId(), title, topLevelPageId);
					fullPlacements.put(title, topLevelPageId);
				}
			}
		}

		// Note that non existent placements (neither empty nor full) are created later
		log.debug("Finished scanning placements full {} empty {} / {}", fullPlacements, emptyTopLevelPageIds, emptySakaiIds);

		// Lets start the actual merge()
		NodeList pageNodes = root.getElementsByTagName("page");

		Map <Long,Long> pageMap = new HashMap<Long,Long>();

		// a convenient map of the xml page id and its corresponding element
		Map <Long,Element> pageElementMap = new HashMap<Long,Element>();

		int count = 0;

		String oldServer = root.getAttribute("server");

		// Scan pages and find the root pages in the import

		log.debug("Scanning for root pages in the import");
		Map<Long, String> rootOldPageIds = new HashMap<>();
		int numPages = pageNodes.getLength();
		for (int p = 0; p < numPages; p++) {
			Node pageNode = pageNodes.item(p);
			if (pageNode.getNodeType() != Node.ELEMENT_NODE) continue;

			Element pageElement = (Element) pageNode;
			String title = pageElement.getAttribute("title");
			if (title == null) continue;

			String oldPageIdString = pageElement.getAttribute("pageid");
			Long oldPageId = NumberUtils.toLong(oldPageIdString, 0L);
			if ( oldPageId < 1 ) continue;

			String oldParentString = pageElement.getAttribute("parent");
			Long oldParent = NumberUtils.toLong(oldParentString, 0L);
			if ( oldParent < 1 ) {
				log.debug("Found root page in archive pageId {}", oldPageId);
				rootOldPageIds.put(oldPageId, title);
			}
		}

		log.debug("Found root pages in import {}", rootOldPageIds);

		Map<String, Long> toolsReused = new HashMap<>();

		// create pages first, build up map of old to new page.
		// Do not create pages if they are already in an existing tree associated with a
		// placement (duplicate removal)
		try {
			numPages = pageNodes.getLength();
			for (int p = 0; p < numPages; p++) {
				Node pageNode = pageNodes.item(p);
				if (pageNode.getNodeType() != Node.ELEMENT_NODE) continue;

				Element pageElement = (Element) pageNode;
				String title = pageElement.getAttribute("title");
				if (title == null) title = "Page";

				String oldPageIdString = pageElement.getAttribute("pageid");
				Long oldPageId = NumberUtils.toLong(oldPageIdString, 0L);
				if ( oldPageId < 1 ) continue;

				String oldParentIdString = pageElement.getAttribute("parent");
				Long oldParentId = NumberUtils.toLong(oldParentIdString, 0L);

				// Duplicate remove:
				// Check if the page is associated with an existing complete top level page/placement
				// Recall that parentPage really points to the top page above a page because we
				// walked up the tree to compute the closure of the child-parent relationships
				Long rootPageId = parentPage.getOrDefault(oldPageId, 0L);
				String rootTitle = rootOldPageIds.getOrDefault(rootPageId, null);
				if ( StringUtils.isNotBlank(rootTitle) && fullPlacements.containsKey(rootTitle) ) {
					log.debug("Skipping page {} because root page {} {} is already in site {}", oldPageId, rootPageId, rootTitle, siteId);
					continue;
				}

				// Check to see if this is the root page and we want to reuse an existing
				// empty page placement instead of making a new page
				SimplePage page = null;
				boolean reused = false;
				log.debug("Extracting {} oldPageId: {} parent {} isTransferCopy {}", title, oldPageId, oldParentId, isTransferCopy);
				if ( ! isTransferCopy && oldParentId < 1 ) {
					Long emptyPageId = emptyTopLevelPageIds.get(title);
					log.debug("Extracting page {} oldPageId: {} emptyPageId {}", title, oldPageId, emptyPageId);

					// See if we can load the existing and empty page
					if ( emptyPageId != null && emptyPageId > 0 ) {
						page = simplePageToolDao.getPage(emptyPageId);
						log.debug("loaded top levelpage {} found {}", emptyPageId, page.getPageId());
					}
				}

				// If we are re-using an empty page associated with a placement, lets remember it
				// otherwise create a new page
				if ( page != null ) {
					toolsReused.put(title, page.getPageId());
					reused = true;
				} else {
					page = simplePageToolDao.makePage("0", siteId, title, 0L, 0L);
					log.debug("Created new page {}", page.getPageId());
				}

				// Fill the page with all the data from the archive
				String gradebookPoints = pageElement.getAttribute("gradebookpoints");
				if (StringUtils.isNotEmpty(gradebookPoints)) {
					page.setGradebookPoints(Double.valueOf(gradebookPoints));
				}
				String folder = pageElement.getAttribute("folder");
				if (StringUtils.isNotEmpty(folder)) page.setFolder(folder);

				//get new page's Date Release property
				String dateString = pageElement.getAttribute("releasedate");
				if (StringUtils.isNotEmpty(dateString)){
					DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
					Date date = formatter.parse(dateString);
					page.setReleaseDate(date);
				}

				//get new page's Hidden property
				String hiddenString = pageElement.getAttribute("hidden");
				String hiddenFromNavigationString = pageElement.getAttribute("hiddenfromnavigation");
				boolean navHidden = StringUtils.equalsIgnoreCase(hiddenFromNavigationString, "true");
				if (StringUtils.isNotEmpty(hiddenString)) {
					boolean hidden = StringUtils.equalsIgnoreCase(hiddenString, "true");
					// Export writes hidden = (hidden || navHidden); restore original 3-state.
					if (navHidden) hidden = false;
					page.setHidden(hidden);
				}
				if (StringUtils.isNotEmpty(hiddenFromNavigationString)) {
					page.setHiddenFromNavigation(navHidden);
				}

				// Carry over the custom CSS sheet if present. These are of the form
				// "/group/SITEID/LB-CSS/whatever.css", so we need to map the SITEID
                String cssSheet = pageElement.getAttribute("csssheet");
                if (StringUtils.isNotEmpty(cssSheet)) {
                    String newCss = cssSheet.replace("/group/" + fromSiteId + "/", "/group/" + siteId + "/");
                    page.setCssSheet(newCss);
                }

				// Save or update the page
				if ( reused ) {
					List<String>elist = new ArrayList<>();
					boolean requiresEditPermission = true;
					simplePageToolDao.update(page, elist, messageLocator.getMessage("simplepage.nowrite"), requiresEditPermission);
					log.debug("updated page: {}", page.getPageId());
				} else {
					simplePageToolDao.quickSaveItem(page);
					log.debug("saving page: {}", page.getPageId());
				}
				if (StringUtils.isNotEmpty(gradebookPoints)) {
					try {
						gradebookIfc.addExternalAssessment(Arrays.asList(siteId), siteId, "lesson-builder:" + page.getPageId(), null,
								title, Double.valueOf(gradebookPoints), null, LessonBuilderConstants.TOOL_ID);
					} catch(ConflictingAssignmentNameException cane){
						log.error("merge: ConflictingAssignmentNameException for title {}.", title);
					}
				}
				log.debug("Adding page to pageElement and pageMap {} => {}", oldPageId, page.getPageId());
				pageMap.put(oldPageId, page.getPageId());
				pageElementMap.put(oldPageId, pageElement);
			}

			log.debug("Starting second pass over pages ({}) {} to create items", pageElementMap.size(), pageMap);

			// Process pages we inserted (in PageElementMap) to create the items
			boolean needFix = false;
			Map <Long,Long> itemMap = new HashMap<Long,Long>();
			for (Map.Entry<Long, Element> entry : pageElementMap.entrySet()) {
				Element pageElement = entry.getValue();

				if (mergePage(pageElement, oldServer, siteId, fromSiteId, pageMap, itemMap, entityMap, mcx)) needFix = true;
			}

			if (needFix) {
				site = siteService.getSite(siteId);
				ResourcePropertiesEdit rp = site.getPropertiesEdit();
				rp.addProperty("lessonbuilder-needsfixup", "2");
				siteService.save(site);
				// unfortunately in duplicate site, site-admin has the site open, so this doesn't actually do anything
				// site-manage will stomp on it. However it does work for the other import operations, which is where
				// we need it, since site manage will call the fixup itself for duplicate
			}

			log.debug("fixing items siteId: {} fromSiteId: {} oldServer: {} itemMap: {} pageMap: {}", siteId, fromSiteId, oldServer, itemMap, pageMap);
			for (Map.Entry<Long, Element> entry : pageElementMap.entrySet()) {
				Element pageElement = entry.getValue();

				fixItems(pageElement, oldServer, siteId, fromSiteId, pageMap, itemMap, mcx);
			}

			// Add necessary placements / top level pages to the left navigation.  If we
			// reused an existing placement / page we  don't re-add them
			// When we add a tool to the site, we need to fill in the tool id for
			// top level pages and set parents to null
			NodeList tools = root.getElementsByTagName("lessonbuilder");
			int numTools =  tools.getLength();
			log.debug("Reading {} lessonbuilder tool placements from archive", numTools);
			for (int i = 0; i < numTools; i++) {
				Node node = tools.item(i);
				if (node.getNodeType() != Node.ELEMENT_NODE) continue;

				// there's an element at top level with no attributes. ignore it
				Element element = (Element) node;
				String oldToolId = trimToNull(element.getAttribute("toolid"));
				if (StringUtils.isBlank(oldToolId)) continue;

				String toolTitle = trimToNull(element.getAttribute("name"));
				if(StringUtils.isBlank(toolTitle)) continue;

				if ( fullPlacements.containsKey(toolTitle) ) {
					log.debug("Placement already has content for {}", toolTitle);
					continue;
				}

				// Check if we are reusing a Sakai left nav placement or if we need to make a new one
				String sakaiPageId = emptySakaiIds.getOrDefault(toolTitle, null);
				SimplePage oldLessonsPage = null;
				SimplePageItem oldLessonsItem = null;
				log.debug("Looking for existing placement for {} = {}", toolTitle, sakaiPageId);
				if ( ! isTransferCopy && sakaiPageId != null ) {
					Long l = simplePageToolDao.getTopLevelPageId(sakaiPageId);
					oldLessonsPage = simplePageToolDao.getPage(l);
					oldLessonsItem = simplePageToolDao.findTopLevelPageItemBySakaiId(String.valueOf(l));
					log.debug("Finding page {} {} item {} for {}", l, oldLessonsPage, oldLessonsItem, sakaiPageId);
				} else {
					log.debug("Creating new placement for {}", toolTitle);
					// Now we need to add a new left nav placement and link it to the root
					// page for the newly imported pages
					String rolelist = element.getAttribute("functions.require");
					String pagePosition = element.getAttribute("pagePosition");
					String pageVisibility = element.getAttribute("pageVisibility");

					// Time to add the left nav placement
					SitePage sakaiPage = site.addPage();
					ToolConfiguration toolConfig = sakaiPage.addTool(LessonBuilderConstants.TOOL_ID);
					if (StringUtils.isNotBlank(pagePosition)) {
						int integerPosition = Integer.parseInt(pagePosition);
						sakaiPage.setPosition(integerPosition);
					}
					log.debug("Added Lessons placement toolTitle={} new sakaiPage={} new tool={} to site", toolTitle, sakaiPage.getId(), toolConfig.getId());

					sakaiPageId = toolConfig.getPageId();
					if (sakaiPageId == null) {
						log.error("unable to find new sakaiPageId for copy of {}", toolTitle);
						continue;
					}

					if (StringUtils.isNotBlank(rolelist)) {
						toolConfig.getPlacementConfig().setProperty("functions.require", rolelist);
					}
					if (StringUtils.isNotBlank(pageVisibility)) {
						toolConfig.getPlacementConfig().setProperty(ToolManager.PORTAL_VISIBLE, pageVisibility);
					}
					toolConfig.setTitle(toolTitle);
					sakaiPage.setTitle(toolTitle);
					sakaiPage.setTitleCustom(true);
					log.debug("saving site {}", site.getId());
					siteService.save(site);
					count++;
				}

				// now fix up the lessons page. new format has it as attribute
				String oldPageId = trimToNull(element.getAttribute("pageId"));  // Case is correct
				if (oldPageId == null) {
					// old format. we should have a page node
					// normally just one
					Node pageNode = element.getFirstChild();
					if (pageNode == null || pageNode.getNodeType() != Node.ELEMENT_NODE) {
						log.error("page node not element");
						continue;
					}
					Element pageElement = (Element)pageNode;
					oldPageId = trimToNull(pageElement.getAttribute("pageid"));  // Case is correct
				}
				if (oldPageId == null) {
					log.error("page node without old pageid");
					continue;
				}

				Long newPageId = pageMap.get(Long.valueOf(oldPageId));
				log.debug("oldPageId: {} loading new pageId: {}", oldPageId, newPageId);

				// fix up the new copy of the page to be top level
				SimplePage simplePage = simplePageToolDao.getPage(newPageId);
				if (simplePage == null) {
					log.error("can't find new copy of top level page {}", newPageId);
					continue;
				}

				simplePage.setParent(null);
				simplePage.setTopParent(null);
				simplePage.setToolId(sakaiPageId);
				simplePageToolDao.quickUpdate(simplePage);
				log.debug("updated top level lessons page: {} to point to site nav page: {}", newPageId, sakaiPageId);

				// See if we can load the existing and empty page - if not we create it
				SimplePageItem item = simplePageToolDao.findTopLevelPageItemBySakaiId(String.valueOf(newPageId));
				boolean itemExists = true;
				log.debug("findTopLevelPageItemBySakaiId {} item {}", newPageId, item);
				if ( item == null ) {
					log.debug("creating vestigial item for top level page: {} type: {}", newPageId, SimplePageItem.PAGE);
					item = simplePageToolDao.makeItem(0, 0, SimplePageItem.PAGE, Long.toString(newPageId), simplePage.getTitle());
					itemExists = false;
				}

				// Revise the top level page's SimplePageItem from the import xml
				Element pageElement = pageElementMap.get(Long.valueOf(oldPageId));
				String pageAttribute = pageElement.getAttribute("required");
				if (StringUtils.isNotEmpty(pageAttribute)) {
					item.setRequired(Boolean.valueOf(pageAttribute));
				}
				pageAttribute = pageElement.getAttribute("prerequisite");
				if (StringUtils.isNotEmpty(pageAttribute)) {
					item.setPrerequisite(Boolean.valueOf(pageAttribute));
				}

				log.debug("saving vestigial item: {}", item.getId());
				simplePageToolDao.quickSaveItem(item);
				if (!itemExists) {
					List<String>elist = new ArrayList<>();
					boolean requiresEditPermission = true;
					simplePageToolDao.update(item, elist, messageLocator.getMessage("simplepage.nowrite"), requiresEditPermission);
					log.debug("updated vestigial item page: {}", item.getId());
				}

				// Include the final configuration for the placement in the results
				Long postPageKey = simplePageToolDao.getTopLevelPageId(sakaiPageId);
				SimplePage postPage = null;
				SimplePageItem postItem = null;
				if ( postPageKey != null ) {
					postPage = simplePageToolDao.getPage(postPageKey);
					postItem  = simplePageToolDao.findTopLevelPageItemBySakaiId(String.valueOf(postPageKey));
				}

				String result = "Placement "+sakaiPageId+" postPageKey "+postPageKey+
					" page "+(postPage == null ? null : postPage.getPageId())+
					" item "+(postItem == null ? null : postItem.getId());
				log.debug(result);
				results.append(result);
			}
			results.append("merging lessonbuilder tool " + siteId + " (" + count + ") items.\n");
		}
		catch (DOMException e)
		{
			log.error(e.getMessage(), e);
			results.append("merging " + getLabel()
					+ " failed during xml parsing.\n");
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
			results.append("merging " + getLabel() + " failed.\n");
		}

		return results.toString();

	} // merge

	@Override
	public boolean parseEntityReference(String reference, Reference ref)
	{
		int i = reference.indexOf("/", 1);
		if (i < 0)
			return false;
		String type = reference.substring(1, i);
		if (!type.equals("lessonbuilder"))
			return false;
		String id = reference.substring(i);
		i = id.indexOf("/", 1);
		if (i < 0)
			return false;
		type = id.substring(1, i);
		String numstring = id.substring(i+1);
		i = numstring.indexOf("/");
		if (i >= 0)
			numstring = numstring.substring(0, i);

		// needed for CC upload
		if (type.equals("site")) {
			ref.set("sakai:lessonbuilder", "site", id, null, id);
			return true;
		}

		if (!type.equals("item")) {
			if (type.equals("page")) {
				long num = 0;
				try {
					num = Long.parseLong(numstring);
				} catch (Exception e) {
					return false;
				}
				SimplePage page = simplePageToolDao.getPage(num);
				if (page == null) {
					return false;
				}
				ref.set("sakai:lessonbuilder", "page", id, null, page.getSiteId());
				return true;
			}
			return false;
		}

		long num = 0;
		try {
			num = Long.parseLong(numstring);
		} catch (Exception e) {
			return false;
		}
		SimplePageItem item = simplePageToolDao.findItem(num);
		if (item == null) {
			return false;
		}
		SimplePage page = simplePageToolDao.getPage(item.getPageId());
		if (page == null) {
			return false;
		}

		ref.set("sakai:lessonbuilder", "item", id, null, page.getSiteId());

		// not for the moment
		return true;
	}

	public String trimToNull(String value)
	{
		if (value == null) return null;
		value = value.trim();
		if (value.length() == 0) return null;
		return value;
	}

	public boolean willArchiveMerge()
	{
		return true;
	}

	@Override
	public List<Map<String, String>> getEntityMap(String fromContext) {
		// Get orphan finder to identify problematic pages
		SimplePageBean simplePageBean = makeSimplePageBean(fromContext);
		OrphanPageFinder orphanFinder = simplePageBean.getOrphanFinder(fromContext);
		
		// Find pages referenced by items, but only from valid (non-orphan) pages
		Map<Long, List<Long>> referencedPages = findReferencedPagesByItems(fromContext);
		Set<Long> validReferencedPageIds = new HashSet<>();
		
		for (Map.Entry<Long, List<Long>> entry : referencedPages.entrySet()) {
			Long referencingPageId = entry.getKey();
			// Only include references from pages that are not orphans
			if (!orphanFinder.isOrphan(referencingPageId)) {
				validReferencedPageIds.addAll(entry.getValue());
			}
		}

		List<SimplePage> sitePages = simplePageToolDao.getSitePages(fromContext);
		if (sitePages == null || sitePages.isEmpty()) {
			return Collections.emptyList();
		}

		return sitePages.stream()
			.filter(p -> {
				// Include page if it's not an orphan OR if it's referenced by a valid (non-orphan) page
				boolean isOrphan = orphanFinder.isOrphan(p.getPageId());
				boolean isValidlyReferenced = validReferencedPageIds.contains(p.getPageId());
				return !isOrphan || isValidlyReferenced;
			})
			.map(p -> {
				String title = p.getTitle();
				return Map.of("id", Long.toString(p.getPageId()), "title", title);
			})
			.collect(Collectors.toList());
	}

	@Override
	public String getToolPermissionsPrefix() {
		return SimplePage.PERMISSION_LESSONBUILDER_PREFIX;
	}

	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options) {
		return transferCopyEntitiesImpl(fromContext, toContext, ids, false);
	}

	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options, boolean cleanup) {
		return transferCopyEntitiesImpl(fromContext, toContext, ids, cleanup);
	}

	public Map<String,String> transferCopyEntitiesImpl(String fromContext, String toContext, List ids, boolean cleanup)
	{
		Map<String,String> entityMap = new HashMap<String,String>();

		try {

			if(cleanup == true) {
				Site toSite = siteService.getSite(toContext);

				List<SitePage> toSitePages = toSite.getPages();
				if (toSitePages != null && !toSitePages.isEmpty()) {
					Vector<String> removePageIds = new Vector<>();
					for (SitePage currPage : toSitePages) {
						List<String> toolIds = myToolList();
						List<ToolConfiguration> toolList = currPage.getTools();
						for (ToolConfiguration toolConfig : toolList) {
							if (toolIds.contains(toolConfig.getToolId())) {
								removePageIds.add(toolConfig.getPageId());
							}
						}
					}
					for (String removeId : removePageIds) {
						SitePage sitePage = toSite.getPage(removeId);
						toSite.removePage(sitePage);
					}

				}
				siteService.save(toSite);
				ToolSession session = sessionManager.getCurrentToolSession();

				if (session != null && session.getAttribute(ATTR_TOP_REFRESH) == null) {
					session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);
				}

				SimplePageBean simplePageBean = makeSimplePageBean(fromContext);
				List<SimplePage> sitePages = simplePageToolDao.getSitePages(toContext);
				if (sitePages != null && !sitePages.isEmpty()) {
					for (SimplePage page: sitePages)
						simplePageBean.deletePage(toContext, page.getPageId());
				}

			}

			log.debug("lesson builder transferCopyEntities");
			Document doc = Xml.createDocument();
			Stack stack = new Stack();
			Time now = timeService.newTime();
			Element root = doc.createElement("archive");
			doc.appendChild(root);
			root.setAttribute("source", fromContext);
			root.setAttribute("server", ServerConfigurationService.getServerId());
			root.setAttribute("serverurl", ServerConfigurationService.getServerUrl());
			root.setAttribute("date", now.toString());
			root.setAttribute("system", "sakai");

			stack.push(root);

			if (ids != null && !ids.isEmpty()) {
				List<String> stringIds = new ArrayList<>();
				for (Object id : ids) {
					stringIds.add(id.toString());
				}
				archive(fromContext, doc, stack, "/tmp/archive", null, stringIds);
			} else {
				archive(fromContext, doc, stack, "/tmp/archive", null);
			}

			stack.pop();

			MergeConfig mcx = new MergeConfig();
			mcx.creatorId = sessionManager.getCurrentSessionUserId();
			mcx.archiveContext = fromContext;
			mcx.archiveServerUrl = ServerConfigurationService.getServerUrl();
			boolean isTransferCopy = true;
			mergeInternal(toContext,  (Element)doc.getFirstChild().getFirstChild(), "/tmp/archive", fromContext, mcx, entityMap, isTransferCopy);

			ToolSession session = sessionManager.getCurrentToolSession();

			if (session != null && session.getAttribute(ATTR_TOP_REFRESH) == null) {
				session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);
			}

			// We've done the fixups but still need to do group adjustments
			Site site = siteService.getSite(toContext);
			ResourcePropertiesEdit rp = site.getPropertiesEdit();
			rp.removeProperty("lessonbuilder-needsfixup");
			siteService.save(site);
			// unfortunately in duplicate site, site-admin has the site open, so this doesn't actually do anything
			// site-manage will stomp on it. So we need a different way to say that group fixup is needed

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		try {
			Site toSite = siteService.getSite(toContext);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		// set this flag for the group update, which we really do need in duplicate
		simplePageToolDao.setNeedsGroupFixup(toContext, 2);

		return entityMap;

	}

	// update our references to Sakai objects that live in other tools. ID numbers in new site
	// will of course be different than in the old site

	// map has entities for all objects. Look for all entities that look like /ref/lessonbuilder.
	// this is mapping from LB item id to underlying object in old site.
	// find the object in the new site and fix up the item id
    public void updateEntityReferences(String toContext, Map<String, String> transversalMap) {

        migrateEmbeddedLinks(toContext, transversalMap);
        // update lessonbuilder_ref property of groups and kill bogus groups
        Map<String,String> mapGroups = new HashMap<String,String>();
		for (Map.Entry<String,String> entry: transversalMap.entrySet()) {
			String entityid = entry.getKey();
			String objectid = entry.getValue();
			if (entityid == null || objectid == null || !entityid.startsWith(REF_LB))
				continue;

			LessonEntity e = null;
			String itemstring = null;
			if (entityid.startsWith(REF_LB_ASSIGNMENT)) {
				e = assignmentEntity;
				itemstring = entityid.substring(REF_LB_ASSIGNMENT.length());
			} else if (entityid.startsWith(REF_LB_ASSESSMENT)) {
				e = quizEntity;
				itemstring = entityid.substring(REF_LB_ASSESSMENT.length());
			} else {
				e = forumEntity;
				itemstring = entityid.substring(REF_LB_FORUM.length());
			}

			// find the object in the new site. There are two approaches:
			// if we're lucky, we find it in the traveralMap. That's built by Sakai, and maps objects in
			//   the old site to objects in the new site.
			// this uses the alt field, which for these item types contains an object ID such as assignment/ID/TITLE
			// findObject them asks the tool to find that object in the new site. Obviously it's the title we use,
			// since the ID will be different in the new site. Of course if the object is in the tranversalMap, we use
			// that, but not all tools make entries in the map.

			String sakaiid = e.findObject(objectid, transversalMap, toContext);
			if (sakaiid != null) {
				long itemid = -1;
				try {
					itemid = Long.parseLong(itemstring);
				} catch (Exception ignore) {}
				SimplePageItem item = simplePageToolDao.findItem(itemid);
				if (item != null) {
					String oldSakaiId = item.getSakaiId();
					if (oldSakaiId.equals(SimplePageItem.DUMMY)) {
						mapGroups.put(item.getAlt(), sakaiid);
					} else if (!oldSakaiId.equals(sakaiid)) {
						mapGroups.put(oldSakaiId, sakaiid);
					}
					item.setSakaiId(sakaiid);
					simplePageToolDao.quickUpdate(item);
				}
			}
		}

		simplePageToolDao.setNeedsGroupFixup(toContext, 1);

		// Also remap any per-page custom CSS selections using the transversalMap produced by
		// the overall site copy. When Resources renames files (e.g., to avoid collisions), the
		// CSS file path saved on the page can point to the old ID and the UI will fall back to
		// default CSS. If we find a mapping for the CSS resource, update the page to the new ID.
		try {
				if (transversalMap != null && !transversalMap.isEmpty()) {
						List<SimplePage> pages = simplePageToolDao.getSitePages(toContext);
						if (pages != null && !pages.isEmpty()) {
								for (SimplePage page : pages) {
										String css = page.getCssSheet();
										if (css == null || css.isEmpty()) continue;

										// transversalMap keys use ContentHostingService resource IDs (e.g., "/group/...").
										String keyGroup = css; // e.g., /group/SITEID/LB-CSS/custom.css

										String mapped = transversalMap.get(keyGroup);
										if (mapped != null && !mapped.isEmpty()) {
												// Ensure we store a CHS resource ID (strip leading "/content" if present)
												String newId = mapped.startsWith("/content") ? mapped.substring("/content".length()) : mapped;

												if (!newId.equals(css)) {
														page.setCssSheet(newId);
														simplePageToolDao.quickUpdate(page);
														log.debug("Remapped Lessons CSS for page {} from {} to {}", page.getPageId(), css, newId);
												}
										}
								}
						}
				}
		} catch (Exception e) {
				log.warn("Problem remapping Lessons CSS selections during site copy: {}", e);
		}

	}

	private void migrateEmbeddedLinks(String toContext, Map<String, String> transversalMap){
		Set entrySet = (Set) transversalMap.entrySet();
		// findTextItemsInSite accesses the raw database.
		// I'm concerned that updates may have been made and not written to the database.
		// I considered moving findTextItemsInSite to using real hibernate objects.
		// But we did have a massave failure one time
		simplePageToolDao.flush();
		simplePageToolDao.clear();
		List<SimplePageItem> items = simplePageToolDao.findTextItemsInSite(toContext);
		for (SimplePageItem item: items) {
			String msgBody = item.getHtml();
			if (msgBody == null) {
				continue;
			}
			try {
				String newBody = linkMigrationHelper.migrateAllLinks(entrySet, msgBody);

				if (!msgBody.equals(newBody)) {
					// items in findTextItemsInSite don't come from hibernate, so we have to get a real one
					SimplePageItem i = simplePageToolDao.findItem(item.getId());
					if (i != null) {
						i.setHtml(newBody);
						log.debug("html - (post mod): {}", msgBody);
						simplePageToolDao.quickUpdate(i);
					}
				}
			} catch (Exception e) {
				log.warn("Problem migrating links in Lessonbuilder {}", e.toString());
			}
		}
	}

	// called from tool, to fix up all dummy references in site toContext if possible
	public void updateEntityReferences(String toContext) {
		List<SimplePageItem> dummyItems = simplePageToolDao.findDummyItemsInSite(toContext);
		Map<String, String> entityMap = new HashMap<String, String>();

		// find list of dummy items and and objects, for fixup

		for (SimplePageItem item: dummyItems) {
			String entityid = null;
			int type = item.getType();

			if (type == SimplePageItem.ASSIGNMENT)
				entityid = REF_LB_ASSIGNMENT + item.getId();
			else if (type == SimplePageItem.ASSESSMENT)
				entityid = REF_LB_ASSESSMENT + item.getId();
			else
				entityid = REF_LB_FORUM + item.getId();
			entityMap.put(entityid, item.getAlt());
		}

		// now do the fixups
		updateEntityReferences(toContext, entityMap);

	}


	// fixup access control groups. They have a group property pointing to the
	// object that they control. That needs to be updated to the object in
	// the new site or we'll get duplicate groups. Remove any groups with that
	// property where there's no object in the new site. Presumably those are
	// some kind of leftover from confusion. Since there's no matching object
	// they can't be useful for anything.

	// it should be OK to call this more than once, though we try not to

	public void fixupGroupRefs (String toContext, SimplePageBean simplePageBean, int fixupType) {
		// fixup groups

		Map<String, String> objectMap = simplePageToolDao.getObjectMap(toContext);

		Site site = null;
		if (fixupType == 2) {
			try {
				site = siteService.getSite(toContext);
			} catch (Exception e) {
				log.error("can't get site {} {}", toContext, e.toString());
				return;
			}

			List<Group>delGroups = new ArrayList<Group>();
			Collection<Group> allGroups = site.getGroups();
			for (Group group: allGroups) {
				String groupRef = group.getProperties().getProperty("lessonbuilder_ref");
				if (groupRef != null) {
					String newGroupRef = objectMap.get(groupRef);
					if (newGroupRef != null && newGroupRef.length() > 1) {
						group.getPropertiesEdit().addProperty("lessonbuilder_ref", newGroupRef);
					} else if (newGroupRef == null) {
						delGroups.add(group);
					} else {
					}

					// newGroupRef "" is if the group is already a new one. leave it alone
				}
			}

			for (Group group: delGroups) {
				try {
					site.deleteGroup(group);
				} catch (AuthzRealmLockException arle) {
					log.warn("GROUP LOCK REGRESSION: {}", arle.getMessage(), arle);
				}
			}
			try {
				siteService.save(site);
			} catch (Exception e) {
				log.warn("unable to save set to upgrade groups", e);
			}

		}

		// now make sure none of the tools are resstricted to any of our groups
		// and then add back a clean restriction

		Set<String> sakaiIds = new HashSet<String>();
		for (Map.Entry<String,String> entry: objectMap.entrySet()) {
			if ("".equals(entry.getValue()))
				sakaiIds.add(entry.getKey());
		}


		for (String sakaiId: sakaiIds) {

			if (fixupType == 2) {

				LessonEntity lessonEntity = null;
				lessonEntity = assignmentEntity.getEntity(sakaiId);
				if (lessonEntity == null)
					lessonEntity = quizEntity.getEntity(sakaiId);
				if (lessonEntity == null)
					lessonEntity = forumEntity.getEntity(sakaiId);
				// remove any of our access control groups.
				if (lessonEntity != null) {
					Collection<String> groupIds = lessonEntity.getGroups(true);
					List<String> okIds = new ArrayList<String>();
					if (groupIds != null && groupIds.size() > 0) {
						boolean changed = false;
						for (String groupId: groupIds) {
							Group group = site.getGroup(groupId);
							if (group.getProperties().getProperty("lessonbuilder_ref") != null ||
									group.getTitle().startsWith("Access: ")) {
								changed = true;
								continue;
									}
							okIds.add(groupId);
						}
						if (changed)
							lessonEntity.setGroups(okIds);
					}
					SimplePageGroup group = simplePageToolDao.findGroup(sakaiId);
					if (group != null)
						simplePageToolDao.deleteItem(group);
				}
				// we've now removed anything that checkControlGroup would have done
			} // end fixuptype == 2

			// if items using this group are controlled, put control group back
			List<SimplePageItem> items = simplePageToolDao.findItemsBySakaiId(sakaiId);
			for (SimplePageItem item: items) {
				if (item.isPrerequisite()) {
					String sid = item.getSakaiId();
					if (!sid.equals(SimplePageItem.DUMMY) && !sid.startsWith("/sam_core/"))
						simplePageBean.checkControlGroup(item, true);
				}
			}

		}

	}

	public void setToolManager(ToolManager s) {
		toolManager = s;
	}

	public void setSecurityService(SecurityService s) {
		securityService = s;
	}

	public void setSessionManager(SessionManager s) {
		sessionManager = s;
	}

	public void setSiteService(SiteService s) {
		siteService = s;
	}

	public void setContentHostingService(ContentHostingService s) {
		contentHostingService = s;
	}

	public void setSimplePageToolDao(Object dao) {
		simplePageToolDao = (SimplePageToolDao) dao;
	}

	public void setForumEntity (LessonEntity e) {
		forumEntity = (LessonEntity)e;
	}

	public void setQuizEntity (LessonEntity e) {
		quizEntity = (LessonEntity)e;
	}

	public void setAssignmentEntity (LessonEntity e) {
		assignmentEntity = (LessonEntity)e;
	}

	public void setBltiEntity (LessonEntity e) {
		bltiEntity = (LessonEntity)e;
	}

	public void setGradebookIfc(GradebookIfc g) {
		gradebookIfc = g;
	}

	public void setMemoryService(MemoryService m) {
		memoryService = m;
	}

	public void setMessageSource(MessageSource s) {
		messageSource = s;
	}

	public void setLtiService(LTIService s) {
		ltiService = s;
	}

	public void setTimeService(TimeService s) {
		timeService = s;
	}


	// sitestats support

	public boolean entityExists(String id) {
		return true;
	}

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	/**
	 * Return the associated common tool.id for this tool
	 *
	 * @return the tool id (example: "sakai.messages")
	 */
	public String getAssociatedToolId() {
		return LessonBuilderConstants.TOOL_ID;
	}

	public final static String[] EVENT_KEYS=
		new String[] {LessonBuilderEvents.PAGE_CREATE,
			LessonBuilderEvents.PAGE_READ,
			LessonBuilderEvents.PAGE_UPDATE,
			LessonBuilderEvents.PAGE_DELETE,
			LessonBuilderEvents.ITEM_CREATE,
			LessonBuilderEvents.ITEM_READ,
			LessonBuilderEvents.ITEM_UPDATE,
			LessonBuilderEvents.ITEM_DELETE,
			LessonBuilderEvents.COMMENT_CREATE,
			LessonBuilderEvents.COMMENT_UPDATE,
			LessonBuilderEvents.COMMENT_DELETE};

	/**
	 * Return an array of all the event keys which should be tracked for statistics
	 *
	 * @return an array if event keys (example: "message.new" , "message.delete")
	 */
	public String[] getEventKeys() {
		return EVENT_KEYS;
	}

	/**
	 * OPTIONAL: return null if you do not want to implement this<br/>
	 * Return the event key => event name map for a given Locale,
	 * allows the author to create human readable i18n names for their event keys
	 *
	 * @param locale the locale to return the names for
	 * @return the map of event key => event name (example: for a 'en' locale: {"message.new","A new message"}) OR null to use the event keys
	 */
	public Map<String, String> getEventNames (Locale locale) {
		Map<String, String> localeEventNames = new HashMap<String, String>();
		ResourceLoader msgs = new ResourceLoader("lessons-events");
		msgs.setContextLocale(locale);
		for(int i=0; i<EVENT_KEYS.length; i++) {
			localeEventNames.put(EVENT_KEYS[i], msgs.getString(EVENT_KEYS[i]));
		}
		return localeEventNames;
	}

	//
	// Code to fix up URLs in HTML. Use the kernel service once KNL-737 is implemented.
	// The code here is copied from the patch.
	//

	public class ContentCopyContext {
		String oldSiteId;
		String newSiteId;
		String oldServer;
		ContentCopyContext (String oldId, String newId, String oldServer) {
			oldSiteId = oldId;
			newSiteId = newId;
		}
		String getOldSiteId () {
			return oldSiteId;
		}
		String getNewSiteId () {
			return newSiteId;
		}
		String getOldServer () {
			return oldServer;
		}
	}

	private String convertHtmlContent(ContentCopyContext context, String content, String contentUrl, Map<Long,Long> itemMap) {
		// this old code (below) seems to have come from an old version of the kerne's reference migrator.
		// It's too complex for me to verify that it's right.
		// At this point the kernel just does string replacements. So I'm going to
		// replace NNN with the new value
		// and also fix up the dummy references.

		String oldSiteReference = context.getOldSiteId();
		String newSiteReference = context.getNewSiteId();
		content = content.replaceAll("\\b" + Pattern.quote(oldSiteReference) + "\\b", newSiteReference);

		// no point doing this code unless we actually have a dummy url in it
		if (content.indexOf(ITEMDUMMY) >= 0) {

			StringBuffer newcontent = new StringBuffer();
			Matcher matcher = dummyPattern.matcher(content);
			while (matcher.find()) {
				String itemnumber = matcher.group().substring(ITEMDUMMY.length());
				long oldItem = 0;
				try {
					oldItem = Long.parseLong(itemnumber);
					Long newItem = itemMap.get(oldItem);
					if (newItem != null)
						matcher.appendReplacement(newcontent, ITEMDUMMY + newItem);
				} catch (Exception e) {
				}
			}
			matcher.appendTail(newcontent);
			content = newcontent.toString();
		}

		return content;

	}

	final int ITEMDUMMYLEN = ITEMDUMMY.length();

	/* support for /direct.
	   For the moment the only operation is loading a Common Cartridge file.
	   This is a particularly horrendous operation.
	   */

	/* PUT request will send a ZIP file for Cc input.
	   return the input stream so we can load it */
	public Object translateFormattedData(EntityReference ref, String format, InputStream input, Map<String, Object> params) {
		return input;
	}

	/* It's a ZIP file, but the newer formats call it imscc, which probably browsers won't recognize */
	public String[] getHandledInputFormats() {
		return null;
	}

	/* this shouldn't be used, because we override translate */
	public Object getSampleEntity() {
		return null;
	}

	// the real MessageLocator won't work except in an RSAC session, which we can't reasonably create
	// this is a reasonable fake, given that we have no way to get a locale

	public class MyMessageLocator extends MessageLocator {
		public String getMessage(String[] code, Object[] args) {
			if (code != null) {
				for (String s: code) {
					try {
						return messageSource.getMessage(s, args, Locale.getDefault());
					} catch (Exception e) {
						// message not found, one presumes
					}
				}
				// if none found, just use the code
				return code[0];
			} else
				return "";
		}
	}

	MyMessageLocator messageLocator = new MyMessageLocator();

	// sakai.session is the JSESSIONID up to the period.
	// curl -F"cartridge=@/users/sakai/IMS-tests-v1.1/cc1p1vtd01v1p0.imscc;type=application/zip" "http://heidelberg.rutgers.edu/direct/lessonbuilder?site=b51a66b4-a574-489c-8e88-81024d32436e&sakai.session=510667cc-1df2-41d1-98b1-491f57f22c46"

	// the challenge here is that we're not in a request context, but a lot of the support code assumes we are
	// we have to fake up a fair amount of context

	/* create */
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		DiskFileItem cartridge = (DiskFileItem)params.get("cartridge");
		String siteId = (String)params.get("site");

		return loadCartridge(cartridge.getStoreLocation(), null, siteId);
	}

	public String loadCartridge(File cartFile, String unzippedDir, String siteId) {
		if ((cartFile == null && unzippedDir == null) || siteId == null)
			return "missing arguments " + cartFile + " " + siteId;

		String siteref = "/site/" + siteId;
		if (! securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_UPDATE, siteref))
			return "Need lessonbuilder update permission";

		// fake  up a tool session
		Session ses = sessionManager.getCurrentSession();
		ToolSession toolSession = null;
		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (Exception e) {
			return "bad site ID";
		}

		boolean found = false;
		String dummyPageId = null;

		// find a lesson builder tool to use for the tool session.
		// So there must be one entity already.
		Collection<ToolConfiguration> toolConfs = site.getTools(myToolIds());
		if (toolConfs != null && !toolConfs.isEmpty())  {
			for (ToolConfiguration config: toolConfs) {
				if (config.getToolId().equals(LessonBuilderConstants.TOOL_ID)) {
					// this stuff copied from a JSP to load Samigo assessments.
					// I need at least some of it, but I don't guarantee that
					// all of this code works.
					toolSession = ses.getToolSession(config.getId());
					sessionManager.setCurrentToolSession(toolSession);
					ThreadLocalManager.set("sakai:ToolComponent:current.placement", config);
					ThreadLocalManager.set("sakai:ToolComponent:current.tool", config.getTool());
					found = true;
					break;
				}
			}
		}

		if (!found) {
			SitePage page = site.addPage();
			ToolConfiguration tool = page.addTool(LessonBuilderConstants.TOOL_ID);
			tool.setTitle("dummy lesson");
			page.setTitle("dummy lesson");
			try {
				siteService.save(site);
				// don't set until we know the save worked
				dummyPageId = page.getId();
			} catch (Exception e) {
				log.info("can't add dummy page to site {} {}", siteId, e.toString());
			}
			toolSession = ses.getToolSession(tool.getId());
			sessionManager.setCurrentToolSession(toolSession);
			ThreadLocalManager.set("sakai:ToolComponent:current.placement", tool);
			ThreadLocalManager.set("sakai:ToolComponent:current.tool", tool.getTool());
		}

		// this is loosely based on SimplePageBean.importcc

		File root = null;
		try {
			root = File.createTempFile("ccloader", "root");
			if (root.exists()) {
				if (!root.delete())
					return "unable to delete temp file";
			}
			if (!root.mkdir())
				return "unable to make temp directory for load";

			CartridgeLoader cartridgeLoader = null;
			if (unzippedDir != null)
				cartridgeLoader = ZipLoader.getUtilities(unzippedDir);
			else
				cartridgeLoader = ZipLoader.getUtilities(cartFile, root.getCanonicalPath());

			Parser parser = Parser.createCartridgeParser(cartridgeLoader);

			// fake up a SimplePageBean. Set up just enough state to let it do the import

			SimplePageBean simplePageBean = makeSimplePageBean(siteId);

			toolSession.removeAttribute("lessonbuilder.errors");

			parser.parse(new PrintHandler(simplePageBean, cartridgeLoader, simplePageToolDao, quizEntity, forumEntity, bltiEntity, assignmentEntity, false));

			List <String> errors = simplePageBean.errMessages();
			if (errors == null)
				return "ok";

			String ret = "";
			for (String e:errors)
				ret = ret + e + "\n";
			return ret;

		} catch (Exception e) {
			log.info("exception in createentity {} {}", siteId, e.toString());
			return "exception in createentity " + siteId + " " + e.toString();
		} finally {
			try {
				deleteRecursive(root);
			} catch (Exception e){
				return "unable to delete temp file " + root;
			}
			// if we had to create a dummy lesson, remove it
			if (dummyPageId != null) {
				try {
					// safest to get fresh copy of site
					site = siteService.getSite(siteId);
					List<SitePage> pages = site.getPages();
					for (SitePage page: pages) {
						if (dummyPageId.equals(page.getId())) {
							site.removePage(page);
							siteService.save(site);
							break;
						}
					}
				} catch (Exception e){
					return "unable to delete dummy lesson " + e;
				}
			}
		}

	}

	public String deleteOrphanPages(String siteId) {
		SimplePageBean spb = makeSimplePageBean(siteId);
		return spb.deleteOrphanPagesInternal();
	}

	SimplePageBean makeSimplePageBean(String siteId) {
		SimplePageBean simplePageBean = new SimplePageBean();
		simplePageBean.setMessageLocator(messageLocator);
		simplePageBean.setToolManager(toolManager);
		simplePageBean.setSecurityService(securityService);
		simplePageBean.setSessionManager(sessionManager);
		simplePageBean.setSiteService(siteService);
		simplePageBean.setContentHostingService(contentHostingService);
		simplePageBean.setSimplePageToolDao(simplePageToolDao);
		simplePageBean.setForumEntity(forumEntity);
		simplePageBean.setQuizEntity(quizEntity);
		simplePageBean.setAssignmentEntity(assignmentEntity);
		simplePageBean.setBltiEntity(bltiEntity);
		simplePageBean.setGradebookIfc(gradebookIfc);
		simplePageBean.setCurrentSiteId(siteId);
		return simplePageBean;
	}

	public boolean deleteRecursive(File path) throws FileNotFoundException{
		if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
		boolean ret = true;
		if (path.isDirectory()){
			for (File f : path.listFiles()){
				ret = ret && deleteRecursive(f);
			}
		}
		return ret && path.delete();
	}

	private long idFromRef (String reference, int length) {
		long id=-1;
		try {
			String[] refParts = reference.split(Entity.SEPARATOR);
			if (refParts.length == length) {
				id = Integer.parseInt(refParts[length-1]);
			}
		}
		catch (PatternSyntaxException p) {
			return id;
		}
		catch (NumberFormatException p) {
			return id;
		}
		return id;
	}

	//Seems like there should be a method for this, but is what most of the code does, lessons length is 4
	private long idFromRef (String reference) {
		return idFromRef(reference,4);
	}

	/**
	 * Parses an HTML content, extracts the URLs of the HTML content and transfer the missing files into the new site.
	 */
	private String transferAttachmentFiles(String msgBody, String oldSiteId, String newSiteId) {
		String replacedBody = msgBody;
		String serverURL = ServerConfigurationService.getServerUrl();
		String collectionPrefix = "/access/content/";
		if(StringUtils.isNotBlank(msgBody)) {
			org.jsoup.nodes.Document doc = Jsoup.parse(msgBody);

			Elements links = doc.select("a[href]");
			Elements media = doc.select("[src]");
			Elements imports = doc.select("link[href]");
			Set<String> references = new HashSet<>();
			// href ...
			for (org.jsoup.nodes.Element link : links) {
				references.add(link.attr("abs:href"));
			}

			// img ...
			for (org.jsoup.nodes.Element src : media) {
				references.add(src.attr("abs:src"));
			}

			// js, css, ...
			for (org.jsoup.nodes.Element link : imports) {
				references.add(link.attr("abs:href"));
			}

			for (String reference : references) {
				if (reference.contains(oldSiteId) && reference.contains(collectionPrefix)) {
					try {
						String oldReferenceId = reference;
						oldReferenceId = StringUtils.replace(oldReferenceId, collectionPrefix, "/");
						oldReferenceId = StringUtils.replace(oldReferenceId, serverURL, StringUtils.EMPTY);
						//Try secure and non-secure URLs too, for instances with a mix of configurations.
						oldReferenceId = StringUtils.replace(oldReferenceId, StringUtils.replace(serverURL, "https://", "http://"), StringUtils.EMPTY);
						oldReferenceId = StringUtils.replace(oldReferenceId, StringUtils.replace(serverURL, "http://", "https://"), StringUtils.EMPTY);
						String newReferenceId = oldReferenceId.replaceAll("\\b" + Pattern.quote(oldSiteId) + "\\b", newSiteId);

						// Avoid creating duplicates if Resources already copied the item
						boolean sourceFileExists = false;
						boolean targetFileExists = false;
						try {
							sourceFileExists = contentHostingService.getResource(oldReferenceId).getId() != null;
							targetFileExists = contentHostingService.getResource(newReferenceId).getId() != null;
						} catch(IdUnusedException e) {
							log.debug("Check for source {} and target file {} in site {}", sourceFileExists, targetFileExists, newSiteId);
						}
						if (sourceFileExists && !targetFileExists) {
							contentHostingService.copy(oldReferenceId, newReferenceId);
						}
						replacedBody = replacedBody.replaceAll("\\b" + Pattern.quote(oldSiteId) + "\\b", newSiteId);
					} catch(IdUnusedException ide) {
						log.warn("Warn transfering file from site {} to site {}.", oldSiteId, newSiteId, ide);
					} catch(Exception e) {
						log.error("Error transfering file from site {} to site {}.", oldSiteId, newSiteId, e);
					}
				}
			}
		}
		return replacedBody;
	}

	private String copyLTIContent(Map<String, Object> ltiContent, String siteId, String oldSiteId)
	{
		Object result = ltiService.copyLTIContent(ltiContent, siteId, oldSiteId);
		String sakaiId = null;
		if ( result == null ) {
			return null;
		} else if ( result instanceof Long ) {
			sakaiId = "/blti/" + result.toString();
		} else if ( result instanceof String ) {
			log.error("Could not insert content - {}", result);
		} else {
			log.debug("Adding LTI tool {}", result);
		}

		return sakaiId;
	}



}
