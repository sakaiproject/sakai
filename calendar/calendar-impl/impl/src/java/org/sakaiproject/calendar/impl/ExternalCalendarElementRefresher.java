package org.sakaiproject.calendar.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.calendar.api.ExternalSubscription;

/**
 * This attempts to refresh an external calendar.
 * @author buckett
 *
 */
public class ExternalCalendarElementRefresher implements ElementRefresher {

	private static final Log LOG = LogFactory.getLog(ExternalCalendarElementRefresher.class);

	private BaseExternalCalendarSubscriptionService service;

	public void setExeternalCalendarSubscriptionService(BaseExternalCalendarSubscriptionService service) {
		this.service = service;
	}

	public Object updateElement(Object key, Object value) {
		ExternalSubscription newSubscription = null;
		if (value instanceof BaseExternalSubscription) {
			BaseExternalSubscription subscription = (BaseExternalSubscription) value;
			String url = subscription.getSubscriptionUrl();
			String context = subscription.getContext();
			// Load the calendar.
			newSubscription = service.loadCalendarSubscriptionFromUrl(url, context);
		} else {
			LOG.warn("updateElement() called on on cache that doesn't contain BaseExternalSubscriptions.");
		}

		return newSubscription;
	}

}
