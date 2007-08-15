package org.sakaiproject.scorm.dao;

import java.util.List;

import org.adl.datamodels.IDataManager;
import org.adl.datamodels.SCODataManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.dao.api.DataManagerDao;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


public class DataManagerDaoImpl extends HibernateDaoSupport implements DataManagerDao {
	private static Log log = LogFactory.getLog(DataManagerDaoImpl.class);
	
	public IDataManager find(String courseId, String userId) {
				
		List r = getHibernateTemplate().find(
				"from " + SCODataManager.class.getName()
						+ " where courseId=? and userId=?", 
						new Object[]{ courseId, userId });
		log.info("DataManagerDaoImpl::find: records: " + r.size());	
		
		if (r.size() == 0)
			return null;
			
		SCODataManager dm = (SCODataManager)r.get(r.size() - 1);
		
		return dm;
	}

	public void save(IDataManager dataManager) {
		getHibernateTemplate().saveOrUpdate(dataManager);
	}
	
	
}
