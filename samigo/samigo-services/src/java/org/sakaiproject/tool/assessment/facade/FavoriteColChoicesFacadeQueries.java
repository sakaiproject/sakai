/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.facade;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.sakaiproject.tool.assessment.data.dao.assessment.FavoriteColChoices;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class FavoriteColChoicesFacadeQueries implements FavoriteColChoicesFacadeQueriesAPI {

    @Setter private SessionFactory sessionFactory;

    public FavoriteColChoicesFacadeQueries() {
    }

    public void saveOrUpdate(final FavoriteColChoices choices) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<FavoriteColChoices> cq = cb.createQuery(FavoriteColChoices.class);
        Root<FavoriteColChoices> a = cq.from(FavoriteColChoices.class);

        cq.select(a).where(cb.equal(a.get("favoriteName"), choices.getFavoriteName()));

        List<FavoriteColChoices> favoriteList = session.createQuery(cq).getResultList();

        if (favoriteList != null) {
            Iterator iter = favoriteList.iterator();
            if (iter.hasNext()) {
                FavoriteColChoices fChoice = (FavoriteColChoices) iter.next();
                //remove the existing entry
                session.remove(fChoice);
            }
        }
        int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount().intValue();
        while (retryCount > 0) {
            try {
                session.persist(choices);
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem saving favoriteColChoices: " + e.getMessage());
                retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
            }
        }
    }

    public List<FavoriteColChoices> getFavoriteColChoicesByAgent(final String siteAgentId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<FavoriteColChoices> cq = cb.createQuery(FavoriteColChoices.class);
        Root<FavoriteColChoices> a = cq.from(FavoriteColChoices.class);

        cq.select(a).where(cb.equal(a.get("ownerStringId"), siteAgentId));

        return session.createQuery(cq).getResultList();
    }
}
