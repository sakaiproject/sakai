package org.sakaiproject.rubrics.logic.model;

public interface Modifiable {
    Long getId();
    Metadata getModified();
    void setModified(Metadata metadata);
}
