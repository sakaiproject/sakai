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
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.cluster.api.ClusterService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.*;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

/**
 * Provides common fixture set-up operations. This is necessary
 * so long as the Sessions domain is implemented as inner classes
 * on {@link SessionComponent}.
 * 
 * @author dmccallum@unicon.net
 *
 */
public abstract class BaseSessionComponentTest {

	protected SessionComponent sessionComponent;
	@Mock protected IdManager idManager;
	@Mock protected ComponentManager componentManager;
	@Mock protected ThreadLocalManager threadLocalManager;
	@Mock protected ToolManager toolManager;
	@Mock protected ClusterService clusterService;
	protected SessionAttributeListener sessionListener;
	private int uuidDiscriminator;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
		setUpComponentManager();
		this.sessionComponent = new SessionComponent();
		// Inject the mocks into the sessionComponent
		this.sessionComponent.setThreadLocalManager(threadLocalManager);
		this.sessionComponent.setIdManager(idManager);
		this.sessionComponent.setToolManager(toolManager);
		this.sessionComponent.setClusterManager(clusterService);
		//commenting out this next line, because it doesn't seem to be needed
		//this.sessionComponent.setClusterableTools("");
		this.sessionComponent.init();
		// it's up to individual tests to start or stop maintenance
		stopMaintenance();
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
		when(idManager.createUuid()).thenReturn(nextUuid);
	}
	
	protected void expectCreateUuidRequest() {
		expectCreateUuidRequest(nextUuid());
	}
	
	protected void allowCreateUuidRequest(final String nextUuid) {
		when(idManager.createUuid()).thenReturn(nextUuid);
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
		// No need to set up expectation for set() with Mockito - it's a void method
	}

	protected void expectGetSession(final Session session) {
		when(threadLocalManager.get(SessionComponent.CURRENT_SESSION)).thenReturn(session);
	}
	
	protected void allowGetAndUnsetCurrentSession(final Session session) {
		when(threadLocalManager.get(SessionComponent.CURRENT_SESSION)).thenReturn(session);
		// No need to set up expectation for set() with Mockito - it's a void method
	}

	protected void allowPlacementCheck(final String context) {
		Placement mockPlacement = mock(Placement.class);
		when(toolManager.getCurrentPlacement()).thenReturn(mockPlacement);
		when(mockPlacement.getContext()).thenReturn(context);
	}

	protected void allowToolCheck(final String toolId) {
		Tool mockTool = mock(Tool.class);
		when(toolManager.getCurrentTool()).thenReturn(mockTool);
		when(mockTool.getId()).thenReturn(toolId);
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
	
	@After
	public void tearDown() throws Exception {
		sessionComponent.destroy();
		// TODO try to verify Maintenance shutdown? prob overkill.
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
