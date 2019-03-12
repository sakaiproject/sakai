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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A Gradebook is the top-level object in the Sakai Gradebook tool.  Only one
 * Gradebook should be associated with any particular course (or site, as they
 * exist in Sakai 1.5) for any given academic term.  How courses and terms are
 * determined will likely depend on the particular Sakai installation.
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Gradebook implements Serializable {

	private static final long serialVersionUID = 1L;

	@ToString.Include
	private Long id;

	@ToString.Include
	@EqualsAndHashCode.Include
    private String uid;

    private int version;

	@ToString.Include
    private String name;

    private GradeMapping selectedGradeMapping;

    private Set<GradeMapping> gradeMappings;

    private boolean assignmentsDisplayed;

	// Is the course grade to be shown at all?
    private boolean courseGradeDisplayed;

	// If the course grade is displayed, should the letter grade be displayed?
    private boolean courseLetterGradeDisplayed;

	// If the course grade is displayed, should the total points be displayed?
    private boolean coursePointsDisplayed;

    private boolean totalPointsDisplayed;

	// If the course grade is displayed, should the percentage be displayed?
    private boolean courseAverageDisplayed;

    private boolean allAssignmentsEntered;

    private boolean locked;

    private int grade_type;

    private int category_type;

    private Boolean equalWeightCategories;

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

}
