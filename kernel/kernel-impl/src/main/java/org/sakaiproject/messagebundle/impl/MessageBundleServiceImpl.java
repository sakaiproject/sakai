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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.messagebundle.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.messagebundle.api.MessageBundleService;
import org.sakaiproject.messagebundle.api.MessageBundleProperty;

import org.hibernate.*;
import org.hibernate.type.NullableType;
import org.hibernate.type.Type;
import org.springframework.beans.BeanUtils;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Responsible for managing the message bundle data in a database.  Provides search capabilities
 * for finding keys based on values.
 *
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Mar 16, 2010
 * Time: 1:38:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class MessageBundleServiceImpl extends HibernateDaoSupport implements MessageBundleService {
    private static Log logger = LogFactory.getLog(MessageBundleServiceImpl.class);

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
     *  Queue of method invocations to save or update message bundle data
     */
    private List queue = Collections.synchronizedList(new ArrayList());

    public void init() {
        timer.schedule(new SaveOrUpdateTask(), 0, scheduleDelay);
    }

    public int getSearchCount(String searchQuery, String module, String baseName, String locale) {
        List<String> values = new ArrayList<String>();
        List<NullableType> types = new ArrayList<NullableType>();
        StringBuffer queryString = new StringBuffer("");

        try {
            if (searchQuery != null && searchQuery.length() > 0) {
                queryString.append("(defaultValue like ? OR value like ? OR propertyName = ?)");
                values.add("%" + searchQuery + "%");
                values.add("%" + searchQuery + "%");
                values.add(searchQuery);
                types.add(Hibernate.STRING);
                types.add(Hibernate.STRING);
                types.add(Hibernate.STRING);
            }
            if (module != null && module.length() > 0) {
                if (queryString.length() > 0) {
                    queryString.append(" AND ");
                }
                queryString.append("moduleName = ? ");
                values.add(module);
                types.add(Hibernate.STRING);

            }
            if (baseName != null && baseName.length() > 0) {
                if (queryString.length() > 0) {
                    queryString.append(" AND ");
                }
                queryString.append("baseName = ?");
                values.add(baseName);
                types.add(Hibernate.STRING);

            }
            if (locale != null && locale.length() > 0) {
                if (queryString.length() > 0) {
                    queryString.append(" AND ");
                }
                queryString.append("locale = ?");
                values.add(locale);
                types.add(Hibernate.STRING);

            }

            if (queryString.length() > 0) {
                queryString.insert(0, "select count(*) from MessageBundleProperty where ");
            } else {
                queryString.insert(0, "select count(*) from MessageBundleProperty");
            }

            Integer count = null;
            try {
                Query query = getSession().createQuery(queryString.toString());
                query.setParameters(values.toArray(), (Type[]) types.toArray(new Type[types.size()]));
                count= (Integer) query.uniqueResult();
            } catch (HibernateException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            return count.intValue();


        } catch (Exception e) {
            logger.error("problem searching the message bundle data", e);
        }
        return 0;
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
    public void saveOrUpdate(String baseName, String moduleName, ResourceBundle newBundle, Locale loc) {
        String keyName = getIndexKeyName(baseName, moduleName, loc.toString());
        if (indexedList.contains(keyName)) {
            logger.debug("skip saveOrUpdate() as its already happened once for :" + keyName);
            return;
        }

        if (scheduleSaves) {
            queueBundle(baseName, moduleName, convertResourceBundleToMap(newBundle),loc);
        } else {
            saveOrUpdateInternal(baseName, moduleName, convertResourceBundleToMap(newBundle), loc.toString());
        }

    }

    public void saveOrUpdate(String baseName, String moduleName, ResourceBundle newBundle, Locale loc, boolean newThread) {
        if (newThread && scheduleSaves) {
            saveOrUpdate(baseName, moduleName, newBundle, loc);
        } else {
            saveOrUpdateInternal(baseName, moduleName, convertResourceBundleToMap(newBundle), loc.toString());
        }
    }

    /**
     * internal work for responding to a save or update request.  This method will add new bundles data
     * if it doesn't exist, otherwise updates the data preserving any current value if its been modified.
     * This approach allows for upgrades to automatically detect and persist new keys, as well as updating
     * any default values that flow in from an upgrade.
     * @param baseName
     * @param moduleName
     * @param newBundle
     * @param loc
     */
    protected void saveOrUpdateInternal(String baseName, String moduleName, Map<String, String> newBundle, String loc) {
        String keyName = getIndexKeyName(baseName, moduleName, loc);
        if (indexedList.contains(keyName)) {
            logger.debug("skip saveOrUpdate() as its already happened once for :" + keyName);
            return;
        }
        
        Set<Entry<String, String>> entrySet = newBundle.entrySet();
        Iterator<Entry<String, String>> entries = entrySet.iterator();

        while (entries.hasNext()) {
        	Entry<String, String> entry = entries.next();  
            String key = entry.getKey();
            MessageBundleProperty mbp = new MessageBundleProperty();
            mbp.setBaseName(baseName);
            mbp.setModuleName(moduleName);
            mbp.setLocale(loc.toString());
            mbp.setPropertyName(key);
            mbp.setDefaultValue(entry.getValue());
            MessageBundleProperty existingMbp = getProperty(mbp);
            if (existingMbp != null) {
                //don"t update id or value, we don't want to loose that data
                BeanUtils.copyProperties(mbp, existingMbp, new String[]{"id","value"});
                logger.debug("updating message bundle data for : " +
                        getIndexKeyName(mbp.getBaseName(),mbp.getModuleName(), mbp.getLocale()));
                updateMessageBundleProperty(existingMbp);
            } else {
                logger.debug("adding message bundle data for : " +
                        getIndexKeyName(mbp.getBaseName(),mbp.getModuleName(), mbp.getLocale()));
                updateMessageBundleProperty(mbp);
            }

        }

        indexedList.add(getIndexKeyName(baseName, moduleName, loc));
    }

    @SuppressWarnings("unchecked")
    public List<MessageBundleProperty> search(String searchQuery, String module, String baseName, String locale) {
        List<String> values = new ArrayList<String>();
        StringBuffer queryString = new StringBuffer("");

        try {
            if (searchQuery != null && searchQuery.length() > 0) {
                queryString.append("(defaultValue like ? OR value like ? OR propertyName = ?)");
                values.add("%" + searchQuery + "%");
                values.add("%" + searchQuery + "%");
                values.add(searchQuery);
            }
            if (module != null && module.length() > 0) {
                if (queryString.length() > 0) {
                    queryString.append(" AND ");
                }
                queryString.append("moduleName = ? ");
                values.add(module);
            }
            if (baseName != null && baseName.length() > 0) {
                if (queryString.length() > 0) {
                    queryString.append(" AND ");
                }
                queryString.append("baseName = ?");
                values.add(baseName);

            }
            if (locale != null && locale.length() > 0) {
                if (queryString.length() > 0) {
                    queryString.append(" AND ");
                }
                queryString.append("locale = ?");
                values.add(locale);
            }

            if (queryString.length() > 0) {
                queryString.insert(0, "from MessageBundleProperty where ");
            } else {
                queryString.insert(0, "from MessageBundleProperty");
            }

            return getHibernateTemplate().find(queryString.toString(), values.toArray() );

        } catch (Exception e) {
            logger.error("problem searching the message bundle data", e);
        }
        return new ArrayList<MessageBundleProperty>();
    }

    private  Map<String, String> convertResourceBundleToMap(ResourceBundle resource) {
        Map<String, String> map = new HashMap<String, String>();

        Enumeration<String> keys = resource.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            map.put(key, resource.getString(key));
        }

        return map;
    }


    public MessageBundleProperty getMessageBundleProperty(long id) {
        return (MessageBundleProperty) getHibernateTemplate().get(MessageBundleProperty.class, id);
    }

    public void updateMessageBundleProperty(MessageBundleProperty mbp) {
	if (mbp.getDefaultValue() == null) { 
	   mbp.setDefaultValue(""); 
	} 
        getHibernateTemplate().saveOrUpdate(mbp);
    }

    public void deleteMessageBundleProperty(MessageBundleProperty mbp) {
        getHibernateTemplate().delete(mbp);
    }

    public MessageBundleProperty getProperty(MessageBundleProperty mbp) {
        Object[] values = new Object[]{mbp.getBaseName(), mbp.getModuleName(), mbp.getPropertyName(), mbp.getLocale()};
        String sql = "from MessageBundleProperty where baseName = ? and moduleName = ? and propertyName = ? and locale = ?";
        List<?> results = getHibernateTemplate().find(sql, values );
        if (results.size() == 0) {
            logger.debug("can't find a default value for : " + mbp);
            return null;
        }
        return (MessageBundleProperty) results.get(0);
    }

    public Map<String,String> getBundle(String baseName, String moduleName, Locale loc) {
        Object[] values = new Object[]{baseName, moduleName, loc.toString()};
        String sql = "from MessageBundleProperty where baseName = ? and moduleName = ? and locale = ? and value != null";
        List<?> results = getHibernateTemplate().find(sql, values );
        Map<String,String> map = new HashMap<String,String>();
        if (results.size() == 0) {
            logger.debug("can't find any overridden values for: " + getIndexKeyName(baseName, moduleName, loc.toString()));
            return map;
        }
        for (Iterator<?> i=results.iterator();i.hasNext();) {
            MessageBundleProperty mbp = (MessageBundleProperty) i.next();
            map.put(mbp.getPropertyName(), mbp.getValue());
        }
        return map;
    }

    private String getIndexKeyName(String baseName, String moduleName, String loc) {
        return moduleName + "_"  + baseName + "_"  +loc;
    }

    public int getModifiedPropertiesCount() {
        String query = "select count(*) from MessageBundleProperty where value != null";
        return executeCountQuery(query);
    }

    @SuppressWarnings("unchecked")
    public List<MessageBundleProperty> getAllProperties(String locale, String module) {
        if ((locale == null || locale.length() == 0) && (module == null || module.length() == 0)) {
            return getHibernateTemplate().find("from MessageBundleProperty");
        } else if (module == null || module.length() == 0) {
            return getHibernateTemplate().find("from MessageBundleProperty where locale = ?", locale);
        } else if (locale == null || locale.length() == 0) {
            return getHibernateTemplate().find("from MessageBundleProperty where moduleName = ?", module);
        } else {
            return getHibernateTemplate().find("from MessageBundleProperty where locale = ? and moduleName = ?",
                    new String[]{locale, module});
        }

    }

    public int revertAll(final String locale) {
        HibernateCallback callback = new HibernateCallback() {
           public Object doInHibernate(org.hibernate.Session session) throws HibernateException, SQLException {
               String hql = "update MessageBundleProperty set value = null where locale = :locale";
               org.hibernate.Query query = session.createQuery(hql);
               query.setString("locale", locale);
               int rowCount = query.executeUpdate();
               return  Integer.valueOf(rowCount);
           }
        };

        try {
          return ((Integer) getHibernateTemplate().execute(callback)).intValue();
        } catch (HibernateObjectRetrievalFailureException e) {
           logger.debug("",e);
        }
        return 0;
    }

    public int importProperties(List<MessageBundleProperty> properties) {
        int rows = 0;
        for (MessageBundleProperty property: properties) {
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

    @SuppressWarnings("unchecked")
    public List<String> getAllModuleNames() {
        List<String> retValue = getHibernateTemplate().find("select distinct(moduleName) from MessageBundleProperty  order by moduleName");
        if (retValue == null) return new ArrayList();
        //force deep load
        retValue.size();
        return retValue;
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllBaseNames() {
         List<String> retValue = getHibernateTemplate().find("select distinct(baseName) from MessageBundleProperty  order by baseName");
        if (retValue == null) return new ArrayList();
        //force deep load
        retValue.size();
        return retValue;

    }

    public void revert(MessageBundleProperty mbp) {
        mbp.setValue(null);
        getHibernateTemplate().update(mbp);
    }

    protected int executeCountQuery(String query) {
      Integer count = null;
      try {
         count = (Integer) getSession().createQuery(query).uniqueResult();
      } catch (HibernateException e) {
         throw new RuntimeException(e.getMessage(),e);
      }
      return count.intValue();
   }

    @SuppressWarnings("unchecked")
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
            query = getSession().createQuery(queryString);
            query.setFirstResult(startingIndex);
            query.setMaxResults(pageSize);
            return query.list();
        } catch (HibernateException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    /**
        * queues up a call to add or update message bundle data
        * @param baseName
        * @param moduleName
        * @param bundleData
        * @param loc
        */
       protected void queueBundle(String baseName, String moduleName, Map<String,String> bundleData, Locale loc) {
           SaveOrUpdateCall call = new SaveOrUpdateCall();
           call.baseName =baseName;
           call.moduleName = moduleName;
           call.bundleData = bundleData;
           call.loc = loc;
           queue.add(call);
       }

       @SuppressWarnings("unchecked")
       public List<String> getLocales() {
           return getHibernateTemplate().find("select distinct(locale) from MessageBundleProperty");
       }

       public void setScheduleDelay(long scheduleDelay) {
           this.scheduleDelay = scheduleDelay;
       }

       // represents one method call, encapsulate this so we can queue them up
       class SaveOrUpdateCall {
               String baseName;
               String moduleName;
               Map<String,String> bundleData;
               Locale loc;
           }

       /**
        *
        */
       class SaveOrUpdateTask extends TimerTask {

           public SaveOrUpdateTask(){
           }

           /**
            * step through queue and call the real saveOrUpdateInternal method to do the work
            */
           public void run() {
               List queueList = new ArrayList(queue);
               for (Iterator i = queueList.iterator(); i.hasNext(); ) {
                   SaveOrUpdateCall call = (SaveOrUpdateCall) i.next();
                   try {
                       saveOrUpdateInternal(call.baseName, call.moduleName, call.bundleData, call.loc.toString());
                   } catch (Throwable e) {
                       logger.error("problem saving bundle data:", e);
                   } finally {
                       queue.remove(call);
                   }
               }

           }
       }
   }

