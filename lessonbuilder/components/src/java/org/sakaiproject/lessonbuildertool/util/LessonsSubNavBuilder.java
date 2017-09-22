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

package org.sakaiproject.lessonbuildertool.util;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.ResourceLoader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
public class LessonsSubNavBuilder {

    private static ResourceLoader rb = new ResourceLoader("subnav");

    private String siteId;
    private boolean isInstructor;
    private Map<String, ArrayList<Map<String, String>>> subnavData;

    public LessonsSubNavBuilder(final String siteId, final boolean isInstructor) {
        this.siteId = siteId;
        this.isInstructor = isInstructor;
        this.subnavData = new HashMap<>();
    }

    public String toJSON() {
        applyPrerequisites();

        final Map<String, Object> objectToSerialize = new HashMap<>();
        objectToSerialize.put("pages", this.subnavData);
        objectToSerialize.put("i18n", getI18n());
        objectToSerialize.put("siteId", this.siteId);
        objectToSerialize.put("isInstructor", this.isInstructor);

        return JSONObject.toJSONString(objectToSerialize);
    }


    public static List<String> collectPageIds(final List pages) {
        final List<Map<String,String>> typedPages = (List<Map<String,String>>) pages;
        final List<String> pageIds = new ArrayList<>(typedPages.size());

        for (Map<String, String> page : typedPages) {
            // try to limit to only lesson pages
            if (!page.containsKey("wellKnownToolId") || "sakai.lessonbuildertool".equals(page.get("wellKnownToolId"))) {
                pageIds.add(page.get("pageId"));
            }
        }

        return pageIds;
    }


    public Map<String, String> processResult(final ResultSet rs) throws SQLException {
        final String sakaiToolId = rs.getString("sakaiToolId");

        if (isHidden(rs)) {
            return null;
        }

        if (!this.subnavData.containsKey(sakaiToolId)) {
            this.subnavData.put(sakaiToolId, new ArrayList<>());
        }
        
        final Map<String, String> subnavItem = new HashMap<>();

        subnavItem.put("toolId", rs.getString("sakaiToolId"));
        subnavItem.put("siteId", rs.getString("sakaiSiteId"));
        subnavItem.put("sakaiPageId", rs.getString("sakaiPageId"));
        subnavItem.put("itemId", rs.getString("itemId"));
        subnavItem.put("sendingPage", rs.getString("itemSakaiId"));
        subnavItem.put("name", rs.getString("itemName"));
        subnavItem.put("description", rs.getString("itemDescription"));
        subnavItem.put("hidden", rs.getInt("pageHidden") == 1 ? "true" : "false");

        subnavItem.put("required", rs.getInt("required") == 1 ? "true" : "false");
        subnavItem.put("completed", rs.getInt("completed") == 1 ? "true" : "false");
        subnavItem.put("prerequisite", rs.getInt("prerequisite") == 1 ? "true" : "false");

        if (rs.getTimestamp("pageReleaseDate") != null) {
            final Timestamp releaseDate = rs.getTimestamp("pageReleaseDate");
            if (releaseDate.getTime() > System.currentTimeMillis()) {
                subnavItem.put("hidden", "true");
                final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, rb.getLocale());
                final TimeZone tz = TimeService.getLocalTimeZone();
                df.setTimeZone(tz);
                subnavItem.put("releaseDate", df.format(releaseDate));
            }
        }

        this.subnavData.get(sakaiToolId).add(subnavItem);

        return subnavItem;
    }

    private boolean isHidden(final ResultSet rs) throws SQLException {
        if (this.isInstructor) {
            return false;
        }

        if (rs.getInt("pageHidden") == 1) {
            return true;
        } else if (rs.getTimestamp("pageReleaseDate") != null) {
            if (rs.getTimestamp("pageReleaseDate").getTime() > System.currentTimeMillis()) {
                return true;
            }
        }

        return false;
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

    private void applyPrerequisites() {
        for (final String pageId : this.subnavData.keySet()) {
            boolean prerequisiteApplies = false;
            final List<Map<String, String>> pages = this.subnavData.get(pageId);
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
