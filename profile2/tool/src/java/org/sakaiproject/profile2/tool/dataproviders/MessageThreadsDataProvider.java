/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.tool.dataproviders;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileMessagingLogic;
import org.sakaiproject.profile2.model.MessageThread;
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
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileMessagingLogic")
	private ProfileMessagingLogic messagingLogic;
	
	public MessageThreadsDataProvider(String userUuid) {
		this.userUuid = userUuid;
		
		Injector.get().inject(this);
	}
	
	/**
	 * retrieve a sublist from the database, for paging
	 * TODO make it retrieve only the sublist from the DB, this just gets the lot and then returns a sublist.
	 * 
	 * @see org.apache.wicket.markup.repeater.data.IDataProvider#iterator(int, int)
	 */
	public Iterator<MessageThread> iterator(long first, long count){
		
		//deference for backwards compatibility
		//should really check bounds here 
		int f = (int) first;
		int c = (int) count;	
		
		List<MessageThread> slice = messagingLogic.getMessageThreads(userUuid).subList(f, f + c);
		Collections.sort(slice, Collections.reverseOrder());
		return slice.iterator();
		
	}

	/**
	 * returns total number of message thread headers
	 * 
	 * @see org.apache.wicket.markup.repeater.data.IDataProvider#size()
	 */
	public long size(){
		return messagingLogic.getMessageThreadsCount(userUuid);
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