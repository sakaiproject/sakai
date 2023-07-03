/*
 * Copyright (c) 2003-2023 The Apereo Foundation
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
package org.sakaiproject.tool.messageforums.util;

public class PrivateMessagesToolHelper {
    /**
     * Removes the recipientsUndisclosed from the recipientsAsText string and returns the modified string.
     * @param recipientsAsText The original string containing the recipients.
     * @param recipientsUndisclosed The string to be removed from the recipientsAsText.
     * @return The processed recipients text without recipientsUndisclosed string, trailing semicolons and spaces.
     * @throws IllegalArgumentException if recipientsAsText or recipientsUndisclosed is null.
     */
    public static String removeRecipientUndisclosed(String recipientsAsText, String recipientsUndisclosed) throws IllegalArgumentException {
        if (recipientsAsText == null) {
            throw new IllegalArgumentException("recipientsAsText cannot be null");
        }
        if (recipientsUndisclosed == null) {
            throw new IllegalArgumentException("recipientsUndisclosed cannot be null");
        }

        return recipientsAsText
                // Remove recipientsUndisclosed followed by optional space and semicolon
                .replaceAll(recipientsUndisclosed + "\\s*;?\\s*$", "")
                // Remove semicolon followed by optional spaces at the end of the string
                .replaceAll(";\\s*$", "")
                .trim();
    }

}
