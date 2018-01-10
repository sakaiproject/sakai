/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.impl;

import java.lang.reflect.Field;
import java.util.concurrent.*;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.mutable.MutableLong;

import org.jmock.Expectations;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolSession;

/**
 * Mostly black-box {@link SessionComponent} unit tests intended to guard against
 * regressions. Was specifically motivated by an effort to re-implement
 * session attribute storage against a distributable cache. Class
 * name is unfortunate, but is necessitated by the existence of
 * {@link SessionComponentTest} in the "main" src dir.
 * 
 * @author dmccallum@unicon.net
 */
@Slf4j
public class SessionComponentRegressionTest extends BaseSessionComponentTest {

	public void testGetSessionReturnsNullIfNoSuchSession() {
		assertNull(sessionComponent.getSession("COMPLETE_NONSENSE"));
	}
	
	/**
	 * Ensures {@link Session} has entity semantics, i.e. the same
	 * object is returned to each request for that object. "Always"
	 * here is limited to non-expired sessions.
	 * 
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public void testGetSessionAlwaysReturnsSessionCreatedByStartSession() 
	throws InterruptedException, ExecutionException, TimeoutException {
		final Session startedSession = startSessionForUser();
		assertSame(startedSession, sessionComponent.getSession(startedSession.getId()));
		assertSame(startedSession, sessionComponent.getSession(startedSession.getId())); // intentional duplicate
		// all threads should get the same Session obj for a given key
		FutureTask<Session> asynchGet = new FutureTask<Session>(new Callable<Session>() {
			public Session call() {
				return sessionComponent.getSession(startedSession.getId());
			}
		});
		new Thread(asynchGet).start();
		assertSame(startedSession, asynchGet.get(1, TimeUnit.SECONDS));
	}
	
	/**
	 * Identical to {@link #testGetSessionReturnsSessionCreatedByStartSession()}
	 * except that it tests the overload of <code>startSession()</code>
	 * ({@link SessionComponent#startSession(String)}.
	 * 
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public void testGetSessionAlwaysReturnsSessionStartedWithClientSpecifiedId() 
	throws InterruptedException, ExecutionException, TimeoutException {
		final Session startedSession = sessionComponent.startSession("9876543210");
		assertSame(startedSession, sessionComponent.getSession(startedSession.getId()));
		assertSame(startedSession, sessionComponent.getSession(startedSession.getId())); // intentional duplicate
		// all threads should get the same Session obj for a given key
		FutureTask<Session> asynchGet = new FutureTask<Session>(new Callable<Session>() {
			public Session call() {
				return sessionComponent.getSession(startedSession.getId());
			}
		});
		new Thread(asynchGet).start();
		assertSame(startedSession, asynchGet.get(1, TimeUnit.SECONDS));
	}
	
	/**
	 * Verifies the end-result of session invalidation triggered by 
	 * <code>SessionComponent</code>'s internal maintenance thread.
	 * 
	 * @see #testGetSessionReturnsNullIfSessionExplicitlyInvalidated()
	 * @throws InterruptedException
	 * @throws IllegalAccessException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 */
	public void testGetSessionReturnsNullIfSessionExpired() 
	throws InterruptedException, SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		resetMaintenance("1", "1");
		Session session = startSessionAndExpectItsExpiration();
		awaitExpirationOrFail(session, 1);
		String id = session.getId();
		assertNull(sessionComponent.getSession(id));
	}
	
	/**
	 * An integration test which more explicitly verifies the invalidation
	 * callbacks from the {@link Session}s created by this <code>SessionComponent</code>.
	 * Compare to {@link #testGetSessionReturnsNullIfSessionExpired()} which
	 * tests the same <code>SessionComponent</code>-internal cleanup code,
	 * but relies on its internal maintenance thread to trigger the session
	 * invalidation.
	 */
	public void testGetSessionReturnsNullIfSessionExplicitlyInvalidated() {
		stopMaintenance(); // ensure that it's actually the test code triggering invalidation
		Session session = startSessionAndExpectItsExpiration();
		String id = session.getId();
		session.invalidate();
		assertNull(sessionComponent.getSession(id));
	}
	
	
	protected Session startSessionAndExpectItsExpiration() {
		final Session startedSession = startSessionForUser();
		// Session invalidation involves de-selecting itself as "current". We
		// make the session appear "current" to avoid lazy alloc of another session.
		expectGetAndUnsetCurrentSession(startedSession);
		return startedSession;
	}

	/**
	 * Verifies that the lazily-instantiated session is not stored in
	 * the "global" session lookup table, and is therefore not findable by ID. See 
	 * {@link #testGetCurrentSessionCachesThreadScopedSession()} for verification
	 * that the session is subsequently available to the current thread by other
	 * means, though.
	 */
	public void testGetCurrentSessionLazilyCreatesTransientSession() {
		expectLazyCurrentSessionCreation();
		Session session = sessionComponent.getCurrentSession();
		assertNotNull("Should have allocated a new session", session);
		assertNull("Should not have registered lazily created \"current\" session with the global lookup table",
				sessionComponent.getSession(session.getId()));
	}
	
	/**
	 * Verifies that a session created as a side-effect of 
	 * {@link SessionComponent#getCurrentSession()} is available in a
	 * thread-scoped cache, i.e. that a call to {@link {@link SessionComponent#getCurrentSession()}
	 * on a different, sessionless thread receives a different session. This is 
	 * distinct from {@link #testGetCurrentSessionLazilyCreatesTransientSession()} 
	 * which is concerned with whether or not lazily created "current" sessions
	 * are findable by ID (they are <em>not</em>).
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public void testGetCurrentSessionCachesLazilyCreatedThreadScopedSession() 
	throws InterruptedException, ExecutionException, TimeoutException {
		expectLazyCurrentSessionCreation("1234546789");
		final Session session = sessionComponent.getCurrentSession();
		assertNotNull("Should have allocated a new session", session);
		
		// Since we control the return value of the "gets" on threadLocalManager,
		// the important bit here is that the "get" expectation defined immediately 
		// below is satisfied, much less so the "sameness" assertion following that. 
		// The same basic point holds for the asynch further on down.
		checking(new Expectations() {{
			one(threadLocalManager).get(with(equal(SessionComponent.CURRENT_SESSION))); 
			will(returnValue(session));
		}});
		assertSame("A thread should always receive the same \"current\" session", 
				session, sessionComponent.getCurrentSession());
		
		// other threads should get different "current" Session objects
		final SessionHolder sessionHolder = new SessionHolder();
		expectLazyCurrentSessionCreation(sessionHolder, "987654321");
		FutureTask<Session> asynchGet = new FutureTask<Session>(new Callable<Session>() {
			public Session call() {
				return sessionComponent.getCurrentSession();
			}
		});
		new Thread(asynchGet).start();
		assertNotSame("Should have allocated a different \"current\" session for other thread",
				session, asynchGet.get(1, TimeUnit.SECONDS));
	}
	
	/**
	 * Verifies that the sessions created lazily by {@link SessionComponent#getCurrentSession()}
	 * are initialized to the correct state. This effectively means that the resulting
	 * session exhibits all the characteristics of any other "started" session.
	 */
	public void testGetCurrentSessionCachesLazilyCreatedSessionsInExpectedState() {
		String expectedSessionId = "123456789";
		String expectedInactiveInterval = "10";
		resetMaintenance("10", expectedInactiveInterval);
		expectLazyCurrentSessionCreation(expectedSessionId);
		Session session = sessionComponent.getCurrentSession();
		assertStartedSessionState(session, expectedSessionId, Integer.parseInt(expectedInactiveInterval));
	}
	
	/**
	 * Verifies that {@link SessionComponent#getCurrentSession()} returns a thread-
	 * local session if one exists. This is actually implicitly tested to some extent in 
	 * several other tests, but is included here to explicitly verify that the lazy
	 * session creation logic is skipped when appropriate.
	 *  
	 */
	public void testGetCurrentSessionReturnsExistingThreadScopedSession() {
		final Session session = mock(Session.class);
		checking(new Expectations() {{
			one(threadLocalManager).get(with(equal(SessionComponent.CURRENT_SESSION))); 
			will(returnValue(session));
		}});
		assertSame("Should have returned existing session", 
				session, sessionComponent.getCurrentSession());
	}

	/**
	 * Verifies the state of a variety of fields on the {@link Session}
	 * returned from {@link SessionComponent#startSession()}. To reduce
	 * duplication, most assertions are actually implemented by 
	 * {@link #assertStartedSessionState(Session)}.
	 */
	public void testStartsSessionsInCorrectState() {
		resetMaintenance("10", "10");
		String expectedSessionId = "123456789";
		expectCreateUuidRequest("123456789");
		Session startedSession = sessionComponent.startSession();
		assertStartedSessionState(startedSession, expectedSessionId, 10);
	}
	
	/**
	 * Identical to {@link #testStartSessionCreatesSessionInCorrectState()}
	 * except that it tests the overload of <code>startSession()</code>
	 * ({@link SessionComponent#startSession(String)}.
	 */
	public void testStartsSpecificSessionsInCorrectState() {
		resetMaintenance("10", "10");
		String expectedSessionId = "987654321";
		Session startedSession = sessionComponent.startSession(expectedSessionId);
		assertStartedSessionState(startedSession, expectedSessionId, 10);
	}
	
	/**
	 * Verifies expected state of a newly "started" {@link Session}.
	 * 
	 * @param startedSession the session to assert on
	 * @param expectedSessionId the ID to expect on the given session (so
	 *   we can assert on sessions created by both forms of <code>startSession</code>)
	 * @param expectedInactiveInterval since this value is not accessible
	 *   from {@link SessionComponent} except via reflection.
	 */
	protected void assertStartedSessionState(Session startedSession,
			String expectedSessionId, int expectedInactiveInterval) {
		assertEquals("Sessions should be started with the allocated or specified ID", 
				expectedSessionId, startedSession.getId());
		assertEquals("Sessions should be started with identical creation and last access times",
				startedSession.getCreationTime(), startedSession.getLastAccessedTime());
		assertEquals("Sessions should be started with the inactive interval configured on the SessionManager",
				expectedInactiveInterval,
				startedSession.getMaxInactiveInterval());
		assertNull("Sessions should be started without a user ID", startedSession.getUserId());
		assertNull("Sessions should be started without a user EID", startedSession.getUserEid());
	}

	public void testStartingSessionDoesNotSetCurrentSession() {
		// Currently, getCurrentSession() lazily allocates a thread-scoped
		// Session if such a Session does not already exist. That behavior
		// is tested elsewhere, but we specify "allows" here to at
		// least keep jMock quiet. This also explains the assertNotSame()
		// call, rather than assertNull()
		expectCreateUuidRequest("123456789");
		Session startedSession = sessionComponent.startSession();
		allowLazyCurrentSessionCreation();
		assertNotSame(startedSession, sessionComponent.getCurrentSession());
	}
	
	public void testStartingSpecificSessionDoesNotSetCurrentSession() {
		allowLazyCurrentSessionCreation();
		Session startedSession = sessionComponent.startSession("987654321");	
		assertNotSame(startedSession, sessionComponent.getCurrentSession());
	}
	
	public void testGetCurrentSessionUserIdRetrievesIdFromThreadScopedSession() {
		final Session session = mock(Session.class);
		final String userId = "SOME_USER_ID";
		checking(new Expectations() {{
			one(threadLocalManager).get(SessionComponent.CURRENT_SESSION);
			will(returnValue(session));
			one(session).getUserId();
			will(returnValue(userId));
		}});
		assertEquals("Incorrect current session user ID", 
				userId, sessionComponent.getCurrentSessionUserId());
	}
	
	public void testGetCurrentSessionUserIdReturnsNullIfNoThreadScopedSession() {
		checking(new Expectations() {{
			one(threadLocalManager).get(SessionComponent.CURRENT_SESSION);
			will(returnValue(null));
		}});
		assertNull("Should have returned null user ID", 
				sessionComponent.getCurrentSessionUserId());
	}
	
	public void testGetCurrentToolSessionReturnsThreadScopedSession() {
		final ToolSession session = mock(ToolSession.class);
		checking(new Expectations() {{
			one(threadLocalManager).get(SessionComponent.CURRENT_TOOL_SESSION);
			will(returnValue(session));
		}});
		assertEquals("Should have returned the current thread-scoped ToolSession", 
				session, sessionComponent.getCurrentToolSession());
	}
	
	public void testGetCurrentToolSessionReturnsNullIfNoThreadScopedSession() {
		checking(new Expectations() {{
			one(threadLocalManager).get(SessionComponent.CURRENT_TOOL_SESSION);
			will(returnValue(null));
		}});
		assertNull("Should have returned null ToolSession", 
				sessionComponent.getCurrentToolSession());
	}
	
	/**
	 * Verifies that the specified session is cached in the correct
	 * <code>ThreadLocal</code> <em>and</em> that the specified session is 
	 * not stored in the "global" session lookup table, and is therefore not 
	 * findable by ID.
	 */
	public void testSetCurrentSessionCachesSessionInThreadScope() {
		final String sessionId = "123456789";
		final Session session = mock(Session.class);
		checking(new Expectations() {{
			one(threadLocalManager).set(SessionComponent.CURRENT_SESSION, session);
			allowing(session).getId(); will(returnValue(sessionId));
			
		}});
		sessionComponent.setCurrentSession(session);
		assertNull("Should not have registered \"current\" session with the global lookup table",
				sessionComponent.getSession(sessionId));
	}
	
	public void testSetCurrentSessionCachesNullSessionInThreadScope() {
		checking(new Expectations() {{
			one(threadLocalManager).set(SessionComponent.CURRENT_SESSION, null);
		}});
		sessionComponent.setCurrentSession(null);
	}
	
	/**
	 * Verifies that the specified tool session is cached in the correct
	 * <code>ThreadLocal</code>. No need to verify that the tool session 
	 * isn't placed in the global lookup table b/c the API effectively
	 * prevents this. 
	 */
	public void testSetCurrentToolSessionCachesSessionInThreadScope() {
		final ToolSession session = mock(ToolSession.class);
		checking(new Expectations() {{
			one(threadLocalManager).set(SessionComponent.CURRENT_TOOL_SESSION, session);
		}});
		sessionComponent.setCurrentToolSession(session);
	}
	
	public void testSetCurrentToolSessionCachesNullSessionInThreadScope() {
		checking(new Expectations() {{
			one(threadLocalManager).set(SessionComponent.CURRENT_TOOL_SESSION, null);
		}});
		sessionComponent.setCurrentToolSession(null);
	}
	
	/**
	 * "Activeness" is configurable by method input and refers to session 
	 * accesses not just open sessions. As currently implemented, the only
	 * reason this method happens to work is that the internal datastructure
	 * for storing sessions is a {@link ConcurrentHashMap}. In fact,
	 * this test would fail with a <code>ConcurrentModificationException</code> 
	 * were {@link SessionComponent}'s internal session map implemented by
	 * something other than a {@link ConcurrentMap}.
	 */
	public void testGetActiveUserCount() {
		startSessionForUser();
		startSessionForUser();
		startSessionForUser();
		assertEquals(3, sessionComponent.getActiveUserCount(100000));
	}
	
	/**
	 * Verifies that {@link SessionComponent#getActiveUserCount(int)}
	 * does not count any session which has not been "accessed" in the
	 * given seconds interval.
	 * 
	 * <p>Implementation note: We elected to implement this in a relatively
	 * black-box fashion which involves actually waiting the "activeness
	 * interval" before retrieving the count. Another approach would
	 * attempt to "manually" adjust the session(s) 
	 * <code>lastAccessedTime</code>, but such an implementation would
	 * require mocking out {@link Session} and overriding 
	 * {@link SessionComponent}'s construction thereof. There is currently 
	 * no good way to do the latter. So we live with the off
	 * chance that this test might fail if the 
	 * {@link #startSessionForUser()} calls take an exceptionally
	 * long time to return.</p>
	 * 
	 * @see Session#getLastAccessedTime()
	 * @throws InterruptedException
	 */
	public void testGetActiveUserCountFiltersInactiveSessions() 
	throws InterruptedException {
		startSessionForUser();
		Thread.sleep(1001);
		startSessionForUser();
		startSessionForUser();
		assertEquals(2, sessionComponent.getActiveUserCount(1));
	}

	/**
	 * As currently implemented a "special" user session is associated
	 * with the super user ("admin"), the postmaster, or with a null
	 * user ID. We elected to test this separately from "basic"
	 * active user counting ({@link #testGetActiveUserCount()}) since
	 * there's really two distinct concepts in play: activeness and
	 * specialness. Misimplementation of one needn't imply misimplementation
	 * of the other.
	 */
	public void testGetActiveUserCountFiltersSpecialUserSessions() {
		startSessionForUser("admin", "admin");
		startSessionForUser("postmaster", "admin");
		startSessionForUser(null, null);
		assertEquals(0, sessionComponent.getActiveUserCount(100000));
		
		// and now to make sure a misguided impl doesn't do something
		// like accidentally reset the entire count upon encountering
		// a special user
		startSessionForUser();
		startSessionForUser("admin", "admin");
		assertEquals(1, sessionComponent.getActiveUserCount(100000));
	}
	
	/**
	 * Expiration is subtly different than inactivity. Expired sessions
	 * will be ejected from memory altogether. Activity is determined by
	 * a method parameter to {@link SessionComponent#getActiveUserCount(int)}.
	 * Expired sessions should not ever be considered active, though, regardless
	 * of that <code>int</code> value.
	 * 
	 * @see #testGetSessionReturnsNullIfSessionExpired()
	 * @throws InterruptedException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public void testGetActiveUserCountFiltersExpiredSessions() 
	throws InterruptedException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		resetMaintenance("1", "1");
		Session toBeExpired = startSessionAndExpectItsExpiration();
		awaitExpirationOrFail(toBeExpired, 1);
		startSessionForUser();
		assertEquals(1, sessionComponent.getActiveUserCount(100000));
	}
	
	private void awaitExpirationOrFail(Session toBeExpired, int inactiveInterval) 
	throws InterruptedException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		// unfortunate, but doesn't seem to be any other way around
		// timing problems
		Thread.sleep(inactiveInterval * 1500L); // ensure expiration window elapses, and then some
		Field isValid = toBeExpired.getClass().getDeclaredField("m_valid");
		isValid.setAccessible(true);
		int cnt = 0;
		while ( isValid.getBoolean(toBeExpired) && cnt++ < 10) {
			Thread.sleep(100);
		}
		if ( isValid.getBoolean(toBeExpired) ) {
			fail("Session should have expired");
		}
		Thread.sleep(50);
	}
	

	/**
	 * A clarifying test for dispelling possible ambiguity in
	 * the {@link SessionComponent#getActiveUserCount(int)} method name. That
	 * method does in fact count users, not sessions per se.
	 */
	public void testGetActiveUserCountIncludesMultipleSessionsForSameUser() {
		String userUuid = nextUuid();
		startSessionForUser(userUuid, userUuid);
		startSessionForUser(userUuid, userUuid);
		assertEquals(1, sessionComponent.getActiveUserCount(100000));
	}
	
	public void testSessionIsInvalidatedDuringMaintenance() throws InterruptedException {
		// 20 seconds
		sessionComponent.setInactiveInterval("20");
	
		final CountDownLatch startedLatch = new CountDownLatch(1);
		final CountDownLatch completedLatch = new CountDownLatch(1);
		final CountDownLatch blockerLatch = new CountDownLatch(1);
		
		MySession session = (MySession)newSessionWithBlockableInvalidate(startedLatch,
				blockerLatch, completedLatch);
		// now minus 15 seconds
		session.m_accessed = System.currentTimeMillis() - (25 * 1000L);
		// now minus 5 seconds
		session.expirationTimeSuggestion.setValue(System.currentTimeMillis() - (5*1000L));
		expectGetCurrentSessionReturnNull(session);
		registerSession(session);
		resetMaintenance("20", "20");

		if (!(startedLatch.await(2, TimeUnit.SECONDS))) {
			fail("Took too long for the maintenance thread to start up");
		}
		blockerLatch.countDown();		
		if ( !(completedLatch.await(5, TimeUnit.SECONDS)) ) {
			fail("Took too long for the Session.invalidate() to complete");
		}
	}
	
	/**
	 * Tests that sessions can be created while a maintenance sweep is in
	 * progress. Read on for implementation notes.
	 * 
	 * <p>Certain operations in {@link SessionComponent} have traditionally
	 * been implemented to iterate over the {@link SessionComponent#m_sessions}
	 * datastructure. These operations (expiration, active user counting) need
	 * to cope with concurrent write operations ({@link SessionComponent#startSession()}).</p>
	 * 
	 * <p>One way to verify that this is the case would be to test
	 * the data structure directly. However, the implementation may
	 * choose to lock access to that map in other ways such that the
	 * implementation of the datastructure itself is not actually relevant.</p>
	 * 
	 * <p>As a workaround we decided to inject custom Session implementations
	 * directly into the datastructure for which we could cause certain
	 * operations to block on a condition. We've resisted committing this
	 * sin elsewhere in this test (see any other asnych test) on the grounds
	 * that it violates the black-box intentions of this test class. No other
	 * option was really feasible for this particular test, though.</p>
	 * @throws InterruptedException 
	 * 
	 * @see #testCanStartSessionWhileCalculatingActiveUserCount()
	 */
	public void testCanStartSessionWhilePerformingMaintenance() 
	throws InterruptedException {
		final CountDownLatch maintenanceStartedLatch = new CountDownLatch(1);
		final CountDownLatch maintenanceCompletedLatch = new CountDownLatch(2);
		final CountDownLatch maintenanceBlockerLatch = new CountDownLatch(1);
		
		// need two sessions to ensure Iterator.hasNext() is invoked
		// twice before the test's main thread exits. It's the hasNext()
		// method that we expect to fail if the data structure is not
		// handled/specified correctly.
		registerSession(newSessionWithBlockableMutableLong(maintenanceStartedLatch,
				maintenanceBlockerLatch, maintenanceCompletedLatch));
		registerSession(newSessionWithBlockableMutableLong(maintenanceStartedLatch,
				maintenanceBlockerLatch, maintenanceCompletedLatch));
		resetMaintenance("10", "10");
		
		
		// Wait for the maintenance thread to start checking for
		// inactive sessions. This and the other wait time below need
		// to be relatively low, at least lower than the maintenance
		// sleep window, otherwise the maintenance thread will wake up
		// again and defeat the test.
		if (!(maintenanceStartedLatch.await(2, TimeUnit.SECONDS))) {
			fail("Took too long for the maintenance thread to start up");
		}
		startSessionForUser(); // the actual code exercise
		maintenanceBlockerLatch.countDown(); // allow the maintenance thread to proceed
		if ( !(maintenanceCompletedLatch.await(2, TimeUnit.SECONDS)) ) {
			fail("Took too long for the maintenance thread to complete");
		}
	}
	
	/**
	 * Tests that sessions can be created while an active user count is in
	 * progress.
	 * 
	 * <p>Same as {@link #testCanStartSessionWhilePerformingMaintenance()} but
	 * for {@link SessionComponent#getActiveUserCount(int)}</p>
	 * @throws InterruptedException 
	 */
	public void testCanStartSessionWhileCalculatingActiveUserCount() 
	throws InterruptedException {
		final CountDownLatch countStartedLatch = new CountDownLatch(1);
		final CountDownLatch countCompletedLatch = new CountDownLatch(2);
		final CountDownLatch countBlockerLatch = new CountDownLatch(1);
		
		// need two sessions to ensure Iterator.hasNext() is invoked
		// twice before the test's main thread exits. It's the hasNext()
		// method that we expect to fail if the data structure is not
		// handled/specified correctly.
		registerSession(newSessionWithBlockableGetLastAccessedTimeImpl(countStartedLatch,
				countBlockerLatch, countCompletedLatch));
		registerSession(newSessionWithBlockableGetLastAccessedTimeImpl(countStartedLatch,
				countBlockerLatch, countCompletedLatch));
		new Thread() {
			public void run() {
				sessionComponent.getActiveUserCount(100000);
			}
		}.start();
		
		// Wait for the counting thread to start checking for
		// active users. The wait time is arbitrary, but ensures
		// the test exits. Same for the await() call at bottom.
		if (!(countStartedLatch.await(2, TimeUnit.SECONDS))) {
			fail("Took too long for the counting thread to start up");
		}
		startSessionForUser(); // the actual code exercise
		countBlockerLatch.countDown(); // allow the counting thread to proceed
		if ( !(countCompletedLatch.await(2, TimeUnit.SECONDS)) ) {
			fail("Took too long for the counting thread to complete");
		}
	}	
	
	protected Session newSessionWithBlockableInvalidate(final CountDownLatch opStarted,
			final CountDownLatch opBlocker, final CountDownLatch opCompleted) {
		// unfortunately, the Maintenance implementation compels us to
		// use MySession rather than an interface.
		String uuid = nextUuid();
		final MySession session = new MySession(sessionComponent,uuid,threadLocalManager,idManager,
				sessionComponent,sessionListener,sessionComponent.getInactiveInterval(),new MyNonPortableSession(),
				new MutableLong(System.currentTimeMillis()), null) {
			
			// Make eclipse warnings go away and define this
			private static final long serialVersionUID = 1L;
			
			@Override
			public void invalidate() {
				Callable<Boolean> callback = new Callable<Boolean>() {
					public Boolean call() throws Exception {
						return superInvalidate();
					}
				};
				execBlockableSessionOp(opStarted, opBlocker, opCompleted, callback);
			}
			private boolean superInvalidate() {
				log.debug("**cris** invalidate");
				super.invalidate();
				return true;
			}
		};

		return session;
	}
	
	protected Session newSessionWithBlockableMutableLong(final CountDownLatch opStarted,
			final CountDownLatch opBlocker, final CountDownLatch opCompleted) {
		// unfortunately, the Maintenance implementation compels us to
		// use MySession rather than an interface.
		String uuid = nextUuid();
		final MutableLong expirationTimeSuggestion = new MutableLong(System.currentTimeMillis()) {
			@Override
			public long longValue() {
				Callable<Long> callback = new Callable<Long>() {
					public Long call() throws Exception {
						return superLongValue();
					}
				};
				Long result = 
					execBlockableSessionOp(opStarted, opBlocker, opCompleted, callback);
				return result;
			}
			private long superLongValue() {
				return super.longValue();
			}
		};
		final MySession session = new MySession(sessionComponent,uuid,threadLocalManager,idManager,
				sessionComponent,sessionListener,sessionComponent.getInactiveInterval(),new MyNonPortableSession(),
				expirationTimeSuggestion, null);
		return session;
	}
	
	protected Session newSessionWithBlockableGetLastAccessedTimeImpl(final CountDownLatch opStarted,
			final CountDownLatch opBlocker, final CountDownLatch opCompleted) {
		// unfortunately, the getActiveUserCount() implementation compels us to
		// use MySession rather than an interface.
		String uuid = nextUuid();
		final MySession session = new MySession(sessionComponent,uuid,threadLocalManager,idManager,
				sessionComponent,sessionListener,sessionComponent.getInactiveInterval(),new MyNonPortableSession(),
				new MutableLong(System.currentTimeMillis()), null) {
			private long superGetLastAccessedTime() {
				return super.getLastAccessedTime();
			}
			@Override
			public long getLastAccessedTime()
			{
				Callable<Long> callback = new Callable<Long>() {
					public Long call() throws Exception {
						return superGetLastAccessedTime();
					}
				};
				Long result = 
					execBlockableSessionOp(opStarted, opBlocker, opCompleted, callback);
				return result;
			}
		};
		return session;
	}
	
	// Doesn't necessarily take less code in the client to split this method
	// out, but the *latch calls are error prone.
	protected <V> V execBlockableSessionOp(final CountDownLatch opStarted,
			final CountDownLatch opBlocker, final CountDownLatch opCompleted,
			Callable<V> callback) {
		try {
			opStarted.countDown();
			if (!(opBlocker.await(10, TimeUnit.SECONDS))) {
				fail("Took too long");
			}
			V result = callback.call();
			// Op only completes if the super impl returns non-exceptionally.
			// This allows us to detect failures outside the main test thread.
			opCompleted.countDown();
			return result;
		} catch ( Throwable e ) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e); // typically ends up in logs, at best 
		}
	}
	
	/**
	 * A back-door for registering sessions with {@link SessionComponent}'s
	 * internal data structure.
	 * 
	 * @param session
	 */
	@SuppressWarnings(value={"unchecked"})
	protected void registerSession(Session session) {
		sessionComponent.m_sessions.put(session.getId(), session);
		if (session instanceof MySession) {
			MySession mySession = (MySession)session;
			sessionComponent.expirationTimeSuggestionMap.put(session.getId(),mySession.expirationTimeSuggestion);
		}
	}
	
	protected Session startSessionForUser() {
		return startSessionForUser(nextUuid(), nextUuid());
	}
	
	protected Session startSessionForUser(String userId, String userEid) {
		expectCreateUuidRequest();
		Session session = sessionComponent.startSession();
		session.setUserId(userId);
		session.setUserEid(userEid);
		return session;
	}
	
	protected void allowLazyCurrentSessionCreation() {
		allowLazyCurrentSessionCreation(null,null);
	}
	
	protected void allowLazyCurrentSessionCreation(SessionHolder createdSessionHolder) {
		allowLazyCurrentSessionCreation(createdSessionHolder, null);
	}
	
	protected void allowLazyCurrentSessionCreation(String sessionId) {
		allowLazyCurrentSessionCreation(null, sessionId);
	}
	
	protected void allowLazyCurrentSessionCreation(final SessionHolder createdSessionHolder,
			String sessionId) {
		final String scrubbedSessionId = sessionId == null ? "LAZY_SESSION_ID" : sessionId;
		checking(new Expectations() {{
			allowing(threadLocalManager).get(with(equal(SessionComponent.CURRENT_SESSION))); 
			will(returnValue(null));
			allowing(idManager).createUuid(); will(returnValue(scrubbedSessionId));
			allowing(threadLocalManager).set(with(equal(SessionComponent.CURRENT_SESSION)), 
					with(sessionHavingId(scrubbedSessionId, createdSessionHolder)));
		}});
	}
	
	protected void expectLazyCurrentSessionCreation() {
		expectLazyCurrentSessionCreation(null, null);
	}
	
	protected void expectLazyCurrentSessionCreation(SessionHolder createdSessionHolder) {
		expectLazyCurrentSessionCreation(createdSessionHolder, null);
	}
	
	protected void expectLazyCurrentSessionCreation(String sessionId) {
		expectLazyCurrentSessionCreation(null, sessionId);
	}
	
	protected void expectLazyCurrentSessionCreation(final SessionHolder createdSessionHolder,
			String sessionId) {
		final String scrubbedSessionId = sessionId == null ? "LAZY_SESSION_ID" : sessionId;
		checking(new Expectations() {{
			one(threadLocalManager).get(with(equal(SessionComponent.CURRENT_SESSION))); 
			will(returnValue(null));
			one(idManager).createUuid(); will(returnValue(scrubbedSessionId));
			one(threadLocalManager).set(with(equal(SessionComponent.CURRENT_SESSION)), 
					with(sessionHavingId(scrubbedSessionId, createdSessionHolder)));
		}});
	}
	
	protected void expectGetCurrentSessionReturnNull(final Session session) {
		checking(new Expectations() {{
			allowing(threadLocalManager).get(with(equal(SessionComponent.CURRENT_SESSION)));
//			exactly(2).of(threadLocalManager).get(with(any(String.class)));
			will(returnValue(session));
			allowing(threadLocalManager).set(with(equal(SessionComponent.CURRENT_SESSION)),with(equal((Object)null)));
		}});
	}
	
}
