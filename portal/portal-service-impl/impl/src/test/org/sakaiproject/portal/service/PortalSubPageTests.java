package org.sakaiproject.portal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.portal.api.PortalSubPageData;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PortalSubPageTests {

    private static String loadFromFile(String fileName) {
        Resource resource = new ClassPathResource(fileName);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Can't read file: " + fileName, e);
        }
    }

    // TODO more tests need to be added
    @Test
    public void testDeserializePortalSubPageData() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        String json = loadFromFile("subpage.json");

        // Deserialize from JSON
        PortalSubPageData deserializedData = objectMapper.readValue(json, PortalSubPageData.class);

        // Assertions
        Assert.assertNotNull(deserializedData);
        Assert.assertEquals("ff4fb877-4f84-48d1-b1b1-6f1cda7fcb4e", deserializedData.getSiteId());
        Assert.assertEquals("6cf7782d-8eb8-4f37-a4ce-1f1b1374f7d2", deserializedData.getUserId());

        List<PortalSubPageData.PageData> pages = deserializedData.getPages().get("783d0765-bd24-4253-8a63-ac8889cb25e0");
        Assert.assertEquals(1, pages.size());

        // PageData Assertions
        PortalSubPageData.PageData page = pages.get(0);
        Assert.assertEquals("783d0765-bd24-4253-8a63-ac8889cb25e0", page.getToolId());
        Assert.assertEquals("11", page.getItemId());
        Assert.assertFalse(page.isHidden());
        Assert.assertEquals("Chapter One", page.getName());
        Assert.assertFalse(page.isPrerequisite());
        Assert.assertEquals("ff4fb877-4f84-48d1-b1b1-6f1cda7fcb4e", page.getSiteId());
        Assert.assertEquals("", page.getDescription());
        Assert.assertEquals("f6060e88-82e0-412f-ae3b-3a98a45c2619", page.getSakaiPageId());
        Assert.assertTrue(page.isCompleted());
        Assert.assertEquals("8", page.getSendingPage());
        Assert.assertFalse(page.isRequired());

        // TopLevelPage Assertions
        List<PortalSubPageData.PageProps> topLevelPages = deserializedData.getTopLevelPageProps();
        Assert.assertEquals(1, topLevelPages.size());

        PortalSubPageData.PageProps topLevelPage = topLevelPages.get(0);
        Assert.assertEquals("783d0765-bd24-4253-8a63-ac8889cb25e0", topLevelPage.getToolId());
        Assert.assertFalse(topLevelPage.isHidden());
        Assert.assertEquals("Lessons", topLevelPage.getName());
        Assert.assertFalse(topLevelPage.isPrerequisite());
        Assert.assertEquals("ff4fb877-4f84-48d1-b1b1-6f1cda7fcb4e", topLevelPage.getSiteId());
        Assert.assertTrue(topLevelPage.isCompleted());
        Assert.assertFalse(topLevelPage.isRequired());

        // i18n Assertions
        PortalSubPageData.I18n i18n = deserializedData.getI18n();
        Assert.assertEquals("[You must complete all prerequisites before viewing this item]", i18n.getPrerequisiteAndDisabled());
        Assert.assertEquals("Expand to show subpages", i18n.getExpand());
        Assert.assertEquals("Click to open top-level page", i18n.getOpenTopLevelPage());
        Assert.assertEquals("[Hidden]", i18n.getHidden());
        Assert.assertEquals("[Not released until {releaseDate}]", i18n.getHiddenWithReleaseDate());
        Assert.assertEquals("[Has prerequisites]", i18n.getPrerequisite());
        Assert.assertEquals("Main Page", i18n.getMainLinkName());
        Assert.assertEquals("Collapse to hide subpages", i18n.getCollapse());
    }
}
