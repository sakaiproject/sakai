package org.sakaiproject.basiclti;

import org.sakaiproject.event.cover.EventTrackingService;

public class LocalEventTrackingService {
    /*
    public static org.sakaiproject.event.api.Event newEvent(java.lang.String param0, java.lang.String param1, java.lang.String param3, boolean param3, int param4) {
        //For 2.6
        return EventTrackingService.newEvent(param0,param1,param2,param3,param4);
    }
    */
    public static org.sakaiproject.event.api.Event newEvent(java.lang.String param0, java.lang.String param1, java.lang.String param2, boolean param3, int param4) {
        //For 2.5
        return EventTrackingService.newEvent(param0,param1,param3);
    }

    public static void post(org.sakaiproject.event.api.Event param0) {
        EventTrackingService.post(param0);
    }
}
