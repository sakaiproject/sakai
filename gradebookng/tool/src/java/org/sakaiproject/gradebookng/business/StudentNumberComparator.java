package org.sakaiproject.gradebookng.business;

import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.User;

/**
 * Comparator class for sorting a list of users by student number
 * @author plukasew
 */
@RequiredArgsConstructor
public class StudentNumberComparator implements Comparator<User>
{
	private final CandidateDetailProvider provider;
	private final Site site;
	
	@Override
	public int compare(final User u1, final User u2)
	{
		String stunum1 = provider.getInstitutionalNumericId(u1, site).orElse("");
		String stunum2 = provider.getInstitutionalNumericId(u2, site).orElse("");
		return stunum1.compareTo(stunum2);
	}
}
