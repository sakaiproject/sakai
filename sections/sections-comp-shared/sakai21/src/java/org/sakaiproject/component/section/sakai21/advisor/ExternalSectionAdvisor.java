package org.sakaiproject.component.section.sakai21.advisor;

import org.sakaiproject.site.api.Site;

/**
 * When SectionManager changes a site's sections to be "externally managed", it
 * must remove all of the manually created sections and retrieve the "externally
 * managed" sections from someplace.  Where that "someplace" is will be determined
 * by how the installation is integrated with external systems.
 * 
 * Some installations use web services to populate their sites and sections.  Others
 * use a provider with the legacy CourseManagementService and
 * CourseManagementProvider.  Still others will use the new CourseManagementService
 * and CourseManagementGroupProvider to populate sites and sections.
 * 
 * However an institution chooses to interface with the external world, they must
 * use an implementation of ExternalSectionAdvisor to keep Sakai's "internal" sections
 * in sync with the "external" sections.
 * 
 * @author josh
 *
 */
public interface ExternalSectionAdvisor {
	public void replaceManualSectionsWithExternalSections(Site site);
}
