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

package org.sakaiproject.attendance.tool.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.user.api.User;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * PrintPanel is a panel which allows users to pring an Attendance Sheet or
 * Sign-in Sheet
 *
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class PrintPanel extends BasePanel {
	private static final long serialVersionUID = 1L;
	// TODO: Internationalize
	private static final List<String> PRINT_OPTIONS = Arrays.asList(
			new ResourceModel("attendance.overview.sign.in.sheet").getObject().toString(),
			new ResourceModel("attendance.overview.attendance.sheet").getObject().toString());

	private IModel<AttendanceEvent> eventModel;
	private DropDownChoice<String> groupChoice;
	private transient List<User> userList;
	private String groupOrSiteTitle;

	private String selected = new ResourceModel("attendance.overview.sign.in.sheet").getObject().toString();

	public PrintPanel(String id, IModel<AttendanceEvent> event) {
		super(id, event);
		this.eventModel = event;

		add(createPrintForm());
	}

	private Form<?> createPrintForm() {

		final Form<?> printForm = new Form<Void>("print-form") {

			@Override
			protected void onSubmit() {

				if (groupChoice.getModelObject() == null) {
					userList = sakaiProxy.getCurrentSiteMembership();
					groupOrSiteTitle = sakaiProxy.getCurrentSiteTitle();
				} else {
					userList = sakaiProxy.getGroupMembershipForCurrentSite(groupChoice.getModelObject());
					groupOrSiteTitle = sakaiProxy.getGroupTitleForCurrentSite(groupChoice.getModelObject());
				}

				final boolean isSignIn = selected.equals(new ResourceModel("attendance.overview.sign.in.sheet").getObject().toString());
				String filename = eventModel.getObject().getName().trim().replaceAll("\\s+", "")
						+ (isSignIn ? "-signin.pdf" : "-attendance.pdf");

				AbstractResourceStreamWriter rstream = new AbstractResourceStreamWriter() {
					@Override
					public void write(OutputStream outputStream) throws IOException {
						if (isSignIn) {
							pdfExporter.createSignInPdf(eventModel.getObject(), outputStream, userList,
									groupOrSiteTitle);
						} else {
							pdfExporter.createAttendanceSheetPdf(eventModel.getObject(), outputStream, userList,
									groupOrSiteTitle);
						}
					}
				};

				ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(rstream, filename);
				getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
			}
		};

		if (eventModel.getObject() != null) {
			printForm.add(new Label("event-name", eventModel.getObject().getName()));
			printForm.add(new Label("event-date", eventModel.getObject().getStartDateTime()));
		} else {
			printForm.add(new Label("event-name", ""));
			printForm.add(new Label("event-date", ""));
		}

		List<String> groupIds = sakaiProxy.getAvailableGroupsForCurrentSite();
		Collections.sort(groupIds, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return sakaiProxy.getGroupTitleForCurrentSite(o1).compareTo(sakaiProxy.getGroupTitleForCurrentSite(o2));
			}
		});
		groupChoice = new DropDownChoice<String>("group-choice", new Model<String>(), groupIds,
				new IChoiceRenderer<String>() {
					@Override
					public Object getDisplayValue(String s) {
						return sakaiProxy.getGroupTitleForCurrentSite(s);
					}

					@Override
					public String getIdValue(String s, int i) {
						return s;
					}
				});
		groupChoice.setNullValid(true);
		printForm.add(groupChoice);

		RadioChoice<String> printFormat = new RadioChoice<String>("print-format",
				new PropertyModel<String>(this, "selected"), PRINT_OPTIONS);

		printForm.add(printFormat);

		SubmitLink print = new SubmitLink("print");

		printForm.add(print);

		return printForm;
	}
}
