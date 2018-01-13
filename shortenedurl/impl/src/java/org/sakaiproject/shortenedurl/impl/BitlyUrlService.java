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
package org.sakaiproject.shortenedurl.impl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.sakaiproject.shortenedurl.api.ShortenedUrlService;

/**
 * This implementation of {@link org.sakaiproject.shortenedurl.api.ShortenedUrlService} uses bit.ly to shorten URLs
 *  
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class BitlyUrlService implements ShortenedUrlService {

	private String login;
	private String apiKey;
	private final String BITLY_API_URL = "http://api.bit.ly/v3/shorten";

	/**
	 * Shorten the give URL using the bit.ly service
	 */
	public String shorten(String url) {
		
		//check auth params are set
		checkAuth();
		
		//setup url params
		Map<String,String> params = new HashMap<String,String>();
		params.put("login", login);
		params.put("apiKey", apiKey);
		params.put("format", "txt");
		params.put("longUrl", url);
		
		//do it
		String response = doGet(BITLY_API_URL, params);
		
		//TODO add caching since the bitly API is rate limited and we want to store the key and original URL for future lookups

		
		return response;
		
		
	}

	/**
	 * Not implemented, simply calls generateShortUrl(String url)
	 */
	public String shorten(String url, boolean secure) {
		return shorten(url);
	}

	/**
	 * Not implemented and not needed. The short URL will be a bit.ly URL that will resolve to the original URL.
	 */
	public String resolve(String key) {
		return null;
	}

	public void init() {
  		log.debug("Sakai BitlyUrlService init().");
  	}
	
	/**
	 * Helper to check auth is set
	 */
	private void checkAuth() {
		if(StringUtils.isBlank(login) || StringUtils.isBlank(apiKey)){
			throw new IllegalArgumentException("Missing auth for BitlyUrlService: login=" + login + ", key=" + apiKey);
		}
	}

	/**
	 * Helper to URL encode a string
	 * @param s 	string to encode
	 * @return
	 */
	private String encode(String s){
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.getClass() + ":" + e.getMessage());
		}
		return null;
	}

	/**
	 * Makes a GET request to the given address. Any query string should be appended already.
	 * @param address	the fully qualified URL to make the request to
	 * @return
	 */
	private String doGet(String address){
		try {
			
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(address);
			HttpResponse response = httpclient.execute(httpget);
			
			//check reponse code
			StatusLine status = response.getStatusLine();
			if(status.getStatusCode() != 200) {
				log.error("Error shortening URL. Status: " + status.getStatusCode() + ", reason: " + status.getReasonPhrase());
				return null;
			}
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
			    return EntityUtils.toString(entity);
			}
			
		} catch (Exception e) {
			log.error(e.getClass() + ":" + e.getMessage());
		} 
		return null;
	}

	/**
	 * Make a GET request and append the Map of parameters onto the query string.
	 * @param address		the fully qualified URL to make the request to
	 * @param parameters	the Map of parameters, ie key,value pairs
	 * @return
	 */
	private String doGet(String address, Map<String, String> parameters){
		try {
			
			List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
			
			for (Map.Entry<String,String> entry : parameters.entrySet()) {
				queryParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			
			URI uri = URIUtils.createURI(null, address, -1, null, URLEncodedUtils.format(queryParams, "UTF-8"), null);
			
			log.info(uri.toString());
			
			return doGet(uri.toString());
		
		} catch (URISyntaxException e) {
			log.error(e.getClass() + ":" + e.getMessage());
		}
		return null;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	/*
	 * this is a placeholder until someone who understands bitly decides what the
	 * right approach is
	 */
	public boolean shouldCopy(String url) {
		return false;
	}

}
