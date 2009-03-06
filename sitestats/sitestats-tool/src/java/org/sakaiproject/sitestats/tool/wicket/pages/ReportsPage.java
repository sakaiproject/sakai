package org.sakaiproject.sitestats.tool.wicket.pages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.components.CSSFeedbackPanel;
import org.sakaiproject.sitestats.tool.wicket.components.LastJobRun;
import org.sakaiproject.sitestats.tool.wicket.components.Menus;
import org.sakaiproject.sitestats.tool.wicket.models.ReportDefModel;
import org.sakaiproject.sitestats.tool.wicket.providers.ReportDefsProvider;

public class ReportsPage extends BasePage {
	private static final long		serialVersionUID	= 1L;
	private static Log				LOG					= LogFactory.getLog(ReportsPage.class);

	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade	facade;

	private String					realSiteId;
	private String					siteId;

	
	public ReportsPage() {		
	}
	
	public ReportsPage(PageParameters pageParameters) {
		realSiteId = getFacade().getToolManager().getCurrentPlacement().getContext();
		if(pageParameters != null) {
			siteId = pageParameters.getString("siteId");
		}
		if(siteId == null){
			siteId = realSiteId;
		}
		boolean allowed = getFacade().getStatsAuthz().isUserAbleToViewSiteStats(siteId);
		if(allowed) {
			renderBody();
		}else{
			setResponsePage(NotAuthorizedPage.class);
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.renderJavascriptReference(JQUERYSCRIPT);
		response.renderJavascriptReference("/sakai-sitestats-tool/script/jquery.ifixpng2.js");
		StringBuilder onDomReady = new StringBuilder();
		onDomReady.append("jQuery.ifixpng('/sakai-sitestats-tool/images/transparent.gif');");
		onDomReady.append("jQuery('img').ifixpng();");
		response.renderOnDomReadyJavascript(onDomReady.toString());
	}
	
	private void renderBody() {	
		boolean isSiteStatsAdminPage = getFacade().getStatsAuthz().isSiteStatsAdminPage();
		boolean isAdministering = isSiteStatsAdminPage && realSiteId.equals(siteId);
		boolean isFilteringReportsWithToolsInSite = !isAdministering;
		
		// menu
		add(new Menus("menu", siteId));

		// model
		setModel(new CompoundPropertyModel(this));
		
		add(new Label("pageTitle"));
		
		// last job run
		add(new LastJobRun("lastJobRun", siteId));
		
		// form
		Form form = new Form("reportsForm");
		add(form);

		// feedback panel (messages)
		form.add(new CSSFeedbackPanel("messages"));
		
		
		// my reports
		WebMarkupContainer myReportsContainer = new WebMarkupContainer("myReportsContainer");
		// new report link
		Link lnkNewReport = new Link("lnkNewReport") {
			private static final long	serialVersionUID	= 1L;
			@Override
			public void onClick() {
				PageParameters pageParameters = new PageParameters("siteId="+siteId+",predefined=false");
				setResponsePage(new ReportsEditPage(null, pageParameters, ReportsPage.this));
			}					
		};
		myReportsContainer.add(lnkNewReport);
		// table
		final WebMarkupContainer noReports = new WebMarkupContainer("noReports");
		myReportsContainer.add(noReports);
		final ReportDefsProvider myReportsProvider = new ReportDefsProvider(siteId, ReportDefsProvider.MODE_MYREPORTS, true, false);
		DataView myReports = new DataView("myReports", myReportsProvider) {
			private static final long	serialVersionUID	= 1L;

			@Override
			protected void populateItem(Item item) {
				final ReportDefModel model = (ReportDefModel) item.getModel();
				item.add(new ReportRowFragment("reportRow", model, ReportDefsProvider.MODE_MYREPORTS));
				if(((ReportDef) model.getObject()).isHidden()) {
					item.add(new AttributeModifier("class", true, new Model("hiddenReport")));
				}
			}
			@Override
			protected void onBeforeRender() {
				noReports.setVisible(getRowCount() == 0);
				super.onBeforeRender();
			}
		};
		myReportsContainer.add(myReports);
		form.add(myReportsContainer);
		
		
		// predefined reports
		WebMarkupContainer predefinedReportsContainer = new WebMarkupContainer("predefinedReportsContainer");
		final WebMarkupContainer noPredefReports = new WebMarkupContainer("noReports");
		predefinedReportsContainer.add(noPredefReports);
		WebMarkupContainer adminAddContainer = new WebMarkupContainer("adminAddContainer");
		adminAddContainer.setVisible(isAdministering);
		predefinedReportsContainer.add(adminAddContainer);
		// new predefined report link
		Link lnkPDNewReport = new Link("lnkNewReport") {
			private static final long	serialVersionUID	= 1L;
			@Override
			public void onClick() {
				PageParameters pageParameters = new PageParameters("siteId="+siteId+",predefined=true");
				setResponsePage(new ReportsEditPage(null, pageParameters, ReportsPage.this));
			}					
		};
		adminAddContainer.add(lnkPDNewReport);
		final ReportDefsProvider predefinedReportsProvider = new ReportDefsProvider(siteId, ReportDefsProvider.MODE_PREDEFINED_REPORTS, isFilteringReportsWithToolsInSite, isAdministering);
		DataView predefinedReports = new DataView("predefinedReports", predefinedReportsProvider) {
			private static final long	serialVersionUID	= 1L;

			@Override
			protected void populateItem(Item item) {
				final ReportDefModel model = (ReportDefModel) item.getModel();
				item.add(new ReportRowFragment("reportRow", model, ReportDefsProvider.MODE_PREDEFINED_REPORTS));
				if(((ReportDef) model.getObject()).isHidden()) {
					item.add(new AttributeModifier("class", true, new Model("hiddenReport")));
				}
			}
			@Override
			protected void onBeforeRender() {
				noPredefReports.setVisible(getRowCount() == 0);
				super.onBeforeRender();
			}
		};
		predefinedReportsContainer.add(predefinedReports);
		predefinedReportsContainer.setVisible(isSiteStatsAdminPage || predefinedReportsProvider.size() != 0);
		form.add(predefinedReportsContainer);
		
	}
	
	public String getPageTitle() {
		return (String) new ResourceModel("menu_reports").getObject();
	}
	
	/** Fragment representing a row from report list table. */
	public class ReportRowFragment extends Fragment {
		private static final long	serialVersionUID	= 1L;
		
		public ReportRowFragment(String id, final ReportDefModel model, int mode) {
			super(id, "reportRowFragment", ReportsPage.this);

			final ReportDef reportDef = (ReportDef) model.getObject();
			final boolean isSiteStatsAdminPage = getFacade().getStatsAuthz().isSiteStatsAdminPage();
			final boolean isPredefinedReport = reportDef.getSiteId() == null;
			
			// icon
			WebMarkupContainer icon = new WebMarkupContainer("icon");
			if(mode == ReportDefsProvider.MODE_PREDEFINED_REPORTS) {
				icon.add(new AttributeModifier("src", true, new Model("images/silk/icons/report.png")));
			}
			add(icon);
			
			// link
			Link link = new Link("link") {
				private static final long	serialVersionUID	= 1L;
				@Override
				public void onClick() {
					setResponsePage(new ReportDataPage(model, new PageParameters("siteId="+siteId), ReportsPage.this));
				}					
			};
			
			// title
			String titleStr = null;
			if(reportDef.isTitleLocalized()) {
				titleStr = (String) new ResourceModel(reportDef.getTitleBundleKey()).getObject();
			}else{
				titleStr = reportDef.getTitle();
			}
			link.add(new Label("title", titleStr));
			add(link);
			
			// description
			String descriptionStr = null;
			if(reportDef.isDescriptionLocalized()) {
				descriptionStr = (String) new ResourceModel(reportDef.getDescriptionBundleKey()).getObject();
			}else{
				descriptionStr = reportDef.getDescription();
			}
			add(new Label("description", descriptionStr));
			
			// edit
			Link edit = new Link("edit") {
				private static final long	serialVersionUID	= 1L;
				@Override
				public void onClick() {
					if(isPredefinedReport) {
						setResponsePage(new ReportsEditPage(model, new PageParameters("siteId="+siteId+",predefined=true"), ReportsPage.this));						
					}else{
						setResponsePage(new ReportsEditPage(model, new PageParameters("siteId="+siteId+",predefined=false"), ReportsPage.this));
					}
				}					
			};
			add(edit);
			
			// hide
			WebMarkupContainer hideContainer = new WebMarkupContainer("hideContainer");			
			hideContainer.setVisible(isPredefinedReport && isSiteStatsAdminPage && realSiteId.equals(siteId));
			add(hideContainer);
			Link hide = new Link("hide") {
				private static final long	serialVersionUID	= 1L;
				@Override
				public void onClick() {
					reportDef.setHidden(!reportDef.isHidden());
					getFacade().getReportManager().saveReportDefinition(reportDef);
				}
			};
			hideContainer.add(hide);
			Label hideLabel = new Label("hideLabel");
			if(reportDef.isHidden()) {
				hideLabel.setModel(new ResourceModel("report_unhide"));
			}else{
				hideLabel.setModel(new ResourceModel("report_hide"));
			}
			hide.add(hideLabel);
			
			// duplicate
			WebMarkupContainer duplicateContainer = new WebMarkupContainer("duplicateContainer");
			add(duplicateContainer);
			Link duplicate = new Link("duplicate") {
				private static final long	serialVersionUID	= 1L;
				@Override
				public void onClick() {
					String originalTitle = "";
					if(reportDef.isTitleLocalized()) {
						originalTitle = (String) new ResourceModel(reportDef.getTitleBundleKey()).getObject();
					}else{
						originalTitle = reportDef.getTitle();
					}
					reportDef.setId(0);
					String copyTitle = (String) new ResourceModel("report_duplicate_name").getObject();
					copyTitle = copyTitle.replaceAll("\\$\\{title\\}", originalTitle);
					reportDef.setTitle(copyTitle);
					if(isPredefinedReport && !isSiteStatsAdminPage) {
						reportDef.setSiteId(siteId);
						reportDef.getReportParams().setSiteId(siteId);
					}
					getFacade().getReportManager().saveReportDefinition(reportDef);
				}	
			};
			duplicateContainer.add(duplicate);
			
			// delete
			WebMarkupContainer deleteContainer = new WebMarkupContainer("deleteContainer");
			deleteContainer.setVisible(!isPredefinedReport || (isPredefinedReport && isSiteStatsAdminPage && realSiteId.equals(siteId)));
			add(deleteContainer);
			Link delete = new Link("delete") {
				private static final long	serialVersionUID	= 1L;
				@Override
				public void onClick() {
					getFacade().getReportManager().removeReportDefinition(reportDef);
				}					
				@Override
				protected CharSequence getOnClickScript(CharSequence url) {
					String msg = new StringResourceModel("report_confirm_delete", getPage(), model).getString();
					return "return confirm('"+msg+"');";
				}
			};
			deleteContainer.add(delete);
		}

		
	}
	
	private SakaiFacade getFacade() {
		if(facade == null) {
			InjectorHolder.getInjector().inject(this);
		}
		return facade;
	}
	
}
