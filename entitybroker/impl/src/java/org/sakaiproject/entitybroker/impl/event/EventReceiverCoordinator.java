/**
 * $Id$
 * $URL$
 * EBlogic.java - entity-broker - Apr 15, 2008 4:29:18 PM - azeckoski
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

package org.sakaiproject.entitybroker.impl.event;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.azeckoski.reflectutils.refmap.ReferenceMap;
import org.azeckoski.reflectutils.refmap.ReferenceType;
import org.sakaiproject.entitybroker.event.EventReceiver;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Collects beans implementing {@link EventReceiver} from around the context, and distributes
 * matching events to them.<br/>
 * FIXME - make a way to handle events by allowing manual registration of receivers,
 * the way this works now the event receivers will not be registered from webapps, bad bad
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Aaron Zeckoski (azeckoski at gmail.com)
 */
public class EventReceiverCoordinator implements ApplicationContextAware {

    private Map<ClassLoader, EventReceiver> receivers = new ReferenceMap<ClassLoader, EventReceiver>(ReferenceType.WEAK, ReferenceType.STRONG);

    EventTrackingService eventTrackingService;
    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public void init() {
        // register a single observer for the EB system (switched from local observer)
        eventTrackingService.addObserver(new Observer() {
            public void update(Observable o, Object arg) {
                if (arg instanceof Event) {
                    Event event = (Event) arg;
                    handleEvent(event);
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        String[] autobeans = context.getBeanNamesForType(EventReceiver.class, false, false);
        for (String autobean : autobeans) {
            EventReceiver receiver = (EventReceiver) context.getBean(autobean);
            if (receiver != null) {
                receivers.put(receiver.getClass().getClassLoader(), receiver);
            }
        }
    }

    /**
     * Called when events occur which come in from the event system
     * @param event the event from the system
     */
    protected void handleEvent(Event event) {
        for (EventReceiver receiver : receivers.values()) {
            if (match(receiver, event)) {
                receiver.receiveEvent(event.getEvent(), event.getResource());
            }
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
        if (!matchprefix) {
            return false;
        }
        String resprefix = receiver.getResourcePrefix();
        if (resprefix == null 
                || event.getResource().startsWith(resprefix)) {
            return true;
        } else {
            return false;
        }
    }

}
