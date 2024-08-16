/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.assignment.tool;

import java.time.Instant;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;

import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.user.api.UserDirectoryService;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public abstract class BaseComparator {

    protected String criteria;
    protected AssignmentService assignmentService;
    protected ResourceLoader rb;
    protected SiteService siteService;
    protected UserDirectoryService userDirectoryService;

    private Collator collator;

    public BaseComparator() {

        try {
            collator = new RuleBasedCollator(((RuleBasedCollator) Collator.getInstance()).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
        } catch (ParseException e) {
            // error with init RuleBasedCollator with rules
            // use the default Collator
            collator = Collator.getInstance();
            log.warn(this + " AssignmentComparator cannot init RuleBasedCollator. Will use the default Collator instead. " + e);
        }
    }

    protected int compareString(String s1, String s2) {

        int result;
        if (s1 == null && s2 == null) {
            result = 0;
        } else if (s2 == null) {
            result = 1;
        } else if (s1 == null) {
            result = -1;
        } else {
            result = collator.compare(s1.toLowerCase(), s2.toLowerCase());
        }
        return result;
    }

    protected int compareInstant(Instant t1, Instant t2) {

        if (t1 == null) {
            return -1;
        } else if (t2 == null) {
            return 1;
        } else if (t1.isBefore(t2)) {
            return -1;
        } else {
            return 1;
        }
    }

    protected int compareEstimate(String t1, String t2) {

        int result;
        if (StringUtils.isAllBlank(t1, t2)) {
            result = 0;
        } else if (StringUtils.isBlank(t2)) {
            result = 1;
        } else if (StringUtils.isBlank(t1)) {
            result = -1;
        } else {
            int i1, i2;
            i1 = assignmentService.timeToInt(t1);
            i2 = assignmentService.timeToInt(t2);
            result = (i1 < i2) ? -1 : 1;
        }
        return result;
    }

}
