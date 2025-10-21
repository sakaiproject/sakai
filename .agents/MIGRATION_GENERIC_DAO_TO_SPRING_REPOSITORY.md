# Migration from Generic DAO to Spring Repository Pattern

**Date:** 2025-10-06
**Migration Type:** Generic DAO → Spring Repository (SpringCrudRepositoryImpl)

## Overview

This document describes the migration from Sakai's deprecated Generic DAO pattern to Spring's Repository pattern using `SpringCrudRepositoryImpl` located in the Sakai kernel.

## Background

Tools using Generic DAO:
- **Generic DAO Pattern**: `HibernateCompleteGenericDao` with search-based queries
- **Mixed Concerns**: Business logic and data access queries in the DAO class
- **XML Configuration**: Hibernate XML mappings for entities

The migration modernizes this to:
- **Spring Repository Pattern**: `SpringCrudRepositoryImpl` with typed repositories
- **Separation of Concerns**: Service layer for business logic (a class ending in `Service`), repository layer for queries (a class ending in `Repository`)
- **JPA Annotations**: Modern JPA annotations on entity classes

## Architecture Changes

### Old Architecture

```
Controllers/Jobs
      ↓
SomeLogic (interface) optional
      ↓
SomeLogicImpl (mixed business logic + queries) optional
      ↓
SomeDao (extends GeneralGenericDao)
      ↓
SomeLogicDao (extends HibernateCompleteGenericDao)
      ↓
Database (via Hibernate XML mapping)
```

### New Architecture

```
Controllers/Jobs
      ↓
SomeService (interface)
      ↓
SomeServiceImpl (business logic only)
      ↓
SomeRepository (extends SpringCrudRepository<T,ID>)
      ↓
SomeRepositoryImpl (extends SpringCrudRepositoryImpl)
      ↓
SomeClass (pojo persisted to database via JPA annotations + Hibernate XML for review)
```

## Key Changes

### 1. Entity Layer

**File:** `Some.java` (pojo class containing fields to be persisted)

**Added:**
- `@Entity` - Marks class as JPA entity
- `@Table(name = "TABLE_NAME")` - Maps to database table typically matching class name
- `@Id` with `@GeneratedValue` for primary key use `@GenericGenerator` if it is to be looked up via entity broker then prefer uuid2 or `@SequenceGenerator` for related tables using a sequence
- `@Column` annotations for all persistent fields
- `@Temporal(TemporalType.TIMESTAMP)` for date/time fields prefer using newer time types LocalDateTime
- `@Transient` for non-persistent fields (password, password2, terms)
- `implements PersistableEntity<T>` - Required by SpringCrudRepositoryImpl

**Key Pattern:**
- For identifiable entities that will be looked by the Entity Broker use uuid2
```java
@Entity
@Table(name = "SomeClass")
public class SomeClass implements PersistableEntity<String> {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "ID", length = 36, nullable = false)
    private String id;

    @Column(name = "SOME_FIELD", length = 255, nullable = false)
    private String someField;

    @OneToMany(mappedBy = "someClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RelatedClass> related = new HashSet<>();

    // ... other fields
}
```

- For entities that support SomeClass use sequence for connecting tables
```java
@Entity
@Table(name = "RelatedClass")
public class RelatedClass implements PersistableEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "some_seq")
    @SequenceGenerator(name = "some_seq", sequenceName = "SOME_ID_SEQ")
    private Long id;

    @Column(name = "SOME_FIELD", length = 255, nullable = false)
    private String someField;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SOME_CLASS_ID")
    private SomeClass someClass;

    // ... other fields
}
```

**Important:** The Hibernate XML mapping file (`SomeClass.hbm.xml`) was **retained** for verification purposes. It should be compared against the JPA annotations and deleted once verified to be identical.

### 2. Repository Layer

#### Interface: `SomeRepository.java`

**Location:** `{project}/api/src/java/org/sakaiproject/{project}/api/repository/`

**Pattern:**
```java
public interface SomeRepository
    extends SpringCrudRepository<SomeClass, String> {

    // Custom query methods
    Optional<SomeClass> findBySomeField(String someField);;
    Optional<SomeClass> findBySomeId(String someId);
    List<SomeClass> findByStatus(Integer status);
}
```

**Key Points:**
- Extends `SpringCrudRepository<T, ID>` which provides standard CRUD operations
- Custom query methods follow Spring Data naming conventions
- Returns `Optional<T>` for single results (Spring Data style)
- Returns `List<T>` for multiple results (empty list if none found)

#### Implementation: `SomeRepositoryImpl.java`

**Location:** `{project}/impl/src/java/org/sakaiproject/{project}/impl/repository/`

**Pattern:**
```java
@Transactional
public class SomeRepositoryImpl
    extends SpringCrudRepositoryImpl<SomeClass, String>
    implements SomeRepository {

    public Optional<SomeClass> findBySomeField(String someValue) {
        if (someValue == null) return Optional.empty();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<SomeClass> query = cb.createQuery(SomeClass.class);
        Root<SomeClass> root = query.from(SomeClass.class);

        query.select(root)
                .where(cb.equal(root.get("someField"), someValue));

        SomeClass result = sessionFactory.getCurrentSession()
                .createQuery(query)
                .uniqueResult();

        return Optional.ofNullable(result);
    }

    // ... other methods
}
```

**Key Points:**
- Uses `sessionFactory.getCriteriaBuilder()` from parent class to create JPA CriteriaBuilder queries
- Null-safe: returns `Optional.empty()` or empty `List` for null parameters
- Uses JPA CriteriaBuilder API (`query.select(root).where(cb.equal(root.get("someField"), someValue));`) for queries

### 3. Service Layer

#### Interface: `SomeService.java`

**Location:** `{project}/impl/src/java/org/sakaiproject/{project}/api/service/`

**Purpose:** Interface with identical method signatures for backward compatibility.

**Key Points:**
- Can use same method names as original
- Maintains existing API contract
- Focused on business operations, not data access

#### Implementation: `SomeServiceImpl.java`

**Location:** `{project}/impl/src/java/org/sakaiproject/{project}/impl/service/`

**Pattern:**
```java
@Slf4j
@Transactional
public class SomeServiceImpl implements SomeService {

    @Setter private SomeRepository repository;
    @Setter private SecurityService securityService;
    // ... other dependencies

    public void init() {
        // Initialization logic
    }

    @Override
    public SomeClass getSomeClassById(Long id) {
        // perform necessary security checks or application logic
        securityService.unlock(SomeConstants.SOME_PERMISSION, reference);
        return repository.findById(id).orElse(null);
    }

    @Override
    public SomeClass getSomeClassBySomeField(String someValue) {
        // perform necessary security checks or application logic
        securityService.unlock(SomeConstants.SOME_PERMISSION, reference);
        return repository.findBySomeField(someValue).orElse(null);
    }

    // ... business logic methods
}
```

**Key Changes from Old Implementation:**
- **Before:** `dao.findBySearch(SomeClass.class, search)` with `Search` and `Restriction` objects
- **After:** `repository.findBySomeField(someValue)` - direct, type-safe method calls
- **Before:** Manual null checking and list size checking
- **After:** `Optional.orElse(null)` for backward compatibility
- **Dependencies:** Injected `repository` instead of `dao`

### 4. Spring Configuration

**File:** `components.xml`

**Before:**

```xml
<!-- Old DAO configuration -->
<bean id="org.sakaiproject.{project}.dao.SomeLogicDao" ...>
  <property name="sessionFactory" ... />
  <property name="persistentClasses">
    <list>
      <value>org.sakaiproject.{project}.model.SomeClass</value>
    </list>
  </property>
</bean>

<!-- Old logic configuration -->
<bean id="org.sakaiproject.{project}.dto.SomeLogic"
      class="...SomeLogicImpl">
  <property name="dao" ref="..."/>
  ...
</bean>
```

**After:**
```xml
<!-- Repository configuration -->
<bean id="org.sakaiproject.{project}.api.repository.SomeRepository"
      class="org.sakaiproject.{project}.impl.repository.SomeRepositoryImpl">
    <property name="sessionFactory"
        ref="org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory" />
</bean>

<!-- Service configuration -->
<bean id="org.sakaiproject.{project}.api.service.SomeService"
      class="org.sakaiproject.{project}.impl.service.SomeServiceImpl"
      init-method="init">
    <property name="repository" ref="org.sakaiproject.{project}.api.repository.SomeRepository" />
    <property name="securityService" ref="org.sakaiproject.authz.api.SecurityService"/>
    <!-- ... other services -->
</bean>
```

**Key Changes:**
- Repository bean only needs `sessionFactory` (no `persistentClasses` list)
- Service bean references `repository` instead of `dao`
- Consolidated into single `components.xml` (removed `spring-hibernate.xml`)
- Job beans updated to reference new service as `someService`

### 5. Consumer Updates

All classes that used `SomeLogic` were updated to use `SomeService`:

**Controllers:**
- `{project}/tool/src/main/java/org/sakaiproject/{project}/tool/MainController.java`

## Migration Patterns and Best Practices

### 1. Query Migration Pattern

**Old Pattern (Generic DAO):**
```java
Search search = new Search();
Restriction rest = new Restriction("someField", someValue);
search.addRestriction(rest);
List<SomeClass> l = dao.findBySearch(SomeClass.class, search);
if (l.size() > 0) {
    return (SomeClass) l.get(0);
}
return null;
```

**New Pattern (Spring Repository):**
```java
return repository.findByUserId(userId).orElse(null);
```

**Benefits:**
- Type-safe: No casting required
- Concise: Single line vs 6+ lines
- Readable: Method name describes intent
- Null-safe: `Optional` handles null checking

### 2. Entity Annotation Pattern

**Critical Mappings:**
- Table name must match HBM exactly: `@Table(name = "SOME_TABLE")`
- Column names must match HBM: `@Column(name = "SOME_FIELD")`
- Sequence name must match HBM: `sequenceName = "SOME_ID_SEQ"`
- Nullable constraints must match: `nullable = false`

**Transient Fields:**
- Fields not in database: `@Transient` (password, password2, terms)
- These were not in the HBM and should not be persisted

### 3. Repository Implementation Pattern

**Custom Query Template:**
```java
@Override
public Optional<Entity> findBySomeField(String someValue) {
    if (someValue == null) return Optional.empty();

    CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
    CriteriaQuery<SomeClass> query = cb.createQuery(SomeClass.class);
    Root<SomeClass> root = query.from(SomeClass.class);

    query.select(root)
            .where(cb.equal(root.get("someField"), someValue));

    SomeClass result = sessionFactory.getCurrentSession()
            .createQuery(query)
            .uniqueResult();

    return Optional.ofNullable(result);
}
```

**List Query Template:**
```java
@Override
public List<SomeClass> findByStatus(Integer status) {
    if (status == null) return List.of();

    CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
    CriteriaQuery<SomeClass> query = cb.createQuery(SomeClass.class);
    Root<SomeClass> root = query.from(SomeClass.class);

    query.select(root)
            .where(cb.equal(root.get("status"), status));

    return sessionFactory.getCurrentSession()
            .createQuery(query)
            .getResultList();
}
```

### 4. Service Layer Pattern

**Business Logic Only:**
- Service should NOT contain query logic
- Service delegates to repository for data access
- Service contains complex business rules, validation, application logic, etc.

**Example:**
```java
// Good - Service delegates to repository
public SomeClass getSomeClassBySomeField(String someValue) {
    return repository.findBySomeField(someValue).orElse(null);
}

// Good - Service contains business logic
public boolean isExpired(SomeClass someClass) {
    if (someClass == null) {
        throw new IllegalArgumentException("null SomeClass");
    }

    if (someClass.getStatus() != null && someClass.getStatus().equals(SomeClass.SOME_STATUS)) {
        int minutes = serverConfigurationService.getInt(SOME_CONFIG, ...);
        long maxMillis = minutes * 60L * 1000L;
        long sentTime = someClass.getTime();

        if (System.currentTimeMillis() - time > maxMillis) {
            someClass.setStatus(SomeClass.STATUS_EXPIRED);
            someClass.setValidationReceived(new Date());
            repository.save(someClass);
            return true;
        }
    }

    return SomeClass.STATUS_EXPIRED.equals(someClass.getStatus());
}
```

## Files Created

### API Module
```
{project}-api/src/main/java/org/sakaiproject/{project}/api/
├── repository/
│   └── SomeRepository.java (NEW)
└── service/
    └── SomeService.java (NEW)
```

### Implementation Module
```
{project}-impl/src/main/java/org/sakaiproject/{project}/impl/
├── repository/
│   └── SomeRepositoryImpl.java (NEW)
└── service/
    └── SomeServiceImpl.java (NEW)
```

## Files Modified

```
{project}-api/src/main/java/org/sakaiproject/{project}/api/model/
└── SomeClass.java (MODIFIED - added JPA annotations)

{project}-tool/src/main/java/org/sakaiproject/{project}/tool/controller/
└── MainController.java (MODIFIED - uses SomeService)

{project}-impl/src/main/java/org/sakaiproject/{project}/impl/jobs/
├── JobClass1.java (MODIFIED - uses SomeService)
└── JobClass2.java (MODIFIED - uses SomeService)

{project}-impl/src/main/webapp/WEB-INF/
└── components.xml (MODIFIED - new bean definitions)
```

## Files Retained for Review

```
{project}-api/src/main/java/org/sakaiproject/{project}/model/
└── SomeClass.hbm.xml (RETAINED - verify JPA annotations match before deleting)
```

## Important Considerations

### 1. Hibernate Mapping Verification

**Action Required:** Before deleting `SomeClass.hbm.xml`, verify that:
- All table/column names match exactly
- All field types match (String, Integer, Date, etc.)
- All constraints match (nullable, length)
- The sequence name matches

### 2. Transaction Management

The `SpringCrudRepositoryImpl` uses Hibernate's `SessionFactory` which is already configured with transaction management in Sakai.
The `@Transactional` annotation should be present on the service and repository implementations.

**No additional transaction configuration needed** as it's handled by the global transaction manager.

### 3. Backward Compatibility

The new `SomeService` maintains the exact same method signatures as the old `SomeLogic` interface, ensuring:
- Same method names
- Same return types
- Same parameters
- Same behavior

This ensures minimal disruption to existing code.

### 4. Testing Considerations

**Key Areas to Test:**
1. **Entity Persistence:**
   - Create
   - Read
   - Update
   - Delete

2. **Custom Queries:**
   - Find by fields

3. **Business Logic:**
   - Business logic should be unit tested

4. **Controllers:**
   - requests to the controller should be unit tested

## Lessons Learned

### 1. Separation of Concerns is Critical

The original `SomeLogicImpl` mixed business logic and data access, making it:
- Hard to test in isolation
- Difficult to understand what's a query vs business rule
- Tightly coupled to the DAO implementation

**Lesson:** Always separate query logic (repository) from business logic (service).

### 2. Type-Safe Queries are Superior

Generic DAO's `findBySearch()` with reflection-based property names:
- No compile-time checking
- Easy to make typos in property names
- Requires casting results
- Verbose and hard to read

**Lesson:** Prefer typed repository methods with clear names.

### 3. Spring's Repository Pattern is Well-Suited for Sakai

The `SpringCrudRepositoryImpl` from Sakai kernel provides:
- Standard CRUD operations out of the box
- JPA Criteria Builder API support
- Proper transaction management integration
- Consistent patterns across different modules

**Lesson:** Use Sakai's provided base classes; they're already integrated with the framework.

### 4. JPA Annotations vs XML Mapping

JPA annotations provide:
- Co-location of mapping with entity class
- Better IDE support (autocomplete, refactoring)
- Type-safe column names
- Easier to maintain

**Lesson:** Prefer JPA annotations over XML mappings for new code, but verify carefully when migrating.

### 5. Naming Conventions Matter

Using `SomeService` instead of `SomeLogic`:
- Consistent across all consumers

**Lesson:** Choose concise, meaningful names for frequently-used dependencies.

### 6. Configuration Consolidation

Having multiple XML files (`components.xml`, `spring-hibernate.xml`, `hibernate-hbms.xml`) made it:
- Harder to understand the full configuration
- More files to maintain
- More places to look for bean definitions

**Lesson:** Consolidate configuration where possible, keeping only necessary separations (e.g., HBM files for mapping verification).

### 7. Optional vs Null Returns

The repository returns `Optional<T>` (modern Java style) but the service returns `null` (legacy compatibility). This hybrid approach:
- Allows modern code in repository
- Maintains compatibility in service
- Provides flexibility for future changes

**Lesson:** Use `Optional` in new internal APIs, but maintain existing contracts in public APIs.


## References

### Sakai Framework Classes

- **`SpringCrudRepository<T, ID>`**: Interface in `kernel/api/.../springframework/data/`
- **`SpringCrudRepositoryImpl<T, ID>`**: Base class in `kernel/api/.../springframework/data/`
- **`PersistableEntity<T>`**: Interface in `kernel/api/.../springframework/data/`

### JPA API

- **`CriteriaBuilder`**: JPA criteria query API
- **`SessionFactory`**: Hibernate session factory

### Spring Framework

- **`@Transactional`**: Handles transaction boundaries

## Conclusion

This migration successfully modernizes the tool's data access layer while maintaining backward compatibility. The new architecture provides:

✅ **Better Separation of Concerns** - Repository vs Service layers
✅ **Type-Safe Queries** - No more reflection-based property names
✅ **Modern Patterns** - JPA annotations, Optional returns
✅ **Easier Maintenance** - Clear responsibilities, readable code
✅ **Framework Integration** - Uses Sakai's SpringCrudRepositoryImpl

The migration can serve as a template for other Sakai tools still using Generic DAO.

---

**Document Version:** 1.0
**Last Updated:** 2025-10-06
**Author:** Migration performed with Claude Code
