package org.sakaiproject.sitestats.test.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.AbstractTransactionalSpringContextTests;


public class FakeDataPreload extends AbstractTransactionalSpringContextTests {
	private static Log	log	= LogFactory.getLog(FakeDataPreload.class);

	public void init() {
		log.info("FakeDataPreload.init()");
	}
}
