/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
 package org.sakaiproject.tool.assessment.util.comparator;

import java.util.Comparator;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.springframework.util.comparator.NullSafeComparator;

/**
 * This sorts ItemGrading items by itemGradingId.
 */

public class ImageMapGradingItemComparator implements Comparator<ItemGradingData> {
	
	@Override
	public int compare(ItemGradingData it1, ItemGradingData it2) {
		  
		  if(it1 == null && it2 == null) return 0;
  		  if(it1 == null) return 1;
  		  if(it2 == null) return -1;  		  
  		  
  		  return NullSafeComparator.NULLS_HIGH.compare(it1.getItemGradingId(), it2.getItemGradingId()); 	  
	}
}
