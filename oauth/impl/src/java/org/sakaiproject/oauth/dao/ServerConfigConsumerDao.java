/*
 * #%L
 * OAuth ServerConfig DAO
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
package org.sakaiproject.oauth.dao;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.oauth.domain.Consumer;

import java.util.*;

/**
 * @author Colin Hebert
 */
public class ServerConfigConsumerDao implements ConsumerDao {
    /**
     * Property prefix for OAuth settings
     */
    private static final String OAUTH_PREFIX = "oauth";
    private Map<String, Consumer> consumers;

    public ServerConfigConsumerDao(ServerConfigurationService serverConfig) {
        Collection<String> consumerKeys = getStringsOrSplit(OAUTH_PREFIX + ".consumers", serverConfig);
        consumers = new HashMap<String, Consumer>(consumerKeys.size());
        for (String consumerKey : consumerKeys) {
            Consumer consumer = new Consumer();
            consumer.setId(consumerKey);
            String consumerPrefix = OAUTH_PREFIX + "." + consumerKey;
            consumer.setName(serverConfig.getString(consumerPrefix + ".name", null));
            consumer.setDescription(serverConfig.getString(consumerPrefix + ".description", null));
            consumer.setUrl(serverConfig.getString(consumerPrefix + ".url", null));
            consumer.setCallbackUrl(serverConfig.getString(consumerPrefix + ".callbackUrl", null));
            consumer.setSecret(serverConfig.getString(consumerPrefix + ".secret"));
            consumer.setAccessorSecret(serverConfig.getString(consumerPrefix + ".accessorsecret", null));
            consumer.setDefaultValidity(serverConfig.getInt(consumerPrefix + ".validity", 0));
            consumer.setRights(new HashSet<String>(getStringsOrSplit(consumerPrefix + ".rights", serverConfig)));
            consumer.setRecordModeEnabled(serverConfig.getBoolean(consumerPrefix + ".record", false));

            consumers.put(consumerKey, consumer);
        }
    }

    private static Collection<String> getStringsOrSplit(String name, ServerConfigurationService serverConfig) {
        String stringValue = serverConfig.getString(name, null);
        String[] values;
        if (stringValue != null)
            values = stringValue.split(",");
        else
            values = serverConfig.getStrings(name);

        return (values != null) ? Arrays.asList(values) : Collections.<String>emptyList();
    }

    @Override
    public void create(Consumer consumer) {
        throw new UnsupportedOperationException("Can't create a consumer in the server config");
    }

    @Override
    public Consumer get(String consumerId) {
        return consumers.get(consumerId);
    }

    @Override
    public Consumer update(Consumer consumer) {
        throw new UnsupportedOperationException("Can't update a consumer in the server config");
    }

    @Override
    public void remove(Consumer consumer) {
        throw new UnsupportedOperationException("Can't remove a consumer in the server config");
    }

    @Override
    public Collection<Consumer> getAll() {
        return consumers.values();
    }
}
