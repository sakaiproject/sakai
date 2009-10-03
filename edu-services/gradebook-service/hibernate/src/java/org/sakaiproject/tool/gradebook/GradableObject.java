/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A GradableObject is a component of a Gradebook for which students can be
 * assigned a GradeRecord.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public abstract class GradableObject implements Serializable {
    protected static final Log log = LogFactory.getLog(GradableObject.class);

    protected Long id;
    protected int version;
    protected Gradebook gradebook;
    protected String name;
    protected Double mean;	// not persisted; not used in all contexts (in Overview & Assignment Grading,
    	                    // not in Roster or Student View)

    protected boolean removed;  // We had trouble with foreign key constraints in the UCB pilot when
                                // instructors "emptied" all scores for an assignment and then tried to
                                // delete the assignment.  Instead, we should hide the "removed" assignments
                                // from the app by filtering the removed assignments in the hibernate queries


    /**
     * @return Whether this gradable object is a course grade
     */
    public abstract boolean isCourseGrade();
    
    /**
     * @return Whether this gradable object is an assignment
     */
    public abstract boolean isAssignment();
    
    /**
     * @return Whether this gradable object is a category
     */
    public abstract boolean getIsCategory();

	/**
	 * @return Returns the id.
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(Long id) {
		this.id = id;
	}
    /**
     * @return Returns the gradebook.
     */
    public Gradebook getGradebook() {
        return gradebook;
    }
    /**
     * @param gradebook The gradebook to set.
     */
    public void setGradebook(Gradebook gradebook) {
        this.gradebook = gradebook;
    }

    /**
     * @return Returns the mean.
     */
    public Double getMean() {
        return mean;
    }

    /**
	 * @return Returns the mean while protecting against displaying NaN.
	 */
	public Double getFormattedMean() {
        if(mean == null || mean.equals(new Double(Double.NaN))) {
        	return null;
        } else {
            return new Double(mean.doubleValue() / 100.0);
        }
	}

    /**
	 * @param mean The mean to set.
	 */
	public void setMean(Double mean) {
		this.mean = mean;
	}
	
	/**
	 * This should really only be a field in Assignment objects, since
	 * the string describing CourseGrade needs to allow for localization.
	 * Unfortunately, such we keep CourseGrade and Assignment objects in
	 * the same table, and since we want Assignment names to be enforced
	 * as non-nullable, we're stuck with a bogus CourseGrade "name" field
	 * for now. The UI will have to be smart enough to disregard it.
	 * 
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return Returns the version.
	 */
	public int getVersion() {
		return version;
	}
	/**
	 * @param version The version to set.
	 */
	public void setVersion(int version) {
		this.version = version;
	}
    /**
     * @return Returns the removed.
     */
    public boolean isRemoved() {
        return removed;
    }
    /**
     * @param removed The removed to set.
     */
    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public String toString() {
        return new ToStringBuilder(this).
        append("id", id).
        append("name", name).toString();

    }

    public boolean equals(Object other) {
        if (!(other instanceof GradableObject)) {
        	return false;
        }
        GradableObject go = (GradableObject)other;
        return new EqualsBuilder()
            .append(gradebook, go.getGradebook())
            .append(id, go.getId())
            .append(name, go.getName()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().
          append(gradebook).
          append(id).
          append(name).
          toHashCode();
	}
}



