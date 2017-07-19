/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2017 Apereo Foundation
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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * IPAddrUtil contains utility methods for working with IP addresses.
 * </p>
 */
public class IPAddrUtil
{
	private static final Logger log = LoggerFactory.getLogger(IPAddrUtil.class);

	/**
	 * Match an address against a list of IP CIDR addresses
	 * 
	 * @param addrlist
	 *        The comma-separated list of addresses
	 * @param addr
	 *        The IP address to match
	 * @return true if address is contained in one or more of the CIDR network blocks listed in addrlist, false if not
	 */
	public static boolean matchIPList(String addrlist, String addr)
	{
		log.info("Matching IP '" + addr + "' to whitelist '" + addrlist + "'");

		// TODO Support IPv6

		if (StringUtils.isBlank(addrlist) || StringUtils.isBlank(addr))
			return false;

		boolean match = false;

		List<String> subnetMasks = Arrays.asList(addrlist.split(","));

		for (String subnetMask : subnetMasks) {
			if (!subnetMask.contains("/") && subnetMask.equals(addr)) {
				// Exact match
				match = true;
				break;
			} else {
				// Subnet
				try {
					SubnetUtils.SubnetInfo subnet = new SubnetUtils(subnetMask).getInfo();
					log.info("Checking IP " + addr + " to subnet " + subnet.getCidrSignature());
					if (subnet.isInRange(addr)) {
						log.info("IP Address " + addr + " is in range " + subnet.getCidrSignature());
						match = true;
						break;
					}
				} catch (IllegalArgumentException e) {
					log.warn("IP Address " + addr + " or mask " + subnetMask + " is not a valid IP address format");
				}
			}
		}

		return match;
	}

}
