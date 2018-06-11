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
package org.sakaiproject.service.gradebook.shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * DTO to wrap the persistent GradeMapping and provides utility methods for dealing with grade mappings
 */
public class GradeMappingDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id; //note that this is a Long in GradeMapping but we convert for simplicity
	private String name;
	private Map<String, Double> gradeMap;
	private Map<String, Double> defaultBottomPercents;

	public GradeMappingDefinition(final Long id, final String name, final Map<String,Double> gradeMap, final Map<String, Double> defaultBottomPercents){
		this.id = Long.toString(id);
		this.name = name;
		this.gradeMap = gradeMap;
		this.defaultBottomPercents = defaultBottomPercents;
	}

	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Get the current grade mappings
	 *
	 * @return
	 */
	public Map<String, Double> getGradeMap() {
		return this.gradeMap;
	}

	public void setGradeMap(final Map<String, Double> gradeMap) {
		this.gradeMap = gradeMap;
	}

	/**
	 * Get the default grade mappings
	 *
	 * @return
	 */
	public Map<String, Double> getDefaultBottomPercents() {
		return this.defaultBottomPercents;
	}

	public void setDefaultBottomPercents(final Map<String, Double> defaultBottomPercents) {
		this.defaultBottomPercents = defaultBottomPercents;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	/**
	 * Handles the sorting of a grade mapping.
	 *
	 * @param gradeMap the grademap to be sorted
	 * @return {@link LinkedHashMap} of sorted entries
	 */
	public static Map<String, Double> sortGradeMapping(final Map<String, Double> gradeMap) {

		// we only ever order by bottom percents now
		final DoubleComparator doubleComparator = new DoubleComparator(gradeMap);
		final Map<String, Double> tMap = new TreeMap<>(doubleComparator);
		tMap.putAll(gradeMap);
		final Map<String, Double> rval = new LinkedHashMap<>(tMap);
		
		return rval;
	}

	/**
	 * Determines if the grade mapping is different to the defaults
	 *
	 * @return
	 */
	public boolean isModified() {
		// TreeMap.equals uses compareTo for comparisons so cannot be used for equals in this case. Convert to HashMap.
		final Map<String, Double> left = new HashMap<>(this.gradeMap);
		final Map<String, Double> right = new HashMap<>(this.defaultBottomPercents);
		return !left.equals(right);
	}

}
