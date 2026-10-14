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

import static org.junit.Assert.assertEquals;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.junit.Test;
import org.sakaiproject.serialization.MapperFactory;

public class TagJsonTest {
    @Test
    public void roundTripsLiteralLabelsAndCollectionNames() throws Exception {
        String label = "</script><img src=x onerror=alert(1)> \" & \\ \n 日本語";
        String collection = "<p>Collection</p> \" &";
        String json = TagJson.serialize(List.of(new TagJson("id", label, collection)));
        JsonNode tag = MapperFactory.createDefaultJsonMapper().readTree(json).get(0);
        assertEquals("id", tag.get("tagId").asText());
        assertEquals(label, tag.get("tagLabel").asText());
        assertEquals(collection, tag.get("tagCollectionName").asText());
        assertEquals("[]", TagJson.serialize(null));
        assertEquals("[]", TagJson.serialize(List.of()));
    }
}
