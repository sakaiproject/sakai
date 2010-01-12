package org.sakaiproject.profile2.tool.models;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.model.LoadableDetachableModel;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.model.MessageThread;
import org.sakaiproject.profile2.tool.Locator;

/**
 * Detachable model for an instance of MessageThread
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
public class DetachableMessageThreadModel extends LoadableDetachableModel<MessageThread>{

	private static final long serialVersionUID = 1L;
	private final long threadId;

	protected ProfileLogic getProfileLogic(){
		return Locator.getProfileLogic();
	}
	  
	/**
	 * @param c
	 */
	public DetachableMessageThreadModel(MessageThread m){
		this.threadId = m.getId();
	}
	
	/**
	 * @param id
	 */
	public DetachableMessageThreadModel(long threadId){
		this.threadId = threadId;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new Long(threadId).hashCode();
	}
	
	/**
	 * used for dataview with ReuseIfModelsEqualStrategy item reuse strategy
	 * 
	 * @see org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(final Object obj){
		if (obj == this){
			return true;
		}
		else if (obj == null){
			return false;
		}
		else if (obj instanceof DetachableMessageModel) {
			DetachableMessageThreadModel other = (DetachableMessageThreadModel)obj;
			return other.threadId == threadId;
		}
		return false;
	}
	
	/**
	 * @see org.apache.wicket.model.LoadableDetachableModel#load()
	 */
	protected MessageThread load(){
		return getProfileLogic().getMessageThread(threadId);
	}
}