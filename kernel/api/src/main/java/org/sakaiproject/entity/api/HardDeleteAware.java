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
package org.sakaiproject.entity.api;

/**
 * Services which implement HardDeleteAware declare that they are able to purge themselves. Note that services must also be registered EntityProducers.
 *
 * @since 10.0
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public interface HardDeleteAware {

	/**
	 * Hard delete the content for the implementing service in the given site
	 * 
	 * @param siteId the siteId to use when finding content to delete
	 */
	public void hardDelete(String siteId);
	
}
