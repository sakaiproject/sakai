package org.sakaiproject.scheduler.events.hibernate;

import javax.persistence.*;

/**
 * Just maps a context ID, component ID to a quartz trigger UUID.
 */
@Entity(name = "context_mapping")
@Table(uniqueConstraints={@UniqueConstraint(columnNames = {"componentId", "contextId"})})
public class ContextMapping {

    @Id
    // This is the ID of the quartz trigger
    private String uuid;

    // This is the context ID (opaque ID) passed when the job was created.
    private String contextId;

    // This is the component ID, we have this as a separate column so that overlapping context IDs (opaque IDs) aren't
    // a problem.
    private String componentId;

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContextMapping that = (ContextMapping) o;

        if (!uuid.equals(that.uuid)) return false;
        if (!contextId.equals(that.contextId)) return false;
        return componentId.equals(that.componentId);

    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + contextId.hashCode();
        result = 31 * result + componentId.hashCode();
        return result;
    }
}
