package org.sakaiproject.sitemanage.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.EntityTransferrerRefMigrator;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.shortenedurl.api.ShortenedUrlService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.sitemanage.api.SiteManageConstants;
import org.sakaiproject.sitemanage.api.SiteManageService;
import org.sakaiproject.sitemanage.api.UserNotificationProvider;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ArrayUtil;
import org.sakaiproject.util.Web;
import org.sakaiproject.util.api.LinkMigrationHelper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
public class SiteManageServiceImpl implements SiteManageService {

    @Setter private ContentHostingService contentHostingService;
    @Setter private EntityManager entityManager;
    @Setter private EventTrackingService eventTrackingService;
    @Setter private LinkMigrationHelper linkMigrationHelper;
    @Setter private PreferencesService preferencesService;
    @Setter private SecurityService securityService;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SessionManager sessionManager;
    @Setter private ShortenedUrlService shortenedUrlService;
    @Setter private SiteService siteService;
    @Setter private ToolManager toolManager;
    @Setter private TransactionTemplate transactionTemplate;
    @Setter private UserDirectoryService userDirectoryService;
    @Setter private UserNotificationProvider userNotificationProvider;

    @Setter private Integer siteImportThreadCount;

    private ExecutorService executorService;
    private Set<String> currentSiteImports;

    public void init() {
        // while this Set isn't cluster wide sessions are node specific
        // so this is only unsafe for more than one session performing an import on the same site
        // which is a really low percentage
        currentSiteImports = new ConcurrentSkipListSet<>();
        executorService = Executors.newFixedThreadPool(siteImportThreadCount);
    }

    public void destroy() {
        try {
            executorService.shutdown();
            executorService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            log.warn("Site Import executor did not shutdown gracefully");
        } finally {
            if (!executorService.isTerminated()) {
                executorService.shutdownNow();
            }
        }
    }

    @Override
    public boolean importToolsIntoSiteThread(final Site site, final List<String> existingTools, final Map<String, List<String>> importTools, final boolean cleanup) {
        final User user = userDirectoryService.getCurrentUser();
        final Locale locale = preferencesService.getLocale(user.getId());
        final Session session = sessionManager.getCurrentSession();
        final ToolSession toolSession = sessionManager.getCurrentToolSession();
        final String id = site.getId();

        Runnable siteImportTask = () -> {
            sessionManager.setCurrentSession(session);
            sessionManager.setCurrentToolSession(toolSession);
            eventTrackingService.post(eventTrackingService.newEvent(SiteService.EVENT_SITE_IMPORT_START, site.getReference(), false));

            try {
                importToolsIntoSite(site, existingTools, importTools, cleanup);
            } catch (Exception e) {
                log.warn("Site Import Task encountered an exception for site {}, {}", id, e.getMessage());
            } finally {
                currentSiteImports.remove(id);
            }

            if (serverConfigurationService.getBoolean(SiteManageConstants.SAK_PROP_IMPORT_NOTIFICATION, true)) {
                userNotificationProvider.notifySiteImportCompleted(user.getEmail(), locale, id, site.getTitle());
            }
            eventTrackingService.post(eventTrackingService.newEvent(SiteService.EVENT_SITE_IMPORT_END, site.getReference(), false));
        };

        // only if the siteId was added to the list do we start the task
        if (currentSiteImports.add(id)) {
            try {
                executorService.execute(siteImportTask);
                return true;
            } catch (RejectedExecutionException ree) {
                log.warn("Site Import Task was rejected by the executor, {}", ree.getMessage());
                currentSiteImports.remove(id);
            }
        }
        return false;
    }

    @Override
    public void importToolContent(String oSiteId, Site site, boolean bypassSecurity) {
        SecurityAdvisor securityAdvisor = null;
        String nSiteId = site.getId();

        // import tool content
        if (bypassSecurity) {
            // importing from template, bypass the permission checking:
            // temporarily allow the user to read and write from assignments (asn.revise permission)
            securityAdvisor = (userId, function, reference) -> SecurityAdvisor.SecurityAdvice.ALLOWED;
            securityService.pushAdvisor(securityAdvisor);
        }

        List<SitePage> pageList = site.getPages();
        Set<String> toolsCopied = new HashSet<>();

        Map<String, String> transversalMap = new HashMap<>();

        if (pageList != null) {
            for (SitePage page : pageList) {
                List<ToolConfiguration> pageToolList = page.getTools();
                if (pageToolList != null) {
                    Tool tool = pageToolList.get(0).getTool();
                    String toolId = tool != null ? tool.getId() : "";
                    if (toolId.equalsIgnoreCase("sakai.resources")) {
                        // special handleling for resources
                        transversalMap.putAll(
                                transferCopyEntities(toolId,
                                        contentHostingService.getSiteCollection(oSiteId),
                                        contentHostingService.getSiteCollection(nSiteId),
                                        false));
                        transversalMap.putAll(getDirectToolUrlEntityReferences(toolId, oSiteId, nSiteId));

                    } else if (toolId.equalsIgnoreCase(SiteManageConstants.SITE_INFO_TOOL_ID)) {
                        // handle Home tool specially, need to update the site infomration display url if needed
                        String newSiteInfoUrl = transferSiteResource(oSiteId, nSiteId, site.getInfoUrl());
                        site.setInfoUrl(newSiteInfoUrl);
                        saveSite(site);
                    } else {
                        // other
                        // tools
                        // SAK-19686 - added if statement and toolsCopied.add
                        if (!toolsCopied.contains(toolId)) {
                            transversalMap.putAll(transferCopyEntities(toolId, oSiteId, nSiteId, false));
                            transversalMap.putAll(getDirectToolUrlEntityReferences(toolId, oSiteId, nSiteId));
                            toolsCopied.add(toolId);
                        }
                    }

                    updateEntityReferences(toolId, nSiteId, transversalMap, site);
                }
            }
        }

        if (bypassSecurity) {
            securityService.popAdvisor(securityAdvisor);
        }
    }

    @Override
    public boolean isAddMissingToolsOnImportEnabled() {
        return serverConfigurationService.getBoolean("site.setup.import.addmissingtools", true);
    }

    @Override
    public String transferSiteResource(String oSiteId, String nSiteId, String siteAttribute) {
        String rv = siteAttribute;

        String access = serverConfigurationService.getAccessUrl();
        if (siteAttribute != null && siteAttribute.contains(oSiteId) && access != null) {
            Reference ref = null;
            try {
                URI accessUrl = new URI(access);
                URI url = new URI(siteAttribute);
                String path = url.getPath();
                String accessPath = accessUrl.getPath();

                // stripe out the access url, get the relative form of "url"
                String contentRef = path.replaceAll(accessPath, "");

                ref = entityManager.newReference(contentRef);

                ContentResource resource = contentHostingService.getResource(ref.getId());
                // the new resource
                ContentResource nResource = null;
                String nResourceId = resource.getId().replaceAll(oSiteId, nSiteId);
                try {
                    nResource = contentHostingService.getResource(nResourceId);
                } catch (Exception e) {
                    log.warn("Cannot find resource with id={} copying it from the original resource, {}", nResourceId, e.getMessage());
                    // copy the resource then
                    try {
                        nResourceId = contentHostingService.copy(resource.getId(), nResourceId);
                        nResource = contentHostingService.getResource(nResourceId);
                    } catch (Exception ee) {
                        log.warn("Something happened copying the resource with id={}, {}", resource.getId(), ee.getMessage());
                    }
                }

                // get the new resource url
                rv = nResource != null ? nResource.getUrl(false) : "";

            } catch (URISyntaxException use) {
                log.warn("Couldn't update site resource: {}, {}", siteAttribute, use.getMessage());
            } catch (Exception e) {
                log.warn("Cannot find resource with ref={}, {}", ref.getReference(), e.getMessage());
            }
        }

        return rv;
    }

    /**
     * Helper to add a tool to a site if the site does not contain an instance of the tool.
     * <p>
     * Note that it does NOT save the site. The caller must handle this.
     *
     * @param site   the site to check
     * @param toolId the tool to add (eg sakai.resources)
     * @return site the updated site object
     */
    private Site addToolToSiteIfMissing(Site site, String toolId) {

        if (site.getToolForCommonId(toolId) != null) {
            return site;
        }

        log.debug("Adding tool to site: {}, tool: {}", site.getId(), toolId);

        SitePage page = site.addPage(); //inherit the tool's title.

        ToolConfiguration tool = page.addTool();
        tool.setTool(toolId, toolManager.getTool(toolId));
        tool.setTitle(toolManager.getTool(toolId).getTitle());

        return site;
    }

    /**
     * Copies the site information from one site ot another.
     * @param fromSiteId    the source site
     * @param toSiteId      the destinatination site
     * @return the site with the updated site information
     */
    private Site copySiteInformation(String fromSiteId, String toSiteId) {
        Site toSite = null;
        try {
            Site fromSite = siteService.getSite(fromSiteId);
            toSite = siteService.getSite(toSiteId);
            toSite.setDescription(fromSite.getDescription());
            toSite.setInfoUrl(fromSite.getInfoUrl());
            saveSite(toSite);
        } catch (IdUnusedException iue) {
            log.warn("Site not found, {}", iue.getMessage());
        }
        return toSite;
    }

    @Override
    public void importToolsIntoSite(Site site, List<String> toolIds, Map<String, List<String>> importTools, boolean cleanup) {
        if (importTools != null && !importTools.isEmpty()) {

            //if add missing tools is enabled, add the tools ito the site before importing content
            if (isAddMissingToolsOnImportEnabled()) {

                //add the toolId lists into a set to remove dupes
                Set<String> toolsToProcess = new HashSet<>(toolIds);
                toolsToProcess.addAll(importTools.keySet());

                //now compare what we have to what we need to add
                final List<String> selectedTools = new ArrayList<String>(toolsToProcess);
                log.debug("selectedTools: " + selectedTools);

                List<String> missingToolIds = new ArrayList<String>(selectedTools);
                missingToolIds.removeAll(toolIds);
                log.debug("missingToolIds: " + missingToolIds);

                //and add
                for (String missingToolId : missingToolIds) {
                    site = addToolToSiteIfMissing(site, missingToolId);
                    saveSite(site);
                }

                //now update toolIds to match importTools so that the content is imported
                toolIds.clear();
                toolIds.addAll(importTools.keySet());
            }

            Map<String, String> transversalMap = new HashMap<>();

            // import resources first
            boolean resourcesImported = false;
            for (int i = 0; i < toolIds.size() && !resourcesImported; i++) {
                String toolId = toolIds.get(i);

                if (toolId.equalsIgnoreCase("sakai.resources") && importTools.containsKey(toolId)) {
                    List<String> importSiteIds = importTools.get(toolId);

                    for (String fromSiteId : importSiteIds) {
                        String toSiteId = site.getId();

                        String fromSiteCollectionId = contentHostingService.getSiteCollection(fromSiteId);
                        String toSiteCollectionId = contentHostingService.getSiteCollection(toSiteId);

                        transversalMap.putAll(transferCopyEntities(toolId, fromSiteCollectionId, toSiteCollectionId, cleanup));
                        transversalMap.putAll(getDirectToolUrlEntityReferences(toolId, fromSiteId, toSiteId));
                        resourcesImported = true;
                    }
                }
            }

            // import other tools then
            for (String toolId : toolIds) {
                if (!toolId.equalsIgnoreCase("sakai.resources") && importTools.containsKey(toolId)) {
                    List<String> importSiteIds = importTools.get(toolId);

                    for (String fromSiteId : importSiteIds) {
                        String toSiteId = site.getId();
                        if (SiteManageConstants.SITE_INFO_TOOL_ID.equals(toolId)) {
                            site = copySiteInformation(fromSiteId, toSiteId);
                        } else {
                            transversalMap.putAll(transferCopyEntities(toolId, fromSiteId, toSiteId, cleanup));
                            transversalMap.putAll(getDirectToolUrlEntityReferences(toolId, fromSiteId, toSiteId));
                        }
                    }
                }
            }

            //update entity references
            for (String toolId : toolIds) {
                if (importTools.containsKey(toolId)) {
                    List<String> importSiteIds = importTools.get(toolId);
                    for (int k = 0; k < importSiteIds.size(); k++) {
                        String toSiteId = site.getId();
                        updateEntityReferences(toolId, toSiteId, transversalMap, site);
                    }
                }
            }
        }
    }

    /**
     * Save a site
     * @param site  the site to save
     */
    private void saveSite(Site site) {
        try {
            siteService.save(site);
        } catch (IdUnusedException iue) {
            log.warn("The site {} must exist in order to update it, {}", site.getId(), iue.getMessage());
        } catch (PermissionException pe) {
            log.warn("The user cannot update the site {}, {}", site.getId(), pe.getMessage());
        }
    }

    /**
     * Transfer a copy of all entites from another context for any entity
     * producer that claims this tool id.
     *
     * @param toolId      The tool id.
     * @param fromContext The context to import from.
     * @param toContext   The context to import into.
     */
    private Map<String, String> transferCopyEntities(String toolId, String fromContext, String toContext, boolean cleanup) {
        Map<String, String> transversalMap = new HashMap<>();

        // offer to all EntityProducers
        for (EntityProducer ep : entityManager.getEntityProducers()) {
            if (ep instanceof EntityTransferrer) {
                try {
                    EntityTransferrer et = (EntityTransferrer) ep;

                    // if this producer claims this tool id
                    if (ArrayUtil.contains(et.myToolIds(), toolId)) {
                        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                            @Override
                            protected void doInTransactionWithoutResult(TransactionStatus status) {
                                if (ep instanceof EntityTransferrerRefMigrator) {
                                    EntityTransferrerRefMigrator etrm = (EntityTransferrerRefMigrator) ep;
                                    Map<String, String> entityMap = etrm.transferCopyEntitiesRefMigrator(fromContext, toContext, new ArrayList<>(), cleanup);
                                    if (entityMap != null) {
                                        transversalMap.putAll(entityMap);
                                    }
                                } else {
                                    et.transferCopyEntities(fromContext, toContext, new ArrayList<>());
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    log.error("Error encountered while asking EntityTransfer to transferCopyEntities from: {} to: {}, {}", fromContext, toContext, e.getMessage());
                }
            }
        }
        return transversalMap;
    }

    private Map<String, String> getDirectToolUrlEntityReferences(String toolId, String fromSiteId, String toSiteId) {

        Map<String, String> transversalMap = new HashMap<>();
        // record direct URL for this tool in old and new sites, so anyone using the URL in HTML text will
        // get a proper update for the HTML in the new site
        // Some tools can have more than one instance. Because getTools should always return tools
        // in order, we can assume that if there's more than one instance of a tool, the instances
        // correspond

        Collection<ToolConfiguration> fromTools = null;
        Collection<ToolConfiguration> toTools = null;
        try {
            Site fromSite = siteService.getSite(fromSiteId);
            Site toSite = siteService.getSite(toSiteId);
            fromTools = fromSite.getTools(toolId);
            toTools = toSite.getTools(toolId);
        } catch (Exception e) {
            log.warn("Can't get site, {}", e.getMessage());
        }

        // getTools appears to return tools in order. So we should be able to match them
        if (fromTools != null && toTools != null) {
            Iterator<ToolConfiguration> toToolIt = toTools.iterator();
            // step through tools in old and new site in parallel
            // I believe the first time this is called for a given
            // tool all instances will be copied, but stop if not
            // all instances have been copied yet
            for (ToolConfiguration fromTool : fromTools) {
                if (toToolIt.hasNext()) {
                    ToolConfiguration toTool = toToolIt.next();
                    String fromUrl = serverConfigurationService.getPortalUrl() + "/directtool/" + Web.escapeUrl(fromTool.getId()) + "/";
                    String toUrl = serverConfigurationService.getPortalUrl() + "/directtool/" + Web.escapeUrl(toTool.getId()) + "/";
                    transversalMap.putIfAbsent(fromUrl, toUrl);
                    if (shortenedUrlService.shouldCopy(fromUrl)) {
                        fromUrl = shortenedUrlService.shorten(fromUrl, false);
                        toUrl = shortenedUrlService.shorten(toUrl, false);
                        if (fromUrl != null && toUrl != null)
                            transversalMap.put(fromUrl, toUrl);
                    }
                } else {
                    break;
                }
            }
        }
        return transversalMap;
    }

    /**
     * Updates the references to entities that have been copied.
     * @param toolId         the tool continaing references to be updated
     * @param toContext      the context (site) related to the references that will be updated
     * @param transversalMap Map containing new references -> old references
     * @param site           the new site
     */
    private void updateEntityReferences(String toolId, String toContext, Map<String, String> transversalMap, Site site) {
        if (toolId.equalsIgnoreCase(SiteManageConstants.SITE_INFO_TOOL_ID)) {
            updateSiteInfoToolEntityReferences(transversalMap, site);
        } else {
            for (EntityProducer ep : entityManager.getEntityProducers()) {
                if (ep instanceof EntityTransferrerRefMigrator && ep instanceof EntityTransferrer) {
                    try {
                        EntityTransferrer et = (EntityTransferrer) ep;
                        EntityTransferrerRefMigrator etrm = (EntityTransferrerRefMigrator) ep;

                        // if this producer claims this tool id
                        if (ArrayUtil.contains(et.myToolIds(), toolId)) {
                            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                                @Override
                                protected void doInTransactionWithoutResult(TransactionStatus status) {
                                    etrm.updateEntityReferences(toContext, transversalMap);
                                }
                            });
                        }
                    } catch (Exception e) {
                        log.error("Error encountered while asking EntityTransfer to updateEntityReferences at site: {}", toContext, e);
                    }
                }
            }
        }
    }

    /**
     * Updates links in the site information to point to the new site.
     * It uses the {@link LinkMigrationHelper} service to migrate the links to the new site.
     * @param transversalMap the map of tools
     * @param site           the new site where links should be updated
     */
    private void updateSiteInfoToolEntityReferences(Map<String, String> transversalMap, Site site) {
        if (transversalMap != null && !transversalMap.isEmpty() && site != null) {
            Set<Map.Entry<String, String>> entrySet = transversalMap.entrySet();

            String msgBody = site.getDescription();
            if (StringUtils.isNotBlank(msgBody)) {
                String msgBodyPreMigrate = msgBody;
                msgBody = linkMigrationHelper.migrateAllLinks(entrySet, msgBody);

                if (!msgBody.equals(msgBodyPreMigrate)) {
                    //update the site b/c some tools (Lessonbuilder) updates the site structure (add/remove pages) and we don't want to
                    //over write this
                    try {
                        site = siteService.getSite(site.getId());
                        site.setDescription(msgBody);
                        saveSite(site);
                    } catch (IdUnusedException iue) {
                        log.warn("A site with id {} doesn't exist, {}", site.getId(), iue.getMessage());
                    }
                }
            }
        }
    }
}
