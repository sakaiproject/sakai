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
package org.sakaiproject.profile2.tool.models;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileMessagingLogic;
import org.sakaiproject.profile2.model.MessageThread;

/**
 * Detachable model for an instance of MessageThread
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
public class DetachableMessageThreadModel extends LoadableDetachableModel<MessageThread>{

	private static final long serialVersionUID = 1L;
	private final String id;

	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileMessagingLogic")
	private ProfileMessagingLogic messagingLogic;
	  
	/**
	 * @param m
	 */
	public DetachableMessageThreadModel(MessageThread m){
		this.id = m.getId();
		Injector.get().inject(this);
	}
	
	/**
	 * @param id
	 */
	public DetachableMessageThreadModel(String id){
		this.id = id;
		Injector.get().inject(this);
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return id.hashCode();
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
		else if (obj instanceof DetachableMessageThreadModel) {
			DetachableMessageThreadModel other = (DetachableMessageThreadModel)obj;
			return other.id == id;
		}
		return false;
	}
	
	/**
	 * @see org.apache.wicket.model.LoadableDetachableModel#load()
	 */
	protected MessageThread load(){
		return messagingLogic.getMessageThread(id);
	}
}