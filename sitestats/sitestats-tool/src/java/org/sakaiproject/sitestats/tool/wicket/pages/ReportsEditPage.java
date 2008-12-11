package org.sakaiproject.sitestats.tool.wicket.pages;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.markup.html.form.select.IOptionRenderer;
import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.extensions.markup.html.form.select.SelectOption;
import org.apache.wicket.extensions.markup.html.form.select.SelectOptions;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.Strings;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.components.AjaxIndicator;
import org.sakaiproject.sitestats.tool.wicket.components.CSSFeedbackPanel;
import org.sakaiproject.sitestats.tool.wicket.components.FileSelectorPanel;
import org.sakaiproject.sitestats.tool.wicket.components.IStylableOptionRenderer;
import org.sakaiproject.sitestats.tool.wicket.components.IndicatingAjaxDropDownChoice;
import org.sakaiproject.sitestats.tool.wicket.components.LastJobRun;
import org.sakaiproject.sitestats.tool.wicket.components.Menus;
import org.sakaiproject.sitestats.tool.wicket.components.StylableSelectOptions;
import org.sakaiproject.sitestats.tool.wicket.components.StylableSelectOptionsGroup;
import org.sakaiproject.sitestats.tool.wicket.models.EventModel;
import org.sakaiproject.sitestats.tool.wicket.models.ReportDefModel;
import org.sakaiproject.sitestats.tool.wicket.models.ToolModel;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.Web;

/**
 * @author Nuno Fernandes
 */
public class ReportsEditPage extends BasePage {
	private static final long		serialVersionUID	= 1L;
	private static Log				LOG					= LogFactory.getLog(ReportsEditPage.class);
	private static final String		REPORT_THISSITE		= "this";
	private static final String		REPORT_ALLSITES		= "all";

	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade	facade;

	private String					realSiteId;
	private String					siteId;
	private String					siteTitle;
	private boolean					predefined		= false;
	private String					reportSiteOpt	= REPORT_THISSITE;
	private boolean					visitsEnabled	= true;
	private FeedbackPanel			feedback		= null;
	
	/** Report related */
	private ReportDefModel			reportDefModel;
	private PrefsData				prefsdata		= null;
	private WebPage					returnPage;

	/** Ajax update lock */
	private final ReentrantLock		ajaxUpdateLock	= new ReentrantLock();
	private boolean					usersLoaded		= false;
	
	private transient Collator		collator		= Collator.getInstance();
	
	public ReportsEditPage() {
		this(null, null, null);
	}
	
	public ReportsEditPage(ReportDefModel reportDef) {
		this(reportDef, null, null);
	}

	public ReportsEditPage(PageParameters pageParameters) {
		this(null, pageParameters, null);
	}
	
	public ReportsEditPage(ReportDefModel reportDef, PageParameters pageParameters, final WebPage returnPage) {
		realSiteId = facade.getToolManager().getCurrentPlacement().getContext();
		if(pageParameters != null) {
			siteId = pageParameters.getString("siteId");
			predefined = pageParameters.getBoolean("predefined");
		}
		if(siteId == null) {
			siteId = realSiteId;
		}
		if(reportDef != null) {
			this.reportDefModel = reportDef;
		}else{
			if(predefined) {
				this.reportDefModel = new ReportDefModel(null, null);
			}else{
				this.reportDefModel = new ReportDefModel(siteId, siteId);
			}
		}
		if(returnPage == null) {
			this.returnPage = new ReportsPage(pageParameters);			
		}else{
			this.returnPage = returnPage;
		}
		boolean allowed = facade.getStatsAuthz().isUserAbleToViewSiteStats(siteId);
		if(allowed) {
			renderBody();
		}else{
			setResponsePage(NotAuthorizedPage.class);
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		response.renderJavascriptReference("/library/js/jquery.js");
		response.renderJavascriptReference("/sakai-sitestats-tool/script/common.js");
		response.renderJavascriptReference("/sakai-sitestats-tool/script/reports.js");
		super.renderHead(response);
	}
	
	private void renderBody() {
		StatsManager statsManager = facade.getStatsManager();
		
		// menu
		add(new Menus("menu", siteId));
		
		// reportAction
		String action = null;
		if(getReportDef().isTitleLocalized()) {
			if(reportDefModel.isNew()) {
				action = (String) new ResourceModel("report_adding").getObject();
			}else{
				action = (String) new ResourceModel("report_editing").getObject();
			}
			action = action.replaceAll("\\$\\{title\\}", (String) new ResourceModel(getReportDef().getTitleBundleKey()).getObject());
		}else{
			if(reportDefModel.isNew()) {
				action = new StringResourceModel("report_adding", this, reportDefModel).getString();
			}else{
				action = new StringResourceModel("report_editing", this, reportDefModel).getString();
			}
		}
		add(new Label("reportAction", action));
		
		// model
		visitsEnabled = statsManager.isEnableSiteVisits();
		if(!visitsEnabled) {
			getReportParams().setWhat(ReportManager.WHAT_EVENTS_BYTOOL);
		}
		setModel(new CompoundPropertyModel(this));
		
		// last job run
		add(new LastJobRun("lastJobRun", siteId));
		
		// form
		Form form = new Form("reportsForm");
		add(form);

		// feedback panel (messages)
		feedback = new CSSFeedbackPanel("messages");
		form.add(feedback);
		
		// report details, what, when & who
		renderReportDetailsUI(form);
		renderWhatUI(form);
		renderWhenUI(form);
		renderWhoUI(form);
		renderHowUI(form);
		
		// buttons
		final Button generateReport = new Button("generateReport") {
			@Override
			public void onSubmit() {
				if(validReportParameters()) {
					if(predefined) {
						getReportParams().setSiteId(siteId);
					}
					setResponsePage(new ReportDataPage(reportDefModel, new PageParameters("siteId="+siteId), ReportsEditPage.this));
				}
				super.onSubmit();
			}
		};
		form.add(generateReport);
		final Button saveReport = new Button("saveReport") {
			@Override
			public void onSubmit() {
				if(validReportParameters()) {
					if(getReportDef().getTitle() == null || getReportDef().getTitle().trim().length() == 0) {
						error((String) new ResourceModel("report_reporttitle_req").getObject());
					}else{
						if(predefined) {
							getReportParams().setSiteId(null);
						}
						boolean saved = facade.getReportManager().saveReportDefinition(getReportDef());
						String titleStr = null;
						if(saved) {
							if(getReportDef().isTitleLocalized()) {
								titleStr = (String) new ResourceModel("report_save_success").getObject();
								titleStr = titleStr.replaceAll("\\$\\{title\\}", (String) new ResourceModel(getReportDef().getTitleBundleKey()).getObject());
							}else{
								titleStr = new StringResourceModel("report_save_success", getPage(), reportDefModel).getString();
							}							
							info(titleStr);
							setResponsePage(returnPage);
						}else{
							if(getReportDef().isTitleLocalized()) {
								titleStr = (String) new ResourceModel("report_save_error").getObject();
								titleStr = titleStr.replaceAll("\\$\\{title\\}", (String) new ResourceModel(getReportDef().getTitleBundleKey()).getObject());
							}else{
								titleStr = new StringResourceModel("report_save_error", getPage(), reportDefModel).getString();
							}		
							error(titleStr);							
						}						
					}
				}
				super.onSubmit();
			}
		};
		saveReport.setVisible(!predefined || (predefined && facade.getStatsAuthz().isSiteStatsAdminPage() && realSiteId.equals(siteId)));
		form.add(saveReport);
		final Button back = new Button("back") {
			@Override
			public void onSubmit() {
				setResponsePage(returnPage);
				super.onSubmit();
			}
		};
		form.add(back);
	}

	@SuppressWarnings("serial")
	private void renderReportDetailsUI(Form form) {
		// top
		WebMarkupContainer reportDetailsTop = new WebMarkupContainer("reportDetailsTop");
		WebMarkupContainer reportDetailsShow = new WebMarkupContainer("reportDetailsShow");
		reportDetailsTop.add(reportDetailsShow);
		form.add(reportDetailsTop);
		WebMarkupContainer fakeReportDetails = new WebMarkupContainer("fakeReportDetails");
		reportDetailsTop.add(fakeReportDetails);
		
		// details
		WebMarkupContainer reportDetails = new WebMarkupContainer("reportDetails");
		form.add(reportDetails);
		
		// details: title
		TextField title = new TextField("reportDef.title");
		reportDetails.add(title);
		final WebMarkupContainer titleLocalizedContainer = new WebMarkupContainer("titleLocalizedContainer");
		titleLocalizedContainer.setOutputMarkupId(true);
		titleLocalizedContainer.setOutputMarkupPlaceholderTag(true);
		titleLocalizedContainer.setVisible(getReportDef().isTitleLocalized());
		titleLocalizedContainer.add(new Label("titleLocalized"));
		reportDetails.add(titleLocalizedContainer);
		title.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				titleLocalizedContainer.setVisible(getReportDef().isTitleLocalized());
				target.addComponent(titleLocalizedContainer);
				target.appendJavascript("setMainFrameHeightNoScroll(window.name);");
			}		
		});
		
		// details: description
		TextArea description = new TextArea("reportDef.description");
		reportDetails.add(description);
		final WebMarkupContainer descriptionLocalizedContainer = new WebMarkupContainer("descriptionLocalizedContainer");
		descriptionLocalizedContainer.setOutputMarkupId(true);
		descriptionLocalizedContainer.setOutputMarkupPlaceholderTag(true);
		descriptionLocalizedContainer.setVisible(getReportDef().isDescriptionLocalized());
		descriptionLocalizedContainer.add(new Label("descriptionLocalized"));
		reportDetails.add(descriptionLocalizedContainer);
		description.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				descriptionLocalizedContainer.setVisible(getReportDef().isDescriptionLocalized());
				target.addComponent(descriptionLocalizedContainer);
				target.appendJavascript("setMainFrameHeightNoScroll(window.name);");
			}			
		});

		// set visibility
		if(predefined) {
			if(facade.getStatsAuthz().isSiteStatsAdminPage() && realSiteId.equals(siteId)) {
				reportDetailsTop.setVisible(true);
				reportDetailsShow.setVisible(false);
				reportDetails.setVisible(true);
				fakeReportDetails.setVisible(false);
			}else{
				reportDetailsTop.setVisible(false);
				reportDetailsShow.setVisible(false);
				reportDetails.setVisible(false);
			}
		}else{
			reportDetailsTop.setVisible(true);
			reportDetailsShow.setVisible(true);
			reportDetails.add(new AttributeModifier("style", true, new Model("display: none")));
		}
	}

	@SuppressWarnings("serial")
	private void renderWhatUI(Form form) {
		// -------------------------------------------------------
		// left panel
		// -------------------------------------------------------
		// activity
		List<String> whatOptions = Arrays.asList(ReportManager.WHAT_VISITS, ReportManager.WHAT_EVENTS, ReportManager.WHAT_RESOURCES);
		IChoiceRenderer whatChoiceRenderer = new IChoiceRenderer() {
			public Object getDisplayValue(Object object) {
				if(ReportManager.WHAT_VISITS.equals(object)) {
					return new ResourceModel("report_what_visits").getObject();
				}
				if(ReportManager.WHAT_EVENTS.equals(object)) {
					return new ResourceModel("report_what_events").getObject();
				}
				if(ReportManager.WHAT_RESOURCES.equals(object)) {
					return new ResourceModel("report_what_resources").getObject();
				}
				return object;
			}
			public String getIdValue(Object object, int index) {
				return (String) object;
			}		
		};
		form.add(new DropDownChoice("reportParams.what", whatOptions, whatChoiceRenderer));
		
		// event selection type
		List<String> whatEventSelTypeOptions = Arrays.asList(ReportManager.WHAT_EVENTS_BYTOOL, ReportManager.WHAT_EVENTS_BYEVENTS);
		IChoiceRenderer whatEventSelTypeChoiceRenderer = new IChoiceRenderer() {
			public Object getDisplayValue(Object object) {
				if(ReportManager.WHAT_EVENTS_BYTOOL.equals(object)) {
					return new ResourceModel("report_what_events_bytool").getObject();
				}
				if(ReportManager.WHAT_EVENTS_BYEVENTS.equals(object)) {
					return new ResourceModel("report_what_events_byevent").getObject();
				}
				return object;
			}
			public String getIdValue(Object object, int index) {
				return (String) object;
			}		
		};
		form.add(new DropDownChoice("reportParams.whatEventSelType", whatEventSelTypeOptions, whatEventSelTypeChoiceRenderer).setEscapeModelStrings(false));
		
		// tool selection
		Select whatToolIds = new Select("reportParams.whatToolIds");
		RepeatingView selectOptionsRV1 = new RepeatingView("selectOptionsRV1");
		whatToolIds.add(selectOptionsRV1);
		whatToolIds.add(new AttributeModifier("title", true, new ResourceModel("report_multiple_sel_instruction")));
		addTools(selectOptionsRV1);
		form.add(whatToolIds);
		
		// event selection
		Select whatEventIds = new Select("reportParams.whatEventIds");
		RepeatingView selectOptionsRV2 = new RepeatingView("selectOptionsRV2");
		whatEventIds.add(selectOptionsRV2);
		whatEventIds.add(new AttributeModifier("title", true, new ResourceModel("report_multiple_sel_instruction")));
		addEvents(selectOptionsRV2);
		form.add(whatEventIds);
		
		// resources selection
		form.add(new CheckBox("reportParams.whatLimitedAction"));
		form.add(new CheckBox("reportParams.whatLimitedResourceIds"));
		final FileSelectorPanel whatResourceIds = new FileSelectorPanel("reportParams.whatResourceIds", siteId);
		form.add(whatResourceIds);
		whatResourceIds.setEnabled(true);
		
		// resource actions
		List<String> resourceActions = new ArrayList<String>();
		resourceActions.add(ReportManager.WHAT_RESOURCES_ACTION_NEW);
		resourceActions.add(ReportManager.WHAT_RESOURCES_ACTION_READ);
		resourceActions.add(ReportManager.WHAT_RESOURCES_ACTION_REVS);
		resourceActions.add(ReportManager.WHAT_RESOURCES_ACTION_DEL);
		DropDownChoice whatResourceAction = new DropDownChoice("reportParams.whatResourceAction", resourceActions, new IChoiceRenderer() {
			public Object getDisplayValue(Object object) {
				if(object == null){
					return "";
				}else{
					return (String) new ResourceModel("action_" + ((String) object)).getObject();
				}
			}
			public String getIdValue(Object object, int index) {
				return (String) object;
			}		
		}) {
			@Override
			protected CharSequence getDefaultChoice(Object selected) {
				return "";
			}
			
		};
		form.add(whatResourceAction);
	}

	@SuppressWarnings("serial")
	private void renderWhenUI(Form form) {
		List<String> whenOptions = Arrays.asList(
				ReportManager.WHEN_ALL, ReportManager.WHEN_LAST7DAYS,
				ReportManager.WHEN_LAST30DAYS, ReportManager.WHEN_CUSTOM
				);
		IChoiceRenderer whenChoiceRenderer = new IChoiceRenderer() {
			public Object getDisplayValue(Object object) {
				if(ReportManager.WHEN_ALL.equals(object)) {
					return new ResourceModel("report_when_all").getObject();
				}
				if(ReportManager.WHEN_LAST7DAYS.equals(object)) {
					return new ResourceModel("report_when_last7days").getObject();
				}
				if(ReportManager.WHEN_LAST30DAYS.equals(object)) {
					return new ResourceModel("report_when_last30days").getObject();
				}
				if(ReportManager.WHEN_CUSTOM.equals(object)) {
					return new ResourceModel("report_when_custom").getObject();
				}
				return object;
			}
			public String getIdValue(Object object, int index) {
				return (String) object;
			}		
		};
		form.add(new DropDownChoice("reportParams.when", whenOptions, whenChoiceRenderer));
		
		// custom dates
		form.add(new DateTimeField("reportParams.whenFrom") {			
			@Override
			protected DateTextField newDateTextField(String id, PropertyModel dateFieldModel) {
				return new DateTextField(id, dateFieldModel, new StyleDateConverter("S-", true));
			}
		});
		form.add(new DateTimeField("reportParams.whenTo") {			
			@Override
			protected DateTextField newDateTextField(String id, PropertyModel dateFieldModel) {
				return new DateTextField(id, dateFieldModel, new StyleDateConverter("S-", true));
			}
		});
	}
	
	
	@SuppressWarnings("serial")
	private void renderWhoUI(Form form) {		
		List<String> groups = getGroups();
		final RepeatingView selectOptionsRV = new RepeatingView("selectOptionsRV");
		final Select whoUserIds = new MultipleSelect("reportParams.whoUserIds");
		
		// who		
		List<String> whoOptions = new ArrayList<String>();
		whoOptions.add(ReportManager.WHO_ALL);
		whoOptions.add(ReportManager.WHO_ROLE);
		whoOptions.add(ReportManager.WHO_CUSTOM);
		whoOptions.add(ReportManager.WHO_NONE);
		if(groups.size() > 0) {
			whoOptions.add(2, ReportManager.WHO_GROUPS);
		}
		IChoiceRenderer whoChoiceRenderer = new IChoiceRenderer() {
			public Object getDisplayValue(Object object) {
				if(ReportManager.WHO_ALL.equals(object)) {
					return new ResourceModel("report_who_all").getObject();
				}
				if(ReportManager.WHO_ROLE.equals(object)) {
					return new ResourceModel("report_who_role").getObject();
				}
				if(ReportManager.WHO_GROUPS.equals(object)) {
					return new ResourceModel("report_who_group").getObject();
				}
				if(ReportManager.WHO_CUSTOM.equals(object)) {
					return new ResourceModel("report_who_custom").getObject();
				}
				if(ReportManager.WHO_NONE.equals(object)) {
					return new ResourceModel("report_who_none").getObject();
				}
				return object;
			}
			public String getIdValue(Object object, int index) {
				return (String) object;
			}		
		};
		final IndicatingAjaxDropDownChoice who = new IndicatingAjaxDropDownChoice("reportParams.who", whoOptions, whoChoiceRenderer);
		who.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if(ReportManager.WHO_CUSTOM.equals(getReportParams().getWho())) {
					addUsers(selectOptionsRV);
					whoUserIds.add(new AttributeModifier("style", true, new Model("width: 300px")));
					who.remove(this);
					whoUserIds.add(new AttributeModifier("onchange", true, new Model("checkWhoSelection();")));
					target.addComponent(who);
					target.addComponent(whoUserIds);
				}
				target.appendJavascript("checkWhoSelection();");
			}
			@Override
			protected CharSequence generateCallbackScript(CharSequence partialCall) {
				CharSequence ajaxScript =  super.generateCallbackScript(partialCall);
				StringBuilder b = new StringBuilder();
				b.append("checkWhoSelection();");
				b.append("if(jQuery('.who').val() == 'who-custom') {;");
				b.append(ajaxScript);
				b.append("}");
				return b.toString();
			}
		});
		form.add(who);
		
		// users
		selectOptionsRV.setRenderBodyOnly(true);
		selectOptionsRV.setEscapeModelStrings(true);		
		whoUserIds.add(selectOptionsRV);
		whoUserIds.add(new AttributeModifier("title", true, new ResourceModel("report_multiple_sel_instruction")));
		whoUserIds.setOutputMarkupId(true);
		whoUserIds.setOutputMarkupPlaceholderTag(true);
		whoUserIds.setEscapeModelStrings(true);
		form.add(whoUserIds);
		boolean preloadData = ReportManager.WHO_CUSTOM.equals(getReportParams().getWho());
		if(preloadData) {
			addUsers(selectOptionsRV);
		}
		
		// roles
		List<String> roles = getRoles();
		IChoiceRenderer rolesRenderer = new IChoiceRenderer() {
			public Object getDisplayValue(Object object) {
				return (String) object;
			}
			public String getIdValue(Object object, int index) {
				return (String) object;
			}			
		};
		Collections.sort(roles, getChoiceRendererComparator(collator, rolesRenderer));
		DropDownChoice whoRoleId = new DropDownChoice("reportParams.whoRoleId", roles, rolesRenderer);
		whoRoleId.setEnabled(roles.size() > 0);
		if(getReportParams().getWhoRoleId() == null) {
			if(roles.size() > 0) {
				getReportParams().setWhoRoleId(roles.get(0));
			}else{
				getReportParams().setWhoRoleId("");
			}
		}
		form.add(whoRoleId);
		
		// groups
		WebMarkupContainer whoGroupTr = new WebMarkupContainer("who-groups-tr");
		form.add(whoGroupTr);
		IChoiceRenderer groupsRenderer = new IChoiceRenderer() {
			public Object getDisplayValue(Object object) {
				try{
					return facade.getSiteService().getSite(siteId).getGroup((String) object).getTitle();
				}catch(IdUnusedException e){
					return (String) object;
				}
			}
			public String getIdValue(Object object, int index) {
				return (String) object;
			}		
		};
		Collections.sort(groups, getChoiceRendererComparator(collator, groupsRenderer));
		DropDownChoice whoGroupId = new DropDownChoice("reportParams.whoGroupId", groups, groupsRenderer);
		if(groups.size() == 0) {
			whoGroupTr.setVisible(false);
		}else{
			if(getReportParams().getWhoGroupId() == null) {
				if(groups.size() > 0) {
					getReportParams().setWhoGroupId(groups.get(0));
				}else{
					getReportParams().setWhoGroupId("");
				}
			}
		}
		whoGroupTr.add(whoGroupId);
	}

	@SuppressWarnings("serial")
	private void renderHowUI(Form form) {		
		boolean isSiteStatsAdminTool = facade.getStatsAuthz().isSiteStatsAdminPage();
		boolean renderSiteSelectOption = facade.getStatsAuthz().isSiteStatsAdminPage() && !predefined && realSiteId.equals(siteId);
		boolean renderSiteSortOption = isSiteStatsAdminTool && !predefined && realSiteId.equals(siteId);
		boolean renderSortAscendingOption = isSiteStatsAdminTool && predefined && realSiteId.equals(siteId);

		// site to report
		WebMarkupContainer siteContainer = new WebMarkupContainer("siteContainer");		
		siteContainer.setVisible(renderSiteSelectOption);
		form.add(siteContainer);
		List<String> reportSiteOptions = Arrays.asList(REPORT_THISSITE, REPORT_ALLSITES);
		IChoiceRenderer reportSiteRenderer = new IChoiceRenderer() {
			public Object getDisplayValue(Object object) {
				if(REPORT_THISSITE.equals(object)) {
					return (String) new ResourceModel("report_reportsite_this").getObject();
				}
				if(REPORT_ALLSITES.equals(object)) {
					return (String) new ResourceModel("report_reportsite_all").getObject();
				}
				return (String) new ResourceModel("report_reportsite_this").getObject();
			}
			public String getIdValue(Object object, int index) {
				return (String) object;
			}		
		};
		siteContainer.add(new DropDownChoice("reportSite",new PropertyModel(this, "reportSite") , reportSiteOptions, reportSiteRenderer));
		if(getReportParams().getSiteId() == null) {
			this.reportSiteOpt = REPORT_ALLSITES;
		}else {
			this.reportSiteOpt = REPORT_THISSITE;
		}
		
		// totals by
		Select howTotalsBy = new Select("reportParams.howTotalsBy");
		form.add(howTotalsBy);
		RepeatingView howTotalsByOptions = new RepeatingView("howTotalsByOptions");
		howTotalsBy.add(howTotalsByOptions);
		addGroupOptions(howTotalsByOptions);
		
		// sorting
		WebMarkupContainer trSortBy = new WebMarkupContainer("trSortBy");
		trSortBy.setVisible(renderSortAscendingOption);
		form.add(trSortBy);
		trSortBy.add(new CheckBox("reportParams.howSort"));
		// sort options
		List<String> sortOptions = null;
		if(renderSiteSortOption) {
			sortOptions = Arrays.asList(StatsManager.T_SITE, /*StatsManager.T_USER,*/ StatsManager.T_EVENT, StatsManager.T_RESOURCE, StatsManager.T_RESOURCE_ACTION, StatsManager.T_DATE, StatsManager.T_TOTAL);
		}else{
			sortOptions = Arrays.asList(/*StatsManager.T_USER,*/ StatsManager.T_EVENT, StatsManager.T_RESOURCE, StatsManager.T_RESOURCE_ACTION, StatsManager.T_DATE, StatsManager.T_TOTAL);
		}
		IChoiceRenderer howSortByRenderer = new IChoiceRenderer() {
			public Object getDisplayValue(Object object) {
				if(object != null) {
					String id = (String) object;
					if(ReportManager.HOW_SORT_DEFAULT.equals(id)) {
						return (String) new ResourceModel("default").getObject();
					}
					if(StatsManager.T_SITE.equals(id)) {
						return (String) new ResourceModel("th_site").getObject();
					}
					if(StatsManager.T_USER.equals(id)) {
						return (String) new ResourceModel("report_howtotalsby_user").getObject();
					}
					if(StatsManager.T_EVENT.equals(id)) {
						return (String) new ResourceModel("th_event").getObject();
					}
					if(StatsManager.T_RESOURCE.equals(id)) {
						return (String) new ResourceModel("th_resource").getObject();
					}
					if(StatsManager.T_RESOURCE_ACTION.equals(id)) {
						return (String) new ResourceModel("th_action").getObject();
					}
					if(StatsManager.T_DATE.equals(id)) {
						return (String) new ResourceModel("th_date").getObject();
					}
					if(StatsManager.T_TOTAL.equals(id)) {
						return (String) new ResourceModel("th_total").getObject();
					}
				}
				return (String) new ResourceModel("default").getObject();
			}
			public String getIdValue(Object object, int index) {
				return (String) object;
			}		
		};
		trSortBy.add(new DropDownChoice("reportParams.howSortBy", sortOptions, howSortByRenderer));
		trSortBy.add(new CheckBox("reportParams.howSortAscending"));
		
		// max results
		form.add(new CheckBox("reportParams.howLimitedMaxResults"));
		form.add(new TextField("reportParams.howMaxResults",int.class) {
			@Override
			public String getInput() {
				String[] input = getInputAsArray();
				if(input == null || input.length == 0){
					return "0";
				}else{
					return trim(input[0]);
				}
			}
		});
	}
	
	
	@SuppressWarnings("serial")
	private void addTools(final RepeatingView rv) {
		List<SelectOption> tools = new ArrayList<SelectOption>();
		List<ToolInfo> siteTools = facade.getEventRegistryService().getEventRegistry(siteId, getPrefsdata().isListToolEventsOnlyAvailableInSite());
		Iterator<ToolInfo> i = siteTools.iterator();
		// add tools
		while(i.hasNext()){
			final ToolInfo toolInfo = i.next();
			if(isToolSuported(toolInfo)) {
				tools.add(new SelectOption("option", new ToolModel(toolInfo)));
			}
		}		
		WebMarkupContainer optgroupItem = new WebMarkupContainer(rv.newChildId());
		optgroupItem.setRenderBodyOnly(true);
		rv.add(optgroupItem);
		IStylableOptionRenderer optionRenderer = new IStylableOptionRenderer() {
			public String getDisplayValue(Object object) {
				SelectOption opt = (SelectOption) object;
				return ((ToolModel) opt.getModel()).getToolName();				
			}
			public IModel getModel(Object value) {
				SelectOption opt = (SelectOption) value;
				return new Model(((ToolModel) opt.getModel()).getToolId());
			}
			public String getStyle(Object object) {
				SelectOption opt = (SelectOption) object;
				ToolModel toolModel = (ToolModel) opt.getModel();
				String toolId = toolModel.getToolId();
				if(!ReportManager.WHAT_EVENTS_ALLTOOLS.equals(toolId)) {
					String toolIconPath = "background-image: url(" + facade.getEventRegistryService().getToolIcon(toolId) + ");";
					String style = "background-position:left center; background-repeat:no-repeat; margin-left:3px; padding-left:20px; "+toolIconPath;
					return style;
				}
				return null;
			}		
		};
		Collections.sort(tools, getOptionRendererComparator(collator, optionRenderer));
		// "all" tools (insert in position 0
		tools.add(0, new SelectOption("option", new ToolModel(ReportManager.WHAT_EVENTS_ALLTOOLS, ReportManager.WHAT_EVENTS_ALLTOOLS)));
		StylableSelectOptions selectOptions = new StylableSelectOptions("selectOptions", tools, optionRenderer);
		selectOptions.setRenderBodyOnly(true);
		optgroupItem.add(selectOptions);
	}
	
	@SuppressWarnings("serial")
	private void addEvents(final RepeatingView rv) {
		List<ToolInfo> siteTools = facade.getEventRegistryService().getEventRegistry(siteId, getPrefsdata().isListToolEventsOnlyAvailableInSite());
		Collections.sort(siteTools, getToolInfoComparator(collator));
		// add events
		Iterator<ToolInfo> i = siteTools.iterator();
		while(i.hasNext()){
			ToolInfo toolInfo = i.next();
			if(isToolSuported(toolInfo)) {
				List<EventInfo> eventInfos = toolInfo.getEvents();
				List<SelectOption> events = new ArrayList<SelectOption>();
				Iterator<EventInfo> iE = eventInfos.iterator();				
				while(iE.hasNext()){
					EventInfo e = iE.next();
					SelectOption opt = new SelectOption("option", new EventModel(e));
					events.add(opt);
				}
				WebMarkupContainer optgroupItem = new WebMarkupContainer(rv.newChildId());
				optgroupItem.setRenderBodyOnly(true);
				rv.add(optgroupItem);
				String toolIconPath = "background-image: url(" + facade.getEventRegistryService().getToolIcon(toolInfo.getToolId()) + ");";
				String style = "background-position:left top; background-repeat:no-repeat; margin-left:3px; padding-left:20px; "+toolIconPath;
				StylableSelectOptionsGroup group = new StylableSelectOptionsGroup("group", new Model(toolInfo.getToolName()), new Model(style));
				optgroupItem.add(group);
				SelectOptions selectOptions = new SelectOptions("selectOptions", events, new IOptionRenderer() {
					public String getDisplayValue(Object object) {
						SelectOption opt = (SelectOption) object;
						return ((EventModel) opt.getModel()).getEventName();
					}
					public IModel getModel(Object value) {
						SelectOption opt = (SelectOption) value;
						return new Model(((EventModel) opt.getModel()).getEventId());
					}			
				});
				selectOptions.setRenderBodyOnly(true);				
				group.add(selectOptions);
			}
		}
	}
	
	@SuppressWarnings("serial")
	private void addUsers(final RepeatingView rv) {
		if(usersLoaded) {
			return;
		}
		ajaxUpdateLock.lock();
		try{
			List<SelectOption> users = new ArrayList<SelectOption>();
			// anonymous access
			if(facade.getStatsManager().isShowAnonymousAccessEvents()) {
				SelectOption anon = new SelectOption("option", new Model("?"));
				users.add(anon);
			}
			// site users
			Set<String> siteUsers = null;
			try{
				siteUsers = facade.getSiteService().getSite(siteId).getUsers();
			}catch(IdUnusedException e){
				LOG.warn("Site does not exist: " + siteId);
				siteUsers = new HashSet<String>();
			}
			Iterator<String> i = siteUsers.iterator();
			while(i.hasNext()){
				String userId = i.next();
				if(userId != null) {
					SelectOption opt = new SelectOption("option", new Model(userId));
					opt.setEscapeModelStrings(true);
					users.add(opt);
				}
			}		
			WebMarkupContainer optgroupItem = new WebMarkupContainer(rv.newChildId());
			optgroupItem.setRenderBodyOnly(true);
			rv.add(optgroupItem);
			IOptionRenderer optionRenderer = new IOptionRenderer() {
				public String getDisplayValue(Object object) {
					SelectOption opt = (SelectOption) object;
					String userId = (String) opt.getModel().getObject();
					if(("?").equals(userId)) {
						return Web.escapeHtml( (String) new ResourceModel("user_anonymous_access").getObject() );
					}else{
						User u = null;
						try{
							u = facade.getUserDirectoryService().getUser(userId);
						}catch(UserNotDefinedException e){
							return Web.escapeHtml(userId);
						}
						StringBuilder buff = new StringBuilder();
						buff.append(u.getDisplayName());
						buff.append(" (");
						buff.append(u.getDisplayId());
						buff.append(")");
						return Web.escapeHtml(buff.toString());
					}
				}
				public IModel getModel(Object value) {
					SelectOption opt = (SelectOption) value;
					return new Model( (String) opt.getModel().getObject() );
				}			
			};
			Collections.sort(users, getOptionRendererComparator(collator, optionRenderer));
			SelectOptions selectOptions = new SelectOptions("selectOptions", users, optionRenderer);
			selectOptions.setRenderBodyOnly(true);
			optgroupItem.add(selectOptions);
			usersLoaded = true;
		}finally{
			ajaxUpdateLock.unlock();
		}
	}
	
	@SuppressWarnings("serial")
	private void addGroupOptions(final RepeatingView rv) {
		boolean isSiteStatsAdminTool = facade.getStatsAuthz().isSiteStatsAdminPage();
		boolean renderAdminOptions = isSiteStatsAdminTool && !predefined && realSiteId.equals(siteId);
		
		List<String> totalsOptions = new ArrayList<String>();
		totalsOptions.add(StatsManager.T_USER);
		totalsOptions.add(StatsManager.T_EVENT);
		totalsOptions.add(StatsManager.T_RESOURCE);
		totalsOptions.add(StatsManager.T_RESOURCE_ACTION);
		totalsOptions.add(StatsManager.T_DATE);
		totalsOptions.add(StatsManager.T_LASTDATE);
		if(renderAdminOptions) {
			totalsOptions.add(StatsManager.T_SITE);
		}
		
		// add grouping options
		List<SelectOption> selectOptionList = new ArrayList<SelectOption>();
		Iterator<String> i = totalsOptions.iterator();
		while(i.hasNext()){
			String totalOpt = i.next();
			SelectOption so = new SelectOption("option", new Model(totalOpt));
			so.setEscapeModelStrings(false);
			selectOptionList.add(so);
		}		
		
		WebMarkupContainer optgroupItem = new WebMarkupContainer(rv.newChildId());
		optgroupItem.setRenderBodyOnly(true);
		rv.add(optgroupItem);
		IOptionRenderer optionRenderer = new IOptionRenderer() {
			public String getDisplayValue(Object o) {
				SelectOption opt = (SelectOption) o;
				Object object = opt.getModel().getObject();
				if(StatsManager.T_USER.equals(object)) {
					return (String) new ResourceModel("report_howtotalsby_user").getObject();					
				}
				if(StatsManager.T_EVENT.equals(object)) {
					return (String) new ResourceModel("report_howtotalsby_event").getObject();
				}
				if(StatsManager.T_RESOURCE.equals(object)) {
					return (String) new ResourceModel("report_howtotalsby_resource").getObject();
				}
				if(StatsManager.T_RESOURCE_ACTION.equals(object)) {
					return (String) new ResourceModel("report_howtotalsby_resourceaction").getObject();
				}
				if(StatsManager.T_DATE.equals(object)) {
					return (String) new ResourceModel("report_howtotalsby_date").getObject();
				}
				if(StatsManager.T_LASTDATE.equals(object)) {
					return (String) new ResourceModel("report_howtotalsby_lastdate").getObject();
				}
				if(StatsManager.T_SITE.equals(object)) {
					return (String) new ResourceModel("report_howtotalsby_site").getObject();
				}
				return (String) object;			
			}
			
			public IModel getModel(Object value) {
				SelectOption opt = (SelectOption) value;
				return opt.getModel();
			}
		};
		SelectOptions selectOptions = new SelectOptions("selectOptions", selectOptionList, optionRenderer);
		selectOptions.setRenderBodyOnly(true);
		selectOptions.setEscapeModelStrings(false);
		optgroupItem.add(selectOptions);
	}
	
	private List<String> getGroups() {
		List<String> groups = new ArrayList<String>();
		try{
			Collection<Group> groupCollection = facade.getSiteService().getSite(siteId).getGroups();
			Iterator<Group> i = groupCollection.iterator();
			while(i.hasNext()){
				Group g = i.next();
				groups.add(g.getId());
			}
		}catch(IdUnusedException e){
			LOG.warn("Site does not exist: " + siteId);
			
		}
		return groups;
	}
	
	private List<String> getRoles() {
		List<String> roles = new ArrayList<String>();
		try{
			Set<Role> roleSet = facade.getSiteService().getSite(siteId).getRoles();
			Iterator<Role> i = roleSet.iterator();
			while(i.hasNext()){
				Role r = i.next();
				roles.add(r.getId());
			}
		}catch(IdUnusedException e){
			LOG.warn("Site does not exist: " + siteId);
			
		}
		return roles;
	}
	
	private boolean isToolSuported(final ToolInfo toolInfo) {
		if(facade.getStatsManager().isEventContextSupported()){
			return true;
		}else{
			List<ToolInfo> siteTools = facade.getEventRegistryService().getEventRegistry(siteId, getPrefsdata().isListToolEventsOnlyAvailableInSite());
			Iterator<ToolInfo> i = siteTools.iterator();
			while (i.hasNext()){
				ToolInfo t = i.next();
				if(t.getToolId().equals(toolInfo.getToolId())){
					EventParserTip parserTip = t.getEventParserTip();
					if(parserTip != null && parserTip.getFor().equals(StatsManager.PARSERTIP_FOR_CONTEXTID)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static final Comparator<String> getStringComparator(final Collator collator){
		return new Comparator<String>(){
			public int compare(String o1, String o2) {
				return collator.compare(o1, o2);
			}		
		};
	}
	
	public static final Comparator<ToolInfo> getToolInfoComparator(final Collator collator){
		return new Comparator<ToolInfo>(){
			public int compare(ToolInfo o1, ToolInfo o2) {
				return collator.compare(o1.getToolName(), o2.getToolName());
			}		
		};
	}
	
	public static final Comparator<Object> getOptionRendererComparator(final Collator collator, final IOptionRenderer renderer){
		return new Comparator<Object>(){
			public int compare(Object o1, Object o2) {
				return collator.compare(
						renderer.getDisplayValue(o1),
						renderer.getDisplayValue(o2)
						);
			}		
		};
	}
	
	public static final Comparator<Object> getChoiceRendererComparator(final Collator collator, final IChoiceRenderer renderer){
		return new Comparator<Object>(){
			public int compare(Object o1, Object o2) {
				return collator.compare(
						renderer.getDisplayValue(o1),
						renderer.getDisplayValue(o2)
						);
			}		
		};
	}

	private PrefsData getPrefsdata() {
		if(prefsdata == null) {
			prefsdata = facade.getStatsManager().getPreferences(siteId, true);
		}
		return prefsdata;
	}

	private boolean validReportParameters() {
		Site site = null;
		try{
			site = facade.getSiteService().getSite(siteId);
		}catch(IdUnusedException e){
			LOG.error("No site with id: "+siteId);
		}
		
		// check WHAT
		if(getReportParams().getWhat().equals(ReportManager.WHAT_EVENTS)
				&& getReportParams().getWhatEventSelType().equals(ReportManager.WHAT_EVENTS_BYTOOL) 
				&& (getReportParams().getWhatToolIds() == null || getReportParams().getWhatToolIds().size() == 0)){
			error((String) new ResourceModel("report_err_notools").getObject());
		}
		if(getReportParams().getWhat().equals(ReportManager.WHAT_EVENTS) 
				&& getReportParams().getWhatEventSelType().equals(ReportManager.WHAT_EVENTS_BYEVENTS) 
				&& (getReportParams().getWhatEventIds() == null || getReportParams().getWhatEventIds().size() == 0)) {
			error((String) new ResourceModel("report_err_noevents").getObject());
		}
		if(getReportParams().getWhat().equals(ReportManager.WHAT_RESOURCES) 
				&& getReportParams().isWhatLimitedResourceIds() 
				&& (getReportParams().getWhatResourceIds() == null || getReportParams().getWhatResourceIds().size() == 0)){
			error((String) new ResourceModel("report_err_noresources").getObject());	
		}
		
		// check WHEN
		if(getReportParams().getWhen().equals(ReportManager.WHEN_CUSTOM)
				&& (getReportParams().getWhenFrom() == null || getReportParams().getWhenTo() == null)) {
			error((String) new ResourceModel("report_err_nocustomdates").getObject());
		}
			
		// check WHO
		if(getReportParams().getWho().equals(ReportManager.WHO_ROLE)){
			if(site.getUsersHasRole(getReportParams().getWhoRoleId()).isEmpty())
				error((String) new ResourceModel("report_err_emptyrole").getObject());	
		}else if(getReportParams().getWho().equals(ReportManager.WHO_GROUPS)){
			if(getReportParams().getWhoGroupId() == null || getReportParams().getWhoGroupId().equals(""))
				error((String) new ResourceModel("report_err_nogroup").getObject());
			else if(site.getGroup(getReportParams().getWhoGroupId()).getUsers().isEmpty())
				error((String) new ResourceModel("report_err_emptygroup").getObject());	
		}else if(getReportParams().getWho().equals(ReportManager.WHO_CUSTOM) 
				&& (getReportParams().getWhoUserIds() == null || getReportParams().getWhoUserIds().size() == 0)){
			error((String) new ResourceModel("report_err_nousers").getObject());
		}
		
		// check HOW
		if(getReportParams().getHowTotalsBy() != null){				
			if(getReportParams().getWhat().equals(ReportManager.WHAT_EVENTS)
					&& (getReportParams().getHowTotalsBy().contains(StatsManager.T_RESOURCE) || getReportParams().getHowTotalsBy().contains(StatsManager.T_RESOURCE_ACTION) )) {
				error((String) new ResourceModel("report_err_totalsbyevent").getObject());	
			}else if(getReportParams().getWhat().equals(ReportManager.WHAT_RESOURCES)
					&& getReportParams().getHowTotalsBy().contains(StatsManager.T_EVENT)) {
				error((String) new ResourceModel("report_err_totalsbyresource").getObject());	
			}

		}
		if(getReportParams().isHowSort() && getReportParams().getHowSortBy() != null && !getReportParams().getHowSortBy().equals(ReportManager.HOW_SORT_DEFAULT)){
			if(!StatsManager.T_TOTAL.equals(getReportParams().getHowSortBy()) 
					&& !getReportParams().getHowTotalsBy().contains(getReportParams().getHowSortBy())
					){
				getReportParams().setHowSort(false);
				getReportParams().setHowSortBy(null);
			}
		}
		
		
		return !hasErrorMessage();
	}
	
	public String getReportSite() {
		return reportSiteOpt;
	}
	
	public void setReportSite(String reportSiteOpt) {
		this.reportSiteOpt = reportSiteOpt;
		if(REPORT_THISSITE.equals(reportSiteOpt)) {
			getReportParams().setSiteId(siteId);
		}else if(REPORT_ALLSITES.equals(reportSiteOpt)) {
			getReportParams().setSiteId(null);
		}
	}
	
	public String getTitleLocalized() {
		return (String) new ResourceModel(getReportDef().getTitleBundleKey()).getObject();
	}
	
	public String getDescriptionLocalized() {
		return (String) new ResourceModel(getReportDef().getDescriptionBundleKey()).getObject();
	}

	public ReportDef getReportDef() {
		return (ReportDef) this.reportDefModel.getObject();
	}

	public void setReportParams(ReportParams reportParams) {
		getReportDef().setReportParams(reportParams);
	}

	public ReportParams getReportParams() {
		return getReportDef().getReportParams();
	}
	
	/** Subclass of Select that fixes behavior when used with AjaxFormChoiceComponentUpdatingBehavior.*/
	static class MultipleSelect extends Select {
		private static final long	serialVersionUID	= 1L;
		
		public MultipleSelect(String id) {
			super(id);
		}

		@Override
		public void updateModel() {
			Object converted = getConvertedInput();
			Collection modelCollection = new ArrayList();
			modelChanging();
			if(converted != null){
				modelCollection.addAll((Collection) converted);
			}
			modelChanged();
			getModel().setObject(modelCollection);
			
		}
		
		@Override
		protected void convertInput() {
			String[] paths = getInputAsArray();

			// nothing selected
			if(paths == null || paths.length == 0){
				setConvertedInput(null);
				return;
			}

			// convert
			List converted = new ArrayList(paths.length);
			for(int i = 0; i < paths.length; i++){
				String path = paths[i];
				if(!Strings.isEmpty(path)){
					/*
					 * option component path sans select component path =
					 * relative path from group to option since we know the
					 * option is child of select
					 */
					path = path.substring(getPath().length() + 1);

					// retrieve the selected option component
					SelectOption option = (SelectOption) get(path);

					if(option == null){
						throw new WicketRuntimeException(
								"submitted http post value ["
										+ paths.toString()
										+ "] for SelectOption component ["
										+ getPath()
										+ "] contains an illegal relative path element ["
										+ path
										+ "] which does not point to an SelectOption component. Due to this the Select component cannot resolve the selected SelectOption component pointed to by the illegal value. A possible reason is that component hierarchy changed between rendering and form submission.");
					}
					converted.add(option.getModelObject());
				}
			}
			if(converted.isEmpty()){
				setConvertedInput(null);
			}else{
				setConvertedInput(converted);
			}
		}
	}
}

