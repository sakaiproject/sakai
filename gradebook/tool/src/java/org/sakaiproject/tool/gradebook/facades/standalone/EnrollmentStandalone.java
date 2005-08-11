/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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
package org.sakaiproject.tool.gradebook.facades.standalone;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.facades.Enrollment;
import org.sakaiproject.tool.gradebook.facades.User;

public class EnrollmentStandalone implements Enrollment {
	private User user;
    private Long id;
    private Gradebook gradebook;

    public EnrollmentStandalone() {
    }

    public EnrollmentStandalone(User user, Gradebook gradebook) {
		this.user = user;
    	this.gradebook = gradebook;
    }

	public User getUser() {
		return this.user;
	}
	public void setUser(User user) {
		this.user = user;
	}

    public Gradebook getGradebook() {
    	return gradebook;
    }
    public void setGradebook(Gradebook gradebook) {
    	this.gradebook = gradebook;
    }

    public boolean equals(Object o) {
        if (!(o instanceof EnrollmentStandalone)) {
            return false;
        }
        EnrollmentStandalone other = (EnrollmentStandalone)o;

        return new EqualsBuilder()
            .append(getUser(), other.getUser()).
            append(gradebook, other.getGradebook()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().
		    append(getUser()).
			append(gradebook).
            toHashCode();
    }

    public String toString() {
        return new ToStringBuilder(this).
            append(getUser()).
            append(gradebook).
            toString();
    }
}



