/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.user.api.User;

/**
 * The user messaging service is intendened to the the single interface via which tools
 * send messages to users. Multiple media will be supported although at the moment only email
 * and digest are supported. The idea will be to support SMS and SSE events via this same
 * interface. The advantage of this approach is that we can handle user preference filtering here
 * rather than having that logic scattered through tool code.
 */
public interface UserMessagingService {

    /**
     * Send a message to a set of users, via 1 to many message media. If a template is involved
     * you can supply a map of token replacements. User preferences are queried by the
     * implementation and don't have to be handled by the caller.
     *
     * @param users A Set of users to message
     * @param media A list of MessageMedium objects to control how messages are sent
     * @param replacements A Map of replacement tokens for any template processing
     * @param priority a notification priority. If you supply NotificationService.NOTI_REQUIRED
     *          then user preferences will not be honoured and the message will go, regardless.
     *
     */
    void message(Set<User> users, Message message, List<MessageMedium> media, Map<String, Object> replacements, int priority);

    /**
     * Method for importing templates into the EmailTemplateService
     *
     * @param templateResource The resource path for your template. The current thread classloader
     *          will be used to attempt to load a stream for this.
     * @param templateRegistrationKey the unique key to identifiy this particular template when
     *          sending messages.
     */
    boolean importTemplateFromResourceXmlFile(String templateResource, String templateRegistrationKey);
}
