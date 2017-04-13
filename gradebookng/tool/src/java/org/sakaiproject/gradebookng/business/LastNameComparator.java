package org.sakaiproject.gradebookng.business;

import java.text.Collator;
import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.sakaiproject.user.api.User;

/**
 * Comparator class for sorting a list of users by last name.
 * Secondary sort is on first name to maintain consistent order for those with the same last name
 */
public class LastNameComparator implements Comparator<User> {

	private final Collator collator = Collator.getInstance();

	@Override
	public int compare(final User u1, final User u2) {
		this.collator.setStrength(Collator.PRIMARY);
		return new CompareToBuilder()
				.append(u1.getLastName(), u2.getLastName(), this.collator)
				.append(u1.getFirstName(), u2.getFirstName(),this.collator)
				.toComparison();
	}

}
