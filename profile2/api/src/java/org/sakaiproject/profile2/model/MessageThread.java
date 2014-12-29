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
package org.sakaiproject.profile2.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.commons.lang.builder.ToStringBuilder;
/**
 * MessageThread.java
 * 
 * An object to represent info about a message thread
 * This is a persistent Hibernate model
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Data
@NoArgsConstructor
public class MessageThread implements Serializable, Comparable<Object> {

	private static final long serialVersionUID = 1L;

	/**
	 * thread id (a uuid)
	 */
	private String id;
	
	/**
	 * subject of the thread
	 */
	private String subject;
	
	/**
	 * the most recent message in this thread - not persisted.
	 */
	private Message mostRecentMessage;
	
	/**
	 * Natural sort by order
	 */
	public int compareTo(Object o) {
		Date thisDate = getMostRecentMessage().getDatePosted();
		Date otherDate = ((MessageThread)o).getMostRecentMessage().getDatePosted();
        int lastCmp = thisDate.compareTo(otherDate);
        return (lastCmp != 0 ? lastCmp : thisDate.compareTo(otherDate));
	}
	
	
	
}
