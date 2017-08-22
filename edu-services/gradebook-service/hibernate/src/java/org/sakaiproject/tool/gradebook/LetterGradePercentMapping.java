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
package org.sakaiproject.tool.gradebook;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LetterGradePercentMapping implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Long id;
	private int version;

	private int mappingType; //value of 1 or 2 - 1 is the default mapping in the system.
	private Long gradebookId;
	private Map<String, Double> gradeMap;

	public LetterGradePercentMapping() 
	{
	}

	public Double getValue(String grade) 
	{
		if(gradeMap != null && gradeMap.containsKey(grade))
		{
			return gradeMap.get(grade);
		}
		return null;
	}
	
	public String getGrade(Double value) 
	{
		if(gradeMap != null)
		{
			List percentList = new ArrayList();
			for(Iterator iter = gradeMap.keySet().iterator(); iter.hasNext();)
			{
				percentList.add(gradeMap.get((iter.next())));
			}
			Collections.sort(percentList);
			for(int i=0; i<percentList.size(); i++)
			{
				Double mappingDouble = (Double)percentList.get(percentList.size() - 1 - i);
				if(mappingDouble.compareTo(value) <= 0)
					return getGradeMapping(mappingDouble);
			}
			
			//return the last grade if double value is less than the minimum value in gradeMapping - "F"
			return getGradeMapping(((Double)percentList.get(percentList.size() - 1)));
		}
		return null;
	}	
	
	/*
	 * this method returns the mapping letter grade value for Double value
	 * according to the exact pair of key-value in gradeMap.
	 */
	private String getGradeMapping(Double value) 
	{
		if(gradeMap != null)
		{
			Iterator iter = gradeMap.keySet().iterator();
			while(iter.hasNext())
			{
				String key = (String) iter.next();
				Double gradeValue = gradeMap.get(key);
				if(gradeValue.equals(value))
				{
					return key;
				}
			}
			return null;
		}
		return null;
	}


	public Long getId() 
	{
		return id;
	}

	public void setId(Long id) 
	{
		this.id = id;
	}

	public int getVersion() 
	{
		return version;
	}

	public void setVersion(int version) 
	{
		this.version = version;
	}

	public Map<String, Double> getGradeMap() 
	{
		return gradeMap;
	}

	public void setGradeMap(Map<String, Double> gradeMap) 
	{
		this.gradeMap = gradeMap;
	}

	public int getMappingType()
	{
		return mappingType;
	}

	public void setMappingType(int mappingType)
	{
		this.mappingType = mappingType;
	}

	public Long getGradebookId()
	{
		return gradebookId;
	}

	public void setGradebookId(Long gradebookId)
	{
		this.gradebookId = gradebookId;
	}
	
	/**
     * Enable any-case input of grades (typically lowercase input
     * for uppercase grades). Look for a case-insensitive match
     * to the input text and if it's found, return the official
     * version.
     *
     * @return The normalized version of the grade, or null if not found.
     */
    public String standardizeInputGrade(String inputGrade) {
    	String standardizedGrade = null;
    	for (Iterator iter = gradeMap.keySet().iterator(); iter.hasNext(); ) {
    		String grade = (String)iter.next();
    		if (grade.equalsIgnoreCase(inputGrade)) {
    			standardizedGrade = grade;
    			break;
    		}
    	}
    	return standardizedGrade;
    }
}