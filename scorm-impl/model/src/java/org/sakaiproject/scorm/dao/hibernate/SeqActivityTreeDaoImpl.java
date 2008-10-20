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

import java.util.List;

import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.SeqActivityTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.dao.api.SeqActivityTreeDao;
import org.sakaiproject.scorm.model.api.SeqActivityTreeSnapshot;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class SeqActivityTreeDaoImpl extends HibernateDaoSupport implements SeqActivityTreeDao {
	private static Log log = LogFactory.getLog(SeqActivityTreeDaoImpl.class);
			
	public ISeqActivityTree find(long contentPackageId, String userId) {
		List r = getHibernateTemplate().find(
				"from " + SeqActivityTree.class.getName()
						+ " where contentPackageId=? and mLearnerID=?", 
						new Object[]{ contentPackageId, userId });
		
		
		if (log.isInfoEnabled())
			log.info("SeqActivityTreeDAO::find: records: " + r.size());					
		
		
		if (r.size() == 0)
			return null;
			
		return (ISeqActivityTree) r.get(0);
	}
	
	public SeqActivityTreeSnapshot findSnapshot(String courseId, String userId) {
		List r = getHibernateTemplate().find(
				"from " + SeqActivityTreeSnapshot.class.getName()
						+ " where mCourseID=? and mLearnerID=?", 
						new Object[]{ courseId, userId });
		
		if (log.isInfoEnabled())
			log.info("SeqActivityTreeDAO::findSnapshot: records: " + r.size());					
		
		
		if (r.size() == 0)
			return null;
			
		return (SeqActivityTreeSnapshot) r.get(0);
	}
	
	public List<SeqActivityTreeSnapshot> findUserSnapshots(String userId) {
		List r = getHibernateTemplate().find(
				"from " + SeqActivityTreeSnapshot.class.getName()
						+ " where mLearnerID=?", 
						new Object[]{ userId });
		
		if (log.isInfoEnabled())
			log.info("SeqActivityTreeDAO::findUserSnapshots: records: " + r.size());					
		
		return r;
	}
	
	
	public void save(ISeqActivityTree tree) {
		getHibernateTemplate().saveOrUpdate(tree);
	}
	
	
}
