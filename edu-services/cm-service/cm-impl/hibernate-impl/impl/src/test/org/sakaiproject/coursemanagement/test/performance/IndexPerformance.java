/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.coursemanagement.test.performance;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import org.sakaiproject.coursemanagement.api.CourseManagementAdministration;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.Section;

@Slf4j
public class IndexPerformance {
	private static final int secCount = 10;
	private static final int enrollmentsPerEnrollmentSet = 100;
	private static final int membersPerSection = 100;
	
	private static final String esPrefix = "IndexPerf ES ";
	private static final String secPrefix = "IndexPerf SEC ";

	protected ApplicationContext appContext;
	protected CourseManagementAdministration cmAdmin;
	protected CourseManagementService cmService;
	protected PlatformTransactionManager tm;
	
	public IndexPerformance() {
		init();
	}
	
	public void init() {
		appContext = new ClassPathXmlApplicationContext(new String[] {"spring-test.xml", "spring-config-test.xml"});
		cmAdmin = (CourseManagementAdministration)appContext.getBean(CourseManagementAdministration.class.getName());
		cmService = (CourseManagementService)appContext.getBean(CourseManagementService.class.getName());
		tm = (PlatformTransactionManager)appContext.getBean("cmTransactionManager");
	}
	
	public void loadLotsOfData() {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus status = tm.getTransaction(def);
		
		try {
			String asId = "IndexPerf AS";
			cmAdmin.createAcademicSession(asId, asId, asId, null, null);
			
			String csId = "IndexPerf CS";
			cmAdmin.createCourseSet(csId, csId, csId, "DEPT", null);
			
			String ccId = "IndexPerf CC";
			cmAdmin.createCanonicalCourse(ccId, ccId, ccId);
			
			String coId = "IndecPerf CO";
			cmAdmin.createCourseOffering(coId, csId, csId, "open", asId, ccId, null, null);
			
			for(int i = 1; i <= secCount; i++) {
				String esId = esPrefix + i;
				Set<String> instructors = new HashSet<String>();
				instructors.add("instructor_A_" + i);
				instructors.add("instructor_B_" + i);
				instructors.add("instructor_C_" + i);
				cmAdmin.createEnrollmentSet(esId, esId, esId, "lecture", "3", coId, instructors);
			}
			
			for(int i = 1; i <= secCount; i++) {
				String secId = secPrefix + i;
				cmAdmin.createSection(secId, secId, secId, "lecture", null, coId, (esPrefix + i));
			}
			
			for(int i = 1; i <= secCount; i++) {
				for(int j = 1; j <= enrollmentsPerEnrollmentSet; j++) {
					cmAdmin.addOrUpdateEnrollment("student" + j, esPrefix + i, "enrolled", "3", "letter grade");
				}
			}

			for(int i = 1; i <= secCount; i++) {
				for(int j = 1; j <= membersPerSection; j++) {
					cmAdmin.addOrUpdateSectionMembership("student" + j, "some role", secPrefix + i, "some status");
				}
			}
		} catch (Exception e) {
			tm.rollback(status);
		} finally {
			if(!status.isCompleted()) {
				tm.commit(status);
			}
		}
	}
	
	public long getTimeToSelectEnrollments(String studentEid, String esEid) {
		long start = System.currentTimeMillis();
		Enrollment enr = cmService.findEnrollment(studentEid, esEid);
		long end = System.currentTimeMillis();
		return end - start;
	}

	public long getTimeToSelectMembers(String studentEid) {
		long start = System.currentTimeMillis();
		Map<String, String> roleMap = cmService.findSectionRoles(studentEid);
		long end = System.currentTimeMillis();
		return end - start;
	}

	public long getTimeToSelectInstructors(String instructorEid) {
		long start = System.currentTimeMillis();
		Set<Section> sections = cmService.findInstructingSections(instructorEid);
		long end = System.currentTimeMillis();
		return end - start;
	}

	// Static methods

	public static void main(String[] args) {
		IndexPerformance indexPerf = new IndexPerformance();
		indexPerf.loadLotsOfData();
		logEnrollmentSelects(indexPerf);
		logMemberSelects(indexPerf);
		logInstructorSelects(indexPerf);
	}

	private static void logEnrollmentSelects(IndexPerformance indexPerf) {
		long total = 0;
		for(int i = 1; i <= enrollmentsPerEnrollmentSet; i++) {
			long thisRun = indexPerf.getTimeToSelectEnrollments("student" + i, esPrefix + 1);
			total += thisRun;
		}
		double esMean = (double)total / (double)enrollmentsPerEnrollmentSet;
		log.info("Mean time (in milliseconds) to select Enrollments:\t" + esMean);
	}
	
	private static void logMemberSelects(IndexPerformance indexPerf) {
		long total = 0;
		for(int i = 1; i <= membersPerSection; i++) {
			long thisRun = indexPerf.getTimeToSelectMembers("student" + i);
			total += thisRun;
		}
		double secMean = (double)total / (double)membersPerSection;
		log.info("Mean time (in milliseconds) to select Memberships:\t" + secMean);
	}

	private static void logInstructorSelects(IndexPerformance indexPerf) {
		long total = 0;
		for(int i = 1; i <= secCount; i++) {
			long thisRun = indexPerf.getTimeToSelectInstructors("instructor_B_" + i);
			total += thisRun;
		}
		double secMean = (double)total / (double)secCount;
		log.info("Mean time (in milliseconds) to select Instructors:\t" + secMean);
	}

}
