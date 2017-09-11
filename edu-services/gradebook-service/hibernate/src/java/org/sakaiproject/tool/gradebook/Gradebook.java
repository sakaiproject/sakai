/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.tool.gradebook;

import java.io.Serializable;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A Gradebook is the top-level object in the Sakai Gradebook tool.  Only one
 * Gradebook should be associated with any particular course (or site, as they
 * exist in Sakai 1.5) for any given academic term.  How courses and terms are
 * determined will likely depend on the particular Sakai installation.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class Gradebook implements Serializable {
    
	private static final long serialVersionUID = 1L;
	
	private Long id;
    private String uid;
    private int version;
    private String name;
    private GradeMapping selectedGradeMapping;
    private Set<GradeMapping> gradeMappings;
    private boolean assignmentsDisplayed;
    private boolean courseGradeDisplayed;
    private boolean courseLetterGradeDisplayed;
    private boolean coursePointsDisplayed;
    private boolean totalPointsDisplayed;
    private boolean courseAverageDisplayed;
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
    private Boolean showStatisticsChart;

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
	 * Is the course grade to be shown at all?
	 * @return boolean
	 */
    public boolean isCourseGradeDisplayed() {
        return courseGradeDisplayed;
    }
   
    public void setCourseGradeDisplayed(boolean courseGradeDisplayed) {
        this.courseGradeDisplayed = courseGradeDisplayed;
    }

    public boolean isTotalPointsDisplayed() {
      return totalPointsDisplayed;
    }

    public void setTotalPointsDisplayed(boolean totalPointsDisplayed) {
      this.totalPointsDisplayed = totalPointsDisplayed;
    }

    /**
	 * If the course grade is displayed, should the total points be displayed?
	 * @return true/false if total points should be displayed
	 */
    public boolean isCoursePointsDisplayed() {
		return coursePointsDisplayed;
	}

	public void setCoursePointsDisplayed(boolean coursePointsDisplayed) {
		this.coursePointsDisplayed = coursePointsDisplayed;
	}

	/**
	 * If the course grade is displayed, should the percentage be displayed?
	 * @return true/false if percentage should be displayed
	 */
	public boolean isCourseAverageDisplayed() {
      return courseAverageDisplayed;
    }

    public void setCourseAverageDisplayed(boolean courseAverageDisplayed) {
      this.courseAverageDisplayed = courseAverageDisplayed;
    }

    @Override
	public String toString() {
        return new ToStringBuilder(this).
        append("id", id).
        append("uid", uid).
        append("name", name).toString();
    }

    @Override
	public boolean equals(Object other) {
        if (!(other instanceof Gradebook)) {
            return false;
        }
        Gradebook gb = (Gradebook)other;
        return new EqualsBuilder().
		    append(uid, gb.getUid()).isEquals();
    }

    @Override
	public int hashCode() {
        return new HashCodeBuilder().
            append(uid).toHashCode();
    }

	public int getCategory_type() {
		return category_type;
	}

	public void setCategory_type(int category_type) {
		this.category_type = category_type;
	}

	public int getGrade_type() {
		return grade_type;
	}

	public void setGrade_type(int grade_type) {
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

	public Boolean getShowStatisticsChart() {
		return showStatisticsChart;
	}

	public void setShowStatisticsChart(Boolean showStatisticsChart) {
		this.showStatisticsChart = showStatisticsChart;
	}

	/**
	 * If the course grade is displayed, should the letter grade be displayed?
	 * @return true/false if letter grade should be displayed
	 */
	public boolean isCourseLetterGradeDisplayed() {
		return courseLetterGradeDisplayed;
	}

	/**
	 * Set whether or not the letter grade should be displayed. Only consulted if course grade is displayed.
	 * @param courseLetterGradeDisplayed true/false to show letter grade or not
	 */
	public void setCourseLetterGradeDisplayed(boolean courseLetterGradeDisplayed) {
		this.courseLetterGradeDisplayed = courseLetterGradeDisplayed;
	}
	
}



