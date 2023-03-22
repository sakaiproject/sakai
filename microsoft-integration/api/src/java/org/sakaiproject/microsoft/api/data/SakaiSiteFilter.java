package org.sakaiproject.microsoft.api.data;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SakaiSiteFilter {
	public static final String TYPE_COURSE = "course";
	public static final String TYPE_PROJECT = "project";
	public static final String TYPE_ALL = "all";
	
	private String siteType = TYPE_ALL;
	private boolean published = false;
	private String siteProperty = "";
	
	public String getSiteType() {
		return getSiteType(true);
	}
	public String getSiteType(boolean printAll) {
		if(TYPE_PROJECT.equals(siteType)) {
			return TYPE_PROJECT;
		}
		
		if(TYPE_COURSE.equals(siteType)) {
			return TYPE_COURSE;
		}

		return (printAll) ? TYPE_ALL : null;

	}
	
	public Map<String, String> getSitePropertyMap() {
		if(StringUtils.isNotBlank(siteProperty)) {
			Map<String, String> ret = new HashMap<>();
			String[] tokens = siteProperty.split("=");
			if(tokens.length == 1) {
				ret.put(tokens[0], "%");
			}
			if(tokens.length > 1) {
				ret.put(tokens[0], tokens[1]);
			}
			return ret;
		}
		return null;
	}
}
