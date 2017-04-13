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
package org.sakaiproject.component.app.scheduler.jobs.cm.processor.sis;

import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.component.app.scheduler.jobs.cm.processor.BaseCsvFileProcessor;
import org.sakaiproject.coursemanagement.api.CourseManagementAdministration;
import org.sakaiproject.coursemanagement.api.CourseManagementService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

abstract public class AbstractCMProcessor extends BaseCsvFileProcessor {

    @Setter
    protected CourseManagementService cmService;
    @Setter
    protected CourseManagementAdministration cmAdmin;
    @Setter
    protected String dateFormat = "MM/dd/yyyy";

    public Date getDate(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern(dateFormat);
        try {
            return Date.from(LocalDate.parse(str, df).atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException dtpe) {
            throw new RuntimeException("Cannot parse the date from: " + str, dtpe);
        }
    }
}
