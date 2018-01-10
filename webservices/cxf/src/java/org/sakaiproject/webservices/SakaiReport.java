/**
 * Copyright (c) 2005 The Apereo Foundation
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
package org.sakaiproject.webservices;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.xml.serializer.utils.XMLChar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jumpmind.symmetric.csv.CsvWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.util.Xml;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 1/23/12
 * Time: 10:34 AM
 * To change this template use File | Settings | File Templates.
 */

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
@Slf4j
public class SakaiReport extends AbstractWebService {

    private SqlService sqlService;

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static final int MAX_ROWS = 200;

    private static final int NVARCHAR = -9;
    private static final int NCHAR = -15;
    private static final int LONGNVARCHAR = -16;
    private static final int NCLOB = 2011;
    public static final int CLOBBUFFERSIZE = 2048;

    static final String TYPE_CSV = "csv";
    static final String TYPE_XML = "xml";
    static final String TYPE_JSON = "json";
    static final String TYPE_CSV_WITH_HEADER_ROW = "csv_with_header_row";


    @WebMethod
    @Path("/executeQuery")
    @Produces("application/xml")
    @GET
    public String executeQuery(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "query", partName = "query") @QueryParam("query") String query,
            @WebParam(name = "hash", partName = "hash") @QueryParam("hash") String hash) {
        return executeQueryInternal(sessionid, query, hash, MAX_ROWS, TYPE_XML);
    }


    @WebMethod
    @Path("/executeQuery2")
    @Produces("text/plain")
    @GET
    public String executeQuery2(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "query", partName = "query") @QueryParam("query") String query,
            @WebParam(name = "hash", partName = "hash") @QueryParam("hash") String hash,
            @WebParam(name = "rowCount", partName = "rowCount") @QueryParam("rowCount") int rowCount,
            @WebParam(name = "format", partName = "format") @QueryParam("format") String format) {
        return executeQueryInternal(sessionid, query, hash, rowCount, convertFormatToEnum(format));
    }

    @WebMethod(exclude = true)
    @Path("/executeQueryWithFormat")
    @GET
    public Response executeQueryWithFormat(@QueryParam("sessionid") String sessionid,
                                           @QueryParam("query") String query,
                                           @QueryParam("hash") String hash,
                                           @QueryParam("format") String format) {
        String responseData = executeQueryInternal(sessionid, query, hash, MAX_ROWS, convertFormatToEnum(format));
        String contentType = MediaType.APPLICATION_XML;
        if (format.toLowerCase().startsWith("json")) {
            contentType = MediaType.APPLICATION_JSON;
        }
        if (format.toLowerCase().startsWith("csv")) {
            contentType = "text/csv";
        }

        javax.ws.rs.core.Response.ResponseBuilder rBuild = Response.ok(responseData, contentType);
        return rBuild.build();
    }

    @WebMethod
    @Path("/executeQuery3")
    @Produces("text/plain")
    @GET
    public String executeQuery3(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "query", partName = "query") @QueryParam("query") String query,
            @WebParam(name = "hash", partName = "hash") @QueryParam("hash") String hash,
            @WebParam(name = "format", partName = "format") @QueryParam("format") String format) {
        return executeQueryInternal(sessionid, query, hash, MAX_ROWS, convertFormatToEnum(format));
    }


    @WebMethod
    @Path("/executeQuery4")
    @Produces("text/plain")
    @GET
    public String executeQuery4(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "query", partName = "query") @QueryParam("query") String query,
            @WebParam(name = "hash", partName = "hash") @QueryParam("hash") String hash,
            @WebParam(name = "rowCount", partName = "rowCount") @QueryParam("rowCount") int rowCount) {
        return executeQueryInternal(sessionid, query, hash, rowCount, TYPE_XML);

    }

    protected String executeQueryInternal(String sessionid, String query, String hash, int rowCount, String format) {
        Session session = establishSession(sessionid);

        boolean isEnabled = serverConfigurationService.getBoolean("webservice.report.enabled", false);
        if (isEnabled == false) {
            log.warn("Report service not enabled, use webservice.report.enabled=true to enable");
            throw new RuntimeException("Report service not enabled.");
        }
        if (session == null) {
            log.warn("No session for: " + sessionid);
            throw new RuntimeException("No session for " + sessionid);
        }
        
        if (!securityService.isSuperUser()) {
            log.warn("Non super user attempted access to report service: " + session.getUserId());
            throw new RuntimeException("Non super user attempted to access report service: " + session.getUserId());
        }

        // validate hash
        if (hash == null || !validateHash(sessionid, query, hash)) {
            throw new RuntimeException("hash value does not match, ignoring request");
        }

        return getQueryAsString(query, new String[0], rowCount, format);
    }

    protected String convertFormatToEnum(String format) {
        if (format.equalsIgnoreCase("csv")) {
            return TYPE_CSV;
        }

        if (format.equalsIgnoreCase("csv_with_header_row")) {
            return TYPE_CSV_WITH_HEADER_ROW;
        }
        if (format.equalsIgnoreCase("json")) {
            return TYPE_JSON;
        }

        return TYPE_XML;
    }

    protected boolean validateHash(String sessionid, String query, String hash) {

        // TODO add in shared secret to make this safer
        String calculatedHash = DigestUtils.sha256Hex(sessionid + query);
        log.info("received hash of: " + hash + " calculated hash value as: " + calculatedHash);
        return hash.equals(calculatedHash);

    }

    protected String toCsvString(ResultSet rs) throws IOException, SQLException {
        return toCsvString(rs, false);
    }

    protected String toCsvString(ResultSet rs, boolean includeHeaderRow) throws IOException, SQLException {
        StringWriter stringWriter = new StringWriter();
        CsvWriter writer = new CsvWriter(stringWriter, ',');
        writer.setRecordDelimiter('\n');
        writer.setForceQualifier(true);
        ResultSetMetaData rsmd = rs.getMetaData();
        int numColumns = rsmd.getColumnCount();

        if (includeHeaderRow) {
            String[] row = new String[numColumns];
            for (int i = 1; i < numColumns + 1; i++) {
                row[i - 1] = rsmd.getColumnLabel(i);
            }
            writer.writeRecord(row);
        }

        while (rs.next()) {
            String[] row = new String[numColumns];
            for (int i = 1; i < numColumns + 1; i++) {

                String column_name = rsmd.getColumnName(i);

                log.debug("Column Name=" + column_name + ",type=" + rsmd.getColumnType(i));

                switch (rsmd.getColumnType(i)) {
                    case Types.BIGINT:
                        row[i - 1] = String.valueOf(rs.getInt(i));
                        break;
                    case Types.BOOLEAN:
                        row[i - 1] = String.valueOf(rs.getBoolean(i));
                        break;
                    case Types.BLOB:
                        row[i - 1] = rs.getBlob(i).toString();
                        break;
                    case Types.DOUBLE:
                        row[i - 1] = String.valueOf(rs.getDouble(i));
                        break;
                    case Types.FLOAT:
                        row[i - 1] = String.valueOf(rs.getFloat(i));
                        break;
                    case Types.INTEGER:
                        row[i - 1] = String.valueOf(rs.getInt(i));
                        break;
                    case Types.LONGVARCHAR:
                        row[i - 1] = rs.getString(i);
                        break;
                    case Types.NVARCHAR:
                        row[i - 1] = rs.getNString(i);
                        break;
                    case Types.VARCHAR:
                        row[i - 1] = rs.getString(i);
                        break;
                    case Types.TINYINT:
                        row[i - 1] = String.valueOf(rs.getInt(i));
                        break;
                    case Types.SMALLINT:
                        row[i - 1] = String.valueOf(rs.getInt(i));
                        break;
                    case Types.DATE:
                        row[i - 1] = rs.getDate(i).toString();
                        break;
                    case Types.TIMESTAMP:
                        row[i - 1] = rs.getTimestamp(i).toString();
                        break;
                    default:
                        row[i - 1] = rs.getString(i);
                        break;

                }
                log.debug("value: " + row[i - 1]);
            }
            writer.writeRecord(row);
            //writer.endRecord();

        }

        log.debug("csv output:" + stringWriter.toString());

        return stringWriter.toString();
    }

    protected String stripInvalidXmlCharacters(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (XMLChar.isValid(c)) {
                sb.append(c);
            } else {
                log.debug(c + " is not a valid XML char, stripping it: ");
            }
        }

        return sb.toString();
    }

    protected String toJsonString(ResultSet rs) throws SQLException, JSONException {
        ResultSetMetaData rsmd = rs.getMetaData();
        JSONArray array = new JSONArray();
        int numColumns = rsmd.getColumnCount();

        while (rs.next()) {

            JSONObject obj = new JSONObject();
            for (int i = 1; i < numColumns + 1; i++) {

                String column_label = rsmd.getColumnLabel(i);

                log.debug("Column Name=" + column_label + ",type=" + rsmd.getColumnType(i));

                switch (rsmd.getColumnType(i)) {
                    case Types.ARRAY:
                        obj.put(column_label, rs.getArray(i));
                        break;
                    case Types.BIGINT:
                        obj.put(column_label, rs.getInt(i));
                        break;
                    case Types.BOOLEAN:
                        obj.put(column_label, rs.getBoolean(i));
                        break;
                    case Types.BLOB:
                        obj.put(column_label, rs.getBlob(i));
                        break;
                    case Types.DOUBLE:
                        obj.put(column_label, rs.getDouble(i));
                        break;
                    case Types.FLOAT:
                        obj.put(column_label, rs.getFloat(i));
                        break;
                    case Types.INTEGER:
                        obj.put(column_label, rs.getInt(i));
                        break;
                    case Types.NVARCHAR:
                        obj.put(column_label, rs.getNString(i));
                        break;
                    case Types.VARCHAR:
                        obj.put(column_label, rs.getString(i));
                        break;
                    case Types.TINYINT:
                        obj.put(column_label, rs.getInt(i));
                        break;
                    case Types.SMALLINT:
                        obj.put(column_label, rs.getInt(i));
                        break;
                    case Types.DATE:
                        obj.put(column_label, rs.getDate(i));
                        break;
                    case Types.TIMESTAMP:
                        obj.put(column_label, rs.getTimestamp(i));
                        break;
                    default:
                        obj.put(column_label, rs.getObject(i));
                        break;
                }

            }
            array.put(obj);

        }
        return array.toString();
    }

    protected Document toDocument(ResultSet rs)
            throws ParserConfigurationException, SQLException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element results = doc.createElement("Results");
        doc.appendChild(results);

        ResultSetMetaData rsmd = rs.getMetaData();
        int colCount = rsmd.getColumnCount();

        while (rs.next()) {
            Element row = doc.createElement("Row");
            results.appendChild(row);

            for (int i = 1; i <= colCount; i++) {
                String columnName = rsmd.getColumnLabel(i);
                Object value = null;
                try {
                    value = getColumnValue(rs, rsmd.getColumnType(i), i);
                    Element node = doc.createElement(columnName);
                    node.appendChild(doc.createTextNode(stripInvalidXmlCharacters(value.toString())));
                    row.appendChild(node);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }


            }
        }
        return doc;
    }

    private String getColumnValue(ResultSet rs, int colType, int colIndex)
            throws SQLException, IOException {
        String value = "";
        switch (colType) {
            case Types.BIT:
            case Types.JAVA_OBJECT:
                value = handleObject(rs.getObject(colIndex));
                break;
            case Types.BOOLEAN:
                boolean b = rs.getBoolean(colIndex);
                value = Boolean.valueOf(b).toString();
                break;
            case NCLOB: // todo : use rs.getNClob
            case Types.CLOB:
                Clob c = rs.getClob(colIndex);
                if (c != null) {
                    value = read(c);
                }
                break;
            case Types.BIGINT:
                value = handleLong(rs, colIndex);
                break;
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
            case Types.NUMERIC:
                value = handleBigDecimal(rs.getBigDecimal(colIndex));
                break;
            case Types.INTEGER:
            case Types.TINYINT:
            case Types.SMALLINT:
                value = handleInteger(rs, colIndex);
                break;
            case Types.DATE:
                value = handleDate(rs, colIndex);
                break;
            case Types.TIME:
                value = handleTime(rs.getTime(colIndex));
                break;
            case Types.TIMESTAMP:
                value = handleTimestamp(rs.getTimestamp(colIndex));
                break;
            case NVARCHAR: // todo : use rs.getNString
            case NCHAR: // todo : use rs.getNString
            case LONGNVARCHAR: // todo : use rs.getNString
            case Types.LONGVARCHAR:
            case Types.VARCHAR:
            case Types.CHAR:
                value = rs.getString(colIndex);
                break;
            case Types.VARBINARY:
            case Types.BINARY:
                value = handleRaw(rs.getBytes(colIndex));
                break;
            default:
                value = "";
        }


        if (value == null) {
            value = "";
        }

        return value;

    }

    private String handleObject(Object obj) {
        return obj == null ? "" : String.valueOf(obj);
    }

    private String handleBigDecimal(BigDecimal decimal) {
        return decimal == null ? "" : decimal.toString();
    }

    private String handleLong(ResultSet rs, int columnIndex) throws SQLException {
        long lv = rs.getLong(columnIndex);
        return rs.wasNull() ? "" : Long.toString(lv);
    }

    private String handleInteger(ResultSet rs, int columnIndex) throws SQLException {
        int i = rs.getInt(columnIndex);
        return rs.wasNull() ? "" : Integer.toString(i);
    }

    private String handleDate(ResultSet rs, int columnIndex) throws SQLException {
        Date date = rs.getDate(columnIndex);
        String value = null;
        if (date != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
            value = dateFormat.format(date);
        }
        return value;
    }

    private String handleTime(Time time) {
        return time == null ? null : time.toString();
    }

    private String handleTimestamp(Timestamp timestamp) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        return timestamp == null ? null : timeFormat.format(timestamp);
    }

    private String handleRaw(byte[] bytes) {
        String result = "";
        if (bytes == null) return result;
        for (int i = 0; i < bytes.length; i++) {
            result +=
                    Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    private static String read(Clob c) throws SQLException, IOException {
        StringBuilder sb = new StringBuilder((int) c.length());
        Reader r = c.getCharacterStream();
        char[] cbuf = new char[CLOBBUFFERSIZE];
        int n;
        while ((n = r.read(cbuf, 0, cbuf.length)) != -1) {
            sb.append(cbuf, 0, n);
        }
        return sb.toString();
    }

    protected String getQueryAsString(String query, Object[] args, int rowCount, String type) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = sqlService.borrowConnection();
            conn.setReadOnly(true);

            ps = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            if (rowCount > 0) {
                ps.setMaxRows(rowCount);
            }

            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof String) {
                    ps.setString(i + 1, (String) args[i]);
                } else if (args[i] instanceof java.util.Date) {
                    // select * from sakai_event where event_date between to_date('2001-12-12 12:12','YYYY-MM-DD HH24:MI') and to_date('2017-12-12 12:12','YYYY-MM-DD HH24:MI')
                    if (sqlService.getVendor().equals("oracle")) {
                        ps.setString(i + 1, df.format(args[i]));
                        // select * from sakai_event where event_date between '2001-12-12 12:12' and '2017-12-12 12:12';
                    } else {
                        ps.setString(i + 1, df.format(args[i]));
                    }
                }
            }
            log.info("preparing query: " + ps.toString());

            rs = ps.executeQuery();
            //return toJsonString(rs);
            if (type == TYPE_CSV) {
                return stripInvalidXmlCharacters(toCsvString(rs));
            }
            if (type == TYPE_CSV_WITH_HEADER_ROW) {
                return stripInvalidXmlCharacters(toCsvString(rs, true));
            }

            if (type == TYPE_JSON) {
                return stripInvalidXmlCharacters(toJsonString(rs));
            }

            return Xml.writeDocumentToString(toDocument(rs));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
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
    }


    @WebMethod(exclude = true)
    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }
}
