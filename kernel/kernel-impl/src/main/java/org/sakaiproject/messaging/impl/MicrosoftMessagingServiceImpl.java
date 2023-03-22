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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.apache.ignite.IgniteMessaging;
import org.sakaiproject.ignite.EagerIgniteSpringBean;
import org.sakaiproject.messaging.api.MicrosoftMessage;
import org.sakaiproject.messaging.api.MicrosoftMessageListener;
import org.sakaiproject.messaging.api.MicrosoftMessagingService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MicrosoftMessagingServiceImpl implements MicrosoftMessagingService {

    @Resource
    private EagerIgniteSpringBean ignite;

    private IgniteMessaging messaging;
    
    private ExecutorService executor;

    public void init() {
    	log.info("Initializing Microsoft Messaging Service");

    	executor = Executors.newFixedThreadPool(20);
        messaging = ignite.message(ignite.cluster().forLocal());
    }
    
    public void destroy() {
        executor.shutdownNow();
    }

    public void listen(MicrosoftMessage.Topic topic, MicrosoftMessageListener listener) {
    	messaging.localListen(topic, (nodeId, message) -> {
    		executor.execute(() -> {
    			listener.read((MicrosoftMessage)message);
    		});
    		return true;
    	});
    }

    public void send(MicrosoftMessage.Topic topic, MicrosoftMessage msg) {
    	try {
    		messaging.send(topic, msg);
    	} catch(Exception e) {
    		log.error("Error sending MicrosoftMessage");
    	}
    }

}
