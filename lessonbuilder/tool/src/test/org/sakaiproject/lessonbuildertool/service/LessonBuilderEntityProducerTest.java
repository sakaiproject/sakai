/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LessonBuilderEntityProducerTest {

    private LessonBuilderEntityProducer producer;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private SimplePageToolDao dao;

    @Mock
    private Reference ref;

    @Before
    public void setUp() {
        producer = new LessonBuilderEntityProducer();
        producer.setSimplePageToolDao(dao);
    }

    @Test
    public void testParseEntityReferenceNotOurs() {
        assertFalse(producer.parseEntityReference("/else", ref));
        assertFalse(producer.parseEntityReference("/", ref));
        assertFalse(producer.parseEntityReference("", ref));
    }

    @Test
    public void testParseEntityReferenceNoItem() {
        assertFalse(producer.parseEntityReference("/lessonbuilder/item", ref));
    }

    @Test
    public void testParseEntityReferenceNoSite() {
        assertFalse(producer.parseEntityReference("/lessonbuilder/site", ref));
    }

    @Test
    public void testParseEntityReferenceNoPage() {
        assertFalse(producer.parseEntityReference("/lessonbuilder/page", ref));
    }

    @Test
    public void testParseEntityReferencePageNotInt() {
        assertFalse(producer.parseEntityReference("/lessonbuilder/page/notNumber", ref));
    }

    @Test
    public void testParseEntityReferencePageMissingId() {
        assertFalse(producer.parseEntityReference("/lessonbuilder/page/10", ref));
    }

    @Test
    public void testParseEntityReferencePageWithId() {
        SimplePage page = Mockito.mock(SimplePage.class);
        Mockito.when(page.getSiteId()).thenReturn("siteId");
        Mockito.when(dao.getPage(10)).thenReturn(page);
        assertTrue(producer.parseEntityReference("/lessonbuilder/page/10", ref));
        Mockito.verify(ref).set("sakai:lessonbuilder", "page", "/page/10", null, "siteId");
    }

    @Test
    public void testParseEntityReferenceItemWithId() {
        SimplePageItem item = Mockito.mock(SimplePageItem.class);
        Mockito.when(dao.findItem(10)).thenReturn(item);
        Mockito.when(item.getPageId()).thenReturn(11L);
        SimplePage page = Mockito.mock(SimplePage.class);
        Mockito.when(page.getSiteId()).thenReturn("siteId");
        Mockito.when(dao.getPage(11)).thenReturn(page);
        assertTrue(producer.parseEntityReference("/lessonbuilder/item/10", ref));
        Mockito.verify(ref).set("sakai:lessonbuilder", "item", "/item/10", null, "siteId");
    }

    @Test
    public void testParseEntityReferenceSite() {
        assertTrue(producer.parseEntityReference("/lessonbuilder/site/siteId", ref));
        Mockito.verify(ref).set("sakai:lessonbuilder", "site", "/site/siteId", null, "/site/siteId");
    }

}
