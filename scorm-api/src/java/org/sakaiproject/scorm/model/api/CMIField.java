package org.sakaiproject.scorm.model.api;

import java.util.LinkedList;
import java.util.List;

public class CMIField {

	private Long id;

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

	public void addChild(CMIField child) {
		children.add(child);
	}

	public void addFieldValue(String fieldValue) {
		this.fieldValues.add(fieldValue);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CMIField other = (CMIField) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public List<CMIField> getChildren() {
		return children;
	}

	public String getDescription() {
		return description;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getFieldValue() {
		if (fieldValues == null || fieldValues.size() == 0)
			return null;

		return fieldValues.get(0);
	}

	public List<String> getFieldValues() {
		return fieldValues;
	}

	public Long getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public boolean isParent() {
		return children != null && children.size() > 0;
	}

	public void setChildren(List<CMIField> children) {
		this.children = children;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
