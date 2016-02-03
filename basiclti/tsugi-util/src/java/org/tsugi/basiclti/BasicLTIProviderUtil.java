/*
 * $URL$
 * $Id$
 *
 * Copyright (c) 2008-2016 Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.tsugi.basiclti;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;

public class BasicLTIProviderUtil {
	private static Log M_log = LogFactory.getLog(BasicLTIProviderUtil.class);
	public static final String EMAIL_TRUSTED_CONSUMER = "basiclti.provider.email.trusted.consumers";
	public static final String HIGHLY_TRUSTED_CONSUMER = "basiclti.provider.highly.trusted.consumers";

	public static boolean isHighlyTrustedConsumer(Map payload) {
		String oauth_consumer_key = (String) payload.get("oauth_consumer_key");
		boolean isHighlyTrustedConsumer = findTrustedConsumer(oauth_consumer_key, HIGHLY_TRUSTED_CONSUMER);

		if (M_log.isDebugEnabled()) {
			M_log.debug("Consumer=" + oauth_consumer_key);
			M_log.debug("Trusted=" + isHighlyTrustedConsumer);
		}
		return isHighlyTrustedConsumer;
	}

	public static boolean isEmailTrustedConsumer(Map payload) {
		String oauth_consumer_key = (String) payload.get("oauth_consumer_key");
		return isEmailTrustedConsumer(oauth_consumer_key);
	}

	public static boolean isEmailTrustedConsumer(String oauth_consumer_key) {
		boolean isEmailTrustedConsumer = findTrustedConsumer(oauth_consumer_key, EMAIL_TRUSTED_CONSUMER);

		if (M_log.isDebugEnabled()) {
			M_log.debug("Consumer=" + oauth_consumer_key);
			M_log.debug("EmailTrusted=" + isEmailTrustedConsumer);
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
