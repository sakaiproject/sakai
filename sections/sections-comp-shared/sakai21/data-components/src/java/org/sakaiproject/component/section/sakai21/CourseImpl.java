/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.component.section.sakai21;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.service.legacy.authzGroup.Member;
import org.sakaiproject.service.legacy.authzGroup.Role;
import org.sakaiproject.service.legacy.entity.ResourceProperties;
import org.sakaiproject.service.legacy.entity.ResourcePropertiesEdit;
import org.sakaiproject.service.legacy.site.Section;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.SitePage;
import org.sakaiproject.service.legacy.site.ToolConfiguration;
import org.sakaiproject.service.legacy.time.Time;
import org.sakaiproject.service.legacy.user.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CourseImpl implements Course, Site, Serializable {
	private static final long serialVersionUID = 1L;

	public static final String EXTERNALLY_MAINTAINED = "sections_externally_maintained";
	public static final String STUDENT_REGISTRATION_ALLOWED = "sections_student_registration_allowed";
	public static final String STUDENT_SWITCHING_ALLOWED = "sections_student_switching_allowed";
	

	protected Site siteInstance;

	/**
	 * Creates a course from a sakai Site
	 * 
	 * @param site The Sakai site
	 */
	public CourseImpl(Site site) {
		this.siteInstance = site;
	}
	
	/**
	 * The siteContext is the site's ID
	 */
	public String getSiteContext() {
		return siteInstance.getId();
	}

	/**
	 * The course's UUID is the site's reference
	 */
	public String getUuid() {
		return siteInstance.getReference();
	}

	public boolean isExternallyManaged() {
		String str = siteInstance.getProperties().getProperty(EXTERNALLY_MAINTAINED);
		return "true".equals(str);
	}

	public boolean isSelfRegistrationAllowed() {
		String str = siteInstance.getProperties().getProperty(STUDENT_REGISTRATION_ALLOWED);
		return "true".equals(str);
	}

	public boolean isSelfSwitchingAllowed() {
		String str = siteInstance.getProperties().getProperty(STUDENT_REGISTRATION_ALLOWED);
		return "true".equals(str);
	}

	public int compareTo(Object o) {
		return siteInstance.compareTo(o);
	}

	public User getCreatedBy() {
		return siteInstance.getCreatedBy();
	}

	public Time getCreatedTime() {
		return siteInstance.getCreatedTime();
	}

	public String getDescription() {
		return siteInstance.getDescription();
	}

	public String getIconUrl() {
		return siteInstance.getIconUrl();
	}

	public String getIconUrlFull() {
		return siteInstance.getIconUrlFull();
	}

	public String getId() {
		return siteInstance.getId();
	}

	public String getInfoUrl() {
		return siteInstance.getInfoUrl();
	}

	public String getInfoUrlFull() {
		return siteInstance.getInfoUrlFull();
	}

	public String getJoinerRole() {
		return siteInstance.getJoinerRole();
	}

	public User getModifiedBy() {
		return siteInstance.getModifiedBy();
	}

	public Time getModifiedTime() {
		return siteInstance.getModifiedTime();
	}

	public List getOrderedPages() {
		return siteInstance.getOrderedPages();
	}

	public SitePage getPage(String id) {
		return siteInstance.getPage(id);
	}

	public List getPages() {
		return siteInstance.getPages();
	}

	public ResourceProperties getProperties() {
		return siteInstance.getProperties();
	}

	public String getReference() {
		return siteInstance.getReference();
	}

	public Section getSection(String id) {
		return siteInstance.getSection(id);
	}

	public Collection getSections() {
		return siteInstance.getSections();
	}

	public String getShortDescription() {
		return siteInstance.getShortDescription();
	}

	public String getSkin() {
		return siteInstance.getSkin();
	}

	public String getTitle() {
		return siteInstance.getTitle();
	}

	public ToolConfiguration getTool(String id) {
		return siteInstance.getTool(id);
	}

	public String getType() {
		return siteInstance.getType();
	}

	public String getUrl() {
		return siteInstance.getUrl();
	}

	public boolean hasSections() {
		return siteInstance.hasSections();
	}

	public boolean isJoinable() {
		return siteInstance.isJoinable();
	}

	public boolean isPublished() {
		return siteInstance.isPublished();
	}

	public boolean isPubView() {
		return siteInstance.isPubView();
	}

	public boolean isType(Object type) {
		return siteInstance.isType(type);
	}

	public void loadAll() {
		siteInstance.loadAll();
	}

	public Element toXml(Document doc, Stack stack) {
		return siteInstance.toXml(doc, stack);
	}

	public SitePage addPage() {
		return siteInstance.addPage();
	}

	public Section addSection() {
		return siteInstance.addSection();
	}

	public ResourcePropertiesEdit getPropertiesEdit() {
		return siteInstance.getPropertiesEdit();
	}

	public boolean isActiveEdit() {
		return siteInstance.isActiveEdit();
	}

	public void regenerateIds() {
		siteInstance.regenerateIds();
	}

	public void removePage(SitePage page) {
		siteInstance.removePage(page);
	}

	public void removeSection(Section section) {
		siteInstance.removeSection(section);
	}

	public void setDescription(String description) {
		siteInstance.setDescription(description);
	}

	public void setIconUrl(String url) {
		siteInstance.setIconUrl(url);
	}

	public void setInfoUrl(String url) {
		siteInstance.setInfoUrl(url);
	}

	public void setJoinable(boolean joinable) {
		siteInstance.setJoinable(joinable);
	}

	public void setJoinerRole(String role) {
		siteInstance.setJoinerRole(role);
	}

	public void setPublished(boolean published) {
		siteInstance.setPublished(published);
	}

	public void setPubView(boolean pubView) {
		siteInstance.setPubView(pubView);
	}

	public void setShortDescription(String description) {
		siteInstance.setShortDescription(description);
	}

	public void setSkin(String skin) {
		siteInstance.setSkin(skin);
	}

	public void setTitle(String title) {
		siteInstance.setTitle(title);
	}

	public void setType(String type) {
		siteInstance.setType(type);
	}

	public void addMember(String userId, String roleId, boolean active, boolean provided) {
		siteInstance.addMember(userId, roleId, active, provided);
	}

	public Role addRole(String id) throws IdUsedException {
		return siteInstance.addRole(id);
	}

	public Role addRole(String id, Role other) throws IdUsedException {
		return siteInstance.addRole(id, other);
	}

	public String getMaintainRole() {
		return siteInstance.getMaintainRole();
	}

	public Member getMember(String userId) {
		return siteInstance.getMember(userId);
	}

	public Set getMembers() {
		return siteInstance.getMembers();
	}

	public String getProviderGroupId() {
		return siteInstance.getProviderGroupId();
	}

	public Role getRole(String id) {
		return siteInstance.getRole(id);
	}

	public Set getRoles() {
		return siteInstance.getRoles();
	}

	public Role getUserRole(String userId) {
		return siteInstance.getUserRole(userId);
	}

	public Set getUsers() {
		return siteInstance.getUsers();
	}

	public Set getUsersHasRole(String role) {
		return siteInstance.getUsersHasRole(role);
	}

	public Set getUsersIsAllowed(String function) {
		return siteInstance.getUsersIsAllowed(function);
	}

	public boolean hasRole(String userId, String role) {
		return siteInstance.hasRole(userId, role);
	}

	public boolean isAllowed(String userId, String function) {
		return siteInstance.isAllowed(userId, function);
	}

	public boolean isEmpty() {
		return siteInstance.isEmpty();
	}

	public void removeMember(String userId) {
		siteInstance.removeMember(userId);
	}

	public void removeMembers() {
		siteInstance.removeMembers();
	}

	public void removeRole(String role) {
		siteInstance.removeRole(role);
	}

	public void removeRoles() {
		siteInstance.removeRoles();
	}

	public void setMaintainRole(String role) {
		siteInstance.setMaintainRole(role);
	}

	public void setProviderGroupId(String id) {
		siteInstance.setProviderGroupId(id);
	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
