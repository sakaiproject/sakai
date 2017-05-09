package org.sakaiproject.gradebook.entity;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.gradebook.logic.ExternalLogic;

@RunWith(MockitoJUnitRunner.class)
public class GradesEntityProviderTest {
	
	
	private static final String ADMIN_AND_INSTRUCTOR = "admin instructor";
	private static final String ADMIN_NOT_INSTRUCTOR = "admin !instructor";
	private static final String NOT_ADMIN_AND_INSTRUCTOR = "!admin instructor";
	private static final String NOT_ADMIN_NOT_INSTRUCTOR = "!admin !instructor";


	GradesEntityProvider gradesEntityProvider;
	GradesEntityProvider gradesEntityProviderMock;
	ExternalLogic externalLogicMock;
	EntityView entityViewMock;
	Gradebook gradebookMock;
	
	@Rule
	public final ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setUp() {
		gradesEntityProvider = new GradesEntityProvider();
		gradesEntityProviderMock = spy(new GradesEntityProvider());
		gradebookMock = gradebookMockDefaults();
		externalLogicMock = externalLogicMockDefaults();
		entityViewMock = entityViewMockDefaults();
		
		gradesEntityProvider.setExternalLogic(externalLogicMock);
		gradesEntityProviderMock.setExternalLogic(externalLogicMock);
	}
	
	private Gradebook gradebookMockDefaults() {
		Gradebook mock = spy(new Gradebook("id"));
		mock.items.add(mock(GradebookItem.class));
		
		return mock;
	}
	
	private ExternalLogic externalLogicMockDefaults() {
		ExternalLogic mock = mock(ExternalLogic.class);
		
		// by default, assume the current user is both an admin and an instructor
		when(mock.getCurrentUserId()).thenReturn(ADMIN_AND_INSTRUCTOR);
		
		when(mock.isUserAdmin(ADMIN_AND_INSTRUCTOR)).thenReturn(true);
		when(mock.isUserAdmin(ADMIN_NOT_INSTRUCTOR)).thenReturn(true);
		when(mock.isUserAdmin(NOT_ADMIN_AND_INSTRUCTOR)).thenReturn(false);
		when(mock.isUserAdmin(NOT_ADMIN_NOT_INSTRUCTOR)).thenReturn(false);
		
		when(mock.isUserInstructor(ADMIN_AND_INSTRUCTOR)).thenReturn(true);
		when(mock.isUserInstructor(ADMIN_NOT_INSTRUCTOR)).thenReturn(false);
		when(mock.isUserInstructor(NOT_ADMIN_AND_INSTRUCTOR)).thenReturn(true);
		when(mock.isUserInstructor(NOT_ADMIN_NOT_INSTRUCTOR)).thenReturn(false);
				
		List<Course> courses = new ArrayList<>();
		courses.add(mock(Course.class));
		when(mock.getCoursesForInstructor(anyString())).thenReturn(courses);
		
		if (gradebookMock == null) {
			gradebookMock = gradebookMockDefaults();
		}
		when(mock.getCourseGradebook(anyString(), any())).thenReturn(gradebookMock);
		
		return mock;
	}
	
	private EntityView entityViewMockDefaults() {
		EntityView mock = mock(EntityView.class);
		
		when(mock.getPathSegment(any(Integer.class))).thenReturn("not null");
		
		return mock;
	}
	
	@Test
	public void ensureExternalLogic() {
		try {
			gradesEntityProvider.ensureExternalLogic();
		} catch (IllegalStateException e) {
			fail("No exception expected");
		}
	}
	
	@Test
	public void ensureExternalLogicThrowsIllegalStateException() {
		gradesEntityProvider.setExternalLogic(null);
		exception.expect(IllegalStateException.class);
		
		gradesEntityProvider.ensureExternalLogic();
	}
	
	@Test
	public void ensureCurrentUser() {
		try {
			gradesEntityProvider.ensureCurrentUser("");
		} catch (SecurityException e) {
			fail("No exception expected");
		}
	}
	
	@Test
	public void ensureCurrentUserThrowsSecurityException() {
		when(externalLogicMock.getCurrentUserId()).thenReturn(null);
		exception.expect(SecurityException.class);
		exception.expectMessage("A message we set");
		
		gradesEntityProvider.ensureCurrentUser("A message we set");
	}
	
	@Test
	public void ensureCurrentUserIsAdminOrInstructor() {
		when(externalLogicMock.getCurrentUserId()).thenReturn(ADMIN_AND_INSTRUCTOR);
		try {
			gradesEntityProvider.ensureCurrentUserIsAdminOrInstructor("");
		} catch (SecurityException e) {
			fail("No exception expected");
		}
		
		when(externalLogicMock.getCurrentUserId()).thenReturn(ADMIN_NOT_INSTRUCTOR);
		try {
			gradesEntityProvider.ensureCurrentUserIsAdminOrInstructor("");
		} catch (SecurityException e) {
			fail("No exception expected");
		}
		
		when(externalLogicMock.getCurrentUserId()).thenReturn(NOT_ADMIN_AND_INSTRUCTOR);
		try {
			gradesEntityProvider.ensureCurrentUserIsAdminOrInstructor("");
		} catch (SecurityException e) {
			fail("No exception expected");
		}
	}
	
	@Test
	public void ensureCurrentUserIsAdminOrInstructorThrowsSecurityException() {
		when(externalLogicMock.getCurrentUserId()).thenReturn(NOT_ADMIN_NOT_INSTRUCTOR);
		exception.expect(SecurityException.class);
		exception.expectMessage("A message we set");
		
		gradesEntityProvider.ensureCurrentUserIsAdminOrInstructor("A message we set");
	}
	
	@Test
	public void ensureEntityViewPathSegment() {
		try {
			gradesEntityProvider.ensureEntityViewPathSegment(entityViewMock, 0, "");
		} catch (IllegalArgumentException e) {
			fail("No exception expected");
		}
	}
	
	@Test
	public void ensureEntityViewPathSegmentThrowsIllegalArgumentException() {
		when(entityViewMock.getPathSegment(any(Integer.class))).thenReturn(null);
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("A message we set");
		
		gradesEntityProvider.ensureEntityViewPathSegment(entityViewMock, 0, "A message we set");
	}
	
	@Test
	public void testGetInstructorCoursesList() {
		when(entityViewMock.getPathSegment(2)).thenReturn(null);
		assertThat(gradesEntityProviderMock.getInstructorCourses(entityViewMock), 
				instanceOf(List.class));
		verify(gradesEntityProviderMock).ensureExternalLogic();
		verify(gradesEntityProviderMock).ensureCurrentUser(any());
	}
	
	@Test
	public void testGetInstructorCoursesSingle() {
		when(entityViewMock.getPathSegment(2)).thenReturn("not null");
		
		assertThat(gradesEntityProviderMock.getInstructorCourses(entityViewMock), 
				instanceOf(Course.class));
		verify(gradesEntityProviderMock).ensureExternalLogic();
		verify(gradesEntityProviderMock).ensureCurrentUser(any());
	}
	
	@Test
	public void testGetInstructorCoursesThrowsSecurityException() {
		List<Course> emptyList = new ArrayList<>();
		when(externalLogicMock.getCoursesForInstructor(anyString())).thenReturn(emptyList);
		
		exception.expect(SecurityException.class);
		gradesEntityProviderMock.getInstructorCourses(entityViewMock);
		verify(gradesEntityProviderMock).ensureExternalLogic();
		verify(gradesEntityProviderMock).ensureCurrentUser(anyString());
	}
	
	@Test
	public void testGetCourseStudents() {
		assertThat(gradesEntityProviderMock.getCourseStudents(entityViewMock),
				instanceOf(List.class));
		
		verify(gradesEntityProviderMock).ensureExternalLogic();
		verify(gradesEntityProviderMock)
			.ensureEntityViewPathSegment(any(EntityView.class), any(Integer.class), anyString());
		verify(gradesEntityProviderMock).ensureCurrentUser(anyString());
		verify(gradesEntityProviderMock).ensureCurrentUserIsAdminOrInstructor(anyString());
	}
	
	@Test
	public void testGetCourseGradebook() {
		gradesEntityProviderMock.getCourseGradebook(entityViewMock);
		
		verify(gradesEntityProviderMock).ensureExternalLogic();
		verify(gradesEntityProviderMock)
			.ensureEntityViewPathSegment(any(EntityView.class), any(Integer.class), anyString());
		verify(gradesEntityProviderMock).ensureCurrentUser(anyString());
		verify(gradesEntityProviderMock).ensureCurrentUserIsAdminOrInstructor(anyString());
	}

	@Test
	public void testHandleGradeItemGET() {
		when(entityViewMock.getMethod()).thenReturn("GET");
		gradesEntityProviderMock.handleGradeItem(entityViewMock);
		
		verify(gradesEntityProviderMock).ensureExternalLogic();
		verify(gradesEntityProviderMock)
			.ensureEntityViewPathSegment(any(EntityView.class), any(Integer.class), anyString());
		verify(gradesEntityProviderMock).ensureCurrentUser(anyString());
		verify(gradesEntityProviderMock).ensureCurrentUserIsAdminOrInstructor(anyString());
	}
	
	@Test
	public void testHandleGradeItemPOST() {
		when(entityViewMock.getMethod()).thenReturn("POST");
		try {
			gradesEntityProviderMock.handleGradeItem(entityViewMock);
		} catch (NullPointerException e) {
			// we expect this null pointer exception because the request isn't being mocked
		}
		
		verify(gradesEntityProviderMock).ensureExternalLogic();
		verify(gradesEntityProviderMock)
			.ensureEntityViewPathSegment(any(EntityView.class), any(Integer.class), anyString());
		verify(gradesEntityProviderMock).ensureCurrentUser(anyString());
		verify(gradesEntityProviderMock).ensureCurrentUserIsAdminOrInstructor(anyString());
	}
	
	@Test
	public void testHandleGradeItemPUT() {
		when(entityViewMock.getMethod()).thenReturn("PUT");
		try {
			gradesEntityProviderMock.handleGradeItem(entityViewMock);
		} catch (NullPointerException e) {
			// we expect this null pointer exception because the request isn't being mocked
		}
		
		verify(gradesEntityProviderMock).ensureExternalLogic();
		verify(gradesEntityProviderMock)
			.ensureEntityViewPathSegment(any(EntityView.class), any(Integer.class), anyString());
		verify(gradesEntityProviderMock).ensureCurrentUser(anyString());
		verify(gradesEntityProviderMock).ensureCurrentUserIsAdminOrInstructor(anyString());
	}
}
