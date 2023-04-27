/*
 * Copyright (c) 2003-2022 The Apereo Foundation
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
package org.sakaiproject.messaging.api;

/**
 * Marker interface for a handler that will register and unregister itself.
 *
 * This is to accommodate any unknown implementations of the UserNotificationHandler, which were previously autowired into the
 * {@link UserMessagingService}. The {@link org.sakaiproject.messaging.api.AbstractUserNotificationHandler} base
 * class handles this registration for convenience in subclasses. However, there may be bare implementations of the
 * interface that need to be scooped up by a post-processor and implicitly registered to preserve the previous behavior.
 */
public interface SelfRegisteringUserNotificationHandler extends UserNotificationHandler {
}
