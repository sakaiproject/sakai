package org.sakaiproject.tool.assessment.facade;

import java.sql.SQLException;

import java.util.Iterator;
import java.util.List;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.tool.assessment.data.dao.assessment.FavoriteColChoices;
import org.sakaiproject.tool.assessment.data.dao.assessment.FavoriteColChoicesItem;
import org.sakaiproject.tool.assessment.data.dao.shared.TypeD;

import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.sakaiproject.tool.assessment.services.PersistenceService;


public class FavoriteColChoicesFacadeQueries extends HibernateDaoSupport 
					implements FavoriteColChoicesFacadeQueriesAPI {
	private static Log log = LogFactory.getLog(FavoriteColChoicesFacadeQueries.class);
	 public FavoriteColChoicesFacadeQueries () {
	  }


	  public void saveOrUpdate(final FavoriteColChoices choices) {
		    
		  List favoriteList = null;
		  final String query = "from FavoriteColChoices as a " +
		  "where a.favoriteName=? ";
		  HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					Query q = session.createQuery(query);
					q.setString(0, choices.getFavoriteName());
					return q.list();
				};
			};
			favoriteList = getHibernateTemplate().executeFind(hcb);
			if(favoriteList != null){
				Iterator iter = favoriteList.iterator();
				if(iter.hasNext()){
					FavoriteColChoices fChoice = (FavoriteColChoices)iter.next();
					//remove the existing entry
					getHibernateTemplate().delete(fChoice);
				}
			}
				int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
				while (retryCount > 0){
					try {

						getHibernateTemplate().save(choices);
						retryCount = 0;
					}
					catch (Exception e) {
						log.warn("problem saving favoriteColChoices: "+e.getMessage());
						retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
					}
				}
			
		  }
	
	public List getFavoriteColChoicesByAgent(final String siteAgentId){
		
			final String query = "from FavoriteColChoices as a " +
			"where a.ownerStringId=? ";
			//"order by b.sequence";
					
		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(query);
				q.setString(0, siteAgentId);
				return q.list();
			};
		};
		return getHibernateTemplate().executeFind(hcb);
		
	}
}
