/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.component.app.scheduler.jobs;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.id.api.IdManager;

/**
 * This is to test autowiring of Jobs.
 */
@Slf4j
public class AutowiredTestJob implements Job {

    @Inject
    public void setIdManager(IdManager idManager) {
        this.idManager = idManager;
    }

    private IdManager idManager;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (idManager != null) {
            log.info("Autowiring worked, generated ID {}", idManager.createUuid());
        } else {
            log.info("IdManager was not autowired :-(");
        }
    }
}
