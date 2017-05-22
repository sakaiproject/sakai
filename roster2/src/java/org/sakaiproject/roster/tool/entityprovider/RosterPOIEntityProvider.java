/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational
* Community License, Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.roster.tool.entityprovider;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.roster.api.RosterEnrollment;
import org.sakaiproject.roster.api.RosterFunctions;
import org.sakaiproject.roster.api.RosterGroup;
import org.sakaiproject.roster.api.RosterMember;
import org.sakaiproject.roster.api.RosterSite;
import org.sakaiproject.roster.api.SakaiProxy;
import org.sakaiproject.util.ResourceLoader;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <code>RosterPOIEntityProvider</code> allows Roster to export to Excel via
 * Apache's POI.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
@Setter @Slf4j
public class RosterPOIEntityProvider extends AbstractEntityProvider implements
		AutoRegisterEntityProvider, ActionsExecutable, RequestAware {
    
	public final static String ENTITY_PREFIX		= "roster-export";
	public final static String DEFAULT_ID			= ":ID:";
	
	// error messages
	public final static String MSG_INVALID_ID			= "Invalid site ID";
	public final static String MSG_NO_SESSION			= "Must be logged in";
	public final static String MSG_NO_SITE_ID			= "Must provide a site ID";
	public final static String MSG_NO_FILE_CREATED		= "Error creating file";
	public final static String MSG_NO_EXPORT_PERMISSION = "Current user does not have export permission";
	public final static String MSG_UNABLE_TO_RETRIEVE_SITE = "Unable to retrieve the requested site";
	
	// roster views
	public final static String VIEW_OVERVIEW			= "overview";
	public final static String VIEW_ENROLLMENT_STATUS	= "status";
		
	// key passed as parameters
	public final static String KEY_GROUP_ID				= "groupId";
	public final static String KEY_ROLE_ID				= "roleId";
	public final static String KEY_VIEW_TYPE			= "viewType";
	public final static String KEY_BY_GROUP				= "byGroup";
	public final static String KEY_ENROLLMENT_SET_ID	= "enrollmentSetId";
	public final static String KEY_ENROLLMENT_STATUS	= "enrollmentStatus";
	public final static String KEY_FACET_NAME			= "facetName";
	public final static String KEY_FACET_USER_ID		= "facetUserId";
	public final static String KEY_FACET_EMAIL			= "facetEmail";
	public final static String KEY_FACET_ROLE			= "facetRole";
	public final static String KEY_FACET_GROUPS			= "facetGroups";
	public final static String KEY_FACET_STATUS			= "facetStatus";
	public final static String KEY_FACET_CREDITS		= "facetCredits";
		
	// defaults to use if any keys are not specified
	public final static String DEFAULT_FACET_NAME		= "Name";
	public final static String DEFAULT_FACET_USER_ID	= "User ID";
	public final static String DEFAULT_FACET_EMAIL		= "Email Address";
	public final static String DEFAULT_FACET_ROLE		= "Role";
	public final static String DEFAULT_FACET_GROUPS		= "Groups";
	public final static String DEFAULT_FACET_STATUS		= "Status";
	public final static String DEFAULT_FACET_CREDITS	= "Credits";
	public final static String DEFAULT_GROUP_ID			= "all";
	public final static String DEFAULT_ENROLLMENT_STATUS= "all";
	public final static String DEFAULT_VIEW_TYPE		= VIEW_OVERVIEW;
	public final static boolean DEFAULT_BY_GROUP		= false;
	
	// misc
	public final static String FILE_EXTENSION		= ".xlsx";
	public final static String FILENAME_SEPARATOR	= "_";
	public final static String FILENAME_BYGROUP		= "ByGroup";
	public final static String FILENAME_UNGROUPED	= "Ungrouped";
		
	private SakaiProxy sakaiProxy;
	private RequestGetter requestGetter;
		
	/**
	 * {@inheritDoc}
	 */
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
	
	@EntityCustomAction(action = "export-to-excel", viewKey = EntityView.VIEW_SHOW)
	public void exportToExcel(OutputStream out, EntityReference reference, Map<String, Object> parameters) {

		String userId = developerHelperService.getCurrentUserId();

		if (userId == null) {
			throw new EntityException(MSG_NO_SESSION, reference.getReference());
		}

		HttpServletResponse response = requestGetter.getResponse();

		String siteId = reference.getId();
		if (StringUtils.isBlank(siteId) || DEFAULT_ID.equals(siteId)) {
			throw new EntityException(MSG_NO_SITE_ID, reference.getReference());
		}

		try {
			if (sakaiProxy.hasUserSitePermission(userId, RosterFunctions.ROSTER_FUNCTION_EXPORT, siteId)) {
				RosterSite site = sakaiProxy.getRosterSite(siteId);
				if (null == site) {
					throw new EntityException(MSG_UNABLE_TO_RETRIEVE_SITE, reference.getReference());
				}
				export(userId, response, site, parameters);
				
			} else {
				throw new EntityException(MSG_NO_EXPORT_PERMISSION, reference.getReference());
			}
		} catch (IOException e) {
			log.error(MSG_NO_FILE_CREATED, e);
			throw new EntityException(MSG_NO_FILE_CREATED, reference.getReference());
		}
	}
		
	private void addResponseHeader(HttpServletResponse response, String filename) {
		
		response.addHeader("Content-Encoding", "base64");
		response.addHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.addHeader("Content-Disposition", "attachment; filename=" + filename);
	}
	
	// TODO split into separate methods for different roster views
	private String createFilename(RosterSite site, String groupId,
			String viewType, boolean byGroup, String enrollmentSetId,
			String enrollmentStatus) {

		StringBuffer filename = new StringBuffer();

		if (VIEW_OVERVIEW.equals(viewType)) {

			filename.append(site.getTitle());
			
			if (null != groupId && !DEFAULT_GROUP_ID.equals(groupId)) {

				for (RosterGroup group : site.getSiteGroups()) {
					if (group.getId().equals(groupId)) {
						filename.append(FILENAME_SEPARATOR);
						filename.append(group.getTitle());
						break;
					}
				}
			}
		} else if (VIEW_ENROLLMENT_STATUS.equals(viewType)) {
			filename.append(enrollmentSetId);
			filename.append(FILENAME_SEPARATOR);
			filename.append(enrollmentStatus);
		}

		Date date = new Date();
		// ISO formatted date
		DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");

		filename.append(FILENAME_SEPARATOR);
		filename.append(isoFormat.format(date));

		filename = new StringBuffer(filename.toString().replaceAll("\\W", FILENAME_SEPARATOR));
		filename.append(FILE_EXTENSION);

		return filename.toString();
	}
	
	private void export(String currentUserId, HttpServletResponse response, RosterSite site, Map<String, Object> parameters) throws IOException {

		// TODO one generic method could handle the parameters?
		String groupId = getGroupIdValue(parameters);
		String viewType = getViewTypeValue(parameters);
		boolean byGroup = getByGroupValue(parameters);
		String roleId = getRoleIdValue(parameters);

		String enrollmentSetId = getEnrollmentSetIdValue(parameters);
		String enrollmentStatus = getEnrollmentStatusValue(parameters);

		String enrollmentSetTitle = null;
		if (null != enrollmentSetId) {
			for (RosterEnrollment enrollmentSet : site.getSiteEnrollmentSets()) {
				if (enrollmentSetId.equals(enrollmentSet.getId())) {
					enrollmentSetTitle = enrollmentSet.getTitle();
					break;
				}
			}
		}

		addResponseHeader(response, createFilename(site, groupId, viewType,
				byGroup, enrollmentSetTitle, enrollmentStatus));

		List<List<String>> dataInRows = new ArrayList<>();

		createSpreadsheetTitle(dataInRows, site, groupId, viewType,
				enrollmentSetTitle);

		List<String> header = createColumnHeader(viewType, site.getId());

		if (VIEW_OVERVIEW.equals(viewType)) {

			List<RosterMember> rosterMembers = getMembership(currentUserId, site.getId(), groupId,roleId);

			if (null != rosterMembers) {
				addOverviewRows(dataInRows, rosterMembers, header, site.getId());
			}
		} else if (VIEW_ENROLLMENT_STATUS.equals(viewType)) {

			List<RosterMember> rosterMembers = getEnrolledMembership(currentUserId, site.getId(), enrollmentSetId, enrollmentStatus);

			if (null != rosterMembers) {
				addEnrollmentStatusRows(dataInRows, rosterMembers, header,
						enrollmentSetTitle, enrollmentStatus, site.getId());
			}
		}

		Workbook workBook = new XSSFWorkbook();
		Sheet sheet = workBook.createSheet();

		for (int i = 0; i < dataInRows.size(); i++) {
			Row row = sheet.createRow(i);
			for (int j = 0; j < dataInRows.get(i).size(); j++) {
				Cell cell = row.createCell(j);
				cell.setCellValue(dataInRows.get(i).get(j));
			}
		}

		workBook.write(response.getOutputStream());
		response.getOutputStream().close();
	}

	private List<RosterMember> getMembership(String userId, String siteId, String groupId,String roleId) {
		
		List<RosterMember> rosterMembers;
		
		if (DEFAULT_GROUP_ID.equals(groupId)) {
			rosterMembers = sakaiProxy.getMembership(userId, siteId, null, roleId, null, null);
		} else {
			rosterMembers = sakaiProxy.getMembership(userId, siteId, groupId, roleId, null, null);
		}
		
		if (null == rosterMembers) {
			return null;
		}

		return rosterMembers;
	}
	
	private List<RosterMember> getEnrolledMembership(String currentUserId, String siteId, String enrollmentSetId, String enrollmentStatusId) {

		List<RosterMember> rosterMembers = sakaiProxy.getMembership(currentUserId, siteId, null, null, enrollmentSetId, enrollmentStatusId);
		
		List<RosterMember> membersByStatus = null;
		if (DEFAULT_ENROLLMENT_STATUS.equals(enrollmentStatusId)) {
			membersByStatus = rosterMembers;
		} else {
			membersByStatus = new ArrayList<>();
			for (RosterMember rosterMember : rosterMembers) {
				if (enrollmentStatusId.equals(rosterMember.getEnrollmentStatusId())) {
					membersByStatus.add(rosterMember);
				}
			}
		}
		
		return membersByStatus;
	}

	private void addOverviewRows(List<List<String>> dataInRows,
			List<RosterMember> rosterMembers, List<String> header, String siteId) {

		String userId = developerHelperService.getCurrentUserId();

		dataInRows.add(header);
		// blank line
		dataInRows.add(new ArrayList<String>());

		for (RosterMember member : rosterMembers) {

			List<String> row = new ArrayList<String>();

			if (sakaiProxy.getFirstNameLastName()) {
				row.add(member.getDisplayName());
			} else {
				row.add(member.getSortName());
			}

			if (sakaiProxy.getViewUserDisplayId()) {
				row.add(member.getDisplayId());
			}

			if (sakaiProxy.getViewEmail(siteId)) {
				row.add(member.getEmail());
			}

			row.add(member.getRole());

			if (sakaiProxy.hasUserSitePermission(userId, RosterFunctions.ROSTER_FUNCTION_VIEWGROUP, siteId)) {
				row.add(member.getGroups().values().stream().collect(Collectors.joining(", ")));
			}

			dataInRows.add(row);
		}
	}

	private void addGroupMembershipUngroupedRows(List<List<String>> dataInRows,
			List<RosterMember> rosterMembers, List<String> header) {
		
		dataInRows.add(header);
		// blank line
		dataInRows.add(new ArrayList<String>());
		
		for (RosterMember member : rosterMembers) {

			List<String> row = new ArrayList<String>();

			if (sakaiProxy.getFirstNameLastName()) {
				row.add(member.getDisplayName());
			} else {
				row.add(member.getSortName());
			}
			
			if (sakaiProxy.getViewUserDisplayId()) {
				row.add(member.getDisplayId());
			}
			
			row.add(member.getRole());
			row.add(member.getGroupsToString());
			
			dataInRows.add(row);
		}
	}

	private void addGroupMembershipByGroupRows(List<List<String>> dataInRows,
			List<RosterMember> rosterMembers, RosterSite site, List<String> header) {

		for (RosterGroup group : site.getSiteGroups()) {
			List<String> groupTitle = new ArrayList<String>();
			groupTitle.add(group.getTitle());

			dataInRows.add(groupTitle);
			// blank line
			dataInRows.add(new ArrayList<String>());

			dataInRows.add(header);
			// blank line
			dataInRows.add(new ArrayList<String>());

			for (RosterMember member : rosterMembers) {

				if (null != member.getGroups().get(group.getId())) {

					List<String> row = new ArrayList<String>();

					if (sakaiProxy.getFirstNameLastName()) {
						row.add(member.getDisplayName());
					} else {
						row.add(member.getSortName());
					}
					
					if (sakaiProxy.getViewUserDisplayId()) {
						row.add(member.getDisplayId());
					}
					
					row.add(member.getRole());
					row.add(member.getGroupsToString());
					dataInRows.add(row);
				}
			}

			// blank line
			dataInRows.add(new ArrayList<String>());
		}
	}
	
	private void addEnrollmentStatusRows(List<List<String>> dataInRows,
			List<RosterMember> enrollmentSet, /* RosterSite site, */
			List<String> header, String enrollmentSetTitle,
			String enrollmentStatus, String siteId) {

		String userId = developerHelperService.getCurrentUserId();

		List<String> enrollmentSetTitleRow = new ArrayList<>();
		enrollmentSetTitleRow.add(enrollmentSetTitle);
		dataInRows.add(enrollmentSetTitleRow);

		// blank line
		dataInRows.add(new ArrayList<>());

		List<String> enrollmentStatusRow = new ArrayList<>();
		enrollmentStatusRow.add(enrollmentStatus);
		dataInRows.add(enrollmentStatusRow);

		// blank line
		dataInRows.add(new ArrayList<>());

		dataInRows.add(header);
		
		// blank line
		dataInRows.add(new ArrayList<>());

		for (RosterMember member : enrollmentSet) {

			List<String> row = new ArrayList<>();

			if (sakaiProxy.getFirstNameLastName()) {
				row.add(member.getDisplayName());
			} else {
				row.add(member.getSortName());
			}
			
			if (sakaiProxy.getViewUserDisplayId()) {
				row.add(member.getDisplayId());
			}

			if (sakaiProxy.getViewEmail(siteId)) {
				row.add(member.getEmail());
			}
			
			row.add(member.getEnrollmentStatusText());
			row.add(member.getCredits());

			if (sakaiProxy.hasUserSitePermission(userId, RosterFunctions.ROSTER_FUNCTION_VIEWGROUP, siteId)) {
				row.add(member.getGroups().values().stream().collect(Collectors.joining(", ")));
			}
			
			dataInRows.add(row);
		}
	}

	private String getEnrollmentSetIdValue(Map<String, Object> parameters) {
		String enrollmentSetId = null;
		if (null != parameters.get(KEY_ENROLLMENT_SET_ID)) {
			enrollmentSetId = parameters.get(KEY_ENROLLMENT_SET_ID).toString();
		}
		return enrollmentSetId;
	}
	
	private String getEnrollmentStatusValue(Map<String, Object> parameters) {
		String enrollmentStatus = null;
		if (null != parameters.get(KEY_ENROLLMENT_STATUS)) {
			enrollmentStatus = parameters.get(KEY_ENROLLMENT_STATUS).toString().toLowerCase();
		}
		return enrollmentStatus;
	}

	private boolean getByGroupValue(Map<String, Object> parameters) {

		if (null != parameters.get(KEY_BY_GROUP)) {
			return Boolean
					.parseBoolean(parameters.get(KEY_BY_GROUP).toString());
		} else {
			return DEFAULT_BY_GROUP;
		}
	}

	private String getViewTypeValue(Map<String, Object> parameters) {
		
		if (null != parameters.get(KEY_VIEW_TYPE)) {
			return parameters.get(KEY_VIEW_TYPE).toString();
		} else {
			return DEFAULT_VIEW_TYPE;
		}
	}

	private String getGroupIdValue(Map<String, Object> parameters) {
		
		if (null != parameters.get(KEY_GROUP_ID)) {
			return parameters.get(KEY_GROUP_ID).toString();
		}
		return null;
	}
	
	private String getRoleIdValue(Map<String, Object> parameters) {
		
		if (null != parameters.get(KEY_ROLE_ID)) {
			return parameters.get(KEY_ROLE_ID).toString();
		}
		return null;
	}

	private List<String> createColumnHeader(String viewType, String siteId) {

		String userId = developerHelperService.getCurrentUserId();

		ResourceLoader rl = new ResourceLoader("org.sakaiproject.roster.i18n.ui");

		List<String> header = new ArrayList<>();
		header.add(rl.getString("facet_name"));

		if (sakaiProxy.getViewUserDisplayId()) {
		    header.add(rl.getString("facet_userId"));
		}
		if (sakaiProxy.getViewEmail(siteId)) {
			header.add(rl.getString("facet_email"));
		}

		if (VIEW_OVERVIEW.equals(viewType)) {
			header.add(rl.getString("facet_role"));
		} else if (VIEW_ENROLLMENT_STATUS.equals(viewType)) {
			header.add(rl.getString("facet_status"));
			header.add(rl.getString("facet_credits"));
		}
		if (sakaiProxy.hasUserSitePermission(userId, RosterFunctions.ROSTER_FUNCTION_VIEWGROUP, siteId)) {
			header.add(rl.getString("facet_groups"));
		}

		return header;
	}

	private void createSpreadsheetTitle(List<List<String>> dataInRows,
			RosterSite site, String groupId, String viewType, String enrollmentSet) {

		List<String> title = new ArrayList<>();
		title.add(site.getTitle());
		dataInRows.add(title);
		// blank line
		dataInRows.add(new ArrayList<>());

		// SAK-18513
		if (VIEW_OVERVIEW.equals(viewType)) {
			if (null != groupId && !DEFAULT_GROUP_ID.equals(groupId)) {

				// TODO look at using maps in RosterSite instead
				for (RosterGroup group : site.getSiteGroups()) {
					
					if (group.getId().equals(groupId)) {
						List<String> groupTitle = new ArrayList<>();
						groupTitle.add(group.getTitle());
						dataInRows.add(groupTitle);
						// blank line
						dataInRows.add(new ArrayList<>());
						
						break;
					}
				}
			}
		}
	}
}
