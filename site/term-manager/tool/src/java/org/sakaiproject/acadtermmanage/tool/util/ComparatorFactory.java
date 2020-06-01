/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.acadtermmanage.tool.util;

import java.util.Comparator;
import java.util.Date;

import org.sakaiproject.acadtermmanage.model.Semester;
import org.sakaiproject.acadtermmanage.tool.AcademicTermConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * Creates Comparators for specific properties of Semester objects 
 *
 */
@Slf4j
public class ComparatorFactory implements AcademicTermConstants {
	 
			
	public static final <T extends Comparable<T>> int compare(T o1,T o2) {
		if (o1.equals(o2)) { // catches both objects being null - among other things
			return 0;
		}
		else if (o1 != null) {
			if (o2 != null) { // java.util.Date throws a NullPointer when doing "date.compareTo(null)" :-(
				return o1.compareTo(o2);
			}
			else {
				return 1; // o1!=null && o2==null
			}
		}
		else if (o2 != null) { // o1==null && o2!=null
			return -1;
		}
		else { // can't happen (famous last words)
			log.debug("how interesting; two null values which haven't been equal...");
			return 0;
		}
	}

	
	
	private static Comparator<Semester> createEIdComparator(){
		return new Comparator<Semester>() {
			@Override
			public int compare(Semester o1, Semester o2) {
				String e1 = o1 != null?o1.getEid():null;
				String e2 = o2 != null?o2.getEid():null;
				return ComparatorFactory.compare(e1, e2);
				
			}				
		};
	}
	
	
	
public static Comparator<Semester> createComparator(String propertyName) {
		
		if (PROP_EID.equals(propertyName)) {
			return createEIdComparator();
		}
		else if (PROP_START.equals(propertyName)){
			return new Comparator<Semester>() {
				@Override
				public int compare(Semester o1, Semester o2) {				
					Date d1 = o1!=null?o1.getStartDate():null;
					Date d2 = o2!=null?o2.getStartDate():null;
					return ComparatorFactory.compare(d1, d2);
					
				}				
			};
		}
		else if (PROP_END.equals(propertyName)){
			return new Comparator<Semester>() {
				@Override
				public int compare(Semester o1, Semester o2) {				
					Date d1 = o1!=null?o1.getEndDate():null;
					Date d2 = o2!=null?o2.getEndDate():null;
					return ComparatorFactory.compare(d1, d2);					
				}				
			};
		}
		else if (PROP_TITLE.equals(propertyName)){
			return new Comparator<Semester>() {
				@Override
				public int compare(Semester o1, Semester o2) {				
					String d1 = o1!=null?o1.getTitle():null;
					String d2 = o2!=null?o2.getTitle():null;
					return ComparatorFactory.compare(d1, d2);					
				}				
			};
		}
		else if (PROP_DESC.equals(propertyName)) {
			return new Comparator<Semester>() {
				@Override
				public int compare(Semester o1, Semester o2) {				
					String d1 = o1!=null?o1.getDescription():null;
					String d2 = o2!=null?o2.getDescription():null;
					return ComparatorFactory.compare(d1, d2);					
				}
			};
		}
		else if (PROP_CURRENT.equals(propertyName)) {
			return new Comparator<Semester>() {
				@Override
				public int compare(Semester o1, Semester o2) {				
					Boolean d1 = o1!=null?o1.isCurrent():false;
					Boolean d2 = o2!=null?o2.isCurrent():false;
					return ComparatorFactory.compare(d1, d2);															
				}
			};			
		}
		else {
			log.warn("unknown property selected for sort comparison: \"{}\"; using EID instead", propertyName);
			return createEIdComparator();
		}
	}
	
	
}
