/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.entity.impl;

import java.util.Collection;
import java.util.Collections;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * <p>
 * Implementation of the Reference API
 * </p>
 * <p>
 * Note: a Reference is immutable.
 * </p>
 */
@Slf4j
public class ReferenceComponent implements Reference {
    /**
     * The reference string.
     */
    @Getter private String reference;

    /**
     * The reference type (a service name string).
     */
    @Getter private String type = "";

    /**
     * The reference sub type.
     */
    @Getter private String subType = "";

    /**
     * The reference primary id.
     */
    @Getter private String id = null;

    /**
     * The reference containment ids.
     */
    @Getter private String container = null;

    /**
     * Another container, the context id.
     */
    @Getter private String context = null;

    /**
     * Set to true once the values are set.
     */
    @Getter private boolean setAlready = false;

    /**
     * The service owning the entity.
     */
    @Getter private EntityProducer entityProducer = null;


    private final UserDirectoryService userDirectoryService;

    /**
     * Construct with a reference string.
     *
     * @param entityManager     The EntityManager service that will parse this reference with a matching EntityProducer
     * @param reference         The resource reference.
     */
    public ReferenceComponent(EntityManagerComponent entityManager, String reference) {
        this.reference = reference;
        this.userDirectoryService = entityManager.getUserDirectoryService();
        parse(entityManager);
    }

    /**
     * Construct with a Reference.
     *
     * @param reference The resource reference.
     */
    public ReferenceComponent(Reference reference) {
        ReferenceComponent fromRef = (ReferenceComponent) reference;

        this.reference = fromRef.reference;
        this.type = fromRef.type;
        this.subType = fromRef.subType;
        this.id = fromRef.id;
        this.container = fromRef.container;
        this.context = fromRef.context;
        this.entityProducer = fromRef.entityProducer;
        this.userDirectoryService = fromRef.userDirectoryService;
    }

    /**
     * Check if the reference's type is known
     *
     * @return true if known, false if not.
     */
    @Override
    public boolean isKnownType() {
        return !type.isEmpty();
    }

    /**
     * Find the ResourceProperties object for this reference.
     *
     * @return A ResourcesProperties object found (or constructed) for this reference.
     */
    @Override
    public ResourceProperties getProperties() {
        ResourceProperties props = null;

        if (entityProducer != null) {
            props = entityProducer.getEntityResourceProperties(this);
        }

        return props;
    }

    /**
     * Find the Entity that is referenced.
     *
     * @return The Entity object that this references.
     */
    @Override
    public Entity getEntity() {
        Entity e = null;

        if (entityProducer != null) {
            e = entityProducer.getEntity(this);
        }

        return e;
    }

    /**
     * Access the URL which can be used to access the referenced resource.
     *
     * @return The URL which can be used to access the referenced resource.
     */
    @Override
    public String getUrl() {
        String url = null;

        if (entityProducer != null) {
            url = entityProducer.getEntityUrl(this);
        }

        return url;
    }

    /**
     * @return a description of the resource referenced.
     */
    @Override
    public String getDescription() {
        String rv = "unknown";

        if (entityProducer != null) {
            rv = entityProducer.getEntityDescription(this);

            if (rv == null) {
                rv = entityProducer.getLabel() + " " + reference;
            }
        }

        return rv;
    }

    @Override
    public Collection<String> getAuthzGroups() {
        return getAuthzGroups(null);
    }

    @Override
    public Collection<String> getAuthzGroups(String userId) {
        Collection<String> realms = null;

        if (entityProducer != null) {
            realms = entityProducer.getEntityAuthzGroups(this, userId);
        }

        if (realms == null) realms = Collections.emptyList();

        return realms;
    }

    /**
     * Add the AuthzGroup(s) for context as a site.
     *
     * @param authzGroup The list.
     */
    @Override
    public void addSiteContextAuthzGroup(Collection<String> authzGroup) {
        String context = getContext();
        if (context == null) return;

        // site using context as id
        // TODO: taken from site -ggolden was: rv.add(SiteService.siteReference(getContext()));
        authzGroup.add("/site/" + context);

        // site helper
        authzGroup.add("!site.helper");
    }

    /**
     * Add the AuthzGroup for this user id, or for the user's type template, or for the general template.
     *
     * @param authzGroup The list.
     * @param userId The user id.
     */
    @Override
    public void addUserAuthzGroup(Collection<String> authzGroup, String userId) {
        if (userId == null) {
            userId = "";
        }

        // the user's realm (unless it's anon)
        if (!userId.isEmpty()) {
            authzGroup.add(userDirectoryService.userReference(userId));
        }

        addUserTemplateAuthzGroup(authzGroup, userId);
    }

    /**
     * Add the AuthzGroup for this user id, or for the user's type template, or for the general template.
     *
     * @param authzGroup The list.
     * @param userId The user id.
     */
    @Override
    public void addUserTemplateAuthzGroup(Collection<String> authzGroup, String userId) {
        if (userId == null) {
            userId = "";
        }

        // user type template
        String template = "!user.template";
        try {
            User user = userDirectoryService.getUser(userId);
            String type = user.getType();
            if (type != null) {
                authzGroup.add(template + "." + type);
            }
        } catch (Exception ignore) {
        }

        // general user template
        authzGroup.add("!user.template");
    }

    /**
     * Accept the settings for a reference - may be rejected if already set
     *
     * @param type          the reference type
     * @param subType       the reference sub type
     * @param id            the reference id
     * @param container     the reference container
     * @param context       the reference context
     * @return true if settings are accepted, false if not.
     */
    @Override
    public boolean set(String type, String subType, String id, String container, String context) {
        if (setAlready) return false;

        // these must not be null
        this.type = type;
        this.subType = subType;
        if (this.type == null) this.type = "";
        if (this.subType == null) this.subType = "";

        // these should be null if empty
        this.id = id;
        this.container = container;
        this.context = context;
        if ((this.id != null) && (this.id.isEmpty())) this.id = null;
        if ((this.container != null) && (this.container.isEmpty())) this.container = null;
        if ((this.context != null) && (this.context.isEmpty())) this.context = null;

        setAlready = true;

        return true;
    }

    @Override
    public void updateReference(String reference) {
        this.reference = reference;
    }

    private void parse(EntityManagerComponent entityManagerComponent) {
        if (reference == null) return;

        EntityProducer producer = entityManagerComponent.getEntityProducer(reference.trim(), this);
        if (producer != null) this.entityProducer = producer;
        else log.debug("EntityProducer not found for reference: {}", reference);
    }
}
