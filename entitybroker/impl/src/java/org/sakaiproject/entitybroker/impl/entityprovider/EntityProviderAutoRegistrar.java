/**
 * EntityProviderAutoRegistrar.java - created by antranig on 17 May 2007
 */

package org.sakaiproject.entitybroker.impl.entityprovider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Finds and registers any {@link EntityProvider} implementation which also implements
 * {@link AutoRegisterEntityProvider}
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityProviderAutoRegistrar implements ApplicationContextAware {

   private static Log log = LogFactory.getLog(EntityProviderAutoRegistrar.class);

   EntityProviderManager entityProviderManager;
   public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
      this.entityProviderManager = entityProviderManager;
   }

   public void init() {
      log.info("init");
   }

   public void setApplicationContext(ApplicationContext context) throws BeansException {
      log.debug("setAC: " + context.getDisplayName());
      String[] autobeans = context.getBeanNamesForType(AutoRegisterEntityProvider.class, false, false);
      StringBuilder registeredPrefixes = new StringBuilder();
      for (String autobean : autobeans) {
         AutoRegisterEntityProvider register = (AutoRegisterEntityProvider) context
               .getBean(autobean);
         if (register.getEntityPrefix() == null || register.getEntityPrefix().equals("")) {
            // should this die here or is this error log enough? -AZ
            log.error("Could not autoregister EntityProvider because the enity prefix is null or empty string for class: "
                        + register.getClass().getName());
         } else {
            registeredPrefixes.append(" : " + register.getEntityPrefix());
            entityProviderManager.registerEntityProvider(register);
         }
      }
      log.info("AutoRegistered EntityProvider prefixes " + registeredPrefixes);
      // TODO - deal with de-registration in the case we ever support dynamic contexts.
   }

}
