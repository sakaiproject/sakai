/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/branches/SAK-18678/api/src/main/java/org/sakaiproject/site/api/Site.java $
 * $Id: Site.java 81275 2010-08-14 09:24:56Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.messagebundle.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.messagebundle.api.MessageBundleProperty;
import org.sakaiproject.messagebundle.api.MessageBundleService;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Responsible for managing the message bundle data in a database.  Provides search capabilities
 * for finding keys based on values.
 * <p>
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Mar 16, 2010
 * Time: 1:38:05 PM
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
public class MessageBundleServiceImpl implements MessageBundleService {
    /**
     * list of bundles that we've already indexed, only want to update once per startup
     */
    private Set<String> indexedList = new HashSet<String>();
    /**
     * number of millis before running saveOrUpdateTask again to clear queue
     */
    private long scheduleDelay = 5000;
    /**
     * whether or not to save bundle data right away, or queue up for processing in another thread.
     */
    private boolean scheduleSaves = true;

    /**
     * Timer used to schedule processing
     */
    private Timer timer = new Timer(true);
    /**
     * Queue of method invocations to save or update message bundle data
     */
    private List<SaveOrUpdateCall> queue = Collections.synchronizedList(new ArrayList<>());

    @Getter private boolean enabled = false;

    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SessionFactory sessionFactory;
    @Setter private TransactionTemplate transactionTemplate;

    public void init() {
        if (serverConfigurationService.getBoolean("load.bundles.from.db", true)) {
            enabled = true;
            timer.schedule(new SaveOrUpdateTask(), 0, scheduleDelay);
        }
    }

    public void destroy() {
        if (enabled) {
            timer.cancel();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getSearchCount(String searchQuery, String module, String baseName, String locale) {
        Number count = 0;
        Criteria query = sessionFactory.getCurrentSession().createCriteria(MessageBundleProperty.class);
        try {
            if (StringUtils.isNotEmpty(searchQuery)) {
                query.add(Restrictions.disjunction()
                        .add(Restrictions.ilike("defaultValue", searchQuery, MatchMode.ANYWHERE))
                        .add(Restrictions.ilike("value", searchQuery, MatchMode.ANYWHERE))
                        .add(Restrictions.ilike("propertyName", searchQuery, MatchMode.ANYWHERE)));
            }
            if (StringUtils.isNotEmpty(module)) {
                query.add(Restrictions.eq("moduleName", module));
            }
            if (StringUtils.isNotEmpty(baseName)) {
                query.add(Restrictions.eq("baseName", baseName));
            }
            if (StringUtils.isNotEmpty(locale)) {
                query.add(Restrictions.eq("locale", locale));
            }
            query.setProjection(Projections.rowCount());
            try {
                count = (Number) query.uniqueResult();
            } catch (HibernateException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("problem searching the message bundle data", e);
        }
        return count.intValue();
    }

    /**
     * schedule timer task to save/update the bundle data.  We are using Timer to offload the work,
     * otherwise intial loads of tools will appear very slow, this way it happens in the background.
     * In the original rSmart impl JMS was used, but since the MessageService is in contrib not core
     * we need another solution to avoid that dependency. Currently we are using a java.util.Timer and
     * scheduled Timer task to queue up and process the calls to this method.  This is a similar
     * strategy to that used in BaseDigestService.
     *
     * @param baseName
     * @param moduleName
     * @param newBundle
     * @param loc
     */
    @Override
    @Transactional
    public void saveOrUpdate(String baseName, String moduleName, ResourceBundle newBundle, Locale loc) {
        if (enabled && newBundle != null && loc != null && (StringUtils.isNotBlank(baseName) && StringUtils.isNotBlank(moduleName))) {
            if (scheduleSaves) {
                queueBundle(baseName, moduleName, convertResourceBundleToMap(newBundle), loc);
            } else {
                saveOrUpdateInternal(baseName, moduleName, convertResourceBundleToMap(newBundle), loc);
            }
        }
    }

    /**
     * internal work for responding to a save or update request.  This method will add new bundles data
     * if it doesn't exist, otherwise updates the data preserving any current value if its been modified.
     * This approach allows for upgrades to automatically detect and persist new keys, as well as updating
     * any default values that flow in from an upgrade.
     *
     * @param baseName
     * @param moduleName
     * @param newBundle
     * @param loc
     */
    protected void saveOrUpdateInternal(String baseName, String moduleName, Map<String, String> newBundle, Locale loc) {
        String keyName = getIndexKeyName(baseName, moduleName, loc.toString());
        if (indexedList.contains(keyName)) {
            log.debug("skip bundle as its already happened once for: {}", keyName);
            return;
        }

        for (Entry<String, String> entry : newBundle.entrySet()) {
            String value = entry.getValue();
            MessageBundleProperty mbp = new MessageBundleProperty(baseName, moduleName, loc.toString(), entry.getKey());
            MessageBundleProperty existingMbp = getProperty(mbp);
            if (existingMbp == null) {
                // new property so add
                mbp.setDefaultValue(value);
                updateMessageBundleProperty(mbp);
                log.debug("adding message bundle: {}", mbp.toString());
            } else {
                // update an existing properties default value if different
                if (!StringUtils.equals(value, existingMbp.getDefaultValue())) {
                    existingMbp.setDefaultValue(value);
                    updateMessageBundleProperty(existingMbp);
                    log.debug("updating message bundle: {}", existingMbp.toString());
                }
            }
        }
        indexedList.add(keyName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageBundleProperty> search(String searchQuery, String module, String baseName, String locale) {

        Criteria query = sessionFactory.getCurrentSession().createCriteria(MessageBundleProperty.class);

        try {
            if (StringUtils.isNotEmpty(searchQuery)) {
                query.add(Restrictions.disjunction()
                        .add(Restrictions.ilike("defaultValue", searchQuery, MatchMode.ANYWHERE))
                        .add(Restrictions.ilike("value", searchQuery, MatchMode.ANYWHERE))
                        .add(Restrictions.ilike("propertyName", searchQuery, MatchMode.ANYWHERE)));
            }
            if (StringUtils.isNotEmpty(module)) {
                query.add(Restrictions.eq("moduleName", module));
            }
            if (StringUtils.isNotEmpty(baseName)) {
                query.add(Restrictions.eq("baseName", baseName));
            }
            if (StringUtils.isNotEmpty(locale)) {
                query.add(Restrictions.eq("locale", locale));
            }

            return (List<MessageBundleProperty>) query.list();

        } catch (Exception e) {
            log.error("problem searching the message bundle data", e);
        }
        return new ArrayList<>();
    }

    private Map<String, String> convertResourceBundleToMap(ResourceBundle resource) {
        Map<String, String> map = new HashMap<String, String>();

        Enumeration<String> keys = resource.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            map.put(key, resource.getString(key));
        }

        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public MessageBundleProperty getMessageBundleProperty(long id) {
        return (MessageBundleProperty) sessionFactory.getCurrentSession().get(MessageBundleProperty.class, id);
    }

    @Override
    @Transactional
    public void updateMessageBundleProperty(MessageBundleProperty mbp) {
        if (mbp == null) return;
        if (mbp.getDefaultValue() == null) {
            mbp.setDefaultValue("");
        }
        sessionFactory.getCurrentSession().merge(mbp);
    }

    @Override
    @Transactional
    public void deleteMessageBundleProperty(MessageBundleProperty mbp) {
        try {
            sessionFactory.getCurrentSession().delete(mbp);
        } catch (Exception e) {
            log.warn("Cound not delete MessageBundleProperty " + mbp + ", " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public MessageBundleProperty getProperty(MessageBundleProperty mbp) {
        if (mbp == null) {
            return null;
        }
        Query query = sessionFactory.getCurrentSession().getNamedQuery("findProperty");
        query.setString("basename", mbp.getBaseName());
        query.setString("module", mbp.getModuleName());
        query.setString("name", mbp.getPropertyName());
        query.setString("locale", mbp.getLocale());
        List<MessageBundleProperty> results = (List<MessageBundleProperty>) query.list();
        if (results.size() == 0) {
            if (log.isDebugEnabled()) log.debug("can't find a message bundle property for : " + mbp);
            return null;
        }
        return results.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getBundle(String baseName, String moduleName, Locale loc) {
        Map<String, String> map = new HashMap<>();

        if (enabled && loc != null && StringUtils.isNoneBlank(baseName, moduleName)) {
            Query query = sessionFactory.getCurrentSession().getNamedQuery("findPropertyWithNullValue");
            query.setString("basename", baseName);
            query.setString("module", moduleName);
            query.setString("locale", loc.toString());
            List<MessageBundleProperty> results = (List<MessageBundleProperty>) query.list();

            for (MessageBundleProperty mbp : results) {
                map.put(mbp.getPropertyName(), mbp.getValue());
            }

            if (map.isEmpty() && log.isDebugEnabled()) {
                log.debug("can't find any values for: " + getIndexKeyName(baseName, moduleName, loc.toString()));
            }
        }
        return map;
    }

    /**
     * This produces a key used to uniquely identify a bundle, it will always return a valid key
     * @param baseName basename of the bundle
     * @param moduleName modulename of the bundle
     * @param locale locale of the bundle
     * @return a String that uniquely identifies a bundle
     */
    public static String getIndexKeyName(String baseName, String moduleName, String locale) {
        String mName = StringUtils.isNotBlank(moduleName) ? moduleName : "*";
        String bName = StringUtils.isNotBlank(baseName) ? baseName : "*";
        String lName = StringUtils.isNotBlank(locale) ? locale : "*";
        return String.join("_", mName, bName, lName);
    }

    @Override
    @Transactional(readOnly = true)
    public int getModifiedPropertiesCount() {
        String query = "select count(*) from MessageBundleProperty where value != null";
        return executeCountQuery(query);
    }

    @Override
    @Transactional(readOnly = true)
    public int getAllPropertiesCount() {
        String query = "select count(*) from MessageBundleProperty";
        return executeCountQuery(query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageBundleProperty> getAllProperties(String locale, String basename, String module) {

        Criteria query = sessionFactory.getCurrentSession().createCriteria(MessageBundleProperty.class);
        query.setCacheable(true);

        if (StringUtils.isNotEmpty(locale)) {
            query.add(Restrictions.eq("locale", locale));
        }
        if (StringUtils.isNotEmpty(basename)) {
            query.add(Restrictions.eq("baseName", basename));
        }
        if (StringUtils.isNotEmpty(module)) {
            query.add(Restrictions.eq("moduleName", module));
        }

        return (List<MessageBundleProperty>) query.list();
    }

    @Override
    @Transactional
    public int revertAll(final String locale) {
       Query query = sessionFactory.getCurrentSession().createQuery("update MessageBundleProperty set value = null where locale = :locale");
       query.setString("locale", locale);

        try {
            return query.executeUpdate();
        } catch (Exception e) {
            log.warn("Cound not revert all MessageBundleProperty's " + e.getMessage(), e);
            return 0;
        }
    }

    @Override
    @Transactional
    public int importProperties(List<MessageBundleProperty> properties) {
        int rows = 0;
        for (MessageBundleProperty property : properties) {
            MessageBundleProperty loadedMbp = getProperty(property);
            if (loadedMbp != null) {
                BeanUtils.copyProperties(property, loadedMbp, new String[]{"id"});
                updateMessageBundleProperty(loadedMbp);
            } else {
                updateMessageBundleProperty(property);
            }

            rows++;
        }
        return rows;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllModuleNames() {
        Criteria query = sessionFactory.getCurrentSession().createCriteria(MessageBundleProperty.class)
                .setProjection(Projections.distinct(Projections.property("moduleName")))
                .addOrder(Order.asc("moduleName"));
        query.setCacheable(true);

        List<String> results = (List<String>) query.list();
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllBaseNames() {
        Criteria query = sessionFactory.getCurrentSession().createCriteria(MessageBundleProperty.class)
                .setProjection(Projections.distinct(Projections.property("baseName")))
                .addOrder(Order.asc("baseName"));
        List<String> results = (List<String>) query.list();
        return results;

    }

    @Override
    @Transactional
    public void revert(MessageBundleProperty mbp) {
        if (mbp == null) return;
        mbp.setValue(null);
        try {
            sessionFactory.getCurrentSession().merge(mbp);
        } catch (Exception e) {
            log.warn("Cound not revert MessageBundleProperty {}, {}",  mbp, e.getMessage(), e);
        }
    }

    protected int executeCountQuery(String query) {
        Long count;
        try {
            count = (Long) sessionFactory.getCurrentSession().createQuery(query).uniqueResult();
        } catch (HibernateException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return count.intValue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageBundleProperty> getModifiedProperties(int sortOrder, int sortField, int startingIndex, int pageSize) {
        String orderBy = "asc";
        if (sortOrder == SORT_ORDER_DESCENDING) {
            orderBy = "desc";
        }
        String sortFieldName = "id";
        if (sortField == SORT_FIELD_MODULE) {
            sortFieldName = "moduleName";
        }
        if (sortField == SORT_FIELD_PROPERTY) {
            sortFieldName = "propertyName";
        }
        if (sortField == SORT_FIELD_LOCALE) {
            sortFieldName = "locale";
        }
        if (sortField == SORT_FIELD_BASENAME) {
            sortFieldName = "baseName";
        }
        org.hibernate.Query query = null;
        String queryString = "from MessageBundleProperty where value != null order by " + sortFieldName + " " + orderBy;
        try {
            query = sessionFactory.getCurrentSession().createQuery(queryString);
            query.setFirstResult(startingIndex);
            if (pageSize >= 0) {
                query.setMaxResults(pageSize);
            }
            return query.list();
        } catch (HibernateException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * queues up a call to add or update message bundle data
     *
     * @param baseName
     * @param moduleName
     * @param bundleData
     * @param loc
     */
    protected void queueBundle(String baseName, String moduleName, Map<String, String> bundleData, Locale loc) {
        SaveOrUpdateCall call = new SaveOrUpdateCall();
        call.baseName = baseName;
        call.moduleName = moduleName;
        call.bundleData = bundleData;
        call.loc = loc;
        queue.add(call);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getLocales() {
        return (List<String>) sessionFactory.getCurrentSession().getNamedQuery("findLocales").list();
    }

    public void setScheduleDelay(long scheduleDelay) {
        this.scheduleDelay = scheduleDelay;
    }

    public void setScheduleSaves(boolean scheduleSaves) {
        this.scheduleSaves = scheduleSaves;
    }

    // represents one method call, encapsulate this so we can queue them up
    class SaveOrUpdateCall {
        String baseName;
        String moduleName;
        Map<String, String> bundleData;
        Locale loc;
    }

    /**
     *
     */
    class SaveOrUpdateTask extends TimerTask {

        public SaveOrUpdateTask() {
        }

        /**
         * step through queue and call the real saveOrUpdateInternal method to do the work
         */
        @Override
        public void run() {
            List<SaveOrUpdateCall> queueList = new ArrayList<>(queue);
            transactionTemplate.execute(
                    new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(TransactionStatus status) {
                            for (SaveOrUpdateCall call : queueList) {
                                try {
                                    saveOrUpdateInternal(call.baseName, call.moduleName, call.bundleData, call.loc);
                                } catch (Throwable e) {
                                    log.error("problem saving bundle data:", e);
                                } finally {
                                    queue.remove(call);
                                }
                            }

                        }
                    }
            );
        }
    }
}
