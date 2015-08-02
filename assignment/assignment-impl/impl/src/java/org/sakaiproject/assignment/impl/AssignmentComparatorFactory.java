package org.sakaiproject.assignment.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Comparator;

/**
 * the AssignmentComparator class
 */
class AssignmentComparatorFactory
{

    private final Log log = LogFactory.getLog(AssignmentComparatorFactory.class);

    private SiteService siteService;
    private UserDirectoryService userDirectoryService;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public Comparator getInstance(String criteria, String asc) {
        return new AssignmentComparator(criteria, asc, false);
    }

    public Comparator getInstance(String criteria, String asc, boolean group) {
        return new AssignmentComparator(criteria, asc, group);
    }

    private class AssignmentComparator implements Comparator {
        Collator collator = null;

        /**
         * the criteria
         */
        String m_criteria = null;

        /**
         * the criteria
         */
        String m_asc = null;

        /**
         * is group submission
         */
        boolean m_group_submission = false;

        public AssignmentComparator(String criteria, String asc, boolean group) {
            m_criteria = criteria;
            m_asc = asc;
            m_group_submission = group;
            try {
                collator = new RuleBasedCollator(((RuleBasedCollator) Collator.getInstance()).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
            } catch (ParseException e) {
                // error with init RuleBasedCollator with rules
                // use the default Collator
                collator = Collator.getInstance();
                log.warn(this + " AssignmentComparator cannot init RuleBasedCollator. Will use the default Collator instead. " + e);
            }
        }

        /**
         * implementing the compare function
         *
         * @param o1 The first object
         * @param o2 The second object
         * @return The compare result. 1 is o1 < o2; -1 otherwise
         */
        public int compare(Object o1, Object o2) {
            int result = -1;

            /************** for sorting submissions ********************/
            if ("submitterName".equals(m_criteria)) {
                String name1 = getSubmitterSortname(o1);
                String name2 = getSubmitterSortname(o2);
                result = compareString(name1, name2);
            }
            /** *********** for sorting assignments ****************** */
            else if ("duedate".equals(m_criteria)) {
                // sorted by the assignment due date
                Time t1 = ((Assignment) o1).getDueTime();
                Time t2 = ((Assignment) o2).getDueTime();

                if (t1 == null) {
                    result = -1;
                } else if (t2 == null) {
                    result = 1;
                } else if (t1.before(t2)) {
                    result = -1;
                } else {
                    result = 1;
                }
            } else if ("sortname".equals(m_criteria)) {
                // sorted by the user's display name
                String s1 = null;
                String userId1 = (String) o1;
                if (userId1 != null) {
                    try {
                        User u1 = userDirectoryService.getUser(userId1);
                        s1 = u1 != null ? u1.getSortName() : null;
                    } catch (Exception e) {
                        log.warn(" AssignmentComparator.compare " + e.getMessage() + " id=" + userId1);
                    }
                }

                String s2 = null;
                String userId2 = (String) o2;
                if (userId2 != null) {
                    try {
                        User u2 = userDirectoryService.getUser(userId2);
                        s2 = u2 != null ? u2.getSortName() : null;
                    } catch (Exception e) {
                        log.warn(" AssignmentComparator.compare " + e.getMessage() + " id=" + userId2);
                    }
                }

                result = compareString(s1, s2);
            }

            // sort ascending or descending
            if (m_asc.equals(Boolean.FALSE.toString())) {
                result = -result;
            }
            return result;
        }

        /**
         * get the submitter sortname String for the AssignmentSubmission object
         *
         * @param o2
         * @return
         */
        private String getSubmitterSortname(Object o2) {
            String rv = "";
            if (o2 instanceof AssignmentSubmission) {
                // get Assignment
                AssignmentSubmission _submission = (AssignmentSubmission) o2;
                if (_submission.getAssignment().isGroup()) {
                    // get the Group
                    try {
                        Site _site = siteService.getSite(_submission.getAssignment().getContext());
                        rv = _site.getGroup(_submission.getSubmitterId()).getTitle();
                    } catch (Throwable _dfd) {
                    }
                } else {
                    User[] users2 = ((AssignmentSubmission) o2).getSubmitters();
                    if (users2 != null) {
                        StringBuffer users2Buffer = new StringBuffer();
                        for (int i = 0; i < users2.length; i++) {
                            users2Buffer.append(users2[i].getSortName() + " ");
                        }
                        rv = users2Buffer.toString();
                    }
                }
            }
            return rv;
        }

        private int compareString(String s1, String s2) {
            int result;
            if (s1 == null && s2 == null) {
                result = 0;
            } else if (s2 == null) {
                result = 1;
            } else if (s1 == null) {
                result = -1;
            } else {
                result = collator.compare(s1.toLowerCase(), s2.toLowerCase());
            }
            return result;
        }
    }
}
