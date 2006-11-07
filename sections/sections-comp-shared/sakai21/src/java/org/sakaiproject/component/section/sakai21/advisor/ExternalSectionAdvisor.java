package org.sakaiproject.component.section.sakai21.advisor;

import java.util.Map;

import org.sakaiproject.site.api.Site;

/**
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

	/**
	 * When SectionManager changes a site's sections to be "externally managed", it
	 * must remove all of the manually created sections and retrieve the "externally
	 * managed" sections from someplace.  Where that "someplace" is will be determined
	 * by how the installation is integrated with external systems.
	 * 
	 * @param site The site that is replacing its manually defined sections with
	 * externally managed sections.
	 */
	public void replaceManualSectionsWithExternalSections(Site site);

	/**
	 * Updates the CourseSections in an site with externally managed sections.  This 
	 * @param site
	 */
	public void updateInternalSections(Site site);
	
	public Map<String, String> getSectionCategoryMap();
	
}
