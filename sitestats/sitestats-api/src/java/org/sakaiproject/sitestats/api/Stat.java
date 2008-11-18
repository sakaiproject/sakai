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
package org.sakaiproject.sitestats.api;

import java.util.Date;

/**
 * Represents common fields of records from the SST_EVENTS and SST_RESOURCES tables.
 * @author Nuno Fernandes
 */
public interface Stat {
	/** Get the db record id. */ 
	public long getId();
	/** Set the db record id. */	
	public void setId(long id);
	
	/** Get the user Id this record refers to. */ 
	public String getUserId();
	/** Set the user Id this record refers to. */
	public void setUserId(String userId);
	
	/** Get the context (site Id) this record refers to. */
	public String getSiteId();
	/** Set the context (site Id) this record refers to. */
	public void setSiteId(String siteId);
	
	/** Get the total value. */
	public long getCount();
	/** Set the total value. */
	public void setCount(long count);
	
	/** Get the date this record refers to. Only year,month and day are important. */
	public Date getDate();
	/** Set the date this record refers to. Only year,month and day are important. */
	public void setDate(Date date);
}
