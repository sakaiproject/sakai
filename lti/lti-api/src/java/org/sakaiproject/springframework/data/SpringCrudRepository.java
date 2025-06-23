package org.sakaiproject.springframework.data;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Interface for CRUD operations in Sakai.
 *
 * @param <T> the entity type
 * @param <ID> the id type
 */
public interface SpringCrudRepository<T, ID extends Serializable> {

    /**
     * Saves the given entity.
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    T save(T entity);

    /**
     * Saves all the given entities.
     *
     * @param entities the entities to save
     * @return the saved entities
     */
    Iterable<T> saveAll(Iterable<T> entities);

    /**
     * Finds the entity by id.
     *
     * @param id the id
     * @return the entity
     */
    Optional<T> findById(ID id);

    /**
     * Checks if the entity with the given id exists.
     *
     * @param id the id
     * @return true if the entity exists
     */
    boolean existsById(ID id);

    /**
     * Finds all entities.
     *
     * @return all entities
     */
    List<T> findAll();

    /**
     * Finds all entities with the given ids.
     *
     * @param ids the ids
     * @return the entities with the given ids
     */
    Iterable<T> findAllById(Iterable<ID> ids);

    /**
     * Returns the number of entities.
     *
     * @return the number of entities
     */
    long count();

    /**
     * Deletes the entity with the given id.
     *
     * @param id the id
     */
    void deleteById(ID id);

    /**
     * Deletes the given entity.
     *
     * @param entity the entity to delete
     */
    void delete(T entity);

    /**
     * Deletes all the given entities.
     *
     * @param entities the entities to delete
     */
    void deleteAll(Iterable<T> entities);

    /**
     * Deletes all entities.
     */
    void deleteAll();
}