/**
 * Copyright (c) 2003-2020 The Apereo Foundation
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
package org.sakaiproject.integration;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Convenience class for integration tests; runs under spring and loads beans.
 *
 * This uses a ContextHierachy to make the kernel configuration available for
 * extension or override in specific test scenarios.
 *
 * By default, we use the "sakai.test" directory packed into the helper jar to
 * read test-appropriate properties like the in-memory database config, but you
 * can override it by setting the system property in a {@link BeforeClass}
 * method or similar. You can set "sakai.home" similarly.
 */
@RunWith(SpringRunner.class)
@ContextHierarchy({
		@ContextConfiguration(name = "kernel", loader = IntegrationTestContextLoader.class, classes = IntegrationTestConfig.class) })
@WebAppConfiguration
public abstract class IntegrationTest {
}
