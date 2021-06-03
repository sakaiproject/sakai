package org.sakaiproject.springframework.data;

/**
 * If you are using the SpringCrudRepositoryImpl for your JPA persistence, you need
 * to implement this interface in your entity beans. This allows the repo code to work out
 * if an entity is new or not, based on its id. If you are using Lombok in your entity bean and
 * you have an attribute of "id", then you'll have the impl already and you just need to add 
 * <code>implements PersistableEntity</code> with the type of your id field. For an example, take
 * a look at Task and UserTask in the kernel api.
 */
public interface PersistableEntity<T> {
    T getId();
}
