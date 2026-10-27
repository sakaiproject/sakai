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
package org.sakaiproject.site.tool;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.UncheckedIOException;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.SakaiArchive;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.FileItem;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.archive.api.ArchiveService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.api.LinkMigrationHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class SiteActionTest {

    private static final String[] PROJECT_SITE_TYPES = {"project"};

    @Mock private AliasService aliasService;
    @Mock private ArchiveService archiveService;
    @Mock private AuthzGroupService authzGroupService;
    @Mock private ContentHostingService contentHostingService;
    @Mock private CourseManagementService courseManagementService;
    @Mock private EntityManager entityManager;
    @Mock private EventTrackingService eventTrackingService;
    @Mock private FormattedText formattedText;
    @Mock private IdManager idManager;
    @Mock private LinkMigrationHelper linkMigrationHelper;
    @Mock private LTIService ltiService;
    @Mock private PreferencesService preferencesService;
    @Mock private SecurityService securityService;
    @Mock private ServerConfigurationService serverConfigurationService;
    @Mock private SessionManager sessionManager;
    @Mock private SiteService siteService;
    @Mock private ThreadLocalManager threadLocalManager;
    @Mock private ToolManager toolManager;
    @Mock private UserAuditRegistration userAuditRegistration;
    @Mock private UserDirectoryService userDirectoryService;
    @Mock private UserTimeService userTimeService;

    private MockedStatic<ComponentManager> componentManagerMock;
    private SiteAction siteAction;

    @Before
    public void setUp() throws Exception {
        componentManagerMock = Mockito.mockStatic(ComponentManager.class);

        componentManagerMock.when(() -> ComponentManager.get(AliasService.class)).thenReturn(aliasService);
        componentManagerMock.when(() -> ComponentManager.get(ArchiveService.class)).thenReturn(archiveService);
        componentManagerMock.when(() -> ComponentManager.get(AuthzGroupService.class)).thenReturn(authzGroupService);
        componentManagerMock.when(() -> ComponentManager.get(CourseManagementService.class)).thenReturn(courseManagementService);
        componentManagerMock.when(() -> ComponentManager.get(EntityManager.class)).thenReturn(entityManager);
        componentManagerMock.when(() -> ComponentManager.get(EventTrackingService.class)).thenReturn(eventTrackingService);
        componentManagerMock.when(() -> ComponentManager.get(FormattedText.class)).thenReturn(formattedText);
        componentManagerMock.when(() -> ComponentManager.get(IdManager.class)).thenReturn(idManager);
        componentManagerMock.when(() -> ComponentManager.get(PreferencesService.class)).thenReturn(preferencesService);
        componentManagerMock.when(() -> ComponentManager.get(SecurityService.class)).thenReturn(securityService);
        componentManagerMock.when(() -> ComponentManager.get(ServerConfigurationService.class)).thenReturn(serverConfigurationService);
        componentManagerMock.when(() -> ComponentManager.get(SessionManager.class)).thenReturn(sessionManager);
        componentManagerMock.when(() -> ComponentManager.get(SiteService.class)).thenReturn(siteService);
        componentManagerMock.when(() -> ComponentManager.get(ThreadLocalManager.class)).thenReturn(threadLocalManager);
        componentManagerMock.when(() -> ComponentManager.get(ToolManager.class)).thenReturn(toolManager);
        componentManagerMock.when(() -> ComponentManager.get(UserDirectoryService.class)).thenReturn(userDirectoryService);
        componentManagerMock.when(() -> ComponentManager.get(UserTimeService.class)).thenReturn(userTimeService);
        componentManagerMock.when(() -> ComponentManager.get("org.sakaiproject.content.api.ContentHostingService")).thenReturn(contentHostingService);
        componentManagerMock.when(() -> ComponentManager.get("org.sakaiproject.util.api.LinkMigrationHelper")).thenReturn(linkMigrationHelper);
        componentManagerMock.when(() -> ComponentManager.get("org.sakaiproject.lti.api.LTIService")).thenReturn(ltiService);
        componentManagerMock.when(() -> ComponentManager.get("org.sakaiproject.userauditservice.api.UserAuditRegistration.sitemanage")).thenReturn(userAuditRegistration);

        siteAction = new SiteAction();
    }

    @After
    public void tearDown() {
        if (componentManagerMock != null) {
            componentManagerMock.close();
        }
    }

    private void configureProjectSiteTypes(String... types) {
        Mockito.when(siteService.getSiteTypeStrings("project")).thenReturn(Arrays.asList(types));
        Mockito.when(serverConfigurationService.getString(Mockito.eq("projectSiteTargetType"), Mockito.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
    }

    @Test
    public void testGetToolRegistrationsSimple() {
        Tool projectTool = Mockito.mock(Tool.class);
        Mockito.when(toolManager.findTools(Collections.singleton("project"), null, false)).thenReturn(Collections.singleton(projectTool));
        configureProjectSiteTypes(PROJECT_SITE_TYPES);

        SessionState state = Mockito.mock(SessionState.class);
        Set<Tool> project = siteAction.getToolRegistrations(state, "project", false);
        Assert.assertThat(project, CoreMatchers.hasItems(projectTool));
    }

    @Test
    public void testGetToolRegistrationNone() {
        configureProjectSiteTypes(PROJECT_SITE_TYPES);
        SessionState state = Mockito.mock(SessionState.class);
        Mockito.when(toolManager.findTools(ArgumentMatchers.anySet(), Mockito.isNull(), ArgumentMatchers.anyBoolean())).thenReturn(Collections.emptySet());

        Set<Tool> other = siteAction.getToolRegistrations(state, "other", false);
        Assert.assertTrue(other.isEmpty());
    }

    @Test
    public void testGetToolRegistrationDefault() {
        Tool projectTool = Mockito.mock(Tool.class);
        SessionState state = Mockito.mock(SessionState.class);
        Mockito.when(state.getAttribute(SiteAction.STATE_DEFAULT_SITE_TYPE)).thenReturn("project");

        Mockito.when(toolManager.findTools(Collections.singleton("new"), null, false)).thenReturn(Collections.emptySet());
        Mockito.when(toolManager.findTools(Collections.singleton("project"), null, false)).thenReturn(Collections.singleton(projectTool));
        configureProjectSiteTypes("project", "new");

        Set<Tool> tools = siteAction.getToolRegistrations(state, "new", false);
        Assert.assertThat(tools, CoreMatchers.hasItems(projectTool));
    }

    @Test
    public void testOriginalToolIds() {
        Tool singleTool = Mockito.mock(Tool.class);
        Tool multipleTool = Mockito.mock(Tool.class);
        Mockito.when(singleTool.getId()).thenReturn("sakai.single");
        Mockito.when(multipleTool.getId()).thenReturn("sakai.multiple");

        Set<Tool> availableTools = new HashSet<>();
        availableTools.add(singleTool);
        availableTools.add(multipleTool);
        Mockito.when(toolManager.findTools(ArgumentMatchers.anySet(), Mockito.isNull(), ArgumentMatchers.anyBoolean())).thenReturn(availableTools);

        Mockito.when(toolManager.getTool("sakai.single")).thenReturn(singleTool);
        Mockito.when(toolManager.getTool("sakai.multiple")).thenReturn(multipleTool);

        Properties singleToolProperties = new Properties();
        singleToolProperties.put("allowMultipleInstances", "false");
        Mockito.when(singleTool.getRegisteredConfig()).thenReturn(singleToolProperties);

        Properties multipleToolProperties = new Properties();
        multipleToolProperties.put("allowMultipleInstances", "true");
        Mockito.when(multipleTool.getRegisteredConfig()).thenReturn(multipleToolProperties);

        configureProjectSiteTypes(PROJECT_SITE_TYPES);

        SessionState state = Mockito.mock(SessionState.class);
        Mockito.when(state.getAttribute(SiteAction.STATE_SITE_TYPE)).thenReturn("project");

        Assert.assertTrue(siteAction.getOriginalToolIds(Collections.singletonList("not.present"), state).isEmpty());
        Assert.assertEquals(Collections.singletonList("sakai.single"), siteAction.getOriginalToolIds(Collections.singletonList("sakai.single"), state));
        Assert.assertEquals(Collections.singletonList("sakai.single"), siteAction.getOriginalToolIds(Arrays.asList("sakai.single", "not.present"), state));
        Assert.assertEquals(Collections.emptyList(), siteAction.getOriginalToolIds(Arrays.asList("sakai.single1", "not.present"), state));
        Assert.assertEquals(Collections.singletonList("sakai.multiple"), siteAction.getOriginalToolIds(Arrays.asList("sakai.multiple", "not.present"), state));
        Assert.assertEquals(Collections.singletonList("sakai.multiple"), siteAction.getOriginalToolIds(Arrays.asList("sakai.multiple1", "sakai.multiple2"), state));
        Assert.assertEquals(Collections.singletonList("sakai.multiple"), siteAction.getOriginalToolIds(Collections.singletonList("012345678901234567890123456789012345sakai.multiple"), state));
    }
    @Rule
    public TemporaryFolder archiveFiles = new TemporaryFolder();

    @Test
    public void replacingUploadedArchiveDeletesPreviousFile() throws Exception {
        Path directory = archiveFiles.newFolder().toPath();
        Path previous = Files.writeString(directory.resolve("previous.zip"), "old upload");
        Map<String, Object> values = new HashMap<>();
        values.put("uploadedArchivePath", previous.toString());
        SessionState state = archiveState(values);
        Mockito.when(siteService.allowImportArchiveSite()).thenReturn(true);
        Mockito.when(serverConfigurationService.getString(Mockito.eq("archive.storage.path"), Mockito.anyString())).thenReturn(directory.toString());
        ParameterParser parameters = Mockito.mock(ParameterParser.class);
        Mockito.when(parameters.getFileItem("importFile")).thenReturn(new FileItem("upload.zip", "application/zip",
            new ByteArrayInputStream(new byte[] {1, 2, 3})));
        JetspeedRunData data = new JetspeedRunData(null, state, "site-info", parameters);
        siteAction.doUploadArchive(data);
        Assert.assertFalse(Files.exists(previous));
        Path replacement = Path.of((String) values.get("uploadedArchivePath"));
        Assert.assertArrayEquals(new byte[] {1, 2, 3}, Files.readAllBytes(replacement));
        Assert.assertEquals("upload.zip", values.get("uploadedArchiveNAme"));
    }

    @Test
    public void failedReplacementPreservesPreviousUploadedArchive() throws Exception {
        Path directory = archiveFiles.newFolder().toPath();
        Path previous = Files.writeString(directory.resolve("previous.zip"), "old upload");
        Map<String, Object> values = new HashMap<>();
        values.put("uploadedArchivePath", previous.toString());
        Mockito.when(siteService.allowImportArchiveSite()).thenReturn(true);
        Mockito.when(serverConfigurationService.getString(Mockito.eq("archive.storage.path"), Mockito.anyString())).thenReturn(directory.toString());
        ParameterParser parameters = Mockito.mock(ParameterParser.class);
        InputStream failing = new InputStream() {
            @Override public int read() throws IOException { throw new IOException("copy failed"); }
        };
        Mockito.when(parameters.getFileItem("importFile")).thenReturn(new FileItem("upload.zip", "application/zip", failing));
        siteAction.doUploadArchive(new JetspeedRunData(null, archiveState(values), "site-info", parameters));
        Assert.assertEquals(previous.toString(), values.get("uploadedArchivePath"));
        Assert.assertEquals("old upload", Files.readString(previous));
        try (Stream<Path> files = Files.list(directory)) {
            Assert.assertEquals(1, files.count());
        }
    }

    @Test
    public void cancellingSiteCreationDeletesUploadedArchive() throws Exception {
        Path archive = archiveFiles.newFile("uploaded.zip").toPath();
        Map<String, Object> values = new HashMap<>();
        values.put("uploadedArchivePath", archive.toString());
        values.put("uploadedArchiveNAme", "uploaded.zip");
        siteAction.doCancel_create(new JetspeedRunData(null, archiveState(values), "site-info", null));
        Assert.assertFalse(Files.exists(archive));
        Assert.assertFalse(values.containsKey("uploadedArchivePath"));
        Assert.assertFalse(values.containsKey("uploadedArchiveNAme"));
    }

    @Test
    public void cancellingStillClearsStateWhenArchiveCannotBeDeleted() throws Exception {
        Path archive = archiveFiles.newFolder().toPath();
        Files.writeString(archive.resolve("keep.txt"), "delete must fail");
        Map<String, Object> values = new HashMap<>();
        values.put("uploadedArchivePath", archive.toString());
        siteAction.doCancel_create(new JetspeedRunData(null, archiveState(values), "site-info", null));
        Assert.assertTrue(Files.exists(archive.resolve("keep.txt")));
        Assert.assertFalse(values.containsKey("uploadedArchivePath"));
    }

    @Test
    public void failedImportReportsErrorAndReleasesSourceAndSessionState() {
        ImportDataSource source = Mockito.mock(ImportDataSource.class,
            Mockito.withSettings().extraInterfaces(SakaiArchive.class));
        Mockito.doThrow(new UncheckedIOException(new IOException("rejected archive")))
            .when((SakaiArchive) source).buildSourceFolder(Mockito.anyCollection());
        Map<String, Object> values = importState(source);
        siteAction.doSaveMtrlSite(new JetspeedRunData(null, archiveState(values), "site-info", null));
        Mockito.verify(source).cleanup();
        Mockito.verify(archiveService, Mockito.never()).merge(Mockito.anyString(), Mockito.anyString(), Mockito.any());
        Assert.assertNotNull(values.get("message"));
        Assert.assertFalse(values.containsKey("import_data_source"));
        Assert.assertFalse(values.containsKey("allzipImports"));
        Assert.assertFalse(values.containsKey("finalzipImports"));
        Assert.assertEquals("45", values.get(org.sakaiproject.site.util.SiteConstants.STATE_TEMPLATE_INDEX));
    }

    @Test
    public void successfulImportReleasesSourceAndRetainsCompletionSummary() {
        ImportDataSource source = Mockito.mock(ImportDataSource.class,
            Mockito.withSettings().extraInterfaces(SakaiArchive.class));
        Mockito.when(((SakaiArchive) source).getSourceFolder()).thenReturn("archive/source");
        Map<String, Object> values = importState(source);
        siteAction.doSaveMtrlSite(new JetspeedRunData(null, archiveState(values), "site-info", null));
        Mockito.verify(source).cleanup();
        Mockito.verify(archiveService).merge("archive/source", "site-a", null);
        Assert.assertFalse(values.containsKey("import_data_source"));
        Assert.assertTrue(values.containsKey("finalzipImports"));
        Assert.assertEquals("48", values.get(org.sakaiproject.site.util.SiteConstants.STATE_TEMPLATE_INDEX));
    }

    private Map<String, Object> importState(ImportDataSource source) {
        Map<String, Object> values = new HashMap<>();
        values.put("site.instance.id", "site-a");
        values.put("finalzipImports", new ArrayList<>());
        values.put("directzipImports", new ArrayList<>());
        values.put("allzipImports", new ArrayList<>());
        values.put("import_data_source", source);
        return values;
    }

    private SessionState archiveState(Map<String, Object> values) {
        return Mockito.mock(SessionState.class, invocation -> {
            switch (invocation.getMethod().getName()) {
                case "getAttribute": return values.get(invocation.getArgument(0));
                case "setAttribute": values.put(invocation.getArgument(0), invocation.getArgument(1)); return null;
                case "removeAttribute": values.remove(invocation.getArgument(0)); return null;
                default: return Mockito.RETURNS_DEFAULTS.answer(invocation);
            }
        });
    }

}
