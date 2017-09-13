/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.sitemembers.ui.components;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.profile2.util.ProfileConstants;

import org.sakaiproject.sitemembers.ui.WidgetPage;
import lombok.extern.slf4j.Slf4j;

/**
 * Generic grid panel that can be used to render a list of {@link WidgetPage.GridPerson}s as their images
 */
@Slf4j
public class ConnectionsGrid extends Panel {

    private static final long serialVersionUID = 1L;
    private int cols = 4;
    private String prefix = "";

    public ConnectionsGrid(final String id, final IModel<List<? extends WidgetPage.GridPerson>> iModel, int cols, boolean isCourse) {
        super(id, iModel);
        this.cols = cols;
        if(isCourse) {
            prefix = "course.heading.";
        } else {
            prefix = "other.heading.";
        }
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        @SuppressWarnings("unchecked")
        final List<WidgetPage.GridPerson> connections = (List<WidgetPage.GridPerson>) getDefaultModelObject();

        final int nUsers = connections.size();
        int rows = (nUsers+cols-1)/cols; /* round up number of rows */

        final ListDataProvider<WidgetPage.GridPerson> dataProvider = new ListDataProvider<WidgetPage.GridPerson>(connections);

        final GridView<WidgetPage.GridPerson> gridView = new GridView<WidgetPage.GridPerson>("rows", dataProvider) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(final Item<WidgetPage.GridPerson> item) {
                final WidgetPage.GridPerson connection = item.getModelObject();
                final String connection_uuid = connection.uuid;
                final ProfileThumbnail img = new ProfileThumbnail("img", Model.of(connection_uuid));

                final String url = "javascript:;";

                img.add(new AttributeModifier("href", url));
                img.add(new AttributeModifier("target", "_top"));
                img.add(new AttributeModifier("data-user-id", connection_uuid));
                item.add(img);

                //name link
                ExternalLink l = new ExternalLink("name", url, connection.displayName);
                l.add(new AttributeModifier("data-user-id", connection_uuid));
                item.add(l);

                //role
                if (connection.role != "STUDENT"){
                    ResourceModel rm = new ResourceModel(prefix+connection.role.toLowerCase());
                    item.add(new Label("role", rm.getObject()));
                } else {
                    item.add(new EmptyPanel("role"));
                }

                String t = "online-status";
                if(connection.onlineStatus  == ProfileConstants.ONLINE_STATUS_ONLINE){
                    t += " online";
                }
                else if(connection.onlineStatus == ProfileConstants.ONLINE_STATUS_AWAY){
                    t += " away";
                } else {
                    t += " offline";
                }

                Label lbl = new Label("online_status", "");
                lbl.add(new AttributeModifier("class",t));

                item.add(lbl);
            }

            @Override
            protected void populateEmptyItem(final Item<WidgetPage.GridPerson> item) {
                item.add(new EmptyPanel("img"));
                item.add(new EmptyPanel("name"));
                item.add(new EmptyPanel("role"));
                item.setVisible(false);
            }
        };

        /* handle case where nUsers=0 */
        if(rows <=0) {rows = 1;}
        gridView.setRows(rows);
        gridView.setColumns(cols);

        add(gridView);
    }
}
