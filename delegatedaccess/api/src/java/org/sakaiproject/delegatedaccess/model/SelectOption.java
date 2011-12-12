package org.sakaiproject.delegatedaccess.model;

import java.io.Serializable;

public class SelectOption implements Serializable{
	private static final long serialVersionUID = 1L;
	private String label;
	private String value;

	public SelectOption(String label, String value) {
		this.setLabel(label);
		this.setValue(value);
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
