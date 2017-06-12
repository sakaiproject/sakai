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

import org.sakaiproject.oauth.domain.Accessor;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Colin Hebert
 */
public class MemoryAccessorDao implements AccessorDao {
    private final Map<String, Accessor> accessors;

    public MemoryAccessorDao(Map<String, Accessor> accessors) {
        this.accessors = accessors;
    }

    @Override
    public void create(Accessor accessor) {
        accessors.put(accessor.getToken(), accessor);
    }

    @Override
    public Accessor get(String accessorId) {
        return accessors.get(accessorId);
    }

    @Override
    public Collection<Accessor> getByUser(String userId) {
        Collection<Accessor> retrievedAccessors = new LinkedList<Accessor>();
        for (Accessor accessor : accessors.values()) {
            if (userId.equals(accessor.getUserId()))
                retrievedAccessors.add(accessor);
        }

        return retrievedAccessors;
    }

    @Override
    public Collection<Accessor> getByConsumer(String consumerId) {
        Collection<Accessor> retrievedAccessors = new LinkedList<Accessor>();
        for (Accessor accessor : accessors.values()) {
            if (consumerId.equals(accessor.getConsumerId()))
                retrievedAccessors.add(accessor);
        }

        return retrievedAccessors;
    }

    @Override
    public Accessor update(Accessor accessor) {
        accessors.put(accessor.getToken(), accessor);
        return get(accessor.getToken());
    }

    @Override
    public void remove(Accessor accessor) {
        accessors.remove(accessor.getToken());
    }

    @Override
    public void markExpiredAccessors() {
        Collection<String> expiredIds = new LinkedList<String>();
        for (Accessor accessor : accessors.values()) {
            if (accessor.getExpirationDate().before(new Date()))
                expiredIds.add(accessor.getToken());
        }

        for (String expiredId : expiredIds) {
            accessors.remove(expiredId);
        }
    }
}
