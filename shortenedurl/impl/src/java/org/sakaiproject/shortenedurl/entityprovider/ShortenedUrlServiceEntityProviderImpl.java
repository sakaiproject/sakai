/**
 * Copyright (c) 2009-2016 The Apereo Foundation
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
package org.sakaiproject.shortenedurl.entityprovider;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.shortenedurl.api.ShortenedUrlService;

/**
 * Implementation of the EntityProvider for the ShortenedUrlService to allow URL shortening via GET requests.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class ShortenedUrlServiceEntityProviderImpl implements ShortenedUrlServiceEntityProvider,  EntityProvider, AutoRegisterEntityProvider, Describeable, ActionsExecutable {

	
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	@EntityCustomAction(action="shorten",viewKey=EntityView.VIEW_LIST)
	public Object shorten(OutputStream out, EntityView view, Map<String, Object> params) {
		
		String path = (String)params.get("path");
		if(StringUtils.isBlank(path)){
			throw new EntityException("Invalid path.", path);
		}
		
		//SHORTURL-38 check if eternal urls are allowed to be shortened, defaults to false (only internal urls are allowed)
		//if external not allowed then we need to check the host and the url to be shortened, otherwise we don't care
		boolean externalAllowed = serverConfigurationService.getBoolean("shortenedurl.external.enabled", false);
		if(!externalAllowed) {
			String serverUrl = serverConfigurationService.getServerUrl();
			
			//decode path
			String pathDecoded;
			try {
				pathDecoded = URLDecoder.decode(path, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new EntityException("Unable to decode path.", path);
			}
			
			//path could be a relative fragment (ie /portal/site/abc), if so, create full url and check
			String fullUrl = pathDecoded;
			if(StringUtils.startsWith(pathDecoded, "/")) {
				fullUrl = serverUrl + pathDecoded;
				log.debug("Path: " + pathDecoded + ", full URL: " + fullUrl);
			}
						
			//now have full url so check they start with the same value. otherwise it is external and it should be blocked.
			if(!StringUtils.startsWith(fullUrl, serverUrl)) {
				log.error("Attempted to shorten:" + pathDecoded + ", but this does not have the same prefix as the current server: " + serverUrl);
				throw new EntityException("Couldn't shorten URL as external URLs are not permitted. The path parameter must contain either a relative path or a full URL that is for the same host.", path, HttpServletResponse.SC_FORBIDDEN);
			}
		}
		
		boolean secure = Boolean.parseBoolean((String)params.get("secure"));
		
		try {
			String shortenedUrl = shortenedUrlService.shorten(URLDecoder.decode(path, "UTF-8"), secure);
			if(StringUtils.isBlank(shortenedUrl)){
				throw new EntityException("Couldn't shorten URL.", path);
			}
			return shortenedUrl;
		} catch (UnsupportedEncodingException e) {
			throw new EntityException("Unable to decode path.", path);
		}
	
	}
	
	@Setter
	private ShortenedUrlService shortenedUrlService;
	
	@Setter
	private ServerConfigurationService serverConfigurationService;
	
}
