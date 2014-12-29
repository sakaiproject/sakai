package org.sakaiproject.content.impl.serialize.impl.test;

import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.impl.serialize.impl.conversion.SAXSerializableCollectionAccess;
import org.sakaiproject.content.impl.serialize.impl.conversion.SAXSerializableResourceAccess;

import junit.framework.TestCase;

public class SaxSerializerTest extends TestCase {

	
	private static final String R_XML_START = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<resource content-length=\"12\" content-type=\"text/plain\" " +
			" filePath=\"/2007/255/12/9bc0da9e-175c-4e26-9beb-8fbffbadc8f5\" " +
			"id=\"/private/sampleAccess/mercury/test.txt\" " +
			" resource-type=\"org.sakaiproject.content.types.fileUpload\" ";
	
	private static final String R_XML_END =
			"sakai:hidden=\"false\"> " +
			"<properties><property enc=\"BASE64\" name=\"CHEF:creator\" " +
			" value=\"YWRtaW4=\"/><property enc=\"BASE64\" name=\"CHEF:modifiedby\" " +
			" value=\"YWRtaW4=\"/><property enc=\"BASE64\" name=\"CHEF:is-collection\" " +
			" value=\"ZmFsc2U=\"/><property enc=\"BASE64\" name=\"DAV:getlastmodified\" " +
			" value=\"MjAwNzA5MTIxMjA5MDk1MjQ=\"/><property enc=\"BASE64\" " +
			" name=\"DAV:getcontentlength\" value=\"MTI=\"/><property enc=\"BASE64\" " +
			" name=\"DAV:getcontenttype\" value=\"dGV4dC9wbGFpbg==\"/> " +
			" <property enc=\"BASE64\" name=\"sakai:reference-root\" " +
			" value=\"L3NhbXBsZUFjY2Vzcw==\"/><property enc=\"BASE64\" " +
			" name=\"DAV:creationdate\" value=\"MjAwNzA5MTIxMjA5MDk1MjM=\"/>" +
			" <property enc=\"BASE64\" name=\"DAV:displayname\" value=\"dGVzdC50eHQ=\"/>" +
			" </properties></resource>";
	private static final String C_XML_START = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
	"<collection  " +
	"  " +
	"id=\"/private/sampleAccess/mercury/test.txt\" " +
	"  ";

private static final String C_XML_END =
	"sakai:hidden=\"false\"> " +
	"<properties><property enc=\"BASE64\" name=\"CHEF:creator\" " +
	" value=\"YWRtaW4=\"/><property enc=\"BASE64\" name=\"CHEF:modifiedby\" " +
	" value=\"YWRtaW4=\"/><property enc=\"BASE64\" name=\"CHEF:is-collection\" " +
	" value=\"ZmFsc2U=\"/><property enc=\"BASE64\" name=\"DAV:getlastmodified\" " +
	" value=\"MjAwNzA5MTIxMjA5MDk1MjQ=\"/><property enc=\"BASE64\" " +
	" name=\"DAV:getcontentlength\" value=\"MTI=\"/> " +
	" <property enc=\"BASE64\" name=\"sakai:reference-root\" " +
	" value=\"L3NhbXBsZUFjY2Vzcw==\"/><property enc=\"BASE64\" " +
	" name=\"DAV:creationdate\" value=\"MjAwNzA5MTIxMjA5MDk1MjM=\"/>" +
	" <property enc=\"BASE64\" name=\"DAV:displayname\" value=\"dGVzdC50eHQ=\"/>" +
	" </properties></collection>";
	private static final String R_TEST_NONE_XML = R_XML_START+" "+R_XML_END;
	private static final String R_TEST_INHERITED_XML = R_XML_START+" sakai:access_mode=\"inherited\" "+R_XML_END;
	private static final String R_TEST_SITE_XML = R_XML_START+" sakai:access_mode=\"site\" "+R_XML_END;
	private static final String R_TEST_GROUPED_XML = R_XML_START+" sakai:access_mode=\"grouped\" "+R_XML_END;
	private static final String C_TEST_NONE_XML = C_XML_START+" "+C_XML_END;
	private static final String C_TEST_INHERITED_XML = C_XML_START+" sakai:access_mode=\"inherited\" "+C_XML_END;
	private static final String C_TEST_SITE_XML = C_XML_START+" sakai:access_mode=\"site\" "+C_XML_END;
	private static final String C_TEST_GROUPED_XML = C_XML_START+" sakai:access_mode=\"grouped\" "+C_XML_END;
	public void testResourcesAccess() throws Exception {
		SAXSerializableResourceAccess sr = new SAXSerializableResourceAccess();
		sr.parse(R_TEST_GROUPED_XML);
		assertEquals(AccessMode.GROUPED, sr.getSerializableAccess());
	}
	public void testResourcesAccessNone() throws Exception {
		SAXSerializableResourceAccess sr = new SAXSerializableResourceAccess();
		sr.parse(R_TEST_NONE_XML);
		assertEquals(AccessMode.INHERITED, sr.getSerializableAccess());
	}
	public void testResourcesAccessInherited() throws Exception {
		SAXSerializableResourceAccess sr = new SAXSerializableResourceAccess();
		sr.parse(R_TEST_INHERITED_XML);
		assertEquals(AccessMode.INHERITED, sr.getSerializableAccess());
	}
	public void testResourcesAccessSite() throws Exception {
		SAXSerializableResourceAccess sr = new SAXSerializableResourceAccess();
		sr.parse(R_TEST_SITE_XML);
		assertEquals(AccessMode.INHERITED, sr.getSerializableAccess());
	}
	
	public void testCollectionAccess() throws Exception {
		SAXSerializableCollectionAccess sr = new SAXSerializableCollectionAccess();
		sr.parse(C_TEST_GROUPED_XML);
		assertEquals(AccessMode.GROUPED, sr.getSerializableAccess());
	}
	public void testCollectionAccessNone() throws Exception {
		SAXSerializableCollectionAccess sr = new SAXSerializableCollectionAccess();
		sr.parse(C_TEST_NONE_XML);
		assertEquals(AccessMode.INHERITED, sr.getSerializableAccess());
	}
	public void testCollectionAccessInherited() throws Exception {
		SAXSerializableCollectionAccess sr = new SAXSerializableCollectionAccess();
		sr.parse(C_TEST_INHERITED_XML);
		assertEquals(AccessMode.INHERITED, sr.getSerializableAccess());
	}
	public void testCollectionAccessSite() throws Exception {
		SAXSerializableCollectionAccess sr = new SAXSerializableCollectionAccess();
		sr.parse(C_TEST_SITE_XML);
		assertEquals(AccessMode.INHERITED, sr.getSerializableAccess());
	}
	
}
