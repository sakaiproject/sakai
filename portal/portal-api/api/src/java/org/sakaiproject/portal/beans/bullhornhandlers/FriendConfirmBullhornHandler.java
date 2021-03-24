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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.portal.api.BullhornData;
import org.sakaiproject.profile2.logic.ProfileLinkLogic;
import org.sakaiproject.profile2.util.ProfileConstants;

import org.hibernate.SessionFactory;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FriendConfirmBullhornHandler extends AbstractBullhornHandler {

    @Inject
    private ProfileLinkLogic profileLinkLogic;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    private SessionFactory sessionFactory;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    private PlatformTransactionManager transactionManager;

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(ProfileConstants.EVENT_FRIEND_CONFIRM);
    }

    @Override
    public Optional<List<BullhornData>> handleEvent(Event e, Cache<String, Long> countCache) {

        String from = e.getUserId();

        String ref = e.getResource();
        String[] pathParts = ref.split("/");

        String to = pathParts[2];
        try {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.execute(status -> {

                sessionFactory.getCurrentSession().createQuery("delete BullhornAlert where event = :event and fromUser = :fromUser")
                    .setString("event", ProfileConstants.EVENT_FRIEND_REQUEST)
                    .setString("fromUser", to).executeUpdate();
                return null;
            });
        } catch (Exception e1) {
            log.error("Failed to delete bullhorn request event", e1);
        }
        String url = profileLinkLogic.getInternalDirectUrlToUserConnections(to);
        countCache.remove(to);
        countCache.remove(from);
        return Optional.of(Collections.singletonList(new BullhornData(from, to, "", "", url)));
    }
}
