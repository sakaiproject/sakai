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
 * Nominal implementation of the Editor interface.
 * This should suffice for typical usage where only the values are needed without additional logic.
 */
public class BaseEditor implements Editor {
	
	private String id;
	private String name;
	private String editorUrl;
	private String launchUrl;
	private String preloadScript;
	
	public BaseEditor() {
		this.id = "";
		this.name = "";
		this.editorUrl = "";
		this.launchUrl = "";
		this.preloadScript = "";
	}
	
	public BaseEditor(String id, String name, String editorUrl, String launchUrl) {
		this.id = id;
		this.name = name;
		this.editorUrl = editorUrl;
		this.launchUrl = launchUrl;
		this.preloadScript = "";
	}
	
	public BaseEditor(String id, String name, String editorUrl, String launchUrl, String preloadScript) {
		this.id = id;
		this.name = name;
		this.editorUrl = editorUrl;
		this.launchUrl = launchUrl;
		this.preloadScript = preloadScript;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setEditorUrl(String editorUrl) {
		this.editorUrl = editorUrl;
	}
	
	public String getEditorUrl() {
		return editorUrl;
	}

	public void setLaunchUrl(String launchUrl) {
		this.launchUrl = launchUrl;
	}
	
	public String getLaunchUrl() {
		return launchUrl;
	}
	
	public void setPreloadScript(String preloadScript) {
		this.preloadScript = preloadScript;
	}
	
	public String getPreloadScript() {
		return preloadScript;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Editor && getId().equals( ((Editor)obj).getId() ));
	}
	
	@Override
	public int hashCode() {
		return getId().hashCode();
	}

}
