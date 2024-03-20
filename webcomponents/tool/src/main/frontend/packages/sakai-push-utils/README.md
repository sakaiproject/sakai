# sakai-push-utils

A set of utility functions for handling translations in Sakai. It converts
java properties file formats into js objects.

## Installation

```bash
npm i @sakai-ui/sakai-push-utils
```

## Usage

```html
import { setup } from "@sakai-ui/sakai-push-utils";

setup.then(() => console.log("push setup complete"));

```

## Linting and formatting

To scan the project for linting and formatting errors, run

```bash
npm run lint
```

To automatically fix linting and formatting errors, run

```bash
npm run format
```

## Testing with Web Test Runner

To execute a single test run:

```bash
npm run test
```

To run the tests in interactive watch mode run:

```bash
npm run test:watch
```
