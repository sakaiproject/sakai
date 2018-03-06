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

import org.sakaiproject.user.api.User;

/**
 * This sorts users.
 */
@Slf4j
public class UserComparator implements Comparator<User> {

    private Collator collator;

    public UserComparator() {
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

    public int compare(User u1, User u2) {
        String name1 = u1.getSortName();
        String name2 = u2.getSortName();
        return collator.compare(name1, name2);
    }
}
