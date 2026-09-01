/******************************************************************************
 * $URL: $
 * $Id: $
 ******************************************************************************
 *
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *****************************************************************************/

package org.sakaiproject.config.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import org.sakaiproject.config.api.HibernateConfigItem;
import org.sakaiproject.config.api.HibernateConfigItemDao;
import org.sakaiproject.db.api.SqlService;
import org.springframework.transaction.annotation.Transactional;

/**
 * KNL-1063
 * HibernateConfigItemDaoImpl
 * Implementation for HibernateConfigItemDao
 *
 * @author Earle Nietzel
 *         Created on Mar 8, 2013
 */
@Slf4j
@Transactional
public class HibernateConfigItemDaoImpl implements HibernateConfigItemDao {
    private static String SAKAI_CONFIG_ITEM_SQL = "sakai_config_item";
    private SqlService sqlService;

    @Setter private SessionFactory sessionFactory;

    private boolean autoDdl;

    public void init() {
        if (autoDdl) {
            log.info("init: autoDDL " + SAKAI_CONFIG_ITEM_SQL);
            sqlService.ddl(this.getClass().getClassLoader(), SAKAI_CONFIG_ITEM_SQL);
        }
    }

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public void setAutoDdl(boolean autoDdl) {
        this.autoDdl = autoDdl;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#create(org.sakaiproject.config.api.HibernateConfigItem)
     */
    @Override
    public void create(HibernateConfigItem item) {
        if (item != null) {
            sessionFactory.getCurrentSession().save(item);
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#read(java.lang.Long)
     */
    @Override
    public HibernateConfigItem read(Long id) {
        if (id == null) {
            return null;
        }

        return (HibernateConfigItem) sessionFactory.getCurrentSession().get(HibernateConfigItem.class, id);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#update(org.sakaiproject.config.api.HibernateConfigItem)
     */
    @Override
    public void update(HibernateConfigItem item) {
        if (item == null) {
            return;
        }

        sessionFactory.getCurrentSession().update(item);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#delete(org.sakaiproject.config.api.HibernateConfigItem)
     */
    @Override
    public void delete(HibernateConfigItem item) {
        if (item == null) {
            return;
        }

        sessionFactory.getCurrentSession().delete(item);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#countByNode(java.lang.String)
     */
    public int countByNode(String node) {
        if (node == null) {
            return -1;
        }
        Long count = sessionFactory.getCurrentSession()
            .createQuery("select count(*) from HibernateConfigItem where node = :node", Long.class)
            .setParameter("node", node)
            .uniqueResult();

        return count != null ? count.intValue() : 0;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#countByNodeAndName(java.lang.String, java.lang.String)
     */
    @Override
    public int countByNodeAndName(String node, String name) {
        if (node == null || name == null) {
            return -1;
        }
        Long count = sessionFactory.getCurrentSession()
            .createQuery("select count(*) from HibernateConfigItem where node = :node and name = :name", Long.class)
            .setParameter("node", node)
            .setParameter("name", name)
            .uniqueResult();

        return count != null ? count.intValue() : 0;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#saveOrUpdateAll(java.util.List)
     */
    @Override
    public void saveOrUpdateAll(List<HibernateConfigItem> items) {
        if (items == null) {
            return;
        }

        for (HibernateConfigItem item : items) {
            if (item != null) {
                saveOrUpdate(item);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#saveOrUpdate(org.sakaiproject.config.api.HibernateConfigItem)
     */
    @Override
    public void saveOrUpdate(HibernateConfigItem item) {
        if (item == null) {
            return;
        }

        sessionFactory.getCurrentSession().saveOrUpdate(item);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#findAllByCriteriaByNode(java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Boolean, java.lang.Boolean, java.lang.Boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<HibernateConfigItem> findAllByCriteriaByNode(String node, String name, Boolean defaulted, Boolean registered, Boolean dynamic, Boolean secured) {
        if (node == null) { // TODO get the globals only
            return Collections.emptyList();
        }

        StringBuilder hql = new StringBuilder("from HibernateConfigItem where node = :node");
        if (name != null && !name.isEmpty()) {
            hql.append(" and name = :name");
        }
        if (defaulted != null) {
            hql.append(" and defaulted = :defaulted");
        }
        if (registered != null) {
            hql.append(" and registered = :registered");
        }
        if (dynamic != null) {
            hql.append(" and dynamic = :dynamic");
        }
        if (secured != null) {
            hql.append(" and secured = :secured");
        }

        Query<HibernateConfigItem> query = sessionFactory.getCurrentSession()
            .createQuery(hql.toString(), HibernateConfigItem.class)
                .setParameter("node", node);
        if (name != null && !name.isEmpty()) {
            query.setParameter("name", name);
        }
        if (defaulted != null) {
            query.setParameter("defaulted", defaulted);
        }
        if (registered != null) {
            query.setParameter("registered", registered);
        }
        if (dynamic != null) {
            query.setParameter("dynamic", dynamic);
        }
        if (secured != null) {
            query.setParameter("secured", secured);
        }

        // TODO throw away cases where node is null AND node is set (only keep node is set) - use order by name & node
        return query.list();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#findPollOnByNode(java.lang.String, java.util.Date, java.util.Date)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<HibernateConfigItem> findPollOnByNode(String node, Date onOrAfter, Date before) {
        StringBuilder hql = new StringBuilder("from HibernateConfigItem where node = :node");

        if (onOrAfter == null && before == null) {
            hql.append(" and pollOn is not null");
        } else {
            if (onOrAfter != null) {
                hql.append(" and pollOn >= :onOrAfter");
            }
            if (before != null) {
                hql.append(" and pollOn < :before");
            }
        }

        Query<HibernateConfigItem> query = sessionFactory.getCurrentSession()
            .createQuery(hql.toString(), HibernateConfigItem.class)
            .setParameter("node", node);
        if (onOrAfter != null) {
            query.setParameter("onOrAfter", onOrAfter);
        }
        if (before != null) {
            query.setParameter("before", before);
        }

        return query.list();
    }
}
