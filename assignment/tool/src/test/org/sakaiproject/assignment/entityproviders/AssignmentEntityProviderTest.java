package org.sakaiproject.assignment.entityproviders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.ToolManager;

public class AssignmentEntityProviderTest {

    private AssignmentEntityProvider provider;
    private List<Site> sites;
    private Site site1, site2;
    private Properties properties1, properties2;
    private ToolManager toolManager;

    @Before
    public void setUp() {
        toolManager = mock(ToolManager.class);
        provider = new AssignmentEntityProvider();
        provider.setToolManager(toolManager);

        // Create sites
        sites = new ArrayList<Site>();
        //Site1
        site1 = mock(Site.class);
        when(site1.getId()).thenReturn("Site1");
        when(site1.getType()).thenReturn("course");

        SitePage sitePage1 = mock(SitePage.class);
        when(site1.getPages()).thenReturn(List.of(sitePage1));

        ToolConfiguration tool1 = mock(ToolConfiguration.class);
        when(tool1.getToolId()).thenReturn("sakai.assignment.grades");
        properties1 = mock(Properties.class);

        when(tool1.getConfig()).thenReturn(properties1);
        when(sitePage1.getTools()).thenReturn(List.of(tool1));

        sites.add(site1);
        //Site2
        site2 = mock(Site.class);
        when(site2.getId()).thenReturn("Site2");
        when(site2.getType()).thenReturn("course");

        SitePage sitePage2 = mock(SitePage.class);
        when(site2.getPages()).thenReturn(List.of(sitePage2));

        ToolConfiguration tool2 = mock(ToolConfiguration.class);
        when(tool2.getToolId()).thenReturn("sakai.assignment.grades");
        properties2 = mock(Properties.class);

        when(tool2.getConfig()).thenReturn(properties2);
        when(sitePage2.getTools()).thenReturn(List.of(tool2));

        sites.add(site2);
    }

    @Test
    public void isHiddenAssignmentInAllSitesTest() throws Exception{
        when(properties1.getProperty(ToolManager.PORTAL_VISIBLE)).thenReturn("false");
        when(properties2.getProperty(ToolManager.PORTAL_VISIBLE)).thenReturn("false");

        assertTrue(!sites.isEmpty());
        for (Site site : sites) {
            assertTrue(!site.getId().isBlank());
            assertEquals("false", site.getPages().get(0).getTools().get(0).getConfig().getProperty(ToolManager.PORTAL_VISIBLE));
            boolean visible = Boolean.parseBoolean(site.getPages().get(0).getTools().get(0).getConfig().getProperty(ToolManager.PORTAL_VISIBLE));
            //isHidden return true if visible is false
            when(toolManager.isHidden(site.getPages().get(0).getTools().get(0))).thenReturn(!visible);
        }

        assertTrue(provider.isHiddenAssignmentInAllSites(sites));
    }

    @Test
    public void notIsHiddenAssignmentInAllSitesTest() throws Exception{
        when(properties1.getProperty(ToolManager.PORTAL_VISIBLE)).thenReturn("true");
        when(properties2.getProperty(ToolManager.PORTAL_VISIBLE)).thenReturn("false");

        assertTrue(!sites.isEmpty());
        for (Site site : sites) {
            assertTrue(!site.getId().isBlank());
            boolean visible = Boolean.parseBoolean(site.getPages().get(0).getTools().get(0).getConfig().getProperty(ToolManager.PORTAL_VISIBLE));
            //isHidden return true if visible is false
            when(toolManager.isHidden(site.getPages().get(0).getTools().get(0))).thenReturn(!visible);
        }

        assertTrue(!provider.isHiddenAssignmentInAllSites(sites));
    }

    @Test
    public void isHiddenAssignmentToolTest() throws Exception{
        when(properties1.getProperty(ToolManager.PORTAL_VISIBLE)).thenReturn("false");

        assertTrue(!site1.getId().isBlank());
        boolean visible = Boolean.parseBoolean(site1.getPages().get(0).getTools().get(0).getConfig().getProperty(ToolManager.PORTAL_VISIBLE));
        //isHidden return true if visible is false
        when(toolManager.isHidden(site1.getPages().get(0).getTools().get(0))).thenReturn(!visible);

        assertTrue(provider.isHiddenAssignmentTool(site1));
    }

    @Test
    public void notIsHiddenAssignmentToolTest() throws Exception{
        when(properties2.getProperty(ToolManager.PORTAL_VISIBLE)).thenReturn("true");

        assertTrue(!site2.getId().isBlank());
        boolean visible = Boolean.parseBoolean(site2.getPages().get(0).getTools().get(0).getConfig().getProperty(ToolManager.PORTAL_VISIBLE));
        //isHidden return true if visible is false
        when(toolManager.isHidden(site2.getPages().get(0).getTools().get(0))).thenReturn(!visible);

        assertTrue(!provider.isHiddenAssignmentTool(site2));
    }

}
