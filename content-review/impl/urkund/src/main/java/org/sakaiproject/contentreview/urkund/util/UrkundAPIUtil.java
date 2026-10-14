/**
 * Copyright (c) 2017 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.contentreview.urkund.util;

import java.io.IOException;
import java.util.Base64;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.sakaiproject.contentreview.urkund.UrkundSubmission;

/**
 * This is a utility class for wrapping the Rest calls to the Urkund Service
 *
 */
@Slf4j
public class UrkundAPIUtil {
	public static String postDocument(String baseUrl, String receiverAddress, String externalId, UrkundSubmission submission, String urkundUsername, String urkundPassword, int timeout){
			String ret = null;
			
			RequestConfig.Builder requestBuilder = RequestConfig.custom();
			requestBuilder = requestBuilder.setConnectTimeout(Timeout.ofMilliseconds(timeout));
			requestBuilder = requestBuilder.setConnectionRequestTimeout(Timeout.ofMilliseconds(timeout));
			
			HttpClientBuilder builder = HttpClientBuilder.create();     
			builder.setDefaultRequestConfig(requestBuilder.build());
			try (CloseableHttpClient httpClient = builder.build()) {
				
				
				HttpPost httppost = new HttpPost(baseUrl+"submissions/"+receiverAddress+"/"+externalId);
				//------------------------------------------------------------
				EntityBuilder eBuilder = EntityBuilder.create();
				eBuilder.setBinary(submission.getContent());

				httppost.setEntity(eBuilder.build());
				//------------------------------------------------------------
				if(StringUtils.isNotBlank(urkundUsername) && StringUtils.isNotBlank(urkundPassword)) {
					addAuthorization(httppost, urkundUsername, urkundPassword);
				}
				//------------------------------------------------------------
				httppost.addHeader("Accept", "application/json");
				httppost.addHeader("Content-Type", submission.getMimeType());
				httppost.addHeader("Accept-Language", submission.getLanguage());
				httppost.addHeader("x-urkund-filename", submission.getFilenameEncoded());
				httppost.addHeader("x-urkund-submitter", submission.getSubmitterEmail());
				httppost.addHeader("x-urkund-anonymous", Boolean.toString(submission.isAnon()));
				httppost.addHeader("x-urkund-subject", submission.getSubject());
				httppost.addHeader("x-urkund-message", submission.getMessage());
				//------------------------------------------------------------

				try (CloseableHttpResponse response = httpClient.execute(httppost)) {
					HttpEntity resEntity = response.getEntity();
					if (resEntity != null) {
						ret = EntityUtils.toString(resEntity);
						EntityUtils.consume(resEntity);
					}
				}

			} catch (IOException | ParseException e) {
				log.error("ERROR uploading File : ", e);
			}
			
			
			return ret;
	}
	
	public static String getFileInfo(String baseUrl, String externalId, String receiverAddress, String urkundUsername, String urkundPassword, int timeout){
		String ret = null;
		RequestConfig.Builder requestBuilder = RequestConfig.custom();
		requestBuilder = requestBuilder.setConnectTimeout(Timeout.ofMilliseconds(timeout));
		requestBuilder = requestBuilder.setConnectionRequestTimeout(Timeout.ofMilliseconds(timeout));
		
		HttpClientBuilder builder = HttpClientBuilder.create();     
		builder.setDefaultRequestConfig(requestBuilder.build());
		try (CloseableHttpClient httpClient = builder.build()) {
			HttpGet httpget = new HttpGet(baseUrl+"submissions/"+receiverAddress+"/"+externalId);
			
			//------------------------------------------------------------
			if(StringUtils.isNotBlank(urkundUsername) && StringUtils.isNotBlank(urkundPassword)) {
				addAuthorization(httpget, urkundUsername, urkundPassword);
			}
			//------------------------------------------------------------
			httpget.addHeader("Accept", "application/json");
			//------------------------------------------------------------
			
			try (CloseableHttpResponse response = httpClient.execute(httpget)) {
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					ret = EntityUtils.toString(resEntity);
					EntityUtils.consume(resEntity);
				}
			}
			
		} catch (IOException | ParseException e) {
			log.error("ERROR getting File Info : ", e);
		}
		return ret;
	}
	

	private static void addAuthorization(HttpUriRequestBase request, String user, String pwd) {
		try {
			//String authHeaderStr = String.format("Basic %s", new String(Base64.encodeBase64(String.format("%s:%s", user, pwd).getBytes()), "UTF-8"));
			String authHeaderStr = String.format("Basic %s", new String(Base64.getEncoder().encode(String.format("%s:%s", user, pwd).getBytes("UTF-8"))));
			request.addHeader("Authorization", authHeaderStr);
		} catch(Exception e){
			log.error("ERROR adding authorization header : ", e);
		}
	}

}
