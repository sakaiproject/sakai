/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.content.impl;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.FileConversionService;
import org.sakaiproject.content.api.persistence.FileConversionQueueItem;
import org.sakaiproject.content.api.persistence.FileConversionServiceRepository;
import org.sakaiproject.content.impl.converters.LoolFileConverter;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
@Setter
public class FileConversionServiceImpl implements FileConversionService {

    @Autowired private ContentHostingService contentHostingService;
    @Autowired private FileConversionServiceRepository repository;
    @Autowired private SecurityService securityService;
    @Autowired private ServerConfigurationService serverConfigurationService;

    private String converterBaseUrl;
    private boolean conversionEnabled;
    private boolean submitEnabled;
    private List<String> fromTypes;
    private ScheduledExecutorService master;
    private int pause;
    private int queueIntervalMinutes;
    private ExecutorService workers;

    public void init() {

        submitEnabled = serverConfigurationService.getBoolean("fileconversion.submit.enabled", false);

        if (submitEnabled) {
            fromTypes = serverConfigurationService.getStringList("fileconversion.fromtypes", DEFAULT_TYPES);
        }

        conversionEnabled = serverConfigurationService.getBoolean("fileconversion.conversion.enabled", false);

        if (conversionEnabled) {
            converterBaseUrl = serverConfigurationService.getString("fileconversion.converterurl", "http://localhost:9980");
            workers = Executors.newFixedThreadPool(serverConfigurationService.getInt("fileconversion.workerthreads", 5));
            master = Executors.newScheduledThreadPool(1);
            queueIntervalMinutes = serverConfigurationService.getInt("fileconversion.queueintervalminutes", 1);
            pause = serverConfigurationService.getInt("fileconversion.pausemillis", 1000);
        }

        startIfEnabled();
    }

    public void startIfEnabled() {

        log.debug("startIfEnabled()");

        if (!conversionEnabled) {
            log.debug("Conversion not enabled in Sakai properties. Not starting.");
            return;
        }

        SecurityAdvisor securityAdvisor = (String userId, String function, String reference) -> {

            if (ContentHostingService.AUTH_RESOURCE_READ.equals(function)
                    || ContentHostingService.AUTH_RESOURCE_ADD.equals(function)
                    || ContentHostingService.AUTH_RESOURCE_WRITE_ANY.equals(function)) {
                return SecurityAdvisor.SecurityAdvice.ALLOWED;
            } else {
                return SecurityAdvisor.SecurityAdvice.NOT_ALLOWED;
            }
        };

        master.scheduleWithFixedDelay(() -> {

            List<FileConversionQueueItem> items
                = repository.findByStatus(FileConversionQueueItem.Status.NOT_STARTED);

            log.debug("Number of unstarted conversion items: {}", items.size());

            securityService.pushAdvisor(securityAdvisor);

            try {
                items.forEach(item -> {

                    final String ref = item.getReference();

                    log.debug("Converting ref {} ...", ref);

                    try {
                        final ContentResource source = contentHostingService.getResource(ref);

                        workers.submit(() -> {

                            log.debug("Setting item with ref {} to IN_PROGRESS ...", ref);
                            item.setStatus(FileConversionQueueItem.Status.IN_PROGRESS);
                            item.setAttempts(item.getAttempts() + 1);
                            item.setLastAttemptStarted(Instant.now());

                            FileConversionQueueItem inProgressItem = repository.save(item);

                            try {
                                String sourceFileName = ref.substring(ref.lastIndexOf("/") + 1);
                                log.debug("Source Filename: {}", sourceFileName);
                                String convertedFileName = "";
                                if (sourceFileName.contains(".")) {
                                    convertedFileName = sourceFileName.split("\\.")[0] + ".pdf";
                                } else {
                                    convertedFileName = sourceFileName + ".pdf";
                                }

                                byte[] convertedFileBytes = LoolFileConverter.convert(converterBaseUrl, source.streamContent());

                                String previewId = ref.substring(0, ref.lastIndexOf("/")) + convertedFileName;

                                log.debug("Converted file id: {}", previewId);
                                ResourcePropertiesEdit properties = contentHostingService.newResourceProperties();
                                properties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, convertedFileName);

                                securityService.pushAdvisor(securityAdvisor);
                                ContentResource previewResource
                                        = contentHostingService.addAttachmentResource(convertedFileName, "application/pdf", convertedFileBytes, properties);

                                contentHostingService.addProperty(ref, ContentHostingService.PREVIEW, previewResource.getId());

                                log.debug("Deleting item with ref {}. It's been successfully converted.", ref);

                                repository.deleteById(inProgressItem.getId());
                            } catch (Exception e) {

                                inProgressItem.setStatus(FileConversionQueueItem.Status.NOT_STARTED);
                                repository.save(inProgressItem);
                                log.error("Call to conversion service failed", e);
                            } finally {
                                securityService.popAdvisor(securityAdvisor);
                            }
                        });
                    } catch (IdUnusedException iue) {
                        log.error("No resource found for ref {}", ref);
                    } catch (PermissionException pe) {
                        log.error("No permissions to get the resource or its properties. Resource: {}", ref, pe);
                    } catch (Exception e) {
                        log.error("Failed to convert resource for ref {}", ref, e);
                    }
                    try {
                        Thread.sleep(pause);
                    } catch (Exception se) {
                    }
                });
            } finally {
                securityService.popAdvisor(securityAdvisor);
            }
        }, 0, queueIntervalMinutes, TimeUnit.MINUTES);
    }

    public void destroy() {
        workers.shutdownNow();
        master.shutdownNow();
    }

    public boolean canConvert(String fromType) {
        return submitEnabled && fromTypes.contains(fromType);
    }

    @Transactional
    public void submit(String ref) {

        if (!submitEnabled) {
            log.debug("Submit not enabled in Sakai properties. Not submitting {}.", ref);
            return;
        }

        log.debug("submit({})", ref);

        FileConversionQueueItem newItem = new FileConversionQueueItem();
        newItem.setReference(ref);
        newItem.setStatus(FileConversionQueueItem.Status.NOT_STARTED);
        newItem.setAttempts(0);
        repository.save(newItem);
    }
}
