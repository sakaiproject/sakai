/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.section.decorator;

import java.io.Serializable;
import java.sql.Time;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.Meeting;
import org.sakaiproject.tool.section.jsf.JsfUtil;
import org.sakaiproject.tool.section.jsf.RowGroupable;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
/**
 * Decorates a CourseSection for use in the instructor's (and TA's) page views.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class SectionDecorator implements RowGroupable,Serializable, Comparable{
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(SectionDecorator.class);

    public static final int NAME_TRUNCATION_LENGTH = 20;
    public static final int LOCATION_TRUNCATION_LENGTH = 15;
    public static final String READ_ONLY_SECTION_CATEGORIES = "section.info.readonly.section.categories";

    protected CourseSection section;
    protected String categoryForDisplay;
    protected List<MeetingDecorator> decoratedMeetings;

    protected List<String> instructorNames;
    protected int totalEnrollments;
    protected String spotsAvailable;
    private boolean flaggedForRemoval;

    /* Whether this decorator should show the number of spots available as a negative
      * number or zero when the section is overenrolled */
    protected boolean showNegativeSpots;

    // SAK-23495
    protected boolean readOnly;
    private static Set<String> readOnlyCategories;

    /**
     * Creates a SectionDecorator from a vanilla CourseSection.
     *
     * @param section
     */
    public SectionDecorator(CourseSection section, boolean showNegativeSpots) {
        this.section = section;
        this.showNegativeSpots = showNegativeSpots;
        this.decoratedMeetings = new ArrayList<MeetingDecorator>();
        if(section.getMeetings() != null) {
            for(Iterator iter = section.getMeetings().iterator(); iter.hasNext();) {
                decoratedMeetings.add(new MeetingDecorator((Meeting)iter.next()));
            }
        }
        if (readOnlyCategories == null) {
            readOnlyCategories = new HashSet<String>();
            ServerConfigurationService serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
            if (serverConfigurationService != null) {
                String[] readOnlySectionCategories = serverConfigurationService.getStrings(READ_ONLY_SECTION_CATEGORIES);
                if (readOnlySectionCategories != null) {
                    readOnlyCategories = new HashSet<String>(Arrays.asList(readOnlySectionCategories));
                }
            }
        }
        this.readOnly = readOnlyCategories.contains(section.getCategory());  
    }

    /**
     * Creates a SectionDecorator with more contextual information about the section.
     *
     * @param section The CourseSection to decorate
     * @param categoryForDisplay The CourseSection's category label
     * @param instructorNames The names of TAs in this CourseSection
     * @param totalEnrollments The total number of enrollments in this CourseSection
     */
    public SectionDecorator(CourseSection section, String categoryForDisplay,
                            List<String> instructorNames, int totalEnrollments, boolean showNegativeSpots) {
        this(section, showNegativeSpots);
        this.categoryForDisplay = categoryForDisplay;

        this.instructorNames = instructorNames;
        this.totalEnrollments = totalEnrollments;

        if(section.getMaxEnrollments() == null) {
            spotsAvailable = JsfUtil.getLocalizedMessage("section_max_size_unlimited");
        } else {
            int spots = section.getMaxEnrollments().intValue() - totalEnrollments;
            if(spots < 0 && ! showNegativeSpots) {
                spotsAvailable = "0";
            } else {
                spotsAvailable = Integer.toString(spots);
            }
        }
    }

    public SectionDecorator() {
        // Needed for serialization
    }

    public boolean isReadOnly() {
    	return readOnly;
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
        return this.getTitle().toLowerCase().compareTo(((SectionDecorator)o).getTitle().toLowerCase());
    }

    /**
     * Compares SectionDecorators by the section's title.
     *
     * @param sortAscending
     * @return
     */
    public static final Comparator<SectionDecorator> getTitleComparator(final boolean sortAscending) {
        return new Comparator<SectionDecorator>() {
            public int compare(SectionDecorator section1, SectionDecorator section2) {
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

    /**
     * Compares SectionDecorators by the section's first meeting times.
     *
     * @param sortAscending
     * @return
     */
    public static final Comparator<SectionDecorator> getTimeComparator(final boolean sortAscending) {
        return new Comparator<SectionDecorator>() {
            public int compare(SectionDecorator section1, SectionDecorator section2) {

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
                            return getTitleComparator(sortAscending).compare(section1, section2);
                        }
                        return sortAscending ? startTime1.compareTo(startTime2) : startTime2.compareTo(startTime1);
                    } else {
                        return categoryNameComparison;
                    }
            }
        };
    }

    /**
     * Compares SectionDecorators by the section's first meeting days.
     *
     * @param sortAscending
     * @return
     */
    public static final Comparator<SectionDecorator> getDayComparator(final boolean sortAscending) {
        return new Comparator<SectionDecorator>() {
            public int compare(SectionDecorator section1, SectionDecorator section2) {
                    // First compare the category name, then compare the time
                    int categoryNameComparison = section1.getCategory().compareTo(section2.getCategory());
                    if(categoryNameComparison == 0) {
                        // These are in the same category, so compare by the first meeting time
                        List<MeetingDecorator> meetings1 = section1.getDecoratedMeetings();
                        List<MeetingDecorator> meetings2 = section2.getDecoratedMeetings();

                        String sortString1 = generateSortableDayString(meetings1.get(0));
                        String sortString2 = generateSortableDayString(meetings2.get(0));

                        int diff = sortString1.compareTo(sortString2);

                        if(diff == 0) {
                            return getTitleComparator(sortAscending).compare(section1, section2);
                        }
                        return sortAscending ? diff : -1*diff ;
                    } else {
                        return categoryNameComparison;
                    }
            }
        };
    }

    /**
     * Generate a string that contains information on the meeting days for a section
     * meeting, and is sortable.
     *
     * @param meeting A meeting we're interested in sorting by day of the week.
     * @return A string that sorts in the order of the meetings' days of the week.
     */
    private static final String generateSortableDayString(MeetingDecorator meeting) {
        StringBuilder sb = new StringBuilder();
        if(meeting.isMonday()) {
            sb.append("a");
        }
        if(meeting.isTuesday()) {
            sb.append("b");
        }
        if(meeting.isWednesday()) {
            sb.append("c");
        }
        if(meeting.isThursday()) {
            sb.append("d");
        }
        if(meeting.isFriday()) {
            sb.append("e");
        }
        if(meeting.isSaturday()) {
            sb.append("f");
        }
        if(meeting.isSunday()) {
            sb.append("g");
        }
        return sb.toString();
    }
    /**
     * Compares SectionDecorators by the section's first meeting location.
     *
     * @param sortAscending
     * @return
     */
    public static final Comparator<SectionDecorator> getLocationComparator(final boolean sortAscending) {
        return new Comparator<SectionDecorator>() {
            public int compare(SectionDecorator section1, SectionDecorator section2) {

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
                            return getTitleComparator(sortAscending).compare(section1, section2);
                        }
                        return sortAscending ? location1.compareTo(location2) : location2.compareTo(location1);
                    } else {
                        return categoryNameComparison;
                    }
            }
        };
    }

    /**
     * Compares SectionDecorators by the section's TA names.
     *
     * @param sortAscending
     * @return
     */
    public static final Comparator<SectionDecorator> getManagersComparator(final boolean sortAscending) {
        return new Comparator<SectionDecorator>() {
            public int compare(SectionDecorator section1, SectionDecorator section2) {
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
                        return getTitleComparator(sortAscending).compare(section1, section2);
                    }
                    int managersComparison = managers1.get(0).toString().compareTo(managers2.get(0).toString());
                    if(managersComparison == 0) {
                        return getTitleComparator(sortAscending).compare(section1, section2);
                    }
                    return sortAscending ? managersComparison : (-1 * managersComparison);
                }
                // These are in different categories, so sort them by category name
                return categoryNameComparison;
            }
        };
    }

    /**
     * Compares SectionDecorators by the section's enrollments.
     *
     * @param sortAscending Whether to sort ascending or descending
     * @param useAvailable Whether to use the number of available enrollments, or the total number of enrollments to sort
     * @return
     */
    public static final Comparator<SectionDecorator> getEnrollmentsComparator(final boolean sortAscending, final boolean useAvailable) {
        return new Comparator<SectionDecorator>() {
            public int compare(SectionDecorator section1, SectionDecorator section2) {
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
                                return getTitleComparator(sortAscending).compare(section1, section2);
                            }
                            availEnrollmentComparison = (maxEnrollments1.intValue() - section1.totalEnrollments) -
                                    (maxEnrollments2.intValue() - section2.totalEnrollments);
                        } else {
                            availEnrollmentComparison = total1 - total2;
                        }

                        // If these are in the same category, and have the same number of enrollments, use the title to sort
                        if(availEnrollmentComparison == 0) {
                            return getTitleComparator(sortAscending).compare(section1, section2);
                        }
                        return sortAscending ? availEnrollmentComparison : (-1 * availEnrollmentComparison);
                    }
                    // These are in different categories, so sort them by category name
                    return categoryNameComparison;
            }
        };
    }

    public CourseSection getSection() {
        return section;
    }

    public List<MeetingDecorator> getDecoratedMeetings() {
        return decoratedMeetings;
    }

    // Decorator methods
    public String getCategoryForDisplay() {
        return categoryForDisplay;
    }

    // Delegate methods

    public String getCategory() {
        return section.getCategory();
    }

    public Course getCourse() {
        return section.getCourse();
    }

    public Integer getMaxEnrollments() {
        return section.getMaxEnrollments();
    }

    public String getTitle() {
        return section.getTitle();
    }

    public String getUuid() {
        return section.getUuid();
    }

    public class MeetingDecorator implements Serializable {
        private static final long serialVersionUID = 1L;
        private Meeting meeting;

        public MeetingDecorator() {
            // Needed for serialization
        }

        public MeetingDecorator(Meeting meeting) {
            this.meeting = meeting;
        }

        private List<String> getDayList() {
            List<String> list = new ArrayList<String>();
            if(meeting.isMonday())
                list.add("day_of_week_monday");
            if(meeting.isTuesday())
                list.add("day_of_week_tuesday");
            if(meeting.isWednesday())
                list.add("day_of_week_wednesday");
            if(meeting.isThursday())
                list.add("day_of_week_thursday");
            if(meeting.isFriday())
                list.add("day_of_week_friday");
            if(meeting.isSaturday())
                list.add("day_of_week_saturday");
            if(meeting.isSunday())
                list.add("day_of_week_sunday");
            return list;
        }

        private List<String> getAbbreviatedDayList() {
            List<String> list = new ArrayList<String>();
            ResourceLoader rl = new ResourceLoader();
            DateFormatSymbols dfs = new DateFormatSymbols(rl.getLocale());
            String[] daysOfWeek = dfs.getShortWeekdays();
            if(meeting.isMonday())
                list.add(daysOfWeek[Calendar.MONDAY]);
            if(meeting.isTuesday())
                list.add(daysOfWeek[Calendar.TUESDAY]);
            if(meeting.isWednesday())
                list.add(daysOfWeek[Calendar.WEDNESDAY]);
            if(meeting.isThursday())
                list.add(daysOfWeek[Calendar.THURSDAY]);
            if(meeting.isFriday())
                list.add(daysOfWeek[Calendar.FRIDAY]);
            if(meeting.isSaturday())
                list.add(daysOfWeek[Calendar.SATURDAY]);
            if(meeting.isSunday())
                list.add(daysOfWeek[Calendar.SUNDAY]);
            return list;
        }

        public String getTimes() {
            String timeSepChar = ",";

            StringBuilder sb = new StringBuilder();

            // Start time
            ResourceLoader rl = new ResourceLoader();
            DateFormat df = new SimpleDateFormat(JsfUtil.TIME_PATTERN_LONG, rl.getLocale());
            df.setTimeZone(TimeService.getLocalTimeZone());
            sb.append(" ");
            if(meeting.getStartTime() != null) {
                sb.append(df.format(new Date(meeting.getStartTime().getTime())).toLowerCase());
            }

            // End time
            if(meeting.getStartTime() != null &&
                    meeting.getEndTime() != null) {
                sb.append(timeSepChar);
            }

            if(meeting.getEndTime() != null) {
                sb.append(df.format(new Date(meeting.getEndTime().getTime())).toLowerCase());
            }
            if(log.isDebugEnabled()) log.debug("Meeting times = " + sb.toString());
            return sb.toString();
        }

        public String getAbbreviatedDays() {
            String daySepChar = ",";

            StringBuilder sb = new StringBuilder();
            for(Iterator iter = getAbbreviatedDayList().iterator(); iter.hasNext();) {
                String day = (String)iter.next();
                sb.append(day);
                if(iter.hasNext()) {
                    sb.append(daySepChar);
                }
            }
            if(log.isDebugEnabled()) log.debug("Meeting days = " + sb.toString());
            return sb.toString();
        }

        public String getDays() {
            String daySepChar = ",";

            StringBuilder sb = new StringBuilder();
            for(Iterator iter = getDayList().iterator(); iter.hasNext();) {
                String day = (String)iter.next();
                sb.append(day);
                if(iter.hasNext()) {
                    sb.append(daySepChar);
                }
            }
            if(log.isDebugEnabled()) log.debug("Meeting days = " + sb.toString());
            return sb.toString();
        }

        // Meeting delegate methods

        public Time getEndTime() {
            return meeting.getEndTime();
        }

        public String getLocation() {
            return meeting.getLocation();
        }

        public Time getStartTime() {
            return meeting.getStartTime();
        }

        public boolean isFriday() {
            return meeting.isFriday();
        }

        public boolean isMonday() {
            return meeting.isMonday();
        }

        public boolean isSaturday() {
            return meeting.isSaturday();
        }

        public boolean isSunday() {
            return meeting.isSunday();
        }

        public boolean isThursday() {
            return meeting.isThursday();
        }

        public boolean isTuesday() {
            return meeting.isTuesday();
        }

        public boolean isWednesday() {
            return meeting.isWednesday();
        }
    }

    public String getRowGroupId() {
        return section.getCategory();
    }

    public String getRowGroupTitle() {
        return categoryForDisplay;
    }

}

