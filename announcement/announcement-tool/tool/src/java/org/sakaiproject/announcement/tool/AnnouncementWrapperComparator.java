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

import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.cover.AnnouncementService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.api.Time;

/**
 * Comparator for announcements.
 */
@Slf4j
class AnnouncementWrapperComparator implements Comparator<AnnouncementAction.AnnouncementWrapper> {

    private static RuleBasedCollator collator_ini = (RuleBasedCollator)Collator.getInstance();
    private Collator collator = Collator.getInstance();
    
    // the criteria
    String m_criteria = null;

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
    public AnnouncementWrapperComparator(String criteria, boolean asc) {
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
    public int compare(AnnouncementAction.AnnouncementWrapper o1, AnnouncementAction.AnnouncementWrapper o2) {
        int result = -1;

        if (m_criteria.equals(AnnouncementAction.SORT_SUBJECT)) {
            // sorted by the discussion message subject
            result = collator.compare(((AnnouncementMessage) o1).getAnnouncementHeader().getSubject(),
                    ((AnnouncementMessage) o2).getAnnouncementHeader().getSubject());

        } else if (m_criteria.equals(AnnouncementAction.SORT_DATE)) {

            Time o1ModDate = null;
            Time o2ModDate = null;

            try {
                o1ModDate = ((AnnouncementMessage) o1).getProperties().getTimeProperty(AnnouncementService.MOD_DATE);
            } catch (Exception e) {
                // release date not set, use the date in header
                // NOTE: this is an edge use case for courses with pre-existing announcements that do not yet have MOD_DATE
                o1ModDate = ((AnnouncementMessage) o1).getHeader().getDate();
            }

            try {
                o2ModDate = ((AnnouncementMessage) o2).getProperties().getTimeProperty(AnnouncementService.MOD_DATE);
            } catch (Exception e) {
                // release date not set, use the date in the header
                // NOTE: this is an edge use case for courses with pre-existing announcements that do not yet have MOD_DATE
                o2ModDate = ((AnnouncementMessage) o2).getHeader().getDate();
            }

            if (o1ModDate != null && o2ModDate != null) {
                // sorted by the discussion message date
                if (o1ModDate.before(o2ModDate)) {
                    result = -1;
                } else {
                    result = 1;
                }
            } else if (o1ModDate == null) {
                return 1;
            } else {
                return -1;
            }
        } else if (m_criteria.equals(AnnouncementAction.SORT_MESSAGE_ORDER)) {
            // sorted by the message order
            if ((o1.getAnnouncementHeader().getMessage_order()) <
                    (o2.getAnnouncementHeader().getMessage_order())) {
                result = -1;
            } else {
                result = 1;
            }
        } else if (m_criteria.equals(AnnouncementAction.SORT_RELEASEDATE)) {
            Time o1releaseDate = null;
            Time o2releaseDate = null;

            try {
                o1releaseDate = o1.getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE);
            } catch (Exception e) {
                // release date not set, go on
            }

            try {
                o2releaseDate = o2.getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE);
            } catch (Exception e) {
                // release date not set, go on
            }

            if (o1releaseDate != null && o2releaseDate != null) {
                // sorted by the discussion message date
                if (o1releaseDate.before(o2releaseDate)) {
                    result = -1;
                } else {
                    result = 1;
                }
            } else if (o1releaseDate == null) {
                return 1;
            } else {
                return -1;
            }
        } else if (m_criteria.equals(AnnouncementAction.SORT_RETRACTDATE)) {
            Time o1retractDate = null;
            Time o2retractDate = null;

            try {
                o1retractDate = o1.getProperties().getTimeProperty(AnnouncementService.RETRACT_DATE);
            } catch (Exception e) {
                // release date not set, go on
            }

            try {
                o2retractDate = o2.getProperties().getTimeProperty(AnnouncementService.RETRACT_DATE);
            } catch (Exception e) {
                // release date not set, go on
            }

            if (o1retractDate != null && o2retractDate != null) {
                // sorted by the discussion message date
                if (o1retractDate.before(o2retractDate)) {
                    result = -1;
                } else {
                    result = 1;
                }
            } else if (o1retractDate == null) {
                return 1;
            } else {
                return -1;
            }
        } else if (m_criteria.equals(AnnouncementAction.SORT_FROM)) {
            // sorted by the discussion message author
            // The anonymous user doesn't have a sort name (it's null).
            String sortName1 = o1.getAnnouncementHeader().getFrom().getSortName();
            if (sortName1 == null) {
                sortName1 = "";
            }
            String sortName2 = o2.getAnnouncementHeader().getFrom().getSortName();
            if (sortName2 == null) {
                sortName2 = "";
            }
            result = collator.compare(sortName1,
                    sortName2);
        } else if (m_criteria.equals(AnnouncementAction.SORT_CHANNEL)) {
            // sorted by the channel name.
            result = collator.compare(o1.getChannelDisplayName(),
                    o2.getChannelDisplayName());
        } else if (m_criteria.equals(AnnouncementAction.SORT_PUBLIC)) {
            // sorted by the public view attribute
            String factor1 = o1.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW);
            if (factor1 == null) factor1 = "false";
            String factor2 = o2.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW);
            if (factor2 == null) factor2 = "false";
            result = collator.compare(factor1, factor2);
        } else if (m_criteria.equals(AnnouncementAction.SORT_FOR)) {
            // sorted by the public view attribute
            String factor1 = o1.getRange();
            String factor2 = o2.getRange();
            result = collator.compare(factor1, factor2);
        }

        // sort ascending or descending
        if (!m_asc) {
            result = -result;
        }
        return result;

    } // compare

} // AnnouncementWrapperComparator
