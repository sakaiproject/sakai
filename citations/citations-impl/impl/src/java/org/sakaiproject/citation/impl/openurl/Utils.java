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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Supporting static utilities for the OpenURL code.
 * @author buckett
 *
 */
public class Utils {
	
	/**
	 * Just gets the first value for the key out of the map.
	 * @param params
	 * @param key
	 * @return The value or <code>null</code> if it isn't found or is null.
	 */
	public static String getValue(Map<String, String[]> params, String key) {
		String[] values = params.get(key);
		if (values != null) {
			if (values.length > 0) {
				if (values.length > 1) {
					// Too many values, dropping one.
				}
				return values[0];
			}
		}
		return null;
	}

	/**
	 * This splits the source string up into a map. Similar to the servlet request 
	 * parsing but we don't decode it yet, until we know the encoding we should use.
	 * @return Always returns a Map of the values.
	 */
	public static Map<String, String[]>split(String source) {
		Map<String, String[]> values = new HashMap<String,String[]>();
		if (source != null) {
			String[] parts = source.split("&");
			for(String part: parts) {
				String kv[] = part.split("=");
				if ((kv.length == 2 || kv.length == 1) && kv[0].length() > 1 ) {
					String key = kv[0];
					String value = (kv.length > 1)?kv[1]:null;
					if (values.containsKey(key)) {
						// Not very efficient, but not may params have multiple values.
						List<String> valuesList = new ArrayList<String>(Arrays.asList(values.get(key)));
						valuesList.add(value);
						values.put(key, valuesList.toArray(new String[]{}));
					} else {
						values.put(key, new String[]{value});
					}
				}
			}
		}
		return values;
	}

	/**
	 * Decode all the keys and values based on the encoding.
	 */
	public static Map<String, String[]> decode(Map<String, String[]> source, String encoding) {
		try {
			Map <String, String[]> decoded = new HashMap<String, String[]>();
			for(Map.Entry<String, String[]> entry: source.entrySet()) {
				String key = URLDecoder.decode(entry.getKey(), encoding);
				String[] values = new String[entry.getValue().length];
				for(int i = 0; i< entry.getValue().length; i++) {
					values[i] = (entry.getValue()[i] == null)?null:URLDecoder.decode(entry.getValue()[i], encoding);
				}
				decoded.put(key, values);
			}
			return decoded;
		} catch (UnsupportedEncodingException uee) {
			throw new IllegalArgumentException("Unsupported encoding: "+encoding);
		}
	}

	/**
	 * Attempts to extract the author from the multiple fields in an OpenURL and return an author string.
	 * @return Author string, such as John Smith.
	 */
	public static String lookForAuthor(Map<String, List<String>> values) {
		// If we don't have a surname don't event bother
		StringBuilder author = new StringBuilder();
		if (appendFirst(author, values.get("aulast"), null)) {
			
			// Check for author in other fields. 
			if (values.containsKey("au")) {
				for (String otherAuthor: values.get("au")) {
					if (otherAuthor != null && otherAuthor.toLowerCase().contains(author.toString().toLowerCase())) {
						// Surname already exist in other au field, assume we don't need this one.
						return null;
					}
				}
			}
			
			// Try to build as much of a name as possible without duplicating parts.
			if (!appendFirst(author, values.get("aufirst"), ", ")) {
				if (!appendFirst(author, values.get("auinit"), ", ")) {
					if (appendFirst(author, values.get("auinit1"), ", ")) {
						appendFirst(author, values.get("auinitm"), " ");
					}
				}
			}
			return author.toString();
		}
		return null;
	}
	
	/**
	 * Simple utility function for adding the first value of the list of strings to the buffer.
	 * @param buffer
	 * @param values
	 * @param seperator
	 */
	public static boolean appendFirst(StringBuilder buffer, List<String> values, String seperator) {
		if (values != null && !values.isEmpty()) {
			String value = values.get(0);
			if (value != null) {
				if (seperator != null) {
					buffer.append(seperator);
				}
				buffer.append(value);
				return true;
			}
		}
		return false;
	}
}
