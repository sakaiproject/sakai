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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.extensions.markup.html.form.select.SelectOption;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceItemStats;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.tool.dataproviders.AttendanceStatusProvider;
import org.sakaiproject.attendance.tool.dataproviders.EventDataProvider;
import org.sakaiproject.attendance.tool.panels.EventInputPanel;
import org.sakaiproject.attendance.tool.panels.PrintPanel;
import org.sakaiproject.attendance.tool.util.ConfirmationLink;

import java.util.*;

/**
 * The overview page which lists AttendanceEvents and basic statistics of each
 * events AttendanceRecords.
 * 
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public class Overview extends BasePage {
	private static final long serialVersionUID = 1L;

	PrintPanel printPanel;
	WebMarkupContainer printContainer;

	private AttendanceStatusProvider attendanceStatusProvider;

	private Model<String> printHiddenClass = new Model<String>("printHidden");

	public Overview() {
		disableLink(this.homepageLink);

		if (this.role != null && this.role.equals("Student")) {
			throw new RestartResponseException(StudentView.class);
		}

		this.attendanceStatusProvider = new AttendanceStatusProvider(attendanceLogic.getCurrentAttendanceSite(), AttendanceStatusProvider.ACTIVE);
		
		createHeaders();
		createTable();

		this.printContainer = new WebMarkupContainer("print-container");
		printContainer.setOutputMarkupId(true);

		this.printPanel = new PrintPanel("print-panel", new Model<>());

		printContainer.add(printPanel);

		printContainer.add(AttributeModifier.append("class", printHiddenClass));

		add(printContainer);

		createTakeAttendanceNow();
		//createAddAttendanceItem();
	}

	private void createHeaders() {
		// Main header
		Label headerOverview 		= new Label("header-overview",				new ResourceModel("attendance.overview.header"));

		String addButtonText = (new ResourceModel("attendance.add.button")).getObject();
		String takeAttendanceNowText = (new ResourceModel("attendance.now.button")).getObject();
		Label headerInfo 			= new Label("overview-header-info",			new StringResourceModel("attendance.overview.header.info",
				null, new Object[]{addButtonText, takeAttendanceNowText}));
		headerInfo.setEscapeModelStrings(false);

		//headers for the table
		Label headerEventName 		= new Label("header-event-name", 			new ResourceModel("attendance.overview.header.event.name"));
		Label headerEventActions	= new Label("header-event-actions", 		new ResourceModel("attendance.overview.header.event.actions"));
		Label headerEventDate 		= new Label("header-event-date", 			new ResourceModel("attendance.overview.header.event.date"));

		DataView<AttendanceStatus> statusHeaders = new DataView<AttendanceStatus>("status-headers", attendanceStatusProvider) {
			@Override
			protected void populateItem(Item<AttendanceStatus> item) {
				item.add(new Label("header-status-name", getStatusString(item.getModelObject().getStatus())));
			}
		};
		add(statusHeaders);

		add(headerOverview);
		add(headerInfo);
		add(headerEventName);
		add(headerEventActions);
		add(headerEventDate);

	}

	private void createTable() {
		EventDataProvider eventDataProvider = new EventDataProvider();
		DataView<AttendanceEvent> attendanceEventDataView = new DataView<AttendanceEvent>("events", eventDataProvider) {
			@Override
			protected void populateItem(final Item<AttendanceEvent> item) {
				final AttendanceEvent modelObject = item.getModelObject();
				final String name = modelObject.getName();
				final AttendanceItemStats itemStats = attendanceLogic.getStatsForEvent(modelObject);
				Link<Void> eventLink = new Link<Void>("event-link") {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						setResponsePage(new EventView(modelObject, BasePage.OVERVIEW_PAGE));
					}
				};
				eventLink.add(new Label("event-name", name));
				item.add(eventLink);

				Label eventDate = new Label("event-date", modelObject.getStartDateTime());
				eventDate.add(new AttributeModifier("data-text", modelObject.getStartDateTime() != null ? modelObject.getStartDateTime().getTime() : 0));
				item.add(eventDate);

				DataView<AttendanceStatus> activeStatusStats = new DataView<AttendanceStatus>("active-status-stats", attendanceStatusProvider) {
					@Override
					protected void populateItem(Item<AttendanceStatus> item) {
						Status status = item.getModelObject().getStatus();
						int stat = attendanceLogic.getStatsForStatus(itemStats, status);
						item.add(new Label("event-stats", stat));
					}
				};
				item.add(activeStatusStats);
				final AjaxLink eventEditLink = getAddEditWindowAjaxLink(modelObject, "event-edit-link");
				item.add(eventEditLink);
				final AjaxLink printLink = new AjaxLink<Void>("print-link"){
					@Override
					public void onClick(AjaxRequestTarget ajaxRequestTarget) {
						printPanel = new PrintPanel("print-panel", item.getModel());
						printContainer.setOutputMarkupId(true);
						printContainer.addOrReplace(printPanel);
						printHiddenClass.setObject("printVisible");
						ajaxRequestTarget.add(printContainer);
					}
				};
				item.add(printLink);

				ConfirmationLink<Void> deleteLink = new ConfirmationLink<Void>("delete-link", getString("attendance.delete.confirm")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget ajaxRequestTarget) {
						final String name = modelObject.getName();
						if(attendanceLogic.deleteAttendanceEvent(modelObject)) {
							getSession().info(name + " deleted successfully.");
						} else {
							getSession().error("Failed to delete " + name);
						}
						Class<? extends Page> currentPageClass = getPage().getPageClass();
						// Not possible to return to an Event View of a page for an item you've just deleted
						if(EventView.class.equals(currentPageClass)) {
							setResponsePage(Overview.class);
						} else {
							setResponsePage(currentPageClass);
						}
					}

					@Override
					public boolean isEnabled() {
						return !attendanceLogic.getCurrentAttendanceSite().getIsSyncing();
					}
				};
				item.add(deleteLink);
			}
		};
		add(attendanceEventDataView);

		// Create empty table placeholder and make visible based on empty data provider
		Label noEvents = new Label("no-events", getString("attendance.overview.no.items"));
		noEvents.setEscapeModelStrings(false);

		if(eventDataProvider.size() > 0) {
			noEvents.setVisible(false);
		}

		add(noEvents);
	}

	private void createTakeAttendanceNow() {
		final Form<?> takeAttendanceNowForm = new Form<Void>("take-attendance-now-form") {
			@Override
			protected void onSubmit() {
				AttendanceEvent newEvent = new AttendanceEvent();
				newEvent.setAttendanceSite(attendanceLogic.getCurrentAttendanceSite());
				newEvent.setName(new ResourceModel("attendance.now.name").getObject());
				newEvent.setStartDateTime(new Date());
				Long newEventId = (Long) attendanceLogic.addAttendanceEventNow(newEvent);
				if(newEventId != null) {
					newEvent = attendanceLogic.getAttendanceEvent(newEventId);
					setResponsePage(new EventView(newEvent, BasePage.OVERVIEW_PAGE));
				} else {
					error(new ResourceModel("attendance.now.error").getObject());
				}
			}
		};
		takeAttendanceNowForm.add(new SubmitLink("take-attendance-now"));
		add(takeAttendanceNowForm);
	}

	private void createAddAttendanceItem() {
		final Form<?> addAttendanceItemForm = new Form<Void>("add-attendance-item-form");
		final AjaxButton addAttendanceItem = new AjaxButton("add-attendance-item") {
			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				final ModalWindow window = getAddOrEditItemWindow();
				window.setTitle(new ResourceModel("attendance.add.header"));
				window.setContent(new EventInputPanel(window.getContentId(), window, null));
				window.show(target);
			}
		};

		addAttendanceItem.setDefaultFormProcessing(false);
		addAttendanceItem.setOutputMarkupId(true);
		addAttendanceItemForm.add(addAttendanceItem);

		add(addAttendanceItemForm);
	}
}
