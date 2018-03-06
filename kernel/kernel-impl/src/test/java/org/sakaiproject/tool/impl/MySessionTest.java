/**
 * Copyright (c) 2003-2014 The Apereo Foundation
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

import org.apache.commons.lang.mutable.MutableLong;
import org.jmock.Expectations;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.*;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * Verifies behavior of {@link MySession}, which
 * is the standard implementation of {@link Session}.
 * 
 * <p>Tests are not comprehensive. Where an implementation is simple
 * field access, the corresponding test has typically been skipped.</p>
 * 
 * <p>Read on for more design notes.</p>
 * 
 * <p>Theoretically, this guards against regressions in this module.
 * In reality, though, this test will eventually fail as-is b/c it 
 * actually does test <code>MySession</code> as a unit as opposed to, 
 * for example, relying on factory methods like 
 * {@link SessionComponent#startSession()}. Thus, were 
 * <code>MySession</code> refactored to a top-level class, which we
 * expect, this test will need to be modified accordingly. So we were 
 * forced to choose between "pure" unit tests, which break 
 * (paradoxically) if the relationship between <code>MySession</code> 
 * and <code>SessionComponent</code> changes, and black box-ish tests 
 * which intentionally obscure the division of responsibilities between 
 * <code>MySession</code> and <code>SessionComponent</code>.</p>
 * 
 * <p>We decided to pursue "pure" unit tests in combination with a small
 * quantity of black box-ish tests in {@link SessionComponentRegressionTest}
 * to guard against complete failure. For example, see 
 * {@link SessionComponentRegressionTest#testGetSessionReturnsNullIfSessionExpired()},
 * which implicitly tests callbacks to <code>SessionComponent</code> 
 * from {@link Session#invalidate()}. We felt this was the correct 
 * decision because testing the behavior of factory methods should be a 
 * separate concern from testing the behavior of the created object 
 * itself. Such a design improves the overall quality of the code base 
 * while still satisfying our original goal of supporting anticipated 
 * modifications to both {@link SessionComponent} and its internal 
 * implementations of the Sessions domain.</p> 
 * 
 * 
 * @author dmccallum@unicon.net
 *
 */
public class MySessionTest extends BaseSessionComponentTest {
	
	
	public void testCreatedInExpectedState() throws Exception {
		final String sessionId = "SESSION_ID";
		doTestCreatedInExpectedState(sessionId, new Callable<MySession>() {
			public MySession call() throws Exception {
				return createSession(sessionId);
			}
		});
	}

	public void testCreatedInExpectedStateWithClientSpecifiedId()
			throws Exception {
		final String sessionId = "SESSION_ID";
		doTestCreatedInExpectedState(sessionId, new Callable<MySession>() {
			public MySession call() throws Exception {
				return createSessionWithClientSpecifiedId(sessionId);
			}
		});
	}
	
	protected void doTestCreatedInExpectedState(String sessionId,
			Callable<MySession> factoryCallback) throws Exception {
		MySession session = factoryCallback.call();
		assertEquals(sessionId, session.getId());
		assertTrue(session.getCreationTime() > 0);
		assertEquals(session.getCreationTime(), session.getLastAccessedTime());
		assertEquals(sessionComponent.m_defaultInactiveInterval, session
				.getMaxInactiveInterval());
		assertNull(session.getUserId());
		assertNull(session.getUserEid());
		assertFalse(session.getAttributeNames().hasMoreElements());
	}
	
	/**
	 * {@link MySession#invalidate()} should behave like
	 * {@link MySession#clear()} but with extra logic for unsetting itself as
	 * the "current" session.
	 * 
	 * @see #doTestSessionClear(org.sakaiproject.tool.impl.MySessionTest.MyTestableSession,
	 *      Runnable)
	 */
	public void testInvalidateClearsSessionAndUnsetsItselfAsCurrent() {
		allowToolCheck("simple.unit.test");
		allowPlacementCheck("sakai-context-1");
		final MyTestableSession session = createSession();
		doTestSessionClear(session, new Runnable() {
			public void run() {
				expectGetAndUnsetCurrentSession(session);
				session.invalidate();
			}
		});
	}

	/**
	 * Exactly the same as {@link #testInvalidateClearsSessionAndUnsetsItselfAsCurrent()}
	 * except that the session's currentness should not be affected.
	 * 
	 * @see #doTestSessionClear(org.sakaiproject.tool.impl.MySessionTest.MyTestableSession, Runnable)
	 */
	public void testClearUnbindsAttributes() {
		allowToolCheck("simple.unit.test");
		allowPlacementCheck("sakai-context-1");
		final MyTestableSession session = createSession();
		doTestSessionClear(session, new Runnable() {
			public void run() {
				session.clear();
			}
		});
	}
	
	protected void doTestSessionClear(final MyTestableSession session, Runnable codeExerciseCallback) {
		ContextSession contextSession = getContextSession(session, "CONTEXT_SESSION_ID");
		ToolSession toolSession = getToolSession(session, "PLACEMENT_ID");
		
		final String sessionAttribKey = "SESSION_ATTRIB_KEY";
		final ListeningAttribValue sessionAttribValue = 
			setNewListeningAttribValue(session, sessionAttribKey);
		setNewListeningAttribValue(contextSession, "CONTEXT_SESSION_ATTRIB_KEY");
		setNewListeningAttribValue(toolSession, "TOOL_SESSION_ATTRIB_KEY");
		
		codeExerciseCallback.run();
		
		assertHasNoAttributes(session);
		assertHasNoAttributes(toolSession);
		assertHasNoAttributes(contextSession);
		
		assertEquals(new HashMap<String,Object>() {{
				put(sessionAttribKey, sessionAttribValue);
			}}, session.unbindInvokedWith);
		
		// Oddly, we can still use the session as a factory even after its been 
		// invalidated. Were this to change, we'd need to devise a more clever
		// mechanism for detecting cascaded context/tool session invalidation.
		assertNotSame(contextSession, session.getContextSession(contextSession.getContextId()));
		assertNotSame(toolSession, session.getToolSession(toolSession.getPlacementId()));
	}
	
	/**
	 * Hard to believe this is the correct behavior, but this is what
	 * the "legacy" implementation does. The {@link Session}'s own attributes
	 * are selectively filtered, retaining only those named by the specified
	 * <code>Collection</code>, but all child {@link ToolSession} and
	 * {@link ContextSession} attributes are wiped away by virtue of a call to
	 * {@link Session#clear()}.
	 */
	public void testClearExceptFiltersSessionAttribsButClearsAllToolAndContextSessionAttribs() {
		MyTestableSession session = createSession();
		ContextSession contextSession = getContextSession(session, "CONTEXT_SESSION_ID");
		allowToolCheck("simple.unit.test");
		allowPlacementCheck("PLACEMENT_ID");
		ToolSession toolSession = getToolSession(session, "PLACEMENT_ID");

		final String sessionAttribKey1 = "SESSION_ATTRIB_KEY_1";
		final String sessionAttribKey2 = "SESSION_ATTRIB_KEY_2";
		final ListeningAttribValue sessionAttribValue1 = 
			setNewListeningAttribValue(session, sessionAttribKey1);
		final ListeningAttribValue sessionAttribValue2 = 
			setNewListeningAttribValue(session, sessionAttribKey2);
		setNewListeningAttribValue(contextSession, "CONTEXT_SESSION_ATTRIB_KEY");
		setNewListeningAttribValue(toolSession, "TOOL_SESSION_ATTRIB_KEY");
		session.clearExcept(new HashSet<String>() {{ add(sessionAttribKey1); }});
		
		assertEquals(sessionAttribValue1, session.getAttribute(sessionAttribKey1));
		assertNull(session.getAttribute(sessionAttribKey2));
		assertEquals(new HashMap<String,Object>() {{
			put(sessionAttribKey2, sessionAttribValue2);
		}}, session.unbindInvokedWith);
		assertHasNoAttributes(toolSession);
		assertHasNoAttributes(contextSession);
		
	}
	
	public void testRemoveAttributeReleasesAttributeAndFiresUnbind() {
		MyTestableSession session = createSession();
		final String sessionAttribKey = "SESSION_ATTRIB_KEY";
		final ListeningAttribValue sessionAttribValue = 
			setNewListeningAttribValue(session, sessionAttribKey);
		session.removeAttribute(sessionAttribKey);
		assertNull(session.getAttribute(sessionAttribKey));
		assertHasNoAttributes(session);
		assertEquals(new HashMap<String,Object>() {{
			put(sessionAttribKey, sessionAttribValue);
		}}, session.unbindInvokedWith);
	}
	
	public void testSetAndRemoveAttributeDoNotUpdateLastAccessedTime() 
	throws InterruptedException {
		MyTestableSession session = createSession();
		long origLastAccessed = session.getLastAccessedTime();
		final String sessionAttribKey = "SESSION_ATTRIB_KEY";
		final ListeningAttribValue sessionAttribValue = 
			setNewListeningAttribValue(session, sessionAttribKey);
		Thread.sleep(2); // we might execute too quickly to affect lastAccessedTime 
		session.removeAttribute(sessionAttribKey);
		assertEquals(origLastAccessed, session.getLastAccessedTime());
	}
	
	public void testSettingNullAttributeValueReleasesAttributeAndFiresUnbind() {
		MyTestableSession session = createSession();
		final String sessionAttribKey = "SESSION_ATTRIB_KEY";
		final ListeningAttribValue sessionAttribValue = 
			setNewListeningAttribValue(session, sessionAttribKey);
		session.setAttribute(sessionAttribKey, null);
		assertNull(session.getAttribute(sessionAttribKey));
		assertHasNoAttributes(session);
		assertEquals(new HashMap<String,Object>() {{
			put(sessionAttribKey, sessionAttribValue);
		}}, session.unbindInvokedWith);
	}
	
	public void testSetAttributeCachesAttributeAndFiresBind() {
		MyTestableSession session = createSession();
		final String sessionAttribKey = "SESSION_ATTRIB_KEY";
		final ListeningAttribValue sessionAttribValue = 
			setNewListeningAttribValue(session, sessionAttribKey);
		assertEquals(sessionAttribValue, session.getAttribute(sessionAttribKey));
		assertEquals(sessionAttribKey, session.getAttributeNames().nextElement());
		assertEquals(new HashMap<String,Object>() {{
			put(sessionAttribKey, sessionAttribValue);
		}}, session.bindInvokedWith);
	}
	
	public void testSetAttributeOverwritesExistingAttributeAndFiresBindAndUnbind() {
		MyTestableSession session = createSession();
		final String sessionAttribKey = "SESSION_ATTRIB_KEY";
		final ListeningAttribValue sessionAttribValue1 = 
			setNewListeningAttribValue(session, sessionAttribKey);
		final ListeningAttribValue sessionAttribValue2 = 
			setNewListeningAttribValue(session, sessionAttribKey);
		assertEquals(sessionAttribValue2, session.getAttribute(sessionAttribKey));
		assertEquals(sessionAttribKey, session.getAttributeNames().nextElement());
		assertEquals(new HashMap<String,Object>() {{
			put(sessionAttribKey, sessionAttribValue1);
		}}, session.unbindInvokedWith);
		assertEquals(new HashMap<String,Object>() {{
			put(sessionAttribKey, sessionAttribValue2);
		}}, session.bindInvokedWith);
	}
	
	
	public void testLazilyCreatesToolSessionInExpectedState() {
		MySession session = createSession();
		session.setUserEid("USER_EID");
		session.setUserId("USER_ID");
		String toolSessionId = "TOOL_SESSION_ID";
		String placementId = "TOOL_PLACEMENT_ID";
		allowToolCheck("sakai.tool.id");
		allowPlacementCheck(placementId);
		ToolSession toolSession = getToolSession(session, placementId, toolSessionId);
		assertEquals(toolSessionId, toolSession.getId());
		assertEquals(placementId, toolSession.getPlacementId());
		assertTrue(toolSession.getCreationTime() > 0);
		assertTrue(toolSession.getLastAccessedTime() >= toolSession.getCreationTime());
		assertEquals(session.getUserEid(), toolSession.getUserEid());
		assertEquals(session.getUserId(), toolSession.getUserId());
		assertHasNoAttributes(toolSession);
	}
	
	public void testLazilyCreatesContextSessionInExpectedState() {
		MySession session = createSession();
		session.setUserEid("USER_EID");
		session.setUserId("USER_ID");
		String contextSessionId = "CONTEXT_SESSION_ID";
		String contextId = "CONTEXT_ID";
		ContextSession contextSession = getContextSession(session, contextId, contextSessionId);
		assertEquals(contextSessionId, contextSession.getId());
		assertEquals(contextId, contextSession.getContextId());
		assertTrue(contextSession.getCreationTime() > 0);
		assertTrue(contextSession.getLastAccessedTime() >= contextSession.getCreationTime());
		assertEquals(session.getUserEid(), contextSession.getUserEid());
		assertEquals(session.getUserId(), contextSession.getUserId());
		assertHasNoAttributes(contextSession);
	}
	
	// note this is part of the protected API
	public void testIsInactive() throws InterruptedException {
		//return ((m_inactiveInterval > 0) && (System.currentTimeMillis() > (m_accessed + (m_inactiveInterval * 1000))));
		MySession session = createSession();
		int inactivityThreshold = 1;
		session.setMaxInactiveInterval(1);
		session.setActive();
		Thread.sleep(inactivityThreshold * 2000);
		assertTrue(session.isInactive());
	}
	
	public void testNeverInactiveIfMaxInactiveIntervalLteZero() {
		MySession session = createSession();
		session.setMaxInactiveInterval(0);
		assertFalse(session.isInactive());
		session.setMaxInactiveInterval(-1);
		assertFalse(session.isInactive());
	}
	
	public void testSetActiveDoesNotUpdateTimeExpirationSuggestion() {
		// inactivity in seconds
		int inactivityThreshold = 30;
		// original expirationTimeSuggestion value gets set to current time + MaxInActive
		MySession session = createSessionSetMaxInActive(inactivityThreshold);

		long originalValue = session.expirationTimeSuggestion.longValue();
		// setActive should force expirationTimeSuggestion value to be updated
		// if the difference between currentTime and expiry time is smaller then inactive period/2 
		session.setActive();
		long newValue = session.expirationTimeSuggestion.longValue();

		// make sure the expiry value was not updated (because we let no time expire)
		// by asserting the original and new value are the same
		assertEquals(originalValue, newValue);
	}
	
	public void testSetActiveUpdatesTimeExpirationSuggestion() {
		// inactivity in seconds
		int inactivityThreshold = 30;
		// original expirationTimeSuggestion value gets set to current time + MaxInActive
		// Simulate some time has past since the session was last accessed by setting the accessed time
		long pastTime = now() - ((inactivityThreshold*1000)+5000);
		MySession session = createSessionSetMaxInActiveAndAccessTime(inactivityThreshold,pastTime);

		long originalValue = session.expirationTimeSuggestion.longValue();
		// setActive should force expirationTimeSuggestion value to be updated
		// if the difference between currentTime and expiry time is smaller then inactive period/2 
		session.setActive();
		long newValue = session.expirationTimeSuggestion.longValue();

		// make sure the expiry value was not updated (because we let no time expire)
		// by asserting the original and new value are the same
		assertNotSame(Long.valueOf(originalValue), Long.valueOf(newValue));
	}
	
	public MySession createSessionSetMaxInActive(int maxInactive) {
		MySession session = createSession();
		session.setMaxInactiveInterval(maxInactive);
		return session;
	}

	public MySession createSessionSetMaxInActiveAndAccessTime(int maxInactive, long accessedTime) {
		MySession session = createSessionSetMaxInActive(maxInactive);
		session.m_accessed = accessedTime;
		return session;
	}
	
	public long now() {
		return System.currentTimeMillis();
	}
	
	public void testEqualsMatchesAnySessionImplementorHavingSameId() {
		// attributes added in for a little noise, to be "extra sure" we only care about IDs.
		final MySession session1 = createSession();
		setNewListeningAttribValue(session1, "SESSION_ATTRIB_KEY_1");
		MySession session2 = createSession(session1.getId());
		setNewListeningAttribValue(session2, "SESSION_ATTRIB_KEY_2");
		assertEquals(session1, session2);
		// now lets see if it cares about a sibling implementation
		final Session session3 = mock(Session.class);
		checking(new Expectations(){{
			one(session3).getId();
			will(returnValue(session1.getId()));
		}});
		assertEquals(session1, session3);
	}
	
	public void testUnbindNotifiesValueIfIsSessionBindingListener() {
		MySession session = createSession();
		ListeningAttribValue attribValue = new ListeningAttribValue();
		session.unBind("SESSION_KEY", attribValue);
		assertEquals(1, attribValue.sessionValueUnboundInvokedWith.size());
		SessionBindingEvent event = attribValue.sessionValueUnboundInvokedWith.get(0);
		assertEventState(event, "SESSION_KEY", session, attribValue);
	}

	public void testUnbindNotifiesValueIfIsHttpSessionBindingListener() {
		MySession session = createSession();
		ListeningAttribValue attribValue = new ListeningAttribValue();
		session.unBind("SESSION_KEY", attribValue);
		assertEquals(1, attribValue.httpSessionValueUnboundInvokedWith.size());
		HttpSessionBindingEvent event = attribValue.httpSessionValueUnboundInvokedWith.get(0);
		assertEventState(event, "SESSION_KEY", session, attribValue);
	}
	
	public void testBindNotifiesValueIfIsSessionBindingListener() {
		MySession session = createSession();
		ListeningAttribValue attribValue = new ListeningAttribValue();
		session.bind("SESSION_KEY", attribValue);
		assertEquals(1, attribValue.sessionValueBoundInvokedWith.size());
		SessionBindingEvent event = attribValue.sessionValueBoundInvokedWith.get(0);
		assertEventState(event, "SESSION_KEY", session, attribValue);
	}

	public void testBindNotifiesValueIfIsHttpSessionBindingListener() {
		MySession session = createSession();
		ListeningAttribValue attribValue = new ListeningAttribValue();
		session.bind("SESSION_KEY", attribValue);
		assertEquals(1, attribValue.httpSessionValueBoundInvokedWith.size());
		HttpSessionBindingEvent event = attribValue.httpSessionValueBoundInvokedWith.get(0);
		assertEventState(event, "SESSION_KEY", session, attribValue);
	}

	public void testNonPortableAttributesStoreAndRetrieve() {
		MySession session = createSession();
		String value = "VALUE";
		System.setProperty("sakai.cluster.terracotta","true");
		session.setAttribute("SESSION_KEY", value);
		System.setProperty("sakai.cluster.terracotta","false");
		session.setAttribute("SESSION_KEY_2", value);
		assertEquals(value,session.getAttribute("SESSION_KEY"));
		assertEquals(value,session.getAttribute("SESSION_KEY_2"));
	}
	
	public void testNonPortableBindNotifiesValueIfIsSessionBindingListener() {
		MySession session = createSession();
		System.setProperty("sakai.cluster.terracotta","true");
		ListeningAttribValue attribValue = new ListeningAttribValue();
		session.bind("SESSION_KEY", attribValue);
		assertEquals(1, attribValue.sessionValueBoundInvokedWith.size());
		SessionBindingEvent event = attribValue.sessionValueBoundInvokedWith.get(0);
		assertEventState(event, "SESSION_KEY", session, attribValue);
		System.setProperty("sakai.cluster.terracotta","false");
	}
	
	public void testNonPortableUnbindNotifiesValueIfIsSessionBindingListener() {
		MySession session = createSession();
		System.setProperty("sakai.cluster.terracotta","true");
		ListeningAttribValue attribValue = new ListeningAttribValue();
		session.unBind("SESSION_KEY", attribValue);
		assertEquals(1, attribValue.sessionValueUnboundInvokedWith.size());
		SessionBindingEvent event = attribValue.sessionValueUnboundInvokedWith.get(0);
		assertEventState(event, "SESSION_KEY", session, attribValue);
		System.setProperty("sakai.cluster.terracotta","false");
	}
	
	public void testNonPortableRemoveAttributeReleasesAttributeAndFiresUnbind() {
		System.setProperty("sakai.cluster.terracotta","true");
		MyTestableSession session = createSession();
		allowToolCheck("simple.unit.test");
		final String sessionAttribKey = "SESSION_ATTRIB_KEY";
		final ListeningAttribValue sessionAttribValue = 
			setNewListeningAttribValue(session, sessionAttribKey);
		session.removeAttribute(sessionAttribKey);
		assertNull(session.getAttribute(sessionAttribKey));
		assertHasNoAttributes(session);
		assertEquals(new HashMap<String,Object>() {{
			put(sessionAttribKey, sessionAttribValue);
		}}, session.unbindInvokedWith);
		System.setProperty("sakai.cluster.terracotta","false");
	}

	public void testNonPortableClearUnbindsAttributes() {
		final MyTestableSession session = createSession();
		System.setProperty("sakai.cluster.terracotta","true");
		allowToolCheck("simple.unit.test");
		allowPlacementCheck("sakai-context-1");
		doTestSessionClear(session, new Runnable() {
			public void run() {
				session.clear();
			}
		});
		System.setProperty("sakai.cluster.terracotta","false");
	}
	
	protected void assertEventState(SessionBindingEvent event, String name,
			MySession session, Object value) {
		assertEquals(name, event.getName());
		assertEquals(session, event.getSession());
		assertEquals(value, event.getValue());
	}
	
	protected void assertEventState(HttpSessionBindingEvent event, String name,
			MySession session, Object value) {
		assertEquals(name, event.getName());
		assertEquals(session, event.getSession());
		assertEquals(value, event.getValue());
	}
	
	protected ContextSession getContextSession(MySession session,
			String contextId) {
		allowCreateUuidRequest();
		return session.getContextSession(contextId);
	}
	
	protected ContextSession getContextSession(MySession session,
			String contextId, String contextSessionId) {
		expectCreateUuidRequest(contextSessionId); // mtd signature implies we must _expect_ the IdManager call
		return session.getContextSession(contextId);
	}
	
	protected ToolSession getToolSession(MySession session,
			String placementId) {
		allowCreateUuidRequest();
		return session.getToolSession(placementId);
	}
	
	protected ToolSession getToolSession(MySession session,
			String placementId, String toolSessionId) {
		expectCreateUuidRequest(toolSessionId); // mtd signature implies we must _expect_ the IdManager call
		return session.getToolSession(placementId);
	}
	
	protected MyTestableSession createSession() {
		String uuid = nextUuid();
		return new MyTestableSession(sessionComponent,uuid,threadLocalManager,idManager,sessionListener,new MyNonPortableSession());
	}
	
	protected MyTestableSession createSession(String sessionId) {
		return new MyTestableSession(sessionComponent,sessionId,threadLocalManager,idManager,sessionListener,new MyNonPortableSession());
	}
	
	/**
	 * Like {@link #createSession(String)} but assigns the given ID
	 * directly, rather than allowing the created session to
	 * retrieve an ID from the <code>IdManager</code>. This allows
	 * us to test a different constructor than does 
	 * {@link #createSession(String)}.
	 * 
	 * @param sessionId
	 * @return
	 */
	protected MyTestableSession createSessionWithClientSpecifiedId(String sessionId) {
		return new MyTestableSession(sessionComponent, sessionId, threadLocalManager, idManager, sessionListener, new MyNonPortableSession());
	}

	protected ListeningAttribValue setNewListeningAttribValue(
			ToolSession toolSession, String key) {
		ListeningAttribValue value = new ListeningAttribValue();
		toolSession.setAttribute(key, value);
		return value;
	}
	
	protected ListeningAttribValue setNewListeningAttribValue(
			ContextSession contextSession, String key) {
		ListeningAttribValue value = new ListeningAttribValue();
		contextSession.setAttribute(key, value);
		return value;
	}
	
	protected ListeningAttribValue setNewListeningAttribValue(MySession session,
			String key) {
		ListeningAttribValue value = new ListeningAttribValue();
		session.setAttribute(key, value);
		return value;
	}

	protected void assertHasNoAttributes(ContextSession contextSession) {
		assertFalse(contextSession.getAttributeNames().hasMoreElements());
	}

	protected void assertHasNoAttributes(ToolSession toolSession) {
		assertFalse(toolSession.getAttributeNames().hasMoreElements());
	}
	
	protected void assertHasNoAttributes(MySession session) {
		assertFalse(session.getAttributeNames().hasMoreElements());
	}

	/**
	 * Verifies that multiple {@link MySession#invalidate()} calls can proceed
	 * concurrently without error and with the session properly invalidated
	 * after all threads return. This turns out to be quite difficult to test
	 * reliably, even with knowledge of the implementation. In fact, you can't get
	 * the following test to fail, even if you remove all explicit concurrency
	 * precautions in <code>MySession.invalidate()</code>. It will fail, though,
	 * if sleep times are injected after taking a local copies of the attribute
	 * map, though. So, at best this test will catch truly gross errors, but
	 * for the most part is dead-weight.
	 */
	public void testConcurrentInvalidation() {
		final MyTestableSession session = createSession();
		Collection<ListeningAttribValue> attribValues =
			new ArrayList<ListeningAttribValue>();
		attribValues.add(setNewListeningAttribValue(session, "SESSION_ATTRIB_KEY_1"));
		attribValues.add(setNewListeningAttribValue(session, "SESSION_ATTRIB_KEY_2"));
		attribValues.add(setNewListeningAttribValue(session, "SESSION_ATTRIB_KEY_3"));
		attribValues.add(setNewListeningAttribValue(session, "SESSION_ATTRIB_KEY_4"));
		attribValues.add(setNewListeningAttribValue(session, "SESSION_ATTRIB_KEY_5"));
		final Set<Throwable> failures = Collections.synchronizedSet(new HashSet<Throwable>());
		final int workersCnt = 5;
		final CyclicBarrier invalidateBarrier = new CyclicBarrier(workersCnt);
		final CyclicBarrier testExitBarrier = new CyclicBarrier(workersCnt + 1);
		class Worker extends Thread {
			public void run() {
				try {
					invalidateBarrier.await(10, TimeUnit.SECONDS);
					session.invalidate();
				} catch ( Throwable t ) {
					failures.add(t);
				} finally {
					try {
						testExitBarrier.await();
					} catch ( Throwable t ) {}
				}
			}
		}
		allowGetAndUnsetCurrentSession(session);
		Worker[] workers = new Worker[workersCnt];
		for ( int p = 0; p < workersCnt; p++) {
			workers[p] = new Worker();
			workers[p].start();
		}
		try {
			testExitBarrier.await();
		} catch ( InterruptedException e ) {
		} catch ( BrokenBarrierException e ) {}
		assertEquals(Collections.synchronizedSet(new HashSet<Throwable>()), failures);
		for ( ListeningAttribValue attribValue : attribValues ) {
			assertEquals(1, attribValue.httpSessionValueUnboundInvokedWith.size());
			assertEquals(1, attribValue.sessionValueUnboundInvokedWith.size());
		}
	}

	private static class MyTestableSession extends MySession {

		private Map<String,Object> unbindInvokedWith = new HashMap<String,Object>();
		private Map<String,Object> bindInvokedWith = new HashMap<String,Object>();

		public MyTestableSession(SessionComponent outer, String sessionId, ThreadLocalManager threadLocalManager,
				IdManager idManager, SessionAttributeListener sessionListener,  NonPortableSession nps) {
			super(outer, sessionId, threadLocalManager, idManager, outer, sessionListener, outer.getInactiveInterval(),nps,new MutableLong(System.currentTimeMillis()), null);
		}

		@Override
		protected void unBind(String name, Object value) {
			unbindInvokedWith.put(name, value);
			super.unBind(name, value);
		}

		@Override
		protected void bind(String name, Object value) {
			bindInvokedWith.put(name, value);
			super.bind(name, value);
		}
	}
	
	private static class ListeningAttribValue implements SessionBindingListener, HttpSessionBindingListener {

		private List<SessionBindingEvent> sessionValueBoundInvokedWith =
			Collections.synchronizedList(new ArrayList<SessionBindingEvent>());
		private List<SessionBindingEvent> sessionValueUnboundInvokedWith =
			Collections.synchronizedList(new ArrayList<SessionBindingEvent>());
		private List<HttpSessionBindingEvent> httpSessionValueBoundInvokedWith =
			Collections.synchronizedList(new ArrayList<HttpSessionBindingEvent>());
		private List<HttpSessionBindingEvent> httpSessionValueUnboundInvokedWith =
			Collections.synchronizedList(new ArrayList<HttpSessionBindingEvent>());

		public void valueBound(SessionBindingEvent event) {
			this.sessionValueBoundInvokedWith.add(event);
		}

		public void valueUnbound(SessionBindingEvent event) {
			this.sessionValueUnboundInvokedWith.add(event);
		}

		public void valueBound(HttpSessionBindingEvent event) {
			this.httpSessionValueBoundInvokedWith.add(event);
		}

		public void valueUnbound(HttpSessionBindingEvent event) {
			this.httpSessionValueUnboundInvokedWith.add(event);
		}

	}
	
	/* Questioning the cost/benefit ration of the following:
	public void testConcurrentInvalidationAndSetAttribute() {
		fail("implement me");
	}
	
	public void testConcurrentClear() {
		fail("implement me");
	}
	
	public void testConcurrentSetAttributeAndGetAttributeNames() {
		fail("implement me");
	}
	
	public void testConcurrentClearExcept() {
		fail("implement me");
	}
	*/
	
}
