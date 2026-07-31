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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.util.ResourceLoader;

/**
 * Builds {@link DataTableConfig} instances, including the localized defaults
 * (paging, length menu, and language messages) driven by the DataTablesMessages bundle.
 */
public class DataTableConfigBuilder {

    private static final String I18N_ALL = "all";
    private static final String I18N_ARIA = "aria";
    private static final String I18N_EMPTY_TABLE = "emptyTable";
    private static final String I18N_INFO = "info";
    private static final String I18N_INFO_EMPTY = "infoEmpty";
    private static final String I18N_INFO_FILTERED = "infoFiltered";
    private static final String I18N_LENGTH_MENU = "lengthMenu";
    private static final String I18N_NEXT = "next";
    private static final String I18N_PAGINATE = "paginate";
    private static final String I18N_PREVIOUS = "previous";
    private static final String I18N_SEARCH = "search";
    private static final String I18N_SORT_ASCENDING = "sortAscending";
    private static final String I18N_SORT_DESCENDING = "sortDescending";
    private static final String I18N_ZERO_RECORDS = "zeroRecords";
    private static final String I18N_ENTITIES = "entities";

    private final ResourceLoader resourceLoader;

    private Boolean paging;
    private List<Set<Object>> lengthMenu;
    private Set<Set<Object>> order;
    private List<DataTableColumn> columns;
    private List<DataTableColumn> columnDefs;
    private Map<String, Object> language;

    public DataTableConfigBuilder() {
        this(new ResourceLoader("org.sakaiproject.tool.assessment.bundle.DataTablesMessages"));
    }

    public DataTableConfigBuilder(ResourceLoader loader) {
        resourceLoader = loader;
    }

    public static DataTableConfigBuilder withDefaults() {
        DataTableConfigBuilder builder = new DataTableConfigBuilder();
        ResourceLoader resourceLoader = builder.resourceLoader;

        // LinkedHashSet rather than Set.of: DataTables reads the length menu as two parallel
        // ordered sequences, the page sizes and their labels, so iteration order is significant
        Set<Object> lengthMenuValues = new LinkedHashSet<>(List.of(5, 10, 20, 50, 100, 200, -1));
        Set<Object> lengthMenuLabels = new LinkedHashSet<>(
                List.of("5", "10", "20", "50", "100", "200", resourceLoader.getString(I18N_ALL)));

        // entitiesMessage() below adds further entries, so this one has to stay mutable
        Map<String, Object> language = new HashMap<>();
        language.put(I18N_SEARCH, resourceLoader.getString(I18N_SEARCH) + " ");
        language.put(I18N_INFO, resourceLoader.getString(I18N_INFO));
        language.put(I18N_PAGINATE, Map.of(
                I18N_NEXT, resourceLoader.getString(I18N_PAGINATE + "_" + I18N_NEXT),
                I18N_PREVIOUS, resourceLoader.getString(I18N_PAGINATE + "_" + I18N_PREVIOUS)));
        language.put(I18N_ARIA, Map.of(
                I18N_SORT_ASCENDING, resourceLoader.getString(I18N_ARIA + "_" + I18N_SORT_ASCENDING),
                I18N_SORT_DESCENDING, resourceLoader.getString(I18N_ARIA + "_" + I18N_SORT_DESCENDING)));

        return builder
                .paging(true)
                .lengthMenu(List.of(lengthMenuValues, lengthMenuLabels))
                .language(language)
                .entitiesMessage(resourceLoader.getString(I18N_ENTITIES));
    }

    public DataTableConfigBuilder paging(Boolean paging) {
        this.paging = paging;
        return this;
    }

    public DataTableConfigBuilder lengthMenu(List<Set<Object>> lengthMenu) {
        this.lengthMenu = lengthMenu;
        return this;
    }

    public DataTableConfigBuilder order(Set<Set<Object>> order) {
        this.order = order;
        return this;
    }

    public DataTableConfigBuilder columns(List<DataTableColumn> columns) {
        this.columns = columns;
        return this;
    }

    public DataTableConfigBuilder columnDefs(List<DataTableColumn> columnDefs) {
        this.columnDefs = columnDefs;
        return this;
    }

    public DataTableConfigBuilder language(Map<String, Object> language) {
        this.language = language;
        return this;
    }

    public DataTableConfigBuilder entitiesMessage(String entitiesMessage) {
        String infoEmptyMessage = resourceLoader.getString(I18N_INFO_EMPTY);
        addMessage(I18N_INFO_EMPTY, infoEmptyMessage, entitiesMessage);
        addMessage(I18N_EMPTY_TABLE, infoEmptyMessage, entitiesMessage);
        addMessage(I18N_ZERO_RECORDS, infoEmptyMessage, entitiesMessage);
        addMessage(I18N_LENGTH_MENU, resourceLoader.getString(I18N_LENGTH_MENU), entitiesMessage);
        addMessage(I18N_INFO_FILTERED, resourceLoader.getString(I18N_INFO_FILTERED), entitiesMessage);

        return this;
    }

    private void addMessage(String messageKey, String message, String... inserts) {
        if (language == null) {
            language = new HashMap<>();
        }

        language.put(messageKey, MessageFormat.format(message, (Object[]) inserts));
    }

    public DataTableConfig build() {
        DataTableConfig config = new DataTableConfig();
        config.setPaging(paging);
        config.setLengthMenu(lengthMenu);
        config.setOrder(order);
        config.setColumns(columns);
        config.setColumnDefs(columnDefs);
        config.setLanguage(language);
        return config;
    }
}
