/***********************************************************************************
 * Copyright (c) 2020 Apereo Foundation

 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.util.comparator;

import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

public class AlphaNumericComparator implements Comparator<String> {
	private static Comparator stringComparator = Comparator
				.comparing(s -> String.valueOf(s).replaceAll("\\d", ""), String.CASE_INSENSITIVE_ORDER)
				.thenComparingLong(s -> StringUtils.isNumeric(s.toString().replaceAll("\\D", "")) ?
						Long.parseLong(s.toString().replaceAll("\\D", "")) : 0);

	@Override
	public int compare(String o1, String o2) {
		try {
			return stringComparator.compare(o1, o2);
		} catch (NumberFormatException nfe) {
			return Comparator.comparing(String::valueOf).compare(o1, o2);
		}
	}
}
