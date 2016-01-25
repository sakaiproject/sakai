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
 * Created on 21 Nov 2006
 */
package org.sakaiproject.rsf.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.rsf.copies.Web;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

import uk.org.ponder.rsf.viewstate.BaseURLProvider;
import uk.org.ponder.rsf.viewstate.support.StaticBaseURLProvider;
import uk.org.ponder.servletutil.ServletUtil;

public class SakaiBaseURLProviderFactory implements ApplicationContextAware, FactoryBean {
	private HttpServletRequest request;

	private WebApplicationContext wac;

	public void setApplicationContext(ApplicationContext applicationContext) {
		wac = (WebApplicationContext) applicationContext;
	}

	public void setHttpServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	private String resourceurlbase;

	// Sakai is a very poor URL environment, where the return from
	// req.getRequestURL() may not actually be valid to access this server.
	// This method adjusts the protocol and port from a "correctly" computed
	// URL to be closer to reality, via the hackery from Sakai "Web" utils.
	public static String fixSakaiURL(HttpServletRequest req, String computed) {
		String serverURL = Web.serverUrl(req);
		int endprotpos = computed.indexOf("://");
		int slashpos = computed.indexOf('/', endprotpos + 3);
		return serverURL + computed.substring(slashpos);
	}

	public StaticBaseURLProvider computeBaseURLProvider(HttpServletRequest request) {
		ServletContext servletcontext = wac.getServletContext();
		// yes, these two fields are not request-scope, but not worth creating
		// a whole new class and bean file for them.
		resourceurlbase = servletcontext.getInitParameter("resourceurlbase");
		if (resourceurlbase == null) {
			resourceurlbase = ServletUtil.computeContextName(servletcontext);
		}

		// compute the baseURLprovider.
		StaticBaseURLProvider sbup = new StaticBaseURLProvider();
		String baseurl = fixSakaiURL(request, ServletUtil.getBaseURL2(request));
		sbup.setResourceBaseURL(computeResourceURLBase(baseurl));
		// baseurl += SakaiEarlyRequestParser.FACES_PATH + "/";
		sbup.setBaseURL(baseurl);
		return sbup;
	}

	// The argument to this is what Sakai "claims" is our base URL. The true
	// resource URL will be somewhat unrelated in that it will share (at most)
	// the host name and port of this URL.
	private String computeResourceURLBase(String baseurl) {
		if (resourceurlbase.charAt(0) == '/') {
			int endprotpos = baseurl.indexOf("://");
			int firstslashpos = baseurl.indexOf('/', endprotpos + 3);
			return baseurl.substring(0, firstslashpos) + resourceurlbase;
		} else { // it is an absolute URL
			return resourceurlbase;
		}
	}

	public Object getObject() throws Exception {
		return computeBaseURLProvider(request);
	}

	public Class getObjectType() {
		return BaseURLProvider.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
