package org.sakaiproject.gradebookng.tool.pages;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;


/**
 * Page displayed when an internal error occurred.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@CommonsLog
public class ErrorPage extends BasePage {
	
	private static final long serialVersionUID = 1L;

	public ErrorPage(Exception e) {
		
		//generate an error code so we can log the exception with it without giving the user the stacktrace
		//note that wicket will already have logged the stacktrace so we aren't going to bother logging it again
		String code = RandomStringUtils.randomAlphanumeric(10);
		
		log.error("User supplied error code for the above stacktrace: " + code);
		
		Label error = new Label("error", new StringResourceModel("errorpage.text", null, new Object[]{ code }));
		error.setEscapeModelStrings(false);
		add(error);
		
		//show the stacktrace. This should be configurable at some point
		String stacktrace = ExceptionUtils.getStackTrace(e);
		add(new Label("stacktrace", stacktrace));


	}
}
