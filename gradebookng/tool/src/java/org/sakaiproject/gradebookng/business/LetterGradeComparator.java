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

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

/**
 * Comparator to ensure correct ordering of letter grades, catering for + and - in the grade Copied from GradebookService and made
 * Serializable as we use it in a TreeMap. Also has the fix from SAK-30094. If this changes, be sure to update the other.
 */
public class LetterGradeComparator implements Comparator<String>, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public int compare(final String o1, final String o2) {
		if (o1.toLowerCase().charAt(0) == o2.toLowerCase().charAt(0)) {
			// only take the first 2 chars, to cater for GradePointsMapping as well
			final String s1 = StringUtils.trim(StringUtils.left(o1, 2));
			final String s2 = StringUtils.trim(StringUtils.left(o2, 2));

			if (s1.length() == 2 && s2.length() == 2) {
				if (s1.charAt(1) == '+') {
					return -1; // SAK-30094
				} else {
					return 1;
				}
			}
			if (s1.length() == 1 && s2.length() == 2) {
				if (o2.charAt(1) == '+') {
					return 1; // SAK-30094
				} else {
					return -1;
				}
			}
			if (s1.length() == 2 && s2.length() == 1) {
				if (s1.charAt(1) == '+') {
					return -1; // SAK-30094
				} else {
					return 1;
				}
			}
			return 0;
		} else {
			return o1.toLowerCase().compareTo(o2.toLowerCase());
		}
	}
}
