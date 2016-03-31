package org.sakaiproject.lti.api;

import java.util.ArrayList;
import java.util.List;

public class LTISearchData {
	private String search = null;
	private List<Object> values = new ArrayList<Object>();
	
	public boolean hasValue() {
		return (search != null);
	}
	
	public void addSerchValue(Object value) {
		values.add(value);
	}
	
	public String getSearch() {
		return search;
	}
	public void setSearch(String search) {
		this.search = search;
	}
	public List<Object> getValues() {
		return values;
	}
	public void setValues(List<Object> values) {
		this.values = values;
	}
}
