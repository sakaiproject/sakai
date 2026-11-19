/**
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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
 
package org.sakaiproject.util;

import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * EditorConfiguration is a utility class that provides methods to access
 * information that is relevant to the configuration of a rich-text editor.
 */
public class EditorConfiguration 
{
	/**
	 * Access the identifier for the editor currently in use.  This value is
	 * supplied by the ServerConfigurationService and uniquely identifies a 
	 * particular editor supported by Sakai. 
	 * @return The unique identifier for the editor as specified in 
	 * "sakai.properties". 
	 */
	public static String getWysiwigEditor()
	{
		return ServerConfigurationService.getString("wysiwyg.editor");
	}

	/**
	 * Get the file browser that should be used when picking a file in CKEditor.
	 * @return The file browser, i.e. elfinder
	 */
	public static String getCKEditorFileBrowser()
	{
		return ServerConfigurationService.getString("wysiwyg.editor.ckeditor.browser", "elfinder");
	}
}
