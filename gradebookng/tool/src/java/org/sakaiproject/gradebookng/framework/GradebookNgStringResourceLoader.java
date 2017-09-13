/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.gradebookng.framework;

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.sakaiproject.util.ResourceLoader;

/**
 * IStringResourceLoader that plugs into sakai's resource loader.
 */
public class GradebookNgStringResourceLoader implements IStringResourceLoader {

	private ResourceLoader loader = new ResourceLoader("org.sakaiproject.gradebookng.GradebookNgApplication");

	@Override
	public String loadStringResource(Class<?> clazz, String key, Locale locale, String style, String variation) {
		return loadString(locale, key);
	}

	@Override
	public String loadStringResource(Component component, String key, Locale locale, String style, String variation) {
		return loadString(locale, key);
	}

	private String loadString(Locale locale, String key) {
		Locale sakaiLocale = loader.getLocale();
		if (locale != null && sakaiLocale == null) {
			loader.setContextLocale(locale);
		}
		return loader.getString(key);
	}
}
