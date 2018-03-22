/******************************************************************************
 * $URL$
 * $Id$
 ******************************************************************************
 *
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *****************************************************************************/


package org.sakaiproject.user.tool;

import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserDirectoryService.PasswordRating;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Date;
import java.util.Stack;

/**
 * Utility class for enforcing password policy
 * @author plukasew, bjones86 - SAK-23568
 */
@SuppressWarnings("deprecation")
public class PasswordPolicyHelper {
    /** User directory API */
    private static UserDirectoryService uds = (UserDirectoryService) ComponentManager.get(UserDirectoryService.class);

    /** velocity param name for password policy enabled/disabled */
    private static final String JS_ENABLED_KEY = "isPasswordPolicyEnabled";

    /** velocity param value for password policy enabled/disabled */
    private static final boolean policyEnabled = (uds.getPasswordPolicy() != null);

    /**
     * Default zero-arg constructor
     */
    public PasswordPolicyHelper() {}

    /**
     * Validate the given password for the given user
     * @param password
     * 				the password to be validated
     * @param user
     * 				the user the password belongs to
     * @return true/false (valid/invalid)
     */
    public PasswordRating validatePassword(String password, User user) {
        if (policyEnabled) {
            return uds.validatePassword(password, user);
        }
        return PasswordRating.PASSED_DEFAULT;
    }

    /**
     * Add necessary parameters into the context for password policy enforcement
     * @param context
     */
    public void addJavaScriptParamsToContext(Context context) {
        context.put(JS_ENABLED_KEY, policyEnabled);
    }

    /**
     * This class is needed to allow input and output since the User/UserEdit classes are too hard to work with
     */
    @SuppressWarnings("unused")
    public static class TempUser implements User {
        @Getter @Setter private String eid;
        @Getter @Setter private String email;
        @Getter @Setter private String firstName;
        @Getter @Setter private String lastName;
        @Getter @Setter private String displayName;
        @Getter @Setter private String password;
        @Getter @Setter private String type;

        /**
         * Default zero-arg constructor.
         * DO NOT USE!
         */
        public TempUser() {}

        /**
         * Constructor
         *
         * @param eid
         * 				the user's external ID
         * @param email
         * 				the user's email address
         * @param firstName
         * 				the user's first name
         * @param lastName
         * 				the user's last name
         * @param displayName
         *				the user's display name
         * @param password
         * 				the user's password
         * @param type
         * 				the user's type
         */
        public TempUser(String eid, String email, String firstName, String lastName, String displayName, String password, String type) {
            this.eid 			= eid;
            this.password 		= password;
            this.email 			= email;
            this.firstName 		= firstName;
            this.lastName 		= lastName;
            this.displayName 	= displayName;
            this.type 			= type;
        }

        /***********************************************************************************************
         ********************************* UNIMPLEMENTED METHODS ***************************************
         ***********************************************************************************************/
        @Override public ResourceProperties getProperties() 							{ return null; }
        @Override public Element 			toXml(Document arg0, Stack<Element> arg1) 	{ return null; }
        @Override public boolean 			checkPassword(String arg0) 					{ return false; }
        @Override public String 			getId() 									{ return null; }
        @Override public String 			getDisplayId() 								{ return null; }
        @Override public String 			getDisplayName() 							{ return null; }
        @Override public String 			getEid() 									{ return null; }
        @Override public String 			getFirstName() 								{ return null; }
        @Override public String 			getLastName() 								{ return null; }
        @Override public String 			getReference() 								{ return null; }
        @Override public String 			getReference(String arg0)					{ return null; }
        @Override public String 			getUrl() 									{ return null; }
        @Override public String 			getUrl(String arg0) 						{ return null; }
        @Override public String 			getSortName() 								{ return null; }
        @Override public String 			getType() 									{ return null; }
        @Override public User 				getModifiedBy() 							{ return null; }
        @Override public User 				getCreatedBy() 								{ return null; }
        @Override public Date 				getCreatedDate() 							{ return null; }
        @Override public Date 				getModifiedDate() 							{ return null; }
        @Override public int 				compareTo(Object o) 						{ return 0; }
    }
}
