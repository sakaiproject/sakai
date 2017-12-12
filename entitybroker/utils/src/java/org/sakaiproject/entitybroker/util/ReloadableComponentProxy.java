/**
 * Copyright (c) 2007-2016 The Apereo Foundation
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
package org.sakaiproject.entitybroker.util;


import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;

import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.beans.factory.InitializingBean;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * WARNING: Requires Spring 1.2.8 or newer libraries in the classpath <br/>
 * Allows you to define a Sakai component in a webapp so it can be reloaded,
 * this makes development easier since it does not require all of Sakai to be restarted
 * and yet allows you to expose beans to the Sakai component manager<br/>
 * <b>WARNING:</b> This is currently experimental as your bean will not be able to be found by the component
 * manager while things are starting up (since your webapp will not have loaded up yet),
 * it is basically only useful for development and some testing right now
 * <br/>
 * How to use:<br/>
 * 1) Create a bean for the service you want to proxy in your webapp application context (example: myLocalBean)<br/>
 * 2) Create a bean in your webapp like so:
<xmp><bean class="org.sakaiproject.entitybroker.util.ReloadableComponentProxy">
     <property name="proxyInterfaces" value="org.sakaiproject.myproject.MyService" />
     <property name="sakaiComponentName" value="org.sakaiproject.myproject.MyService" />
     <property name="localSakaiComponentBean" ref="myLocalBeanName" />
</bean></xmp><br/>
 * 3) Put the interface for your service into shared (this has to be the same interface you are registering in proxyInterfaces)<br/>
 * 4) Use {@link ComponentManager#get(Class)} to load up the proxied bean in the service/thing that is using your service
 * at the point where it is used (not in the init or it will fail):
<xmp>if (webappService == null) {
     webappService = (MyService) ComponentManager.get(MyService.class);
}</xmp>
 * That's it. Good luck.<br/>
 * 
 * @author Steven Githens (sgithens@caret.cam.ac.uk)
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@Slf4j
public class ReloadableComponentProxy extends ProxyFactoryBean implements InitializingBean {

   private String sakaiComponentName;
   /**
    * @param sakaiComponentName (optional) the name to use for this bean in the component manager,
    * if unset then this will use the name of the registered proxyInterface
    */
   public void setSakaiComponentName(String sakaiComponentName) {
      this.sakaiComponentName = sakaiComponentName;
   }

   private Object localSakaiComponentBean;
   /**
    * @param localSakaiComponentBean this is the bean which you want to expose via the proxy
    */
   public void setLocalSakaiComponentBean(Object localSakaiComponentBean) {
      this.localSakaiComponentBean = localSakaiComponentBean;
   }

   public void afterPropertiesSet() throws Exception {
      // run this when the bean is loaded

      // auto set the name if it is not set
      if (sakaiComponentName == null || "".equals(sakaiComponentName)) {
         Class<?>[] interfaces = getProxiedInterfaces();
         if (interfaces.length > 0) {
            sakaiComponentName = interfaces[0].getName();
            log.info("Autogenerating component name from interface: " + sakaiComponentName);
         }
      }

      // get the component from the Sakai CM if it is in there
      Object obj = ComponentManager.get(sakaiComponentName);

      /*
       * If the obj is null, that means this is the first time we have loaded
       * the component, so we will add it to the Component Manager. If this
       * component is already available from the component manager, then we
       * will simply update it's proxy target.
       */
      if (obj == null) {
         this.setTargetSource(new HotSwappableTargetSource(localSakaiComponentBean));  
         ComponentManager.loadComponent(sakaiComponentName, this);
      }
      else {
         try {
            Method getTargetSource = obj.getClass().getMethod("getTargetSource");              
            HotSwappableTargetSource hsts = (HotSwappableTargetSource) getTargetSource.invoke(obj);
            hsts.swap(localSakaiComponentBean);
         } catch (Exception e) {
            log.error("Unable to update reloadable SakaiComponent: " + sakaiComponentName, e);
         }
      }
      log.info("Added component proxy from webapp to component manager: " + sakaiComponentName);
   }
}
