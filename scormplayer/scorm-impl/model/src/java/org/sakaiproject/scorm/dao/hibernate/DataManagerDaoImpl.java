/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.adl.datamodels.IDataManager;
import org.adl.datamodels.SCODataManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.dao.api.DataManagerDao;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


public class DataManagerDaoImpl extends HibernateDaoSupport implements DataManagerDao {
	private static Log log = LogFactory.getLog(DataManagerDaoImpl.class);
	
	public IDataManager load(long id) {
		return (IDataManager)getHibernateTemplate().load(SCODataManager.class, id);
	}
	
	public List<IDataManager> find(String courseId) {
		List r = getHibernateTemplate().find(
				"from " + SCODataManager.class.getName()
						+ " where courseId=? ", 
						new Object[]{ courseId });
		
		return r;
	}
	
	public IDataManager find(String courseId, String scoId, String userId, long attemptNumber) {
				
		return find(courseId, scoId, userId, true, attemptNumber);
	}
	
	
	public IDataManager find(String courseId, String scoId, String userId, boolean fetchAll, long attemptNumber) {
		StringBuilder buffer = new StringBuilder();
		
		buffer.append("from ").append(SCODataManager.class.getName());
		
		if (fetchAll) 
			buffer.append(" fetch all properties ");
		
		buffer.append(" where courseId=? and scoId=? and userId=? and attemptNumber=? ");
		
		
		List r = getHibernateTemplate().find(buffer.toString(), 
						new Object[]{ courseId, scoId, userId, attemptNumber });
		
		if (log.isDebugEnabled())
			log.debug("DataManagerDaoImpl::find: records: " + r.size());	
		
		if (r.size() == 0)
			return null;
			
		SCODataManager dm = (SCODataManager)r.get(r.size() - 1);
		
		return dm;
	}
	
	public List<IDataManager> find(long contentPackageId, String learnerId, long attemptNumber) {
		StringBuilder buffer = new StringBuilder();
		
		buffer.append("from ").append(SCODataManager.class.getName())
			.append(" where contentPackageId=? and userId=? and attemptNumber=? ");
	
		return getHibernateTemplate().find(buffer.toString(), 
				new Object[]{ contentPackageId, learnerId, attemptNumber });
	}
	
	public IDataManager findByActivityId(long contentPackageId, String activityId, String userId, long attemptNumber) {
		StringBuilder buffer = new StringBuilder();
		
		buffer.append("from ").append(SCODataManager.class.getName());
		buffer.append(" where contentPackageId=? and activityId=? and userId=? and attemptNumber=? ");
		
		
		List r = getHibernateTemplate().find(buffer.toString(), 
						new Object[]{ contentPackageId, activityId, userId, attemptNumber });
		
		if (log.isDebugEnabled())
			log.debug("DataManagerDaoImpl::findByActivityId: records: " + r.size());	
		
		if (r.size() == 0)
			return null;
			
		SCODataManager dm = (SCODataManager)r.get(r.size() - 1);
		
		return dm;
	}

	public void save(IDataManager dataManager) {
		dataManager.setLastModifiedDate(new Date());
		getHibernateTemplate().saveOrUpdate(dataManager);
	}
	
	
}
