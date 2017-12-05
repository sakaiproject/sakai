/**
 * $Id: $
 * $URL: $
 *
 **************************************************************************
 * Copyright (c) 2013 The Apereo Foundation
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
 */
package org.sakaiproject.content;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Job to remove old delete content from content hosting. This is in a separate
 * project as the kernel (content hosting) can't bind to stuff outside it.
 * 
 * @author buckett
 * 
 */
@Slf4j
public class CleanupDeletedContent implements Job {

	private ContentHostingService chs;
	private ServerConfigurationService scs;
	private TimeService ts;
	private SessionManager sm;

	public void setContentHostingService(ContentHostingService chs) {
		this.chs = chs;
	}

	public void setServerConfigurationService(ServerConfigurationService scs) {
		this.scs = scs;
	}

	public void setTimeService(TimeService ts) {
		this.ts = ts;
	}
	
	public void setSessionManager(SessionManager sm) {
		this.sm = sm;
	}

	public void execute(JobExecutionContext context)
	throws JobExecutionException {
		Session sakaiSession = sm.getCurrentSession();
		sakaiSession.setUserId("admin");
		sakaiSession.setUserEid("admin");

		List<ContentResource> deleted = (List<ContentResource>) chs
				.getAllDeletedResources("/");
		long daysToKeep = scs.getInt("content.keep.deleted.files.days", 30);
		Time oldest = ts.newTime(System.currentTimeMillis()
				- (daysToKeep * 1000 * 60 * 60 * 24));
		log.info("Looking at " + deleted.size()
				+ " resources, and removing anything older than: " + oldest.toStringLocalDate());
		int removed = 0, attempted = 0;
		long totalSize = 0, removedSize = 0;
		for (ContentResource resource : deleted) {
			// We can't get at the deleted field in the DB but the modification
			// time is updated when it's deleted.
			Object property = resource.getProperties().get(ResourceProperties.PROP_MODIFIED_DATE);
			long size = resource.getContentLength();
			totalSize += size;
			if (property != null && property instanceof String) {
				Time time = ts.newTimeGmt((String)property);
				if (oldest.after(time)) {
					try {
						attempted++;
						chs.removeDeletedResource(resource.getId());
						removed++;
						removedSize += size;
					} catch (PermissionException e) {
						log.warn("Failed to remove due to lack of permission: "
								+ resource.getId());
					} catch (IdUnusedException e) {
						log
								.warn("Failed to remove due to not beging able to find: "
										+ resource.getId());
					} catch (TypeException e) {
						log.warn("Failed to remove due to type exception: "
								+ resource.getId());
					} catch (InUseException e) {
						log.warn("Failed to remove due resource being in use: "
								+ resource.getId());
					}
				}
				else {
					log.debug("Resource {} is still too new, skipping.", resource.getId());
				}
			} 
			else {
				log.warn("No modified date set for file with id {}. Cannot process for deletion.", resource.getId());
			}
		}
		int failed = attempted - removed;
		log.info("Out of {}(~{}) deleted resources, successfully removed {}(~{}), failed resources: {}",
				deleted.size(), formatSize(totalSize), removed, formatSize(removedSize), failed);
	}
	
	static String formatSize(long size) {
		if (size >= 1L<<30) {
			return ""+ (size / 1L>>30)+ "Gb";
		} else if (size >= 1L<<20) {
			return ""+ (size / 1L>>20)+ "Mb";
		} else if (size >= 1L<<10) {
			return ""+ (size / 1L>>10)+ "kb";
		} else {
			return ""+ size+ "b";
		}
	}

}
