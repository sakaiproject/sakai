/**********************************************************************************
*
* $Header: /cvs/sakai2/gradebook/component-data/src/java/org/sakaiproject/tool/gradebook/Gradebook.java,v 1.4 2005/05/26 18:04:54 josh.media.berkeley.edu Exp $
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

package org.sakaiproject.tool.gradebook;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A Gradebook is the top-level object in the Sakai Gradebook tool.  Only one
 * Gradebook should be associated with any particular course (or site, as they
 * exist in Sakai 1.5) for any given academic term.  How courses and terms are
 * determined will likely depend on the particular Sakai installation.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class Gradebook {
    private Long id;
    private String uid;
    private int version;
    private String name;
    private GradeMapping selectedGradeMapping;
    private Set gradeMappings;
    private boolean assignmentsDisplayed;
    private boolean courseGradeDisplayed;
    private boolean allAssignmentsEntered;
    private boolean locked;

    /**
     * Default no-arg constructor needed for persistence
     */
    public Gradebook() {
    }

    /**
     * Creates a new gradebook with the given siteId and name
	 * @param name
	 */
	public Gradebook(String name) {
        this.name = name;
	}

    /**
     * Lists the grade mappings available to a gradebook.  If an institution
     * wishes to add or remove grade mappings, they will need to create a new
     * java class, add the class to the GradeMapping hibernate configuration,
     * and add the class here.
     *
     * This method will generally not be used, but can helpful when creating new
     * gradebooks.
     *
     * @return A Set of available grade mappings
     */
    public Set getAvailableGradeMappings() {
        Set set = new HashSet();
        set.add(new LetterGradeMapping());
        set.add(new LetterGradePlusMinusMapping());
        set.add(new PassNotPassMapping());
        return set;
    }

    public Class getDefaultGradeMapping() {
        return LetterGradePlusMinusMapping.class;
    }

    public GradeMapping getGradeMapping(Long id) {
        for(Iterator iter = getGradeMappings().iterator(); iter.hasNext();) {
            GradeMapping gm = (GradeMapping)iter.next();
            if(gm.getId().equals(id)) {
                return gm;
            }
        }
        return null;
    }

	/**
	 * @return Returns the allAssignmentsEntered.
	 */
	public boolean isAllAssignmentsEntered() {
		return allAssignmentsEntered;
	}
	/**
	 * @param allAssignmentsEntered The allAssignmentsEntered to set.
	 */
	public void setAllAssignmentsEntered(boolean allAssignmentsEntered) {
		this.allAssignmentsEntered = allAssignmentsEntered;
	}
	/**
	 * @return Returns the assignmentsDisplayed.
	 */
	public boolean isAssignmentsDisplayed() {
		return assignmentsDisplayed;
	}
	/**
	 * @param assignmentsDisplayed The assignmentsDisplayed to set.
	 */
	public void setAssignmentsDisplayed(boolean assignmentsDisplayed) {
		this.assignmentsDisplayed = assignmentsDisplayed;
	}
	/**
	 * @return Returns the gradeMappings.
	 */
	public Set getGradeMappings() {
		return gradeMappings;
	}
	/**
	 * @param gradeMappings The gradeMappings to set.
	 */
	public void setGradeMappings(Set gradeMappings) {
		this.gradeMappings = gradeMappings;
	}
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
	 * @return Returns the uid.
	 */
	public String getUid() {
		return uid;
	}
	/**
	 * @param uid The uid to set.
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}
	/**
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
	 * @return Returns the locked.
	 */
	public boolean isLocked() {
		return locked;
	}
	/**
	 * @param locked The locked to set.
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	/**
	 * @return Returns the selectedGradeMapping.
	 */
	public GradeMapping getSelectedGradeMapping() {
		return selectedGradeMapping;
	}
	/**
	 * @param selectedGradeMapping The selectedGradeMapping to set.
	 */
	public void setSelectedGradeMapping(GradeMapping selectedGradeMapping) {
		this.selectedGradeMapping = selectedGradeMapping;
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
     * @return Returns the courseGradeDisplayed.
     */
    public boolean isCourseGradeDisplayed() {
        return courseGradeDisplayed;
    }
    /**
     * @param courseGradeDisplayed The courseGradeDisplayed to set.
     */
    public void setCourseGradeDisplayed(boolean courseGradeDisplayed) {
        this.courseGradeDisplayed = courseGradeDisplayed;
    }

    public String toString() {
        return new ToStringBuilder(this).
        append("id", id).
        append("uid", uid).
        append("name", name).toString();
    }

    public boolean equals(Object other) {
        if (!(other instanceof Gradebook)) {
            return false;
        }
        Gradebook gb = (Gradebook)other;
        return new EqualsBuilder().
		    append(uid, gb.getUid()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().
            append(uid).toHashCode();
    }
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/gradebook/component-data/src/java/org/sakaiproject/tool/gradebook/Gradebook.java,v 1.4 2005/05/26 18:04:54 josh.media.berkeley.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
