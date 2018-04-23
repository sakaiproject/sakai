/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test.mocks;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

public abstract class FakeSessionManager implements SessionManager {
	private List<Session> sessions = new ArrayList<Session>();
	private Session currentSession	= Mockito.spy(FakeSession.class);

	public FakeSessionManager() {
		sessions.add(currentSession);
	}
	
	public Session getCurrentSession() {
		return currentSession;
	}

	public String getCurrentSessionUserId() {
		return currentSession.getUserId();
	}

	public Session getSession(String arg0) {
		return currentSession;
	}

	public List<Session> getSessions() {
		return sessions;
	}

}
