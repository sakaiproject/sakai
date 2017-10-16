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
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * DTO to wrap the persistent GradeMapping
 */
public class GradeMappingDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id; //note that this is a Long in GradeMapping but we convert for simplicity
	private String name;
	private Map<String, Double> gradeMap;
	private Map<String, Double> defaultBottomPercents;
	
	public GradeMappingDefinition(Long id, String name, Map<String,Double> gradeMap, Map<String, Double> defaultBottomPercents){
		this.id = Long.toString(id);
		this.name = name;
		this.gradeMap = gradeMap;
		this.defaultBottomPercents = defaultBottomPercents;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Double> getGradeMap() {
		return gradeMap;
	}

	public void setGradeMap(Map<String, Double> gradeMap) {
		this.gradeMap = gradeMap;
	}

	public Map<String, Double> getDefaultBottomPercents() {
		return defaultBottomPercents;
	}

	public void setDefaultBottomPercents(Map<String, Double> defaultBottomPercents) {
		this.defaultBottomPercents = defaultBottomPercents;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
