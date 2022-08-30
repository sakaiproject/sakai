/**
 * Copyright (c) 2021- The Apereo Foundation
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
package org.sakaiproject.basiclti.util;

import java.util.Map;
import java.util.TreeMap;
import java.time.Instant;

import static org.junit.Assert.assertEquals;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.springframework.cache.Cache;

import java.security.KeyPair;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import org.tsugi.basiclti.BasicLTIUtil;

import org.sakaiproject.basiclti.util.SakaiKeySetUtil;
import org.tsugi.lti13.LTI13Util;
import org.tsugi.lti13.LTI13KeySetUtil;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

// TODO: Use a ignite mock once we have one

@Slf4j
public class SakaiKeySetUtilTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testKeyPair() {
        KeyPair kp = LTI13Util.generateKeyPair();
        assertNotNull(kp);
        String pub = LTI13Util.getPublicEncoded(kp);
        assertNotNull(pub);
        assertTrue(pub.contains("-----END PUBLIC KEY-----"));
        assertFalse(pub.contains("::"));
        String priv = LTI13Util.getPrivateEncoded(kp);
        assertNotNull(priv);
        assertTrue(priv.contains("-----END PRIVATE KEY-----"));
        assertFalse(priv.contains("::"));
    }

    @Test
    public void testExpire()
    {
        // Special unit test version
        SakaiKeySetUtil.testExpireDays = 12;
        long days = SakaiKeySetUtil.getExpireDays();
        assertTrue(days == 12);
    }

    @Test
    public void testRotationAndKeySet()
       throws NoSuchAlgorithmException
    {
        Map fakeCache = new TreeMap<String, String> ();
        SakaiKeySetUtil.mockIgnite();
        SakaiKeySetUtil.testExpireDays = 12;
        SakaiKeySetUtil.putCacheKey("hello", "world");
        String world = SakaiKeySetUtil.getCacheKey("hello");
        assertEquals("world", world);

        // String kid = LTI13KeySetUtil.getPublicKID(publicKey);
        KeyPair kp = SakaiKeySetUtil.getCurrent();
        Key pubKey = kp.getPublic();
        assertNotNull(pubKey);
        String pub = LTI13Util.getPublicEncoded(kp);

        assertTrue(pub.contains("-----END PUBLIC KEY-----"));
        assertFalse(pub.contains("::"));
        String priv = LTI13Util.getPrivateEncoded(kp);
        assertNotNull(kp);
        assertTrue(priv.contains("-----END PRIVATE KEY-----"));
        assertFalse(priv.contains("::"));

        kp = SakaiKeySetUtil.getCurrent();
        assertNotNull(kp);
        String pub2 = LTI13Util.getPublicEncoded(kp);
        assertTrue(pub2.contains("-----END PUBLIC KEY-----"));
        assertFalse(pub2.contains("::"));
        String priv2 = LTI13Util.getPrivateEncoded(kp);
        assertNotNull(kp);
        assertTrue(priv2.contains("-----END PRIVATE KEY-----"));
        assertFalse(priv2.contains("::"));

        assertEquals(pub, pub2);
        assertEquals(priv, priv2);

        // Go back in time
        Instant now = Instant.now();
        long nowSeconds = now.getEpochSecond();
        SakaiKeySetUtil.putCacheKey("current_time", (nowSeconds-10000000)+"");

        // First rotation copies current to previous
        kp = SakaiKeySetUtil.getCurrent();
        assertNotNull(kp);
        String pub3 = LTI13Util.getPublicEncoded(kp);
        assertTrue(pub3.contains("-----END PUBLIC KEY-----"));
        assertFalse(pub3.contains("::"));
        String priv3 = LTI13Util.getPrivateEncoded(kp);
        assertNotNull(kp);
        assertTrue(priv3.contains("-----END PRIVATE KEY-----"));
        assertFalse(priv3.contains("::"));

        assertEquals(pub2, pub3);
        assertEquals(priv2, priv3);

        // Second rotation moves next to current
        kp = SakaiKeySetUtil.getCurrent();
        assertNotNull(kp);
        String pub4 = LTI13Util.getPublicEncoded(kp);
        assertTrue(pub4.contains("-----END PUBLIC KEY-----"));
        assertFalse(pub4.contains("::"));
        String priv4 = LTI13Util.getPrivateEncoded(kp);
        assertNotNull(kp);
        assertTrue(priv4.contains("-----END PRIVATE KEY-----"));
        assertFalse(priv4.contains("::"));

        assertFalse(pub3.equals(pub4));
        assertFalse(priv3.equals(priv4));

        // Third rotation does nothing
        kp = SakaiKeySetUtil.getCurrent();
        assertNotNull(kp);
        String pub5 = LTI13Util.getPublicEncoded(kp);
        assertTrue(pub5.contains("-----END PUBLIC KEY-----"));
        assertFalse(pub5.contains("::"));
        String priv5 = LTI13Util.getPrivateEncoded(kp);
        assertNotNull(kp);
        assertTrue(priv5.contains("-----END PRIVATE KEY-----"));
        assertFalse(priv5.contains("::"));

        assertEquals(pub4, pub5);
        assertEquals(priv4, priv5);

        String keySetJSON = SakaiKeySetUtil.getKeySet();
        assertNotNull(keySetJSON);
        assertTrue(keySetJSON.contains("{\"keys\":[{\"kty\":"));
        assertTrue(keySetJSON.contains(",\"alg\":\"RS256\""));
        JSONObject ks_json = BasicLTIUtil.parseJSONObject(keySetJSON);
        JSONArray keys = (JSONArray) ks_json.get("keys");
        assertEquals(keys.size(), 3);
        for(int i=0; i< keys.size(); i++) {
            JSONObject key = (JSONObject) keys.get(i);
            assertNotNull(key);
            String kid = (String) key.get("kid");
            assertNotNull(kid);
            String n = (String) key.get("n");
            assertNotNull(n);
        }

        // Test the expiration of the previous key
        KeyPair [] pairs = SakaiKeySetUtil.getValidSigningKeys();
        assertEquals(pairs.length, 3);
        SakaiKeySetUtil.putCacheKey("prev_time", (nowSeconds-10000000)+"");
        pairs = SakaiKeySetUtil.getValidSigningKeys();
        assertEquals(pairs.length, 2);

        keySetJSON = SakaiKeySetUtil.getKeySet();
        assertNotNull(keySetJSON);
        assertTrue(keySetJSON.contains("{\"keys\":[{\"kty\":"));
        assertTrue(keySetJSON.contains(",\"alg\":\"RS256\""));
        ks_json = BasicLTIUtil.parseJSONObject(keySetJSON);
        keys = (JSONArray) ks_json.get("keys");
        assertEquals(keys.size(), 2);
    }

}


