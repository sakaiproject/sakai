# @sakai-ui/sakai-element

A base class for Sakai UI web components.

## Description

This package provides a base class for Sakai web components, with common functionality and utilities. It renders to the light dom and gives us the loadTranslations and tr methods.

## Installation

```bash
npm install @sakai-ui/sakai-element
```

## Usage

```javascript
import { SakaiElement } from '@sakai-ui/sakai-element';

class MyComponent extends SakaiElement {
  // Implement your component logic here
}
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
