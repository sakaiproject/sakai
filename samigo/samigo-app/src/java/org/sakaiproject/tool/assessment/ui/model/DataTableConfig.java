package org.sakaiproject.tool.assessment.ui.model;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataTableConfig {


    private static final String SORT_ASCENDING = "asc";
    private static final String SORT_DESCENDING = "desc";
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
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.DataTablesMessages");

    private Boolean paging;
    private List<Set<Object>> lengthMenu;
    private Set<Set<Object>> order;
    private List<DataTableColumn> columns;
    private Map<String, Object> language;


    public String toJsonString() {
        Gson gson = new GsonBuilder().create();
        JsonElement jsonElement = gson.toJsonTree(this);
        JsonObject jsonObject = (JsonObject) jsonElement;

        return jsonObject.toString();
    }

    // Convenience method to be called from the jsp: bean.dataTablesConfig.json
    public String getJson() {
        return toJsonString();
    }

    public static DataTableConfigBuilder builderWithDefaults() {
        return DataTableConfig.builder()
                .paging(true)
                .lengthMenu(new LinkedList<>() {{
                        add(new LinkedHashSet<>() {{
                                add(5);
                                add(10);
                                add(20);
                                add(50);
                                add(100);
                                add(200);
                                add(-1);
                        }});
                        add(new LinkedHashSet<>() {{
                                add("5");
                                add("10");
                                add("20");
                                add("50");
                                add("100");
                                add("200");
                                add(RESOURCE_BUNDLE.getString(I18N_ALL));
                        }});
                }})
                .language(new HashMap<>() {{
                        put(I18N_SEARCH, RESOURCE_BUNDLE.getString(I18N_SEARCH) + " ");
                        put(I18N_INFO, RESOURCE_BUNDLE.getString(I18N_INFO));
                        put(I18N_PAGINATE, new HashMap<>() {{
                                put(I18N_NEXT, RESOURCE_BUNDLE.getString(I18N_PAGINATE + "_" + I18N_NEXT));
                                put(I18N_PREVIOUS, RESOURCE_BUNDLE.getString(I18N_PAGINATE + "_" + I18N_PREVIOUS));
                        }});
                        put(I18N_ARIA, new HashMap<>() {{
                                put(I18N_SORT_ASCENDING, RESOURCE_BUNDLE.getString(I18N_ARIA + "_" + I18N_SORT_ASCENDING));
                                put(I18N_SORT_DESCENDING, RESOURCE_BUNDLE.getString(I18N_ARIA + "_" + I18N_SORT_DESCENDING));
                        }});
                }})
                .entitiesMessage(RESOURCE_BUNDLE.getString(I18N_ENTITIES));
    }


    public static class DataTableConfigBuilder {

        public DataTableConfigBuilder initialSort(int columnIndex, boolean ascending) {
            order = new HashSet<>() {{
                    add(new HashSet<>() {{
                            add(columnIndex);
                    }});
                    add(new HashSet<>() {{
                            add(ascending ? SORT_ASCENDING : SORT_DESCENDING);
                    }});
            }};

            return this;
        }

        public DataTableConfigBuilder entitiesMessage(String entitiesMessage) {
            String infoEmptyMessage =  RESOURCE_BUNDLE.getString(I18N_INFO_EMPTY);
            addMessage(I18N_INFO_EMPTY, infoEmptyMessage, entitiesMessage);
            addMessage(I18N_EMPTY_TABLE, infoEmptyMessage, entitiesMessage);
            addMessage(I18N_ZERO_RECORDS, infoEmptyMessage, entitiesMessage);
            addMessage(I18N_LENGTH_MENU, RESOURCE_BUNDLE.getString(I18N_LENGTH_MENU), entitiesMessage);
            addMessage(I18N_INFO_FILTERED, RESOURCE_BUNDLE.getString(I18N_INFO_FILTERED), entitiesMessage);

            return this;
        }

        private void addMessage(String messageKey, String message, String... inserts) {
            if (language == null) {
                language = new HashMap<>();
            }

            language.put(messageKey, MessageFormat.format(message, (Object[]) inserts));
        }
    }
}
