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

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.adl.validator.contentpackage.LaunchData;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.api.HardDeleteAware;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
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
import org.sakaiproject.tool.api.SessionManager;

/**
 * Entity producer enabling Import from Site for SCORM Player.
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

    private EntityManager entityManager;
    private ScormContentService scormContentService;
    private ContentPackageDao contentPackageDao;
    private ContentPackageManifestDao contentPackageManifestDao;
    private ScormResourceService scormResourceService;
    private ContentHostingService contentHostingService;
    private SecurityService securityService;
    private SessionManager sessionManager;

    protected GradingService gradingService() {
        return null; // Overridden by Spring lookup-method
    }

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
                    .collect(Collectors.toList());
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
            ContentPackageManifest clone = SerializationUtils.clone(manifest);
            clone.setId(null);

            List<LaunchData> launchDataList = clone.getLaunchData();
            if (launchDataList != null) {
                launchDataList.forEach(ld -> ld.setId(null));
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
        GradingService gs = gradingService();
        if (gs == null) {
            return;
        }

        ContentPackageManifest manifest = contentPackageManifestDao.load(copy.getManifestId());
        if (manifest == null || manifest.getLaunchData() == null) {
            return;
        }

        for (LaunchData launchData : manifest.getLaunchData()) {
            if (!"sco".equalsIgnoreCase(launchData.getSCORMType())) {
                continue;
            }

            String itemIdentifier = launchData.getItemIdentifier();
            String sourceExternalId = source.getContentPackageId() + ":" + itemIdentifier;
            String destExternalId = copy.getContentPackageId() + ":" + itemIdentifier;

            // Only copy if source had gradebook sync enabled
            if (!gs.isExternalAssignmentDefined(fromContext, sourceExternalId)) {
                continue;
            }

            try {
                String title = StringUtils.defaultIfBlank(copy.getTitle(), launchData.getItemTitle());
                gs.addExternalAssessment(toContext, toContext, destExternalId, null, title,
                        100.0, copy.getDueOn(), ScormConstants.SCORM_DFLT_TOOL_NAME, null, false, null, null);
            } catch (Exception e) {
                log.debug("Could not copy gradebook item for SCO {}: {}", itemIdentifier, e.getMessage());
            }
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
        if (displayName == null || displayName.trim().isEmpty()) {
            return getDisplayName(collectionId);
        }
        return displayName;
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

    private String extractResourceId(String collectionId) {
        if (collectionId == null) {
            return null;
        }
        String trimmed = collectionId.endsWith(Entity.SEPARATOR) ? collectionId.substring(0, collectionId.length() - 1) : collectionId;
        if (trimmed.startsWith(ScormConstants.ROOT_DIRECTORY)) {
            return trimmed.substring(ScormConstants.ROOT_DIRECTORY.length());
        }
        return trimmed;
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
