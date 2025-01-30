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

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.comparator.NullSafeComparator;

/**
 * This sorts two strings, but makes some assumption about what might be in those
 * strings. So, there could be multiple names in each name, or there could be accents
 * in the names
 */
@Slf4j
public class UserNameComparator implements Comparator<String> {

    private Collator collator;
    private boolean nullsLow = false;

    public UserNameComparator() {

        collator = Collator.getInstance();
        collator.setStrength(Collator.SECONDARY); // ignore case but do differentiate on accents
    }

    public UserNameComparator(boolean nullsLow) {

        this();
        this.nullsLow = nullsLow;
    }

    public int compare(String name1, String name2) {

        if (name1 == name2) return 0;
        if (name1 == null) return (nullsLow ? -1 : 1);
        if (name2 == null) return (nullsLow ? 1 : -1);

        Comparator c = new NullSafeComparator<>(collator, nullsLow);

        // Replace spaces to handle sorting scenarios where a name has space
        name1 = StringUtils.replace(name1, " ", "+");
        name2 = StringUtils.replace(name2, " ", "+");

        return c.compare(name1, name2);
    }
}
