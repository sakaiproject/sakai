# Vendoring Apache MyFaces Tomahawk into Sakai

This document records why Tomahawk was forked into the Sakai source tree, and how the
vendored copy is intended to differ from upstream.

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

## Planned changes from upstream

1. **Import Tomahawk 1.1.14 source**, including the `org.apache.myfaces.shared_tomahawk.*`
   tree. Upstream ships those 245 classes bundled inside `tomahawk21-1.1.14.jar` alongside the
   other 695 rather than as a separate artifact, so the vendored module reproduces that single-jar
   shape.
2. **Modernise the build** for current Maven and JDK 17 (the upstream build is from 2012).
3. **Migrate `javax.*` to `jakarta.*`** across bytecode *and* resources. The resource side
   matters as much as the source: `META-INF/faces-config.xml` (~40KB), `META-INF/tomahawk.tld`
   (~856KB) and `META-INF/tomahawk.taglib.xml` (~776KB) all reference `javax.faces.*` class
   names as strings, and a source-only migration would leave a jar that loads but fails when
   tag handlers resolve.
4. **Target Jakarta Faces 3.0** rather than upstream's JSF 2.1.

**Not pruned.** Unlike the Pluto fork, the whole component set is kept. Sakai exercises only a
fraction today, but the unused components are plausible replacements for some of the
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

The Maven module exists; the source import is still to come. Until then this module builds an
empty jar.

## Source provenance

Upstream: Apache MyFaces Tomahawk 1.1.14 (Apache License 2.0)
https://archive.apache.org/dist/myfaces/source/

Last upstream release 2012-10-23. This copy is frozen there and will not track upstream.
