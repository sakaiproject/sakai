/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/ecl2
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.gradebookng.business;

import java.text.Collator;
import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.sakaiproject.gradebookng.business.model.GbUser;

/**
 * Comparator class for sorting a list of GbUser by first name.
 * Secondary sort is on last name to maintain consistent order for those with the same first name.
 */
public class FirstNameComparatorGbUser implements Comparator<GbUser> {

    private final Collator collator = Collator.getInstance();

    @Override
    public int compare(final GbUser u1, final GbUser u2) {
        this.collator.setStrength(Collator.PRIMARY);
        return new CompareToBuilder()
                .append(u1.getFirstName(), u2.getFirstName(), this.collator)
                .append(u1.getLastName(), u2.getLastName(), this.collator)
                .toComparison();
    }
}
