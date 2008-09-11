package org.sakaiproject.sitestats.tool.bean;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sitestats.api.ServerWideReportManager;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;

public class ServerWideReportBean
{
	/** Our log (commons). */
	private static Log LOG = LogFactory.getLog (ServerWideReportBean.class);

	/** Resource bundle */
	private static String bundleName = FacesContext.getCurrentInstance ()
			.getApplication ().getMessageBundle ();
	private static ResourceLoader msgs = new ResourceLoader (bundleName);

	/** Manager APIs */
	private StatsAuthz SST_authz = null;
	private ServerWideReportManager serverWideReportManager = null;

	// ######################################################################################
	// ManagedBean property methods
	// ######################################################################################
	public void setServiceBean (ServiceBean serviceBean)
	{
		this.SST_authz = serviceBean.getSstAuthz ();
	}

	public void setServerWideReportManager (
			ServerWideReportManager serverWideReportManager)
	{
		this.serverWideReportManager = serverWideReportManager;
	}

	public boolean isAllowed ()
	{
		boolean allowed = SST_authz.isUserAbleToViewSiteStatsAdmin (ToolManager
				.getCurrentPlacement ().getContext ());

		if (!allowed) {
			FacesContext fc = FacesContext.getCurrentInstance ();
			fc.addMessage ("allowed", new FacesMessage (
					FacesMessage.SEVERITY_FATAL, msgs
							.getString ("unauthorized"), null));
		}
		return allowed;
	}

	public void generateReportChart (OutputStream out, Object data)
			throws IOException
	{
		ChartParamsBean params = null;
		if (data instanceof ChartParamsBean) {
			params = (ChartParamsBean) data;
		} else {
			LOG.warn ("data NOT instanceof ChartParamsBean!");
			return;
		}
		BufferedImage img = serverWideReportManager.generateReportChart(
				params.getSelectedReportType(), params.getChartWidth(), params.getChartHeight()
				);

		try{
			ImageIO.write(img, "png", out);
		}catch(Exception e){
			// Load canceled by user. Do nothing.
			LOG.warn("Report chart transfer aborted by client.");
		}
	}

}
