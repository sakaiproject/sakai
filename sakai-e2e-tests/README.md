# Sakai E2E Tests (Playwright)

Playwright end-to-end tests for Sakai live in `sakai-e2e-tests/tests`.

## Run locally

From `sakai-e2e-tests/`:

```bash
npm ci
npm run install:browsers
```

Run all tests against local Sakai:

```bash
PLAYWRIGHT_BASE_URL=http://127.0.0.1:8080 npx playwright test
```

Run all tests against another instance:

```bash
PLAYWRIGHT_BASE_URL=https://sakai.example npx playwright test
```

Run one spec:

```bash
PLAYWRIGHT_BASE_URL=https://sakai.example npx playwright test tests/sakai-trunk/assignment.spec.js
```

## Adding tests

- Add new specs in `tests/sakai-trunk/` using `*.spec.js`.
- Import the shared fixture:

```js
const { test, expect } = require('../support/fixtures');
```

- Prefer state-based waits (`expect(...).toBeVisible()`, `waitForLoadState`) over fixed sleeps.
- Use deterministic selectors (role/text/labels) and avoid brittle CSS when possible.
- If a test depends on shared state across steps in one file, use serial mode:
  `test.describe.configure({ mode: 'serial' });`

## Available helpers (`tests/support/fixtures.js`)

The `sakai` fixture exposes:

- `sakai.login(username)`
- `sakai.goto(pathOrUrl)`
- `sakai.toolClick(labelOrRegex)`
- `sakai.createCourse(username, toolIds)`
- `sakai.createRubric(instructor, sakaiUrl)`
- `sakai.typeCkEditor(editorId, html)`
- `sakai.selectDate(selector, value)`
- `sakai.checkA11y(impacts)`
- `sakai.expectInViewport(locatorOrSelector)`
- `sakai.expectNotInViewport(locatorOrSelector)`
- `sakai.randomId()`

## Why we run with one worker

`playwright.config.js` pins `workers: 1` on purpose.

These tests share Sakai accounts and mutate user/site UI state (tool menu state, site setup, in-flight edits). Parallel workers cause cross-test interference and flaky navigation/session behavior. Use one worker unless tests are redesigned to be fully isolated (separate users/data per worker).
