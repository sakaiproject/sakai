/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.gradebookng.business;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.api.LocaleService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GradebookNgTestConfiguration {

	@Bean
	public GradebookNgBusinessService gradebookNgBusinessService(final SiteService siteService,
			final UserDirectoryService userDirectoryService, final SecurityService securityService,
			final SectionManager sectionManager, final CourseManagementService courseManagementService,
			final GroupProvider groupProvider, final LocaleService localeService) {

		final GradebookNgBusinessService service = new GradebookNgBusinessService();
		service.setSiteService(siteService);
		service.setUserDirectoryService(userDirectoryService);
		service.setSecurityService(securityService);
		service.setSectionManager(sectionManager);
		service.setCourseManagementService(courseManagementService);
		service.setGroupProvider(groupProvider);
		service.setLocaleService(localeService);
		return service;
	}

	@Bean
	public SiteService siteService() throws IdUnusedException {
		final SiteService siteService = mock(SiteService.class);
		final Site site = mock(Site.class);
		final Group group = mock(Group.class);
		final Member student1 = mock(Member.class);
		final Member student2 = mock(Member.class);

		when(student1.getUserId()).thenReturn("student1");
		when(student2.getUserId()).thenReturn("student2");
		when(group.getMembers()).thenReturn(Set.of(student1, student2));
		when(site.getId()).thenReturn("siteId");
		when(site.getReference()).thenReturn("/site/siteId");
		when(site.getGroup("gUid")).thenReturn(group);
		when(site.getUsersIsAllowed(GbRole.STUDENT.getValue())).thenReturn(
				new HashSet<>(Set.of("student1", "student2")));
		when(siteService.getSite("siteId")).thenReturn(site);
		return siteService;
	}

	@Bean
	public UserDirectoryService userDirectoryService() {
		final UserDirectoryService userDirectoryService = mock(UserDirectoryService.class);
		final User currentUser = mock(User.class);
		final User student1 = user("student1", "Stu Dent1");
		final User student2 = user("student2", "Stu Dent2");

		when(currentUser.getId()).thenReturn("instructor");
		when(userDirectoryService.getCurrentUser()).thenReturn(currentUser);
		when(userDirectoryService.getUsers(anyList())).thenAnswer(invocation ->
				new ArrayList<>(List.of(student1, student2)));
		return userDirectoryService;
	}

	private User user(final String id, final String displayName) {
		final User user = mock(User.class);
		when(user.getId()).thenReturn(id);
		when(user.getDisplayId()).thenReturn(id);
		when(user.getDisplayName()).thenReturn(displayName);
		when(user.getFirstName()).thenReturn(displayName);
		when(user.getLastName()).thenReturn("");
		when(user.getSortName()).thenReturn(displayName);
		return user;
	}

	@Bean
	public SecurityService securityService() {
		final SecurityService securityService = mock(SecurityService.class);
		when(securityService.unlock("instructor", GbRole.INSTRUCTOR.getValue(), "/site/siteId")).thenReturn(true);
		return securityService;
	}

	@Bean
	public SectionManager sectionManager() {
		return mock(SectionManager.class);
	}

	@Bean
	public CourseManagementService courseManagementService() {
		return mock(CourseManagementService.class);
	}

	@Bean
	public GroupProvider groupProvider() {
		return mock(GroupProvider.class);
	}

	@Bean
	public LocaleService localeService() {
		final LocaleService localeService = mock(LocaleService.class);
		when(localeService.getLocaleForCurrentSiteAndUser()).thenReturn(Locale.US);
		return localeService;
	}
}
