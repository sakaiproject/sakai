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
package org.sakaiproject.component.app.scheduler.jobs.logmessage;

import lombok.extern.slf4j.Slf4j;

import org.quartz.JobExecutionException;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import org.sakaiproject.component.app.scheduler.jobs.AbstractConfigurableJob;

/**
 * This is a simple Job that allows a message to be logged.
 * This is useful for testing and time stamping the log files.
 */
@Slf4j
public class LogMessageJob extends AbstractConfigurableJob {

    private static final Marker fatal = MarkerFactory.getMarker("FATAL");

    @Override
    public void runJob() throws JobExecutionException {
        String level = getConfiguredProperty("level");
        String message = getConfiguredProperty("message");
        String logger = getConfiguredProperty("logger");
        if ("trace".equalsIgnoreCase(level)) {
            log.trace(message);
        } else if ("debug".equalsIgnoreCase(level)) {
            log.debug(message);
        } else if ("info".equalsIgnoreCase(level)) {
            log.info(message);
        } else if ("warn".equalsIgnoreCase(level)) {
            log.warn(message);
        } else if ("error".equalsIgnoreCase(level)) {
            log.error(message);
        } else if ("fatal".equalsIgnoreCase(level)) {
            log.error(fatal, message);
        }
    }
}
