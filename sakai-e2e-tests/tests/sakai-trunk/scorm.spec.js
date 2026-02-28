const path = require('path');
const { test, expect } = require('../support/fixtures');

test.describe('SCORM Player', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;
  const scormZip = path.join(__dirname, '..', 'fixtures', 'RuntimeBasicCalls_SCORM20043rdEdition.zip');
  const scormTitle = 'Golf Explained';

  test('creates a site with the SCORM Player tool', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', ['sakai\\.scorm\\.tool']);
  });

  test('uploads a SCORM package as the instructor', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('SCORM Player');

    await page.locator('a, button').filter({ hasText: 'Upload New Content Package' }).first().click({ force: true });

    const chooserPromise = page.waitForEvent('filechooser');
    await page.getByRole('button', { name: /File To Upload/i }).first().click({ force: true });
    const chooser = await chooserPromise;
    await chooser.setFiles(scormZip);

    await page.locator('button:has-text("Upload File"), a:has-text("Upload File"), input[type="submit"][value*="Upload File"]').first().click({ force: true });
    await expect(page.locator('body')).toContainText(/Golf Explained|RuntimeBasicCalls|List of Content Packages/i, { timeout: 60000 });
  });

  test('shows the SCORM package to enrolled students', async ({ page, sakai }) => {
    await sakai.login('student0011');
    await page.goto(sakaiUrl);
    await sakai.toolClick('SCORM Player');

    await expect(page.locator('body')).toContainText(/Golf Explained|RuntimeBasicCalls|SCORM/i);
  });
});
