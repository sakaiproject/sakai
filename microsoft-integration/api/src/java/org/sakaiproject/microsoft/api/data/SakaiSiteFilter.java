/**
 * Copyright (c) 2024 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.microsoft.api.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.site.api.Site;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
