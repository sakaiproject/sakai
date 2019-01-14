/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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

import lombok.Getter;
import lombok.Setter;

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

	@Getter
	@Setter
	private Long id;

	@Getter
	@Setter
    private String uid;

	@Getter
	@Setter
    private int version;

	@Getter
	@Setter
    private String name;

	@Getter
	@Setter
    private GradeMapping selectedGradeMapping;

	@Getter
	@Setter
    private Set<GradeMapping> gradeMappings;

	@Getter
	@Setter
    private boolean assignmentsDisplayed;

	// Is the course grade to be shown at all?
	@Getter
	@Setter
    private boolean courseGradeDisplayed;

	// If the course grade is displayed, should the letter grade be displayed?
	@Getter
	@Setter
    private boolean courseLetterGradeDisplayed;

	// If the course grade is displayed, should the total points be displayed?
	@Getter
	@Setter
    private boolean coursePointsDisplayed;

	@Getter
	@Setter
    private boolean totalPointsDisplayed;

	// If the course grade is displayed, should the percentage be displayed?
	@Getter
	@Setter
    private boolean courseAverageDisplayed;

	@Getter
	@Setter
    private boolean allAssignmentsEntered;

	@Getter
	@Setter
    private boolean locked;

	@Getter
	@Setter
    private int grade_type;

	@Getter
	@Setter
    private int category_type;

	@Getter
	@Setter
    private Boolean equalWeightCategories;

	@Getter
	@Setter
    private Boolean scaledExtraCredit;

	@Getter
	@Setter
    private Boolean showMean;

	@Getter
	@Setter
    private Boolean showMedian;

	@Getter
	@Setter
    private Boolean showMode;

	@Getter
	@Setter
    private Boolean showRank;

	@Getter
	@Setter
    private Boolean showItemStatistics;

	@Getter
	@Setter
    private Boolean showStatisticsChart;

	@Getter
	@Setter
	private boolean assignmentStatsDisplayed;

	@Getter
	@Setter
	private boolean courseGradeStatsDisplayed;

    /**
     * Default no-arg constructor needed for persistence
     */
    public Gradebook() {
    }

    /**
     * Creates a new gradebook with the given siteId and name
	 * @param name
	 */
	public Gradebook(final String name) {
        this.name = name;
	}


    @Override
	public String toString() {
        return new ToStringBuilder(this).
        append("id", this.id).
        append("uid", this.uid).
        append("name", this.name).toString();
    }

    @Override
	public boolean equals(final Object other) {
        if (!(other instanceof Gradebook)) {
            return false;
        }
        final Gradebook gb = (Gradebook)other;
        return new EqualsBuilder().
		    append(this.uid, gb.getUid()).isEquals();
    }

    @Override
	public int hashCode() {
        return new HashCodeBuilder().
            append(this.uid).toHashCode();
    }

}



