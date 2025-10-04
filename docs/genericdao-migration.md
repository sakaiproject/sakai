# Migrating Away From genericdao

This document explains why and how Sakai should retire the legacy `genericdao` helper library in favor of standard Spring + Hibernate/JPA patterns available in modern Java (JDK 17+, targeting JDK 21).

It also includes a proof‑of‑concept (POC) refactor in the Polls tool that removes `genericdao` usage entirely.

## Why Migrate
- Maintainability: `genericdao` re‑implements DAO patterns that Spring and Hibernate/JPA already solve better with clearer APIs and less bespoke code.
- Modern Java: The library predates Java 8 streams, JPA Criteria, and stronger generics. Sticking to standard APIs simplifies onboarding and reduces technical debt.
- Tooling/Performance: First‑class Hibernate/JPA integration enables better diagnostics (SQL logs, stats), standard transaction management, and easier caching.
- Risk Reduction: Fewer custom abstractions lowers the surface area for subtle ClassLoader and proxy issues.

## What genericdao Provided (and How To Replace It)
- Generic DAO base classes (CRUD, batch delete, simple queries) → Use a focused DAO with Hibernate `SessionFactory` or adopt Spring Data JPA for repositories.
- `Search`/`Restriction` DSL → Replace with explicit methods per use case, implemented with HQL/JPQL or the JPA Criteria API. These are readable, type‑safe, and testable.
- Custom transaction proxies → Use Spring’s standard `TransactionProxyFactoryBean` or `@Transactional` on services.

## Recommended Approach (Aligned with Assignments)
1. Define explicit repositories/DAOs per aggregate (e.g., `PollDao`) with clear domain methods. Do not re‑create a generic search DSL.
2. Implement with Hibernate SessionFactory (as Assignments does) or JPA `EntityManager`. Keep HBM mappings initially; migrate to JPA annotations later.
3. Transactions on services via annotations:
   - Annotate service classes with `@Transactional(readOnly = true)`; mark write methods with `@Transactional`.
   - Enable `<tx:annotation-driven transaction-manager="org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager"/>` in the tool’s Spring XML.
   - Do NOT wrap DAOs with `TransactionProxyFactoryBean`; let services define boundaries. This avoids classloader/CGLIB complexity.
4. Remove `genericdao` dependencies from module POMs.

## Typical Migration Steps
- Identify usages: `rg -n "org\.sakaiproject\.genericdao|Search\(|Restriction\("`.
- Replace DAO interfaces extending `GeneralGenericDao` with explicit methods:
  - `save(entity)`, `delete(entity)`, `deleteAll(Collection)` if needed.
  - Domain queries, e.g., `findPollsBySite(String siteId)`, `findOptionsByPoll(Long pollId)`, etc.
- Rewrite services to call explicit DAO methods. Remove imports of `Search`, `Restriction`, `Order`.
- Update Spring wiring:
  - Replace `CurrentClassLoaderTxProxyFactoryBean` with Spring’s `TransactionProxyFactoryBean`, or annotate services with `@Transactional`.
  - Keep using the global `SessionFactory` and existing HBM mappings.
- Update tests to remove `genericdao` references and mock the new DAO methods directly.

## Polls Tool POC (What Changed)
- New DAO: `org.sakaiproject.poll.dao.impl.PollDaoHibernateImpl` using `SessionFactory` + HQL.
- Updated interface: `org.sakaiproject.poll.dao.PollDao` declares explicit methods (no `GeneralGenericDao`).
- Services refactored: `PollListManagerImpl` and `PollVoteManagerImpl` call DAO methods directly; no `Search`/`Restriction`.
- Transactions via annotations at services; no DAO proxy bean. XML adds `<tx:annotation-driven .../>`.
- POM cleanup: Removed `org.sakaiproject.genericdao:generic-dao` from `polls-impl`.
- Tests updated to the new DAO bean id and API.

## Query Mapping Examples
- `new Restriction("siteId", siteIds[])` → `where p.siteId in (:siteIds)`.
- `new Restriction("voteOpen", now, LESS)` and `new Restriction("voteClose", now, GREATER)` → `where p.voteOpen < :now and p.voteClose > :now`.
- `new Restriction("pollId", pollId)` → `where p.pollId = :pollId`.
- `Order("creationDate", false)` → `order by p.creationDate desc`.
- Distinct submission count → `select count(distinct v.submissionId) from Vote v where v.pollId = :pollId`.

## Transactions
- Use `@Transactional` on service classes with read‑only on finders; annotate write methods for updates/deletes.
- Enable `<tx:annotation-driven .../>` in the tool XML. Avoid DAO proxies.

## Incremental Rollout Strategy
1. Migrate one tool (Polls) end‑to‑end.
2. For other tools, convert DAOs and services module‑by‑module; keep HBM mappings; avoid broad framework changes.
3. Once all modules are migrated, mark `genericdao` as deprecated and remove from the build.

## Notes for JDK 17/21
- Use `var` locally where clear; prefer records for immutable DTOs (not entities).
- Keep entity classes as POJOs while still using existing HBMs; consider JPA annotations in a later phase.

## Checklist
- [ ] Remove `genericdao` imports
- [ ] Replace `Search`/`Restriction` usages
- [ ] Provide explicit DAO methods
- [ ] Update Spring transaction wiring
- [ ] Update tests
- [ ] Remove `genericdao` dependency from module POM
