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
package org.sakaiproject.util.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.component.api.ServerConfigurationService;

import org.sakaiproject.util.MergeConfig;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Simple tests to check it's working correctly.
 */
@RunWith(MockitoJUnitRunner.class)
public class LinkMigrationHelperImplTest {


    @Mock
    private ServerConfigurationService serverConfigurationService;
    private LinkMigrationHelperImpl impl;

    @Before
    public void setUp() {
        impl = new LinkMigrationHelperImpl();
        impl.setServerConfigurationService(serverConfigurationService);
        // Always return defaults
        when(serverConfigurationService.getString(anyString(), anyString())).then(a -> a.getArgument(1));
        when(serverConfigurationService.getServerUrl()).thenReturn("http://localhost:8080");
    }

    @Test
    public void testBracketAndNullifySelecedLinks() throws Exception {
        assertEquals("", impl.bracketAndNullifySelectedLinks(""));

        assertEquals("<a href='http://example.com/'>example.com</a>", impl.bracketAndNullifySelectedLinks("<a href='http://example.com/'>example.com</a>" ) );

        assertEquals("This  [Link] .", impl.bracketAndNullifySelectedLinks("This <a href='/url/posts/'>Link</a>."));

        assertEquals("This  [<a href='/url/forum/'>Link</a>] .", impl.bracketAndNullifySelectedLinks("This <a href='/url/forum/'>Link</a>."));
    }

    @Test
    public void testBracketAndNullifySelectedLinksIdempotent() throws Exception {
        String once = impl.bracketAndNullifySelectedLinks("This <a href='/url/forum/'>Link</a>.");
        assertEquals(once, impl.bracketAndNullifySelectedLinks(once));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBracketAndNullifySelectedLinksBroken() throws Exception {
        impl.bracketAndNullifySelectedLinks("<a href='http://example.com'>No closing tag");
    }

    @Test
    public void testMigrateOneLink() {
        assertEquals("this <a href='/url/newId'>newId</a>", impl.migrateOneLink("oldId", "newId", "this <a href='/url/oldId'>oldId</a>"));
    }

    @Test
    public void testMigrateLinksInMergedRTE() {
        // Test basic migration of oldId to newId
        MergeConfig mcx = new MergeConfig();
        mcx.archiveContext = "oldId";
        mcx.archiveServerUrl = "http://www.zap.com";
        String content = "this <a href='http://www.zap.com/access/content/group/oldId/ietf-postel-06.png'>text</a>";
        String migrated = impl.migrateLinksInMergedRTE("newId", mcx, content);
        String result  = "this <a href='http://localhost:8080/access/content/group/newId/ietf-postel-06.png'>text</a>";
        assertEquals(result, migrated);

        // Test link with different site ID (should remain unchanged)
        content = "this <a href='http://www.zap.com/access/content/group/weirdId/ietf-postel-06.png'>text</a>";
        migrated = impl.migrateLinksInMergedRTE("newId", mcx, content);
        result = "this <a href='http://localhost:8080/access/content/group/weirdId/ietf-postel-06.png'>text</a>";
        assertEquals(result, migrated);

        // Test multiple links in the same content
        content = "this <a href='http://www.zap.com/access/content/group/oldId/file1.pdf'>link1</a> and " +
                 "<a href='http://www.zap.com/access/content/group/oldId/file2.jpg'>link2</a>";
        migrated = impl.migrateLinksInMergedRTE("newId", mcx, content);
        result = "this <a href='http://localhost:8080/access/content/group/newId/file1.pdf'>link1</a> and " +
                 "<a href='http://localhost:8080/access/content/group/newId/file2.jpg'>link2</a>";
        assertEquals(result, migrated);

        
        // Test with non-matching URL pattern (should remain unchanged)
        content = "this <a href='http://www.zap.com/different/path/oldId/file.pdf'>text</a>";
        migrated = impl.migrateLinksInMergedRTE("newId", mcx, content);
        result = "this <a href='http://www.zap.com/different/path/oldId/file.pdf'>text</a>";
        assertEquals(result, migrated);

        // Test direct link migration
        content = "Check this discussion [http://www.zap.com/direct/forum_topic/123]";
        migrated = impl.migrateLinksInMergedRTE("newId", mcx, content);
        result = "Check this discussion [http://localhost:8080/direct/forum_topic/123]";
        assertEquals(result, migrated);

        // Test multiple direct links in the same content
        content = "First topic [http://www.zap.com/direct/forum_topic/123] and " +
                 "second topic [http://www.zap.com/direct/forum_topic/456]";
        migrated = impl.migrateLinksInMergedRTE("newId", mcx, content);
        result = "First topic [http://localhost:8080/direct/forum_topic/123] and " +
                "second topic [http://localhost:8080/direct/forum_topic/456]";
        assertEquals(result, migrated);

        // Test mix of direct and content links
        content = "Resource <a href='http://www.zap.com/access/content/group/oldId/file.pdf'>here</a> " +
                 "and discussion [http://www.zap.com/direct/forum_topic/789]";
        migrated = impl.migrateLinksInMergedRTE("newId", mcx, content);
        result = "Resource <a href='http://localhost:8080/access/content/group/newId/file.pdf'>here</a> " +
                "and discussion [http://localhost:8080/direct/forum_topic/789]";
        assertEquals(result, migrated);

        // Test with different source domain
        mcx.archiveServerUrl = "https://other-domain.com";
        content = "this <a href='https://other-domain.com/access/content/group/oldId/file.pdf'>text</a>";
        migrated = impl.migrateLinksInMergedRTE("newId", mcx, content);
        result = "this <a href='http://localhost:8080/access/content/group/newId/file.pdf'>text</a>";
        assertEquals(result, migrated);

    }

}
