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

package org.sakaiproject.tool.section.decorator;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Decorates a CourseSection for use in the students' UI.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class StudentSectionDecorator extends InstructorSectionDecorator
	implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(StudentSectionDecorator.class);

	protected boolean full;
	protected boolean joinable;
	protected boolean switchable;
	protected boolean member;
	
	public StudentSectionDecorator(CourseSection courseSection, String categoryForDisplay,
			List instructorNames, int totalEnrollments, boolean member,
			boolean memberOtherSection) {
		super(courseSection, categoryForDisplay, instructorNames, totalEnrollments);
		this.member = member;
		if( ! this.member && this.spotsAvailable.equals("0")) {
			this.full = true;
		}
		if( ! this.member && ! this.full) {
			this.switchable = memberOtherSection;
			this.joinable = ! memberOtherSection;
		}
	}
	
	/**
	 * Overrides the behavior of the superclass.  Students should not see a
	 * negative number of available spots.
	 */
	protected void populateSpotsAvailable(CourseSection courseSection) {
		if(courseSection.getMaxEnrollments() == null) {
			spotsAvailable = JsfUtil.getLocalizedMessage("section_max_size_unlimited");
		} else {
			// Do not allow negative values to be displayed
			int spots = courseSection.getMaxEnrollments().intValue() - totalEnrollments;
			if(spots < 0) {
				spotsAvailable = "0";
			} else {
				spotsAvailable = Integer.toString(spots);
			}
		}
	}

	public StudentSectionDecorator() {
		// Needed for serialization
	}
	
	public List getInstructorNames() {
		return instructorNames;
	}
	
	public static final Comparator getChangeComparator(final boolean sortAscending,
			final boolean joinAllowed, final boolean switchAllowed) {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				if(o1 instanceof StudentSectionDecorator && o2 instanceof StudentSectionDecorator) {
					StudentSectionDecorator section1 = (StudentSectionDecorator)o1;
					StudentSectionDecorator section2 = (StudentSectionDecorator)o2;

					// First compare the category name, then compare the change link
					int categoryNameComparison = section1.getCategory().compareTo(section2.getCategory());
					if(categoryNameComparison == 0) {
						// These are in the same category, so compare by the change link
						boolean member1 = section1.isMember();
						boolean member2 = section2.isMember();
						boolean full1 = section1.isFull();
						boolean full2 = section2.isFull();
						boolean switch1 = section1.isSwitchable();
						boolean switch2 = section2.isSwitchable();
						boolean join1 = section1.isJoinable();
						boolean join2 = section2.isJoinable();
						
						// If these are the same, sort by title
						if(member1 && member2 || full1 && full2 || switch1 && switch2 || join1 && join2) {
							return getFieldComparator("title", sortAscending).compare(o1, o2);
						}

						String section1ChangeLabel = getChangeLabel(section1, joinAllowed, switchAllowed);
						String section2ChangeLabel = getChangeLabel(section2, joinAllowed, switchAllowed);
						if(log.isDebugEnabled()) log.debug("Comparing " + section1ChangeLabel + " to " + section2ChangeLabel);
						int changeComparison = section1ChangeLabel.compareTo(section2ChangeLabel);
						return sortAscending ? changeComparison : (-1 * changeComparison);
					}
					// These are in different categories, so sort them by category name
					return categoryNameComparison;
				}
				if(log.isDebugEnabled()) log.debug("One of these is not a StudentSectionDecorator: "
						+ o1 + "," + o2);
				return 0;
			}

			private String getChangeLabel(StudentSectionDecorator section,
					boolean joinAllowed, boolean switchAllowed) {
				if(section.isJoinable() && joinAllowed) {
					return JsfUtil.getLocalizedMessage("student_view_join");
				} else  if(section.isSwitchable() && switchAllowed) {
					return JsfUtil.getLocalizedMessage("student_view_switch");
				} else if(section.isFull()) {
					return JsfUtil.getLocalizedMessage("student_view_full");
				} else if(section.isMember()) {
					return JsfUtil.getLocalizedMessage("student_view_member");
				} else {
					return "";
				}
			}
		};
	}

	public boolean isFull() {
		return full;
	}

	public boolean isJoinable() {
		return joinable;
	}

	public boolean isMember() {
		return member;
	}

	public boolean isSwitchable() {
		return switchable;
	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
