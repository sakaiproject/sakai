/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.util.impl;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.RandomStringUtils;
import org.jasypt.util.text.AES256TextEncryptor;
import org.sakaiproject.util.api.EncryptionUtilityService;

@Slf4j
public class EncryptionUtilityServiceImpl implements EncryptionUtilityService {

    AES256TextEncryptor textEncryptor = new AES256TextEncryptor();

    public void init() {
        int keyLength = 30;
        boolean useLetters = true;
        boolean useNumbers = true;
        String serverSecretKey = RandomStringUtils.random(keyLength, useLetters, useNumbers);
        log.info("Server secret key has been set, not exposing it for security reasons.");
        textEncryptor.setPassword(serverSecretKey);
    }

    public String encrypt(String stringToEncrypt) {
        return textEncryptor.encrypt(stringToEncrypt);
    }

    public String decrypt(String stringToDecrypt) {
        return textEncryptor.decrypt(stringToDecrypt);
    }

}
