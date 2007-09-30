/**
 * AutoRegisterEntityProvider.java - created by aaronz on 11 May 2007
 */

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import org.sakaiproject.entitybroker.collector.AutoRegister;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * By implementing this interface you are telling the {@link EntityProviderManager} to register this
 * entity broker as soon as spring creates it, to be exposed as part of the {@link EntityBroker}
 * <br/> This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public interface AutoRegisterEntityProvider extends EntityProvider, AutoRegister {

   // no methods (this space intentionally left blank -AZ)

}
