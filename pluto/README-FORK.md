# SAK-52804 — Vendoring Apache Pluto into Sakai

This document records how Apache Pluto 1.1.7 was imported into the Sakai source tree, pruned, adapted to Sakai Maven conventions, and renamed so it cannot be confused with upstream Maven Central artifacts.

Branch: `SAK-52804`

## Goal

Ship a Sakai-owned build of the Pluto portlet container (Portlet 1.0 RI) from source, under distinct artifact names, using Sakai’s master POM and versioning — without yet wiring Pluto into the top-level Sakai reactor.

## What landed

Top-level directory: `pluto/`

| Module | Artifact |
|--------|----------|
| Parent aggregator | `org.apache.pluto:sak-pluto` |
| Descriptor API | `org.apache.pluto:sak-pluto-descriptor-api` |
| Descriptor impl | `org.apache.pluto:sak-pluto-descriptor-impl` |
| Container | `org.apache.pluto:sak-pluto-container` |
| Tag library | `org.apache.pluto:sak-pluto-taglib` |

Version: `${sakai.pluto.version}` → `${sakai.version}` (currently `27-SNAPSHOT`)

## Commit history (on `SAK-52804`)

1. **Import Apache Pluto 1.1.7 source**  
   Copied the full upstream tree into `pluto/`, excluding `target/` build output.

2. **Prune Pluto modules unused by Sakai**  
   Removed portal driver, portal, testsuite, util, maven plugin, ant tasks, site-skin, and assembly. Kept only the four artifacts Sakai already consumed from Maven Central.

3. **Allow Pluto modules to build with modern Maven/JDK**  
   Removed broken `pluto-site` remote-resources wiring, set Java 8+ compiler settings (later superseded by master), fixed container `META-INF` resource `targetPath`.

4. **Reparent Pluto under Sakai master POM**  
   Switched parent from `org.apache:apache:3` to `org.sakaiproject:master`. Dropped Apache mailing lists / distributionManagement / gpg noise. Used Sakai-managed dependency versions (servlet-api 4, junit, spring-test, castor, xerces, jcl-over-slf4j, etc.). Overrode source layout back to Maven standard (`src/main/java`) because master defaults to Sakai’s `src/java`. Added Servlet 3.1 stubs on `PrintWriterServletOutputStream` for servlet-api 4.

5. **Rename Pluto artifacts to `sak-pluto-*`**  
   Renamed module directories and artifactIds; updated `master`, `deploy`, portal POMs, and docker Tomcat jar-scan / shared-classloader patterns. GroupId remains `org.apache.pluto` for now.

6. **Align Pluto with Sakai master versioning**  
   Dropped hard-coded `1.1.7`. Inherit `27-SNAPSHOT` from master. Set `sakai.pluto.version` to `${sakai.version}` in `master/pom.xml`. Inter-module deps use `${sakai.pluto.version}`.

## Intentional exceptions

- **Portlet API 1.0** — Pluto is the Portlet 1.0 reference implementation. Sakai master manages `portlet-api` 3.0.1 for tools; compiling this container against 3.0.1 fails. Container and taglib keep an explicit `portlet-api:1.0` dependency.
- **Portlet spec identity in filtered resources** — `javax.portlet.version.major=1` / `minor=0` remain in the aggregator POM for `environment.properties` filtering (runtime identity of the RI, not the Maven dependency version).
- **Test-only deps not in master** — `jmock` 1.2.0 and `xmlunit` 1.6 are still pinned for legacy unit tests.
- **Not in the Sakai reactor yet** — `pluto` is not listed in the root `pom.xml` `<modules>`. Building Sakai alone will not build these jars until that wiring is added (or you `mvn install` from `pluto/` first).

## How to build

From the Sakai trunk checkout:

```bash
cd pluto
mvn clean -DskipTests package
```

Artifacts land under each module’s `target/` as `sak-pluto-*-27-SNAPSHOT.jar`.

To publish into the local Maven repo (so `deploy` / portal can resolve them before reactor wiring):

```bash
cd pluto
mvn clean -DskipTests install
```

## Sakai consumers updated

These now depend on `sak-pluto-*` via `${sakai.pluto.version}`:

- `master/pom.xml` (dependencyManagement)
- `deploy/pom.xml` (shared/lib deploy)
- `portal/portal-service-impl/impl/pom.xml`
- `portal/portal-render-impl/impl/pom.xml`
- `docker/tomcat/conf/catalina.properties` (shared loader jar patterns)
- `docker/tomcat/conf/context.xml` (TLD scan for `sak-pluto-taglib-*.jar`)

## Still out of scope

- Adding `<module>pluto</module>` to the Sakai base reactor
- Changing `deploy.target` / `sakai:deploy` so a Pluto-only build copies jars into Tomcat (today Tomcat still gets Pluto via the Sakai `deploy` module after artifacts are installed/resolved)
- Migrating Java package names away from `org.apache.pluto`
- Changing groupId to `org.sakaiproject`
- Porting the container to Portlet 2.0 / 3.0 APIs

## Source provenance

Upstream: Apache Pluto 1.1.7 (Apache License 2.0)  
Original import tree: local `pluto-1.1.7` distribution (source only; no `target/` artifacts).
