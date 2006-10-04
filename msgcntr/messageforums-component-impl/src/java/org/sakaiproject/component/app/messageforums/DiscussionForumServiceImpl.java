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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.Validator;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DiscussionForumServiceImpl  implements DiscussionForumService, EntityTransferrer
{
	private static final String MESSAGEFORUM = "messageforum";
	private static final String DISCUSSION_FORUM = "discussion_forum";
	private static final String DISCUSSION_TOPIC = "discussion_topic";
	private static final String DISCUSSION_FORUM_TITLE = "category";
	private static final String DISCUSSION_FORUM_DESC = "body";
	private static final String DISCUSSION_FORUM_SHORT_DESC = "summary";
	private static final String TOPIC_TITLE = "subject";
	private static final String DRAFT = "draft";
	private static final String LOCKED = "locked";
	private static final String PROPERTIES = "properties";
	private static final String PROPERTY = "property";
	private static final String TOPIC_SHORT_DESC = "Classic:bboardForums_description";
	private static final String TOPIC_LONG_DESC = "Classic:bboardForums_content";
	private static final String NAME = "name";
	private static final String ENCODE = "enc";
	private static final String BASE64 = "BASE64";
	private static final String VALUE = "value";
	private static final String ATTACHMENT = "attachment";
	private static final String ATTACH_URL = "relative_url";

	private static final String SITE_NAME = "siteName";
	private static final String SITE_ID = "siteId";
	private static final String SITE_ARCHIVE = "siteArchive";

	private MessageForumsForumManager forumManager;
	private AreaManager areaManager;
	private MessageForumsMessageManager messageManager;
	private MessageForumsTypeManager typeManager;
	private DiscussionForumManager dfManager;

	private static final Log LOG = LogFactory.getLog(DiscussionForumService.class);

	public void init()
	{
		EntityManager.registerEntityProducer(this, REFERENCE_ROOT);	
	}

	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		StringBuffer results = new StringBuffer();

		try { 	
			int forumCount = 0;
			results.append("archiving " + getLabel() + " context "
					+ Entity.SEPARATOR + siteId + Entity.SEPARATOR
					+ SiteService.MAIN_CONTAINER + ".\n");
			// start with an element with our very own (service) name
			Element element = doc.createElement(DiscussionForumService.class.getName());
			((Element) stack.peek()).appendChild(element);
			stack.push(element);

			if (siteId != null && siteId.trim().length() > 0) {
				Element siteElement = doc.createElement(SITE_ARCHIVE);
				siteElement.setAttribute(SITE_ID, SiteService.getSite(siteId).getId());
				siteElement.setAttribute(SITE_NAME, SiteService.getSite(siteId)
						.getTitle());

				Area dfArea = areaManager.getAreaByContextIdAndTypeId(siteId, typeManager.getDiscussionForumType());

				if (dfArea != null)
				{
					Element dfElement = doc.createElement(MESSAGEFORUM);

					List forums = dfManager.getDiscussionForumsByContextId(siteId);

					if (forums != null && !forums.isEmpty())
					{
						Iterator forumsIter = forums.iterator();
						while (forumsIter.hasNext())
						{
							DiscussionForum forum = (DiscussionForum)forumsIter.next();

							if (forum != null)
							{
								forumCount++;
								Element df_data = doc.createElement(DISCUSSION_FORUM);
								df_data.setAttribute(DISCUSSION_FORUM_TITLE, forum.getTitle());
								df_data.setAttribute(DRAFT, forum.getDraft().toString());
								df_data.setAttribute(LOCKED, forum.getLocked().toString());


								try {
									String encoded = Base64.encode(forum.getExtendedDescription().getBytes());
									df_data.setAttribute(DISCUSSION_FORUM_DESC, encoded);
								}
								catch(Exception e) {
									//LOG.warn("Encode DF Extended Desc - " + e);
									df_data.setAttribute(DISCUSSION_FORUM_DESC, "");
								}

								try {
									String encoded = Base64.encode(forum.getShortDescription().getBytes());
									df_data.setAttribute(DISCUSSION_FORUM_SHORT_DESC, encoded);
								}
								catch(Exception e) {
									//LOG.warn("Encode DF Short Desc - " + e);
									df_data.setAttribute(DISCUSSION_FORUM_SHORT_DESC, "");
								}


								//TODO archive attachments
								// need to use Reference
								/*List atts = forumManager.getForumById(true, forum.getId()).getAttachments();
								for (int i = 0; i < atts.size(); i++)
								{
									Element forum_attachment = doc.createElement(ATTACHMENT);
									String attachUrl = ((Attachment)atts.get(i)).getAttachmentUrl();

									// if it's in the attachment area, and not already in the list
									if ((attachUrl.startsWith("/content/attachment/")) && (!attachments.contains(attachUrl)))
									{
										attachments.add(attachUrl);
									}
									forum_attachment.setAttribute(ATTACH_URL, attachUrl);
									df_data.appendChild(forum_attachment);
								}*/


								List topicList = dfManager.getTopicsByIdWithMessagesAndAttachments(forum.getId());
								if (topicList != null && topicList.size() > 0) {
									Iterator topicIter = topicList.iterator();
									while (topicIter.hasNext()) {
										DiscussionTopic topic = (DiscussionTopic) topicIter.next();
										Element topic_data = doc.createElement(DISCUSSION_TOPIC);
										topic_data.setAttribute(TOPIC_TITLE, topic.getTitle());
										topic_data.setAttribute(DRAFT, topic.getDraft().toString());
										topic_data.setAttribute(LOCKED, topic.getLocked().toString());

										Element topic_properties = doc.createElement(PROPERTIES);
										Element topic_short_desc = doc.createElement(PROPERTY);

										try {
											String encoded = Base64.encode(topic.getShortDescription().getBytes());
											topic_short_desc.setAttribute(NAME, TOPIC_SHORT_DESC);
											topic_short_desc.setAttribute(ENCODE, BASE64);
											topic_short_desc.setAttribute(VALUE, encoded);
										} catch(Exception e) {
											//LOG.warn("Encode Topic Short Desc - " + e);
											topic_short_desc.setAttribute(NAME, TOPIC_SHORT_DESC);
											topic_short_desc.setAttribute(ENCODE, BASE64);
											topic_short_desc.setAttribute(VALUE, "");
										}


										topic_properties.appendChild(topic_short_desc);

										Element topic_long_desc = doc.createElement(PROPERTY);

										try {
											String encoded = Base64.encode(topic.getExtendedDescription().getBytes());
											topic_long_desc.setAttribute(NAME, TOPIC_LONG_DESC);
											topic_long_desc.setAttribute(ENCODE, BASE64);
											topic_long_desc.setAttribute(VALUE, encoded);
										} catch(Exception e) {
											//LOG.warn("Encode Topic Ext Desc - " + e);
											topic_long_desc.setAttribute(NAME, TOPIC_LONG_DESC);
											topic_long_desc.setAttribute(ENCODE, BASE64);
											topic_long_desc.setAttribute(VALUE, "");
										}


										topic_properties.appendChild(topic_long_desc);

										topic_data.appendChild(topic_properties);

										//TODO Archive attachments
										//Need to use attachment Reference
										/*List topicAtts = forumManager.getTopicByIdWithAttachments(topic.getId()).getAttachments();
										for (int j = 0; j < topicAtts.size(); j++)
										{
											Element topic_attachment = doc.createElement(ATTACHMENT);
											String attachUrl = ((Attachment)topicAtts.get(j)).getAttachmentUrl();

											// if it's in the attachment area, and not already in the list
											if ((attachUrl.startsWith("/content/attachment/")) && (!attachments.contains(attachUrl)))
											{
												attachments.add(attachUrl);
											}
											topic_attachment.setAttribute(ATTACH_URL, attachUrl);
											topic_data.appendChild(topic_attachment);
										}*/

										df_data.appendChild(topic_data);
									}
								}

								dfElement.appendChild(df_data);
							}
						}

						siteElement.appendChild(dfElement);      
					}
					results.append("archiving " + getLabel() + ": (" + forumCount
							+ ") messageforum DF items archived successfully.\n");
				}
				else
				{
					results.append("archiving " + getLabel()
							+ ": empty messageforum DF archived.\n");
				}
				((Element) stack.peek()).appendChild(siteElement);
				stack.push(siteElement);
			}
			stack.pop();

		}
		catch (DOMException e)
		{
			LOG.error(e.getMessage(), e);
		}
		catch (IdUnusedException e)
		{
			LOG.error(e.getMessage(), e);
		}
		return results.toString();
	}

	public Entity getEntity(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getEntityAuthzGroups(Reference ref, String userId)
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

	public String[] myToolIds()
	{
		String[] toolIds = { "sakai.messagecenter" };
		return toolIds;
	}

	public void transferCopyEntities(String fromContext, String toContext, List ids) {
		try 
		{
			LOG.debug("transfer copy mc items by transferCopyEntities");

			List fromDfList = dfManager.getDiscussionForumsByContextId(fromContext);

			if (fromDfList != null && !fromDfList.isEmpty()) {
				for (int currForum = 0; currForum < fromDfList.size(); currForum++) {
					DiscussionForum fromForum = (DiscussionForum)fromDfList.get(currForum);
					Long fromForumId = fromForum.getId();

					DiscussionForum newForum = forumManager.createDiscussionForum();
					newForum.setTitle(fromForum.getTitle());

					if (fromForum.getShortDescription() != null && fromForum.getShortDescription().length() > 0) 
						newForum.setShortDescription(fromForum.getShortDescription());

					if (fromForum.getExtendedDescription() != null && fromForum.getExtendedDescription().length() > 0) 
						newForum.setExtendedDescription(fromForum.getExtendedDescription());

					newForum.setDraft(fromForum.getDraft());
					newForum.setLocked(fromForum.getLocked());

					// get permissions
					Set membershipItemSet = fromForum.getMembershipItemSet();

					if (membershipItemSet != null && !membershipItemSet.isEmpty()) {
						Iterator membershipIter = membershipItemSet.iterator();
						while (membershipIter.hasNext()) {
							DBMembershipItem item = (DBMembershipItem)membershipIter.next();
							newForum.addMembershipItem(item);
						}
					}

					// get/add the forum's attachments
					List fromAttach = forumManager.getForumById(true, fromForumId).getAttachments();
					if (fromAttach != null && !fromAttach.isEmpty()) {
						for (int currAttach=0; currAttach < fromAttach.size(); currAttach++) {                   			
							Attachment thisAttach = (Attachment)fromAttach.get(currAttach);
							Attachment newAttach = copyAttachment(thisAttach);

							if (newForum != null)
								newForum.addAttachment(newAttach);
						}
					}       		

					// save the forum
					Area area = areaManager.getDiscusionArea();
					newForum.setArea(area);
					newForum.setDraft(new Boolean("false"));
					forumManager.saveDiscussionForum(newForum, false);

					// get/add the topics
					List topicList = dfManager.getTopicsByIdWithMessagesAndAttachments(fromForumId);
					if (topicList != null && !topicList.isEmpty()) {
						for (int currTopic = 0; currTopic < topicList.size(); currTopic++) {
							DiscussionTopic fromTopic = (DiscussionTopic)topicList.get(currTopic);
							Long fromTopicId = fromTopic.getId();

							DiscussionTopic newTopic = forumManager.createDiscussionForumTopic(newForum);

							newTopic.setTitle(fromTopic.getTitle());
							if (fromTopic.getShortDescription() != null && fromTopic.getShortDescription().length() > 0)
								newTopic.setShortDescription(fromTopic.getShortDescription());
							if (fromTopic.getExtendedDescription() != null && fromTopic.getExtendedDescription().length() > 0)
								newTopic.setExtendedDescription(fromTopic.getExtendedDescription());
							newTopic.setLocked(fromTopic.getLocked());
							newTopic.setDraft(fromTopic.getDraft());

							// Get/set the topic's permissions
							Set topicMembershipItemSet = fromTopic.getMembershipItemSet();

							if (topicMembershipItemSet != null && !topicMembershipItemSet.isEmpty()) {
								Iterator membershipIter = topicMembershipItemSet.iterator();
								while (membershipIter.hasNext()) {
									DBMembershipItem item = (DBMembershipItem)membershipIter.next();
									newTopic.addMembershipItem(item);
								}
							}
							// Add the attachments
							List fromTopicAttach = forumManager.getTopicByIdWithAttachments(fromTopicId).getAttachments();
							if (fromTopicAttach != null && !fromTopicAttach.isEmpty()) {
								for (int topicAttach=0; topicAttach < fromTopicAttach.size(); topicAttach++) {                   			
									Attachment thisAttach = (Attachment)fromTopicAttach.get(topicAttach);
									Attachment newAttach = copyAttachment(thisAttach);

									if (newForum != null)
										newForum.addAttachment(newAttach);

									newTopic.addAttachment(newAttach);
								}			
							}				

							forumManager.saveDiscussionForumTopic(newTopic);
						}
					}	
				}
			}			
		}

		catch (Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
		}
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

										String forumDraft = forumElement.getAttribute(DRAFT);
										if(forumDraft != null && forumDraft.length() >0)
											dfForum.setDraft(new Boolean(forumDraft));

										String forumLocked = forumElement.getAttribute(LOCKED);
										if(forumLocked != null && forumLocked.length() >0)
											dfForum.setLocked(new Boolean(forumLocked));

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

										String forumShortDesc = forumElement.getAttribute(DISCUSSION_FORUM_SHORT_DESC);
										String trimSummary = null;
										if(forumShortDesc != null && forumShortDesc.length() >0)
										{
											trimSummary = trimToNull(forumShortDesc);
											if (trimSummary != null && trimSummary.length() >0)
											{
												byte[] decoded = Base64.decode(trimBody);
												trimSummary = new String(decoded, "UTF-8");
											}
										}
										if(trimSummary != null)
										{
											dfForum.setShortDescription(trimSummary);
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

													String topicLocked = topicElement.getAttribute(LOCKED);
													if(topicLocked != null && topicLocked.length() >0)
														dfTopic.setLocked(new Boolean(topicLocked));

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

	private Attachment copyAttachment(Attachment thisAttach) {
		Attachment newAttach = messageManager.createAttachment();
		try {
			ContentResource oldAttachment = ContentHostingService.getResource(thisAttach.getAttachmentId());
			ContentResource attachment = ContentHostingService.addAttachmentResource(
					oldAttachment.getProperties().getProperty(
							ResourceProperties.PROP_DISPLAY_NAME), ToolManager
							.getCurrentPlacement().getContext(), ToolManager.getTool(
							"sakai.messagecenter").getTitle(), oldAttachment.getContentType(),
							oldAttachment.getContent(), oldAttachment.getProperties());

			newAttach.setAttachmentId(attachment.getId());
			newAttach.setAttachmentUrl(attachment.getUrl());
			newAttach.setAttachmentName(attachment.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
			newAttach.setAttachmentSize(attachment.getProperties().getProperty(ResourceProperties.PROP_CONTENT_LENGTH));
			newAttach.setAttachmentType(attachment.getProperties().getProperty(ResourceProperties.PROP_CONTENT_TYPE));
		} catch(Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
		}

		return newAttach;
	}

	public MessageForumsMessageManager getMessageManager()
	{
		return messageManager;
	}

	public void setMessageManager(MessageForumsMessageManager messageManager)
	{
		this.messageManager = messageManager;
	}

	public MessageForumsTypeManager getTypeManager()
	{
		return typeManager;
	}

	public void setTypeManager(MessageForumsTypeManager typeManager)
	{
		this.typeManager = typeManager;
	}

	public DiscussionForumManager getDfManager() {
		return dfManager;
	}

	public void setDfManager(DiscussionForumManager dfManager) {
		this.dfManager = dfManager;
	}

}