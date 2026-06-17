/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.widget;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_PAGE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_LESSONS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.sitestats.api.Util;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LessonsWidget extends Panel {

    private static final long       serialVersionUID    = 1L;

    /** The site id. */
    private String                  siteId              = null;
    private int                     totalPages          = -1;

    /**
     * Default constructor.
     * @param id The wicket:id
     * @param siteId The related site id
     */
    public LessonsWidget(String id, final String siteId) {

        super(id);
        this.siteId = siteId;
        setRenderBodyOnly(true);

        // Single values (MiniStat)
        List<WidgetMiniStat> widgetMiniStats = new ArrayList<WidgetMiniStat>();
        widgetMiniStats.add(getMiniStatPages());
        widgetMiniStats.add(getMiniStatReadPages());
        widgetMiniStats.add(getMiniStatMostReadPage());
        widgetMiniStats.add(getMiniStatUserThatReadMorePages());

        // Tabs
        List<AbstractTab> tabs = new ArrayList<AbstractTab>();
        tabs.add(new AbstractTab(new ResourceModel("overview_tab_bydate")) {
            private static final long   serialVersionUID    = 1L;
            @Override
            public Panel getPanel(String panelId) {
                return getWidgetTabByDate(panelId);
            }
        });
        tabs.add(new AbstractTab(new ResourceModel("overview_tab_byuser")) {
            private static final long   serialVersionUID    = 1L;
            @Override
            public Panel getPanel(String panelId) {
                return getWidgetTabByUser(panelId);
            }
        });
        tabs.add(new AbstractTab(new ResourceModel("overview_tab_bypage")) {
            private static final long   serialVersionUID    = 1L;
            @Override
            public Panel getPanel(String panelId) {
                return getWidgetTabByPage(panelId);
            }
        });

        // Final Widget object
        String title = (String) new ResourceModel("overview_title_lessonpages").getObject();
        Widget widget = new Widget("widget", WIDGET_LESSONS, "sakai-gradebook-tool", title, widgetMiniStats, tabs, siteId);
        add(widget);
    }

    // -------------------------------------------------------------------------------
    /** MiniStat:: Pages count */
    private WidgetMiniStat getMiniStatPages() {

        return new WidgetMiniStat() {

            private static final long   serialVersionUID    = 1L;
            @Override
            public String getValue() {
                return Integer.toString(getTotalPages());
            }
            @Override
            public String getSecondValue() {
                return null;
            }
            @Override
            public String getTooltip() {
                return null;
            }
            @Override
            public boolean isWiderText() {
                return false;
            }
            @Override
            public String getLabel() {
                return (String) new ResourceModel("overview_title_pages_sum").getObject();
            }
        };
    }

    /** MiniStat:: Read pages */
    private WidgetMiniStat getMiniStatReadPages() {

        return new WidgetMiniStat() {

            private static final long   serialVersionUID            = 1L;
            private int                totalDistinctPageReads      = -1;

            @Override
            public String getValue() {

                return Integer.toString(getTotalReadPages());
            }

            @Override
            public String getSecondValue() {

                double percentage = getTotalPages()==0 ? 0 : Util.round(100 * totalDistinctPageReads / (double) getTotalPages(), 0);
                return String.valueOf((int) percentage) + '%';
            }

            @Override
            public String getTooltip() {
                return null;
            }

            @Override
            public boolean isWiderText() {
                return false;
            }

            @Override
            public String getLabel() {
                return (String) new ResourceModel("overview_title_readpages_sum").getObject();
            }

            /** Return total (existent) files (excluding collections). */
            private int getTotalReadPages() {

                if (totalDistinctPageReads == -1) {
                    try {
                        totalDistinctPageReads = Locator.getFacade().getStatsManager().getTotalReadLessonPages(siteId);
                    } catch (Exception e) {
                        log.error("Caught exception while getting the read pages total. Setting totalDistinctReadPages to 0 ...", e);
                        totalDistinctPageReads = 0;
                    }
                } else {
                    log.debug("totalDistinctReadPages has already been set and won't be updated.");
                }
                return totalDistinctPageReads;
            }

        };
    }

    /** MiniStat:: Most read page */
    private WidgetMiniStat getMiniStatMostReadPage() {

        return new WidgetMiniStat() {
            private static final long   serialVersionUID            = 1L;
            private String              mostReadPage              = null;

            @Override
            public String getValue() {

                if (mostReadPage == null) {
                    try {
                        mostReadPage = Locator.getFacade().getStatsManager().getMostReadLessonPage(siteId);
                    } catch (Exception e) {
                        log.error("Caught exception while getting the most read page. Setting mostReadPage to \"\" ...", e);
                        mostReadPage = "";
                    }
                } else {
                    log.debug("mostReadPage has already been set and won't be updated.");
                }

                return mostReadPage;
            }

            @Override
            public String getSecondValue() {
                return null;
            }

            @Override
            public String getTooltip() {
                return mostReadPage;
            }

            @Override
            public boolean isWiderText() {
                return true;
            }

            @Override
            public String getLabel() {
                return (String) new ResourceModel("overview_title_mostreadpage_sum").getObject();
            }

        };
    }

    /** MiniStat:: User that opened more file */
    private WidgetMiniStat getMiniStatUserThatReadMorePages() {

        return new WidgetMiniStat() {

            private static final long   serialVersionUID            = 1L;
            private String              user                        = null;

            @Override
            public String getValue() {

                String val = null;
                if (user == null) {
                    user = Locator.getFacade().getStatsManager().getMostActiveLessonPageReader(siteId);
                }
                if (user != null) {
                    String id = null;
                    if ("-".equals(user) || EventTrackingService.UNKNOWN_USER.equals(user)){
                        id = "-";
                    } else {
                        try {
                            id = Locator.getFacade().getUserDirectoryService().getUser(user).getDisplayId();
                        } catch (UserNotDefinedException e1){
                            id = user;
                        }
                    }
                    val = id;
                } else {
                    val = "-";
                }
                return val;
            }

            @Override
            public String getSecondValue() {
                return null;
            }

            @Override
            public String getTooltip() {

                if (user != null) {
                    String name = null;
                    if (("-").equals(user)) {
                        name = (String) new ResourceModel("user_anonymous").getObject();
                    }else if (EventTrackingService.UNKNOWN_USER.equals(user)) {
                        name = (String) new ResourceModel("user_anonymous_access").getObject();
                    } else {
                        name = Locator.getFacade().getStatsManager().getUserNameForDisplay(user);
                    }
                    return name;
                } else {
                    return null;
                }
            }

            @Override
            public boolean isWiderText() {
                return true;
            }

            @Override
            public String getLabel() {
                return (String) new ResourceModel("overview_title_userreadmorepage_sum").getObject();
            }

        };
    }

    /** WidgetTab: By date */
    protected WidgetTabTemplate getWidgetTabByDate(String panelId) {

        return new WidgetTabTemplate(panelId, LessonsWidget.this.siteId, WIDGET_LESSONS, TAB_BY_DATE) {
            private static final long   serialVersionUID    = 1L;

            @Override
            public List<Integer> getFilters() {
                return Arrays.asList(FILTER_DATE, FILTER_ROLE, FILTER_LESSON_ACTION);
            }
        };
    }

    /** WidgetTab: By user */
    protected WidgetTabTemplate getWidgetTabByUser(String panelId) {
        return new WidgetTabTemplate(panelId, LessonsWidget.this.siteId, WIDGET_LESSONS, TAB_BY_USER) {
            private static final long   serialVersionUID    = 1L;

            @Override
            public List<Integer> getFilters() {
                return Arrays.asList(FILTER_DATE, FILTER_ROLE, FILTER_LESSON_ACTION);
            }
        };
    }

    /** WidgetTab: By page */
    protected WidgetTabTemplate getWidgetTabByPage(String panelId) {

        return new WidgetTabTemplate(panelId, LessonsWidget.this.siteId, WIDGET_LESSONS, TAB_BY_PAGE) {

            private static final long   serialVersionUID    = 1L;

            @Override
            public List<Integer> getFilters() {
                return Arrays.asList(FILTER_DATE, FILTER_ROLE, FILTER_LESSON_ACTION);
            }
        };
    }

    /** Return total (existent) files (excluding collections). */
    private int getTotalPages() {

        if (totalPages == -1) {
            try {
                totalPages = Locator.getFacade().getStatsManager().getTotalLessonPages(siteId);
            } catch (Exception e) {
                log.error("Caught exception while getting the page total. Setting totalPages to 0 ...", e);
                totalPages = 0;
            }
        } else {
            log.debug("totalPages has already been set and won't be updated.");
        }
        return totalPages;
    }
}
