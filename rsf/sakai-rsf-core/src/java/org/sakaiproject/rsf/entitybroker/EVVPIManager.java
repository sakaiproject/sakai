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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityView;

import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.stringutil.StringList;

public class EVVPIManager implements ViewParamsReporter {
	private List inferrers;
	private EntityViewAccessRegistrar accessRegistrar;
	private EntityView entityViewProxy;
	private Map inferrermap = new HashMap();

	public void setAccessRegistrar(EntityViewAccessRegistrar accessRegistrar) {
		this.accessRegistrar = accessRegistrar;
	}

	public void setEntityViewViewParamsInferrers(List inferrers) {
		this.inferrers = inferrers;
	}

	public void init() {
		StringList allprefixes = RegistrationUtil.collectPrefixes(inferrers, inferrermap);
		accessRegistrar.registerPrefixes(allprefixes.toStringArray());
	}

	public void setSakaiEntityView(EntityView entityViewProxy) {
		this.entityViewProxy = entityViewProxy;
	}

	public ViewParameters getViewParameters() {
		EntityViewViewParamsInferrer evvpi = null;
		if (entityViewProxy.getEntityReference() != null) {
			String prefix = entityViewProxy.getEntityReference().prefix;
			evvpi = (EntityViewViewParamsInferrer) inferrermap.get(prefix);
		}
		return evvpi == null ? null : evvpi.inferDefaultViewParameters(entityViewProxy);
	}
}
