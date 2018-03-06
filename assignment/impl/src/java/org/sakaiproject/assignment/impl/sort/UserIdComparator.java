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
package org.sakaiproject.assignment.impl.sort;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Comparator;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * Sorts a collection of User IDs by their sortnames.
 */
@Slf4j
public class UserIdComparator implements Comparator<String> {

    private Collator collator;
    private UserDirectoryService userDirectoryService;

    public UserIdComparator(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
        // TODO this should be in a service and should repect the current user's locale
        try {
            collator = new RuleBasedCollator(((RuleBasedCollator) Collator.getInstance()).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
        } catch (ParseException e) {
            // error with init RuleBasedCollator with rules
            // use the default Collator
            collator = Collator.getInstance();
            log.warn("{} UserIdComparator cannot init RuleBasedCollator. Will use the default Collator instead. {}", this, e);
        }
        // This is to ignore case of the values
        collator.setStrength(Collator.SECONDARY);
    }

    @Override
    public int compare(String id1, String id2) {
        // sorted by the user's display name
        String s1 = null;
        if (id1 != null) {
            try {
                User u1 = userDirectoryService.getUser(id1);
                s1 = u1 != null ? u1.getSortName() : null;
            } catch (Exception e) {
                log.warn(" AssignmentComparator.compare {} id={}", e.getMessage(), id1);
            }
        }

        String s2 = null;
        if (id2 != null) {
            try {
                User u2 = userDirectoryService.getUser(id2);
                s2 = u2 != null ? u2.getSortName() : null;
            } catch (Exception e) {
                log.warn(" AssignmentComparator.compare {} id={}", e.getMessage(), id2);
            }
        }

        return compareNullSafeString(s1, s2);
    }


    private int compareNullSafeString(String s1, String s2) {
        int result;
        if (s1 == null && s2 == null) {
            result = 0;
        } else if (s2 == null) {
            result = 1;
        } else if (s1 == null) {
            result = -1;
        } else {
            result = collator.compare(s1, s2);
        }
        return result;
    }
}
