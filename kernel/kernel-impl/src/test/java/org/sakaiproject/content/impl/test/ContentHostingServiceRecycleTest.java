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
package org.sakaiproject.content.impl.test;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Test for deleting files.
 */
@Slf4j
public class ContentHostingServiceRecycleTest  extends SakaiKernelTestBase {
	private static final String SAMPLE_FOLDER = "/user/admin/";
	
	@BeforeClass
	public static void beforeClass() {
		try {
			// These properties need to be dynamic so they work across linux/mac/windows.
			Properties properties = new Properties();
			properties.put("org.sakaiproject.content.api.ContentHostingService@bodyPath",
					Files.createTempDirectory(FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir")), "files").toString());
			properties.put("org.sakaiproject.content.api.ContentHostingService@bodyPathDeleted",
					Files.createTempDirectory(FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir")), "deleted").toString());
			oneTimeSetup(null, null, properties);    		
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	@Test
    public void testDeleteResource() throws Exception {
        ContentHostingService ch = getService(ContentHostingService.class);
        SessionManager sm = getService(SessionManager.class);
        Session session = sm.getCurrentSession();
        session.setUserId("admin");
        session.setUserEid("admin");

        // Create a file
        String filename = "/"+ UUID.randomUUID().toString();
        ContentResourceEdit resource = ch.addResource(filename);
        resource.setContent("Hello World".getBytes());
        ch.commitResource(resource);

        // Delete the file (into the recycle bin)
        ch.removeResource(filename);
        ch.restoreResource(filename);
    }

    /**
     * This is to test that you can only store one copy of a deleted file.
     * The database schema and UI appear to support having multiple copies of a file presented to a user,
     * however when you restore a file you only pass in the ID and not the UUID so there's no way to
     * determine which copy of a file the user requested be restored. In the future we may wish to change
     * this and support storing multiple old copies of a file.
     *
     * @throws Exception
     */
	@Test
    public void testDeleteResourceTwice() throws Exception {
        ContentHostingService ch = getService(ContentHostingService.class);
        SessionManager sm = getService(SessionManager.class);
        ThreadLocalManager tl = getService(ThreadLocalManager.class);
        reset(tl, sm);

        // Create a file
        String filename = "/"+ UUID.randomUUID().toString();
        ContentResourceEdit resource = ch.addResource(filename);
        resource.setContent("First".getBytes());
        ch.commitResource(resource);

        // Delete the file (into the recycle bin)
        ch.removeResource(filename);

        ContentResourceEdit resource2 = ch.addResource(filename);
        resource2.setContent("Second".getBytes());
        ch.commitResource(resource2);

        ch.removeResource(filename);

        try {
            ch.getResource(filename);
            Assert.fail("We shouldn't be able to find: "+ filename);
        } catch (IdUnusedException e) {
            // Expected
        }

        List<ContentResource> allDeleted = ch.getAllDeletedResources("/");
        int found = 0;
        for (ContentResource deleted : allDeleted) {
            if (deleted.getId().equals(filename)) {
                found++;
            }
        }
        Assert.assertEquals("There should only be one copy of the file in the recycle bin.", 1, found);
    }

    /**
     * This is to check check that when a restore is attempted and the file already exists
     * we correctly unlock the file we are attempting to restore ontop of.
     * @throws Exception
     */
	@Test
    public void testDeleteResourceRestoreOnTop() throws Exception {
        ContentHostingService ch = getService(ContentHostingService.class);
        SessionManager sm = getService(SessionManager.class);
        ThreadLocalManager tl = getService(ThreadLocalManager.class);
        reset(tl, sm);

        String filename = "/"+ UUID.randomUUID().toString();
        ContentResourceEdit resource = ch.addResource(filename);
        resource.setContent("First".getBytes());
        ch.commitResource(resource);

        // Delete the file (into the recycle bin)
        ch.removeResource(filename);

        // Upload another copy to same ID
        ContentResourceEdit resource2 = ch.addResource(filename);
        resource2.setContent("Second".getBytes());
        ch.commitResource(resource2);

        // Attempt to restore ontop of existing file.
        try {
            ch.restoreResource(filename);
            Assert.fail("We should have thrown an exception as the file has been re-created.");
        } catch (IdUsedException iue) {
            // Expected
        }
        // The file in resources shouldn't be locked.
        ch.removeResource(filename);
    }

    /**
     * This is to check that when a restore is attempted and the file exceed quota
     * the file is not restored and we correctly unlock it.
     * @throws Exception
     */
	@Test
    public void testRestoreOnOverquota() throws Exception {
        ContentHostingService ch = getService(ContentHostingService.class);
        SessionManager sm = getService(SessionManager.class);
        ThreadLocalManager tl = getService(ThreadLocalManager.class);
        reset(tl, sm);

        // Set quota to 1kb
		ResourcePropertiesEdit props = ch.newResourceProperties();
		props.addProperty (ResourceProperties.PROP_COLLECTION_BODY_QUOTA,"1");
		ContentCollection c = ch.addCollection(SAMPLE_FOLDER,props);
		ContentCollectionEdit ce = ch.editCollection(SAMPLE_FOLDER);
		ch.commitCollection(ce);

		long quota = ch.getQuota(ch.getCollection(SAMPLE_FOLDER));
		Assert.assertEquals("The quota is set to 1",1,quota);
		
        // Create a file
        String filename = SAMPLE_FOLDER + UUID.randomUUID().toString();

        try {
        	ContentResourceEdit resource = ch.addResource(filename);
        	resource.setContent(new byte[1048]);
        	ch.commitResource(resource);
        	Assert.fail("We should have exceed the quota.");
        } catch (OverQuotaException oqe) {
        	// OverQuota Resource Goes to Trash
        }
        
        try {
            ch.getResource(filename);
            Assert.fail("We shouldn't be able to find: "+ filename);
        } catch (IdUnusedException e) {
            // Expected
        }
        
        try {
            ch.restoreResource(filename);
            Assert.fail("We shouldn't be able to restore: "+ filename);
        } catch (OverQuotaException e) {
            // Expected
        }

        try {
            ch.getResource(filename);
            Assert.fail("We shouldn't be able to find: "+ filename);
        } catch (IdUnusedException e) {
            // Expected
        }

        List<ContentResource> allDeleted = ch.getAllDeletedResources(SAMPLE_FOLDER);
        Assert.assertEquals("There should only be one copy of the file in the recycle bin.", 1, allDeleted.size());

    }

    /**
     * Clear out any threadlocals and reset the session to be admin.
     * @param tl ThreadLocalManager service.
     * @param sm SessionManager service.
     */
    private void reset(ThreadLocalManager tl, SessionManager sm) {
        tl.clear();
        Session session = sm.getCurrentSession();
        session.setUserId("admin");
        session.setUserEid("admin");
    }
}
