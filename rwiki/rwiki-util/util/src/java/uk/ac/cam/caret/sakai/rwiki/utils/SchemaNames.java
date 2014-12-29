/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.utils;

/**
 * @author ieb
 */
public class SchemaNames
{
	/**
	 * event name space.
	 */
	public static final String NS_EVENTS = "";

	/**
	 * root element of events.
	 */
	public static final String EL_EVENTS = "events";

	/**
	 * root element of events.
	 */
	public static final String EL_NSEVENTS = "events";

	/**
	 * event container.
	 */
	public static final String EL_EVENT = "event";

	/**
	 * event container.
	 */
	public static final String EL_NSEVENT = "event";

	public final static String EL_EXCEPTION = "exception";

	public final static String EL_NSEXCEPTION = "exception";

	public final static String ATTR_EXMESSAGE = "message";

	/**
	 * Name space of the xml container.
	 */
	public static final String NS_CONTAINER = "";

	/**
	 * Top level elemenr local name.
	 */
	public static final String EL_XMLSERVICE = "xmlservice";

	/**
	 * Top level element qname.
	 */
	public static final String EL_NSXMLSERVICE = "xmlservice";

	/**
	 * properties container.
	 */
	public static final String EL_XMLPROPERTIES = "properties";

	/**
	 * properties container.
	 */
	public static final String EL_NSXMLPROPERTIES = "properties";

	/**
	 * Property element.
	 */
	public static final String EL_XMLPROPERTY = "property";

	/**
	 * property element.
	 */
	public static final String EL_NSXMLPROPERTY = "property";

	/**
	 * name of the attribute.
	 */
	public static final String ATTR_NAME = "name";

	public final static String NS_PROPS = "";

	public final static String EL_PROPERTIES = "rproperties";

	public final static String EL_NSPROPERTIES = "rproperties";

	public final static String EL_PROPERTY = "rproperty";

	public final static String EL_NSPROPERTY = "rproperty";

	public final static String ATTR_LIST = "name";

	public final static String EL_NAME = "name";

	public final static String EL_VALUE = "value";

	/**
	 * Name of the event.
	 */
	public static final String EL_EVENTNAME = "eventname";

	/**
	 * Name of the event.
	 */
	public static final String EL_NSEVENTNAME = "eventname";

	/**
	 * Name of the event.
	 */
	public static final String EL_EVENTTIMESTAMP = "eventtimestamp";

	/**
	 * Name of the event.
	 */
	public static final String EL_NSEVENTTIMESTAMP = "eventtimestamp";

	/**
	 * Has the subject been modified.
	 */
	public static final String EL_MODIFY = "modify";

	/**
	 * Has the subject been modified.
	 */
	public static final String EL_NSMODIFY = "modify";

	/**
	 * Priority.
	 */
	public static final String EL_PRIORITY = "priority";

	/**
	 * Priority.
	 */
	public static final String EL_NSPRIORITY = "priority";

	/**
	 * Resource event is about.
	 */
	public static final String EL_RESOURCE = "resource";

	/**
	 * Resource event is about.
	 */
	public static final String EL_NSRESOURCE = "resource";

	/**
	 * Session ID.
	 */
	public static final String EL_SESSIONID = "sessionid";

	/**
	 * Session ID.
	 */
	public static final String EL_NSSESSIONID = "sessionid";

	/**
	 * User ID.
	 */
	public static final String EL_USERID = "userid";

	/**
	 * User ID.
	 */
	public static final String EL_NSUSERID = "userid";

	/**
	 * A wiki Page
	 */
	public static final String EL_RENDEREDCONTENT = "rendered-content";

	/**
	 * 
	 */
	public static final String EL_ENTITYSERVICE = "entity-service";

	/**
	 * 
	 */
	public static final String EL_ENTITY = "entity";
	
	public static final String EL_SIDEBAR = "sidebar";

	public static final String EL_ERROR = "error";

	public static final String EL_ERRORDESC = "error-description";

	public static final String EL_RAWCONTENT = "wiki-content";

	public static final String EL_REQUEST_PARAM = "request-param";

	public static final String EL_NSVALUE = "value";

	public static final String EL_REQUEST_PARAMS = "request-params";

	public static final String EL_REQUEST_PROPERTIES = "request-properties";

	public static final String EL_REQUEST_ATTRIBUTES = "request-attributes";

	public static final String EL_REQUEST_ATTRIBUTE = "request-attribute";

	public static final String ATTR_REQUEST_PATH_INFO = "request-path-info";

	public static final String ATTR_REQUEST_USER = "request-user";

	public static final String ATTR_REQUEST_PROTOCOL = "request-protocol";

	public static final String ATTR_REQUEST_SERVER_NAME = "request-servername";

	public static final String ATTR_REQUEST_SERVER_PORT = "request-serverport";

	public static final String ATTR_REQUEST_REQUEST_URL = "request-url";

	public static final String EL_NSENTITYSERVICE = "entity-service";

	public static final String EL_NSREQUEST_PROPERTIES = "request-properties";

	public static final String EL_NSSIDEBAR = "sidebar";
	
	public static final String EL_NSENTITY = "entity";

	public static final String EL_NSRENDEREDCONTENT = "rendered-content";

	public static final String EL_NSERROR = "error";

	public static final String EL_NSERRORDESC = "error-description";

	public static final String EL_NSRAWCONTENT = "wiki-content";

	public static final String EL_NSREQUEST_ATTRIBUTES = "request-attributes";

	public static final String EL_NSREQUEST_ATTRIBUTE = "request-attribute";

	public static final String EL_NSREQUEST_PARAMS = "request-params";

	public static final String EL_NSREQUEST_PARAM = "request-param";

	public static final String EL_CHANGES = "changes";

	public static final String EL_NSCHANGES = "changes";

	public static final String ATTR_ID = "eid";

	public static final String ATTR_OWNER = "owner";

	public static final String ATTR_REALM = "pagerealm";

	public static final String ATTR_REFERENCED = "referenced";

	public static final String ATTR_SHA1 = "sha1";

	public static final String ATTR_USER = "last-edited-by";

	public static final String ATTR_REVISION = "revision";

	public static final String ATTR_LAST_CHANGE = "last-modified";

	public static final String EL_CHANGE = "change";

	public static final String EL_NSCHANGE = "change";

	public static final String ATTR_SERVER_URL = "server-url";

	public static final String EL_WIKIPAGE = "wikipage";

	public static final String ATTR_PAGE_NAME = "page-name";

	public static final String EL_WIKICONTENT = "wikicontent";

	public static final String EL_NSWIKICONTENT = "wikicontent";

	public static final String ATTR_LOCAL_NAME = "local-name";

	public static final String ATTR_DISPLAY_USER = "user-display";

	public static final String EL_PAGEVISITS = "page-visits";
	
	public static final String EL_NSPAGEVISITS = "page-visits";

	public static final String EL_PAGEVISIT = "page-visit";

	public static final String EL_NSPAGEVISIT = "page-visit";

	public static final String ATTR_URL = "url";
}
