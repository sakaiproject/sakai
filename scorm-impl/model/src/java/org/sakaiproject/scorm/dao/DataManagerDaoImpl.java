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
	
	public List<IDataManager> find(String courseId) {
		List r = getHibernateTemplate().find(
				"from " + SCODataManager.class.getName()
						+ " where courseId=? ", 
						new Object[]{ courseId });
		
		return r;
	}
	
	public IDataManager find(String courseId, String userId) {
				
		return find(courseId, userId, true);
	}
	
	
	public IDataManager find(String courseId, String userId, boolean fetchAll) {
		StringBuilder buffer = new StringBuilder();
		
		buffer.append("from ").append(SCODataManager.class.getName());
		
		if (fetchAll) 
			buffer.append(" fetch all properties ");
		
		buffer.append(" where courseId=? and userId=? ");
		
		
		List r = getHibernateTemplate().find(buffer.toString(), 
						new Object[]{ courseId, userId });
		
		if (log.isDebugEnabled())
			log.debug("DataManagerDaoImpl::find: records: " + r.size());	
		
		if (r.size() == 0)
			return null;
			
		SCODataManager dm = (SCODataManager)r.get(r.size() - 1);
		
		return dm;
	}
	
	
	

	public void save(IDataManager dataManager) {
		getHibernateTemplate().saveOrUpdate(dataManager);
	}
	
	
}
