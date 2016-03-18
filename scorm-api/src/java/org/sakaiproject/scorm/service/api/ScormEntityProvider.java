/*
 * #%L
 * SCORM API
 * %%
 * Copyright (C) 2007 - 2016 Sakai Project
 * %%
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
 * #L%
 */
package org.sakaiproject.scorm.service.api;

public interface ScormEntityProvider {
	public static String ENTITY_PREFIX = "scorm";

	public static String LAUNCH_ID = "launch";

	public static String NAVIGATE_ID = "navigate";

	public static String CONTENT_ID = "content";

	public static String RESOURCE_ID = "resource";

	public static String[] VALID_IDS = { LAUNCH_ID, NAVIGATE_ID, CONTENT_ID, RESOURCE_ID };
}
