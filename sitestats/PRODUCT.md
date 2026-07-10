# SiteStats Product Context

## Register

product

## Users

Instructors and site maintainers use SiteStats to understand participation, activity, resources, lessons, and saved reports within a Sakai site. Sakai administrators use the companion admin tool to inspect sites and server-wide activity. Users work inside the existing Sakai portal and expect its navigation, permissions, locale, and timezone conventions.

## Product Purpose

SiteStats turns Sakai event and presence data into understandable site and system reports. The Wicket-to-Thymeleaf conversion succeeds when existing workflows remain familiar, reporting behavior remains trustworthy, and the presentation layer becomes easier to maintain without changing stored data or public JSON contracts.

## Brand Personality

Clear, familiar, and dependable. SiteStats should feel like a current Sakai tool: information-dense where reporting requires it, restrained in presentation, and explicit about actions and errors.

## Anti-references

Do not create a visually separate analytics product inside Sakai. Avoid decorative dashboards, custom control vocabularies, hidden navigation, animation that does not communicate state, or card-heavy layouts that reduce useful data density.

## Design Principles

- Keep the reporting task primary and the framework invisible.
- Preserve familiar Sakai navigation and control patterns.
- Make permissions, filters, empty results, and failures explicit.
- Prefer semantic HTML and progressive enhancement over client-only interaction.
- Reuse the SiteStats JSON and Lit reporting components as the authoritative presentation of report data.

## Accessibility & Inclusion

Target WCAG 2.2 AA. Support keyboard-only operation, visible focus, programmatically associated labels and errors, non-color state cues, responsive reflow, screen-reader landmarks and table semantics, reduced motion, and Sakai-resolved locale and timezone behavior.
