package org.adl.datamodels.ieee;

import junit.framework.TestCase;

import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.DMFactory;
import org.adl.datamodels.DMProcessingInfo;
import org.adl.datamodels.DMRequest;
import org.adl.datamodels.DataModel;

public class SCORM_2004_DMTest extends TestCase {
	DataModel dm;
	IValidatorFactory validatorFactory;

	private String getElementValue(String element) {
		DMProcessingInfo oInfo = new DMProcessingInfo();
		DMRequest request = new DMRequest(element);
		request.getNextToken();
		int value = dm.getValue(request, oInfo);
		assertEquals(DMErrorCodes.NO_ERROR, value);
		String val = oInfo.mValue;
		return val;
	};

	@Override
	protected void setUp() throws Exception {
		validatorFactory = new ValidatorFactory();
		dm = DMFactory.createDM(DMFactory.DM_SCORM_2004, validatorFactory);
	}

	public void testSimple() {
		assertEquals(DMErrorCodes.NO_ERROR, dm.initialize());
		assertEquals("cmi", dm.getDMBindingString());
		assertEquals("1.0", getElementValue("cmi._version"));
		{
			DMRequest request = new DMRequest("cmi.comments_from_learner.0.comment", "{lang=en}Characterstring in the English language");
			request.getNextToken();
			assertEquals(DMErrorCodes.NO_ERROR, dm.setValue(request, validatorFactory));
		}
		assertEquals("comments_from_learner", dm.getDMElement("comments_from_learner").getDMElementBindingString());
		{
			assertEquals("Characterstring in the English language", getElementValue("cmi.comments_from_learner.0.comment"));
			assertEquals("1", getElementValue("cmi.comments_from_learner._count"));
			assertTrue(getElementValue("cmi.comments_from_learner._children").contains("comment"));
			assertTrue(getElementValue("cmi.comments_from_learner._children").contains("location"));
			assertTrue(getElementValue("cmi.comments_from_learner._children").contains("timestamp"));
			System.out.println(dm.toString());
		}
		{
			DMRequest request = new DMRequest("cmi.comments_from_learner.2.comment");
			request.getNextToken();
			assertEquals(DMErrorCodes.OUT_OF_RANGE, dm.getValue(request, new DMProcessingInfo()));
		}
		{
			DMRequest request = new DMRequest("cmi.comments_from_learner.1.comment", "{lang=de}Characterstring in the German language");
			request.getNextToken();
			assertEquals(DMErrorCodes.NO_ERROR, dm.setValue(request, validatorFactory));
			assertEquals("2", getElementValue("cmi.comments_from_learner._count"));
			assertEquals("{lang=de}Characterstring in the German language", getElementValue("cmi.comments_from_learner.1.comment"));
		}
	}
}
