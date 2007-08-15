package org.sakaiproject.scorm.dao.api;

import org.adl.sequencer.ISeqActivityTree;

public interface SeqActivityTreeDao {

	public ISeqActivityTree find(String courseId, String userId);
	
	public void save(ISeqActivityTree tree);

}
