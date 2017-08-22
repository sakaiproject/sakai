/**
 * Copyright (c) 2006-2015 The Apereo Foundation
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
package org.sakaiproject.util;

/**
 * <p>
 * MyFaces has a issue/bug whereby if a managed bean implements Map then it calls {@link #put(Object, Object)} rather
 * than an explicit setter. This means that setting the baseName for the resource bundle doesn't work on
 * the plain ResourceLoader as ResourceLoader doesn't support {@link #put(Object, Object)}.
 * </p>
 * <p>
 * We also implement {@link #get(Object)} because with MyFaces is initialising managed properties it calls the setting
 * first and without this we get warning about using a null bundle.
 * </p>
 *
 * @author Matthew Buckett
 */
public class ResourceLoaderWrapper extends ResourceLoader {

	@Override
	public Object get(Object key) {
		if ("baseName".equals(key)) {
			return baseName;
		} else {
			return super.get(key);
		}
	}

	@Override
	public Object put(Object key, Object value) {
		if ("baseName".equals(key) && value instanceof String) {
			setBaseName((String)value);
			return null;
		} else {
			throw new IllegalArgumentException("We don't support put() with :"+ key);
		}
	}

}
