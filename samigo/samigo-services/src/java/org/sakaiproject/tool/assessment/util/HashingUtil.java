/**
 * Copyright (c) ${license.git.copyrightYears} ${holder}
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
package org.sakaiproject.tool.assessment.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class HashingUtil {

    private static final String SHA256 = "SHA-256";

    public static String hashString(String string) {
        if (string == null) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance(SHA256);

            byte[] hashBytes = md.digest(string.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            BigInteger sigNum = new BigInteger(1, hashBytes);
            StringBuilder hexString = new StringBuilder(sigNum.toString(16));

            // Add zero padding
            while (hexString.length() < 64) {
                hexString.insert(0, '0');
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // This is not expected. SHA-256 is a requirement for every JVM
            log.error("Hashing algorithm {} not found. {}", SHA256, e.toString());
            return null;
        }
    }
}
