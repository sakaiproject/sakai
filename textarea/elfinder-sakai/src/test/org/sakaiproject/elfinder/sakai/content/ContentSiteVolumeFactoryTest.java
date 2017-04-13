package org.sakaiproject.elfinder.sakai.content;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsVolume;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.elfinder.sakai.SakaiFsService;
import org.sakaiproject.elfinder.sakai.SiteVolume;
import org.sakaiproject.site.api.SiteService;

@RunWith(MockitoJUnitRunner.class)
public class ContentSiteVolumeFactoryTest {

    @Mock
    private ContentHostingService contentHostingService;

    @Mock
    private SiteService siteService;

    @Mock
    private SakaiFsService service;

    @Mock
    private FsItem siteItem;
    @Mock
    private FsVolume siteVolume;

    private ContentSiteVolumeFactory factory;

    @Before
    public void setUp() {

        factory = new ContentSiteVolumeFactory();
        factory.setSiteService(siteService);
        factory.setContentHostingService(contentHostingService);

    }

    @Test
    public void testGetParent() {

        Mockito.when(contentHostingService.getSiteCollection("siteId")).thenReturn("/group/siteId");
        Mockito.when(contentHostingService.getContainingCollectionId("/group/siteId/example")).thenReturn("/group/siteId");
        Mockito.when(service.getSiteVolume("siteId")).thenReturn(siteVolume);
        Mockito.when(siteVolume.getRoot()).thenReturn(siteItem);

        SiteVolume volume = factory.getVolume(service, "siteId");

        // This is for a path inside the same site
        FsItem fsItem = volume.fromPath("/group/siteId/example");
        Assert.assertNotNull(fsItem);
        FsItem parent = volume.getParent(fsItem);
        Assert.assertNotNull(parent);
        FsItem siteParent = volume.getParent(parent);
        Assert.assertNotNull(siteParent);

        Assert.assertEquals("Should get site back", siteItem, siteParent);
    }

    @Test
    public void testParentCrossSite() {

        Mockito.when(contentHostingService.getSiteCollection("siteId")).thenReturn("/group/siteId");
        Mockito.when(service.getSiteVolume("siteId")).thenReturn(siteVolume);
        Mockito.when(siteVolume.getRoot()).thenReturn(siteItem);

        SiteVolume volume = factory.getVolume(service, "siteId");

        // This is for a path outisde the same site
        FsItem otherItem = volume.fromPath("/group/otherId/example");
        Assert.assertNotNull(otherItem);
        FsItem otherParent = volume.getParent(otherItem);
        Assert.assertNotNull(otherParent);
        // Should have got back the site parent instead
        Assert.assertEquals("Should get site back", siteItem, otherParent);
    }

}
