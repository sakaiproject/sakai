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
