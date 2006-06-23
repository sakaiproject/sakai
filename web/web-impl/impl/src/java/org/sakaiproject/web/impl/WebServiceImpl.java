/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/web/trunk/web-impl/impl/src/java/org/sakaiproject/web/impl/WebServiceImpl.java$
 * $Id: WebServiceImpl.java 9227 2006-06-22 02:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.web.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.web.api.WebService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WebServiceImpl implements WebService, EntityTransferrer
{
	
	private static Log M_log = LogFactory.getLog(WebServiceImpl.class);
	
  private static final String WEB_CONTENT = "web_content";
  private static final String REDIRECT_TAB = "redirect_tab";
  private static final String WEB_CONTENT_TITLE = "title";
  private static final String WEB_CONTENT_URL = "url";
	public static final String ATTR_TOP_REFRESH = "sakai.vppa.top.refresh";

	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Entity getEntity(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getEntityAuthzGroups(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityDescription(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityUrl(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public HttpAccess getHttpAccess()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport)
	{
		M_log.info("merge starts for Web Content...");
    if (siteId != null && siteId.trim().length() > 0)
    {
      try
      {
      	Site site = SiteService.getSite(siteId);
        NodeList allChildrenNodes = root.getChildNodes();
        int length = allChildrenNodes.getLength();
        for (int i = 0; i < length; i++)
        {
          Node siteNode = allChildrenNodes.item(i);
          if (siteNode.getNodeType() == Node.ELEMENT_NODE)
          {
            Element siteElement = (Element) siteNode;
            if (siteElement.getTagName().equals(WEB_CONTENT))
            {
            	NodeList allContentNodes = siteElement.getChildNodes();
              int lengthContent = allContentNodes.getLength();
              for (int j = 0; j < lengthContent; j++)
              {
                Node child1 = allContentNodes.item(j);
                if (child1.getNodeType() == Node.ELEMENT_NODE)
                {
                  Element contentElement = (Element) child1;
                  if (contentElement.getTagName().equals(REDIRECT_TAB))
                  {
                  	String contentTitle = contentElement.getAttribute(WEB_CONTENT_TITLE);
                  	String trimBody = null;
                  	if(contentTitle != null && contentTitle.length() >0)
                  	{
                      trimBody = trimToNull(contentTitle);
                      if (trimBody != null && trimBody.length() >0)
                      {
                      	byte[] decoded = Base64.decode(trimBody);
                      	contentTitle = new String(decoded, "UTF-8");
                      }
                  	}
                  	String contentUrl = contentElement.getAttribute(WEB_CONTENT_URL);
                  	trimBody = null;
                  	if(contentUrl != null && contentUrl.length() >0)
                  	{
                      trimBody = trimToNull(contentUrl);
                      if (trimBody != null && trimBody.length() >0)
                      {
                      	byte[] decoded = Base64.decode(trimBody);
                      	contentUrl = new String(decoded, "UTF-8");
                      }
                  	}
                  	
                  	if(contentTitle != null && contentUrl != null && contentTitle.length() >0 && contentUrl.length() >0)
                  	{
                			Tool tr = ToolManager.getTool("sakai.iframe");
                			SitePage page = site.addPage();
                			page.setTitle(contentTitle);
                			ToolConfiguration tool = page.addTool();
                			tool.setTool("sakai.iframe", tr);
                			tool.setTitle(contentTitle);
    									tool.getPlacementConfig().setProperty("source", contentUrl);
                  	}
                  }
                }
              }
            }
          }
        }
        SiteService.save(site);
    		ToolSession session = SessionManager.getCurrentToolSession();

    		if (session.getAttribute(ATTR_TOP_REFRESH) == null)
    		{
    			session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);
    		}
      }
      catch(Exception e)
      {
      	M_log.error("errors in merge for WebServiceImpl");
      	e.printStackTrace();
      }
    }
    
		return null;
	}

	public boolean parseEntityReference(String reference, Reference ref)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean willArchiveMerge()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public String[] myToolIds()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void transferCopyEntities(String fromContext, String toContext, List ids)
	{
		// TODO Auto-generated method stub
		
	}

	public String trimToNull(String value)
	{
		if (value == null) return null;
		value = value.trim();
		if (value.length() == 0) return null;
		return value;
	}


}