# @sakai-ui/sakai-permissions

The Sakai permissions component.

## Description

This component provides an interface for managing permissions in Sakai. It handles display and manipulation of permissions for a Sakai tool.

### Attributes

- **tool**: The tool ID (required)
- **bundle-key**: Allows to set the bundle name (e.g., "announcement"). By default, it will use the tool attribute value.
- **on-refresh**: Allows to set the return page location. By default, it will refresh the current URL.
- **group-reference**: Allows to set reference to get permissions from. By default, "/site/${portal.siteId}".
- **disable-groups**: Disables group permissions editing

## Installation

```bash
npm install @sakai-ui/sakai-permissions
```

## Usage

```javascript
import '@sakai-ui/sakai-permissions/sakai-permissions.js';

// In your HTML
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

## License

ECL-2.0
