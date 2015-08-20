package org.sakaiproject.webservices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 9/19/11
 * Time: 10:21 AM
 * To change this template use File | Settings | File Templates.
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
public class Activity extends AbstractWebService {
    private static final Log LOG = LogFactory.getLog(Activity.class);
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    
    @WebMethod
    @Path("/getUserActivity")
    @Produces("text/plain")
    @GET
    public String getUserActivity(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "startDate", partName = "startDate") @QueryParam("startDate") Date startDate,
            @WebParam(name = "endDate", partName = "endDate") @QueryParam("endDate") Date endDate) {
        String query = "select se.event_id, se.event_date, se.event, se.ref, se.context, se.event_code, um.user_id, um.eid from " +
                "sakai_event se, sakai_session ss, sakai_user_id_map um " +
                "where se.SESSION_ID = ss.SESSION_ID and um.eid = ? and ";

        if (sqlService.getVendor().equals("oracle")) {
            query += "EVENT_DATE BETWEEN to_date(?, 'YYYY-MM-DD HH24:MI') AND to_date(?, 'YYYY-MM-DD HH24:MI') ";
        } else {
            query += "EVENT_DATE BETWEEN ? AND ?";
        }
        query += " and um.user_id = ss.session_user order by se.event_date desc";

        return queryEventTableWithEid(sessionid, query, new Object[]{eid, startDate, endDate});
    }
    
    
    @WebMethod
    @Path("/getUserActivityStringDates")
    @Produces("text/plain")
    @GET
    public String getUserActivityStringDates(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "startDate", partName = "startDate") @QueryParam("startDate") String startDateString,
            @WebParam(name = "endDate", partName = "endDate") @QueryParam("endDate") String endDateString) {
        
        Date startDate = getDateFromString(startDateString);
        Date endDate = getDateFromString(endDateString);
        String query = "select se.event_id, se.event_date, se.event, se.ref, se.context, se.event_code, um.user_id, um.eid from " +
                "sakai_event se, sakai_session ss, sakai_user_id_map um " +
                "where se.SESSION_ID = ss.SESSION_ID and um.eid = ? and ";

        if (sqlService.getVendor().equals("oracle")) {
            query += "EVENT_DATE BETWEEN to_date(?, 'YYYY-MM-DD HH24:MI') AND to_date(?, 'YYYY-MM-DD HH24:MI') ";
        } else {
            query += "EVENT_DATE BETWEEN ? AND ?";
        }
        query += " and um.user_id = ss.session_user order by se.event_date desc";

        return queryEventTableWithEid(sessionid, query, new Object[]{eid, startDate, endDate});
    }

    @WebMethod
    @Path("/getUserLogonActivity")
    @Produces("text/plain")
    @GET
    public String getUserLogonActivity(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid) {
        String query = ("select se.event_id, se.event_date, se.event, se.ref, se.context, se.event_code, um.user_id, um.eid from " +
                "sakai_event se, sakai_session ss, sakai_user_id_map um " +
                "where se.SESSION_ID = ss.SESSION_ID and um.eid = ? and " +
                "um.user_id = ss.session_user and se.event = 'user.login' order by se.event_date desc");

        return queryEventTableWithEid(sessionid, query, new Object[]{eid});
    }


    @WebMethod
    @Path("/getUserActivityByType")
    @Produces("text/plain")
    @GET
    public String getUserActivityByType(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "eventType", partName = "eventType") @QueryParam("eventType") String eventType) {
        String query = ("select se.event_id, se.event_date, se.event, se.ref, se.context, se.event_code, um.user_id, um.eid from " +
                "sakai_event se, sakai_session ss, sakai_user_id_map um " +
                "where se.SESSION_ID = ss.SESSION_ID and um.eid = ? and " +
                "um.user_id = ss.session_user and se.event = ? order by se.event_date desc");

        return queryEventTableWithEid(sessionid, query, new Object[]{eid, eventType});
    }

    protected String queryEventTableWithEid(String sessionid, String query, Object[] args) {
        Session session = establishSession(sessionid);
        if (!securityService.isSuperUser()) {
            LOG.warn("NonSuperUser trying to collect activity stats: " + session.getUserId());
            throw new RuntimeException("NonSuperUser trying to collect activity stats: " + session.getUserId());
        }


        Document doc = Xml.createDocument();
        Node results = doc.createElement("events");
        doc.appendChild(results);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = sqlService.borrowConnection();

            ps = conn.prepareStatement(query);
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof String) {
                    ps.setString(i + 1, (String) args[i]);
                } else if (args[i] instanceof Date) {
                    ps.setString(i + 1, df.format(args[i]));
                    long date = ((Date) args[i]).getTime();
                }
            }
            rs = ps.executeQuery();

            buildXmlFromResultSet(results, rs);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
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
        }
        return Xml.writeDocumentToString(doc);

    }


    private Document buildXmlFromResultSet(Node results, ResultSet rs) throws SQLException {
        Document doc = results.getOwnerDocument();

        while (rs.next()) {
            Node eventNode = doc.createElement("event");
            results.appendChild(eventNode);

            Node id = doc.createElement("event_id");
            id.appendChild(doc.createTextNode(rs.getString("event_id")));
            eventNode.appendChild(id);

            Node event_date = doc.createElement("event_date");
            event_date.appendChild(doc.createTextNode(df.format(rs.getTimestamp("event_date"))));
            eventNode.appendChild(event_date);

            Node event = doc.createElement("event_type");
            event.appendChild(doc.createTextNode(rs.getString("event")));
            eventNode.appendChild(event);

            Node ref = doc.createElement("ref");
            ref.appendChild(doc.createTextNode(rs.getString("ref")));
            eventNode.appendChild(ref);

            Node context = doc.createElement("context");
            context.appendChild(doc.createTextNode(rs.getString("context")));
            eventNode.appendChild(context);

            Node event_code = doc.createElement("event_code");
            event_code.appendChild(doc.createTextNode(rs.getString("event_code")));
            eventNode.appendChild(event_code);

            Node user_id = doc.createElement("user_id");
            user_id.appendChild(doc.createTextNode(rs.getString("user_id")));
            eventNode.appendChild(user_id);

            Node eidNode = doc.createElement("eid");
            eidNode.appendChild(doc.createTextNode(rs.getString("eid")));
            eventNode.appendChild(eidNode);


        }
        return doc;
    }


    private Date getDateFromString(String dateString) {
        try {
            Date date = df.parse(dateString);
            return date;
        } catch (ParseException e) {
            LOG.warn("Date format should be yyyy-MM-dd HH:mm:ss");
            return null;
        }

    }
}
