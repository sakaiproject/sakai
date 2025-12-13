/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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
package org.sakaiproject.gradebookng.tool.stats;

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.wicket.model.IModel;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.grading.api.GradeType;

/**
 * Implementation of {@link BaseStatistics} to render assignment stats
 */
public class AssignmentStatistics extends BaseStatistics {

	private static final long serialVersionUID = 1L;
	private final List<GbStudentGradeInfo> gradeInfo;
	private final Long assignmentId;
	private final Map<String, Double> gradeMap;
	private final GradeType gradeType;

	public AssignmentStatistics(final String id, final IModel<?> model) {
		super(id, model);

		// unpack model
		final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
		this.gradeInfo = (List<GbStudentGradeInfo>) modelData.get("gradeInfo");
		this.assignmentId = (Long) modelData.get("assignmentId");
		this.gradeMap = (Map<String, Double>) modelData.get("gradeMap");
		this.gradeType = (GradeType) modelData.get("gradeType");
	}

	/**
	 * Calculates stats based on the calculated course grade values, excluding any empty grades
	 *
	 * @return {@link DescriptiveStatistics}
	 */
	@Override
	protected DescriptiveStatistics calculateStatistics() {

		final DescriptiveStatistics stats = new DescriptiveStatistics();

		for (int i = 0; i < this.gradeInfo.size(); i++) {
			GbStudentGradeInfo studentGradeInfo = this.gradeInfo.get(i);

			Map<Long, GbGradeInfo> studentGrades = studentGradeInfo.getGrades();
			GbGradeInfo grade = studentGrades.get(this.assignmentId);

			if (grade != null && grade.getGrade() != null) {
				stats.addValue(gradeType == GradeType.LETTER ? gradeMap.get(grade.getGrade()) : Double.valueOf(grade.getGrade()));
			}
		}

		return stats;
	}

}
