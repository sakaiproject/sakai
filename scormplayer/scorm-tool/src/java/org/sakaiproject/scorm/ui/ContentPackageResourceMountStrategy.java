/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
/*
 * The logic in this class was modeled after the Apache Wicket class
 * 	org.apache.wicket.request.target.coding.SharedResourceRequestTargetUrlCodingStrategy
 * authored by Gili Tzabari and distributed under the following license:
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.ui;

import java.util.Collections;
import java.util.Map;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.coding.AbstractRequestTargetUrlCodingStrategy;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.value.ValueMap;

public class ContentPackageResourceMountStrategy extends AbstractRequestTargetUrlCodingStrategy {

	public ContentPackageResourceMountStrategy(String mountPath) {
		super(mountPath);
	}

	public IRequestTarget decode(RequestParameters requestParameters) {
		final String parametersFragment = requestParameters.getPath().substring(
				getMountPath().length());
		final ValueMap parameters = decodeParameters(parametersFragment, requestParameters
				.getParameters());

		requestParameters.setParameters(parameters);
		return new ContentPackageResourceRequestTarget(requestParameters);
	}

	public CharSequence encode(IRequestTarget requestTarget) {
		if (!(requestTarget instanceof ContentPackageResourceRequestTarget))
		{
			throw new IllegalArgumentException("This encoder can only be used with " +
					"instances of " + ContentPackageResourceRequestTarget.class.getName());
		}
		final AppendingStringBuffer url = new AppendingStringBuffer(40);
		url.append(getMountPath());
		final ContentPackageResourceRequestTarget target = (ContentPackageResourceRequestTarget)requestTarget;

		RequestParameters requestParameters = target.getRequestParameters();
		appendParameters(url, requestParameters.getParameters());
		return url;
	}

	public boolean matches(IRequestTarget requestTarget) {
		if (requestTarget instanceof ContentPackageResourceRequestTarget)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	protected ValueMap decodeParameters(String urlFragment, Map urlParameters)
	{
		// Hack off any leading slash
		if (urlFragment.startsWith("/"))
		{
			urlFragment = urlFragment.substring(1);
		}
		// Hack off any trailing slash
		if (urlFragment.length() > 0 && urlFragment.endsWith("/"))
		{
			urlFragment = urlFragment.substring(0, urlFragment.length() - 1);
		}

		if (urlFragment.length() == 0)
		{
			return new ValueMap(urlParameters != null ? urlParameters : Collections.EMPTY_MAP);
		}
		
		ValueMap parameters = new ValueMap();
		
		// Grab resourceId 
		if (urlFragment.startsWith("resourceId")) {
			int resourceIdStart = urlFragment.indexOf('/');
			int resourceIdEnd = urlFragment.indexOf('/', resourceIdStart + 1);
			String resourceIdFragment = urlFragment.substring(resourceIdStart + 1, resourceIdEnd);
			
			parameters.add("resourceId", urlDecode(resourceIdFragment));
			
			String pathFragment = urlFragment.substring(resourceIdEnd + 1);
			if (pathFragment.startsWith("/"))
				pathFragment = pathFragment.substring(1);
			
			if (pathFragment.startsWith("path/")) {
				int pathStart = pathFragment.indexOf('/');
				String path = pathFragment.substring(pathStart + 1);
				
				parameters.add("path", urlDecode(path));
			}
		}

		if (urlParameters != null)
		{
			parameters.putAll(urlParameters);
		}

		return parameters;
	}
}
