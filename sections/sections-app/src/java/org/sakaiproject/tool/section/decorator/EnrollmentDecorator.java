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
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.User;

import lombok.extern.slf4j.Slf4j;

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

    private static Collator createCollator(Locale locale) {
        Collator fallback = Collator.getInstance(locale);
        fallback.setStrength(Collator.TERTIARY);
        try {
            Collator base = Collator.getInstance(locale);
            if (!(base instanceof RuleBasedCollator)) {
                return fallback;
            }
            Collator c = new RuleBasedCollator(((RuleBasedCollator) base).getRules().replaceAll("<'_'", "<' '<'_'"));
            c.setStrength(Collator.TERTIARY);
            return c;
        } catch (ParseException e) {
            log.warn("Failed to create RuleBasedCollator for EnrollmentDecorator", e);
            return fallback;
        }
    }

    public static final Comparator<EnrollmentDecorator> getNameComparator(final boolean sortAscending, final Locale locale) {
        return new Comparator<EnrollmentDecorator>() {
            private final Collator collator = createCollator(locale);
            public int compare(EnrollmentDecorator enr1, EnrollmentDecorator enr2) {
                int comparison = new CompareToBuilder()
                        .append(enr1.getUser().getSortName(), enr2.getUser().getSortName(), this.collator)
                        .toComparison();
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

    public static final Comparator<EnrollmentDecorator> getCategoryComparator(final String categoryId, final boolean sortAscending, final Locale locale) {
        log.debug("Comparing enrollment decorators by {}", categoryId);
        final Collator collator = createCollator(locale);
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
                int comparison;
                if(section1 == null && section2 == null) {
                    comparison = collator.compare(enr1.getUser().getSortName(), enr2.getUser().getSortName());
                } else {
                    int titleComparison = collator.compare(section1.getTitle(), section2.getTitle());
                    if (titleComparison == 0) {
                        comparison = collator.compare(enr1.getUser().getSortName(), enr2.getUser().getSortName());
                    } else {
                        comparison = titleComparison;
                    }
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
