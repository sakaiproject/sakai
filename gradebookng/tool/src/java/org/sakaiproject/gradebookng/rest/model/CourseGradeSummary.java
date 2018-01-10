package org.sakaiproject.gradebookng.rest.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import lombok.Getter;
import lombok.Setter;

public class CourseGradeSummary {
	
	/**
	 * Key is the label, ie letter grade, value is the count of students that have that grade
	 */
	@Getter 
	@Setter
	Map<String,Integer> dataset;
	
	public CourseGradeSummary() {
		this.dataset = new LinkedHashMap<>();
	}

	/**
	 * If label present then increment its count, otherwise add it
	 * @param label letter grade we are wrking with
	 */
	public void add(String label) {
		dataset.computeIfPresent(label, (k,v) -> v+1);
		dataset.computeIfAbsent(label, value -> 1);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
