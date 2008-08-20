package org.sakaiproject.test;

import java.io.IOException;

import org.sakaiproject.component.cover.TestComponentManagerContainer;
import org.sakaiproject.util.NoisierDefaultListableBeanFactory;

import junit.framework.TestCase;

public class SakaiKernelTestBase extends TestCase {

	private static String CONFIG = "../kernel-component/src/main/webapp/WEB-INF/components.xml";
	protected static TestComponentManagerContainer testComponentManagerContainer;

	protected static Object getService(String beanId) {
		return testComponentManagerContainer.getService(beanId);
	}

	protected static void oneTimeSetup(String additional) throws IOException {
		if (additional != null) {
			testComponentManagerContainer = new TestComponentManagerContainer(
					CONFIG + ";" + additional);
		} else {
			testComponentManagerContainer = new TestComponentManagerContainer(
					CONFIG);

		}
	}

	protected static void oneTimeTearDown() {
		NoisierDefaultListableBeanFactory.noisyClose = false;
		testComponentManagerContainer.getComponentManager().close();
		NoisierDefaultListableBeanFactory.noisyClose = true;
	}

}
