package org.sakaiproject.scorm.navigation;

public class SequencingEngine {

	
	public ADLLaunch navigate(User user, Activity current) {
		WrappedADLSequencer seq = SequencerFactory.getSequencer(ADL);
		
		// Has the activityTree
		// Has userid 
		// Has state -- to know user -- in order to get activityTree 
		// from DataModel
		
		seq.setValues();
		seq.navigate();
		
		
		// If this is successful, we want to record/persist whatever happened
		
		
		
		
	}
	
	
	
}
