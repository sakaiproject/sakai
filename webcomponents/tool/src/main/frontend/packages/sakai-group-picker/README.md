# sakai-group-picker

A component that renders a list of Sakai groups and allows you to select one

## Installation

```bash
npm i @sakai-ui/sakai-group-picker
```

## Usage

```html
import { SakaiElement } from '@sakai-ui/sakai-element';
import '@sakai-ui/sakai-group-picker';

class MyElement extends SakaiElement {

  render() {

    return html`
      <sakai-group-picker site-id="xyz"></sakai-group-picker>
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
