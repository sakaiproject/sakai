package org.sakaiproject.scorm.ui.player.behaviors;

public class SCORM13APITest extends AbstractSCORM13APBase {

	public void testSimple() {
		assertEquals("true", scorm13api.Initialize(""));
		assertEquals("OBJ_FLASH", scorm13api.GetValue("cmi.objectives.0.id"));
		assertEquals("OBJ_DIRECTOR", scorm13api.GetValue("cmi.objectives.1.id"));
		// Count
		assertEquals("2", scorm13api.GetValue("cmi.objectives._count"));
		
		// 301
		assertEquals("false", scorm13api.SetValue("cmi.interactions.1.id", "koe"));
		assertEquals("301", scorm13api.GetLastError());
		
		// Count
		assertEquals("0", scorm13api.GetValue("cmi.interactions._count"));
		// Ok
		assertEquals("true", scorm13api.SetValue("cmi.interactions.0.id", "koe"));
		assertEquals("0", scorm13api.GetLastError());

		// Count
		assertEquals("1", scorm13api.GetValue("cmi.interactions._count"));
		
		scorm13api.SetValue("cmi.session_time", "PT1H5M");
		scorm13api.Commit("");
		scorm13api.SetValue("does.not.extist", "que");
		
		scorm13api.Terminate("");
	}

}
