/******************************************************************************
 * $URL: $
 * $Id: $
 ******************************************************************************
 *
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *****************************************************************************/

package org.sakaiproject.config.impl;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.IllegalClassException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigData;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigItem;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigurationListener;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigurationProvider;
import org.sakaiproject.component.impl.ConfigItemImpl;
import org.sakaiproject.config.api.HibernateConfigItem;
import org.sakaiproject.config.api.HibernateConfigItemDao;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * KNL-1063
 * Manages the persistence details of a ConfigItem in the form of a HibernateConfigItem.
 * This service does take into consideration multiple application servers and partitions
 * the properties appropriately using the serverId.
 * <p/>
 * DO NOT USE THIS SERVICE to change properties use ServerConfigurationService.
 * <p/>
 * Once SCS has been updated with property changes they will then persisted no need to
 * do anything else.
 *
 * @author Earle Nietzel
 *         Created on Mar 8, 2013
 */
public class StoredConfigService implements ConfigurationListener, ConfigurationProvider {
    // enables persistence of ConfigItems that SCS knows about
    public static final String SAKAI_CONFIG_STORE_ENABLE = "sakai.config.store.enable";
    // registers with SCS as a provider of config
    public static final String SAKAI_CONFIG_PROVIDE_ENABLE = "sakai.config.provide.enable";
    public static final boolean SAKAI_CONFIG_PROVIDE_ENABLE_DEFAULT = true;
    // enables the database poller to provide config during a running system
    public static final String SAKAI_CONFIG_POLL_ENABLE = "sakai.config.poll.enable";
    // how often to poll for config
    public static final String SAKAI_CONFIG_POLL_SECONDS = "sakai.config.poll.seconds";
    // use unexpanded values hence raw
    public static final String SAKAI_CONFIG_USE_RAW = "sakai.config.use.raw";
    // config that should never be persisted
    public static final String SAKAI_CONFIG_NEVER_PERSIST = "sakai.config.never.persist";

    private final Logger log = LoggerFactory.getLogger(StoredConfigService.class);
    private ScheduledExecutorService scheduler;

    private ServerConfigurationService serverConfigurationService;
    private HibernateConfigItemDao dao;
    private PBEStringEncryptor textEncryptor;
    private Set<String> neverPersistItems;

    private String node;

    public void init() {
        log.info("init()");

        node = serverConfigurationService.getServerId();
        if (StringUtils.isBlank(node)) {
            log.error("init(); node cannot be blank, StoredConfigService is disabled");
            return;
        }

        // get configured config to skip
        String[] doNotPersistConfig = serverConfigurationService.getStrings(SAKAI_CONFIG_NEVER_PERSIST);
        List<String> tmpdoNotPersist;
        if (doNotPersistConfig == null) {
            // SCS can return a null here if config is not found
            tmpdoNotPersist = new ArrayList<String>();
        } else {
            tmpdoNotPersist = Arrays.asList(doNotPersistConfig);
        }
        // always add password@org.jasypt.encryption.pbe.PBEStringEncryptor
        tmpdoNotPersist.add("password@org.jasypt.encryption.pbe.PBEStringEncryptor");
        // TODO add more stuff here like serverId, DB password
        // Setup list of items we should never persist
        neverPersistItems = Collections.unmodifiableSet(new HashSet<String>(tmpdoNotPersist));

        // delete items that should never persisted
        for (String item : neverPersistItems) {
            deleteHibernateConfigItem(item);
        }

        // enables storing of all config that SCS knows about
        if (serverConfigurationService.getBoolean(SAKAI_CONFIG_STORE_ENABLE, false)) {
            learnConfig(serverConfigurationService.getConfigData().getItems());
            serverConfigurationService.registerListener(this);
        }

        if (serverConfigurationService.getBoolean(SAKAI_CONFIG_POLL_ENABLE, false)) {
            final int pollDelaySeconds = serverConfigurationService.getInt(SAKAI_CONFIG_POLL_SECONDS, 60);
            // setup an ExecutorService
            scheduler = Executors.newSingleThreadScheduledExecutor();
            // schedule task for every pollDelaySeconds
            scheduler.scheduleWithFixedDelay(
                new Runnable() {
                    Date pollDate;
                    @Override
                    public void run() {
                        pollDate = storedConfigPoller(pollDelaySeconds, pollDate);
                    }
                },
                pollDelaySeconds < 120 ? 120 : pollDelaySeconds, // minimally wait 120 seconds for sakai to start
                pollDelaySeconds, TimeUnit.SECONDS
            );
            log.info("init() " + SAKAI_CONFIG_POLL_ENABLE + " is enabled and polling every " + pollDelaySeconds + " seconds");
        }
    }

    public void destroy() {
        // terminate the scheduler
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    private Date storedConfigPoller(int delay, Date then) {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();

        if (then == null) {
            // on start set then
            calendar.add(Calendar.SECOND, -(delay));
            then = calendar.getTime();
        }

        List<HibernateConfigItem> polled = findPollOn(then, now);

        int registered = 0;

        for (HibernateConfigItem item : polled) {
            if (item.isRegistered()) { // registering items that are not marked as registered leads to bad things in SCS
                ConfigItem cItem = serverConfigurationService.getConfigItem(item.getName());
                if (!item.similar(cItem)) { // only register items that are not similar, reduces history on items in SCS
                    serverConfigurationService.registerConfigItem(createConfigItem(item));
                    registered++;
                }
            } else {
                log.warn("storedConfigPoller() item " + item.getName() + " is not registered skipping");
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("storedConfigPoller() Polling found " + polled.size() + " config item(s) (from " +
                              new SimpleDateFormat("HH:mm:ss").format(then) + " to " +
                              new SimpleDateFormat("HH:mm:ss").format(now) + "), " + registered + " item(s) registered");
        }

        return now;
    }

    /**
     * Creates a HibernateConfigItem for every item in the list and persists it for this node.
     *
     * @param items List of ConfigItem
     */
    private void learnConfig(List<ConfigItem> items) {
        if (items == null) {
            return;
        }

        int total = items.size();
        int updated = 0;
        int created = 0;


        for (ConfigItem item : items) {
            if (item == null) {
                continue; // skip this item
            }

            HibernateConfigItem hItem = null;
            int rows = countByName(item.getName());

            if (rows == 0) {
                // new item, create it
                hItem = createHibernateConfigItem(item);

                created++;
            } else {
                // sync config with files when store enabled and not provide enabled
                if (!serverConfigurationService.getBoolean(SAKAI_CONFIG_PROVIDE_ENABLE, SAKAI_CONFIG_PROVIDE_ENABLE_DEFAULT)) {
                    hItem = findByName(item.getName());

                    hItem = updateHibernateConfigItem(hItem, item);

                    if (hItem != null) {
                        // if hItem is not null it was updated
                        updated++;
                    }
                }
            }

            saveOrUpdate(hItem);
        }
        log.info("initItems() processed " + total + " config items, updated " + updated + " created " + created);
    }

    /**
     * Persists or updates (if it already exists) a HibernateConfigItem to the database for this node
     *
     * @param hItem a HibernateConfigItem
     */
    public void saveOrUpdate(HibernateConfigItem hItem) {

        if (hItem == null) {
            return;
        }

        String name = hItem.getName();
        String type = hItem.getType();

        if (name == null || name.isEmpty()) {
            log.warn("saveOrUpdate() item name is missing");
            return;
        } else if (!(ServerConfigurationService.TYPE_STRING.equals(type)
                             || ServerConfigurationService.TYPE_INT.equals(type)
                             || ServerConfigurationService.TYPE_BOOLEAN.equals(type)
                             || ServerConfigurationService.TYPE_ARRAY.equals(type))) {
            log.warn("saveOrUpdate() item type is incorrect");
            return;
        }

        dao.saveOrUpdate(hItem);
    }

    /**
     * Get all registered HibernateConfigItem as ConfigItems for this node
     *
     * @return a List of ConfigItem
     */
    public List<ConfigItem> getConfigItems() {
        List<ConfigItem> configItems = new ArrayList<ConfigItem>();
        List<HibernateConfigItem> regItems = findRegistered();

        for (HibernateConfigItem hItem : regItems) {
            ConfigItem item = createConfigItem(hItem);
            if (item != null) {
                configItems.add(item);
                if (log.isDebugEnabled()) {
                    log.debug("getConfigItems() " + item.toString());
                }
            }
        }

        return configItems;
    }

    /**
     * Creates an equivalent ConfigItem from a HibernateConfigItem
     *
     * @param hItem a HibernateConfigItem
     * @return a ConfigItem
     * @throws IllegalClassException this can occur during deserialization when creating a ConfigItem
     */
    public ConfigItem createConfigItem(HibernateConfigItem hItem) throws IllegalClassException {
        if (hItem == null) {
            return null;
        }

        String value;

        if (serverConfigurationService.getBoolean(SAKAI_CONFIG_USE_RAW, false)
                    && StringUtils.isNotBlank(hItem.getRawValue())
                ) {
            value = hItem.getRawValue();
        } else {
            value = hItem.getValue();
        }

        // create new ConfigItem
        ConfigItem item = new ConfigItemImpl(
            hItem.getName(),
            deSerializeValue(value, hItem.getType(), hItem.isSecured()),
            hItem.getType(),
            hItem.getDescription(),
            this.getClass().getName(),
            deSerializeValue(hItem.getDefaultValue(), hItem.getType(), hItem.isSecured()),
            0, // requested
            0, // changed
            null, // history
            hItem.isRegistered(),
            hItem.isDefaulted(),
            hItem.isSecured(),
            hItem.isDynamic()
        );

        if (log.isDebugEnabled()) {
            log.debug("createConfigItem() " + item.toString());
        }

        return item;
    }

    /**
     * Creates an equivalent HibernateConfigItem from a ConfigItem
     *
     * @param item a ConfigItem
     * @return a HibernateConfigItem
     * @throws IllegalClassException thrown when the item.getValue doesn't return the right type
     */
    public HibernateConfigItem createHibernateConfigItem(ConfigItem item) throws IllegalClassException {
        if (item == null || neverPersistItems.contains(item.getName())) {
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("createHibernateConfigItem() New ConfigItem = " + item.toString());
        }

        String serialValue;
        String serialDefaultValue;
        String serialRawValue;

        try {
            serialValue = serializeValue(item.getValue(), item.getType(), item.isSecured());
            serialDefaultValue = serializeValue(item.getDefaultValue(), item.getType(), item.isSecured());
            serialRawValue = serializeValue(getRawProperty(item.getName()), ServerConfigurationService.TYPE_STRING, item.isSecured());
        } catch (IllegalClassException ice) {
            log.error("createHibernateConfigItem() IllegalClassException " + ice.getMessage() + " skip ConfigItem " + item.toString(), ice);
            return null;
        }

        HibernateConfigItem hItem = new HibernateConfigItem(node,
                                                                   item.getName(),
                                                                   serialValue,
                                                                   serialRawValue,
                                                                   item.getType(),
                                                                   item.getDescription(),
                                                                   item.getSource(),
                                                                   serialDefaultValue,
                                                                   item.isRegistered(),
                                                                   item.isDefaulted(),
                                                                   item.isSecured(),
                                                                   item.isDynamic());

        if (log.isDebugEnabled()) {
            log.debug("createHibernateConfigItem() Created HibernateConfigItem = " + hItem.toString());
        }

        return hItem;
    }

    /**
     * Updates a HibernateConfigItem with a ConfigItem's data; the name, node
     *
     * @param hItem    a HibernateConfigItem
     * @param item     a ConfigItem
     * @return a HibernateConfigItem if it was updated or null if it was not updated
     * @throws IllegalClassException thrown when the item.getValue doesn't return the right type
     */
    public HibernateConfigItem updateHibernateConfigItem(HibernateConfigItem hItem, ConfigItem item) throws IllegalClassException {
        if (hItem == null || item == null) {
            return null;
        }

        HibernateConfigItem updatedItem = null;

        // check if updating is needed, update it
        if (!hItem.similar(item)) {
            // if they are not similar update it
            if (log.isDebugEnabled()) {
                log.debug("updateHibernateConfigItem() Before " + hItem.toString());
            }

            Object value = deSerializeValue(hItem.getValue(), hItem.getType(), hItem.isSecured());
            Object defaultValue = deSerializeValue(hItem.getDefaultValue(), hItem.getType(), hItem.isSecured());

            try {
                if (value == null) {
                    if (item.getValue() != null) {
                        // different update
                        hItem.setValue(serializeValue(item.getValue(), item.getType(), item.isSecured()));
                    }
                } else if (!value.equals(item.getValue())) {
                    // different update
                    hItem.setValue(serializeValue(item.getValue(), item.getType(), item.isSecured()));
                }

                if (defaultValue == null) {
                    if (item.getDefaultValue() != null) {
                        // different update
                        hItem.setDefaultValue(serializeValue(item.getDefaultValue(), item.getType(), item.isSecured()));
                    }
                } else if (!defaultValue.equals(item.getDefaultValue())) {
                    // different update
                    hItem.setDefaultValue(serializeValue(item.getDefaultValue(), item.getType(), item.isSecured()));
                }
            } catch (IllegalClassException ice) {
                log.error("updateHibernateConfigItem() IllegalClassException " + ice.getMessage() + " skip ConfigItem " + item.toString(), ice);
                return null;
            }


            hItem.setType(item.getType());
            hItem.setDefaulted(item.isDefaulted());
            hItem.setSecured(item.isSecured());
            hItem.setRegistered(item.isRegistered());
            hItem.setSource(item.getSource());
            hItem.setDescription(item.getDescription());
            hItem.setDynamic(item.isDynamic());
            hItem.setModified(Calendar.getInstance().getTime());

            if (log.isDebugEnabled()) {
                log.debug("updateHibernateConfigItem() After " + hItem.toString());
            }

            updatedItem = hItem;
        }

        // check if raw value needs updating
        // raw values are always strings
        String rawValue = (String) deSerializeValue(hItem.getRawValue(), ServerConfigurationService.TYPE_STRING, hItem.isSecured());

        if (!StringUtils.equals(rawValue, getRawProperty(hItem.getName()))) {
            // different update
            hItem.setRawValue(serializeValue(getRawProperty(hItem.getName()), ServerConfigurationService.TYPE_STRING, item.isSecured()));
            updatedItem = hItem;
        }

        // only return a hItem if it was updated
        return updatedItem;
    }

    /**
     * Delete a HibernateConfigItem
     *
     * @param name The HibernateConfigItem that should be deleted
     */
    public void deleteHibernateConfigItem(String name) {
        if (StringUtils.isBlank(name)) {
            return;
        }

        HibernateConfigItem hItem = findByName(name);
        if (hItem != null) {
            log.info("deleteHibernateConfigItem() delete HibernateConfigItem = " + hItem);
            dao.delete(hItem);
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.component.api.ServerConfigurationService.ConfigurationListener#changed(org.sakaiproject.component.api.ServerConfigurationService.ConfigItem, org.sakaiproject.component.api.ServerConfigurationService.ConfigItem)
     */
    @Override
    public void changed(ConfigItem item, ConfigItem previous) {
        // a ConfigItem has changed
        if (item == null) {
            return;
        }

        HibernateConfigItem hItem = findByName(item.getName());
        if (hItem == null) {
            // new hItem
            hItem = createHibernateConfigItem(item);
        } else {
            // existing hItem, update it
            hItem = updateHibernateConfigItem(hItem, item);
        }

        saveOrUpdate(hItem);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.component.api.ServerConfigurationService.ConfigurationListener#changing(org.sakaiproject.component.api.ServerConfigurationService.ConfigItem, org.sakaiproject.component.api.ServerConfigurationService.ConfigItem)
     */
    @Override
    public ConfigItem changing(ConfigItem currentConfigItem, ConfigItem newConfigItem) {
        // no need to do anything before item has changed
        return null;
    }

    @Override
    public List<ConfigItem> registerConfigItems(ConfigData configData) {
        if (serverConfigurationService.getBoolean(SAKAI_CONFIG_PROVIDE_ENABLE, SAKAI_CONFIG_PROVIDE_ENABLE_DEFAULT)) {
            return getConfigItems();
        }
        return Collections.emptyList();
    }

    /**
     * Find a HibernateConfigItem with a matching name for this node
     *
     * @param name the name of the item to find
     * @return a HibernateConfigItem matching the name for this node or null if none found
     */
    public HibernateConfigItem findByName(String name) {
        HibernateConfigItem item = null;
        List<HibernateConfigItem> list = dao.findAllByCriteriaByNode(node, name, null, null, null, null);
        if (list.size() == 1) {
            item = list.get(0);
        }
        return item;
    }

    /**
     * Number of properties with a matching name on this node
     *
     * @param name the name of the item to count
     * @return a positive integer or -1 if name or node is null
     */
    public int countByName(String name) {
        return dao.countByNodeAndName(node, name);
    }

    /**
     * Number of all properties on this node
     *
     * @return a positive integer or -1 if node is null
     */
    public int countAll() {
        return dao.countByNode(node);
    }

    /**
     * Find all HibernateConfigItem(s) for this node
     *
     * @return a List of HibernateConfigItem(s)
     */
    public List<HibernateConfigItem> findAll() {
        return dao.findAllByCriteriaByNode(node, null, null, null, null, null);
    }

    /**
     * Find all Secured HibernateConfigItem(s) for this node
     *
     * @return a List of HibernateConfigItem(s)
     */
    public List<HibernateConfigItem> findSecured() {
        return dao.findAllByCriteriaByNode(node, null, null, null, null, true);
    }

    /**
     * Find all Registered HibernateConfigItem(s) for this node
     *
     * @return a List of HibernateConfigItem(s)
     */
    public List<HibernateConfigItem> findRegistered() {
        return dao.findAllByCriteriaByNode(node, null, null, true, null, null);
    }

    /**
     * Find all Dynamic HibernateConfigItem(s) for this node
     *
     * @return a List of HibernateConfigItem(s)
     */
    public List<HibernateConfigItem> findDynamic() {
        return dao.findAllByCriteriaByNode(node, null, null, null, true, null);
    }

    /**
     * Find all Defaulted HibernateConfigItem(s) for this node
     *
     * @return a List of HibernateConfigItem(s)
     */
    public List<HibernateConfigItem> findDefaulted() {
        return dao.findAllByCriteriaByNode(node, null, true, null, null, null);
    }

    /**
     * Find all HibernateConfigItem(s) that have a pollOn during a specified time
     * If a date is null then that date is left open then time is unbounded in that direction
     * If both dates are null then every config item that has a pollOn date is returned
     *
     * @param after  select items after this timestamp
     * @param before select items before this timestamp
     * @return a List of HibernateConfigItem(s)
     */
    public List<HibernateConfigItem> findPollOn(Date after, Date before) {
        return dao.findPollOnByNode(node, after, before);
    }

    private String getRawProperty(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        String value = null;
        ConfigItem ci = serverConfigurationService.getConfigItem(name); // null if it does not exist
        if (ci != null) {
            String rawValue = serverConfigurationService.getRawProperty(name);
            value = StringUtils.trimToNull(rawValue);
        }
        return value;
    }

    private Object deSerializeValue(String value, String type, boolean secured) throws IllegalClassException {
        if (value == null || type == null) {
            return null;
        }

        String string;

        if (secured) {
            // sanity check should be Base64 encoded
            if (Base64.isBase64(value)) {
                string = textEncryptor.decrypt(value);
            } else {
                log.warn("deSerializeValue() Invalid value found attempting to decrypt a secured property, check your secured properties");
                string = value;
            }
        } else {
            string = value;
        }

        Object obj;

        if (ServerConfigurationService.TYPE_STRING.equals(type)) {
            obj = string;
        } else if (ServerConfigurationService.TYPE_INT.equals(type)) {
            obj = Integer.valueOf(string);
        } else if (ServerConfigurationService.TYPE_BOOLEAN.equals(type)) {
            obj = Boolean.valueOf(string);
        } else if (ServerConfigurationService.TYPE_ARRAY.equals(type)) {
            obj = string.split(HibernateConfigItem.ARRAY_SEPARATOR);
        } else {
            throw new IllegalClassException("deSerializeValue() invalid TYPE, while deserializing");
        }
        return obj;
    }

    private String serializeValue(Object obj, String type, boolean secured) throws IllegalClassException {
        if (obj == null || type == null) {
            return null;
        }

        String string;

        if (ServerConfigurationService.TYPE_STRING.equals(type)) {
            if (obj instanceof String) {
                string = String.valueOf(obj);
            } else {
                throw new IllegalClassException(String.class, obj);
            }
        } else if (ServerConfigurationService.TYPE_INT.equals(type)) {
            if (obj instanceof Integer) {
                string = Integer.toString((Integer) obj);
            } else {
                throw new IllegalClassException(Integer.class, obj);
            }
        } else if (ServerConfigurationService.TYPE_BOOLEAN.equals(type)) {
            if (obj instanceof Boolean) {
                string = Boolean.toString((Boolean) obj);
            } else {
                throw new IllegalClassException(Boolean.class, obj);
            }
        } else if (ServerConfigurationService.TYPE_ARRAY.equals(type)) {
            if (obj instanceof String[]) {
                string = StringUtils.join((String[]) obj, HibernateConfigItem.ARRAY_SEPARATOR);
            } else {
                throw new IllegalClassException("serializeValue() expected an array of type String[]");
            }
        } else {
            throw new IllegalClassException("serializeValue() invalid TYPE, while serializing");
        }

        if (secured) {
            string = textEncryptor.encrypt(string);
        }

        return string;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setDao(HibernateConfigItemDao dao) {
        this.dao = dao;
    }

    public void setTextEncryptor(PBEStringEncryptor textEncryptor) {
        this.textEncryptor = textEncryptor;
    }

}

