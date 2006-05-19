/*
 * Copyright 2004-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.store.jdbc;

import java.util.Properties;

import org.apache.lucene.store.jdbc.handler.MarkDeleteFileEntryHandler;
import org.apache.lucene.store.jdbc.index.FetchOnBufferReadJdbcIndexInput;
import org.apache.lucene.store.jdbc.index.RAMAndFileJdbcIndexOutput;

/**
 * A file entry level settings. An abstract view of any type of setting that cab be
 * used by the actual file entry handler that uses it.
 * <p/>
 * Holds the {@link #FILE_ENTRY_HANDLER_TYPE} that defines the type of the
 * {@link org.apache.lucene.store.jdbc.handler.FileEntryHandler} that will be created
 * and initialized with the settings.
 * <p/>
 * Default values for a new instanciated instnce are: {@link MarkDeleteFileEntryHandler} for
 * the {@link #FILE_ENTRY_HANDLER_TYPE} setting, {@link FetchOnBufferReadJdbcIndexInput} for the
 * {@link #INDEX_INPUT_TYPE_SETTING} setting, and {@link RAMAndFileJdbcIndexOutput} for the
 * {@link #INDEX_OUTPUT_TYPE_SETTING} setting.
 *
 * @author kimchy
 */
public class JdbcFileEntrySettings {

    /**
     * The class name of the {@link org.apache.lucene.store.IndexInput}. Only applies
     * to {@link org.apache.lucene.store.jdbc.handler.FileEntryHandler}s that use it.
     */
    public static final String INDEX_INPUT_TYPE_SETTING = "indexInput.type";

    /**
     * The class name of the {@link org.apache.lucene.store.IndexOutput}. Only applies
     * to {@link org.apache.lucene.store.jdbc.handler.FileEntryHandler}s that use it.
     */
    public static final String INDEX_OUTPUT_TYPE_SETTING = "indexOutput.type";

    /**
     * The class name of the {@link org.apache.lucene.store.jdbc.handler.FileEntryHandler}.
     */
    public static final String FILE_ENTRY_HANDLER_TYPE = "type";

    private Properties settings = new Properties();

    /**
     * Creates a new file entry settings, and intialize it to default values.
     */
    public JdbcFileEntrySettings() {
        setClassSetting(JdbcFileEntrySettings.FILE_ENTRY_HANDLER_TYPE, MarkDeleteFileEntryHandler.class);
        setClassSetting(JdbcFileEntrySettings.INDEX_INPUT_TYPE_SETTING, FetchOnBufferReadJdbcIndexInput.class);
        setClassSetting(JdbcFileEntrySettings.INDEX_OUTPUT_TYPE_SETTING, RAMAndFileJdbcIndexOutput.class);
    }

    /**
     * Returns the inner java properties.
     */
    public Properties getProperties() {
        return settings;
    }

    /**
     * Returns the value match for the given setting. <code>null</code> if no
     * setting is found.
     *
     * @param setting The setting name
     * @return The value of the setting, or <code>null</code> if none is found
     */
    public String getSetting(String setting) {
        return settings.getProperty(setting);
    }

    /**
     * Returns the value that matches the given setting. If none is found,
     * the default value is used.
     *
     * @param setting The setting name
     * @param defaultValue The default value to be used if no setting is found
     * @return The value of the setting, or defaultValue if none is found.
     */
    public String getSetting(String setting, String defaultValue) {
        return settings.getProperty(setting, defaultValue);
    }

    /**
     * Returns the float value that matches the given setting. If none if found,
     * the default value is used.
     *
     * @param setting The setting name
     * @param defaultValue The default value to be used if no setting is found
     * @return The value of the setting, or defaultValue if none is found.
     */
    public float getSettingAsFloat(String setting, float defaultValue) {
        String sValue = getSetting(setting);
        if (sValue == null) {
            return defaultValue;
        }
        return Float.parseFloat(sValue);
    }

    /**
     * Returns the int value that matches the given setting. If none if found,
     * the default value is used.
     *
     * @param setting The setting name
     * @param defaultValue The default value to be used if no setting is found
     * @return The value of the setting, or defaultValue if none is found.
     */
    public int getSettingAsInt(String setting, int defaultValue) {
        String sValue = getSetting(setting);
        if (sValue == null) {
            return defaultValue;
        }
        return Integer.parseInt(sValue);
    }

    /**
     * Returns the long value that matches the given setting. If none if found,
     * the default value is used.
     *
     * @param setting The setting name
     * @param defaultValue The default value to be used if no setting is found
     * @return The value of the setting, or defaultValue if none is found.
     */
    public long getSettingAsLong(String setting, long defaultValue) {
        String sValue = getSetting(setting);
        if (sValue == null) {
            return defaultValue;
        }
        return Long.parseLong(sValue);
    }

    /**
     * Returns the class value that matches the given setting. If none if found,
     * the default value is used.
     *
     * @param setting The setting name
     * @param defaultValue The default value to be used if no setting is found
     * @return The value of the setting, or defaultValue if none is found.
     */
    public Class getSettingAsClass(String setting, Class defaultValue) throws ClassNotFoundException {
        return getSettingAsClass(setting, defaultValue, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Returns the class value that matches the given setting. If none if found,
     * the default value is used.
     *
     * @param setting The setting name
     * @param defaultValue The default value to be used if no setting is found
     * @param classLoader The class loader to be used to load the class
     * @return The value of the setting, or defaultValue if none is found.
     * @throws ClassNotFoundException
     */
    public Class getSettingAsClass(String setting, Class defaultValue, ClassLoader classLoader) throws ClassNotFoundException {
        String sValue = getSetting(setting);
        if (sValue == null) {
            return defaultValue;
        }
        return Class.forName(sValue, true, classLoader);
    }

    /**
     * Returns the boolean value that matches the given setting. If none if found,
     * the default value is used.
     *
     * @param setting The setting name
     * @param defaultValue The default value to be used if no setting is found
     * @return The value of the setting, or defaultValue if none is found.
     */
    public boolean getSettingAsBoolean(String setting, boolean defaultValue) {
        String sValue = getSetting(setting);
        if (sValue == null) {
            return defaultValue;
        }
        return Boolean.valueOf(sValue).booleanValue();
    }

    public JdbcFileEntrySettings setSetting(String setting, String value) {
        this.settings.setProperty(setting, value);
        return this;
    }

    public JdbcFileEntrySettings setBooleanSetting(String setting, boolean value) {
        setSetting(setting, String.valueOf(value));
        return this;
    }

    public JdbcFileEntrySettings setFloatSetting(String setting, float value) {
        setSetting(setting, String.valueOf(value));
        return this;
    }

    public JdbcFileEntrySettings setIntSetting(String setting, int value) {
        setSetting(setting, String.valueOf(value));
        return this;
    }

    public JdbcFileEntrySettings setLongSetting(String setting, long value) {
        setSetting(setting, String.valueOf(value));
        return this;
    }

    public JdbcFileEntrySettings setClassSetting(String setting, Class clazz) {
        setSetting(setting, clazz.getName());
        return this;
    }
}
