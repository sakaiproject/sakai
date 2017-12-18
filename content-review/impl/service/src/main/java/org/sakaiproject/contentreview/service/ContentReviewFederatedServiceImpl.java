/**
 * Copyright (c) 2003 The Apereo Foundation
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.exception.ContentReviewProviderException;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;

import lombok.Setter;

/* This class is passed a list of providers in the bean as references, it will use the first
 * by default unless overridden by a site property.
 */
@Slf4j
public class ContentReviewFederatedServiceImpl implements ContentReviewService {

	@Setter
	private ServerConfigurationService serverConfigurationService;

	@Setter
	private ToolManager toolManager;

	@Setter
	private SiteService siteService;

	@Setter
	private List<ContentReviewService> providers;

	private String defaultProvider;

	private List<String> enabledProviders;


	public void init() {
		enabledProviders = configureEnabledProviders();

		if (enabledProviders.isEmpty()) {
			ContentReviewService noop = new NoOpContentReviewService(); 
			providers.add(noop);
			enabledProviders.add(noop.getServiceName());
		}

		providers.stream().forEach(p -> log.debug("Found Content Review Provider: "+ p.getServiceName() + " with providerId of " + p.getProviderId()));
		enabledProviders.stream().forEach(p -> log.info("Enabled Content Review Provider: " + p + " with providerId of " + Math.abs(p.hashCode())));

		Optional<String> configuredDefaultProvider = Optional.ofNullable(serverConfigurationService.getString("contentreview.defaultProvider"));
		if (configuredDefaultProvider.isPresent()) {
			String cdp = configuredDefaultProvider.get();
			if (enabledProviders.contains(cdp)) {
				defaultProvider = cdp;
				log.info("Default Content Review Provider: " + defaultProvider + " with providerId of " + Math.abs(defaultProvider.hashCode()));
			}
		}
		if (StringUtils.isBlank(defaultProvider)) {
			// set the default provider to the first provider in the list
			defaultProvider = enabledProviders.get(0);
			log.info("Default Content Review Provider: " + defaultProvider + " with providerId of " + Math.abs(defaultProvider.hashCode()));
		}
	}

	private Optional<Site> getCurrentSite() {
		Optional<Site> site = null;
		try {
			String context = toolManager.getCurrentPlacement().getContext();
			site = Optional.of(siteService.getSite(context));
		} catch (Exception e) {
			// sakai failed to get us a location so we can assume we are not ins
			// ide the portal
			site = Optional.empty();
		}
		return site;
	}
	
	private List<String> configureEnabledProviders() {
		List<String> enabledProviders = new ArrayList<>();
		Optional<String[]> configuredProviders = Optional.ofNullable(serverConfigurationService.getStrings("contentreview.enabledProviders"));
		if (configuredProviders.isPresent()) {
			List<String> configProviders = Arrays.asList(configuredProviders.get());
			enabledProviders = providers.stream().filter(crs -> configProviders.contains(crs.getServiceName())).map(crs -> crs.getServiceName()).collect(Collectors.toList());
		}
		return enabledProviders;
	}

	private ContentReviewService getSelectedProvider() {
		if (StringUtils.isBlank(defaultProvider)) {
			throw new ContentReviewProviderException("No Default Content Review Provider");
		}
		Optional<Site> currentSite = getCurrentSite();
		
		if (currentSite.isPresent()) {
			if (log.isDebugEnabled()) log.debug("In Location:" + currentSite.get().getReference());
			final String overrideProvider = currentSite.get().getProperties().getProperty("contentreview.provider");
			
			if (enabledProviders.contains(overrideProvider)) {
				return providers.stream().filter(crs -> crs.getServiceName().equals(overrideProvider)).collect(Collectors.toList()).get(0);	
			}
		}
		return providers.stream().filter(crs -> crs.getServiceName().equals(defaultProvider)).collect(Collectors.toList()).get(0);
	}

	public boolean allowResubmission() {
		return getSelectedProvider().allowResubmission();
	}

	public void checkForReports() {
		// this is a method that the jobs call and should check for reports for all enabled providers
		providers.stream().filter(provider -> enabledProviders.contains(provider.getServiceName())).forEach(ContentReviewService::checkForReports);
	}

	public void createAssignment(String arg0, String arg1, Map arg2)
			throws SubmissionException, TransientSubmissionException {
		getSelectedProvider().createAssignment(arg0, arg1, arg2);
	}

	public List<ContentReviewItem> getAllContentReviewItems(String arg0, String arg1)
			throws QueueException, SubmissionException, ReportException {
		return getSelectedProvider().getAllContentReviewItems(arg0, arg1);
	}

	public Map getAssignment(String arg0, String arg1) throws SubmissionException, TransientSubmissionException {
		return getSelectedProvider().getAssignment(arg0, arg1);
	}

	public Date getDateQueued(String arg0) throws QueueException {
		return getSelectedProvider().getDateQueued(arg0);
	}

	public Date getDateSubmitted(String arg0) throws QueueException, SubmissionException {
		return getSelectedProvider().getDateSubmitted(arg0);
	}

	public String getIconCssClassforScore(int score, String contentId) {
		return getSelectedProvider().getIconCssClassforScore(score, contentId);
	}

	public String getLocalizedStatusMessage(String arg0) {
		return getSelectedProvider().getLocalizedStatusMessage(arg0);
	}

	public String getLocalizedStatusMessage(String arg0, String arg1) {
		return getSelectedProvider().getLocalizedStatusMessage(arg0, arg1);
	}

	public String getLocalizedStatusMessage(String arg0, Locale arg1) {
		return getSelectedProvider().getLocalizedStatusMessage(arg0, arg1);
	}

	public List<ContentReviewItem> getReportList(String siteId)
			throws QueueException, SubmissionException, ReportException {
		return getSelectedProvider().getReportList(siteId);
	}

	public List<ContentReviewItem> getReportList(String siteId, String taskId)
			throws QueueException, SubmissionException, ReportException {
		return getSelectedProvider().getReportList(siteId, taskId);
	}

	public String getReviewReport(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		return getSelectedProvider().getReviewReport(contentId, assignmentRef, userId);
	}

	public String getReviewReportInstructor(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		return getSelectedProvider().getReviewReportInstructor(contentId, assignmentRef, userId);
	}

	public String getReviewReportStudent(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		return getSelectedProvider().getReviewReportStudent(contentId, assignmentRef, userId);
	}

	public Long getReviewStatus(String contentId) throws QueueException {
		return getSelectedProvider().getReviewStatus(contentId);
	}

	public String getServiceName() {
		return getSelectedProvider().getServiceName();
	}

	public boolean allowAllContent() {
		return getSelectedProvider().allowAllContent();
	}

	public boolean isAcceptableContent(ContentResource arg0) {
		return getSelectedProvider().isAcceptableContent(arg0);
	}

	public Map<String, SortedSet<String>> getAcceptableExtensionsToMimeTypes() {
		return getSelectedProvider().getAcceptableExtensionsToMimeTypes();
	}

	public Map<String, SortedSet<String>> getAcceptableFileTypesToExtensions() {
		return getSelectedProvider().getAcceptableFileTypesToExtensions();
	}

	public boolean isSiteAcceptable(Site arg0) {
		return getSelectedProvider().isSiteAcceptable(arg0);
	}

	public void processQueue() {
		// this is a method that the jobs call and should process items for all enabled providers
		providers.stream().filter(provider -> enabledProviders.contains(provider.getServiceName())).forEach(ContentReviewService::processQueue);
	}

	public void queueContent(String userId, String siteId, String assignmentReference, List<ContentResource> content)
			throws QueueException {
		getSelectedProvider().queueContent(userId, siteId, assignmentReference, content);
	}

	public void removeFromQueue(String arg0) {
		getSelectedProvider().removeFromQueue(arg0);
	}

	public void resetUserDetailsLockedItems(String arg0) {
		getSelectedProvider().resetUserDetailsLockedItems(arg0);
	}

	public String getReviewError(String contentId) {
		return getSelectedProvider().getReviewError(contentId);
	}

	public int getReviewScore(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException, Exception {
		return getSelectedProvider().getReviewScore(contentId, assignmentRef, userId);
	}

	public ContentReviewItem getContentReviewItemByContentId(String arg0) {
		return getSelectedProvider().getContentReviewItemByContentId(arg0);
	}

}