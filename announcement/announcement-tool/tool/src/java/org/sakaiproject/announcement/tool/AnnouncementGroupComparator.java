package org.sakaiproject.announcement.tool;

import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.cover.AnnouncementService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.time.api.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Comparator;

/**
 * Comparator for announcements.
 */
class AnnouncementComparator implements Comparator {

    private Logger log = LoggerFactory.getLogger(AnnouncementComparator.class);


    private static RuleBasedCollator collator_ini = (RuleBasedCollator)Collator.getInstance();
    private Collator collator = Collator.getInstance();
    
    // the criteria
    String m_criteria = null;

    {
        try {
            collator = new RuleBasedCollator(collator_ini.getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
        } catch (ParseException e) {
            log.error(this + " Cannot init RuleBasedCollator. Will use the default Collator instead.", e);
        }
    }

    // the criteria - asc
    boolean m_asc = true;

    /**
     * constructor
     *  @param criteria The sort criteria string
     * @param asc      The sort order string. "true" if ascending; "false" otherwise.
     */
    public AnnouncementComparator(String criteria, boolean asc) {
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
    public int compare(Object o1, Object o2) {
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
            if ((((AnnouncementMessage) o1).getAnnouncementHeader().getMessage_order()) <
                    (((AnnouncementMessage) o2).getAnnouncementHeader().getMessage_order())) {
                result = -1;
            } else {
                result = 1;
            }
        } else if (m_criteria.equals(AnnouncementAction.SORT_RELEASEDATE)) {
            Time o1releaseDate = null;
            Time o2releaseDate = null;

            try {
                o1releaseDate = ((AnnouncementMessage) o1).getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE);
            } catch (Exception e) {
                // release date not set, go on
            }

            try {
                o2releaseDate = ((AnnouncementMessage) o2).getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE);
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
                o1retractDate = ((AnnouncementMessage) o1).getProperties().getTimeProperty(AnnouncementService.RETRACT_DATE);
            } catch (Exception e) {
                // release date not set, go on
            }

            try {
                o2retractDate = ((AnnouncementMessage) o2).getProperties().getTimeProperty(AnnouncementService.RETRACT_DATE);
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
            // sorted by the discussion message subject
            result = collator.compare(((AnnouncementMessage) o1).getAnnouncementHeader().getFrom().getSortName(),
                    ((AnnouncementMessage) o2).getAnnouncementHeader().getFrom().getSortName());
        } else if (m_criteria.equals(AnnouncementAction.SORT_CHANNEL)) {
            // sorted by the channel name.
            result = collator.compare(((AnnouncementAction.AnnouncementWrapper) o1).getChannelDisplayName(),
                    ((AnnouncementAction.AnnouncementWrapper) o2).getChannelDisplayName());
        } else if (m_criteria.equals(AnnouncementAction.SORT_PUBLIC)) {
            // sorted by the public view attribute
            String factor1 = ((AnnouncementMessage) o1).getProperties().getProperty(ResourceProperties.PROP_PUBVIEW);
            if (factor1 == null) factor1 = "false";
            String factor2 = ((AnnouncementMessage) o2).getProperties().getProperty(ResourceProperties.PROP_PUBVIEW);
            if (factor2 == null) factor2 = "false";
            result = collator.compare(factor1, factor2);
        } else if (m_criteria.equals(AnnouncementAction.SORT_FOR)) {
            // sorted by the public view attribute
            String factor1 = ((AnnouncementAction.AnnouncementWrapper) o1).getRange();
            String factor2 = ((AnnouncementAction.AnnouncementWrapper) o2).getRange();
            result = collator.compare(factor1, factor2);
        } else if (m_criteria.equals(AnnouncementAction.SORT_GROUPTITLE)) {
            // sorted by the group title
            String factor1 = ((Group) o1).getTitle();
            String factor2 = ((Group) o2).getTitle();
            result = collator.compare(factor1, factor2);
        } else if (m_criteria.equals(AnnouncementAction.SORT_GROUPDESCRIPTION)) {
            // sorted by the group title
            String factor1 = ((Group) o1).getDescription();
            String factor2 = ((Group) o2).getDescription();
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

} // AnnouncementComparator
