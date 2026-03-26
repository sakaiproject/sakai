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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractUserNotificationHandler implements UserNotificationHandler {

    @Autowired protected SecurityService securityService;
    @Autowired protected UserMessagingService userMessagingService;

    @PostConstruct
    protected void registerForEvents() {
        log.info("Registering notification handler: {}", this.getName());
        userMessagingService.registerHandler(this);
    }

    @PreDestroy
    protected void unregisterForEvents() {
        log.info("Unregistering notification handler: {}", this.getName());
        userMessagingService.unregisterHandler(this);
    }

    protected SecurityAdvisor unlock(final String[] functions) {

        SecurityAdvisor securityAdvisor = (String userId, String function, String reference) -> {

            if (functions != null) {
                if (Arrays.asList(functions).contains(function)) {
                    return SecurityAdvisor.SecurityAdvice.ALLOWED;
                } else {
                    return SecurityAdvisor.SecurityAdvice.NOT_ALLOWED;
                }
            } else {
                return SecurityAdvisor.SecurityAdvice.ALLOWED;
            }
        };

        securityService.pushAdvisor(securityAdvisor);
        return securityAdvisor;
    }

    protected void lock(SecurityAdvisor sa) {
        securityService.popAdvisor(sa);
    }
}
