package org.sakaiproject.scorm.model.api;

import java.io.Serializable;

public class CMIData implements Serializable {

	private String fieldName;
	private String fieldValue;
	private String description;
	
	public CMIData(String fieldName, String fieldValue, String description) {
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
		this.description = description;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
