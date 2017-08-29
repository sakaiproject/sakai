/**
 * Copyright (c) 2003-2013 The Apereo Foundation
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
package org.sakaiproject.tool.messageforums;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sakaiproject.api.app.messageforums.SynopticMsgcntrItem;

public class SynopticSiteSemesterComparator implements Comparator<SynopticMsgcntrItem> {

	
	//this sorts the list in two groups: Course sites then Non-Course Sites
	//	-A course site lists higher than a non-course site
	//	-Non course sites are ordered alphabetically
	// 	-Course sites are ordered by year, then semester, then alphabetically
	
	private static final String SPRING = "SP", SUMMER = "SU", FALL = "FA";
	private static final Pattern COURSE_SITE_PATTERN_MATCH =
	    Pattern.compile(
	    		"^(\\D{2})" +  	//Semester (spring, summer, fall)
	    		"(\\d{2})" +	//year
	    		"((\\s\\w+){4})$"); 	//rest of the title for a course site
	
	public SynopticSiteSemesterComparator(){}
	
	public int compare(SynopticMsgcntrItem o1, SynopticMsgcntrItem o2) {
		final Matcher o1Matcher = COURSE_SITE_PATTERN_MATCH.matcher(o1.getSiteTitle());
		final Matcher o2Matcher = COURSE_SITE_PATTERN_MATCH.matcher(o2.getSiteTitle());
		
		if(o1Matcher.matches() && o2Matcher.matches()){
			//both are course sites
			
			//check year:
			String o1Year = o1Matcher.group(2);
			String o2Year = o2Matcher.group(2);
			
			if(o1Year.equals(o2Year)){
				//years are equal, so check semesters
				String o1Semester = o1Matcher.group(1);
				String o2Semester = o2Matcher.group(1);
				
				if(o1Semester.equals(o2Semester)){		
					//years and semesters are equal, so compare titles
					return o1.getSiteTitle().compareToIgnoreCase(o2.getSiteTitle());						
				}else{
					//to lessen the number of compares, 
					//make summer the default since summer is the smallest case
					//SPRING = 0
					//SUMMER = 1
					//FALL = 2
					int o1SemesterRank = SPRING.equals(o1Semester) ? 0 : FALL.equals(o1Semester) ? 2 : 1;
					int o2SemesterRank = SPRING.equals(o2Semester) ? 0 : FALL.equals(o2Semester) ? 2 : 1;
					
					return (o1SemesterRank < o2SemesterRank) ? 1 : -1;
				}
			}						
			else
				return o1Year.compareTo(o2Year) * -1;
		}else if(o1Matcher.matches()){
			//o1 is a course site and o2 is not
			return -1;
		}else if(o2Matcher.matches()){
			//o2 is a course site and o1 is not
			return 1;
		}else{
			//both are not course sites
			return o1.getSiteTitle().compareToIgnoreCase(o2.getSiteTitle());
		}
	}
}
