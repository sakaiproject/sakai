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
package org.sakaiproject.scorm.ui.player.behaviors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.sakaiproject.scorm.api.ScormConstants.*;

public class SCORM13APITest extends AbstractSCORM13APBase
{
	@Before
	@Override
	public void onSetUpBeforeTransaction() throws Exception
	{
		super.onSetUpBeforeTransaction();
	}

	@Test
	public void testSimple()
	{
		Assert.assertEquals("true", scorm13api.Initialize(""));
		Assert.assertEquals("OBJ_FLASH", scorm13api.GetValue(CMI_OBJECTIVES_ROOT + "0.id"));
		Assert.assertEquals("OBJ_DIRECTOR", scorm13api.GetValue(CMI_OBJECTIVES_ROOT+ "1.id"));

		// Count
		Assert.assertEquals("2", scorm13api.GetValue(CMI_OBJECTIVES_COUNT));

		// 301
		Assert.assertEquals("false", scorm13api.SetValue(CMI_INTERACTIONS_ROOT + "1.id", "koe"));
		Assert.assertEquals("301", scorm13api.GetLastError());

		// Count
		Assert.assertEquals("0", scorm13api.GetValue(CMI_INTERACTIONS_COUNT));

		// Ok
		Assert.assertEquals("true", scorm13api.SetValue(CMI_INTERACTIONS_ROOT + "0.id", "koe"));
		Assert.assertEquals("0", scorm13api.GetLastError());

		// Count
		Assert.assertEquals("1", scorm13api.GetValue(CMI_INTERACTIONS_COUNT));

		scorm13api.SetValue(CMI_SESSION_TIME, "PT1H5M");
		scorm13api.Commit("");
		scorm13api.SetValue("does.not.extist", "que");
		scorm13api.Terminate("");
	}
}
