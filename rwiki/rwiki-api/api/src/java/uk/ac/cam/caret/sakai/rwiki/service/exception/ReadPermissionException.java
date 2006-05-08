/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.service.exception;

import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;

// FIXME: Service

public class ReadPermissionException extends PermissionException
{

	/**
	 * @see java.io.ObjectInputStream.resolveClass()
	 */
	private static final long serialVersionUID = -3744459824034953929L;

	private String user;

	private RWikiObject rwikiObject;

	private String realm;

	public ReadPermissionException(String user, RWikiObject rwikiObject)
	{
		super("User: " + user + " cannot read RWikiObject " + rwikiObject);
		this.user = user;
		this.rwikiObject = rwikiObject;
		this.realm = rwikiObject.getRealm();
	}

	public ReadPermissionException(String user, RWikiObject rwikiObject,
			Throwable cause)
	{
		super("User: " + user + " cannot read RWikiObject " + rwikiObject,
				cause);
		this.user = user;
		this.rwikiObject = rwikiObject;
		this.realm = rwikiObject.getRealm();
	}

	public ReadPermissionException(String user, String realm)
	{
		super("User: " + user + " is not permitted to read in realm " + realm);
		this.user = user;
		this.realm = realm;
		this.rwikiObject = null;
	}

	public RWikiObject getRWikiObject()
	{
		return rwikiObject;
	}

	public void setRWikiObject(RWikiObject rwikiObject)
	{
		this.rwikiObject = rwikiObject;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public String getRealm()
	{
		return realm;
	}

	public void setRealm(String realm)
	{
		this.realm = realm;
	}

}
