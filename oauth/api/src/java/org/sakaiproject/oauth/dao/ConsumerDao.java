/*
 * #%L
 * OAuth API
 * %%
 * Copyright (C) 2009 - 2013 Sakai Foundation
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

/**
 * Data access object for consumers (clients).
 *
 * @author Colin Hebert
 */
public interface ConsumerDao {
    void create(Consumer consumer);

    Consumer get(String consumerId);

    Consumer update(Consumer consumer);

    /**
     * Removes a consumer, making it impossible to connect through oAuth with its credentials.
     * <p>
     * A proper implementation of this method MUST also revoke every token associated with the consumer.
     * </p>
     *
     * @param consumer consumer to remove
     */
    void remove(Consumer consumer);

    Collection<Consumer> getAll();
}
