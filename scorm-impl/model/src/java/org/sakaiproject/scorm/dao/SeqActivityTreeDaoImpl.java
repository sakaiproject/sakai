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

import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.SeqActivityTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao;
import org.sakaiproject.scorm.dao.api.SeqActivityTreeDao;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class SeqActivityTreeDaoImpl extends HibernateDaoSupport implements SeqActivityTreeDao {
	private static Log log = LogFactory.getLog(SeqActivityTreeDaoImpl.class);
			
	public ISeqActivityTree find(String courseId, String userId) {
		List r = getHibernateTemplate().find(
				"from " + SeqActivityTree.class.getName()
						+ " where mCourseID=? and mLearnerID=?", 
						new Object[]{ courseId, userId });
		
		
		log.info("SeqActivityTreeDAO::find: records: " + r.size());					
		
		
		if (r.size() == 0)
			return null;
			
		return (ISeqActivityTree) r.get(0);
	}
	
	public void save(ISeqActivityTree tree) {
		getHibernateTemplate().saveOrUpdate(tree);
	}
	
	
}
