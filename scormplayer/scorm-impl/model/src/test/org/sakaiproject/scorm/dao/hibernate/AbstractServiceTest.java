/**
 * Copyright Edia 2010. All rights reserved.
 */
package org.sakaiproject.scorm.dao.hibernate;

import org.springframework.test.annotation.AbstractAnnotationAwareTransactionalTests;

/**
 * @author Jan Vesely
 * Created on 20 apr. 2011
 * 
 * Base class for all service test classes.
 */
public abstract class AbstractServiceTest extends AbstractAnnotationAwareTransactionalTests {

	@Override
	protected String[] getConfigLocations() {
		return new String[] {
				"hibernate-test.xml",
				// "sakai-test.xml",
				"classpath*:org/sakaiproject/scorm/**/spring-hibernate-*.xml",
				"classpath*:org/sakaiproject/scorm/**/spring-scorm-*.xml",
				"classpath*:org/sakaiproject/scorm/**/spring-adl-*.xml",
				"classpath*:org/sakaiproject/scorm/**/spring-standalone-*.xml",
				"classpath*:org/sakaiproject/scorm/**/spring-mock-*.xml", };
	}

}
