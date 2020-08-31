package org.sakaiproject.messaging.api;

import java.util.List;

public interface MessagingService {

    public void listen(String topic, MessageListener listener);
    public void send(String topic, BullhornAlert ba);

    /**
     * @param userId The user to retrieve alerts for
     * @return the list of current alerts for the specified user
     */
    public List<BullhornAlert> getAlerts(String userId);

    /**
     * @param userId The user to clear the alert for
     * @param alertId The alert to clear
     * @return boolean to indicate success
     */
    public boolean clearAlert(String userId, long alertId);

    /**
     * @param userId The user to clear the alerts for
     * @return boolean to indicate success
     */
    public boolean clearAllAlerts(String userId);

}
