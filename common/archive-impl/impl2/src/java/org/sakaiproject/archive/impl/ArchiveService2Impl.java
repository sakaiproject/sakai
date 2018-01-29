/***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.archive.impl;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.archive.api.ArchiveService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;

@Slf4j
public class ArchiveService2Impl implements ArchiveService
{
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
	
	/** Dependency: SiteZipper */
	protected SiteZipper m_siteZipper = null;
	public void setSiteZipper(SiteZipper siteZipper) {
		m_siteZipper = siteZipper;
	}
	
	/*********************************************/
	/* Injected Default Settings                 */
	/*********************************************/
	/** A full path and file name to the storage file. */
	protected String m_storagePath = "/";
	public void setStoragePath(String path) {
		m_storagePath = path;
	}
	
	/** Path used for processing zips **/
	protected String m_unzipPath = "/";
	public void setUnzipPath(String unzipPath) {
		m_unzipPath = unzipPath;
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

	    m_storagePath = m_serverConfigurationService.getString("archive.storage.path", m_storagePath);
		if (!StringUtils.endsWith(m_storagePath,"/")) {
			m_storagePath = m_storagePath + "/";
		}
		
		m_unzipPath = m_serverConfigurationService.getString("archive.unzip.path", m_unzipPath);
		if(!StringUtils.endsWith(m_unzipPath, "/")) {
			m_unzipPath = m_unzipPath + "/";
		}
		
		m_filterSakaiServices = m_serverConfigurationService.getBoolean("archive.merge.filter.services", m_filterSakaiServices);
		m_filterSakaiRoles = m_serverConfigurationService.getBoolean("archive.merge.filter.roles", m_filterSakaiRoles);
		String[] filteredServices = m_serverConfigurationService.getStrings("archive.merge.filtered.services");
		if (filteredServices != null) {
		    m_filteredSakaiServices = filteredServices;
		}
        String[] filteredRoles = m_serverConfigurationService.getStrings("archive.merge.filtered.roles");
        if (filteredRoles != null) {
            m_filteredSakaiRoles = filteredRoles;
        }
		
		log.info("init(): storage path: " + m_storagePath + ", unzip path: " + m_unzipPath + ", merge filter{services="+m_filterSakaiServices+", roles="+m_filterSakaiRoles+"}");
		if (!new File(m_storagePath).isDirectory()) {
			log.warn("Failed to find directory {} please create or configure {}.", m_storagePath, "archive.storage.path");
		}
		if (!new File(m_unzipPath).isDirectory()) {
			log.warn("Failed to find directory {} please create or configure {}.", m_unzipPath, "archive.unzip.path");
		}

	}

	public void destroy() {
		log.info("destroy()");
	}

	
	/**
	* Create an archive for the resources of a site.
	* @param siteId The id of the site to archive.
	* @return A log of messages from the archive.
	*/
	public String archive(String siteId)
	{
		return m_siteArchiver.archive(siteId, m_storagePath, FROM_SAKAI_2_8);
	}

	/**
	* Process a merge for the file, or if it's a directory, for all contained files (one level deep).
	* @param fileName The site name (for the archive file) to read from.
	* @param siteId The site ID into which to merge the contents of the archive.
	* @param creatorId The creator id
	* If null or blank, the date/time string of the merge is used.
	*/
	public String merge(String fileName, String siteId, String creatorId)
	{
		return m_siteMerger.merge(fileName, siteId, creatorId, m_storagePath, m_filterSakaiServices, m_filteredSakaiServices, m_filterSakaiRoles, m_filteredSakaiRoles);
	}

	@Override
	public String mergeFromZip(String zipFilePath, String siteId, String creatorId) {
		try {
			String folderName = m_siteZipper.unzipArchive(zipFilePath, m_unzipPath);
			//not a lot we can do with the return value here since it always returns a string. would need a reimplementation/wrapper method to return a better value (boolean or some status)
			if (folderName == null || folderName.isEmpty()) {
				return "Failed to find folder in zip archive";
			}
			try {
				return m_siteMerger.merge(folderName, siteId, creatorId, m_unzipPath, m_filterSakaiServices, m_filteredSakaiServices, m_filterSakaiRoles, m_filteredSakaiRoles);
			} finally {
				FileUtils.deleteDirectory(new File(m_unzipPath,folderName));
			}
		} catch (IOException e) {
			log.error("Error merging from zip: " + e.getClass() + ":" + e.getMessage());
			return "Error merging from zip: " + e.getClass() + ":" + e.getMessage();
		}
	}

	@Override
	public String archiveAndZip(String siteId) throws IOException {
		String log = m_siteArchiver.archive(siteId, m_storagePath, FROM_SAKAI_2_8);
		if (m_siteZipper.zipArchive(siteId, m_storagePath) ){
			log = log + "Zipfile success.\n";
		} else {
			log = log + "Zipfile failed\n";
		}
		return log;
	}
	
} // ArchiveService2Impl
