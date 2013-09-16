/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008, 2009, 2010 Sakai Foundation
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

package org.sakaiproject.user.api;

/**
 * This interface provides the method stubs needed for any password policy object. 
 * All password policy implementations need to implement this interface.
 * 
 * https://jira.sakaiproject.org/browse/KNL-1123
 */
public interface PasswordPolicyProvider {

    /** value for minimum password entropy */
    public static final int DEFAULT_MIN_ENTROPY = 16;

    /** value for maximum password sequence length */
    public static final int DEFAULT_MAX_SEQ_LENGTH = 3;

    /** sakai.property for minimum password entropy */
    public static final String SAK_PROP_MIN_PASSWORD_ENTROPY = "user.password.minimum.entropy";

    /** sakai.property for maximum password sequence length */
    public static final String SAK_PROP_MAX_PASSWORD_SEQ_LENGTH = "user.password.maximum.sequence.length";

    /** sakai.property for minimum password entropy */
    public static final String SAK_PROP_PROVIDER_NAME = "user.password.policy.provider.name";

    /**
     * This function returns a boolean value of true/false, depending on if the given password meets the validation criteria.
     * 
     * Based on verifyPasswordStrength() in http://grepcode.com/file/repo1.maven.org/maven2/org.owasp.esapi/esapi/2.0_rc10/org/owasp/esapi/reference/FileBasedAuthenticator.java
     * 
     * @param password the password to be validated
     * @param userDisplayID the user's login ID
     * @return true/false (password is valid/invalid)
     */
    public boolean validatePassword(String password, String userDisplayID);

    /**
     * This method is called to retrieve the JavaScript validation function to be used client-side.
     * 
     * @return the JavaScript function to be used client-side
     */
    public String getClientValidatePasswordFunction();
}
