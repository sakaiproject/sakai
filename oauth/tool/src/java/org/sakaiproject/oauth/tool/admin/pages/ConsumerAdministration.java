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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.oauth.domain.Consumer;
import org.sakaiproject.oauth.service.OAuthAdminService;
import org.sakaiproject.oauth.tool.pages.SakaiPage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * @author Colin Hebert
 */
public class ConsumerAdministration extends SakaiPage {
    private final Consumer consumer;
    @SpringBean
    private FunctionManager functionManager;
    @SpringBean
    private OAuthAdminService oAuthAdminService;

    public ConsumerAdministration() {
        consumer = new Consumer();
        // Manually set an empty Set for rights to avoid confusion with CheckBoxMultipleChoice
        // and not ending up with a List
        consumer.setRights(new HashSet<String>());
        init(false);
    }

    public ConsumerAdministration(PageParameters parameters) {
        super(parameters);
        String consumerId = parameters.get("consumer").toString();
        consumer = oAuthAdminService.getConsumer(consumerId);
        init(true);
    }

    private void init(final boolean edit) {
        addMenuLink(ListConsumers.class, new ResourceModel("menu.list.consumer"), null);
        addMenuLink(ConsumerAdministration.class, new ResourceModel("menu.add.consumer"), null);

        Form consumerForm = new Form<Void>("consumerForm") {
            @Override
            protected void onSubmit() {
                super.onSubmit();
                try {
                    if (edit)
                        oAuthAdminService.updateConsumer(consumer);
                    else
                        oAuthAdminService.createConsumer(consumer);
                    setResponsePage(ListConsumers.class);
                    getSession().info(consumer.getName() + " has been saved.");
                } catch (Exception e) {
                    error("Couldn't update '" + consumer.getName() + "': " + e.getLocalizedMessage());
                }
            }
        };

        TextField<String> idTextField;
        if (edit) {
            idTextField = new TextField<String>("id");
            idTextField.add(new AttributeModifier("disabled", "disabled"));
            idTextField.setModel(Model.of(consumer.getId()));
        } else {
            idTextField = new RequiredTextField<String>("id", new PropertyModel<String>(consumer, "id"));
        }
        consumerForm.add(idTextField);

        consumerForm.add(new RequiredTextField<String>("name", new PropertyModel<String>(consumer, "name")));
        consumerForm.add(new TextArea<String>("description", new PropertyModel<String>(consumer, "description")));
        consumerForm.add(new TextField<String>("url", new PropertyModel<String>(consumer, "url")));
        consumerForm.add(new TextField<String>("callbackUrl", new PropertyModel<String>(consumer, "callbackUrl")));
        consumerForm.add(new RequiredTextField<String>("secret", new PropertyModel<String>(consumer, "secret")));
        consumerForm.add(new TextField<String>("accessorSecret",
                new PropertyModel<String>(consumer, "accessorSecret")));
        consumerForm.add(new TextField<Integer>("defaultValidity",
                new PropertyModel<Integer>(consumer, "defaultValidity")));

        // Create a list of possible rights as checkboxes, pre-check already granted permissions
        CheckBoxMultipleChoice<String> rightCheckboxes = new CheckBoxMultipleChoice<String>("rights",
                new PropertyModel<Collection<String>>(consumer, "rights"), getAvailableFunctions());
        consumerForm.add(rightCheckboxes);

        add(new Label("consumerName", consumer.getName()));
        add(consumerForm);
    }

    private List<String> getAvailableFunctions() {
        return functionManager.getRegisteredFunctions();
    }
}
