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
 * Sorts assignment submissions by the submission's anonymous ID.
 */
public class AnonymousSubmissionComparator implements Comparator<AssignmentSubmission> {

    private static Logger M_log = LoggerFactory.getLogger(AnonymousSubmissionComparator.class);

    private Collator collator;

    public AnonymousSubmissionComparator() {
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
        String name1 = a1.getAnonymousSubmissionId();
        String name2 = a2.getAnonymousSubmissionId();
        result = collator.compare(name1, name2);
        return result;
    }

}
