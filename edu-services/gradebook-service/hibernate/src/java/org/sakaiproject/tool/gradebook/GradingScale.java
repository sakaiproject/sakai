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

package org.sakaiproject.tool.gradebook;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sakaiproject.service.gradebook.shared.GradingScaleDefinition;

public class GradingScale implements Serializable, Comparable<Object> {
	
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private int version;

	private String uid;
	private String name;
	private List<String> grades;
	private Map<String, Double> defaultBottomPercents;	// From grade to percentage
	private boolean unavailable;

	public Map<String, Double> getDefaultBottomPercents() {
		return defaultBottomPercents;
	}
	public void setDefaultBottomPercents(Map<String, Double> defaultBottomPercents) {
		this.defaultBottomPercents = defaultBottomPercents;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Because the Gradebook now supports non-calculated manual-only grades with
	 * no percentage equivalent, it is possible for the list of grades to include
	 * codes that are not included in the defaultBottomPercents map. In other
	 * words, callers shouldn't expect getDefaultBottomPercents.keySet() to be
	 * equivalent to this list.
	 * @return list of supported grade codes, ordered from highest to lowest
	 */
	public List<String> getGrades() {
		return grades;
	}
	public void setGrades(List<String> grades) {
		this.grades = grades;
	}
	public boolean isUnavailable() {
		return unavailable;
	}
	public void setUnavailable(boolean unavailable) {
		this.unavailable = unavailable;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}

    @Override
	public int compareTo(Object o) {
        return getName().compareTo(((GradingScale)o).getName());
    }
    @Override
	public String toString() {
        return new ToStringBuilder(this).
            append(getUid()).toString();
    }

    /**
     * Convert this GradeingScale instance to a GradingScaleDefinition
     * @return
     */
	public GradingScaleDefinition toGradingScaleDefinition(){
		GradingScaleDefinition scaleDef = new GradingScaleDefinition();
		scaleDef.setUid(this.getUid());
		scaleDef.setName(this.getName());
		
		Map<String, Double> mapBottomPercents = this.getDefaultBottomPercents();
		scaleDef.setDefaultBottomPercents(mapBottomPercents);

		//build the bottom percents as a list as well
		List<Object> listBottomPercents = new ArrayList<>();
		List<String> grades = new ArrayList<>();
		for(Map.Entry<String, Double> pair : mapBottomPercents.entrySet()) {
			listBottomPercents.add(pair.getValue());
			grades.add(pair.getKey());
		}
		scaleDef.setGrades(grades);
		scaleDef.setDefaultBottomPercentsAsList(listBottomPercents);
		
		return scaleDef;
	}


}
