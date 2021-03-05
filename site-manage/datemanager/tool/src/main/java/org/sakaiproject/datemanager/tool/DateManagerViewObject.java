package org.sakaiproject.datemanager.tool;

import org.json.simple.JSONArray;

import lombok.Data;

@Data
public class DateManagerViewObject {
	
	public JSONArray toolJson;
	private String toolTitle;

}
