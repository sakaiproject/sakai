/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.assignment.impl;

import java.util.Collection;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.util.BaseResourceProperties;

import lombok.ToString;

/**
 * Created by enietzel on 4/28/17.
 */
@ToString
public class FakeReference implements Reference {

    /** The reference string. */
    private String reference = null;

    /** The reference type (a service name string). */
    private String type = "";

    /** The reference sub-type. */
    private String subType = "";

    /** The reference primary id. */
    private String id = null;

    /** The reference containment ids. */
    private String container = null;

    /** Another container, the context id. */
    private String context = null;

    /** Set to true once the values are set. */
    private boolean setAlready = false;

    /** The service owning the entity. */
    private EntityProducer service = null;

    public FakeReference(EntityProducer producer, String reference) {
        this.service = producer;
        this.reference = reference;
    }

    @Override
    public void addSiteContextAuthzGroup(Collection<String> rv) {

    }

    @Override
    public void addUserAuthzGroup(Collection<String> rv, String id) {

    }

    @Override
    public void addUserTemplateAuthzGroup(Collection<String> rv, String id) {

    }

    @Override
    public String getContainer() {
        return container;
    }

    @Override
    public String getContext() {
        return context;
    }

    @Override
    public String getDescription() {
        String description = "unknown";

        if (service != null) {
            service.getEntityDescription(this);
        }

        return description;
    }

    @Override
    public Entity getEntity() {
        if (service != null)
        {
            return service.getEntity(this);
        }
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ResourceProperties getProperties() {
        if (service != null) {
            return service.getEntityResourceProperties(this);
        }
        return new BaseResourceProperties();
    }

    @Override
    public Collection<String> getAuthzGroups() {
        return getAuthzGroups(null);
    }

    @Override
    public Collection<String> getAuthzGroups(String userId) {
        if (service != null) {
            return service.getEntityAuthzGroups(this, userId);
        }
        return new Vector<>();
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public String getSubType() {
        return subType;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getUrl() {
        if (service != null) {
            return service.getEntityUrl(this);
        }
        return null;
    }

    @Override
    public boolean isKnownType() {
        return StringUtils.isNotBlank(type);
    }

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
        if (StringUtils.isBlank(this.id)) this.id = null;
        if (StringUtils.isBlank(this.container)) this.container = null;
        if (StringUtils.isBlank(this.context)) this.context = null;

        setAlready = true;

        return true;
    }

    @Override
    public void updateReference(String ref) {
        reference = ref;
    }

    @Override
    public EntityProducer getEntityProducer() {
        return service;
    }
}
