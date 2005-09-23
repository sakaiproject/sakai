/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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

package org.sakaiproject.component.section;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.sakaiproject.api.section.coursemanagement.User;

/**
 * A detachable User for persistent storage.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class UserImpl extends AbstractPersistentObject implements User, Serializable {

	private static final long serialVersionUID = 1L;
	
	protected String userUid;
	protected String sortName;
	protected String displayId;
	protected String displayName;
	
    protected long id;
    protected int version;

    /**
	 * No-arg constructor needed for hibernate
	 */
	public UserImpl() {		
	}
	
	public UserImpl(String displayName, String displayId, String sortName, String userUid) {
		this.displayName = displayName;
		this.displayId = displayId;
		this.sortName = sortName;
		this.userUid = userUid;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	public String getDisplayId() {
		return displayId;
	}
	public String getSortName() {
		return sortName;
	}
	public String getUserUid() {
		return userUid;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(o instanceof UserImpl) {
			UserImpl other = (UserImpl)o;
			return new EqualsBuilder()
				.append(userUid, other.userUid)
				.isEquals();
		}
		return false;
	}

	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(userUid)
			.toHashCode();
	}
	
	public String toString() {
		return new ToStringBuilder(this).append(displayName)
		.append(userUid).append(id).toString();
	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
