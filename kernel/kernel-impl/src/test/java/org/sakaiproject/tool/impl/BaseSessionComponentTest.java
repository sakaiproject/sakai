/**
 * Copyright (c) 2003-2015 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.impl;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.sakaiproject.cluster.api.ClusterService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.*;

import java.lang.reflect.Field;

/**
 * Provides common fixture set-up operations. This is necessary
 * so long as the Sessions domain is implemented as inner classes
 * on {@link SessionComponent}.
 * 
 * @author dmccallum@unicon.net
 *
 */
public abstract class BaseSessionComponentTest extends MockObjectTestCase {

	protected SessionComponent sessionComponent;
	protected IdManager idManager;
	protected ComponentManager componentManager;
	protected ThreadLocalManager threadLocalManager;
	protected ToolManager toolManager;
	// not used - test passes null value to MySession() constructor
	protected SessionAttributeListener sessionListener;
	private int uuidDiscriminator;
	protected ClusterService clusterService;
	
	protected void setUp() throws Exception {
		this.idManager = mock(IdManager.class);
		this.threadLocalManager = mock(ThreadLocalManager.class);
		this.toolManager = mock(ToolManager.class);
		this.clusterService = mock(ClusterService.class);
		setUpComponentManager();
		this.sessionComponent = new SessionComponent() {

			@Override
			protected IdManager idManager() {
				return idManager;
			}

			@Override
			protected ClusterService clusterManager() {
				return clusterService;
			}

			@Override
			protected ThreadLocalManager threadLocalManager() {
				return threadLocalManager;
			}

			@Override
			protected ToolManager toolManager() {
				return toolManager;
			}			

			@Override
			protected RebuildBreakdownService rebuildBreakdownService() {
				return null;
			}

			@Override
			protected boolean isClosing() {
				return false;
			}
		};
		//commenting out this next line, because it doesn't seem to be needed
		//this.sessionComponent.setClusterableTools("");
		this.sessionComponent.init();
		// it's up to individual tests to start or stop maintenance
		stopMaintenance();
		super.setUp();
	}
	
	/**
	 * Initializes the {@link org.sakaiproject.component.cover.ComponentManager}} cover 
	 * referenced during {@link SessionComponent#init()}. This implementation
	 * effectively short-circuits the internal initialization that would normally
	 * occur in the CM cover when <code>SessionComponent</code> 
	 * starts an internal thread which invokes the static 
	 * {@link org.sakaiproject.component.cover.ComponentManager#waitTillConfigured()}.
	 * 
	 * 
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	protected void setUpComponentManager() 
	throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		this.componentManager = mock(ComponentManager.class);
		// 
		Field componentManagerField = 
			org.sakaiproject.component.cover.ComponentManager.
			class.getDeclaredField("m_componentManager");
		componentManagerField.setAccessible(true);
		componentManagerField.set(null, componentManager);
	}
	
	/**
	 * Destroys and recreates the current {@link SessionComponent}, setting
	 * <code>checkEvery</code> and <code>inactiveInterval</code> values to
	 * the received arguments. This is overkill in many cases for this class,
	 * but is our only "blackbox" means of "knowing" what <code>checkEvery</code>
	 * and <code>inactiveInterval</code> are set to.
	 * 
	 * @param checkEvery
	 * @param inactiveInterval
	 */
	protected void resetMaintenance(String checkEvery, String inactiveInterval) {
		stopMaintenance();
		sessionComponent.setCheckEvery(checkEvery);
		sessionComponent.setInactiveInterval(inactiveInterval);
		startMaintenance();
	}

	protected void stopMaintenance() {
		// Basically copied directly from SessionComponent.destroy().
		// Didn't want to use the latter since its semantics could be
		// more broad than simply stopping the maintenance thread.
		if (sessionComponent.m_maintenance != null)
		{
			sessionComponent.m_maintenance.stop();
			sessionComponent.m_maintenance = null;
		}
	}
	
	protected void startMaintenance() {
		// Basically copied directly from SessionComponent.init().
		// Didn't want to use the latter since its semantics could be
		// more broad than simply starting the maintenance thread.
		if (sessionComponent.m_checkEvery > 0)
		{
			sessionComponent.m_maintenance = sessionComponent.new Maintenance();
			sessionComponent.m_maintenance.start();
		}
	}
	
	protected void expectCreateUuidRequest(final String nextUuid) {
		checking(new Expectations() {{
			one(idManager).createUuid(); will(returnValue(nextUuid));
		}});
	}
	
	protected void expectCreateUuidRequest() {
		expectCreateUuidRequest(nextUuid());
	}
	
	protected void allowCreateUuidRequest(final String nextUuid) {
		checking(new Expectations() {{
			allowing(idManager).createUuid(); will(returnValue(nextUuid));
		}});
	}
	
	protected void allowCreateUuidRequest() {
		allowCreateUuidRequest(nextUuid());
	}
	
	/**
	 * Doesn't actually return a UUID, but a String that should be "unique enough"
	 * for testing purposes.
	 * 
	 * @return
	 */
	protected String nextUuid() {
		return "" + System.currentTimeMillis() + uuidDiscriminator++;
	}
	
	protected void expectGetAndUnsetCurrentSession(final Session session) {
		expectGetSession(session);
		checking(new Expectations() {{
			one(threadLocalManager).set(SessionComponent.CURRENT_SESSION, null);
		}});
	}
	
	protected void expectGetSession(final Session session) {
		checking(new Expectations() {{
			one(threadLocalManager).get(SessionComponent.CURRENT_SESSION); 
			will(returnValue(session));
		}});
	}
	
	protected void allowGetAndUnsetCurrentSession(final Session session) {
		checking(new Expectations() {{
			allowing(threadLocalManager).get(SessionComponent.CURRENT_SESSION); 
			will(returnValue(session));
			allowing(threadLocalManager).set(SessionComponent.CURRENT_SESSION, null);
		}});
	}

	protected void allowPlacementCheck(final String context) {
		checking(new Expectations() {{
			Placement mockPlacement = mock(Placement.class);
			allowing(toolManager).getCurrentPlacement();
			will(returnValue(mockPlacement));
			allowing(mockPlacement).getContext();
			will(returnValue(context));
		}});
	}

	protected void allowToolCheck(final String toolId) {
		checking(new Expectations() {{
			Tool mockTool = mock(Tool.class);
			allowing(toolManager).getCurrentTool();
			will(returnValue(mockTool));
			allowing(mockTool).getId();
			will(returnValue(toolId));
		}});
	}
	
	protected Matcher<Session> sessionHavingId(final String toMatch, final SessionHolder matchedSessionHolder) {
		return new TypeSafeMatcher<Session>() {

			@Override
			public boolean matchesSafely(Session item) {
				if (matchedSessionHolder != null) matchedSessionHolder.setSession(item);
				return toMatch.equals(item.getId());
			}

			public void describeTo(Description description) {
				description.appendText("a session having ID ").appendValue(toMatch);
			}
			
		};
	}
	
	protected void tearDown() throws Exception {
		sessionComponent.destroy();
		// TODO try to verify Maintenance shutdown? prob overkill.
		super.tearDown();
	}
	
	protected static final class SessionHolder {
		private Session session;

		public Session getSession() {
			return session;
		}

		public void setSession(Session session) {
			this.session = session;
		}
	}
	
}
