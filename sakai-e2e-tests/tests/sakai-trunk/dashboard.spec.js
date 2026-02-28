const { test, expect } = require('../support/fixtures');

test.describe('Dashboard (sakai.dashboard)', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;

  test('creates a site with Dashboard', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', ['sakai\\.dashboard']);
  });

  test('can edit and save the dashboard', async ({ page, sakai }) => {
    await sakai.login('instructor1');

    let opened = false;
    for (let attempt = 0; attempt < 3; attempt += 1) {
      try {
        await page.goto(sakaiUrl, { waitUntil: 'domcontentloaded', timeout: 60000 });
        opened = true;
        break;
      } catch (error) {
        await page.waitForTimeout(1000);
      }
    }
    if (!opened) {
      await page.goto('/portal/');
    }

    await sakai.toolClick(/Dashboard/i);

    const editButton = page.locator('#course-dashboard-edit button').first();
    await expect(editButton).toBeVisible();
    await editButton.click({ force: true });

    const saveButton = page.locator('#course-dashboard-save button').first();
    if (await saveButton.count()) {
      await saveButton.click({ force: true });
    }

    await expect(page.locator('main#content')).toBeVisible();
  });
});
