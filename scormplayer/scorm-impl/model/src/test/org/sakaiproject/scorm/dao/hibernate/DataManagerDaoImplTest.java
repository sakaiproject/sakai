package org.sakaiproject.scorm.dao.hibernate;

import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.DMFactory;
import org.adl.datamodels.DMProcessingInfo;
import org.adl.datamodels.DMRequest;
import org.adl.datamodels.SCODataManager;
import org.adl.datamodels.ieee.ValidatorFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.scorm.dao.api.DataManagerDao;

public class DataManagerDaoImplTest extends AbstractServiceTest {
	DataManagerDao dataManagerDao;

	@Before
	public void onSetUpBeforeTransaction() throws Exception {
		dataManagerDao = (DataManagerDao) applicationContext.getBean("org.sakaiproject.scorm.dao.api.DataManagerDao");
	}

	@Test
	public void testSimple() {

		SCODataManager dataManager = new SCODataManager();
		ValidatorFactory validatorFactory = new ValidatorFactory();
		dataManager.addDM(DMFactory.DM_SCORM_2004, validatorFactory);
		dataManager.addDM(DMFactory.DM_SCORM_NAV, validatorFactory);
		dataManager.addDM(DMFactory.DM_SSP, validatorFactory);
		dataManager.setScoId("sco01");
		dataManagerDao.save(dataManager);
		dataManager.setValue(new DMRequest("cmi.interactions.0.id", "1"), validatorFactory);

		dataManagerDao.update(dataManager);
		
		Assert.assertEquals(0, dataManager.getValue(new DMRequest("cmi.interactions.0.id"), new DMProcessingInfo()));
		Assert.assertEquals(DMErrorCodes.OUT_OF_RANGE, dataManager.getValue(new DMRequest("cmi.interactions.1.id"), new DMProcessingInfo()));
	}

}
