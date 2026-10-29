/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.component.app.syllabus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.api.app.syllabus.SyllabusAttachment;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;

public class SyllabusContentProducerTest {

    private static final String REFERENCE = "/syllabus/site1/7";

    private final SyllabusManager syllabusManager = mock(SyllabusManager.class);
    private final SearchIndexBuilder searchIndexBuilder = mock(SearchIndexBuilder.class);
    private final EntityContentProducer resourcesProducer = mock(EntityContentProducer.class);
    private final SyllabusData data = new SyllabusData();
    private SyllabusContentProducer producer;

    @Before
    public void setup() {
        EntityManager entityManager = mock(EntityManager.class);
        Reference reference = mock(Reference.class);
        when(reference.getId()).thenReturn("7");
        when(entityManager.newReference(REFERENCE)).thenReturn(reference);
        when(syllabusManager.getSyllabusData("7")).thenReturn(data);
        when(syllabusManager.getSyllabusAttachmentsForSyllabusData(data)).thenReturn(new LinkedHashSet<>());

        data.setTitle("Week one");
        data.setAsset("<p>Read chapter two</p>");
        data.setStatus(SyllabusData.ITEM_POSTED);

        producer = new SyllabusContentProducer();
        producer.setEntityManager(entityManager);
        producer.setSyllabusManager(syllabusManager);
        producer.setSearchIndexBuilder(searchIndexBuilder);
    }

    @Test
    public void postedItemContentHoldsTitleAndBody() {
        String content = producer.getContent(REFERENCE);

        assertTrue(content, content.contains("Week one"));
        assertTrue(content, content.contains("Read chapter two"));
    }

    @Test
    public void unpublishedItemsHaveNoContent() {
        data.setStatus(SyllabusData.ITEM_DRAFT);

        // the indexer drops documents without content
        assertEquals("", producer.getContent(REFERENCE));
    }

    @Test
    public void attachmentNamesAndTextAreIncluded() {
        attach("/attachment/site1/Syllabus/notes.pdf", "notes.pdf");
        when(searchIndexBuilder.newEntityContentProducer("/content/attachment/site1/Syllabus/notes.pdf")).thenReturn(resourcesProducer);
        when(resourcesProducer.getContent("/content/attachment/site1/Syllabus/notes.pdf")).thenReturn("Photosynthesis lecture");

        String content = producer.getContent(REFERENCE);

        assertTrue(content, content.contains("notes.pdf"));
        assertTrue(content, content.contains("Photosynthesis lecture"));
    }

    @Test
    public void attachmentNameIsIncludedOnceWhenTheTextStartsWithIt() {
        attach("/attachment/site1/Syllabus/notes.pdf", "notes.pdf");
        when(searchIndexBuilder.newEntityContentProducer("/content/attachment/site1/Syllabus/notes.pdf")).thenReturn(resourcesProducer);
        when(resourcesProducer.getContent("/content/attachment/site1/Syllabus/notes.pdf")).thenReturn("notes.pdf Photosynthesis lecture");

        String content = producer.getContent(REFERENCE);

        assertEquals(content, content.indexOf("notes.pdf"), content.lastIndexOf("notes.pdf"));
    }

    @Test
    public void wordsOfTheAttachmentNamesAreSearchedButNotShown() {
        attach("/attachment/site1/Syllabus/Course_ReadingGuide.pdf", "Course_ReadingGuide.pdf");

        assertEquals(Map.of("attachment_names", List.of("Course_ReadingGuide Course ReadingGuide Course Reading Guide")),
            producer.getCustomProperties(REFERENCE));
        assertFalse(producer.getContent(REFERENCE).contains("Reading Guide"));
    }

    @Test
    public void wordsOfTheAttachmentName() {
        assertEquals("Course_Syllabus_2026 Course Syllabus 2026", SyllabusContentProducer.getNameWords("Course_Syllabus_2026.pdf"));
        assertEquals("SyllabusTest Syllabus Test", SyllabusContentProducer.getNameWords("SyllabusTest.pdf"));
        assertEquals("week-one reading week one reading", SyllabusContentProducer.getNameWords("week-one reading.pdf"));
        assertEquals("notes", SyllabusContentProducer.getNameWords("notes.pdf"));
        assertEquals("README", SyllabusContentProducer.getNameWords("README"));
    }

    @Test
    public void unreadableAttachmentsKeepTheirNameAndDoNotStopIndexing() {
        attach("/attachment/site1/Syllabus/broken.pdf", "broken.pdf");
        attach("/attachment/site1/Syllabus/missing.pdf", "missing.pdf");
        when(searchIndexBuilder.newEntityContentProducer("/content/attachment/site1/Syllabus/broken.pdf")).thenReturn(resourcesProducer);
        when(resourcesProducer.getContent("/content/attachment/site1/Syllabus/broken.pdf")).thenThrow(new RuntimeException("cannot digest"));

        String content = producer.getContent(REFERENCE);

        for (String expected : List.of("Week one", "broken.pdf", "missing.pdf")) {
            assertTrue(content, content.contains(expected));
        }
    }

    private void attach(String attachmentId, String name) {
        SyllabusAttachment attachment = new SyllabusAttachment();
        attachment.setAttachmentId(attachmentId);
        attachment.setName(name);
        syllabusManager.getSyllabusAttachmentsForSyllabusData(data).add(attachment);
    }
}
