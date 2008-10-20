package org.sakaiproject.scorm.model.api;

import java.util.LinkedList;
import java.util.List;

public class CMIFieldGroup {

	private long id;
	private long contentPackageId;
	private List<CMIField> list;

	public CMIFieldGroup() { 
		this.list = new LinkedList<CMIField>();
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public List<CMIField> getList() {
		return list;
	}

	public void setList(List<CMIField> list) {
		this.list = list;
	}

	public long getContentPackageId() {
		return contentPackageId;
	}

	public void setContentPackageId(long contentPackageId) {
		this.contentPackageId = contentPackageId;
	}
	
}
