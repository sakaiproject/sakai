package org.sakaiproject.scorm.adl.impl;

import org.adl.datamodels.IDataManager;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.ISequencer;
import org.adl.sequencer.impl.ADLSequencer;
import org.sakaiproject.scorm.adl.ADLConsultant;
import org.sakaiproject.scorm.dao.api.DataManagerDao;
import org.sakaiproject.scorm.dao.api.SeqActivityTreeDao;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.model.api.ScoBean;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.ScormResourceService;

public abstract class ADLConsultantImpl implements ADLConsultant {

	protected abstract ScormResourceService resourceService();
	protected abstract DataManagerDao dataManagerDao();
	protected abstract SeqActivityTreeDao seqActivityTreeDao();
	
	
	public ISeqActivityTree getActivityTree(SessionBean sessionBean) {
		// First, we check to see if the tree is cached in the session bean 
		ISeqActivityTree tree = sessionBean.getTree();
		
		if (tree == null) {
			// If not, we look to see if there's a modified version in the data store
			tree = seqActivityTreeDao().find(sessionBean.getCourseId(), sessionBean.getLearnerId());
			
			if (tree == null) {
				// Finally, if all else fails, we look up the prototype version - this is the first time
				// the user has launched the content package
				ContentPackageManifest manifest = getManifest(sessionBean);
				tree = manifest.getActTreePrototype();
				tree.setCourseID(sessionBean.getCourseId());
				tree.setLearnerID(sessionBean.getLearnerId());
			}
			
			sessionBean.setTree(tree);
		}
		
		return tree;
	}
	
	public IDataManager getDataManager(SessionBean sessionBean, ScoBean scoBean) {
		if (scoBean.getDataManager() == null)
			scoBean.setDataManager(dataManagerDao().find(sessionBean.getCourseId(), scoBean.getScoId(), sessionBean.getLearnerId(), sessionBean.getAttemptNumber()));
		
		return scoBean.getDataManager();
	}
	

	public ISequencer getSequencer(ISeqActivityTree tree) {
        // Create the sequencer and set the tree		
        ISequencer sequencer = new ADLSequencer();
        sequencer.setActivityTree(tree);
        
        return sequencer;
	}
	
	public ContentPackageManifest getManifest(SessionBean sessionBean) {
		// First, check to see if the manifest is cached in the session bean
		ContentPackageManifest manifest = sessionBean.getManifest();
		
		if (manifest == null) {
			manifest = resourceService().getManifest(sessionBean.getCourseId(), null);
			//sessionBean.setManifest(manifest);
		}
		
		return manifest;
	}

}
