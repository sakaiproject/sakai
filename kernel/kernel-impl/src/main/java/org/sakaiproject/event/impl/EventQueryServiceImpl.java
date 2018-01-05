/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.event.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.sakaiproject.event.api.EventQueryService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.util.Xml;

/**
 * Implementation of EventQueryService
 * It returns xml event lists for a given user eid.
 */
@Slf4j
public class EventQueryServiceImpl implements EventQueryService {
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


	/**
	 * Returns a list of events of a user between 2 dates.
	 * @param eid	is the user that we want to query
	 * @param startDate limit the query ti these dates
	 * @param endDate limit the query ti these dates
	 * @return	String as the result of the Query in xml
	 */
	public String getUserActivity(String eid, Date startDate, Date endDate) {
		String query = "select se.EVENT_ID, se.EVENT_DATE, se.EVENT, se.REF, se.CONTEXT, se.EVENT_CODE, um.USER_ID, um.EID from " +
				"SAKAI_EVENT se, SAKAI_SESSION ss, SAKAI_USER_ID_MAP um " +
				"where se.SESSION_ID = ss.SESSION_ID and um.EID = ? and ";

		if (sqlService.getVendor().equals("oracle"))
		{
			query += "EVENT_DATE BETWEEN to_date(?, 'YYYY-MM-DD HH24:MI') AND to_date(?, 'YYYY-MM-DD HH24:MI') ";
		}
		else
		{
			query += "EVENT_DATE BETWEEN ? AND ?";
		}

		query += " and um.USER_ID = ss.SESSION_USER order by se.EVENT_DATE desc";
		return queryEventTableWithEid(query, new Object[]{eid, startDate, endDate});
	}

	/**
	 * Returns a list of events of a user between 2 dates.
	 * @param eid	is the user that we want to query
	 * @param startDateString limit the query ti these dates. In this case as String if we call it as a rest
	 * @param endDateString limit the query ti these dates. In this case as String if we call it as a rest
	 * @return	String as the result of the Query in xml
	 */
	public String getUserActivityRestVersion(String eid, String startDateString, String endDateString) {
		Date startDate;
		Date endDate;

		try
		{
			startDate = getDateFromString(startDateString);
			endDate = getDateFromString(endDateString);
		}
		catch (ParseException e)
		{
			return "Activity Webservices exception : " + e.getMessage();
		}

		String query = "select se.EVENT_ID, se.EVENT_DATE, se.EVENT, se.REF, se.CONTEXT, se.EVENT_CODE, um.USER_ID, um.EID from " +
				"SAKAI_EVENT se, SAKAI_SESSION ss, SAKAI_USER_ID_MAP um " +
				"where se.SESSION_ID = ss.SESSION_ID and um.EID = ? and ";

		if (sqlService.getVendor().equals("oracle"))
		{
			query += "EVENT_DATE BETWEEN to_date(?, 'YYYY-MM-DD HH24:MI') AND to_date(?, 'YYYY-MM-DD HH24:MI') ";
		}
		else
		{
			query += "EVENT_DATE BETWEEN ? AND ?";
		}

		query += " and um.USER_ID = ss.SESSION_USER order by se.EVENT_DATE desc";
		return queryEventTableWithEid(query, new Object[]{eid, startDate, endDate});
	}

	/**
	 * Returns the User's logon activity.
	 * @param eid	is the user that we want to query
	 * @return	String as the result of the Query in xml
	 */
	public String getUserLogonActivity(String eid) {
		String query = ("select se.EVENT_ID, se.EVENT_DATE, se.EVENT, se.REF, se.CONTEXT, se.EVENT_CODE, um.USER_ID, um.EID from " +
				"SAKAI_EVENT se, SAKAI_SESSION ss, SAKAI_USER_ID_MAP um " +
				"where se.SESSION_ID = ss.SESSION_ID and um.EID = ? and " +
				"um.USER_ID = ss.SESSION_USER and se.EVENT = 'user.login' order by se.EVENT_DATE desc");

		return queryEventTableWithEid(query, new Object[]{eid});
	}

	/**
	 * Returns the User's activity filtered by one event type.
	 * @param eid	is the user that we want to query
	 * @param eventType the event type to filter
	 * @return	String as the result of the Query in xml
	 */
	public String getUserActivityByType(String eid, String eventType) {
		String query = ("select se.EVENT_ID, se.EVENT_DATE, se.EVENT, se.REF, se.CONTEXT, se.EVENT_CODE, um.USER_ID, um.EID from " +
				"SAKAI_EVENT se, SAKAI_SESSION ss, SAKAI_USER_ID_MAP um " +
				"where se.SESSION_ID = ss.SESSION_ID and um.EID = ? and " +
				"um.USER_ID = ss.SESSION_USER and se.EVENT = ? order by se.EVENT_DATE desc");

		return queryEventTableWithEid(query, new Object[]{eid, eventType});
	}


	/**
	 * Parses the string to create a date object
	 */
	private Date getDateFromString(String dateString) throws ParseException {

		try {
			Date date = df.parse(dateString);
			return date;
		} catch (ParseException e) {
			log.warn("Date format should be yyyy-MM-dd HH:mm:ss");
			throw new ParseException("Date format should be yyyy-MM-dd HH:mm:ss",e.getErrorOffset());
		}

	}


	/**
	 * Executes the query
	 */
	private String queryEventTableWithEid(String query, Object[] args) {

		Document doc = Xml.createDocument();
		Node results = doc.createElement("events");
		doc.appendChild(results);
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean returnErrorMessage = false;
		String errorMessage = "";
		try {
			conn = sqlService.borrowConnection();
			ps = conn.prepareStatement(query);
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof String) {
					ps.setString(i + 1, (String) args[i]);
				}
				else if (args[i] instanceof Date) {
					ps.setString(i + 1, df.format(args[i]));
					long date = ((Date) args[i]).getTime();
				}
			}
			rs = ps.executeQuery();
			buildXmlFromResultSet(results, rs);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			returnErrorMessage = true;
			errorMessage= "Activity Webservices exception :" + e.getMessage();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {

				}
			}

			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {

				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {

				}
			}
			if (returnErrorMessage) return errorMessage;
		}

		return Xml.writeDocumentToString(doc);

	}

	/**
	 * Creates the xml result based in the SQL query
	 */
	private Document buildXmlFromResultSet(Node results, ResultSet rs) throws SQLException {

		Document doc = results.getOwnerDocument();
		while (rs.next()) {
			Node eventNode = doc.createElement("event");
			results.appendChild(eventNode);

			Node id = doc.createElement("event_id");
			id.appendChild(doc.createTextNode(rs.getString("EVENT_ID")));
			eventNode.appendChild(id);

			Node event_date = doc.createElement("event_date");
			event_date.appendChild(doc.createTextNode(df.format(rs.getTimestamp("EVENT_DATE"))));
			eventNode.appendChild(event_date);

			Node event = doc.createElement("event_type");
			event.appendChild(doc.createTextNode(rs.getString("EVENT")));
			eventNode.appendChild(event);

			Node ref = doc.createElement("ref");
			ref.appendChild(doc.createTextNode(rs.getString("REF")));
			eventNode.appendChild(ref);

			Node context = doc.createElement("context");
			context.appendChild(doc.createTextNode(rs.getString("CONTEXT")));
			eventNode.appendChild(context);

			Node event_code = doc.createElement("event_code");
			event_code.appendChild(doc.createTextNode(rs.getString("EVENT_CODE")));
			eventNode.appendChild(event_code);

			Node user_id = doc.createElement("user_id");
			user_id.appendChild(doc.createTextNode(rs.getString("USER_ID")));
			eventNode.appendChild(user_id);

			Node eidNode = doc.createElement("eid");
			eidNode.appendChild(doc.createTextNode(rs.getString("EID")));
			eventNode.appendChild(eidNode);


		}
		return doc;

	}




	/*************************************************************************************************************************************************
	 * Dependencies
	 ************************************************************************************************************************************************/

	/** Dependency: SqlService. */

	protected SqlService sqlService;

	public void setSqlService(SqlService sqlService) {

		this.sqlService = sqlService;

	}


	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Init method.
	 */
	public void init() {

		log.info(this + ".init()");

	}

	/**
	 * Final cleanup.
	 */
	public void destroy() {

		log.info(this + ".destroy()");

	}

}
