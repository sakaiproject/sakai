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
    private boolean useDisplayName = false;

    public UserSortNameComparator() {
        collator = Collator.getInstance();
        collator.setStrength(Collator.SECONDARY); // ignore case but do differentiate on accents
    }

    public UserSortNameComparator(boolean nullsLow) {
        this();
        this.nullsLow = nullsLow;
    }

    public UserSortNameComparator(boolean nullsLow, boolean useDisplayName) {
        this();
        this.nullsLow = nullsLow;
        this.useDisplayName = useDisplayName;
    }

    public int compare(User u1, User u2) {
        if (u1 == u2) return 0;
        if (u1 == null) return (nullsLow ? -1 : 1);
        if (u2 == null) return (nullsLow ? 1 : -1);

        Comparator c = new NullSafeComparator<>(collator, nullsLow);

        String prop1 = u1.getSortName();
        String prop2 = u2.getSortName();

        if (useDisplayName) {
            prop1 = u1.getDisplayName();
            prop2 = u2.getDisplayName();
        }

        // Replace spaces to handle sorting scenarios where surname has space
        prop1 = StringUtils.replace(prop1, " ", "+");
        prop2 = StringUtils.replace(prop2, " ", "+");

        // Secondary comparison on display name if full name is identical
        // E.g., John Smith (smithj1) and John Smith (smithj2)
        if (c.compare(prop1, prop2) == 0) {
            prop1 = u1.getDisplayId();
            prop2 = u2.getDisplayId();
        }

        return c.compare(prop1, prop2);
    }
}
