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

package org.sakaiproject.lti.util;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.tsugi.lti.LTIUtil;

import org.sakaiproject.lti.util.SakaiLTIUtil;
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
                return SakaiLTIUtil.getOurServerUrl() + "/imsblti/provider/"+toolRegistration;
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
		if (LTIUtil.isNotBlank(trustedConsumersConfig)) {
			List<String> consumersList = Arrays.asList(trustedConsumersConfig.split(":"));
			if (consumersList.contains(oauth_consumer_key)) {
				isTrusted = true;
			}
		}
		return isTrusted;
	}

	/**
	 * Return URL to follow when an LTI 1.1 provider launch fails, or null when
	 * the error must be rendered on this server.
	 *
	 * The URL is ignored until the launch has been validated. After that, the
	 * target must be a path on this server or an http(s) URL whose host is the
	 * consumer key. That key is the platform host in lti.provider.[hostname].secret.
	 */
	public static String allowedLaunchErrorReturnUrl(String returnUrl, boolean validated, String consumerKey) {
		if (!validated || returnUrl == null || consumerKey == null || consumerKey.isEmpty()) {
			return null;
		}
		if (returnUrl.length() <= 1 || containsControl(returnUrl) || returnUrl.indexOf('\\') >= 0) {
			return null;
		}
		String lower = returnUrl.toLowerCase(Locale.ROOT);
		if (lower.contains("%0d") || lower.contains("%0a") || lower.contains("%00")
				|| lower.contains("%09") || lower.contains("%5c")) {
			return null;
		}
		if (matchesConsumerHost(returnUrl, consumerKey) || isSameOriginPath(returnUrl)) {
			return returnUrl;
		}
		return null;
	}

	private static boolean matchesConsumerHost(String returnUrl, String consumerKey) {
		String host = Pattern.quote(consumerKey);
		String rest = "(:[0-9]+)?([/?#].*)?";
		return returnUrl.matches("(?i)https?://" + host + rest)
				|| returnUrl.matches("(?i)//" + host + rest);
	}

	private static boolean isSameOriginPath(String returnUrl) {
		if (!returnUrl.startsWith("/") || returnUrl.startsWith("//")) {
			return false;
		}
		String lower = returnUrl.toLowerCase(Locale.ROOT);
		return !lower.startsWith("/%2f") && !lower.startsWith("/%5c");
	}

	private static boolean containsControl(String value) {
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c <= 0x20 || c == 0x7f) {
				return true;
			}
		}
		return false;
	}

}
