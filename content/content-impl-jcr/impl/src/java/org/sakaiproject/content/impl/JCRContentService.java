/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

package org.sakaiproject.content.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * <p>
 * DbContentService is an extension of the BaseContentService with a database
 * implementation.
 * </p>
 * <p>
 * The sql scripts in src/sql/chef_content.sql must be run on the database.
 * </p>
 */
public class JCRContentService extends DbContentService
{

	/** Our logger. */
	private static final Log log = LogFactory.getLog(JCRContentService.class);

	private JCRStorage storage;

	private SessionManager sessionManager;

	private UserDirectoryService userDirectoryService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#init()
	 */
	@Override
	public void init()
	{

		m_autoDdl = false;
		m_convertToFile = false;
		m_sqlService = null;
		// log in as admin
		Session session = null;
		try
		{
			session = sessionManager.startSession();
			User u = userDirectoryService.getUser("admin");
			session.setUserId(u.getId());
			sessionManager.setCurrentSession(session);
			log.info("Session Has been set to admin "+sessionManager.getCurrentSessionUserId());
			super.init();
		}
		catch (UserNotDefinedException e)
		{
			log.error("Admin User is not defined.... wierd!",e);
		}
		finally
		{
			try
			{
				sessionManager.setCurrentSession(null);
				session.invalidate();
			}
			catch (Exception ex)
			{
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#newStorage()
	 */
	@Override
	protected JCRStorage newStorage()
	{
		log.info("Retruning Storage as " + storage);
		return storage;
	}

	/**
	 * @return the storage
	 */
	public JCRStorage getStorage()
	{
		return storage;
	}

	/**
	 * @param storage
	 *        the storage to set
	 */
	public void setStorage(JCRStorage storage)
	{
		this.storage = storage;
	}

	/**
	 * The following overrides are in place to prevent DbContentService from
	 * using the database, These methods appear to have leaked out of the
	 * storage layer and should not really be here
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#countQuery(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	protected int countQuery(String sql, String param) throws IdUnusedException
	{
		log.error("Should not be using this countQuery with JCR");
		throw new UnsupportedOperationException(
				"Should not be using this countQuery with JCR");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#setUuidInternal(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	protected void setUuidInternal(String id, String uuid)
	{
		log.error("JCR uuids are immutable and cannot be set");
		throw new UnsupportedOperationException(
				"JCR uuids are immutable and cannot be set");
		// storage.setResourceUuid(id,uuid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService#moveCollection(org.sakaiproject.content.api.ContentCollectionEdit,
	 *      java.lang.String)
	 */
	@Override
	protected String moveCollection(ContentCollectionEdit thisCollection,
			String new_folder_id) throws PermissionException, IdUnusedException,
			TypeException, InUseException, OverQuotaException, IdUsedException,
			ServerOverloadException
	{
		return storage.moveCollection(thisCollection, new_folder_id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService#moveResource(org.sakaiproject.content.api.ContentResourceEdit,
	 *      java.lang.String)
	 */
	@Override
	protected String moveResource(ContentResourceEdit thisResource, String new_id)
			throws PermissionException, IdUnusedException, TypeException, InUseException,
			OverQuotaException, IdUsedException, ServerOverloadException
	{
		return storage.moveResource(thisResource, new_id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#findUuid(java.lang.String)
	 */
	@Override
	protected String findUuid(String id)
	{
		log
				.error("Should not be using this findUuid with JCR, getUuid dipuplicates the effort ");
		throw new UnsupportedOperationException(
				"Should not be using this findUuid with JCR");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#getUuid(java.lang.String)
	 */
	@Override
	public String getUuid(String id)
	{
		return storage.getUuid(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#setUuid(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public void setUuid(String id, String uuid) throws IdInvalidException
	{
		log.error("JCR uuids are immutable and cannot be set");
		throw new UnsupportedOperationException(
				"JCR uuids are immutable and cannot be set");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#resolveUuid(java.lang.String)
	 *      This method is crazy, why have resolve, find and get UUID that all
	 *      do the same thing ? Got to override it just in case :( I am almost
	 *      certain the the horrible catch throwable is not necessary, but its
	 *      there to make it behave the same as the DbContentService version
	 */
	@Override
	public String resolveUuid(String uuid)
	{
		try
		{
			return getUuid(uuid);
		}
		catch (Throwable t)
		{
			log.error("resolve UUID failed  for " + uuid);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#convertToFile()
	 */
	@Override
	protected void convertToFile()
	{
		log
				.error("JCR manages where data is stored, it is not possible with the service to convert a JCR from DB storage to File Storage, please contact your JCR supplier for a utility");
		throw new UnsupportedOperationException(
				"JCR manages where data is stored, it is not possible with the service to convert a JCR from DB storage to File Storage, please contact your JCR supplier for a utility");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService#findCollection(java.lang.String)
	 */
	@Override
	protected ContentCollection findCollection(String id) throws TypeException
	{
		// log.info("+++++++++ Start to find Collection "+id);
		ContentCollection cc = storage.getCollection(id);
		// log.info("========= End to find Collection "+id+" "+cc);
		return cc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService#findResource(java.lang.String)
	 */
	@Override
	protected ContentResource findResource(String id) throws TypeException
	{
		return storage.getResource(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#getCollectionSize(java.lang.String)
	 */
	@Override
	public int getCollectionSize(String id) throws IdUnusedException, TypeException,
			PermissionException
	{
		Exception e = new Exception("Traceback:");
		log.info("Get Collection Size called ", e);
		return 0; // TODO : something sensible
	}

	/**
	 * @return the sessionManager
	 */
	public SessionManager getSessionManager()
	{
		return sessionManager;
	}

	/**
	 * @param sessionManager the sessionManager to set
	 */
	public void setSessionManager(SessionManager sessionManager)
	{
		this.sessionManager = sessionManager;
	}

	/**
	 * @return the userDirectoryService
	 */
	public UserDirectoryService getUserDirectoryService()
	{
		return userDirectoryService;
	}

	/**
	 * @param userDirectoryService the userDirectoryService to set
	 */
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.BaseContentService#addResourceToDeleteTable(org.sakaiproject.content.api.ContentResourceEdit, java.lang.String, java.lang.String)
	 */
	@Override
	public void addResourceToDeleteTable(ContentResourceEdit edit, String uuid, String userId) throws PermissionException
	{
		log.warn("Delete table not implemented in JCR, managed by persistance manger ");
	}
}
