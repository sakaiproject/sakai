/**
 * Copyright (c) 2003-2014 The Apereo Foundation
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
package org.sakaiproject.site.api;

/**
 * Allows handlers to know just before a site is removed.
 * This allows tools to do cleanup or something else before a site is removed.
 * It does not allow the deletion to be cancelled or aborted.
 * The advantage of using this over listening for events is that the event is only generated once the site has been
 * removed so you can no longer find it.
 *
 * @author Matthew Buckett
 */
public interface SiteRemovalAdvisor {

	/**
	 * Called just before a site is removed.
	 * @param site The site being removed, this will never be <code>null</code>.
	 */
	public void removed(Site site);

}
