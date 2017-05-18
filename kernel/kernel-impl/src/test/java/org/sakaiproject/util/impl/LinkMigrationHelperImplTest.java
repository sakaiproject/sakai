package org.sakaiproject.util.impl;

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

        assertEquals("This  [<a href='/url/forum/'>Link</a>] .", impl.bracketAndNullifySelectedLinks("This <a href='/url/forum/'>Link</a>."));
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
