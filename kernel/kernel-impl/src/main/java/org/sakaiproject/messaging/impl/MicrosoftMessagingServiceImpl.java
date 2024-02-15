/**
 * Copyright (c) 2023 The Apereo Foundation
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
package org.sakaiproject.messaging.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.ignite.IgniteMessaging;
import org.apache.ignite.IgniteSpringBean;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.sakaiproject.messaging.api.MicrosoftMessage;
import org.sakaiproject.messaging.api.MicrosoftMessageListener;
import org.sakaiproject.messaging.api.MicrosoftMessagingService;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MicrosoftMessagingServiceImpl implements MicrosoftMessagingService {

    @Setter private IgniteSpringBean ignite;
    private ExecutorService executor;
    private IgniteMessaging messaging;

    private List<MessageTopic> messageTopics;

    public MicrosoftMessagingServiceImpl() {
        messageTopics = Collections.emptyList();
    }

    public void init() {
    	executor = Executors.newFixedThreadPool(10);
        messaging = ignite.message(ignite.cluster().forLocal());
    }
    
    public void destroy() {
        messageTopics.forEach(t -> messaging.stopLocalListen(t.topic, t.predicate));
        executor.shutdown();
    }

    public void listen(MicrosoftMessage.Topic topic, MicrosoftMessageListener listener) {
        if (topic != null && listener != null) {
            MessageTopic messageTopic = new MessageTopic(topic, (nodeId, message) -> {
                executor.execute(() -> listener.read((MicrosoftMessage) message));
                return true;
            });

            List<MessageTopic> updatedTopics = new ArrayList<>(messageTopics);
            messaging.localListen(messageTopic.topic, messageTopic.predicate);
            updatedTopics.add(messageTopic);
            messageTopics = Collections.unmodifiableList(updatedTopics);
        } else {
            log.warn("Unable to register listener for topic [{}]", topic);
        }
    }

    public void send(MicrosoftMessage.Topic topic, MicrosoftMessage msg) {
        if (topic != null && msg != null) {
            try {
                messaging.send(topic, msg);
            } catch (Exception e) {
                log.warn("Could not send message for topic [{}], message [{}], {}", topic, msg, e.toString());
            }
        } else {
            log.debug("skip sending message for topic [{}], {}", topic, msg);
        }
    }

    @AllArgsConstructor
    private class MessageTopic {
        private MicrosoftMessage.Topic topic;
        private IgniteBiPredicate<UUID, ?> predicate;
    }
}
