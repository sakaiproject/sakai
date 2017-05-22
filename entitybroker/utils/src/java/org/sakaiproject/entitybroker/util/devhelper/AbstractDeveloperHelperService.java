/**
 * $Id$
 * $URL$
 * DeveloperHelperServiceImpl.java - entity-broker - Apr 13, 2008 6:30:08 PM - azeckoski
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

package org.sakaiproject.entitybroker.util.devhelper;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.azeckoski.reflectutils.ReflectUtils;
import org.azeckoski.reflectutils.transcoders.HTMLTranscoder;
import org.azeckoski.reflectutils.transcoders.JSONTranscoder;
import org.azeckoski.reflectutils.transcoders.Transcoder;
import org.azeckoski.reflectutils.transcoders.XMLTranscoder;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.providers.EntityPropertiesService;
import org.sakaiproject.entitybroker.providers.EntityRESTProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * implementation of the helper service methods which are internal only
 * NOTE: you should probably override encode and decode data rather than simply using the current impl
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@Slf4j
public abstract class AbstractDeveloperHelperService implements DeveloperHelperService {

    /**
     * EMPTY - do not use
     */
    public AbstractDeveloperHelperService() { super(); }

    /**
     * MINIMAL
     * @param entityBroker the main EntityBroker service
     * @param entityBrokerManager the main EB manager service
     */
    public AbstractDeveloperHelperService(EntityBroker entityBroker,
            EntityBrokerManager entityBrokerManager) {
        super();
        this.entityBroker = entityBroker;
        this.entityBrokerManager = entityBrokerManager;
        this.requestStorage = entityBrokerManager.getRequestStorage();
        this.entityProperties = entityBrokerManager.getEntityPropertiesService();
    }

    // weak ref to ensure we do not hold anything open
    protected WeakReference<EntityRESTProvider> entityRESTProviderWeak;
    public EntityRESTProvider getEntityRESTProvider() {
        EntityRESTProvider erp = null;
        if (this.entityRESTProviderWeak == null) {
            setEntityRESTProvider( this.entityBrokerManager.getEntityRESTProvider() );
        }
        return erp;
    }
    /**
     * Set this to include an optional encoding/decoding handler
     * @param entityRESTProvider the encoding manager service
     */
    public void setEntityRESTProvider(EntityRESTProvider entityRESTProvider) {
        if (entityRESTProvider != null) {
            this.entityRESTProviderWeak = new WeakReference<EntityRESTProvider>(entityRESTProvider);
        } else {
            this.entityRESTProviderWeak = null;
        }
    }

    // ENCODING / DECODING

    public Map<String, Object> decodeData(String data, String format) {
        if (format == null) {
            format = Formats.XML;
        }
        Map<String, Object> decoded = new HashMap<String, Object>(0);
        if (getEntityRESTProvider() == null) {
            log.warn("No entityRESTProvider available for decoding, using basic internal decoder");
            if (data != null) {
                Transcoder transcoder = getTranscoder(format);
                try {
                    decoded = transcoder.decode(data);
                } catch (RuntimeException e) {
                    // convert failure to UOE
                    throw new UnsupportedOperationException("Failure encoding data ("+data+") of type ("+data.getClass()+"): " + e.getMessage(), e);
                }
            }
        } else {
            decoded = getEntityRESTProvider().decodeData(data, format);
        }
        return decoded;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.DeveloperHelperService#encodeData(java.lang.Object, java.lang.String, java.lang.String, java.util.Map)
     */
    public String encodeData(Object data, String format, String name, Map<String, Object> properties) {
        if (format == null) {
            format = Formats.XML;
        }
        String encoded = "";
        if (getEntityRESTProvider() == null) {
            log.warn("No entityRESTProvider available for encoding, using basic internal encoder");
            if (data != null) {
                Transcoder transcoder = getTranscoder(format);
                try {
                    encoded = transcoder.encode(data, name, properties);
                } catch (RuntimeException e) {
                    // convert failure to UOE
                    throw new UnsupportedOperationException("Failure encoding data ("+data+") of type ("+data.getClass()+"): " + e.getMessage(), e);
                }
            }
        } else {
            encoded = getEntityRESTProvider().encodeData(data, format, name, properties);
        }
        return encoded;
    }

    /**
     * Encoding method to use when URL encoding
     */
    public static String URL_ENCODING = "UTF-8";
    /**
     * The site reference base
     */
    public static String GROUP_BASE = "/site/";
    /**
     * The user reference base
     */
    public static String USER_BASE = "/user/";

    protected final String CURRENT_USER_MARKER = "originalCurrentUser";

    // INTERNAL
    public EntityBroker entityBroker;
    public void setEntityBroker(EntityBroker entityBroker) {
        this.entityBroker = entityBroker;
    }

    public EntityBrokerManager entityBrokerManager;
    public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
        this.entityBrokerManager = entityBrokerManager;
    }

    public RequestStorage requestStorage;
    public void setRequestStorage(RequestStorage requestStorage) {
        this.requestStorage = requestStorage;
    }

    public EntityPropertiesService entityProperties;
    /**
     * Set this to include an optional properties handler
     * @param entityProperties
     */
    public void setEntityProperties(EntityPropertiesService entityProperties) {
        this.entityProperties = entityProperties;
    }


    // ENTITY

    public boolean entityExists(String reference) {
        return entityBroker.entityExists(reference);
    }

    public Object fetchEntity(String reference) {
        Object entity = entityBroker.fetchEntity(reference);
        return entity;
    }

    public void fireEvent(String eventName, String reference) {
        entityBroker.fireEvent(eventName, reference);
    }

    public String getEntityURL(String reference, String viewKey, String extension) {
        return entityBrokerManager.getEntityURL(reference, viewKey, extension);
    }


    // CONFIG

    // USER

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.DeveloperHelperService#getCurrentLocale()
     */
    public Locale getCurrentLocale() {
        return Locale.getDefault();
    }

    public String getUserIdFromRef(String userReference) {
        String userId = null;
        if (userReference != null) {
            if (userReference.startsWith("/")) {
                // assume the form of "/user/userId" (the UDS method is protected)
                userId = new EntityReference(userReference).getId();
            } else {
                // otherwise assume this is the id
                userId = userReference;
            }
        }
        return userId;
    }

    public String getUserRefFromUserId(String userId) {
        String userRef = null;
        if (userId != null) {
            // use the UDS method for controlling its references
            userRef = USER_BASE + userId;
        }
        return userRef;
    }

    public String getCurrentUserReference() {
        String userRef = null;
        String userId = getCurrentUserId();
        if (userId != null) {
            userRef = USER_BASE + userId;
        }
        return userRef;
    }

    // LOCATION

    public String getLocationIdFromRef(String locationReference) {
        String locationId = null;
        if (locationReference != null) {
            // assume the form of "/site/siteId" (the Site method is protected)
            locationId = new EntityReference(locationReference).getId();
        }
        return locationId;
    }

    public String getCurrentLocationReference() {
        String locRef = null;
        String locId = getCurrentLocationId();
        if (locId != null) {
            locRef = GROUP_BASE + locId;
        }
        return locRef;
    }

    public String getStartingLocationReference() {
        return "/";
    }

    public String getUserHomeLocationReference(String userReference) {
        if (userReference == null) {
            userReference = getCurrentUserReference();
        }
        String userId = getUserIdFromRef(userReference);
        String locationRef = null;
        if (userId != null) {
            locationRef = GROUP_BASE + "~" + userId; // make this manually
        } else {
            log.warn("Cannot get the userhome locationReference because there is no current user: " + userReference);
        }
        return locationRef;
    }

    // TOOLS

    public String getToolIdFromToolRef(String toolReference) {
        String toolId = null;
        if (toolReference != null) {
            toolId = new EntityReference(toolReference).getId();
        }
        return toolId;
    }

    // URLS

    public String getUserHomeLocationURL(String userReference) {
        String locationReference = getUserHomeLocationReference(userReference);
        if (locationReference == null) {
            throw new IllegalArgumentException("Could not get location from userReference ("+userReference+") to generate URL");
        }
        return getLocationReferenceURL(locationReference);
    }

    public String getLocationReferenceURL(String locationReference) {
        new EntityReference(locationReference); // validate the reference
        return getPortalURL() + locationReference;
    }

    // PERMISSIONS

    /**
     * Checks to see if a request is internal and therefore can bypass some or all security
     * @param reference an entity reference string
     * @return true if internal OR false if external or REST
     */
    public boolean isEntityRequestInternal(String reference) {
        boolean internal = false;
        String origin = (String) requestStorage.getStoredValue(RequestStorage.ReservedKeys._requestOrigin.name());
        if (RequestStorage.RequestOrigin.INTERNAL.name().equals(origin)) {
            internal = true;
        } else {
            if (reference != null) {
                String ref = (String) requestStorage.getStoredValue(RequestStorage.ReservedKeys._requestEntityReference.name());
                if (reference.equals(ref)) {
                    // if this ref was the one requested from outside it is definitely not internal
                    internal = false;
                } else {
                    internal = true;
                }
            }
        }
        return internal;
    }

    // BEANS

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.DeveloperHelperService#cloneBean(java.lang.Object, int, java.lang.String[])
     */
    public <T> T cloneBean(T bean, int maxDepth, String[] propertiesToSkip) {
        return ReflectUtils.getInstance().clone(bean, maxDepth, propertiesToSkip);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.DeveloperHelperService#copyBean(java.lang.Object, java.lang.Object, int, java.lang.String[], boolean)
     */
    public void copyBean(Object orig, Object dest, int maxDepth, String[] fieldNamesToSkip,
            boolean ignoreNulls) {
        ReflectUtils.getInstance().copy(orig, dest, maxDepth, fieldNamesToSkip, ignoreNulls);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.DeveloperHelperService#populate(java.lang.Object, java.util.Map)
     */
    public List<String> populate(Object object, Map<String, Object> properties) {
        return ReflectUtils.getInstance().populate(object, properties);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.DeveloperHelperService#convert(java.lang.Object, java.lang.Class)
     */
    public <T> T convert(Object object, Class<T> type) {
        return ReflectUtils.getInstance().convert(object, type);
    }


    private Map<String, Transcoder> transcoders;
    private Transcoder getTranscoder(String format) {
        if (transcoders == null) {
            transcoders = new HashMap<String, Transcoder>();
            JSONTranscoder jt = new JSONTranscoder(true, true, false);
            transcoders.put(jt.getHandledFormat(), jt);
            XMLTranscoder xt = new XMLTranscoder(true, true, false, false);
            transcoders.put(xt.getHandledFormat(), xt);
            HTMLTranscoder ht = new HTMLTranscoder();
            transcoders.put(ht.getHandledFormat(), ht);
        }
        Transcoder transcoder = transcoders.get(format);
        if (transcoder == null) {
            throw new IllegalArgumentException("Failed to find a transcoder for format, none exists, cannot encode or decode data for format: " + format);
        }
        return transcoder;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.DeveloperHelperService#getMessage(java.lang.String, java.lang.String)
     */
    public String getMessage(String prefix, String messageKey) {
        if (entityProperties == null) {
            throw new IllegalStateException("No entityPropertiesHandler available for retrieving properties strings");
        }
        return entityProperties.getProperty(prefix, messageKey);
    }

}