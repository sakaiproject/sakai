/**
 * Copyright (c) 2003-2013 The Apereo Foundation
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
package org.sakaiproject.courier.api;

/**
 * Interface that will be used in additions to Delivery to indicate that a delivery will eventually expire.
 * @author Chris Maurer <chmaurer@iu.edu>
 *
 */
public interface Expirable {

	/**
	 * Get the date that the object was created
	 * @return
	 */
	long getCreated();
	
	/**
	 * Get the Time-to-Live for the object (in seconds).
	 * @return
	 */
	int getTtl();
	
}
