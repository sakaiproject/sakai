/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.lessonbuildertool.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lessonbuildertool.LessonBuilderAccessAPI;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderEvents;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.EntityContentProducerEvents;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchUtils;
import org.sakaiproject.search.model.SearchBuilderItem;

import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.cover.UserDirectoryService;

@Slf4j
public class LessonsEntityContentProducer implements EntityContentProducer, EntityContentProducerEvents
{
	static final String REFERENCE_ROOT = Entity.SEPARATOR + "lessonbuilder";

	private SearchIndexBuilder searchIndexBuilder = null;

	private EntityManager entityManager = null;
	
	private SimplePageToolDao simplePageToolDao;
	
	// Map of events to their corresponding search index actions
	private static final Map<String, Integer> EVENT_ACTIONS = Map.of(
			LessonBuilderEvents.ITEM_CREATE, SearchBuilderItem.ACTION_ADD,
			LessonBuilderEvents.ITEM_UPDATE, SearchBuilderItem.ACTION_ADD,
			LessonBuilderEvents.ITEM_DELETE, SearchBuilderItem.ACTION_DELETE,
			// pages aren't indexed, but the text boxes on them take their title
			LessonBuilderEvents.PAGE_UPDATE, SearchBuilderItem.ACTION_UNKNOWN
	);

	private LessonBuilderAccessAPI lessonBuilderAccessAPI;

	private EventTrackingService eventTrackingService;
	private SecurityService securityService;
	private ToolManager toolManager;
	private SessionManager sessionManager;
	private SiteService siteService;
	
	private LessonsAccess lessonsAccess;
	
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setLessonBuilderAccessAPI(LessonBuilderAccessAPI lessonBuilderAccessAPI) {
		this.lessonBuilderAccessAPI = lessonBuilderAccessAPI;
	}

	public void setSimplePageToolDao(SimplePageToolDao simplePageToolDao) {
		this.simplePageToolDao = simplePageToolDao;
	}


	
	public LessonsAccess getLessonsAccess() {
		return lessonsAccess;
	}

	public void setLessonsAccess(LessonsAccess lessonsAccess) {
		this.lessonsAccess = lessonsAccess;
	}

	public void init()
	{
		try
		{
			ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
					.getInstance();
			log.info("init()");
			searchIndexBuilder = (SearchIndexBuilder) load(cm, SearchIndexBuilder.class
					.getName());
			entityManager = (EntityManager) load(cm, EntityManager.class.getName());

			searchIndexBuilder.registerEntityContentProducer(this);
		}
		catch (Throwable t)
		{
			log.error("Failed to init ", t);
		}

	}

	private Object load(ComponentManager cm, String name)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			log.error("Cant find Spring component named " + name);
		}
		return o;
	}

	public String getContent(String reference)
	{

		long id = idFromRef(reference);
		
		SimplePageItem item = simplePageToolDao.findItem(id);
		if (item != null) {
			// a subpage is found by its title
			return item.getType() == SimplePageItem.PAGE ? item.getName() : item.getHtml();
        }
		else {
			log.info("Could not getContent for reference  "+id);
		}
		
	    return "";
	}

	public String getTitle(String reference)
	{
	    
		long id = idFromRef(reference);
		
		SimplePageItem item = simplePageToolDao.findItem(id);
		if (item == null) {
			return "";
		}
		String title = item.getName();
		if (StringUtils.isBlank(title)) {
			// text boxes have no name, so they take the title of the page they are on
			SimplePage page = simplePageToolDao.getPage(item.getPageId());
			title = page != null ? StringUtils.defaultString(page.getTitle()) : "";
		}
		return SearchUtils.appendCleanString(title, null).toString().trim();

	}

	public boolean matches(String reference)
	{
		try
		{
			Reference ref = getReference(reference);
			EntityProducer ep = ref.getEntityProducer();
			String className = ep.getClass().getName();
			return ("org.sakaiproject.lessonbuildertool.service.LessonBuilderEntityProducer".equals(className));
		}
		catch (Exception ex)
		{
			return false;
		}
	}
	
	public Integer getAction(Event event)
	{
		if (LessonBuilderEvents.PAGE_UPDATE.equals(event.getEvent())) {
			reindexTextBoxes(idFromRef(event.getResource()));
		}
		return EVENT_ACTIONS.getOrDefault(event.getEvent(), SearchBuilderItem.ACTION_UNKNOWN);
	}

	// the event is only handed to the index, not posted, so this doesn't show up as users editing these items
	private void reindexTextBoxes(long pageId) {
		for (SimplePageItem item : simplePageToolDao.findItemsOnPage(pageId)) {
			if (item.getType() == SimplePageItem.TEXT && StringUtils.isBlank(item.getName())) {
				searchIndexBuilder.addResource(null, eventTrackingService.newEvent(LessonBuilderEvents.ITEM_UPDATE, makeReference("item", item.getId()), false));
			}
		}
	}

	public boolean matches(Event event)
	{
		return EVENT_ACTIONS.containsKey(event.getEvent());
	}

	public String getTool()
	{
		return "lessons";
	}

	public String getUrl(String reference)
	{
		SimplePageItem item = simplePageToolDao.findItem(idFromRef(reference));
		if (item != null) {
			// open the page the item is on, or a subpage through the link that was found
			String pageUrl = item.getType() == SimplePageItem.PAGE
					? simplePageToolDao.getPageUrl(NumberUtils.toLong(item.getSakaiId()), item.getId())
					: simplePageToolDao.getPageUrl(item.getPageId());
			if (pageUrl != null) {
				return pageUrl;
			}
		}

		Reference ref = getReference(reference);
		//Need to implement public String getEntityUrl(Reference ref)

		if (ref != null && ref.getUrl() != null) {
			return ref.getUrl(); 
		}
		return "";
	}

	private String getSiteId(Reference ref)
	{
		String context = ref.getContext();
		if (context == null) {
		    return null;
		}
		if (context.startsWith("/site/"))
		{
			context = context.substring("/site/".length());
		}
		if (context.startsWith("/"))
		{
			context = context.substring(1);
		}
		int slash = context.indexOf("/");
		if (slash > 0)
		{
			context = context.substring(0, slash);
		}
		if (log.isDebugEnabled())
		{
			log.debug("Lessons.getSiteId" + ref + ":" + context);
		}
		return context;
	}

	public String getSiteId(String resourceName)
	{
		String r = getSiteId(entityManager.newReference(resourceName));
		if (log.isDebugEnabled())
		{
			log.debug("Lessons.getSiteId" + resourceName + ":" + r);
		}
		return r;
	}

	public String makeReference(String type, long id) {
		String ref = REFERENCE_ROOT + Entity.SEPARATOR + type + Entity.SEPARATOR + Long.toString(id);
		return ref;
	}
	
	public Iterator getSiteContentIterator(String context)
	{
		List<Long> itemIds = new ArrayList<>();
		simplePageToolDao.findTextItemsInSite(context).forEach(item -> itemIds.add(item.getId()));
		itemIds.addAll(simplePageToolDao.findSubpageItemIdsInSite(context));
		return itemIds.stream().map(id -> makeReference("item", id)).iterator();
	}

	public boolean isForIndex(String reference)
	{
		long id = idFromRef(reference);
		
		SimplePageItem item = simplePageToolDao.findItem(id);
	
		// top-level pages are in the site navigation already
		return item != null && item.getPageId() != 0L
				&& (SimplePageItem.TEXT == item.getType() || SimplePageItem.PAGE == item.getType()
				|| SimplePageItem.COMMENTS == item.getType() || SimplePageItem.STUDENT_CONTENT == item.getType());
	}

	public boolean canRead(String reference)
	{
		try {
			//Looks like /lessonbuilder/item/39
			long itemId = idFromRef(reference);
			log.debug("canRead:{} itemId:{}", reference, itemId);
			SimplePageItem item = simplePageToolDao.findItem(itemId);
			SimplePage page = item != null ? simplePageToolDao.getPage(item.getPageId()) : null;
			if (page == null || !canReadLessonsInSite(page.getSiteId())) {
				return false;
			}
			// no lock needed, each check builds its own bean and LessonsAccess only holds services
			return lessonsAccess.isItemAccessible(itemId, page.getSiteId(), UserDirectoryService.getCurrentUser().getId(), null);
		} catch (Exception e) {
			// deny this result rather than fail the search for every tool
			log.error("Could not check access to {}", reference, e);
			return false;
		}
	}

	// isItemAccessible doesn't check site permissions, and search covers every site unless told otherwise
	private boolean canReadLessonsInSite(String siteId) {
		try {
			siteService.getSiteVisit(siteId);
		} catch (IdUnusedException | PermissionException e) {
			return false;
		}
		return securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_READ, siteService.siteReference(siteId));
	}

	public Map getCustomProperties(String ref)
	{
		return null;
	}

	public String getCustomRDF(String ref)
	{
		return null;
	}

	private Reference getReference(String reference)
	{
		try
		{
			Reference r = entityManager.newReference(reference);
            log.debug("Lessons.getReference:{}:{}", reference, r);
			return r;
		}
		catch (Exception ex)
		{
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getId(java.lang.String)
	 */
	public String getId(String reference)
	{
		try
		{
			String r = getReference(reference).getId();
            log.debug("Lessons.getId:{}:{}", reference, r);
			return r;
		}
		catch (Exception ex)
		{
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getSubType(java.lang.String)
	 */
	public String getSubType(String reference)
	{
		try
		{
			String r = getReference(reference).getSubType();
            log.debug("Lessons.getSubType:{}:{}", reference, r);
			return r;
		}
		catch (Exception ex)
		{
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getType(java.lang.String)
	 */
	public String getType(String reference)
	{
		try
		{
			String r = getReference(reference).getType();
            log.debug("Lessons.getType:{}:{}", reference, r);
			return r;
		}
		catch (Exception ex)
		{
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.EntityContentProducer#getType(java.lang.String)
	 */
	public String getContainer(String reference)
	{
		try
		{
			String r = getReference(reference).getContainer();
			if (log.isDebugEnabled())
			{
				log.debug("Lessons.getContainer:" + reference + ":" + r);
			}
			return r;
		}
		catch (Exception ex)
		{
			return "";
		}
	}
	
	private long idFromRef (String reference, int length) {
		long id=-1;
		String[] refParts = reference.split(Entity.SEPARATOR);
		if (refParts.length == length) {
			id = Integer.parseInt(refParts[length-1]);
		}		
		return id;	
	}
	
	//Seems like there should be a method for this, but is what most of the code does, lessons length is 4
	private long idFromRef (String reference) {
		return idFromRef(reference,4);
	}

	@Override
	public Set<String> getTriggerFunctions() {
		return EVENT_ACTIONS.keySet();
	}

}
