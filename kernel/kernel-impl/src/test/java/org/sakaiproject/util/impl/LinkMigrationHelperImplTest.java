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

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.component.api.ServerConfigurationService;

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
    }

    @Test
    public void testBracketAndNullifySelecedLinks() throws Exception {
        assertEquals("", impl.bracketAndNullifySelectedLinks(""));

        assertEquals("<a href='http://example.com/'>example.com</a>", impl.bracketAndNullifySelectedLinks("<a href='http://example.com/'>example.com</a>" ) );

        assertEquals("This  [Link] .", impl.bracketAndNullifySelectedLinks("This <a href='/url/posts/'>Link</a>."));

        assertEquals("This <a href='/url/forum/'>Link</a>.", impl.bracketAndNullifySelectedLinks("This <a href='/url/forum/'>Link</a>."));
        assertEquals("This <a href='/direct/assignment/123'>Assignment</a>.", impl.bracketAndNullifySelectedLinks("This <a href='/direct/assignment/123'>Assignment</a>."));
        assertEquals("This  [Quiz] .", impl.bracketAndNullifySelectedLinks("This <a href='/samigo/sam_pub/123'>Quiz</a>."));
    }

    @Test
    public void testMigrateAllLinksPreservesLabels() {
        Map<String, String> links = Map.of(
                "/direct/assignment/old", "/direct/assignment/new",
                "/direct/forum_topic/old", "/direct/forum_topic/new",
                "/access/content/group/old", "/access/content/group/new");
        String content = "<p><a href='/direct/assignment/old'>Homework</a></p>"
                + "<p><a href='/direct/forum_topic/old'>Discussion</a></p>"
                + "<p><a href='/access/content/group/old/file.pdf'>Resource</a></p>";
        String expected = "<p><a href='/direct/assignment/new'>Homework</a></p>"
                + "<p><a href='/direct/forum_topic/new'>Discussion</a></p>"
                + "<p><a href='/access/content/group/new/file.pdf'>Resource</a></p>";
        String migrated = impl.migrateAllLinks(links.entrySet(), content);
        assertEquals(expected, migrated);
        assertEquals(expected, impl.migrateAllLinks(links.entrySet(), migrated));
    }

    @Test
    public void testExplicitLinkBracketingConfiguration() throws Exception {
        when(serverConfigurationService.getString("LinkMigrationHelper.linksToBracket", ""))
                .thenReturn("assignment,forum");
        assertEquals(" [<a href='/direct/assignment/123'>Homework</a>] ",
                impl.bracketAndNullifySelectedLinks("<a href='/direct/assignment/123'>Homework</a>"));
        assertEquals(" [<a href='/direct/forum_topic/123'>Discussion</a>] ",
                impl.bracketAndNullifySelectedLinks("<a href='/direct/forum_topic/123'>Discussion</a>"));
    }

    @Test
    public void testBracketAndNullifySelectedLinksIdempotent() throws Exception {
        when(serverConfigurationService.getString("LinkMigrationHelper.linksToBracket", ""))
                .thenReturn("assignment,forum");
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

}
