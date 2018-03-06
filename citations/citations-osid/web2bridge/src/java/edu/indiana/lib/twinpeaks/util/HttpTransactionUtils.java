/**********************************************************************************
*
 * Copyright (c) 2003, 2004, 2007, 2008 The Sakai Foundation
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
package edu.indiana.lib.twinpeaks.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * HTTP utilites
 */
@Slf4j
public class HttpTransactionUtils
{

  private HttpTransactionUtils() {
  }
  /**
   * Default HTTP character set
   */
  public static final String DEFAULTCS	= "ISO-8859-1";

	/*
	 * Parameter handling
	 */

	/**
	 * Format one HTTP parameter
	 * @param name Parameter name
	 * @param value Parameter value (URLEncoded using default chracter set)
	 * @return Parameter text (ampersand+name=url-encoded-value)
	 */
  public static String formatParameter(String name, String value) {
  	return formatParameter(name, value, "&", DEFAULTCS);
  }

	/**
	 * Format one HTTP parameter
	 * @param name Parameter name
	 * @param value Parameter value (will be URLEncoded)
	 * @param separator Character to separate parameters
	 * @param cs Character set specification (utf-8, etc)
	 * @return Parameter text (separator+name=url-encoded-value)
	 */
  public static String formatParameter(String name, String value,
  																		 String separator, String cs) {
  	StringBuilder parameter = new StringBuilder();

    if (!StringUtils.isNull(value)) {

	   	parameter.append(separator);
 		 	parameter.append(name);
 			parameter.append('=');

			try {
  	 		parameter.append(URLEncoder.encode(value, cs));
  	 	} catch (UnsupportedEncodingException exception) {
  	 		throw new IllegalArgumentException("Invalid character set: \""
  	 																			 + cs
  	 																			 + "\"");
  		}
  	}
  	return parameter.toString();
  }

	/*
   * HTTP status values
   */

 	/**
 	 * Informational status?
 	 * @return true if so
 	 */
	public static boolean isHttpInfo(int status) {
		return ((status / 100) == 1);
	}

 	/**
 	 * HTTP redirect?
 	 * @return true if so
 	 */
	public static boolean isHttpRedirect(int status) {
		return ((status / 100) == 3);
	}

 	/**
 	 * Success status?
 	 * @return true if so
 	 */
	public static boolean isHttpSuccess(int status) {
		return ((status / 100) == 2);
	}

 	/**
 	 * Error in request?
 	 * @return true if so
 	 */
	public static boolean isHttpRequestError(int status) {
		return ((status / 100) == 4);
	}

 	/**
 	 * Server error?
 	 * @return true if so
 	 */
	public static boolean isHttpServerError(int status) {
		return ((status / 100) == 5);
	}

 	/**
 	 * General "did an error occur"?
 	 * @return true if so
 	 */
	public static boolean isHttpError(int status) {
		return isHttpRequestError(status) || isHttpServerError(status);
	}

	/**
	 * Set up a simple Map of HTTP request parameters (assumes no duplicate names)
	 * @param request HttpServletRequest object
	 * @return Map of name=value pairs
	 */
	public static Map getAttributesAsMap(HttpServletRequest request) {
		Enumeration enumeration		= request.getParameterNames();
		HashMap			map						= new HashMap();

		while (enumeration.hasMoreElements()) {
			String name	= (String) enumeration.nextElement();

			map.put(name, request.getParameter(name));
		}
		return map;
	}

  /**
   * Format a base URL string ( protocol://server[:port] )
   * @param url URL to format
   * @return URL string
   */
  public static String formatUrl(URL url) throws MalformedURLException {
  	return formatUrl(url, false);
  }

  /**
   * Format a base URL string ( protocol://server[:port][/file-specification] )
   * @param url URL to format
   * @param preserveFile Keep the /directory/filename portion of the URL?
   * @return URL string
   */
  public static String formatUrl(URL url, boolean preserveFile)
  																				throws MalformedURLException {
    StringBuilder	result;
		int						port;

		result = new StringBuilder(url.getProtocol());

    result.append("://");
    result.append(url.getHost());

    if ((port = url.getPort()) != -1) {
    	result.append(":");
    	result.append(String.valueOf(port));
   	}

   	if (preserveFile) {
   		String file = url.getFile();

   		if (file != null) {
   			result.append(file);
   		}
   	}
    return result.toString();
	}

	/**
	 * Pull the server [and port] from a URL specification
	 * @param url URL string
	 * @return server[:port]
	 */
	public static String getServer(String url) {
    String  server  = url;
    int     protocol, slash;

    if ((protocol = server.indexOf("//")) != -1) {
      if ((slash = server.substring(protocol + 2).indexOf("/")) != -1) {
        server = server.substring(0, protocol + 2 + slash);
      }
    }
 		return server;
 	}

	/*
	 * urlEncodeParameters(): URL component specifications
	 */

	/**
	 * protocol://server
	 */
	public static final String	SERVER				= "server";
	/**
	 * /file/specification
	 */
	public static final String	FILE					= "file";
	/**
	 * ?parameter1=value1&parameter2=value2
	 */
	public static final String	PARAMETERS		= "parameters";
	/**
	 * /file/specification?parameter1=value1&parameter2=value2
	 */
	public static final String	FILEANDPARAMS	= "fileandparameters";

  /**
   * Fetch a component from a URL string
   * @param url URL String
   * @param component name (one of server, file, parameters, fileandparameters)
   * @return URL component string (null if none)
   */
  public static String getUrlComponent(String url, String component)
  																		 throws MalformedURLException {
		String	file;
		int			index;

		if (component.equalsIgnoreCase(SERVER)) {
			return getServer(url);
		}

		if (!component.equalsIgnoreCase(FILE) &&
		    !component.equalsIgnoreCase(PARAMETERS) &&
		    !component.equalsIgnoreCase(FILEANDPARAMS)) {
			throw new IllegalArgumentException(component);
		}

		file = new URL(url).getFile();
		if (file == null) {
			return null;
		}
		/*
		 * Fetch file and parameters?
		 */
		if (component.equalsIgnoreCase(FILEANDPARAMS)) {
			return file;
		}
		/*
		 * File portion only?
		 */
		index	= file.indexOf('?');

		if (component.equalsIgnoreCase(FILE)) {
			switch (index) {
				case -1:				// No parameters
					return file;
				case 0:					// Only parameters (no file)
					return null;
				default:
					return file.substring(0, index);
			}
		}
		/*
		 * Isolate parameters
		 */
		return (index == -1) ? null : file.substring(index);
	}

	/**
	 * URLEncode parameter names and values
	 * @param original Full URL specification (http://example.com/xxx?a=b&c=d)
	 * @return Original URL with (possibly) encoded parameters
	 */
	public static String urlEncodeFullUrl(String original) {
		StringBuilder 	encoded;
		String				base, file, params;

		try {
			base		= getUrlComponent(original, SERVER);
			file		= getUrlComponent(original, FILE);
			params	= getUrlComponent(original, PARAMETERS);

		} catch (MalformedURLException exception) {
			log.warn("Invalid URL provided: " + original);
			return original;
		}

		if (StringUtils.isNull(params)) {
			return original;
		}

		encoded = new StringBuilder();
		encoded.append(base);

		if (!StringUtils.isNull(file)) {
			encoded.append(file);
		}
		encoded.append(urlEncodeParameters(params));
		return encoded.toString();
	}


	/**
	 * URLEncode parameter names and values
	 * @param original Original parameter list (?a=b&c=d)
	 * @return Possibly encoded parameter list
	 */
	public static String urlEncodeParameters(String original) {
		StringBuilder 	encoded	= new StringBuilder();

		for (int i = 0; i < original.length(); i++) {
			String c = original.substring(i, i + 1);

			if (!c.equals("&") && !c.equals("=") && !c.equals("?")) {
				c = URLEncoder.encode(c);
			}
			encoded.append(c);
		}
		return encoded.toString();
	}


  /*
   * Test
   */
  public static void main(String[] args) throws Exception {
  	String u = "http://example.com/dir1/dir2/file.html?parm1=1&param2=2";

  	log.debug("Server: {}", getUrlComponent(u, "server"));
  	log.debug("File: {}", getUrlComponent(u, "file"));
  	log.debug("Parameters: {}", getUrlComponent(u, "parameters"));
  	log.debug("File & Parameters: {}", getUrlComponent(u, "fileandparameters"));
  	log.debug("Bad: {}", getUrlComponent(u, "bad"));
  }
}
