/*
 * #%L
 * OAuth Hibernate DAO
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

import org.springframework.orm.hibernate4.support.HibernateDaoSupport;
import org.sakaiproject.oauth.domain.Consumer;

import java.util.Collection;

public class HibernateConsumerDao extends HibernateDaoSupport implements ConsumerDao {
    @Override
    public void create(final Consumer consumer) {
        getHibernateTemplate().save(consumer);
    }

    @Override
    public Consumer get(String consumerId) {
        return (Consumer) getHibernateTemplate().get(Consumer.class, consumerId);
    }

    @Override
    public Consumer update(Consumer consumer) {
        getHibernateTemplate().update(consumer);
        return consumer;
    }

    @Override
    public void remove(Consumer consumer) {
        getHibernateTemplate().delete(consumer);
    }

    @Override
    public Collection<Consumer> getAll() {
        return getHibernateTemplate().loadAll(Consumer.class);
    }
}
