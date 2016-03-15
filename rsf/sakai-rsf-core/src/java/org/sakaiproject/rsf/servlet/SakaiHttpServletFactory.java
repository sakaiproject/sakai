/**
 * Copyright © 2005, CARET, University of Cambridge
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
/*
 * Created on Dec 2, 2005
 */
package org.sakaiproject.rsf.servlet;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;

import uk.org.ponder.rsac.servlet.StaticHttpServletFactory;
import uk.org.ponder.util.Logger;

public class SakaiHttpServletFactory extends StaticHttpServletFactory {
	private HttpServletRequest request;
	private HttpServletResponse response;

	public void setHttpServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setHttpServletResponse(HttpServletResponse response) {
		this.response = response;
	}

	private String entityref = null;

	public void setEntityReference(String entityref) {
		this.entityref = entityref;
	}

	/**
	 * Since it seems we can no longer apply servlet mappings in our web.xml as
	 * of Sakai 2.0, we perform this feat manually, using the resourceurlbase
	 * init parameter, and this string which represents the offset path for
	 * resource handled by RSF. Any paths falling outside this will be treated
	 * as static, and sent to the resourceurlbase.
	 */
	public static final String FACES_PATH = "faces";
	private String extrapath;

	// Use old-style initialisation semantics since this bean is populated by
	// inchuck.
	private boolean initted = false;

	private void checkInit() {
		if (!initted) {
			initted = true;
			init();
		}
	}

	public void init() {
		// only need to perform request demunging if this has not come to us
		// via the AccessRegistrar.
		if (entityref.equals("")) {
			extrapath = "/" + computePathInfo(request);
			final StringBuffer requesturl = request.getRequestURL();
			// now handled with implicitNullPathRedirect in RSF proper
			// if (extrapath.equals("")) {
			// extrapath = defaultview;
			// requesturl.append('/').append(FACES_PATH).append(extrapath);
			// }

			HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request) {
				public String getPathInfo() {
					return extrapath;
				}

				public StringBuffer getRequestURL() {
					StringBuffer togo = new StringBuffer();
					togo.append(requesturl);
					return togo;
				}
			};
			request = wrapper;
		}
	}

	public HttpServletRequest getHttpServletRequest() {
		checkInit();
		return request;
	}

	public HttpServletResponse getHttpServletResponse() {
		checkInit();
		return response;
	}

	private static final String PORTAL_TOOL = "/portal/tool/";

	public static String computePathInfo(HttpServletRequest request) {
		String requesturl = request.getRequestURL().toString();
		String extrapath = request.getPathInfo();
		if (extrapath == null) {
			extrapath = "";
		}
		if (extrapath.length() > 0 && extrapath.charAt(0) == '/') {
			extrapath = extrapath.substring(1);
		}
		int earlypos = requesturl.indexOf('/' + FACES_PATH);
		// within a Sakai helper, the request wrapper is even FURTHER screwed up
		if ("".equals(extrapath) && earlypos >= 0) {
			extrapath = requesturl.substring(earlypos + 1);
		}
		// Now, the Sakai "PathInfo" is *longer* than we would expect were we
		// mapped properly, since it will include what we call the "FACES_PATH",
		// as inserted there by RequestParser when asked for the baseURL.
		if (extrapath.startsWith(FACES_PATH)) {
			extrapath = extrapath.substring(FACES_PATH.length());
		}
		// The Websphere dispatching environment is entirely broken, and never
		// gives us
		// any information on pathinfo. Make our best attempt to guess what it
		// should be
		// in certain common situations.
		if ("websphere".equals(ServerConfigurationService.getString("servlet.container"))) {
			try { // Resolve RSF-129. Override all previous decisions if we can
					// detect a global
					// /portal/tool request
				URL url = new URL(requesturl);
				String path = url.getPath();
				if (path.startsWith(PORTAL_TOOL)) {
					int nextslashpos = path.indexOf('/', PORTAL_TOOL.length() + 1);
					if (nextslashpos != -1) {
						extrapath = path.substring(nextslashpos + 1);
						int furtherslashpos = extrapath.indexOf('/');
						int helperpos = extrapath.indexOf(".helper");
						if (helperpos != -1) {
							if (helperpos < furtherslashpos) {
								extrapath = extrapath.substring(furtherslashpos + 1);
							} else if (furtherslashpos == -1) {
								extrapath = "";
							}
						}
					} else {
						extrapath = "";
					}
				}
			} catch (MalformedURLException e) {
				Logger.log.info("Malformed input request URL", e);
			}
		}

		Logger.log.info("Beginning ToolSinkTunnelServlet service with requestURL of " + requesturl
				+ " and extra path of " + extrapath);

		return extrapath;
	}

}
