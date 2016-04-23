/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.assignment.impl;

import junit.framework.TestCase;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.mockito.Mockito;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.user.api.User;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UserDirectoryService.class)
public class AssignmentComparatorTest extends TestCase {

	private static final Logger log = LoggerFactory.getLogger(AssignmentComparatorTest.class);
	
	private BaseAssignmentService.AssignmentComparator sortNameComparator;
	private BaseAssignmentService.AssignmentComparator submitterNameComparator;
	private AssignmentSubmission assignmentSubmission1, assignmentSubmission2, assignmentSubmission3, assignmentSubmission4;
	
	protected void setUp() throws Exception {
		// Mock Static Cover
		PowerMockito.mockStatic(UserDirectoryService.class);

		sortNameComparator = new BaseAssignmentService.AssignmentComparator("sortname", "true");
		submitterNameComparator = new BaseAssignmentService.AssignmentComparator("submitterName", "true");

		Assignment asm = Mockito.mock(Assignment.class);
		Mockito.when(asm.isGroup()).thenReturn(false);
		
		User user1 = Mockito.mock(User.class);
		Mockito.when(user1.getSortName()).thenReturn("Muñoz");

		assignmentSubmission1 = (AssignmentSubmission) Mockito.mock(AssignmentSubmission.class);
		Mockito.when(assignmentSubmission1.getAssignment()).thenReturn(asm);
		Mockito.when(assignmentSubmission1.getSubmitters()).thenReturn(new User[]{user1});

		User user2 = Mockito.mock(User.class);
		Mockito.when(user2.getSortName()).thenReturn("Muñiz");

		assignmentSubmission2 = (AssignmentSubmission) Mockito.mock(AssignmentSubmission.class);
		Mockito.when(assignmentSubmission2.getAssignment()).thenReturn(asm);
		Mockito.when(assignmentSubmission2.getSubmitters()).thenReturn(new User[]{user2});
		
		User user3 = Mockito.mock(User.class);
		Mockito.when(user3.getSortName()).thenReturn("Smith");

		assignmentSubmission3 = (AssignmentSubmission) Mockito.mock(AssignmentSubmission.class);
		Mockito.when(assignmentSubmission3.getAssignment()).thenReturn(asm);
		Mockito.when(assignmentSubmission3.getSubmitters()).thenReturn(new User[]{user3});

		User user4 = Mockito.mock(User.class);
		Mockito.when(user4.getSortName()).thenReturn("Adam");

		assignmentSubmission4 = (AssignmentSubmission) Mockito.mock(AssignmentSubmission.class);
		Mockito.when(assignmentSubmission4.getAssignment()).thenReturn(asm);
		Mockito.when(assignmentSubmission4.getSubmitters()).thenReturn(new User[]{user4});

		try {
			Mockito.when(UserDirectoryService.getUser("user1")).thenReturn(user1);
			Mockito.when(UserDirectoryService.getUser("user2")).thenReturn(user2);
			Mockito.when(UserDirectoryService.getUser("user3")).thenReturn(user3);
			Mockito.when(UserDirectoryService.getUser("user4")).thenReturn(user4);
			Mockito.when(UserDirectoryService.getUser("usernull")).thenReturn(null);
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.getMessage());
		}
		
	}
	
	public void testSortNameEQComparator() {
		// Equals results
		assertTrue(sortNameComparator.compare("user1","user1") == 0 
				&& sortNameComparator.compare("user2","user2") == 0
				&& sortNameComparator.compare("user3","user3") == 0
				&& sortNameComparator.compare("user4","user4") == 0);
	}
	
	public void testSortNameGTComparator() {
		// Greater than results
		assertTrue(sortNameComparator.compare("user1","user2") == 1
			&& sortNameComparator.compare("user1","user4") == 1
			&& sortNameComparator.compare("user2","user4") == 1
			&& sortNameComparator.compare("user3","user1") == 1
			&& sortNameComparator.compare("user3","user2") == 1
			&& sortNameComparator.compare("user3","user4") == 1);
	}
	
	public void testSortNameLTComparator() {
		// Lower than results
		assertTrue(sortNameComparator.compare("user2","user1") == -1
			&& sortNameComparator.compare("user4","user1") == -1
			&& sortNameComparator.compare("user4","user2") == -1
			&& sortNameComparator.compare("user1","user3") == -1
			&& sortNameComparator.compare("user2","user3") == -1
			&& sortNameComparator.compare("user4","user3") == -1);
	}
	
	
	public void testSubmitterNameEQComparator() {
		// Equals results
		assertTrue(submitterNameComparator.compare(assignmentSubmission1,assignmentSubmission1) == 0 
				&& submitterNameComparator.compare(assignmentSubmission2,assignmentSubmission2) == 0
				&& submitterNameComparator.compare(assignmentSubmission3,assignmentSubmission3) == 0
				&& submitterNameComparator.compare(assignmentSubmission4,assignmentSubmission4) == 0);
	}
	
	public void testSubmitterNameGTComparator() {
		// Greater than results
		assertTrue(submitterNameComparator.compare(assignmentSubmission1,assignmentSubmission2) == 1
			&& submitterNameComparator.compare(assignmentSubmission1,assignmentSubmission4) == 1
			&& submitterNameComparator.compare(assignmentSubmission2,assignmentSubmission4) == 1
			&& submitterNameComparator.compare(assignmentSubmission3,assignmentSubmission1) == 1
			&& submitterNameComparator.compare(assignmentSubmission3,assignmentSubmission2) == 1
			&& submitterNameComparator.compare(assignmentSubmission3,assignmentSubmission4) == 1);
	}
	
	public void testSubmitterNameLTComparator() {
		// Lower than results
		assertTrue(submitterNameComparator.compare(assignmentSubmission2,assignmentSubmission1) == -1
			&& submitterNameComparator.compare(assignmentSubmission4,assignmentSubmission1) == -1
			&& submitterNameComparator.compare(assignmentSubmission4,assignmentSubmission2) == -1
			&& submitterNameComparator.compare(assignmentSubmission1,assignmentSubmission3) == -1
			&& submitterNameComparator.compare(assignmentSubmission2,assignmentSubmission3) == -1
			&& submitterNameComparator.compare(assignmentSubmission4,assignmentSubmission3) == -1);
	}

}
