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

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.messaging.api.UserMessagingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class EventsController extends AbstractSakaiApiController {

	@Autowired
	private ServerConfigurationService serverConfigurationService;

    @Autowired
    private UserMessagingService userMessagingService;

    @GetMapping("/keys/sakaipush")
    public ResponseEntity<String> getPushKey() {

        String home = serverConfigurationService.getSakaiHomePath();
        String fileName = serverConfigurationService.getString(userMessagingService.PUSH_PUBKEY_PROPERTY, "sakai_push.key.pub");

        try {
            return ResponseEntity.ok(String.join("", Files.readAllLines(Paths.get(home, fileName))));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/users/me/pushEndpoint")
    public ResponseEntity setPushEndpoint(@RequestParam String endpoint,
                                            @RequestParam(required = false) String auth,
                                            @RequestParam(required = false) String userKey,
                                            @RequestParam(required = false) String browserFingerprint) {

		checkSakaiSession();

        userMessagingService.subscribeToPush(endpoint, auth, userKey, browserFingerprint);

        return ResponseEntity.ok().build();
    }
}
