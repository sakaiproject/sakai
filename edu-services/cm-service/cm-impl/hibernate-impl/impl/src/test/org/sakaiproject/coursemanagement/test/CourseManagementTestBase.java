/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.coursemanagement.test;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration(locations={"/spring-test.xml", "/spring-config-test.xml"})
public abstract class CourseManagementTestBase extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    protected SecurityService securityService;

    @Before
    public void allowCourseManagementAdministration() {
        reset(securityService);
        when(securityService.unlock(CourseManagementService.SECURE_CM_ADMIN,
                CourseManagementService.ENTITY_CM_ADMIN)).thenReturn(true);
    }
}
