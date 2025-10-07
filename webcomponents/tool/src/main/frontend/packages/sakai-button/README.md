# @sakai-ui/sakai-button

A Sakai button component.

## Description

A customizable button component for Sakai.

## API

### Properties

| Property | Attribute | Type | Default | Description |
|----------|-----------|------|---------|-------------|
| text | text | String | '' | The button text |
| primary | primary | Boolean | false | Whether this is a primary button |
| secondary | secondary | Boolean | false | Whether this is a secondary button |
| disabled | disabled | Boolean | false | Whether the button is disabled |

### Events

- `click`: Standard click event

## Installation

```bash
npm install @sakai-ui/sakai-button
```

## Usage

```javascript
import '@sakai-ui/sakai-button/sakai-button.js';

// In your HTML
<sakai-button>Click Me</sakai-button>
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
