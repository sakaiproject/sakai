/*
 * #%L
 * OAuth In-memory DAO
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

import org.sakaiproject.oauth.domain.Consumer;

import java.util.Collection;
import java.util.Map;

/**
 * @author Colin Hebert
 */
public class MemoryConsumerDao implements ConsumerDao {
    private final Map<String, Consumer> consumers;

    public MemoryConsumerDao(Map<String, Consumer> consumers) {
        this.consumers = consumers;
    }

    @Override
    public void create(Consumer consumer) {
        // TODO: Throw an exception if the consumer already exists?
        consumers.put(consumer.getId(), consumer);
    }

    @Override
    public Consumer get(String consumerId) {
        return consumers.get(consumerId);
    }

    @Override
    public Consumer update(Consumer consumer) {
        consumers.put(consumer.getId(), consumer);
        // Two steps, to be sure, because we don't know the Map implementation
        return get(consumer.getId());
    }

    @Override
    public void remove(Consumer consumer) {
        consumers.remove(consumer.getId());
    }

    @Override
    public Collection<Consumer> getAll() {
        return consumers.values();
    }
}
