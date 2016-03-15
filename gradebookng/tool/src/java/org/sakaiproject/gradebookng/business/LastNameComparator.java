package org.sakaiproject.gradebookng.business;

import java.util.Comparator;

import org.sakaiproject.user.api.User;

/**
 * Comparator class for sorting a list of users by last name
 */
public class LastNameComparator implements Comparator<User> {

	@Override
	public int compare(final User u1, final User u2) {
		return u1.getLastName().compareTo(u2.getLastName());
	}

}
