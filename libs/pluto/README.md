# Vendoring Apache Pluto into Sakai

This document records how Apache Pluto 1.1.7 was imported into the Sakai source tree, pruned, adapted to Sakai Maven conventions, and wired into the Sakai reactor.

## Goal

Ship a Sakai-owned build of the Pluto portlet container (Portlet 1.0 RI) from source, using Sakai’s master POM and versioning, building and deploying like any other Sakai module.

Upstream Java package names and artifactIds are deliberately kept. The Maven **groupId** (`org.sakaiproject.pluto`) is the only marker that this is Sakai’s fork — see steps 11 and 13.

## What landed

Top-level directory: `libs/pluto/` (reactor module under `org.sakaiproject:libs`)

Java packages: `org.apache.pluto.*` — unchanged from upstream.

| Module | Maven coordinates |
|--------|-------------------|
| Parent aggregator | `org.sakaiproject.pluto:pluto` |
| Descriptor API | `org.sakaiproject.pluto:pluto-descriptor-api` |
| Descriptor impl | `org.sakaiproject.pluto:pluto-descriptor-impl` |
| Container | `org.sakaiproject.pluto:pluto-container` |
| Tag library | `org.sakaiproject.pluto:pluto-taglib` |

Maven version: Sakai’s platform version (currently `27-SNAPSHOT`), not upstream `1.1.7`.
In `master/pom.xml`, `sakai.pluto.version` is set equal to `sakai.version` so portal deps stay aligned.

Each jar module sets `<deploy.target>shared</deploy.target>` so `sakai:deploy` copies them to Tomcat `shared/lib`.

## Change history

Steps 1–11 landed as a single squashed commit importing Pluto into the source tree. Steps 12 and 13 followed later.

1. **Import Apache Pluto 1.1.7 source** into `pluto/` (no `target/` trees).
2. **Prune** unused upstream modules; keep the four Sakai-consumed artifacts.
3. **Modernize build** for current Maven/JDK (drop broken `pluto-site` remote-resources, fix resources path).
4. **Reparent under Sakai master**; use Sakai-managed dependency versions; Servlet 3.1 stubs for servlet-api 4.
5. **Rename artifacts** to `sak-pluto-*` (later reverted — see step 11).
6. **Align versions** with Sakai’s platform version (`27-SNAPSHOT`).
7. **JDK 17 test fixes** (Castor → Apache Xerces serializer; Maven-style version parsing).
8. **Explicit Castor dep** on `portal-service-impl` (no longer transitive under provided scope).
9. **Rename packages** to `org.sakaiproject.pluto` (later reverted — see step 13); change Maven `groupId` to `org.sakaiproject.pluto`; wire `<module>pluto</module>` into the base reactor; deploy jars via `deploy.target=shared` (removed from `deploy/pom.xml` third-party list).
10. **Freeze for Sakai-only use** — descriptor services are read-only (`portlet.xml` load); drop unused web.xml Castor stack, Maven site/assemble leftovers, and upstream portal-driver README. This tree will not track later Pluto releases.
11. **Migrate to `jakarta.servlet`** for Tomcat 10, and **revert the `sak-pluto-*` artifact rename** back to the upstream `pluto-*` names — the `org.sakaiproject.pluto` groupId already disambiguates these coordinates from `org.apache.pluto`, so prefixing the artifactId was unnecessary.
12. **Move under `libs/`** — relocated from the top-level `pluto/` to `libs/pluto/` and reparented from `org.sakaiproject:master` to the new `org.sakaiproject:libs` base POM, which collects third-party libraries forked into the Sakai tree. The `org.sakaiproject.pluto` groupId and all four artifactIds are unchanged, so `master/pom.xml` dependencyManagement and the portal modules needed no edits. Also fixed the parent `relativePath`, which still pointed at `../master/pom.xml` after the move and silently resolved master from `~/.m2` rather than the working tree.
13. **Revert the package rename** (step 9) back to `org.apache.pluto.*`. Relocating packages solves a different problem than the one Sakai has — it lets two incompatible versions of a library coexist on one classpath. Sakai ships a single Pluto, so nothing needed disambiguating at the classpath level; the `org.sakaiproject.pluto` **groupId** already handles it at the Maven level, which is the same reasoning that reverted the artifactId prefix in step 11.

    The rename was actively harmful here because Pluto is a portlet container. `PortletServlet` is a public extension point that every portlet names in its own `web.xml`, so renaming it broke every portlet not in this tree. It also made a second Pluto on the classpath *silently* coexist as two independent containers with separate `PortletContextManager` state, instead of failing loudly as duplicate classes.

    The groupId, all four artifactIds, and the `master`/portal dependency declarations are unchanged. Reverted before release, so no deployment ever saw `org.sakaiproject.pluto.core.PortletServlet`.

## Intentional exceptions

- **Portlet API 1.0** — Pluto is the Portlet 1.0 RI. Master now manages `portlet-api` at 1.0 tree-wide (it previously managed 3.0.1 for tools, forcing container/taglib to pin 1.0 explicitly; those pins are gone). Portlet 3.0 types methods on `javax.servlet.http.Cookie`/`Part`, which do not exist on Tomcat 10+, and there is no `jakarta.portlet` spec to migrate to.
- **Portlet spec identity in filtered resources** — `javax.portlet.version.major=1` / `minor=0` for `environment.properties`.
- **Read-only `portlet.xml` descriptors** — Sakai only loads portlet descriptors. Castor write/marshall, `xercesImpl`, and the unused web.xml descriptor API were removed (JDK JAXP is enough for unmarshall).
- **Test-only deps not in master** — `jmock` 1.2.0.
- **Maven source layout** — Pluto keeps `src/main/java` (overrides Sakai’s `src/java` default).

## How to build / deploy

As part of a normal Sakai build (reactor includes `libs` before `portal`):

```bash
mvn clean install sakai:deploy -Dmaven.tomcat.home=/path/to/tomcat
```

Standalone from the vendored tree:

```bash
cd libs/pluto
mvn clean install
# optional: deploy just these jars
mvn sakai:deploy -Dmaven.tomcat.home=/path/to/tomcat
```

## Sakai consumers updated

- `master/pom.xml` (dependencyManagement → `org.sakaiproject.pluto:pluto-*`)
- `portal/portal-service-impl`, `portal/portal-render-impl` (deps + Java imports)
- `lti` / `web` portlet `web.xml` — `PortletServlet` class name; renamed in step 9 and restored to the upstream `org.apache.pluto.core.PortletServlet` in step 13
- `docker/tomcat/conf/catalina.properties` and `context.xml` (`pluto-*.jar` patterns)

## Still out of scope

- Tracking later Pluto upstream releases (this copy is frozen at 1.1.7 for Sakai)
- Porting the container to Portlet 2.0 / 3.0 APIs (except as needed for Tomcat 10 / jakarta)

## Source provenance

Upstream: Apache Pluto 1.1.7 (Apache License 2.0)  
https://archive.apache.org/dist/portals/pluto/SOURCES/v1.1.7/

Sakai’s forked copy: https://github.com/sakaicontrib/portals-pluto
