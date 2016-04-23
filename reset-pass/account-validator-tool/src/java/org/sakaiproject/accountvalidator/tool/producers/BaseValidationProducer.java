package org.sakaiproject.accountvalidator.tool.producers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.accountvalidator.logic.ValidationLogic;
import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.accountvalidator.tool.params.ValidationViewParams;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.UserDirectoryService;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.springutil.SpringMessageLocator;

/**
 * A base class to minimize code duplication in the producers
 * @author bbailla2
 */
public class BaseValidationProducer implements ViewParamsReporter
{

	private static Logger log = LoggerFactory.getLogger(BaseValidationProducer.class);

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
