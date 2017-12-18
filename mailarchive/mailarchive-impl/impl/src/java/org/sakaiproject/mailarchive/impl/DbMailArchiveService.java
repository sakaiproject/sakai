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

package org.sakaiproject.mailarchive.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.message.api.MessageChannel;
import org.sakaiproject.message.api.MessageChannelEdit;
import org.sakaiproject.message.api.MessageEdit;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.util.BaseDbDoubleStorage;
import org.sakaiproject.util.DoubleStorageUser;
import org.sakaiproject.util.Xml;

/**
 * <p>
 * DbMailArchiveService fills out the BaseMailArchiveService with a database implementation.
 * </p>
 * <p>
 * The sql scripts in src/sql/chef_mailarchive.sql must be run on the database.
 * </p>
 */
@Slf4j
public class DbMailArchiveService extends BaseMailArchiveService
{
	/** The name of the db table holding mail archive channels. */
	protected String m_cTableName = "MAILARCHIVE_CHANNEL";

	/** The name of the db table holding mail archive messages. */
	protected String m_rTableName = "MAILARCHIVE_MESSAGE";

	/** If true, we do our locks in the remote database, otherwise we do them here. */
	protected boolean m_locksInDb = true;

	protected static final String[] FIELDS = { "MESSAGE_DATE", "OWNER", "DRAFT", "PUBVIEW", "SUBJECT", "BODY"};

	protected static final String[] SEARCH_FIELDS = { "OWNER", "SUBJECT", "BODY" };

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
		m_autoDdl = Boolean.valueOf(value).booleanValue();
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
				m_sqlService.ddl(this.getClass().getClassLoader(), "sakai_mailarchive");
				m_sqlService.ddl(this.getClass().getClassLoader(), "sakai_mailarchive_2_6_0");
			}

			super.init();

			log.info("init(): tables: " + m_cTableName + " " + m_rTableName + " locks-in-db: " + m_locksInDb);

			// convert?
			if (m_convertToDraft)
			{
				m_convertToDraft = false;
				convertToDraft();
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
					"PUBVIEW", FIELDS, SEARCH_FIELDS, m_locksInDb, "channel", "message", user, m_sqlService);
			m_locksAreInTable = false;
		} // DbStorage
        
		/* matchXml - Optionaly do a pre-de-serialize match
		 *
		 * A call back to match before the XML is parsed and turned into a
		 * Resource.  If we can decide here - it is more efficient than 
		 * sending the XML through SAX.
		 */
		@Override
		public int matchXml(String xml, String search) 
		{

			if (!xml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"))
				return 0;

			/*
			 * <?xml version="1.0" encoding="UTF-8"?> <message
			 * body="Qm9keSAyMDA4MDEyNzIwMTM0MTkzMw=="
			 * body-html="Qm9keSAyMDA4MDEyNzIwMTM0MTkzMw=="> <header
			 * access="channel" date="20080127201341934" from="admin"
			 * id="d978685c-8730-4975-b3ea-55fdf03e0e5a"
			 * mail-date="20080127201341933" mail-from="from 20080127201341933"
			 * subject="Subject 20080127201341933"/><properties/></message>
			 */
			String body = getXmlAttr(xml, "body");
			String from = getXmlAttr(xml, "from");
			String subject = getXmlAttr(xml, "subject");
			if (body == null || from == null || subject == null)
				return 0;

			try 
			{
				byte[] decoded = Base64.decodeBase64(body); // UTF-8 by default
                body = org.apache.commons.codec.binary.StringUtils.newStringUtf8(decoded);
			} 
			catch (Exception e) 
			{
				log.warn("Exception decoding message body: " + e);
				return 0;
			}

			if (StringUtils.containsIgnoreCase(subject, search)
					|| StringUtils.containsIgnoreCase(from, search)
					|| StringUtils.containsIgnoreCase(body, search)) 
			{
				return 1;
			}
			return -1;
		}

		String getXmlAttr(String xml, String tagName)
		{
			String lookfor = tagName+"=\""; 
			int ipos = xml.indexOf(lookfor);
			if ( ipos < 1 ) return null;
			ipos = ipos + lookfor.length();
			int jpos = xml.indexOf("\"",ipos);
			if ( jpos < 1 || ipos > jpos ) return null;
			return xml.substring(ipos,jpos);
		}
        
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

		public List getMessages(MessageChannel channel,String search, boolean asc, PagingPosition pager)
		{
			return super.getAllResources(channel, null, search, asc, pager);
		}
        
		public int getCount(MessageChannel channel)
		{
			return super.getCount(channel);
		}
        
        	public int getCount(MessageChannel channel, Filter filter)
        	{
			return super.getCount(channel, filter);
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

		public List getMessages(MessageChannel channel, Time afterDate, int limitedToLatest, String draftsForId, boolean pubViewOnly)
		{
			return super.getResources(channel, afterDate, limitedToLatest, draftsForId, pubViewOnly);
		}
 
		public List getMessages(MessageChannel channel, Filter filter,boolean asc, PagingPosition pager) 
		{
			return super.getAllResources(channel,filter, null, asc, pager);
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
							log.warn("convertToDraft(): XML root element not message: " + root.getTagName());
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
							log.info("convertToDraft: channel: " + channelId + " message: " + messageId + " owner: "
									+ owner + " draft: " + draft + " ok: " + ok);

						count++;
						if (count % 100 == 0)
						{
							log.info("convertToDraft: " + count);
						}
						return null;
					}
					catch (Exception ignore)
					{
						return null;
					}
				}
			});

			connection.commit();
			connection.setAutoCommit(wasCommit);
			m_sqlService.returnConnection(connection);
		}
		catch (Exception t)
		{
			log.warn("convertToDraft: failed: " + t);
		}

		log.info("convertToDraft: done");
	}

} // DbCachedMailArchiveService
