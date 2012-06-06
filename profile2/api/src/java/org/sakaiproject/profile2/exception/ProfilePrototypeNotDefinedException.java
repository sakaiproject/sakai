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
package org.sakaiproject.profile2.exception;

public class ProfilePrototypeNotDefinedException extends RuntimeException {
		
	private static final long serialVersionUID = 1L;
	private final String message;
	
	public ProfilePrototypeNotDefinedException(final String message){
		this.message = message;
	}

	public String toString() {
		return ProfilePrototypeNotDefinedException.class + ": " +  message;
	}
}


