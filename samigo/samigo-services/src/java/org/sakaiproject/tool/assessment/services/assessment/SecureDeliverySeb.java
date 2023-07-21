/**
 * Copyright (c) ${license.git.copyrightYears} ${holder}
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
package org.sakaiproject.tool.assessment.services.assessment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.plist.XMLPropertyListConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.assessment.business.entity.SebConfig;
import org.sakaiproject.tool.assessment.business.entity.SebConfig.ConfigMode;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.SebValidationData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SecureDeliveryModuleIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI.Phase;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI.PhaseStatus;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI.PreDeliveryPhase;
import org.sakaiproject.tool.assessment.util.HashingUtil;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

//Secure Delivery Module for Safe Exam browser integration

@Configuration
@Slf4j
public class SecureDeliverySeb implements SecureDeliveryModuleIfc {

    private static final String LOGIN_SERVLET_PATH = "/samigo-app/servlet/Login";
    private static final String SEB_ALWAYS_ENABLED = "always";
    private static final String SEB_ALWAYS_ENABLED_PROPERTY = "seb.enabled";
    private static final String SEB_SCRIPT_PATH = "/samigo-app/js/deliverySafeExamBrowser.js";
    // The property added to a site
    private static final String SEB_SITE_ENABLED_PROPERTY = "sebEnabled";

    private static String sebEnabled;
    private static String sebDownloadLink;

    private PersistenceService persistenceService = PersistenceService.getInstance();
    private SecurityService securityService = ComponentManager.get(SecurityService.class);
    private ServerConfigurationService serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
    private SiteService siteService = ComponentManager.get(SiteService.class);
    private UserDirectoryService userDirectoryService = ComponentManager.get(UserDirectoryService.class);
    private ContentHostingService contentHostingService = ComponentManager.get(ContentHostingService.class);

    private SecurityAdvisor alwaysAllowSecurityAdvisor = (String userId, String function, String reference) -> SecurityAdvisor.SecurityAdvice.ALLOWED;

    public static final String MODULE_NAME = "Safe Exam Browser";
    public static final String SEB_DOWNLOAD_LINK_DEFAULT = "https://safeexambrowser.org/download_en.html";
    public static final String SEB_DOWNLOAD_LINK_PROPERTY = "seb.download.link";

    public boolean initialize() {
        Objects.requireNonNull(persistenceService);
        Objects.requireNonNull(securityService);
        Objects.requireNonNull(serverConfigurationService);
        Objects.requireNonNull(siteService);
        Objects.requireNonNull(userDirectoryService);
        Objects.requireNonNull(contentHostingService);

        sebEnabled = serverConfigurationService.getString(SEB_ALWAYS_ENABLED_PROPERTY, SEB_ALWAYS_ENABLED);
        sebDownloadLink = serverConfigurationService.getString(SEB_DOWNLOAD_LINK_PROPERTY, SEB_DOWNLOAD_LINK_DEFAULT);

        return true;
    }

    public boolean isEnabled() {
        if (!StringUtils.equals(sebEnabled, SEB_ALWAYS_ENABLED)) {
            // This will come back null if from an assessment URL
            String siteId = AgentFacade.getCurrentSiteId();
            return isSiteSebEnabled(siteId);
        }

        return true;
    }

    public boolean isEnabled(Long assessmentId) {
        if (!StringUtils.equals(sebEnabled, SEB_ALWAYS_ENABLED)) {

            PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
            publishedAssessmentService.getPublishedAssessmentStatus(assessmentId);
            PublishedAssessmentFacade publishedAssessment = publishedAssessmentService.getPublishedAssessment(assessmentId.toString());
            String siteId = publishedAssessment.getOwnerSiteId();
            return isSiteSebEnabled(siteId);
        }

        return true;
    }

    public String getModuleName(Locale locale) {
        return MODULE_NAME;
    }

    public String getTitleDecoration(Locale locale) {
        return "(" + getModuleName(locale) + ")";
    }

    public PhaseStatus validatePhase(Phase phase, PublishedAssessmentIfc assessment, HttpServletRequest request) {

        // Check if assessment is set; No? -> ERROR -> return SUCCESS
        if (assessment == null) {
            log.error("Assessment is null, returning SUCCESS. Session: [{}]", request != null ? request.getSession().getId() : null);
            return PhaseStatus.SUCCESS;
        }

        HashMap<String, String> assessmentMetaDataMap = assessment.getAssessmentMetaDataMap();
        SebConfig sebConfig = SebConfig.of(assessmentMetaDataMap);

        Long assessmentId = assessment.getPublishedAssessmentId();
        String userId = getCurrentUserId();


        String startLink = getStartLink(assessment, request);
        sebConfig.setStartLink(startLink != null ? startLink : "");

        ConfigMode configMode = sebConfig.getConfigMode();

        // Check if configMode is set; No? -> ERROR -> return SUCCESS
        if (configMode == null) {
            log.error("ConfigMode not set for published assessment [{}]", assessmentId);
            return PhaseStatus.SUCCESS;
        }

        Optional<SebValidationData> optSebValidationData = persistenceService.getSebValidationFacadeQueries()
                .getLastSebValidation(assessmentId, userId);

        // Check if we have non expired validation data; No? -> INVALID -> return FAILURE
        if (!optSebValidationData.isPresent() || optSebValidationData.get().isExpired()) {
            return PhaseStatus.FAILURE;
        }

        SebValidationData sebValidationData = optSebValidationData.get();

        boolean isConfigKeyValid = false;
        boolean isExamKeyValid = false;

        // Validate exam key and config key, or not, based on config mode
        switch(configMode) {
            case MANUAL:
                isConfigKeyValid = validateConfigKeyHash(sebConfig, sebValidationData.getUrl(), sebValidationData.getConfigKeyHash());
                isExamKeyValid = true;
                break;
            case UPLOAD:
                isConfigKeyValid = true;
                isExamKeyValid = validateExamKeyHash(sebConfig, sebValidationData.getUrl(), sebValidationData.getExamKeyHash());
                break;
            case CLIENT:
                isConfigKeyValid = true;
                isExamKeyValid = validateExamKeyHash(sebConfig, sebValidationData.getUrl(), sebValidationData.getExamKeyHash());
                break;
            default:
                // Unknown config mode -> ERROR -> return SUCCESS
                isConfigKeyValid = true;
                isExamKeyValid = true;
                log.error("Unknown config mode [{}], not validating, returning [{}]", configMode, PhaseStatus.SUCCESS);
                return PhaseStatus.SUCCESS;
        }

        expireValidationData(assessmentId, userId);

        if (isConfigKeyValid && isExamKeyValid) {
            return PhaseStatus.SUCCESS;
        } else {
            return PhaseStatus.FAILURE;
        }
    }

    public String getInitialHTMLFragment(HttpServletRequest request, Locale locale) {
        return "";
    }

    public String getHTMLFragment(PublishedAssessmentIfc assessment, HttpServletRequest request, Phase phase,
            PhaseStatus status, Locale locale) {

        if (assessment == null) {
            log.error("Assessment is null, returning empty. Session: [{}]", request != null ? request.getSession().getId() : null);
            return "";
        }

        SebConfig sebConfig = SebConfig.of(assessment.getAssessmentMetaDataMap());

        log.debug("Returning html fragment for Phase [{}]", phase);

        switch (phase) {
            case ASSESSMENT_START:
                Long assessmentId = assessment.getPublishedAssessmentId();
                String configLink = getConfigLink(sebConfig, assessmentId);
                return "<script>\n"
                     + "    const seb = {\n"
                     + "        assessmentId: " + assessmentId + ",\n"
                     + "        downloadLink: \"" + sebDownloadLink + "\",\n"
                     + "        relativeConfigLink: \"" + configLink + "\",\n"
                     + "    };\n"
                     + "</script>\n"
                     + "<script type=\"text/javascript\" src=\"" + SEB_SCRIPT_PATH + "\"></script>";
            case ASSESSMENT_FINISH:
                String quitLink = SebConfig.QUIT_LINK;
                return "<script>\n"
                     + "    function quitSeb() {\n"
                     + "        window.location.href = \"" + quitLink + "\";\n"
                     + "    }\n"
                     + "</script>\n";
            case ASSESSMENT_REVIEW:
            default:
                return "";
        }
    }

    public PhaseStatus executePreDeliveryPhase(AssessmentIfc assessment, PublishedAssessmentIfc publishedAssessment,
            HttpServletRequest request, PreDeliveryPhase phase) {
        log.debug("Executing pre delivery phase [{}]", phase);

        if (publishedAssessment == null) {
            log.error("Published assessment is null, returning {}", PhaseStatus.FAILURE);
            return PhaseStatus.FAILURE;
        }

        String configModeString = publishedAssessment.getAssessmentMetaDataByLabel(SebConfig.CONFIG_MODE);

        switch (phase) {
            case ASSESSMENT_PUBLISH:
                if (ConfigMode.UPLOAD.toString().equals(configModeString)) {
                    return publishConfigUpload(publishedAssessment, request) ? PhaseStatus.SUCCESS : PhaseStatus.FAILURE;
                } else {
                    return PhaseStatus.SUCCESS;
                }
            default:
                return PhaseStatus.SUCCESS;
        }
    }

    public boolean validateContext(Object context) {
        return true;
    }

    public String encryptPassword(String password) {
        return password;
    }

    public String decryptPassword(String password) {
        return password;
    }

    public Optional<String> getAlternativeDeliveryUrl (Long assessmentId, String uid) {
        return Optional.empty();
    }

    public Optional<String> getInstructorReviewUrl (Long assessmentId, String studentId) {
        return Optional.empty();
    }

    private boolean validateConfigKeyHash(SebConfig sebConfig, String url, String configKeyHash) {
        String config = sebConfig.toJson();

        return StringUtils.equals(configKeyHash, hashString(url + hashString(config)));
    }

    private boolean validateExamKeyHash(SebConfig sebConfig, String url, String examKeyHash) {
        List<String> validExamKeysList = sebConfig.getExamKeys();

        if (validExamKeysList == null || validExamKeysList.isEmpty()) {
            log.debug("No exam keys associated with this assessment, no validation needed.");
            return true;
        }


        for (String validExamKey : validExamKeysList) {
            if (StringUtils.equals(examKeyHash, hashString(url + validExamKey))) {
                log.debug("Exam key hash [{}] validated for URL [{}]", examKeyHash, url);
                return true;
            }
        }

        log.debug("Exam key hash [{}] could not be validated for URL [{}].", examKeyHash, url);
        return false;
    }

    private boolean publishConfigUpload(PublishedAssessmentIfc assessment, HttpServletRequest request) {
        String baseConfigResourceId = assessment.getAssessmentMetaDataByLabel(SebConfig.CONFIG_UPLOAD_ID);

        if (StringUtils.trimToNull(baseConfigResourceId) != null) {
            try {

                PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
                PublishedAssessmentFacade publishedAssessmentFacade = publishedAssessmentService.getPublishedAssessment(
                        assessment.getPublishedAssessmentId().toString());

                // Config that was uploaded in draft settings
                ContentResource baseConfigResource = contentHostingService.getResource(baseConfigResourceId);

                // Read plist config and add the new start link
                Parameters parameters = new Parameters();
                FileBasedConfigurationBuilder<FileBasedConfiguration> configBuilder =
                        new FileBasedConfigurationBuilder<FileBasedConfiguration>(XMLPropertyListConfiguration.class)
                                .configure(parameters.fileBased());
                FileBasedConfiguration config = configBuilder.getConfiguration();
                FileHandler fileHandler = new FileHandler(config);
                fileHandler.setEncoding(SebConfig.CONFIG_ENCODING);
                fileHandler.load(baseConfigResource.streamContent());

                String startLink = getStartLink(assessment, request);

                if (startLink != null) {
                    config.setProperty(SebConfig.START_LINK_KEY, startLink);
                } else {
                    return false;
                }
                config.setProperty(SebConfig.QUIT_LINK_KEY, SebConfig.QUIT_LINK);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                fileHandler.save(outputStream);
                outputStream.flush();

                // Push advisor, beacuse instructors can't add public resources normally
                securityService.pushAdvisor(alwaysAllowSecurityAdvisor);

                // Create new resource that will be used for delivery and published settings
                // Resource needs to be public, because SEB will request it before login, when launched directly from Samigo
                ContentResource publishedConfigResource  = contentHostingService.addResource(
                        UUID.randomUUID().toString() + ".seb",
                        ContentHostingService.COLLECTION_PUBLIC,
                        1,
                        baseConfigResource.getContentType(),
                        new ByteArrayInputStream(outputStream.toByteArray()),
                        baseConfigResource.getProperties(),
                        Collections.emptyList(),
                        false,
                        null,
                        null,
                        NotificationService.NOTI_NONE);

                securityService.popAdvisor(alwaysAllowSecurityAdvisor);

                // Add new resourceId to the assessment meta data
                log.debug("assessment [{}]", publishedAssessmentFacade);
                log.debug("release date [{}]", publishedAssessmentFacade.getStartDate());
                log.debug("due date [{}]", publishedAssessmentFacade.getRetractDate());
                Set<PublishedMetaData> metaDataSet = publishedAssessmentFacade.getAssessmentMetaDataSet();
                PublishedMetaData resourceIdMetaData = metaDataSet.stream()
                        .filter(metaData -> SebConfig.CONFIG_UPLOAD_ID.equals(metaData.getLabel()))
                        .findFirst()
                        .orElse(new PublishedMetaData());

                if (resourceIdMetaData.getId() == null) {
                    resourceIdMetaData.setAssessment(assessment);
                    resourceIdMetaData.setLabel(SebConfig.CONFIG_UPLOAD_ID);
                }
                resourceIdMetaData.setEntry(publishedConfigResource.getId());

                publishedAssessmentService.saveOrUpdateMetaData(resourceIdMetaData);

                return true;
            } catch (Exception e) {
                log.error("Could not create modified SEB configuration {}", e.toString());
            }
        }

        return false;
    }

    private void expireValidationData(Long assessmentId, String userId) {
        log.debug("Expiring SEB valiadtion data for user [{}] and published assessment [{}]", userId, assessmentId);
        persistenceService.getSebValidationFacadeQueries().expireSebValidations(assessmentId, userId);
    }

    private String hashString(String string) {
        return HashingUtil.hashString(string);
    }

    private boolean isSiteSebEnabled(final String siteId) {
        try {
            Site site = siteService.getSite(siteId);
            String sebSiteEnabled = site.getProperties().getProperty(SEB_SITE_ENABLED_PROPERTY);
            return Boolean.parseBoolean(sebSiteEnabled);
        } catch(IdUnusedException e) {
            // Ignore missing site
            log.warn("Site with Id [{}] not found [{}]", siteId);
        }

        return false;
    }

    private String getCurrentUserId() {
        if (userDirectoryService.getCurrentUser() == null) {
            log.error("No access to current user");
            return null;
        }

        return userDirectoryService.getCurrentUser().getId();
    }

    private String getStartLink(PublishedAssessmentIfc assessment, HttpServletRequest request){
        URL requestUrl;
        try {
            requestUrl = new URL(request.getRequestURL().toString());
        } catch (MalformedURLException e) {
            log.error("Request url malformed: [{}]", request.getRequestURL().toString());
            // ERROR -> return SUCCESS
            return null;
        }

        String assessmentAccessId = StringUtils.trimToNull(assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.ALIAS));
        if (assessmentAccessId != null) {
            return UriComponentsBuilder.newInstance()
                    .scheme(requestUrl.getProtocol())
                    .host(requestUrl.getHost())
                    .port(requestUrl.getPort())
                    .path(LOGIN_SERVLET_PATH)
                    .queryParam("id", assessmentAccessId)
                    .build().toUriString();
        } else {
            log.error("Empty assessment alias - could not generate start link.");
            return null;
        }
    }

    private String getConfigLink(SebConfig sebConfig, Long assessmentId) {
        if (sebConfig == null || sebConfig.getConfigMode() == null || assessmentId == null) {
            return "";
        }

        switch (sebConfig.getConfigMode()) {
            case MANUAL:
            case UPLOAD:
                return "/api/sites/any/assessments/published/" + assessmentId + "/sebConfig";
            case CLIENT:
            default:
                return "";
        }
    }
}
