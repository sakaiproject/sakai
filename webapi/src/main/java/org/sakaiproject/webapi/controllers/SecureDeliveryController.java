/******************************************************************************
 * Copyright 2023 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.plist.XMLPropertyListConfiguration;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.assessment.business.entity.SebConfig;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.webapi.beans.SebValidationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 */
@Slf4j
@RestController
public class SecureDeliveryController extends AbstractSakaiApiController {

    private String ASSESSMENTS_TOOL_NAME = "Assessments";
    private String LOGIN_SERVLET_PATH = "/samigo-app/servlet/Login";

    private static char[] ILLEGAL_FILENAME_CHARS =
            new char[] { '#', '%', '&', '{', '}', '\\', '<', '>', '*', '?', '/', '$', '!', '\"', '\'', ':', '@', '+', '`', '|', '=' };

    @Autowired
    @Qualifier("org.sakaiproject.component.api.ServerConfigurationService")
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private ContentHostingService contentHostingService;

    @PutMapping(value = "/sites/{siteId}/assessments/published/{publishedAssessmentId}/sebValidation", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HttpStatus> setSebValidation(@PathVariable String siteId, @PathVariable Long publishedAssessmentId,
            @RequestBody SebValidationBean sebValidation) {
        Session session = checkSakaiSession();
        String userId = session.getUserId();
        String url = StringUtils.trimToNull(sebValidation.getUrl());
        String examKey = StringUtils.trimToNull(sebValidation.getExamKey());
        String configKey = StringUtils.trimToNull(sebValidation.getConfigKey());

        if (publishedAssessmentId != null
                && !StringUtils.isEmpty(url)
                && !StringUtils.isAllEmpty(examKey, configKey)
                && url.length() <= 255 && examKey.length() <= 64 && configKey.length() <= 64) {
            persistenceService.getSebValidationFacadeQueries()
                    .saveSebValidation(publishedAssessmentId, userId, url, configKey, examKey);

            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/sites/{siteId}/assessments/published/{publishedAssessmentId}/sebConfig")
    public ResponseEntity<?> getSebConfig(@PathVariable String siteId, @PathVariable String publishedAssessmentId,
            @RequestParam(defaultValue = "false") boolean launch, HttpServletRequest request) {

        PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
        PublishedAssessmentFacade publishedAssessment = publishedAssessmentService.getPublishedAssessment(publishedAssessmentId);

        if (publishedAssessment == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        HashMap<String, String> assessmentMetaDataMap = publishedAssessment.getAssessmentMetaDataMap();
        SebConfig sebConfig = SebConfig.of(assessmentMetaDataMap);
        switch(sebConfig.getConfigMode()) {
            case MANUAL:
                return getManualSebConfig(publishedAssessment, launch, request);
            case UPLOAD:
                return getUploadSebConfig(publishedAssessment, launch);
            default:
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<String> getManualSebConfig(PublishedAssessmentFacade publishedAssessment,
            boolean launch, HttpServletRequest request) {

        HashMap<String, String> assessmentMetaDataMap = publishedAssessment.getAssessmentMetaDataMap();

        String fileName = getSebConfigFileName(publishedAssessment);

        String assessmentAccessId = assessmentMetaDataMap.get(AssessmentMetaDataIfc.ALIAS);
        SebConfig sebConfig = SebConfig.of(assessmentMetaDataMap);

        try {
            URL requestUrl = new URL(request.getRequestURL().toString());

            String startLink = UriComponentsBuilder.newInstance()
                    .scheme(requestUrl.getProtocol())
                    .host(requestUrl.getHost())
                    .port(requestUrl.getPort())
                    .path(LOGIN_SERVLET_PATH)
                    .queryParam("id", assessmentAccessId)
                    .build().toUriString();
            sebConfig.setStartLink(startLink);
        } catch (MalformedURLException e) {
            log.error("Request url malformed {}", request.getRequestURL().toString());
        }

        return new ResponseEntity<>(sebConfig.toPList(), getSebConfigHeaders(fileName, launch), HttpStatus.OK);
    }


    private String getSebConfigFileName(PublishedAssessmentFacade publishedAssessment) {
        String fileName = publishedAssessment.getTitle() + ".seb";
        fileName = StringUtils.remove(fileName, " " + publishedAssessment.getAssessmentMetaDataByLabel(
                SecureDeliveryServiceAPI.TITLE_DECORATION));
        return cleanFileName(fileName);
    }

    private HttpHeaders getSebConfigHeaders(String fileName, boolean launch) {
        HttpHeaders headers = new HttpHeaders();

        if (launch) {
            headers.setContentType(MediaType.valueOf("application/seb"));
        } else {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData(fileName, fileName);
        }

        return headers;
    }

    public ResponseEntity<String> getUploadSebConfig(PublishedAssessmentFacade publishedAssessment, boolean launch) {
        String uploadResourceId = publishedAssessment.getAssessmentMetaDataByLabel(SebConfig.CONFIG_UPLOAD_ID);
        String fileName = getSebConfigFileName(publishedAssessment);

        try {
            ContentResource uploadResource = contentHostingService.getResource(uploadResourceId);

            return new ResponseEntity<>(IOUtils.toString(uploadResource.streamContent(), StandardCharsets.UTF_8), getSebConfigHeaders(fileName, launch), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Could not get content resource of seb config upload for id [{}]: {}", uploadResourceId, e.toString());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/sites/{siteId}/assessments/new/sebConfig", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> setSebConfig(@PathVariable String siteId, @RequestAttribute("file") FileItem file, HttpServletRequest request) {
        Session session = checkSakaiSession();

        checkSite(siteId);

        try {
            // Parse configuration, to make sure it is a valid plist file
            Parameters parameters = new Parameters();
            FileBasedConfigurationBuilder<FileBasedConfiguration> configBuilder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(XMLPropertyListConfiguration.class)
                .configure(parameters.fileBased());
            FileHandler fileHandler = new FileHandler(configBuilder.getConfiguration());
            fileHandler.setEncoding(SebConfig.CONFIG_ENCODING);
            fileHandler.load(file.getInputStream());

            // Get filename from upload and clean it or generate one
            String fileName = Optional.ofNullable(StringUtils.trimToNull(file.getName()))
                    .map(this::cleanFileName)
                    .orElseGet(() -> UUID.randomUUID().toString() + ".seb");

            // Create properties to add display name
            ResourceProperties resourceProperties = new BaseResourceProperties();
            resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fileName);

            // Check for update assessment permission
            ContentResource contentResource = contentHostingService.addAttachmentResource(fileName, siteId, ASSESSMENTS_TOOL_NAME,
                    file.getContentType(), file.getInputStream(), resourceProperties);

            return new ResponseEntity<>(contentResource.getId(), HttpStatus.OK);
        } catch(ConfigurationException | IOException e) {
            log.error("Could not get input stream of uploaded file {}", e.toString());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (IdInvalidException | IdUsedException | InconsistentException | OverQuotaException | ServerOverloadException e) {
            log.error("Could not process upload: {}", e.toString());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (PermissionException e) {
            log.error("User {} does not have permission to upload attachment on site {}", session.getUserId(), siteId);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    private String cleanFileName(String fileName) {
        String cleanFileName = fileName = StringUtils.replace(fileName, " ", "-");

        for (char illegalChar : ILLEGAL_FILENAME_CHARS) {
            cleanFileName = StringUtils.remove(cleanFileName, illegalChar);
        }

        return cleanFileName;
    }
}
