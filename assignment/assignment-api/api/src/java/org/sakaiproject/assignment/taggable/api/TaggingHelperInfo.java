/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment.taggable.api;

import java.util.Map;

/**
 * An object that can provide the necessary data to interact with a specific
 * helper tool.
 * 
 * @author The Sakai Foundation.
 */
public interface TaggingHelperInfo {

	/**
	 * @return The identifier of the helper that will be called.
	 */
	public String getHelperId();

	/**
	 * @return The provider of the helper that this tagging helper information
	 *         relates to.
	 */
	public TaggingProvider getProvider();

	/**
	 * @return A string for the 'title' attribute of an anchor to the helper.
	 */
	public String getTitle();

	/**
	 * @return A string for the text value of an anchor to the helper.
	 */
	public String getText();

	/**
	 * @return A map of parameter names and values to pass to the helper.
	 */
	public Map<String, ? extends Object> getParameterMap();
}
