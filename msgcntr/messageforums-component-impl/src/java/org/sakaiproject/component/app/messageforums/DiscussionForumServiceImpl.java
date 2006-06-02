/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/DiscussionForumServiceImpl.java $
 * $Id: DiscussionForumServiceImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.component.app.messageforums;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.ArrayList;

import org.apache.xerces.impl.dv.util.Base64;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DiscussionForumServiceImpl  implements DiscussionForumService
{
  private static final String MESSAGEFORUM = "messageforum";
  private static final String DISCUSSION_FORUM = "discussion_forum";
  private static final String DISCUSSION_TOPIC = "discussion_topic";
  private static final String DISCUSSION_FORUM_TITLE = "category";
  private static final String DISCUSSION_FORUM_DESC = "body";
  private static final String TOPIC_TITLE = "subject";
  private static final String DRAFT = "draft";
  private static final String PROPERTIES = "properties";
  private static final String PROPERTY = "property";
  private static final String TOPIC_SHORT_DESC = "Classic:bboardForums_description";
  private static final String TOPIC_LONG_DESC = "Classic:bboardForums_content";
  private static final String NAME = "name";
  private static final String ENCODE = "enc";
  private static final String BASE64 = "BASE64";
  private static final String VALUE = "value";
  private static final String ATTACHMENT = "attachment";
  
  private MessageForumsForumManager forumManager;
  private AreaManager areaManager;
  private MessageForumsMessageManager messageManager;
	
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
		return "messageforum";
	}

	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport)
	{
    StringBuffer results = new StringBuffer();
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
            if (siteElement.getTagName().equals(MESSAGEFORUM))
            {
            	NodeList allForumNodes = siteElement.getChildNodes();
              int lengthForum = allForumNodes.getLength();
              for (int j = 0; j < lengthForum; j++)
              {
                Node child1 = allForumNodes.item(j);
                if (child1.getNodeType() == Node.ELEMENT_NODE)
                {
                  Element forumElement = (Element) child1;
                  if (forumElement.getTagName().equals(DISCUSSION_FORUM))
                  {
                  	DiscussionForum dfForum = forumManager.createDiscussionForum();
                  	
                  	String forumTitle = forumElement.getAttribute(DISCUSSION_FORUM_TITLE);
                  	dfForum.setTitle(forumTitle);

                  	String forumDesc = forumElement.getAttribute(DISCUSSION_FORUM_DESC);
                  	String trimBody = null;
                  	if(forumDesc != null && forumDesc.length() >0)
                  	{
                      trimBody = trimToNull(forumDesc);
                      if (trimBody != null && trimBody.length() >0)
                      {
                      	byte[] decoded = Base64.decode(trimBody);
                      	trimBody = new String(decoded, "UTF-8");
                      }
                  	}
                  	if(trimBody != null)
                  	{
                  		dfForum.setExtendedDescription(trimBody);
                  	}
                  	NodeList topicNodes = forumElement.getChildNodes();
                  	boolean hasTopic = false;
                  	for(int k=0; k<topicNodes.getLength(); k++)
                  	{
                  		Node topicChild = topicNodes.item(k);
                  		if(topicChild.getNodeType() == Node.ELEMENT_NODE)
                  		{
                  			Element topicElement = (Element) topicChild;
                  			if(topicElement.getTagName().equals(DISCUSSION_TOPIC))
                  			{
                  				DiscussionTopic dfTopic = forumManager.createDiscussionForumTopic(dfForum);
                  				List attachStringList = new ArrayList();
                  				
                  				String topicTitle = topicElement.getAttribute(TOPIC_TITLE);
                  				dfTopic.setTitle(topicTitle);
                  				
                  				String topicDraft = topicElement.getAttribute(DRAFT);
                  				if(topicDraft != null && topicDraft.length() >0)
                  					dfTopic.setDraft(new Boolean(topicDraft));
                  				
                  				NodeList topicPropertiesNodes = topicElement.getChildNodes();
                  				for(int m=0; m<topicPropertiesNodes.getLength(); m++)
                  				{
                  					Node propertiesNode = topicPropertiesNodes.item(m);
                  					if(propertiesNode.getNodeType() == Node.ELEMENT_NODE)
                  					{
                  						Element propertiesElement = (Element)propertiesNode;
                  						if(propertiesElement.getTagName().equals(PROPERTIES))
                  						{
                  							NodeList propertyList = propertiesElement.getChildNodes();
                  							for(int n=0; n<propertyList.getLength(); n++)
                  							{
                  								Node propertyNode = propertyList.item(n);
                  								if(propertyNode.getNodeType() == Node.ELEMENT_NODE)
                  								{
                  									Element propertyElement = (Element)propertyNode;
                  									if(propertyElement.getTagName().equals(PROPERTY))
                  									{
                  										if(TOPIC_SHORT_DESC.equals(propertyElement.getAttribute(NAME)))
                  										{
                  											if(BASE64.equals(propertyElement.getAttribute(ENCODE)))
                  											{
                  												String topicDesc = propertyElement.getAttribute(VALUE);
                  		                  	String trimDesc = null;
                  		                  	if(topicDesc != null && topicDesc.length() >0)
                  		                  	{
                  		                      trimDesc = trimToNull(topicDesc);
                  		                      if (trimDesc != null && trimDesc.length() >0)
                  		                      {
                  		                      	byte[] decoded = Base64.decode(trimDesc);
                  		                      	trimDesc = new String(decoded, "UTF-8");
                  		                      }
                  		                  	}
                  		                  	if(trimDesc != null)
                  		                  	{
                  		                  		dfTopic.setShortDescription(trimDesc);
                  		                  	}
                  											}
                  											else
                  												dfTopic.setShortDescription(propertyElement.getAttribute(VALUE));
                  										}
                  										if(TOPIC_LONG_DESC.equals(propertyElement.getAttribute(NAME)))
                  										{
                  											
                  											if(BASE64.equals(propertyElement.getAttribute(ENCODE)))
                  											{
                  												String topicDesc = propertyElement.getAttribute(VALUE);
                  		                  	String trimDesc = null;
                  		                  	if(topicDesc != null && topicDesc.length() >0)
                  		                  	{
                  		                      trimDesc = trimToNull(topicDesc);
                  		                      if (trimDesc != null && trimDesc.length() >0)
                  		                      {
                  		                      	byte[] decoded = Base64.decode(trimDesc);
                  		                      	trimDesc = new String(decoded, "UTF-8");
                  		                      }
                  		                  	}
                  		                  	if(trimDesc != null)
                  		                  	{
                  		                  		dfTopic.setExtendedDescription(trimDesc);
                  		                  	}
                  											}
                  											else
                  												dfTopic.setExtendedDescription(propertyElement.getAttribute(VALUE));                  											
                  										}
                  									}
                  								}
                  							}
                  						}
                  						if(propertiesElement.getTagName().equals(ATTACHMENT))
                  						{
  															String oldUrl = propertiesElement.getAttribute("relative-url");
  															if (oldUrl.startsWith("/content/attachment/"))
  															{
  																String newUrl = (String) attachmentNames.get(oldUrl);
  																if (newUrl != null)
  																{
  																	////if (newUrl.startsWith("/attachment/"))
  																		////newUrl = "/content".concat(newUrl);

  																	propertiesElement.setAttribute("relative-url", Validator
  																			.escapeQuestionMark(newUrl));
  																	
  																	attachStringList.add(Validator.escapeQuestionMark(newUrl));

  																}
  															}
  															else if (oldUrl.startsWith("/content/group/" + fromSiteId + "/"))
  															{
  																String newUrl = "/content/group/" + siteId
  																		+ oldUrl.substring(15 + fromSiteId.length());
  																propertiesElement.setAttribute("relative-url", Validator
  																		.escapeQuestionMark(newUrl));
  																
  																attachStringList.add(Validator.escapeQuestionMark(newUrl));
  															}
			
                  						}
                  					}
                  				}                  				

                      		List attachList = new ArrayList();
                      		for(int m=0; m<attachStringList.size(); m++)
                      		{
                      			Attachment tempAttach = messageManager.createAttachment();
                      			ContentResource cr = ContentHostingService.getResource((String)attachStringList.get(m));
                      			ResourceProperties rp = cr.getProperties();
                      			
                      			tempAttach.setAttachmentName(rp.getProperty(ResourceProperties.PROP_DISPLAY_NAME));
                      			tempAttach.setAttachmentSize(rp.getProperty(ResourceProperties.PROP_CONTENT_LENGTH));
                      			tempAttach.setAttachmentType(rp.getProperty(ResourceProperties.PROP_CONTENT_TYPE));
                      			tempAttach.setAttachmentUrl(cr.getUrl());
                      			tempAttach.setAttachmentId(cr.getId());
                      			
                      			attachList.add(tempAttach);
                      		}
                      		
                  				if(!hasTopic)
                  				{
                  					Area area = areaManager.getDiscusionArea();
                  					dfForum.setArea(area);
                  					dfForum.setDraft(new Boolean("false"));
                  					forumManager.saveDiscussionForum(dfForum, false);
                  				}
                      		hasTopic = true;
                      		dfTopic.setAttachments(attachList);
                    			dfForum.addTopic(dfTopic);
                    			forumManager.saveDiscussionForumTopic(dfTopic);
                  			}                  			
                  		}
                  	}

                  	if(!hasTopic)
                  	{
            					Area area = areaManager.getDiscusionArea();
            					dfForum.setArea(area);
            					dfForum.setDraft(new Boolean("false"));
            					forumManager.saveDiscussionForum(dfForum, false);
                  	}
                  }
                }
              }
            }
          }
        }
      }
      catch (Exception e)
      {     
        results.append("merging " + getLabel() + " failed.\n");
        e.printStackTrace();
      }

    }
		return null;
	}

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

	public boolean willArchiveMerge()
	{
		return true;
	}

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

	public MessageForumsForumManager getForumManager()
	{
		return forumManager;
	}

	public void setForumManager(MessageForumsForumManager forumManager)
	{
		this.forumManager = forumManager;
	}

	public AreaManager getAreaManager()
	{
		return areaManager;
	}

	public void setAreaManager(AreaManager areaManager)
	{
		this.areaManager = areaManager;
	}

	public String trimToNull(String value)
	{
		if (value == null) return null;
		value = value.trim();
		if (value.length() == 0) return null;
		return value;
	}

	public MessageForumsMessageManager getMessageManager()
	{
		return messageManager;
	}

	public void setMessageManager(MessageForumsMessageManager messageManager)
	{
		this.messageManager = messageManager;
	}

}