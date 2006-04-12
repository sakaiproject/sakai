/**
 * 
 */
package uk.ac.cam.caret.sakai.rwiki.utils;

import org.sakaiproject.service.framework.log.Logger;

/**
 * @author ieb
 *
 */
public class TimeLogger {
    
    private static final TimeLogger instance = new TimeLogger();
    private TimeLogger() {
    }
    private Logger log = null;
    private boolean logFullResponse = false; 
    private boolean logResponse = false; 
    
    public static void printTimer(String name, long start, long end) {
        if ( instance.log == null ) {
            instance.log = org.sakaiproject.service.framework.log.cover.Logger.getInstance();
        }
        if ( instance.log == null ) return;
        if ( instance.logFullResponse ) {
            instance.log.info("TIMER:"+name+";"+(end-start)+";"+end+";");
        } else {
            instance.log.debug("TIMER:"+name+";"+(end-start)+";"+end+";");
        }
    }
    
    
    /**
     * @return Returns the logFullResponse.
     */
    public static boolean getLogFullResponse() {
        return instance.logFullResponse;
    }
    
    /**
     * @param logFullResponse The logFullResponse to set.
     */
    public static void setLogFullResponse(boolean logFullResponse) {
        instance.logFullResponse = logFullResponse;
    }
    /**
     * @return Returns the logResponse.
     */
    public static boolean getLogResponse() {
        return instance.logResponse;
    }
    
    /**
     * @param logResponse The logResponse to set.
     */
    public static void setLogResponse(boolean logResponse) {
        instance.logResponse = logResponse;
    }
}





