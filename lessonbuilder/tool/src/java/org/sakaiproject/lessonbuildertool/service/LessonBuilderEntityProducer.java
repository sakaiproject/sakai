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
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
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

   private SimplePageToolDao simplePageToolDao;
   private PublishedAssessmentService pService = new PublishedAssessmentService();
   private LessonEntity forumEntity;

   public void init() {
      logger.info("init()");
      
      try {
         EntityManager.registerEntityProducer(this, REFERENCE_ROOT);
      }
      catch (Exception e) {
         logger.warn("Error registering Link Tool Entity Producer", e);
      }

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

    protected String assignmentRef(String id, String siteId) {
	return "/assignment/a/" + siteId + "/" + id;
    }

    protected void addPage(Document doc, Element element, long pageId) {

	SimplePage page = simplePageToolDao.getPage(pageId);

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
		    if (item.getType() == SimplePageItem.ASSIGNMENT)
			try {
			    String title = AssignmentService.getAssignment(assignmentRef(item.getSakaiId(),page.getSiteId())).getTitle();
			    if (title != null && !title.equals(""))
				addAttr(doc, itemElement, "sakaititle", title);
			} catch (Exception ignore) {
			    logger.warn("archive: unable to archive item " + item.getId() + " can't get assignment title");
			    continue;
			}
		    else if (item.getType() == SimplePageItem.ASSESSMENT)
			try {
			    String title = pService.getPublishedAssessment(item.getSakaiId()).getTitle();
			    if (title != null && !title.equals(""))
				addAttr(doc, itemElement, "sakaititle", title);
			} catch (Exception ignore) {
			    logger.warn("archive: unable to archive item " + item.getId() + " can't get assessment title");
			    continue;
			}
		    else if (item.getType() == SimplePageItem.FORUM) {
			LessonEntity e = forumEntity.getEntity(item.getSakaiId());
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
		if (item.getType() == SimplePageItem.PAGE)
		    addPage(doc, itemElement, new Long(item.getSakaiId()));
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
		 
		 addPage(doc, element,  simplePageToolDao.getTopLevelPageId(config.getPageId()));
		 
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
       return null;
   }

   /**
    * {@inheritDoc}
    */
   public String getLabel() {
       return LESSONBUILDER;
   }

    private Long makePage(Element element, String toolId, String oldSiteId, String siteId, String title, Long parent, Long topParent, boolean hidden) {
       logger.debug("toolId " + toolId + " siteid " + siteId + " title " + title + " parent " + parent + " topParent " + topParent + " hidden " + hidden);
       
       SimplePage page = new SimplePage(toolId, siteId, title, parent, topParent);
       simplePageToolDao.saveItem(page);
       Long pageId = page.getPageId();

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
		       Node child = itemElement.getFirstChild();
		       if (child == null || child.getNodeType() != Node.ELEMENT_NODE) {
			   logger.error("subpage item doesn't have page description");
			   continue;
		       }
		       Element subPageElement = (Element) child;
		       String subtitle = subPageElement.getAttribute("title");
		       if (subtitle == null)
			   subtitle = "Page";
		       Long subPageId = makePage(subPageElement, toolId, oldSiteId, siteId, subtitle, pageId, topParent == null ? pageId : topParent, false);
		       sakaiId = subPageId.toString();
		   }
		   if (type == SimplePageItem.ASSIGNMENT || type == SimplePageItem.ASSESSMENT || type == SimplePageItem.FORUM)
		       sakaiId = SimplePageItem.DUMMY;

		   SimplePageItem item = new SimplePageItem(pageId, sequence, type, sakaiId, name);

		   if (explanation != null) {
		       item.setHtml(explanation);
		   } else {
		       s = itemElement.getAttribute("html");
		       if (s != null)
			   item.setHtml(s);
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

		   simplePageToolDao.saveItem(item);
	       }
	   }
       }
       return pageId;
    }


   /**
    * {@inheritDoc}
    */
   public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
         Set userListAllowImport)
   {
      StringBuilder results = new StringBuilder();

      int count = 0;

      if (siteId != null && siteId.trim().length() > 0)
      {
         try
         {
            NodeList allChildrenNodes = root.getChildNodes();
            int length = allChildrenNodes.getLength();
            for (int i = 0; i < length; i++)
            {
               Node siteNode = allChildrenNodes.item(i);
               if (siteNode.getNodeType() == Node.ELEMENT_NODE)
               {
                  Element element = (Element) siteNode;

                  if (element.getTagName().equals(LESSONBUILDER))
                  {
                     Site site = SiteService.getSite(siteId);

		     // in the model, this code was there. It would require
		     // one tool to already be present. I'm not sure whether that's right or not.
		     // if (site.getToolForCommonId(LINKTOOL_ID) != null) {

		     if (true) {
                        // add the link tools
                        NodeList nodes = element.getChildNodes();
                        int lengthNodes = nodes.getLength();
                        for (int cn = 0; cn < lengthNodes; cn++)
                        {
                           Node node = nodes.item(cn);
                           if (node.getNodeType() == Node.ELEMENT_NODE)
                           {
                              Element linkElement = (Element) node;
                              if (linkElement.getTagName().equals(LESSONBUILDER)) {
				  String oldToolId = linkElement.getAttribute("toolid");
				  String trimBody = null;
				  if(oldToolId != null && oldToolId.length() >0) {
				      trimBody = trimToNull(oldToolId);
				      if (trimBody != null && trimBody.length() >0) {
					  oldToolId = trimBody;
				      }
				  }
				  
				  String toolTitle = linkElement.getAttribute("name");
				  trimBody = null;
				  if(toolTitle != null && toolTitle.length() >0) {
				      trimBody = trimToNull(toolTitle);
				      if (trimBody != null && trimBody.length() >0) {
					  toolTitle = trimBody;
				      }
				  }
				  
				  String rolelist = linkElement.getAttribute("functions.require");

				  // have to have tool ID
				  if (oldToolId == null)
				      continue;

				  if(toolTitle != null) {
				      Tool tr = ToolManager.getTool(LESSONBUILDER_ID);
				      SitePage page = null;
				      ToolConfiguration tool = null;

				      // some code in site action creates all the pages and tools and some doesn't
				      // so see if we already have this page and tool
				      Collection<ToolConfiguration> tools = site.getTools(myToolIds());
				      if (tools != null && !tools.isEmpty())  {
					  for (ToolConfiguration config: tools) {
					      if (config.getToolId().equals(LESSONBUILDER_ID)) {
						  SitePage p = config.getContainingPage();
						  // only use the Sakai page if it has the right title
						  // and we don't already have lessson builder info for it
						  if (p.getTitle().equals(toolTitle) &&
						      simplePageToolDao.getTopLevelPageId(config.getPageId()) == null) {
						      System.out.println("found existing tool for " + toolTitle);
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
					  tool = page.addTool();
				      }
				      tool.setTool(LESSONBUILDER_ID, tr);
				      tool.setTitle(toolTitle);
				      // we have to be able to find this again
				      tool.getPlacementConfig().setProperty("oldtoolid", oldToolId);
				      if (rolelist != null)
					  tool.getPlacementConfig().setProperty("functions.require", rolelist);
				      count++;

				      page.setTitle(toolTitle);
				      page.setTitleCustom(true);
				      SiteService.save(site);

				      String toolId = null;
				      // get updated site
				      site = SiteService.getSite(siteId);
				      // now find the tool
				      tools = site.getTools(myToolIds());
				      if (tools != null && !tools.isEmpty())  {
					  for (ToolConfiguration config: tools) {
					      String oldId = config.getPlacementConfig().getProperty("oldtoolid");
					      // found our tool
					      if (oldId != null && oldId.equals(oldToolId)) {
						  toolId = config.getPageId();
						  break;
					      }
					  }
				      }
				      if (toolId == null) {
					  logger.error("unable to find new toolid for copy of " + oldToolId);
					  break;
				      }
				      
				      // normally just one
				      Node pageNode = linkElement.getFirstChild();
				      if (pageNode.getNodeType() != Node.ELEMENT_NODE) {
					  logger.error("page node not element");
					  break;
				      }
				      Element pageElement = (Element)pageNode;
				      String oldSiteId = pageElement.getAttribute("siteid");
				      String hiddenAttr = pageElement.getAttribute("hidden");
				      boolean hidden = false;
				      if (hiddenAttr != null && hiddenAttr.equals("true"))
					  hidden = true;

				      makePage(pageElement, toolId, oldSiteId, siteId, pageElement.getAttribute("title"), null, null, hidden);

				  }
			      }
			   }
                        }
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
       // not for the moment
       return false;
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
}
