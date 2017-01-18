/**
 * Copyright (c) 2007 The Apereo Foundation
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.coding.AbstractRequestTargetUrlCodingStrategy;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.ValueMap;

public class ContentPackageResourceMountStrategy extends AbstractRequestTargetUrlCodingStrategy {

	private static Log log = LogFactory.getLog(ContentPackageResourceMountStrategy.class);
	
	public ContentPackageResourceMountStrategy(String mountPath) {
		super(mountPath);
	}

	public IRequestTarget decode(RequestParameters requestParameters) {
		final String pathFragment = requestParameters.getPath().substring(getMountPath().length());
		int lastResourceIndex = pathFragment.lastIndexOf("resourceName/");
		final String parametersFragment = pathFragment.substring(lastResourceIndex);
		final ValueMap parameters = decodeParameters(parametersFragment, requestParameters
				.getParameters());

		if (log.isDebugEnabled())
			log.debug("decode -------------> PARAM FRAGMENT: " + parametersFragment);
		
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
		//appendParameters(url, requestParameters.getParameters());
		
		Map parameters = requestParameters.getParameters();
		String resourceName = (String)parameters.get("resourceName");
		
		if (StringUtils.isNotBlank(resourceName)) {
			if (!url.endsWith("/"))
				url.append("/");

			
			try {
				resourceName = URLDecoder.decode(resourceName, "UTF-8");
	        } catch (UnsupportedEncodingException e) {
		        // Very unlikely, but report anyway.
	        	log.error("Error while URL decoding: '"+resourceName+"'", e);
	        }
	        url.append("resourceName");
			
			if (!resourceName.startsWith("/"))
				url.append("/");
			
			url.append(resourceName);
		}
		
		if (log.isDebugEnabled())
			log.debug("encode -----------> URL: " + url); 
		
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
	
	public boolean matches(String path) {
		return path.contains(getMountPath() + "/");
	}
	
	@Override
	protected void appendParameters(AppendingStringBuffer url, Map parameters)
	{
		if (parameters != null && parameters.size() > 0)
		{
			final Iterator entries = parameters.entrySet().iterator();
			while (entries.hasNext())
			{
				Map.Entry entry = (Entry)entries.next();
				Object value = entry.getValue();
				if (value != null)
				{
					if (value instanceof String[])
					{
						String[] values = (String[])value;
						for (int i = 0; i < values.length; i++)
						{
							appendValue(url, entry.getKey().toString(), values[i]);
						}
					}
					else
					{
						appendValue(url, entry.getKey().toString(), value.toString());
					}
				}
			}
		}
	}

	private void appendValue(AppendingStringBuffer url, String key, String value)
	{
		String escapedValue = urlEncode(value);
		if (key.equals("resourceName"))
			escapedValue = value;
		if (!Strings.isEmpty(escapedValue))
		{
			if (!url.endsWith("/"))
			{
				url.append("/");
			}
			url.append(key).append("/").append(escapedValue).append("/");
		}
	}
	
	@Override
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
		if (urlFragment.startsWith("resourceName")) {
			int resourceNameStart = urlFragment.indexOf('/');
			String resourceIdFragment = urlFragment.substring(resourceNameStart);
			
			parameters.add("resourceName", resourceIdFragment); //urlDecode(resourceIdFragment));
		}

		if (urlParameters != null)
		{
			if (log.isDebugEnabled()) {
				for (Iterator keyIterator = urlParameters.keySet().iterator();keyIterator.hasNext();) {
					String key = (String)keyIterator.next();
					Object value = urlParameters.get(key);
					
					log.debug("URL PARAMS KEY: " + key + " VALUE: " + value.toString());
				}
			}
			
			
			parameters.putAll(urlParameters);
		}

		return parameters;
	}
	
	@Override
	protected String urlDecode(String value)
	{
		try
		{
			value = URLDecoder.decode(value, Application.get().getRequestCycleSettings()
					.getResponseRequestEncoding());
		}
		catch (UnsupportedEncodingException ex)
		{
			log.error("error decoding parameter", ex);
		}
		return value;
	}


	@Override
	protected String urlEncode(String string)
	{
		try
		{
			return URLEncoder.encode(string, Application.get().getRequestCycleSettings()
					.getResponseRequestEncoding());
		}
		catch (UnsupportedEncodingException e)
		{
			log.error(e.getMessage(), e);
			return string;
		}

	}
}
