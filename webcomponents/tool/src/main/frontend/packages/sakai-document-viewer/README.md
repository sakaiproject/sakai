# @sakai-ui/sakai-document-viewer

The Sakai document viewing component.

## Description

This component provides a viewer for documents in Sakai, supporting various file formats (DOCX, ODP, ODT, PDF).

PDFs are opened with PDF.js, the same plugin used natively by Chrome and Firefox. ODP (slides) are displayed using
ViewerJS. DOCX and ODT are converted to html on the server and retrieved via a Fetch call. ViewerJS and PDF.js loads
happen in an iframe. You can specify the height of that with the height attribute.

## Installation

```bash
npm install @sakai-ui/sakai-document-viewer
```

## Usage

```javascript
import '@sakai-ui/sakai-document-viewer/sakai-document-viewer.js';

// In your HTML
<sakai-document-viewer></sakai-document-viewer>
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
