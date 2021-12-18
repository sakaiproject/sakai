/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2021- The Apereo Foundation
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

import java.util.Map;
import java.util.TreeMap;
import java.time.Instant;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.cover.ComponentManager;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.KeyPairGenerator;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.tsugi.lti13.LTI13KeySetUtil;
import org.tsugi.lti13.LTI13Util;

/**
 * Maintaining the system-wide keyset  for a Sakai instance.  This code generates,
 * stores, and rotates a three public / private keypairs for the current, next, and
 * previous.
 *
 *
 * Sakai conventions for making and launching BLTI resources within Sakai.
 */
@SuppressWarnings("deprecation")
@Slf4j
public class SakaiKeySetUtil {

	// SAK-45491 - Key rotation interval
	public static final String LTI_ADVANTAGE_KEY_ROTATION_DAYS = "lti.advantage.key.rotation.days";
	public static final String LTI_ADVANTAGE_KEY_ROTATION_DAYS_DEFAULT = "30";

    private static final String CACHE_NAME = SakaiKeySetUtil.class.getName() + "_cache";
    private static final String CACHE_PUBLIC = "key::public";
    private static final String CACHE_PRIVATE = "key::private";

	private static Map<String, String> mockIgnite = null;

	public static void mockIgnite()
	{
		mockIgnite = new TreeMap<String, String>();
	}

	private static Cache getCache()
	{
		CacheManager cacheManager = (CacheManager) ComponentManager.get("org.sakaiproject.ignite.SakaiCacheManager");
        return cacheManager.getCache(CACHE_NAME);
	}

	public static String getCacheKey(String key)
	{
		if ( mockIgnite != null ) {
			return mockIgnite.get(key);
		}
		Cache cache = getCache();
		Cache.ValueWrapper value = cache.get(key);
		if ( value == null ) return null;
		return (String) value.get();
	}

	public static void putCacheKey(String key, String value)
	{
		if ( mockIgnite != null ) {
			mockIgnite.put(key, value);
			return;
		}
		Cache cache = getCache();
        cache.put(key, value);
	}

	public static KeyPair getCurrent()
	{
        return LTI13Util.deSerializeKeyPair(getCacheKey("current"));
	}

	/**
	 * Rotate Keys
	 *
     * if next or previous are empty generate them
     * if current is empty, generate it and set the time
     * if ( current is expired ) {
     *   if (prev and current are identical) {
     *     copy next to current, reset timer for current
     *  } else {
     *     copy current to previous
     *  }
     * } else {
     *   if ( next and current are identical ) {
     *      generate a new next
     *   }
     * }
	 */
	public static void rotateKeys() {

        KeyPair kp;

		Instant now = Instant.now();
		long nowSeconds = now.getEpochSecond();

        KeyPair nextPair = LTI13Util.deSerializeKeyPair(getCacheKey("next"));
        KeyPair prevPair = LTI13Util.deSerializeKeyPair(getCacheKey("prev"));
        KeyPair currentPair = LTI13Util.deSerializeKeyPair(getCacheKey("current"));

		String currentTime = getCacheKey("current_time");
		long currentSeconds = -1;
		try {
			currentSeconds = Long.parseLong(currentTime);
		} catch(Exception e) {
			currentSeconds = -1;
		}

		if ( currentPair == null ) {
			currentPair = LTI13Util.generateKeyPair();
			putCacheKey("current", LTI13Util.serializeKeyPair(currentPair));
			putCacheKey("current_time", nowSeconds+"");
			currentSeconds = nowSeconds;
			log.info("Bootstrapping current public/private key");
		}

		if ( nextPair == null ) {
			nextPair = LTI13Util.generateKeyPair();
			putCacheKey("next", LTI13Util.serializeKeyPair(nextPair));
			log.info("Bootstrapping future public/private key");
		}

		if ( prevPair == null ) {
			prevPair = LTI13Util.generateKeyPair();
			putCacheKey("prev", LTI13Util.serializeKeyPair(prevPair));
			log.info("Bootstrapping previous public/private key");
		}

		// At this point all threee keys should exist
		String currentPublic = LTI13Util.getPublicEncoded(currentPair);
		String nextPublic = LTI13Util.getPublicEncoded(nextPair);
		String prevPublic = LTI13Util.getPublicEncoded(prevPair);
		long duration = nowSeconds - currentSeconds;
		if ( currentSeconds < 0 || duration > 1000 ) {
			log.info("Time to rotate");
			if ( currentPublic.equals(prevPublic) ) {
				putCacheKey("current", LTI13Util.serializeKeyPair(nextPair));
				putCacheKey("current_time", nowSeconds+"");

				nextPair = LTI13Util.generateKeyPair();
				putCacheKey("next", LTI13Util.serializeKeyPair(nextPair));
				log.info("Advancing next to current public/private key");
			} else {
				putCacheKey("prev", LTI13Util.serializeKeyPair(currentPair));
				log.info("Copying current to previous before advancing current");
			}
		}
	}

}
