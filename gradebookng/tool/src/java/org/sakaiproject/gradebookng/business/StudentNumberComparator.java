/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
