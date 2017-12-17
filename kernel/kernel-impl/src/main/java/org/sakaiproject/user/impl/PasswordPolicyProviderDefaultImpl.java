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

package org.sakaiproject.user.impl;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.ArrayUtils;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.user.api.PasswordPolicyProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService.PasswordRating;

/**
 * This is the default implementation of the Password policy provider.
 * 
 * https://jira.sakaiproject.org/browse/KNL-1123
 */
@Slf4j
public class PasswordPolicyProviderDefaultImpl implements PasswordPolicyProvider {
    /** value for minimum password entropy */
    private static final int DEFAULT_MIN_ENTROPY = 16;
    
    /** value for medium password entropy multiplier */
    private static final int DEFAULT_MEDIUM_ENTROPY = 32;
    
    /** value for high password entropy multiplier */
    private static final int DEFAULT_HIGH_ENTROPY = 48;

    /** value for maximum password sequence length */
    private static final int DEFAULT_MAX_SEQ_LENGTH = 3;

    /** sakai.property for minimum password entropy */
    private static final String SAK_PROP_MIN_PASSWORD_ENTROPY = "user.password.minimum.entropy";
    
    /** sakai.property for medium password entropy multiplier */
    private static final String SAK_PROP_MEDIUM_PASSWORD_ENTROPY = "user.password.medium.entropy";
    
    /** sakai.property for high password entropy multiplier */
    private static final String SAK_PROP_HIGH_PASSWORD_ENTROPY = "user.password.high.entropy";

    /** sakai.property for maximum password sequence length */
    private static final String SAK_PROP_MAX_PASSWORD_SEQ_LENGTH = "user.password.maximum.sequence.length";

    /** array of all lower case characters (used for calculating password entropy) */
    private static final char[] CHARS_LOWER = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

    /** array of all upper case characters (used for calculating password entropy) */
    private static final char[] CHARS_UPPER = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

    /** array of all digit characters (used for calculating password entropy) */
    private static final char[] CHARS_DIGIT = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

    /** array of all special characters (used for calculating password entropy) */
    private static final char[] CHARS_SPECIAL = { '!', '$', '*', '+', '-', '.', '=', '?', '@', '^', '_', '|', '~' };

    private static char[] allCharacterSets;

    static {
        allCharacterSets = ArrayUtils.addAll(ArrayUtils.addAll(ArrayUtils.addAll(CHARS_LOWER, CHARS_UPPER), CHARS_DIGIT), CHARS_SPECIAL);
    }

    /** value for minimum password entropy */
    private int minEntropy = DEFAULT_MIN_ENTROPY;
    
    /** value for medium password entropy multiplier */
    private int mediumEntropy = DEFAULT_MEDIUM_ENTROPY;
    
    /** value for high password entropy multiplier */
    private int highEntropy = DEFAULT_HIGH_ENTROPY;

    /** value for maximum password sequence length */
    private int maxSequenceLength = DEFAULT_MAX_SEQ_LENGTH;

    /**
     * Default zero-arg constructor
     * DO NOT USE
     */
    PasswordPolicyProviderDefaultImpl() {
        this(null);
    }

    /**
     * @param serverConfigurationService
     */
    public PasswordPolicyProviderDefaultImpl(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
        init();
    }


    /**
     * Initialization method (Spring)
     */
    public void init() {
        // Get the values from sakai.properties
        if (serverConfigurationService == null) {
            serverConfigurationService = (ServerConfigurationService) ComponentManager.get(org.sakaiproject.component.api.ServerConfigurationService.class);
        }
        if (serverConfigurationService != null) {
            minEntropy = serverConfigurationService.getInt(SAK_PROP_MIN_PASSWORD_ENTROPY, minEntropy);
            mediumEntropy = serverConfigurationService.getInt(SAK_PROP_MEDIUM_PASSWORD_ENTROPY, mediumEntropy);
            highEntropy = serverConfigurationService.getInt(SAK_PROP_HIGH_PASSWORD_ENTROPY, highEntropy);
            maxSequenceLength = serverConfigurationService.getInt(SAK_PROP_MAX_PASSWORD_SEQ_LENGTH, maxSequenceLength);
            if (maxSequenceLength < 0) {
            	maxSequenceLength = 0;
            }
            if (mediumEntropy < minEntropy) {
            	mediumEntropy = DEFAULT_MEDIUM_ENTROPY;
            }
            if (highEntropy < mediumEntropy) {
            	highEntropy = DEFAULT_HIGH_ENTROPY;
            }
        }
        log.info("PasswordPolicyProviderDefaultImpl.init(): minEntropy="+minEntropy+", mediumEntropy="+mediumEntropy+
        		", highEntropy="+highEntropy+", maxSequenceLength="+maxSequenceLength);
    }

    /**
     * Destroy method (Spring)
     */
    public void destroy() {
        if (log.isDebugEnabled())
            log.debug("PasswordPolicyProviderDefaultImpl.destroy()");
    }

    public PasswordRating validatePassword(String password, User user) {
        if (log.isDebugEnabled())
            log.debug("PasswordPolicyProviderDefaultImpl.validatePassword( " + password + " )");

        // If the password is null, it's invalid
        if (password == null) {
            return PasswordRating.FAILED; // SHORT CIRCUIT
        }

        /* If the password contains X number of characters from their display ID, it's invalid
         * (where X is the maximum password sequence length defined in sakai.properties)
         */
        if (user != null) {
            String userDisplayID = user.getDisplayId();
            if (userDisplayID != null) {
                int length = userDisplayID.length();
                for (int i = 0; i < length - maxSequenceLength; i++) {
                    String sub = userDisplayID.substring(i, i + (maxSequenceLength + 1));
                    if (password.indexOf(sub) > -1) {
                        return PasswordRating.FAILED; // SHORT CIRCUIT
                    }
                }
            }
        }

        // Count the number of character sets used in the password
        int characterSets = 0;
        characterSets += isCharacterSetPresentInPassword(CHARS_LOWER, password);
        characterSets += isCharacterSetPresentInPassword(CHARS_UPPER, password);
        characterSets += isCharacterSetPresentInPassword(CHARS_DIGIT, password);
        characterSets += isCharacterSetPresentInPassword(CHARS_SPECIAL, password);
        characterSets += isOtherCharacterTypePresentInPassword(password);

        // Calculate and verify the password strength
        int strength = password.length() * characterSets;
        if (strength < minEntropy) {
            return PasswordRating.FAILED; // SHORT CIRCUIT
        }

        // The password has passed all requirements; determine the strength of the password and return the appropriate flag
        if (strength >= highEntropy) {
        	return PasswordRating.STRONG;
        }
        else if (strength >= mediumEntropy) {
        	return PasswordRating.MODERATE;
        }
        else {
        	return PasswordRating.WEAK;
        }
    }

    /**
     * Determine if the given character set is present in the given password string.
     * 
     * @param characterSet
     *            the set of characters to check for
     * @param password
     *            the password to search for the charachter set in
     * @return 1 if the character set is present in the password, 0 otherwise
     */
    private int isCharacterSetPresentInPassword(char[] characterSet, String password) {
        for (int i = 0; i < password.length(); i++) {
            if (Arrays.binarySearch(characterSet, password.charAt(i)) >= 0) {
                return 1; // SHORT CIRCUIT
            }
        }
        return 0;
    }

    /**
     * Determine if any other characters are present in the given password string
     * for example letters with accents, Chinese or Arabic characters.
     *
     * @param password
     *            the password to be searched
     * @return 1 if there is a character not in the other types of character set, 0 otherwise
     */
    private int isOtherCharacterTypePresentInPassword(String password) {
        for (int i = 0; i < password.length(); i++) {
            if (!ArrayUtils.contains(allCharacterSets, password.charAt(i))) {
                return 1; // SHORT CIRCUIT
            }
        }
        return 0;
    }

    private ServerConfigurationService serverConfigurationService;
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

}
