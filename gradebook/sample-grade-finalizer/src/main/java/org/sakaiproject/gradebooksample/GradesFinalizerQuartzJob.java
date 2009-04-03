/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.gradebooksample;

import java.util.regex.Pattern;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 */
public class GradesFinalizerQuartzJob implements Job {
    private static Pattern siteUidPattern = Pattern.compile("(?i)siteUid=");
	private static Pattern academicSessionEidPattern = Pattern.compile("(?i)academicSessionEid=");
	
	GradesFinalizer gradesFinalizer;

	public void execute(JobExecutionContext context) throws JobExecutionException {
		String jobName = context.getJobDetail().getName();
		if (jobName != null) {
			String[] splitJobName = siteUidPattern.split(jobName);
			if (splitJobName.length == 2) {
				gradesFinalizer.setSiteUid(splitJobName[1]);
			} else {
				splitJobName = academicSessionEidPattern.split(jobName);
				if (splitJobName.length == 2) {
					gradesFinalizer.setAcademicSessionEid(splitJobName[1]);
				}
			}
		}
		gradesFinalizer.execute();
	}

	public void setGradesFinalizer(GradesFinalizer gradesFinalizer) {
		this.gradesFinalizer = gradesFinalizer;
	}

}
