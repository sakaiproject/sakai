/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
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

package org.sakaiproject.tool.assessment.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.sakaiproject.serialization.MapperFactory;
import org.sakaiproject.tool.assessment.data.ifc.assessment.TagIfc;

/** JSON data for tag displays. Render in an escaped HTML field, never inline JavaScript. */
public record TagJson(String tagId, String tagLabel, String tagCollectionName) {

    private static final ObjectWriter WRITER = MapperFactory.createDefaultJsonMapper().writer();
    private static final ObjectReader READER = MapperFactory.createDefaultJsonMapper().readerForListOf(TagJson.class);

    /** Read the saved display data for server-rendered form options. */
    public static List<TagJson> deserialize(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return READER.readValue(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not deserialize tag display data", e);
        }
    }

    public static TagJson fromTag(TagIfc tag) {
        return new TagJson(tag.getTagId(), tag.getTagLabel(), tag.getTagCollectionName());
    }

    public static String serialize(Collection<TagJson> tags) {
        try {
            return WRITER.writeValueAsString(tags == null ? Collections.emptyList() : tags);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize tag display data", e);
        }
    }
}
