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
import java.util.Comparator;
import java.util.Map;
import java.text.Collator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.User;

/**
 * Decorates an EnrollmentRecord for display in the UI.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class EnrollmentDecorator implements Serializable {
    private static final long serialVersionUID = 1L;

    protected EnrollmentRecord enrollment;

    // A Map of categoryId -> Enrolled Section
    protected Map categoryToSectionMap;

    public EnrollmentDecorator(EnrollmentRecord enrollment, Map categoryToSectionMap) {
        this.enrollment = enrollment;
        this.categoryToSectionMap = categoryToSectionMap;
    }

    public static final Comparator<EnrollmentDecorator> getNameComparator(final boolean sortAscending) {
        return new Comparator<EnrollmentDecorator>() {
            private final Collator collator = Collator.getInstance();        	
            private int compareUsers(final User u1, final User u2) {
        	    this.collator.setStrength(Collator.PRIMARY);
        	    return new CompareToBuilder()
        		        .append(u1.getSortName(), u2.getSortName(), this.collator)        				
        		        .toComparison();
            }
            public int compare(EnrollmentDecorator enr1, EnrollmentDecorator enr2) {
                int comparison = compareUsers(enr1.getUser(), enr2.getUser());
                return sortAscending ? comparison : (-1 * comparison);
            }
        };
    }

    public static final Comparator<EnrollmentDecorator> getDisplayIdComparator(final boolean sortAscending) {
        return new Comparator<EnrollmentDecorator>() {
            public int compare(EnrollmentDecorator enr1, EnrollmentDecorator enr2) {
                int comparison = enr1.getUser().getDisplayId().compareTo(enr2.getUser().getDisplayId());
                return sortAscending ? comparison : (-1 * comparison);
            }
        };
    }

    public static final Comparator<EnrollmentDecorator> getCategoryComparator(final String categoryId, final boolean sortAscending) {
        if(log.isDebugEnabled()) log.debug("Comparing enrollment decorators by " + categoryId);
        return new Comparator<EnrollmentDecorator>() {
            public int compare(EnrollmentDecorator enr1, EnrollmentDecorator enr2) {
                CourseSection section1 = (CourseSection)enr1.getCategoryToSectionMap().get(categoryId);
                CourseSection section2 = (CourseSection)enr2.getCategoryToSectionMap().get(categoryId);
                if(section1 == null && section2 != null) {
                    return sortAscending ? -1 : 1;
                }
                if(section1 != null && section2 == null) {
                    return sortAscending ? 1 : -1;
                }
                if(section1 == null && section2 == null) {
                    return getNameComparator(sortAscending).compare(enr1, enr2);
                }

                int comparison = 0;
                if(section1.getTitle().equals(section2.getTitle())) {
                    // Use the student name for comparison if the titles are equal
                    comparison = enr1.getUser().getSortName().compareTo(enr2.getUser().getSortName());
                } else {
                    // Use the section title for comparison if the titles are different
                    comparison = section1.getTitle().compareTo(section2.getTitle());
                }
                return sortAscending ? comparison : (-1 * comparison);
            }
        };
    }

    public String getStatus() {
        return enrollment.getStatus();
    }

    public User getUser() {
        return enrollment.getUser();
    }

    public Map getCategoryToSectionMap() {
        return categoryToSectionMap;
    }


    public String getCategory() {
        return null;
    }

    public String getCatgeoryName() {
        return null;
    }


}
