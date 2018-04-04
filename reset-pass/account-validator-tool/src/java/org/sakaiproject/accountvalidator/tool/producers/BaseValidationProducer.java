/**
 * Copyright (c) 2007-2017 The Apereo Foundation
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

import org.sakaiproject.accountvalidator.logic.ValidationLogic;
import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.accountvalidator.tool.params.ValidationViewParams;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.user.api.UserDirectoryService;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.springutil.SpringMessageLocator;

/**
 * A base class to minimize code duplication in the producers
 * @author bbailla2
 */
@Slf4j
public class BaseValidationProducer implements ViewParamsReporter
{

	protected ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService (ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

	protected ValidationLogic validationLogic;
	public void setValidationLogic(ValidationLogic validationLogic)
	{
		this.validationLogic = validationLogic;
	}

	protected UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}

	protected AuthzGroupService authzGroupService;
	public void setAuthzGroupService(AuthzGroupService authzGroupService)
	{
		this.authzGroupService = authzGroupService;
	}

	protected SiteService siteService;
	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	protected DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(DeveloperHelperService developerHelperService)
	{
		this.developerHelperService = developerHelperService;
	}

	public String getPortalURL()
	{
		return developerHelperService.getPortalURL();
	}

	protected TargettedMessageList tml;
	public void setTargettedMessageList(TargettedMessageList tml)
	{
		this.tml = tml;
	}

	protected SpringMessageLocator messageLocator;
	public void setSpringMessageLocator(SpringMessageLocator messageLocator)
	{
		this.messageLocator = messageLocator;
	}

	public void init()
	{

	}

	/**
	 * Overrides the view parameters so that we can grab the validation tokenId
	 */
	public ViewParameters getViewParameters()
	{
		return new ValidationViewParams();
	}

	public ValidationAccount getValidationAccount(ViewParameters viewparams, TargettedMessageList tml)
	{
		ValidationAccount va = null;
		if (viewparams instanceof ValidationViewParams)
		{
			ValidationViewParams vvp = (ValidationViewParams) viewparams;
			if (vvp.tokenId == null || "".equals(vvp.tokenId))
			{
				tml.addMessage(new TargettedMessage("msg.noCode", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
				return null;
			}
			log.debug("getting token: " + vvp.tokenId);
			va = validationLogic.getVaLidationAcountBytoken(vvp.tokenId);
			if (va == null)
			{
				Object [] args = new Object[]{ vvp.tokenId};
				tml.addMessage(new TargettedMessage("msg.noSuchValidation", args, TargettedMessage.SEVERITY_ERROR));
				return null;
			}
		}
		return va;
	}


	/**
	 * Adds a link to the page for the user to request another validation token
	 * @param toFill the parent of the link
	 */
	protected void addResetPassLink(UIContainer toFill, ValidationAccount va)
	{
		if (toFill == null || va == null)
		{
			// enforce method contract
			throw new IllegalArgumentException("null passed to addResetPassLink()");
		}

		//the url to reset-pass - assume it's on the gateway. Otherwise, we don't render a link and we log a warning
		String url = null;
		try
		{
			//get the link target
			url = getPasswordResetUrl();
		}
		catch (IllegalArgumentException e)
		{
			log.warn("Couldn't create a link to reset-pass; no instance of reset-pass found on the gateway");
		}

		if (url != null)
		{
			//add the container
			UIBranchContainer requestAnotherContainer = UIBranchContainer.make(toFill, "requestAnotherContainer:");
			//add a label
			UIMessage.make(requestAnotherContainer, "request.another.label", "validate.requestanother.label");
			//add the link to reset-pass
			String requestAnother = null;
			if (ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET == va.getAccountStatus())
			{
				requestAnother = messageLocator.getMessage("validate.requestanother.reset");
			}
			else
			{
				requestAnother = messageLocator.getMessage("validate.requestanother");
			}
			UILink.make(requestAnotherContainer, "request.another", requestAnother, url);
		}
		//else - there is no reset pass instance on the gateway, but the user sees an appropriate message regardless (handled by a targetted message)
	}

	/**
	 * Gets the password reset URL. If looks for a configured URL, otherwise it looks
	 * for the password reset tool in the gateway site and builds a link to that.
	 * @return The password reset URL or <code>null</code> if there isn't one or we
	 * can't find the password reset tool.
	 */
	public String getPasswordResetUrl()
	{
		// Has a password reset url been specified in sakai.properties? If so, it rules.
		String passwordResetUrl = serverConfigurationService.getString("login.password.reset.url", null);

		if(passwordResetUrl == null) {
			// No explicit password reset url. Try and locate the tool on the gateway page.
			// If it has been  installed we'll use it.
			String gatewaySiteId = serverConfigurationService.getGatewaySiteId();
			Site gatewaySite = null;
			try {
				gatewaySite = siteService.getSite(gatewaySiteId);
				ToolConfiguration resetTC = gatewaySite.getToolForCommonId("sakai.resetpass");
				if(resetTC != null) {
					passwordResetUrl = resetTC.getContainingPage().getUrl();
				}
			} catch (IdUnusedException e) {
				log.warn("No " + gatewaySiteId + " site found whilst building password reset url, set password.reset.url" +
						" or create " + gatewaySiteId + " and add password reset tool.");
			}
		}
		return passwordResetUrl;
	}

	/**
	 * Determines if the accountValidator.sendLegacyLinks property is enabled - ie. whether the account validation form is one single page or split up
	 */
	public boolean sendLegacyLinksEnabled()
	{
		return serverConfigurationService.getBoolean("accountValidator.sendLegacyLinks", false);
	}

	/**
	 * Renders a link whose rsf:id is "redirectLink". This link will point at the legacy account validation form
	 */
	public void redirectToLegacyLink(UIContainer tofill, ValidationAccount va)
	{
		//If there's any way to automatically redirect to the desired page, please implement that instead.

		//get an appropriate message for the link
		Integer accountStatus = va.getAccountStatus();
		String statusMessage = "msg.acceptInvitation";
		if (accountStatus != null)
		{
			if (accountStatus.equals(ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET))
			{
				statusMessage = "msg.resetPassword";
			}
		}
		String[] args = new String []{serverConfigurationService.getString("ui.service", "Sakai")};
		statusMessage = messageLocator.getMessage(statusMessage, args);

		//build the href
		String url = getViewURL("validate", va);

		UILink.make(tofill, "redirectLink", statusMessage, url);
	}

	public String getViewURL(String view, ValidationAccount va)
	{
		String serverUrl = serverConfigurationService.getServerUrl();
		return serverUrl + "/accountvalidator/faces/" + view + "?tokenId=" + va.getValidationToken();
	}

	public String getUIService()
	{
		return serverConfigurationService.getString("ui.service", "Sakai");
	}

	public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn)
	{
		if (result.resultingView instanceof ValidationViewParams)
		{
			if ("success".equals(actionReturn))
			{
				result.resultingView = new RawViewParameters(getPortalURL());
			}
		}
	}
}
