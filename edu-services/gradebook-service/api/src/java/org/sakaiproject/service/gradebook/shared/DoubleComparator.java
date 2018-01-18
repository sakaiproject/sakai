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
package org.sakaiproject.service.gradebook.shared;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Comparator to ensure correct ordering of letter grade / percent mappings
 */
public class DoubleComparator implements Comparator<String>, Serializable {
	private static final long serialVersionUID = 1L;

	private final Map<String, Double> base = new HashMap<>();

	public DoubleComparator(final Map<String, Double> base) {
		this.base.putAll(base);
	}

	@Override
	public int compare(final String a, final String b) {

		if (this.base.get(a) >= this.base.get(b)) {
			return -1;
		} else {
			return 1;
		}
	}
}
