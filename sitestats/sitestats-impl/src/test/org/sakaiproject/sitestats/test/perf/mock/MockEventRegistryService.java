/**
 * Copyright (c) 2006-2017 The Apereo Foundation
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
package org.sakaiproject.sitestats.test.perf.mock;

import org.sakaiproject.sitestats.api.event.EventRegistryService;

import java.util.Arrays;
import java.util.List;

public abstract class MockEventRegistryService implements EventRegistryService {

	@Override
	public List<String> getServerEventIds() {
		return Arrays.asList(new String[]{"site.add", "site.del", "user.add", "user.del", "user.login"});
	}

	@Override
	public boolean isRegisteredEvent(String eventId) {
		return  !getServerEventIds().contains(eventId);
	}

}
