/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.sakaiproject.util.IPAddrUtil;


/**
 * Testing the IPAddrUtil
 */
public class IPAddrUtilTest {

    /**
     * Test method for {@link org.sakaiproject.content.util.IPAddrUtil#matchIPList()}.
     * 
     */
    @Test
    public void testMatchIPList() {

	String privateRanges = "10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16 , 198.51.100.0/24, 127.0.0.0/8";

	// null or empty list never matches
	Assert.assertFalse(IPAddrUtil.matchIPList("", "1.2.3.4"));
	Assert.assertFalse(IPAddrUtil.matchIPList(null, "1.2.3.4"));

	// Inside the range
	Assert.assertTrue(IPAddrUtil.matchIPList(privateRanges, "10.0.3.1"));
	Assert.assertTrue(IPAddrUtil.matchIPList(privateRanges, "172.25.3.250"));
	Assert.assertTrue(IPAddrUtil.matchIPList(privateRanges, "192.168.4.10"));
	Assert.assertTrue(IPAddrUtil.matchIPList(privateRanges, "127.0.0.1"));

	// Outside the range
	Assert.assertFalse(IPAddrUtil.matchIPList(privateRanges, "11.0.3.1"));
	Assert.assertFalse(IPAddrUtil.matchIPList(privateRanges, "172.32.0.0"));
	Assert.assertFalse(IPAddrUtil.matchIPList(privateRanges, "192.169.0.1"));
	Assert.assertFalse(IPAddrUtil.matchIPList(privateRanges, "128.3.2.1"));

	// Invalid address format
	Assert.assertFalse(IPAddrUtil.matchIPList(privateRanges, "301.3.2.1"));
	Assert.assertFalse(IPAddrUtil.matchIPList(privateRanges, "10.0.3"));
	Assert.assertFalse(IPAddrUtil.matchIPList(privateRanges, "address"));

        // Invalid format inside the list
	Assert.assertTrue(IPAddrUtil.matchIPList("10.0.0.0/8,address,127.0.0.0/8", "10.0.0.1"));
	Assert.assertFalse(IPAddrUtil.matchIPList("10.0.0.0:8,address,127.0.0.0/8", "10.0.0.1"));

        // Single address
	Assert.assertTrue(IPAddrUtil.matchIPList("10.0.0.33,address,127.0.0.0/8", "10.0.0.33"));
	Assert.assertFalse(IPAddrUtil.matchIPList("10.0.0.33,address,127.0.0.0/8", "10.0.0.32"));

    }

}
