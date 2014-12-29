package org.sakaiproject.search.elasticsearch;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationAction;
import org.w3c.dom.Element;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 10/31/12
 * Time: 12:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchNotificationAction implements NotificationAction {
    private ElasticSearchIndexBuilder searchIndexBuilder;

    public SearchNotificationAction(ElasticSearchIndexBuilder searchIndexBuilder){
        this.searchIndexBuilder = searchIndexBuilder;
    }

    @Override
    public void set(Element element) {
    }

    @Override
    public void set(NotificationAction notificationAction) {
    }

    @Override
    public NotificationAction getClone() {
        return new SearchNotificationAction(searchIndexBuilder);
    }

    @Override
    public void toXml(Element element) {
    }

    @Override
    public void notify(Notification notification, Event event) {
         searchIndexBuilder.addResource(notification, event);
    }


}
