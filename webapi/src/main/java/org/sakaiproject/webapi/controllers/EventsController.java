/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.messaging.api.MessagingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.UserDirectoryService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
public class EventsController extends AbstractSakaiApiController {

	@Resource
	private EntityManager entityManager;

	@Resource
	private SecurityService securityService;

	@Resource(name = "org.sakaiproject.component.api.ServerConfigurationService")
	private ServerConfigurationService serverConfigurationService;

	@Resource
	private SiteService siteService;

	@Resource
	private UserDirectoryService userDirectoryService;

    @Resource
    private MessagingService messagingService;

    @GetMapping("/users/{userId}/events")
    public Flux<ServerSentEvent<String>> streamEvents() {

        Session session = checkSakaiSession();

        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        return Flux.<ServerSentEvent<String>>create(emitter -> {

            messagingService.listen("USER#" + session.getUserId(), message -> {

                String event = "notifications";

                try {
                    emitter.next(ServerSentEvent.<String> builder()
                    .event(event)
                    .data(mapper.writeValueAsString(message))
                    .build());
                } catch (Exception e) {
                    log.error("Failed to emit SSE event", e);
                }
            });

            messagingService.listen("GENERAL", message -> {

                try {
                    emitter.next(ServerSentEvent.<String> builder()
                    .data((new ObjectMapper()).writeValueAsString(message))
                    .build());
                } catch (Exception e) {
                    log.error("Failed to emit SSE event", e);
                }
            });
        });
    }
}
