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
/**
 * Message.java
 * 
 * An object to represent a message posted to someone
 * This is a persistent Hibernate model
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */

@Data
@NoArgsConstructor
public class Message implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * message Id (a uuid)
	 */
	private String id;
		
	/**
	 * uuid from
	 */
	private String from;
	
	/**
	 * body of the message
	 */
	private String message;
	
	/**
	 * what thread ID this message is associated with
	 */
	private String thread;
	
	/**
	 * date this message was posted
	 */
	private Date datePosted;
	
}
