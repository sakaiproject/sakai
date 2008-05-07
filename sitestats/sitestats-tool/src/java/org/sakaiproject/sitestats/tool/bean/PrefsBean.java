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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.tree2.TreeNode;
import org.apache.myfaces.custom.tree2.TreeNodeBase;
import org.sakaiproject.sitestats.api.EventInfo;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.ToolInfo;
import org.sakaiproject.sitestats.tool.jsf.InitializableBean;
import org.sakaiproject.sitestats.tool.util.ToolNodeBase;
import org.sakaiproject.util.ResourceLoader;



/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class PrefsBean extends InitializableBean {
	private final static String			CLIENT_ID					= "prefsForm:msg";

	/** Our log (commons). */
	private static Log					LOG							= LogFactory.getLog(ReportsBean.class);

	/** Resource bundle */
	private String						bundleName					= FacesContext.getCurrentInstance().getApplication().getMessageBundle();
	private ResourceLoader				msgs						= new ResourceLoader(bundleName);

	/** Activity events: UI behavior vars */
	private TreeNode					treeData;

	/** Faces message */
	private boolean						showSuccessMessage			= false;
	private boolean						showFatalMessage			= false;

	/** Control vars */
	private String						previousSiteId				= "";
	private PrefsData					prefsdata					= null;
	private List<SelectItem>			chartAlphaValues			= null;

	/** Statistics Manager object */
	private transient ServiceBean		serviceBean					= null;
	private transient StatsManager		SST_sm						= null;
	private transient MessageHandler	messageHandler				= null;
	
	
	// ######################################################################################
	// ManagedBean property methods
	// ######################################################################################	
	public void setServiceBean(ServiceBean serviceBean){
		this.serviceBean = serviceBean;
		this.SST_sm = serviceBean.getSstStatsManager();
	}
	
	public void setMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}
	
	public PrefsData getPrefsdata() {
		String siteId = serviceBean.getSiteId();
		if(prefsdata == null || !previousSiteId.equals(siteId)){
			previousSiteId = siteId;
			prefsdata = SST_sm.getPreferences(siteId, true);
		}
		return prefsdata;
	}
	
	// ######################################################################################
	// Bean methods: Activity tool/events related 
	// ######################################################################################
	public TreeNode getTreeData() {
		treeData = new ToolNodeBase("tool", "Tools", false);
		
		List<ToolInfo> toolImpls = getPrefsdata().getToolEventsDef();
		
		Iterator<ToolInfo> iT = toolImpls.iterator();
		while(iT.hasNext()){
			ToolInfo t = iT.next();
			ToolNodeBase toolNode = new ToolNodeBase("tool", t.getToolName(), t.getToolId(), false, t.isSelected());
			int totalEvents = 0;
			int selectedEvents = 0;
			Iterator<EventInfo> iTE = t.getEvents().iterator();
			while(iTE.hasNext()){
				EventInfo e = iTE.next();
				toolNode.getChildren().add(new ToolNodeBase("event", e.getEventName(), e.getEventId(), true, e.isSelected()));
				totalEvents++;
				if(e.isSelected())
					selectedEvents++;
			}
			toolNode.setAllChildsSelected(t.isSelected() && (selectedEvents == totalEvents));
			treeData.getChildren().add(toolNode);
		}

		return treeData;
	}
	
	public void setTreeData(TreeNode treeNode) {
		LOG.info("setTreeData()");
		this.treeData = treeNode;
	}
	
	public List<SelectItem> getChartAlphaValues() {
		if(chartAlphaValues == null) {
			chartAlphaValues = new ArrayList<SelectItem>();
			for(int i=100; i>=10; i-=10)
				chartAlphaValues.add( new SelectItem(Integer.toString(i), Integer.toString(i)+"%") );
		}
		return chartAlphaValues;
	}
	
	public void setChartTransparency(String value) {
		float converted = (float) round(Double.parseDouble(value)/100,1);
		getPrefsdata().setChartTransparency(converted);
	}
	
	public String getChartTransparency() {
		String currentValue = Integer.toString((int) round(getPrefsdata().getChartTransparency()*100,0) );
		return currentValue;
	}
	

	// ######################################################################################
	// Action/ActionListener methods
	// ######################################################################################
	public void processUpdate(ActionEvent e) {
		if(serviceBean.isEnableSiteActivity()) {
			TreeNode treeObject = treeData;		
			List<ToolInfo> newToolEventPrefs = new ArrayList<ToolInfo>();
			Iterator<ToolNodeBase> iToolNodes = treeObject.getChildren().iterator();
			while(iToolNodes.hasNext()){
				ToolNodeBase toolNB = iToolNodes.next();
				if(toolNB.isSelected()){
					ToolInfo toolInfo = SST_sm.getToolFactory().createTool(toolNB.getIdentifier());
					toolInfo.setSelected(true);
					Iterator<ToolNodeBase> iEventNodes = toolNB.getChildren().iterator();
					while(iEventNodes.hasNext()){
						ToolNodeBase eventNB = iEventNodes.next();
						if(eventNB.isSelected()){
							EventInfo eventInfo = SST_sm.getEventFactory().createEvent(eventNB.getIdentifier());
							eventInfo.setSelected(true);
							toolInfo.addEvent(eventInfo);
						}
					}
					newToolEventPrefs.add(toolInfo);
				}
			}		
			getPrefsdata().setToolEventsDef(newToolEventPrefs);
		}
		boolean opOk = SST_sm.setPreferences(serviceBean.getSiteId(), prefsdata);		
		if(opOk){
			serviceBean.setPreferencesModified();
			messageHandler.addInfoMessage(CLIENT_ID, msgs.getString("prefs_updated"));			
		}else{
			showFatalMessage = true;
			messageHandler.addFatalMessage(CLIENT_ID, msgs.getString("prefs_not_updated"));
		}
		prefsdata = null;
	}

	public void processCancel(ActionEvent e) {
		prefsdata = null;
	}
	
	private static double round(double val, int places) {
		long factor = (long) Math.pow(10, places);
		// Shift the decimal the correct number of places to the right.
		val = val * factor;
		// Round to the nearest integer.
		long tmp = Math.round(val);
		// Shift the decimal the correct number of places back to the left.
		return (double) tmp / factor;
	}
}
