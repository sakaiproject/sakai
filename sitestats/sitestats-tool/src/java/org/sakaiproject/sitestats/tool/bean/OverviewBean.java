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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.faces.event.ActionEvent;
import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.SummaryActivityTotals;
import org.sakaiproject.sitestats.api.SummaryVisitsTotals;



/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class OverviewBean {

	/** Our log (commons). */
	private static Log							LOG						= LogFactory.getLog(OverviewBean.class);

	/** Rendering control vars */
	private boolean								renderVisitsTable		= false;
	private boolean								renderActivityTable		= false;

	/** Summary tables objects */
	private SummaryVisitsTotals					summaryVisitsTotals		= null;
	private SummaryActivityTotals				summaryActivityTotals	= null;

	/** Benas, Services */
	private transient ServiceBean				serviceBean				= null;

	/** Other */
	private String								previousSiteId			= "";
	private PrefsData							prefsdata				= null;
	private long								prefsLastModified		= 0;
	
	
	// ######################################################################################
	// ManagedBean property methods
	// ######################################################################################	
	public void setServiceBean(ServiceBean serviceBean){
		this.serviceBean = serviceBean;
	}	
	
	// ######################################################################################
	// Main methods
	// ######################################################################################
	public OverviewBean() {
	}
	
	private PrefsData getPrefsdata(String siteId) {
		if(siteId == null)
			siteId = serviceBean.getSiteId();
		if(prefsdata == null || prefsLastModified < serviceBean.getPreferencesLastModified() || !previousSiteId.equals(siteId)){
			previousSiteId = siteId;
			prefsdata = serviceBean.getSstStatsManager().getPreferences(siteId, false);
			prefsLastModified = serviceBean.getPreferencesLastModified();
		}
		return prefsdata;
	}
	
	// ######################################################################################
	// Chart methods
	// ######################################################################################
	public void generateVisitsChart(OutputStream out, Object data) throws IOException {
		LOG.debug("generateVisitsChart()");
		ChartParamsBean params = null;
		if(data instanceof ChartParamsBean)
			 params = (ChartParamsBean) data;
		else{
			LOG.warn("data NOT instanceof ChartParamsBean!");
			return;
		}
		BufferedImage img = serviceBean.getSstChartService().generateVisitsChart(
				params.getSiteId(), params.getSelectedVisitsView(),
				params.getChartWidth(), params.getChartHeight(), 
				getPrefsdata(params.getSiteId()).isChartIn3D(), 
				getPrefsdata(params.getSiteId()).getChartTransparency(), 
				getPrefsdata(params.getSiteId()).isItemLabelsVisible());

		try{
			ImageIO.write(img, "png", out);
			LOG.debug("generateVisitsChart(): chart written to outputstream");
		}catch(Exception e){
			// Load canceled by user. Do nothing.
			LOG.warn("Visits chart transfer aborted by client.");
		}
	}
	
	public void generateActivityChart(OutputStream out, Object data) throws IOException {
		LOG.debug("generateActivityChart()");
		ChartParamsBean params = null;
		if(data instanceof ChartParamsBean)
			 params = (ChartParamsBean) data;
		else{
			LOG.warn("data NOT instanceof ChartParamsBean!");
			return;
		}
		BufferedImage img = serviceBean.getSstChartService().generateActivityChart(
				params.getSiteId(), params.getSelectedActivityView(), params.getSelectedActivityChartType(),
				params.getChartWidth(), params.getChartHeight(), 
				getPrefsdata(params.getSiteId()).isChartIn3D(), 
				getPrefsdata(params.getSiteId()).getChartTransparency(), 
				getPrefsdata(params.getSiteId()).isItemLabelsVisible());

		try{
			ImageIO.write(img, "png", out);
			LOG.debug("generateActivityChart(): chart written to outputstream");
		}catch(Exception e){
			// Load canceled by user. Do nothing.
			LOG.warn("Activity chart transfer aborted by client.", e);
		}
	}
	
	// ######################################################################################
	// Summary tables methods
	// ######################################################################################
	public SummaryVisitsTotals getSummaryVisitsTotals(){
		summaryVisitsTotals = serviceBean.getSstStatsManager().getSummaryVisitsTotals(serviceBean.getSiteId());
		return summaryVisitsTotals;
	}
	
	public SummaryActivityTotals getSummaryActivityTotals(){
		summaryActivityTotals = serviceBean.getSstStatsManager().getSummaryActivityTotals(serviceBean.getSiteId());//, getPrefsdata());
		return summaryActivityTotals;
	}
	
	// ######################################################################################
	// Table render methods
	// ######################################################################################
	public boolean isRenderVisitsTable() {
		return renderVisitsTable;
	}
	
	public void renderVisitsTable(ActionEvent e) {
		this.renderVisitsTable = true;
	}
	
	public boolean isRenderActivityTable() {
		return renderActivityTable;
	}
	
	public void renderActivityTable(ActionEvent e) {
		this.renderActivityTable = true;
	}
	
	public void setAllRenderFalse(ActionEvent e) {
		this.renderVisitsTable = false;
		this.renderActivityTable = false;		
	}
	
}
