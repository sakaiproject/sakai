package org.sakaiproject.scorm.ui.player.behaviors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SCORM13APITest extends AbstractSCORM13APBase {

	@Before
	public void onSetUpBeforeTransaction() throws Exception {
		super.onSetUpBeforeTransaction();
	}

	@Test
	public void testSimple() {
		Assert.assertEquals("true", scorm13api.Initialize(""));
		Assert.assertEquals("OBJ_FLASH", scorm13api.GetValue("cmi.objectives.0.id"));
		Assert.assertEquals("OBJ_DIRECTOR", scorm13api.GetValue("cmi.objectives.1.id"));
		// Count
		Assert.assertEquals("2", scorm13api.GetValue("cmi.objectives._count"));
		
		// 301
		Assert.assertEquals("false", scorm13api.SetValue("cmi.interactions.1.id", "koe"));
		Assert.assertEquals("301", scorm13api.GetLastError());
		
		// Count
		Assert.assertEquals("0", scorm13api.GetValue("cmi.interactions._count"));
		// Ok
		Assert.assertEquals("true", scorm13api.SetValue("cmi.interactions.0.id", "koe"));
		Assert.assertEquals("0", scorm13api.GetLastError());

		// Count
		Assert.assertEquals("1", scorm13api.GetValue("cmi.interactions._count"));
		
		scorm13api.SetValue("cmi.session_time", "PT1H5M");
		scorm13api.Commit("");
		scorm13api.SetValue("does.not.extist", "que");
		
		scorm13api.Terminate("");
	}

}
