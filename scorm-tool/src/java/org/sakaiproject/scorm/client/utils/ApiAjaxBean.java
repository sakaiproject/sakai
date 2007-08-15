package org.sakaiproject.scorm.client.utils;

import java.io.Serializable;

public class ApiAjaxBean implements Serializable
{
	private String arg1, arg2, result, value, scoId;

	public String getArg1() {
		return arg1;
	}

	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}

	public String getArg2() {
		return arg2;
	}

	public void setArg2(String arg2) {
		this.arg2 = arg2;
	}
	
	public String getResult()
	{
		return result;
	}
	
	public void setResult(String result) {
		this.result = result;
	}
	
	public String getScoId() {
		return scoId;
	}
	
	public void setScoId(String scoId) {
		this.scoId = scoId;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
