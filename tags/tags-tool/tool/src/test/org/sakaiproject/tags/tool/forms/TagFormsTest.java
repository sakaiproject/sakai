/**********************************************************************************
 *
 * Copyright (c) 2026 The Apereo Foundation
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

package org.sakaiproject.tags.tool.forms;

import org.junit.Test;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagCollection;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

public class TagFormsTest {
    @Test
    public void tagFormPreservesNullMetadataAcrossDisplayAndSubmission() {
        Tag displayed = TagForm.fromTag(Tag.builder().tagId("tag").build()).toTag();
        assertNull(displayed.getCreationDate());
        assertNull(displayed.getLastModificationDate());
        assertNull(displayed.getExternalCreationDate());
        assertNull(displayed.getLastUpdateDateInExternalSystem());
        assertNull(displayed.getExternalCreation());
        assertNull(displayed.getExternalUpdate());
        MockHttpServletRequest request = new MockHttpServletRequest();
        for (String field : new String[] { "creationDate", "lastModificationDate", "externalCreationDate",
                "lastUpdateDateInExternalSystem", "externalCreation", "externalUpdate" }) {
            request.setParameter(field, "");
        }
        Tag submitted = TagForm.fromRequest("tag", request).toTag();
        assertNull(submitted.getCreationDate());
        assertNull(submitted.getLastModificationDate());
        assertNull(submitted.getExternalCreationDate());
        assertNull(submitted.getLastUpdateDateInExternalSystem());
        assertNull(submitted.getExternalCreation());
        assertNull(submitted.getExternalUpdate());
    }

    @Test
    public void collectionFormPreservesNullMetadataAcrossDisplayAndSubmission() {
        TagCollection displayed = TagCollectionForm.fromTagCollection(TagCollection.builder()
            .tagCollectionId("collection").build()).toTagCollection();
        assertNull(displayed.getCreationDate());
        assertNull(displayed.getLastModificationDate());
        assertNull(displayed.getLastSynchronizationDate());
        assertNull(displayed.getLastUpdateDateInExternalSystem());
        assertNull(displayed.getExternalCreation());
        assertNull(displayed.getExternalUpdate());
        TagCollection submitted = TagCollectionForm.fromRequest("collection", new MockHttpServletRequest()).toTagCollection();
        assertNull(submitted.getCreationDate());
        assertNull(submitted.getLastModificationDate());
        assertNull(submitted.getLastSynchronizationDate());
        assertNull(submitted.getLastUpdateDateInExternalSystem());
        assertNull(submitted.getExternalCreation());
        assertNull(submitted.getExternalUpdate());
    }

    @Test
    public void explicitZeroAndFalseRemainDistinctFromNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("externalCreationDate", "0");
        request.setParameter("lastSynchronizationDate", "0");
        request.setParameter("externalCreation", "false");
        request.setParameter("externalUpdate", "true");
        Tag tag = TagForm.fromRequest("tag", request).toTag();
        TagCollection collection = TagCollectionForm.fromRequest("collection", request).toTagCollection();
        assertEquals(Long.valueOf(0L), tag.getExternalCreationDate());
        assertEquals(Long.valueOf(0L), collection.getLastSynchronizationDate());
        assertEquals(Boolean.FALSE, tag.getExternalCreation());
        assertEquals(Boolean.FALSE, collection.getExternalCreation());
        assertEquals(Boolean.TRUE, tag.getExternalUpdate());
        assertEquals(Boolean.TRUE, collection.getExternalUpdate());
    }
}
