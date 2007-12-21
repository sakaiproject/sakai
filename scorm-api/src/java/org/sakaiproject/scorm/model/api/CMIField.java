package org.sakaiproject.scorm.model.api;

import java.util.LinkedList;
import java.util.List;

public class CMIField {

	private long id;
	private String fieldName;
	private List<String> fieldValues;
	private String description;
	private List<CMIField> children;
	
	public CMIField() {
		this(null, null);
	}
	
	public CMIField(String fieldName, String description) {
		this.fieldName = fieldName;
		this.description = description;
		this.children = new LinkedList<CMIField>();
		this.fieldValues = new LinkedList<String>();
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public void addChild(CMIField child) {
		children.add(child);
	}
	
	public List<CMIField> getChildren() {
		return children;
	}
	public void setChildren(List<CMIField> children) {
		this.children = children;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public boolean isParent() {
		return children != null && children.size() > 0;
	}

	public String getFieldValue() {
		if (fieldValues == null || fieldValues.size() == 0)
			return null;
		
		return fieldValues.get(0);
	}

	public List<String> getFieldValues() {
		return fieldValues;
	}
	
	public void addFieldValue(String fieldValue) {
		this.fieldValues.add(fieldValue);
	}
	
}
