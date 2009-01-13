/**********************************************************************************
 * $URL$
 * $Id$
***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Yale University
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *   
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/
package org.sakaiproject.signup.logic;

/**
 * <P>
 * This class retains permission information for attend, update and delete
 * operations
 * </P>
 */
public class Permission {

	private boolean attend;

	private boolean update;

	private boolean delete;

	/**
	 * Constructor
	 * 
	 * @param attend
	 *            boolean value
	 * @param update
	 *            boolean value
	 * @param delete
	 *            boolean value
	 */
	public Permission(boolean attend, boolean update, boolean delete) {
		this.attend = attend;
		this.update = update;
		this.delete = delete;
	}

	/**
	 * check if the user has attend permission
	 * 
	 * @return true if user has the attend permission
	 */
	public boolean isAttend() {
		return attend;
	}

	/**
	 * check if the user has delete permission
	 * 
	 * @return true if the user has delete permission
	 */
	public boolean isDelete() {
		return delete;
	}

	/**
	 * check if the user has update permission
	 * 
	 * @return true if the user has update permission
	 */
	public boolean isUpdate() {
		return update;
	}

	/**
	 * set attendee permission
	 * 
	 * @param attend
	 *            boolean value
	 */
	public void setAttend(boolean attend) {
		this.attend = attend;
	}

	/**
	 * set delete permission
	 * 
	 * @param delete
	 *            boolean value
	 */
	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	/**
	 * set update permission
	 * 
	 * @param update
	 *            boolean value
	 */
	public void setUpdate(boolean update) {
		this.update = update;
	}

}
