/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
import java.io.StringReader;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.contentreview.dao.ContentReviewConstants;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.exception.ContentReviewProviderException;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

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
	private static final String REDIRECT_URL_TEMPLATE =  "/content-review-tool/viewreport?contentId=%s&assignmentRef=%s&contextId=%s";
	//full path since it will be used externally
	private static final String WEBHOOK_URL_TEMPLATE = "%scontent-review-tool/webhooks?providerId=%s";

	private ResourceLoader rb;
	
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
		ContentReviewItem item = checkContentItemStatus(contentId);
		return String.format(REDIRECT_URL_TEMPLATE, contentId, assignmentRef, item.getSiteId());
	}
	
	@Override
	public String getReviewReportInstructor(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		ContentReviewItem item = checkContentItemStatus(contentId);
		return String.format(REDIRECT_URL_TEMPLATE, contentId, assignmentRef, item.getSiteId());
	}
	
	@Override
	public String getReviewReportStudent(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		ContentReviewItem item = checkContentItemStatus(contentId);
		return String.format(REDIRECT_URL_TEMPLATE, contentId, assignmentRef, item.getSiteId());
	}
	
	private ContentReviewItem checkContentItemStatus(String contentId) throws ReportException {
		ContentReviewItem item = getContentReviewItemByContentId(contentId);
		if(item == null
				|| !ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE.equals(item.getStatus())) {
			throw new ReportException("Report status: " + (item != null ? item.getStatus() : ""));
		}
		return item;
	}
	
	@Override
	public String getReviewReportRedirectUrl(String contentId, String assignmentRef, String userId, String contextId, boolean isInstructor) {
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

	// ================== Internationalized Error Messages ==================

	// For methods below, see I18nXmlUtility's class level javadoc comments

	/**
	 * Creates XML that represents calls to ResourceLoader.getFormattedMessage().
	 * @return an XML element representing the i18n message key and arguments - unless document is null, in which case the formatted message is returned as a String using the current session locale
	 */
	protected Object createFormattedMessageXML(Document document, String key, Object... args) {
		try {
			return I18nXmlUtility.createFormattedMessageXML(document, key, args);
		} catch (Exception e) {
			log.warn("Could not create last error xml, returning the raw message localized to the current session", e);
			return getResourceLoader().getFormattedMessage(key, args);
		}
	}

	/**
	 * Functional interface for updateLastError
	 */
	public static interface LastErrorUpdater {
		public Object createLastErrorXML(Document doc);
	}

	/**
	 * Sets the ContentReviewItem's lastError property to the result of createLastError(leu).
	 * Changes are not committed - the caller is responsible to do so.
	 * Afterward, the message can be localized to end users via getLocalizedLastError(item).
	 */
	protected void setLastError(ContentReviewItem item, LastErrorUpdater leu) {
		item.setLastError(createLastError(leu));
	}

	/**
	 * Convenience method:
	 * If the Exception is an instance of ContentReviewProviderException, the item's lastError is set to the exception's I18nXml;
	 * otherwise it falls back to getLocalizedMessage()
	 */
	protected void setLastError(ContentReviewItem item, Exception e) {
		setLastError(item, e, null);
	}

	/**
	 * If the Exception is an instance of ContentReviewProviderException, the item's lastError is set to the exception's I18nXml;
	 * otherwise it falls back to leu;
	 * if leu is null, it falls back to getLocalizedMessage()
	 */
	protected void setLastError(ContentReviewItem item, Exception e, LastErrorUpdater leu) {
		if (e instanceof ContentReviewProviderException) {
			item.setLastError(((ContentReviewProviderException)e).getI18nXml());
		} else {
			String message = leu == null ? e.getLocalizedMessage() : createLastError(leu);
			item.setLastError(message);
		}
	}

	/**
	 * Create's an XML model that represents calls to ResourceLoader.getFormattedMessage(...).
	 *
	 * Example - represent an i18n compliant message for "An error has occurred with the service. Error code: 42; cause: A Sakaiger ate the paper"
	 * given:
	 *     service.error=An error has occurred with the service. Error code: {0}; cause: {1}
	 *     service.sakaiger.ate.paper=A Sakaiger ate the paper
	 * You may invoke:
	 * createLastError(doc -&gt; createFormattedMessageXML(doc, "service.error", 42, createFormattedMessageXML(doc, "service.sakaiger.ate.paper")));
	 */
	protected String createLastError(LastErrorUpdater leu) {
		Document document = null;
		try {
			document = I18nXmlUtility.createXmlDocument();
		} catch (ContentReviewProviderException e) {
			// Shouldn't happen, but if it does, just use a null document. It'll generate the formatted string using the current session locale
			log.warn("Failed to create an XML document", e);
		}

		Object lastErrorXML = leu.createLastErrorXML(document);

		String lastError = null;
		if (lastErrorXML instanceof Element) {
			lastError = I18nXmlUtility.addElementAndGetDocumentAsString(document, (Element)lastErrorXML);
		}
		else if (lastErrorXML instanceof String) {
			lastError = (String)lastErrorXML;
		}
		else {
			throw new ContentReviewProviderException("Unexpected behavior while generating the last_error value");
		}

		return lastError;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLocalizedLastError(ContentReviewItem item) {
		String xmlString = item.getLastError();
		if (StringUtils.isBlank(xmlString)) {
			return "";
		}
		try {
			Document doc = I18nXmlUtility.getDocumentBuilder().parse(new InputSource(new StringReader(xmlString)));

			Node root = doc.getFirstChild();

			return I18nXmlUtility.getLocalizedMessage(getResourceLoader(), root);
		} catch (Exception e) {
			log.warn("Could not internationalize last_error value:\n{}\nReturning the raw data", xmlString);
			log.debug("Cause:", e);
			// The content is most likely raw (e.g. data from before SAK-41883)
			return xmlString;
		}
	}

	/**
	 * The Resource Loader specific to the content-review service implementation
	 */
	protected ResourceLoader getResourceLoader() {
		if (rb == null) {
			rb = new ResourceLoader(getResourceLoaderName());
		}
		return rb;
	}

	/**
	 * Gets the name of the resource loader's file. Default implementation is getServiceName() to lower case, and this should be overriden as necessary
	 */
	protected String getResourceLoaderName() {
		return getServiceName().toLowerCase();
	}
	
	@Override
	public boolean allowSubmissionsOnBehalf() {
		return false;
	}
}
