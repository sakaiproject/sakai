package org.sakaiproject.springframework.data;

import java.io.Serializable;

/**
 * Interface for persistable entities.
 *
 * @param <ID> the id type
 */
public interface PersistableEntity<ID extends Serializable> {

    /**
     * Gets the id.
     *
     * @return the id
     */
    ID getId();

    /**
     * Sets the id.
     *
     * @param id the id
     */
    void setId(ID id);
}