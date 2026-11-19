/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.webservices;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserLockedException;
import org.sakaiproject.user.api.UserPermissionException;

/**
 * The user-editing operations must release the edit lock they took when they fail,
 * and must not try to cancel an edit they never obtained.
 */
public class SakaiScriptUserEditLockTest extends AbstractCXFTest {
	private static final String SESSION_ID = "***SESSION_HAS_BEEN_MOCKERIZED***";
	private static final String ENDPOINT = "userEditLock";
	private static final String ADMIN_ID = "admin";
	private static final String LOCKED_EID = "lockeduser";
	private static final String LOCKED_ID = "lockeduser-id";
	private static final String EDITABLE_EID = "editableuser";
	private static final String EDITABLE_ID = "editableuser-id";
	private static final String[] CHANGE_USER_OPERATIONS = {
		"changeUserInfo", "changeUserName", "changeUserEmail", "changeUserType", "changeUserPassword"
	};
	private static final String[] LOCKED_USER_OPERATIONS = {
		"changeUserInfo", "changeUserName", "changeUserEmail", "changeUserType", "changeUserPassword", "removeUser"
	};

	private AbstractWebService service;
	private UserEdit userEdit;

	@Override
	protected <T extends AbstractWebService> Class<T> getTestClass() {
		return (Class<T>) SakaiScript.class;
	}

	@Override
	protected String getOperation() {
		return ENDPOINT;
	}

	@Override
	protected void addServiceMocks(AbstractWebService service) {
		this.service = service;

		Session session = mock(Session.class);
		when(session.getUserId()).thenReturn(ADMIN_ID);
		when(service.sessionManager.getSession(SESSION_ID)).thenReturn(session);
		when(service.securityService.isSuperUser(ADMIN_ID)).thenReturn(true);

		userEdit = mock(UserEdit.class);
		when(userEdit.isActiveEdit()).thenReturn(true);
		when(userEdit.getPropertiesEdit()).thenReturn(mock(ResourcePropertiesEdit.class));

		User lockedUser = mock(User.class);
		when(lockedUser.getId()).thenReturn(LOCKED_ID);
		User editableUser = mock(User.class);
		when(editableUser.getId()).thenReturn(EDITABLE_ID);

		try {
			when(service.userDirectoryService.getUserByEid(LOCKED_EID)).thenReturn(lockedUser);
			when(service.userDirectoryService.editUser(LOCKED_ID)).thenThrow(new UserLockedException(LOCKED_ID));
			when(service.userDirectoryService.getUserByEid(EDITABLE_EID)).thenReturn(editableUser);
			when(service.userDirectoryService.editUser(EDITABLE_ID)).thenReturn(userEdit);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to set up user directory mocks", e);
		}
	}

	@Test
	public void testLockedUserIsReportedWithoutCancellingAnEdit() {
		for (String operation : LOCKED_USER_OPERATIONS) {
			String result = request(operation, LOCKED_EID).get(String.class);
			assertTrue(operation + " returned: " + result, result.startsWith(UserLockedException.class.getName()));
		}

		// editUser() failed, so there is no edit to cancel; cancelEdit(null) used to throw a NullPointerException
		verify(service.userDirectoryService, never()).cancelEdit(any());
	}

	@Test
	public void testChangeUserOperationsReleaseLockWhenCommitFails() throws Exception {
		doThrow(new UserAlreadyDefinedException(EDITABLE_ID)).when(service.userDirectoryService).commitEdit(userEdit);

		for (String operation : CHANGE_USER_OPERATIONS) {
			String result = request(operation, EDITABLE_EID).get(String.class);
			assertTrue(operation + " returned: " + result, result.startsWith(UserAlreadyDefinedException.class.getName()));
		}

		verify(service.userDirectoryService, times(CHANGE_USER_OPERATIONS.length)).cancelEdit(userEdit);
	}

	@Test
	public void testRemoveUserReleasesLockWhenRemovalFails() throws Exception {
		doThrow(new UserPermissionException(ADMIN_ID, "user.del", EDITABLE_ID)).when(service.userDirectoryService).removeUser(userEdit);

		String result = request("removeUser", EDITABLE_EID).get(String.class);

		assertTrue("removeUser returned: " + result, result.startsWith(UserPermissionException.class.getName()));
		verify(service.userDirectoryService).cancelEdit(userEdit);
	}

	@Test
	public void testSetUserPropertyReleasesLockWhenCommitFails() throws Exception {
		doThrow(new UserAlreadyDefinedException(EDITABLE_ID)).when(service.userDirectoryService).commitEdit(userEdit);

		WebClient client = request("setUserProperty", EDITABLE_EID);
		client.query("key", "Company");
		client.query("value", "Apereo");

		assertEquals("failure", client.get(String.class));
		verify(service.userDirectoryService).cancelEdit(userEdit);
	}

	private WebClient request(String operation, String eid) {
		WebClient client = WebClient.create(getFullEndpointAddress());
		addCXFClientMocks(client);
		client.accept("text/plain");
		client.path("/" + operation);
		client.query("sessionid", SESSION_ID);
		client.query("eid", eid);
		return client;
	}
}
