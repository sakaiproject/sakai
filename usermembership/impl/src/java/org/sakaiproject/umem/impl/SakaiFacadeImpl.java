package org.sakaiproject.umem.impl;

import java.util.TimeZone;

import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.umem.api.SakaiFacade;

public class SakaiFacadeImpl implements SakaiFacade{

	private UserTimeService userTimeService;
	public void setUserTimeService(UserTimeService userTimeService) {
		this.userTimeService = userTimeService;
	}
	@Override
	public TimeZone getLocalTimeZone() {
		return userTimeService.getLocalTimeZone();
	}

}
