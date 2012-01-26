package org.sakaiproject.scorm.dao.hibernate;

import org.adl.datamodels.DMFactory;
import org.adl.datamodels.DMRequest;
import org.adl.datamodels.DataModel;
import org.adl.datamodels.SCODataManager;
import org.adl.datamodels.ieee.SCORM_2004_DM;
import org.adl.datamodels.ieee.ValidatorFactory;
import org.sakaiproject.scorm.dao.api.DataManagerDao;

public class DataManagerDaoImplTest extends AbstractServiceTest {
	DataManagerDao dataManagerDao;

	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		super.onSetUpBeforeTransaction();
		dataManagerDao = (DataManagerDao) getApplicationContext().getBean("org.sakaiproject.scorm.dao.api.DataManagerDao");
	}

	public void testLoad() {
	}

	public void testFindString() {
	}

	public void testFindStringStringStringLong() {
	}

	public void testFindStringStringStringBooleanLong() {
	}

	public void testFindLongStringLong() {
	}

	public void testFindLongStringLongString() {
	}

	public void testFindByActivityId() {
	}

	public void testSave() {
	}

	public void testUpdate() {
		
		SCODataManager dataManager = new SCODataManager();
		dataManager.setValidatorFactory(new ValidatorFactory());
		DataModel dm = dataManager.addDM(DMFactory.DM_SCORM_2004);
		dataManager.addDM(DMFactory.DM_SCORM_NAV);
		dataManager.addDM(DMFactory.DM_SSP);
		dataManagerDao.save(dataManager);
		
		dataManager.setValue(new DMRequest("cmi.interactions.1.id", "1"));
		
		dataManagerDao.update(dataManager);
	}

}
