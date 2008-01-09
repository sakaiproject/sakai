package org.sakaiproject.scorm.dao.hibernate;

import java.util.List;

import org.sakaiproject.scorm.dao.api.SeqActivityDao;
import org.sakaiproject.scorm.model.api.SeqActivitySnapshot;
import org.sakaiproject.scorm.model.api.SeqActivityTreeSnapshot;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class SeqActivityDaoImpl extends HibernateDaoSupport implements SeqActivityDao {

	public SeqActivitySnapshot findSnapshot(String activityId) {
		List<SeqActivitySnapshot> r = getHibernateTemplate().find(
				"from " + SeqActivitySnapshot.class.getName()
						+ " where activityId=?", 
						new Object[]{ activityId });
		
		if (r.size() == 0)
			return null;
			
		for (SeqActivitySnapshot snapshot : r) {
			if (snapshot.getScoId() != null)
				return snapshot;
		}
		
		
		return null;
	}

}
