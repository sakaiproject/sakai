/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.event.api;

import java.util.Date;
import java.util.Map;

/**
 * Provides support for Sakai to work with Learning Record Stores (LRS)
 * Allows centralized registration of LRS activity statements which Sakai
 * will then route over to the configured LRS system (via the Experience API (XAPI)).
 * See https://jira.sakaiproject.org/browse/KNL-1042
 * 
 * http://en.wikipedia.org/wiki/Learning_Record_Store 
 * A Learning Record Store (LRS) is a data store that serve as a repository for learning records 
 * necessary for using the Experience API (XAPI). The Experience API (XAPI) is also known as "next-gen SCORM" 
 * or previously the TinCanAPI. The concept of the LRS was introduced to the e-learning industry in 2011, 
 * and is a shift to the way e-learning specifications function.
 * 
 * @author Aaron Zeckoski (azeckoski @ vt.edu)
 */
public interface LearningResourceObjectService {

    /**
     * Register an activity statement with the LRS
     * This is generally only for internal/system/service use, though it can be used by tools if needed
     * NOTE: this will run asynchronously to avoid slowing anything down so there is no return
     * 
     * @param statement the LRS statement representing the activity statement
     * @throws IllegalArgumentException if the input statement is invalid
     * @throws RuntimeException if there is a failure
     */
    public void registerStatement(LRS_Statement statement);

    public static class LRS_Statement {
        // actor, verb, and object are required
        /**
         * UUID assigned by LRS or other trusted source.
         * Set by LRS.
         */
        String id = null;
        /**
         * Indicates that the statement has been voided.
         * NOTE: Default=false, voided=true
         */
        boolean voided = false;
        /**
         * Timestamp of when what this statement describes happened.
         * If null, the LRS will set this to the stored time.
         */
        Date timestamp;
        /**
         * Timestamp of when this statement was recorded.
         * Set by LRS.
         */
        Date stored;
        /**
         * REQUIRED:
         * Who the statement is about, as an Agent or Group object. "I"
         */
        LRS_Actor actor;
        /**
         * REQUIRED:
         * Action of the Learner or Team object. "Did"
         */
        LRS_Verb verb;
        /**
         * REQUIRED:
         * Activity, agent, or another statement that is the object of the statement, "this".
         * NOTE that objects which are provided as a value for this field should include a 'objectType' field. If not specified, the object is assumed to be an activity.
         */
        LRS_Object object;
        /**
         * Result object, further details relevant to the specified verb.
         */
        LRS_Result result;
        /**
         * Context that gives the statement more meaning.
         * Examples: Team actor is working with, altitude in a flight simulator, course in a classroom activity.
         */
        LRS_Context context;

        /**
         * use of the empty constructor is restricted
         */
        protected LRS_Statement() {
            timestamp = new Date();
        }
        public LRS_Statement(LRS_Actor actor, LRS_Verb verb, LRS_Object object) {
            this();
            if (actor == null) {
                throw new IllegalArgumentException("LRS_Actor cannot be null");
            }
            if (verb == null) {
                throw new IllegalArgumentException("LRS_Verb cannot be null");
            }
            if (object == null) {
                throw new IllegalArgumentException("LRS_Object cannot be null");
            }
            this.actor = actor;
            this.verb = verb;
            this.object = object;
        }
        /**
         * Construct a simple LRS statement
         * 
         * @param actorEmail the user email address, "I"
         * @param verbStr a string indicating the action, "did"
         * @param objectURI URI indicating the object of the statement, "this"
         */
        public LRS_Statement(String actorEmail, String verbStr, String objectURI) {
            this(new LRS_Actor(actorEmail), new LRS_Verb(verbStr), new LRS_Object(objectURI));
        }
    }

    public static class LRS_Actor {
        /*
         * NOTE: for now we are only representing an Agent type of Actor (no Group actors)
         * NOTE: For now we are ignoring the account and openid agent inverse functional identifiers
         * 
         * An Agent object is identified by an email address (or its hash), OpenID, or account on some system (such as twitter),
         * but only for values where any two Agents that share the same identifying property definitely represent the same identity. 
         * The term used for properties with that characteristic is "inverse functional identifiers”. 
         * In addition to the standard inverse functional properties from FOAF of mbox, mbox_sha1sum, and openid, 
         * account is an inverse functional property in XAPI Agents.
         * For reasons of practicality and privacy, TCAPI Agents MUST be identified by one and only one inverse functional identifier. 
         * Agents MUST NOT include more than one inverse functional identifier. 
         * If an Activity Provider is concerned about revealing identifying information such as emails, 
         * it SHOULD instead use an account with an opaque account name to identify the person.
         */
        /**
         * "Agent" or "Group" (Optional, except when used as a statement’s object)
         */
        String objectType;
        /**
         * Display String (Optional)
         */
        String name;
        /**
         * String in the form "mailto:email address". 
         * (Note: Only emails that have only ever been and will ever be assigned to this Agent, but no others, should be used for this property and mbox_sha1sum).
         */
        String mbox;
        /**
         * @param email the user email address
         * @return an actor built using the given email address
         */
        static LRS_Actor makeFromEmail(String email) {
            LRS_Actor actor = new LRS_Actor(email);
            return actor;
        }
        /**
         * use of the empty constructor is restricted
         */
        protected LRS_Actor() {
            objectType = "Agent";
        }
        /**
         * Construct an actor using an email address
         * @param email the user email address
         */
        public LRS_Actor(String email) {
            this();
            if (email == null) {
                throw new IllegalArgumentException("LRS_Actor email cannot be null");
            }
            mbox = "mailto:"+email;
        }
        /**
         * @param name OPTIONAL display value for this actor
         */
        public void setName(String name) {
            this.name = name;
        }
    }

    public static class LRS_Verb {
        /*
         * A verb defines what the action is between actors, activities, or most commonly, between an actor and activity. 
         * The Experience API does not specify any particular verbs, but rather defines how verbs are to be created. 
         * It is expected that verb lists exist for various communities of practice. Verbs appear in statements as objects 
         * consisting of a URI and a set of display names.
         * 
         * The Verb URI should identify the particular semantics of a word, not the word itself. 
         * For example, the English word "fired" could mean different things depending on context, 
         * such as "fired a weapon", "fired a kiln", or "fired an employee". 
         * In this case, a URI should identify one of these specific meanings, not the word "fired".
         */
        /**
         * REQUIRED: 
         * A URI that corresponds to a verb definition. 
         * Each verb definition corresponds to the meaning of a verb, not the word.
         * A URI should be human-readable and contain the verb meaning.
         * Example: www.adlnet.gov/XAPIprofile/ran(travelled_a_distance)
         */
        String id;
        /**
         * OPTIONAL: 
         * A language map containing the human readable display representation 
         * of the verb in at least one language. This does not have any impact 
         * on the meaning of the statement, but only serves to give a human-readable display 
         * of the meaning already determined by the chosen verb.
         * Example: { "en-US" => "ran", "es" => "corrió" }
         */
        Map<String, String> display;
        /**
         * use of the empty constructor is restricted
         */
        protected LRS_Verb() {}
        /**
         * The verb should probably come from this listing:
         * http://tincanapi.wikispaces.com/Verbs+and+Activities
         * 
         * @param verb a string indicating the action
         */
        public LRS_Verb(String verb) {
            this();
            if (verb == null) {
                throw new IllegalArgumentException("LRS_Verb verb cannot be null");
            }
            id = "www.adlnet.gov/XAPIprofile/"+verb;
        }
        /**
         * OPTIONAL: 
         * A language map containing the human readable display representation 
         * of the verb in at least one language. This does not have any impact 
         * on the meaning of the statement, but only serves to give a human-readable display 
         * of the meaning already determined by the chosen verb.
         * Example: { "en-US" => "ran", "es" => "corrió" }
         */
        public void setDisplay(Map<String, String> display) {
            this.display = display;
        }
    }

    public static class LRS_Object {
        /*
         * The object of a statement is the Activity, Agent, or Statement that is the object of the statement, "this". 
         * Note that objects which are provided as a value for this field should include an "objectType" field. 
         * If not specified, the object is assumed to be an Activity.
         * 
         * An activity URI must always refer to a single unique activity. 
         * There may be corrections to that activity's definition. Spelling fixes would be appropriate, 
         * for example, but changing correct responses would not.
         * The activity URI is unique, and any reference to it always refers to the same activity. 
         * Activity Providers must ensure this is true and the LRS may not attempt to treat multiple references to 
         * the same URI as references to different activities, regardless of any information which indicates 
         * two authors or organizations may have used the same activity URI.
         */
        /**
         * Should always be "Activity" when present. Used in cases where type cannot otherwise be determined, 
         * such as the value of a statement’s object field.
         */
        String objectType;
        /**
         * URI. An activity URI must always refer to a single unique activity.
         * If a URL, the URL should refer to metadata for this activity
         * Example: http://example.adlnet.gov/xapi/example/simpleCBT
         */
        String id;
        /**
         * use of the empty constructor is restricted
         */
        /**
         * OPTIONAL: 
         * A language map containing the human readable display representation 
         * of the object in at least one language. This does not have any impact 
         * on the meaning of the statement, but only serves to give a human-readable display 
         * of the meaning already determined by the chosen verb.
         * Example: { "en-US" => "ran", "es" => "corrió" }
         */
        Map<String, String> name;
        protected LRS_Object() {
            objectType = "Activity";
        }
        // TODO include the other optional Interaction Activities and Activities fields?
        /**
         * Create an LRS object
         * 
         * @param uri activity URI that refers to a single unique activity. 
         *      Example: http://example.adlnet.gov/xapi/example/simpleCBT
         */
        public LRS_Object(String uri) {
            this();
            if (uri == null) {
                throw new IllegalArgumentException("LRS_Object uri cannot be null");
            }
            id = uri;
        }
        /**
         * OPTIONAL: 
         * A language map containing the human readable display representation 
         * of the object in at least one language. This does not have any impact 
         * on the meaning of the statement, but only serves to give a human-readable display 
         * of the meaning already determined by the chosen verb.
         * Example: { "en-US" => "ran", "es" => "corrió" }
         */
        public void setName(Map<String, String> name) {
            this.name = name;
        }
    }

    public static class LRS_Result {
        /*
         * The result field represents a measured outcome related to the statement, such as completion, success, or score. 
         * It is also extendible to allow for arbitrary measurements to be included.
         */
        // TODO optional fields?
    }

    public static class LRS_Context {
        // TODO incomplete
    }

}
