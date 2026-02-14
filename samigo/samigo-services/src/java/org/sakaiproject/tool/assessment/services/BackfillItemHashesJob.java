/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.services;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionException;
import org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidationException;
import org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidator;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.scheduler.jobs.AbstractConfigurableJob;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.assessment.facade.BackfillItemHashResult;

@Slf4j
public class BackfillItemHashesJob extends AbstractConfigurableJob {

    private static final String BATCH_SIZE = "batch.size";
    private static final String BATCH_SIZE_NON_NUMERIC = "batch.size.non.numeric";
    private static final String BACKFILL_ITEMS = "backfill.items";
    private static final String BACKFILL_PUBLISHED_ITEMS = "backfill.published.items";
    private static final String BACKFILL_PUBLISHED_ITEMS_BASELINE_HASH = "backfill.published.items.baseline.hash";
    private static final String NOTIFICATION_EMAIL_ADDR = "notification.email.address";
    private static final String INVALID_NOTIFICATION_EMAIL_ADDR = "invalid.notification.email.address";
    private static final String UNRECOGNIZED_JOB_CONFIG_PROPERTY = "unrecognized.job.config.property";
    private static final int DEFAULT_BATCH_SIZE = 100;

    private SessionManager sessionManager;
    private ServerConfigurationService serverConfigurationService;
    private EmailService emailService;

    @Override
    public void runJob() throws JobExecutionException {
        final long start = System.currentTimeMillis();
        log.info("Backfill question hashing - Job start");

        BackfillItemHashResult itemBackfillResult = null;
        BackfillItemHashResult publishedItemBackfillResult = null;
        RuntimeException error = null;
        try {
            logIn();
            final int batchSize = getBatchSize();
            final boolean backfillItemHashes = getBackfillItemHashes();
            if ( backfillItemHashes ) {
                final ItemService itemService = new ItemService();
                itemBackfillResult = itemService.backfillItemHashes(batchSize, true);
            }
            final boolean backfillPublishedItemHashes = getBackfillPublishedItemHashes();
            if ( backfillPublishedItemHashes ) {
                final boolean backfillPublishedItemBaselineHashes = getBackfillPublishedItemBaselineHashes();
                final PublishedItemService publishedItemService = new PublishedItemService();
                publishedItemBackfillResult = publishedItemService.backfillItemHashes(batchSize, backfillPublishedItemBaselineHashes);
            }
        } catch (RuntimeException e) {
            error = e;
        } finally {
            try {
                if (error == null) {
                    log.info("Backfill question hashing - Job exiting \n{}\n{}",
                            elapsedTimeMessage(start),
                            jobResultMessage(itemBackfillResult, publishedItemBackfillResult, " "));
                } else {
                    // we know Quartz will not log exceptions by default, so we do it here
                    log.info("Backfill question hashing - Job exiting with error.\n{}\n{}",
                            elapsedTimeMessage(start),
                            jobResultMessage(itemBackfillResult, publishedItemBackfillResult, " "), error);
                }
                sentNotification(itemBackfillResult, publishedItemBackfillResult, error);
            } catch ( RuntimeException e ) {
                log.warn("Failed to send job completion email notification", e);
            } finally {
                try {
                    logOut();
                } catch (Exception e) {
                    log.warn("Backfill question hashing - Failed to cleanup Sakai session.", e);
                }
            }
            if ( error != null ) {
                throw new JobExecutionException(error);
            }
        }
    }

    private void sentNotification(BackfillItemHashResult itemBackfillResult, BackfillItemHashResult publishedItemBackfillResult, RuntimeException error) {
        final String to = getNotificationEmailAddress();
        if ( to == null ) {
            return;
        }

        final String from = "<"+ serverConfigurationService.getSmtpFrom() + ">";
        final StringBuilder body = new StringBuilder();
        if ( error != null ) {
            body.append("Job failed with following error message. See logs for full details.").append("\n\n")
                    .append(error.getMessage())
                    .append("\n\n");
        }
        body.append(jobResultMessage(itemBackfillResult, publishedItemBackfillResult, "\n\n"));
        emailService.send(from,
                to,
                "Backfill Item Hash Job: " + (error == null ? "Success" : "Failure"),
                body.toString(),
                to,
                null,
                null);
    }

    private int getBatchSize() {
        String val = StringUtils.trimToNull(getConfiguredProperty(BATCH_SIZE));
        if ( val == null ) {
            return DEFAULT_BATCH_SIZE;
        }
        int valInt = Integer.parseInt(val);
        return valInt <= 0 ? DEFAULT_BATCH_SIZE : valInt;
    }

    private boolean getBackfillItemHashes() {
        return getBooleanConfig(BACKFILL_ITEMS, true);
    }

    private boolean getBackfillPublishedItemHashes() {
        return getBooleanConfig(BACKFILL_PUBLISHED_ITEMS, true);
    }

    private boolean getBackfillPublishedItemBaselineHashes() {
        return getBooleanConfig(BACKFILL_PUBLISHED_ITEMS_BASELINE_HASH, false);
    }

    private boolean getBooleanConfig(String name, boolean defaultTo) {
        String val = StringUtils.trimToNull(getConfiguredProperty(name));
        if ( val == null ) {
            return defaultTo;
        }
        return Boolean.parseBoolean(val);
    }

    private String getNotificationEmailAddress() {
        return StringUtils.trimToNull(getConfiguredProperty(NOTIFICATION_EMAIL_ADDR));
    }

    private String jobResultMessage(BackfillItemHashResult itemBackfillResult, BackfillItemHashResult publishedItemBackfillResult, String separator) {
        final StringBuilder sb = new StringBuilder();
        if ( itemBackfillResult != null ) {
            sb.append("Item backfill results: [").append(itemBackfillResult).append("]");
            if ( publishedItemBackfillResult != null ) {
                sb.append(separator);
            }
        }
        if ( publishedItemBackfillResult != null ) {
            sb.append("Published item backfill results: [").append(publishedItemBackfillResult).append("]");
        }
        return sb.toString();
    }

    private String elapsedTimeMessage(long start) {
        return "Elapsed time: [" + (System.currentTimeMillis() - start) + "ms]";
    }

    private void logIn() {
        Session sakaiSession = sessionManager.getCurrentSession();
        sakaiSession.setUserId("admin");
        sakaiSession.setUserEid("admin");
    }

    private void logOut() {
        final Session currentSession = sessionManager.getCurrentSession();
        currentSession.invalidate(); // includes removing from ThreadLocal storage
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public static class BackfillItemHashesConfigurationValidator implements ConfigurableJobPropertyValidator {

        public void assertValid(String propertyLabel, String value)
                throws ConfigurableJobPropertyValidationException {
            value = StringUtils.trimToNull(value);
            if ( BATCH_SIZE.equals(propertyLabel) ) {
                if ( value == null ) {
                    return;
                }

                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new ConfigurableJobPropertyValidationException(BATCH_SIZE_NON_NUMERIC);
                }
            } else if ( BACKFILL_ITEMS.equals(propertyLabel)
                    || BACKFILL_PUBLISHED_ITEMS.equals(propertyLabel)
                    || BACKFILL_PUBLISHED_ITEMS_BASELINE_HASH.equals(propertyLabel)) {
                // parsing booleans never fails
            } else if ( NOTIFICATION_EMAIL_ADDR.equals(propertyLabel) ) {
                if ( value == null ) {
                    return;
                }
                try {
                    new InternetAddress(value, true);
                } catch (AddressException e) {
                    throw new ConfigurableJobPropertyValidationException(INVALID_NOTIFICATION_EMAIL_ADDR);
                }
            } else {
                throw new ConfigurableJobPropertyValidationException(UNRECOGNIZED_JOB_CONFIG_PROPERTY);
            }
        }
    }

}
