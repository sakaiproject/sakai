/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.onedrive.util;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * HTTP methods required by the service
 */
@Slf4j
public class HTTPConnectionUtil {

	public static String formUrlencodedString(Map<String,String> params) {
		StringBuilder param = new StringBuilder("");
		for (Map.Entry<String, String> item : params.entrySet()) {
			if (param.length() != 0) {
				param.append('&');
			}
			param.append(item.getKey());
			param.append('=');
			param.append(item.getValue().toString());
		}
		log.debug("formUrlencodedString : " + param.toString());
		return param.toString();
	}

	public static String makePostCall(String endpoint, StringEntity body) throws Exception {
		try {
			URIBuilder uriBuilder = new URIBuilder(endpoint);
			URI apiUri = uriBuilder.build();

			HttpPost request = new HttpPost(apiUri);
			request.addHeader("content-type", "application/x-www-form-urlencoded");			
			request.setEntity(body);
			
			// Configure request timeouts.
			RequestConfig requestConfig = RequestConfig.custom().build();
			request.setConfig(requestConfig);

			CloseableHttpResponse response = null;
			log.debug(request.toString());
			InputStream stream = null;
			try (CloseableHttpClient client = HttpClients.createDefault()) {
				response = client.execute(request);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					stream = entity.getContent();
					String streamString = IOUtils.toString(stream, "UTF-8");
					return streamString;
				}
			} catch (Exception e) {
				log.warn("Could not fetch results from OneDrive API." + e.getMessage());
			}
		
		} catch (URISyntaxException e) {
			log.error("Incorrect OneDrive API url syntax.", e);
		}
		return null;
	}
	
	public static String makeGetCall(String endpoint, List<NameValuePair> params, String bearer) throws Exception {
		try {
			URIBuilder uriBuilder = new URIBuilder(endpoint).addParameters(params);
			URI apiUri = uriBuilder.build();

			HttpGet request = new HttpGet(apiUri);
			request.addHeader("Authorization", "Bearer " + bearer);

			// Configure request timeouts.
			RequestConfig requestConfig = RequestConfig.custom().build();
			request.setConfig(requestConfig);

			CloseableHttpResponse response = null;
			log.debug(request.toString());
			InputStream stream = null;
			try (CloseableHttpClient client = HttpClients.createDefault()) {
				response = client.execute(request);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					stream = entity.getContent();
					String streamString = IOUtils.toString(stream, "UTF-8");
					return streamString;
				}
			} catch (Exception e) {
				log.warn("Could not fetch results from OneDrive API." + e.getMessage());
			}
		} catch (URISyntaxException e) {
			log.error("Incorrect OneDrive API url syntax.", e);
		}

		return null;
	}

}
