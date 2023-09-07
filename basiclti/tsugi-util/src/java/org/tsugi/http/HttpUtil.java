/*
 * Copyright (c) 2018- Charles R. Severance
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

import java.util.Enumeration;
import java.util.Map;
import java.util.List;
import java.util.Date;
import java.time.Instant;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.apache.commons.httpclient.util.DateUtil;

/**
 * Some Tsugi Utility code for to make using Http easier to use.
 */
@SuppressWarnings("deprecation")
@Slf4j
public class HttpUtil {

	private static final Pattern p = Pattern.compile("<(.*)>; *rel=\"(.*)\"");

	// https://stackoverflow.com/questions/18944302/how-do-i-print-the-content-of-httprequest-request
	public static void printHeaders(HttpServletRequest request) {
		Enumeration<String> headerNames = request.getHeaderNames();
		System.out.println("Headers for "+request.getRequestURI());
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			System.out.println("Header Name - " + headerName + ", Value - " + request.getHeader(headerName));
		}
	}

	public static void printParameters(HttpServletRequest request) {
		System.out.println("Parameters for "+request.getRequestURI());
		Enumeration<String> params = request.getParameterNames();
		while (params.hasMoreElements()) {
			String paramName = params.nextElement();
			System.out.println("Parameter Name - " + paramName + ", Value - " + request.getParameter(paramName));
		}
	}

	public static String getCookie(HttpServletRequest request, String lookup) {
		if ( request == null || lookup == null ) return null;

		// https://stackoverflow.com/questions/11047548/getting-cookie-in-servlet
		Cookie[] cookies = request.getCookies();
		if ( cookies == null ) return null;
		for (int i = 0; i < cookies.length; i++) {
			Cookie cookie=cookies[i];
			String cookieName = cookie.getName();
			String cookieValue = cookie.getValue();
			if ( StringUtils.isEmpty(cookieName) ) continue;
			if ( cookieName.equalsIgnoreCase(lookup) ) {
				return cookieValue;
			}
		}
		return null;
	}

	public static String augmentGetURL(String url, Map<String, String> data) {

		if ( data == null || data.size() < 1 ) return url;

		var builder = new StringBuilder();
		builder.append(url);
		boolean questionMark = url.contains("?");

		for (Map.Entry<String, String> entry : data.entrySet()) {
			if (! questionMark ) {
				builder.append("?");
			} else {
				builder.append("&");
			}
			builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
			builder.append("=");
			builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
		}
		return builder.toString();
	}

	// https://www.imsglobal.org/spec/lti-nrps/v2p0#limit-query-parameter
	// All values of Link: [<http://localhost:8080/imsblis/lti13/namesandroles/8b206920-d4d9-4df5-9aba-bd100a2a0af0?start=2&limit=2>; rel="next"]
	public static String extractLinkByRel(List<String> allValuesOfLink, String rel)
	{
		if ( rel == null || allValuesOfLink == null ) return null;
		for(String value: allValuesOfLink) {
			Matcher m = p.matcher(value);
			if (m.find() && rel.equals(m.group(2)) ) {
				return m.group(1);
			}
		}

		// Some LMSs (cough, cough) return one long string with commas and no space
		// This confuses the java.util.net.HttpHeaders.allValues() so we compensate

		if ( allValuesOfLink.size() != 1 ) return null;

		String [] pieces = allValuesOfLink.get(0).split(",");
		for(String value: pieces) {
			value = value.trim();

			Matcher m = p.matcher(value);
			if (m.find() && rel.equals(m.group(2)) ) {
				return m.group(1);
			}
		}
		return null;  // Foiled again
	}

	// Retry-After: Date: Wed, 21 Oct 2015 07:28:00 GMT
	//
	public static Instant getInstantFromHttp(String headerDate)
	{
		// TODO: Work this out
		return null;
	}
}
