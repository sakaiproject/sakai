# @sakai-ui/sakai-date-fns

A collection of functions for date handling in Sakai.

## Description

This package provides utility functions for working with dates in Sakai, built on top of date-fns.

## Installation

```bash
npm install @sakai-ui/sakai-date-fns
```

## Usage

```javascript
import { sakaiFormatDistance } from "@sakai-ui/sakai-date-fns";

const humanizedTimespan = sakaiFormatDistance(new Date(toMillis), new Date());
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
