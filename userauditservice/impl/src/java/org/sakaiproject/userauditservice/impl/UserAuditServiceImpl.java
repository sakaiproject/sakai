/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.userauditservice.impl;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;

public class UserAuditServiceImpl implements UserAuditService {

	private List<UserAuditRegistration> registeredItems = new ArrayList<UserAuditRegistration>();
	private List<String> keys = new ArrayList<String>();
	
	/**
	 * {@inheritDoc}
	 */
	public void register(UserAuditRegistration uar) {
		getRegisteredItems().add(uar);
		getKeys().add(uar.getDatabaseSourceKey());
	}
	
	/**
	 * Setter
	 * @param registeredItems
	 */
	public void setRegisteredItems(List<UserAuditRegistration> registeredItems) {
		this.registeredItems = registeredItems;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<UserAuditRegistration> getRegisteredItems() {
		return registeredItems;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<String> getKeys() {
		return keys;
	}
	
	/**
	 * Setter
	 * @param keys
	 */
	public void setKeys(List<String> keys) {
		this.keys = keys;
	}
	
	/** Dependency: SqlService */
	protected SqlService m_sqlService = null;
	
	/**
	 * Dependency: SqlService.
	 * 
	 * @param service
	 *        The SqlService.
	 */
	public void setSqlService(SqlService service)
	{
		m_sqlService = service;
	}
	
	/** Configuration: to run the ddl on init or not. */
	protected boolean m_autoDdl = false;
	
	/**
	 * Configuration: to run the ddl on init or not.
	 * 
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
	    m_autoDdl = Boolean.valueOf(value).booleanValue();
	}
	
	public void init()
	{
		// if we are auto-creating our schema, check and create
		if (m_autoDdl)
		{
			m_sqlService.ddl(this.getClass().getClassLoader(), "user_audits");
		}
	}
}
