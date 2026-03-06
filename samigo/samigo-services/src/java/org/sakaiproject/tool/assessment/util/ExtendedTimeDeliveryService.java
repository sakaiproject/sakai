/*
 * Copyright (c) 2016, The Apereo Foundation
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
 *
 */

package org.sakaiproject.tool.assessment.util;

import java.util.*;

import lombok.Getter;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.facade.*;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;

/**
 * This class will instantiate with all the proper values for the current user's
 * extended time values for the given published assessment.
 * 
 * @author pdagnall1
 * @author Leonardo Canessa
 *
 */
public class ExtendedTimeDeliveryService {
	private static final int MINS_IN_HOUR = 60;
	private static final int SECONDS_IN_MIN = 60;

	private String siteId;
	private AuthzGroupService authzGroupService;

	private boolean hasExtendedTime;
	private Integer timeLimit;
	private Date startDate;
	private Date dueDate;
	private Date retractDate;

	@Getter
	private Long publishedAssessmentId;
	@Getter
	private String agentId;

	/**
	 * Creates an ExtendedTimeService object using the userId in the agentFacade as the current user
	 * @param publishedAssessment a published assessment object
	 */
	public ExtendedTimeDeliveryService(PublishedAssessmentFacade publishedAssessment) {
		this(publishedAssessment, AgentFacade.getAgentString());
	}

	/**
	 * Creates an ExtendedTimeDeliveryService object based on a specific agentId (userId)
	 * @param publishedAssessment a published assessment object
	 * @param agentId a specific userId to look up
	 *
	 */
	public ExtendedTimeDeliveryService(PublishedAssessmentFacade publishedAssessment, String agentId) {
		PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
		if (!assessmentInitialized(publishedAssessment)) {
			publishedAssessment = publishedAssessmentService
					.getPublishedAssessmentQuick(publishedAssessment.getPublishedAssessmentId().toString());;
		}
		authzGroupService = ComponentManager.get(AuthzGroupService.class);

		// Grab the site id from the publishedAssessment because the user may
		// not be in a site
		// if they're taking the test via url.
		publishedAssessmentId = publishedAssessment.getPublishedAssessmentId();
		String pubId = publishedAssessmentId.toString();
		siteId = publishedAssessmentService.getPublishedAssessmentSiteId(pubId);
		PublishedAssessmentData pubData = publishedAssessmentService.getBasicInfoOfPublishedAssessment(pubId);

		this.agentId = agentId;

		ExtendedTimeFacade extendedTimeFacade = PersistenceService.getInstance().getExtendedTimeFacade();
		List<ExtendedTime> extendedTimes = extendedTimeFacade.getEntriesForPub(pubData);
		List<String> groups = getGroups(extendedTimes);
		String group = isUserInGroups(groups, agentId);

		ExtendedTime extendedTime = extendedTimeFacade.getEntryForPubAndUser(pubData, agentId);
		ExtendedTime groupExtendedTime = null;
		if(!group.isEmpty()) {
			groupExtendedTime = extendedTimeFacade.getEntryForPubAndGroup(pubData, group);
		}

		applyExtendedTime(publishedAssessment, extendedTime != null ? extendedTime : groupExtendedTime);
	}

	ExtendedTimeDeliveryService(PublishedAssessmentFacade publishedAssessment, String agentId, ExtendedTime resolvedExtendedTime) {
		if (!assessmentInitialized(publishedAssessment)) {
			PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
			publishedAssessment = publishedAssessmentService
					.getPublishedAssessmentQuick(publishedAssessment.getPublishedAssessmentId().toString());
		}

		this.publishedAssessmentId = publishedAssessment.getPublishedAssessmentId();
		this.agentId = agentId;
		applyExtendedTime(publishedAssessment, resolvedExtendedTime);
	}

	ExtendedTimeDeliveryService(Long publishedAssessmentId, String agentId, ExtendedTime resolvedExtendedTime) {
		this.publishedAssessmentId = publishedAssessmentId;
		this.agentId = agentId;
		applyExtendedTime(null, resolvedExtendedTime);
	}

	public static Map<Long, ExtendedTimeDeliveryService> buildForStudentSiteList(List<PublishedAssessmentFacade> publishedAssessments,
			String agentId, String siteId) {
		if (publishedAssessments == null || publishedAssessments.isEmpty()) {
			return Collections.emptyMap();
		}

		List<Long> publishedAssessmentIds = new ArrayList<>(publishedAssessments.size());
		for (PublishedAssessmentFacade publishedAssessment : publishedAssessments) {
			if (publishedAssessment.getPublishedAssessmentId() != null) {
				publishedAssessmentIds.add(publishedAssessment.getPublishedAssessmentId());
			}
		}

		Set<String> siteGroupIds = getSiteGroupIds(siteId, agentId);
		List<String> groupIds = new ArrayList<>(siteGroupIds);
		List<ExtendedTime> extendedTimes = PersistenceService.getInstance().getExtendedTimeFacade()
				.getEntriesForPublishedAssessments(publishedAssessmentIds, agentId, groupIds);
		Map<Long, ExtendedTime> resolvedEntries = resolveEntriesByPublishedAssessment(extendedTimes, agentId, siteGroupIds);

		Map<Long, ExtendedTimeDeliveryService> extendedTimeByAssessment = new HashMap<>();
		for (PublishedAssessmentFacade publishedAssessment : publishedAssessments) {
			Long publishedAssessmentId = publishedAssessment.getPublishedAssessmentId();
			if (publishedAssessmentId == null) {
				continue;
			}
			ExtendedTime resolvedExtendedTime = resolvedEntries.get(publishedAssessmentId);
			if (resolvedExtendedTime != null) {
				extendedTimeByAssessment.put(publishedAssessmentId,
						new ExtendedTimeDeliveryService(publishedAssessmentId, agentId, resolvedExtendedTime));
			}
		}
		return extendedTimeByAssessment;
	}

	static Map<Long, ExtendedTime> resolveEntriesByPublishedAssessment(List<ExtendedTime> extendedTimes, String agentId,
			Set<String> siteGroupIds) {
		Map<Long, ExtendedTime> groupEntries = new HashMap<>();
		Map<Long, ExtendedTime> userEntries = new HashMap<>();
		if (extendedTimes == null || extendedTimes.isEmpty()) {
			return Collections.emptyMap();
		}

		List<ExtendedTime> orderedExtendedTimes = new ArrayList<>(extendedTimes);
		orderedExtendedTimes.sort(
				Comparator.comparing(ExtendedTime::getPubAssessmentId, Comparator.nullsFirst(Long::compareTo))
						.thenComparing(e -> e.getUser() == null ? 0 : 1)
						.thenComparing(ExtendedTime::getGroup, Comparator.nullsFirst(String::compareTo))
						.thenComparing(ExtendedTime::getUser, Comparator.nullsFirst(String::compareTo))
						.thenComparing(ExtendedTime::getId, Comparator.nullsFirst(Long::compareTo)));

		for (ExtendedTime extendedTime : orderedExtendedTimes) {
			Long pubAssessmentId = extendedTime.getPubAssessmentId();
			if (pubAssessmentId == null) {
				continue;
			}

			if (agentId != null && agentId.equals(extendedTime.getUser())) {
				userEntries.put(pubAssessmentId, extendedTime);
			} else if (extendedTime.getGroup() != null && siteGroupIds.contains(extendedTime.getGroup())) {
				groupEntries.put(pubAssessmentId, extendedTime);
			}
		}

		Map<Long, ExtendedTime> resolvedEntries = new HashMap<>(groupEntries);
		resolvedEntries.putAll(userEntries);
		return resolvedEntries;
	}

	private static Set<String> getSiteGroupIds(String siteId, String agentId) {
		if (siteId == null || agentId == null) {
			return Collections.emptySet();
		}

		SiteService siteService = ComponentManager.get(SiteService.class);
		if (siteService == null) {
			return Collections.emptySet();
		}

		try {
			Site site = siteService.getSite(siteId);
			Collection<Group> siteGroups = site.getGroupsWithMember(agentId);
			Set<String> siteGroupIds = new HashSet<>();
			for (Group group : siteGroups) {
				siteGroupIds.add(group.getId());
			}
			return siteGroupIds;
		} catch (IdUnusedException e) {
			return Collections.emptySet();
		}
	}

	private void applyExtendedTime(PublishedAssessmentFacade publishedAssessment, ExtendedTime extendedTime) {
		this.hasExtendedTime = extendedTime != null;
		if (this.hasExtendedTime) {
			int hours = extendedTime.getTimeHours() == null ? 0 : extendedTime.getTimeHours();
			int minutes = extendedTime.getTimeMinutes() == null ? 0 : extendedTime.getTimeMinutes();
			this.timeLimit = hours * MINS_IN_HOUR * SECONDS_IN_MIN + minutes * SECONDS_IN_MIN;
			this.startDate = extendedTime.getStartDate();
			this.dueDate = extendedTime.getDueDate();
			this.retractDate = extendedTime.getRetractDate();
		} else {
			this.timeLimit = 0;
			this.startDate = publishedAssessment.getStartDate();
			this.dueDate = publishedAssessment.getDueDate();
			this.retractDate = publishedAssessment.getRetractDate();
		}
	}

	private List<String> getGroups(List<ExtendedTime> extendedTimeList) {
		List<String> list = new ArrayList<>();
		extendedTimeList.forEach(extendedTime -> {
			if(!"".equals(extendedTime.getGroup())) {
				list.add(extendedTime.getGroup());
			}
		});

		return list;
	}

	// Depending on the scope the assessment info sometimes is not initialized.
	private boolean assessmentInitialized(PublishedAssessmentFacade publishedAssessment) {
		if (publishedAssessment == null) {
			return false;
		}
		if (publishedAssessment.getStartDate() != null) {
			return true;
		}
		if (publishedAssessment.getDueDate() != null) {
			return true;
		}
		if (publishedAssessment.getRetractDate() != null) {
			return true;
		}

		return publishedAssessment.getTimeLimit() != null;
	}

	private String isUserInGroups(List<String> groups, String agentId) {
		String returnString = "";
		if(groups != null && !groups.isEmpty()) {
			for(String group : groups) {
				if(isUserInGroup(group, agentId)) {
					returnString = group;
				}
			}
		}

		return returnString;
	}

	private boolean isUserInGroup(String groupId, String agentId) {
		String realmId = "/site/" + siteId + "/group/" + groupId;
		boolean isMember = false;
		try {
			AuthzGroup group = authzGroupService.getAuthzGroup(realmId);
			if (group.getUserRole(agentId) != null)
				isMember = true;
		} catch (Exception e) {
			return false; // this isn't a group
		}
		return isMember;
	}

	public Integer getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(Integer timeLimit) {
		this.timeLimit = timeLimit;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public Date getRetractDate() {
		return retractDate;
	}

	public void setRetractDate(Date retractDate) {
		this.retractDate = retractDate;
	}

	public boolean hasExtendedTime() {
		return hasExtendedTime;
	}

	public void setHasExtendedTime(boolean hasExtendedTime) {
		this.hasExtendedTime = hasExtendedTime;
	}
}
