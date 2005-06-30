/**********************************************************************************
*
* $Header: /cvs/sakai2/syllabus/syllabus-comp-shared/src/java/org/sakaiproject/component/app/syllabus/SyllabusServiceImpl.java,v 1.4 2005/05/19 14:26:30 cwen.iupui.edu Exp $
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.component.app.syllabus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.api.app.syllabus.SyllabusService;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.framework.config.cover.ServerConfigurationService;
import org.sakaiproject.service.framework.log.Logger;
import org.sakaiproject.service.legacy.resource.ReferenceVector;
import org.sakaiproject.service.legacy.resource.Resource;
import org.sakaiproject.service.legacy.site.cover.SiteService;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

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
  private Logger logger = null;
  

//sakai2 -- add init and destroy methods  
	public void init()
	{
	  ServerConfigurationService.registerResourceService(this);
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
  public void setLogger(Logger logger)
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
   * @see org.sakaiproject.service.legacy.resource.ResourceService#getLabel()
   */
  public String getLabel()
  {
    return "syllabus";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sakaiproject.service.legacy.resource.ResourceService#archive(java.lang.String,
   *      org.w3c.dom.Document, java.util.Stack, java.lang.String,
   *      org.sakaiproject.service.legacy.resource.ReferenceVector)
   */
  public String archive(String siteId, Document doc, Stack stack, String arg3,
      ReferenceVector attachments)
  {

    StringBuffer results = new StringBuffer();

    try
    {
      int syDataCount = 0;
      results.append("archiving " + getLabel() + " context "
          + Resource.SEPARATOR + siteId + Resource.SEPARATOR
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

                      Node assetNode = doc.createTextNode(syllabusData
                          .getAsset());
                      asset.appendChild(assetNode);
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
   * @see org.sakaiproject.service.legacy.resource.ResourceService#merge(java.lang.String,
   *      org.w3c.dom.Element, java.lang.String, java.lang.String, java.util.Map, java.util.HashMap,
   *      java.util.Set)
   */
  public String merge(String siteId, Element root, String archivePath,
      String fromSiteId, Map attachmentNames, HashMap userIdTrans,
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
                                      }
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
   * @see org.sakaiproject.service.legacy.resource.ResourceService#importResources(java.lang.String,
   *      java.lang.String, java.util.List)
   */
  public void importResources(String fromSiteId, String toSiteId,
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

}

/*
 *  public String merge(String siteId, Element root, String archivePath,
    String fromSiteId, Map attachmentNames, HashMap userIdTrans,
    Set userListAllowImport)
{
  class SyllabusData_M extends SyllabusDataImpl
  {
    private SyllabusData syllabusData;

    public SyllabusData_M()
    {

    }

    public SyllabusData_M(SyllabusData syllabusData)
    {
      this.syllabusData = syllabusData;
    }

    public int hashCode()
    {
      return IdService.getUniqueId().hashCode();
    }

    public SyllabusData getSyllabusData()
    {
      return this.syllabusData;
    }
  }

  // buffer for the results log
  StringBuffer results = new StringBuffer();
  // populate SyllabusItem
  int syDataCount = 0;
  SyllabusItem syItem = null;
  Set syllabusDataSet = new HashSet();
  try
  {
    NodeList allChildrenNodes = root.getChildNodes();
    int length = allChildrenNodes.getLength();
    for (int i = 0; i < length; i++)
    {
      Node child = allChildrenNodes.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE)
      {
        Element syElement = (Element) child;

        if (syElement.getTagName().equals(SYLLABUS))
        {
          syItem = new SyllabusItemImpl();
          syItem
              .setRedirectURL(syElement.getAttribute(SYLLABUS_REDIRECT_URL));

          NodeList allSyllabiNodes = syElement.getChildNodes();
          int lengthSyllabi = allSyllabiNodes.getLength();
          for (int j = 0; j < lengthSyllabi; j++)
          {
            Node child2 = allSyllabiNodes.item(j);
            if (child2.getNodeType() == Node.ELEMENT_NODE)
            {

              Element syDataElement = (Element) child2;
              if (syDataElement.getTagName().equals(SYLLABUS_DATA))
              {
                syDataCount = syDataCount + 1;
                SyllabusData_M syData = new SyllabusData_M();
                syData
                    .setView(syDataElement.getAttribute(SYLLABUS_DATA_VIEW));
                syData.setTitle(syDataElement
                    .getAttribute(SYLLABUS_DATA_TITLE));
                syData.setStatus(syDataElement
                    .getAttribute(SYLLABUS_DATA_STATUS));
                syData.setEmailNotification(syDataElement
                    .getAttribute(SYLLABUS_DATA_EMAIL_NOTIFICATION));

                NodeList allAssetNodes = syDataElement.getChildNodes();
                int lengthSyData = allAssetNodes.getLength();
                for (int k = 0; k < lengthSyData; k++)
                {
                  Node child3 = allAssetNodes.item(k);
                  if (child3.getNodeType() == Node.ELEMENT_NODE)
                  {
                    Element assetEle = (Element) child3;
                    if (assetEle.getTagName().equals(SYLLABUS_DATA_ASSET))
                    {
                      NodeList assetStringNodes = assetEle.getChildNodes();
                      int lengthAssetNodes = assetStringNodes.getLength();
                      for (int l = 0; l < lengthAssetNodes; l++)
                      {
                        Node child4 = assetStringNodes.item(l);
                        if (child4.getNodeType() == Node.TEXT_NODE)
                        {
                          Text textNode = (Text) child4;
                          syData.setAsset(textNode.getData());
                        }
                      }
                    }
                  }
                }
                if (syllabusDataSet != null && syData != null)
                {
                  syllabusDataSet.add(syData);
                }
              }
            }
          }
          if (syllabusDataSet.size() > 0)
          {
            syItem.setSyllabi(syllabusDataSet);
          }

        }

      }

    }
    if (syItem != null)
    {
      // processMerge in existing tools for given site
      if (siteId != null && siteId.trim().length() > 0)
      {

        Iterator pageIter = getSyllabusPages(siteId);
        if (pageIter != null)
        {
          while (pageIter.hasNext())
          {
            String page = (String) pageIter.next();
            if (page != null)
            {
              SyllabusItem syllabusItem = syllabusManager
                  .getSyllabusItemByContextId(page);
              if (syllabusItem == null)
              {
                syllabusItem = syllabusManager.createSyllabusItem(
                    UserDirectoryService.getCurrentUser().getId(), page,
                    syItem.getRedirectURL());
              }
              if (syItem.getSyllabi() != null
                  && syItem.getSyllabi().size() > 0)
              {
                Set lst = (HashSet) syItem.getSyllabi();
                Iterator syDataIter = lst.iterator();
                while (syDataIter.hasNext())
                {
                  SyllabusData_M mySyData = (SyllabusData_M) syDataIter
                      .next();
                  int initPosition = syllabusManager
                      .findLargestSyllabusPosition(syllabusItem).intValue() + 1;
                  SyllabusData syData = syllabusManager
                      .createSyllabusDataObject(mySyData.getTitle(),
                          (new Integer(initPosition)), mySyData.getAsset(),
                          mySyData.getView(), mySyData.getStatus(), mySyData
                              .getEmailNotification());

                  syllabusManager.addSyllabusToSyllabusItem(syllabusItem,
                      syData);
                }
              }
            }
          }
          results.append("merging syllabus " + siteId + " (" + syDataCount
              + ") syllabus items.\n");
          return results.toString();
        }
        else
        {
          results
              .append("merging syllabus : no syllabus tool found for site "
                  + siteId + " \n");
          return results.toString();
        }

      }
    }
    else
    {
      results
          .append("merging syllabus: error : no syllabus found in xml file"
              + siteId + " \n");
    }
  }
  catch (DOMException e)
  {
    logger.error(e.getMessage(), e);
    results.append("merging " + getLabel() + " failed during xml parsing.\n");

  }
  catch (Exception e)
  {
    logger.error(e.getMessage(), e);
    results.append("merging " + getLabel() + " failed.\n");
  }

  return results.toString();
}

 * 
 */

/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/syllabus/syllabus-comp-shared/src/java/org/sakaiproject/component/app/syllabus/SyllabusServiceImpl.java,v 1.4 2005/05/19 14:26:30 cwen.iupui.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/

