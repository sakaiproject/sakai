package org.sakaiproject.widget.impl;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.widget.api.WidgetService;
import org.sakaiproject.widget.api.persistence.WidgetRepository;
import org.sakaiproject.widget.model.Widget;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * This is main service for widgets, all the buisiness logic about widgets should
 * exist here. The service should try to have unit tests that on every method in
 * its api. The service will be used by the tool and possibly from other services.
 */
@Transactional
public class WidgetServiceImpl implements WidgetService {

    @Setter private WidgetRepository widgetRepository;

    public void init() {

    }

    @Override
    public Set<Widget> getWidgetsForSiteWithStatus(String site, Widget.STATUS... statuses) {
        if (StringUtils.isBlank(site)) {
            return Collections.emptySet();
        }
        return new HashSet<>(widgetRepository.getWidgetsForSiteWithStatus(site, statuses));
    }

    @Override
    public void addWidget(Widget widget) {
        Objects.requireNonNull(widget);

        if (widget.getId() != null) {
            throw new IllegalArgumentException("Can't persist a new widget with a non null id.");
        }
        if (widget.getContext() == null) {
            throw new IllegalArgumentException("Widget must have a context.");
        }

        widget.setStatus(Widget.STATUS.UNLOCKED);
        Instant now = Instant.now();
        widget.setDateCreated(now);
        widget.setDateModified(now);

        // Save the widget and verify
        Widget saved = widgetRepository.save(widget);
        if (saved == null || StringUtils.isBlank(saved.getId())) {
            throw new IllegalArgumentException("Failed to persist the widget: " + widget.toString());
        }
    }

    @Override
    public void updateWidget(Widget widget) {
        Objects.requireNonNull(widget);
        widget.setDateModified(Instant.now());
        widgetRepository.update(widget);
    }

    @Override
    public Optional<Widget> getWidget(String id) {
        if (StringUtils.isBlank(id)) return Optional.empty();
        return Optional.ofNullable(widgetRepository.findOne(id));
    }

    @Override
    public void deleteWidget(String id) {
        widgetRepository.delete(id);
    }
}
