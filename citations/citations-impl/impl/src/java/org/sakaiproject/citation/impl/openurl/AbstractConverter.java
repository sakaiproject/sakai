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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sakaiproject.citation.api.Citation;


public abstract class AbstractConverter implements Converter {

	protected static final String DOI_PREFIX = "info:doi/";
	protected static final String ISSN_PREFIX = "ISSN:";
	protected static final String ISBN_URN_PREFIX = "urn:ISBN:";

	protected void convertSimple(Map<String, Object> props, ContextObjectEntity entity) {
		for (Map.Entry<String, Object> entry : props.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			String entityKey = getOpenUrlKey(key);
			
			// If it maps to a CO property
			if (entityKey != null) {
				// TODO Not sure that citations ever uses anything other than strings.
				addValue(entity, value, entityKey);
			} else {
				// Do other mapping.
				if ("doi".equals(key)) {
					if (value instanceof String) {
						entity.addId("info:doi"+ value);
					}
				} else if ("otherIds".equals(key)) {
					if (value instanceof String || value instanceof Date) {
						entity.addId((String)value);
					} else if (value instanceof List) {
						for(String id : (List<String>)value) {
							entity.addId(id);
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the key to the other format when doing the conversion.
	 * Typically you just hook this up to a bidimap.
	 * @param key
	 * @return
	 */
	protected abstract String getOpenUrlKey(String citationKey);
	
	protected abstract String getCitationsKey(String openUrlKey);

	protected void convertSimple(Map<String, List<String>> values, Citation citation) {
		// Map the rest of the values.
		for(Map.Entry<String, List<String>> entry: values.entrySet()) {
			String key = entry.getKey();
			List<String> entryValues = entry.getValue();
			String citationKey = getCitationsKey(key);
			if (citationKey != null) {
				if (citation.hasCitationProperty(citationKey)) {
					if (citation.isMultivalued(citationKey)) {
						for (String value: entryValues) {
							citation.setCitationProperty(citationKey, value);
						}
					}
					
				} else {
					for (String value: entryValues) {
						if (value != null) {
							citation.setCitationProperty(citationKey, value);
						}
					}
				}
			}
		}
	}
	
	void addValue(ContextObjectEntity entity, Object value,
			String entityKey) {
		if (value instanceof String || value instanceof Date) {
			entity.addValue(entityKey, value.toString());
		} else if (value instanceof List) {
			// If it's multivalued add them all.
			for (String listValue : (List<String>) value) {
				entity.addValue(entityKey, listValue);
			}
		}
	}
	
	void addId(ContextObjectEntity entity, Object value, String prefix) {
		if (value instanceof String || value instanceof Date) {
			entity.addId(prefix + value.toString());
		} else if (value instanceof List) {
			// If it's multivalued add them all.
			for (String listValue : (List<String>) value) {
				entity.addId(prefix+ listValue);
			}
		}
	}




	

}
