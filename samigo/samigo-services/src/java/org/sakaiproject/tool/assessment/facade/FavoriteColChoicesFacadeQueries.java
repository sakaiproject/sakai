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

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
import org.sakaiproject.tool.assessment.data.dao.assessment.FavoriteColChoices;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

@Slf4j
public class FavoriteColChoicesFacadeQueries extends HibernateDaoSupport implements FavoriteColChoicesFacadeQueriesAPI {

    public FavoriteColChoicesFacadeQueries() {
    }

    public void saveOrUpdate(final FavoriteColChoices choices) {

        HibernateCallback<List<FavoriteColChoices>> hcb = session -> {
            Query q = session.createQuery("from FavoriteColChoices as a where a.favoriteName = :name");
            q.setString("name", choices.getFavoriteName());
            return q.list();
        };
        List<FavoriteColChoices> favoriteList = getHibernateTemplate().execute(hcb);
        if (favoriteList != null) {
            Iterator iter = favoriteList.iterator();
            if (iter.hasNext()) {
                FavoriteColChoices fChoice = (FavoriteColChoices) iter.next();
                //remove the existing entry
                getHibernateTemplate().delete(fChoice);
            }
        }
        int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount().intValue();
        while (retryCount > 0) {
            try {
                getHibernateTemplate().save(choices);
                retryCount = 0;
            } catch (Exception e) {
                log.warn("problem saving favoriteColChoices: " + e.getMessage());
                retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
            }
        }
    }

    public List<FavoriteColChoices> getFavoriteColChoicesByAgent(final String siteAgentId) {

        HibernateCallback<List<FavoriteColChoices>> hcb = session -> {
            Query q = session.createQuery("from FavoriteColChoices as a where a.ownerStringId = :site");
            q.setString("site", siteAgentId);
            return q.list();
        };
        return getHibernateTemplate().execute(hcb);
    }
}
