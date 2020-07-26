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
import org.sakaiproject.content.api.repository.FileConversionQueueItemRepository;
import org.sakaiproject.content.hbm.FileConversionQueueItem;
import org.sakaiproject.content.impl.converters.LoolFileConverter;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
public class FileConversionServiceImpl implements FileConversionService {

    @Autowired private ContentHostingService contentHostingService;
    @Autowired private FileConversionQueueItemRepository repository;
    @Autowired private SecurityService securityService;
    @Autowired private ServerConfigurationService serverConfigurationService;
    private TransactionTemplate transactionTemplate;

    private String converterBaseUrl;
    private boolean enabled;
    private List<String> fromTypes;
    private ScheduledExecutorService master;
    private int pause;
    private int queueIntervalMinutes;
    private ExecutorService workers;

    public void init() {

        enabled = serverConfigurationService.getBoolean("fileconversion.enabled", false);

        if (enabled) {
            fromTypes = serverConfigurationService.getStringList("fileconversion.fromtypes", DEFAULT_TYPES);
            converterBaseUrl = serverConfigurationService.getString("fileconversion.converterurl", "http://localhost:9980");

            workers = Executors.newFixedThreadPool(serverConfigurationService.getInt("fileconversion.workerthreads", 5));
            master = Executors.newScheduledThreadPool(1);
            queueIntervalMinutes = serverConfigurationService.getInt("fileconversion.queueintervalminutes", 1);
            pause = serverConfigurationService.getInt("fileconversion.pausemillis", 1000);
        }
    }

    public void startIfEnabled() {

        log.debug("startIfEnabled()");

        if (!enabled) {
            log.debug("Not enabled in Sakai properties. Not starting.");
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

            log.debug("scheduling ...");

            List<FileConversionQueueItem> items = repository.findByStatus(FileConversionQueueItem.Status.NOT_STARTED);

            items.forEach(i -> log.info(i.getReference()));

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

                            FileConversionQueueItem inProgressItem = transactionTemplate.execute(status -> repository.save(item));

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
                                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                                    public void doInTransactionWithoutResult(TransactionStatus status) {
                                        repository.delete(inProgressItem);
                                    }
                                });
                            } catch (Exception e) {
                                item.setStatus(FileConversionQueueItem.Status.NOT_STARTED);
                                transactionTemplate.execute(new TransactionCallbackWithoutResult() {

                                    public void doInTransactionWithoutResult(TransactionStatus status) {
                                        repository.save(item);
                                    }
                                });
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
        return enabled && fromTypes.contains(fromType);
    }

    @Transactional(transactionManager = "jpaTransactionManager")
    public void convert(String ref) {

        if (!enabled) {
            log.debug("Not enabled in Sakai properties. Not converting {}.", ref);
            return;
        }

        log.debug("convert({})", ref);

        FileConversionQueueItem newItem = new FileConversionQueueItem();
        newItem.setReference(ref);
        newItem.setStatus(FileConversionQueueItem.Status.NOT_STARTED);
        newItem.setAttempts(0);
        repository.save(newItem);
    }
}
