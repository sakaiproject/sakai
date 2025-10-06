# Quick Reference: Generic DAO to Spring Repository Migration Checklist

A condensed checklist for migrating Sakai components from Generic DAO to Spring Repository pattern.

## Prerequisites

- [ ] Identify the entity class(es) to migrate
- [ ] Locate the existing DAO interface and implementation
- [ ] Locate the logic/service class using the DAO
- [ ] List all consumers (controllers, jobs, etc.)

## Step 1: Update Entity

- [ ] Add `@Entity` annotation
- [ ] Add `@Table(name = "TABLE_NAME")` (match HBM exactly)
- [ ] Add `implements PersistableEntity<ID_TYPE>`
- [ ] Add `@Id` with generation strategy
- [ ] Add `@SequenceGenerator` if using sequences (match HBM)
- [ ] Add `@Column` for all persistent fields (match HBM names)
- [ ] Add `@Temporal(TemporalType.TIMESTAMP)` for Date fields
- [ ] Add `@Transient` for non-persistent fields
- [ ] Keep HBM file for verification

## Step 2: Create Repository Interface

**Location:** `{module}-api/src/java/.../repository/`

```java
public interface EntityRepository extends SpringCrudRepository<Entity, IdType> {
    // Custom query methods
    Optional<Entity> findByField(String field);
    List<Entity> findByAnotherField(Integer field);
}
```

- [ ] Create interface extending `SpringCrudRepository<T, ID>`
- [ ] Add custom query methods with clear names
- [ ] Use `Optional<T>` for single results
- [ ] Use `List<T>` for multiple results

## Step 3: Create Repository Implementation

**Location:** `{module}-impl/src/java/.../repository/impl/`

```java
@Repository
public class EntityRepositoryImpl
    extends SpringCrudRepositoryImpl<Entity, IdType>
    implements EntityRepository {

    @Override
    public Optional<Entity> findByField(String field) {
        if (field == null) return Optional.empty();

        Criteria criteria = startCriteriaQuery();
        criteria.add(Restrictions.eq("fieldName", field));

        Entity result = (Entity) criteria.uniqueResult();
        return Optional.ofNullable(result);
    }
}
```

- [ ] Extend `SpringCrudRepositoryImpl<T, ID>`
- [ ] Implement your repository interface
- [ ] Add `@Repository` annotation
- [ ] Implement custom queries using `startCriteriaQuery()`
- [ ] Handle null parameters gracefully

## Step 4: Create Service Interface

**Location:** `{module}-api/src/java/.../service/`

- [ ] Create new service interface (or keep existing logic interface name)
- [ ] Copy method signatures from old logic interface
- [ ] Maintain backward compatibility

## Step 5: Create Service Implementation

**Location:** `{module}-impl/src/java/.../service/impl/`

```java
@Service
public class EntityServiceImpl implements EntityService {

    @Setter private EntityRepository repository;
    // ... other dependencies

    public void init() {
        // Initialization
    }

    @Override
    public Entity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    // Business logic methods...
}
```

- [ ] Add `@Service` annotation
- [ ] Inject `repository` dependency
- [ ] Copy business logic from old implementation
- [ ] Replace DAO calls with repository calls
- [ ] Keep `init()` method if needed

## Step 6: Update Spring Configuration

**File:** `components.xml`

```xml
<!-- Repository bean -->
<bean id="com.example.repository.EntityRepository"
      class="com.example.repository.impl.EntityRepositoryImpl">
    <property name="sessionFactory"
        ref="org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory" />
</bean>

<!-- Service bean -->
<bean id="com.example.service.EntityService"
      class="com.example.service.impl.EntityServiceImpl"
      init-method="init">
    <property name="repository" ref="com.example.repository.EntityRepository" />
    <!-- ... other dependencies -->
</bean>
```

- [ ] Add repository bean definition
- [ ] Add service bean definition
- [ ] Update job beans to reference new service
- [ ] Remove old DAO bean definitions
- [ ] Remove separate hibernate XML if consolidated

## Step 7: Update All Consumers

For each controller, job, or other class using the old service:

- [ ] Update import statements
- [ ] Change `private OldLogic oldLogic` to `private NewService newService`
- [ ] Update all method calls (if names changed)
- [ ] Test the consumer

**Pattern:**
```java
// Before
@Autowired
private ValidationLogic validationLogic;

validationLogic.getById(id);

// After
@Autowired
private EntityService entityService;

entityService.getById(id);
```

## Step 8: Remove Old Code

- [ ] Remove old DAO interface
- [ ] Remove old DAO implementation
- [ ] Remove old logic interface (if replaced)
- [ ] Remove old logic implementation
- [ ] Remove separate hibernate XML files (if consolidated)
- [ ] Keep HBM file for verification

## Step 9: Verify and Test

- [ ] Compare JPA annotations with HBM file
  - [ ] Table name
  - [ ] Column names
  - [ ] Column types
  - [ ] Nullable constraints
  - [ ] Field lengths
  - [ ] Sequence name
- [ ] Run integration tests
- [ ] Test all custom queries
- [ ] Test CRUD operations
- [ ] Test business logic
- [ ] Test jobs/scheduled tasks
- [ ] Test controllers/endpoints

## Step 10: Final Cleanup

- [ ] Delete HBM file after verification
- [ ] Remove HBM import from components.xml if no other mappings
- [ ] Update documentation
- [ ] Commit changes

## Common Patterns

### Query Migration

**Old:**
```java
Search search = new Search();
Restriction rest = new Restriction("field", value);
search.addRestriction(rest);
List<Entity> list = dao.findBySearch(Entity.class, search);
return list.size() > 0 ? list.get(0) : null;
```

**New:**
```java
return repository.findByField(value).orElse(null);
```

### Save/Update Migration

**Old:**
```java
dao.save(entity);
```

**New:**
```java
repository.save(entity);
```

### Delete Migration

**Old:**
```java
dao.delete(entity);
```

**New:**
```java
repository.delete(entity);
```

### Find by ID Migration

**Old:**
```java
Search search = new Search();
search.addRestriction(new Restriction("id", id));
List<Entity> list = dao.findBySearch(Entity.class, search);
return list.size() > 0 ? list.get(0) : null;
```

**New:**
```java
return repository.findById(id).orElse(null);
```

## Common Pitfalls

❌ **Don't:**
- Mix query logic in service layer
- Forget `@Transient` on non-persistent fields
- Mismatch table/column names between JPA and HBM
- Use wrong sequence name
- Delete HBM before verification
- Remove `init()` method if it has logic

✅ **Do:**
- Keep queries in repository layer
- Return `Optional` from repository methods
- Handle null parameters in custom queries
- Use `startCriteriaQuery()` for complex queries
- Test thoroughly before removing old code
- Maintain backward compatibility in service methods

## File Structure Template

```
{module}-api/src/java/com/example/
├── model/
│   └── Entity.java (add JPA annotations)
├── repository/
│   └── EntityRepository.java (NEW)
└── service/
    └── EntityService.java (NEW or renamed)

{module}-impl/src/java/com/example/
├── repository/impl/
│   └── EntityRepositoryImpl.java (NEW)
└── service/impl/
    └── EntityServiceImpl.java (NEW or migrated)

{module}-impl/src/webapp/WEB-INF/
└── components.xml (UPDATE)
```

## Key Imports

**Entity:**
```java
import javax.persistence.*;
import org.sakaiproject.springframework.data.PersistableEntity;
```

**Repository:**
```java
import org.sakaiproject.springframework.data.SpringCrudRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import java.util.Optional;
```

**Service:**
```java
import org.springframework.stereotype.Service;
import lombok.Setter;
```

## Time Estimates

- **Simple entity** (1-2 queries): 2-3 hours
- **Medium entity** (3-5 queries): 4-6 hours
- **Complex entity** (6+ queries, many consumers): 1-2 days

## Resources

- Full migration guide: `MIGRATION_GENERIC_DAO_TO_SPRING_REPOSITORY.md`
- Sakai kernel: `kernel/api/src/main/java/org/sakaiproject/springframework/data/`
- Example: Account Validator migration (this project)

---

**Quick Tip:** Start with a simple entity first to learn the pattern, then apply to more complex entities.
