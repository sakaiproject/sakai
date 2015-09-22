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
 * Created on 06-Mar-2006
 */
package org.sakaiproject.rsf.state;

import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;

import uk.org.ponder.rsf.state.TokenStateHolder;

/**
 * A TokenStateHolder that stores flow state in the Sakai Tool-specific session,
 * falling back to the global Sakai Session if no ToolSession is active (in the
 * case we are operating without a tool placement for some reason).
 * 
 * NB Expiryseconds not yet implemented. Would require *extra* server-side
 * storage of map of tokens to sessions, in order to save long-term storage
 * within sessions - awaiting research from performance clients.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * 
 */
public class InSakaiSessionTSH implements TokenStateHolder {
	// NB - this is a proxy of the request-scope session!!!
	private SessionManager sessionmanager;
	private int expiryseconds;

	public void setSessionManager(SessionManager sessionmanager) {
		this.sessionmanager = sessionmanager;
	}

	public Object getTokenState(String tokenID) {
		ToolSession toolSession = sessionmanager.getCurrentToolSession();
		// In the case ToolSession is not available (perhaps this is an access
		// request) fall back to the global Session.
		return toolSession == null ? sessionmanager.getCurrentSession().getAttribute(tokenID)
				: toolSession.getAttribute(tokenID);
	}

	public void putTokenState(String tokenID, Object trs) {
		ToolSession toolSession = sessionmanager.getCurrentToolSession();
		if (toolSession == null) {
			sessionmanager.getCurrentSession().setAttribute(tokenID, trs);
		} else {
			toolSession.setAttribute(tokenID, trs);
		}
	}

	public void clearTokenState(String tokenID) {
		ToolSession toolSession = sessionmanager.getCurrentToolSession();
		if (toolSession == null) {
			sessionmanager.getCurrentSession().removeAttribute(tokenID);
		} else {
			sessionmanager.getCurrentToolSession().removeAttribute(tokenID);
		}
	}

	public void setExpirySeconds(int seconds) {
		this.expiryseconds = seconds;
	}

	public String getId() {
		ToolSession toolSession = sessionmanager.getCurrentToolSession();
		return toolSession == null ? sessionmanager.getCurrentSession().getId() : toolSession.getId();
	}

}
