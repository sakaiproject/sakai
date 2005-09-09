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

public class InstructorSectionDecorator extends CourseSectionDecorator
	implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(InstructorSectionDecorator.class);

	protected String instructorNames;
	protected int totalEnrollments;
	protected String spotsAvailable;
	
	public InstructorSectionDecorator(CourseSection courseSection, String categoryForDisplay,
			String instructorNames, int totalEnrollments) {
		super(courseSection, categoryForDisplay);
		this.instructorNames = instructorNames;
		this.totalEnrollments = totalEnrollments;
		if(courseSection.getMaxEnrollments() == null) {
			spotsAvailable = JsfUtil.getLocalizedMessage("section_max_size_unlimited");
		} else {
			int spots = courseSection.getMaxEnrollments().intValue() - totalEnrollments;
			if(spots < 0) {
				spotsAvailable = "0";
			} else {
				spotsAvailable = Integer.toString(spots);
			}
		}
	}
	
	public InstructorSectionDecorator() {
		// Needed for serialization
	}
	
	public String getInstructorNames() {
		return instructorNames;
	}
	public String getSpotsAvailable() {
		return spotsAvailable;
	}
	
	public int compareTo(Object o) {
		return this.getTitle().compareTo(((InstructorSectionDecorator)o).getTitle());
	}
	
	public static final Comparator getTitleComparator(final boolean sortAscending,
			final List categoryNames, final List categoryIds) {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				if(o1 instanceof InstructorSectionDecorator && o2 instanceof InstructorSectionDecorator) {
					InstructorSectionDecorator section1 = (InstructorSectionDecorator)o1;
					InstructorSectionDecorator section2 = (InstructorSectionDecorator)o2;
	
					// First compare the category name, then compare the title
					int categoryNameComparison = categoryNameComparison(section1,
							section2, categoryNames, categoryIds);
					if(categoryNameComparison == 0) {
						// These are in the same category, so compare by title
						int titleComparison = section1.getTitle().compareTo(section2.getTitle());
						return sortAscending ? titleComparison : (-1 * titleComparison);
					}
					// These are in different categories, so sort them by category name
					return categoryNameComparison;
				}
				if(log.isDebugEnabled()) log.debug("One of these is not an InstructorSectionDecorator: "
						+ o1 + "," + o2);
				return 0;
			}
		};
	}
	
	public static final Comparator getTimeComparator(final boolean sortAscending,
			final List categoryNames, final List categoryIds) {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				if(o1 instanceof InstructorSectionDecorator && o2 instanceof InstructorSectionDecorator) {
					InstructorSectionDecorator section1 = (InstructorSectionDecorator)o1;
					InstructorSectionDecorator section2 = (InstructorSectionDecorator)o2;

					// First compare the category name, then compare the time
					int categoryNameComparison = categoryNameComparison(section1,
							section2, categoryNames, categoryIds);
					if(categoryNameComparison == 0) {
						// These are in the same category, so compare by time
						String meetingTimes1 = section1.getMeetingTimes();
						String meetingTimes2 = section2.getMeetingTimes();
						if(meetingTimes1 == null && meetingTimes2 != null) {
							return sortAscending? -1 : 1 ;
						}
						if(meetingTimes2 == null && meetingTimes1 != null) {
							return sortAscending? 1 : -1 ;
						}
						if(meetingTimes1 == null && meetingTimes2 == null) {
							return 0;
						}
						int timeComparison = meetingTimes1.compareTo(meetingTimes2);
						return sortAscending ? timeComparison : (-1 * timeComparison);
					}
					// These are in different categories, so sort them by category name
					return categoryNameComparison;
				}
				if(log.isDebugEnabled()) log.debug("One of these is not an InstructorSectionDecorator: "
						+ o1 + "," + o2);
				return 0;
			}
		};
	}

	public static final Comparator getLocationComparator(final boolean sortAscending,
			final List categoryNames, final List categoryIds) {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				if(o1 instanceof InstructorSectionDecorator && o2 instanceof InstructorSectionDecorator) {
					InstructorSectionDecorator section1 = (InstructorSectionDecorator)o1;
					InstructorSectionDecorator section2 = (InstructorSectionDecorator)o2;

					// First compare the category name, then compare the location
					int categoryNameComparison = categoryNameComparison(section1,
							section2, categoryNames, categoryIds);
					if(categoryNameComparison == 0) {
						// These are in the same category, so compare by location
						String location1 = section1.getLocation();
						String location2 = section2.getLocation();
						if(location1 == null && location2 != null) {
							return sortAscending? -1 : 1 ;
						}
						if(location2 == null && location1 != null) {
							return sortAscending? 1 : -1 ;
						}
						if(location1 == null && location2 == null) {
							return 0;
						}
						int locationComparison = location1.compareTo(location2);
						return sortAscending ? locationComparison : (-1 * locationComparison);
					}
					// These are in different categories, so sort them by category name
					return categoryNameComparison;
				}
				if(log.isDebugEnabled()) log.debug("One of these is not an InstructorSectionDecorator: "
						+ o1 + "," + o2);
				return 0;
			}
		};
	}

	public static final Comparator getMaxEnrollmentsComparator(final boolean sortAscending,
			final List categoryNames, final List categoryIds) {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				if(o1 instanceof InstructorSectionDecorator && o2 instanceof InstructorSectionDecorator) {
					InstructorSectionDecorator section1 = (InstructorSectionDecorator)o1;
					InstructorSectionDecorator section2 = (InstructorSectionDecorator)o2;

					// First compare the category name, then compare max enrollments
					int categoryNameComparison = categoryNameComparison(section1,
							section2, categoryNames, categoryIds);
					if(categoryNameComparison == 0) {
						// These are in the same category, so compare by max enrollments
						Integer maxEnrollments1 = section1.getMaxEnrollments();
						Integer maxEnrollments2 = section2.getMaxEnrollments();
						if(maxEnrollments1 == null && maxEnrollments2 != null) {
							return sortAscending? 1 : -1 ;
						}
						if(maxEnrollments2 == null && maxEnrollments1 != null) {
							return sortAscending? -1 : 1 ;
						}
						if(maxEnrollments1 == null && maxEnrollments2 == null) {
							return 0;
						}
						int maxEnrollmentComparison = maxEnrollments1.compareTo(maxEnrollments2);
						return sortAscending ? maxEnrollmentComparison : (-1 * maxEnrollmentComparison);
					}
					// These are in different categories, so sort them by category name
					return categoryNameComparison;
				}
				if(log.isDebugEnabled()) log.debug("One of these is not an InstructorSectionDecorator: "
						+ o1 + "," + o2);
				return 0;
			}
		};
	}

	public static final Comparator getAvailableEnrollmentsComparator(final boolean sortAscending,
			final List categoryNames, final List categoryIds) {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				if(o1 instanceof InstructorSectionDecorator && o2 instanceof InstructorSectionDecorator) {
					InstructorSectionDecorator section1 = (InstructorSectionDecorator)o1;
					InstructorSectionDecorator section2 = (InstructorSectionDecorator)o2;

					// First compare the category name, then compare available spots
					int categoryNameComparison = categoryNameComparison(section1,
							section2, categoryNames, categoryIds);
					if(categoryNameComparison == 0) {
						// These are in the same category, so compare by available enrollments
						Integer maxEnrollments1 = section1.getMaxEnrollments();
						Integer maxEnrollments2 = section2.getMaxEnrollments();
						if(maxEnrollments1 == null && maxEnrollments2 != null) {
							return sortAscending? 1 : -1 ;
						}
						if(maxEnrollments2 == null && maxEnrollments1 != null) {
							return sortAscending? -1 : 1 ;
						}
						if(maxEnrollments1 == null && maxEnrollments2 == null) {
							return 0;
						}
						int availEnrollmentComparison =
							(maxEnrollments1.intValue() - section1.totalEnrollments) -
							(maxEnrollments2.intValue() - section2.totalEnrollments);
						return sortAscending ? availEnrollmentComparison : (-1 * availEnrollmentComparison);
					}
					// These are in different categories, so sort them by category name
					return categoryNameComparison;
				}
				if(log.isDebugEnabled()) log.debug("One of these is not an InstructorSectionDecorator: "
						+ o1 + "," + o2);
				return 0;
			}
		};
	}

	private static final int categoryNameComparison(InstructorSectionDecorator section1,
			InstructorSectionDecorator section2, List categoryNames, List categoryIds) {
		String section1Name = (String)categoryNames.get(categoryIds.indexOf(section1.getCategory()));
		String section2Name = (String)categoryNames.get(categoryIds.indexOf(section2.getCategory()));
		return section1Name.compareTo(section2Name);
	}

}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
