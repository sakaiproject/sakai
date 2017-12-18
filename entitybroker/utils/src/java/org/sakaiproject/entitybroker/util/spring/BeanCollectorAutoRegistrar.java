/**
 * $Id$
 * $URL$
 * BeanCollectorAutoRegistrar.java - entity-broker - Apr 15, 2008 4:29:18 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.util.spring;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

import org.sakaiproject.entitybroker.collector.AutoRegister;
import org.sakaiproject.entitybroker.collector.BeanCollector;
import org.sakaiproject.entitybroker.collector.OrderedBean;
import org.sakaiproject.entitybroker.collector.BeanMapCollector;

/**
 * This will collect the autoregistered beans and place them into all the locations which requested them
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@Slf4j
public class BeanCollectorAutoRegistrar implements ApplicationListener, ApplicationContextAware, InitializingBean {

   private ApplicationContext applicationContext;
   public void setApplicationContext(ApplicationContext applicationContext) {
      this.applicationContext = applicationContext;
   }

   public void onApplicationEvent(ApplicationEvent event) {
      // We do not actually respond to any events, purpose is to time initialisation only.
   }

   private Set<String> autoRegistered; 
   public void init() {
      String[] autobeans = applicationContext.getBeanNamesForType(AutoRegister.class, false, false);
      autoRegistered = new HashSet<String>();
      for (int i = 0; i < autobeans.length; i++) {
         autoRegistered.add(autobeans[i]);
      }
   }


   public void afterPropertiesSet() throws Exception {
      log.debug("setAC: " + applicationContext.getDisplayName());
      ConfigurableApplicationContext cac = (ConfigurableApplicationContext) applicationContext;
      ConfigurableBeanFactory cbf = (ConfigurableBeanFactory) cac.getBeanFactory();

      cbf.addBeanPostProcessor(new BeanPostProcessor() {
         @SuppressWarnings("unchecked")
         public Object postProcessBeforeInitialization(Object bean, String beanName) {
            if (bean instanceof BeanCollector<?>) {
               BeanCollector<Object> bc = (BeanCollector<Object>) bean;
               Class<?> c = bc.getCollectedType();
               if (c == null) {
                  throw new IllegalArgumentException("collected type cannot be null");
               }

               List<Object> l = getAutoRegisteredBeansOfType(c);
               logCollectedBeanInsertion(beanName, c.getName(), l);
               bc.setCollectedBeans(l);
            } else if (bean instanceof BeanMapCollector) {
               BeanMapCollector bc = (BeanMapCollector) bean;
               Class<?>[] cArray = bc.getCollectedTypes();
               if (cArray == null) {
                  throw new IllegalArgumentException("collected types cannot be null");
               }

               Map<Class<?>, List<?>> collectedBeans = new HashMap<Class<?>, List<?>>();
               for (int i = 0; i < cArray.length; i++) {
                  List<Object> l = getAutoRegisteredBeansOfType(cArray[i]);
                  logCollectedBeanInsertion(beanName, cArray[i].getName(), l);
                  collectedBeans.put(cArray[i], l);
               }
               bc.setCollectedBeansMap(collectedBeans);
            }
            return bean;
         }

         public Object postProcessAfterInitialization(Object bean, String beanName) {
            return bean;
         }
      });

   }


   /**
    * Get all autoregistered beans of a specific type
    * @param c the class type
    * @return a list of the matching beans
    */
   private List<Object> getAutoRegisteredBeansOfType(Class<?> c) {
      String[] autobeans = applicationContext.getBeanNamesForType(c, false, false);
      List<Object> l = new ArrayList<Object>();
      for (String autobean : autobeans) {
         if (autoRegistered.contains(autobean)) {
            Object bean = applicationContext.getBean(autobean);
            l.add(bean);
         }
      }
      Collections.sort(l, new OrderComparator());
      return l;
   }

   /**
    * Generate a log message about the collected insertion
    * @param beanName the name of the bean getting collected beans inserted into it
    * @param l the list of beans that were inserted
    */
   private void logCollectedBeanInsertion(String beanName, String beanType, List<Object> l) {
      StringBuilder registeredBeans = new StringBuilder();
      registeredBeans.append("[");
      for (int i = 0; i < l.size(); i++) {
         if (i > 0) {
            registeredBeans.append(",");
         }
         registeredBeans.append(l.get(i).getClass().getName());
      }
      registeredBeans.append("]");
      log.info("Set collected beans of type ("+beanType+") on bean ("+beanName+") to ("+l.size()+") " + registeredBeans.toString());
   }

   /**
    * Comparator to order the collected beans based on order or use default order otherwise
    * 
    * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
    */
   public static class OrderComparator implements Comparator<Object>, Serializable {
      public final static long serialVersionUID = 1l;
      public int compare(Object arg0, Object arg1) {
         if (arg0 instanceof OrderedBean &&
               arg1 instanceof OrderedBean) {
            return ((OrderedBean)arg0).getOrder() - ((OrderedBean)arg1).getOrder();
         } else if (arg0 instanceof OrderedBean) {
            return -1;
         } else if (arg1 instanceof OrderedBean) {
            return 1;
         } else {
            return 0;
         }
      }      
   }

}
