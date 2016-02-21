/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
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

package org.sakaiproject.tool.gradebook;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A GradableObject is a component of a Gradebook for which students can be
 * assigned a GradeRecord.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public abstract class GradableObject implements Serializable {
    protected static final Log log = LogFactory.getLog(GradableObject.class);

    protected Long id;
    protected int version;
    protected Gradebook gradebook;
    protected String name;
    protected Integer sortOrder;
    protected Integer categorizedSortOrder;

    protected Double mean;	// not persisted; not used in all contexts (in Overview & Assignment Grading,
    	                    // not in Roster or Student View)

    protected boolean removed;  // We had trouble with foreign key constraints in the UCB pilot when
                                // instructors "emptied" all scores for an assignment and then tried to
                                // delete the assignment.  Instead, we should hide the "removed" assignments
                                // from the app by filtering the removed assignments in the hibernate queries

    public static Comparator<GradableObject> defaultComparator;
    public static Comparator<GradableObject> sortingComparator;
    public static Comparator<Assignment> dateComparator;
    public static Comparator<GradableObject> meanComparator;
    public static Comparator<GradableObject> nameComparator;
    public static Comparator<GradableObject> idComparator;
    public static Comparator<Assignment> categoryComparator;
    static {
        categoryComparator = new Comparator<Assignment>() {
            @SuppressWarnings("unchecked")
            public int compare(Assignment one, Assignment two) {
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
            public int compare(GradableObject one, GradableObject two) {
                if (one.getId() == null && two.getId() == null) {
                    return 0;
                } else if (one.getName() == null) {
                    return 1;
                } else if (two.getName() == null) {
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
            public int compare(GradableObject one, GradableObject two) {
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
            public int compare(GradableObject one, GradableObject two) {
                if (one.getMean() == null && two.getMean() == null) {
                    return nameComparator.compare(one, two);
                } else if (one.getMean() == null) {
                    return 1;
                } else if (two.getMean() == null) {
                    return -1;
                } else {
                    return one.getMean().compareTo(two.getMean());
                }
            }
            @Override
            public String toString() {
                return "GradableObject.meanComparator";
            }
        };
        dateComparator = new Comparator<Assignment>() {
            public int compare(Assignment one, Assignment two) {
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
            public int compare(GradableObject one, GradableObject two) {
                if (one.getSortOrder() == null && two.getSortOrder() == null) {
                    if (one.getClass().equals(two.getClass()) 
                            && one.getClass().isAssignableFrom(Assignment.class) ) {
                        // special handling for assignments
                        return dateComparator.compare((Assignment)one, (Assignment)two);
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

	/**
	 * @return Returns the id.
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(Long id) {
		this.id = id;
	}
    /**
     * @return Returns the gradebook.
     */
    public Gradebook getGradebook() {
        return gradebook;
    }
    /**
     * @param gradebook The gradebook to set.
     */
    public void setGradebook(Gradebook gradebook) {
        this.gradebook = gradebook;
    }

    /**
     * @return Returns the mean.
     */
    public Double getMean() {
        return mean;
    }

    /**
	 * @return Returns the mean while protecting against displaying NaN.
	 */
	public Double getFormattedMean() {
        if(mean == null || mean.equals(Double.valueOf(Double.NaN))) {
        	return null;
        } else {
            return Double.valueOf(mean.doubleValue() / 100.0);
        }
	}

    /**
	 * @param mean The mean to set.
	 */
	public void setMean(Double mean) {
		this.mean = mean;
	}
	
	/**
	 * This should really only be a field in Assignment objects, since
	 * the string describing CourseGrade needs to allow for localization.
	 * Unfortunately, such we keep CourseGrade and Assignment objects in
	 * the same table, and since we want Assignment names to be enforced
	 * as non-nullable, we're stuck with a bogus CourseGrade "name" field
	 * for now. The UI will have to be smart enough to disregard it.
	 * 
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return Returns the version.
	 */
	public int getVersion() {
		return version;
	}
	/**
	 * @param version The version to set.
	 */
	public void setVersion(int version) {
		this.version = version;
	}
    /**
     * @return Returns the removed.
     */
    public boolean isRemoved() {
        return removed;
    }
    /**
     * @param removed The removed to set.
     */
    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("name", name)
            .append("sort", sortOrder)
            .toString();
    }

    public boolean equals(Object other) {
        if (!(other instanceof GradableObject)) {
        	return false;
        }
        GradableObject go = (GradableObject)other;
        return new EqualsBuilder()
            .append(gradebook, go.getGradebook())
            .append(id, go.getId())
            .append(name, go.getName()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
          .append(gradebook)
          .append(id)
          .append(name)
          .toHashCode();
	}



    private int sortTotalItems = 1;
    private int sortTruePosition = -1;
    public void assignSorting(int sortTotalItems, int sortTruePosition) {
        // this will help correctly figure out the first/last setting and sorting
        this.sortTotalItems = sortTotalItems;
        this.sortTruePosition = sortTruePosition;
    }

    public boolean isFirst() {
        return sortTruePosition == 0;
    }

    public boolean isLast() {
        return sortTruePosition >= (sortTotalItems - 1);
    }

    public int getSortPosition() {
        return sortTruePosition;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getCategorizedSortOrder() {
        return categorizedSortOrder;
    }

    public void setCategorizedSortOrder(Integer value) {
        this.categorizedSortOrder = value;
    }

}



