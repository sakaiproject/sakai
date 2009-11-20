/**********************************************************************************
*
* $Id: GradeDefinition.java 59674 2009-04-03 23:05:58Z arwhyte@umich.edu $
*
***********************************************************************************
*
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.service.gradebook.shared;


/**
 *  Provides information describing a gradebook category that may be useful
 *  to consumers of the shared gradebook services.  Not persisted.
 */
public class CategoryDefinition {
    private Long id;
    private String name;
    private Double weight;
    
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
}
