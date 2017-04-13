/*
 * #%L
 * OAuth Tool
 * %%
 * Copyright (C) 2009 - 2013 The Sakai Foundation
 * %%
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
 * #L%
 */
package org.sakaiproject.oauth.tool.admin.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.oauth.domain.Consumer;
import org.sakaiproject.oauth.service.OAuthAdminService;
import org.sakaiproject.oauth.tool.pages.SakaiPage;

import java.util.ArrayList;

/**
 * @author Colin Hebert
 */
public class ListConsumers extends SakaiPage {
    @SpringBean
    private OAuthAdminService oAuthAdminService;

    public ListConsumers() {
        addMenuLink(ListConsumers.class, new ResourceModel("menu.list.consumer"), null);
        addMenuLink(ConsumerAdministration.class, new ResourceModel("menu.add.consumer"), null);

        ListView<Consumer> consumerList = new ListView<Consumer>("consumerlist",
                new ArrayList<Consumer>(oAuthAdminService.getAllConsumers())) {
            @Override
            protected void populateItem(ListItem<Consumer> components) {
                components.add(new Label("id", components.getModelObject().getId()));
                components.add(new Label("name", components.getModelObject().getName()));
                components.add(new Label("description", components.getModelObject().getDescription()));

                components.add(new BookmarkablePageLink<Consumer>("edit", ConsumerAdministration.class,
                        new PageParameters().add("consumer", components.getModelObject().getId())
                ));

                components.add(new Link<Consumer>("delete", components.getModel()) {
                    @Override
                    public void onClick() {
                        try {
                            oAuthAdminService.deleteConsumer(getModelObject());
                            setResponsePage(getPage().getClass());
                            getSession().info(getModelObject().getName() + " has been removed.");
                        } catch (Exception e) {
                            warn("Couldn't remove '" + getModelObject().getName() + "': " + e.getLocalizedMessage());
                        }
                    }
                });

                Link<Consumer> recordLink = new Link<Consumer>("record", components.getModel()) {
                    @Override
                    public void onClick() {
                        try {
                            oAuthAdminService.switchRecordMode(getModelObject());
                            setResponsePage(getPage().getClass());
                            getSession().info(getModelObject().getName() + " record mode has changed.");
                        } catch (Exception e) {
                            warn("Couldn't change record mode on " + getModelObject().getName() + "': "
                                    + e.getLocalizedMessage());
                        }
                    }
                };
                if (components.getModelObject().isRecordModeEnabled())
                    recordLink.add(new Label("recordLink", new ResourceModel("record.disable.link")));
                else
                    recordLink.add(new Label("recordLink", new ResourceModel("record.enable.link")));
                components.add(recordLink);

            }

            @Override
            public boolean isVisible() {
                return !getModelObject().isEmpty() && super.isVisible();
            }
        };
        add(consumerList);

        Label noConsumerLabel = new Label("noConsumer", new ResourceModel("no.consumer"));
        noConsumerLabel.setVisible(!consumerList.isVisible());
        add(noConsumerLabel);
    }
}
