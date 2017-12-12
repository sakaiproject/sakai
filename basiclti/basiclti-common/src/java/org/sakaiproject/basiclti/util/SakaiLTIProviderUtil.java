/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2016- Charles R. Severance
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.basiclti.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.tsugi.basiclti.BasicLTIUtil;

import org.sakaiproject.basiclti.util.SakaiBLTIUtil;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * Some Sakai Utility code for the Sakai LTI Provider
 */
@SuppressWarnings("deprecation")
@Slf4j
public class SakaiLTIProviderUtil {

	public static String getProviderLaunchUrl(String toolRegistration) 
	{
                return SakaiBLTIUtil.getOurServerUrl() + "/imsblti/provider/"+toolRegistration;
	}

	public static final String EMAIL_TRUSTED_CONSUMER = "basiclti.provider.email.trusted.consumers";
	public static final String HIGHLY_TRUSTED_CONSUMER = "basiclti.provider.highly.trusted.consumers";

	public static boolean isHighlyTrustedConsumer(Map payload) {
		String oauth_consumer_key = (String) payload.get("oauth_consumer_key");
		boolean isHighlyTrustedConsumer = findTrustedConsumer(oauth_consumer_key, HIGHLY_TRUSTED_CONSUMER);

		if (log.isDebugEnabled()) {
			log.debug("Consumer={}", oauth_consumer_key);
			log.debug("Trusted={}", isHighlyTrustedConsumer);
		}
		return isHighlyTrustedConsumer;
	}

	public static boolean isEmailTrustedConsumer(Map payload) {
		String oauth_consumer_key = (String) payload.get("oauth_consumer_key");
		return isEmailTrustedConsumer(oauth_consumer_key);
	}

	public static boolean isEmailTrustedConsumer(String oauth_consumer_key) {
		boolean isEmailTrustedConsumer = findTrustedConsumer(oauth_consumer_key, EMAIL_TRUSTED_CONSUMER);

		if (log.isDebugEnabled()) {
			log.debug("Consumer={}", oauth_consumer_key);
			log.debug("EmailTrusted={}", isEmailTrustedConsumer);
		}
		return isEmailTrustedConsumer;
	}

	private static boolean findTrustedConsumer(String oauth_consumer_key, String trustedConsumerProp) {
		boolean isTrusted = false;
		ServerConfigurationService cnf = (ServerConfigurationService) ComponentManager
				.get(ServerConfigurationService.class);
		final String trustedConsumersConfig = cnf.getString(trustedConsumerProp, null);
		if (BasicLTIUtil.isNotBlank(trustedConsumersConfig)) {
			List<String> consumersList = Arrays.asList(trustedConsumersConfig.split(":"));
			if (consumersList.contains(oauth_consumer_key)) {
				isTrusted = true;
			}
		}
		return isTrusted;
	}

}
