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
package org.sakaiproject.contentreview.service;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.sakaiproject.site.api.Site;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpContentReviewService extends BaseContentReviewService {
	private static final String SERVICE_NAME = "No Operation";

	@Override
	public void queueContent(String userId, String siteId, String taskId, List<ContentResource> content)
			throws QueueException {
		log.debug("void queueContent {} {} {} {}", userId, siteId, taskId, content);
	}

	@Override
	public int getReviewScore(String contentId, String taskId, String userId)
			throws QueueException, ReportException, Exception {
		log.debug("{} getReviewScore {} {} {}", 0, contentId, taskId, userId);
		return 0;
	}

	@Override
	public String getReviewReport(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		log.debug("{} getReviewReport {} {} {}", null, contentId, assignmentRef, userId);
		return null;
	}

	@Override
	public String getReviewReportStudent(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		log.debug("{} getReviewReportStudent {} {} {}", null, contentId, assignmentRef, userId);
		return null;
	}

	@Override
	public String getReviewReportInstructor(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		log.debug("{} getReviewReportInstructor {} {} {}", null, contentId, assignmentRef, userId);
		return null;
	}

	@Override
	public Long getReviewStatus(String contentId) throws QueueException {
		log.debug("{} getReviewStatus {}", null, contentId);
		return null;
	}

	@Override
	public Date getDateQueued(String contextId) throws QueueException {
		log.debug("{} getDateQueued {}", null, contextId);
		return null;
	}

	@Override
	public Date getDateSubmitted(String contextId) throws QueueException, SubmissionException {
		log.debug("{} getDateSubmitted {}", null, contextId);
		return null;
	}

	@Override
	public void processQueue() {
		log.debug("void processqueue");
	}

	@Override
	public void checkForReports() {
		log.debug("void checkForReports");
	}

	@Override
	public List<ContentReviewItem> getReportList(String siteId, String taskId)
			throws QueueException, SubmissionException, ReportException {
		log.debug("{} getReportList {} {}", null, siteId, taskId);
		return null;
	}

	@Override
	public List<ContentReviewItem> getReportList(String siteId)
			throws QueueException, SubmissionException, ReportException {
		log.debug("{} getReportList {}", null, siteId);
		return null;
	}

	@Override
	public List<ContentReviewItem> getAllContentReviewItems(String siteId, String taskId)
			throws QueueException, SubmissionException, ReportException {
		log.debug("{} getAllContentReviewItems {} {}", null, siteId, taskId);
		return null;
	}

	@Override
	public String getServiceName() {
		log.debug("{} getServiceName", SERVICE_NAME);
		return SERVICE_NAME;
	}

	@Override
	public void resetUserDetailsLockedItems(String userId) {
		log.debug("void resetUserDetailsLockedItems {}", userId);
	}

	@Override
	public boolean allowAllContent() {
		boolean ret = false;
		//Return true to debug this in demo mode
		if ("true".equalsIgnoreCase(System.getProperty("sakai.demo"))) {
			ret = true;
		}
		log.debug("{} allowAllContent", ret);
		return ret;
	}

	@Override
	public boolean isAcceptableContent(ContentResource resource) {
		boolean ret=false;
		//Return true to debug this in demo mode
		if ("true".equalsIgnoreCase(System.getProperty("sakai.demo"))) {
			ret = true;
		}
		log.debug("{} isAcceptableContent {}", ret, resource);
		return ret;
	}

	@Override
	public Map<String, SortedSet<String>> getAcceptableExtensionsToMimeTypes() {
		HashMap<String, SortedSet<String>> ret = new HashMap<String, SortedSet<String>>();
		log.debug("{} getAcceptableExtensionsToMimeTypes", ret);
		return ret;
	}

	@Override
	public Map<String, SortedSet<String>> getAcceptableFileTypesToExtensions() {
		HashMap<String, SortedSet<String>> ret = new HashMap<String, SortedSet<String>>();
		log.debug("{} getAcceptableFileTypesToExtensions", ret);
		return ret;
	}

	@Override
	public boolean isSiteAcceptable(Site site) {
		boolean ret=false;
		//Return true to debug this in demo mode
		if ("true".equalsIgnoreCase(System.getProperty("sakai.demo"))) {
			ret = true;
		}
		log.debug("{} isSiteAcceptable {}", ret, site);
		return ret;
	}

	@Override
	public String getIconCssClassforScore(int score, String contentId) {
		String ret = "contentReviewIconNoService";
		log.debug("{} getIconCssClassforScore {} {}", ret, score, contentId);
		return ret;
	}

	@Override
	public boolean allowResubmission() {
		log.debug("{} allowResubmission", false);
		return false;
	}

	@Override
	public void removeFromQueue(String contentId) {
		log.debug("void removeFromQueue {}", contentId);
	}

	@Override
	public String getLocalizedStatusMessage(String messageCode, String userRef) {
		String ret = "There is no content review service configured, please see your administrator";
		log.debug("{} getLocalizedStatusMessage {} {}", ret, messageCode, userRef);
		return ret;
	}

	@Override
	public String getLocalizedStatusMessage(String messageCode) {
		String ret = "There is no content review service configured, please see your administrator";
		log.debug("{} getLocalizedStatusMessage {}", ret, messageCode);
		return ret;
	}

	@Override
	public String getReviewError(String contentId) {
		String ret = "There is no content review service configured, please see your administrator";
		log.debug("{} getReviewError {}", ret, contentId);
		return ret;
	}

	@Override
	public String getLocalizedStatusMessage(String messageCode, Locale locale) {
		String ret = "There is no content review service configured, please see your administrator";
		log.debug("{} getLocalizedStatusMessage {} {}", ret, messageCode, locale);
		return ret;
	}

	@Override
	public Map getAssignment(String siteId, String taskId) throws SubmissionException, TransientSubmissionException {
		log.debug("{} getAssignment {} {}", null, siteId, taskId);
		return null;
	}

	@Override
	public void createAssignment(String siteId, String taskId, Map extraAsnnOpts)
			throws SubmissionException, TransientSubmissionException {
		log.debug("void createAssignment {} {} {}", siteId, taskId, extraAsnnOpts);
	}

	@Override
	public ContentReviewItem getContentReviewItemByContentId(String contentId) {
		ContentReviewItem ret = null;
		log.debug("{} getContentReviewItemByContentId {}", ret, contentId);
		return ret;
	}

	@Override
	public String getEndUserLicenseAgreementLink(String userId) {
		String ret = "There is no content review service configured, please see your administrator";
		log.debug("{} getEndUserLicenseAgreementLink {}", ret, userId);
		return null;
	}

	@Override
	public Instant getEndUserLicenseAgreementTimestamp() {
		String ret = "There is no content review service configured, please see your administrator";
		log.debug("{} getEndUserLicenseAgreementTimestamp", ret);
		return null;
	}
	
	@Override
	public Instant getUserEULATimestamp(String userId) {
		String ret = "There is no content review service configured, please see your administrator";
		log.debug("{} getUserEULATimestamp {}", ret, userId);
		return null;
	}
	
	@Override
	public void updateUserEULATimestamp(String userId) {
		String ret = "There is no content review service configured, please see your administrator";
		log.debug("{} updateUserEULATimestamp {}", ret, userId);
	}

	@Override
	public String getEndUserLicenseAgreementVersion() {
		String ret = "There is no content review service configured, please see your administrator";
		log.debug("{} getEndUserLicenseAgreementVersion", ret);
		return null;
	}

	@Override
	public void webhookEvent(HttpServletRequest request, int providerId, Optional<String> customParam) {
		String ret = "There is no content review service configured, please see your administrator";
		log.debug("{} webhookEvent", ret);
	}
}
