/**
 * $Id: MainProducer.java 105078 2012-02-24 23:00:38Z ottenhoff@longsight.com $
 * $URL: https://source.sakaiproject.org/svn/reset-pass/trunk/account-validator-tool/src/java/org/sakaiproject/accountvalidator/tool/producers/MainProducer.java $
 * DeveloperHelperService.java - entity-broker - Apr 13, 2008 5:42:38 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.accountvalidator.tool.producers;

import java.util.Locale;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;

import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.rsf.components.*;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Produces passwordReset.html - builds a form containing nothing more than the a password and confirm password field
 * @author bbailla2
 */
@Slf4j
public class PasswordResetProducer extends BaseValidationProducer implements ViewComponentProducer, ActionResultInterceptor {

	public static final String VIEW_ID = "passwordReset";
	private static final String MAX_PASSWORD_RESET_MINUTES = "accountValidator.maxPasswordResetMinutes";
	private static final int MAX_PASSWORD_RESET_MINUTES_DEFAULT = 60;
	private static LocaleGetter localeGetter;
	
	public void setLocaleGetter(LocaleGetter localeGetter)
	{
		this.localeGetter = localeGetter;
	}

	private Locale getLocale()
	{
		return localeGetter.get();
	}

	public String getViewID() {
		return VIEW_ID;
	}

	public void init()
	{

	}
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) 
	{

		Object[] args = new Object[]{serverConfigurationService.getString("ui.service", "Sakai")};
		UIMessage.make(tofill, "welcome1", "validate.welcome1.reset", args);

		ValidationAccount va = getValidationAccount(viewparams, tml);
		if (va == null)
		{
			//handled by getValidationAccount
			return;
		}
		else if (!va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET))
		{
			//this is not a password reset
			args = new Object[] {va.getValidationToken()};
			//no such validation of the required account status
			tml.addMessage(new TargettedMessage("msg.noSuchValidation", args, TargettedMessage.SEVERITY_ERROR));
			return;
		}
		else if (ValidationAccount.STATUS_CONFIRMED.equals(va.getStatus()))
		{
			args = new Object[] {va.getValidationToken()};
			tml.addMessage(new TargettedMessage("msg.alreadyValidated", args, TargettedMessage.SEVERITY_ERROR));
			addResetPassLink(tofill, va);
			return;
		}
		else if (ValidationAccount.STATUS_EXPIRED.equals(va.getStatus()))
		{
			/*
			 * If accountValidator.maxPasswordResetMinutes is configured, 
			 * we give them an approrpiate message, otherwise we give them the default
			 */
			TargettedMessage message = getExpirationMessage();
			if (message == null)
			{
				//give them the default
				args = new Object[]{ va.getValidationToken() };
				tml.addMessage(new TargettedMessage("msg.expiredValidation", args, TargettedMessage.SEVERITY_ERROR));
				addResetPassLink(tofill, va);
			}
			else
			{
				tml.addMessage(message);
			}
			return;
		}
		else if (sendLegacyLinksEnabled())
		{
			redirectToLegacyLink(tofill, va);
			return;
		}
		else
		{
			/*
			 * Password resets should go quickly. If it takes longer than accountValidator.maxPasswordResetMinutes, 
			 * it could be an intruder who stumbled on an old validation token.
			 */
			if (va.getAccountStatus() != null)
			{
				int minutes = serverConfigurationService.getInt(MAX_PASSWORD_RESET_MINUTES, MAX_PASSWORD_RESET_MINUTES_DEFAULT);

				//get the time limit and convert to millis
				long maxMillis = minutes * 60 * 1000;

				//the time when the validation token was sent to the email server
				long sentTime = va.getValidationSent().getTime();

				if (System.currentTimeMillis() - sentTime > maxMillis)
				{
					//it's been too long, so invalide the token and stop the user
					va.setStatus(ValidationAccount.STATUS_EXPIRED);

					//get a nice expiration meesage
					TargettedMessage expirationMessage = getExpirationMessage();
					if (expirationMessage == null)
					{
						//should never happen
						args = new Object[]{ va.getValidationToken() };
						tml.addMessage(new TargettedMessage("msg.expiredValidation", args, TargettedMessage.SEVERITY_ERROR));
						return;
					}
					tml.addMessage(expirationMessage);
					addResetPassLink(tofill, va);
					return;
				}
			}
		}

		User u = null;
		try
		{	
			u = userDirectoryService.getUser(EntityReference.getIdFromRef(va.getUserId()));
		}
		catch (UserNotDefinedException e)
		{

		}

		if (u == null)
		{
			log.error("user ID does not exist for ValidationAccount with tokenId: " + va.getValidationToken());
			tml.addMessage(new TargettedMessage("validate.userNotDefined", new Object[]{getUIService()}, TargettedMessage.SEVERITY_ERROR));
			return;
		}

		int minutes = serverConfigurationService.getInt(MAX_PASSWORD_RESET_MINUTES, MAX_PASSWORD_RESET_MINUTES_DEFAULT);
		UIMessage.make(tofill, "welcome2", "validate.expirationtime", new Object[]{getFormattedMinutes(minutes)});

		//the form
		UIForm detailsForm = UIForm.make(tofill, "setDetailsForm");

		UICommand.make(detailsForm, "addDetailsSub", UIMessage.make("submit.new.reset"), "accountValidationLocator.validateAccount");

		String otp = "accountValidationLocator." + va.getValidationToken();

		UIMessage.make(detailsForm, "username.new", "username.new.reset", args);
		UIOutput.make(detailsForm, "eid", u.getDisplayId());

		boolean passwordPolicyEnabled = (userDirectoryService.getPasswordPolicy() != null);
		String passwordPolicyEnabledJavaScript = "VALIDATOR.isPasswordPolicyEnabled = " + Boolean.toString(passwordPolicyEnabled) + ";";
		UIVerbatim.make(tofill, "passwordPolicyEnabled", passwordPolicyEnabledJavaScript);

		UIBranchContainer row1 = UIBranchContainer.make(detailsForm, "passrow1:");
		UIInput.make(row1, "password1", otp + ".password");

		UIBranchContainer row2 = UIBranchContainer.make(detailsForm, "passrow2:");
		UIInput.make(row2, "password2", otp + ".password2");
	}

	/**
	 * Converts some number of minutes into a presentable String'
	 * ie for English:
	 * 122 minutes	->	2 hours 2 minutes
	 * 121 minutes	->	2 hours 1 minute
	 * 120 minutes	->	2 hours
	 * 62 minutes	->	1 hour 2 minutes
	 * 61 minutes	->	1 hour 1 minute
	 * 60 minutes	->	1 hour
	 * 2 minutes	->	2 minutes
	 * 1 minutes	->	1 minute
	 * 0 minutes	->	0 minutes
	 * Works with other languages too.
	 * @param totalMinutes some number of minutes
	 * @return a presentable String representation of totalMinutes
	 */
	public String getFormattedMinutes(int totalMinutes)
	{
		// Create a joda time period (takes milliseconds)
		Period period = new Period(totalMinutes*60*1000);
		// format the period for the locale
		/* 
		 * Covers English, Danish, Dutch, French, German, Japanese, Portuguese, and Spanish. 
		 * To translate into others, see http://joda-time.sourceforge.net/apidocs/org/joda/time/format/PeriodFormat.html#wordBased(java.util.Locale)
		 * (ie. put the properties mentioned in http://joda-time.sourceforge.net/apidocs/src-html/org/joda/time/format/PeriodFormat.html#line.94 into the classpath resource bundle)
		 */
		PeriodFormatter periodFormatter = PeriodFormat.wordBased(getLocale());
		return periodFormatter.print(period);
	}

	/**
	 * When a user's validation token expires (by accountValidator.maxPasswordResetMinutes elapsing)
	 * this returns an appropriate TargettedMessage
	 * @return an appropriate TargettedMessage when a validation token has expired
	 */
	private TargettedMessage getExpirationMessage()
	{
		//get the time limit (iff possible)
		int minutes = serverConfigurationService.getInt(MAX_PASSWORD_RESET_MINUTES, MAX_PASSWORD_RESET_MINUTES_DEFAULT);

		//get a formatted string representation of the time limit, and create the return value
		String formattedTime = getFormattedMinutes(minutes);

		Object [] args = new Object[]{ formattedTime };
		return new TargettedMessage("msg.expiredValidationRealTime", args, TargettedMessage.SEVERITY_ERROR);
	}
}
