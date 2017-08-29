/**
 * Copyright (c) 2003-2008 The Apereo Foundation
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
package org.sakaiproject.site.util;

import java.util.Comparator;

import org.sakaiproject.tool.api.Tool;

public class ToolComparator implements Comparator {
	/**
	 * implementing the Comparator compare function
	 * 
	 * @param o1
	 *            The first object
	 * @param o2
	 *            The second object
	 * @return The compare result. 1 is o1 < o2; 0 is o1.equals(o2); -1
	 *         otherwise
	 */
	public int compare(Object o1, Object o2) {
		try {
			return ((Tool) o1).getTitle().compareToIgnoreCase(((Tool) o2).getTitle());
		} catch (Exception e) {
		}
		return -1;

	} // compare

} // ToolComparator
