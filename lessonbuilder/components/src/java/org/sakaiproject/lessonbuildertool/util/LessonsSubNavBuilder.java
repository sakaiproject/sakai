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
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.portal.api.PortalSubPageData;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.util.ResourceLoader;


import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public class LessonsSubNavBuilder {

    private static final ResourceLoader rb = new ResourceLoader("subnav");

    private final UserTimeService userTimeService;
    private final ServerConfigurationService serverConfigurationService;

    private final boolean isInstructor;
    private final List<String> groups;
    private final PortalSubPageData subPageData;

    public LessonsSubNavBuilder(ServerConfigurationService serverConfigurationService,
                                UserTimeService userTimeService,
                                PortalSubPageData data,
                                boolean isInstructor,
                                List<String> groups) {
        this.serverConfigurationService = serverConfigurationService;
        this.userTimeService = userTimeService;
        this.groups = groups;
        this.subPageData = data;
        this.isInstructor = isInstructor;
    }

    public void toSubPageData(Collection<String> pageIds) {
        applyPrerequisites(pageIds);
        setI18n();
    }

    private void buildSubpageUrl(PortalSubPageData.PageData subpage) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(serverConfigurationService.getPortalUrl());
        uriBuilder.pathSegment("site", subpage.getSiteId(), "tool", subpage.getToolId(), "ShowPage");
        uriBuilder.queryParam("sendingPage", subpage.getSakaiPageId());
        uriBuilder.queryParam("itemId", subpage.getItemId());
        uriBuilder.queryParam("path", "clear_and_push");
        uriBuilder.queryParam("title", subpage.getName());
        uriBuilder.queryParam("newTopLevel", "false");
        subpage.setUrl(uriBuilder.build().toUriString());
    }

    public void processResult(String toolId, SimplePage parentPage, SimplePageItem spi, SimplePage page, SimplePageLogEntry le) {
        if (isHidden(page)) return;

        List<PortalSubPageData.PageData> subPages = subPageData
                .getPages()
                .computeIfAbsent(toolId, k -> new ArrayList<>());

        PortalSubPageData.PageData subPageItem = new PortalSubPageData.PageData();

        subPageItem.setToolId(toolId);
        subPageItem.setSiteId(page.getSiteId());
	    subPageItem.setSakaiPageId(parentPage.getToolId());
        subPageItem.setItemId(Long.toString(spi.getId()));
        subPageItem.setSendingPage(spi.getSakaiId());
        subPageItem.setName(spi.getName());
        subPageItem.setDescription(spi.getDescription());
        subPageItem.setHidden(page.isHidden());
        subPageItem.setRequired(spi.isRequired());
        subPageItem.setCompleted(le != null && le.isComplete());
        subPageItem.setPrerequisite(spi.isPrerequisite());
        buildSubpageUrl(subPageItem);

        processDateReleased(page, subPageItem);

        boolean contains = true;
        String group = spi.getGroups();
        if (StringUtils.isNotEmpty(group) && !isInstructor) {
            contains = Arrays.stream(group.split(",")).anyMatch(groups::contains);
            // nothing needed for if the user is in the group it will display as normal
            // if the user is not in the groups, subpage is marked hidden above
            if (!contains) subPageItem.setHidden(true);
        }
        // only send the subpage if user is in the group
        if (contains) subPages.add(subPageItem);
    }

    public void processTopLevelPageProperties(final String toolId, SimplePage page, SimplePageItem spi, SimplePageLogEntry le) {
        if (isHidden(page)) return;

        PortalSubPageData.PageProps pageProps = new PortalSubPageData.PageProps();

        pageProps.setToolId(toolId);
        pageProps.setSiteId(page.getSiteId());
        pageProps.setName(page.getTitle());
        pageProps.setIcon("si-sakai-lessonbuildertool");
        pageProps.setHidden(page.isHidden());
        pageProps.setRequired(spi.isRequired());
        pageProps.setCompleted(le != null && le.isComplete());
        pageProps.setPrerequisite(spi.isPrerequisite());

        processDateReleased(page, pageProps);
        subPageData.getTopLevelPageProps().add(pageProps);
    }

    private void processDateReleased(SimplePage page, PortalSubPageData.PageProps pageProps) {
        if (page.getReleaseDate() != null) {
            Date releaseDate = page.getReleaseDate();
            if (releaseDate.getTime() > System.currentTimeMillis()) {
                pageProps.setDisabled(true);
                DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(rb.getLocale());
                ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(releaseDate.toInstant(), userTimeService.getLocalTimeZone().toZoneId());
                pageProps.setReleaseDate(dtf.format(zonedDateTime));
            }
        }
    }

    private boolean isHidden(final SimplePage p) {
        if (isInstructor) return false;
        return p.isHidden();
    }


    // TODO does this need to be different for every tool?
    private void setI18n() {
        PortalSubPageData.I18n i18n = subPageData.getI18n();
        i18n.setExpand(rb.getString("lessons_subnav.expand"));
        i18n.setCollapse(rb.getString("lessons_subnav.collapse"));
        i18n.setOpenTopLevelPage(rb.getString("lessons_subnav.open_top_level_page"));
        i18n.setHidden(rb.getString("lessons_subnav.hidden"));
        i18n.setHiddenWithReleaseDate(rb.getString("lessons_subnav.hidden_with_release_date"));
        i18n.setMainLinkName(rb.getString("lessons_subnav.main_link_name"));
        i18n.setPrerequisite(rb.getString("lessons_subnav.prerequisite"));
        i18n.setPrerequisiteAndDisabled(rb.getString("lessons_subnav.prerequisite_and_disabled"));
    }

    private void applyPrerequisites(Collection<String> pageIds) {
        List<PortalSubPageData.PageData> pages = subPageData.getPages().entrySet().stream()
                .filter(e -> pageIds.contains(e.getKey()))
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toList());
        applyPrerequisitesToPageList(pages);

        List<String> toolIds = pages.stream()
                .map(PortalSubPageData.PageData::getToolId)
                .collect(Collectors.toList());
        applyPrerequisitesToPageList(subPageData.getTopLevelPageProps().stream()
                .filter(p -> toolIds.contains(p.getToolId()))
                .collect(Collectors.toList()));
    }

    public void applyPrerequisitesToPageList(List<? extends PortalSubPageData.PageProps> pageProps) {
        boolean prerequisiteApplies = false;
        for (PortalSubPageData.PageProps props : pageProps) {

            // if a sibling page with a smaller sequence is required
            // then disable the current page for students
            if (props.isPrerequisite() && prerequisiteApplies) {
                props.setDisabledDueToPrerequisite(true);
                props.setDisabled(!isInstructor);
            }

            // only disable pages that have prerequisites below the current page
            // when the current page is required and the user is yet to complete it
            if (props.isRequired()) {
                if (!props.isCompleted()) {
                    prerequisiteApplies = true;
                }
            }
        }
    }
}
