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

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.util.ResourceLoader;


import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;

@Slf4j
public class LessonsSubNavBuilder {

    private static final ResourceLoader rb = new ResourceLoader("subnav");

    private final UserTimeService userTimeService;

    private final List<String> groups;
    private final boolean isInstructor;
    private final String siteId;
    private final Map<String, List<Map<String, String>>> subnavData;
    private final List<Map<String, String>> topLevelPageProps;

    public LessonsSubNavBuilder(UserTimeService userTimeService, String siteId, boolean isInstructor, List<String> groups) {
        this.userTimeService = userTimeService;
        this.siteId = siteId;
        this.isInstructor = isInstructor;
        this.groups = groups;
        this.subnavData = new HashMap<>();
        this.topLevelPageProps = new ArrayList<>();
    }

    public String toJSON() {
        applyPrerequisites();

        final Map<String, Object> objectToSerialize = new HashMap<>();
        objectToSerialize.put("pages", this.subnavData);
        objectToSerialize.put("topLevelPageProps", this.topLevelPageProps);
        objectToSerialize.put("i18n", getI18n());
        objectToSerialize.put("siteId", this.siteId);
        objectToSerialize.put("isInstructor", this.isInstructor);

        return JSONObject.toJSONString(objectToSerialize);
    }


    public static List<String> collectPageIds(final List<Map<String, Object>> pages) {
        return pages.stream()
                .filter(p -> !p.containsKey("wellKnownToolId") || "sakai.lessonbuildertool".equals(p.get("wellKnownToolId")))
                .map(p -> String.valueOf(p.get("pageId")))
                .collect(Collectors.toList());
    }


    public void processResult(final String sakaiToolId, SimplePage parentPage, SimplePageItem spi, SimplePage page, SimplePageLogEntry le) {
        if (isHidden(page)) return;

        if (!this.subnavData.containsKey(sakaiToolId)) {
            this.subnavData.put(sakaiToolId, new ArrayList<>());
        }
        
        final Map<String, String> subnavItem = new HashMap<>();

        subnavItem.put("toolId", sakaiToolId);
        subnavItem.put("siteId", page.getSiteId());
	    subnavItem.put("sakaiPageId", parentPage.getToolId());
        subnavItem.put("itemId", Long.toString(spi.getId()));
        subnavItem.put("sendingPage", spi.getSakaiId());
        subnavItem.put("name", spi.getName());
        subnavItem.put("description", spi.getDescription());
        subnavItem.put("hidden", String.valueOf(page.isHidden() || page.isHiddenFromNavigation()));
        subnavItem.put("hiddenFromNavigation", String.valueOf(page.isHiddenFromNavigation()));
        subnavItem.put("required", String.valueOf(spi.isRequired()));
        subnavItem.put("completed", String.valueOf(le != null && le.isComplete()));
        subnavItem.put("prerequisite", String.valueOf(spi.isPrerequisite()));

        processDateReleased(page, subnavItem);

        boolean contains = true;
        String group = spi.getGroups();
        if (StringUtils.isNotEmpty(group) && !isInstructor) {
            contains = Arrays.stream(group.split(",")).anyMatch(groups::contains);
            // nothing needed for if the user is in the group it will display as normal
            // if the user is not in the groups, subpage is marked hidden above
            if (!contains) subnavItem.put("hidden", "true");
        }
        // only send the subpage if user is in the group
        if (contains) this.subnavData.get(sakaiToolId).add(subnavItem);
    }

    public void processTopLevelPageProperties(final String sakaiToolId, SimplePage page, SimplePageItem spi, SimplePageLogEntry le) {
        if (isHidden(page)) return;

        final Map<String, String> pageProps = new HashMap<>();

        pageProps.put("toolId", sakaiToolId);
        pageProps.put("siteId", page.getSiteId());
        pageProps.put("name", page.getTitle());
        pageProps.put("hidden", String.valueOf(page.isHidden() || page.isHiddenFromNavigation()));
        pageProps.put("hiddenFromNavigation", String.valueOf(page.isHiddenFromNavigation()));
        pageProps.put("required", String.valueOf(spi.isRequired()));
        pageProps.put("completed", String.valueOf(le != null && le.isComplete()));
        pageProps.put("prerequisite", String.valueOf(spi.isPrerequisite()));

        processDateReleased(page, pageProps);
        this.topLevelPageProps.add(pageProps);

    }


    private void processDateReleased(SimplePage page, Map<String, String> pageProps) {
        if (page.getReleaseDate() != null) {
            Date releaseDate = page.getReleaseDate();
            if (releaseDate.getTime() > System.currentTimeMillis()) {
                pageProps.put("disabled", "true");
                DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(rb.getLocale());
                ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(releaseDate.toInstant(), userTimeService.getLocalTimeZone().toZoneId());
                pageProps.put("releaseDate", dtf.format(zonedDateTime));
            }
        }
    }


    private boolean isHidden(final SimplePage p) {
        if (this.isInstructor) return false;
        return p.isHidden();
    }


    private Map<String, String> getI18n() {
        final Map<String, String> translations = new HashMap<>();

        translations.put("expand", rb.getString("lessons_subnav.expand"));
        translations.put("collapse", rb.getString("lessons_subnav.collapse"));
        translations.put("open_top_level_page", rb.getString("lessons_subnav.open_top_level_page"));
        translations.put("hidden", rb.getString("lessons_subnav.hidden"));
        translations.put("hidden_with_release_date", rb.getString("lessons_subnav.hidden_with_release_date"));
        translations.put("main_link_name", rb.getString("lessons_subnav.main_link_name"));
        translations.put("prerequisite", rb.getString("lessons_subnav.prerequisite"));
        translations.put("prerequisite_and_disabled", rb.getString("lessons_subnav.prerequisite_and_disabled"));

        return translations;
    }

    private void applyPrerequisites() {
        for (final String pageId : this.subnavData.keySet()) {
            applyPrerequisitesToPageList(this.subnavData.get(pageId));
        }

        applyPrerequisitesToPageList(this.topLevelPageProps);
    }

    private void applyPrerequisitesToPageList(List<Map<String, String>> pages) {
        boolean prerequisiteApplies = false;
        for (Map<String, String> pageData : pages) {

            // if a sibling page with a smaller sequence is required
            // then disable the current page for students
            if (pageData.get("prerequisite").equals("true") && prerequisiteApplies) {
                pageData.put("disabledDueToPrerequisite", "true");
                pageData.put("disabled", String.valueOf(!this.isInstructor));
            }

            // only disable pages that have prerequisites below the current page
            // when the current page is required and the user is yet to complete it
            if (pageData.get("required").equals("true")) {
                if (pageData.get("completed").equals("false")) {
                    prerequisiteApplies = true;
                }
            }
        }
    }
}
