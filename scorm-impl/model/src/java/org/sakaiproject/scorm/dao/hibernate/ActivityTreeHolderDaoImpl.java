package org.sakaiproject.scorm.dao.hibernate;

import java.util.List;

import org.sakaiproject.scorm.dao.api.ActivityTreeHolderDao;
import org.sakaiproject.scorm.model.api.ActivityTreeHolder;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class ActivityTreeHolderDaoImpl extends HibernateDaoSupport implements ActivityTreeHolderDao {

	public ActivityTreeHolder find(long contentPackageId, String learnerId) {
		List r = getHibernateTemplate().find(
				"from " + ActivityTreeHolder.class.getName()
						+ " where contentPackageId=? and learnerId=?", 
						new Object[]{ contentPackageId, learnerId });
		
		if (r.size() == 0)
			return null;
			
		ActivityTreeHolder holder = (ActivityTreeHolder) r.get(0);
		
		return holder;
	}

	public void save(ActivityTreeHolder holder) {
		getHibernateTemplate().saveOrUpdate(holder);
	}

}
