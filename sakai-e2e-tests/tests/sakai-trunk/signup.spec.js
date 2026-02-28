const { test, expect } = require('../support/fixtures');

test.describe('Sign-Up (sakai.signup)', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;
  const title = `Playwright Sign-Up ${Date.now()}`;
  const location = 'Room 101';
  const category = 'General';
  const description = 'This is a Playwright-created sign-up item.';

  test('creates a site with Sign-Up', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', ['sakai\\.signup']);
  });

  test('adds and publishes a sign-up', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick(/Sign-?Up/i);

    await expect(page.locator('.portletBody').first()).toBeVisible();
    await page.locator('.navIntraTool a, .navIntraTool button').filter({ hasText: /^Add$/i }).first().click({ force: true });

    if (await page.locator('[name="meeting:name"]').count()) {
      await page.locator('[name="meeting:name"]').fill(title);
    } else {
      await page.locator('form input[type="text"]:visible').first().fill(title);
    }

    if (await page.locator('[name="meeting:customLocation"]').count()) {
      await page.locator('[name="meeting:customLocation"]').fill(location);
    } else if (await page.locator('[name="meeting:location"]').count()) {
      await page.locator('[name="meeting:location"]').fill(location);
    } else {
      await page.locator('form input[type="text"]:visible').nth(1).fill(location);
    }

    if (await page.locator('[name="meeting:customCategory"]').count()) {
      const field = page.locator('[name="meeting:customCategory"]').first();
      const tag = await field.evaluate((node) => node.tagName.toLowerCase());
      if (tag === 'select') {
        await field.selectOption({ label: category });
      } else {
        await field.fill(category);
      }
    } else if (await page.locator('[name="meeting:category"]').count()) {
      const field = page.locator('[name="meeting:category"]').first();
      const tag = await field.evaluate((node) => node.tagName.toLowerCase());
      if (tag === 'select') {
        await field.selectOption({ label: category });
      } else {
        await field.fill(category);
      }
    } else {
      const select = page.locator('form select:visible').first();
      if (await select.count()) {
        await select.selectOption({ label: category });
      }
    }

    const hasCkEditor = await page.evaluate(() => !!(window.CKEDITOR && Object.keys(window.CKEDITOR.instances || {}).length));
    if (hasCkEditor) {
      const editorId = await page.evaluate(() => Object.keys(window.CKEDITOR.instances || {})[0]);
      await sakai.typeCkEditor(editorId, `<p>${description}</p>`);
    } else {
      await page.locator('textarea:visible, [contenteditable="true"]:visible, div[role="textbox"]:visible').first().fill(description);
    }

    await page.locator('button:has-text("Next"), input[type="submit"][value*="Next"], .act button:has-text("Next"), .act input[value*="Next"]').first().click({ force: true });
    await page.locator('button:has-text("Publish"), input[type="submit"][value*="Publish"], .act button:has-text("Publish"), .act input[value*="Publish"]').first().click({ force: true });

    await expect(page.getByRole('link', { name: title }).first()).toBeVisible();
  });
});
