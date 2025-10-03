# Ehcache 3.11.1 Migration Impact

This note inventories the areas that would have to change in order to replace the legacy `net.sf.ehcache:ehcache` 2.x dependency with the current `org.ehcache:ehcache` 3.11.1 coordinates.

## 1. Update Maven dependencies and packaging
* The corporate parent POM hard-codes the Ehcache 2.x groupId, artifactId, and version; moving to 3.x would require switching these properties to `org.ehcache:ehcache:3.11.1` and verifying every module that inherits them. 【F:master/pom.xml†L29-L31】
* Several modules declare direct dependencies on `net.sf.ehcache:ehcache`, either using the shared property or hard-coded coordinates (for example the kernel API/impl, deploy aggregator, and site term manager tool). Each of those POMs must be updated to the new coordinates, or replaced with the Ehcache 3 modules they now need. 【F:kernel/kernel-impl/pom.xml†L186-L189】【F:kernel/api/pom.xml†L115-L117】【F:deploy/pom.xml†L976-L985】【F:site/term-manager/impl/pom.xml†L31-L35】
* Docker/Tomcat packaging currently whitelists `ehcache-core-*.jar`. The filename pattern will change once Ehcache 3 artifacts are introduced, so the loader inclusion list must be revised to match the new jars. 【F:docker/tomcat/conf/catalina.properties†L300-L309】

## 2. Rewrite the memory service layer
* `EhcacheMemoryService` exposes the Ehcache 2.x `CacheManager`, `Ehcache`, and `Element` types throughout the Sakai memory abstraction. Ehcache 3 replaces these with `org.ehcache.CacheManager`, `Cache`, and `CacheConfiguration` builders, so the implementation has to be rewritten around the new API (including cache creation, statistics, eviction, and configuration loading). 【F:kernel/kernel-impl/src/main/java/org/sakaiproject/memory/impl/EhcacheMemoryService.java†L31-L190】
* `EhcacheCache` wraps the Ehcache 2 `Ehcache` object directly (including `Element`, event listeners, and statistics). The Ehcache 3 event/listener and statistics APIs are incompatible, so this adapter layer must be redesigned to work with the new listener interfaces and value access patterns. 【F:kernel/kernel-impl/src/main/java/org/sakaiproject/memory/impl/EhcacheCache.java†L28-L387】
* Multiple services (security, site caching, etc.) unwrap the Ehcache 2 classes to reach lower-level behavior. Those call sites must either be removed or rewritten against the Ehcache 3 equivalents. 【F:kernel/kernel-impl/src/main/java/org/sakaiproject/authz/impl/SakaiSecurity.java†L29-L70】【F:kernel/kernel-impl/src/main/java/org/sakaiproject/authz/impl/SakaiSecurity.java†L562-L576】【F:kernel/kernel-impl/src/main/java/org/sakaiproject/site/impl/SiteCacheImpl.java†L31-L333】
* Unit tests instantiate `net.sf.ehcache.CacheManager` directly; they need to be updated once the production code switches to the Ehcache 3 builder APIs. 【F:calendar/calendar-impl/impl/src/test/org/sakaiproject/calendar/impl/BaseExternalCalendarSubscriptionTest.java†L64-L156】

## 3. Replace Spring integration helpers
* The project still ships a customized copy of Spring’s old `EhCacheFactoryBean` and `EhCacheManagerFactoryBean`, which only support Ehcache 2. Spring 5 dropped native support for Ehcache 3 in these classes, so the caching beans must be rewritten to use either Ehcache 3’s Spring Boot integration or the JSR-107 (`javax.cache`) bridge. 【F:kernel/api/src/main/java/org/sakaiproject/memory/util/EhCacheFactoryBean.java†L68-L173】【F:kernel/api/src/main/java/org/sakaiproject/memory/util/EhCacheManagerFactoryBean.java†L26-L120】
* Downstream tools wire `org.springframework.cache.ehcache.EhCacheFactoryBean` directly in their Spring XML. Those bean definitions need to migrate to whatever new factory (Ehcache 3 native or JCache) replaces the legacy wiring. 【F:site/term-manager/impl/src/webapp/WEB-INF/components.xml†L32-L45】

## 4. Convert Ehcache configuration files
* The shared cache descriptors under `common/impl/src/java/ehcache.xml` and `kernel/api/src/main/java/org/sakaiproject/memory/api/ehcache.xml` use the Ehcache 2 XML schema (e.g., `maxElementsInMemory`, RMI peer replication factories). Ehcache 3 uses a different XML namespace and replaces many of these attributes with resource pool builders and new clustering modules, so the descriptors must be rewritten or replaced with programmatic configuration. 【F:common/impl/src/java/ehcache.xml†L1-L158】【F:kernel/api/src/main/java/org/sakaiproject/memory/api/ehcache.xml†L1-L146】
* Hibernate integration is hard-coded to `net.sf.ehcache.hibernate.AbstractEhcacheRegionFactory`. Ehcache 3 requires the updated Hibernate 5.x provider (`org.ehcache.jsr107.EhcacheCachingProvider` or the official Ehcache 3 Hibernate region factory), so Spring wiring and Hibernate properties need to be updated accordingly. 【F:kernel/kernel-impl/src/main/webapp/WEB-INF/util-components.xml†L43-L53】

## 5. Revisit management and monitoring
* JMX exposure currently goes through `net.sf.ehcache.management.ManagementService`. Ehcache 3 ships a different management module, so the `EhCacheJmxRegistration` helper must be rewritten to use the new management API (or dropped if JMX is no longer required). 【F:kernel/kernel-impl/src/main/java/org/sakaiproject/memory/impl/EhCacheJmxRegistration.java†L30-L55】
* Configuration flags such as `memory.ehcache.jmx` and any documentation referencing Ehcache 2 behavior should be reviewed once the new management hooks are in place. 【F:kernel/kernel-impl/src/main/java/org/sakaiproject/memory/impl/EhcacheMemoryService.java†L83-L188】

## 6. Validate clustering/distribution features
* The default Ehcache configuration enables legacy RMI-based replication factories (`net.sf.ehcache.distribution.*`). Ehcache 3 replaces these with new clustering modules (Terracotta or REST). Any production deployments that rely on clustered caches will need an equivalent strategy using the new APIs. 【F:kernel/api/src/main/java/org/sakaiproject/memory/api/ehcache.xml†L71-L145】

## 7. Regression testing and verification
* After refactoring the service layer and configuration, the caching-dependent integration tests and tool-specific caches should be exercised (e.g., Sections service, calendar imports, sitestats) to make sure no assumptions about Ehcache 2 remain. Their configurations currently assume 2.x semantics such as `maxElementsInMemory` and `overflowToDisk`. 【F:edu-services/sections-service/sections-impl/sakai/model/pom.xml†L50-L50】【F:calendar/calendar-impl/impl/src/test/resources/ehcache.xml†L1-L12】

In short, upgrading to Ehcache 3.11.1 is a breaking change that touches dependency management, the shared memory service implementation, Spring integration points, XML configuration, and monitoring. The migration will require a coordinated rewrite of those layers before simply switching the Maven coordinates.
