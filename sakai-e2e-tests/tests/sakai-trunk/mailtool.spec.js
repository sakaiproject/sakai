const { test, expect } = require('../support/fixtures');

test.describe('Email (sakai.mailtool)', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;
  const subject = `Playwright Mail ${Date.now()}`;
  const body = 'This is a Playwright test email body.';

  test('creates a site with Email', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', ['sakai\\.mailtool']);
  });

  test('sends an email to All', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Email');

    const allLabel = page.locator('label').filter({ hasText: /^All$/ }).first();
    if (await allLabel.count()) {
      const forId = await allLabel.getAttribute('for');
      if (forId) {
        await page.locator(`#${forId.replace(/([:.])/g, '\\\\$1')}`).check({ force: true });
      } else {
        await allLabel.locator('..').locator('input[type="checkbox"]').first().check({ force: true });
      }
    } else {
      await page.locator('input[type="checkbox"]:visible').first().check({ force: true });
    }

    await page.locator('form input[type="text"]:visible').first().fill(subject);

    const hasCkEditor = await page.evaluate(() => !!(window.CKEDITOR && Object.keys(window.CKEDITOR.instances || {}).length));
    if (hasCkEditor) {
      const editorId = await page.evaluate(() => Object.keys(window.CKEDITOR.instances || {})[0]);
      await sakai.typeCkEditor(editorId, `<p>${body}</p>`);
    } else {
      await page.locator('textarea:visible, [contenteditable="true"]:visible, div[role="textbox"]:visible').first().fill(body);
    }

    await page.locator('button:has-text("Send Mail"), input[type="submit"][value*="Send Mail"]').first().click({ force: true });
    await expect(page.getByText(/Message sent to/i)).toBeVisible();
  });
});
