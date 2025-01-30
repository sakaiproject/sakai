# sakai-push-utils

A set of utility functions for setting up browser push in Sakai.

## Installation

```bash
npm i @sakai-ui/sakai-push-utils
```

## Usage

```html
import { pushSetupComplete } from "@sakai-ui/sakai-push-utils";

pushSetupComplete.then(() => console.log("push setup complete"));

```

## Linting and formatting

To scan the project for linting and formatting errors, run

```bash
npm run lint:fix
```

## Testing with Web Test Runner

To execute the tests for this module, run

```bash
npm run test
```
