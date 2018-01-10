package org.sakaiproject.sitemanage.api;

import java.util.List;
import java.util.Map;

import org.sakaiproject.site.api.Site;

public interface SiteManageService {

    /**
     * Import tools and content into the site.
     * <br/>
     * The actual copy is handled by a separate thread so that the user's thread is not held up.
     * There can only be one import/copy per site at a given time
     * <br/>
     * If the isAddMissingToolsOnImportEnabled setting is true, tools that are selected for import
     * and don't already exist in the target site will be added automatically.
     * <br/>
     * This essentially calls importToolsIntoSite in a new thread.
     *
     * @param site          the site to import content into
     * @param toolIds       list of tools already in the site
     * @param toolsToImport list of tools that were selected for import
     * @param cleanup       remove existing content in destination first
     * @return true if the site was successfully queued by the executor, false if there is already another
     * import/copy being performed for this site.
     */
    boolean importToolsIntoSiteThread(final Site site, final List<String> toolIds, final Map<String, List<String>> toolsToImport, final boolean cleanup);

    /**
     * Contains the actual workflow for tools to be imported and their references to be updated
     * @param site        the site
     * @param toolIds     the tool ids in the site to be imported into
     * @param importTools the tools selected to be imported
     * @param cleanup     true if content should be removed before the tool is copied
     */
    void importToolsIntoSite(Site site, List<String> toolIds, Map<String, List<String>> importTools, boolean cleanup);

    /**
     * Copy tool content from old site
     *
     * @param oSiteId        source (old) site id
     * @param site           destination site
     * @param bypassSecurity use SecurityAdvisor if true
     */
    void importToolContent(String oSiteId, Site site, boolean bypassSecurity);

    /**
     * Configuration setting for adding missing tools while importing
     * @return true if missing tools should be added, otherwise false
     */
    boolean isAddMissingToolsOnImportEnabled();

    /**
     * This is used to update exsiting site attributes with encoded site id in it.
     * A new resource item is added to the new site when needed
     *
     * @param oSiteId       source (old) site id
     * @param nSiteId       destination site
     * @param siteAttribute attribute to update
     * @return the new migrated resource url
     */
    String transferSiteResource(String oSiteId, String nSiteId, String siteAttribute);
}
