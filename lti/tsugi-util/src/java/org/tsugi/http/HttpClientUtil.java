/*
 * Copyright (c) 2020- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 */
package org.tsugi.http;

import java.lang.StringBuffer;

import java.io.InputStream;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;

import org.tsugi.http.HttpUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

/*
 *
 * A Java-11 HTTP utility based on
 *
 * https://mkyong.com/java/how-to-send-http-request-getpost-in-java/
 * https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpResponse.BodyHandlers.html
 */

@SuppressWarnings("deprecation")
@Slf4j
public class HttpClientUtil {

	public static HttpRequest setupGet(String url, Map<String, String> parameters, Map<String, String> headers, StringBuffer dbs) throws Exception {

		String getUrl = HttpUtil.augmentGetURL(url, parameters);

		if ( dbs != null ) {
			dbs.append("setupGet url ");
			dbs.append(getUrl);
			dbs.append("\n");
		}

		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create(getUrl))
				// .timeout(10)
				.header("User-Agent", "org.tsugi.http.HttpClientUtil web service request");

		if ( headers != null ) {
			if ( dbs != null && headers.size() > 0 ) {
				dbs.append("headers\n");
				dbs.append(headers.toString());
				dbs.append("\n");
			}
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				builder.setHeader(entry.getKey().toString(), entry.getValue().toString());
			}
		}

		HttpRequest request = builder.build();
		return request;
	}

	public static HttpClient getClient() {

		HttpClient httpClient = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.build();

		return httpClient;
	}

	public static HttpResponse<String> sendGet(String url, Map<String, String> parameters, Map<String, String> headers, StringBuffer dbs) throws Exception {
		HttpRequest request = setupGet(url, parameters, headers, dbs);
		HttpResponse<String> response = getClient().send(request, HttpResponse.BodyHandlers.ofString());

		if ( dbs != null ) {
			dbs.append("http status=");
			dbs.append(response.statusCode());
			dbs.append("\n");
		}

		return response;
	}

	public static HttpResponse<InputStream> sendGetStream(String url, Map<String, String> parameters, Map<String, String> headers, StringBuffer dbs) throws Exception {
		HttpRequest request = setupGet(url, parameters, headers, dbs);
		HttpResponse<InputStream> response = getClient().send(request, HttpResponse.BodyHandlers.ofInputStream());

		if ( dbs != null ) {
			dbs.append("http status=");
			dbs.append(response.statusCode());
			dbs.append("\n");
		}

		return response;
	}

	// Convenience method
	public static HttpResponse<String> sendPost(String url, Map<String, String> data, Map<String, String> headers, StringBuffer dbs) throws Exception {
		return sendBody("POST", url, data, headers, dbs);
	}

	// Convenience method
	public static HttpResponse<String> sendPost(String url, String data, Map<String, String> headers, StringBuffer dbs) throws Exception {
		return sendBody("POST", url, data, headers, dbs);
	}

	// Key/value body
	public static HttpResponse<String> sendBody(String method, String url, Map<String, String> data, Map<String, String> headers, StringBuffer dbs) throws Exception {
		HttpRequest.BodyPublisher body = buildFormDataFromMap(data, dbs);
		if ( headers == null ) headers = new HashMap<String, String>();
		if ( headers.get("Content-Type") == null ) headers.put("Content-Type", "application/x-www-form-urlencoded");
		return sendBody(method, url, body, headers, dbs);
	}

	// Straight up text body
	public static HttpResponse<String> sendBody(String method, String url, String data, Map<String, String> headers, StringBuffer dbs) throws Exception {
		if ( dbs != null && data != null && data.length() > 0 ) {
			dbs.append("sendPost data\n");
			dbs.append(StringUtils.truncate(data, 1000));
			dbs.append("\n");
		}

		HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(data);
		if ( headers == null ) headers = new HashMap<String, String>();
		return sendBody(method, url, body, headers, dbs);
	}

	public static HttpResponse<String> sendBody(String method, String url, HttpRequest.BodyPublisher body, Map<String, String> headers, StringBuffer dbs) throws Exception {

		HttpRequest.Builder builder = HttpRequest.newBuilder()
			.method(method, body)
			// .timeout(10)
			.uri(URI.create(url))
			.header("User-Agent", "org.tsugi.http.HttpClientUtil web service request");

		if ( dbs != null ) {
			dbs.append("send");
			dbs.append(method);
			dbs.append(" url ");
			dbs.append(url);
			dbs.append("\n");
		}

		if ( headers != null ) {
			if ( dbs != null && headers.size() > 0 ) {
				dbs.append("headers\n");
				dbs.append(headers.toString());
				dbs.append("\n");
			}
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				builder.setHeader(entry.getKey().toString(), entry.getValue().toString());
			}
		}

		HttpRequest request = builder.build();

		HttpClient httpClient = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

		if ( dbs != null ) {
			dbs.append("http status=");
			dbs.append(response.statusCode());
			dbs.append("\n");
		}

		return response;
	}

	private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<String, String> data, StringBuffer dbs) {
		if ( data == null || data.size() < 1 ) return null;
		var builder = new StringBuilder();
		for (Map.Entry<String, String> entry : data.entrySet()) {
			if (builder.length() > 0) {
				builder.append("&");
			}
			builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
			builder.append("=");
			builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
		}

		if ( dbs != null ) {
			dbs.append("request builder: ");
			dbs.append(builder.toString());
			dbs.append("\n");
		}

		return HttpRequest.BodyPublishers.ofString(builder.toString());
	}

}
