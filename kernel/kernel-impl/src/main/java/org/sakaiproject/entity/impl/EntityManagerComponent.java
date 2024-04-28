/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.Validator;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * EntityManagerComponent is an implementation of the EntityManager.
 * </p>
 */
@Slf4j
public class EntityManagerComponent implements EntityManager {

    private Set<String> unresolvableRoots; // immutable, holds roots that are not claimed by any producers
    private Map<String, EntityProducer> producers; // immutable, map of roots -> producers
    private Map<String, Call> producerStatistics; // immutable, map of roots -> producers with statistics
    private int count = 0;
    private long timeSpent = 0;


    @Getter @Setter private UserDirectoryService userDirectoryService;

    public EntityManagerComponent() {
        unresolvableRoots = Collections.emptySet();
        producers = Collections.emptyMap();
        producerStatistics = Collections.emptyMap();
    }

    public void init() {
        addUnresolvableRoot("library");
    }

    @Override
    public Collection<EntityProducer> getEntityProducers() {
        return Collections.unmodifiableCollection(producers.values());
    }

    @Override
    public void registerEntityProducer(EntityProducer manager, String referenceRoot) {
        // some services don't provide a reference root,
        // in that case they get something that will never match.
        if (StringUtils.isBlank(referenceRoot)) {
            referenceRoot = "EMPTY_ROOT_" + System.currentTimeMillis();
            log.warn("Entity Producer does not provide a root reference : {}", manager);
        }
        if (referenceRoot.startsWith("/")) {
            referenceRoot = referenceRoot.substring(1);
        }

        // this is a thread safe way of updating map
        Map<String, EntityProducer> mutableProducers = new HashMap<>(producers);
        mutableProducers.put(referenceRoot, manager);
        Map<String, Call> mutablePerformance = new HashMap<>(producerStatistics);
        mutablePerformance.put(referenceRoot, new Call(manager));

        producers = Collections.unmodifiableMap(mutableProducers);
        producerStatistics = Collections.unmodifiableMap(mutablePerformance);
    }

    private void addUnresolvableRoot(String referenceRoot) {
        // some services don't provide a reference root,
        // in that case they get something that will never match.
        if (StringUtils.isBlank(referenceRoot)) return;

        log.debug("Adding root [{}] to unresolvable roots", referenceRoot);

        // thread safe way of updating set
        Set<String> mutableSet = new HashSet<>(unresolvableRoots);
        mutableSet.add(referenceRoot);
        unresolvableRoots = Collections.unmodifiableSet(mutableSet);
    }

    @Override
    public Optional<Entity> getEntity(String ref) {

        Reference r = newReference(ref);
        EntityProducer ep = r.getEntityProducer();

        if (ep != null) {
            return Optional.ofNullable(ep.getEntity(r));
        } else {
            log.debug("No entity producer for reference {}", ref);
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getTool(String ref) {

        Reference r = newReference(ref);
        EntityProducer ep = r.getEntityProducer();

        if (ep != null) {
            return ep.getTool();
        } else {
            log.debug("No entity producer for reference {}", ref);
            return Optional.empty();
        }
    }

    @Override
    public Reference newReference(String refString) {
        return new ReferenceComponent(this, refString);
    }

    @Override
    public Reference newReference(Reference copyMe) {
        return new ReferenceComponent(copyMe);
    }

    @Override
    public List<Reference> newReferenceList() {
        return new ReferenceVectorComponent();
    }

    @Override
    public List<Reference> newReferenceList(List<Reference> list) {
        return new ReferenceVectorComponent(list);
    }

    @Override
    public boolean checkReference(String reference) {
        // the rules:
        // Null is rejected
        // all blank is rejected
        // INVALID_CHARS_IN_RESOURCE_ID characters are rejected

        Reference ref = newReference(reference);

        String id = ref.getId();
        if (StringUtils.isBlank(id)) return false;

        // we must reject certain characters that we cannot even escape and get
        // into Tomcat via a URL
        return StringUtils.containsNone(id, Validator.INVALID_CHARS_IN_RESOURCE_ID);
    }

    @Override
    public Optional<String> getUrl(String ref, Entity.UrlType urlType) {

        Reference r = newReference(ref);
        EntityProducer ep = r.getEntityProducer();

        if (ep != null) {
            return ep.getEntityUrl(r, urlType);
        } else {
            log.debug("No entity producer for reference {}", ref);
            return Optional.empty();
        }
    }

    @Override
    public EntityProducer getEntityProducer(String reference, Reference target) {
        if (reference.isEmpty()) return null;

        String root = parseReferenceRoot(reference);

        if (unresolvableRoots.contains(root)) return null;

        EntityProducer producer;

        if (log.isDebugEnabled()) {
            producer = getEntityProducerWithStats(reference, root, target);
        } else {
            producer = getEntityProducerNoStats(reference, root, target);
        }

        return producer;
    }

    private EntityProducer getEntityProducerWithStats(String reference, String referenceRoot, Reference target) {
        count++;

        if (count == 1000) {
            count = 0;
            StringBuilder sb = new StringBuilder();
            sb.append("\n     ")
                    .append("Unresolvable roots [")
                    .append(unresolvableRoots.size())
                    .append("] = ")
                    .append(unresolvableRoots);
            sb.append("\n     ")
                    .append("Total time [")
                    .append(timeSpent)
                    .append("ms] parsing references");
            for (Map.Entry<String, Call> entry : producerStatistics.entrySet()) {
                sb.append("\n     ")
                        .append(entry.getValue())
                        .append("\t\t\troot [")
                        .append(entry.getKey())
                        .append("]");
            }
            log.debug("EntityManager Monitor {}", sb);
        }

        long start = System.currentTimeMillis();
        try {
            // direct lookup
            Call directCall = producerStatistics.get(referenceRoot);
            EntityProducer directCallProducer = null;
            if (directCall != null) {
                directCallProducer = directCall.producer;
                try {
                    directCall.lookupStart();
                    if (directCallProducer.parseEntityReference(reference, target)) {
                        directCall.lookupMatch();
                        return directCallProducer;
                    }
                } finally {
                    directCall.lookupEnd();
                }
            }

            // search producers
            for (Call call : producerStatistics.values()) {
                EntityProducer producer = call.producer;
                if (directCallProducer != producer) {
                    // don't search the same producer as the direct call
                    try {
                        call.searchStart();
                        if (producer.parseEntityReference(reference, target)) {
                            call.searchMatch();
                            return producer;
                        }
                    } finally {
                        call.searchEnd();
                    }
                }
            }
            if (directCallProducer == null) addUnresolvableRoot(referenceRoot);
        } finally {
            timeSpent += (System.currentTimeMillis() - start);
        }

        log.debug("Search yielded no producer for reference {} with root {}", reference, referenceRoot, new Throwable("Stacktrace"));
        return null;
    }

    private String parseReferenceRoot(String reference) {
        int n = reference.indexOf('/', 1);
        if (n > 0) {
            if (reference.charAt(0) == '/') {
                return reference.substring(1, n);
            } else {
                return reference.substring(0, n);
            }
        } else {
            if (reference.charAt(0) == '/') {
                return reference.substring(1);
            }
        }
        return null;
    }

    private EntityProducer getEntityProducerNoStats(String reference, String referenceRoot, Reference target) {

        // direct lookup
        EntityProducer producer = producers.get(referenceRoot);
        if (producer != null) {
            if (producer.parseEntityReference(reference, target)) {
                return producer;
            }
        }

        // search all producers for a match
        for (EntityProducer ep : producers.values()) {
            // don't search the same producer as the direct call
            if (producer != ep && ep.parseEntityReference(reference, target)) {
                return ep;
            }
        }

        // it is possible that a root was found but the entity doesn't exist
        // so only add to unresolvable roots when a producer was not found
        if (producer == null) addUnresolvableRoot(referenceRoot);
        return null;
    }


    public static class Call {
        private final EntityProducer producer;
        private long lastStart = System.currentTimeMillis();
        private long lookups = 0;
        private long lookupMatch = 0;
        private long searches = 0;
        private long searchMatch = 0;
        private long lookupTime;
        private long searchTime;


        public Call(EntityProducer producer) {
            this.producer = producer;
        }

        @Override
        public String toString() {
            double rate;
            if ((lookups + searches) > 999) {
                rate = (1.0 * (lookupTime + searchTime)) / ((1.0 * (lookups + searches)) / 1000);
            } else {
                rate = (1.0 * (lookupTime + searchTime)) / (1.0 * (lookups + searches));
            }
            return MessageFormat.format("lookups [{0} matches out of {1}]" +
                            "\tsearches [{2} matches out of {3}]" +
                            "\t\taverage parse time [{4}ms per 1k]" +
                            "\t\ttotal parse time [{5}ms]" +
                            "\t\tproducer [{6}]",
                    lookupMatch,
                    lookups,
                    searchMatch,
                    searches,
                    rate,
                    (lookupTime + searchTime),
                    producer.getClass().getSimpleName());
        }

        public void lookupStart() {
            lastStart = System.currentTimeMillis();
            lookups++;
        }

        public void lookupMatch() {
            lookupMatch++;
        }

        public void searchStart() {
            lastStart = System.currentTimeMillis();
            searches++;
        }

        public void searchMatch() {
            searchMatch++;
        }

        public void lookupEnd() {
            lookupTime += (System.currentTimeMillis() - lastStart);
        }

        public void searchEnd() {
            searchTime += (System.currentTimeMillis() - lastStart);
        }
    }
}
