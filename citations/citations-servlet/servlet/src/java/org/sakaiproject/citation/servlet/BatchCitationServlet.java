/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
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

package org.sakaiproject.citation.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.Schema;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.Validator;

/**
 * 
 *
 */
@Slf4j
public class BatchCitationServlet extends CitationServlet
{
	/**
	 * respond to an HTTP GET request
	 * 
	 * @param req
	 *        HttpServletRequest object with the client request
	 * @param res
	 *        HttpServletResponse object back to the client
	 * @exception ServletException
	 *            in case of difficulties
	 * @exception IOException
	 *            in case of difficulties
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
	    super.doGet(req, res);
	}

	/**
	 * 
	 * @param req
	 *        HttpServletRequest object with the client request
	 * @param res
	 *        HttpServletResponse object back to the client
	 * @exception ServletException
	 *            in case of difficulties
	 * @exception IOException
	 *            in case of difficulties
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// process any login that might be present
		basicAuth.doLogin(req);
		
		// catch the login helper posts
		String option = req.getPathInfo();
		String[] parts = option.split("/");
		
		if ((parts.length == 2) && ((parts[1].equals("login"))))
		{
			doLogin(req, res, null);
		}

		else if (req.getParameter("batch_urls") == null)
		{
		    // There is no POST handling at the base, so just throw here
		    // If there was some single-URL handling, we could call super.doPost
		    sendError(res, HttpServletResponse.SC_NOT_FOUND);
		}
		else
		{
			setupResponse(req, res);
			ContentResource resource = null;
			try {
				ParameterParser paramParser = (ParameterParser) req
					.getAttribute(ATTR_PARAMS);
				resource = findResource(paramParser, option);

				ArrayList<Citation> citations = new ArrayList<Citation>();
				ArrayList<String> failures = new ArrayList<String>();
				String[] urls = req.getParameterValues("url[]");
				if (urls != null && urls.length > 0) {
					for (String url : urls) {
						//decode POSTed URL
						String decodedUrl = URLDecoder.decode(url);
						Map<String, String[]> params = getUrlParameters(decodedUrl);
						OpenUrlRequest wrappedReq = new OpenUrlRequest(req, params);
						Citation citation = findOpenUrlCitation(wrappedReq);
						if (citation != null) {
							citations.add(citation);
							addCitation(resource, citation);
						}
						else {
							failures.add(url);
						}
					}
				}

				// set the success flag
				setVmReference("success", citations.size() > 0, req);

				if (citations.size() > 0) {
					setVmReference( "citations", citations, req );
					setVmReference("topRefresh", Boolean.TRUE, req ); // TODO
					String resourceUuid = this.contentService.getUuid(resource.getId());
					setVmReference( "resourceId", resourceUuid , req );
				} else {
					// return failure
					setVmReference("error", rb.getString("error.notfound"), req);
				}
			} catch (IdUnusedException iue) {
				setVmReference("error", rb.getString("error.noid"), req);
			} catch (ServerOverloadException e) {
				setVmReference("error", rb.getString("error.unavailable"), req);
			} catch (PermissionException e) {
				setVmReference("error", rb.getString("error.permission"), req);
			}
			// Set near end so we always have something
			setVmReference("titleArgs",  new String[]{ getCollectionTitle(resource) }, req);
			
			setVmReference("openUrlLabel", configurationService.getSiteConfigOpenUrlLabel(), req);
			setVmReference("titleProperty", Schema.TITLE, req);
			// validator
			setVmReference("xilator", new Validator(), req);

			// return the servlet template
			includeVm( COMPACT_TEMPLATE, req, res );
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doDelete(req, res);
	}

	// This is a little adapter to return parsed values rather than the raw
	// ones supplied in the real request. This is to avoid needing to modify
	// the existing parsing code in citations-impl (OpenURLServiceImpl,
	// InlineHttpTransport, etc.). We parse the real POST body and supply these
	// faked requests to CitationService.addCitation. Each one looks like a GET
	// request with the decoded parameters on the query string as a standard
	// Open URL 1.0 request would appear.

	public class OpenUrlRequest extends HttpServletRequestWrapper {
		private Map<String, String[]> openUrlParams;

		private StringBuilder queryString;

		public OpenUrlRequest(HttpServletRequest request, Map<String, String[]> openUrlParams) {
			super(request);
			this.openUrlParams = openUrlParams;

			queryString = new StringBuilder();
			for (String key : openUrlParams.keySet()) {
				if (queryString.length() > 0) {
					queryString.append("&");
				}
				boolean first = true;
				for (String val : openUrlParams.get(key)) {
					if (!first) {
						queryString.append("&");
						first = false;
					}
					try {
						queryString.append(URLEncoder.encode(key, "UTF-8"));
						queryString.append("=");
						queryString.append(URLEncoder.encode(val, "UTF-8"));
					} catch (UnsupportedEncodingException uee) {
						// These should really never happen since UTF-8 is the W3C advised encoding
						log.warn("Error encoding key/value pairs for OpenURL 1.0. [" + key + " => " + val + "] -- " + uee.getLocalizedMessage());
					}
				}
			}
		}

		public String getMethod() {
			return "GET";
		}

		public String getQueryString() {
			return queryString.toString();
		}

		public Map<String, String[]> getParameterMap() {
			return Collections.unmodifiableMap(openUrlParams);
		}

		public String getParameter(String name) {
			String[] vals = openUrlParams.get(name);
			if (vals != null && vals.length > 0) {
				return vals[0];
			}
			return null;
		}

		public Enumeration<String> getParameterNames() {
			return Collections.enumeration(Collections.unmodifiableSet(openUrlParams.keySet()));
		}

		public String[] getParameterValues(String name) {
			String[] val = openUrlParams.get(name);
			if (val != null) {
				ArrayList list = new ArrayList(Arrays.asList(val));
				ArrayList copy = new ArrayList(list);
				return (String[]) copy.toArray(new String[] {});
			}
			return null;
		}
	}
	
	public static Map<String, String[]> getUrlParameters(String url) throws UnsupportedEncodingException {
		Map<String, List<String>> params = new HashMap<String, List<String>>();
		if (url != null) {
			for (String param : url.split("&")) {
				String pair[] = param.split("=");
				String key = URLDecoder.decode(pair[0], "UTF-8");
				String value = "";
				if (pair.length > 1) {
					value = URLDecoder.decode(pair[1], "UTF-8");
				}
				List<String> values = params.get(key);
				if (values == null) {
					values = new ArrayList<String>();
					params.put(key, values);
				}
				values.add(value);
			}
		}

		Map<String, String[]> ret = new HashMap<String, String[]>();
		for (String k : params.keySet()) {
			ret.put(k, (String[]) params.get(k).toArray(new String[] {}));
		}
		return ret;
	}

}
