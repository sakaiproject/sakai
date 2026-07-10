# SiteStats Design Context

## Visual System

SiteStats uses the Sakai portal's Bootstrap 5.2-based product UI. The conversion should inherit Sakai skin colors, typography, spacing, focus treatment, banners, buttons, forms, tables, and responsive utilities rather than introducing a tool-specific theme.

## Layout

- Use `.portletBody` as the tool container and `.navIntraTool` for primary tool navigation.
- Keep one clear `h1` per view and use semantic sections and fieldsets beneath it.
- Use single-column forms with grouped controls; allow report tables and filter toolbars to use the width they need.
- Wrap wide tables in `.table-responsive` and keep actions adjacent to the object they affect.

## Components

- Use Sakai banner classes for success, information, warning, and error messages.
- Use Bootstrap buttons, form controls, checks, tables, pagination, and responsive utilities consistently.
- Keep `<sakai-sitestats-report-panel>` as the report table/chart surface.
- Use native links for navigation and POST forms for mutations; do not simulate either with generic elements.

## Interaction and Accessibility

- Controls have visible labels, keyboard focus, and at least a practical 44-by-44 CSS-pixel target where the surrounding Sakai layout permits.
- Validation appears after interaction or submission and connects errors with `aria-describedby` or `aria-errormessage`.
- Dynamic status messages use appropriate status or alert semantics.
- JavaScript enhances behavior but is not required to submit forms, navigate, or recover from errors.
- Motion is limited to existing Sakai state transitions and honors reduced-motion preferences.
