package org.sakaiproject.lessonbuildertool.tool.beans;

import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageItemImpl;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.memory.api.MemoryService;

import uk.org.ponder.messageutil.MessageLocator;

public class SimplePageBeanTest {

    private SimplePageBean simplePageBean;

    @Before
    public void before() {
        MessageLocator messageLocator = mock(MessageLocator.class);
        SimplePageToolDao dao = mock(SimplePageToolDao.class);
        MemoryService memoryService = mock(MemoryService.class);

        try (MockedStatic<ComponentManager> cm = mockStatic(ComponentManager.class);
             MockedStatic<ServerConfigurationService> scs = mockStatic(ServerConfigurationService.class)) {
            cm.when(() -> ComponentManager.get("org.sakaiproject.memory.api.MemoryService")).thenReturn(memoryService);
            scs.when(() -> ServerConfigurationService.getString("lessonbuilder.html.types", "html,xhtml,htm,xht")).thenReturn("html,xhtml,htm,xht");
            simplePageBean = new SimplePageBean();
        }

        simplePageBean.setMessageLocator(messageLocator);
        simplePageBean.setSimplePageToolDao(dao);

        // common data for tests
        SimplePageItem i1 = new SimplePageItemImpl(1, 0, 0, 2, "1", "Lessons");
        SimplePageItem i2 = new SimplePageItemImpl(2, 1, 1, 2, "2", "SubPage 1");
        SimplePageItem i3 = new SimplePageItemImpl(3, 1, 2, 2, "3", "SubPage 2");
        SimplePageItem i4 = new SimplePageItemImpl(4, 2, 1, 2, "4", "SubPage 1 - 1");
        SimplePageItem i5 = new SimplePageItemImpl(5, 2, 2, 2, "5", "SubPage 1 - 2");
        SimplePageItem i6 = new SimplePageItemImpl(6, 3, 1, 2, "6", "SubPage 2 - 1");
        List<SimplePageItem> l1 = Collections.singletonList(i1);
        List<SimplePageItem> l2 = Collections.singletonList(i2);
        List<SimplePageItem> l3 = Collections.singletonList(i3);
        List<SimplePageItem> l4 = Collections.singletonList(i4);
        List<SimplePageItem> l5 = Collections.singletonList(i5);
        List<SimplePageItem> l6 = Collections.singletonList(i6);
        when(dao.findItemsBySakaiId("1")).thenReturn(l1);
        when(dao.findItemsBySakaiId("2")).thenReturn(l2);
        when(dao.findItemsBySakaiId("3")).thenReturn(l3);
        when(dao.findItemsBySakaiId("4")).thenReturn(l4);
        when(dao.findItemsBySakaiId("5")).thenReturn(l5);
        when(dao.findItemsBySakaiId("6")).thenReturn(l6);
        when(simplePageBean.getMessageLocator().getMessage("simplepage.printall.continuation")).thenReturn("continuation");
    }

    @Test
    public void subPagePathTest() {
        SimplePageItem i6 = new SimplePageItemImpl(6, 3, 1, 2, "6", "SubPage 2 - 1");
        String path = simplePageBean.getSubPagePath(i6, false);
        Assert.assertEquals("Lessons > SubPage 2", path);

        path = simplePageBean.getSubPagePath(i6, true);
        Assert.assertEquals("Lessons (continuation) SubPage 2", path);
    }

    @Test
    public void infiniteSubPagePathTest() {
        SimplePageItem i7 = new SimplePageItemImpl(7, 0, 0, 2, "0", "Infinite");
        SimplePageItem i5 = new SimplePageItemImpl(5, 2, 2, 2, "5", "SubPage 1 - 2");
        List<SimplePageItem> l7 = Collections.singletonList(i7);
        when(simplePageBean.getSimplePageToolDao().findItemsBySakaiId("0")).thenReturn(l7);

        String path = simplePageBean.getSubPagePath(i5, false);
        Assert.assertEquals("Infinite > Lessons > SubPage 1", path);
        verify(simplePageBean.getSimplePageToolDao(), times(2)).findItemsBySakaiId("0");
    }
}
