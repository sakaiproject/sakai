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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.serialization.MapperFactory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class DataTableConfig {

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final ObjectMapper jsonMapper;

    private Boolean paging;
    private List<Set<Object>> lengthMenu;
    private Set<Set<Object>> order;
    private List<DataTableColumn> columns;
    private List<DataTableColumn> columnDefs;
    private Map<String, Object> language;

    public DataTableConfig() {
        jsonMapper = MapperFactory.createDefaultJsonMapper();
    }

    /**
     * Convenience method to serialize this DataTableConfig to JSON for use in JSP pages.
     * <p>
     * This method can be called from JSP expressions like: {@code ${bean.dataTablesConfig.json}}
     * </p>
     *
     * @return a JSON string representation of this configuration object, or {@code "{}"} if
     *         serialization fails. Any serialization errors are logged as warnings.
     * @see DataTableConfigBuilder for building configuration instances
     */
    @JsonIgnore
    public String getJson() {
        String json = "{}";
        try {
            json = jsonMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize DataTableConfig to json", e);
        }
        return json;
    }
}
