package org.sakaiproject.widget.api.persistence;

import org.sakaiproject.hibernate.CrudRepository;
import org.sakaiproject.widget.model.Widget;

import java.util.List;

public interface WidgetRepository extends CrudRepository<Widget, String> {
    long countWidgetsForSite(String context);
    List<Widget> getWidgetsForSiteWithStatus(String context, Widget.STATUS... statuses);
}
