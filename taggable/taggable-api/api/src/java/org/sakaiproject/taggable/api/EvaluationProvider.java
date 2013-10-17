package org.sakaiproject.taggable.api;


/**
 * This interface should be implemented something that can provide the evaluations.
 * @author chrismaurer
 *
 */
public interface EvaluationProvider {

	/**
	 * Get an EvaluationContainer object for a given itemRef and tag ref.  (i.e. an assignment submission in a wizard page definition)
	 * @param itemRef String reference to an item (i.e. an assignment submission)
	 * @param tagRef String reference to a tag (i.e. a wizard page definition
	 * @param currentUserId The String userId of the currently logged in user
	 * @param itemRefOwner The String userId of the user that "owns" the item.  Most likely the owner of the assignment submission
	 * @return List of Evaluation objects
	 */
	public EvaluationContainer getEvaluationContainer(String itemRef, String tagRef, String currentUserId, String itemRefOwner);
	
}
