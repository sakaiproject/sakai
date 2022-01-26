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
