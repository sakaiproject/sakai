package org.sakaiproject.contentreview.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.sakaiproject.contentreview.model.ContentReviewItem;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
/* This class is passed a list of providers in the bean as references, it will use the first
 * by default unless overridden by a site property.
 */
public class ContentReviewFederatedServiceImpl implements ContentReviewService {

    private static Logger log = LoggerFactory.getLogger(ContentReviewFederatedServiceImpl.class);

	private ServerConfigurationService serverConfigurationService;
	private List <ContentReviewService> providers;
	private Map <String,Integer> providersMap;
	
	Integer defaultProvider;

	private ToolManager toolManager;
	private SiteService siteService;
	
    public Site getCurrentSite() {
        Site site = null;
        try {
            String context = toolManager.getCurrentPlacement().getContext();
            site = siteService.getSite( context );
        } catch (Exception e) {
            // sakai failed to get us a location so we can assume we are not ins ide the portal
            site = null;
        }
        return site;
    }

	public void init(){
		defaultProvider = serverConfigurationService.getInt("contentreview.defaultProvider", 0);
	}
	
	public SiteService getSiteService() {
		return siteService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public ToolManager getToolManager() {
		return toolManager;
	}

	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	public ContentReviewService getSelectedProvider() {
		Site currentSite = getCurrentSite();
		String overrideProvider = null;
		if (currentSite != null) {
			if (log.isDebugEnabled())
				log.debug("In Location:" + currentSite.getReference());
			overrideProvider = currentSite.getProperties().getProperty("contentreview.provider");
		}
		if (providers.size() > 0) {
			//Try to get the selected value from the property
			Integer mapProvider = defaultProvider;
			if (overrideProvider != null) {
				mapProvider = providersMap.get(overrideProvider.toLowerCase());
				//Default provider for no lookup in map
				if (mapProvider == null) {
					mapProvider = defaultProvider;
				}
			}
			return providers.get(mapProvider);
		}
		return null;
	}

	public List <ContentReviewService> getProviders() {
		return providers;
	}

	public void setProviders(List <ContentReviewService> providers) {
		this.providers = providers;
		providersMap = new HashMap <String,Integer> ();
		if (log.isDebugEnabled())
			log.debug("Providers registered count:" + providers.size());
		for (int i = 0; i < providers.size(); i++) {
			ContentReviewService p = providers.get(i);
			if (log.isDebugEnabled())
				log.debug("Provider class " + i + " registered as: " + p.getServiceName());
			providersMap.put(p.getServiceName().toLowerCase(), i);
		}
	}

	public ServerConfigurationService getServerConfigurationService() {
		return serverConfigurationService;
	}

	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	public boolean allowResubmission() {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.allowResubmission();
		return false;
	}

	public void checkForReports() {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			provider.checkForReports();	
	}

	public void createAssignment(String arg0, String arg1, Map arg2)
		throws SubmissionException, TransientSubmissionException {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			provider.createAssignment(arg0,arg1,arg2);
		
	}

	public List<ContentReviewItem> getAllContentReviewItems(String arg0,
			String arg1) throws QueueException, SubmissionException,
			ReportException {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.getAllContentReviewItems(arg0,arg1);
		return null;
	}

	public Map getAssignment(String arg0, String arg1)
			throws SubmissionException, TransientSubmissionException {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.getAssignment(arg0,arg1);
		return null;
	}

	public Date getDateQueued(String arg0) throws QueueException {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.getDateQueued(arg0);
		return null;
	}

	public Date getDateSubmitted(String arg0) throws QueueException,
			SubmissionException {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.getDateSubmitted(arg0);
		return null;
	}

	public String getIconUrlforScore(Long score) {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.getIconUrlforScore(score);
		return null;
	}

	public String getLocalizedStatusMessage(String arg0) {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.getLocalizedStatusMessage(arg0);
		return null;
	}

	public String getLocalizedStatusMessage(String arg0, String arg1) {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.getLocalizedStatusMessage(arg0,arg1);
		return null;
	}

	public String getLocalizedStatusMessage(String arg0, Locale arg1) {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.getLocalizedStatusMessage(arg0,arg1);
		return null;
	}

	public List<ContentReviewItem> getReportList(String siteId)
			throws QueueException, SubmissionException, ReportException {
		// TODO Auto-generated method stub
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.getReportList(siteId);
		return null;
	}

	public List<ContentReviewItem> getReportList(String siteId, String taskId)
			throws QueueException, SubmissionException, ReportException {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.getReportList(siteId,taskId);
		return null;
	}

	public String getReviewReport(String contentId, String assignmentRef, String userId) throws QueueException,
			ReportException {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.getReviewReport(contentId, assignmentRef, userId);
		return null;
	}

	public String getReviewReportInstructor(String contentId, String assignmentRef, String userId) throws QueueException,
			ReportException {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.getReviewReportInstructor(contentId, assignmentRef, userId);
		return null;
	}

	public String getReviewReportStudent(String contentId, String assignmentRef, String userId) throws QueueException,
			ReportException {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.getReviewReportStudent(contentId, assignmentRef, userId);
		return null;
	}
	
	public Long getReviewStatus(String contentId) throws QueueException {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.getReviewStatus(contentId);
		return null;
	}

	public String getServiceName() {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.getServiceName();
		return null;
	}

	public boolean allowAllContent()
	{
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.allowAllContent();
		return true;
	}

	public boolean isAcceptableContent(ContentResource arg0) {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.isAcceptableContent(arg0);
		return false;
	}

	public Map<String, SortedSet<String>> getAcceptableExtensionsToMimeTypes()
	{
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
		{
			return provider.getAcceptableExtensionsToMimeTypes();
		}
		return null;
	}

	public Map<String, SortedSet<String>> getAcceptableFileTypesToExtensions()
	{
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
		{
			return provider.getAcceptableFileTypesToExtensions();
		}
		return null;
	}

	public boolean isSiteAcceptable(Site arg0) {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.isSiteAcceptable(arg0);
		return false;
	}

	public void processQueue() {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			provider.processQueue();
	}

	public void queueContent(String userId, String siteId, String assignmentReference, List<ContentResource> content) throws QueueException{
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			provider.queueContent(userId,siteId,assignmentReference,content);
	}

	public void removeFromQueue(String arg0) {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			provider.removeFromQueue(arg0);
	}

	public void resetUserDetailsLockedItems(String arg0) {
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			provider.resetUserDetailsLockedItems(arg0);
		
	}
	public String getReviewError(String contentId){
		ContentReviewService provider = getSelectedProvider();
		if (provider != null)
			return provider.getReviewError(contentId);
		return null;
	}

	public int getReviewScore(String contentId, String assignmentRef, String userId) throws QueueException,
                        ReportException, Exception {
		ContentReviewService provider = getSelectedProvider();
                if (provider != null)
                        return provider.getReviewScore(contentId, assignmentRef, userId);
                return 0;
	}

}
