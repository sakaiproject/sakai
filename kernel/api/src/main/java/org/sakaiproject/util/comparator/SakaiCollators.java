/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
import java.util.Locale;
import java.util.Objects;

public final class SakaiCollators {

    private SakaiCollators() {
    }

    public static Collator getCollatorWithUnderscoreAfterSpace(Locale locale, int strength) {
        Collator collator = Collator.getInstance(Objects.requireNonNull(locale));

        if (collator instanceof RuleBasedCollator) {
            try {
                collator = new RuleBasedCollator(((RuleBasedCollator) collator).getRules()
                        .replaceAll("<'\u005f'", "<' '<'\u005f'"));
            } catch (ParseException e) {
                collator = Collator.getInstance(locale);
            }
        }

        collator.setStrength(strength);
        return collator;
    }

    public static Collator getSortNameCollator(Locale locale) {
        return getCollatorWithUnderscoreAfterSpace(locale, Collator.SECONDARY);
    }
}
