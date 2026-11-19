# @sakai-ui/sakai-dialog

The Sakai dialog component.

## Description

This component creates and styles a dialog element, with subclasses providing the content.

## Installation

```bash
npm install @sakai-ui/sakai-dialog
```

## Usage

Create your subclass with your title and body ...

```js
import { SakaiDialog } from "@sakai-ui/sakai-dialog";

export class MyDialog extends SakaiDialog {

  title() {
    return "My Awesome Dialog";
  }

  body() {
    return html`
      <div>This is amazing!</div>
    `;
  }
}

customElements.define('my-dialog', MyDialog);
```

In your html markup ...

```html
<my-dialog></my-dialog>
```

Show the dialog in your JS ...

```js
this.renderRoot.querySelector("my-dialog").show();
```

## Linting and formatting

To scan the project for linting and formatting errors, run ...

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
