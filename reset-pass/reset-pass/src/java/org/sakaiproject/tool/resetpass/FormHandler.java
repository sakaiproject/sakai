package org.sakaiproject.tool.resetpass;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.accountvalidator.logic.ValidationLogic;
import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;

import uk.org.ponder.messageutil.MessageLocator;

public class FormHandler {

	private static java.lang.String SECURE_UPDATE_USER_ANY = org.sakaiproject.user.api.UserDirectoryService.SECURE_UPDATE_USER_ANY;

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {

		this.messageLocator = messageLocator;
	}


	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService ds){
		this.userDirectoryService = ds;
	}


	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService s) {
		this.serverConfigurationService = s;
	}

	private EmailService emailService;
	public void setEmailService(EmailService e) {
		this.emailService = e;
	}

	private RetUser userBean;
	public void setUserBean(RetUser u){
		this.userBean = u;
	}

	private EventTrackingService  eventService;
	public void setEventService(EventTrackingService  etc) {
		eventService=etc;
	}

	private SecurityService securityService;
	public void setSecurityService(SecurityService ss) {
		securityService = ss;
	}

	private ValidationLogic validationLogic;	
	public void setValidationLogic(ValidationLogic validationLogic) {
		this.validationLogic = validationLogic;
	}


	private static Log m_log  = LogFactory.getLog(FormHandler.class);

	public String processAction() {
		//siteManage.validateNewUsers = false use the classic method:
		boolean validatingAccounts = serverConfigurationService.getBoolean("siteManage.validateNewUsers", true);
		if (! validatingAccounts) {
			return resetPassClassic();
		}

		//otherwise lets we need some info on the user.
		//is the user validated?
		String userId = userBean.getUser().getId().trim();
		if (!validationLogic.isAccountValidated(userId)) {
			m_log.debug("account is not validated");
			//its possible that the user has an outstanding Validation
			ValidationAccount va = validationLogic.getVaLidationAcountByUserId(userId);
			if (va == null) {
				//we need to validate the account.
				m_log.debug("This is a legacy user to validate!");
				validationLogic.createValidationAccount(userBean.getUser().getId(), ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET);
			} else {
				m_log.debug("resending validation");
				validationLogic.resendValidation(va.getValidationToken());
			}
			
			
			return "Success";
		} else {
			//there may be a pending VA that needs to be verified
			ValidationAccount va = validationLogic.getVaLidationAcountByUserId(userId);
			if (va == null ) {
				//the account is validated we need to send a password reset
				m_log.info("no account found!");
				validationLogic.createValidationAccount(userId, ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET);
			} else if (va.getValidationReceived() == null) {
				m_log.debug("no response on validation!");
				validationLogic.resendValidation(va.getValidationToken());
			} else {
				m_log.debug("creating a new validation for password reset");
				validationLogic.createValidationAccount(userId, ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET);
			}
			return "Success";
		}
	}

	/**
	 * The classic method that mails a new password
	 * template is constructed from strings in resource bundle
	 * @return
	 */
	private String resetPassClassic() {
		m_log.info("getting password for " + userBean.getEmail());

		String from = serverConfigurationService.getString("setup.request", null);
		if (from == null)
		{
			m_log.warn(this + " - no 'setup.request' in configuration");
			from = "postmaster@".concat(serverConfigurationService.getServerName());
		}

		//now we need to reset the password
		SecurityAdvisor sa = new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference) {
				if (SECURE_UPDATE_USER_ANY.equals(function)) {
					return SecurityAdvice.ALLOWED;
				}
				return SecurityAdvice.PASS;
			}
		};
		
		try {

			// Need: SECURE_UPDATE_USER_ANY
			securityService.pushAdvisor(sa);

			UserEdit userE = userDirectoryService.editUser(userBean.getUser().getId().trim());
			String pass = getRandPass();
			userE.setPassword(pass);
			userDirectoryService.commitEdit(userE);

			//securityService.popAdvisor(sa);

			String productionSiteName = serverConfigurationService.getString("reset-pass.productionSiteName", "");
			if(productionSiteName == null || "".equals(productionSiteName))
				productionSiteName = serverConfigurationService.getString("ui.service", "");

			StringBuffer buff = new StringBuffer();
			buff.setLength(0);
			buff.append(messageLocator.getMessage("mailBodyPre",userE.getDisplayName()) + "\n\n");

			buff.append(messageLocator.getMessage("mailBody1",new Object[]{productionSiteName, serverConfigurationService.getPortalUrl()})+ "\n\n");
			buff.append(messageLocator.getMessage("mailBody2",new Object[]{userE.getEid()})+ "\n");
			buff.append(messageLocator.getMessage("mailBody3",new Object[]{pass})+ "\n\n");

			if (serverConfigurationService.getString("support.email", null) != null )
				buff.append(messageLocator.getMessage("mailBody4",new Object[]{serverConfigurationService.getString("support.email")}) + "\n\n");

			m_log.debug(messageLocator.getMessage("mailBody1",new Object[]{productionSiteName}));
			buff.append(messageLocator.getMessage("mailBodySalut")+"\n");
			buff.append(messageLocator.getMessage("mailBodySalut1",productionSiteName));

			String body = buff.toString();


			List<String> headers = new ArrayList<String>();
			headers.add("Precedence: bulk");

			emailService.send(from,userBean.getUser().getEmail(),messageLocator.getMessage("mailSubject", new Object[]{productionSiteName}),body,
					userBean.getUser().getEmail(), null, headers);

			m_log.info("New password emailed to: " + userE.getEid() + " (" + userE.getId() + ")");
			eventService.post(eventService.newEvent("user.resetpass", userE.getReference() , true));

		}
		catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
		finally {
			securityService.popAdvisor(sa);
		}

		return "Success";
	}

	//borrowed from siteaction
	private String getRandPass() {
		// set password to a random positive number
		Random generator = new Random(System.currentTimeMillis());
		Integer num = Integer.valueOf(generator.nextInt(Integer.MAX_VALUE));
		if (num.intValue() < 0) num = Integer.valueOf(num.intValue() *-1);
		return num.toString();
	}
}
