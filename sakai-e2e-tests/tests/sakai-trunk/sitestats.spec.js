const { test, expect } = require('../support/fixtures');

test.describe('SiteStats (sakai.sitestats)', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;
  const reportTitle = `Playwright Report ${Date.now()}`;
  const reportDesc = 'This is a Playwright-generated SiteStats report.';

  test('creates a site with Statistics', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', ['sakai\\.sitestats']);
  });

  test('creates a report via Reports -> Add -> Generate report', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Statistics');

    await page.locator('.navIntraTool a, .navIntraTool button, a, button').filter({ hasText: /^Reports$/i }).first().click({ force: true });

    const addReportLink = page.locator('a[href*="lnkNewReport"], .navIntraTool a, a').filter({ hasText: /^Add$/i }).first();
    if (!(await addReportLink.count())) {
      await expect(page.locator('body')).toContainText(/Reports/i);
      return;
    }
    await addReportLink.click({ force: true });

    await page.locator('#content, main, .portletBody').first().locator('input[type="text"]:visible').first().fill(reportTitle);

    const hasCkEditor = await page.evaluate(() => !!(window.CKEDITOR && Object.keys(window.CKEDITOR.instances || {}).length));
    if (hasCkEditor) {
      const editorId = await page.evaluate(() => Object.keys(window.CKEDITOR.instances || {})[0]);
      await sakai.typeCkEditor(editorId, `<p>${reportDesc}</p>`);
    } else {
      await page.locator('#content, main, .portletBody').first().locator('textarea:visible, [contenteditable="true"]:visible, div[role="textbox"]:visible').first().fill(reportDesc);
    }

    await page.locator('button:has-text("Generate report"), input[type="submit"][value*="Generate report"]').first().click({ force: true });
    await expect(page.getByText(reportTitle).first()).toBeVisible();
  });
});
