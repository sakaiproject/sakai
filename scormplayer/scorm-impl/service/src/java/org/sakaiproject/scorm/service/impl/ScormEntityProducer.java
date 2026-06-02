/**
 * Copyright (c) 2025 The Apereo Foundation
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
package org.sakaiproject.scorm.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.adl.validator.contentpackage.LaunchData;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.api.HardDeleteAware;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.dao.api.ContentPackageDao;
import org.sakaiproject.scorm.dao.api.ContentPackageManifestDao;
import org.sakaiproject.scorm.exceptions.ResourceNotDeletedException;
import org.sakaiproject.scorm.exceptions.ResourceStorageException;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.util.MergeConfig;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Entity producer for the SCORM Player, supporting Import from Site (transfer/copy),
 * CC+ archive/merge (export and re-import), and hard delete of a site's SCORM content.
 */
@Setter
@Slf4j
public class ScormEntityProducer implements EntityProducer, EntityTransferrer, HardDeleteAware {

    private static final String REFERENCE_ROOT = Entity.SEPARATOR + "scorm";
    private static final String DEFAULT_USER = "admin";
    private static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = 100;
    private static final Set<String> SCORM_CONTENT_FUNCTIONS = Set.of(
            ContentHostingService.AUTH_RESOURCE_ADD,
            ContentHostingService.AUTH_RESOURCE_READ,
            ContentHostingService.AUTH_RESOURCE_WRITE_ANY,
            ContentHostingService.AUTH_RESOURCE_WRITE_OWN,
            ContentHostingService.AUTH_RESOURCE_REMOVE_ANY,
            ContentHostingService.AUTH_RESOURCE_REMOVE_OWN
    );
    private static final String SCORM_REFERENCE_PREFIX = ContentHostingService.REFERENCE_ROOT + ScormConstants.ROOT_DIRECTORY;

    // Archive/merge (CC+ archive) constants
    private static final String ARCHIVE_ELEMENT = "contentpackage";
    private static final String ATTR_TITLE = "title";
    private static final String ATTR_ARCHIVE = "archive";
    private static final String ATTR_RESOURCE_ID = "resourceId";
    private static final String ATTR_NUMBER_OF_TRIES = "numberOfTries";
    private static final String ATTR_SHOW_TOC = "showToc";
    private static final String ATTR_SHOW_NAV_BAR = "showNavBar";
    private static final String ATTR_RELEASE_ON = "releaseOn";
    private static final String ATTR_DUE_ON = "dueOn";
    private static final String ATTR_ACCEPT_UNTIL = "acceptUntil";
    private static final String SCO_TYPE = "sco";
    // Per-SCO Gradebook binding persisted within each <contentpackage>
    private static final String GRADEBOOK_ELEMENT = "gradebookitem";
    private static final String ATTR_ITEM_IDENTIFIER = "itemIdentifier";
    private static final String ATTR_GB_NAME = "name";
    private static final String ATTR_GB_POINTS = "points";
    private static final String ATTR_GB_CATEGORY_ID = "categoryId";

    private EntityManager entityManager;
    private ScormContentService scormContentService;
    private ContentPackageDao contentPackageDao;
    private ContentPackageManifestDao contentPackageManifestDao;
    private ScormResourceService scormResourceService;
    private ContentHostingService contentHostingService;
    private SecurityService securityService;
    private GradingService gradingService;
    private ServerConfigurationService serverConfigurationService;

    public void init() {
        try {
            entityManager.registerEntityProducer(this, REFERENCE_ROOT);
            log.info("Registered SCORM entity producer");
        } catch (Exception e) {
            log.warn("Unable to register SCORM entity producer", e);
        }
    }

    @Override
    public String getLabel() {
        return "scorm";
    }

    @Override
    public String[] myToolIds() {
        return new String[] { ScormConstants.SCORM_TOOL_ID };
    }

    @Override
    public Optional<List<String>> getTransferOptions() {
        return Optional.of(List.of(EntityTransferrer.COPY_PERMISSIONS_OPTION));
    }

    @Override
    public String getToolPermissionsPrefix() {
        return "scorm.";
    }

    @Override
    public List<Map<String, String>> getEntityMap(String fromContext) {
        try {
            return scormContentService.getContentPackages(fromContext).stream()
                    .map(cp -> Map.of("id", String.valueOf(cp.getContentPackageId()), "title", cp.getTitle()))
                    .toList();
        } catch (ResourceStorageException e) {
            log.warn("Unable to build SCORM entity map for {}: {}", fromContext, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> transferOptions) {

        if (transferOptions != null && transferOptions.contains(EntityTransferrer.COPY_PERMISSIONS_OPTION)) {
            log.debug("Skipping SCORM content copy because only permissions were requested");
            return new HashMap<>();
        }

        Map<String, String> transversalMap = new HashMap<>();
        Set<String> idFilter = (ids == null || ids.isEmpty()) ? Collections.emptySet() : new HashSet<>(ids);
        Set<String> reservedTitles = new HashSet<>();

        try {
            scormContentService.getContentPackages(toContext).forEach(cp -> reservedTitles.add(cp.getTitle()));
        } catch (ResourceStorageException e) {
            log.debug("Unable to preload destination SCORM titles for {}: {}", toContext, e.getMessage());
        }

        List<ContentPackage> sourcePackages;
        try {
            sourcePackages = scormContentService.getContentPackages(fromContext);
        } catch (ResourceStorageException e) {
            log.warn("Unable to fetch SCORM packages from {} for site copy: {}", fromContext, e.getMessage());
            return transversalMap;
        }

        for (ContentPackage source : sourcePackages) {
            if (shouldSkip(idFilter, source)) {
                continue;
            }

            try {
                String newResourceId = copyResourceTree(source.getResourceId());
                Serializable newManifestId = copyManifest(source.getManifestId());

                ContentPackage copy = buildCopy(source, toContext, newResourceId, newManifestId, reservedTitles);
                contentPackageDao.save(copy);
                reservedTitles.add(copy.getTitle());

                recordReferenceMapping(transversalMap, fromContext, toContext, source, copy);
                updateCollectionDisplayName(newResourceId, copy.getTitle());

                // Copy gradebook settings for SCOs
                copyGradebookSettings(fromContext, toContext, source, copy);
            } catch (Exception e) {
                log.error("Failed to copy SCORM package {} from {} to {}", source.getContentPackageId(), fromContext, toContext, e);
            }
        }

        return transversalMap;
    }

    @Override
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> transferOptions, boolean cleanup) {

        if (transferOptions != null && transferOptions.contains(EntityTransferrer.COPY_PERMISSIONS_OPTION)) {
            log.debug("Skipping SCORM cleanup/copy because only permissions were requested");
            return new HashMap<>();
        }

        if (cleanup) {
            try {
                List<ContentPackage> existing = scormContentService.getContentPackages(toContext);
                for (ContentPackage pkg : existing) {
                    purgeContentPackage(pkg);
                }
            } catch (ResourceStorageException e) {
                log.warn("Unable to cleanup SCORM content in {} prior to import: {}", toContext, e.getMessage());
            }
        }

        return transferCopyEntities(fromContext, toContext, ids, transferOptions);
    }

    private boolean shouldSkip(Set<String> idFilter, ContentPackage source) {
        if (idFilter.isEmpty()) {
            return false;
        }

        String pkgId = source.getContentPackageId() != null ? String.valueOf(source.getContentPackageId()) : null;
        return (pkgId == null || !idFilter.contains(pkgId)) && (source.getResourceId() == null || !idFilter.contains(source.getResourceId()));
    }

    private ContentPackage buildCopy(ContentPackage source, String toContext, String newResourceId, Serializable manifestId, Set<String> reservedTitles) {
        ContentPackage copy = new ContentPackage();
        copy.setContext(toContext);
        copy.setTitle(ensureUniqueTitle(source.getTitle(), reservedTitles));
        copy.setResourceId(newResourceId);
        copy.setManifestId(manifestId != null ? manifestId : source.getManifestId());
        copy.setManifestResourceId(source.getManifestResourceId());
        copy.setUrl(source.getUrl());
        copy.setReleaseOn(source.getReleaseOn());
        copy.setDueOn(source.getDueOn());
        copy.setAcceptUntil(source.getAcceptUntil());
        copy.setCreatedOn(source.getCreatedOn() != null ? source.getCreatedOn() : new Date());
        copy.setCreatedBy(StringUtils.defaultIfBlank(source.getCreatedBy(), DEFAULT_USER));
        copy.setModifiedOn(source.getModifiedOn() != null ? source.getModifiedOn() : new Date());
        copy.setModifiedBy(StringUtils.defaultIfBlank(source.getModifiedBy(), DEFAULT_USER));
        copy.setNumberOfTries(source.getNumberOfTries());
        copy.setShowTOC(source.isShowTOC());
        copy.setShowNavBar(source.isShowNavBar());
        copy.setDeleted(false);
        return copy;
    }

    private String ensureUniqueTitle(String originalTitle, Set<String> reservedTitles) {
        String baseTitle = (originalTitle == null || originalTitle.isBlank()) ? "Imported Package" : originalTitle;
        if (!reservedTitles.contains(baseTitle)) {
            return baseTitle;
        }

        int suffix = 2;
        String candidate = baseTitle + " (" + suffix + ")";
        while (reservedTitles.contains(candidate) && suffix < MAXIMUM_ATTEMPTS_FOR_UNIQUENESS) {
            suffix++;
            candidate = baseTitle + " (" + suffix + ")";
        }
        if (reservedTitles.contains(candidate)) {
            log.warn("Unable to generate unique title for '{}' after {} attempts", baseTitle, MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
            return candidate;
        }
        return candidate;
    }

    private String copyResourceTree(String sourceResourceId) throws Exception {
        final String sourceCollectionId = buildCollectionPath(sourceResourceId);

        Callable<String> copyTask = () -> {
            for (int attempt = 0; attempt < MAXIMUM_ATTEMPTS_FOR_UNIQUENESS; attempt++) {
                String targetUuid = UUID.randomUUID().toString();
                String targetCollectionId = buildCollectionPath(targetUuid);
                try {
                    cloneCollection(sourceCollectionId, targetCollectionId);
                    return targetUuid;
                } catch (IdUsedException e) {
                    removeCollectionQuietly(targetCollectionId);
                    log.debug("Collision copying SCORM content package collection {}, retrying (attempt {})", sourceCollectionId, attempt + 1, e);
                }
            }
            throw new ResourceStorageException("Unable to allocate unique destination for SCORM content package copy from " + sourceCollectionId);
        };

        return runWithAdvisor(copyTask);
    }

    private Serializable copyManifest(Serializable manifestId) {
        if (manifestId == null) {
            return null;
        }

        try {
            ContentPackageManifest manifest = contentPackageManifestDao.load(manifestId);
            if (manifest == null) {
                return null;
            }

            // Deep copy fully detached from the Hibernate session, including each LaunchData
            ContentPackageManifest clone = SerializationUtils.clone(manifest);
            clone.setId(null);

            if (clone.getLaunchData() != null) {
                clone.setLaunchData(clone.getLaunchData().stream()
                        .map(ld -> {
                            LaunchData ldClone = SerializationUtils.clone(ld);
                            ldClone.setId(null);
                            return ldClone;
                        })
                        .collect(Collectors.toList()));
            }

            return contentPackageManifestDao.save(clone);
        } catch (Exception e) {
            log.warn("Unable to clone manifest {}: {}", manifestId, e.getMessage());
            return null;
        }
    }

    private void recordReferenceMapping(Map<String, String> transversalMap, String fromContext, String toContext, ContentPackage source, ContentPackage copy) {
        if (source.getContentPackageId() != null && copy.getContentPackageId() != null) {
            transversalMap.put(buildPackageReference(fromContext, source.getContentPackageId()),
                    buildPackageReference(toContext, copy.getContentPackageId()));
        }

        if (source.getResourceId() != null && copy.getResourceId() != null) {
            transversalMap.put(buildResourcePath(source.getResourceId()), buildResourcePath(copy.getResourceId()));
            transversalMap.put(buildCollectionPath(source.getResourceId()), buildCollectionPath(copy.getResourceId()));
        }
    }

    private void updateCollectionDisplayName(String resourceId, String title) {
        if (title == null || title.isBlank()) {
            return;
        }

        String path = buildCollectionPath(resourceId);

        try {
            ContentCollection collection = contentHostingService.getCollection(path);
            ResourceProperties props = collection.getProperties();
            String displayName = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
            if (title.equals(displayName)) {
                return;
            }
        } catch (Exception e) {
            log.debug("Unable to read collection properties for {}: {}", resourceId, e.getMessage());
            return;
        }

        ContentCollectionEdit edit = null;
        try {
            edit = contentHostingService.editCollection(path);
            edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, title);
            contentHostingService.commitCollection(edit);
        } catch (Exception e) {
            if (edit != null && edit.isActiveEdit()) {
                contentHostingService.cancelCollection(edit);
            }
            log.debug("Unable to update collection display name for {}: {}", resourceId, e.getMessage());
        }
    }

    private void copyGradebookSettings(String fromContext, String toContext, ContentPackage source, ContentPackage copy) {

        if (copy.getManifestId() == null) {
            return;
        }

        ContentPackageManifest manifest = contentPackageManifestDao.load(copy.getManifestId());
        if (manifest == null || manifest.getLaunchData() == null) {
            return;
        }

        for (LaunchData launchData : manifest.getLaunchData()) {
            if (!SCO_TYPE.equalsIgnoreCase(launchData.getSCORMType())) {
                continue;
            }

            String itemIdentifier = launchData.getItemIdentifier();
            String sourceExternalId = scoExternalId(source.getContentPackageId(), itemIdentifier);
            String destExternalId = scoExternalId(copy.getContentPackageId(), itemIdentifier);

            if (!gradingService.isExternalAssignmentDefined(fromContext, sourceExternalId)) {
                continue;
            }

            Assignment sourceAssignment = gradingService.getExternalAssignment(fromContext, sourceExternalId);
            if (sourceAssignment == null) {
                log.warn("Skipping gradebook copy for SCO {} - source assignment not found", itemIdentifier);
                continue;
            }

            recreateScoAssessment(toContext, destExternalId, sourceAssignment.getName(),
                    sourceAssignment.getPoints(), copy.getDueOn(), sourceAssignment.getCategoryId());
        }
    }

    /** External assessment id for a SCO, as used by the SCORM Gradebook integration: "{contentPackageId}:{itemIdentifier}". */
    private String scoExternalId(Long contentPackageId, String itemIdentifier) {
        return contentPackageId + ":" + itemIdentifier;
    }

    /**
     * Create (remap) a SCO's Gradebook external assessment in the given site. Shared by site-copy and archive merge.
     * Failures (e.g. duplicate or invalid category in the destination) are logged and skipped so one SCO can't abort the rest.
     */
    private void recreateScoAssessment(String context, String externalId, String name, Double points, Date dueOn, Long categoryId) {
        try {
            gradingService.addExternalAssessment(context, context, externalId, null, name, points, dueOn,
                    ScormConstants.SCORM_DFLT_TOOL_NAME, null, false, categoryId, null);
        } catch (Exception e) {
            log.warn("Could not create gradebook item {} in {}: {}", externalId, context, e.toString());
        }
    }

    /**
     * Persist each SCO's Gradebook binding (external assessment name/points/category) for the package into the archive XML,
     * so {@link #mergeGradebookBindings} can re-create them against the new contentPackageId on import.
     */
    private void archiveGradebookBindings(Document doc, Element element, String siteId, ContentPackage pkg) {
        if (pkg.getManifestId() == null) {
            return;
        }
        ContentPackageManifest manifest = contentPackageManifestDao.load(pkg.getManifestId());
        if (manifest == null || manifest.getLaunchData() == null) {
            return;
        }

        for (LaunchData launchData : manifest.getLaunchData()) {
            if (!SCO_TYPE.equalsIgnoreCase(launchData.getSCORMType())) {
                continue;
            }
            String itemIdentifier = launchData.getItemIdentifier();
            String externalId = scoExternalId(pkg.getContentPackageId(), itemIdentifier);
            if (!gradingService.isExternalAssignmentDefined(siteId, externalId)) {
                continue;
            }
            try {
                Assignment assignment = gradingService.getExternalAssignment(siteId, externalId);
                if (assignment == null) {
                    continue;
                }
                Element gb = doc.createElement(GRADEBOOK_ELEMENT);
                gb.setAttribute(ATTR_ITEM_IDENTIFIER, StringUtils.trimToEmpty(itemIdentifier));
                gb.setAttribute(ATTR_GB_NAME, StringUtils.trimToEmpty(assignment.getName()));
                if (assignment.getPoints() != null) {
                    gb.setAttribute(ATTR_GB_POINTS, String.valueOf(assignment.getPoints()));
                }
                if (assignment.getCategoryId() != null) {
                    gb.setAttribute(ATTR_GB_CATEGORY_ID, String.valueOf(assignment.getCategoryId()));
                }
                element.appendChild(gb);
            } catch (Exception e) {
                log.warn("Unable to archive gradebook binding for SCO {} in {}: {}", itemIdentifier, siteId, e.getMessage());
            }
        }
    }

    /**
     * Re-create the SCO Gradebook bindings captured by {@link #archiveGradebookBindings} against the merged package's
     * new contentPackageId.
     */
    private void mergeGradebookBindings(String siteId, ContentPackage pkg, Element element) {
        NodeList items = element.getElementsByTagName(GRADEBOOK_ELEMENT);
        for (int i = 0; i < items.getLength(); i++) {
            Node node = items.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element gb = (Element) node;
            String itemIdentifier = gb.getAttribute(ATTR_ITEM_IDENTIFIER);
            if (StringUtils.isBlank(itemIdentifier)) {
                continue;
            }
            String externalId = scoExternalId(pkg.getContentPackageId(), itemIdentifier);
            if (gradingService.isExternalAssignmentDefined(siteId, externalId)) {
                continue;
            }
            Double points = gb.hasAttribute(ATTR_GB_POINTS) ? NumberUtils.toDouble(gb.getAttribute(ATTR_GB_POINTS)) : null;
            Long categoryId = gb.hasAttribute(ATTR_GB_CATEGORY_ID) ? NumberUtils.toLong(gb.getAttribute(ATTR_GB_CATEGORY_ID)) : null;
            recreateScoAssessment(siteId, externalId, gb.getAttribute(ATTR_GB_NAME), points, pkg.getDueOn(), categoryId);
        }
    }

    private <T> T runWithAdvisor(Callable<T> task) throws Exception {
        SecurityAdvisor advisor = createScormContentAdvisor();
        securityService.pushAdvisor(advisor);
        try {
            return task.call();
        } finally {
            securityService.popAdvisor(advisor);
        }
    }

    private SecurityAdvisor createScormContentAdvisor() {
        return (userId, function, reference) -> {
            if (function == null || reference == null) {
                return SecurityAdvice.PASS;
            }
            if (!SCORM_CONTENT_FUNCTIONS.contains(function)) {
                return SecurityAdvice.PASS;
            }
            if (reference.startsWith(SCORM_REFERENCE_PREFIX) || reference.startsWith(ScormConstants.ROOT_DIRECTORY)) {
                return SecurityAdvice.ALLOWED;
            }
            return SecurityAdvice.PASS;
        };
    }

    private void cloneCollection(String sourceCollectionId, String targetCollectionId) throws Exception {
        ContentCollection sourceCollection = contentHostingService.getCollection(sourceCollectionId);
        ContentCollectionEdit targetEdit = null;
        try {
            targetEdit = contentHostingService.addCollection(targetCollectionId);
            ResourceProperties sourceProps = sourceCollection.getProperties();
            ResourcePropertiesEdit targetProps = targetEdit.getPropertiesEdit();
            targetProps.addProperty(ResourceProperties.PROP_DISPLAY_NAME,
                    fallbackDisplayName(sourceProps != null ? sourceProps.getProperty(ResourceProperties.PROP_DISPLAY_NAME) : null, targetCollectionId));
            targetEdit.setAvailability(sourceCollection.isHidden(), sourceCollection.getReleaseDate(), sourceCollection.getRetractDate());
            contentHostingService.commitCollection(targetEdit);
        } catch (Exception e) {
            if (targetEdit != null && targetEdit.isActiveEdit()) {
                contentHostingService.cancelCollection(targetEdit);
            }
            throw e;
        }

        try {
            List<ContentEntity> members = sourceCollection.getMemberResources();
            for (ContentEntity member : members) {
                contentHostingService.copyIntoFolder(member.getId(), targetCollectionId);
            }
        } catch (Exception e) {
            removeCollectionQuietly(targetCollectionId);
            throw e;
        }
    }

    private String getDisplayName(String collectionId) {
        String trimmed = collectionId.endsWith(Entity.SEPARATOR) ? collectionId.substring(0, collectionId.length() - 1) : collectionId;
        int lastSeparator = trimmed.lastIndexOf(Entity.SEPARATOR);
        return lastSeparator == -1 ? trimmed : trimmed.substring(lastSeparator + 1);
    }

    private String fallbackDisplayName(String displayName, String collectionId) {
        return StringUtils.isBlank(displayName) ? getDisplayName(collectionId) : displayName;
    }

    private void removeCollectionQuietly(String collectionId) {
        try {
            contentHostingService.removeCollection(collectionId);
        } catch (Exception e) {
            log.debug("Unable to remove temporary SCORM collection {} during copy cleanup", collectionId, e);
        }
    }

    private void purgeContentPackage(ContentPackage pkg) {
        if (pkg == null) {
            return;
        }

        SecurityAdvisor advisor = createScormContentAdvisor();
        securityService.pushAdvisor(advisor);
        try {
            try {
                contentPackageDao.remove(pkg);
            } catch (Exception e) {
                log.warn("Unable to mark SCORM package {} as deleted: {}", pkg.getContentPackageId(), e.getMessage());
            }

            try {
                if (pkg.getResourceId() != null) {
                    scormResourceService.removeResources(pkg.getResourceId());
                }
            } catch (ResourceNotDeletedException e) {
                log.warn("Unable to remove SCORM resources for {}: {}", pkg.getContentPackageId(), e.getMessage());
            }
        } finally {
            securityService.popAdvisor(advisor);
        }
    }

    @Override
    public void hardDelete(String siteId) {
        if (siteId == null || siteId.isBlank()) {
            return;
        }

        try {
            List<ContentPackage> packages = scormContentService.getContentPackages(siteId);
            packages.forEach(this::purgeContentPackage);
        } catch (ResourceStorageException e) {
            log.warn("Unable to hard delete SCORM content for {}: {}", siteId, e.getMessage());
        }
    }

    @Override
    public boolean willArchiveMerge() {
        return true;
    }

    /**
     * Archive the SCORM content packages for a site as part of a CC+ (Common Cartridge plus archive) export.
     * Each package is written as a self-contained zip alongside the archive XML so it can be re-imported via {@link #merge}.
     */
    @Override
    public String archive(String siteId, Document doc, Stack<Element> stack, String archivePath, List<Reference> attachments) {
        StringBuilder results = new StringBuilder();

        // The element tag must be the producer label ("scorm"): SiteArchiver writes this producer's
        // output to "{label}.xml" and SiteMerger dispatches/filters merge by the element's tag name
        // (matched against the archive.merge.filtered.services list, see getLabel()).
        Element root = doc.createElement(getLabel());
        ((Element) stack.peek()).appendChild(root);
        stack.push(root);

        int archived = 0;
        try {
            List<ContentPackage> packages = scormContentService.getContentPackages(siteId);
            for (ContentPackage pkg : packages) {
                try {
                    String zipName = "scorm_" + pkg.getResourceId() + ".zip";
                    if (writePackageArchive(pkg.getResourceId(), archivePath + zipName)) {
                        Element element = doc.createElement(ARCHIVE_ELEMENT);
                        element.setAttribute(ATTR_TITLE, StringUtils.trimToEmpty(pkg.getTitle()));
                        element.setAttribute(ATTR_RESOURCE_ID, StringUtils.trimToEmpty(pkg.getResourceId()));
                        element.setAttribute(ATTR_ARCHIVE, zipName);
                        element.setAttribute(ATTR_NUMBER_OF_TRIES, String.valueOf(pkg.getNumberOfTries()));
                        element.setAttribute(ATTR_SHOW_TOC, String.valueOf(pkg.isShowTOC()));
                        element.setAttribute(ATTR_SHOW_NAV_BAR, String.valueOf(pkg.isShowNavBar()));
                        setDateAttribute(element, ATTR_RELEASE_ON, pkg.getReleaseOn());
                        setDateAttribute(element, ATTR_DUE_ON, pkg.getDueOn());
                        setDateAttribute(element, ATTR_ACCEPT_UNTIL, pkg.getAcceptUntil());
                        archiveGradebookBindings(doc, element, siteId, pkg);
                        root.appendChild(element);
                        archived++;
                    }
                } catch (Exception e) {
                    log.warn("Unable to archive SCORM package {} for site {}: {}", pkg.getContentPackageId(), siteId, e.getMessage());
                }
            }
        } catch (ResourceStorageException e) {
            log.warn("Unable to list SCORM packages while archiving site {}: {}", siteId, e.getMessage());
        }

        stack.pop();
        results.append("archiving ").append(archived).append(" SCORM content package(s).\n");
        return results.toString();
    }

    /**
     * Re-import the SCORM content packages produced by {@link #archive} into the destination site.
     */
    @Override
    public String merge(String siteId, Element root, String archivePath, String fromSiteId, MergeConfig mcx) {
        StringBuilder results = new StringBuilder();

        if (StringUtils.isBlank(siteId)) {
            return "SCORM merge stopped, no siteId provided\n";
        }

        Set<String> reservedTitles = new HashSet<>();
        try {
            scormContentService.getContentPackages(siteId).forEach(cp -> reservedTitles.add(cp.getTitle()));
        } catch (ResourceStorageException e) {
            log.debug("Unable to preload SCORM titles for {}: {}", siteId, e.getMessage());
        }

        // On merge, archivePath is the path to this producer's archive file (e.g. ".../sakai_archive/scorm.xml"),
        // not a directory (unlike archive()). The package zips live alongside it, so resolve against its parent.
        File archiveFile = new File(archivePath);
        File archiveDir = archiveFile.isDirectory() ? archiveFile : archiveFile.getParentFile();

        NodeList packages = root.getElementsByTagName(ARCHIVE_ELEMENT);
        int merged = 0;
        for (int i = 0; i < packages.getLength(); i++) {
            Node node = packages.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) node;
            String title = element.getAttribute(ATTR_TITLE);

            if (StringUtils.isNotBlank(title) && reservedTitles.contains(title)) {
                log.debug("Skipping SCORM package '{}', a package with that title already exists in {}", title, siteId);
                continue;
            }

            File zip = new File(archiveDir, element.getAttribute(ATTR_ARCHIVE));
            if (!zip.isFile()) {
                log.warn("SCORM archive zip {} not found while merging into {}", zip.getPath(), siteId);
                continue;
            }

            try {
                ContentPackage created = importPackageArchive(siteId, zip, title);
                if (created != null) {
                    restoreSettings(created, element);
                    mergeGradebookBindings(siteId, created, element);
                    reservedTitles.add(created.getTitle());
                    merged++;
                }
            } catch (Exception e) {
                log.warn("Unable to merge SCORM package '{}' into {}: {}", title, siteId, e.getMessage(), e);
            }
        }

        results.append("merging ").append(merged).append(" SCORM content package(s).\n");
        return results.toString();
    }

    /**
     * Stream the unpacked SCORM content collection at {@code /private/scorm/{uuid}/} into a zip archive,
     * preserving paths relative to the package root (so {@code imsmanifest.xml} lands at the zip root).
     */
    private boolean writePackageArchive(String resourceId, String zipPath) throws Exception {
        if (StringUtils.isBlank(resourceId)) {
            return false;
        }

        final String collectionId = buildCollectionPath(resourceId);

        Callable<Boolean> task = () -> {
            ContentCollection collection;
            try {
                collection = contentHostingService.getCollection(collectionId);
            } catch (IdUnusedException e) {
                log.warn("SCORM content collection {} not found, skipping archive", collectionId);
                return false;
            }

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
                zipCollection(collection, collectionId, zos);
            }
            return true;
        };

        return runWithAdvisor(task);
    }

    private void zipCollection(ContentCollection collection, String rootCollectionId, ZipOutputStream zos) throws Exception {
        for (ContentEntity member : collection.getMemberResources()) {
            if (member.isCollection()) {
                zipCollection((ContentCollection) member, rootCollectionId, zos);
            } else {
                String entryName = StringUtils.removeStart(member.getId(), rootCollectionId);
                zos.putNextEntry(new ZipEntry(entryName));
                try (InputStream in = ((ContentResource) member).streamContent()) {
                    in.transferTo(zos);
                } finally {
                    zos.closeEntry();
                }
            }
        }
    }

    /**
     * Recreate a SCORM content package from an archived zip, mirroring the interactive upload flow
     * ({@code putArchive} followed by {@code storeAndValidate}). Returns the newly created package.
     */
    private ContentPackage importPackageArchive(String siteId, File zip, String title) throws Exception {
        Set<Long> existingIds = collectPackageIds(siteId);

        Callable<ContentPackage> task = () -> {
            String name = StringUtils.isNotBlank(title) ? title + ".zip" : zip.getName();
            try (InputStream in = new FileInputStream(zip)) {
                String resourceId = scormResourceService.putArchive(in, name, "application/zip", false, NotificationService.NOTI_NONE);
                String encoding = serverConfigurationService.getString("scorm.zip.encoding", "UTF-8");
                scormContentService.storeAndValidate(resourceId, false, encoding);
            }
            return findNewPackage(siteId, existingIds);
        };

        return runWithAdvisor(task);
    }

    private Set<Long> collectPackageIds(String siteId) {
        return contentPackageDao.find(siteId).stream()
                .map(ContentPackage::getContentPackageId)
                .collect(Collectors.toSet());
    }

    private ContentPackage findNewPackage(String siteId, Set<Long> existingIds) {
        return contentPackageDao.find(siteId).stream()
                .filter(cp -> !existingIds.contains(cp.getContentPackageId()))
                .findFirst()
                .orElse(null);
    }

    private void restoreSettings(ContentPackage pkg, Element element) {
        // storeAndValidate() derives the title from the manifest, which loses any rename made in the UI.
        // Restore the archived title so the package matches what Lessons references when relinking SCORM items.
        String title = element.getAttribute(ATTR_TITLE);
        if (StringUtils.isNotBlank(title)) {
            pkg.setTitle(title);
        }
        pkg.setNumberOfTries(NumberUtils.toInt(element.getAttribute(ATTR_NUMBER_OF_TRIES), pkg.getNumberOfTries()));
        pkg.setShowTOC(parseBoolean(element.getAttribute(ATTR_SHOW_TOC), pkg.isShowTOC()));
        pkg.setShowNavBar(parseBoolean(element.getAttribute(ATTR_SHOW_NAV_BAR), pkg.isShowNavBar()));

        Date releaseOn = parseDate(element.getAttribute(ATTR_RELEASE_ON));
        if (releaseOn != null) {
            pkg.setReleaseOn(releaseOn);
        }
        pkg.setDueOn(parseDate(element.getAttribute(ATTR_DUE_ON)));
        pkg.setAcceptUntil(parseDate(element.getAttribute(ATTR_ACCEPT_UNTIL)));

        contentPackageDao.save(pkg);
    }

    private void setDateAttribute(Element element, String name, Date date) {
        if (date != null) {
            element.setAttribute(name, String.valueOf(date.getTime()));
        }
    }

    private Date parseDate(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return new Date(Long.parseLong(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean parseBoolean(String value, boolean fallback) {
        return StringUtils.isBlank(value) ? fallback : Boolean.parseBoolean(value);
    }

    private String buildPackageReference(String context, Long contentPackageId) {
        if (context == null || contentPackageId == null) {
            return null;
        }
        return REFERENCE_ROOT + Entity.SEPARATOR + context + Entity.SEPARATOR + contentPackageId;
    }

    private String buildResourcePath(String resourceId) {
        return ScormConstants.ROOT_DIRECTORY + resourceId;
    }

    private String buildCollectionPath(String resourceId) {
        return buildResourcePath(resourceId) + Entity.SEPARATOR;
    }

}
