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
package org.sakaiproject.component.app.scheduler.jobs.cm;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.scheduler.jobs.cm.processor.DataProcessor;
import org.sakaiproject.component.app.scheduler.jobs.cm.processor.ProcessorState;
import org.sakaiproject.email.api.EmailService;

import java.util.List;

@Slf4j
public class SynchronizationJob extends AbstractAdminJob {

    @Setter
    private ServerConfigurationService serverConfigurationService;
    @Setter
    private List<DataProcessor> dataProcessors;
    @Setter
    private EmailService emailService;
    @Setter
    private List<String> recipients;
    @Setter
    private boolean emailNotification;

    private String fromAddress = null;

    public void init() {
        fromAddress = "\"" +
                serverConfigurationService.getString("ui.institution", "Sakai") +
                " <no-reply@" +
                serverConfigurationService.getServerName() +
                ">\"";
    }

    public void executeInternal(JobExecutionContext jec) throws JobExecutionException {
        log.info("Starting Integration Job");

        JobDataMap jdm = jec.getMergedJobDataMap();

        if (dataProcessors != null) {
            for (DataProcessor dp : dataProcessors) {
                ProcessorState state = null;

                try {
                    state = dp.init(jdm);
                    dp.preProcess(state);
                    dp.process(state);
                    dp.postProcess(state);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    sendEmail(dp, state);
                    if (state != null) {
                        state.reset();
                    }
                }
            }
        } else {
            throw new JobExecutionException("Data processors list has not been set.");
        }

        log.info("Integration Job Complete");
    }

    private void sendEmail(DataProcessor dp, ProcessorState state) {
        if (emailNotification && recipients != null) {
            for (String recipient : recipients) {
                emailService.send(fromAddress, recipient, dp.getProcessorTitle(), dp.getReport(state), null, null, null);
            }
        }
    }
}
