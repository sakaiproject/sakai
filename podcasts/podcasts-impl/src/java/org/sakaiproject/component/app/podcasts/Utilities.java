/**
 * Copyright (c) 2004-2017 The Apereo Foundation
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
package org.sakaiproject.component.app.podcasts;

public class Utilities {
	
	private Utilities() {
	};
	
	/**
	 * Checks that the value isn't null.
	 * @param arg argument to check.
	 * @param name name of variable used in exception message.
	 * @throws IllegalStateException if the supplied argument is null.
	 */
	public static void checkSet(Object arg, String name) {
		if (arg == null) {
			throw new IllegalStateException("The variable "+ name+ " hasn't been set.");
		}
	}
}
