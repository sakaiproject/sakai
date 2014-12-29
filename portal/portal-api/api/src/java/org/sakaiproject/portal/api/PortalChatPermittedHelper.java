/*
 * Copyright Leidse Onderwijsinstellingen. All Rights Reserved.
 */

package org.sakaiproject.portal.api;


/**
 * @author Tania Tritean, ISDC!
 */
public interface PortalChatPermittedHelper {

	/**
     * Checks if the user has permissions to use the chat.
     * The permission is set in the user site.
     * 
     * @param userId user id
     * @return if the user has chat permission in his user site.
     */
	 boolean checkChatPermitted(String userId);
}
