/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test.mocks;

import java.util.Properties;
import java.util.Set;

import org.sakaiproject.tool.api.Tool;

public abstract class FakeTool implements Tool {
	String id;
	String home;
	String title;
	String description;
	Set<String> categories;
	Set<String> keywords;
	Properties mutableConfig;
	Properties registeredConfig;
	Properties finalConfig;
	
	public AccessSecurity getAccessSecurity() {
		return AccessSecurity.TOOL;
	}

	public Set<String> getCategories() {
		return categories;
	}

	public void setCategories(Set<String> categories) {
		this.categories = categories;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Properties getFinalConfig() {
		return finalConfig;
	}

	public void setFinalConfig(Properties finalConfig) {
		this.finalConfig = finalConfig;
	}

	public String getHome() {
		return home;
	}

	public void setHome(String home) {
		this.home = home;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Set<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(Set<String> keywords) {
		this.keywords = keywords;
	}

	public Properties getMutableConfig() {
		return mutableConfig;
	}

	public void setMutableConfig(Properties mutableConfig) {
		this.mutableConfig = mutableConfig;
	}

	public Properties getRegisteredConfig() {
		return registeredConfig;
	}

	public void setRegisteredConfig(Properties registeredConfig) {
		this.registeredConfig = registeredConfig;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
