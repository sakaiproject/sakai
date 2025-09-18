# @sakai-ui/sakai-i18n

A collection of functions to allow code to get Sakai translations.

## Description

This package provides internationalization utilities for Sakai, allowing components to access translations. It converts
java properties file formats into js objects.

## Installation

```bash
npm install @sakai-ui/sakai-i18n
```

## Usage

```javascript
import { loadProperties, tr } from "@sakai-ui/sakai-i18n";

loadProperties("mybundle").then(r => {
  console.log(r.sometranslationkey);
});

const formattedValue = tr("mybundle", "sometranslationkey", ["sub1"]);
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
