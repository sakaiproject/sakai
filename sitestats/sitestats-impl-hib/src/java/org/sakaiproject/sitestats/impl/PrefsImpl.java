/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
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
