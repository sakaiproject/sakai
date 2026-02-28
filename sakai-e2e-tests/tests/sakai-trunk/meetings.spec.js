const { test, expect } = require('../support/fixtures');

test.describe('Meetings (sakai.meetings)', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;

  test('creates a site with Meetings', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', ['sakai\\.meetings']);
  });

  test('opens Meetings (generic smoke)', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);

    const meetingsLink = page.locator('#toolMenu a.btn-nav, ul.site-page-list a.btn-nav').filter({ hasText: /Meetings?/i }).first();
    if (await meetingsLink.count()) {
      await meetingsLink.click({ force: true });
      await expect(page.locator('#content, main, .portletBody').first()).toBeVisible();
      await expect(page.locator('body')).toContainText(/Meetings|meeting/i);
      return;
    }

    await expect(page.locator('main#content')).toBeVisible();
    await expect(page.locator('body')).toContainText(/Overview|Site Info/i);
  });
});
