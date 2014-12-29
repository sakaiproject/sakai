/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl;

import java.io.Serializable;

import org.sakaiproject.sitestats.api.Prefs;


/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class PrefsImpl implements Prefs, Serializable {
	private static final long	serialVersionUID	= 1L;
	private long				id;
	private String				siteId;
	private String				prefs;

	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof PrefsImpl)) return false;
		PrefsImpl other = (PrefsImpl) o;
		return id == other.getId()
				&& siteId.equals(other.getSiteId())
				&& prefs.equals(other.getPrefs());
	}

	public int hashCode() {
		if(siteId == null || prefs == null) return Integer.MIN_VALUE;
		String hashStr = this.getClass().getName() + ":"
				+ this.getId()
				+ this.getSiteId().hashCode()
				+ this.getPrefs().hashCode();
		return hashStr.hashCode();
	}
	
	public String toString(){
		return siteId + " : " + prefs;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Prefs#getId()
	 */
	public long getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Prefs#setId(long)
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Prefs#getSiteId()
	 */
	public String getSiteId() {
		return this.siteId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Prefs#setSiteId(java.lang.String)
	 */
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Prefs#getPrefs()
	 */
	public String getPrefs() {
		return prefs;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Prefs#setPrefs(java.lang.String)
	 */
	public void setPrefs(String prefs) {
		this.prefs = prefs;
	}
}
