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
		getHibernateTemplate().save(tree);
	}
	
	
}
