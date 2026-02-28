const { test, expect } = require('../support/fixtures');

test.describe('Feedback (sakai.feedback)', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;

  test('creates a site with Feedback', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', ['sakai\\.feedback']);
  });

  test('opens Feedback (generic smoke)', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Contact Us');

    await expect(page.locator('.portletBody').first()).toBeVisible();
  });
});
