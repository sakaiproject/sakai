package org.sakaiproject.scorm.service.api;

import org.adl.datamodels.IDataManager;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.ISequencer;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.model.api.SessionBean;

public interface ADLManager {

	public ISeqActivityTree getActivityTree(SessionBean sessionBean);
	
	public IDataManager getDataManager(SessionBean sessionBean, ScoBean scoBean);

	public ContentPackageManifest getManifest(SessionBean sessionBean);
	
	public ISequencer getSequencer(ISeqActivityTree tree);
	
}
