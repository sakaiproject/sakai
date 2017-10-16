/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.myconnections.ui;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.model.BasicConnection;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.model.BasicPerson;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.myconnections.ui.components.ConnectionsGrid;

import lombok.extern.slf4j.Slf4j;

/**
 * Main page for the My Connections widget
 * Note: Far too much of this is too similar to the site-members widget and the 
 * two should be refactored and combined.
 */
@Slf4j
public class WidgetPage extends WebPage {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfileConnectionsLogic")
	private ProfileConnectionsLogic connectionsLogic;

	@SpringBean(name = "org.sakaiproject.tool.api.SessionManager")
	private SessionManager sessionManager;

	@SpringBean(name = "org.sakaiproject.component.api.ServerConfigurationService")
	private ServerConfigurationService serverConfigurationService;

	/**
	 * Class that contains a single site member (just the part needed)
	 */
	public static class GridPerson implements Serializable{
		public String uuid;
		public String displayName;
		public String role;
		public int onlineStatus;
	}

	/**
	 *
	 * Can be overridden via:
	 * widget.myconnections.maxusers=60
	 * widget.myconnections.cols=4
	 */
	int maxUsers = 60;
	int cols = 4;

	public WidgetPage() {
		log.debug("WidgetPage()");
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// get maxUsers, cols
		this.maxUsers = this.serverConfigurationService.getInt("widget.myconnections.maxusers", this.maxUsers);
		this.cols = this.serverConfigurationService.getInt("widget.myconnections.cols", this.cols);

		// get current user
		final String currentUserUuid = this.sessionManager.getCurrentSessionUserId();
		
		// get requests
		List<? extends BasicPerson> requests = this.connectionsLogic.getConnectionRequestsForUser(currentUserUuid);
		
		// sort
		Collections.sort(requests);

		List<GridPerson> requestGridPersons = sliceAndFill(requests, "request", false);

		// get connections, sort and slice
		List<? extends BasicConnection> connections = this.connectionsLogic.getBasicConnectionsForUser(currentUserUuid);

		// sort
		Collections.sort(connections, new Comparator<BasicConnection>() {

			@Override
			public int compare(final BasicConnection o1, final BasicConnection o2) {
				return new CompareToBuilder()
						.append(o1.getOnlineStatus()==ProfileConstants.ONLINE_STATUS_OFFLINE?2:1,
								o2.getOnlineStatus()==ProfileConstants.ONLINE_STATUS_OFFLINE?2:1)
						.append(o1.getDisplayName(), o2.getDisplayName())
						.toComparison();
			}

		});

		List<GridPerson> connectionGridPersons = sliceAndFill(connections, "connection", true);

		// concatenate
		final List<GridPerson> finalList = new ArrayList<GridPerson>();
		finalList.addAll(requestGridPersons);
		finalList.addAll(connectionGridPersons);

		// add connections grid or label
		if (!finalList.isEmpty()) {
			add(new ConnectionsGrid("connections", Model.ofList(finalList), cols));
		} else {
			add(new Label("connections", new ResourceModel("label.noconnections"))
					.add(new AttributeAppender("class", "instruction")));
		}

	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		
		// get the Sakai skin header fragment from the request attribute
		final HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();

		response.render(StringHeaderItem.forString((String) request.getAttribute("sakai.html.head")));
		response.render(OnLoadHeaderItem.forScript("setMainFrameHeight( window.name )"));

		// Tool additions (at end so we can override if required)
		response.render(StringHeaderItem.forString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"));

		// render jQuery and the Wicket event library
		// Both must be priority so they are emitted into the head
		final String cdnQuery = PortalUtils.getCDNQuery();
		response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forUrl(String.format(PortalUtils.getLatestJQueryPath()+ "?version=%s", cdnQuery))));
		response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forUrl(String.format("/my-calendar/scripts/wicket/wicket-event-jquery.min.js?version=%s", cdnQuery))));
	
		// NOTE: All libraries apart from jQuery and Wicket Event must be rendered inline with the application. See WidgetPage.html.
	}

	private List<GridPerson> sliceAndFill(List<? extends BasicPerson> l, String role, boolean canGetStatus){
		List<GridPerson> rval = new ArrayList<>();
		List<BasicPerson> shortList = l
				.stream()
				.limit(this.maxUsers)
				.collect(Collectors.toList());
		for (BasicPerson person : shortList) {
			GridPerson gridPerson = new GridPerson();
			gridPerson.uuid = person.getUuid();
			gridPerson.displayName = person.getDisplayName();
			gridPerson.role = role;
			gridPerson.onlineStatus = canGetStatus ? ((BasicConnection)person).getOnlineStatus() : ProfileConstants.ONLINE_STATUS_OFFLINE;

			rval.add(gridPerson);
		}

		return rval;
	}
}
