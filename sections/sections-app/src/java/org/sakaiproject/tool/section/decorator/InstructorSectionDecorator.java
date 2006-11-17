/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California and The Regents of the University of Michigan
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.section.decorator;

import java.io.Serializable;
import java.sql.Time;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Decorates a CourseSection for use in the instructor's (and TA's) page views.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class InstructorSectionDecorator extends CourseSectionDecorator
	implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(InstructorSectionDecorator.class);

	public static final int NAME_TRUNCATION_LENGTH = 20;
	public static final int LOCATION_TRUNCATION_LENGTH = 15;

	protected List instructorNames;
	protected int totalEnrollments;
	protected String spotsAvailable;
	private boolean flaggedForRemoval;

	public InstructorSectionDecorator(CourseSection courseSection, String categoryForDisplay,
			List instructorNames, int totalEnrollments) {
		super(courseSection, categoryForDisplay);
		this.instructorNames = instructorNames;
		this.totalEnrollments = totalEnrollments;
		
		populateSpotsAvailable(courseSection);
	}

	protected void populateSpotsAvailable(CourseSection courseSection) {
		if(courseSection.getMaxEnrollments() == null) {
			spotsAvailable = JsfUtil.getLocalizedMessage("section_max_size_unlimited");
		} else {
			int spots = courseSection.getMaxEnrollments().intValue() - totalEnrollments;
			// Allow negative values to be displayed
			spotsAvailable = Integer.toString(spots);
		}
	}

	public InstructorSectionDecorator() {
		// Needed for serialization
	}

	public List getInstructorNames() {
		return instructorNames;
	}
	public String getSpotsAvailable() {
		return spotsAvailable;
	}
	public int getTotalEnrollments() {
		return totalEnrollments;
	}
	public boolean isFlaggedForRemoval() {
		return flaggedForRemoval;
	}
	public void setFlaggedForRemoval(boolean flaggedForRemoval) {
		this.flaggedForRemoval = flaggedForRemoval;
	}

	public int compareTo(Object o) {
		return this.getTitle().toLowerCase().compareTo(((InstructorSectionDecorator)o).getTitle().toLowerCase());
	}

	/**
	 * TODO: Now that we need to sort titles non-case sensitive, is there much of a point to keeping the generic getFieldComparator() method?
	 * 
	 * @param sortAscending
	 * @return
	 */
	public static final Comparator getTitleComparator(final boolean sortAscending) {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				InstructorSectionDecorator section1 = (InstructorSectionDecorator)o1;
				InstructorSectionDecorator section2 = (InstructorSectionDecorator)o2;
				int categoryNameComparison = section1.getCategory().compareTo(section2.getCategory());
				if(categoryNameComparison == 0) {
					int comparison =  section1.getTitle().toLowerCase().compareTo(section2.getTitle().toLowerCase());
					return sortAscending ? comparison : (-1 * comparison);
				} else {
					return categoryNameComparison;
				}
			}
		};
	}

	public static final Comparator getTimeComparator(final boolean sortAscending) {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
					InstructorSectionDecorator section1 = (InstructorSectionDecorator)o1;
					InstructorSectionDecorator section2 = (InstructorSectionDecorator)o2;

					// First compare the category name, then compare the time
					int categoryNameComparison = section1.getCategory().compareTo(section2.getCategory());
					if(categoryNameComparison == 0) {
						// These are in the same category, so compare by the first meeting time
						List meetings1 = section1.getDecoratedMeetings();
						List meetings2 = section2.getDecoratedMeetings();
						
						MeetingDecorator meeting1 = (MeetingDecorator)meetings1.get(0);
						MeetingDecorator meeting2 = (MeetingDecorator)meetings2.get(0);
						
						Time startTime1 = meeting1.getStartTime();
						Time startTime2 = meeting2.getStartTime();
						
						if(startTime1 == null && startTime2 != null) {
							return sortAscending? -1 : 1 ;
						}
						if(startTime2 == null && startTime1 != null) {
							return sortAscending? 1 : -1 ;
						}
						
						if(startTime1 == null && startTime2 == null ||
								startTime1.equals(startTime2)) {
							return getTitleComparator(sortAscending).compare(o1, o2);
						}
						return sortAscending ? startTime1.compareTo(startTime2) : startTime2.compareTo(startTime1);
					} else {
						return categoryNameComparison;
					}
			}
		};
	}

	public static final Comparator getDayComparator(final boolean sortAscending) {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
					InstructorSectionDecorator section1 = (InstructorSectionDecorator)o1;
					InstructorSectionDecorator section2 = (InstructorSectionDecorator)o2;

					// First compare the category name, then compare the time
					int categoryNameComparison = section1.getCategory().compareTo(section2.getCategory());
					if(categoryNameComparison == 0) {
						// These are in the same category, so compare by the first meeting time
						List meetings1 = section1.getDecoratedMeetings();
						List meetings2 = section2.getDecoratedMeetings();
						
						MeetingDecorator meeting1 = (MeetingDecorator)meetings1.get(0);
						MeetingDecorator meeting2 = (MeetingDecorator)meetings2.get(0);
						
						String days1 = meeting1.getAbbreviatedDays();
						String days2 = meeting2.getAbbreviatedDays();
						
						if(days1 == null && days2 != null) {
							return sortAscending? -1 : 1 ;
						}
						if(days2 == null && days1 != null) {
							return sortAscending? 1 : -1 ;
						}
						
						if(days1 == null && days2 == null ||
								days1.equals(days2)) {
							return getTitleComparator(sortAscending).compare(o1, o2);
						}
						return sortAscending ? days1.compareTo(days2) : days2.compareTo(days1);
					} else {
						return categoryNameComparison;
					}
			}
		};
	}

	public static final Comparator getLocationComparator(final boolean sortAscending) {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
					InstructorSectionDecorator section1 = (InstructorSectionDecorator)o1;
					InstructorSectionDecorator section2 = (InstructorSectionDecorator)o2;

					// First compare the category name, then compare the time
					int categoryNameComparison = section1.getCategory().compareTo(section2.getCategory());
					if(categoryNameComparison == 0) {
						// These are in the same category, so compare by the first meeting time
						List meetings1 = section1.getDecoratedMeetings();
						List meetings2 = section2.getDecoratedMeetings();
						
						MeetingDecorator meeting1 = (MeetingDecorator)meetings1.get(0);
						MeetingDecorator meeting2 = (MeetingDecorator)meetings2.get(0);
						
						String location1 = meeting1.getLocation();
						String location2 = meeting2.getLocation();
						
						if(location1 == null && location2 != null) {
							return sortAscending? -1 : 1 ;
						}
						if(location2 == null && location1 != null) {
							return sortAscending? 1 : -1 ;
						}
						
						if(location1 == null && location2 == null ||
								location1.equals(location2)) {
							return getTitleComparator(sortAscending).compare(o1, o2);
						}
						return sortAscending ? location1.compareTo(location2) : location2.compareTo(location1);
					} else {
						return categoryNameComparison;
					}
			}
		};
	}

	public static final Comparator getManagersComparator(final boolean sortAscending) {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				if(o1 instanceof InstructorSectionDecorator && o2 instanceof InstructorSectionDecorator) {
					InstructorSectionDecorator section1 = (InstructorSectionDecorator)o1;
					InstructorSectionDecorator section2 = (InstructorSectionDecorator)o2;

					// First compare the category name, then compare the time
					int categoryNameComparison = section1.getCategory().compareTo(section2.getCategory());
					if(categoryNameComparison == 0) {
						// These are in the same category, so compare by the list of managers
						List managers1 = section1.getInstructorNames();
						List managers2 = section2.getInstructorNames();
						if(managers1.isEmpty() && ! managers2.isEmpty()) {
							return sortAscending? -1 : 1 ;
						}
						if(managers2.isEmpty() && ! managers1.isEmpty()) {
							return sortAscending? 1 : -1 ;
						}
						if(managers1.isEmpty() && managers2.isEmpty()) {
							return getTitleComparator(sortAscending).compare(o1, o2);
						}
						int managersComparison = managers1.get(0).toString().compareTo(managers2.get(0).toString());
						if(managersComparison == 0) {
							return getTitleComparator(sortAscending).compare(o1, o2);
						}
						return sortAscending ? managersComparison : (-1 * managersComparison);
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

	public static final Comparator getEnrollmentsComparator(final boolean sortAscending, final boolean useAvailable) {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				if(o1 instanceof InstructorSectionDecorator && o2 instanceof InstructorSectionDecorator) {
					InstructorSectionDecorator section1 = (InstructorSectionDecorator)o1;
					InstructorSectionDecorator section2 = (InstructorSectionDecorator)o2;

					// First compare the category name, then compare available spots
					int categoryNameComparison = section1.getCategory().compareTo(section2.getCategory());
					if(categoryNameComparison == 0) {
						// These are in the same category, so compare by total
						Integer maxEnrollments1 = section1.getMaxEnrollments();
						Integer maxEnrollments2 = section2.getMaxEnrollments();

						int total1 = section1.getTotalEnrollments();
						int total2 = section2.getTotalEnrollments();
						
						int availEnrollmentComparison;

						if(useAvailable) {
							if(maxEnrollments1 == null && maxEnrollments2 != null) {
								return sortAscending? 1 : -1 ;
							}
							if(maxEnrollments2 == null && maxEnrollments1 != null) {
								return sortAscending? -1 : 1 ;
							}
							if(maxEnrollments1 == null && maxEnrollments2 == null) {
								return getTitleComparator(sortAscending).compare(o1, o2);
							}
							availEnrollmentComparison = (maxEnrollments1.intValue() - section1.totalEnrollments) -
									(maxEnrollments2.intValue() - section2.totalEnrollments);
						} else {
							availEnrollmentComparison = total1 - total2;
						}
						
						// If these are in the same category, and have the same number of enrollments, use the title to sort
						if(availEnrollmentComparison == 0) {
							return getTitleComparator(sortAscending).compare(o1, o2);
						}
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
}

