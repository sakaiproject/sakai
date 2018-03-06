/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool.service;

import java.io.InputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.fileupload.disk.DiskFileItem;

import org.springframework.context.MessageSource;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.EntityTransferrerRefMigrator;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Statisticable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.InputTranslatable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderEvents;
import org.sakaiproject.lessonbuildertool.LessonBuilderAccessAPI;
import org.sakaiproject.lessonbuildertool.ToolApi;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageGroup;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.cc.CartridgeLoader;
import org.sakaiproject.lessonbuildertool.cc.Parser;
import org.sakaiproject.lessonbuildertool.cc.PrintHandler;
import org.sakaiproject.lessonbuildertool.cc.ZipLoader;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.OrphanPageFinder;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Xml;
import org.sakaiproject.util.RequestFilter;

/**
 * @author hedrick
 * The goal is to get sites to save and copy. However there's actually no data 
 * involved in this tool. The only configuration is the URL, which is a tool
 * configuration property. That's handled separately in site.xml
 *
 */
@Slf4j
public class LessonBuilderEntityProducer extends AbstractEntityProvider
    implements EntityProducer, EntityTransferrer, EntityTransferrerRefMigrator, Serializable, 
	       CoreEntityProvider, AutoRegisterEntityProvider, Statisticable, InputTranslatable, Createable, ToolApi  {
   private static final String ARCHIVE_VERSION = "2.4"; // in case new features are added in future exports
   private static final String VERSION_ATTR = "version";
   private static final String NAME = "name";
   private static final String VALUE = "value";
   
   private static final String PROPERTIES = "properties";
   private static final String PROPERTY = "property";
   public static final String REFERENCE_ROOT = "/lessonbuilder";
   public static final String LESSONBUILDER_ID = "sakai.lessonbuildertool";
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
   public void setLessonBuilderAccessAPI(LessonBuilderAccessAPI l) {
       lessonBuilderAccessAPI = l;
   }


   private Set<String> servers;

    /* 
     * There are several types of updating when we move a lesson from one site to another:
     * Fixing HTML text:
     *  fixItems - during load, called on the XML structure for each page
     *    for each item object in the new page, if it's text, fixup the URLs (fixUrls)
     *  fixUrls - for a piece of HTML, fixup urls on it, with convertHtmlContent
     *  convertHtmlContent - for a piece of HTML, fixup urls on it
     *      finds all URLs in a text and calls processUrl
     *  processUrl
     *      for special dummy "http://lessonbuilder.sakaiproject.org/ITEMID update the ID
     *      for /access/content, etc, update site ID
     *      see migrateEmbeddedlinks below for updating references to other sakai objects
     *
     * Fixing sakaiid's so that Sakai items point to the assignment, test, etc. in the new site
     *   updateEntityReferences - called by Sakai as part of load with map of old and new references
     *          one-argument version called from tool to get anything that couldn't be done during load
     *      if the kernel supports migrateAllLinks, call migrateEmbeddedLinks
     *      look up the all items in the map, and update the sakaiId to the new assignment, test, etc, id
     *          Sakai supplies a map for objects in old site to new site. However not all tools support it
     *          the one-argument version constructs a map when the entry is an "objectid". This is returned
     *             from the tool-specific Lessons code, and may be different for assignments, test, quizes, etc.
     *             but normally the objectid uses the title of the object in the tool since titles of quizes, etc
     *             are unique in a given site. the update operation calls findobject in the tool-specific interface
     *             to locate the quiz with that title
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
    
   private Class linkMigrationHelper = null;
   private Method migrateAllLinks = null;
   private Object linkMigrationHelperInstance = null;
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

      // LinkMigrationHelper is not present before 2.10. So this code can compile on older systems,
      // find it via introspection.

      try {
	  linkMigrationHelper = RequestFilter.class.getClassLoader().loadClass("org.sakaiproject.util.api.LinkMigrationHelper");
	  // this is in the kernel, so it should already be loaded
	  linkMigrationHelperInstance = ComponentManager.get(linkMigrationHelper);
	  if (linkMigrationHelper != null)
	      migrateAllLinks = linkMigrationHelper.getMethod("migrateAllLinks", new Class[] { Set.class, String.class });
      } catch (Exception e) {
	  log.info("Exception in introspection " + e);
	  log.info("loader " + RequestFilter.class.getClassLoader());
      }

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

      // this slightly odd code is for testing. It lets us test by reloading just lesson builder.
      // otherwise we have to restart sakai, since the entity stuff can't be restarted
      if (false) {
	  SecurityAdvisor mergeAdvisor = new SecurityAdvisor() {
		  public SecurityAdvice isAllowed(String userId, String function, String reference) {
		      return SecurityAdvice.ALLOWED;
		  }
	      };

      try {
	  Document doc = Xml.createDocument();
	  Stack stack = new Stack();
	  Element root = doc.createElement("archive");
	  doc.appendChild(root);
	  root.setAttribute("source", "45d48248-ba23-4829-914a-7219c3ced2dd");
	  root.setAttribute("server", "foo");
	  root.setAttribute("date", "now");
	  root.setAttribute("system", "sakai");
      
	  stack.push(root);

	  archive("45d48248-ba23-4829-914a-7219c3ced2dd", doc, stack, "/tmp/archive", null);

	  stack.pop();
	  
	  Xml.writeDocument(doc, "/tmp/xmlout");

	  // we don't have an actual user at this point, so need to force checks to work
	  securityService.pushAdvisor(mergeAdvisor);

	  merge("0134937b-ce16-440c-80a6-fb088d79e5ad",  (Element)doc.getFirstChild().getFirstChild(), "/tmp/archive", "45d48248-ba23-4829-914a-7219c3ced2dd", null, null, null);
      } catch (Exception e) {
	  log.info(e.getMessage(), e);
      } finally {
	  securityService.popAdvisor(mergeAdvisor);
      }
      }

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

    // lessonbuilder allows new tools to be created that use lessonbuilder. They will have
    // different tool ID's. The best way to find them seems to be to look
    // for all tools that set "linktool" as a keyword. Perhaps I should cache
    // this value. However in theory it would be possible to dynamically add
    // tools. Note that the tools are loaded when LinkTool.class is loaded. That's
    // often after this class, so at init time these lists would be empty.
   
   /**
    * {@inheritDoc}
    */
   public String[] myToolIds()
   {
       String[] toolIds = {LESSONBUILDER_ID};
       return toolIds;
   }
   
   public List<String> myToolList()
   {
       List<String> toolList = new ArrayList<String>();
       toolList.add(LESSONBUILDER_ID);
       return toolList;
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

    protected void addPage(Document doc, Element element, SimplePage page, Site site) {

	long pageId = page.getPageId();

	Element pageElement = doc.createElement("page");

	addAttr(doc, pageElement, "pageid", new Long(pageId).toString());
	addAttr(doc, pageElement, "toolid", page.getToolId());
	addAttr(doc, pageElement, "siteid", page.getSiteId());
	addAttr(doc, pageElement, "title", page.getTitle());

	Long parent = page.getParent();
	if (parent != null)
	    addAttr(doc, pageElement, "parent", parent.toString());

	parent = page.getTopParent();
	if (parent != null)
	    addAttr(doc, pageElement, "topparent", parent.toString());

	addAttr(doc, pageElement, "hidden", page.isHidden() ? "true" : "false");
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
		addAttr(doc, itemElement, "id", new Long(item.getId()).toString());
		addAttr(doc, itemElement, "pageId", new Long(item.getPageId()).toString());
		addAttr(doc, itemElement, "sequence", new Integer(item.getSequence()).toString());
		addAttr(doc, itemElement, "type", new Integer(item.getType()).toString());
		addAttr(doc, itemElement, "sakaiid", item.getSakaiId());
		if (!item.getSakaiId().equals(SimplePageItem.DUMMY)) {
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
		// the Sakai ID is good enough for other object types
		addAttr(doc, itemElement, "name", item.getName());
		addAttr(doc, itemElement, "html", item.getHtml());
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


   /**
    * {@inheritDoc}
    */
   public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
   {
      //prepare the buffer for the results log
       StringBuilder results = new StringBuilder();

      // Orphaned pages need not apply!
       SimplePageBean simplePageBean = makeSimplePageBean(siteId);
       OrphanPageFinder orphanFinder = simplePageBean.getOrphanFinder(siteId);

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

	 List<SimplePage> sitePages = simplePageToolDao.getSitePages(siteId);
	 if (sitePages != null && !sitePages.isEmpty()) {
	     for (SimplePage page: sitePages) {
	       if (!orphanFinder.isOrphan(page.getPageId())) {
		 addPage(doc, lessonbuilder, page, site);
	       } else {
		 orphansSkipped++;
	       }
	     }
	 }

	 log.info("Skipped over " + orphansSkipped + " orphaned pages while archiving site " + siteId);

         Collection<ToolConfiguration> tools = site.getTools(myToolIds());
	 int count = 0;
         if (tools != null && !tools.isEmpty()) 
         {
	     for (ToolConfiguration config: tools) {
		 element = doc.createElement(LESSONBUILDER);

		 addAttr(doc, element, "toolid", config.getPageId());
		 addAttr(doc, element, "name" , config.getContainingPage().getTitle());

		 Properties props = config.getPlacementConfig();

		 String roleList = props.getProperty("functions.require");
		 if (roleList == null)
		     roleList = "";

		 addAttr(doc, element, "functions.require", roleList);
		 
		 // should be impossible for these nulls, but we've seen it
		 if (simplePageToolDao.getTopLevelPageId(config.getPageId()) != null)
		     addAttr(doc, element, "pageId", Long.toString(simplePageToolDao.getTopLevelPageId(config.getPageId())));
		 else
		     log.warn("archive site " + siteId + " tool page " + config.getPageId() + " null lesson");
		 // addPage(doc, element,  simplePageToolDao.getTopLevelPageId(config.getPageId()));
		 
		 lessonbuilder.appendChild(element);
		 count++;
	     }
            
	     results.append("archiving " + count + " LessonBuilder instances.\n");

         } 
         else 
         {
	     results.append("archiving no LessonBuilder instances.\n");
         }

         ((Element) stack.peek()).appendChild(lessonbuilder);
         stack.push(lessonbuilder);

         stack.pop();
      }
      catch (Exception any)
      {
         log.warn("archive: exception archiving service: " + any + " " +  serviceName());
      }

      stack.pop();

      return results.toString();
   }
   
   /**
    * {@inheritDoc}
    */
   public Entity getEntity(Reference ref)
   {
      // I don't see how there could be a reference of this kind
       return null;
   }

   /**
    * {@inheritDoc}
    */
   public Collection getEntityAuthzGroups(Reference ref, String userId)
   {
      //TODO implement this
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public String getEntityDescription(Reference ref)
   {
       // not needed
       return null;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.EntityProducer#getEntityResourceProperties(org.sakaiproject.entity.api.Reference)
    */
   public ResourceProperties getEntityResourceProperties(Reference ref) {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public String getEntityUrl(Reference ref)
   {
       //Just return a URL to the top of the page based on the item's pageId and toolId
       long id = idFromRef(ref.getReference());
       SimplePageItem item = simplePageToolDao.findItem(id);
       String URL = "";
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

   /**
    * {@inheritDoc}
    */
   public HttpAccess getHttpAccess()
   {
       // not for now
       return lessonBuilderAccessAPI.getHttpAccess();
   }

   /**
    * {@inheritDoc}
    */
   public String getLabel() {
       return LESSONBUILDER;
   }

    // the pages are already made. this adds the elements
    private boolean makePage(Element element, String oldServer, String siteId, String fromSiteId, Map<Long,Long> pageMap, Map<Long,Long> itemMap, Map<String,String> entityMap) {
  
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

       NodeList allChildrenNodes = element.getChildNodes();
       int length = allChildrenNodes.getLength();
       for (int i = 0; i < length; i++) {

	   Node node = allChildrenNodes.item(i);
	   if (node.getNodeType() == Node.ELEMENT_NODE) {

	       Element itemElement = (Element) node;
	       if (itemElement.getTagName().equals("item")) {
		   String s = itemElement.getAttribute("sequence");
		   int sequence = new Integer(s);
		   s = itemElement.getAttribute("type");
		   int type = new Integer(s);
		   String sakaiId = itemElement.getAttribute("sakaiid");
		   String name = itemElement.getAttribute("name");
		   String explanation = null;
		   String sakaiTitle = itemElement.getAttribute("sakaititle");
		   String id = itemElement.getAttribute("id");
		   Long itemId = new Long(id);

		   // URL is probably no longer used, but if it is, it probably doesn't need mapping
		   if (type == SimplePageItem.RESOURCE || type == SimplePageItem.MULTIMEDIA) {
		       String prefix = "/group/" + oldSiteId + "/";
		       if (sakaiId.startsWith(prefix))
			   sakaiId = "/group/" + siteId + "/" + sakaiId.substring(prefix.length());
		       else
			   log.error("sakaiId not recognized " + sakaiId);
		   } else if (type == SimplePageItem.PAGE) {
		       // sakaiId should be the new page ID
		       Long newPageId = pageMap.get(Long.valueOf(sakaiId));
		       // we've seen a few cases where sakaiId of a subpage is 0. It won't be
		       // in the map, so this leaves it zero.
		       if (newPageId != null)
			   sakaiId = newPageId.toString();
		   }

		   if (type == SimplePageItem.ASSIGNMENT || type == SimplePageItem.ASSESSMENT || type == SimplePageItem.FORUM) {
		       sakaiId = SimplePageItem.DUMMY;
		       needFix = true;
		   }

		   SimplePageItem item = simplePageToolDao.makeItem(pageId, sequence, type, sakaiId, name);

		   if (explanation != null) {
		       item.setHtml(explanation);
		   } else {
		       item.setHtml(itemElement.getAttribute("html"));
		   }
		   s = itemElement.getAttribute("description");
		   if (s != null)
		       item.setDescription(s);
		   s = itemElement.getAttribute("height");
		   if (s != null)
		       item.setHeight(s);
		   s = itemElement.getAttribute("width");
		   if (s != null)
		       item.setWidth(s);
		   s = itemElement.getAttribute("alt");
		   if (s != null)
		       item.setAlt(s);
		   s = itemElement.getAttribute("required");
		   if (s != null)
		       item.setRequired(s.equals("true"));
		   s = itemElement.getAttribute("prerequisite");
		   if (s != null)
		       item.setPrerequisite(s.equals("true"));
		   s = itemElement.getAttribute("subrequirement");
		   if (s != null)
		       item.setSubrequirement(s.equals("true"));
		   s = itemElement.getAttribute("requirementtext");
		   if (s != null)
		       item.setRequirementText(s);
		   s = itemElement.getAttribute("nextpage");
		   if (s != null)
		       item.setNextPage(s.equals("true"));
		   s = itemElement.getAttribute("format");
		   if (s != null)
		       item.setFormat(s);
		   s = itemElement.getAttribute("samewindow");
		   if (s != null)
		       item.setSameWindow(s.equals("true"));
		   s = itemElement.getAttribute("anonymous");
		   if (s != null)
		       item.setAnonymous(s.equals("true"));
		   s = itemElement.getAttribute("showComments");
		   if (s != null)
		       item.setShowComments(s.equals("true"));
		   s = itemElement.getAttribute("forcedCommentsAnonymous");
		   if (s != null)
		       item.setForcedCommentsAnonymous(s.equals("true"));
		   
		   
		   s = itemElement.getAttribute("gradebookTitle");
		   if (s != null)
		       item.setGradebookTitle(s);
		   s = itemElement.getAttribute("altGradebookTitle");
		   if (s != null)
		       item.setAltGradebookTitle(s);
		   
		   
		   s = itemElement.getAttribute("gradebookPoints");
		   if (s != null && !s.equals("null"))
		       item.setGradebookPoints(Integer.valueOf(s));
		   s = itemElement.getAttribute("altPoints");
		   if (s != null && !s.equals("null"))
		       item.setAltPoints(Integer.valueOf(s));

		   s = itemElement.getAttribute("groupOwned");
		   if (s != null)
		       item.setGroupOwned(s.equals("true"));

		   if (RESTORE_GROUPS) {
		       String groupString = mergeGroups(itemElement, "ownerGroup", siteGroups);
		       if (groupString != null)
			   item.setOwnerGroups(groupString);
		   }

		   // save objectid for dummy items so we can do mapping; alt isn't otherwise used for these items
		   if (type == SimplePageItem.ASSIGNMENT || type == SimplePageItem.ASSESSMENT || type == SimplePageItem.FORUM) {
		       item.setAlt(itemElement.getAttribute("objectid"));
		   }

		   // not currently doing this, although the code has been tested.
		   // The problem is that other tools don't do it. Since much of our group
		   // awareness comes from the other tools, enabling this produces
		   // inconsistent results
		   if (RESTORE_GROUPS) {
		       String groupString = mergeGroups(itemElement, "group", siteGroups);
		       if (groupString != null)
			   item.setGroups(groupString);
		   }

		   NodeList attributes = itemElement.getElementsByTagName("attributes");
		   if (attributes != null && attributes.getLength() > 0) {
		       Node attributesNode = attributes.item(0); // only one
		       String attributeString = attributesNode.getTextContent();
		       item.setAttributeString(attributeString);
		   }

		   simplePageToolDao.quickSaveItem(item);
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

			   gradebookIfc.addExternalAssessment(siteId, s, null, title, Double.valueOf(itemElement.getAttribute("gradebookPoints")), null, "Lesson Builder");
			   needupdate = true;
			   item.setGradebookId(s);
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
			   gradebookIfc.addExternalAssessment(siteId, s, null, title, Double.valueOf(itemElement.getAttribute("altPoints")), null, "Lesson Builder");
			   needupdate = true;
			   item.setAltGradebook(s);
		   }

		   // have to save again, I believe
		   if (needupdate)
		       simplePageToolDao.quickUpdate(item);

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
	   }
       }
       return needFix;
    }

    String mergeGroups(Element itemElement, String attr, Collection<Group> siteGroups) {

	// not currently doing this, although the code has been tested.
	// The problem is that other tools don't do it. Since much of our group
	// awareness comes from the other tools, enabling this produces
	// inconsistent results

	NodeList groups = itemElement.getElementsByTagName(attr);
	String groupString = null;
	
	// translate groups from title to ID
	if (groups != null && siteGroups != null) {
	    for (int n = 0; n < groups.getLength(); n ++) {
		Element group = (Element)groups.item(n);
		String title = group.getAttribute("title");
		if (title != null && !title.equals("")) {
		    for (Group g: siteGroups) {
			if (title.equals(g.getTitle())) {
			    if (groupString == null)
				groupString = g.getId();
			    else
				groupString = groupString + "," + g.getId();
			}
		    }
		}
	    }
	}

	return groupString;

    }

    // fix up items on page. does any updates that need the whole page and item map
    private void fixItems(Element element, String oldServer, String siteId, String fromSiteId, Map<Long,Long> pageMap, Map<Long,Long> itemMap) {
  
       String oldSiteId = element.getAttribute("siteid");
       String oldPageIdString = element.getAttribute("pageid");
       Long oldPageId = Long.valueOf(oldPageIdString);
       Long pageId = pageMap.get(oldPageId);
       Site site = null;

       List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(pageId);

       if (items == null)
	   return;

       for (SimplePageItem item: items) {
	   if (item.getType() == SimplePageItem.TEXT) {
	       String s = item.getHtml();
	       if (s != null) {
		   String fixed = fixUrls(s, oldServer, siteId, fromSiteId, itemMap);
		   if (!s.equals(fixed)) {
		       item.setHtml(fixed);
		       simplePageToolDao.quickUpdate(item);
		   }
	       }
	   }
       }

    }


    public String fixUrls(String s, String oldServer, String siteId, String fromSiteId, Map<Long,Long> itemMap) {

	ContentCopyContext context = new ContentCopyContext(fromSiteId, siteId, oldServer);

       // should use CopyContent in kernel once KNL-737 is implemented. I'm including a copy of
       // it for the moment
	return convertHtmlContent(context, s, null, itemMap);

   }


   public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
		       Set userListAllowImport) {
       return merge(siteId, root, archivePath, fromSiteId, attachmentNames, userIdTrans, userListAllowImport, null);
   }

   /**
    * {@inheritDoc}
    */
   public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
		       Set userListAllowImport, Map<String, String> entityMap)
   {
      StringBuilder results = new StringBuilder();
      // map old to new page ids
      Map <Long,Long> pageMap = new HashMap<Long,Long>();
      Map <Long,Long> itemMap = new HashMap<Long,Long>();

      int count = 0;
      boolean needFix = false;

      String oldServer = root.getAttribute("server");

      if (siteId != null && siteId.trim().length() > 0)
      {
         try
         {
	     // create pages first, build up map of old to new page
	     NodeList pageNodes = root.getElementsByTagName("page");
	     int numPages = pageNodes.getLength();
	     for (int p = 0; p < numPages; p++) {
		 Node pageNode = pageNodes.item(p);
		 if (pageNode.getNodeType() == Node.ELEMENT_NODE) {
		     Element pageElement = (Element) pageNode;
		     String title = pageElement.getAttribute("title");
		     if (title == null)
			 title = "Page";
		     String oldPageIdString = pageElement.getAttribute("pageid");
		     if (oldPageIdString == null)
			 oldPageIdString = "0";
		     Long oldPageId = Long.valueOf(oldPageIdString);
		     SimplePage page = simplePageToolDao.makePage("0", siteId, title, 0L, 0L);
		     String gradebookPoints = pageElement.getAttribute("gradebookpoints");
		     if (StringUtils.isNotEmpty(gradebookPoints)) {
			 page.setGradebookPoints(Double.valueOf(gradebookPoints));
		     }
		     String folder = pageElement.getAttribute("folder");
		     if (StringUtils.isNotEmpty(folder))
			 page.setFolder(folder);
		     // Carry over the custom CSS sheet if present. These are of the form
		     // "/group/SITEID/LB-CSS/whatever.css", so we need to map the SITEID
		     String cssSheet = pageElement.getAttribute("csssheet");
		     if (StringUtils.isNotEmpty(cssSheet))
			 page.setCssSheet(cssSheet.replace("/group/"+fromSiteId+"/", "/group/"+siteId+"/"));
		     simplePageToolDao.quickSaveItem(page);
		     if (StringUtils.isNotEmpty(gradebookPoints)) {
			 gradebookIfc.addExternalAssessment(siteId, "lesson-builder:" + page.getPageId(), null,
							    title, Double.valueOf(gradebookPoints), null, "Lesson Builder");
		     }
		     pageMap.put(oldPageId, page.getPageId());
		 }
	     }

	     // process pages again to create the items
	     pageNodes = root.getElementsByTagName("page");
	     numPages = pageNodes.getLength();
	     for (int p = 0; p < numPages; p++) {
		 Node pageNode = pageNodes.item(p);
		 if (pageNode.getNodeType() == Node.ELEMENT_NODE) {
		     Element pageElement = (Element) pageNode;
		     if (makePage(pageElement, oldServer, siteId, fromSiteId, pageMap, itemMap, entityMap))
			 needFix = true;
		 }
	     }

	     if (needFix) {
		 Site site = siteService.getSite(siteId);
		 ResourcePropertiesEdit rp = site.getPropertiesEdit();
		 rp.addProperty("lessonbuilder-needsfixup", "2");
		 siteService.save(site);
		// unfortunately in duplicate site, site-admin has the site open, so this doesn't actually do anything
		// site-manage will stomp on it. However it does work for the other import operations, which is where
		// we need it, since site manage will call the fixup itself for duplicate

	     }

	     for (int p = 0; p < numPages; p++) {
		 Node pageNode = pageNodes.item(p);
		 if (pageNode.getNodeType() == Node.ELEMENT_NODE) {
		     Element pageElement = (Element) pageNode;
		     fixItems(pageElement, oldServer, siteId, fromSiteId, pageMap, itemMap);
		 }
	     }

	     // process tools and top-level pages
	     // need to fill in the tool id for top level pages and set parents to null
	     NodeList tools = root.getElementsByTagName("lessonbuilder");
	     int numTools =  tools.getLength();
	     for (int i = 0; i < numTools; i++) {
		 Node node = tools.item(i);
		 if (node.getNodeType() == Node.ELEMENT_NODE) {
		     Element element = (Element) node;
		     // there's an element at top level with no attributes. ignore it
		     String oldToolId = trimToNull(element.getAttribute("toolid"));
		     if (oldToolId != null) {

			 String toolTitle = trimToNull(element.getAttribute("name"));
			 String rolelist = element.getAttribute("functions.require");

			 if(toolTitle != null) {
			     Tool tr = toolManager.getTool(LESSONBUILDER_ID);
			     SitePage page = null;
			     ToolConfiguration tool = null;
			     Site site = siteService.getSite(siteId);

			     // some code in site action creates all the pages and tools and some doesn't
			     // so see if we already have this page and tool
			     Collection<ToolConfiguration> toolConfs = site.getTools(myToolIds());
			     if (toolConfs != null && !toolConfs.isEmpty())  {
				 for (ToolConfiguration config: toolConfs) {
				     if (config.getToolId().equals(LESSONBUILDER_ID)) {
					 SitePage p = config.getContainingPage();
					 // only use the Sakai page if it has the right title
					 // and we don't already have lessson builder info for it
					 if (p != null && toolTitle.equals(p.getTitle()) &&
					     simplePageToolDao.getTopLevelPageId(config.getPageId()) == null) {
					     page = p;
					     tool = config;
					     break;
					 }
				     }
				 }
			     }
			     // if we alrady have an appropriate blank page from the template, page and tool are set

			     if (page == null) {
				 page = site.addPage(); 
				 tool = page.addTool(LESSONBUILDER_ID);
			     }

			     String toolId = tool.getPageId();
			     if (toolId == null) {
				 log.error("unable to find new toolid for copy of " + oldToolId);
				 continue;
			     }

			     tool.setTitle(toolTitle);
			     if (rolelist != null)
				 tool.getPlacementConfig().setProperty("functions.require", rolelist);
			     count++;
			     page.setTitle(toolTitle);
			     page.setTitleCustom(true);
			     siteService.save(site);
				      
			     // now fix up the page. new format has it as attribute
			     String pageId = trimToNull(element.getAttribute("pageId"));
			     if (pageId == null) {
				 // old format. we should have a page node
				 // normally just one
				 Node pageNode = element.getFirstChild();
				 if (pageNode == null || pageNode.getNodeType() != Node.ELEMENT_NODE) {
				     log.error("page node not element");
				     continue;
				 }
				 Element pageElement = (Element)pageNode;
				 pageId = trimToNull(pageElement.getAttribute("pageid"));
			     }
			     if (pageId == null) {
				 log.error("page node without old pageid");
				 continue;
			     }

			     // fix up the new copy of the page to be top level
			     SimplePage simplePage = simplePageToolDao.getPage(pageMap.get(Long.valueOf(pageId)));
			     if (simplePage == null) {
				 log.error("can't find new copy of top level page");
				 continue;
			     }
			     simplePage.setParent(null);
			     simplePage.setTopParent(null);
			     simplePage.setToolId(toolId);
			     simplePageToolDao.quickUpdate(simplePage);

			     // create the vestigial item for this top level page
			     SimplePageItem item = simplePageToolDao.makeItem(0, 0, SimplePageItem.PAGE, Long.toString(simplePage.getPageId()), simplePage.getTitle());
			     simplePageToolDao.quickSaveItem(item);
			 }
		     }
		 }
	     }
            results.append("merging link tool " + siteId + " (" + count
                  + ") items.\n");
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
      }
      return results.toString();

   } // merge


   /**
    * {@inheritDoc}
    */
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

   /**
    * {@inheritDoc}
    */
   public boolean willArchiveMerge()
   {
      return true;
   }
   
	public void transferCopyEntities(String fromContext, String toContext, List ids)
	{
	    transferCopyEntitiesImpl(fromContext, toContext, ids, false);
	}

	public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup) {
	    transferCopyEntitiesImpl(fromContext, toContext, ids, cleanup);
	}    

	public Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List<String> ids) {
	    return transferCopyEntitiesImpl(fromContext, toContext, ids, false);
	}

	public Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List<String> ids, boolean cleanup) {
	    return transferCopyEntitiesImpl(fromContext, toContext, ids, cleanup);
	}
   
	public Map<String,String> transferCopyEntitiesImpl(String fromContext, String toContext, List ids, boolean cleanup)
	{	
	    Map<String,String> entityMap = new HashMap<String,String>();

	    try {
   
		if(cleanup == true) {
		    Site toSite = siteService.getSite(toContext);
				
		    List toSitePages = toSite.getPages();
		    if (toSitePages != null && !toSitePages.isEmpty()) {
			Vector removePageIds = new Vector();
			Iterator pageIter = toSitePages.iterator();
			while (pageIter.hasNext()) {
			    SitePage currPage = (SitePage) pageIter.next();

			    List<String> toolIds = myToolList();

			    List toolList = currPage.getTools();
			    Iterator toolIter = toolList.iterator();
			    while (toolIter.hasNext()) {
				
				ToolConfiguration toolConfig = (ToolConfiguration)toolIter.next();

				if (toolIds.contains(toolConfig.getToolId())) {
				    removePageIds.add(toolConfig.getPageId());
				}
			    }
			}
			for (int i = 0; i < removePageIds.size(); i++) {
			    String removeId = (String) removePageIds.get(i);
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
		Element root = doc.createElement("archive");
		doc.appendChild(root);
		root.setAttribute("source", fromContext);
		root.setAttribute("server", "foo");
		root.setAttribute("date", "now");
		root.setAttribute("system", "sakai");
		
		stack.push(root);
		
		archive(fromContext, doc, stack, "/tmp/archive", null);
		
		stack.pop();
	  
		merge(toContext,  (Element)doc.getFirstChild().getFirstChild(), "/tmp/archive", fromContext, null, null, null, entityMap);

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

	if (migrateAllLinks != null)
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
	    try {
		String newBody = (String) migrateAllLinks.invoke(linkMigrationHelperInstance, new Object[] { entrySet, msgBody});
		if (!msgBody.equals(newBody)) {
		    // items in findTextItemsInSite don't come from hibernate, so we have to get a real one
		    SimplePageItem i = simplePageToolDao.findItem(item.getId());
		    if (item != null) {
			i.setHtml(newBody);
			log.debug("html - (post mod):"+msgBody);
			simplePageToolDao.quickUpdate(i);
		    }
		}
	    } catch (Exception e) {
		log.warn("Problem migrating links in Lessonbuilder"+e);
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
	      log.error("can't get site " + toContext + " " + e);
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
	      } catch (IllegalStateException e) {
	        log.error(".fixupGroupRefs: Group with id {} cannot be removed because is locked", group.getId());
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
	return LESSONBUILDER_ID;
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
	ResourceLoader msgs = new ResourceLoader("Events");
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

    private String convertHtmlContent(ContentCopyContext context,
				      String content, String contentUrl, Map<Long,Long> itemMap) {
	
	// this old code (below) seems to have come from an old version of the kerne's reference migrator.
	// It's too complex for me to verify that it's right.
	// At this point the kernel just does string replacements. So I'm going to
	// replace /access/content/group/NNN with the new value
	// and also fix up the dummy references.

	String oldurl = "/access/content/group/" + context.getOldSiteId().replace(" ", "%20");
	String newurl = "/access/content/group/" + context.getNewSiteId().replace(" ", "%20");

	content = content.replace(oldurl, newurl);

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

	if (false) {
	StringBuilder output = new StringBuilder();
	Matcher matcher = attributePattern.matcher(content);
	int contentPos = 0;

	StringBuffer newContent = new StringBuffer();
	while (matcher.find()) {
	    String url = matcher.group(3);

	    // processUrl does a parse of the URL. It will fail if there is a space
	    // in it. But spaces are often used by humans, since they actually work.
	    // And a CKEditor bug inserts them. So handle them correctly.
	    url = url.replace(" ", "%20");
	    url = processUrl(context, url, contentUrl, itemMap);
	    // Content up to the match.
	    int copyTo = matcher.start(3);
	    // Start the second copy after the match.
	    int copyFrom = matcher.end(3);
	    int copyEnd = matcher.end();
	    
	    output.append(content.substring(contentPos, copyTo));
	    output.append(url);
	    output.append(content.substring(copyFrom, copyEnd));
	    contentPos = copyEnd;
	}
	    output.append(content.substring(contentPos));
	    return output.toString();
	} // end of if false

	return content;

    }

    final int ITEMDUMMYLEN = ITEMDUMMY.length();

    /**
     * Takes a URL and then decides if it should be replaced.
     * 
     * @param value
     * @return
     */
    private String processUrl(ContentCopyContext context, String value,
			      String contentUrl, Map<Long,Long>itemMap) {
	// Need to deal with backticks.
	// - /access/group/{siteId}/
	// - /web/{siteId}/
	// - /dav/{siteId}/
	// http(s)://weblearn.ox.ac.uk/ - needs trimming
	try {
	    URI uri = new URI(value);
	    uri = uri.normalize();
	    if (value.startsWith(ITEMDUMMY)) {
		String num = value.substring(ITEMDUMMYLEN);
		int i = num.indexOf("/");
		if (i >= 0)
		    num = num.substring(0, i);
		else 
		    return value;
		long oldItem = 0;
		try {
		    oldItem = Long.parseLong(num);
		} catch (Exception e) {
		    return value;
		}
		Long newItem = itemMap.get(oldItem);
		if (newItem == null)
		    return value;
		return ITEMDUMMY + newItem + "/";
	    } else if ("http".equals(uri.getScheme())
		|| "https".equals(uri.getScheme())) {
		if (uri.getHost() != null) {
		    // oldserver is the server that this archive is coming from
		    // oldserver null means it's a local copy, e.g. duplicate site
		    // for null we match URL against all of our server names
		    String oldServer = context.getOldServer();
		    if (oldServer == null && servers.contains(uri.getHost()) ||
			uri.getHost().equals(oldServer)) {
			// Drop the protocol and the host.
			uri = new URI(null, null, null, -1, uri.getPath(),
				      uri.getQuery(), uri.getFragment());
		    }
		}
	    }
	    // Only do replacement on our URLs.
	    if (uri.getHost() == null && uri.getPath() != null) {
		// Need to attempt todo path replacement now.
		String path = uri.getPath();
		Matcher matcher = pathPattern.matcher(path);

		if (matcher.matches()
		    && context.getOldSiteId().equals(matcher.group(1))) {
		    // Need to push the old URL onto the list of resources to
		    // process. Except that we can't do that inside Lesson Builder
		    //		    addPath(context, path);
		    String replacementPath = path
			.substring(0, matcher.start(1))
			+ context.getNewSiteId()
			+ path.substring(matcher.end(1));
		    // Create a new URI with the new path
		    uri = new URI(uri.getScheme(), uri.getUserInfo(),
				  uri.getHost(), uri.getPort(), replacementPath,
				  uri.getQuery(), uri.getFragment());
		} else if (!path.startsWith("/") && contentUrl != null) {
		    // Relative URL.
		    try {
			URI base = new URI(contentUrl);
			URI link = base.resolve(uri);
			// sorry, no can do
			//addPath(context, link.getPath());
		    } catch (URISyntaxException e) {
			log.error("Supplied contentUrl isn't valid: {}", contentUrl);
		    }
		}
	    }
	    return uri.toString();
	} catch (URISyntaxException e) {
	    // Logger this so we may get an idea of the things that are breaking
	    // the parser.
	    log.error("Failed to parse URL: {} {}", value, e.getMessage());
	}
	return value;
    }
    
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
		if (config.getToolId().equals(LESSONBUILDER_ID)) {
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
	    ToolConfiguration tool = page.addTool(LESSONBUILDER_ID);
	    tool.setTitle("dummy lesson");
	    page.setTitle("dummy lesson");
	    try {
		siteService.save(site);
		// don't set until we know the save worked
		dummyPageId = page.getId();
	    } catch (Exception e) {
		log.info("can't add dummy page to site " + e);
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
		log.info("exception in createentity " + e);
		return "exception in createentity " + e;
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

}
