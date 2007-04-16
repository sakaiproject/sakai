package org.sakaiproject.scorm.client;

import java.util.TimeZone;

import wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.util.time.Duration;

public class Index extends WebPage
{
	/**
	 * Constructor.
	 */
	public Index()
	{
		// add the clock component
		Clock clock = new Clock("clock", TimeZone.getTimeZone("America/Los_Angeles"));
		add(clock);
		clock.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)));		
	
		String userName = ((ScormWicketApplication)getApplication()).getScormClientService().getUserName();
		
		add(new Label("user", userName));
	}
}
