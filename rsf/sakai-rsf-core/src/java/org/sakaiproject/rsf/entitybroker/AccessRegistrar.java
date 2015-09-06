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
 * Created on 18 May 2007
 */
package org.sakaiproject.rsf.entitybroker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.springframework.beans.factory.DisposableBean;

import uk.org.ponder.beanutil.WBLAcceptor;
import uk.org.ponder.beanutil.WriteableBeanLocator;
import uk.org.ponder.rsac.RSACBeanLocator;
import uk.org.ponder.rsac.servlet.RSACUtils;
import uk.org.ponder.util.Logger;
import uk.org.ponder.util.UniversalRuntimeException;

public class AccessRegistrar implements EntityViewAccessProvider, DisposableBean {

	private EntityViewAccessProviderManager accessProviderManager;
	private EntityBroker entityBroker;
	private RSACBeanLocator rsacbl;
	private String[] prefixes;
	private CommonAccessHandler accessHandler;

	public void setEntityBroker(EntityBroker entityBroker) {
		this.entityBroker = entityBroker;
	}

	public void setAccessProviderManager(EntityViewAccessProviderManager accessProviderManager) {
		this.accessProviderManager = accessProviderManager;
	}

	public void setRSACBeanLocator(RSACBeanLocator rsacbl) {
		this.rsacbl = rsacbl;
	}

	public void init() {
		accessHandler = makeCommonAccessHandler();
	}

	public void registerPrefixes(String[] prefixes) {
		this.prefixes = prefixes;
		for (int i = 0; i < prefixes.length; ++i) {
			accessProviderManager.registerProvider(prefixes[i], this);
		}
	}

	public void destroy() {
		if (prefixes != null) {
			for (int i = 0; i < prefixes.length; ++i) {
				accessProviderManager.unregisterProvider(prefixes[i]);
			}
		}
	}

	// Despite our very low profile of earlyRequestParser, it is still easier to
	// fake out pathInfo this way here. It is considered the authoritative
	// determiner by getBaseURL2.
	private HttpServletRequest wrapRequest(HttpServletRequest req) {
		String oldpathinfo = req.getPathInfo();
		EntityReference parsed = entityBroker.parseReference(oldpathinfo);
		int extent = parsed.toString().length();
		final String newpathinfo = extent < oldpathinfo.length() ? oldpathinfo.substring(parsed.toString().length())
				: "";

		return new HttpServletRequestWrapper(req) {
			public String getPathInfo() {
				return newpathinfo;
			}
		};
	}

	public void handleAccess(EntityView view, HttpServletRequest req, HttpServletResponse res) {

		EntityReference reference = view.getEntityReference();
		accessHandler.handleAccess(req, res, reference, null);
	}

	public CommonAccessHandler getCommonAccessHandler() {
		return accessHandler;
	}

	private CommonAccessHandler makeCommonAccessHandler() {
		return new CommonAccessHandler() {

			public void handleAccess(HttpServletRequest req, HttpServletResponse res, EntityReference reference,
					WBLAcceptor acceptor) {
				try {
					rsacbl.startRequest();
					// A request bean locator just good for this request.
					WriteableBeanLocator rbl = rsacbl.getBeanLocator();
					// inchuck entityReference
					rbl.set("sakai-EntityReference", reference.toString());
					if (acceptor != null) {
						acceptor.acceptWBL(rbl);
					}
					RSACUtils.startServletRequest(wrapRequest(req), res, rsacbl, RSACUtils.HTTP_SERVLET_FACTORY);
					// pass the request to RSF.
					rbl.locateBean("rootHandlerBean");
				} catch (Exception t) {
					// Access servlet performs no useful logging, do it here.
					Logger.log.error("Error handling access request", t);
					Throwable unwrapped = UniversalRuntimeException.unwrapException(t);
					if (unwrapped instanceof RuntimeException) {
						throw ((RuntimeException) unwrapped);
					} else
						throw UniversalRuntimeException.accumulate(unwrapped, "Error handling access request");
				} finally {
					rsacbl.endRequest();
				}
			}

		};
	}

}
