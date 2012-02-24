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

import java.util.List;

public interface EditorRegistry {
	
	/**
	 * Registers an instantiated Editor for portal use.
	 * Note that the ID is case-insensitive.
	 * @param editor the Editor
	 */
	public void register(Editor editor);
	
	/**
	 * Registers an editor for portal use by supplying requisite values instead of an instance.
	 * An Editor instance will be created and registered. The ID is case-insensitive. See {@link Editor} for details.
	 * @param id Editor ID
	 * @param name Editor Name
	 * @param editorUrl Editor URL
	 * @param launchUrl Launch URL
	 * @param preloadScript Preload Script
	 */
	public void register(String id, String name, String editorUrl, String launchUrl, String preloadScript);
	
	/**
	 * Unregister an Editor, making it unavailable.
	 * Note that the Editor is identified by its getId() method, not by object identity.
	 * @param editor The Editor to unregister.
	 * @return Whether the editor was unregistered successfully.
	 */
	public boolean unregister(Editor editor);
	
	/**
	 * Unregister an Editor by its ID, making it unavailable.
	 * @param id The ID of the Editor to unregister.
	 * @return Whether the editor was unregistered successfully.
	 */
	public boolean unregister(String id);
		
	/**
	 * Retrieve an Editor by its ID.
	 * @param id The ID of the desired Editor.
	 * @return The identified Editor or null if one is not registered with the supplied ID.
	 */
	public Editor getEditor(String id);
	
	/**
	 * Retrieve a list of all registered Editors.
	 * @return All registered editors in a copied list; any modifications will not be reflected in the registered Editors.
	 */
	public List<Editor> getEditors();

}
