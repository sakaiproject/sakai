/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.user.cover.AuthenticationManager;


/**
 * This is implemented in a filter, since most httpclients (i.e. non browser
 * clients) don't know what to do with a redirect.
 * 
 * There are 2 mechanisms for selecting basic authentication. 1. The client is
 * not a browser as reported by the BasicAuthFilter.isBrowser method. 2. The
 * user requested basic auth in the URL and the
 * BasicAuthFilter.requestedBasicAuth confirms this.
 * 
 * in sakai.properties if allow.basic.auth.login = true, then this feature is
 * enabled in BasicAuthFilter, the determination of non browser clients is
 * driven by matching user agent headers against a sequence of regex patterns.
 * These are defined in BasicAuthFilter with the form if the pattern matches a
 * browser 1pattern or if it does not match 0pattern
 * 
 * Additional patterns may be added to sakai.properties as a multiple string
 * property against login.browser.user.agent
 * 
 * The list is matched in order, the first match found being definitive. If no
 * match is found, then the client is assumed to be a browser.
 * 
 * e.g. if itunes was not listed as a client, either:
 * 
 * Add
 * 
 *    login.browser.user.agent.count=1
 *    login.browser.user.agent.1=0itunes.*
 * 
 * to sakai.properties, or
 * 
 * Add __auth=basic to the end of the url, e.g.
 * 
 *    http://localhost:8080/access/wiki/123-1231-32123-132123/-.20.rss?someparam=someval&__auth=basic
 * 
 * This string is available in BasicAuthFilter.BASIC_AUTH_LOGIN_REQUEST
 * 
 */
@Slf4j
public class BasicAuth {

	/**
	 * The query parameter and value that indicates the request will want basic
	 * auth if required
	 */
	public static final String BASIC_AUTH_LOGIN_REQUEST = "__auth=basic";

	public static Pattern[] patterns = null;

	private static String[] match;

	/**
	 * The default set of UserAgent patterns to force basic auth with
	 */
	private static String[] matchPatterns = { 
			"0.*Thunderbird.*", "1Mozilla.*", 
			"0i[tT]unes.*", "0Jakarta Commons-HttpClient.*", 
			"0.*Googlebot/2.1.*", "0[gG]oogle[bB]ot.*", "0curl.*"
	};

	/**
	 * Initialise the patterns, since some of the spring stuf may not be up when
	 * the bean is created, this is here to make certain that init is performed
	 * when spring is ready
	 * 
	 */
	public void init() {
		ArrayList<Pattern> pat = new ArrayList<Pattern>();
		ArrayList<String> mat = new ArrayList<String>();
		String[] morepatterns = null;
		try {
			morepatterns = ServerConfigurationService.getStrings("login.browser.user.agent");
		} catch (Exception ex) {

		}
		if (morepatterns != null) {
			for (int i = 0; i < morepatterns.length; i++) {
				String line = morepatterns[i];
				// line shouldn't be null (API contract), but it might be empty.
				if (line != null && line.length() > 0) {
					String check = line.substring(0, 1);
					mat.add(check);
					line = line.substring(1);
					pat.add(Pattern.compile(line));
				}
			}
		}
		for (int i = 0; i < matchPatterns.length; i++) {
			String line = matchPatterns[i];
			String check = line.substring(0, 1);
			mat.add(check);
			line = line.substring(1);
			pat.add(Pattern.compile(line));
		}

		patterns = new Pattern[pat.size()];
		patterns = (Pattern[]) pat.toArray(patterns);
		match = new String[mat.size()];
		match = (String[]) mat.toArray(match);
	}

	/**
	 * If this method returns true, the user agent is a browser
	 * 
	 * @param header
	 * @return
	 */
	protected boolean isBrowser(String userAgentHeader) {
		if (userAgentHeader == null)
			return false;

		if (patterns != null) {
			for (int i = 0; i < patterns.length; i++) {
				Matcher m = patterns[i].matcher(userAgentHeader);
				if (m.matches()) {
					return "1".equals(match[i]);
				}
			}
			return true;
		}
		return true;
	}

	/**
	 * This method looks at the returnUrl and if there is a request parameter in
	 * the URL requesting basic authentication, this method returns true
	 * 
	 * @param returnUrl
	 * @return
	 */
	protected boolean requestedBasicAuth(HttpServletRequest request) {
		String queryString = request.getQueryString();
		if (queryString == null) {
			return false;
		} else {
			boolean ret = (queryString.indexOf(BASIC_AUTH_LOGIN_REQUEST) != -1);
			return ret;
		}
	}

	/**
	 * Should a basic auth be used
	 * @param req
	 * @return
	 */
	protected boolean doBasicAuth(HttpServletRequest req) {
		boolean allowBasicAuth = ServerConfigurationService.getBoolean(
				"allow.basic.auth.login", false);

		if (allowBasicAuth) {
			if (requestedBasicAuth(req)
					|| !isBrowser(req.getHeader("User-Agent"))) {
				allowBasicAuth = true;
			} else {
				allowBasicAuth = false;
			}
		}

		return allowBasicAuth;
	}

	/**
	 * Perform a login based on the headers, if they are not present or it fails do nothing
	 * @param req
	 * @return
	 * @throws IOException
	 */
	public boolean doLogin(HttpServletRequest req) throws IOException {

		if (doBasicAuth(req)) {
		
			String auth;
			auth = req.getHeader("Authorization");
			
			Evidence e = null;
			try {
				if (auth != null) {
					auth = auth.trim();
					if (auth.startsWith("Basic ")) {
						auth = auth.substring(6).trim();
						auth = new String(Base64.decodeBase64(auth.getBytes("UTF-8")));
						int colon = auth.indexOf(":");
						if (colon != -1) {
							String eid = auth.substring(0, colon);
							String pw = auth.substring(colon + 1);
							if (eid.length() > 0 && pw.length() > 0) {
								e = new IdPwEvidence(eid, pw, req.getRemoteAddr());
							}
						}
					}
				}
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}

			// authenticate
			try {
				if (e == null) {
					throw new AuthenticationException("missing required fields");
				}

				Authentication a = AuthenticationManager.authenticate(e);

				// login the user
				if (UsageSessionService.login(a, req)) {
					return true;
				} else {
					return false;
				}
			} catch (AuthenticationException ex) {
				log.error(ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}

	/**
	 * Emit the basic auth headers and a 401
	 * @param req
	 * @param res
	 * @return
	 * @throws IOException
	 */
	public boolean doAuth(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		if (doBasicAuth(req)) {
		    String uiService = ServerConfigurationService.getString("ui.service", "Sakai");
			res.addHeader("WWW-Authenticate", "Basic realm=\"" + uiService + "\"");
			res.sendError(HttpServletResponse.SC_UNAUTHORIZED,
					"Authorization Required");
			return true;
		}
		return false;

	}

}
