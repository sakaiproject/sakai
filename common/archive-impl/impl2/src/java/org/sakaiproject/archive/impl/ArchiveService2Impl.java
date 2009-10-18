/***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.archive.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.archive.api.ArchiveService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;

public class ArchiveService2Impl implements ArchiveService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(ArchiveService2Impl.class);

	/*******************************************************************************
	* Dependencies and their setter methods
	*******************************************************************************/

	/** Dependency: ServerConfigurationService. */
	protected ServerConfigurationService m_serverConfigurationService = null;
	public void setServerConfigurationService(ServerConfigurationService service) {
		m_serverConfigurationService = service;
	}

	/** Dependency: EntityManager. */
	protected EntityManager m_entityManager = null;
	public void setEntityManager(EntityManager service) {
		m_entityManager = service;
	}
	
	/** Dependency: SiteArchiver */
	protected SiteArchiver m_siteArchiver = null;
	public void setSiteArchiver(SiteArchiver siteArchiver) {
		m_siteArchiver = siteArchiver;
	}
	
	/** Dependency: SiteMerger */
	protected SiteMerger m_siteMerger = null;
	public void setSiteMerger(SiteMerger siteMerger) {
		m_siteMerger = siteMerger;
	}
	
	/*********************************************/
	/* Injected Default Settings                 */
	/*********************************************/
	/** A full path and file name to the storage file. */
	protected String m_storagePath = "/";
	public void setStoragePath(String path) {
		m_storagePath = path;
	}
	
	protected boolean m_filterSakaiServices = false;
	public void setMergeFilterSakaiServices(boolean filter) {
		m_filterSakaiServices = filter;
	}
	
	protected boolean m_filterSakaiRoles = false;
	public void setMergeFilterSakaiRoles(boolean filter) {
		m_filterSakaiRoles = filter;
	}
	
	protected String[] m_filteredSakaiServices = null;
	public void setMergeFilteredSakaiServices(String[] filtered) {
		m_filteredSakaiServices = filtered;
	}
	
	protected String[] m_filteredSakaiRoles = null;
	public void setMergeFilteredSakaiRoles(String[] filtered) {
		m_filteredSakaiRoles = filtered;
	}

	/*******************************************************************************
	* Init and Destroy
	*******************************************************************************/
	public void init() {
		
		if ((m_storagePath != null) && (!m_storagePath.endsWith("/"))) {
			m_storagePath = m_storagePath + "/";
		}

		M_log.info("init(): storage path: " + m_storagePath);
	}

	public void destroy() {
		M_log.info("destroy()");
	}

	
	/**
	* Create an archive for the resources of a site.
	* @param siteId The id of the site to archive.
	* @return A log of messages from the archive.
	*/
	public String archive(String siteId)
	{
		return m_siteArchiver.archive(siteId, m_storagePath, FROM_SAKAI);
	}

	/**
	* Process a merge for the file, or if it's a directory, for all contained files (one level deep).
	* @param fileName The site name (for the archive file) to read from.
	* @param mergeId The id string to use to make ids in the merge consistent and unique.
	* @param creatorId The creator id
	* If null or blank, the date/time string of the merge is used.
	*/
	public String merge(String fileName, String siteId, String creatorId)
	{
		return m_siteMerger.merge(fileName, siteId, creatorId, m_storagePath, m_filterSakaiServices, m_filteredSakaiServices, m_filterSakaiRoles, m_filteredSakaiRoles);
	}
	
} // ArchiveService2Impl
