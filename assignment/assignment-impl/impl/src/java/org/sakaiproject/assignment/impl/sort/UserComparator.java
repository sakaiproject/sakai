package org.sakaiproject.assignment.impl.sort;

import org.sakaiproject.user.api.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Comparator;

/**
 * This sorts users.
 */
public class UserComparator implements Comparator<User> {

    private static Logger M_log = LoggerFactory.getLogger(UserComparator.class);

    private Collator collator;

    public UserComparator() {
        // TODO this should be in a service and should repect the current user's locale
        try {
            collator = new RuleBasedCollator(((RuleBasedCollator) Collator.getInstance()).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
        } catch (ParseException e) {
            // error with init RuleBasedCollator with rules
            // use the default Collator
            collator = Collator.getInstance();
            M_log.warn(this + " UserIdComparator cannot init RuleBasedCollator. Will use the default Collator instead. " + e);
        }
        // This is to ignore case of the values
        collator.setStrength(Collator.SECONDARY);
    }

    public int compare(User u1, User u2) {
        String name1 = u1.getSortName();
        String name2 = u2.getSortName();
        return collator.compare(name1, name2);
    }
}
