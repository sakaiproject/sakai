/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2006 University of Cambridge
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

package org.sakaiproject.search.model.impl;

import java.util.Date;

import org.sakaiproject.search.model.SearchWriterLock;

/**
 * @author ieb
 */
public class SearchWriterLockImpl implements SearchWriterLock
{
	private String nodename = null;

	private Date version = null;

	private String id = null;

	private String lockkey = null;

	/**
	 * @return Returns the id.
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @param id
	 *        The id to set.
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * @return Returns the lastUpdate.
	 */
	public Date getVersion()
	{
		return version;
	}

	/**
	 * @param lastUpdate
	 *        The lastUpdate to set.
	 */
	public void setVersion(Date version)
	{
		this.version = version;
	}

	/**
	 * @return Returns the nodeName.
	 */
	public String getNodename()
	{
		return nodename;
	}

	/**
	 * @param nodeName
	 *        The nodeName to set.
	 */
	public void setNodename(String nodename)
	{
		this.nodename = nodename;
	}

	public void setLockkey(String lockkey)
	{
		this.lockkey = lockkey;

	}

	public String getLockkey()
	{
		return lockkey;
	}

}
