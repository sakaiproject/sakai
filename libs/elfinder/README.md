# Vendoring elFinder's Java servlet backend into Sakai

This document records why `elfinder-servlet-2` was forked into the Sakai source tree, and how
the vendored copy differs from upstream.

## Why

`textarea/elfinder-sakai` (module `elfinder-connector`) implements Sakai's elFinder file-manager
tool on top of `com.github.bluejoe2008:elfinder-servlet-2:1.2`, the Java backend for elFinder.
That artifact was last released in 2015, targets Servlet 3.0 (`javax.servlet`), and has no
jakarta-namespace build — Maven Central has nothing newer under `com.github.bluejoe2008` or its
successor groups. So Sakai maintains its own build, the same approach already used for
[[libs/pluto]] and [[libs/tomahawk]].

## What lands

Top-level directory: `libs/elfinder/` (reactor module under `org.sakaiproject:libs`)

Java packages: `cn.bluejoe.elfinder.*` — **unchanged from upstream**, matching the Tomahawk
fork's approach. `elfinder-sakai`'s Spring XML (`elfinder-servlet.xml`) references several of
these classes by fully-qualified name as XML bean strings, so renaming would break wiring for no
benefit.

| Module | Maven coordinates |
|--------|-------------------|
| elFinder servlet backend | `org.sakaiproject.elfinder:elfinder-servlet` |

Maven version: Sakai's platform version (currently `27-SNAPSHOT`), not upstream `1.2`, matching
the Pluto/Tomahawk convention.

### Consumers

`textarea/elfinder-sakai` is the only consumer. It previously depended directly on
`com.github.bluejoe2008:elfinder-servlet-2:1.2:classes`; that dependency (and its `log4j` and
`jakarta.mail-api` exclusions, no longer needed) was replaced with a plain dependency on
`org.sakaiproject.elfinder:elfinder-servlet`, version-managed in `master/pom.xml`.

`SakaiTmbCommandExecutor` in `elfinder-sakai` imports `org.springframework.http.HttpHeaders`
directly, which it previously got transitively through the old dependency's own (non-`provided`)
declaration of `spring-webmvc`. Since this fork declares `spring-webmvc` `provided` (matching
Sakai's tree-wide convention — Spring ships on the shared classpath, see `deploy/pom.xml`),
`elfinder-sakai/pom.xml` now declares `spring-web` `provided` directly.

## Changes from upstream

Source came from `elfinder-servlet-2-1.2-sources.jar`. 48 Java files, 26 touched by the
`javax`→`jakarta` migration.

1. **Migrated three `javax` prefixes to `jakarta`**: `javax.servlet` (22 files),
   `javax.annotation.Resource` (1 file, `ConnectorController`), and
   `javax.mail.internet.MimeUtility` (1 file, `FileCommandExecutor` — a single static
   `encodeText` call for a `Content-Disposition` filename).

   `javax.imageio.*` (used for thumbnail generation in `DimCommandExecutor` /
   `TmbCommandExecutor`) is a JDK API, not part of Jakarta EE, and was left untouched.

2. **`commons-fileupload` 1.x → `commons-fileupload2-jakarta-servlet6`** (2 files,
   `ConnectorController` and `UploadCommandExecutor`). Upstream's `commons-fileupload` is
   javax-servlet-only; Sakai already ships `commons-fileupload2-jakarta-servlet6` (see
   [[libs/tomahawk]], which made the same change). `ServletFileUpload` → `JakartaServletFileUpload`,
   `FileItemStream` → `FileItemInput`, `FileItemIterator` → `FileItemInputIterator`,
   `item.openStream()` → `item.getInputStream()`, `setHeaderEncoding(String)` →
   `setHeaderCharset(Charset)`. `org.apache.commons.fileupload.util.Streams.asString(...)` has
   no fileupload2 equivalent; replaced with `IOUtils.toString(...)` (commons-io was already a
   dependency). The dynamic `Proxy` in `ConnectorController` that wraps a re-readable
   `FileItemStream` was updated to implement `FileItemInput` and intercept `getInputStream`
   instead of `openStream`.

3. **`org.apache.log4j.Logger` → `slf4j`** (2 files, `AbstractCommandExecutor` and
   `DefaultFsService`). Not a `commons-logging` case (so `jcl-over-slf4j` doesn't apply here) —
   upstream called the log4j 1.x API directly, which Sakai does not otherwise carry. Both files
   had few call sites, so they were switched to `org.slf4j.Logger` directly rather than adding a
   `log4j-over-slf4j` bridge dependency for two files.

4. **`org.json:json` bumped from `20090211` (upstream) to Sakai's managed
   `${sakai.org.json.version}`** (`20210307`) — no API changes needed.

### Not changed

Everything else upstream depended on (`commons-codec`, `commons-io`, `commons-lang3`,
`spring-webmvc`, `com.mortennobel:java-image-scaling` for thumbnail scaling) has no
Jakarta-namespace surface and was left as-is, just re-pointed at Sakai's managed versions where
one exists.

## Status

Builds clean against Jakarta Servlet 6 / Jakarta Mail 2 / Jakarta Annotations 3. Not yet
exercised at runtime — verify file browse/upload/rename/thumbnail/download through the elFinder
tool after deploying.

## Source provenance

Upstream: `com.github.bluejoe2008:elfinder-servlet-2:1.2` (BSD 2-Clause License)
https://github.com/bluejoe2008/elfinder-2.x-servlet

Last upstream release 2015. This copy is frozen there and will not track upstream.
