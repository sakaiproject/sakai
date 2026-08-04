# SAK-52804 — Vendoring Apache Pluto into Sakai

This document records how Apache Pluto 1.1.7 was imported into the Sakai source tree, pruned, adapted to Sakai Maven conventions, renamed, and wired into the Sakai reactor.

Branch: `SAK-52804`

## Goal

Ship a Sakai-owned build of the Pluto portlet container (Portlet 1.0 RI) from source, under Sakai package/artifact names, using Sakai’s master POM and versioning, building and deploying like any other Sakai module.

## What landed

Top-level directory: `pluto/` (reactor module)

Java packages: `org.sakaiproject.pluto.*`

| Module | Maven coordinates |
|--------|-------------------|
| Parent aggregator | `org.sakaiproject.pluto:sak-pluto` |
| Descriptor API | `org.sakaiproject.pluto:sak-pluto-descriptor-api` |
| Descriptor impl | `org.sakaiproject.pluto:sak-pluto-descriptor-impl` |
| Container | `org.sakaiproject.pluto:sak-pluto-container` |
| Tag library | `org.sakaiproject.pluto:sak-pluto-taglib` |

Version: `${sakai.pluto.version}` → `${sakai.version}` (currently `27-SNAPSHOT`)

Each jar module sets `<deploy.target>shared</deploy.target>` so `sakai:deploy` copies them to Tomcat `shared/lib`.

## Commit history (on `SAK-52804`)

1. **Import Apache Pluto 1.1.7 source** into `pluto/` (no `target/` trees).
2. **Prune** unused upstream modules; keep the four Sakai-consumed artifacts.
3. **Modernize build** for current Maven/JDK (drop broken `pluto-site` remote-resources, fix resources path).
4. **Reparent under Sakai master**; use Sakai-managed dependency versions; Servlet 3.1 stubs for servlet-api 4.
5. **Rename artifacts** to `sak-pluto-*`.
6. **Align versions** with `${sakai.version}` / `27-SNAPSHOT`.
7. **JDK 17 test fixes** (Castor → Apache Xerces serializer; Maven-style version parsing).
8. **Explicit Castor dep** on `portal-service-impl` (no longer transitive under provided scope).
9. **Rename packages** to `org.sakaiproject.pluto`; change Maven `groupId` to `org.sakaiproject.pluto`; wire `<module>pluto</module>` into the base reactor; deploy jars via `deploy.target=shared` (removed from `deploy/pom.xml` third-party list).

## Intentional exceptions

- **Portlet API 1.0** — Pluto is the Portlet 1.0 RI. Master manages `portlet-api` 3.0.1 for tools; container/taglib keep an explicit `portlet-api:1.0`.
- **Portlet spec identity in filtered resources** — `javax.portlet.version.major=1` / `minor=0` for `environment.properties`.
- **Read-only descriptors** — Sakai only loads `portlet.xml` / related descriptors. Castor write/marshall support and `xercesImpl` were removed from this frozen tree (JDK JAXP is enough for unmarshall).
- **Test-only deps not in master** — `jmock` 1.2.0.
- **Maven source layout** — Pluto keeps `src/main/java` (overrides Sakai’s `src/java` default).

## How to build / deploy

As part of a normal Sakai build (reactor includes `pluto` before `portal`):

```bash
mvn clean install sakai:deploy -Dmaven.tomcat.home=/path/to/tomcat
```

Standalone from the vendored tree:

```bash
cd pluto
mvn clean install
# optional: deploy just these jars
mvn sakai:deploy -Dmaven.tomcat.home=/path/to/tomcat
```

## Sakai consumers updated

- `master/pom.xml` (dependencyManagement → `org.sakaiproject.pluto:sak-pluto-*`)
- `portal/portal-service-impl`, `portal/portal-render-impl` (deps + Java imports)
- `lti` / `web` portlet `web.xml` (`PortletServlet` class name)
- `docker/tomcat/conf/catalina.properties` and `context.xml` (`sak-pluto-*.jar` patterns)

## Still out of scope

- Porting the container to Portlet 2.0 / 3.0 APIs

## Source provenance

Upstream: Apache Pluto 1.1.7 (Apache License 2.0)  
https://archive.apache.org/dist/portals/pluto/SOURCES/v1.1.7/

Checksums and signature for the upstream `pluto-1.1.7-src.zip` are in `pluto/original/`
(see also `Pluto-Distribution.html` for the Apache archive listing).
