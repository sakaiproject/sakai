# Samigo Author Index Facelets Migration Learnings

## Summary
We modernised the Tests & Quizzes (Samigo) author landing flow by replacing its JSP entry point and legacy Facelets fragments with well-formed `.xhtml` views. A new `MainIndexRouter` bean now performs the legacy listener setup and redirects into the correct view, ensuring JSF lifecycle stages are respected. All view-layer fragments now render without `FacesContext` NPEs or malformed markup.

## Key Changes
- **New entry view:** `jsf/index/mainIndex.xhtml` fires `MainIndexRouter.prepare()` on `preRenderView`, replacing the old `mainIndex.jsp` scriptlet logic.
- **Routing bean:** `MainIndexRouter` (request scoped) invokes `AuthorActionListener` or `SelectActionListener` and issues a redirect to `/jsf/author/authorIndex_container.xhtml` or the legacy select JSP.
- **Servlet default:** `WEB-INF/web.xml` now points `SamigoJsfTool` to `jsf/index/mainIndex.xhtml`.
- **Author view cleanup:** `authorIndex_container.xhtml` provides the `<h:head>/<h:body>` wrapper; `authorIndex_content.xhtml` sheds all `<f:verbatim>` blocks in favour of standard HTML/JSF markup and safe EL expressions.
- **Build guard:** `xml-maven-plugin` validates every `.xhtml` during `mvn validate`, catching malformed XML before deployment.

- **Portal header scriptlet replaced:** Added `PortalHeaderConfigurator` to replicate the legacy `header.inc` logic when rendering Facelets views, ensuring portal-provided head/body attributes remain available.
- **JSF client shim:** Because the portal routes resources under `*/javax.faces.resource`, an inline `mojarra.jsfcljs` shim now backs legacy onclick handlers until portal resource mapping can deliver the standard `jsf.js`.


## Gotchas & Lessons Learned
- **Inline script/markup:** Template literals (`` `...` ``) and EL inside JavaScript need quoting converted (`'...'`) and must stay inside CDATA to keep Facelets (XML) parsers happy.
- **Attributes cannot span JSF fragments:** Splitting `<button>` attributes across `f:verbatim` chunks leads to invalid XML. Use full HTML tags with EL/resolved attributes instead.
- **JSP → Facelets requires lifecycle-aware routing:** JSF expects a `FacesContext` before running backing logic; we must move entry logic into a managed bean fired during JSF lifecycle, not raw servlet forwards.
- **Keep both admin & instructor flows intact:** Listeners (`AuthorActionListener`, `SelectActionListener`) still prepare state prior to redirecting; no behaviour changes for end users.

## Next Steps
1. Gradually migrate remaining JSP-based fragments (e.g., select flow) to `.xhtml` using the same patterns.
2. Extend XML validation to other Samigo Facelets directories as they’re introduced.
3. Consider consolidating duplicated dropdown markup into reusable components once all views are Facelets-based.
