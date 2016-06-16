package org.sakaiproject.gradebookng.tool.pages;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;

import lombok.extern.slf4j.Slf4j;

/**
 * Page displayed when an internal error occurred.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class ErrorPage extends BasePage {

	private static final long serialVersionUID = 1L;

	public ErrorPage(final Exception e) {
		
		final String stacktrace = ExceptionUtils.getStackTrace(e);
		
		//log the stacktrace
		log.error(stacktrace);
		
		// generate an error code so we can log the exception with it without giving the user the stacktrace
		// note that wicket will already have logged the stacktrace so we aren't going to bother logging it again
		final String code = RandomStringUtils.randomAlphanumeric(10);
		log.error("User supplied error code for the above stacktrace: " + code);

		final Label error = new Label("error", new StringResourceModel("errorpage.text", null, new Object[] { code }));
		error.setEscapeModelStrings(false);
		add(error);

		// show the stacktrace. This should be configurable at some point
		add(new Label("stacktrace", stacktrace));

	}
}
