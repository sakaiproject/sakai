/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.gradebook.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This subclass of the GradeMappingTest tests the case in which
 * a fresh Gradebook DB has been created and the GradebookService
 * bean hasn't been configured to defined grading scales. What
 * should happen is that default scales will be defined based on the
 * old hard-coded GradeMapping classes.
 *
 * This test needs to run before any other unit tests, as they will
 * muddy the test DB environment with cleanly defined grading scales.
 */
public class GradeMappingConfigTest extends GradeMappingTest {
	private static Log log = LogFactory.getLog(GradeMappingConfigTest.class);

    protected String[] getConfigLocations() {
        String[] configLocations = {"spring-db.xml", "spring-beans.xml", "spring-facades.xml",

        	// Use the standard vanilla service definition, without
        	// any grading scale definitions.
        	"spring-service.xml",

        	"spring-hib.xml",
        	"spring-beans-test.xml",
        	"spring-hib-test.xml",

        	// SectionAwareness integration support.
        	"classpath*:org/sakaiproject/component/section/support/spring-integrationSupport.xml",
			"classpath*:org/sakaiproject/component/section/spring-beans.xml",
			"classpath*:org/sakaiproject/component/section/spring-services.xml",
        };
        return configLocations;
    }
}
