package org.sakaiproject.sitestats.tool.wicket.pages;

import java.text.Collator;
import java.util.ArrayList;
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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.markup.html.form.select.IOptionRenderer;
import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.extensions.markup.html.form.select.SelectOption;
import org.apache.wicket.extensions.markup.html.form.select.SelectOptions;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.ConversionException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.components.CSSFeedbackPanel;
import org.sakaiproject.sitestats.tool.wicket.components.FileSelectorPanel;
import org.sakaiproject.sitestats.tool.wicket.components.IndicatingAjaxRadioGroup;
import org.sakaiproject.sitestats.tool.wicket.components.LastJobRun;
import org.sakaiproject.sitestats.tool.wicket.components.Menu;
import org.sakaiproject.sitestats.tool.wicket.components.SelectOptionsGroup;
import org.sakaiproject.sitestats.tool.wicket.models.EventModel;
import org.sakaiproject.sitestats.tool.wicket.models.ReportParamsModel;
import org.sakaiproject.sitestats.tool.wicket.models.ToolModel;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.Web;

/**
 * @author Nuno Fernandes
 */
public class ReportsPage extends BasePage {
	private static Log				LOG				= LogFactory.getLog(ReportsPage.class);

	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade	facade;

	private String					realSiteId;
	private String					siteId;
	private String					siteTitle;
	private boolean					visitsEnabled	= true;
	private FeedbackPanel			feedback		= null;

	/** Report related */
	private ReportParamsModel		reportParams	= null;
	private PrefsData				prefsdata		= null;

	/** Ajax update lock */
	private final ReentrantLock		ajaxUpdateLock	= new ReentrantLock();
	private boolean					resourceLoaded	= false;
	private boolean					usersLoaded		= false;
	
	private Collator				collator		= Collator.getInstance();
	
	
	public ReportsPage() {
		this(null, null);
	}
	
	public ReportsPage(ReportParams reportParams) {
		this(reportParams, null);
	}

	public ReportsPage(PageParameters pageParameters) {
		this(null, pageParameters);
	}

	public ReportsPage(ReportParams reportParams, PageParameters pageParameters) {
		this.reportParams = (ReportParamsModel) reportParams;
		realSiteId = facade.getToolManager().getCurrentPlacement().getContext();
		if(pageParameters != null) {
			siteId = pageParameters.getString("siteId");
		}
		if(siteId == null){
			siteId = realSiteId;
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
		StatsUpdateManager statsUpdateManager = facade.getStatsUpdateManager();
		
		// menu
		add(new Menu("menu", siteId));
		
		// display site title/id?
		WebMarkupContainer site = new WebMarkupContainer("site");
		site.setVisible(siteId != null && !realSiteId.equals(siteId));
		add(site);
		siteTitle = facade.getSiteService().getSiteDisplay(siteId);
		site.add(new Label("siteTitle", siteTitle));
		
		// model
		visitsEnabled = statsManager.isEnableSiteVisits();
		if(reportParams == null) {
			reportParams = new ReportParamsModel(siteId);
		}
		if(!visitsEnabled) {
			reportParams.setWhat(ReportManager.WHAT_EVENTS_BYTOOL);
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
		
		// what, when & who
		renderWhatUI(form);
		renderWhenUI(form);
		renderWhoUI(form);
		
		// buttons
		Button generateReport = new Button("generateReport") {
			@Override
			public void onSubmit() {
				if(validReportParameters()) {
					setResponsePage(new ReportDataPage(new PageParameters("siteId="+siteId), reportParams, ReportsPage.this));
				}
				super.onSubmit();
			}
		};
		generateReport.setDefaultFormProcessing(true);
		form.add(generateReport);
	}

	@SuppressWarnings("serial")
	private void renderWhatUI(Form form) {
		// -------------------------------------------------------
		// left panel
		// -------------------------------------------------------
		RadioGroup what = new RadioGroup("reportParams.what");
		form.add(what);
		what.add(new Radio("what-visits", new Model("what-visits")).setVisible(visitsEnabled));
		what.add(new Radio("what-events", new Model("what-events")));
		what.add(new Radio("what-resources", new Model("what-resources")));

		RadioGroup whatEventSelType = new RadioGroup("reportParams.whatEventSelType");
		what.add(whatEventSelType);
		whatEventSelType.add(new Radio("what-events-bytool", new Model("what-events-bytool")));
		whatEventSelType.add(new Radio("what-events-byevent", new Model("what-events-byevent")));

		what.add(new CheckBox("reportParams.whatLimitedAction"));


		// -------------------------------------------------------
		// right panel
		// -------------------------------------------------------
		// resources
		final FileSelectorPanel whatResourceIds = new FileSelectorPanel("reportParams.whatResourceIds", siteId);
		what.add(whatResourceIds);
		what.add(new CheckBox("reportParams.whatLimitedResourceIds"));
		whatResourceIds.setEnabled(true);
		
		// tools
		Select whatToolIds = new Select("reportParams.whatToolIds");
		RepeatingView selectOptionsRV1 = new RepeatingView("selectOptionsRV1");
		whatToolIds.add(selectOptionsRV1);
		whatToolIds.add(new AttributeModifier("title", true, new ResourceModel("report_multiple_sel_instruction")));
		addTools(selectOptionsRV1);
		what.add(whatToolIds);		
		
		// events
		Select whatEventIds = new Select("reportParams.whatEventIds");
		RepeatingView selectOptionsRV2 = new RepeatingView("selectOptionsRV2");
		whatEventIds.add(selectOptionsRV2);
		whatEventIds.add(new AttributeModifier("title", true, new ResourceModel("report_multiple_sel_instruction")));
		addEvents(selectOptionsRV2);
		what.add(whatEventIds);
		
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
		what.add(whatResourceAction);
	}

	@SuppressWarnings("serial")
	private void renderWhenUI(Form form) {
		RadioGroup when = new RadioGroup("reportParams.when");
		form.add(when);
		when.add(new Radio("when-all", new Model("when-all")));
		when.add(new Radio("when-last7days", new Model("when-last7days")));
		when.add(new Radio("when-custom", new Model("when-custom")));
		when.add(new Radio("when-last30days", new Model("when-last30days")));		
		when.add(new DateTimeField("reportParams.whenFrom") {			
			@Override
			protected DateTextField newDateTextField(String id, PropertyModel dateFieldModel) {
				//return new DateTextField(id, dateFieldModel, new PatternDateConverter("yyyy-MM-dd", true));
				return new DateTextField(id, dateFieldModel, new StyleDateConverter("S-", true));
			}
		});
		when.add(new DateTimeField("reportParams.whenTo") {			
			@Override
			protected DateTextField newDateTextField(String id, PropertyModel dateFieldModel) {
				//return new DateTextField(id, dateFieldModel, new PatternDateConverter("yyyy-MM-dd", true));
				return new DateTextField(id, dateFieldModel, new StyleDateConverter("S-", true));
			}
		});
	}
	
	@SuppressWarnings("serial")
	private void renderWhoUI(Form form) {		
		// users (part 1)
		final RepeatingView selectOptionsRV = new RepeatingView("selectOptionsRV");
		final Select whoUserIds = new Select("reportParams.whoUserIds");
		final Radio whoCustom = new Radio("who-custom", new Model("who-custom"));
		
		// left radio selectors
		RadioGroup who = null;
		if(!ReportManager.WHO_CUSTOM.equals(reportParams.getWho())) {
			who = new IndicatingAjaxRadioGroup("reportParams.who", ReportManager.WHO_CUSTOM) {
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					removeAjaxUpdatingBehavior();
					addUsers(selectOptionsRV);
					whoUserIds.setEnabled(true);
					whoUserIds.setVisible(true);
					target.addComponent(this);
					target.addComponent(whoUserIds);
				}
			};
			whoUserIds.setEnabled(false);			
		}else{
			who = new RadioGroup("reportParams.who");
			addUsers(selectOptionsRV);
			whoUserIds.setEnabled(true);
			whoUserIds.setVisible(true);
		}
		form.add(who);
		who.add(new Radio("who-all", new Model("who-all")));
		who.add(new Radio("who-role", new Model("who-role")));
		WebMarkupContainer whoGroupTr = new WebMarkupContainer("who-groups-tr");
		who.add(whoGroupTr);
		whoGroupTr.add(new Radio("who-groups", new Model("who-groups")));
		who.add(whoCustom);
		who.add(new Radio("who-none", new Model("who-none")));	
		
		// users (part 2)
		selectOptionsRV.setEscapeModelStrings(true);
		whoUserIds.add(selectOptionsRV);
		whoUserIds.add(new AttributeModifier("title", true, new ResourceModel("report_multiple_sel_instruction")));
		whoUserIds.setOutputMarkupId(true);
		whoUserIds.setOutputMarkupPlaceholderTag(true);
		whoUserIds.setEscapeModelStrings(true);
		who.add(whoUserIds);
		
		// roles
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
		if(reportParams.getWhoRoleId() == null) {
			if(roles.size() > 0) {
				reportParams.setWhoRoleId(roles.get(0));
			}else{
				reportParams.setWhoRoleId("");
			}
		}
		who.add(whoRoleId);
		
		// groups
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
			if(reportParams.getWhoGroupId() == null) {
				if(groups.size() > 0) {
					reportParams.setWhoGroupId(groups.get(0));
				}else{
					reportParams.setWhoGroupId("");
				}
			}
		}
		whoGroupTr.add(whoGroupId);
	}
	
	@SuppressWarnings("serial")
	private void addTools(final RepeatingView rv) {
		List<SelectOption> tools = new ArrayList<SelectOption>();
		List<ToolInfo> siteTools = facade.getEventRegistryService().getEventRegistry(siteId, getPrefsdata().isListToolEventsOnlyAvailableInSite());
		Iterator<ToolInfo> i = siteTools.iterator();
		while(i.hasNext()){
			ToolInfo toolInfo = i.next();
			if(isToolSuported(toolInfo)) {
				SelectOption opt = new SelectOption("option", new ToolModel(toolInfo));
				tools.add(opt);
			}
		}		
		WebMarkupContainer optgroupItem = new WebMarkupContainer(rv.newChildId());
		rv.add(optgroupItem);
		IOptionRenderer optionRenderer = new IOptionRenderer() {
			public String getDisplayValue(Object object) {
				SelectOption opt = (SelectOption) object;
				return ((ToolModel) opt.getModel()).getToolName();
			}
			public IModel getModel(Object value) {
				SelectOption opt = (SelectOption) value;
				return new Model(((ToolModel) opt.getModel()).getToolId());
			}			
		};
		Collections.sort(tools, getOptionRendererComparator(collator, optionRenderer));
		SelectOptions selectOptions = new SelectOptions("selectOptions", tools, optionRenderer);
		optgroupItem.add(selectOptions);
	}
	
	@SuppressWarnings("serial")
	private void addEvents(final RepeatingView rv) {
		List<ToolInfo> siteTools = facade.getEventRegistryService().getEventRegistry(siteId, getPrefsdata().isListToolEventsOnlyAvailableInSite());
		Collections.sort(siteTools, getToolInfoComparator(collator));
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
				rv.add(optgroupItem);
				SelectOptionsGroup group = new SelectOptionsGroup("group", new Model(toolInfo.getToolName()));
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
			optgroupItem.add(selectOptions);
			usersLoaded = true;
		}finally{
			ajaxUpdateLock.unlock();
		}
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
		ResourceModel msg = null;
		Site site = null;
		try{
			site = facade.getSiteService().getSite(siteId);
		}catch(IdUnusedException e){
			LOG.error("No site with id: "+siteId);
		}
		// fix model fields
//		if(!reportParams.isWhatLimitedAction()) {
//			reportParams.setWhatResourceAction(null);
//		}
//		if(!reportParams.isWhatLimitedResourceIds()) {
//			reportParams.setWhatResourceIds(null);
//		}
		
		// check WHAT
		if(reportParams.getWhat().equals(ReportManager.WHAT_EVENTS)
				&& reportParams.getWhatEventSelType().equals(ReportManager.WHAT_EVENTS_BYTOOL) 
				&& (reportParams.getWhatToolIds() == null || reportParams.getWhatToolIds().size() == 0)){
			msg = new ResourceModel("report_err_notools");
		}else if(reportParams.getWhat().equals(ReportManager.WHAT_EVENTS) 
				&& reportParams.getWhatEventSelType().equals(ReportManager.WHAT_EVENTS_BYEVENTS) 
				&& (reportParams.getWhatEventIds() == null || reportParams.getWhatEventIds().size() == 0)) {
			msg = new ResourceModel("report_err_noevents");
		}else if(reportParams.getWhat().equals(ReportManager.WHAT_RESOURCES) 
				&& reportParams.isWhatLimitedResourceIds() 
				&& (reportParams.getWhatResourceIds() == null || reportParams.getWhatResourceIds().size() == 0)){
			msg = new ResourceModel("report_err_noresources");
			
		// check WHEN
		}else if(reportParams.getWhen().equals(ReportManager.WHEN_CUSTOM)
				&& (reportParams.getWhenFrom() == null || reportParams.getWhenTo() == null)) {
			msg = new ResourceModel("report_err_nocustomdates");
			
		// check WHO
		}else if(reportParams.getWho().equals(ReportManager.WHO_ROLE)){
			if(site.getUsersHasRole(reportParams.getWhoRoleId()).isEmpty())
				msg = new ResourceModel("report_err_emptyrole");	
		}else if(reportParams.getWho().equals(ReportManager.WHO_GROUPS)){
			if(reportParams.getWhoGroupId() == null || reportParams.getWhoGroupId().equals(""))
				msg = new ResourceModel("report_err_nogroup");
			else if(site.getGroup(reportParams.getWhoGroupId()).getUsers().isEmpty())
				msg = new ResourceModel("report_err_emptygroup");	
		}else if(reportParams.getWho().equals(ReportManager.WHO_CUSTOM) 
				&& (reportParams.getWhoUserIds() == null || reportParams.getWhoUserIds().size() == 0)){
			msg = new ResourceModel("report_err_nousers");
		}
		
		if(msg != null){
			error((String) msg.getObject());
			return false;
		}
		return true;
	}

	public void setReportParams(ReportParamsModel reportParams) {
		this.reportParams = reportParams;
	}

	public ReportParams getReportParams() {
		return reportParams;
	}
}

