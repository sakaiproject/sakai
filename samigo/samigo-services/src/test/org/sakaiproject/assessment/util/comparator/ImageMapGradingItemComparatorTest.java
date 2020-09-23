package org.sakaiproject.assessment.util.comparator;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.util.comparator.ImageMapGradingItemComparator;

public class ImageMapGradingItemComparatorTest {

	@Test
	public void nullSafeCompare() {
		ImageMapGradingItemComparator nullSafeComparator = new ImageMapGradingItemComparator();
		ItemGradingData it1 = Mockito.mock(ItemGradingData.class);
		ItemGradingData it2 = Mockito.mock(ItemGradingData.class);
		ItemGradingData it3 = Mockito.mock(ItemGradingData.class);
		when(it1.getItemGradingId()).thenReturn(new Long(10));
		when(it2.getItemGradingId()).thenReturn(new Long(15));
		when(it3.getItemGradingId()).thenReturn(new Long(-10));
		
		assertEquals(0, nullSafeComparator.compare(null, null));
		assertEquals(1,nullSafeComparator.compare(null, it1));
		assertEquals(1,nullSafeComparator.compare(null, it2));
		assertEquals(-1,nullSafeComparator.compare(it1, null));
		assertEquals(-1,nullSafeComparator.compare(it2, null));
		
		assertEquals(0, nullSafeComparator.compare(it1, it1));
		assertEquals(-1, nullSafeComparator.compare(it1, it2));
		assertEquals(1, nullSafeComparator.compare(it2, it1));
		assertEquals(-1, nullSafeComparator.compare(it3, it2));
		assertEquals(1, nullSafeComparator.compare(it2, it3));
	}
}
