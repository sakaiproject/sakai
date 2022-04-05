/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.util.comparator;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Comparator;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.user.api.User;
import org.springframework.util.comparator.NullSafeComparator;

/**
 * This sorts users.
 */
@Slf4j
public class UserSortNameComparator implements Comparator<User> {

    private Collator collator;
    private boolean nullsLow = false;

    public UserSortNameComparator() {
        collator = Collator.getInstance();
        try {
            String oldRules = ((RuleBasedCollator) collator).getRules();
            String newRules = oldRules.replaceAll("<'\u005f'", "<' '<'\u005f'");
            collator = new RuleBasedCollator(newRules);
        } catch (ParseException e) {
            log.warn("Can't create custom collator instead using the default collator, {}", e.toString());
        }
        collator.setStrength(Collator.SECONDARY); // ignore case
    }

    public UserSortNameComparator(boolean nullsLow) {
        this();
        this.nullsLow = nullsLow;
    }

    public int compare(User u1, User u2) {
        if (u1 == u2) return 0;
        if (u1 == null) return (nullsLow ? -1 : 1);
        if (u2 == null) return (nullsLow ? 1 : -1);

        // Replace spaces to handle sorting scenarios where surname has space
        String prop1 = StringUtils.replace(u1.getSortName(), " ", "+");
        String prop2 = StringUtils.replace(u2.getSortName(), " ", "+");
        if (StringUtils.equals(prop1, prop2)) {
            prop1 = u1.getEid();
            prop2 = u2.getEid();
        }

        return new NullSafeComparator<>(collator, nullsLow).compare(prop1, prop2);
    }
}
