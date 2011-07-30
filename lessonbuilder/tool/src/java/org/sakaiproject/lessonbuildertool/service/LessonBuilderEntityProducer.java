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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.lessonbuildertool.service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;                                                    

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;
import java.util.Vector;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.Arrays;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Statisticable;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.cover.AssignmentService;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;

import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.lessonbuildertool.LessonBuilderAccessAPI;
import org.sakaiproject.util.Xml;

/**
 * @author hedrick
 * The goal is to get sites to save and copy. However there's actually no data 
 * involved in this tool. The only configuration is the URL, which is a tool
 * configuration property. That's handled separately in site.xml
 *
 */
public class LessonBuilderEntityProducer extends AbstractEntityProvider
    implements EntityProducer, EntityTransferrer, Serializable, 
	       CoreEntityProvider, AutoRegisterEntityProvider, Statisticable  {

   protected final Log logger = LogFactory.getLog(getClass());

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

    // other tools don't copy group access restrictions, so I think we probably shouldn't. The data is
    // there in the archive
    public final boolean RESTORE_GROUPS = false;

   private SimplePageToolDao simplePageToolDao;
   private LessonEntity forumEntity;
   private LessonEntity quizEntity;
   private LessonEntity assignmentEntity;
   private LessonEntity bltiEntity;
   private LessonBuilderAccessAPI lessonBuilderAccessAPI;
   public void setLessonBuilderAccessAPI(LessonBuilderAccessAPI l) {
       lessonBuilderAccessAPI = l;
   }


   private Set<String> servers;

 // The attributes in HTML that should have their values looked at and possibly re-written
   private Collection<String> attributes = new HashSet<String>(
				    Arrays.asList(new String[] { "href", "src", "background", "action",
				    "pluginspage", "pluginurl", "classid", "code", "codebase",
				    "data", "usemap" }));

   private Pattern attributePattern;

   private Pattern pathPattern;


   public void init() {
      logger.info("init()");
      
      try {
         EntityManager.registerEntityProducer(this, REFERENCE_ROOT);
      }
      catch (Exception e) {
         logger.warn("Error registering Link Tool Entity Producer", e);
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
	  .compile("/(?:access/content/group|web|dav|portal/site)/([^/]+)/.*");
      
      // Add the server name to the list of servers
      String serverName = ServerConfigurationService.getString("serverName", null);
      String serverId = ServerConfigurationService.getString("serverId", null);

      servers = new HashSet<String>();
      // prefer servername to serverid, by doing it first
      if (serverName != null)
	  servers.add(serverName);
      if (serverId != null)
	  servers.add(serverId);
      // if neither is defined we're in trouble;
      if (servers.size() == 0)
	  System.out.println("LessonBuilderEntityProducer ERROR: neither servername nor serverid defined in sakai.properties");

      // this slightly odd code is for testing. It lets us test by reloading just lesson builder.
      // otherwise we have to restart sakai, since the entity stuff can't be restarted
      if (false) {
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
	  SecurityService.pushAdvisor(new SecurityAdvisor() {
		  public SecurityAdvice isAllowed(String userId, String function, String reference) {
		      return SecurityAdvice.ALLOWED;
		  }
	      });


	  merge("0134937b-ce16-440c-80a6-fb088d79e5ad",  (Element)doc.getFirstChild().getFirstChild(), "/tmp/archive", "45d48248-ba23-4829-914a-7219c3ced2dd", null, null, null);


      } catch (Exception e) {
	  System.out.println(e);
      } finally {
	  SecurityService.popAdvisor();
      }
      }

      try {
	  ComponentManager.loadComponent("org.sakaiproject.lessonbuildertool.service.LessonBuilderEntityProducer", this);
      } catch (Exception e) {
	  logger.warn("Error registering Lesson Builder Entity Producer with Spring. Lessonbuilder will work, but Lesson Builder instances won't be imported from site archives. This normally happens only if you redeploy Lessonbuilder. Suggest restarting Sakai", e);
      }

   }
   
   /**
    * Destroy
    */
   public void destroy()
   {
      logger.info("destroy()");
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
		if (item.isSameWindow() != null)
		    addAttr(doc, itemElement, "samewindow", item.isSameWindow() ? "true" : "false");

		//		if (item.getType() == SimplePageItem.PAGE)
		//		    addPage(doc, itemElement, new Long(item.getSakaiId()));
		String groupString = item.getGroups();
		Collection<Group> siteGroups = site.getGroups();
		if (groupString != null && !groupString.equals("") && siteGroups != null) {
		    String [] groups = groupString.split(",");
		    for (int i = 0; i < groups.length ; i++) {
			Element groupElement = doc.createElement("group");
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

		pageElement.appendChild(itemElement);
	    }
	}		
	element.appendChild(pageElement);
    }

   /**
    * {@inheritDoc}
    */
   public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
   {
      //prepare the buffer for the results log
       StringBuilder results = new StringBuilder();

      try 
      {
	 Site site = SiteService.getSite(siteId);
         // start with an element with our very own (service) name         
         Element element = doc.createElement(serviceName());
         element.setAttribute(VERSION_ATTR, ARCHIVE_VERSION);
         ((Element) stack.peek()).appendChild(element);
         stack.push(element);

         Element lessonbuilder = doc.createElement(LESSONBUILDER);

	 List<SimplePage> sitePages = simplePageToolDao.getSitePages(siteId);
	 for (SimplePage page: sitePages)
	     addPage(doc, lessonbuilder, page, site);

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
		 
		 addAttr(doc, element, "pageId", Long.toString(simplePageToolDao.getTopLevelPageId(config.getPageId())));
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
         logger.warn("archive: exception archiving service: " + any + " " +  serviceName());
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
       // not needed
       return null;
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
    private Long makePage(Element element, String oldServer, String siteId, String fromSiteId, Map<Long,Long> pageMap) {
  
       String oldSiteId = element.getAttribute("siteid");
       String oldPageIdString = element.getAttribute("pageid");
       Long oldPageId = Long.valueOf(oldPageIdString);
       Long pageId = pageMap.get(oldPageId);
       Site site = null;
       Collection<Group> siteGroups = null;
       // not currently doing this
       if (RESTORE_GROUPS) {
	   try {
	       site = SiteService.getSite(siteId);
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

		   // URL is probably no longer used, but if it is, it probably doesn't need mapping
		   if (type == SimplePageItem.RESOURCE || type == SimplePageItem.MULTIMEDIA) {
		       String prefix = "/group/" + oldSiteId + "/";
		       if (sakaiId.startsWith(prefix))
			   sakaiId = "/group/" + siteId + "/" + sakaiId.substring(prefix.length());
		       else
			   logger.error("sakaiId not recognized " + sakaiId);
		   } else if (type == SimplePageItem.PAGE) {
		       // sakaiId should be the new page ID
		       sakaiId = pageMap.get(Long.valueOf(sakaiId)).toString();
		   }
		   if (type == SimplePageItem.ASSIGNMENT || type == SimplePageItem.ASSESSMENT || type == SimplePageItem.FORUM)
		       sakaiId = SimplePageItem.DUMMY;

		   SimplePageItem item = simplePageToolDao.makeItem(pageId, sequence, type, sakaiId, name);

		   if (explanation != null) {
		       item.setHtml(explanation);
		   } else {
		       s = itemElement.getAttribute("html");
		       if (s != null)
			   item.setHtml(fixUrls(s, oldServer, siteId, fromSiteId));
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

		   // not currently doing this, although the code has been tested.
		   // The problem is that other tools don't do it. Since much of our group
		   // awareness comes from the other tools, enabling this produces
		   // inconsistent results
		   if (RESTORE_GROUPS) {
		       NodeList groups = itemElement.getElementsByTagName("group");
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
		       if (groupString != null)
			   item.setGroups(groupString);
		   }
		   // end if mergeGroups

		   simplePageToolDao.quickSaveItem(item);
	       }
	   }
       }
       return pageId;
    }


    public String fixUrls(String s, String oldServer, String siteId, String fromSiteId) {

	ContentCopyContext context = new ContentCopyContext(fromSiteId, siteId, oldServer);

       // should use CopyContent in kernel once KNL-737 is implemented. I'm including a copy of
       // it for the moment
       return convertHtmlContent(context, s, null);

   }


   /**
    * {@inheritDoc}
    */
   public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
         Set userListAllowImport)
   {
      StringBuilder results = new StringBuilder();
      // map old to new page ids
      Map <Long,Long> pageMap = new HashMap<Long,Long>();

      int count = 0;

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
		     if (gradebookPoints != null && !gradebookPoints.equals(""))
			 page.setGradebookPoints(Double.valueOf(gradebookPoints));
		     simplePageToolDao.quickSaveItem(page);
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
		     makePage(pageElement, oldServer, siteId, fromSiteId, pageMap);
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
			     Tool tr = ToolManager.getTool(LESSONBUILDER_ID);
			     SitePage page = null;
			     ToolConfiguration tool = null;
			     Site site = SiteService.getSite(siteId);

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
				 logger.error("unable to find new toolid for copy of " + oldToolId);
				 continue;
			     }

			     tool.setTitle(toolTitle);
			     if (rolelist != null)
				 tool.getPlacementConfig().setProperty("functions.require", rolelist);
			     count++;
			     page.setTitle(toolTitle);
			     page.setTitleCustom(true);
			     SiteService.save(site);
				      
			     // now fix up the page. new format has it as attribute
			     String pageId = trimToNull(element.getAttribute("pageId"));
			     if (pageId == null) {
				 // old format. we should have a page node
				 // normally just one
				 Node pageNode = element.getFirstChild();
				 if (pageNode.getNodeType() != Node.ELEMENT_NODE) {
				     logger.error("page node not element");
				     continue;
				 }
				 Element pageElement = (Element)pageNode;
				 pageId = trimToNull(pageElement.getAttribute("pageid"));
			     }
			     if (pageId == null) {
				 logger.error("page node without old pageid");
				 continue;
			     }

			     // fix up the new copy of the page to be top level
			     SimplePage simplePage = simplePageToolDao.getPage(pageMap.get(Long.valueOf(pageId)));
			     if (simplePage == null) {
				 logger.error("can't find new copy of top level page");
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
            logger.error(e.getMessage(), e);
            results.append("merging " + getLabel()
                  + " failed during xml parsing.\n");
         }
         catch (Exception e)
         {
            logger.error(e.getMessage(), e);
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
       type = id.substring(1, i);
       String numstring = id.substring(i+1);
       i = numstring.indexOf("/");
       if (i >= 0)
	   numstring = numstring.substring(0, i);

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
	    transferCopyEntities(fromContext, toContext, ids, false);
	}

	public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup)
	{	
	    try {
   
		if(cleanup == true) {
		    Site toSite = SiteService.getSite(toContext);
				
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
		    SiteService.save(toSite);
		    ToolSession session = SessionManager.getCurrentToolSession();

		    if (session.getAttribute(ATTR_TOP_REFRESH) == null) {
			session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);
		    }
			
		}

		logger.debug("lesson builder transferCopyEntities");
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
	  
		merge(toContext,  (Element)doc.getFirstChild().getFirstChild(), "/tmp/archive", fromContext, null, null, null);

		ToolSession session = SessionManager.getCurrentToolSession();

		if (session.getAttribute(ATTR_TOP_REFRESH) == null) {
		    session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);
		}

	    } catch (Exception e) {
		logger.error(e.getMessage(), e);
	    }
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
	new String[] {"lessonbuilder.create", "lessonbuilder.delete", "lessonbuilder.update", "lessonbuilder.read"};

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
				      String content, String contentUrl) {
	StringBuilder output = new StringBuilder();
	Matcher matcher = attributePattern.matcher(content);
	int contentPos = 0;
	while (matcher.find()) {
	    String url = matcher.group(3);

	    url = processUrl(context, url, contentUrl);
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
    }


    /**
     * Takes a URL and then decides if it should be replaced.
     * 
     * @param value
     * @return
     */
    private String processUrl(ContentCopyContext context, String value,
			      String contentUrl) {
	// Need to deal with backticks.
	// - /access/group/{siteId}/
	// - /web/{siteId}/
	// - /dav/{siteId}/
	// http(s)://weblearn.ox.ac.uk/ - needs trimming
	try {
	    URI uri = new URI(value);
	    uri = uri.normalize();
	    if ("http".equals(uri.getScheme())
		|| "https".equals(uri.getScheme())) {
		if (uri.getHost() != null) {
		    if (uri.getHost().equals(context.getOldServer())) {
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
			System.err.println("Supplied contentUrl isn't valid: "
				 + contentUrl);
		    }
		}
	    }
	    return uri.toString();
	} catch (URISyntaxException e) {
	    // Log this so we may get an idea of the things that are breaking
	    // the parser.
	    System.err.println("Failed to parse URL: " + value + " " + e.getMessage());
	}
	return value;
    }
    


}
