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

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.tool.models.DetachablePersonModel;

/**
 * RequestedFriendsDataProvider.java
 * Steve Swinsburg
 * s.swinsburg@lancaster.ac.uk
 * January 2009
 * 
 * This implementation of Wicket's IDataProvider gets a list of friend requests incoming to userId
 * 
 */
@Slf4j
public class RequestedFriendsDataProvider implements IDataProvider<Person>, Serializable {
	
	private static final long serialVersionUID = 1L;
	private String userUuid; 
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileConnectionsLogic")
	private ProfileConnectionsLogic connectionsLogic;
	
	public RequestedFriendsDataProvider(final String userUuid) {
		this.userUuid = userUuid;
		
		Injector.get().inject(this);
	}
	
	public Iterator<Person> iterator(long first, long count) {
		
		//deference for backwards compatibility
		//should really check bounds here 
		int f = (int) first;
		int c = (int) count;	
		
		try {
			List<Person> requests = connectionsLogic.getConnectionRequestsForUser(userUuid);
			Collections.sort(requests);
			List<Person> slice = requests.subList(f, f + c);
			return slice.iterator();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
			return Collections.EMPTY_LIST.iterator();
		}
	}

    public long size() {
    	return connectionsLogic.getConnectionRequestsForUserCount(userUuid);
	}

    public IModel<Person> model(Person object) {
        return new DetachablePersonModel(object);
    }
    
    public void detach() {}
    
}
