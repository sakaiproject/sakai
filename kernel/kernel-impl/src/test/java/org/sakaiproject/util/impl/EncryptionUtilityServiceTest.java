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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.component.cover.ComponentManager;

public class EncryptionUtilityServiceTest {

    EncryptionUtilityServiceImpl encryptionUtilityService;

    @Before
    public void setUp() throws Exception {
        // instantiate the services we need for our test
        ComponentManager.testingMode = true;
        // instantiate what we are testing
        encryptionUtilityService = new EncryptionUtilityServiceImpl();
        encryptionUtilityService.init();
    }

    @After
    public void tearDown() throws Exception {
        ComponentManager.shutdown();
    }

    // TESTS
    @Test
    public void testEncryptionAndDecryption() {
        String message = "StringMessageToTestTheEncryptionService";
        String encryptedMessage = encryptionUtilityService.encrypt(message);
        String decryptedMessage = encryptionUtilityService.decrypt(encryptedMessage);
        Assert.assertNotEquals(message, encryptedMessage);
        Assert.assertNotEquals(encryptedMessage, decryptedMessage);
        Assert.assertEquals(message, decryptedMessage);
    }

}
