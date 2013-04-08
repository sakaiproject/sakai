package org.sakaiproject.tool.syllabus.entityproviders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.api.app.syllabus.SyllabusAttachment;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.ToolManager;

/**
 * Entity provider for the Syllabus tool
 */
@CommonsLog
public class SyllabusEntityProvider extends AbstractEntityProvider implements EntityProvider, AutoRegisterEntityProvider, ActionsExecutable, Outputable, Describeable, RESTful
{

	public final static String ENTITY_PREFIX = "syllabus";

	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	/**
	 * site/siteId
	 */
	@EntityCustomAction(action = "site", viewKey = EntityView.VIEW_LIST)
	public Syllabus getSyllabusForSite(EntityView view) {

		// get siteId
		String siteId = view.getPathSegment(2);

		if(log.isDebugEnabled()) {
			log.debug("news for site " + siteId);
		}

		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException(
					"siteId must be set in order to get the syllabus for a site, via the URL /syllabus/site/siteId");
		}

		//check user can access this site
		Site site;
		try {
			site = siteService.getSiteVisit(siteId);
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Invalid siteId: " + siteId, siteId);
		} catch (PermissionException e) {
			throw new EntityNotFoundException("No access to site: " + siteId, siteId);
		}
		
		//check user can access the tool, it might be hidden
		ToolConfiguration toolConfig = site.getToolForCommonId("sakai.syllabus");
		if(!toolManager.isVisible(site, toolConfig)) {
			throw new EntityNotFoundException("No access to tool in site: " + siteId, siteId);
		}
		
		//get syllabus
		SyllabusItem siteSyllabus = syllabusManager.getSyllabusItemByContextId(siteId);
		if (siteSyllabus == null) {
			throw new EntityNotFoundException("No syllabus for site: " + siteId, siteId);
		}
		
		//If its a redirect, return Syllabus with just the url set
		Syllabus result = new Syllabus();
		result.setSiteId(siteId);
		if(StringUtils.isNotBlank(siteSyllabus.getRedirectURL())) {
			result.setRedirectUrl(siteSyllabus.getRedirectURL());
			return result;
		}
		
		//setup for checking items
		boolean isMaintain = isMaintainer(siteId);
		long currentTime = Calendar.getInstance().getTimeInMillis();
		
		//Get the data
		Set syllabusData = syllabusManager.getSyllabiForSyllabusItem(siteSyllabus);
		
		List<Item> items = new ArrayList<Item>();
		Iterator iter = syllabusData.iterator();
		while(iter.hasNext()) {
			SyllabusData sd = (SyllabusData)iter.next();
						
			//Rule: if item is draft and not maintainer, skip it
			if(StringUtils.equals(sd.getStatus(), SyllabusData.ITEM_DRAFT) && !isMaintain) {
				continue;
			}
			
			//check dates are within range for normal users
			long startDate = dateToLong(sd.getStartDate());
			long endDate = dateToLong(sd.getEndDate());
		
			//Rule: only check dates if not maintain
			if(!isMaintain) {
			
				//Rule: if we have a startDate and our currentTime is before it, then skip item
				if(startDate > 0 && currentTime < startDate) {
					continue;
				}
				
				//Rule: if we have an endDate and our currentTime is after it, then skip item
				if(endDate > 0 && currentTime > endDate) {
					continue;
				}
			}
		
			//convert to our simplified object 
			Item item = new Item();
			item.setTitle(sd.getTitle());
			item.setData(sd.getAsset());
			item.setOrder(sd.getPosition());
			item.setStartDate(startDate);
			item.setEndDate(endDate);
			
			//get the attachments
			List<Attachment> attachments = new ArrayList<Attachment>();
			Set syllabusAttachments = syllabusManager.getSyllabusAttachmentsForSyllabusData(sd);
			Iterator iter2 = syllabusAttachments.iterator();
			while(iter2.hasNext()) {
				SyllabusAttachment sa = (SyllabusAttachment)iter2.next();
				
				Attachment a = new Attachment();
				a.setTitle(sa.getName());
				a.setUrl(sa.getUrl());
				a.setType(sa.getType());
			
				attachments.add(a);
				
			}
			item.setAttachments(attachments);
			items.add(item);
		}
		
		result.setItems(items);
		
		return result;
	}

	
	@Override
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON};
	}

	@Setter
	private SyllabusManager syllabusManager;

	@Setter
	private SiteService siteService;
	
	@Setter
	private ToolManager toolManager;

	
	/**
	 * Simplified helper class to represent the Syllabus for a site
	 */
	public static class Syllabus {
		@Getter @Setter
		private String siteId;
		
		@Getter @Setter
		private String redirectUrl;
		
		@Getter @Setter
		private List<Item> items;
	}
	
	/**
	 * Simplified helper class to represent an individual syllabus item in a site
	 */
	public static class Item {
		
		@Getter @Setter
		private String title;
		
		@Getter @Setter
		private String data;
		
		@Getter @Setter
		private int order;
		
		/* note thet EB turns these into millis anyway, so we might as well return them as millis */
		@Getter @Setter
		private long startDate;
		
		@Getter @Setter
		private long endDate;
		
		@Getter @Setter
		private List<Attachment> attachments;
		
	}
	
	/**
	 * Simplified helper class to store info about an attachment
	 */
	public static class Attachment {
		
		@Getter @Setter
		private String title;
		
		@Getter @Setter
		private String url;
		
		@Getter @Setter
		private String type;
	}
	
	//same logic as the tool uses
	private boolean isMaintainer(String siteId) {
		return siteService.allowUpdateSite(siteId);
	}

	//conert date to long milliseconds, nullsafe.
	private long dateToLong(Date d) {
		if(d != null) {
			return d.getTime();
		} else {
			return 0;
		}
	}
	
	public Object getEntity(EntityReference ref){
		return new HashMap();
	}
	
	public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params){
		if(params != null){
			if(params.containsKey("move")){
				//this is a re-order update:
				try{
					int move = Integer.parseInt(params.get("move").toString());
					boolean ascend = move > 0;
					move = Math.abs(move);
					SyllabusData data = syllabusManager.getSyllabusData(ref.getId());
					SyllabusItem item = syllabusManager.getSyllabusItem(data.getSyllabusItem().getSurrogateKey());
					Long tmp = data.getSyllabusItem().getSurrogateKey();
					//now that we have an obj that has SiteId, let's verify that the user has access to modify it:
					if(!isMaintainer(item.getContextId())){
						throw new IllegalArgumentException("User doesn't have access to modify this site.");
					}
					
					if(data != null){
						boolean foundItem = false;
						int arrayCount = 0;
						List<SyllabusData> movedData = new ArrayList<SyllabusData>();
						String error = "";
						Set syllabusData = syllabusManager.getSyllabiForSyllabusItem(item);
						if(syllabusData != null){
							for(SyllabusData d : (Set<SyllabusData>) syllabusData){
								if(d.getSyllabusId().equals(data.getSyllabusId())){
									foundItem = true;
									if(ascend){
										//we don't need to loop anymore
										break;
									}
								}else{
									if((ascend && !foundItem) || (!ascend && foundItem)){
										movedData.add(d);
										arrayCount++;
									}
								}
								if(!ascend && arrayCount >= move){
									break;
								}
							}
							if("".equals(error)){
								if(ascend){
									//reverse the array if we are ascending
									Collections.reverse(movedData);
								}
								int count = 0;
								for (Iterator iterator = movedData.iterator(); iterator.hasNext();) {
									SyllabusData d = (SyllabusData) iterator.next();
									if(count < move){
										//unfortunately, the positions aren't predictable like a simple pattern 1,2,3,4...
										//but can be a random pattern like 1,4,5,6,9...  This means we can only swap positions
										Integer p1 = data.getPosition();
										Integer p2 = d.getPosition();
										data.setPosition(new Integer(p2));
										d.setPosition(new Integer(p1));
									}else{
										//this isn't being moved
										iterator.remove();
									}
									count++;
								}
								//now that the positions are swapped, store it:
								for(SyllabusData d : movedData){
									syllabusManager.updateSyllabudDataPosition(d, d.getPosition());
								}
								syllabusManager.updateSyllabudDataPosition(data, data.getPosition());
							}else{
								log.warn("Error while changing syllabus position: " + error);
							}
						}
					}
				}catch(Exception e){
					log.warn("Move value wasn't a valid number: " + params.get("move"));
				}
			}
		}
	}

	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params){
		return null;
	}
	
	public void deleteEntity(EntityReference ref, Map<String, Object> params){
		
	}
	public Object getSampleEntity(){
		return new HashMap();
	}

	public List<?> getEntities(EntityReference ref, Search search){
		return null;
	}
	
	public String[] getHandledInputFormats(){
		return new String[] { Formats.XML, Formats.JSON, Formats.HTML};
	}
}
