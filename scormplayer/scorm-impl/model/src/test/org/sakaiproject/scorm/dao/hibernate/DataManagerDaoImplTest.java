/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import static org.sakaiproject.scorm.api.ScormConstants.CMI_INTERACTIONS_ROOT;
import org.sakaiproject.scorm.dao.api.DataManagerDao;

public class DataManagerDaoImplTest extends AbstractServiceTest
{
	DataManagerDao dataManagerDao;

	@Before
	public void onSetUpBeforeTransaction() throws Exception
	{
		dataManagerDao = (DataManagerDao) applicationContext.getBean("org.sakaiproject.scorm.dao.api.DataManagerDao");
	}

	@Test
	public void testSimple()
	{
		SCODataManager dataManager = new SCODataManager();
		ValidatorFactory validatorFactory = new ValidatorFactory();
		dataManager.addDM(DMFactory.DM_SCORM_2004, validatorFactory);
		dataManager.addDM(DMFactory.DM_SCORM_NAV, validatorFactory);
		dataManager.addDM(DMFactory.DM_SSP, validatorFactory);
		dataManager.setScoId("sco01");
		dataManagerDao.save(dataManager);
		dataManager.setValue(new DMRequest(CMI_INTERACTIONS_ROOT + "0.id", "1"), validatorFactory);

		dataManagerDao.update(dataManager);

		Assert.assertEquals(0, dataManager.getValue(new DMRequest(CMI_INTERACTIONS_ROOT + "0.id"), new DMProcessingInfo()));
		Assert.assertEquals(DMErrorCodes.OUT_OF_RANGE, dataManager.getValue(new DMRequest(CMI_INTERACTIONS_ROOT + "1.id"), new DMProcessingInfo()));
	}
}
