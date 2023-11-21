# sakai-permissions

Handles display and manipulation of permissions for a Sakai tool.

Usage, from the Roster tool:

<sakai-permissions tool="roster"></sakai-permissions>

Other attributes:

bundle-key: Allows to set the bundle name (f.ex: "announcement" or "org.sakaiproject.api.app.messagecenter.bundle.Messages"). By default, it will take the tool attribute value.
on-refresh: Allows to set the return page location. By default, it will refresh the current URL.
group-reference: Allows to set reference to get permissions from. By default, "/site/${portal.siteId}". Order is important. This attribute must be set before the tool attribute.
disabled-groups: Disables all other options apart form "Site" in the Site/Group selector. By default, false (groups are shown). Order is important. This attribute must be set before the tool attribute.

This component needs to be able to lookup a tool's translations, and this happens via the
sakai-i18n.js module, loading the translations from a Sakai web service. The translations need
to be jarred and put in TOMCAT/lib, and the permission translation keys need to start with "perm-",
eg: perm-TOOLPERMISSION.

Example:

perm-roster.viewallmembers = View all participants
perm-roster.viewhidden = View hidden participants
perm-roster.export = Export roster
perm-roster.viewgroup = View groups

## Installation

```bash
npm i @sakai-ui/sakai-permissions
```

## Usage

```html

<sakai-permissions tool="roster"></sakai-permissions>

```

## Linting and formatting

To scan the project for linting and formatting errors, run

```bash
npm run lint:fix
```

## Testing with Web Test Runner

To execute a single test run:

```bash
npm run test
```
