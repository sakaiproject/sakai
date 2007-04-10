package org.adl.datamodels;

public class SCODataManagerTest extends HibernateTestCase {

	public void testPersistence() throws Exception {
		SCODataManager dataManager = new SCODataManager();
		
		dataManager.addDM(DMFactory.DM_SCORM_2004);
		
		dataManager.initialize();
		
		put(dataManager);
		
		dataManager.terminate();
		
	}
	
	
}
