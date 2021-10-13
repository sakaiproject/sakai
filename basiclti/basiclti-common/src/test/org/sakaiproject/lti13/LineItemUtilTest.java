/**
 * Copyright (c) 2009-2014 The Apereo Foundation
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
package org.sakaiproject.lti13;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import org.sakaiproject.lti13.util.SakaiLineItem;
import org.sakaiproject.lti13.LineItemUtil;


public class LineItemUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testOne() {
		String fakeDeepLink = "{\"icon_url\":\"http:\\/\\/localhost:8888\\/tsugi-static\\/fontawesome-free-5.8.2-web\\/png\\/trophy.png\",\"fa_icon\":\"fa-trophy\",\"thumbnail\":{\"width\":90,\"url\":\"https:\\/\\/lti.example.com\\/thumb.jpg\",\"height\":90},\"lineItem\":{\"scoreMaximum\":\"70\",\"label\":\"Trophy70\"},\"custom\":{\"submissionEnd\":\"$ResourceLink.submission.endDateTime\",\"availableStart\":\"$ResourceLink.available.startDateTime\",\"availableEnd\":\"$ResourceLink.available.endDateTime\",\"canvas_caliper_url\":\"$Caliper.url\",\"submissionStart\":\"$ResourceLink.submission.startDateTime\"},\"icon\":{\"fa_icon\":\"fa-magic\",\"width\":100,\"url\":\"https:\\/\\/lti.example.com\\/image.jpg\",\"height\":100},\"type\":\"ltiResourceLink\",\"title\":\"Trophy70\",\"url\":\"http:\\/\\/localhost:8888\\/py4e\\/mod\\/trophy\\/\"}";
		SakaiLineItem li = LineItemUtil.extractLineItem(fakeDeepLink);
		assertEquals(li.scoreMaximum, new Double(70));

		String fakeContentItem = "{\"@type\":\"LtiLinkItem\",\"@id\":\":item1\",\"title\":\"Trophy\",\"mediaType\":\"application\\/vnd.ims.lti.v1.ltilink\",\"text\":\"This tool gives students a grade of 0.95 for a launch.\\r\\n\",\"url\":\"http:\\/\\/localhost:8888\\/py4e\\/mod\\/trophy\\/\",\"placementAdvice\":{\"presentationDocumentTarget\":\"iframe\"},\"icon\":{\"@id\":\"http:\\/\\/localhost:8888\\/tsugi-static\\/fontawesome-free-5.8.2-web\\/png\\/trophy.png\",\"fa_icon\":\"fa-trophy\",\"width\":64,\"height\":64},\"lineItem\":{\"@type\":\"LineItem\",\"label\":\"Trophy\",\"reportingMethod\":\"res:totalScore\",\"assignedActivity\":{\"@id\":\"http:\\/\\/toolprovider.example.com\\/assessment\\/66400\",\"activityId\":\"a-9334df-33\"},\"scoreConstraints\":{\"@type\":\"NumericLimits\",\"normalMaximum\":\"8675\"}},\"custom\":{\"availableStart\":\"$ResourceLink.available.startDateTime\",\"availableEnd\":\"$ResourceLink.available.endDateTime\",\"submissionStart\":\"$ResourceLink.submission.startDateTime\",\"submissionEnd\":\"$ResourceLink.submission.endDateTime\",\"canvas_caliper_url\":\"$Caliper.url\"}}";

		li = LineItemUtil.extractLineItem(fakeContentItem);
		assertEquals(li.scoreMaximum, new Double(8675));
	}

	@Test
	public void testTwo() {
		SakaiLineItem li = LineItemUtil.extractLineItem(null);
		assertNull(li);
		li = LineItemUtil.extractLineItem("");
		assertNull(li);

		String fakeLineItem = "{ \"id\" : \"https://lms.example.com/context/2923/lineitems/1\", \"scoreMaximum\" : 60, \"label\" : \"Chapter 5 Test\", \"resourceId\" : \"a-9334df-33\", \"tag\" : \"grade\", \"resourceLinkId\" : \"1g3k4dlk49fk\", \"startDateTime\": \"2018-03-06T20:05:02Z\", \"endDateTime\": \"2018-04-06T22:05:03Z\", \"submissionReview\": { \"reviewableStatus\": [\"InProgress\", \"Submitted\", \"Completed\"], \"label\": \"Open My Tool Viewer\", \"url\": \"https://platform.example.com/act/849023/sub\", \"custom\": { \"action\": \"review\", \"a_id\": \"23942\" } } }";
		li = LineItemUtil.parseLineItem(fakeLineItem);
		assertNotNull(li);
		assertNotNull(li.submissionReview);
		assertNotNull(li.submissionReview.url);
		assertEquals(li.submissionReview.url, "https://platform.example.com/act/849023/sub");
	}

}
