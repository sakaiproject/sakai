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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.component.app.scheduler.jobs.cm.processor.ProcessorState;
import org.sakaiproject.coursemanagement.api.Section;

@Slf4j
public class SectionProcessor extends AbstractCMProcessor {

    public void processRow(String[] data, ProcessorState state) throws Exception {
        log.debug("Reconciling section {}", data[0]);

        if (cmService.isSectionDefined(data[0])) {
            updateSection(cmService.getSection(data[0]), data);
        } else {
            addSection(data);
        }
    }

    public String getProcessorTitle() {
        return "Section Processor";
    }

    public Section updateSection(Section section, String[] data) {
        log.debug("Updating Section {}", section.getEid());
        section.setTitle(data[1]);
        section.setDescription(data[2]);
        section.setCategory(data[3]);
        if (StringUtils.isNotBlank(data[4]) && cmService.isSectionDefined(data[4])) {
            section.setParent(cmService.getSection(data[4]));
        }
        // Note: There's no way to change the course offering.  This makes sense, though.
        if (cmService.isEnrollmentSetDefined(data[5])) {
            section.setEnrollmentSet(cmService.getEnrollmentSet(data[5]));
        }
        cmAdmin.updateSection(section);
        return section;
    }

    public Section addSection(String[] data) {
        log.debug("Adding Section {}", data[0]);
        return cmAdmin.createSection(data[0], data[1], data[2], data[3], StringUtils.defaultIfBlank(data[4], null), data[6], data[5]);
    }
}