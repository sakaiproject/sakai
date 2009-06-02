package uk.ac.lancs.e_science.profile2.tool.components;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;


/**
 * Filter for accepting only feedback messages with this error level.
 *
 * You can use this code under Apache 2.0 license, as long as you retain the copyright messages.
 *
 * Tested with Wicket 1.3.4
 * @author Daan, StuQ.nl
 */
public class ErrorLevelsFeedbackMessageFilter implements IFeedbackMessageFilter
{

	/** The minimum error level */
	private int[] filteredErrorLevels;

	/**
	 * Constructor
	 *
	 * @param filteredErrorLevels The FeedbackMessages that have thes error levels will
     *                            not be shown.
	 */
	public ErrorLevelsFeedbackMessageFilter(int[] filteredErrorLevels){
		this.filteredErrorLevels = filteredErrorLevels;
	}

	/**
     * Method accept, only accept FeedbackMessages that are not in the list of error levels to filter.
     *
     * @param message of type FeedbackMessage
     * @return boolean
     */
    public boolean accept(FeedbackMessage message){
        for (int errorLevel : filteredErrorLevels) {
            if (message.getLevel() == errorLevel) {
                return false;
            }
        }

        return true;
	}
}