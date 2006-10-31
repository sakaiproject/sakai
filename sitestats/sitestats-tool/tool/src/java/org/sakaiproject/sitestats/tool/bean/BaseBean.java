/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package org.sakaiproject.sitestats.tool.bean;

import java.io.Serializable;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.Authz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.tool.jsf.InitializableBean;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;



/**
 * @author nfernandes
 */
public class BaseBean extends InitializableBean implements Serializable {
	private static final long	serialVersionUID	= 2279554800802502977L;

	/** Our log (commons). */
	private static Log				LOG			= LogFactory.getLog(BaseBean.class);

	/** Resource bundle */
	protected static ResourceLoader	msgs		= new ResourceLoader("org.sakaiproject.sitestats.tool.bundle.Messages");

	private ToolManager				M_tm		= (ToolManager) ComponentManager.get(ToolManager.class.getName());
	private SiteService				M_ss		= (SiteService) ComponentManager.get(SiteService.class.getName());
	private SessionManager			M_sm		= (SessionManager) ComponentManager.get(SessionManager.class.getName());
	private Authz					authz		= (Authz) ComponentManager.get(Authz.class.getName());
	private StatsManager			sm			= (StatsManager) ComponentManager.get(StatsManager.class.getName());

	private Boolean					allowed		= null;
	private boolean					adminView	= false;
	private Site					site		= null;
	private String					siteId		= null;
	private String					userId		= M_sm.getCurrentSessionUserId();
	private Tool					tool		= M_tm.getCurrentTool();

	public boolean isAllowed() {
		allowed = new Boolean(authz.isUserAbleToViewSiteStats(M_tm.getCurrentPlacement().getContext()));
		
		if(!allowed.booleanValue()){
			FacesContext fc = FacesContext.getCurrentInstance();
			fc.addMessage("allowed", new FacesMessage(FacesMessage.SEVERITY_FATAL, msgs.getString("unauthorized"), null));
		}
		return allowed.booleanValue();
	}

	public String getSiteId() {
		if(siteId == null){
			Placement placement = M_tm.getCurrentPlacement();
			siteId = placement.getContext();
		}
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public Site getSite() {
		try{
			site = M_ss.getSite(getSiteId());
		}catch(IdUnusedException e){
			LOG.warn("BaseBean: no site found with id: " + siteId);
		}
		return site;
	}

	public void setAdminView(boolean adminView){
		this.adminView = adminView;
	}
	
	public boolean isAdminView(){
		return adminView;
	}
	
	public String getSiteTitle() {
		return getSite().getTitle();
	}
	
	public String processActionSiteId() {
		try{
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			Map paramMap = context.getRequestParameterMap();
			String id = (String) paramMap.get("siteId");
			setSiteId(id);
			setAdminView(true);
			return "overview";
		}catch(Exception e){
			LOG.error("Error getting siteId",e);
			return "sitelist";
		}
	}
	
	
}
