package org.sakaiproject.courier.api;

/**
 * Interface that will be used in additions to Delivery to indicate that a delivery will eventually expire.
 * @author Chris Maurer <chmaurer@iu.edu>
 *
 */
public interface Expirable {

	/**
	 * Get the date that the object was created
	 * @return
	 */
	long getCreated();
	
	/**
	 * Get the Time-to-Live for the object (in seconds).
	 * @return
	 */
	int getTtl();
	
}
