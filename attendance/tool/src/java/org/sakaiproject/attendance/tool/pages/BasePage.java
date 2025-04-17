/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.tool.pages;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.*;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.attendance.api.AttendanceGradebookProvider;
import org.sakaiproject.attendance.logic.AttendanceLogic;
import org.sakaiproject.attendance.logic.SakaiProxy;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.tool.panels.EventInputPanel;
import org.sakaiproject.attendance.tool.util.AttendanceFeedbackPanel;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.ResourceLoader;

import javax.servlet.http.HttpServletRequest;


/**
 * This is our base page for our Sakai app. It sets up the containing markup and top navigation.
 * All top level pages should extend from this page so as to keep the same navigation. The content for those pages will
 * be rendered in the main area below the top nav.
 * 
 * <p>It also allows us to setup the API injection and any other common methods, which are then made available in the other pages.
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Slf4j
public class BasePage extends WebPage implements IHeaderContributor {
	@SpringBean(name="org.sakaiproject.attendance.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.attendance.logic.AttendanceLogic")
	protected AttendanceLogic attendanceLogic;

	@SpringBean(name="org.sakaiproject.attendance.api.AttendanceGradebookProvider")
	protected AttendanceGradebookProvider attendanceGradebookProvider;

	protected String role;
	
	Link<Void> addLink;
	Link<Void> homepageLink;
	Link<Void> settingsLink;
	Link<Void> studentOverviewLink;
	Link<Void> gradingLink;
	Link<Void> exportLink;

	FeedbackPanel feedbackPanel;

	@Getter
	ModalWindow addOrEditItemWindow;
	
	public BasePage() {
		
		log.debug("BasePage()");

		this.role = sakaiProxy.getCurrentUserRoleInCurrentSite();

		//Add Event link/tab
		addLink = new Link<Void>("add-link"){
			private static final long serialVersionUID = 1L;
			public void onClick() {
				EventInputPage addEvent = new EventInputPage(new CompoundPropertyModel<>(new AttendanceEvent()));
				addEvent.setNextPage(getPage().getPageClass().getCanonicalName());
				setResponsePage(addEvent);
			}
		};
		addLink.add(new Label("add-link-label", new ResourceModel("attendance.link.add.event")));
		addLink.add(new AttributeModifier("title", new ResourceModel("attendance.link.add.event.tooltip")));
		add(addLink);

    	//Take Attendance Overview link
		homepageLink = new Link<Void>("homepage-link") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				
				setResponsePage(new Overview());
			}
		};
		homepageLink.add(new Label("homepage-link-label",new ResourceModel("attendance.link.homepage")).setRenderBodyOnly(true));
		homepageLink.add(new AttributeModifier("title", new ResourceModel("attendance.link.homepage.tooltip")));
		add(homepageLink);

		//student Overview Link
		studentOverviewLink = new Link<Void>("student-overview-link") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new StudentOverview());
			}
		};
		studentOverviewLink.add(new Label("student-overview-label", new ResourceModel("attendance.link.student.overview")).setRenderBodyOnly(true));
		studentOverviewLink.add(new AttributeModifier("title", new ResourceModel("attendance.link.student.overview.tooltip")));
		add(studentOverviewLink);

		// Settings Link
		settingsLink = new Link<Void>("settings-link") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new SettingsPage());
			}
		};
		settingsLink.add(new Label("settings-link-label", new ResourceModel("attendance.link.settings.label")).setRenderBodyOnly(true));
		settingsLink.add(new AttributeModifier("title", new ResourceModel("attendance.link.settings.tooltip")));
		add(settingsLink);

		// Grading Link
		gradingLink = new Link<Void>("grading-link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				setResponsePage(new GradingPage());
			}
		};
		gradingLink.add(new Label("grading-link-label", new ResourceModel("grading.link.label")).setRenderBodyOnly(true));
		gradingLink.add(new AttributeModifier("title", new ResourceModel("grading.link.tooltip")));
		add(gradingLink);

		// Export Link
		exportLink = new Link<Void>("export-link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				setResponsePage(new ExportPage());
			}
		};
		exportLink.add(new Label("export-link-label", new ResourceModel("export.link.label")).setRenderBodyOnly(true));
		exportLink.add(new AttributeModifier("title", new ResourceModel("export.link.tooltip")));
		add(exportLink);

		// Add a FeedbackPanel for displaying our messages
        feedbackPanel = new AttendanceFeedbackPanel("feedback");
        add(feedbackPanel);

		if(attendanceLogic.getCurrentAttendanceSite().getIsSyncing()) {
			getSession().error((new ResourceModel("attendance.site.syncing.error")).getObject());
		}

		this.addOrEditItemWindow = new ModalWindow("addOrEditItemWindow");
		this.addOrEditItemWindow.showUnloadConfirmation(false);
		this.addOrEditItemWindow.setInitialHeight(400);
		add(this.addOrEditItemWindow);
		getSession().setLocale((new ResourceLoader()).getLocale());
    }
	
	/**
	 * This block adds the isRequired wrapper markup to style it like a Sakai tool.
	 * Add to this any additional CSS or JS references that you need.
	 * 
	 */
	public void renderHead(IHeaderResponse response) {
		//get the Sakai skin header fragment from the request attribute
		HttpServletRequest request = (HttpServletRequest)getRequest().getContainerRequest();
		
		response.render(StringHeaderItem.forString((String)request.getAttribute("sakai.html.head")));
		response.render(OnLoadHeaderItem.forScript("setMainFrameHeight( window.name )"));
		//add Attendance.css as the css class
		response.render(CssHeaderItem.forUrl("css/attendance.css"));
		//Tool additions (at end so we can override if isRequired)
		response.render(StringHeaderItem.forString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"));
		//response.renderCSSReference("css/my_tool_styles.css");
		//response.renderJavascriptReference("js/my_tool_javascript.js");
		// tablesorter
		final String version = ServerConfigurationService.getString("portal.cdn.version", "");
		response.render(JavaScriptHeaderItem.forUrl(String.format("javascript/jquery.tablesorter.min.js?version=%s", version)));
		response.render(JavaScriptHeaderItem.forUrl(String.format("javascript/jquery.tablesorter.widgets.min.js?version=%s", version)));
	}
	
	/** 
	 * Helper to disable a link. Add the Sakai class 'current'.
	 */
	protected void disableLink(Link<Void> l) {
		l.add(new AttributeAppender("class", new Model<String>("current"), " "));
		l.setEnabled(false);
	}

	/**
	 * Helper to disable the Link Headers
	 */
	protected void hideNavigationLink(Link<Void> l) {
		l.setVisible(false);
	}

	protected String getStatusString(Status s) {
		if(s == null) {
			return new ResourceModel("attendance.status.unknown").getObject();
		}
		switch (s)
		{
			case UNKNOWN: return new ResourceModel("attendance.status.unknown").getObject();
			case PRESENT: return new ResourceModel("attendance.status.present").getObject();
			case EXCUSED_ABSENCE: return new ResourceModel("attendance.status.excused").getObject();
			case UNEXCUSED_ABSENCE: return new ResourceModel("attendance.status.absent").getObject();
			case LATE: return new ResourceModel("attendance.status.late").getObject();
			case LEFT_EARLY: return new ResourceModel("attendance.status.left.early").getObject();
			default: return new ResourceModel("attendance.status.unknown").getObject();
		}
	}

	protected AjaxLink<?> getAddEditWindowAjaxLink(final AttendanceEvent obj, final String id) {
		return new AjaxLink<Void>(id) {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				final ModalWindow window = getAddOrEditItemWindow();
				window.setTitle(new ResourceModel("attendance.add.edit.header"));
				window.setCssClassName(window.getCssClassName() + " editItemModal");
				window.setContent(new EventInputPanel(window.getContentId(), window, new CompoundPropertyModel<>(obj)));
				window.show(target);
			}
		};
	}

	public static final String OVERVIEW_PAGE = "overview";
	public static final String STUDENT_PAGE = "student_view";
	public static final String STUDENT_OVERVIEW_PAGE = "student_overview";
}
