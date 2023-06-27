package org.sakaiproject.microsoft.api.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.site.api.Site;

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
	
	public boolean match(Site site) {
		//if only published sites are allowed and site is not published
		//or
		//if site type does not match
		if((isPublished() && !site.isPublished()) ||  (!getSiteType().equals(SakaiSiteFilter.TYPE_ALL) && !site.getType().equals(getSiteType()))) {
			return false;
		}
		
		//check site properties
		if(StringUtils.isNotBlank(siteProperty)) {
			String[] tokens = siteProperty.split("=");
			String filterPropertyName = (tokens.length >= 1) ? tokens[0] : "";
			String filterPropertyValue = (tokens.length > 1) ? tokens[1] : "";
			
			Optional<String> ret = StreamSupport.stream(Spliterators.spliteratorUnknownSize(site.getProperties().getPropertyNames(), Spliterator.ORDERED), false)
				.filter(propName -> {
					//compare name
					if(propName.equals(filterPropertyName)) {
						//compare value
						if(StringUtils.isNotBlank(filterPropertyValue)) {
							String propValue = site.getProperties().getProperty(propName);
							return filterPropertyValue.equals(propValue);
						}
						return true;
					}
					return false;
				}).findAny();
			
			return ret.isPresent();
		}
		
		return true;
	}
}
