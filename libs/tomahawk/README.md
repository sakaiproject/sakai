# Vendoring Apache MyFaces Tomahawk into Sakai

This document records why Tomahawk was forked into the Sakai source tree, and how the
vendored copy differs from upstream.

## Why

Sakai's Jakarta migration targets **Jakarta Faces 3.0**, which keeps the JSP tag support that
Faces 4.0 removed. Tomahawk is the remaining blocker: it has **no jakarta-namespace build at
any version**. Every artifact in `org.apache.myfaces.tomahawk` on Maven Central stops at
`1.1.14`, released 2012-10-23 — roughly eight years before the jakarta namespace existed. The
actively maintained MyFaces component library (Tobago) is a different, full-framework project
and is not a drop-in replacement.

So Sakai maintains its own build.

## What lands

Top-level directory: `libs/tomahawk/` (reactor module under `org.sakaiproject:libs`)

Java packages: `org.apache.myfaces.*` — **unchanged from upstream**, unlike the Pluto fork
which was repackaged. Renaming would buy nothing and would break existing imports in
`sections-app` and `signup`.

| Module | Maven coordinates |
|--------|-------------------|
| Component library | `org.sakaiproject.tomahawk:tomahawk` |

Maven version: Sakai's platform version (currently `27-SNAPSHOT`), not upstream `1.1.14`,
matching the Pluto convention.

The module sets `<deploy.target>shared</deploy.target>` so `sakai:deploy` copies the jar to
Tomcat `shared/lib`. `docker/tomcat/conf/context.xml` already lists `tomahawk*.jar` in its
`tldScan` filter.

## Changes from upstream

Source came from `tomahawk21-1.1.14-sources.jar` — the exact source behind the binary Sakai
ran previously, so the pre-generated taglib classes under `org.apache.myfaces.generated` are
included and the long-dead `myfaces-builder-plugin` never has to run. 714 Java files and 399
resources; the `org.apache.myfaces.shared_tomahawk.*` tree is included rather than split out,
matching upstream's single-jar shape.

1. **Migrated `javax.*` to `jakarta.*`** across source *and* resources — 559 files. The
   resource side matters as much as the source: `META-INF/faces-config.xml`,
   `META-INF/tomahawk.tld` (~856KB) and `META-INF/tomahawk.taglib.xml` (~776KB) reference
   Faces class names and component-type identifiers as **strings**, so a source-only migration
   would have produced a jar that loads and then fails when tag handlers resolve.

   Exactly three prefixes were migrated: `javax.faces`, `javax.el`, `javax.servlet`.

2. **`javax.portlet` deliberately NOT migrated** (14 files). The Portlet spec (JSR 168/286/362)
   was never part of Jakarta EE, so no `jakarta.portlet` exists — Maven Central has no
   `jakarta.portlet-api` at any version. `javax.portlet` is the current namespace, not a
   legacy one. The module compiles against Portlet 1.0, which has 27 classes and zero
   references to any servlet namespace.

3. **Two `javax/servlet` strings deliberately kept.** `shared_tomahawk/webapp/webxml/WebXmlParser`
   uses `javax/servlet/resources/web-app_2_2.dtd` and `..._2_3.dtd` as classpath EntityResolver
   fallbacks for ancient `web.xml` DOCTYPEs. There is no `jakarta/servlet/resources/`
   equivalent, so renaming these would break a working fallback rather than fix it.

4. **commons-fileupload 1.x → fileupload2** (8 files). Upstream's fileupload is javax-servlet
   only and has no jakarta build; Sakai already ships `commons-fileupload2-jakarta-servlet6`.
   This was an API migration, not a rename: `setSizeMax`/`setFileSizeMax` became
   `setMaxSize`/`setMaxFileSize`, `DiskFileItemFactory` is builder-constructed,
   `setHeaderEncoding(String)` became `setHeaderCharset(Charset)`, `FileItemFactory.createItem(...)`
   became `fileItemBuilder()...get()`, `BoundedInputStream.raiseError` became `onMaxLength`, and
   `FileItem.get()`/`getString()`/`delete()` now declare `IOException`. The `FileUploadIOException`
   tunnelling upstream needed is gone entirely — in fileupload2 `FileUploadException extends
   IOException`, so there is nothing to tunnel through.

5. **Compatibility stubs for APIs added since 2012.** Tomahawk targets JSF 2.1 / Servlet 2.5:
   - `isReady()` / `setWriteListener(...)` on three `ServletOutputStream` subclasses (Servlet 3.1)
   - `encodeWebsocketURL(String)` on two `ExternalContext` wrappers (JSF 2.3)
   - `setStatus(int, String)` delegates to `setStatus(int)` (the two-arg form was removed in Servlet 6.0)

6. **Ten files converted from ISO-8859-1 to UTF-8** — four Java sources and six `Messages_*.properties`
   bundles. Worth knowing for future vendoring: `grep -rl` treats invalid-UTF-8 files as binary
   and silently skips them, so those four Java files escaped the first migration pass entirely
   and were only caught by the compiler.

7. **`jcl-over-slf4j` rather than `commons-logging`** for the 118 JCL imports. commons-logging is
   banned tree-wide in Sakai; `master/pom.xml` excludes it from every transitive path.

8. **Build modernised** for Maven 3 and JDK 17, targeting Jakarta Faces 3.0 rather than JSF 2.1.

### Removed

Four files, none referenced by `faces-config.xml` or the TLD:

- `tomahawk/application/jsp/JspTilesViewHandlerImpl` and `JspTilesTwoViewHandlerImpl` — Struts 1
  / Tiles 2 integration. Upstream marks both dependencies `optional`; both are javax-only and
  Struts 1 has been EOL since 2013, so there is no Jakarta path.
- `webapp/filter/PortletMultipartRequestWrapper` and
  `webapp/filter/portlet/PortletChacheFileSizeErrorsFileUpload` — fileupload2 ships no portlet
  variant. `PortletUtils` was kept with only its `isMultipartContent` method removed, so its
  three non-portlet callers are untouched; the portlet branch in `TomahawkFacesContextWrapper`
  now always sees `multipartContent = false`. **Portlet multipart upload is unsupported in this
  fork**; servlet multipart handling is unaffected.

**Otherwise not pruned.** Unlike the Pluto fork, the whole component set is kept. Sakai exercises
only a fraction today, but the unused components are plausible replacements for some of the
hand-maintained `jsf2-widgets` tags later.

Upstream `sandbox` is a separate experimental component set that has never been on Sakai's
classpath and is out of scope — this fork covers the core library only, matching what the
shipped `tomahawk21` artifact contains.

## Sakai consumers

Taglib usage — 16 distinct tags, prefix `t:`, across samigo-app (30 JSPs), signup and chat
tools (20), user-tool-prefs (9), messageforums-app (6), syllabus-app (1):

```
t:div 97   t:column 87   t:radio 82   t:commandSortHeader 53   t:dataList 47
t:selectOneRadio 40   t:dataTable 31   t:aliasBean 15   t:outputText 9
t:htmlTag 7   t:fieldset 4   t:columns 4   t:selectOneMenu 3
t:inputHidden 2   t:selectItems 1   t:selectBooleanCheckbox 1
```

Java imports — `sections-app` and `signup` only:

- `org.apache.myfaces.shared_tomahawk.renderkit.html.HTML`
- `org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlLinkRendererBase`
- `org.apache.myfaces.custom.sortheader.HtmlCommandSortHeader`
- `org.apache.myfaces.component.UserRoleUtils`
- `org.apache.myfaces.component.html.ext.HtmlDataTable`

These 16 tags and 5 imports are the smoke-test surface for the migration.

## Status

Builds clean: 714 source files compile against Jakarta Faces 3.0, producing 927 classes and
580 resources.

Verified in the built jar: zero `javax/faces`, `javax/el` or `javax/servlet` class references
(`jakarta/faces` in 568 files, `jakarta/servlet` in 81); `javax/portlet` retained in 11 by
design; `faces-config.xml` and `tomahawk.tld` carry 60 and 152 `jakarta.faces` references
respectively and no `javax.faces`.

Not yet exercised at runtime. The smoke-test surface is the 16 tags and 5 imports listed above.

## Source provenance

Upstream: Apache MyFaces Tomahawk 1.1.14 (Apache License 2.0)
https://archive.apache.org/dist/myfaces/source/

Last upstream release 2012-10-23. This copy is frozen there and will not track upstream.
