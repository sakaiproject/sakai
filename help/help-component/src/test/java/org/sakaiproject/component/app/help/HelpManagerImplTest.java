/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.component.app.help;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.api.app.help.Category;
import org.sakaiproject.api.app.help.HelpManager;
import org.sakaiproject.api.app.help.Resource;
import org.sakaiproject.api.app.help.TableOfContents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Iterator;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestContext.class)
public class HelpManagerImplTest {

    @Autowired
    @Qualifier("org.sakaiproject.api.app.help.HelpManager")
    private HelpManager helpManager;

    @Before
    public void setUp() {
        helpManager.initialize();
    }

    @Test
    public void testTableOfContents() {
        TableOfContents tableOfContents = helpManager.getTableOfContents();
        Set<Category> categories = tableOfContents.getCategories();
        assertThat(categories, hasSize(1));
        assertThat(categories, contains(hasProperty("name", equalTo("1st Category"))));

        Category category = categories.iterator().next();
        Set<Category> subCategories = category.getCategories();
        assertThat(subCategories, hasSize(1));
        assertThat(subCategories, contains(hasProperty("name", equalTo("Sample Category"))));

        Set<Resource> resources = subCategories.iterator().next().getResources();
        assertThat(resources, hasSize(1));
        assertThat(resources, contains(hasProperty("name", equalTo("Sample Resource Name"))));
    }

    @Test
    public void testSearch() {
        Set<Resource> test = helpManager.searchResources("test");
        assertNotNull(test);
        Iterator<Resource> iterator = test.iterator();
        assertTrue(iterator.hasNext());
        Resource resource = iterator.next();
        assertEquals("Sample Resource Name", resource.getName());
    }

    @Test
    public void testSearchNotFound() {
        Set<Resource> empty = helpManager.searchResources("notgoingtofindanything");
        assertNotNull(empty);
        assertThat(empty, empty());
    }

    @Test
    public void testGetResourceByDocId() {
        Resource resource = helpManager.getResourceByDocId("resourceDocId");
        assertNotNull(resource);
        assertThat(resource, hasProperty("name", equalTo("Sample Resource Name")));
    }

    @Test
    public void testGetResourceByDocIdNotFound() {
        Resource resource = helpManager.getResourceByDocId("doesNotExist");
        assertNull(resource);
    }
}
