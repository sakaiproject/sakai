/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/syracuse/gmt/branches/oncourse_osp_enhancements/gmt-impl/impl/src/java/org/sakaiproject/gmt/tagging/impl/GmtTaggingHelperInfoImpl.java $
 * $Id: GmtTaggingHelperInfoImpl.java 45333 2008-02-06 20:28:50Z chmaurer@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.taggable.impl;

import java.util.Map;

import org.sakaiproject.taggable.api.TaggingHelperInfo;
import org.sakaiproject.taggable.api.TaggingProvider;

public class TaggingHelperInfoImpl implements TaggingHelperInfo {

	String helperId, description, name;

	Map<String, ? extends Object> parameterMap;

	TaggingProvider provider;

	public TaggingHelperInfoImpl(String helperId, String name,
			String description, Map<String, ? extends Object> parameterMap,
			TaggingProvider provider) {
		this.helperId = helperId;
		this.name = name;
		this.description = description;
		this.parameterMap = parameterMap;
		this.provider = provider;
	}

	public String getHelperId() {
		return helperId;
	}

	public TaggingProvider getProvider() {
		return provider;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Map<String, ? extends Object> getParameterMap() {
		return parameterMap;
	}

}
