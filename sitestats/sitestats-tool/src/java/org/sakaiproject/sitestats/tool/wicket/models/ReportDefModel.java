package org.sakaiproject.sitestats.tool.wicket.models;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;

public class ReportDefModel extends LoadableDetachableModel {
	private static final long		serialVersionUID	= 1L;

	@SpringBean
	private transient SakaiFacade	facade;

	private long					id;
	private String 					siteId;
	private String 					reportSiteId;
	
	public ReportDefModel(String siteId, String reportSiteId) {
		super();
		this.id = 0;
		this.siteId = siteId;
		this.reportSiteId = reportSiteId;
	}

	public ReportDefModel(ReportDef reportDef) {
		super(reportDef);
		this.id = reportDef.getId();
		this.siteId = reportDef.getSiteId();
		this.reportSiteId = reportDef.getReportParams().getSiteId();
	}
	
	public ReportDefModel(long id) {
		super();
		this.id = id;
	}
	
	public boolean isNew() {
		return id == 0;
	}
	
	@Override
	protected Object load() {
		ReportDef reportDef = null;		
		if(id == 0) {
			reportDef = new ReportDef();
			reportDef.setSiteId(getSiteId());
			reportDef.setReportParams(new ReportParams(reportSiteId));
		}else{
			reportDef = getFacade().getReportManager().getReportDefinition(id); 
			if(reportDef.getSiteId() == null && reportDef.getReportParams().getSiteId() == null) {
				// fix siteId for predefined reports
				reportDef.getReportParams().setSiteId(getSiteId());
			}
		}
		return reportDef;
	}

	@Override
	public void detach() {
	}
	
	private String getSiteId() {
		if(siteId == null) {
			siteId = reportSiteId;
		}
		return siteId;
	}
	
	private SakaiFacade getFacade() {
		if(facade == null) {
			InjectorHolder.getInjector().inject(this);
		}
		return facade;
	}
}
