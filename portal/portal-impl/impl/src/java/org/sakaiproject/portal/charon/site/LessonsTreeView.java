/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   New York University
 *   Payten Giles
 *   Mark Triggs
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

package org.sakaiproject.portal.charon.site;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.db.cover.SqlService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.simple.JSONObject;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class LessonsTreeView {

    private static ResourceLoader rb = new ResourceLoader("sitenav");

    private String userId;
    private boolean isInstructor;

    public LessonsTreeView(final String userId, final boolean isInstructor) {
        this.isInstructor = isInstructor;
        this.userId = userId;
    }

    public String lessonsPagesJSON(final List pages) {
        final List<Map<String,String>> typedPages = (List<Map<String,String>>) pages;
        final List<String> pageIds = new ArrayList<>(typedPages.size());

        for (Map<String, String> page : typedPages) {
            pageIds.add(page.get("pageId"));
        }

        final Map<String, List<Map<String, String>>> pageData = getAdditionalLessonsPages(pageIds);
        applyPrerequisites(pageData);

        final Map<String, Object> objectToSerialize = new HashMap<>();
        objectToSerialize.put("pages", pageData);
        objectToSerialize.put("i18n", getI18n());

        return JSONObject.toJSONString(objectToSerialize);
    }

    // Return a mapping of PageID -> list of additional items to show
    private Map<String, List<Map<String, String>>> getAdditionalLessonsPages(final List<String> pageIds) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        final Map<String, List<Map<String, String>>> result = new HashMap<>();

        try {
            try {
                connection = SqlService.borrowConnection();
                ps = connection.prepareStatement(buildSQL(connection, pageIds));

                ps.setString(1, this.userId);

                for (int i = 0; i < pageIds.size(); i++) {
                    ps.setString(i + 2, pageIds.get(i));
                }

                rs = ps.executeQuery();

                while (rs.next()) {
                    if (!this.isInstructor && hiddenFromStudents(rs)) {
                        continue;
                    }

                    if (!result.containsKey(rs.getString("sakaiToolId"))) {
                        result.put(rs.getString("sakaiToolId"), new ArrayList<Map<String, String>>());
                    }

                    result.get(rs.getString("sakaiToolId")).add(makeMap(rs));
                }

            } finally {
                if (ps != null) { ps.close(); }
                if (rs != null) { rs.close(); }

                if (connection != null) {
                    SqlService.returnConnection(connection);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to get lessons tree: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    private String buildSQL(final Connection conn, final List<String> pageIds) {
        return ("SELECT p.toolId as sakaiPageId," +
            " p.pageId as lessonsPageId," +
            " s.site_id as sakaiSiteId," +
            " s.tool_id as sakaiToolId," +
            " i.id as lessonsItemId," +
            " i.name as name," +
            " i.description as description," +
            " " + toChar(conn, "i.sakaiId") + " as sendingPage," +
            " p2.hidden as hidden," +
            " p2.releaseDate as releaseDate," +
            " log.complete as completed," +
            " i.required," +
            " i.prerequisite" +
            " FROM lesson_builder_pages p" +
            " INNER JOIN SAKAI_SITE_TOOL s" +
            "   on p.toolId = s.page_id" +
            " INNER JOIN lesson_builder_items i" +
            "   on (i.pageId = p.pageId AND type = 2)" +
            " INNER JOIN lesson_builder_pages p2" +
            "   on (p2.pageId = i.sakaiId)" +
            " LEFT OUTER JOIN lesson_builder_log log" +
            "   on (log.itemId = i.id AND log.userId = ?)" +
            " WHERE p.parent IS NULL" +
            "   AND p.toolId in (" + placeholdersFor(pageIds) + ")" +
            " ORDER BY i.sequence");
    }

    private String toChar(final Connection conn, final String expr) {
        try {
            final String dbFamily = conn.getMetaData().getDatabaseProductName().toLowerCase(java.util.Locale.ROOT);

            if ("mysql".equals(dbFamily)) {
                return String.format("cast(%s AS CHAR)", expr);
            } else if ("oracle".equals(dbFamily)) {
                return String.format("to_char(%s)", expr);
            } else {
                return expr;
            }
        } catch (SQLException e) {
            return expr;
        }
    }

    private Map<String, String> makeMap(final ResultSet rs) throws SQLException {
        final Map<String, String> result = new HashMap<>();

        result.put("toolId", rs.getString("sakaiToolId"));
        result.put("siteId", rs.getString("sakaiSiteId"));
        result.put("itemId", rs.getString("lessonsItemId"));
        result.put("sendingPage", rs.getString("sendingPage"));
        result.put("name", rs.getString("name"));
        result.put("description", rs.getString("description"));
        result.put("hidden", rs.getInt("hidden") == 1 ? "true" : "false");

        result.put("required", rs.getInt("required") == 1 ? "true" : "false");
        result.put("completed", rs.getInt("completed") == 1 ? "true" : "false");
        result.put("prerequisite", rs.getInt("prerequisite") == 1 ? "true" : "false");

        if (rs.getTimestamp("releaseDate") != null) {
            final Timestamp releaseDate = rs.getTimestamp("releaseDate");
            if (releaseDate.getTime() > System.currentTimeMillis()) {
                result.put("hidden", "true");
                final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, rb.getLocale());
                final TimeZone tz = TimeService.getLocalTimeZone();
                df.setTimeZone(tz);
                result.put("releaseDate", df.format(releaseDate));
            }
        }

        return result;
    }

    private boolean hiddenFromStudents(final ResultSet rs) throws SQLException {
        if (rs.getInt("hidden") == 1) {
            return true;
        } else if (rs.getTimestamp("releaseDate") != null) {
            if (rs.getTimestamp("releaseDate").getTime() > System.currentTimeMillis()) {
                return true;
            }
        }

        return false;
    }

    private <E> String placeholdersFor(final List<E> list) {
        final StringBuilder placeholders = new StringBuilder();
        for (E elt : list) {
            if (placeholders.length() > 0) {
                placeholders.append(", ");
            }

            placeholders.append("?");
        }

        return placeholders.toString();
    }

    private Map<String, String> getI18n() {
        final Map<String, String> translations = new HashMap<>();

        translations.put("expand", rb.getString("lessons_subnav.expand"));
        translations.put("collapse", rb.getString("lessons_subnav.collapse"));
        translations.put("open_top_level_page", rb.getString("lessons_subnav.open_top_level_page"));
        translations.put("hidden", rb.getString("lessons_subnav.hidden"));
        translations.put("hidden_with_release_date", rb.getString("lessons_subnav.hidden_with_release_date"));
        translations.put("prerequisite", rb.getString("lessons_subnav.prerequisite"));
        translations.put("prerequisite_and_disabled", rb.getString("lessons_subnav.prerequisite_and_disabled"));

        return translations;
    }

    private void applyPrerequisites(final Map<String, List<Map<String, String>>> data) {
        for (final String pageId : data.keySet()) {
            boolean prerequisiteApplies = false;
            final List<Map<String, String>> pages = data.get(pageId);
            for (Map<String, String> pageData : pages) {
                // If a sibling item with a smaller sequence is required
                // we want to disable the current item for students
                if (pageData.get("prerequisite").equals("true") && prerequisiteApplies) {
                    pageData.put("disabledDueToPrerequisite", "true");
                    pageData.put("disabled", String.valueOf(!this.isInstructor));
                }

                // Only disable items that have prerequisites below the current item
                // when the current item is required and the user is yet to complete it
                if (pageData.get("required").equals("true")) {
                    if (pageData.get("completed").equals("false")) {
                        prerequisiteApplies = true;
                    }
                }
            }
        }
    }
}
