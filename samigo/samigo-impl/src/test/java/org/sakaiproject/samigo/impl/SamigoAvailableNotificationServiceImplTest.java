package org.sakaiproject.samigo.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;

@RunWith(MockitoJUnitRunner.class)
public class SamigoAvailableNotificationServiceImplTest {

    private static final String OPTIONAL_NOTIFICATION_PREF_KEY = Integer.toString(NotificationService.NOTI_OPTIONAL);

    @Mock private PreferencesService preferencesService;
    @Mock private Preferences preferences;
    @Mock private ResourceProperties resourceProperties;
    @Mock private User user;
    private SamigoAvailableNotificationServiceImpl service;

    @Before
    public void setUp() {
        service = new SamigoAvailableNotificationServiceImpl();
        service.setPreferencesService(preferencesService);
        when(user.getEid()).thenReturn("user1");
    }

    @Test
    public void getOpenNotificationPreferenceDefaultsInvalidPreferenceToImmediate() throws Exception {
        when(user.getId()).thenReturn("user-1");
        when(preferencesService.getPreferences("user-1")).thenReturn(preferences);
        when(preferences.getProperties(NotificationService.PREFS_TYPE + SamigoConstants.NOTI_PREFS_TYPE_SAMIGO_OPEN))
                .thenReturn(resourceProperties);
        when(resourceProperties.getLongProperty(OPTIONAL_NOTIFICATION_PREF_KEY))
                .thenThrow(new EntityPropertyTypeException(OPTIONAL_NOTIFICATION_PREF_KEY));
        when(resourceProperties.getProperty(OPTIONAL_NOTIFICATION_PREF_KEY)).thenReturn("");

        assertEquals(NotificationService.PREF_IMMEDIATE, service.getOpenNotificationPreference(user));
    }

    @Test
    public void getOpenNotificationPreferenceHonorsIgnorePreference() throws Exception {
        when(user.getId()).thenReturn("user-1");
        when(preferencesService.getPreferences("user-1")).thenReturn(preferences);
        when(preferences.getProperties(NotificationService.PREFS_TYPE + SamigoConstants.NOTI_PREFS_TYPE_SAMIGO_OPEN))
                .thenReturn(resourceProperties);
        when(resourceProperties.getLongProperty(OPTIONAL_NOTIFICATION_PREF_KEY))
                .thenReturn((long) NotificationService.PREF_IGNORE);

        assertEquals(NotificationService.PREF_IGNORE, service.getOpenNotificationPreference(user));
    }
}
