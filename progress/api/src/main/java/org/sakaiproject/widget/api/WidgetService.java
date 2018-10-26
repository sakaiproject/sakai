package org.sakaiproject.widget.api;

import org.sakaiproject.widget.model.Widget;

import java.util.Optional;
import java.util.Set;

public interface WidgetService {
    void addWidget(Widget widget);
    void updateWidget(Widget widget);
    Optional<Widget> getWidget(String id);
    void deleteWidget(String id);
    Set<Widget> getWidgetsForSiteWithStatus(String site, Widget.STATUS... statuses);
}
