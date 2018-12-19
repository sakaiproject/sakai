package org.sakaiproject.component.app.help;


import org.mockito.Mockito;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Collections;
import java.util.Locale;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Profile("test")
@Configuration
@ImportResource(locations = {"/datastore.xml", "/help-manager.xml"})
public class TestContext {

    @Bean("org.sakaiproject.user.api.UserDirectoryService")
    @Primary
    public UserDirectoryService userDirectoryService() {
        UserDirectoryService userDirectoryService =  mock(UserDirectoryService.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn("userId");
        when(userDirectoryService.getCurrentUser()).thenReturn(user);
        return userDirectoryService;
    }

    @Bean("org.sakaiproject.tool.api.ActiveToolManager")
    @Primary
    public ActiveToolManager activeToolManager() {
        ActiveToolManager toolManager = mock(ActiveToolManager.class);
        Tool tool = mock(Tool.class);
        // The tool ID gets lowercased when looking for files
        when(tool.getId()).thenReturn("help/toolid");
        Properties properties = new Properties();
        when(tool.getRegisteredConfig()).thenReturn(properties);
        when(toolManager.findTools(null, null)).thenReturn(Collections.singleton(tool));
        return toolManager;
    }

    @Bean("org.sakaiproject.component.api.ServerConfigurationService")
    @Primary
    public ServerConfigurationService serverConfigurationService() {
        ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
        when(serverConfigurationService.getString("help.location")).thenReturn("");
        when(serverConfigurationService.getString("help.localpath", "/help/")).thenReturn("/help/");
        when(serverConfigurationService.getString(eq("help.indexpath"), anyString())).thenReturn("target/index");
        when(serverConfigurationService.getSakaiLocales()).thenReturn(new Locale[]{Locale.ENGLISH});
        when(serverConfigurationService.getString("help.hide")).thenReturn("");
        when(serverConfigurationService.getLocaleFromString(Locale.ENGLISH.toString())).thenReturn(Locale.ENGLISH);
        return serverConfigurationService;
    }

    @Bean("org.sakaiproject.user.api.PreferencesService")
    @Primary
    public PreferencesService preferencesService() {
        PreferencesService preferencesService =  mock(PreferencesService.class);
        when(preferencesService.getLocale("userId")).thenReturn(Locale.ENGLISH);
        return preferencesService;
    }
}
