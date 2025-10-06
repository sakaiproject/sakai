# Migration from Generic DAO to Spring Repository Pattern

**Date:** 2025-10-06
**Component:** Account Validator Tool
**Migration Type:** Generic DAO → Spring Repository (SpringCrudRepositoryImpl)

## Overview

This document describes the migration of the Account Validator tool from Sakai's Generic DAO pattern to Spring's Repository pattern using `SpringCrudRepositoryImpl` from the Sakai kernel.

## Background

The Account Validator tool originally used:
- **Generic DAO Pattern**: `HibernateCompleteGenericDao` with search-based queries
- **Mixed Concerns**: Business logic and data access queries in the same class (`ValidationLogicImpl`)
- **XML Configuration**: Hibernate XML mappings for entities

The migration modernizes this to:
- **Spring Repository Pattern**: `SpringCrudRepositoryImpl` with typed repositories
- **Separation of Concerns**: Service layer for business logic, repository layer for queries
- **JPA Annotations**: Modern JPA annotations on entity classes

## Architecture Changes

### Old Architecture

```
Controllers/Jobs
      ↓
ValidationLogic (interface)
      ↓
ValidationLogicImpl (mixed business logic + queries)
      ↓
ValidationDao (extends GeneralGenericDao)
      ↓
ValidationLogicDao (extends HibernateCompleteGenericDao)
      ↓
Database (via Hibernate XML mapping)
```

### New Architecture

```
Controllers/Jobs
      ↓
AccountValidationService (interface)
      ↓
AccountValidationServiceImpl (business logic only)
      ↓
ValidationAccountRepository (extends SpringCrudRepository<T,ID>)
      ↓
ValidationAccountRepositoryImpl (extends SpringCrudRepositoryImpl)
      ↓
Database (via JPA annotations + Hibernate XML for review)
```

## Key Changes

### 1. Entity Layer

**File:** `ValidationAccount.java`

**Added:**
- `@Entity` - Marks class as JPA entity
- `@Table(name = "VALIDATIONACCOUNT_ITEM")` - Maps to database table
- `@Id` with `@GeneratedValue` and `@SequenceGenerator` for primary key
- `@Column` annotations for all persistent fields
- `@Temporal(TemporalType.TIMESTAMP)` for date fields
- `@Transient` for non-persistent fields (password, password2, terms)
- `implements PersistableEntity<Long>` - Required by SpringCrudRepositoryImpl

**Key Pattern:**
```java
@Entity
@Table(name = "VALIDATIONACCOUNT_ITEM")
public class ValidationAccount implements PersistableEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "validation_account_seq")
    @SequenceGenerator(name = "validation_account_seq", sequenceName = "VALIDATIONACCOUNT_ITEM_ID_SEQ")
    private Long id;

    @Column(name = "USER_ID", length = 255, nullable = false)
    private String userId;

    // ... other fields
}
```

**Important:** The Hibernate XML mapping file (`ValidationAccount.hbm.xml`) was **retained** for verification purposes. It should be compared against the JPA annotations and deleted once verified to be identical.

### 2. Repository Layer

#### Interface: `ValidationAccountRepository.java`

**Location:** `account-validator-api/src/java/org/sakaiproject/accountvalidator/repository/`

**Pattern:**
```java
public interface ValidationAccountRepository
    extends SpringCrudRepository<ValidationAccount, Long> {

    // Custom query methods
    Optional<ValidationAccount> findByValidationToken(String validationToken);
    Optional<ValidationAccount> findByUserId(String userId);
    List<ValidationAccount> findByStatus(Integer status);
}
```

**Key Points:**
- Extends `SpringCrudRepository<T, ID>` which provides standard CRUD operations
- Custom query methods follow Spring Data naming conventions
- Returns `Optional<T>` for single results (Spring Data style)
- Returns `List<T>` for multiple results (empty list if none found)

#### Implementation: `ValidationAccountRepositoryImpl.java`

**Location:** `account-validator-impl/src/java/org/sakaiproject/accountvalidator/repository/impl/`

**Pattern:**
```java
@Repository
public class ValidationAccountRepositoryImpl
    extends SpringCrudRepositoryImpl<ValidationAccount, Long>
    implements ValidationAccountRepository {

    @Override
    public Optional<ValidationAccount> findByValidationToken(String validationToken) {
        if (validationToken == null) {
            return Optional.empty();
        }

        Criteria criteria = startCriteriaQuery();
        criteria.add(Restrictions.eq("validationToken", validationToken));

        ValidationAccount result = (ValidationAccount) criteria.uniqueResult();
        return Optional.ofNullable(result);
    }

    // ... other methods
}
```

**Key Points:**
- Uses `startCriteriaQuery()` from parent class to create Hibernate Criteria queries
- Null-safe: returns `Optional.empty()` or empty `List` for null parameters
- Uses Hibernate Criteria API (`Restrictions.eq()`) for queries
- `@Repository` annotation for Spring component scanning

### 3. Service Layer

#### Interface: `AccountValidationService.java`

**Location:** `account-validator-api/src/java/org/sakaiproject/accountvalidator/service/`

**Purpose:** Replaces `ValidationLogic` interface with identical method signatures for backward compatibility.

**Key Points:**
- Same method names as original `ValidationLogic`
- Maintains existing API contract
- Focused on business operations, not data access

#### Implementation: `AccountValidationServiceImpl.java`

**Location:** `account-validator-impl/src/java/org/sakaiproject/accountvalidator/service/impl/`

**Pattern:**
```java
@Slf4j
@Service
public class AccountValidationServiceImpl implements AccountValidationService {

    @Setter private ValidationAccountRepository repository;
    @Setter private IdManager idManager;
    // ... other dependencies

    public void init() {
        // Initialize email templates
    }

    @Override
    public ValidationAccount getVaLidationAcountById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public ValidationAccount getVaLidationAcountBytoken(String token) {
        return repository.findByValidationToken(token).orElse(null);
    }

    // ... business logic methods
}
```

**Key Changes from Old Implementation:**
- **Before:** `dao.findBySearch(ValidationAccount.class, search)` with `Search` and `Restriction` objects
- **After:** `repository.findByUserId(userId)` - direct, type-safe method calls
- **Before:** Manual null checking and list size checking
- **After:** `Optional.orElse(null)` for backward compatibility
- **Dependencies:** Injected `repository` instead of `dao`

### 4. Spring Configuration

**File:** `components.xml`

**Before:**
```xml
<!-- Old DAO configuration -->
<bean id="org.sakaiproject.accountvalidator.dao.ValidationLogicDao" ...>
    <property name="sessionFactory" ... />
    <property name="persistentClasses">
        <list>
            <value>org.sakaiproject.accountvalidator.model.ValidationAccount</value>
        </list>
    </property>
</bean>

<!-- Old logic configuration -->
<bean id="org.sakaiproject.accountvalidator.logic.ValidationLogic"
      class="...ValidationLogicImpl">
    <property name="dao" ref="..." />
    ...
</bean>
```

**After:**
```xml
<!-- Repository configuration -->
<bean id="org.sakaiproject.accountvalidator.repository.ValidationAccountRepository"
      class="org.sakaiproject.accountvalidator.impl.repository.ValidationAccountRepositoryImpl">
    <property name="sessionFactory"
        ref="org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory" />
</bean>

<!-- Service configuration -->
<bean id="org.sakaiproject.accountvalidator.service.AccountValidationService"
      class="org.sakaiproject.accountvalidator.impl.service.AccountValidationServiceImpl"
      init-method="init">
    <property name="repository" ref="org.sakaiproject.accountvalidator.repository.ValidationAccountRepository" />
    <property name="idManager" ref="org.sakaiproject.id.api.IdManager" />
    <!-- ... other services -->
</bean>
```

**Key Changes:**
- Repository bean only needs `sessionFactory` (no `persistentClasses` list)
- Service bean references `repository` instead of `dao`
- Consolidated into single `components.xml` (removed `spring-hibernate.xml`)
- Job beans updated to reference new service as `avService`

### 5. Consumer Updates

All classes that used `ValidationLogic` were updated to use `AccountValidationService`:

**Controllers:**
- `reset-pass/MainController.java`
- `account-validator-tool/MainController.java`

**Jobs:**
- `CheckValidations.java`
- `CheckAccountsJob.java`

**Pattern:**
```java
// Before
@Autowired
private ValidationLogic validationLogic;

// After
@Autowired
private AccountValidationService avService;
```

**Naming Convention:** Used `avService` (Account Validation Service) for clarity and brevity.

## Migration Patterns and Best Practices

### 1. Query Migration Pattern

**Old Pattern (Generic DAO):**
```java
Search search = new Search();
Restriction rest = new Restriction("userId", userId);
search.addRestriction(rest);
List<ValidationAccount> l = dao.findBySearch(ValidationAccount.class, search);
if (l.size() > 0) {
    return (ValidationAccount) l.get(0);
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
- Table name must match HBM exactly: `@Table(name = "VALIDATIONACCOUNT_ITEM")`
- Column names must match HBM: `@Column(name = "USER_ID")`
- Sequence name must match HBM: `sequenceName = "VALIDATIONACCOUNT_ITEM_ID_SEQ"`
- Nullable constraints must match: `nullable = false`
- Field lengths must match: `length = 255`

**Transient Fields:**
- Fields not in database: `@Transient` (password, password2, terms)
- These were not in the HBM and should not be persisted

### 3. Repository Implementation Pattern

**Custom Query Template:**
```java
@Override
public Optional<Entity> findByField(String field) {
    if (field == null) {
        return Optional.empty();
    }

    Criteria criteria = startCriteriaQuery();
    criteria.add(Restrictions.eq("fieldName", field));

    Entity result = (Entity) criteria.uniqueResult();
    return Optional.ofNullable(result);
}
```

**List Query Template:**
```java
@Override
@SuppressWarnings("unchecked")
public List<Entity> findByField(String field) {
    if (field == null) {
        return List.of();
    }

    Criteria criteria = startCriteriaQuery();
    criteria.add(Restrictions.eq("fieldName", field));

    return criteria.list();
}
```

### 4. Service Layer Pattern

**Business Logic Only:**
- Service should NOT contain query logic
- Service delegates to repository for data access
- Service contains complex business rules, validation, email sending, etc.

**Example:**
```java
// Good - Service delegates to repository
public ValidationAccount getVaLidationAcountByUserId(String userId) {
    return repository.findByUserId(userId).orElse(null);
}

// Good - Service contains business logic
public boolean isTokenExpired(ValidationAccount va) {
    if (va == null) {
        throw new IllegalArgumentException("null ValidationAccount");
    }

    if (va.getAccountStatus() != null &&
        va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET)) {
        int minutes = serverConfigurationService.getInt(MAX_PASSWORD_RESET_MINUTES, ...);
        long maxMillis = minutes * 60 * 1000;
        long sentTime = va.getValidationSent().getTime();

        if (System.currentTimeMillis() - sentTime > maxMillis) {
            va.setStatus(ValidationAccount.STATUS_EXPIRED);
            va.setValidationReceived(new Date());
            repository.save(va);
            return true;
        }
    }

    return ValidationAccount.STATUS_EXPIRED.equals(va.getStatus());
}
```

## Files Created

### API Module
```
account-validator-api/src/java/org/sakaiproject/accountvalidator/
├── repository/
│   └── ValidationAccountRepository.java (NEW)
└── service/
    └── AccountValidationService.java (NEW)
```

### Implementation Module
```
account-validator-impl/src/java/org/sakaiproject/accountvalidator/
├── repository/impl/
│   └── ValidationAccountRepositoryImpl.java (NEW)
└── service/impl/
    └── AccountValidationServiceImpl.java (NEW)
```

## Files Modified

```
account-validator-api/src/java/org/sakaiproject/accountvalidator/model/
└── ValidationAccount.java (MODIFIED - added JPA annotations)

reset-pass/src/main/java/org/sakaiproject/resetpass/controller/
└── MainController.java (MODIFIED - uses AccountValidationService)

account-validator-tool/src/java/org/sakaiproject/accountvalidator/tool/controller/
└── MainController.java (MODIFIED - uses AccountValidationService)

account-validator-impl/src/java/org/sakaiproject/accountvalidator/impl/jobs/
├── CheckValidations.java (MODIFIED - uses AccountValidationService)
└── CheckAccountsJob.java (MODIFIED - uses AccountValidationService)

account-validator-impl/src/webapp/WEB-INF/
└── components.xml (MODIFIED - new bean definitions)
```

## Files Removed

```
account-validator-impl/src/java/org/sakaiproject/accountvalidator/
├── dao/impl/
│   └── ValidationLogicDao.java (REMOVED)
└── logic/
    ├── dao/
    │   └── ValidationDao.java (REMOVED)
    └── impl/
        └── ValidationLogicImpl.java (REMOVED)

account-validator-api/src/java/org/sakaiproject/accountvalidator/logic/
└── ValidationLogic.java (REMOVED)

account-validator-impl/src/webapp/WEB-INF/
└── spring-hibernate.xml (REMOVED - consolidated into components.xml)
```

## Files Retained for Review

```
account-validator-api/src/java/org/sakaiproject/accountvalidator/model/
└── ValidationAccount.hbm.xml (RETAINED - verify JPA annotations match before deleting)
```

## Important Considerations

### 1. Hibernate Mapping Verification

**Action Required:** Before deleting `ValidationAccount.hbm.xml`, verify that:
- All table/column names match exactly
- All field types match (String, Integer, Date, etc.)
- All constraints match (nullable, length)
- The sequence name matches

**Comparison Checklist:**
- [ ] Table name: `VALIDATIONACCOUNT_ITEM`
- [ ] Sequence: `VALIDATIONACCOUNT_ITEM_ID_SEQ`
- [ ] Column: `USER_ID` (nullable=false, length=255)
- [ ] Column: `VALIDATION_TOKEN` (nullable=false, length=255)
- [ ] Column: `EID` (nullable=true, length=255)
- [ ] Column: `VALIDATION_SENT` (type=timestamp)
- [ ] Column: `VALIDATION_RECEIVED` (type=timestamp)
- [ ] Column: `VALIDATIONS_SENT` (type=integer)
- [ ] Column: `STATUS` (type=integer)
- [ ] Column: `FIRST_NAME` (length=255)
- [ ] Column: `SURNAME` (length=255)
- [ ] Column: `ACCOUNT_STATUS` (type=integer)

### 2. Transaction Management

The `SpringCrudRepositoryImpl` uses Hibernate's `SessionFactory` which is already configured with transaction management in Sakai. The `@Transactional` annotations are already present in the base class for mutating operations (save, delete, etc.).

**No additional transaction configuration needed** as it's handled by the global transaction manager.

### 3. Backward Compatibility

The new `AccountValidationService` maintains the exact same method signatures as the old `ValidationLogic` interface, ensuring:
- Same method names (including typos like "VaLidation" for compatibility)
- Same return types
- Same parameters
- Same behavior

This ensures minimal disruption to existing code.

### 4. Testing Considerations

**Key Areas to Test:**
1. **Entity Persistence:**
   - Create new ValidationAccount
   - Update existing ValidationAccount
   - Delete ValidationAccount

2. **Custom Queries:**
   - Find by validation token
   - Find by user ID
   - Find by status

3. **Business Logic:**
   - Token expiration logic
   - Account validation checks
   - Email template sending
   - Account merging

4. **Jobs:**
   - CheckAccountsJob execution
   - CheckValidations job execution

5. **Controllers:**
   - Password reset flow
   - Account validation flow

## Lessons Learned

### 1. Separation of Concerns is Critical

The original `ValidationLogicImpl` mixed business logic and data access, making it:
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
- Hibernate Criteria API support via `startCriteriaQuery()`
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

Using `avService` instead of `validationLogic` or `accountValidationService`:
- Shorter, easier to type
- Clear abbreviation
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

## Future Enhancements

### 1. Remove Hibernate XML Mapping

Once verified, delete `ValidationAccount.hbm.xml` and remove the HBM import from `components.xml`:
```xml
<!-- Remove this line after verification -->
<import resource="hibernate-hbms.xml" />
```

### 2. Migrate to Spring Data JPA

For even more automation, consider migrating to full Spring Data JPA where methods are generated from names:
```java
public interface ValidationAccountRepository
    extends JpaRepository<ValidationAccount, Long> {

    Optional<ValidationAccount> findByValidationToken(String validationToken);
    Optional<ValidationAccount> findByUserId(String userId);
    List<ValidationAccount> findByStatus(Integer status);
}
```

No implementation needed - Spring generates it automatically!

### 3. Add Integration Tests

Create repository integration tests using Spring's test framework:
```java
@SpringBootTest
@Transactional
class ValidationAccountRepositoryTest {

    @Autowired
    private ValidationAccountRepository repository;

    @Test
    void testFindByValidationToken() {
        // Create test data
        ValidationAccount account = new ValidationAccount();
        account.setValidationToken("test-token");
        account.setUserId("user123");
        repository.save(account);

        // Test query
        Optional<ValidationAccount> found =
            repository.findByValidationToken("test-token");

        assertTrue(found.isPresent());
        assertEquals("user123", found.get().getUserId());
    }
}
```

### 4. Use Constructor Injection

Replace setter injection with constructor injection for better immutability:
```java
@Service
public class AccountValidationServiceImpl implements AccountValidationService {

    private final ValidationAccountRepository repository;
    private final IdManager idManager;
    // ... other dependencies

    @Autowired
    public AccountValidationServiceImpl(
        ValidationAccountRepository repository,
        IdManager idManager,
        // ... other dependencies
    ) {
        this.repository = repository;
        this.idManager = idManager;
        // ... other assignments
    }
}
```

### 5. Fix Method Name Typo

The method name `getVaLidationAcountById` has typos (should be `getValidationAccountById`). Consider:
- Creating properly-named methods
- Deprecating old methods
- Providing migration path for consumers

## References

### Sakai Framework Classes

- **`SpringCrudRepository<T, ID>`**: Interface in `kernel/api/.../springframework/data/`
- **`SpringCrudRepositoryImpl<T, ID>`**: Base class in `kernel/api/.../springframework/data/`
- **`PersistableEntity<T>`**: Interface in `kernel/api/.../springframework/data/`

### Hibernate API

- **`Criteria`**: Hibernate criteria query API
- **`Restrictions`**: Static factory for query restrictions
- **`SessionFactory`**: Hibernate session factory

### Spring Framework

- **`@Repository`**: Marks repository implementations
- **`@Service`**: Marks service implementations
- **`@Transactional`**: Handles transaction boundaries

## Conclusion

This migration successfully modernized the Account Validator tool's data access layer while maintaining backward compatibility. The new architecture provides:

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
