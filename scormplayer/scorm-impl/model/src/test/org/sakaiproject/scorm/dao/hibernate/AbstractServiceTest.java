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
public abstract class AbstractServiceTest extends AbstractTransactionalJUnit4SpringContextTests
{
}
