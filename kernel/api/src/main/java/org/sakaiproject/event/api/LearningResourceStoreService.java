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
import java.util.LinkedHashMap;
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
public interface LearningResourceStoreService {

    /**
     * Register an activity statement with the LRS
     * This is generally only for internal/system/service use, though it can be used by tools if needed
     * NOTE: this will run asynchronously to avoid slowing anything down so there is no return
     * 
     * @param statement the LRS statement representing the activity statement
     * @throws IllegalArgumentException if the input statement is invalid or cannot be handled
     * @throws RuntimeException if there is a FATAL failure
     */
    public void registerStatement(LRS_Statement statement);

    /**
     * @return true if LRS tracking is enabled, false otherwise
     */
    public boolean isEnabled();

    /**
     * Allows for manual registration of an LRSP,
     * it is best to simply allow the system to discover all the 
     * LRSPs which the main Sakai Spring AC knows about instead but
     * this allows for some testing and also for cases where Spring is not being used
     * 
     * NOTE: there is no "unregister", the system will simply ignore the provider
     * if it is completely destroyed some time after registration
     * 
     * @param provider an instantiated LRSP
     * @return true if the provider replaced another one with the same ID, false if it was the first one
     */
    public boolean registerProvider(LearningResourceStoreProvider provider);

    public static class LRS_Statement {
        // actor, verb, and object are required
        /**
         * if true then this LRS_Statement is populated with all required fields (actor, verb, and object),
         * if false then check the raw data fields instead: {@link #rawMap} first and if null or empty then {@link #rawJSON},
         * it should be impossible for object to have none of these fields populated
         */
        boolean populated = false;
        /**
         * A raw map of the keys and values which should be able to basically be converted directly into a JSON statement,
         * MUST contain at least actor, verb, and object keys and the values for those cannot be null or empty
         */
        Map<String, Object> rawMap;
        /**
         * The raw JSON string to send as a statement
         * WARNING: this will not be validated
         */
        String rawJSON;
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
        /**
         * MINIMAL objects constructor
         * @param actor
         * @param verb
         * @param object
         */
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
         * FULL objects constructor
         * @param actor
         * @param verb
         * @param object
         * @param result
         * @param context
         */
        public LRS_Statement(LRS_Actor actor, LRS_Verb verb, LRS_Object object, LRS_Result result, LRS_Context context) {
            this(actor, verb, object);
            this.result = result;
            this.context = context;
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
        /**
         * Construct a simple LRS statement with Result
         * 
         * @param actorEmail the user email address, "I"
         * @param verbStr a string indicating the action, "did"
         * @param objectURI URI indicating the object of the statement, "this"
         * @param resultSuccess true if the result was successful (pass) or false if not (fail), "well"
         * @param resultScaledScore Score from 0 to 1.0 where 0=0% and 1.0=100%
         */
        public LRS_Statement(String actorEmail, String verbStr, String objectURI, boolean resultSuccess, float resultScaledScore) {
            this(new LRS_Actor(actorEmail), new LRS_Verb(verbStr), new LRS_Object(objectURI));
            this.result = new LRS_Result(resultScaledScore, resultSuccess);
        }
        /**
         * EXPERT USE ONLY
         * @param rawData map of the keys and values which MUST contain at least actor, verb, and object keys and the values for those cannot be null or empty
         * @throws IllegalArgumentException if any required keys are missing
         * @see #rawMap
         */
        public LRS_Statement(Map<String, Object> rawData) {
            this();
            this.populated = false;
            this.rawMap = rawData;
            if (rawData != null) {
                if (!rawData.containsKey("actor") || rawData.get("actor") == null) {
                    throw new IllegalArgumentException("actor key MUST be set and NOT null");
                }
                if (!rawData.containsKey("verb") || rawData.get("verb") == null) {
                    throw new IllegalArgumentException("verb key MUST be set and NOT null");
                }
                if (!rawData.containsKey("object") || rawData.get("object") == null) {
                    throw new IllegalArgumentException("object key MUST be set and NOT null");
                }
                this.rawMap = new LinkedHashMap<String, Object>(rawData);
                this.rawJSON = null;
            }
        }
        /**
         * INTERNAL USE ONLY
         * Probably will not work for anything that is NOT the Experience API
         * @param rawJSON JSON string to send as a statement
         *          WARNING: this will NOT be validated!
         * @see #rawJSON
         */
        public LRS_Statement(String rawJSON) {
            this();
            this.populated = false;
            this.rawJSON = rawJSON;
            if (rawJSON != null) {
                this.rawMap = null;
            }
        }
        // GETTERS
        /**
         * @see #populated
         */
        public boolean isPopulated() {
            return populated;
        }
        /**
         * @see #rawMap
         */
        public Map<String, Object> getRawMap() {
            return rawMap;
        }
        /**
         * @see #rawJSON
         */
        public String getRawJSON() {
            return rawJSON;
        }
        /**
         * @see #id
         */
        public String getId() {
            return id;
        }
        /**
         * @see #voided
         */
        public boolean isVoided() {
            return voided;
        }
        /**
         * @see #timestamp
         */
        public Date getTimestamp() {
            return timestamp;
        }
        /**
         * @see #stored
         */
        public Date getStored() {
            return stored;
        }
        /**
         * @see #actor
         */
        public LRS_Actor getActor() {
            return actor;
        }
        /**
         * @see #verb
         */
        public LRS_Verb getVerb() {
            return verb;
        }
        /**
         * @see #object
         */
        public LRS_Object getObject() {
            return object;
        }
        /**
         * @see #result
         */
        public LRS_Result getResult() {
            return result;
        }
        /**
         * @see #context
         */
        public LRS_Context getContext() {
            return context;
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
        // GETTERS
        /**
         * @see #objectType
         */
        public String getObjectType() {
            return objectType;
        }
        /**
         * @see #name
         */
        public String getName() {
            return name;
        }
        /**
         * @see #mbox
         */
        public String getMbox() {
            return mbox;
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
        // GETTERS
        /**
         * @see #id
         */
        public String getId() {
            return id;
        }
        /**
         * @see #display
         */
        public Map<String, String> getDisplay() {
            return display;
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
         * OPTIONAL: 
         * A language map containing the human readable display representation 
         * of the object in at least one language. This does not have any impact 
         * on the meaning of the statement, but only serves to give a human-readable display 
         * of the meaning already determined by the chosen verb.
         * Example: { "en-US" => "ran", "es" => "corrió" }
         */
        Map<String, String> name;
        /**
         * use of the empty constructor is restricted
         */
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
        // GETTERS
        /**
         * @see #objectType
         */
        public String getObjectType() {
            return objectType;
        }
        /**
         * @see #id
         */
        public String getId() {
            return id;
        }
        /**
         * @see #name
         */
        public Map<String, String> getName() {
            return name;
        }
    }

    public static class LRS_Result {
        /*
         * The result field represents a measured outcome related to the statement, such as completion, success, or score. 
         * It is also extendible to allow for arbitrary measurements to be included.
         * NOTE: the API score fields types are unclear in the spec (maybe int or float)
         */
        /**
         * Score from 0 to 1.0 where 0=0% and 1.0=100%
         */
        Float scaled;
        /**
         * Raw score - any number
         */
        Number raw;
        /**
         * Minimum score (range) - any number
         */
        Number min;
        /**
         * Maximum score (range) - any number
         */
        Number max;
        /**
         * true if successful, false if not, or not specified
         */
        Boolean success;
        /**
         * true if completed, false if not, or not specified
         */
        Boolean completion;
        /**
         * Duration of the activity in seconds
         * Have to convert this to https://en.wikipedia.org/wiki/ISO_8601#Durations for sending to the Experience API,
         * ignore the value if it is less than 0
         */
        int duration = -1;
        /**
         * A string response appropriately formatted for the given activity.
         */
        String response;
        /**
         * use of the empty constructor is restricted
         */
        protected LRS_Result() {
        }
        /**
         * @param scaled Score from 0 to 1.0 where 0=0% and 1.0=100%
         * @param success true if successful, false if not, or null for not specified
         */
        public LRS_Result(Float scaled, Boolean success) {
            this();
            if (scaled == null) {
                throw new IllegalArgumentException("LRS_Object uri cannot be null");
            }
            setScore(scaled);
        }
        // TODO optional extensions?
        /**
         * @param scaled Score from 0 to 1.0 where 0=0% and 1.0=100%
         */
        public void setScore(Float scaled) {
            this.scaled = scaled;
            if (scaled != null) {
                if (scaled.floatValue() < 0.0f) {
                    this.scaled = 0f;
                }
            }
        }
        /**
         * @param raw Raw score - any number
         */
        public void setScore(Number raw) {
            this.raw = raw;
        }
        /**
         * @param scaled Score from 0 to 1.0 where 0=0% and 1.0=100% (can be null)
         * @param raw Raw score - any number (can be null)
         * @param min Minimum score (range) - any number (can be null)
         * @param max Maximum score (range) - any number (can be null)
         * @throws IllegalArgumentException if the minimum is not less than (or equal to) the maximum
         */
        public void setScore(Float scaled, Number raw, Number min, Number max) {
            setScore(scaled);
            setScore(raw);
            this.min = min;
            this.max = max;
            if (this.min != null && this.max != null) {
                if (min.floatValue() > max.floatValue()) {
                    throw new IllegalArgumentException("score min must be less than max");
                }
            }
        }
        /**
         * @param success true if successful, false if not, or null if not specified
         */
        public void setSuccess(Boolean success) {
            this.success = success;
        }
        /**
         * @param completion true if completed, false if not, or null if not specified
         */
        public void setCompletion(Boolean completion) {
            this.completion = completion;
        }
        /**
         * @param duration Time spent on the activity in seconds, set to -1 to clear this
         */
        public void setDuration(int duration) {
            this.duration = duration;
        }
        // GETTERS
        /**
         * @see #scaled
         */
        public Float getScaled() {
            return scaled;
        }
        /**
         * @see #raw
         */
        public Number getRaw() {
            return raw;
        }
        /**
         * @see #min
         */
        public Number getMin() {
            return min;
        }
        /**
         * @see #max
         */
        public Number getMax() {
            return max;
        }
        /**
         * @see #success
         */
        public Boolean getSuccess() {
            return success;
        }
        /**
         * @see #completion
         */
        public Boolean getCompletion() {
            return completion;
        }
        /**
         * @see #duration
         */
        public int getDuration() {
            return duration;
        }
        /**
         * @see #response
         */
        public String getResponse() {
            return response;
        }
    }

    public static class LRS_Context {
        /*
         * The context field provides a place to add some contextual information to a statement. 
         * We can add information such as the instructor for an experience, if this experience 
         * happened as part of a team activity, or how an experience fits into some broader activity.
         */
        /**
         * OPTIONAL
         * Instructor that the statement relates to, 
         * if not included as the actor or object of the overall statement.
         */
        LRS_Actor instructor;
        /**
         * OPTIONAL
         * Revision of the learning activity associated with this statement.
         * Revisions are to track fixes of minor issues (like a spelling error), 
         * if there is any substantive change to the learning objectives, pedagogy, 
         * or assets associated with an activity, a new activity ID should be used.
         * Revision format is up to the owner of the associated activity.
         */
        String revision;
        /**
         * A map of the types of context to learning activities “activity” this statement is related to.
         * Valid context types are: "parent", "grouping", and "other".
         * For example, if I am studying a textbook, for a test, the textbook is the activity the statement is about, 
         * but the test is a context activity, and the context type is "other".
         * "other" : {"id" : "http://example.adlnet.gov/xapi/example/test"}
         * There could be an activity hierarchy to keep track of, for example question 1 on test 1 for the course Algebra 1. 
         * When recording results for question 1, it we can declare that the question is part of test 1, 
         * but also that it should be grouped with other statements about Algebra 1. This can be done using parent and grouping:
         * { 
         *   "parent" : {"id" : "http://example.adlnet.gov/xapi/example/test 1"}, 
         *   "grouping" : {"id" : "http://example.adlnet.gov/xapi/example/Algebra1"}
         * }
         */
        Map<String, Map<String, String>> activitiesMap;
        // TODO include fields like team, platform, language, statement, and extensions
        /**
         * use of the empty constructor is restricted
         */
        protected LRS_Context() {
        }
        /**
         * @param instructor Instructor user that the statement relates to
         */
        public LRS_Context(LRS_Actor instructor) {
            this();
            if (instructor == null) {
                throw new IllegalArgumentException("LRS_Context instructor cannot be null");
            }
            this.instructor = instructor;
        }
        /**
         * @param contextType must be "parent", "grouping", and "other"
         * @param activityId a URI or key identifying the activity type (e.g. http://example.adlnet.gov/xapi/example/test)
         */
        public LRS_Context(String contextType, String activityId) {
            this();
            setActivity(contextType, activityId);
        }
        /**
         * @param instructor Instructor user that the statement relates to
         */
        public void setInstructor(LRS_Actor instructor) {
            this.instructor = instructor;
        }
        /**
         * @param instructorEmail Instructor user email that the statement relates to
         */
        public void setInstructor(String instructorEmail) {
            this.instructor = new LRS_Actor(instructorEmail);
        }
        /**
         * @param contextType must be "parent", "grouping", and "other"
         * @param activityId a URI or key identifying the activity type (e.g. http://example.adlnet.gov/xapi/example/test)
         */
        public void setActivity(String contextType, String activityId) {
            if (contextType == null || "".equals(contextType)) {
                throw new IllegalArgumentException("contextType MUST be set");
            }
            if (activityId == null || "".equals(activityId)) {
                throw new IllegalArgumentException("activityId MUST be set");
            }
            if (this.activitiesMap == null) {
                this.activitiesMap = new LinkedHashMap<String, Map<String, String>>();
            }
            if (!this.activitiesMap.containsKey(contextType) || this.activitiesMap.get(contextType) == null) {
                this.activitiesMap.put(contextType, new LinkedHashMap<String, String>());
            }
            this.activitiesMap.get(contextType).put("id", activityId);
        }
        /**
         * A map of the types of context to learning activities “activity” this statement is related to.
         * Valid context types are: "parent", "grouping", and "other".
         * For example, if I am studying a textbook, for a test, the textbook is the activity the statement is about, 
         * but the test is a context activity, and the context type is "other".
         * "other" : {"id" : "http://example.adlnet.gov/xapi/example/test"}
         * There could be an activity hierarchy to keep track of, for example question 1 on test 1 for the course Algebra 1. 
         * When recording results for question 1, it we can declare that the question is part of test 1, 
         * but also that it should be grouped with other statements about Algebra 1. This can be done using parent and grouping:
         * { 
         *   "parent" : {"id" : "http://example.adlnet.gov/xapi/example/test 1"}, 
         *   "grouping" : {"id" : "http://example.adlnet.gov/xapi/example/Algebra1"}
         * }
         * @param activitiesMap map where the values should be strings or other maps
         */
        public void setActivitiesMap(Map<String, Map<String, String>> activitiesMap) {
            this.activitiesMap = activitiesMap;
        }
        // GETTERS
        /**
         * @see #instructor
         */
        public LRS_Actor getInstructor() {
            return instructor;
        }
        /**
         * @see #revision
         */
        public String getRevision() {
            return revision;
        }
        /**
         * @see #activitiesMap
         */
        public Map<String, Map<String, String>> getActivitiesMap() {
            return activitiesMap;
        }
    }

}
