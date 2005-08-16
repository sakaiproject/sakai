/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.section;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.sakaiproject.api.section.coursemanagement.LearningContext;

/*
 * A base class for hibernate-managed learning context objects.
 */
public class LearningContextImpl extends AbstractPersistentObject implements
		LearningContext {

	public long getId() {
		// TODO Auto-generated method stub
		return super.getId();
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return super.getTitle();
	}

	public String getUuid() {
		// TODO Auto-generated method stub
		return super.getUuid();
	}

	public int getVersion() {
		// TODO Auto-generated method stub
		return super.getVersion();
	}

	public void setId(long id) {
		// TODO Auto-generated method stub
		super.setId(id);
	}

	public void setTitle(String title) {
		// TODO Auto-generated method stub
		super.setTitle(title);
	}

	public void setUuid(String uuid) {
		// TODO Auto-generated method stub
		super.setUuid(uuid);
	}

	public void setVersion(int version) {
		// TODO Auto-generated method stub
		super.setVersion(version);
	}
	
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(o instanceof LearningContextImpl) {
			LearningContextImpl other = (LearningContextImpl)o;
			return new EqualsBuilder()
				.append(uuid, other.uuid)
				.isEquals();
		}
		return false;
	}

	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(uuid)
			.toHashCode();
	}


}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
