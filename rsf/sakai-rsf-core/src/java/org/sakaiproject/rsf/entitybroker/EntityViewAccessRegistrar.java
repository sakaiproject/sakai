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
 * Created on 21 Jul 2008
 */
package org.sakaiproject.rsf.entitybroker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.springframework.beans.factory.DisposableBean;

import uk.org.ponder.beanutil.WBLAcceptor;
import uk.org.ponder.beanutil.WriteableBeanLocator;

public class EntityViewAccessRegistrar implements EntityViewAccessProvider, DisposableBean {

	private EntityViewAccessProviderManager entityViewAccessProviderManager;

	private CommonAccessHandler commonAccessHandler;

	private String[] prefixes;

	public void setCommonAccessHandler(CommonAccessHandler commonAccessHandler) {
		this.commonAccessHandler = commonAccessHandler;
	}

	public void setEntityViewAccessProviderManager(EntityViewAccessProviderManager entityViewAccessProviderManager) {
		this.entityViewAccessProviderManager = entityViewAccessProviderManager;
	}

	public void registerPrefixes(String[] prefixes) {
		this.prefixes = prefixes;
		for (int i = 0; i < prefixes.length; ++i) {
			entityViewAccessProviderManager.registerProvider(prefixes[i], this);
		}
	}

	public void destroy() {
		if (prefixes != null) {
			for (int i = 0; i < prefixes.length; ++i) {
				entityViewAccessProviderManager.unregisterProvider(prefixes[i]);
			}
		}
	}

	public void handleAccess(final EntityView view, HttpServletRequest req, HttpServletResponse res) {
		commonAccessHandler.handleAccess(req, res, view.getEntityReference(), new WBLAcceptor() {
			public Object acceptWBL(WriteableBeanLocator toaccept) {
				toaccept.set("sakai-EntityView", view);
				return null;
			}
		});
	}

}
