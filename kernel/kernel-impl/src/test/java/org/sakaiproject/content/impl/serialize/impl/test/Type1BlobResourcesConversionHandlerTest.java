/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl.serialize.impl.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.content.impl.serialize.impl.Type1BaseContentResourceSerializer;
import org.sakaiproject.content.impl.serialize.impl.conversion.Type1BlobResourcesConversionHandler;

/**
 * Unit tests for Type1BlobResourcesConversionHandler to ensure SHA256 serialization and deserialization works correctly.
 * 
 * @author ieb
 */
public class Type1BlobResourcesConversionHandlerTest {

    private Type1BlobResourcesConversionHandler handler;
    private Type1BaseContentResourceSerializer serializer;

    @Before
    public void setUp() {
        handler = new Type1BlobResourcesConversionHandler();
        serializer = new Type1BaseContentResourceSerializer();
        serializer.setTimeService(MockTimeService.mockTimeMillisSince());
    }

    /**
     * Test that the conversion handler can properly extract content from different SQL types.
     * This test focuses on the getSource method which handles BLOB, CLOB, and VARCHAR types.
     */
    @Test
    public void testGetSourceWithDifferentSqlTypes() throws Exception {
        String testContent = "Test content for SHA256 verification";
        
        // Test with BLOB type
        ResultSet blobResultSet = createMockResultSet(testContent, Types.BLOB);
        Object blobSource = handler.getSource("blob-test", blobResultSet);
        assertEquals("BLOB source should match content", testContent, blobSource);
        
        // Test with CLOB type
        ResultSet clobResultSet = createMockResultSet(testContent, Types.CLOB);
        Object clobSource = handler.getSource("clob-test", clobResultSet);
        assertEquals("CLOB source should match content", testContent, clobSource);
        
        // Test with VARCHAR type
        ResultSet varcharResultSet = createMockResultSet(testContent, Types.VARCHAR);
        Object varcharSource = handler.getSource("varchar-test", varcharResultSet);
        assertEquals("VARCHAR source should match content", testContent, varcharSource);
    }

    /**
     * Test that the conversion handler properly handles null content from database.
     */
    @Test
    public void testNullContentHandling() throws Exception {
        // Test with null BLOB
        ResultSet nullBlobResultSet = createMockResultSet(null, Types.BLOB);
        Object nullBlobSource = handler.getSource("null-blob-test", nullBlobResultSet);
        assertNull("Null BLOB should return null", nullBlobSource);
        
        // Test with null CLOB
        ResultSet nullClobResultSet = createMockResultSet(null, Types.CLOB);
        Object nullClobSource = handler.getSource("null-clob-test", nullClobResultSet);
        assertNull("Null CLOB should return null", nullClobSource);
        
        // Test with null VARCHAR
        ResultSet nullVarcharResultSet = createMockResultSet(null, Types.VARCHAR);
        Object nullVarcharSource = handler.getSource("null-varchar-test", nullVarcharResultSet);
        assertNull("Null VARCHAR should return null", nullVarcharSource);
    }

    /**
     * Test that the conversion handler properly handles empty content from database.
     */
    @Test
    public void testEmptyContentHandling() throws Exception {
        String emptyContent = "";
        
        // Test with empty BLOB
        ResultSet emptyBlobResultSet = createMockResultSet(emptyContent, Types.BLOB);
        Object emptyBlobSource = handler.getSource("empty-blob-test", emptyBlobResultSet);
        assertEquals("Empty BLOB should return empty string", emptyContent, emptyBlobSource);
        
        // Test with empty CLOB
        ResultSet emptyClobResultSet = createMockResultSet(emptyContent, Types.CLOB);
        Object emptyClobSource = handler.getSource("empty-clob-test", emptyClobResultSet);
        assertEquals("Empty CLOB should return empty string", emptyContent, emptyClobSource);
        
        // Test with empty VARCHAR
        ResultSet emptyVarcharResultSet = createMockResultSet(emptyContent, Types.VARCHAR);
        Object emptyVarcharSource = handler.getSource("empty-varchar-test", emptyVarcharResultSet);
        assertEquals("Empty VARCHAR should return empty string", emptyContent, emptyVarcharSource);
    }

    /**
     * Test that the conversion handler can properly extract content that contains SHA256 data.
     * This test verifies that SHA256 content is correctly handled when stored in different SQL types.
     */
    @Test
    public void testSha256ContentExtraction() throws Exception {
        // Create test content that includes SHA256 information
        String testSha256 = "a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456";
        String testContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<resource>\n" +
                "  <id>/group/test-site/test-resource</id>\n" +
                "  <contentType>text/plain</contentType>\n" +
                "  <contentLength>42</contentLength>\n" +
                "  <resourceType>upload</resourceType>\n" +
                "  <properties>\n" +
                "    <property name=\"CHEF:contentsha256\">" + testSha256 + "</property>\n" +
                "  </properties>\n" +
                "</resource>";
        
        // Test that SHA256 content is properly extracted from BLOB
        ResultSet blobResultSet = createMockResultSet(testContent, Types.BLOB);
        Object blobSource = handler.getSource("sha256-blob-test", blobResultSet);
        assertNotNull("BLOB source should not be null", blobSource);
        assertEquals("BLOB source should contain SHA256 content", testContent, blobSource);
        assertTrue("BLOB source should contain SHA256 value", blobSource.toString().contains(testSha256));
        
        // Test that SHA256 content is properly extracted from CLOB
        ResultSet clobResultSet = createMockResultSet(testContent, Types.CLOB);
        Object clobSource = handler.getSource("sha256-clob-test", clobResultSet);
        assertNotNull("CLOB source should not be null", clobSource);
        assertEquals("CLOB source should contain SHA256 content", testContent, clobSource);
        assertTrue("CLOB source should contain SHA256 value", clobSource.toString().contains(testSha256));
        
        // Test that SHA256 content is properly extracted from VARCHAR
        ResultSet varcharResultSet = createMockResultSet(testContent, Types.VARCHAR);
        Object varcharSource = handler.getSource("sha256-varchar-test", varcharResultSet);
        assertNotNull("VARCHAR source should not be null", varcharSource);
        assertEquals("VARCHAR source should contain SHA256 content", testContent, varcharSource);
        assertTrue("VARCHAR source should contain SHA256 value", varcharSource.toString().contains(testSha256));
    }

    /**
     * Helper method to create a mock ResultSet with the given content and SQL type.
     */
    private ResultSet createMockResultSet(String content, int sqlType) throws SQLException {
        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);
        
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnType(1)).thenReturn(sqlType);
        
        switch (sqlType) {
            case Types.BLOB:
                if (content != null) {
                    Blob mockBlob = mock(Blob.class);
                    when(mockBlob.length()).thenReturn((long) content.length());
                    when(mockBlob.getBytes(1L, content.length())).thenReturn(content.getBytes());
                    when(mockResultSet.getBlob(1)).thenReturn(mockBlob);
                } else {
                    when(mockResultSet.getBlob(1)).thenReturn(null);
                }
                break;
            case Types.CLOB:
                if (content != null) {
                    Clob mockClob = mock(Clob.class);
                    when(mockClob.length()).thenReturn((long) content.length());
                    when(mockClob.getSubString(1L, content.length())).thenReturn(content);
                    when(mockResultSet.getClob(1)).thenReturn(mockClob);
                } else {
                    when(mockResultSet.getClob(1)).thenReturn(null);
                }
                break;
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.LONGVARCHAR:
                if (content != null) {
                    when(mockResultSet.getBytes(1)).thenReturn(content.getBytes());
                } else {
                    when(mockResultSet.getBytes(1)).thenReturn(null);
                }
                break;
        }
        
        return mockResultSet;
    }
}
