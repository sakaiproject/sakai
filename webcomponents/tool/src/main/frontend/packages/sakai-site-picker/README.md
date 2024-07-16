# sakai-site-picker

A component that renders a list of Sakai sites and allows you to select one

## Installation

```bash
npm i @sakai-ui/sakai-site-picker
```

## Usage

```html
import { SakaiElement } from '@sakai-ui/sakai-element';
import '@sakai-ui/sakai-site-picker';

class MyElement extends SakaiElement {

  render() {

    return html`
      <sakai-site-picker sites='["site1", "site2"]'></sakai-site-picker>
    `;
  }
}
```

## Linting and formatting

To scan the project for linting and formatting errors, run

```bash
npm run lint
```

## Testing with Web Test Runner

To execute a single test run:

```bash
npm run test
```
