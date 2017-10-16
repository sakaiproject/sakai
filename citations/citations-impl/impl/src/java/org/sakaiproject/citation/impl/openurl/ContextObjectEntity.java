/**
 * Copyright (c) 2003-2011 The Apereo Foundation
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
package org.sakaiproject.citation.impl.openurl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents one of the entities passes across in an OpenURL. Examples include
 * the Referent or the Requestor.
 * 
 * @author buckett
 * 
 */

public class ContextObjectEntity {

	private String format;
	private Set<String> ids = new HashSet<String>();
	// Some things can appear multiple times and example of this is the au prop on a book.
	private Map<String, List<String>> values = new HashMap<String, List<String>>();
	private String refFormat;
	private String ref;
	private String data;

	public String getFormat() {
		return format;
	}

	public Set<String> getIds() {
		return ids;
	}

	public Map<String, List<String>> getValues() {
		return values;
	}

	public String getRefFormat() {
		return refFormat;
	}

	public String getRef() {
		return ref;
	}

	public String getData() {
		return data;
	}

	public void addId(String id) {
		ids.add(id);
	}
	
	public String getValue(String key) {
		List<String> keyValues = values.get(key);
		if (keyValues != null && keyValues.size() > 0) {
			return keyValues.get(0);
		}
		return null;
	}

	public void addValue(String key, String value) {
		// Don't use too many ArrayLists as most values will be single valued.
		if (values.containsKey(key)) {
			List<String> existing = values.get(key);
			if (existing.size() == 1) {
				existing = new ArrayList<String>(existing);
				existing.add(value);
			} else {
				existing.add(value);
			}
		} else {
			values.put(key, Collections.singletonList(value));
		}
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setRefFormat(String refFormat) {
		this.refFormat = refFormat;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public void setData(String data) {
		this.data = data;
	}
}
