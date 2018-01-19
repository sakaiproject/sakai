/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.announcement.impl;

// import
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.Stack;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.message.api.MessageChannel;
import org.sakaiproject.message.api.MessageChannelEdit;
import org.sakaiproject.message.api.MessageEdit;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.util.BaseDbDoubleStorage;
import org.sakaiproject.util.DoubleStorageUser;
import org.sakaiproject.util.Xml;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.javax.PagingPosition;

/**
 * <p>
 * DbAnnouncementService fills out the BaseAnnouncementService with a database implementation.
 * </p>
 * <p>
 * The sql scripts in src/sql/chef_announcement.sql must be run on the database.
 * </p>
 */
@Slf4j
public class DbAnnouncementService extends BaseAnnouncementService
{
	/** The name of the db table holding announcement channels. */
	protected String m_cTableName = "ANNOUNCEMENT_CHANNEL";

	/** The name of the db table holding announcement messages. */
	protected String m_rTableName = "ANNOUNCEMENT_MESSAGE";

	/** If true, we do our locks in the remote database, otherwise we do them here. */
	protected boolean m_locksInDb = true;

	protected static final String[] FIELDS = { "MESSAGE_DATE", "OWNER", "DRAFT", "PUBVIEW","MESSAGE_ORDER" };

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Constructors, Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Dependency: SqlService */
	protected SqlService m_sqlService = null;

	/**
	 * Dependency: SqlService.
	 * 
	 * @param service
	 *        The SqlService.
	 */
	public void setSqlService(SqlService service)
	{
		m_sqlService = service;
	}

	/**
	 * Configuration: set the table name for the container.
	 * 
	 * @param path
	 *        The table name for the container.
	 */
	public void setContainerTableName(String name)
	{
		m_cTableName = name;
	}

	/**
	 * Configuration: set the table name for the resource.
	 * 
	 * @param path
	 *        The table name for the resource.
	 */
	public void setResourceTableName(String name)
	{
		m_rTableName = name;
	}

	/**
	 * Configuration: set the locks-in-db
	 * 
	 * @param path
	 *        The storage path.
	 */
	public void setLocksInDb(String value)
	{
		m_locksInDb = Boolean.valueOf(value).booleanValue();
	}

	/** Set if we are to run the to-draft/owner conversion. */
	protected boolean m_convertToDraft = false;

	/**
	 * Configuration: run the to-draft/owner conversion
	 * 
	 * @param value
	 *        The conversion desired value.
	 */
	public void setConvertDraft(String value)
	{
		m_convertToDraft = Boolean.valueOf(value).booleanValue();
	}

	/** Set if we are to run the to-pubview conversion. */
	protected boolean m_convertToPubView = false;

	/**
	 * Configuration: run the to-pubview conversion
	 * 
	 * @param value
	 *        The conversion desired value.
	 */
	public void setConvertPubView(String value)
	{
		m_convertToPubView = Boolean.valueOf(value).booleanValue();
	}

	/** Configuration: to run the ddl on init or not. */
	protected boolean m_autoDdl = false;

	/**
	 * Configuration: to run the ddl on init or not.
	 * 
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		m_autoDdl = new Boolean(value).booleanValue();
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// if we are auto-creating our schema, check and create
			if (m_autoDdl)
			{
				m_sqlService.ddl(this.getClass().getClassLoader(), "sakai_announcement");
			}

			super.init();

			log.info("init(): tables: {} {} locks-in-db: {}", m_cTableName, m_rTableName, m_locksInDb);

			// convert draft?
			if (m_convertToDraft)
			{
				m_convertToDraft = false;
				convertToDraft();
			}

			// convert pubview?
			if (m_convertToPubView)
			{
				m_convertToPubView = false;
				convertToPubView();
			}

		}
		catch (Throwable t)
		{
			log.warn("init(): ", t);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * BaseMessage extensions
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct a Storage object.
	 * 
	 * @return The new storage object.
	 */
	protected Storage newStorage()
	{
		return new DbStorage(this);

	} // newStorage

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected class DbStorage extends BaseDbDoubleStorage implements Storage
	{
		/**
		 * Construct.
		 * 
		 * @param user
		 *        The StorageUser class to call back for creation of Resource and Edit objects.
		 */
		public DbStorage(DoubleStorageUser user)
		{
			super(m_cTableName, "CHANNEL_ID", m_rTableName, "MESSAGE_ID", "CHANNEL_ID", "MESSAGE_DATE", "OWNER", "DRAFT",
					"PUBVIEW", FIELDS, m_locksInDb, "channel", "message", user, m_sqlService);

		} // DbStorage

		/** Channels * */

		public boolean checkChannel(String ref)
		{
			return super.getContainer(ref) != null;
		}

		public MessageChannel getChannel(String ref)
		{
			return (MessageChannel) super.getContainer(ref);
		}

		public List getChannels()
		{
			return super.getAllContainers();
		}

		public MessageChannelEdit putChannel(String ref)
		{
			return (MessageChannelEdit) super.putContainer(ref);
		}

		public MessageChannelEdit editChannel(String ref)
		{
			return (MessageChannelEdit) super.editContainer(ref);
		}

		public void commitChannel(MessageChannelEdit edit)
		{
			super.commitContainer(edit);
		}

		public void cancelChannel(MessageChannelEdit edit)
		{
			super.cancelContainer(edit);
		}

		public void removeChannel(MessageChannelEdit edit)
		{
			super.removeContainer(edit);
		}

		public List getChannelIdsMatching(String root)
		{
			return super.getContainerIdsMatching(root);
		}

		/** messages * */

		public boolean checkMessage(MessageChannel channel, String id)
		{
			return super.checkResource(channel, id);
		}

		public Message getMessage(MessageChannel channel, String id)
		{
			return (Message) super.getResource(channel, id);
		}

		public List getMessages(MessageChannel channel)
		{
			return super.getAllResources(channel);
		}

		public MessageEdit putMessage(MessageChannel channel, String id)
		{
			return (MessageEdit) super.putResource(channel, id, null);
		}

		public MessageEdit editMessage(MessageChannel channel, String id)
		{
			return (MessageEdit) super.editResource(channel, id);
		}

		public void commitMessage(MessageChannel channel, MessageEdit edit)
		{
			super.commitResource(channel, edit);
		}

		public void cancelMessage(MessageChannel channel, MessageEdit edit)
		{
			super.cancelResource(channel, edit);
		}

		public void removeMessage(MessageChannel channel, MessageEdit edit)
		{
			super.removeResource(channel, edit);
		}

		public int getCount(MessageChannel channel)
		{
			return super.getCount(channel);
		}

		public int getCount(MessageChannel channel, Filter filter)
		{
			return super.getCount(channel, filter);
		}
                
		public List getMessages(MessageChannel channel, Filter filter, boolean asc, PagingPosition pager)
		{       
			return super.getAllResources(channel, filter, null, asc, pager);
		}

		public List getMessages(MessageChannel channel, Time afterDate, int limitedToLatest, String draftsForId, boolean pubViewOnly)
		{
			return super.getResources(channel, afterDate, limitedToLatest, draftsForId, pubViewOnly);
		}

	} // DbStorage

	/**
	 * fill in the draft and owner db fields
	 */
	protected void convertToDraft()
	{
		log.info("convertToDraft");

		try
		{
			// get a connection
			final Connection connection = m_sqlService.borrowConnection();
			boolean wasCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);

			// read all message records that need conversion
			String sql = "select CHANNEL_ID, MESSAGE_ID, XML from " + m_rTableName /* + " where OWNER is null" */;
			m_sqlService.dbRead(connection, sql, null, new SqlReader()
			{
				private int count = 0;

				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// create the Resource from the db xml
						String channelId = result.getString(1);
						String messageId = result.getString(2);
						String xml = result.getString(3);

						// read the xml
						Document doc = Xml.readDocumentFromString(xml);

						// verify the root element
						Element root = doc.getDocumentElement();
						if (!root.getTagName().equals("message"))
						{
							log.warn("convertToDraft(): XML root element not message: {}", root.getTagName());
							return null;
						}
						Message m = new BaseMessageEdit(null, root);

						// pick up the fields
						String owner = m.getHeader().getFrom().getId();
						boolean draft = m.getHeader().getDraft();

						// update
						String update = "update " + m_rTableName
								+ " set OWNER = ?, DRAFT = ? where CHANNEL_ID = ? and MESSAGE_ID = ?";
						Object fields[] = new Object[4];
						fields[0] = owner;
						fields[1] = (draft ? "1" : "0");
						fields[2] = channelId;
						fields[3] = messageId;
						boolean ok = m_sqlService.dbWrite(connection, update, fields);

						if (!ok)
							log.info("convertToDraft: channel: {} message: {} owner: {} draft: {} ok: {}", channelId, messageId, owner, draft, ok);

						count++;
						if (count % 100 == 0)
						{
							log.info("convertToDraft: {}", count);
						}
						return null;
					}
					catch (Throwable ignore)
					{
						return null;
					}
				}
			});

			connection.commit();
			connection.setAutoCommit(wasCommit);
			m_sqlService.returnConnection(connection);
		}
		catch (Throwable t)
		{
			log.warn("convertToDraft: failed: " + t);
		}

		log.info("convertToDraft: done");
	}

	/**
	 * fill in the pubview db fields
	 */
	protected void convertToPubView()
	{
		log.info("convertToPubView");

		try
		{
			// get a connection
			final Connection connection = m_sqlService.borrowConnection();
			boolean wasCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);

			// read all message records that need conversion
			String sql = "select CHANNEL_ID, MESSAGE_ID, XML, PUBVIEW from " + m_rTableName;
			m_sqlService.dbRead(connection, sql, null, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// create the Resource from the db xml
						String channelId = result.getString(1);
						String messageId = result.getString(2);
						String xml = result.getString(3);
						String pubViewSetting = result.getString(4);

						// read the xml
						Document doc = Xml.readDocumentFromString(xml);

						// verify the root element
						Element root = doc.getDocumentElement();
						if (!root.getTagName().equals("message"))
						{
							log.warn("convertToPubView(): XML root element not message: {}", root.getTagName());
							return null;
						}
						BaseMessageEdit m = new BaseMessageEdit(null, root);

						// check if the record already has pub view set in the properties
						boolean pubview = false;
						if (m.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW) != null)
						{
							// pub view set in properties and in db indicates all is well with this one
							if ("1".equals(pubViewSetting))
							{
								return null;
							}

							// having the property overrides any realm setting...
							pubview = true;
						}

						// if we don't know pubview from the props, check the realm
						else
						{
							// m.getReference() won't work cause we didn't give it its channel...
							Reference channel = m_entityManager.newReference(channelId);
							String ref = messageReference(channel.getContext(), channel.getId(), m.getId());
							pubview = getPubView(ref);

							// if the pubview setting matches the db, and it's false, all is well
							if ((!pubview) && ("0".equals(pubViewSetting)))
							{
								return null;
							}
						}

						// update those that have no pubview
						if (!pubview)
						{
							String update = "update " + m_rTableName + " set PUBVIEW = ? where CHANNEL_ID = ? and MESSAGE_ID = ?";
							Object fields[] = new Object[3];
							fields[0] = "0";
							fields[1] = channelId;
							fields[2] = messageId;
							boolean ok = m_sqlService.dbWrite(connection, update, fields);

							if (!ok)
								log.info("convertToPubView: channel: {} message: {} pubview: {} ok: {}", channelId, messageId, pubview, ok);
						}

						// update those that have pubview
						else
						{
							// set the property
							m.getPropertiesEdit().addProperty(ResourceProperties.PROP_PUBVIEW, Boolean.TRUE.toString());

							// form updated XML
							doc = Xml.createDocument();
							m.toXml(doc, new Stack());
							xml = Xml.writeDocumentToString(doc);

							String update = "update " + m_rTableName
									+ " set PUBVIEW = ?, XML = ? where CHANNEL_ID = ? and MESSAGE_ID = ?";
							Object fields[] = new Object[4];
							fields[0] = "1";
							fields[1] = xml;
							fields[2] = channelId;
							fields[3] = messageId;
							boolean ok = m_sqlService.dbWrite(connection, update, fields);

							if (!ok)
								log.info("convertToPubView: channel: {} message: {} pubview: {} ok: {}", channelId, messageId, pubview, ok);
						}

						return null;
					}
					catch (Throwable ignore)
					{
						return null;
					}
				}
			});

			connection.commit();
			connection.setAutoCommit(wasCommit);
			m_sqlService.returnConnection(connection);
		}
		catch (Throwable t)
		{
			log.warn("convertToPubView: failed: " + t);
		}

		log.info("convertToPubView: done");
	}

	/**
	 * Does this resource support public view? (Support for the conversion)
	 * 
	 * @param ref
	 *        The resource reference
	 * @return true if this resource supports public view, false if not.
	 */
	protected boolean getPubView(String ref)
	{
		// get the realm
		try
		{
			AuthzGroup realm = m_authzGroupService.getAuthzGroup(ref);

			// if the announcement realm has "pubview" role, then the announcement is publicly viewable
			Role pubview = realm.getRole("pubview");
			if (pubview != null) return true;

			// if the announcement realm has the anonymous role and the anonymous
			// role contains content.read then the announcement is publicly viewable.
			// (Because the AuthzGroupService converts pubview role (in a realm)
			// to just .anon role with content.read function)
			Role anon = realm.getRole(".anon");

			if (anon != null && anon.getAllowedFunctions().contains("content.read"))
			{
				return true;
			}

			return false;

			// Set anon = realm.getAnonRoles();
			// if (!anon.contains(pubview))
			// return false;
			//
			// Set auth = realm.getAuthRoles();
			// if (!auth.contains(pubview))
			// return false;
		}
		catch (GroupNotDefinedException e)
		{
			// if no realm, no pub view
			return false;
		}
	}
}
