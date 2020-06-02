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
import java.time.Instant;
import java.util.Comparator;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.announcement.cover.AnnouncementService;
import org.sakaiproject.entity.api.ResourceProperties;

/**
 * Comparator for announcements.
 */
@Slf4j
public class AnnouncementWrapperComparator implements Comparator<AnnouncementWrapper> {

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
    public int compare(AnnouncementWrapper o1, AnnouncementWrapper o2) {
        int result = -1;

        switch (m_criteria) {
        case AnnouncementAction.SORT_MESSAGE_ORDER:
            result = o1.getAnnouncementHeader().getMessage_order().compareTo(o2.getAnnouncementHeader().getMessage_order());
            break;
        case AnnouncementAction.SORT_DATE:
            Instant o1ModDate;
            Instant o2ModDate;
            try {
                o1ModDate = o1.getProperties().getInstantProperty(AnnouncementService.MOD_DATE);
            } catch (Exception e) {
                // release date not set, use the date in header
                // NOTE: this is an edge use case for courses with pre-existing announcements that do not yet have MOD_DATE
                o1ModDate = Instant.ofEpochMilli(o1.getHeader().getDate().getTime());
            }
            try {
                o2ModDate = o2.getProperties().getInstantProperty(AnnouncementService.MOD_DATE);
            } catch (Exception e) {
                // release date not set, use the date in the header
                // NOTE: this is an edge use case for courses with pre-existing announcements that do not yet have MOD_DATE
                o2ModDate = Instant.ofEpochMilli(o2.getHeader().getDate().getTime());
            }
            result = compareInstantsNullSafe(o1ModDate, o2ModDate);
            break;
        case AnnouncementAction.SORT_RELEASEDATE:
        	Instant o1releaseDate;
        	Instant o2releaseDate;
            try {
                o1releaseDate = o1.getProperties().getInstantProperty(AnnouncementService.RELEASE_DATE);
            } catch (Exception e) {
                o1releaseDate = null;
            }
            try {
                o2releaseDate = o2.getProperties().getInstantProperty(AnnouncementService.RELEASE_DATE);
            } catch (Exception e) {
                o2releaseDate = null;
            }
            result = compareInstantsNullSafe(o1releaseDate, o2releaseDate);
            break;
        case AnnouncementAction.SORT_RETRACTDATE:
        	Instant o1retractDate;
        	Instant o2retractDate;
            try {
                o1retractDate = o1.getProperties().getInstantProperty(AnnouncementService.RETRACT_DATE);
            } catch (Exception e) {
                o1retractDate = null;
            }
            try {
                o2retractDate = o2.getProperties().getInstantProperty(AnnouncementService.RETRACT_DATE);
            } catch (Exception e) {
                o2retractDate = null;
            }
            result = compareInstantsNullSafe(o1retractDate, o2retractDate);
            break;
        case AnnouncementAction.SORT_SUBJECT:
            result = collator.compare(o1.getAnnouncementHeader().getSubject(), o2.getAnnouncementHeader().getSubject());
            break;
        case AnnouncementAction.SORT_CHANNEL:
            result = collator.compare(o1.getChannelDisplayName(), o2.getChannelDisplayName());
            break;
        case AnnouncementAction.SORT_FROM:
            // The anonymous user doesn't have a sort name (it's null).
            String sortName1 = o1.getAnnouncementHeader().getFrom().getSortName();
            String sortName2 = o2.getAnnouncementHeader().getFrom().getSortName();
            result = compareStringNullSafe(sortName1, sortName2);
            break;
        case AnnouncementAction.SORT_PUBLIC:
        	String factor1 = o1.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW);
        	String factor2 = o2.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW);
            result = compareStringNullSafe(factor1, factor2);
            break;
        case AnnouncementAction.SORT_FOR:
        	String range1 = o1.getRange();
            String range2 = o2.getRange();
            result = compareStringNullSafe(range1, range2);
            break;
        default:
            break;
        }
        // sort ascending or descending
        if (!m_asc) {
            result = -result;
        }
        return result;
    }

	private int compareStringNullSafe(String sortName1, String sortName2) {
        int result;
        if (sortName1 == null && sortName2 == null) {
            result = 0;
        } else if (sortName1 == null) {
            return -1;
        } else if (sortName2 == null) {
            return 1;
        }
        else {
            result = collator.compare(sortName1, sortName2);
        }
        return result;
    }

    private int compareInstantsNullSafe(Instant date1, Instant o2ModDate) {
        if (date1 == null && o2ModDate == null) {
            return 0;
        } else if (date1 == null) {
            return 1;
        } else if (o2ModDate == null) {
            return -1;
        }
        else {
        	return date1.compareTo(o2ModDate);
        }
    }

} // AnnouncementWrapperComparator
