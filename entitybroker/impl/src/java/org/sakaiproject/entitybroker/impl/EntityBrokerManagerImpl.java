/**
 * $Id$
 * $URL$
 * EntityBrokerManager.java - entity-broker - Jul 22, 2008 11:33:39 AM - azeckoski
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

package org.sakaiproject.entitybroker.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.azeckoski.reflectutils.ConstructorUtils;
import org.azeckoski.reflectutils.ReflectUtils;
import org.azeckoski.reflectutils.exceptions.FieldnameNotFoundException;
import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.access.AccessViews;
import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderMethodStore;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityTitle;
import org.sakaiproject.entitybroker.entityprovider.capabilities.BrowseNestable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.BrowseSearchable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Browseable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.BrowseableCollection;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.EntityViewUrlCustomizable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ReferenceParseable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Sampleable;
import org.sakaiproject.entitybroker.entityprovider.extension.BrowseEntity;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetterWrite;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorageWrite;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.providers.EntityPropertiesService;
import org.sakaiproject.entitybroker.providers.EntityRESTProvider;
import org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider;
import org.sakaiproject.entitybroker.util.EntityDataUtils;
import org.sakaiproject.entitybroker.util.request.RequestUtils;


/**
 * This is the internal service for handling entities,
 * most of the work done by entity broker is handled here<br/>
 * This should be used in
 * preference to the EntityBroker directly by implementation classes 
 * that are part of the EntityBroker system, 
 * rather than the user-facing EntityBroker directly.
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityBrokerManagerImpl implements EntityBrokerManager {
    public static String SVN_REVISION = "$Revision$";
    public static String SVN_LAST_UPDATE = "$Date$";

    public String getVersionInfo() {
        return "MANAGER:: SVN: " + SVN_REVISION + " : " + SVN_LAST_UPDATE;
    }

    public EntityBrokerManagerImpl() { }

    /**
     * Full constructor
     */
    public EntityBrokerManagerImpl(EntityProviderManager entityProviderManager,
            EntityPropertiesService entityPropertiesService,
            EntityViewAccessProviderManager entityViewAccessProviderManager) {
        this(entityProviderManager, entityPropertiesService, entityViewAccessProviderManager, null);
    }

    /**
     * Constructor with optional {@link ExternalIntegrationProvider}
     */
    public EntityBrokerManagerImpl(EntityProviderManager entityProviderManager,
            EntityPropertiesService entityPropertiesService,
            EntityViewAccessProviderManager entityViewAccessProviderManager,
            ExternalIntegrationProvider externalIntegrationProvider) {
        super();
        if (entityProviderManager == null) {
            throw new IllegalArgumentException("entityProviderManager cannot be null");
        }
        this.entityProviderManager = entityProviderManager;
        this.entityPropertiesService = entityPropertiesService;
        this.entityViewAccessProviderManager = entityViewAccessProviderManager;
        this.externalIntegrationProvider = externalIntegrationProvider;
	}

	public void init() {

		// Set the maximum depth of object graph which can be transcoded into JSON.
		// An integer between 4 and 26. Anything below 5 is set at 5, anything above 25 is set to 25.
		String maxJSONLevelString = externalIntegrationProvider.getMaxJSONLevel();

		try {
			maxJSONLevel = Integer.parseInt(maxJSONLevelString);
			if(this.maxJSONLevel < 5) {
				this.maxJSONLevel = 5;
			}
			else if(this.maxJSONLevel > 25) {
				this.maxJSONLevel = 25;
			}
		}
		catch(NumberFormatException nfe) {
		}
    }

    private EntityProviderManager entityProviderManager;
    private EntityPropertiesService entityPropertiesService;
    private EntityViewAccessProviderManager entityViewAccessProviderManager;
    private ExternalIntegrationProvider externalIntegrationProvider;
    private EntityRESTProvider entityRESTProvider;

	private int maxJSONLevel = 7;

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBrokerManager#getMaxJSONLevel()
     */
	public int getMaxJSONLevel() {
		return maxJSONLevel;
	}

    private String defaultServletContext = RequestUtils.getServletContext(null);
    /**
     * We have to do something fairly tricky here because we really need this to be handled
     * on a per thread basis (or at least a per servlet basis anyway) so we need to store the "default"
     * and then also store a threadlocal for each servlet to set and fallback if the TL is not set,
     * what a giant PITA but this should allow multiple servlets to instantiate the EB handlers
     */
    private final ThreadLocal<String> localServletContext = new ThreadLocal<String>();
    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBrokerManager#setServletContext(java.lang.String)
     */
    public void setServletContext(String servletContext) {
        if (servletContext != null) {
            this.localServletContext.set(servletContext);
        } else {
            this.localServletContext.set(null);
        }
    }
    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBrokerManager#getServletContext()
     */
    public String getServletContext() {
        String context = this.defaultServletContext;
        if (this.localServletContext != null && this.localServletContext.get() != null) {
            context = this.localServletContext.get();
        }
        return context;
    }


    /**
     * Determines if an entity exists based on the reference
     * 
     * @param reference an entity reference object
     * @return true if entity exists, false otherwise
     */
    public boolean entityExists(EntityReference ref) {
        boolean exists = false;
        if (ref != null) {
            EntityProvider provider = entityProviderManager.getProviderByPrefix(ref.getPrefix());
            if (provider == null) {
                // no provider found so no entity can't exist
                exists = false;
            } else if (!(provider instanceof CoreEntityProvider)) {
                // no core provider so assume it does exist
                exists = true;
            } else {
                if (ref.getId() == null) {
                    // currently we assume exists if it is only a prefix
                    exists = true;
                } else {
                    exists = ((CoreEntityProvider) provider).entityExists( ref.getId() );
                }
            }
        }
        return exists;
    }

    /**
     * Creates the full URL to an entity using the sakai {@link ServerConfigurationService}, 
     * (e.g. http://server:8080/direct/entity/123/)<br/>
     * <br/>
     * <b>Note:</b> the webapp name (relative URL path) of the direct servlet, of "/direct" 
     * is hardcoded into this method, and the
     * {@link org.sakaiproject.entitybroker.util.servlet.DirectServlet} must be deployed there on this
     * server.
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optionally the local id
     * @param viewKey the specific view type to get the URL for,
     * can be null to determine the key automatically
     * @param extension the optional extension to add to the end,
     * can be null to use no extension
     * @return the full URL to a specific entity or space
     */
    public String getEntityURL(String reference, String viewKey, String extension) {
        // ensure this is a valid reference first
        EntityReference ref = parseReference(reference);
        EntityView view = makeEntityView(ref, viewKey, extension);
        String url = makeFullURL(view.toString());
        return url;
    }

    /**
     * Make a full URL (http://....) from just a path URL (/prefix/id.xml)
     */
    public String makeFullURL(String pathURL) {
        String serverUrl = "http://localhost:8080";
        if (externalIntegrationProvider != null) {
            serverUrl = externalIntegrationProvider.getServerUrl();
        }
        String url = serverUrl + getServletContext() + pathURL;
        return url;
    }

    /**
     * Reduce code duplication and ensure custom templates are used
     */
    public EntityView makeEntityView(EntityReference ref, String viewKey, String extension) {
        if (ref == null) {
            throw new IllegalArgumentException("ref cannot be null");
        }
        EntityView view = new EntityView();
        EntityViewUrlCustomizable custom = (EntityViewUrlCustomizable) entityProviderManager
        .getProviderByPrefixAndCapability(ref.getPrefix(), EntityViewUrlCustomizable.class);
        if (custom != null) {
            // use the custom parsing templates
            view.loadParseTemplates( custom.getParseTemplates() );
        }
        view.setEntityReference(ref);
        if (viewKey != null) {
            view.setViewKey(viewKey);
        }
        if (extension != null) {
            view.setExtension(extension);
        }
        return view;
    }

    /**
     * Parses an entity reference into the appropriate reference form
     * 
     * @param reference a unique entity reference
     * @return the entity reference object or 
     * null if there is no provider found for the prefix parsed out
     * @throws IllegalArgumentException if there is a failure during parsing
     */
    public EntityReference parseReference(String reference) {
        String prefix = EntityReference.getPrefix(reference);
        EntityReference ref = null;
        if (entityProviderManager.getProviderByPrefix(prefix) != null) {
            ReferenceParseable provider = entityProviderManager.getProviderByPrefixAndCapability(prefix, ReferenceParseable.class);
            if (provider == null) {
                ref = new EntityReference(reference);
            } else {
                EntityReference exemplar = provider.getParsedExemplar();
                if (exemplar == null) {
                    ref = new EntityReference(reference);
                } else {
                    if (exemplar.getClass() == EntityReference.class) {
                        ref = new EntityReference(reference);
                    } else {
                        // construct the custom class and then return it
                        try {
                            Constructor<? extends Object> m = exemplar.getClass().getConstructor(String.class);
                            ref = (EntityReference) m.newInstance(reference);
                        } catch (SecurityException e) {
                            throw new RuntimeException("Failed to invoke a constructor which takes a single string "
                                    + "(reference="+reference+") for class: " + exemplar.getClass(), e);
                        } catch (IllegalArgumentException e) {
                            throw new RuntimeException("Failed to invoke a constructor which takes a single string "
                                    + "(reference="+reference+") for class: " + exemplar.getClass(), e);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException("Failed to invoke a constructor which takes a single string "
                                    + "(reference="+reference+") for class: " + exemplar.getClass(), e);
                        } catch (InstantiationException e) {
                            throw new RuntimeException("Failed to invoke a constructor which takes a single string "
                                    + "(reference="+reference+") for class: " + exemplar.getClass(), e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Failed to invoke a constructor which takes a single string "
                                    + "(reference="+reference+") for class: " + exemplar.getClass(), e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException("Failed to invoke a constructor which takes a single string "
                                    + "(reference="+reference+") for class: " + exemplar.getClass(), e);
                        }
                    }
                }
            }
        }
        return ref;
    }

    /**
     * Parses an entity URL into an entity view object,
     * handles custom parsing templates
     * 
     * @param entityURL an entity URL
     * @return the entity view object representing this URL or 
     * null if there is no provider found for the prefix parsed out
     * @throws IllegalArgumentException if there is a failure during parsing
     */
    public EntityView parseEntityURL(String entityURL) {
        if (entityURL == null || "".equals(entityURL)) {
            throw new IllegalArgumentException("entityURL cannot be null or empty");
        }
        // strip off the /direct if this url starts with that
        if (entityURL.startsWith(EntityView.DIRECT_PREFIX)) {
            entityURL = entityURL.substring(EntityView.DIRECT_PREFIX.length());
        }
        EntityView view = null;
        // first get the prefix
        String prefix = EntityReference.getPrefix(entityURL);
        // get the basic provider to see if this prefix is valid
        EntityProvider provider = entityProviderManager.getProviderByPrefix(prefix);
        if (provider != null) {
            // this prefix is valid so check for custom entity templates
            EntityViewUrlCustomizable custom = (EntityViewUrlCustomizable) entityProviderManager
            .getProviderByPrefixAndCapability(prefix, EntityViewUrlCustomizable.class);
            if (custom == null) {
                view = new EntityView(entityURL);
            } else {
                // use the custom parsing templates to build the object
                view = new EntityView();
                view.loadParseTemplates( custom.getParseTemplates() );
                view.parseEntityURL(entityURL);
            }
        }
        return view;
    }

    /**
     * Get an entity object of some kind for this reference if it has an id,
     * will simply return null if no id is available in this reference
     * 
     * @param ref an entity reference
     * @return the entity object for this reference OR null if none can be retrieved
     */
    public Object fetchEntity(EntityReference ref) {
        if (ref == null) {
            throw new IllegalArgumentException("ref cannot be null");
        }
        Object entity = fetchEntityObject(ref);
        if (entity != null) {
            entity = EntityDataUtils.convertToEntity(entity);
        }
        return entity;
    }

    /**
     * Get the entity data for a reference if possible
     * 
     * @param ref an entity reference
     * @return an {@link EntityData} object for this reference if one can be found OR null if not
     */
    public EntityData getEntityData(EntityReference ref) {
        if (ref == null) {
            throw new IllegalArgumentException("ref cannot be null");
        }
        EntityData ed = null;
        Object obj = fetchEntityObject(ref);
        if (obj != null) {
            ed = EntityDataUtils.makeEntityData(ref, obj);
            populateEntityData(new EntityData[] {ed} );
        } else {
            if (entityExists(ref)) {
                String url = getEntityURL(ref.toString(), EntityView.VIEW_SHOW, null);
                ed = new EntityData(ref, (String)null);
                ed.setEntityURL(url);
            }
        }
        return ed;
    }

    /**
     * Get the entity without a change (may be EntityData or just an object)
     */
    protected Object fetchEntityObject(EntityReference ref) {
        Object entity = null;
        Resolvable provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), Resolvable.class);
        if (provider != null) {
            entity = provider.getEntity(ref);
        }
        return entity;
    }

    /**
     * Get a list of entities from {@link CollectionResolvable} first if available or {@link BrowseSearchable} if not,
     * returns the entities as actual entities (converts from {@link EntityData} if that was used),
     * correctly handles references to single entities as well
     * 
     * @param ref the reference
     * @param search a search (should not be null)
     * @param params
     * @return a list of entities OR empty list if none found for the given reference
     */
    public List<?> fetchEntities(EntityReference ref, Search search, Map<String, Object> params) {
        List<?> entities = internalGetEntities(ref, search, params);
        entities = EntityDataUtils.convertToEntities(entities);
        return entities;
    }

    /**
     * Get a list of entities from {@link CollectionResolvable} first if available or {@link BrowseSearchable} if not,
     * returns the entities wrapped in {@link EntityData},
     * correctly handles references to single entities as well
     * 
     * @param ref the reference
     * @param search a search (should not be null)
     * @param params
     * @return a list of entities OR empty list if none found for the given reference
     */
    public List<EntityData> getEntitiesData(EntityReference ref, Search search, Map<String, Object> params) {
        List<?> entities = internalGetEntities(ref, search, params);
        List<EntityData> data = convertToEntityData(entities, ref);
        return data;
    }

    /**
     * Fetches the browseable entities
     * @param prefix
     * @param search
     * @param userReference
     * @param associatedReference
     * @param parentEntityRef
     * @param params
     * @return a list of entity data results to browse
     */
    public List<EntityData> browseEntities(String prefix, Search search,
            String userReference, String associatedReference, EntityReference parentEntityRef, Map<String, Object> params) {
        if (prefix == null) {
            throw new IllegalArgumentException("No prefix supplied for entity browsing resolution, prefix was null");
        }
        List<EntityData> results = null;
        search = EntityDataUtils.translateStandardSearch(search);
        if (parentEntityRef != null) {
            // do the special call to get nested children items
            BrowseNestable nestable = entityProviderManager.getProviderByPrefixAndCapability(prefix, BrowseNestable.class);
            if (nestable != null) {
                List<EntityData> l = nestable.getChildrenEntities(parentEntityRef, search, userReference, associatedReference, params);
                if (l != null) {
                    results = new ArrayList<EntityData>( l );
                }
                populateEntityData( l );
            }
        } else {
            // check for browse searchable first
            BrowseSearchable searchable = entityProviderManager.getProviderByPrefixAndCapability(prefix, BrowseSearchable.class);
            if (searchable != null) {
                List<EntityData> l = searchable.browseEntities(search, userReference, associatedReference, params);
                if (l != null) {
                    results = new ArrayList<EntityData>( l );
                }
                populateEntityData( l );
            } else {
                // get from the collection if available
                BrowseableCollection provider = entityProviderManager.getProviderByPrefixAndCapability(prefix, BrowseableCollection.class);
                if (provider != null) {
                    EntityReference ref = new EntityReference(prefix, "");
                    List<?> l = getEntitiesData(ref, search, params);
                    results = convertToEntityData(l, ref);
                }
            }
        }
        if (results == null) {
            results = new ArrayList<EntityData>();
        }
        return results;
    }

    /**
     * Get the meta data about browseable entities
     * @param parentPrefix the prefix of the parent type (null for the root types)
     * @return the list of browseable entity meta data
     */
    public List<BrowseEntity> getBrowseableEntities(String parentPrefix) {
        Map<String, BrowseEntity> results = new HashMap<String, BrowseEntity>();
        // first we sorta have to get all the browseable entities info
        List<Browseable> browseableProviders = entityProviderManager.getProvidersByCapability(Browseable.class);
        for (Browseable browseable : browseableProviders) {
            String prefix = browseable.getEntityPrefix();
            // add the current prefix to the list of browseable entities
            BrowseEntity currentBE = results.get(prefix);
            if (currentBE == null) {
                currentBE = new BrowseEntity(prefix);
                results.put(prefix, currentBE);
            }
            BrowseNestable bnProvider = entityProviderManager.getProviderByPrefixAndCapability(prefix, BrowseNestable.class);
            if (bnProvider != null) {
                // this must be a non-root node
                results.put(prefix, new BrowseEntity(prefix));
                String parent = bnProvider.getParentprefix();
                currentBE.setParentPrefix(parent); // add this parent prefix to current

                BrowseEntity parentBE = results.get(parent);
                // make the parent if it does not exist yet
                if (parentBE == null) {
                    parentBE = new BrowseEntity(parent);
                    results.put(parent, parentBE);
                }
                parentBE.addNestedPrefix(prefix); // add the child prefix to the parent
            }
            // add in the title/desc if available
            Describeable dProvider = entityProviderManager.getProviderByPrefixAndCapability(prefix, Describeable.class);
            if (dProvider != null) {
                String title = entityPropertiesService.getProperty(prefix, Browseable.BROWSE_TITLE_KEY);
                String description = entityPropertiesService.getProperty(prefix, Browseable.BROWSE_DESC_KEY);
                currentBE.setTitleDesc(title, description);
            }
            // add in the access views info
            EntityViewAccessProvider evap = entityViewAccessProviderManager.getProvider(prefix);
            if (evap != null) {
                if (AccessViews.class.isAssignableFrom(evap.getClass())) {
                    String[] entityViewKeys = ((AccessViews)evap).getHandledEntityViews();
                    currentBE.setEntityViewKeys(entityViewKeys);
                }
            }
        }
        // now filter down to what was asked for only
        List<BrowseEntity> l = new ArrayList<BrowseEntity>();
        for (BrowseEntity browseEntity : results.values()) {
            if (parentPrefix == null) {
                // get root items only
                if (browseEntity.getParentPrefix() == null) {
                    l.add(browseEntity);
                }
            } else {
                // get items with matching parents only
                if (browseEntity.getParentPrefix() != null 
                        && parentPrefix.equals(browseEntity.getParentPrefix())) {
                    l.add(browseEntity);
                }
            }
        }
        Collections.sort(l, new BrowseEntity.TitleComparator());
        return l;
    }

    /**
     * INTERNAL usage:
     * Get a list of entities from {@link CollectionResolvable} first if available or {@link BrowseSearchable} if not,
     * returns the entities as whatever they were returned as, EntityData would need to be populated still,
     * correctly handles references to single entities as well
     * 
     * @param ref the reference
     * @param search a search (should not be null)
     * @param params
     * @return a list of entities OR empty list if none found for the given reference
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected List<?> internalGetEntities(EntityReference ref, Search search, Map<String, Object> params) {
        if (ref == null) {
            throw new IllegalArgumentException("No reference supplied for entity collection resolution, ref was null");
        }
        // get the entities to output
        List entities = null;
        if (ref.getId() == null) {
            // encoding a collection of entities
            CollectionResolvable provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), CollectionResolvable.class);
            if (provider != null) {
                search = EntityDataUtils.translateStandardSearch(search);
                List<?> l = provider.getEntities(ref, search);
                if (l != null) {
                    entities = new ArrayList( l );
                }
            } else {
                BrowseSearchable searchable = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), BrowseSearchable.class);
                if (searchable != null) {
                    search = EntityDataUtils.translateStandardSearch(search);
                    List<?> l = searchable.browseEntities(search, null, null, params);
                    if (l != null) {
                        entities = new ArrayList( l );
                    }
                }
            }
        } else {
            // encoding a single entity
            Object entity = fetchEntityObject(ref);
            if (entity == null) {
                throw new EntityException("Failed to retrieve entity (" + ref + "), entity object could not be found",
                        ref.toString(), HttpServletResponse.SC_NOT_FOUND);
            }
            entities = new ArrayList(1);
            entities.add(entity);
        }
        // make sure no null is returned
        if (entities == null) {
            entities = new ArrayList<String>();
        }
        return entities;
    }

    /**
     * Convert a list of objects to entity data objects (also populates them),
     * will preserve null (i.e. null in => null out)
     */
    public List<EntityData> convertToEntityData(List<?> entities, EntityReference ref) {
        List<EntityData> l = EntityDataUtils.convertToEntityData(entities, ref);
        populateEntityData(l);
        return l;
    }

    /**
     * Convert a single object to an entity data object (also populates it),
     * will preserve null (i.e. null in => null out)
     */
    public EntityData convertToEntityData(Object entity, EntityReference ref) {
        EntityData ed = EntityDataUtils.convertToEntityData(entity, ref);
        if (ed != null) {
            populateEntityData( new EntityData[] {ed} );
        }
        return ed;
    }

    /**
     * Add in the extra meta data (URL, title, etc.) to all entity data objects,
     * handles it as efficiently as possible without remaking an entity view on every call,
     * this is fail safe (i.e. it should throw no exceptions)
     * 
     * @param data a list of entity data
     */
    public void populateEntityData(List<EntityData> data) {
        if (data != null && ! data.isEmpty()) {
            populateEntityData(data.toArray(new EntityData[data.size()]));
        }
    }

    /**
     * Add in the extra meta data (URL, title, etc.) to all entity data objects,
     * handles it as efficiently as possible without remaking an entity view on every call,
     * this is fail safe (i.e. it should throw no exceptions)
     * 
     * @param data a list of entity data
     */
    public void populateEntityData(EntityData[] data) {
        if (data == null) {
            return;
        }
        HashMap<String, EntityView> views = new HashMap<String, EntityView>();
        for (EntityData entityData : data) {
            if (entityData.isPopulated() || entityData.isDataOnly()) {
                continue;
            } else {
                entityData.setPopulated(true);
            }
            // set URL
            EntityReference ref = entityData.getEntityRef();
            EntityView view = null;
            if (views.containsKey(ref.getPrefix())) {
                view = views.get(ref.getPrefix());
            } else {
                view = makeEntityView(ref, EntityView.VIEW_SHOW, null);
                views.put(ref.getPrefix(), view);
            }
            view.setEntityReference(ref);
            String partialURL = view.getEntityURL();
            String fullURL = makeFullURL( partialURL );
            entityData.setEntityURL( fullURL );
            // check what we are dealing with
            boolean isPOJO = false;
            if (entityData.getData() != null) {
                if ( ConstructorUtils.isClassBean(entityData.getData().getClass()) ) {
                    isPOJO = true;
                }
            }
            // attempt to set display title if not set
            if (! entityData.isDisplayTitleSet()) {
                boolean titleNotSet = true;
                // check properties first
                if (entityData.getEntityProperties() != null) {
                    String title = EntityDataUtils.findMapStringValue(entityData.getEntityProperties(), new String[] {"displayTitle","title","displayName","name"});
                    if (title != null) {
                        entityData.setDisplayTitle(title);
                        titleNotSet = false;
                    }
                }
                // check the object itself next
                if (isPOJO && titleNotSet) {
                    try {
                        String title = ReflectUtils.getInstance().getFieldValueAsString(entityData.getData(), "title", EntityTitle.class);
                        if (title != null) {
                            entityData.setDisplayTitle(title);
                            titleNotSet = false;
                        }
                    } catch (FieldnameNotFoundException e) {
                        // could not find any fields with the title, nothing to do but continue
                    }
                }
            }
            // done with this entity data
        }
    }


    /**
     * Safely get the sample entity object which is defined for a prefix,
     * if there is not one then return null
     * @param prefix the entity prefix
     * @param id (optional) will get the actual entity for this id as a sample
     * @return a sample object OR null if none can be found
     */
    public Object getSampleEntityObject(String prefix, String id) {
        Object entity = null;
        if (id != null) {
            // get the current entity if possible
            try {
                Resolvable resolvable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Resolvable.class);
                if (resolvable != null) {
                    entity = resolvable.getEntity( new EntityReference(prefix, id) );
                }
            } catch (RuntimeException e) {
                // failed to get it
                entity = null;
            }
        }
        if (entity == null) {
            // get a sample entity if possible
            try {
                Sampleable sampleable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Sampleable.class);
                if (sampleable != null) {
                    entity = sampleable.getSampleEntity();
                }
            } catch (RuntimeException e) {
                entity = null;
            }
            if (entity == null) {
                // get an entity from resolveable as a last resort
                try {
                    Resolvable resolvable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Resolvable.class);
                    if (resolvable != null) {
                        entity = resolvable.getEntity( new EntityReference(prefix, "") );
                    }
                } catch (RuntimeException e) {
                    entity = null;
                }
            }
        }
        return entity;
    }

    // GETTERS

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBrokerManager#getEntityRESTProvider()
     */
    public EntityRESTProvider getEntityRESTProvider() {
        return entityRESTProvider;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.impl.EntityBrokerManager#getExternalIntegrationProvider()
     */
    public ExternalIntegrationProvider getExternalIntegrationProvider() {
        return this.externalIntegrationProvider;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBrokerManager#getEntityProviderManager()
     */
    public EntityProviderManager getEntityProviderManager() {
        return entityProviderManager;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBrokerManager#getEntityPropertiesService()
     */
    public EntityPropertiesService getEntityPropertiesService() {
        return entityPropertiesService;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBrokerManager#getEntityViewAccessProviderManager()
     */
    public EntityViewAccessProviderManager getEntityViewAccessProviderManager() {
        return entityViewAccessProviderManager;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBrokerManager#getEntityProviderMethodStore()
     */
    public EntityProviderMethodStore getEntityProviderMethodStore() {
        return entityProviderManager.getEntityProviderMethodStore();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBrokerManager#getRequestGetter()
     */
    public RequestGetterWrite getRequestGetter() {
        return entityProviderManager.getRequestGetter();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBrokerManager#getRequestStorage()
     */
    public RequestStorageWrite getRequestStorage() {
        return entityProviderManager.getRequestStorage();
    }


    // SETTERS

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBrokerManager#setEntityRESTProvider(org.sakaiproject.entitybroker.providers.EntityRESTProvider)
     */
    public void setEntityRESTProvider(EntityRESTProvider entityRESTProvider) {
        this.entityRESTProvider = entityRESTProvider;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityBrokerManager#setExternalIntegrationProvider(org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider)
     */
    public void setExternalIntegrationProvider(ExternalIntegrationProvider externalIntegrationProvider) {
        this.externalIntegrationProvider = externalIntegrationProvider;
    }

    public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
        this.entityProviderManager = entityProviderManager;
    }

    public void setEntityPropertiesService(EntityPropertiesService entityPropertiesService) {
        this.entityPropertiesService = entityPropertiesService;
    }

    public void setEntityViewAccessProviderManager(
            EntityViewAccessProviderManager entityViewAccessProviderManager) {
        this.entityViewAccessProviderManager = entityViewAccessProviderManager;
    }

}
