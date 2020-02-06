/**
 * Copyright 2013 Apereo Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.sakaiproject.gradebook.entity;

import java.util.List;
import java.util.Vector;



/**
 * This class hold the category information of the Grade book 
 * @author pushyami
 *
 */

public class Category {
	public String name;
	public Double weight;
	public Integer dropLowest;
	public Integer dropHighest;
	public Integer keepHighest;
	public List<SparseGradebookItem> assignmentsInCategory=new Vector<SparseGradebookItem>();
	
	public Category(String name, Double weight,Integer dropLowest, Integer dropHighest,Integer keepHighest, List<SparseGradebookItem> assignmentsInCategory) {
		this.name=name;
		this.weight=weight;
		this.dropLowest=dropLowest;
		this.dropHighest=dropHighest;
		this.keepHighest=keepHighest;
		this.assignmentsInCategory=assignmentsInCategory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
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

	public List<SparseGradebookItem> getAssignmentsInCategory() {
		return assignmentsInCategory;
	}

	public void setAssignmentsInCategory(List<SparseGradebookItem> assignmentsInCategory) {
		this.assignmentsInCategory = assignmentsInCategory;
	}

}
