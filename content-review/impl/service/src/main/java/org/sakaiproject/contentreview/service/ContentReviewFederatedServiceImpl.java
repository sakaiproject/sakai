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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
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
import lombok.extern.slf4j.Slf4j;

/* This class is passed a list of providers in the bean as references, it will use the first
 * by default unless overridden by a site property.
 */
@Slf4j
public class ContentReviewFederatedServiceImpl extends BaseContentReviewService {

	@Setter
	private ToolManager toolManager;

	@Setter
	private SiteService siteService;

	@Setter
	private List<ContentReviewService> providers;

	private int defaultProvider = -1;

	private Set<Integer> enabledProviders;


	public void init() {
		enabledProviders = configureEnabledProviders();

		if (enabledProviders.isEmpty()) {
			ContentReviewService noop = new NoOpContentReviewService(); 
			providers.add(noop);
			enabledProviders.add(noop.getProviderId().intValue());
		}

		providers.stream().forEach(p -> log.info("Found Content Review Provider: "+ p.getServiceName() + " with providerId of " + p.getProviderId()));
		enabledProviders.stream().forEach(p -> log.info("Enabled Content Review Provider: " + p));

		Optional<String> configuredDefaultProvider = Optional.ofNullable(serverConfigurationService.getString("contentreview.defaultProvider"));
		if (configuredDefaultProvider.isPresent()) {
			Integer cdp = Math.abs(configuredDefaultProvider.get().hashCode());
			if (enabledProviders.stream().anyMatch(p -> p.intValue() == cdp.intValue())) {
				defaultProvider = cdp.intValue();
				log.info("Default Content Review Provider: " + defaultProvider);
			}
		}
		if (defaultProvider < 0) {
			// set the default provider to the first provider in the list
			defaultProvider = new ArrayList<Integer>(enabledProviders).get(0).intValue();
			log.info("Default Content Review Provider: " + defaultProvider);
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
	
	private Set<Integer> configureEnabledProviders() {
		Set<Integer> enabledProviders = new HashSet<Integer>();
		Optional<String[]> configuredProviders = Optional.ofNullable(serverConfigurationService.getStrings("contentreview.enabledProviders"));
		if (configuredProviders.isPresent()) {
			List<String> configProviders = Arrays.asList(configuredProviders.get());
			for(ContentReviewService provider : providers) {
				for(String configProviderName : configProviders) {
					if(configProviderName.equals(provider.getServiceName())
							|| Math.abs(configProviderName.hashCode()) == provider.getProviderId().intValue()) {
						enabledProviders.add(provider.getProviderId().intValue());
					}
				}
			}
		}
		return enabledProviders;
	}

	private ContentReviewService getSelectedProvider() {
		if (defaultProvider < 0) {
			throw new ContentReviewProviderException("No Default Content Review Provider");
		}
		Optional<Site> currentSite = getCurrentSite();
		
		if (currentSite.isPresent()) {
			if (log.isDebugEnabled()) log.debug("In Location:" + currentSite.get().getReference());
			final String overrideProvider = currentSite.get().getProperties().getProperty("contentreview.provider");
			
			if (StringUtils.isNotEmpty(overrideProvider)
					&& enabledProviders.stream().anyMatch(p -> p.intValue() == Math.abs(overrideProvider.hashCode()))) {
				return providers.stream().filter(crs -> crs.getProviderId().intValue() == Math.abs(overrideProvider.hashCode())).collect(Collectors.toList()).get(0);	
			}
		}
		return providers.stream().filter(crs -> crs.getProviderId().intValue() == defaultProvider).collect(Collectors.toList()).get(0);
	}

	public boolean allowResubmission() {
		return getSelectedProvider().allowResubmission();
	}

	public void checkForReports() {
		// this is a method that the jobs call and should check for reports for all enabled providers
		providers.stream().filter(provider -> enabledProviders.stream().anyMatch(ep -> ep.intValue() ==provider.getProviderId().intValue())).forEach(ContentReviewService::checkForReports);
	}

	@Override
	public Integer getProviderId() {
		return getSelectedProvider().getProviderId();
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
		providers.stream().filter(provider -> enabledProviders.stream().anyMatch(ep -> ep.intValue() == provider.getProviderId().intValue())).forEach(ContentReviewService::processQueue);
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

	@Override
	public String getEndUserLicenseAgreementLink(String userId) {
		return getSelectedProvider().getEndUserLicenseAgreementLink(userId);
	}

	@Override
	public Instant getEndUserLicenseAgreementTimestamp() {
		return getSelectedProvider().getEndUserLicenseAgreementTimestamp();
	}

	@Override
	public Instant getUserEULATimestamp(String userId) {
		return getSelectedProvider().getUserEULATimestamp(userId);
	}

	@Override
	public void updateUserEULATimestamp(String userId) {
		getSelectedProvider().updateUserEULATimestamp(userId);
	}

	@Override
	public String getEndUserLicenseAgreementVersion() {
		return getSelectedProvider().getEndUserLicenseAgreementVersion();
	}
	
	@Override
	public String getReviewReportRedirectUrl(String contentId, String assignmentRef, String userId, boolean isInstructor) {
		return getSelectedProvider().getReviewReportRedirectUrl(contentId, assignmentRef, userId, isInstructor);
	}

	@Override
	public void webhookEvent(HttpServletRequest request, int providerId, Optional<String> customParam) {
		providers.stream().filter(crs -> crs.getProviderId().intValue() == providerId).collect(Collectors.toList()).get(0).webhookEvent(request, providerId, customParam);		
	}
}