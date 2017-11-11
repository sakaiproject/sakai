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
package org.sakaiproject.oauth.tool.user.pages;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.oauth.domain.Accessor;
import org.sakaiproject.oauth.domain.Consumer;
import org.sakaiproject.oauth.exception.InvalidConsumerException;
import org.sakaiproject.oauth.service.OAuthService;
import org.sakaiproject.oauth.tool.pages.SakaiPage;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Colin Hebert
 */
public class ListAccessors extends SakaiPage {
    @SpringBean
    private SessionManager sessionManager;
    @SpringBean
    private OAuthService oAuthService;

    public ListAccessors() {
        String userId = sessionManager.getCurrentSessionUserId();
        Collection<Accessor> accessors = oAuthService.getAccessAccessorForUser(userId);
        ListView<Accessor> accessorList = new ListView<Accessor>("accessorlist", new ArrayList<>(accessors)) {
            @Override
            protected void populateItem(ListItem<Accessor> components) {
                try {
                    final Consumer consumer = oAuthService.getConsumer(components.getModelObject().getConsumerId());
                    ExternalLink consumerHomepage = new ExternalLink("consumerUrl", consumer.getUrl(),
                            consumer.getName());
                    consumerHomepage.add(new AttributeModifier("target", "_blank"));
                    consumerHomepage.setEnabled(consumer.getUrl() != null && !consumer.getUrl().isEmpty());
                    components.add(consumerHomepage);
                    components.add(new Label("consumerDescription", consumer.getDescription()));
                    components.add(new Label("creationDate", new StringResourceModel("creation.date", new Model<>(components.getModelObject().getCreationDate()))));
                    components.add(new Label("expirationDate", new StringResourceModel("expiration.date", new Model<>(components.getModelObject().getExpirationDate()))));

                    components.add(new Link<Accessor>("delete", components.getModel()) {
                        @Override
                        public void onClick() {
                            try {
                                oAuthService.revokeAccessor(getModelObject().getToken());
                                setResponsePage(getPage().getClass());
                                getSession().info(consumer.getName() + "' token has been removed.");
                            } catch (Exception e) {
                                warn("Couldn't remove " + consumer.getName() + "'s token': " + e.getLocalizedMessage());
                            }
                        }
                    });
                } catch (InvalidConsumerException invalidConsumerException) {
                    // Invalid consumer, it is probably deleted
                    // For security reasons, this token should be revoked
                    oAuthService.revokeAccessor(components.getModelObject().getToken());
                    components.setVisible(false);
                }
            }

            @Override
            public boolean isVisible() {
                return !getModelObject().isEmpty() && super.isVisible();
            }
        };
        add(accessorList);

        Label noAccessorLabel = new Label("noAccessor", new ResourceModel("no.accessor"));
        noAccessorLabel.setVisible(!accessorList.isVisible());
        add(noAccessorLabel);
    }
}
