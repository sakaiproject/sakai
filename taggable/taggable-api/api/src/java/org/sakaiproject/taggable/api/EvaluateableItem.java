package org.sakaiproject.taggable.api;

/**
 * Interface that can be implemented by classes wanting to participate in evaluations
 * @author chrismaurer
 *
 */
public interface EvaluateableItem {

	/**
	 * Get the reference to the object
	 * @return
	 */
	public String getReference();
}
