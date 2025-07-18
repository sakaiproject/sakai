/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.grading.api.model;

import java.io.Serializable;
import java.util.Comparator;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// TODO: Check this against SAK-46484. I cut this code before that patch.

@Entity
@Table(name = "GB_GRADABLE_OBJECT_T", indexes = {
    @Index(name = "GB_GRADABLE_OBJ_GB_IDX", columnList = "GRADEBOOK_ID"),
    @Index(name = "GB_GRADABLE_OBJ_ASN_IDX", columnList = "OBJECT_TYPE_ID, GRADEBOOK_ID, NAME, REMOVED"),
    @Index(name = "GB_GRADABLE_OBJ_CT_IDX", columnList = "CATEGORY_ID")
})
@DiscriminatorColumn(name = "OBJECT_TYPE_ID", discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("0")
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter @Setter
public abstract class GradableObject implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "gb_gradable_object_id_sequence")
    @SequenceGenerator(name = "gb_gradable_object_id_sequence", sequenceName = "GB_GRADABLE_OBJECT_S")
    @EqualsAndHashCode.Include
    @ToString.Include
    protected Long id;

    @Column(name = "VERSION", nullable = false)
    protected Integer version = 0;

    @ManyToOne
    @JoinColumn(name = "GRADEBOOK_ID", nullable = false)
    @EqualsAndHashCode.Include
    protected Gradebook gradebook;

    @Column(name = "NAME", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    protected String name;

    @Column(name = "REFERENCE")
    @ToString.Include(name = "reference")
    protected String reference;

    @Column(name = "SORT_ORDER")
    @ToString.Include(name = "sort")
    protected Integer sortOrder;

    @Column(name = "CATEGORIZED_SORT_ORDER")
    protected Integer categorizedSortOrder;

    @Column(name = "REMOVED")
    protected Boolean removed = Boolean.FALSE;

    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID")
    protected Category category;

    @Transient
    protected Double mean;

    public static Comparator<GradableObject> defaultComparator;
    public static Comparator<GradableObject> sortingComparator;
    public static Comparator<GradebookAssignment> dateComparator;
    public static Comparator<GradableObject> meanComparator;
    public static Comparator<GradableObject> nameComparator;
    public static Comparator<GradableObject> idComparator;
    public static Comparator<GradebookAssignment> categoryComparator;
    static {
        categoryComparator = new Comparator<GradebookAssignment>() {
            @Override
            @SuppressWarnings("unchecked")
            public int compare(final GradebookAssignment one, final GradebookAssignment two) {
                if (one.getCategory() == null && two.getCategory() == null) {
                    return 0;
                } else if (one.getCategory() == null) {
                    return 1; // no cats to the end
                } else if (two.getCategory() == null) {
                    return -1; // no cats to the end
                } else {
                    // compare the category names the same way as the normal comparator
                    return Category.nameComparator.compare(one.getCategory(), two.getCategory());
                }
            }

            @Override
            public String toString() {
                return "GradableObject.categoryComparator";
            }
        };
        idComparator = new Comparator<GradableObject>() {
            @Override
            public int compare(final GradableObject one, final GradableObject two) {
                if (one.getId() == null && two.getId() == null) {
                    return 0;
                } else if (one.getId() == null) {
                    return 1;
                } else if (two.getId() == null) {
                    return -1;
                } else {
                    return one.getId().compareTo(two.getId());
                }
            }

            @Override
            public String toString() {
                return "GradableObject.idComparator";
            }
        };
        nameComparator = new Comparator<GradableObject>() {
            @Override
            public int compare(final GradableObject one, final GradableObject two) {
                if (one.getName() == null && two.getName() == null) {
                    return idComparator.compare(one, two);
                } else if (one.getName() == null) {
                    return 1;
                } else if (two.getName() == null) {
                    return -1;
                } else {
                    return one.getName().toLowerCase().compareTo(two.getName().toLowerCase());
                }
            }

            @Override
            public String toString() {
                return "GradableObject.nameComparator";
            }
        };
        meanComparator = new Comparator<GradableObject>() {
            @Override
            public int compare(final GradableObject one, final GradableObject two) {
                if (one.getMean() == null && two.getMean() == null) {
                    return nameComparator.compare(one, two);
                } else if (one.getMean() == null) {
                    return 1;
                } else if (two.getMean() == null) {
                    return -1;
                } else {
                    // Handle NaN values to ensure consistent comparator contract
                    Double meanOne = one.getMean();
                    Double meanTwo = two.getMean();
                    if (meanOne.isNaN() && meanTwo.isNaN()) {
                        return nameComparator.compare(one, two);
                    } else if (meanOne.isNaN()) {
                        return 1;
                    } else if (meanTwo.isNaN()) {
                        return -1;
                    } else {
                        return meanOne.compareTo(meanTwo);
                    }
                }
            }

            @Override
            public String toString() {
                return "GradableObject.meanComparator";
            }
        };
        dateComparator = new Comparator<GradebookAssignment>() {
            @Override
            public int compare(final GradebookAssignment one, final GradebookAssignment two) {
                if (one.getDueDate() == null && two.getDueDate() == null) {
                    return nameComparator.compare(one, two);
                } else if (one.getDueDate() == null) {
                    return 1;
                } else if (two.getDueDate() == null) {
                    return -1;
                } else {
                    return one.getDueDate().compareTo(two.getDueDate());
                }
            }

            @Override
            public String toString() {
                return "GradableObject.dateComparator";
            }
        };
        sortingComparator = new Comparator<GradableObject>() {
            @Override
            public int compare(final GradableObject one, final GradableObject two) {
                if (one.getSortOrder() == null && two.getSortOrder() == null) {
                    if (one.getClass().equals(two.getClass())
                            && one.getClass().isAssignableFrom(GradebookAssignment.class)) {
                        // special handling for assignments
                        return dateComparator.compare((GradebookAssignment) one, (GradebookAssignment) two);
                    } else {
                        return nameComparator.compare(one, two);
                    }
                } else if (one.getSortOrder() == null) {
                    return 1;
                } else if (two.getSortOrder() == null) {
                    return -1;
                } else {
                    return one.getSortOrder().compareTo(two.getSortOrder());
                }
            }

            @Override
            public String toString() {
                return "GradableObject.sortingComparator";
            }
        };
        defaultComparator = sortingComparator;
    }

    /**
     * @return Returns the mean while protecting against displaying NaN.
     */
    public Double getFormattedMean() {

        if (this.mean == null || this.mean.equals(Double.valueOf(Double.NaN))) {
            return null;
        } else {
            return Double.valueOf(this.mean.doubleValue() / 100.0);
        }
    }

    /**
     * @return Whether this gradable object is a course grade
     */
    public abstract boolean isCourseGrade();

    /**
     * @return Whether this gradable object is an assignment
     */
    public abstract boolean isAssignment();

    /**
     * @return Whether this gradable object is a category
     */
    public abstract boolean getIsCategory();

    @Transient
    private int sortTotalItems = 1;
    @Transient
    private int sortTruePosition = -1;

    public void assignSorting(int sortTotalItems, int sortTruePosition) {

        // this will help correctly figure out the first/last setting and sorting
        this.sortTotalItems = sortTotalItems;
        this.sortTruePosition = sortTruePosition;
    }

    public boolean isFirst() {
        return this.sortTruePosition == 0;
    }

    public boolean isLast() {
        return this.sortTruePosition >= (this.sortTotalItems - 1);
    }
}
