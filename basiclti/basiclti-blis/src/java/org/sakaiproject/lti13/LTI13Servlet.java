/**
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
 * Author: Charles Severance <csev@umich.edu>
 */
package org.sakaiproject.lti13;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.lti.api.LTIService;
import org.tsugi.basiclti.BasicLTIUtil;
import org.tsugi.lti13.LTI13KeySetUtil;
import org.tsugi.lti13.LTI13Util;

/**
 *
 */
@SuppressWarnings("deprecation")
@Slf4j
public class LTI13Servlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String APPLICATION_JSON = "application/json";
	private static final String ERROR_DETAIL = "X-Sakai-LTI13-Error-Detail";
	protected static LTIService ltiService = null;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if (ltiService == null) {
			ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
		}

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI(); // /imsblis/lti13/keys
		// String launch_url = request.getParameter("launch_url");

		String[] parts = uri.split("/");

		// /imsblis/lti13/keyset/42

		if (parts.length == 5 && "keyset".equals(parts[3])) {
			PrintWriter out = null;
			String client_id = parts[4];
			Long toolKey = SakaiBLTIUtil.getLongKey(client_id);
			String siteId = null;  // Full bypass mode
			Map<String, Object> tool = null;
			if (toolKey >= 0) {
				tool = ltiService.getToolDao(toolKey, siteId);
			}

			if (tool == null) {
				response.setHeader(ERROR_DETAIL, "Could not load keyset for client");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				log.error("Could not load keyset for client_id={}", client_id);
				return;
			}

			String publicSerialized = BasicLTIUtil.toNull((String) tool.get(LTIService.LTI13_PLATFORM_PUBLIC));
			if (publicSerialized == null) {
				response.setHeader(ERROR_DETAIL, "Client has no public key");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				log.error("Client_id={} has no public key", client_id);
				return;
			}

			Key publicKey = LTI13Util.string2PublicKey(publicSerialized);
			if (publicKey == null) {
				response.setHeader(ERROR_DETAIL, "Client public key deserialization error");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				log.error("Client_id={} deserialization error", client_id);
				return;
			}

			// Cast should work :)
			RSAPublicKey rsaPublic = (RSAPublicKey) publicKey;

			String keySetJSON = null;
			try {
				keySetJSON = LTI13KeySetUtil.getKeySetJSON(rsaPublic);
			} catch (NoSuchAlgorithmException ex) {
				response.setHeader(ERROR_DETAIL, "NoSuchAlgorithmException");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				log.error("Client_id={} NoSuchAlgorithmException", client_id);
				return;
			}

			try {
				out = response.getWriter();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return;
			}

			response.setContentType(APPLICATION_JSON);
			try {
				out.println(keySetJSON);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			return;
		}

		response.setHeader(ERROR_DETAIL, "Invalid request");
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Nothing here yet :)
	}

}
