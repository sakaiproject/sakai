/**
 * Copyright (c) 2003-2020 The Apereo Foundation
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
package org.sakaiproject.plus.impl;

import java.util.Observable;
import java.util.Observer;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.plus.api.PlusService;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlusEventObserver implements Observer {

    @Autowired private EventTrackingService eventTrackingService;
    @Autowired private PlusService plusService;

    public void init() {
        eventTrackingService.addLocalObserver(this);
    }

    public void destroy() {
        eventTrackingService.deleteObserver(this);
    }

    /* Two Kinds of events
     *
     * From the UI:
     * gradebook.updateItemScore@/gradebookng/7/12/55a0c76a-69e2-4ca7-816b-3c2e8fe38ce0/42/OK/instructor[m, 2]
     *
     * From web services and server side service calls:
     * gradebook.updateItemScore@/gradebook/a77ed1b6-ceea-4339-ad60-8bbe7219f3b5/Trophy/55a0c76a-69e2-4ca7-816b-3c2e8fe38ce0/99.0/student[m, 2]
     * gradebook.updateItemComment@/gradebook/fd45f02f-1eeb-4478-aee5-d8c5d5f4733b/11/2a2d129e-99ab-4465-82cb-e560bd365a14/0.0/student
     *
     */
    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Event) {
            Event event = (Event) arg;
            if (event.getModify() && StringUtils.isNoneBlank(event.getEvent())) {
                switch (event.getEvent()) {
                    case "gradebook.updateItemScore": // grade updated in gradebook lets attempt to update the submission
                        log.debug("Event = {}", event.getEvent());
                        plusService.processGradeEvent(event);
                        break;
                    case "gradebook.updateItemComment": // comment updated in gradebook lets attempt to update the submission
                        log.debug("Event = {}", event.getEvent());
                        plusService.processGradeEvent(event);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
