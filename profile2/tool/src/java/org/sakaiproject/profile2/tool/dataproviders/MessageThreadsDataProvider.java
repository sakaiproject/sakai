package org.sakaiproject.profile2.tool.dataproviders;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.model.MessageThread;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.tool.models.DetachableMessageThreadModel;

/**
 * Implementation of IDataProvider that retrieves the MessageThreads for a user, containing the most recent message in each
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */

public class MessageThreadsDataProvider implements IDataProvider<MessageThread> {
    
	private static final long serialVersionUID = 1L;
	private final String userUuid;
	
	public MessageThreadsDataProvider(String userUuid) {
		this.userUuid = userUuid;
	}
	
	protected ProfileLogic getProfileLogic(){
		return Locator.getProfileLogic();
	}

	/**
	 * retrieves threads from database, gets the sublist and returns an iterator for that sublist
	 * 
	 * @see org.apache.wicket.markup.repeater.data.IDataProvider#iterator(int, int)
	 */
	public Iterator<MessageThread> iterator(int first, int count){
		
		try {
			List<MessageThread> slice = getProfileLogic().getMessageThreads(userUuid).subList(first, first + count);
			return slice.iterator();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Collections.EMPTY_LIST.iterator();
		}
	}

	/**
	 * returns total number of message thread headers
	 * 
	 * @see org.apache.wicket.markup.repeater.data.IDataProvider#size()
	 */
	public int size(){
		return getProfileLogic().getMessageThreadsCount(userUuid);
	}

	/**
	 * wraps retrieved message pojo with a wicket model
	 * 
	 * @see org.apache.wicket.markup.repeater.data.IDataProvider#model(java.lang.Object)
	 */
	public IModel<MessageThread> model(MessageThread object){
		return new DetachableMessageThreadModel(object);
	}

	/**
	 * @see org.apache.wicket.model.IDetachable#detach()
	 */
	public void detach(){
	}
      
	

  }