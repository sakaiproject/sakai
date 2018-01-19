/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.coursemanagement.impl.job;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CanonicalCourse;
import org.sakaiproject.coursemanagement.api.CourseManagementAdministration;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.CourseSet;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Meeting;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;

/**
 * Synchronizes the state of the local CourseManagementService with an external
 * data source.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public abstract class CmSynchronizer {
	protected CourseManagementService cmService;
	protected CourseManagementAdministration cmAdmin;
	
	protected abstract InputStream getXmlInputStream();

	public synchronized void syncAllCmObjects() {
		long start = System.currentTimeMillis();
		if(log.isInfoEnabled()) log.info("Starting CM synchronization");
		// Load the xml document from the xml stream
		InputStream in = null;
		Document doc = null;
		try {
			in = getXmlInputStream();
			doc = new SAXBuilder().build(in);
			if(log.isDebugEnabled()) log.debug("XML Document built successful from input stream");
		} catch (Exception e) {
			log.error("Could not build a jdom document from the xml input stream... " + e);
			// Close the input stream
			if(in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
					log.error("Unable to close input stream " + in);
				}
			}
			throw new RuntimeException(e);
		}

		try {
			reconcileAcademicSessions(doc);
			reconcileCurrentAcademicSessions(doc);
			reconcileCanonicalCourses(doc);
			reconcileCourseOfferings(doc);
			reconcileSections(doc);
			reconcileEnrollmentSets(doc);
			reconcileCourseSets(doc);
		} finally {
			// Close the input stream
			if(in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
					log.error("Unable to close input stream " + in);
				}
			}
		}

		if(log.isInfoEnabled()) log.info("Finished CM synchronization in " + (System.currentTimeMillis()-start) + " ms");
	}

	protected void reconcileAcademicSessions(Document doc) {
		long start = System.currentTimeMillis();
		if(log.isInfoEnabled()) log.info("Reconciling AcademicSessions");

		// Get a list of all existing academic sessions
		List existing = cmService.getAcademicSessions();
		
		// Create a map of existing AcademicSession EIDs to AcademicSessions
		Map academicSessionMap = new HashMap();
		for(Iterator iter = existing.iterator(); iter.hasNext();) {
			AcademicSession as = (AcademicSession)iter.next();
			academicSessionMap.put(as.getEid(), as);
		}

		// Find the academic sessions specified in the xml doc and reconcile them
		try {
			XPath docsPath = XPath.newInstance("/cm-data/academic-sessions/academic-session");
			List items = docsPath.selectNodes(doc);
			// Add or update each of the academic sessions specified in the xml
			for(Iterator iter = items.iterator(); iter.hasNext();) {
				Element element = (Element)iter.next();
				String eid = element.getChildText("eid");
				if(log.isDebugEnabled()) log.debug("Found academic section to reconcile: " + eid);
				if(academicSessionMap.containsKey(eid)) {
					updateAcademicSession((AcademicSession)academicSessionMap.get(eid), element);
				} else {
					addAcademicSession(element);
				}
			}
		} catch (JDOMException jde) {
			log.error(jde.getMessage());
		}
		
		if(log.isInfoEnabled()) log.info("Finished reconciling AcademicSessions in " + (System.currentTimeMillis()-start) + " ms");
	}
	
	protected void reconcileCurrentAcademicSessions(Document doc) {
		try {
			List<String> academicSessionEids = new ArrayList<String>();
			XPath docsPath = XPath.newInstance("/cm-data/current-academic-sessions/academic-session-eid");
			List<Element> items = docsPath.selectNodes(doc);
			for (Element element : items) {
				academicSessionEids.add(element.getText());
			}
			if(log.isDebugEnabled()) log.debug("Found current academic sessions to reconcile: " + academicSessionEids);
			cmAdmin.setCurrentAcademicSessions(academicSessionEids);
		} catch (JDOMException jde) {
			log.error(jde.getMessage());
		}
	}
	
	protected void addAcademicSession(Element element) {
		String eid = element.getChildText("eid");
		if(log.isDebugEnabled()) log.debug("Adding AcademicSession + " + eid);
		String title = element.getChildText("title");
		String description = element.getChildText("description");
		Date startDate = getDate(element.getChildText("start-date"));
		Date endDate = getDate(element.getChildText("end-date"));
		cmAdmin.createAcademicSession(eid, title, description, startDate, endDate);
	}
	
	protected void updateAcademicSession(AcademicSession session, Element element) {
		if(log.isDebugEnabled()) log.debug("Updating AcademicSession + " + session.getEid());
		session.setTitle(element.getChildText("title"));
		session.setDescription(element.getChildText("description"));
		session.setStartDate(getDate(element.getChildText("start-date")));
		session.setEndDate(getDate(element.getChildText("end-date")));
		cmAdmin.updateAcademicSession(session);
	}
	
	protected void reconcileCanonicalCourses(Document doc) {
		long start = System.currentTimeMillis();
		if(log.isInfoEnabled()) log.info("Reconciling CanonicalCourses");
		
		try {
			XPath docsPath = XPath.newInstance("/cm-data/canonical-courses/canonical-course");
			List items = docsPath.selectNodes(doc);
			if(log.isDebugEnabled()) log.debug("Found " + items.size() + " canonical courses to reconcile");

			// Add or update each of the canonical courses specified in the xml
			for(Iterator iter = items.iterator(); iter.hasNext();) {
				Element element = (Element)iter.next();
				String eid = element.getChildText("eid");
				if(log.isDebugEnabled()) log.debug("Reconciling canonical course " + eid);
				
				if(cmService.isCanonicalCourseDefined(eid)) {
					updateCanonicalCourse(cmService.getCanonicalCourse(eid), element);
				} else {
					addCanonicalCourse(element);
				}
			}
		} catch (JDOMException jde) {
			log.error(jde.getMessage());
		}
		
		if(log.isInfoEnabled()) log.info("Finished reconciling CanonicalCourses in " + (System.currentTimeMillis()-start) + " ms");
	}
	
	protected void addCanonicalCourse(Element element) {
		String eid = element.getChildText("eid");
		if(log.isDebugEnabled()) log.debug("Adding CanonicalCourse + " + eid);
		String title = element.getChildText("title");
		String description = element.getChildText("description");
		cmAdmin.createCanonicalCourse(eid, title, description);
	}

	protected void updateCanonicalCourse(CanonicalCourse canonicalCourse, Element element) {
		if(log.isDebugEnabled()) log.debug("Updating CanonicalCourse + " + canonicalCourse.getEid());
		canonicalCourse.setTitle(element.getChildText("title"));
		canonicalCourse.setDescription(element.getChildText("description"));
		cmAdmin.updateCanonicalCourse(canonicalCourse);
	}

	protected void reconcileCourseOfferings(Document doc) {
		long start = System.currentTimeMillis();
		if(log.isInfoEnabled()) log.info("Reconciling CourseOfferings");
		
		try {
			XPath docsPath = XPath.newInstance("/cm-data/course-offerings/course-offering");
			List items = docsPath.selectNodes(doc);
			if(log.isDebugEnabled()) log.debug("Found " + items.size() + " course offerings to reconcile");

			// Add or update each of the course offerings specified in the xml
			for(Iterator iter = items.iterator(); iter.hasNext();) {
				Element element = (Element)iter.next();
				String eid = element.getChildText("eid");
				if(log.isDebugEnabled()) log.debug("Reconciling course offering " + eid);
				
				CourseOffering courseOffering = null;
				if(cmService.isCourseOfferingDefined(eid)) {
					courseOffering = updateCourseOffering(cmService.getCourseOffering(eid), element);
				} else {
					courseOffering = addCourseOffering(element);
				}
				
				// Update the members
				Element members = element.getChild("members");
				if(members != null) {
					updateCourseOfferingMembers(members, courseOffering);
				}

			}
		} catch (JDOMException jde) {
			log.error(jde.getMessage());
		}
		if(log.isInfoEnabled()) log.info("Finished reconciling CourseOfferings in " + (System.currentTimeMillis()-start) + " ms");
	}
	
	protected CourseOffering updateCourseOffering(CourseOffering courseOffering, Element element) {
		if(log.isDebugEnabled()) log.debug("Updating CourseOffering + " + courseOffering.getEid());
		AcademicSession newAcademicSession = cmService.getAcademicSession(element.getChildText("academic-session-eid"));
		courseOffering.setTitle(element.getChildText("title"));
		courseOffering.setDescription(element.getChildText("description"));
		courseOffering.setStatus(element.getChildText("status"));
		courseOffering.setAcademicSession(newAcademicSession);
		courseOffering.setStartDate(getDate(element.getChildText("start-date")));
		courseOffering.setEndDate(getDate(element.getChildText("end-date")));
		
		// Note: we can't update a course offering's canonical course.  This seems reasonable.
		
		cmAdmin.updateCourseOffering(courseOffering);
		return courseOffering;
	}
	
	protected CourseOffering addCourseOffering(Element element) {
		String eid = element.getChildText("eid");
		if(log.isDebugEnabled()) log.debug("Adding CourseOffering + " + eid);
		String title = element.getChildText("title");
		String description = element.getChildText("description");
		String status = element.getChildText("status");
		String academicSessionEid = element.getChildText("academic-session-eid");
		String canonicalCourseEid = element.getChildText("canonical-course-eid");
		Date startDate = getDate(element.getChildText("start-date"));
		Date endDate = getDate(element.getChildText("end-date"));
		return cmAdmin.createCourseOffering(eid, title, description, status, academicSessionEid, canonicalCourseEid, startDate, endDate);
	}

	protected void updateCourseOfferingMembers(Element membersElement, CourseOffering courseOffering) {
		Set existingMembers = cmService.getCourseOfferingMemberships(courseOffering.getEid());
		
		// Build a map of existing member userEids to Memberships
		Map existingMemberMap = new HashMap(existingMembers.size());
		for(Iterator iter = existingMembers.iterator(); iter.hasNext();) {
			Membership member = (Membership)iter.next();
			existingMemberMap.put(member.getUserId(), member);
		}

		// Keep track of the new members userEids, and add/update them
		Set newMembers = new HashSet();
		List memberElements = membersElement.getChildren("member");
		for(Iterator iter = memberElements.iterator(); iter.hasNext();) {
			Element memberElement = (Element)iter.next();
			String userEid = memberElement.getAttributeValue("userEid");
			String role = memberElement.getAttributeValue("role");
			String status = memberElement.getAttributeValue("status");
			newMembers.add(cmAdmin.addOrUpdateCourseOfferingMembership(userEid, role, courseOffering.getEid(), status));
		}
		
		// For everybody not in the newMembers set, remove their memberships
		existingMembers.removeAll(newMembers);
		for(Iterator iter = existingMembers.iterator(); iter.hasNext();) {
			Membership member = (Membership)iter.next();
			cmAdmin.removeCourseOfferingMembership(member.getUserId(), courseOffering.getEid());
		}
	}

	protected void reconcileEnrollmentSets(Document doc) {
		long start = System.currentTimeMillis();
		if(log.isInfoEnabled()) log.info("Reconciling EnrollmentSets");
		
		try {
			XPath docsPath = XPath.newInstance("/cm-data/enrollment-sets/enrollment-set");
			List items = docsPath.selectNodes(doc);
			if(log.isDebugEnabled()) log.debug("Found " + items.size() + " enrollment sets to reconcile");

			// Add or update each of the enrollment sets specified in the xml
			for(Iterator iter = items.iterator(); iter.hasNext();) {
				Element element = (Element)iter.next();
				String eid = element.getChildText("eid");
				if(log.isDebugEnabled()) log.debug("Reconciling enrollment set " + eid);
				
				EnrollmentSet enr = null;
				if(cmService.isEnrollmentSetDefined(eid)) {
					enr = updateEnrollmentSet(cmService.getEnrollmentSet(eid), element);
				} else {
					enr = addEnrollmentSet(element);
				}
				reconcileEnrollments(element.getChild("enrollments"), enr);
				reconcileOfficialInstructors(element, enr);
			}
		} catch (JDOMException jde) {
			log.error(jde.getMessage());
		}
		
		if(log.isInfoEnabled()) log.info("Finished reconciling EnrollmentSets in " + (System.currentTimeMillis()-start) + " ms");
	}

	protected void reconcileEnrollments(Element enrollmentsElement, EnrollmentSet enrollmentSet) {
		List newEnrollmentElements = enrollmentsElement.getChildren("enrollment");
		Set newUserEids = new HashSet();
		Set existingEnrollments = cmService.getEnrollments(enrollmentSet.getEid());

		for(Iterator iter = newEnrollmentElements.iterator(); iter.hasNext();) {
			Element enrollmentElement = (Element)iter.next();
			String userEid = enrollmentElement.getChildText("userEid");
			newUserEids.add(userEid);
			String status = enrollmentElement.getChildText("status");
			String credits = enrollmentElement.getChildText("credits");
			String gradingScheme = enrollmentElement.getChildText("grading-scheme");
			cmAdmin.addOrUpdateEnrollment(userEid,enrollmentSet.getEid(), status, credits, gradingScheme);
		}
		
		for(Iterator iter = existingEnrollments.iterator(); iter.hasNext();) {
			Enrollment existingEnr = (Enrollment) iter.next();
			if( ! newUserEids.contains(existingEnr.getUserId())) {
				// Drop this enrollment
				cmAdmin.removeEnrollment(existingEnr.getUserId(), enrollmentSet.getEid());
			}
		}
	}

	protected void reconcileOfficialInstructors(Element esElement, EnrollmentSet enrollmentSet) {
		List newInstructorElements = esElement.getChild("official-instructors").getChildren("official-instructor");
		Set newUserEids = new HashSet();

		for(Iterator iter = newInstructorElements.iterator(); iter.hasNext();) {
			String userEid = ((Element)iter.next()).getText();
			newUserEids.add(userEid);
		}
		Set officialInstructors = enrollmentSet.getOfficialInstructors();
		if(officialInstructors == null) {
			officialInstructors = new HashSet();
			enrollmentSet.setOfficialInstructors(officialInstructors);
		}
		officialInstructors.clear();
		officialInstructors.addAll(newUserEids);
		cmAdmin.updateEnrollmentSet(enrollmentSet);
	}

	protected Set getChildValues(Element element) {
		Set childValues = new HashSet();
		for(Iterator elementIter = element.getChildren().iterator(); elementIter.hasNext();) {
			Element childElement = (Element)elementIter.next();
			childValues.add(childElement.getText());
		}
		return childValues;
	}

	protected void updateOfficialInstructors(EnrollmentSet enr, Element element) {
		
	}

	protected EnrollmentSet addEnrollmentSet(Element element) {
		String eid = element.getChildText("eid");
		if(log.isDebugEnabled()) log.debug("Adding EnrollmentSet + " + eid);
		String title = element.getChildText("title");
		String description = element.getChildText("description");
		String category = element.getChildText("category");
		String courseOfferingEid = element.getChildText("course-offering-eid");
		String defaultEnrollmentCredits = element.getChildText("default-enrollment-credits");
		return cmAdmin.createEnrollmentSet(eid, title, description, category, defaultEnrollmentCredits, courseOfferingEid, null);
	}

	protected EnrollmentSet updateEnrollmentSet(EnrollmentSet enrollmentSet, Element element) {
		if(log.isDebugEnabled()) log.debug("Updating EnrollmentSet + " + enrollmentSet.getEid());
		enrollmentSet.setTitle(element.getChildText("title"));
		enrollmentSet.setDescription(element.getChildText("description"));
		enrollmentSet.setCategory(element.getChildText("category"));
		enrollmentSet.setDefaultEnrollmentCredits(element.getChildText("default-enrollment-credits"));
		// Note: It is not possible to change the course offering, but this seems OK.
		
		cmAdmin.updateEnrollmentSet(enrollmentSet);
		return enrollmentSet;
	}

	protected void reconcileSections(Document doc) {
		long start = System.currentTimeMillis();
		if(log.isInfoEnabled()) log.info("Reconciling Sections");
		
		try {
			XPath docsPath = XPath.newInstance("/cm-data/sections/section");
			List items = docsPath.selectNodes(doc);
			if(log.isDebugEnabled()) log.debug("Found " + items.size() + " sections to reconcile");

			// Add or update each of the sections specified in the xml
			for(Iterator iter = items.iterator(); iter.hasNext();) {
				Element element = (Element)iter.next();
				String eid = element.getChildText("eid");
				if(log.isDebugEnabled()) log.debug("Reconciling section " + eid);
				
				Section section = null;
				if(cmService.isSectionDefined(eid)) {
					section = updateSection(cmService.getSection(eid), element);
				} else {
					section = addSection(element);
				}
				
				// Now update the meetings on this section
				Set meetingTimes = section.getMeetings();
				if(meetingTimes == null) {
					meetingTimes = new HashSet();
					section.setMeetings(meetingTimes);
				}
				
				Element meetingsElement = element.getChild("meetings");
				for(Iterator meetingIter = meetingsElement.getChildren().iterator(); meetingIter.hasNext();) {
					Element meetingElement = (Element)meetingIter.next();
					String location = meetingElement.getChildText("location");
					// TODO Sync start and finish times
//					String time = meetingElement.getChildText("time");
					String notes = meetingElement.getChildText("notes");
					Meeting meeting = cmAdmin.newSectionMeeting(eid, location, null, null, notes);
					meetingTimes.add(meeting);
				}
				cmAdmin.updateSection(section);

				// Update the members
				Element members = element.getChild("members");
				if(members != null) {
					updateSectionMembers(members, section);
				}
			}
		} catch (JDOMException jde) {
			log.error(jde.getMessage());
		}
		if(log.isInfoEnabled()) log.info("Finished reconciling Sections in " + (System.currentTimeMillis()-start) + " ms");
	}
	
	protected Section updateSection(Section section, Element element) {
		if(log.isDebugEnabled()) log.debug("Updating Section + " + section.getEid());
		section.setTitle(element.getChildText("title"));
		section.setDescription(element.getChildText("description"));
		section.setCategory(element.getChildText("category"));
		if(cmService.isSectionDefined(element.getChildText("parent-section-eid"))) {
			section.setParent(cmService.getSection(element.getChildText("parent-section-eid")));
		}
		// Note: There's no way to change the course offering.  This makes sense, though.

		if(cmService.isEnrollmentSetDefined(element.getChildText("enrollment-set-eid"))) {
			section.setEnrollmentSet(cmService.getEnrollmentSet(element.getChildText("enrollment-set-eid")));
		}
		cmAdmin.updateSection(section);
		return section;
	}
	
	protected Section addSection(Element element) {
		String eid = element.getChildText("eid");
		if(log.isDebugEnabled()) log.debug("Adding Section + " + eid);
		String title = element.getChildText("title");
		String description = element.getChildText("description");
		String category = element.getChildText("category");
		String parentSectionEid = null;
		String parentIdFromXml =  element.getChildText("parent-section-eid");
		if(parentIdFromXml != null && ! "".equals(parentIdFromXml)) {
			parentSectionEid =parentIdFromXml;
		}
		String courseOfferingEid = element.getChildText("course-offering-eid");
		String enrollmentSetEid = null;
		String enrollmentSetEidFromXml =  element.getChildText("enrollment-set-eid");
		if(cmService.isEnrollmentSetDefined(enrollmentSetEidFromXml)) {
			enrollmentSetEid = enrollmentSetEidFromXml;
		}
		return cmAdmin.createSection(eid, title, description, category, parentSectionEid, courseOfferingEid, enrollmentSetEid);
	}

	protected void updateSectionMembers(Element membersElement, Section section) {
		Set existingMembers = cmService.getSectionMemberships(section.getEid());
		
		// Build a map of existing member userEids to Memberships
		Map existingMemberMap = new HashMap(existingMembers.size());
		for(Iterator iter = existingMembers.iterator(); iter.hasNext();) {
			Membership member = (Membership)iter.next();
			existingMemberMap.put(member.getUserId(), member);
		}

		// Keep track of the new members userEids, and add/update them
		Set newMembers = new HashSet();
		List memberElements = membersElement.getChildren("member");
		for(Iterator iter = memberElements.iterator(); iter.hasNext();) {
			Element memberElement = (Element)iter.next();
			String userEid = memberElement.getAttributeValue("userEid");
			String role = memberElement.getAttributeValue("role");
			String status = memberElement.getAttributeValue("status");
			newMembers.add(cmAdmin.addOrUpdateSectionMembership(userEid, role, section.getEid(), status));
		}
		
		// For everybody not in the newMembers set, remove their memberships
		existingMembers.removeAll(newMembers);
		for(Iterator iter = existingMembers.iterator(); iter.hasNext();) {
			Membership member = (Membership)iter.next();
			cmAdmin.removeSectionMembership(member.getUserId(), section.getEid());
		}
	}

	
	protected void reconcileCourseSets(Document doc) {
		long start = System.currentTimeMillis();
		if(log.isInfoEnabled()) log.info("Reconciling CourseSets");
		
		try {
			XPath docsPath = XPath.newInstance("/cm-data/course-sets/course-set");
			List items = docsPath.selectNodes(doc);
			if(log.isDebugEnabled()) log.debug("Found " + items.size() + " course sets to reconcile");

			// Add or update each of the course offerings specified in the xml
			for(Iterator iter = items.iterator(); iter.hasNext();) {
				Element element = (Element)iter.next();
				String eid = element.getChildText("eid");
				if(log.isDebugEnabled()) log.debug("Reconciling course set " + eid);

				CourseSet courseSet = null;
				if(cmService.isCourseSetDefined(eid)) {
					courseSet = updateCourseSet(cmService.getCourseSet(eid), element);
				} else {
					courseSet = addCourseSet(element);
				}
				
				// Update the members
				Element members = element.getChild("members");
				if(members != null) {
					updateCourseSetMembers(members, courseSet);
				}
			}
		} catch (JDOMException jde) {
			log.error(jde.getMessage());
		}
		if(log.isInfoEnabled()) log.info("Finished reconciling CourseSets in " + (System.currentTimeMillis()-start) + " ms");
	}
	
	protected void updateCourseSetMembers(Element membersElement, CourseSet courseSet) {
		Set existingMembers = cmService.getCourseSetMemberships(courseSet.getEid());
		
		// Build a map of existing member userEids to Memberships
		Map existingMemberMap = new HashMap(existingMembers.size());
		for(Iterator iter = existingMembers.iterator(); iter.hasNext();) {
			Membership member = (Membership)iter.next();
			existingMemberMap.put(member.getUserId(), member);
		}

		// Keep track of the new members userEids, and add/update them
		Set newMembers = new HashSet();
		List memberElements = membersElement.getChildren("member");
		for(Iterator iter = memberElements.iterator(); iter.hasNext();) {
			Element memberElement = (Element)iter.next();
			String userEid = memberElement.getAttributeValue("userEid");
			String role = memberElement.getAttributeValue("role");
			String status = memberElement.getAttributeValue("status");
			newMembers.add(cmAdmin.addOrUpdateCourseSetMembership(userEid, role, courseSet.getEid(), status));
		}
		
		// For everybody not in the newMembers set, remove their memberships
		existingMembers.removeAll(newMembers);
		for(Iterator iter = existingMembers.iterator(); iter.hasNext();) {
			Membership member = (Membership)iter.next();
			cmAdmin.removeCourseSetMembership(member.getUserId(), courseSet.getEid());
		}
	}

	protected CourseSet updateCourseSet(CourseSet courseSet, Element element) {
		if(log.isDebugEnabled()) log.debug("Updating CourseSet + " + courseSet.getEid());
		courseSet.setTitle(element.getChildText("title"));
		courseSet.setDescription(element.getChildText("description"));
		courseSet.setCategory(element.getChildText("category"));
		String parentEid = element.getChildText("parent-course-set");
		if(cmService.isCourseSetDefined(parentEid)) {
			CourseSet parent = cmService.getCourseSet(parentEid);
			courseSet.setParent(parent);
		}
		cmAdmin.updateCourseSet(courseSet);
		return courseSet;
	}
	
	protected CourseSet addCourseSet(Element element) {
		String eid = element.getChildText("eid");
		if(log.isDebugEnabled()) log.debug("Adding CourseSet + " + eid);
		String title = element.getChildText("title");
		String description = element.getChildText("description");
		String category = element.getChildText("category");
		String parentEid = null;
		String parentFromXml = element.getChildText("parent-course-set");
		if(parentFromXml != null && ! "".equals(parentFromXml)) {
			parentEid = parentFromXml;
		}
		return cmAdmin.createCourseSet(eid, title, description, category, parentEid);
	}

	protected Date getDate(String str) {
		if(str == null || "".equals(str)) {
			return null;
		}
		SimpleDateFormat df = new SimpleDateFormat("M/d/yyyy");
		try {
			return df.parse(str);
		} catch (ParseException pe) {
			log.warn("Invalid date: " + str);
			return null;
		}
	}
	
	// Dependency Injection

	public void setCmAdmin(CourseManagementAdministration cmAdmin) {
		this.cmAdmin = cmAdmin;
	}

	public void setCmService(CourseManagementService cmService) {
		this.cmService = cmService;
	}
}
