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
package org.sakaiproject.announcement.tool;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.site.api.Group;

/**
 * Comparator for announcements.
 */
@Slf4j
class AnnouncementGroupComparator implements Comparator<Group> {

    public enum Criteria {TITLE, DESCRIPTION};

    private static RuleBasedCollator collator_ini = (RuleBasedCollator)Collator.getInstance();
    private Collator collator = Collator.getInstance();

    // the criteria
    Criteria m_criteria = null;

    {
        try {
            collator = new RuleBasedCollator(collator_ini.getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
        } catch (ParseException e) {
            log.error("{} Cannot init RuleBasedCollator. Will use the default Collator instead.", this, e);
        }
    }

    // the criteria - asc
    boolean m_asc = true;

    /**
     * constructor
     *  @param criteria The sort criteria string
     * @param asc      The sort order string. "true" if ascending; "false" otherwise.
     */
    public AnnouncementGroupComparator(Criteria criteria, boolean asc) {
        m_criteria = criteria;
        m_asc = asc;

    } // constructor

    /**
     * implementing the compare function
     *
     * @param o1 The first object
     * @param o2 The second object
     * @return The compare result. 1 is o1 < o2; -1 otherwise
     */
    public int compare(Group o1, Group o2) {
        int result = -1;
        if (m_criteria.equals(Criteria.TITLE)) {
            // sorted by the group title
            String factor1 = (o1).getTitle();
            String factor2 = (o2).getTitle();
            result = collator.compare(factor1, factor2);
        } else if (m_criteria.equals(Criteria.DESCRIPTION)) {
            // sorted by the group title
            String factor1 = (o1).getDescription();
            String factor2 = (o2).getDescription();
            if (factor1 == null) {
                factor1 = "";
            }
            if (factor2 == null) {
                factor2 = "";
            }
            result = collator.compare(factor1, factor2);
        }

        // sort ascending or descending
        if (!m_asc) {
            result = -result;
        }
        return result;

    } // compare

} // AnnouncementWrapperComparator
