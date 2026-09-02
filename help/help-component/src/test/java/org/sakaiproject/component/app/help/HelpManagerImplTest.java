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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.api.app.help.Category;
import org.sakaiproject.api.app.help.HelpManager;
import org.sakaiproject.api.app.help.Resource;
import org.sakaiproject.api.app.help.TableOfContents;
import org.sakaiproject.component.app.help.model.CategoryBean;
import org.sakaiproject.component.app.help.model.ResourceBean;
import org.sakaiproject.component.app.help.model.TableOfContentsBean;

@RunWith(MockitoJUnitRunner.class)
public class HelpManagerImplTest {

    @Mock
    private HelpManager helpManager;

    private Resource sampleResource;
    private Category sampleCategory;
    private Set<Category> categories;
    private Set<Category> subCategories;
    private Set<Resource> resources;

    @Before
    public void setUp() {
        sampleResource = new ResourceBean();
        sampleResource.setName("Sample Resource Name");
        sampleResource.setDocId("resourceDocId");

        resources = new HashSet<>();
        resources.add(sampleResource);

        sampleCategory = new CategoryBean();
        sampleCategory.setName("Sample Category");
        sampleCategory.setResources(resources);

        subCategories = new HashSet<>();
        subCategories.add(sampleCategory);

        Category topCategory = new CategoryBean();
        topCategory.setName("1st Category");
        topCategory.setCategories(subCategories);

        categories = new HashSet<>();
        categories.add(topCategory);

        TableOfContents tableOfContents = new TableOfContentsBean();
        tableOfContents.setCategories(categories);

        when(helpManager.getTableOfContents()).thenReturn(tableOfContents);

        Set<Resource> searchResult = new HashSet<>();
        searchResult.add(sampleResource);
        when(helpManager.searchResources("test")).thenReturn(searchResult);
        when(helpManager.searchResources("notgoingtofindanything")).thenReturn(new HashSet<>());

        when(helpManager.getResourceByDocId("resourceDocId")).thenReturn(sampleResource);
        when(helpManager.getResourceByDocId("doesNotExist")).thenReturn(null);
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
        assertEquals(1, test.size());
        assertEquals("Sample Resource Name", test.toArray(new Resource[0])[0].getName());
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
