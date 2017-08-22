/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.component.app.scheduler.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.*;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * Job to make LB-CSS folder hidden but contents accessible.
 * LB-CSS folder should not be visible to access user, this job will hide all existing non-hidden LB-CSS folder.
 * @author neelam
 *
 */
@DisallowConcurrentExecution
public class LBCSSFolderHideJob implements Job {

	private SqlService sqlService;
	private SessionManager sessionManager;

	private static final Log LOG = LogFactory.getLog(LBCSSFolderHideJob.class);
	
	private ContentHostingService contentHostingService;

	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		ContentCollectionEdit contentCollectionEdit;
		ResourcePropertiesEdit resourceProperties;
		Session session = null;
		//sql to get collection ids for all LB-CSS folder
		String sql = "SELECT COLLECTION_ID FROM CONTENT_COLLECTION WHERE COLLECTION_ID LIKE \"%LB-CSS/\" ";
		LOG.debug("SQL to get collection Id for LB-CSS folder " + sql);
		int totalFolders = 0 , foldersUpdated = 0, foldersAlreadySet = 0;
		//set current user in session
		try {
			session = sessionManager.getCurrentSession();
			session.setUserEid("admin");
			session.setUserId("admin");
			List<String> collection_ids = sqlService.dbRead(sql);
			totalFolders = collection_ids.size();
			LOG.info("Number of LB-CSS folders found is " + totalFolders);
			for (String id : collection_ids) {
				try {
					contentCollectionEdit = contentHostingService.editCollection(id);
					resourceProperties = contentCollectionEdit.getPropertiesEdit();
					//if LB-CSS folder is not hidden, hide it
					if (!("true".equals(resourceProperties.getProperty(ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT)))) {
						resourceProperties.addProperty(ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT, "true");
						foldersUpdated++;
						LOG.info("Hiding the collection" + id +  " from access user ");
						contentHostingService.commitCollection(contentCollectionEdit);
					}
					else {
						foldersAlreadySet++;
						contentHostingService.cancelCollection(contentCollectionEdit);
						LOG.info("The collection " + id +" has already been hidden ");
					}
				} catch (SakaiException exception) {
					LOG.error("Failed to update the LB-CSS folder: " + id, exception);
				}
			}
		}
		finally {
			LOG.info("Summary of LBCSSFolderHideJob , Total LB-CSS folders: " + totalFolders + " Updated: " +
					foldersUpdated + " Collection already hidden: " + foldersAlreadySet);
			session.invalidate();
		}
	}

}
