/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import lombok.extern.slf4j.Slf4j;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import org.springframework.beans.factory.annotation.Autowired;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Class to load all the sites, check if they have a provided group and if so refresh
 * the authz groups. We do this through authz groups so we don't load the site objects.
 *
 * @author Matthew Buckett
 */
@DisallowConcurrentExecution
@Slf4j
public class AuthzGroupProviderSync implements InterruptableJob {
	// If it's been modified in the last hour ignore it.
	private long refreshAge = 3600000;
	
	private int batchSize = 200;

	private boolean run = true;
	
	private SessionManager sessionManager;
	private AuthzGroupService authzGroupService;

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		Session session = sessionManager.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");
		int groupsTotal = 0, groupsProcessed = 0, groupsUpdated = 0, groupsNoProvider = 0, groupsTooNew = 0;
		long start = System.currentTimeMillis();
		try {
			groupsTotal = authzGroupService.countAuthzGroups(null);
			Iterator<AuthzGroup> groupsIt = getAuthzGroups();
			while (groupsIt.hasNext() && run) {
				AuthzGroup group = groupsIt.next();
				groupsProcessed++;
				if (group.getProviderGroupId() != null && group.getProviderGroupId().length() > 0) {
					if (System.currentTimeMillis() - group.getModifiedTime().getTime() > refreshAge) {
						try {
							// Need to load the group before we can save it.
							AuthzGroup groupToRefresh = authzGroupService.getAuthzGroup(group.getId());
							authzGroupService.save(groupToRefresh);
							groupsUpdated++;
						} catch (GroupNotDefinedException e) {
							log.warn("Failed to update group ("+ group.getReference()+ "), maybe deleted while processing");
						} catch (AuthzPermissionException e) {
							log.error("Lack of permission to update group: "+ group.getReference());
							throw new JobExecutionException(e);
						}
					} else {
						groupsTooNew++;
						if (log.isDebugEnabled()) {
							log.debug("Ignored group as it has been updated too recently: "+ group.getReference());
						}
					}
				} else {
					groupsNoProvider++;
					if (log.isDebugEnabled()) {
						log.debug("Ignored group as it doesn't have any provided groups: "+ group.getReference());
					}
				}
			}
		} finally {
			long duration = System.currentTimeMillis() - start;
			log.info("Summary (duration: "+ duration+ ") -"+
					" Total: "+ groupsTotal+
					" Processed: "+ groupsProcessed+
					" Updated: "+ groupsUpdated+
					" No Provider: "+ groupsNoProvider+
					" Too New: "+ groupsTooNew+
					((run)?" finished.":" stopped early.")
					);
			session.invalidate();
		}
	}

	/**
	 * Get an iterator for the authzgroups and load them in chunks.
	 * It is possible that we miss one if the order changes while we are doing this
	 * but as we are just syncing stuff it's not too crucial.
	 */
	private Iterator<AuthzGroup> getAuthzGroups() {
		return new Iterator<AuthzGroup>() {
			
			private int current = 1;
			private int size = batchSize;
			private boolean tryGetMore = true;
			private Iterator<AuthzGroup> internalIt = Collections.<AuthzGroup>emptyList().iterator();
			
			private boolean checkOrLoadNext() {
				if (internalIt.hasNext()) {
					return true;
				}
				if (tryGetMore) {
					List<AuthzGroup> groups = authzGroupService.getAuthzGroups(null, new PagingPosition(current, current+size));
					if (groups.size() < size) {
						tryGetMore = false;
					}
					current += groups.size();
					internalIt = groups.iterator();
				}
				return internalIt.hasNext();
			}

			public boolean hasNext() {
				return checkOrLoadNext();
			}

			public AuthzGroup next() {
				if (checkOrLoadNext()) {
					return internalIt.next();
				}
				throw new NoSuchElementException();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Autowired(required = false)
	public void setRefreshAge(long refreshAge) {
		this.refreshAge = refreshAge;
	}

	@Autowired(required = false)
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	@Autowired
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	@Autowired
	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	@Override
	public void interrupt() throws UnableToInterruptJobException {
		run = false;
	}
}
