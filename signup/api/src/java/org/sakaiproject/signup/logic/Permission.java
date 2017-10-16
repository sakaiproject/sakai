/**
 * Copyright (c) 2007-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
