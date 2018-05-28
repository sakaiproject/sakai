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
package org.sakaiproject.portal.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sakaiproject.portal.api.BaseEditor;
import org.sakaiproject.portal.api.Editor;
import org.sakaiproject.portal.api.EditorRegistry;

public class EditorRegistryImpl implements EditorRegistry {
	
	private HashMap<String, Editor> editors = new HashMap<String, Editor>();
	
	public void init() {
		//TODO: pull this out to somewhere appropriate
		register("textarea", "textarea", "/library/editor/textarea/textarea.js", "/library/editor/textarea.launch.js", "");
		register("fckeditor", "FCKeditor", "/library/editor/FCKeditor/fckeditor.js", "/library/editor/fckeditor.launch.js", "");
		register("ckeditor", "CKEditor", "/library/webjars/ckeditor/4.9.1/full/ckeditor.js", "/library/editor/ckeditor.launch.js",
				"var CKEDITOR_BASEPATH='/library/webjars/ckeditor/4.9.1/full/';\n");
	}
	
	public void destroy() {
		editors.clear();
	}

	public void register(Editor editor) {
		editors.put(editor.getId().toLowerCase(), editor);
	}

	public void register(String id, String name, String editorUrl, String launchUrl, String preloadScript) {
		BaseEditor editor = new BaseEditor(id.toLowerCase(), name, editorUrl, launchUrl, preloadScript);
		register(editor);
	}

	public boolean unregister(Editor editor) {
		return unregister(editor.getId());
	}

	public boolean unregister(String id) {
		if (id != null) {
			id = id.toLowerCase();
		}
		if (editors.containsKey(id)) {
			editors.remove(id);
			return true;
		}
		else {
			return false;
		}
	}

	public Editor getEditor(String id) {
		if (id == null) {
			return null;
		}
		else {
			return editors.get(id.toLowerCase());
		}		
	}
	
	public List<Editor> getEditors() {
		ArrayList<Editor> list = new ArrayList<Editor>();
		for (Editor e : editors.values()) {
			BaseEditor be = new BaseEditor(e.getId(), e.getName(), e.getEditorUrl(), e.getLaunchUrl());
			list.add(be);
		}
		return list;
	}
}
