/**
 * Copyright Edia 2010. All rights reserved.
 */
package org.sakaiproject.scorm.dao.hibernate;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * @author Jan Vesely
 * Created on 20 apr. 2011
 * 
 * Base class for all service test classes.
 */
@DirtiesContext
@ContextConfiguration( locations = {
				"/hibernate-test.xml",
				"classpath*:org/sakaiproject/scorm/**/spring-hibernate-*.xml",
				"classpath*:org/sakaiproject/scorm/**/spring-scorm-*.xml",
				"classpath*:org/sakaiproject/scorm/**/spring-adl-*.xml",
				"classpath*:org/sakaiproject/scorm/**/spring-standalone-*.xml",
				"classpath*:org/sakaiproject/scorm/**/spring-mock-*.xml"})
public abstract class AbstractServiceTest extends AbstractTransactionalJUnit4SpringContextTests {
}
