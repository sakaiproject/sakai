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
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

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
        this(Locale.getDefault());
    }

    public UserSortNameComparator(Locale locale) {
        collator = createCollator(Objects.requireNonNull(locale));
    }

    public UserSortNameComparator(boolean nullsLow) {
        this();
        this.nullsLow = nullsLow;
    }

    public UserSortNameComparator(boolean nullsLow, Locale locale) {
        this(locale);
        this.nullsLow = nullsLow;
    }

    public UserSortNameComparator(boolean nullsLow, boolean useDisplayName) {
        this();
        this.nullsLow = nullsLow;
        this.useDisplayName = useDisplayName;
    }

    public UserSortNameComparator(boolean nullsLow, boolean useDisplayName, Locale locale) {
        this(locale);
        this.nullsLow = nullsLow;
        this.useDisplayName = useDisplayName;
    }

    public int compare(User u1, User u2) {
        if (u1 == u2) return 0;
        if (u1 == null) return (nullsLow ? -1 : 1);
        if (u2 == null) return (nullsLow ? 1 : -1);

        String prop1 = u1.getSortName();
        String prop2 = u2.getSortName();

        if (useDisplayName) {
            prop1 = u1.getDisplayName();
            prop2 = u2.getDisplayName();
        }

        return compareSortNames(prop1, u1.getDisplayId(), prop2, u2.getDisplayId());
    }

    private static Collator createCollator(Locale locale) {
        Collator collator = Collator.getInstance(locale);
        collator.setStrength(Collator.SECONDARY); // ignore case but do differentiate on accents
        return collator;
    }

    public int compareSortNames(String sortName1, String displayId1, String sortName2, String displayId2) {
        return compareSortNames(sortName1, displayId1, sortName2, displayId2, nullsLow, collator);
    }

    /**
     * Allows Sakai user-like models, such as Sections' coursemanagement.User, to share the same sort-name behavior
     * without implementing org.sakaiproject.user.api.User.
     */
    public static int compareSortNames(String sortName1, String displayId1, String sortName2, String displayId2,
            boolean nullsLow, Locale locale) {
        return compareSortNames(sortName1, displayId1, sortName2, displayId2, nullsLow,
                createCollator(Objects.requireNonNull(locale)));
    }

    private static int compareSortNames(String sortName1, String displayId1, String sortName2, String displayId2,
            boolean nullsLow, Collator collator) {
        Comparator<String> comparator = new NullSafeComparator<>(
                (String value1, String value2) -> collator.compare(value1, value2), nullsLow);

        // Replace spaces to handle sorting scenarios where surname has space.
        String prop1 = StringUtils.replace(sortName1, " ", "+");
        String prop2 = StringUtils.replace(sortName2, " ", "+");

        // Secondary comparison on display name if full name is identical
        // E.g., John Smith (smithj1) and John Smith (smithj2)
        if (comparator.compare(prop1, prop2) == 0) {
            prop1 = displayId1;
            prop2 = displayId2;
        }

        return comparator.compare(prop1, prop2);
    }
}
