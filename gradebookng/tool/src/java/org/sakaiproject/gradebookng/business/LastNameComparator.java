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

import java.text.Collator;
import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.sakaiproject.user.api.User;

/**
 * Comparator class for sorting a list of users by last name. Secondary sort is on first name to maintain consistent order for those with
 * the same last name
 */
public class LastNameComparator implements Comparator<User> {

	private final Collator collator = Collator.getInstance();

	@Override
	public int compare(final User u1, final User u2) {
		this.collator.setStrength(Collator.PRIMARY);
		return new CompareToBuilder()
				.append(u1.getLastName(), u2.getLastName(), this.collator)
				.append(u1.getFirstName(), u2.getFirstName(), this.collator)
				.toComparison();
	}

}
