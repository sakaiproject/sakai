/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.content.impl.serialize.impl.test;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.impl.serialize.impl.conversion.SAXSerializableCollectionAccess;
import org.sakaiproject.content.impl.serialize.impl.conversion.SAXSerializableResourceAccess;

public class SaxSerializerTest {

	
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

	@Test
	public void testResourcesAccess() throws Exception {
		SAXSerializableResourceAccess sr = new SAXSerializableResourceAccess();
		sr.parse(R_TEST_GROUPED_XML);
		Assert.assertEquals(AccessMode.GROUPED, sr.getSerializableAccess());
	}

	@Test
	public void testResourcesAccessNone() throws Exception {
		SAXSerializableResourceAccess sr = new SAXSerializableResourceAccess();
		sr.parse(R_TEST_NONE_XML);
		Assert.assertEquals(AccessMode.INHERITED, sr.getSerializableAccess());
	}

	@Test
	public void testResourcesAccessInherited() throws Exception {
		SAXSerializableResourceAccess sr = new SAXSerializableResourceAccess();
		sr.parse(R_TEST_INHERITED_XML);
		Assert.assertEquals(AccessMode.INHERITED, sr.getSerializableAccess());
	}

	@Test
	public void testResourcesAccessSite() throws Exception {
		SAXSerializableResourceAccess sr = new SAXSerializableResourceAccess();
		sr.parse(R_TEST_SITE_XML);
		Assert.assertEquals(AccessMode.INHERITED, sr.getSerializableAccess());
	}

	@Test
	public void testCollectionAccess() throws Exception {
		SAXSerializableCollectionAccess sr = new SAXSerializableCollectionAccess();
		sr.parse(C_TEST_GROUPED_XML);
		Assert.assertEquals(AccessMode.GROUPED, sr.getSerializableAccess());
	}

	@Test
	public void testCollectionAccessNone() throws Exception {
		SAXSerializableCollectionAccess sr = new SAXSerializableCollectionAccess();
		sr.parse(C_TEST_NONE_XML);
		Assert.assertEquals(AccessMode.INHERITED, sr.getSerializableAccess());
	}

	@Test
	public void testCollectionAccessInherited() throws Exception {
		SAXSerializableCollectionAccess sr = new SAXSerializableCollectionAccess();
		sr.parse(C_TEST_INHERITED_XML);
		Assert.assertEquals(AccessMode.INHERITED, sr.getSerializableAccess());
	}

	@Test
	public void testCollectionAccessSite() throws Exception {
		SAXSerializableCollectionAccess sr = new SAXSerializableCollectionAccess();
		sr.parse(C_TEST_SITE_XML);
		Assert.assertEquals(AccessMode.INHERITED, sr.getSerializableAccess());
	}
}
