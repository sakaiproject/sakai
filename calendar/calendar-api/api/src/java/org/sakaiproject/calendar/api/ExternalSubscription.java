package org.sakaiproject.calendar.api;

public interface ExternalSubscription
{

	/** Get subscription name */
	public String getSubscriptionName();

	/** Set subscription name */
	public void setSubscriptionName(String subscriptionName);

	/** Get subscription URL */
	public String getSubscriptionUrl();

	/** Set subscription URL */
	public void setSubscriptionUrl(String subscriptionUrl);

	/** Get context (site id) of external subscription */
	public String getContext();

	/** Set context (site id) of external subscription */
	public void setContext(String context);

	/** Get Reference of external subscription */
	public String getReference();

	/** Get Calendar object of external subscription */
	public Calendar getCalendar();

	/** Set Calendar object for external subscription */
	public void setCalendar(Calendar calendar);

	/** Check if external subscription is an institutional subscription */
	public boolean isInstitutional();

	/** Mark this external subscription as an institutional subscription */
	public void setInstitutional(boolean isInstitutional);

}
