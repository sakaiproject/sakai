/**
 * EventReceiverCoordinator.java - created by antranig on 15 May 2007
 */

package org.sakaiproject.entitybroker.impl.event;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import org.sakaiproject.entitybroker.event.EventReceiver;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Collects beans implementing {@link EventReceiver} from around the context, and distributes
 * matching events to them.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class EventReceiverCoordinator implements ApplicationContextAware {

   private Map<EventReceiver, Boolean> receivers = new ConcurrentHashMap<EventReceiver, Boolean>();

   EventTrackingService eventTrackingService;

   public void setEventTrackingService(EventTrackingService eventTrackingService) {
      this.eventTrackingService = eventTrackingService;
   }

   public void init() {
      eventTrackingService.addLocalObserver(new Observer() {

         public void update(Observable o, Object arg) {
            if (!(arg instanceof Event))
               return;
            Event event = (Event) arg;
            handleEvent(event);
         }
      });
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
    */
   public void setApplicationContext(ApplicationContext context) throws BeansException {
      String[] autobeans = context.getBeanNamesForType(EventReceiver.class, false, false);
      for (String autobean : autobeans) {
         EventReceiver register = (EventReceiver) context.getBean(autobean);
         receivers.put(register, Boolean.TRUE);
      }
   }

   /**
    * @param receiver
    * @param event
    * @return
    */
   private boolean match(EventReceiver receiver, Event event) {
      String name = event.getEvent();
      String[] prefixes = receiver.getEventNamePrefixes();

      boolean matchprefix = false;
      if (prefixes == null) {
         matchprefix = true;
      } else {
         for (String prefix : prefixes) {
            if (name.startsWith(prefix)) {
               matchprefix = true;
               break;
            }
         }
      }
      if (!matchprefix)
         return false;
      String resprefix = receiver.getResourcePrefix();
      if (resprefix == null || event.getResource().startsWith(resprefix)) {
         return true;
      } else
         return false;
   }

   /**
    * @param event
    */
   protected void handleEvent(Event event) {
      for (EventReceiver receiver : receivers.keySet()) {
         if (match(receiver, event)) {
            receiver.receiveEvent(event.getEvent(), event.getResource());
         }
      }
   }

}
