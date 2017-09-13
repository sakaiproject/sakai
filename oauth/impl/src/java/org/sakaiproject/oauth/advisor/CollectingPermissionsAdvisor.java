/*
 * #%L
 * OAuth Implementation
 * %%
 * Copyright (C) 2009 - 2013 The Sakai Foundation
 * %%
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
 * #L%
 */
package org.sakaiproject.oauth.advisor;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.oauth.dao.ConsumerDao;
import org.sakaiproject.oauth.domain.Consumer;

/**
 * Advisor used during record phase to determine which permissions must be enabled for the service
 * <p>
 * Every permission used during one request will be enabled automatically.
 * </p>
 *
 * @author Colin Hebert
 */
public class CollectingPermissionsAdvisor implements SecurityAdvisor {
    private static final Logger logger = LoggerFactory.getLogger(CollectingPermissionsAdvisor.class);
    private ConsumerDao consumerDao;
    private Consumer consumer;

    public CollectingPermissionsAdvisor(ConsumerDao consumerDao, Consumer consumer) {
        this.consumerDao = consumerDao;
        this.consumer = consumer;
    }

    @Override
    public SecurityAdvice isAllowed(String userId, String function, String reference) {
        if (!consumer.getRights().contains(function)) {
            logger.info("'" + consumer.getId() + "' requires '" + function + "' right in order to work, enable it.");
            try {
                consumer.getRights().add(function);
                consumerDao.update(consumer);
            } catch (Exception e) {
                // If the update doesn't work, carry on
                logger.warn("Activation of the '" + function + "' right on '" + consumer.getId() + "' failed.", e);
            }
        }
        return SecurityAdvice.PASS;
    }

    @Override
    public String toString() {
        return "Permission collector";
    }
}
