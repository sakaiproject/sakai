package org.sakaiproject.assignment.impl.sort;

import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * Sorts assignment submissions by the submitter's sort name.
 */
@Slf4j
public class AssignmentSubmissionComparator implements Comparator<AssignmentSubmission> {

    private Collator collator;
    private SiteService siteService;
    private AssignmentService assignmentService;
    private UserDirectoryService userDirectoryService;

    public AssignmentSubmissionComparator(SiteService siteService) {
        this.siteService = siteService;
        try {
            collator = new RuleBasedCollator(((RuleBasedCollator) Collator.getInstance()).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
        } catch (ParseException e) {
            // error with init RuleBasedCollator with rules
            // use the default Collator
            collator = Collator.getInstance();
            log.warn("AssignmentComparator cannot init RuleBasedCollator. Will use the default Collator instead.", e);
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
        StringBuffer buffer = new StringBuffer();
        if (o2 instanceof AssignmentSubmission) {
            // get Assignment
            AssignmentSubmission _submission = (AssignmentSubmission) o2;
            if (_submission.getAssignment().getIsGroup()) {
                // get the Group
                try {
                    Site site = siteService.getSite(_submission.getAssignment().getContext());
                    // TODO handle optional
                    buffer.append(site.getGroup(assignmentService.getSubmissionSubmittee(_submission).get().getSubmitter()).getTitle());
                } catch (Throwable _dfd) {
                }
            } else {
                for (AssignmentSubmissionSubmitter submitter : ((AssignmentSubmission) o2).getSubmitters()) {
                    try {
                        User user = userDirectoryService.getUser(submitter.getSubmitter());
                        buffer.append(user.getSortName());
                        buffer.append(" ");
                    } catch (UserNotDefinedException e) {
                        log.warn("Could not get user with id: {}", submitter.getSubmitter());
                    }
                }
            }
        }
        return buffer.toString();
    }

}
