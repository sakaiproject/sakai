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
package uk.ac.cam.caret.sakai.rwiki.message.model;

import java.util.Date;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Preference;

/**
 * @author ieb
 */
public class PreferenceImpl implements Preference
{
	private String id;

	private String userid;

	private Date lastseen;

	private String preference;

	private String prefcontext;

	private String preftype;

	/**
	 * {@inheritDoc}
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getLastseen()
	{
		return lastseen;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLastseen(Date lastseen)
	{
		this.lastseen = lastseen;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPreference()
	{
		return preference;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPreference(String preference)
	{
		this.preference = preference;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserid()
	{
		return userid;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setUserid(String userid)
	{
		this.userid = userid;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPrefcontext()
	{
		return prefcontext;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPrefcontext(String prefcontext)
	{
		this.prefcontext = prefcontext;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPreftype()
	{
		return preftype;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPreftype(String preftype)
	{
		this.preftype = preftype;
	}

}
