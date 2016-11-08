package org.sakaiproject.assignment.impl.sort;

import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.user.api.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Comparator;

/**
 * Sorts assignment submissions by the submitter's sort name.
 */
public class AssignmentSubmissionComparator implements Comparator<AssignmentSubmission> {

    private static Logger M_log = LoggerFactory.getLogger(AssignmentSubmissionComparator.class);

    private Collator collator;

    public AssignmentSubmissionComparator() {
        try {
            collator = new RuleBasedCollator(((RuleBasedCollator) Collator.getInstance()).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
        } catch (ParseException e) {
            // error with init RuleBasedCollator with rules
            // use the default Collator
            collator = Collator.getInstance();
            M_log.warn(this + " AssignmentComparator cannot init RuleBasedCollator. Will use the default Collator instead. " + e);
        }
    }

    @Override
    public int compare(AssignmentSubmission a1, AssignmentSubmission a2) {
        int result;
        String name1 = getSubmitterSortname(a1);
        String name2 = getSubmitterSortname(a2);
        result = compareString(name1, name2);
        return result;
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

    /**
     * get the submitter sortname String for the AssignmentSubmission object
     */
    private String getSubmitterSortname(Object o2) {
        String rv = "";
        if (o2 instanceof AssignmentSubmission) {
            // get Assignment
            AssignmentSubmission _submission = (AssignmentSubmission) o2;
            if (_submission.getAssignment().isGroup()) {
                // get the Group
                try {
                    Site _site = SiteService.getSite(_submission.getAssignment().getContext());
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

}
