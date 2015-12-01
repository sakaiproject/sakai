package org.sakaiproject.service.gradebook.shared;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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
