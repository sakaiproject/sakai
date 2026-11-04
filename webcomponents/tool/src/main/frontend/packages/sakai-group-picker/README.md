# @sakai-ui/sakai-group-picker

Sakai group picker component.

## Description

This component provides a picker for selecting groups in Sakai. It renders a list of Sakai groups and allows you to select one.

## Installation

```bash
npm install @sakai-ui/sakai-group-picker
```

## Usage

```javascript
import '@sakai-ui/sakai-group-picker/sakai-group-picker.js';

// In your HTML
<sakai-group-picker site-id="xyz"></sakai-group-picker>
```

### Enhance an existing group filter

Load `/webcomponents/bundles/group-picker.js` and call
`document.createElement("sakai-group-picker").bindSelect(select)` after the
component is defined. The component adds a searchable combobox and keeps the native
select in its form. The tool supplies the options, default selection, and
change handler. In multiple mode, bulk actions in the dropdown change matching enabled
options and preserve selections outside the search.

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

For a group selection field rendered by a tool, load the same bundle and wrap
the existing select in `<sakai-group-picker bind-select>`. Use
`no-bulk-actions` on the wrapper for forms that only offer individual choices.
Keep the native select's form name, label, options, and selected values.
