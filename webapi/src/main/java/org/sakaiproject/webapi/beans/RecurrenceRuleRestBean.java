/****************************************************************************** 
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://opensource.org/licenses/ECL-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.beans;

import org.sakaiproject.calendar.api.RecurrenceRule;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecurrenceRuleRestBean {

    private int count;
    private String frequency;
    private int interval;
    private long until;

    public RecurrenceRuleRestBean(RecurrenceRule rr) {

        if (rr != null) {
            count = rr.getCount();
            frequency = rr.getFrequency();
            interval = rr.getInterval();
            until = rr.getUntil().getTime();
        }
    }
}
