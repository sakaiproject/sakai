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
 * Created on 20 Jun 2008
 */
package org.sakaiproject.rsf.copies;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Copies of definitions in the utility class org.sakaiproject.util.Web which
 * are used within SakaiRSF. Copied here to avoid dependency risk of concrete
 * JAR dependence from non-Sakai code.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * 
 */

public class Web {

	/**
	 * Compute the URL that would return to this server based on the current
	 * request. Note: this method is duplicated in the kernel/request
	 * RequestFilter.java
	 * 
	 * @param req
	 *            The request.
	 * @return The URL back to this server based on the current request.
	 */
	public static String serverUrl(HttpServletRequest req) {
		String transport = null;
		int port = 0;
		boolean secure = false;

		// if force.url.secure is set (to a https port number), use https and
		// this port
		String forceSecure = System.getProperty("sakai.force.url.secure");
		int forceSecureInt = NumberUtils.toInt(forceSecure);
		if (forceSecureInt > 0 && forceSecureInt <= 65535) {
			transport = "https";
			port = forceSecureInt;
			secure = true;
		}
		// otherwise use the request scheme and port
		else {
			transport = req.getScheme();
			port = req.getServerPort();
			secure = req.isSecure();
		}

		StringBuilder url = new StringBuilder();
		url.append(transport);
		url.append("://");
		url.append(req.getServerName());
		if (((port != 80) && (!secure)) || ((port != 443) && secure)) {
			url.append(":");
			url.append(port);
		}

		return url.toString();
	}

	/**
	 * Return a string based on value that is safe to place into a javascript /
	 * html identifier: anything not alphanumeric change to 'x'. If the first
	 * character is not alphabetic, a letter 'i' is prepended.
	 * 
	 * @param value
	 *            The string to escape.
	 * @return value fully escaped using javascript / html identifier rules.
	 */
	public static String escapeJavascript(String value) {
		if (value == null || "".equals(value))
			return "";
		try {
			StringBuilder buf = new StringBuilder();

			// prepend 'i' if first character is not a letter
			if (!java.lang.Character.isLetter(value.charAt(0))) {
				buf.append("i");
			}

			// change non-alphanumeric characters to 'x'
			for (int i = 0; i < value.length(); i++) {
				char c = value.charAt(i);
				if (!java.lang.Character.isLetterOrDigit(c)) {
					buf.append("x");
				} else {
					buf.append(c);
				}
			}

			String rv = buf.toString();
			return rv;
		} catch (Exception e) {
			return value;
		}
	}

}
