/*
 * Copyright (c) 2003-2023 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;
import org.sakaiproject.util.ResourceLoader;

public class DataTableConfigTest {

    @Test
    public void getJsonSerializesConfiguredFields() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getString("infoEmpty")).thenReturn("No {0} available");
        when(resourceLoader.getString("lengthMenu")).thenReturn("Show {0} per page");
        when(resourceLoader.getString("infoFiltered")).thenReturn("(filtered from {0} total)");

        Set<Object> numericLengths = new LinkedHashSet<>(Arrays.asList(5, 10, -1));

        DataTableColumn column = DataTableColumn.builder()
                .targets("_all")
                .orderable(true)
                .searchable(true)
                .type(DataTableColumn.TYPE_ANY_NUM)
                .build();

        DataTableConfig config = new DataTableConfigBuilder(resourceLoader)
                .paging(true)
                .lengthMenu(Collections.singletonList(numericLengths))
                .columns(Collections.singletonList(column))
                .entitiesMessage("questions")
                .build();

        String json = config.getJson();

        assertTrue(json, json.contains("\"paging\":true"));
        assertTrue(json, json.contains("\"lengthMenu\":[[5,10,-1]]"));
        assertTrue(json, json.contains("\"targets\":\"_all\""));
        assertTrue(json, json.contains("\"infoEmpty\":\"No questions available\""));
        assertTrue(json, json.contains("\"emptyTable\":\"No questions available\""));
        assertTrue(json, json.contains("\"zeroRecords\":\"No questions available\""));
        assertTrue(json, json.contains("\"lengthMenu\":\"Show questions per page\""));
        assertTrue(json, json.contains("\"infoFiltered\":\"(filtered from questions total)\""));
        assertFalse("order should be omitted when not set", json.contains("\"order\""));
        assertFalse("columnDefs should be omitted when not set", json.contains("\"columnDefs\""));
        assertFalse("jsonMapper must never leak into the serialized output", json.contains("jsonMapper"));
    }

    @Test
    public void getJsonReturnsEmptyObjectForDefaultConfig() {
        DataTableConfig config = new DataTableConfigBuilder(mock(ResourceLoader.class)).build();

        assertEquals("{}", config.getJson());
    }
}
