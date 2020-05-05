/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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
package org.sakaiproject.assignment.impl.conversion;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.assignment.api.conversion.AssignmentConversionService;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
@DisallowConcurrentExecution
public class Assignment12ConversionJob implements Job {

    public static final String SIZE_PROPERTY = "length.attribute.property";
    public static final String NUMBER_PROPERTY = "number.attributes.property";

    @Setter
    private AssignmentConversionService assignmentConversionService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("<===== Assignment Conversion Job start =====>");

        // never run as a recovery
        if (context.isRecovering()) {
            log.warn("<===== Assignment Conversion Job doesn't support recovery, job will terminate... =====>");
        } else {
            JobDataMap map = context.getMergedJobDataMap();
            Integer size = Integer.parseInt((String) map.get(SIZE_PROPERTY));
            Integer number = Integer.parseInt((String) map.get(NUMBER_PROPERTY));
            assignmentConversionService.runConversion(number, size);
        }

        log.info("<===== Assignment Conversion Job end =====>");
    }
}
