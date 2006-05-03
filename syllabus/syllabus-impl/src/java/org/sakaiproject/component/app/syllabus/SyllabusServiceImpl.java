/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.syllabus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xerces.impl.dv.util.Base64;
import org.sakaiproject.api.app.syllabus.GatewaySyllabus;
import org.sakaiproject.api.app.syllabus.SyllabusAttachment;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.api.app.syllabus.SyllabusService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.Placement;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author rshastri TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
public class SyllabusServiceImpl implements SyllabusService
{
  private static final String SYLLABUS = "syllabus";
  private static final String SYLLABUS_ID = "id";
  private static final String SYLLABUS_USER_ID = "userID";
  private static final String SYLLABUS_REDIRECT_URL = "redirectUrl";
  private static final String SYLLABUS_CONTEXT_ID = "contextId";
  private static final String SYLLABUS_DATA = "syllabus_data";
  private static final String SYLLABUS_DATA_TITLE = "title";
  private static final String SYLLABUS_DATA_POSITION = "position";
  private static final String SYLLABUS_DATA_VIEW = "view";
  private static final String SYLLABUS_DATA_ID = "syllabus_id";
  private static final String SYLLABUS_DATA_EMAIL_NOTIFICATION = "emailNotification";
  private static final String SYLLABUS_DATA_STATUS = "status";
  private static final String SYLLABUS_DATA_ASSET = "asset";
  private static final String PAGE_ARCHIVE = "pageArchive";
  private static final String SITE_NAME = "siteName";
  private static final String SITE_ID = "siteId";
  private static final String SITE_ARCHIVE = "siteArchive";
  private static final String PAGE_NAME = "pageName";
  private static final String PAGE_ID = "pageId";
  /** Dependency: a SyllabusManager. */
  private SyllabusManager syllabusManager;
 
  /** Dependency: a logger component. */
  private Log logger = LogFactory.getLog(SyllabusServiceImpl.class);
  
  protected NotificationService notificationService = null;
  protected String m_relativeAccessPoint = null;
  
//sakai2 -- add init and destroy methods  
	public void init()
	{
		EntityManager.registerEntityProducer(this, REFERENCE_ROOT);
	  
	  m_relativeAccessPoint = REFERENCE_ROOT;
	  
	  NotificationEdit edit = notificationService.addTransientNotification();
	  
	  edit.setFunction(EVENT_SYLLABUS_POST_NEW);
	  edit.addFunction(EVENT_SYLLABUS_POST_CHANGE);
	  edit.addFunction(EVENT_SYLLABUS_DELETE_POST);
	  
	  edit.setResourceFilter(getAccessPoint(true));
	  
	  edit.setAction(new SiteEmailNotificationSyllabus());
	}

	public void destroy()
	{
	}


  /**
   * Establish logger component dependency.
   * 
   * @param logger
   *          the logger component.
   */
  public void setLogger(Log logger)
  {
    this.logger = logger;
  }

  /** Dependency: a SyllabusManager component. */
  public void setSyllabusManager(SyllabusManager syllabusManager)
  {
    this.syllabusManager = syllabusManager;
  }

 
  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.service.legacy.entity.ResourceService#getLabel()
   */
  public String getLabel()
  {
    return "syllabus";
  }

	/**
	 * {@inheritDoc}
	 */
	public boolean willArchiveMerge()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean willImport()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public HttpAccess getHttpAccess()
	{
		return null;
	}

	/**
	 * from StringUtil.java
	 */
	protected String[] split(String source, String splitter)
	{
		// hold the results as we find them
		Vector rv = new Vector();
		int last = 0;
		int next = 0;
		do
		{
			// find next splitter in source
			next = source.indexOf(splitter, last);
			if (next != -1)
			{
				// isolate from last thru before next
				rv.add(source.substring(last, next));
				last = next + splitter.length();
			}
		}
		while (next != -1);
		if (last < source.length())
		{
			rv.add(source.substring(last, source.length()));
		}

		// convert to array
		return (String[]) rv.toArray(new String[rv.size()]);

	} // split

	/**
	 * {@inheritDoc}
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		if (reference.startsWith(REFERENCE_ROOT))
		{
			// /syllabus/siteid/syllabusid
			String[] parts = split(reference, Entity.SEPARATOR);

			String subType = null;
			String context = null;
			String id = null;
			String container = null;

			// the first part will be null, then next the service, the third will be "calendar" or "event"
			if (parts.length > 2)
			{
				// the site/context
				context = parts[2];

				// the id
				if (parts.length > 3)
				{
					id = parts[3];
				}
			}

			ref.set(SERVICE_NAME, subType, id, container, context);

			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityDescription(Reference ref)
	{
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Entity getEntity(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getEntityAuthzGroups(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityUrl(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void syncWithSiteChange(Site site, EntityProducer.ChangeType change)
	{
	}

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.service.legacy.entity.ResourceService#archive(java.lang.String,
   *      org.w3c.dom.Document, java.util.Stack, java.lang.String,
   *      org.sakaiproject.service.legacy.entity.ReferenceVector)
   */
  public String archive(String siteId, Document doc, Stack stack, String arg3,
      List attachments)
  {

    StringBuffer results = new StringBuffer();

    try
    {
      int syDataCount = 0;
      results.append("archiving " + getLabel() + " context "
          + Entity.SEPARATOR + siteId + Entity.SEPARATOR
          + SiteService.MAIN_CONTAINER + ".\n");
      // start with an element with our very own (service) name
      Element element = doc.createElement(SyllabusService.class.getName());
      ((Element) stack.peek()).appendChild(element);
      stack.push(element);
      if (siteId != null && siteId.trim().length() > 0)
      {
        Element siteElement = doc.createElement(SITE_ARCHIVE);
        siteElement.setAttribute(SITE_NAME, SiteService.getSite(siteId).getId());
        siteElement.setAttribute(SITE_ID, SiteService.getSite(siteId)
            .getTitle());
//sakai2        Iterator pageIter = getSyllabusPages(siteId);
//        if (pageIter != null)
//        {
//          while (pageIter.hasNext())
//          {
//            String page = (String) pageIter.next();
//            if (page != null)
//            {
//              Element pageElement = doc.createElement(PAGE_ARCHIVE);
//              pageElement.setAttribute(PAGE_ID, page);
//sakai2              pageElement.setAttribute(PAGE_NAME, SiteService.getSite(siteId)
//sakai2                  .getPage(page).getTitle());
//sakai2              SyllabusItem syllabusItem = syllabusManager
//                  .getSyllabusItemByContextId(page);
   						SyllabusItem syllabusItem = syllabusManager
   						.getSyllabusItemByContextId(siteId);
              if (syllabusItem != null)
              {
                Element syllabus = doc.createElement(SYLLABUS);
                syllabus.setAttribute(SYLLABUS_ID, syllabusItem
                    .getSurrogateKey().toString());
                syllabus.setAttribute(SYLLABUS_USER_ID, syllabusItem
                    .getUserId());
                syllabus.setAttribute(SYLLABUS_CONTEXT_ID, syllabusItem
                    .getContextId());
                syllabus.setAttribute(SYLLABUS_REDIRECT_URL, syllabusItem
                    .getRedirectURL());

                Set syllabi = syllabusManager
                    .getSyllabiForSyllabusItem(syllabusItem);

                if (syllabi != null && !syllabi.isEmpty())
                {
                  Iterator syllabiIter = syllabi.iterator();
                  while (syllabiIter.hasNext())
                  {
                    SyllabusData syllabusData = (SyllabusData) syllabiIter
                        .next();
                    if (syllabusData != null)
                    {
                      syDataCount++;
                      Element syllabus_data = doc.createElement(SYLLABUS_DATA);
                      syllabus_data.setAttribute(SYLLABUS_DATA_ID, syllabusData
                          .getSyllabusId().toString());
                      syllabus_data.setAttribute(SYLLABUS_DATA_POSITION,
                          syllabusData.getPosition().toString());
                      syllabus_data.setAttribute(SYLLABUS_DATA_TITLE,
                          syllabusData.getTitle());
                      syllabus_data.setAttribute(SYLLABUS_DATA_VIEW,
                          syllabusData.getView());
                      syllabus_data.setAttribute(SYLLABUS_DATA_STATUS,
                          syllabusData.getStatus());
                      syllabus_data.setAttribute(
                          SYLLABUS_DATA_EMAIL_NOTIFICATION, syllabusData
                              .getEmailNotification());
                      Element asset = doc.createElement(SYLLABUS_DATA_ASSET);

                      try
                      {
                        String encoded = Base64.encode(syllabusData.getAsset().getBytes());
                        asset.setAttribute("syllabus_body-html", encoded);
                      }
                      catch(Exception e)
                      {
                        logger.warn("Encode Syllabus - " + e);
                      }
                      
                      
                      syllabus_data.appendChild(asset);
                      syllabus.appendChild(syllabus_data);

                    }
                  }
//sakai2                }
//                pageElement.appendChild(syllabus);
//              }
//              siteElement.appendChild(pageElement);
//            }

            //sakai2
            siteElement.appendChild(syllabus);      
          }
          results.append("archiving " + getLabel() + ": (" + syDataCount
              + ") syllabys items archived successfully.\n");
        }
        else
        {
          results.append("archiving " + getLabel()
              + ": empty syllabus archived.\n");
        }
        ((Element) stack.peek()).appendChild(siteElement);
        stack.push(siteElement);
      }
      stack.pop();

    }
    catch (DOMException e)
    {
      logger.error(e.getMessage(), e);
    }
    catch (IdUnusedException e)
    {
      logger.error(e.getMessage(), e);
    }
    return results.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.service.legacy.entity.ResourceService#merge(java.lang.String,
   *      org.w3c.dom.Element, java.lang.String, java.lang.String, java.util.Map, java.util.HashMap,
   *      java.util.Set)
   */
  public String merge(String siteId, Element root, String archivePath,
      String fromSiteId, Map attachmentNames, Map userIdTrans,
      Set userListAllowImport)
  {
    // buffer for the results log
    StringBuffer results = new StringBuffer();
    // populate SyllabusItem
    int syDataCount = 0;
    SyllabusItem syItem = null;
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
            Element siteElement = (Element) siteNode;
            if (siteElement.getTagName().equals(SITE_ARCHIVE))
            {
//sakai2              NodeList pageNodes = siteElement.getChildNodes();
//              int lengthPageNodes = pageNodes.getLength();
//              for (int p = 0; p < lengthPageNodes; p++)
//              {
//                Node pageNode = pageNodes.item(p);
//                if (pageNode.getNodeType() == Node.ELEMENT_NODE)
//                {
//                  Element pageElement = (Element) pageNode;
//                  if (pageElement.getTagName().equals(PAGE_ARCHIVE))
//                  {
//                    NodeList syllabusNodes = pageElement.getChildNodes();
              			NodeList syllabusNodes = siteElement.getChildNodes();
                    int lengthSyllabusNodes = syllabusNodes.getLength();
                    for (int sn = 0; sn < lengthSyllabusNodes; sn++)
                    {
                      Node syNode = syllabusNodes.item(sn);
                      if (syNode.getNodeType() == Node.ELEMENT_NODE)
                      {
                        Element syElement = (Element) syNode;
                        if (syElement.getTagName().equals(SYLLABUS))
                        {
                          //create a page and all syllabus tool to the page
//sakai2                          String page = addSyllabusToolToPage(siteId,pageElement
//                              .getAttribute(PAGE_NAME));
//                          SyllabusItem syllabusItem = syllabusManager
//                          .createSyllabusItem(UserDirectoryService
//                              .getCurrentUser().getId(), page, syElement
//                              .getAttribute(SYLLABUS_REDIRECT_URL));
                          String page = addSyllabusToolToPage(siteId,siteElement
                            .getAttribute(SITE_NAME));
//sakai2                          SyllabusItem syllabusItem = syllabusManager
//                          .createSyllabusItem(UserDirectoryService
//                              .getCurrentUser().getId(), page, syElement
//                              .getAttribute(SYLLABUS_REDIRECT_URL));
//sakai2 add--
                          SyllabusItem syllabusItem = syllabusManager.getSyllabusItemByContextId(page);
                          if(syllabusItem == null)
                          {
                            syllabusItem = syllabusManager
                              .createSyllabusItem(UserDirectoryService
                                  .getCurrentUser().getId(), page, syElement
                                  .getAttribute(SYLLABUS_REDIRECT_URL));
                          } 
                          //added htripath: get imported redirecturl, even if syllabus item is existing.
                          else{
                            if (syElement.getAttribute(SYLLABUS_REDIRECT_URL) !=null){
                              syllabusItem.setRedirectURL(syElement.getAttribute(SYLLABUS_REDIRECT_URL));
                              syllabusManager.saveSyllabusItem(syllabusItem) ;
                            }                            
                          }
                          //
                          NodeList allSyllabiNodes = syElement.getChildNodes();
                          int lengthSyllabi = allSyllabiNodes.getLength();
                          for (int j = 0; j < lengthSyllabi; j++)
                          {
                            Node child2 = allSyllabiNodes.item(j);
                            if (child2.getNodeType() == Node.ELEMENT_NODE)
                            {

                              Element syDataElement = (Element) child2;
                              if (syDataElement.getTagName().equals(
                                  SYLLABUS_DATA))
                              {
                                syDataCount = syDataCount + 1;
                                SyllabusData syData = new SyllabusDataImpl();
                                syData.setView(syDataElement
                                    .getAttribute(SYLLABUS_DATA_VIEW));
                                syData.setTitle(syDataElement
                                    .getAttribute(SYLLABUS_DATA_TITLE));
                                syData.setStatus(syDataElement
                                    .getAttribute(SYLLABUS_DATA_STATUS));
                                syData
                                    .setEmailNotification(syDataElement
                                        .getAttribute(SYLLABUS_DATA_EMAIL_NOTIFICATION));

                                NodeList allAssetNodes = syDataElement
                                    .getChildNodes();
                                int lengthSyData = allAssetNodes.getLength();
                                for (int k = 0; k < lengthSyData; k++)
                                {
                                  Node child3 = allAssetNodes.item(k);
                                  if (child3.getNodeType() == Node.ELEMENT_NODE)
                                  {
                                    Element assetEle = (Element) child3;
                                    if (assetEle.getTagName().equals(
                                        SYLLABUS_DATA_ASSET))
                                    {
                                      String charset = trimToNull(assetEle.getAttribute("charset"));
                                      if (charset == null) charset = "UTF-8";
                                      
                                      String body = trimToNull(assetEle.getAttribute("syllabus_body-html"));
                                      if (body != null)
                                      {
                                        try
                                        {
                                          byte[] decoded = Base64.decode(body);
                                          body = new String(decoded, charset);
                                        }
                                        catch (Exception e)
                                        {
                                          logger.warn("Decode Syllabus: " + e);
                                        }
                                      }
                                      
                                      if (body == null) body = "";
                                      
                                      String ret;
                                      ret = trimToNull(body);
                                      
                                      syData.setAsset(ret);
/*decode
                                      NodeList assetStringNodes = assetEle
                                          .getChildNodes();
                                      int lengthAssetNodes = assetStringNodes
                                          .getLength();
                                      for (int l = 0; l < lengthAssetNodes; l++)
                                      {
                                        Node child4 = assetStringNodes.item(l);
                                        if (child4.getNodeType() == Node.TEXT_NODE)
                                        {
                                          Text textNode = (Text) child4;
                                          syData.setAsset(textNode.getData());
                                        }
                                      }*/
                                    }
                                  }
                                }

                                int initPosition = syllabusManager
                                    .findLargestSyllabusPosition(syllabusItem)
                                    .intValue() + 1;
                                syData = syllabusManager
                                    .createSyllabusDataObject(
                                        syData.getTitle(), (new Integer(
                                            initPosition)), syData.getAsset(),
                                        syData.getView(), syData.getStatus(),
                                        syData.getEmailNotification());
                                syllabusManager.addSyllabusToSyllabusItem(
                                    syllabusItem, syData);

                              }
                            }
                          }

                        }
                      }
                    }
//sakai2                  }
//sakai2                }
//sakai2              }
            }
          }

        }
        results.append("merging syllabus " + siteId + " (" + syDataCount
            + ") syllabus items.\n");

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
  }

  /**
   * @param attribute
   * @return
   */
  private String addSyllabusToolToPage(String siteId,String pageName)
  {
/*sakai2  try
  {
    SiteEdit siteEdit = SiteService.editSite(siteId);
    SitePageEdit editPage=siteEdit.addPage();
    editPage.setTitle(pageName);
    ToolRegistration reg = ServerConfigurationService.getToolRegistration("sakai.syllabus");
    editPage.addTool(reg);    
    SiteService.commitEdit(siteEdit);
    return editPage.getId();
  }
  
  catch (IdUnusedException e)
  {
  logger.error(e.getMessage(),e);
  }
  catch (PermissionException e)
  {
    logger.error(e.getMessage(),e);
  }
  catch (InUseException e)
  {
    logger.error(e.getMessage(),e);
  }
  
    return null;*/
    return siteId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.service.legacy.entity.ResourceService#importResources(java.lang.String,
   *      java.lang.String, java.util.List)
   */
  public void importEntities(String fromSiteId, String toSiteId,
      List resourceIds)
  {
   try {
//sakai2    Iterator fromPages = getSyllabusPages(fromSiteId);
//    if (fromPages == null || !fromPages.hasNext())
//    {
//      logger
//          .debug("importResources: no source syllabus tool found to be imported");
//    }
//    else
//    {
//      Iterator toPages = getSyllabusPages(toSiteId);
//      if (toPages == null || !toPages.hasNext())
//      {
//        logger
//            .debug("importResources: no destination syllabus tool found where data can be imported");
//      }
//      else
//      {
//        while (fromPages.hasNext())
//        {
          
          logger.debug("importResources: Begin importing syllabus data");
//sakai2          String fromPage = (String) fromPages.next();
//sakai2          toPages = getSyllabusPages(toSiteId);
          String fromPage = fromSiteId;
          SyllabusItem fromSyllabusItem = syllabusManager
              .getSyllabusItemByContextId(fromPage);
          if (fromSyllabusItem != null)
          {
            Set fromSyDataSet = syllabusManager
                .getSyllabiForSyllabusItem(fromSyllabusItem);
            if (fromSyDataSet != null && fromSyDataSet.size() > 0)
            {
//sakai2              while (toPages.hasNext())
//             {
                fromSyDataSet = syllabusManager
                    .getSyllabiForSyllabusItem(fromSyllabusItem);
//sakai2                String toPage = (String) toPages.next();
                // String toPage=addSyllabusToolToPage(toSiteId,SiteService.getSite(fromSiteId).getPage(fromPage).getTitle());
//sakai2                SyllabusItem toSyItem = syllabusManager
//sakai2                    .getSyllabusItemByContextId(toPage);
                String toPage=addSyllabusToolToPage(toSiteId,
                    SiteService.getSite(toSiteId).getTitle());
                SyllabusItem toSyItem = syllabusManager
                    .getSyllabusItemByContextId(toPage);
                if (toSyItem == null)
                {
                  toSyItem = syllabusManager.createSyllabusItem(
                      UserDirectoryService.getCurrentUser().getId(), toPage,
                      fromSyllabusItem.getRedirectURL());
                }
                Iterator fromSetIter = fromSyDataSet.iterator();
                while (fromSetIter.hasNext())
                {
                  SyllabusData toSyData = (SyllabusData) fromSetIter.next();
                  Integer positionNo = new Integer(syllabusManager
                      .findLargestSyllabusPosition(toSyItem).intValue() + 1);
                  SyllabusData newToSyData = syllabusManager
                      .createSyllabusDataObject(toSyData.getTitle(),
                          positionNo, toSyData.getAsset(), toSyData.getView(),
                          toSyData.getStatus(), toSyData.getEmailNotification());
                  syllabusManager.addSyllabusToSyllabusItem(toSyItem,
                      newToSyData);
                }
//sakai2              }
            }
            else
            {
              logger.debug("importResources: no data found for syllabusItem id"
                  + fromSyllabusItem.getSurrogateKey().toString());
            }
          }
//sakai2        }
        logger.debug("importResources: End importing syllabus data");
//sakai2      }
//sakai2    }
   }
//   catch (IdUnusedException e)
//   {
//   logger.error(e.getMessage(),e);
//   }
   catch(Exception e)
   {
     logger.error(e.getMessage(),e);
   }

  }

  /**
   * @param siteId
   * @return iterator of pageids containing syllabus tool for given site
   */
  private Iterator getSyllabusPages(String siteId)
  {
    List syPages = null;
    syPages = new ArrayList();
    syPages.add(siteId);
    return syPages.iterator();
/*sakai2    if (siteId != null && siteId.trim().length() > 0)
    {
      Site site = null;
      try
      {
        site = SiteService.getSite(siteId);
      }
      catch (IdUnusedException e)
      {
        logger.error("Error retriving site: unused IdUnusedException "
            + e.getMessage(), e);
        return null;
      }
      List pages = site.getPages();

      if (pages != null && pages.size() > 0)
      {
        syPages = new ArrayList();

        Iterator pageIter = pages.iterator();
        while (pageIter.hasNext())
        {
          SitePage page = (SitePage) pageIter.next();
          if (page != null)
          {
            // process each tool till we see syllabus
            for (Iterator iTools = page.getTools().iterator(); iTools.hasNext();)
            {
              ToolConfiguration tool = (ToolConfiguration) iTools.next();
              String toolId = tool.getId();
              if (toolId != null && tool.getToolId().equals("sakai.syllabus"))
              {
                syPages.add(page.getId());
              }
            }
          }
        }
      }
    }
    if (syPages != null && syPages.size() > 0)
    {
      return syPages.iterator();
    }
    else
    {
      return null;
    }*/
  }

  public void setNotificationService(NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}

  protected String getAccessPoint(boolean relative)
	{
		return (relative ? "" : ServerConfigurationService.getAccessUrl()) + m_relativeAccessPoint;
	}
  
  public void postNewSyllabus(SyllabusData data)
  {
    BaseResourceEdit bre = new BaseResourceEdit(data.getSyllabusId().toString(), data);
    
    addLiveSyllabusProperties(bre);
    
    bre.setEvent(EVENT_SYLLABUS_POST_NEW);
    
    String emailNotify = data.getEmailNotification();
    
    int priority;
    
    if(emailNotify.equalsIgnoreCase("none"))
    {
      priority = NotificationService.NOTI_NONE;
    }
    else if(emailNotify.equalsIgnoreCase("high"))
    {
      priority = NotificationService.NOTI_REQUIRED;
    }
    else if(emailNotify.equalsIgnoreCase("low"))
    {
      priority = NotificationService.NOTI_OPTIONAL;
    }
    else
    {
      priority = NotificationService.NOTI_NONE;
    }

		Event event =
		  	EventTrackingService.newEvent(bre.getEvent(), bre.getReference(), 
			    true, priority);
		
		EventTrackingService.post(event);
  }
  
  public class BaseResourceEdit implements Entity, Edit
  {
		protected String m_id = null;

		protected String m_event = null;

		protected boolean m_active = false;

		protected boolean m_isRemoved = false;

		protected boolean m_bodyUpdated = false;
		
		protected ResourcePropertiesEdit m_properties = null;
		
		protected String m_reference = null;
		
		protected SyllabusData m_data = null;

		public BaseResourceEdit(String id, SyllabusData data)
		{
	    Placement placement = ToolManager.getCurrentPlacement();
			String currentSiteId = placement.getContext();
			
			m_id = id;
			
			m_data = data;
			
			m_reference = Entity.SEPARATOR + currentSiteId + Entity.SEPARATOR + m_id;
			
			m_properties = new BaseResourcePropertiesEdit();
			
			m_properties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, data.getTitle());
		}

		public String getUrl()
    {
			return getAccessPoint(false) + "/" + m_id;
    }

    public String getReference()
    {
      String thisString = getAccessPoint(true) + m_reference;
			return getAccessPoint(true) + m_reference;
    }

	/**
	 * @inheritDoc
	 */
	public String getReference(String rootProperty)
	{
		return getReference();
	}

	/**
	 * @inheritDoc
	 */
	public String getUrl(String rootProperty)
	{
		return getUrl();
	}

	public String getId()
    {
      return m_id;
    }

    public ResourceProperties getProperties()
    {
      return m_properties;
    }

	public Element toXml(Document doc, Stack stack)
    {
			Element syllabus = doc.createElement("syllabus");

			if (stack.isEmpty())
			{
				doc.appendChild(syllabus);
			}
			else
			{
				((Element) stack.peek()).appendChild(syllabus);
			}

			stack.push(syllabus);

			syllabus.setAttribute("id", m_id);
			syllabus.setAttribute("subject", m_data.getTitle());
			syllabus.setAttribute("body", m_data.getAsset());

			m_properties.toXml(doc, stack);

			stack.pop();

			return syllabus;

    }

    public boolean isActiveEdit()
    {
      return m_active;
    }

    public ResourcePropertiesEdit getPropertiesEdit()
    {
      return m_properties;
    }
    
    protected void closeEdit()
    {
      m_active = false;
    }
    
    protected void activate()
    {
      m_active = true;
    }
    
		protected String getEvent()
		{
			return m_event;
		}
		
		protected void setEvent(String event)
		{
		  m_event = event; 
		}
  }
  
	protected void addLiveSyllabusProperties(BaseResourceEdit r)
	{
		ResourcePropertiesEdit p = r.getPropertiesEdit();

		String current = SessionManager.getCurrentSessionUserId();
		p.addProperty(ResourceProperties.PROP_CREATOR, current);
		p.addProperty(ResourceProperties.PROP_MODIFIED_BY, current);

		String now = TimeService.newTime().toString();
		p.addProperty(ResourceProperties.PROP_CREATION_DATE, now);
		p.addProperty(ResourceProperties.PROP_MODIFIED_DATE, now);

		p.addProperty(ResourceProperties.PROP_IS_COLLECTION, "false");
	}
	
	public void postChangeSyllabus(SyllabusData data)
	{
    BaseResourceEdit bre = new BaseResourceEdit(data.getSyllabusId().toString(), data);
    
    addLiveSyllabusProperties(bre);
    
    bre.setEvent(EVENT_SYLLABUS_POST_CHANGE);
    
    String emailNotify = data.getEmailNotification();
    
    int priority;
    
    if(emailNotify.equalsIgnoreCase("none"))
    {
      priority = NotificationService.NOTI_NONE;
    }
    else if(emailNotify.equalsIgnoreCase("high"))
    {
      priority = NotificationService.NOTI_REQUIRED;
    }
    else if(emailNotify.equalsIgnoreCase("low"))
    {
      priority = NotificationService.NOTI_OPTIONAL;
    }
    else
    {
      priority = NotificationService.NOTI_NONE;
    }

		Event event =
		  	EventTrackingService.newEvent(bre.getEvent(), bre.getReference(), 
			    true, priority);
		
		EventTrackingService.post(event);
	}

	public void deletePostedSyllabus(SyllabusData data)
	{
    BaseResourceEdit bre = new BaseResourceEdit(data.getSyllabusId().toString(), data);
    
    addLiveSyllabusProperties(bre);
    
    bre.setEvent(EVENT_SYLLABUS_DELETE_POST);
    
    String emailNotify = data.getEmailNotification();
    
    int priority;
    
    if(emailNotify.equalsIgnoreCase("none"))
    {
      priority = NotificationService.NOTI_NONE;
    }
    else if(emailNotify.equalsIgnoreCase("high"))
    {
      priority = NotificationService.NOTI_REQUIRED;
    }
    else if(emailNotify.equalsIgnoreCase("low"))
    {
      priority = NotificationService.NOTI_OPTIONAL;
    }
    else
    {
      priority = NotificationService.NOTI_NONE;
    }

		Event event =
		  	EventTrackingService.newEvent(bre.getEvent(), bre.getReference(), 
			    true, priority);
		
		EventTrackingService.post(event);
	}
	
	public String trimToNull(String value)
	{
		if (value == null) return null;
		value = value.trim();
		if (value.length() == 0) return null;
		return value;

	}
	
	public List getMessages(String id)
	{
	  ArrayList list = new ArrayList();
	  
	  SyllabusItem syllabusItem = syllabusManager.getSyllabusItemByContextId(id);
	  if(syllabusItem == null)
	  {
	    return null;
	  }
	  Set listSet = syllabusManager.getSyllabiForSyllabusItem(syllabusItem);
	  Iterator iter = listSet.iterator();
	  while(iter.hasNext())
	  {
	    SyllabusData sd = (SyllabusData)iter.next();
	    if(sd.getView().equalsIgnoreCase("yes") && (sd.getStatus().equalsIgnoreCase("Posted")))
	    {
	      ArrayList attachList = new ArrayList();
	  	  Set attachSet = syllabusManager.getSyllabusAttachmentsForSyllabusData(sd);
	  	  Iterator attachIter = attachSet.iterator();
	  	  while(attachIter.hasNext())
	  	  {
	  	    attachList.add((SyllabusAttachment)attachIter.next());
	  	  }

	      GatewaySyllabus gs = new GatewaySyllabusImpl(sd, attachList);
	      
	      list.add(gs);
	    }
	  }
	  
	  return list;
	}
}

