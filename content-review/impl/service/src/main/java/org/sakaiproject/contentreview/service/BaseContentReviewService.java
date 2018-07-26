package org.sakaiproject.contentreview.service;

import java.time.Instant;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.contentreview.dao.ContentReviewConstants;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseContentReviewService implements ContentReviewService{
	
	@Setter
	protected PreferencesService preferencesService;
	@Setter
	protected ServerConfigurationService serverConfigurationService;
	
	private static final String PROP_KEY_EULA = "contentReviewEULA";
	private static final String PROP_KEY_EULA_TIMESTAMP = "contentReviewEULATimestamp";
	private static final String PROP_KEY_EULA_VERSION = "contentReviewEULAVersion";
	//relative path since it will be used within Sakai
	private static final String REDIRECT_URL_TEMPLATE =  "/content-review-tool/viewreport?contentId=%s&assignmentRef=%s";
	//full path since it will be used externally
	private static final String WEBHOOK_URL_TEMPLATE = "%scontent-review-tool/webhooks?providerId=%s";
	
	@Override
	public Instant getUserEULATimestamp(String userId) {
		Instant timestamp = null;
		try {
			ResourceProperties pref = preferencesService.getPreferences(userId).getProperties(PROP_KEY_EULA + getProviderId());
			if(pref != null) {
				timestamp = Instant.ofEpochMilli(pref.getLongProperty(PROP_KEY_EULA_TIMESTAMP));
			}
		}catch(EntityPropertyNotDefinedException e) {
			//nothing to do, prop is just not set
		}catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		return timestamp;
	}
	
	@Override
	public String getUserEULAVersion(String userId) {
		String version = null;
		try {
			ResourceProperties pref = preferencesService.getPreferences(userId).getProperties(PROP_KEY_EULA + getProviderId());
			if(pref != null) {
				version = pref.getProperty(PROP_KEY_EULA_VERSION);
			}
		}catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		return version;
	}
	
	@Override
	public void updateUserEULATimestamp(String userId) {
		try {
			PreferencesEdit pref = preferencesService.edit(userId);
			try {
				if(pref != null) {
					ResourcePropertiesEdit resourcePropEdit = pref.getPropertiesEdit(PROP_KEY_EULA + getProviderId());
					if(resourcePropEdit != null) {
						resourcePropEdit.addProperty(PROP_KEY_EULA_TIMESTAMP, "" + Instant.now().toEpochMilli());
						String EULAVersion = getEndUserLicenseAgreementVersion();
						if(StringUtils.isNotEmpty(EULAVersion)) {
							resourcePropEdit.addProperty(PROP_KEY_EULA_VERSION, EULAVersion);
						}
						preferencesService.commit(pref);
					}else {
						preferencesService.cancel(pref);
					}
				}
			}catch(Exception e) {
				log.error(e.getMessage(), e);
				preferencesService.cancel(pref);
			}
		}catch(Exception e) {
			log.error(e.getMessage(), e);
		}	
	}
	
	@Override
	public String getReviewReport(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		checkContentItemStatus(contentId);
		return String.format(REDIRECT_URL_TEMPLATE, contentId, assignmentRef);
	}
	
	@Override
	public String getReviewReportInstructor(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		checkContentItemStatus(contentId);
		return String.format(REDIRECT_URL_TEMPLATE, contentId, assignmentRef);
	}
	
	@Override
	public String getReviewReportStudent(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		checkContentItemStatus(contentId);
		return String.format(REDIRECT_URL_TEMPLATE, contentId, assignmentRef);
	}
	
	private void checkContentItemStatus(String contentId) throws ReportException {
		ContentReviewItem item = getContentReviewItemByContentId(contentId);
		if(item == null
				|| !ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE.equals(item.getStatus())) {
			throw new ReportException("Report status: " + (item != null ? item.getStatus() : ""));
		}
	}
	
	@Override
	public String getReviewReportRedirectUrl(String contentId, String assignmentRef, String userId, boolean isInstructor) {
		return null;
	}

	public String getWebhookUrl(Optional<String> customParam) {
		StringBuilder sb = new StringBuilder();
		sb.append(serverConfigurationService.getServerUrl());
		if(!StringUtils.endsWith(sb.toString(), "/")) {
			sb.append("/");
		}
		return String.format(WEBHOOK_URL_TEMPLATE, sb.toString(), getProviderId()) + (customParam.isPresent() ? "&custom=" + customParam.get() : "");
	}
}
