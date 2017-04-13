package org.sakaiproject.tool.assessment.facade;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.sakaiproject.tool.assessment.data.dao.assessment.FavoriteColChoices;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import lombok.extern.slf4j.Slf4j;

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
