/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2019 The Sakai Foundation
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

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;

import org.sakaiproject.sitestats.tool.wicket.components.AjaxLazyLoadFragment;

public class StudentVisitsWidget extends Panel {
    private List<WidgetMiniStat> widgetMiniStats = null;

    public StudentVisitsWidget(String id, List<WidgetMiniStat> widgetMiniStats) {
        super(id);
        this.widgetMiniStats = widgetMiniStats;
    }

    @Override
    protected void onBeforeRender() {
        renderWidget();
        super.onBeforeRender();
    }

    private void renderWidget() {
        setRenderBodyOnly(true);
        removeAll();

        // MiniStats ajax load behavior
        AjaxLazyLoadFragment ministatContainer = new AjaxLazyLoadFragment("ministatContainer") {
            private static final long   serialVersionUID    = 12L;

            @Override
            public Fragment getLazyLoadFragment(String markupId) {
                return StudentVisitsWidget.this.getLazyLoadedMiniStats(markupId);
            }

            @Override
            public Component getLoadingComponent(String markupId) {
                StringBuilder loadingComp = new StringBuilder();
                loadingComp.append("<div class=\"ministat load\">");
                loadingComp.append("  <img src=\"");
                loadingComp.append(RequestCycle.get().urlFor(AbstractDefaultAjaxBehavior.INDICATOR, null));
                loadingComp.append("\" alt=\"" + (String) new ResourceModel("statistics_loading").getObject() + "\"/>");
                loadingComp.append("</div>");
                return new Label(markupId, loadingComp.toString()).setEscapeModelStrings(false);
            }
        };
        add(ministatContainer);
    }

    private Fragment getLazyLoadedMiniStats(String markupId) {
        Fragment ministatFragment = new Fragment(markupId, "ministatFragment", this);
        int miniStatsCount = widgetMiniStats != null ? widgetMiniStats.size() : 0;
        Loop miniStatsLoop = new Loop("widgetRow", miniStatsCount) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void populateItem(LoopItem item) {
                int index = item.getIndex();
                WidgetMiniStat ms = widgetMiniStats.get(index);

                Label widgetValue = new Label("widgetValue", Model.of(ms.getValue()));
                Label widgetLabel = new Label("widgetLabel", Model.of(ms.getLabel()));
                WebMarkupContainer widgetIcon = new WebMarkupContainer("widgetIcon");
                widgetIcon.add(new AttributeAppender("class", " " + ms.getSecondValue()));

                item.add(widgetValue);
                item.add(widgetLabel);
                item.add(widgetIcon);
            }
        };
        ministatFragment.add(miniStatsLoop);
        return ministatFragment;
    }
}
