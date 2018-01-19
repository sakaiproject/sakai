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

package org.sakaiproject.service.gradebook.shared;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 *  Provides information describing a gradebook category that may be useful
 *  to consumers of the shared gradebook services.  Not persisted.
 */
public class CategoryDefinition implements Serializable {
  
    private static final long serialVersionUID = 1L;
	
    private Long id;
    private String name;
    private Double weight;
    private Integer dropLowest;
    private Integer dropHighest;
    private Integer keepHighest;
    private Boolean extraCredit;
    private Integer categoryOrder;
    
    public static Comparator<CategoryDefinition> orderComparator;

    private List<Assignment> assignmentList;
    
    public CategoryDefinition() {
    	
    }
    
    public CategoryDefinition(Long id, String name) {
    	this.id = id;
    	this.name = name;
    }
    
    /**
     * 
     * @return the id of the Category object
     */
    public Long getId()
    {
        return id;
    }
    
    /**
     * 
     * @param id the id of the Category object
     */
    public void setId(Long id)
    {
        this.id = id;
    }
    
    /**
     * 
     * @return the category name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * 
     * @param name the category name
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * 
     * @return the weight set for this category if part of a weighted gradebook.
     * null if gradebook is not weighted
     */
    public Double getWeight()
    {
        return weight;
    }
    
    /**
     * the weight set for this category if part of a weighted gradebook.
     * null if gradebook is not weighted
     * @param weight
     */
    public void setWeight(Double weight)
    {
        this.weight = weight;
    }

    /**
     * Get the list of Assignments associated with this category
     * @return 
     */
	public List<Assignment> getAssignmentList() {
		return assignmentList;
	}

	 /**
     * Set the list of Assignments for this category
     * @return 
     */
	public void setAssignmentList(List<Assignment> assignmentList) {
		this.assignmentList = assignmentList;
	}

	public Integer getDropLowest() {
		return dropLowest;
	}

	public void setDropLowest(Integer dropLowest) {
		this.dropLowest = dropLowest;
	}

	public Integer getDropHighest() {
		return dropHighest;
	}

	public void setDropHighest(Integer dropHighest) {
		this.dropHighest = dropHighest;
	}

	public Integer getKeepHighest() {
		return keepHighest;
	}

	public void setKeepHighest(Integer keepHighest) {
		this.keepHighest = keepHighest;
	}

	public Boolean isExtraCredit() {
		return extraCredit;
	}

	public void setExtraCredit(Boolean extraCredit) {
		this.extraCredit = extraCredit;
	}

	public Integer getCategoryOrder() {
		return categoryOrder;
	}

	public void setCategoryOrder(Integer categoryOrder) {
		this.categoryOrder = categoryOrder;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	static {
		orderComparator = new Comparator<CategoryDefinition>() {
			@Override
			public int compare(final CategoryDefinition c1, final CategoryDefinition c2) {
				if (c1.getCategoryOrder() == null && c2.getCategoryOrder() == null) {
					return c1.getName().compareTo(c2.getName());
				}
				if(c1.getCategoryOrder() == null) {
					return -1;
				}
				if(c2.getCategoryOrder() == null) {
					return 1;
				}
				return c1.getCategoryOrder().compareTo(c2.getCategoryOrder());
			}
		};
	}
}
