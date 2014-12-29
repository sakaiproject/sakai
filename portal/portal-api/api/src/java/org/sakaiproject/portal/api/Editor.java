/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.portal.api;

/**
 * Interface to encapsulate information needed to register pluggable editors and use them
 * within the Sakai portal and Tool system.
 */
public interface Editor {

	/**
	 * Retrieve the unique identifier of this Editor.
	 * The ID should be human readable and serves as the system selector for a given editor.
	 * It is suggested that the ID be all lower case, alphabetical with optional dashes.
	 * The EditorRegistry is case-insensitive for all IDs.
	 * @return the String ID of this Editor.
	 */
	public String getId();
	
	/**
	 * Retrieve the formal name of this Editor.
	 * The name of an editor includes any stylistic capitalization or spaces as appropriate
	 * for a label in an interface. 
	 * @return The String name of the Editor as for UI or formal identification.
	 */
	public String getName();
	
	/**
	 * Retrieve the URL of the Editor's primary script.
	 * The Editor URL is what must be included for any number of editor instances to be used
	 * on a given page. If multiple files are required, this should be a loader. 
	 * @return The main script URL for this Editor.
	 */
	public String getEditorUrl();
	
	/**
	 * Retrieve the URL of the Sakai launch script for this Editor.
	 * The Launch URL points to a script that implements the Sakai binding for an editor.
	 * This implementation allows UI toolkits to use a consistent interface for loading various editors.
	 * @return The launch script URL for this Editor.
	 */
	public String getLaunchUrl();
	
	/**
	 * Retrieve any inline script that should be run before loading this Editor.
	 * In some cases, an editor may depend on environmental setup before loading the main script file.
	 * This offers an opportunity to supply such pre-loading script as inline markup. It should not include any
	 * <script> or other tags as it is a script snippet. This will typically return null or an empty string,
	 * either of which will be handled by the portal.
	 * @return Any inline script that should be embedded before the Editor URL is loaded.
	 */
	public String getPreloadScript();

}
