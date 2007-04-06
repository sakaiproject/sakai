package org.adl.datamodels;

import junit.framework.Test;
import junit.framework.TestSuite;

public class DataModelsAllTests {

	public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(SCORM_2004_DMElementTestCase.class);
        return suite;
    }
	
}
