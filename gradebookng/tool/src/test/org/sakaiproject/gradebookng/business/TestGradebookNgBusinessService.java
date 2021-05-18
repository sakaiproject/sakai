/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.gradebookng.business;

import java.text.NumberFormat;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.gradebookng.business.util.FormatHelper;

public class TestGradebookNgBusinessService {

	@InjectMocks
	GradebookNgBusinessService service;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void injectionOk() {
		Assert.assertNotNull(service);
	}
	
	@Test
	public void testCourseGradeRoundingUp() {
		double d = 89.4455D;
		String s = "89.4455";

		double de = 89.45;
		NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
		String roundedExcepted = nf.format(de);

		String rounded = FormatHelper.formatGradeForDisplay(d);
		Assert.assertEquals(rounded, roundedExcepted);

		rounded = FormatHelper.formatGradeForDisplay(s);
		Assert.assertEquals(rounded, roundedExcepted);

		rounded = FormatHelper.formatStringAsPercentage(s);
		Assert.assertEquals(rounded, roundedExcepted+"%");
	}
	
	@Test
	public void testCourseGradeRoundingDown() {
		double d = 89.4449D;
		String s = "89.4449";

		double de = 89.44;
		NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
		String roundedExcepted = nf.format(de);

		String rounded = FormatHelper.formatGradeForDisplay(d);
		Assert.assertEquals(rounded, roundedExcepted);

		rounded = FormatHelper.formatGradeForDisplay(s);
		Assert.assertEquals(rounded, roundedExcepted);

		rounded = FormatHelper.formatStringAsPercentage(s);
		Assert.assertEquals(rounded, roundedExcepted+"%");
	}

	@Test
	public void testDropTrailingZero() {
		double d = 89.0000D;
		String s = "89.000000";

		String rounded = FormatHelper.formatGradeForDisplay(d);
		Assert.assertEquals(rounded, "89");

		rounded = FormatHelper.formatGradeForDisplay(s);
		Assert.assertEquals(rounded, "89");

		rounded = FormatHelper.formatStringAsPercentage(s);
		Assert.assertEquals(rounded, "89%");
	}

}
