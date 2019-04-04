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
package org.sakaiproject.portal.beans.bullhornhandlers;

import java.util.Arrays;

import javax.inject.Inject;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.portal.api.BullhornHandler;

abstract class AbstractBullhornHandler implements BullhornHandler {

    @Inject
    protected SecurityService securityService;

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
