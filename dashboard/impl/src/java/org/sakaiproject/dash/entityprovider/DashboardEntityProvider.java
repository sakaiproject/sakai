package org.sakaiproject.dash.entityprovider;

import java.util.List;
import java.util.Map;

import lombok.Setter;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.app.DashboardCommonLogic;
import org.sakaiproject.dash.app.SakaiProxy;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;

public class DashboardEntityProvider extends AbstractEntityProvider implements EntityProvider, AutoRegisterEntityProvider, Outputable, Describeable, ActionsExecutable {
    
    	public String getEntityPrefix() {
            		return "dash";
            	}
    
    	public String[] getHandledOutputFormats() {
            		return new String[] {Formats.JSON, Formats.XML};
            	}
    
    	public boolean entityExists(String id) {
            		return true;
            	}
    
    	@EntityCustomAction(action="news",viewKey=EntityView.VIEW_LIST)
    	public List<?> getNewsItems(EntityView view, EntityReference ref, Map<String, Object> params) {
            		
            		String userUuid = sakaiProxy.getCurrentUserId();
            		
            		if(StringUtils.isBlank(userUuid)) {
                    			throw new SecurityException("You must be logged in to get a user's dashboard");
                    		}
            			
            		//get optional params
            		String siteId = (String)params.get("site");
            		boolean hidden = BooleanUtils.toBoolean((String)params.get("hidden"));
        		boolean starred = BooleanUtils.toBoolean((String)params.get("starred"));
        		
            		//only return hidden items
            		if(hidden){
                    			return dashboardCommonLogic.getHiddenNewsLinks(userUuid, siteId,true);
                    		}
            		
            		//only return starred items
            		if(starred){
                    			return dashboardCommonLogic.getStarredNewsLinks(userUuid, siteId,true);
                    		}
            		
            		//return everything
            		return dashboardCommonLogic.getCurrentNewsLinks(userUuid, siteId, true);
            	}
    	
    	@EntityCustomAction(action="calendar",viewKey=EntityView.VIEW_LIST)
    	public List<?> getCalendarItems(EntityView view, EntityReference ref, Map<String, Object> params) {
            		
            		String userUuid = sakaiProxy.getCurrentUserId();
        		
            		if(StringUtils.isBlank(userUuid)) {
                    			throw new SecurityException("You must be logged in to get a user's dashboard");
                    		}
            			
        		//get optional params
            		String siteId = (String)params.get("site");
            		boolean hidden = BooleanUtils.toBoolean((String)params.get("hidden"));
            		boolean starred = BooleanUtils.toBoolean((String)params.get("starred"));
            		boolean past = BooleanUtils.toBoolean((String)params.get("past"));
            				
            		//only return starred items
            		if(starred){
                    			return dashboardCommonLogic.getStarredCalendarLinks(userUuid, siteId,true);
                    		}
            		
            		//only return past items. Could be hidden depending on param
            		if(past){
                    			return dashboardCommonLogic.getPastCalendarLinks(userUuid, siteId, hidden,true);
                    		}
            		
            		//return everything. Could be hidden depending on param
            		return dashboardCommonLogic.getFutureCalendarLinks(userUuid, siteId, hidden, true);
            	}
    	
    	
    	@Setter
    	private DashboardLogic dashboardLogic;

	@Setter
	private DashboardCommonLogic dashboardCommonLogic;
    	
    	@Setter
    	private SakaiProxy sakaiProxy;

    }