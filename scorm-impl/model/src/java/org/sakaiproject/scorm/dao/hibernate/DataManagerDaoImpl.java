/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.adl.datamodels.DMElement;
import org.adl.datamodels.IDataManager;
import org.adl.datamodels.SCODataManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.dao.api.DataManagerDao;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

public class DataManagerDaoImpl extends HibernateDaoSupport implements DataManagerDao {
	private static final Log LOG = LogFactory.getLog(DataManagerDaoImpl.class);

	public List<IDataManager> find(long contentPackageId, String learnerId, long attemptNumber) {
		StringBuilder buffer = new StringBuilder();

		buffer.append("from ").append(SCODataManager.class.getName()).append(" where contentPackageId=? and userId=? and attemptNumber=? ");

		return (List<IDataManager>) getHibernateTemplate().find(buffer.toString(), new Object[] { contentPackageId, learnerId, attemptNumber });
	}

	public IDataManager find(long contentPackageId, String learnerId, long attemptNumber, String scoId) {
		StringBuilder buffer = new StringBuilder();

		buffer.append("from ").append(SCODataManager.class.getName()).append(" where contentPackageId=? and userId=? and attemptNumber=? and scoId=?");

		List r = getHibernateTemplate().find(buffer.toString(), new Object[] { contentPackageId, learnerId, attemptNumber, scoId });

		if (r.isEmpty())
		{
			return null;
		}

		SCODataManager dm = (SCODataManager) r.get(0);

		return dm;
	}

	public List<IDataManager> find(String courseId) {
		List r = getHibernateTemplate().find("from " + SCODataManager.class.getName() + " where courseId=? ", new Object[] { courseId });

		return r;
	}

	public IDataManager find(String courseId, String scoId, String userId, boolean fetchAll, long attemptNumber) {
		StringBuilder buffer = new StringBuilder();

		buffer.append("from ").append(SCODataManager.class.getName());

		if (fetchAll) {
			buffer.append(" fetch all properties ");
		}

		buffer.append(" where courseId=? and scoId=? and userId=? and attemptNumber=? ");

		List r = getHibernateTemplate().find(buffer.toString(), new Object[] { courseId, scoId, userId, attemptNumber });

		if (LOG.isDebugEnabled()) {
			LOG.debug("DataManagerDaoImpl::find: records: " + r.size());
		}

		if (r.isEmpty())
		{
			return null;
		}

		SCODataManager dm = (SCODataManager) r.get(r.size() - 1);

		return dm;
	}

	public IDataManager find(String courseId, String scoId, String userId, long attemptNumber) {

		return find(courseId, scoId, userId, true, attemptNumber);
	}

	public IDataManager findByActivityId(long contentPackageId, String activityId, String userId, long attemptNumber) {
		StringBuilder buffer = new StringBuilder();

		buffer.append("from ").append(SCODataManager.class.getName());
		buffer.append(" where contentPackageId=? and activityId=? and userId=? and attemptNumber=? ");

		List r = getHibernateTemplate().find(buffer.toString(), new Object[] { contentPackageId, activityId, userId, attemptNumber });

		if (LOG.isDebugEnabled()) {
			LOG.debug("DataManagerDaoImpl::findByActivityId: records: " + r.size());
		}

		if (r.isEmpty())
		{
			return null;
		}

		SCODataManager dm = (SCODataManager) r.get(r.size() - 1);

		return dm;
	}

	public IDataManager load(long id) {
		return (IDataManager) getHibernateTemplate().load(SCODataManager.class, id);
		//	
		//		List r = getHibernateTemplate().find(
		//				"from " + SCODataManager.class.getName()
		//						+ " where id=? ", 
		//						new Object[]{ id });
		//	
		//		if (r != null && r.size() > 0)
		//			return (IDataManager)r.get(0);
		//		
		//		return null;
	}

	private void merge(DMElement element) {
		/*
		if (element.getDescription() != null) {
			getHibernateTemplate().saveOrUpdate(element.getDescription());
		}
		
		Map<String, DMElement> children = element.getChildren();
		Collection<DMElement> values = (children != null ? children.values() : Collections.<DMElement>emptySet());
		for (DMElement el : values) {
		    merge(el);
		}
		getHibernateTemplate().saveOrUpdate(element);
		*/

		/*List<DMDelimiter> delims = element.getDelimiters();
		
		if (delims != null) {
			for (DMDelimiter delim : delims) {
				if (delim.getDescription() != null)
					getHibernateTemplate().saveOrUpdate(delim.getDescription());
					//getHibernateTemplate().merge(delim.getDescription());
			}
		}*/
	}

	public void save(IDataManager dataManager) {
		saveOrUpdate(dataManager, true);
	}

	private void saveOrUpdate(boolean isFirstTime, Object object) {
		/*if (isFirstTime)
			getHibernateTemplate().save(object);
		else
			getHibernateTemplate().update(object);
		*/
		getHibernateTemplate().saveOrUpdate(object);

	}

	private void saveOrUpdate(DMElement element, boolean isFirstTime) {
		if (element.getParent() != null) {
			saveOrUpdate(element.getParent(), isFirstTime);
		}

		if (element.getDescription() != null) {
			saveOrUpdate(isFirstTime, element.getDescription());
		}

	}

	private void saveOrUpdate(IDataManager dataManager, boolean isFirstTime) {
		dataManager.setLastModifiedDate(new Date());

		/*
		Map<String, DataModel> dataModels = dataManager.getDataModels();
		
		if (dataModels != null) {
			for (java.util.Iterator<String> it = dataModels.keySet().iterator();it.hasNext();) {
				String key = it.next();
				DataModel dm = dataModels.get(key);
				
				if (dm instanceof SCORM_2004_DM) {
					SCORM_2004_DM rdm = (SCORM_2004_DM)dm;
					
					Map<String, DMElement> map = rdm.getElements();
					
					for (DMElement element : map.values()) {
						merge(element);
					}
										
				} else if (dm instanceof SCORM_2004_NAV_DM) {
					SCORM_2004_NAV_DM rdm = (SCORM_2004_NAV_DM)dm;
					
					Map<String, DMElement> map = rdm.getElements();
					
					for (DMElement element : map.values()) {
						merge(element);
					}
					
				} else {
					SSP_DataModel rdm = (SSP_DataModel)dm;
					
					List<DMElement> list = rdm.getManagedElements();
					
					for (DMElement element : list) {
						merge(element);
					}
				}
				
			}

		}
		*/

		saveOrUpdate(isFirstTime, dataManager);

	}

	public void update(IDataManager dataManager) {
		saveOrUpdate(dataManager, false);
	}

}
