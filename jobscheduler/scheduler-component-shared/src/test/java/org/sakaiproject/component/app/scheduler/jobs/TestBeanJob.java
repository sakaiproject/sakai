/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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

import lombok.extern.slf4j.Slf4j;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: John Ellis
 * Date: Dec 1, 2005
 * Time: 5:16:00 PM
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
public class TestBeanJob implements Job {

   private String configMessage;

   public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
      log.debug("Test bean job trigger with message: {}", getConfigMessage());   
   }

   public String getConfigMessage() {
      return configMessage;
   }

   public void setConfigMessage(String configMessage) {
      this.configMessage = configMessage;
   }
}
