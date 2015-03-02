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

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.sakaiproject.oauth.domain.Accessor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class HibernateAccessorDao extends HibernateDaoSupport implements AccessorDao {
    @Override
    public void create(final Accessor accessor) {
        getHibernateTemplate().save(accessor);
    }

    @Override
    public Accessor get(String accessorId) {
        return (Accessor) getHibernateTemplate().get(Accessor.class, accessorId);
    }

    @Override
    public List<Accessor> getByUser(String userId) {
        return (List<Accessor>) getHibernateTemplate().find(
                "FROM Accessor a WHERE a.userId = ?",
                new Object[]{userId});
    }

    @Override
    public Collection<Accessor> getByConsumer(String consumerId) {
        return (List<Accessor>) getHibernateTemplate().find(
                "FROM Accessor a WHERE a.consumerId = ?",
                new Object[]{consumerId});
    }

    @Override
    public void markExpiredAccessors() {
        getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.createQuery(
                        "UPDATE Accessor a SET a.status=? WHERE a.expirationDate < ?")
                        .setParameter(0, Accessor.Status.EXPIRED)
                        .setDate(1, new Date())
                        .executeUpdate();
                return null;
            }
        });
    }


    @Override
    public Accessor update(Accessor accessor) {
        getHibernateTemplate().update(accessor);
        return accessor;
    }

    @Override
    public void remove(Accessor accessor) {
        getHibernateTemplate().delete(accessor);
    }
}
