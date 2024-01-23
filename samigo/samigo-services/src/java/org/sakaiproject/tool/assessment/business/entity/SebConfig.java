/*
 * Copyright (c) 2023, The Apereo Foundation
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
 *
 */

package org.sakaiproject.tool.assessment.business.entity;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.plist.XMLPropertyListConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.util.HashingUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@Slf4j
public class SebConfig {

    // Added from Map
    private ConfigMode configMode;
    private String configKey;
    private String configUploadId;
    private List<String> examKeys;

    // Not added from map
    private String startLink;

    // Added from map
    private String quitLink;
    private String quitPassword;
    private Boolean allowAudioControl;
    private Boolean allowSpellChecking;
    private Boolean allowUserQuitSeb;
    private Boolean muteOnStartup;
    private Boolean showKeyboardLayout;
    private Boolean showTaskbar;
    private Boolean showTime;
    private Boolean showWifiControl;
    private Boolean userConfirmQuit;

    // Keys in map of assessment metadata

    // Not Needed in pList config
    public static final String CONFIG_MODE = "ASSESSMENT_SEB_CONFIG_MODE";
    public static final String CONFIG_KEY = "ASSESSMENT_SEB_CONFIG_KEY";
    public static final String EXAM_KEYS = "ASSESSMENT_SEB_EXAM_KEYS";
    public static final String CONFIG_UPLOAD_ID = "ASSESSMENT_SEB_CONFIG_UPLOAD_ID";

    // Needed in pList config (SEB client config)
    public static final String QUIT_LINK = "http://quit.seb";
    public static final String ALLOW_AUDIO_CONTROL = "ASSESSMENT_SEB_ALLOW_AUDIO_CONTROL";
    public static final String ALLOW_RELOAD_IN_EXAM = "ASSESSMENT_SEB_ALLOW_RELOAD_IN_EXAM";
    public static final String ALLOW_SPELL_CHECKING = "ASSESSMENT_SEB_ALLOW_SPELL_CHECKING";
    public static final String ALLOW_USER_QUIT_SEB = "ASSESSMENT_SEB_ALLOW_USER_QUIT_SEB";
    public static final String MUTE_ON_STARTUP = "ASSESSMENT_SEB_MUTE_ON_STARTUP";
    public static final String SHOW_KEYBOARD_LAYOUT = "ASSESSMENT_SEB_SHOW_KEYBOARD_LAYOUT";
    public static final String SHOW_RELOAD_BUTTON = "ASSESSMENT_SEB_SHOW_RELOAD_BUTTON";
    public static final String SHOW_TASKBAR = "ASSESSMENT_SEB_SHOW_TASKBAR";
    public static final String SHOW_TIME = "ASSESSMENT_SEB_SHOW_TIME";
    public static final String SHOW_WIFI_CONTROL = "ASSESSMENT_SEB_SHOW_WIFI_CONTROL";
    public static final String USER_CONFIRM_QUIT = "ASSESSMENT_SEB_USER_CONFIRM_QUIT";

    private static final String QUIT_PASSWORD = SecureDeliveryServiceAPI.EXITPWD_KEY;

    // Keys for pList config (SEB client config)

    public static final String START_LINK_KEY = "startURL";
    public static final String QUIT_LINK_KEY = "quitURL";

    private static final String CLEAR_COOKIES_ON_START_KEY = "examSessionClearCookiesOnStart";
    private static final String ALLOW_CONFIG_WINDOW_KEY = "allowPreferencesWindow";
    private static final String SEB_WEBVIEW_VERSION_KEY = "browserWindowWebView";

    private static final String ALLOW_AUDIO_CONTROL_KEY = "audioControlEnabled";
    private static final String ALLOW_RELOAD_IN_EXAM_KEY = "browserWindowAllowReload";
    private static final String ALLOW_SPELL_CHECKING_KEY = "allowSpellCheck";
    private static final String ALLOW_USER_QUIT_SEB_KEY = "allowQuit";
    private static final String MUTE_ON_STARTUP_KEY = "audioMute";
    private static final String SHOW_KEYBOARD_LAYOUT_KEY = "showInputLanguage";
    private static final String SHOW_RELOAD_BUTTON_KEY = "showReloadButton";
    private static final String SHOW_TASKBAR_KEY = "showTaskBar";
    private static final String SHOW_TIME_KEY = "showTime";
    private static final String SHOW_WIFI_CONTROL_KEY = "allowWlan";
    private static final String USER_CONFIRM_QUIT_KEY = "quitURLConfirm";
    private static final String QUIT_PASSWORD_KEY = "hashedQuitPassword";

    public static final String CONFIG_ENCODING = "UTF-8";

    public enum ConfigMode { MANUAL, UPLOAD, CLIENT }

    public static SebConfig of(HashMap<String, String> assessmentMetaDataMap) {
        SebConfig newSebConfig = new SebConfig();

        String configModeString = assessmentMetaDataMap.get(CONFIG_MODE);
        newSebConfig.setConfigMode(configModeString != null ? ConfigMode.valueOf(configModeString) : null);
        newSebConfig.setConfigUploadId(assessmentMetaDataMap.get(CONFIG_UPLOAD_ID));
        newSebConfig.setConfigKey(assessmentMetaDataMap.get(CONFIG_KEY));
        log.info("setting config key {}", newSebConfig.getConfigKey());
        newSebConfig.setExamKeys(getListFromMap(assessmentMetaDataMap, EXAM_KEYS));

        newSebConfig.setQuitLink(assessmentMetaDataMap.get(QUIT_LINK));
        newSebConfig.setQuitPassword(assessmentMetaDataMap.get(QUIT_PASSWORD));
        newSebConfig.setAllowAudioControl(getBooleanFromMap(assessmentMetaDataMap, ALLOW_AUDIO_CONTROL));
        newSebConfig.setAllowSpellChecking(getBooleanFromMap(assessmentMetaDataMap, ALLOW_SPELL_CHECKING));
        newSebConfig.setAllowUserQuitSeb(getBooleanFromMap(assessmentMetaDataMap, ALLOW_USER_QUIT_SEB));
        newSebConfig.setShowKeyboardLayout(getBooleanFromMap(assessmentMetaDataMap, SHOW_KEYBOARD_LAYOUT));
        newSebConfig.setShowTaskbar(getBooleanFromMap(assessmentMetaDataMap, SHOW_TASKBAR));
        newSebConfig.setShowTime(getBooleanFromMap(assessmentMetaDataMap, SHOW_TIME));
        newSebConfig.setShowWifiControl(getBooleanFromMap(assessmentMetaDataMap, SHOW_WIFI_CONTROL));
        newSebConfig.setUserConfirmQuit(getBooleanFromMap(assessmentMetaDataMap, USER_CONFIRM_QUIT));

        return newSebConfig;
    }

    public static SebConfig defaults() {
        SebConfig newSebConfig = new SebConfig();

        newSebConfig.setConfigMode(ConfigMode.MANUAL);
        newSebConfig.setConfigUploadId(null);
        newSebConfig.setConfigKey("");
        newSebConfig.setExamKeys(new ArrayList<>());

        newSebConfig.setQuitPassword("");
        newSebConfig.setAllowAudioControl(true);
        newSebConfig.setAllowSpellChecking(true);
        newSebConfig.setAllowUserQuitSeb(true);
        newSebConfig.setShowKeyboardLayout(true);
        newSebConfig.setShowTaskbar(true);
        newSebConfig.setShowTime(true);
        newSebConfig.setShowWifiControl(true);
        newSebConfig.setUserConfirmQuit(true);

        return newSebConfig;
    }

    private static Boolean getBooleanFromMap(HashMap<String, String> map, String key) {
        String value = map.get(key);
        if (value != null) {
            return Boolean.valueOf(value);
        } else {
            return null;
        }
    }

    // Converts String with newline separated values to a list of strings
    private static List<String> getListFromMap(HashMap<String, String> map, String key) {
        String valuesString = StringUtils.trimToNull(map.get(key));

        if (valuesString == null) {
            return null;
        }

        return Arrays.stream(StringUtils.split(valuesString, "\n"))
                .map(StringUtils::trimToNull)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

    public String toJson() {
        // Get map representation and sort it by key
        // If more properties are desired, which may start with capital letters (like URLFilterEnable),
        // keys will need to be sorted based in a case insensitive manner
        TreeMap<String, Object> map = toMap().entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, TreeMap::new));
        try {
            // Write sorted hashmap to string in json format
            return new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("Could not convert linked hashmap to json {}", e.toString());
            return null;
        }
    }

    public String toPList() {
        BasicConfigurationBuilder<XMLPropertyListConfiguration> configBuilder =
                new BasicConfigurationBuilder<>(XMLPropertyListConfiguration.class)
                        .configure(new Parameters().xml().setThrowExceptionOnMissing(true));
        try {
            XMLPropertyListConfiguration pListConfiguration = configBuilder.getConfiguration();

            // Add values from map representation to configuration
            toMap().entrySet().stream().forEach(entry -> {
                pListConfiguration.addProperty(entry.getKey(), entry.getValue());
            });

            FileHandler fileHandler = new FileHandler(pListConfiguration);
            fileHandler.setEncoding(CONFIG_ENCODING);

            // Write config to string
            StringWriter stringWriter = new StringWriter();
            fileHandler.save(stringWriter);

            return stringWriter.toString();
        } catch (ConfigurationException e) {
            log.error("PList config could not be written: {}", e.toString());
            return null;
        }

    }

    private HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put(QUIT_LINK_KEY, quitLink);
        map.put(ALLOW_AUDIO_CONTROL_KEY, allowAudioControl);
        map.put(ALLOW_SPELL_CHECKING_KEY, allowSpellChecking);
        map.put(ALLOW_USER_QUIT_SEB_KEY, allowUserQuitSeb);
        map.put(SHOW_KEYBOARD_LAYOUT_KEY, showKeyboardLayout);
        map.put(SHOW_TASKBAR_KEY, showTaskbar);
        map.put(SHOW_TIME_KEY, showTime);
        map.put(SHOW_WIFI_CONTROL_KEY, showWifiControl);
        map.put(USER_CONFIRM_QUIT_KEY, userConfirmQuit);
        map.put(MUTE_ON_STARTUP_KEY, muteOnStartup);
        map.put(START_LINK_KEY, startLink);
        map.put(QUIT_PASSWORD_KEY, HashingUtil.hashString(quitPassword));

        // DEFAULTS
        map.put(CLEAR_COOKIES_ON_START_KEY, false);
        map.put(ALLOW_CONFIG_WINDOW_KEY, false);
        map.put(SEB_WEBVIEW_VERSION_KEY, 3);
        map.put(QUIT_LINK_KEY, QUIT_LINK);
        map.put(ALLOW_RELOAD_IN_EXAM_KEY, false);
        map.put(SHOW_RELOAD_BUTTON_KEY, false);

        // Useful properties for demos, makes it possible to record or share the screen
        // Commented, since it is disabling security features of SEB
        //map.put("killExplorerShell", false);
        //map.put("createNewDesktop", false);

        return map;
    }

}
