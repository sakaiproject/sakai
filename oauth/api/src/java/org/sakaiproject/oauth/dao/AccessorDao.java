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

import org.sakaiproject.oauth.domain.Accessor;

import java.util.Collection;

/**
 * Data access object for accessors (tokens).
 *
 * @author Colin Hebert
 */
public interface AccessorDao {
    void create(Accessor accessor);

    Accessor get(String accessorId);

    /**
     * Gets every accessor for a specific user.
     *
     * @param userId user associated with accessors
     * @return accessors for a user
     */
    Collection<Accessor> getByUser(String userId);

    /**
     * Gets every accessor created for a consumer.
     * @param consumerId consumer responsible of these accessors
     * @return accessors for a consumer
     */
    Collection<Accessor> getByConsumer(String consumerId);

    Accessor update(Accessor accessor);

    void remove(Accessor accessor);

    /**
     * Checks every accessor and set them as expired if the date of expiration has passed.
     */
    void markExpiredAccessors();
}
