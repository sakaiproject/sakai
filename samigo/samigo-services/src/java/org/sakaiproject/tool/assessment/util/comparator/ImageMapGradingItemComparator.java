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
