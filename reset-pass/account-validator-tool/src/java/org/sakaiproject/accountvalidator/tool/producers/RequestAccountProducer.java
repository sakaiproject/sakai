/**
 * Copyright (c) 2007-2016 The Apereo Foundation
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
package org.sakaiproject.accountvalidator.tool.producers;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.rsf.components.*;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;


/**
 * Produces requestAccount.html - builds a form that allows the user to claim an account that has been created for them
 * @author bbailla2
 */
@Slf4j
public class RequestAccountProducer extends BaseValidationProducer implements ViewComponentProducer, ActionResultInterceptor
{
    public static final String VIEW_ID = "requestAccount";

    public String getViewID()
    {
        return VIEW_ID;
    }

    public void init()
    {

    }

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker)
    {
        Object[] args = new Object[]{getUIService()};
        UIMessage.make(tofill, "welcome1", "validate.welcome1", args);
        UIMessage.make(tofill, "welcome2", "validate.welcome.request", args);

        ValidationAccount va = getValidationAccount(viewparams, tml);
        if (va == null)
        {
            // handled by getValidationAccount
            return;
        }
        else if (!va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_REQUEST_ACCOUNT))
        {
            // this form is not appropriate
            args = new Object[] {va.getValidationToken()};
            // no such validation of the required account status
            tml.addMessage(new TargettedMessage("msg.noSuchValidation", args, TargettedMessage.SEVERITY_ERROR));
            return;
        }
        else if (ValidationAccount.STATUS_CONFIRMED.equals(va.getStatus()))
        {
            args = new Object[] {va.getValidationToken()};
            tml.addMessage(new TargettedMessage("msg.alreadyValidated", args, TargettedMessage.SEVERITY_ERROR));
            return ;
        }
        else if (sendLegacyLinksEnabled())
        {
            // Ignore; the request account feature should work independently of legacy links
        }

        User u = null;
        try
        {
            u = userDirectoryService.getUser(EntityReference.getIdFromRef(va.getUserId()));
        }
        catch (UserNotDefinedException e)
        {
            log.error("user ID does not exist for ValidationAccount with tokenId: " + va.getValidationToken());
            tml.addMessage(new TargettedMessage("validate.userNotDefined", new Object[]{getUIService()}, TargettedMessage.SEVERITY_ERROR));
        }

        if (u != null)
        {
            // we need some values to fill in. The nulls are placeholders. Other UIs use the same message with createdBy.getDisplayName() and createdBy.getEmailAddress().
            args = new Object[]{
                serverConfigurationService.getString("ui.service", "Sakai"),
                null,
                null,
                u.getDisplayId()
            };

            UIForm detailsForm = UIForm.make(tofill, "setDetailsForm");

            UICommand.make(detailsForm, "addDetailsSub", UIMessage.make("submit.new.account"), "accountValidationLocator.validateAccount");

            String otp = "accountValidationLocator." + va.getValidationToken();
            UIMessage.make(detailsForm, "username.new", "username.new", args);
            UIOutput.make(detailsForm, "eid", u.getDisplayId());

            UIInput.make(detailsForm, "firstName", otp + ".firstName", u.getFirstName());
            UIInput.make(detailsForm, "surName", otp + ".surname", u.getLastName());

            boolean passwordPolicyEnabled = userDirectoryService.getPasswordPolicy() != null;
            String passwordPolicyEnabledJavaScript = "VALIDATOR.isPasswordPolicyEnabled = " + Boolean.toString(passwordPolicyEnabled) + ";";
            UIVerbatim.make(tofill, "passwordPolicyEnabled", passwordPolicyEnabledJavaScript);

            UIBranchContainer row1 = UIBranchContainer.make(detailsForm, "passrow1:");
            UIInput.make(row1, "password1", otp + ".password");

            UIBranchContainer row2 = UIBranchContainer.make(detailsForm, "passrow2:");
            UIInput.make(row2, "password2", otp + ".password2");

            // If we have some terms, get the user to accept them.
            if (!"".equals(serverConfigurationService.getString("account-validator.terms")))
            {
                String termsURL = serverConfigurationService.getString("account-validator.terms");
                UIBranchContainer termsRow = UIBranchContainer.make(detailsForm, "termsrow:");

                UIBoundBoolean.make(termsRow, "terms", otp + ".terms");
                // If someone wants to re-write this to be RSF like great, but this works.
                // Although it doesn't escape the bundle strings.
                String terms = messageLocator.getMessage("terms", new Object[]
                {
                    "<a href='" + termsURL + "' target='_new'>" + messageLocator.getMessage("terms.link")+"</a>"
                });
                UIVerbatim.make(termsRow, "termsLabel", terms);
            }
        }
    }
}
