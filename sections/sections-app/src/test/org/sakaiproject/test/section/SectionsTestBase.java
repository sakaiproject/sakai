/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.test.section;

import org.springframework.test.AbstractTransactionalSpringContextTests;

public class SectionsTestBase extends AbstractTransactionalSpringContextTests {
    protected String[] getConfigLocations() {
        String[] configLocations = {
			"org/sakaiproject/component/section/spring-beans.xml",
			"org/sakaiproject/component/section/spring-db.xml",
			"org/sakaiproject/component/section/support/spring-hib-test.xml",
			"org/sakaiproject/component/section/support/spring-services-test.xml",
			"org/sakaiproject/component/section/support/spring-integrationSupport.xml"
        };
        return configLocations;
    }

}




