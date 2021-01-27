package org.sakaiproject.webservices;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.tool.api.Session;

public class SakaiReportCSVTest extends AbstractCXFTest {

    public static final String SESSION_ID = "***SESSION_HAS_BEEN_MOCKERIZED***";
    private static final String SOAP_OPERATION = "executeQueryWithFormat";
    private static final String QUERY = "SELECT * FROM SAKAI_USER_ID_MAP";

    @Override
    protected <T extends AbstractWebService> Class<T> getTestClass() {
        return (Class<T>) SakaiReport.class;
    }

    @Override
    protected String getOperation() {
        return SOAP_OPERATION;
    }

    @Override
    protected void addServiceMocks(AbstractWebService service) {
        Session mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn(SESSION_ID);
        when(service.sessionManager.getSession(SESSION_ID)).thenReturn(mockSession);

        when(service.securityService.isSuperUser()).thenReturn(true);
        when(service.serverConfigurationService.getBoolean("webservice.report.enabled", false)).thenReturn(true);

        try {
            Connection connection = mock(Connection.class);
            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            ResultSet resultSet = mock(ResultSet.class);
            ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
            when(connection.prepareStatement(QUERY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
            when(resultSetMetaData.getColumnCount()).thenReturn(2);
            when(resultSetMetaData.getColumnName(1)).thenReturn("USER_ID");
            when(resultSetMetaData.getColumnName(2)).thenReturn("EID");
            when(resultSetMetaData.getColumnType(anyInt())).thenReturn(Types.VARCHAR);
            when(resultSet.next()).thenReturn(true).thenReturn(false);
            when(resultSet.getString(1)).thenReturn("7a2ab51e-0fb5-425a-9886-c53d3e8a53a0");
            when(resultSet.getString(2)).thenReturn("000313486");
            when(service.sqlService.borrowConnection()).thenReturn(connection);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Exception while setting up mocks: " + e.toString());
        }
    }

    @Test
    public void executeQueryWithCSVFormat() {
        WebClient client = WebClient.create(getFullEndpointAddress());

        addCXFClientMocks(client);

        // client call
        client.accept("text/plain");
        client.path("/" + getOperation());
        client.query("sessionid", SESSION_ID);
        client.query("query", QUERY);
        client.query("hash", getHash(SESSION_ID, QUERY));
        client.query("format", "csv");

        // client result
        String result = client.get(String.class);

        // test verifications
        assertNotNull(result);
        assertEquals("\"7a2ab51e-0fb5-425a-9886-c53d3e8a53a0\",\"000313486\"\n", result);
    }

    private String getHash(String sessionid, String query) {
        return DigestUtils.sha256Hex(sessionid + query);
    }
}
