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

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.SubnetUtils;

/**
 * <p>
 * IPAddrUtil contains utility methods for working with IP addresses.
 * </p>
 */
@Slf4j
public class IPAddrUtil
{
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
		log.info("Checking login IP '" + addr + "' is contained in whitelist '" + addrlist + "'");

		// TODO Support IPv6

		if (StringUtils.isBlank(addrlist) || StringUtils.isBlank(addr))
			return false;

		boolean match = false;

		for (String netaddr : Arrays.asList(addrlist.split(","))) {
			if (netaddr.contains("/")) {
				// Contained in subnet?
				try {
					SubnetUtils.SubnetInfo subnet = new SubnetUtils(netaddr.trim()).getInfo();
					if (subnet.isInRange(addr)) {
						log.debug("IP Address " + addr + " is in network range " + subnet.getCidrSignature());
						match = true;
						break;
					}
				} catch (IllegalArgumentException e) {
					log.warn("IP network address '" + netaddr + "' is not a valid CIDR format");
				}
			} else {
				// Exact match?
				if (netaddr.trim().equals(addr)) {
					match = true;
					break;
				}
			}
		}
		return match;
	}
}
