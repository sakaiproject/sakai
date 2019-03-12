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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Some Tsugi Utility code for to make using Jackson easier to use.
 */
@SuppressWarnings("deprecation")
@Slf4j
public class HttpUtil {

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
}
