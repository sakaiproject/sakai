package org.sakaiproject.gradebookng.business;

import java.text.Collator;
import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;

import org.sakaiproject.user.api.User;

/**
 * Comparator class for sorting a list of users by last name
 */
public class LastNameComparator implements Comparator<User> {

	private Collator collator = Collator.getInstance();
	@Override
	public int compare(final User u1, final User u2) {
		collator.setStrength(Collator.PRIMARY);
		return new CompareToBuilder().append(u1.getLastName(), u2.getLastName(), collator).toComparison();
	}

}
