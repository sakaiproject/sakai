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
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 *  Provides information describing a gradebook category that may be useful
 *  to consumers of the shared gradebook services.  Not persisted.
 */
public class CategoryDefinition implements Serializable {
  
    private static final long serialVersionUID = 1L;
	
    @Getter @Setter private Long id;
    @Getter @Setter private String name;
    @Getter @Setter private Double weight;
    @Getter @Setter private Integer dropLowest;
    @Getter @Setter private Integer dropHighest;
    @Getter @Setter private Integer keepHighest;
    @Getter @Setter private Boolean extraCredit;
    @Getter @Setter private Boolean equalWeight;
    @Getter @Setter private Integer categoryOrder;
    @Getter @Setter private Boolean dropKeepEnabled;
    
    public static Comparator<CategoryDefinition> orderComparator;

    @Getter @Setter private List<Assignment> assignmentList;
    
    public CategoryDefinition() {
    	
    }
    
    public CategoryDefinition(Long id, String name) {
    	this.id = id;
    	this.name = name;
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

	/**
	 * Helper method to get the total points associated with a category
	 *
	 * @return the sum of all assignment totals associated with this category
	 */
	public Double getTotalPoints() {
		BigDecimal totalPoints = new BigDecimal(0);

		if (getAssignmentList() != null) {
			for (final Assignment assignment : getAssignmentList()) {
				totalPoints = totalPoints.add(BigDecimal.valueOf(assignment.getPoints()));
			}
		}
		return totalPoints.doubleValue();
	}

	public boolean isAssignmentInThisCategory(String assignmentId) {
		for (Assignment thisAssignment : assignmentList) {
			if (thisAssignment.getExternalId() == null) {
				continue;
			}

			if (thisAssignment.getExternalId().equalsIgnoreCase(assignmentId)) {
				return true;
			}
		}

		return false;
	}

	public Double getPointsForCategory() {
		if (!dropKeepEnabled) {
			return null;
		}

		for (Assignment thisAssignment : assignmentList) {
			return thisAssignment.getPoints();
		}

		return null;
	}
}
