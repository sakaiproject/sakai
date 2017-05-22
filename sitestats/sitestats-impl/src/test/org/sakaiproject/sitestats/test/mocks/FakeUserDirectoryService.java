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

import org.mockito.Mockito;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public abstract class FakeUserDirectoryService implements UserDirectoryService {

	public User getCurrentUser() {
		return Mockito.spy(FakeUser.class);
	}

	public User getUser(String arg0) throws UserNotDefinedException {
		return Mockito.spy(FakeUser.class);
	}

	public User getUserByEid(String arg0) throws UserNotDefinedException {
		return Mockito.spy(FakeUser.class);
	}

	@Override
	public User getUserByAid(String aid) throws UserNotDefinedException {
		return Mockito.spy(FakeUser.class);
	}

	public String getUserEid(String arg0) throws UserNotDefinedException {
		return Mockito.spy(FakeUser.class).getEid();
	}

	public String getUserId(String arg0) throws UserNotDefinedException {
		return Mockito.spy(FakeUser.class).getId();
	}
}
