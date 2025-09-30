# @sakai-ui/sakai-push-utils

A collection of functions related to service worker and push initialization.

## Description

This package provides utilities for working with push notifications and service workers in Sakai.

## Installation

```bash
npm install @sakai-ui/sakai-push-utils
```

## Usage

```javascript
import { pushSetupComplete } from "@sakai-ui/sakai-push-utils";

pushSetupComplete.then(() => console.log("push setup complete"));
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
