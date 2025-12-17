/**
 * $Id$
 * $URL$
 * DeveloperHelperService.java - entity-broker - Apr 13, 2008 5:42:38 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.util.SakaiToolData;

/**
 * Includes methods which are likely to be helpful to developers who are implementing
 * entity providers in Sakai and working with references
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface DeveloperHelperService {

    // ENTITY

    /**
     * <b>Convenience method from {@link EntityBroker}</b><br/>
     * Check if an entity exists by the globally unique reference string, (the global reference
     * string will consist of the entity prefix and any local ID). If no {@link EntityProvider} for
     * the reference is found which implements {@link CoreEntityProvider}, this method will return
     * <code>true</code> by default, in other words, this cannot determine if a legacy
     * entity exists, only a new entity
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments (normally the id at least)
     * @return true if the entity exists, false otherwise
     */
    public boolean entityExists(String reference);

    /**
     * <b>Convenience method from {@link EntityBroker}</b><br/>
     * Get the full absolute URL to the entity view defined by these params, this will fail-safe
     * to a direct URL to an entity space URL if that is all that is available,
     * this will use the default entity URL template associated with the viewKey and include
     * an optional extension if specified (these will be inferred if they are missing)
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optionally the local id
     * @param viewKey the specific view type to get the URL for,
     * use the VIEW_* constants from {@link EntityView} (e.g. {@link EntityView#VIEW_LIST}),
     * can be null to determine the key automatically
     * @param extension the optional extension to add to the end 
     * which defines the expected data which is returned,
     * use constants in {@link Formats} (e.g. {@link Formats#XML}),
     * can be null to use no extension,  default is assumed to be html if none is set
     * @return the full URL string to a specific entity or space,
     * (e.g. http://server/direct/prefix/id)
     */
    public String getEntityURL(String reference, String viewKey, String extension);

    /**
     * <b>Convenience method from {@link EntityBroker}</b><br/>
     * Fire an event to Sakai with the specified name, targetted at the supplied reference, which
     * should be a reference to an existing entity managed by this broker<br/>
     * <b>NOTE:</b> This will allow events to be fired for references without a broker or invalid references
     * 
     * @param eventName a string which represents the name of the event (e.g. announcement.create),
     * cannot be null or empty
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments,
     * cannot be null or empty
     */
    public void fireEvent(String eventName, String reference);

    /**
     * <b>Convenience method from {@link EntityBroker}</b><br/>
     * Fetches a concrete object representing this entity reference; either one from the
     * {@link Resolvable} capability if implemented by the responsible {@link EntityProvider}, or
     * else from the underlying legacy Sakai entity system
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments
     * @return an object which represents the entity or null if none can be found
     */
    public Object fetchEntity(String reference);


    /**
     * Gets messages from the entity message bundles (the entities which implement {@link Describeable} or a sub interface),
     * 
     * @param prefix the string which represents a type of entity handled by an entity provider
     * @param messageKey the message bundle property key
     * @return the property value string OR null if none found
     */
    String getMessage(String prefix, String messageKey);

    // CONFIG

    /**
     * String type: gets the printable name of this server 
     */
    public static String SETTING_SERVER_NAME = "server.name";
    /**
     * String type: gets the unique id of this server (safe for clustering if used)
     */
    public static String SETTING_SERVER_ID = "server.cluster.id";
    /**
     * String type: gets the URL to this server
     */
    public static String SETTING_SERVER_URL = "server.main.URL";
    /**
     * String type: gets the URL to the portal on this server (or just returns the server URL if no portal in use)
     */
    public static String SETTING_PORTAL_URL = "server.portal.URL";
    /**
     * Boolean type: if true then there will be data preloads and DDL creation,
     * if false then data preloads are disabled (and will cause exceptions if preload data is missing)
     */
    public static String SETTING_AUTO_DDL = "auto.ddl";

    /**
     * Retrieves settings from the configuration service (sakai.properties)
     * 
     * @param settingName the name of the setting to retrieve,
     * Should be a string name: e.g. auto.ddl, mystuff.config, etc. OR one of the SETTING constants (e.g {@link #SETTING_AUTO_DDL})
     * 
     * @param defaultValue a specified default value to return if this setting cannot be found,
     * <b>NOTE:</b> You can set the default value to null but you must specify the class type in parens
     * @return the value of the configuration setting OR the default value if none can be found
     */
    public <T> T getConfigurationSetting(String settingName, T defaultValue);


    // USER
    public static final String ADMIN_USER_ID = "admin";
    public static final String ADMIN_USER_REF = "/user/admin";

    /**
     * Get the user entity reference (e.g. /user/{userId} - not id, eid, or username) 
     * of the current user if there is one,
     * this is not equivalent to the current user id
     * 
     * @return the user entity reference (e.g. /user/{userId} - not id, eid, or username) OR null if none
     */
    public String getCurrentUserReference();

    /**
     * Get the user entity id (e.g. not eid or username) 
     * of the current user if there is one,
     * this is not equivalent to the current user reference
     * <br/>
     * recommend you use {@link #getCurrentUserReference()} instead
     * 
     * @return the user id OR null if there is no current user (or the user is anonymous)
     */
    public String getCurrentUserId();

    /**
     * Translate the user entity reference into a userId
     * 
     * @param userReference the user entity reference (e.g. /user/{userId} - not id, eid, or username)
     * @return the userId as extracted from this user entity reference (needed for some Sakai API operations) 
     */
    public String getUserIdFromRef(String userReference);

    /**
     * Translate the userId into a user entity reference,
     * (may or may not validate the userId first)
     * 
     * @param userId the internal user Id (not the eid or username)
     * @return the user entity reference (e.g. /user/{userId})
     */
    public String getUserRefFromUserId(String userId);

    /**
     * Translate the user EID (username/loginname typicaly) into a user reference
     * 
     * @param userEid the external user Id (probably the loginname or username)
     * @return the user entity reference (e.g. /user/{userId}) OR null if the eid is invalid
     */
    public String getUserRefFromUserEid(String userEid);

    /**
     * @return the Locale for the current user or the system set locale
     */
    public Locale getCurrentLocale();

    /**
     * Set the current user to match the supplied user reference,
     * the current user reference will be stored and returned (may be null),
     * this is primarily useful when you need to switch a user to an admin or 
     * to some other user temporarily OR there is no current user but something you are
     * calling expects to find one
     * 
     * @param userReference the user entity reference (e.g. /user/{userId} - not id, eid, or username)
     * @return the previous current user entity reference
     * @throws IllegalArgumentException if the userReference is invalid
     */
    public String setCurrentUser(String userReference);

    /**
     * Restores the current user to the one from before {@link #setCurrentUser(String)} was called or
     * does nothing if there was no previous user stored
     * 
     * @return the restored current user reference OR null if there was no user to restore
     */
    public String restoreCurrentUser();


    // LOCATION

    /**
     * @return the entity reference (/site/{siteId}) of the current location for the current session
     * (represents the current site/group of the current user in the system)
     */
    public String getCurrentLocationReference();

    /**
     * @return the entity id of the current location for the current session
     * (represents the current site/group of the current user in the system)
     * <br/>
     * recommend you use {@link #getCurrentLocationReference()} instead
     */
    public String getCurrentLocationId();

    /**
     * @param locationReference the location reference (/site/{siteId})
     * @return the location id 
     */
    public String getLocationIdFromRef(String locationReference);

    /**
     * @return the entity reference of the location which is the main starting point for the system
     * (in Sakai this is probably the reference to the gateway site)
     */
    public String getStartingLocationReference();

    /**
     * Get the entity reference of the location of a user's workspace/homespace
     * @param userReference the user entity reference (e.g. /user/{userId} - not id, eid, or username)
     * @return the entity reference of the location OR null if it cannot be generated
     */
    public String getUserHomeLocationReference(String userReference);

    // TOOLS

    /**
     * @return the entity reference of the current active tool for the current session
     * (represents the tool that is currently being used by the current user in the system)
     */
    public String getCurrentToolReference();

    /**
     * Translate a tool entity reference into a tool Id 
     * 
     * @param toolReference the entity reference of a tool (e.g. /tool/{toolId})
     * @return the toolId (needed for other Sakai API operations)
     */
    public String getToolIdFromToolRef(String toolReference);

    /**
     * @param toolRegistrationId this is the id string from the Sakai
     * tool registration XML file (i.e. sakai.mytool.xml) and will 
     * probably look something like "sakai.mytool"
     * @param locationReference (optional) an entity reference to a location (e.g. /site/siteId) 
     * OR null if it should be for the current site
     * @return an object which contains data about a tool
     * @throws IllegalArgumentException if any parameters are invalid
     * or a tool with this toolRegistrationId cannot be located in the given location
     */
    public SakaiToolData getToolData(String toolRegistrationId, String locationReference);

    // PERMISSIONS

    /**
     * Check if this user has super admin level access (permissions)
     * 
     * @param userReference the user entity reference (e.g. /user/{userId} - not id, eid, or username)
     * @return true if the user has admin access, false otherwise
     */
    public boolean isUserAdmin(String userReference);

    /**
     * Check if a user has a specified permission for the entity reference, 
     * primarily a convenience method for checking location permissions
     * 
     * @param userReference the user entity reference (e.g. /user/{userId} - not id, eid, or username)
     * @param permission a permission string constant
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments (normally the id at least)
     * @return true if allowed, false otherwise
     */
    public boolean isUserAllowedInEntityReference(String userReference, String permission, String reference);

    /**
     * Find the entity references which a user has a specific permission in,
     * this is most commonly used to get the list of sites which a user has a permission in but
     * it will work for any entity type which uses Sakai permissions
     * 
     * @param userReference the user entity reference (e.g. /user/{userId} - not id, eid, or username)
     * @param permission a permission string constant
     * @return a set of entity references - a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments (normally the id at least)
     */
    public Set<String> getEntityReferencesForUserAndPermission(String userReference, String permission);

    /**
     * Get the user references which have the given permission in the given entity reference,
     * this is most commonly used to get the users which have a permission in a site but it should
     * work for any entity type which uses Sakai permissions
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments (normally the id at least)
     * @param permission a permission string constant
     * @return a set of user entity references (e.g. /user/{userId} - not id, eid, or username)
     */
    public Set<String> getUserReferencesForEntityReference(String reference, String permission);

    /**
     * Register a permission key as a valid permission for use in Sakai,
     * permissions will not appear unless they are registered each time Sakai starts
     * up so you should run this in your service init method
     * @param permission the permission key (e.g.: toolname.read.all, toolname.delete.owned)
     */
    public void registerPermission(String permission);

    /**
     * Checks to see if the current entity request is internal 
     * (and therefore can optionally bypass some or all security),
     * see the {@link RequestStorage} (from {@link RequestStorable} capability) for more info
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments (normally the id at least)
     * @return true if the current request is internal OR false if external or REST based
     */
    public boolean isEntityRequestInternal(String reference);

    // URLS

    /**
     * @return the full portal URL as Sakai understands it (e.g. http://server:port/portal)
     */
    public String getPortalURL();

    /**
     * @return the full server base URL (e.g. http://server:port)
     */
    public String getServerURL();   

    /**
     * @param locationReference an entity reference to a location (e.g. /site/siteId)
     * @return the full URL to a location (e.g. http://server:port/portal/site/siteId)
     * @throws IllegalArgumentException if this reference does not appear to be valid
     */
    public String getLocationReferenceURL(String locationReference);

    /**
     * @param userReference the user entity reference (e.g. /user/{userId} - not id, eid, or username)
     * @return the full URL to a user's workspace/homespace (e.g. http://server:port/portal/~someuser)
     * @throws IllegalArgumentException if this user reference does not appear to be valid
     */
    public String getUserHomeLocationURL(String userReference);

    /**
     * Generate a URL to a tool which will work from anywhere and 
     * can carry parameters with it<br/>
     * <b>NOTE:</b> you should set the A tag target="_top" if you
     * are inside an existing tool iFrame
     * 
     * @param toolRegistrationId this is the id string from the Sakai
     * tool registration XML file (i.e. sakai.mytool.xml) and will 
     * probably look something like "sakai.mytool"
     * @param localView (optional) the local URL of the view/page 
     * to navigate to within the tool OR null to go to the starting view/page,
     * examples: /view, /page.jsp, /path/to/someview,
     * make sure you include the leading slash ("/")
     * @param parameters (optional) a map of parameters to include
     * in the URL and send along to the tool (these will be turned
     * into GET parameters in the URL), the map should contain
     * parameterName -> parameterValue (e.g. "thing" -> "value")
     * @param locationReference (optional) an entity reference to a location (e.g. /site/siteId) 
     * OR null if it should be for the current site
     * @return a full URL to a tool (e.g. http://server:port/portal/site/siteId/page/pageId?toolstate-toolpid=/view?thing=value)
     * @throws IllegalArgumentException if any parameters are invalid
     * or a tool with this toolRegistrationId cannot be located in the given location
     */
    public String getToolViewURL(String toolRegistrationId, String localView, 
            Map<String, String> parameters, String locationReference);

    // BEANS

    /**
     * Deep clone a bean (object) and all the values in it into a brand new object of the same type,
     * this will traverse the bean and will make new objects for all non-null values contained in the object,
     * the level indicates the number of contained objects to traverse and clone,
     * setting this to zero will only clone basic type values in the bean,
     * setting this to one will clone basic fields, references, and collections in the bean,
     * etc.<br/>
     * This is mostly useful for making a copy of a hibernate object so it will no longer 
     * be the persistent object with the hibernate proxies and lazy loading
     * 
     * @param <T>
     * @param bean any java bean, this can also be a list, map, array, or any simple
     * object, it does not have to be a custom object or even a java bean,
     * also works with apache beanutils DynaBeans
     * @param maxDepth the number of objects to follow when traveling through the object and copying
     * the values from it, 0 means to only copy the simple values in the object, any objects will
     * be ignored and will end up as nulls, 1 means to follow the first objects found and copy all
     * of their simple values as well, and so forth
     * @param propertiesToSkip the names of properties to skip while cloning this object,
     * this only has an effect on the bottom level of the object, any properties found
     * on child objects will always be copied (if the maxDepth allows)
     * @return the clone of the bean
     * @throws IllegalArgumentException if there is a failure cloning the bean
     */
    public <T> T cloneBean(T bean, int maxDepth, String[] propertiesToSkip);

    /**
     * Deep copies one bean (object) into another, this is primarily for copying between identical types of objects but
     * it can also handle copying between objects which are quite different, 
     * this does not just do a reference copy of the values but actually creates new objects in the current classloader
     * and traverses through all properties of the object to make a complete deep copy
     * 
     * @param original the original object to copy from
     * @param destination the object to copy the values to (must have the same fields with the same types)
     * @param maxDepth the number of objects to follow when traveling through the object and copying
     * the values from it, 0 means to only copy the simple values in the object, any objects will
     * be ignored and will end up as nulls, 1 means to follow the first objects found and copy all
     * of their simple values as well, and so forth
     * @param fieldNamesToSkip the names of fields to skip while cloning this object,
     * this only has an effect on the bottom level of the object, any fields found
     * on child objects will always be copied (if the maxDepth allows)
     * @param ignoreNulls if true then nulls are not copied and the destination retains the value it has,
     * if false then nulls are copied and the destination value will become a null if the original value is a null
     * @throws IllegalArgumentException if the copy cannot be completed because the objects to copy do not have matching fields or types
     */
    public void copyBean(Object orig, Object dest, int maxDepth, String[] fieldNamesToSkip, boolean ignoreNulls);

    /**
     * Populates an object with the values in the properties map,
     * this will not fail if the fieldName in the map is not a property on the
     * object or the fieldName cannot be written to with the value in the object.
     * This will attempt to convert the provided object values into the right values
     * to place in the object<br/>
     * <b>NOTE:</b> simple types like numbers and strings can almost always be converted from
     * just about anything though they will probably end up as 0 or ""<br/>
     * Setting fields supports simple, nested, indexed, and mapped values:<br/>
     * <b>Simple:</b> Get/set a field in a bean (or map), Example: "title", "id"<br/>
     * <b>Nested:</b> Get/set a field in a bean which is contained in another bean, Example: "someBean.title", "someBean.id"<br/>
     * <b>Indexed:</b> Get/set a list/array item by index in a bean, Example: "myList[1]", "anArray[2]"<br/>
     * <b>Mapped:</b> Get/set a map entry by key in a bean, Example: "myMap(key)", "someMap(thing)"<br/>
     * 
     * @param object any object
     * @param properties a map of fieldNames -> Object
     * @return the list of fieldNames which were successfully written to the object
     * @throws IllegalArgumentException if the arguments are invalid
     */
    public List<String> populate(Object object, Map<String, Object> properties);

    /**
     * Converts an object to any other object if possible
     * 
     * @param <T>
     * @param object any object
     * @param type any class type that you want to try to convert the object to
     * @return the converted value (allows null to pass through except in the case of primitives which become the primitive default)
     * @throws UnsupportedOperationException if the conversion cannot be completed
     */
    public <T> T convert(Object object, Class<T> type);

    /**
     * Encode data into a given format, can handle any java object,
     * note that unsupported formats and invalid data will result in an exception <br/>
     * Current formats supported: JSON, XML, HTML
     * 
     * @param data the data to encode
     * @param format the format to use for output (from {@link Formats})
     * @param name (optional) the name to use for the encoded data (e.g. root node for XML)
     * @param properties (optional) extra properties to add into the encoding, ignored if encoded object is not a map or bean
     * @return the encoded string in the requested format
     * @throws UnsupportedOperationException if the data cannot be encoded
     * @throws IllegalArgumentException if the format requested cannot be encoded because there is no encoder
     */
    public String encodeData(Object data, String format, String name, Map<String, Object> properties);

    /**
     * Decode a string of a specified format into a java map of simple objects <br/> 
     * Returned map can be fed into {@link #populate(Object, Map)} if you want to convert it
     * into a known object type <br/> 
     * Types are likely to require conversion as guesses are made about the right formats,
     * use of the {@link #convert(Object, Class)} method is recommended <br/>
     * Current formats supported: JSON, XML
     * 
     * @param data encoded data
     * @param format the format of the encoded data (from {@link Formats})
     * @return a map containing all the data derived from the encoded data
     * @throws UnsupportedOperationException if the data cannot be decoded
     * @throws IllegalArgumentException if the data cannot be decoded because there is no decoder for that format
     */
    public Map<String, Object> decodeData(String data, String format);

}
