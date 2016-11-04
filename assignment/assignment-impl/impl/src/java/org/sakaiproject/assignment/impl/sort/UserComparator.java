package org.sakaiproject.assignment.impl.sort;

import org.sakaiproject.user.api.User;

import java.util.Comparator;

/**
 * This sorts users.
 */
public class UserComparator implements Comparator<User> {
    public int compare(User u1, User u2) {
        return u1.compareTo(u2);
    }
}
