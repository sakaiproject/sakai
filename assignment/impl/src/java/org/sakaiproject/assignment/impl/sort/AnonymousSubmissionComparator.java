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
package org.sakaiproject.assignment.impl.sort;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.assignment.api.model.AssignmentSubmission;

/**
 * Sorts assignment submissions by the submission's anonymous ID.
 */
@Slf4j
public class AnonymousSubmissionComparator implements Comparator<AssignmentSubmission> {

    private Collator collator;

    public AnonymousSubmissionComparator() {
        try {
            collator = new RuleBasedCollator(((RuleBasedCollator) Collator.getInstance()).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
        } catch (ParseException e) {
            // error with init RuleBasedCollator with rules
            // use the default Collator
            collator = Collator.getInstance();
            log.warn("{} AssignmentComparator cannot init RuleBasedCollator. Will use the default Collator instead. {}", this, e);
        }
    }

    @Override
    public int compare(AssignmentSubmission a1, AssignmentSubmission a2) {
        int result;
        String name1 = a1.getId();
        String name2 = a2.getId();
        result = collator.compare(name1, name2);
        return result;
    }

}
