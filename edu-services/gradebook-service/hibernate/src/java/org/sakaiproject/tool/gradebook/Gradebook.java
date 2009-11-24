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
import java.util.*;
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
public class Gradebook implements Serializable {
    private Long id;
    private String uid;
    private int version;
    private String name;
    private GradeMapping selectedGradeMapping;
    private Set<GradeMapping> gradeMappings;
    private boolean assignmentsDisplayed;
    private boolean courseGradeDisplayed;
    private boolean allAssignmentsEntered;
    private boolean locked;
    private int grade_type;
    private int category_type;
    private Boolean equalWeightCategories;
    private Boolean scaledExtraCredit;
    private Boolean showMean;
    private Boolean showMedian;
    private Boolean showMode;
    private Boolean showRank;
    private Boolean showItemStatistics;

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
/*
    public Set getAvailableGradeMappings() {
        Set set = new HashSet();
        set.add(new LetterGradeMapping());
        set.add(new LetterGradePlusMinusMapping());
        set.add(new PassNotPassMapping());
        return set;
    }
*/

/*
    public Class getDefaultGradeMapping() {
        return LetterGradePlusMinusMapping.class;
    }
*/

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
	public Set<GradeMapping> getGradeMappings() {
		return gradeMappings;
	}
	/**
	 * @param gradeMappings The gradeMappings to set.
	 */
	public void setGradeMappings(Set<GradeMapping> gradeMappings) {
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

		public int getCategory_type()
		{
			return category_type;
		}

		public void setCategory_type(int category_type)
		{
			this.category_type = category_type;
		}

		public int getGrade_type()
		{
			return grade_type;
		}

		public void setGrade_type(int grade_type)
		{
			this.grade_type = grade_type;
		}

		public Boolean isEqualWeightCategories() {
			return equalWeightCategories;
		}

		public void setEqualWeightCategories(Boolean equalWeightCategories) {
			this.equalWeightCategories = equalWeightCategories;
		}

		public Boolean isScaledExtraCredit() {
			return scaledExtraCredit;
		}

		public void setScaledExtraCredit(Boolean scaledExtraCredit) {
			this.scaledExtraCredit = scaledExtraCredit;
		}

		public Boolean getShowMean() {
			return showMean;
		}

		public void setShowMean(Boolean showMean) {
			this.showMean = showMean;
		}

		public Boolean getShowMedian() {
			return showMedian;
		}

		public void setShowMedian(Boolean showMedian) {
			this.showMedian = showMedian;
		}

		public Boolean getShowMode() {
			return showMode;
		}

		public void setShowMode(Boolean showMode) {
			this.showMode = showMode;
		}

		public Boolean getShowRank() {
			return showRank;
		}

		public void setShowRank(Boolean showRank) {
			this.showRank = showRank;
		}

		public Boolean getShowItemStatistics() {
			return showItemStatistics;
		}

		public void setShowItemStatistics(Boolean showItemStatistics) {
			this.showItemStatistics = showItemStatistics;
		}

		public Boolean getScaledExtraCredit() {
			return scaledExtraCredit;
		}
}



