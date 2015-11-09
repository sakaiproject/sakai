/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.content.metadata.model;

import java.util.Map;

/**
 * Allows values associated to a certain kind of metadata to be converted in the application
 *
 * @author Colin Hebert
 */
public interface MetadataConverter<T>
{
	/**
	 * Transforms the object in parameter into a simple String.
	 * The string can be later parsed with {@link MetadataConverter#fromString(String)}
	 *
	 * @param metaValue Object to convert as a String
	 * @return String value of the parameter
	 */
	String toString(T metaValue);

	/**
	 * Converts a String into an object of the appropriate type.
	 * The given String should be generated with {@link MetadataConverter#toString(Object)}
	 *
	 * @param stringValue String to convert into an Object
	 * @return The converted result
	 */
	T fromString(String stringValue);

	/**
	 * Transforms the object into a properties map.
	 * The map should only contain String and List&lt;String&gt; elements.
	 *
	 * @param metaValue Object to convert
	 * @return a Map containing every relevant values
	 */
	Map<String, ?> toProperties(T metaValue);

	T fromProperties(Map<String, ?> properties);

	/**
	 * Fetches relevant information in a map (usually the parameters from a HttpServletRequest) to build an Object.
	 *
	 * @param parameters			Map containing the information
	 * @param parameterSuffix Optional suffix which can be used in HTML forms
	 * @return The converted result
	 */
	T fromHttpForm(Map<String, ?> parameters, String parameterSuffix);
}
