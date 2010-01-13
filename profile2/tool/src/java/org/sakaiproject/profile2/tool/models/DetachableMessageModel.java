/**
 * Copyright (c) 2008-2010 The Sakai Foundation
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

import org.apache.wicket.model.LoadableDetachableModel;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.model.Message;
import org.sakaiproject.profile2.tool.Locator;

/**
 * Detachable model for an instance of Message
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
public class DetachableMessageModel extends LoadableDetachableModel<Message>{

	private static final long serialVersionUID = 1L;
	private final long id;

	protected ProfileLogic getProfileLogic(){
		return Locator.getProfileLogic();
	}
	  
	/**
	 * @param c
	 */
	public DetachableMessageModel(Message m){
		this.id = m.getId();
	}
	
	/**
	 * @param id
	 */
	public DetachableMessageModel(long id){
		if (id == 0) {
			throw new IllegalArgumentException();
		}
		this.id = id;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return Long.valueOf(id).hashCode();
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
			DetachableMessageModel other = (DetachableMessageModel)obj;
			return other.id == id;
		}
		return false;
	}
	
	/**
	 * @see org.apache.wicket.model.LoadableDetachableModel#load()
	 */
	protected Message load(){
		// loads message from the database
		return getProfileLogic().getMessage(id);
	}
}