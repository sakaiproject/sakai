const { test, expect } = require('../support/fixtures');

test.describe('Polls (sakai.poll)', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;
  const pollTitle = `Playwright Poll ${Date.now()}`;

  test('creates a site with Polls', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', ['sakai\\.poll']);
  });

  test('can create a poll with two options', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick(/Poll/i);

    await page.locator('.navIntraTool a, .navIntraTool button, ul.nav a').filter({ hasText: /Add|New/i }).first().click({ force: true });

    await page.locator('form:visible input[type="text"]').first().fill(pollTitle);

    const now = new Date();
    const yyyy = now.getFullYear();
    const mm = String(now.getMonth() + 1).padStart(2, '0');
    const dd = String(now.getDate()).padStart(2, '0');
    const hh = String(now.getHours()).padStart(2, '0');
    const mi = String(now.getMinutes()).padStart(2, '0');
    const datetime = `${yyyy}-${mm}-${dd}T${hh}:${mi}`;

    await page.locator('input[name="openDate"]').fill(datetime);
    await page.locator('input[name="closeDate"]').fill(datetime);

    await page.locator('form:visible').first().locator('button[type="submit"]:has-text("Save"), button[type="submit"]:has-text("Add"), button[type="submit"]:has-text("Create"), button[type="submit"]:has-text("Continue"), input[type="submit"][value*="Save"], input[type="submit"][value*="Add"], input[type="submit"][value*="Create"], input[type="submit"][value*="Continue"], .act button:has-text("Save"), .act button:has-text("Add"), .act button:has-text("Create"), .act button:has-text("Continue"), .act input[value*="Save"], .act input[value*="Add"], .act input[value*="Create"], .act input[value*="Continue"]').first().click({ force: true });

    await expect(page.locator('textarea')).toBeVisible();

    await page.locator('textarea').fill('Yes');
    await page.locator('button:has-text("Save"), input[type="submit"][value*="Save"]').first().click({ force: true });

    await expect(page.locator('.sak-banner-success')).toBeVisible();
    await page.locator('input[type="button"][value="Add option"]').click({ force: true });

    await page.locator('textarea').fill('No');
    await page.locator('button:has-text("Save"), input[type="submit"][value*="Save"]').first().click({ force: true });

    await expect(page.locator('.sak-banner-success')).toBeVisible();
    await page.locator('button:has-text("Save"), input[type="submit"][value*="Save"]').first().click({ force: true });

    await expect(page.locator('.sak-banner-success')).toContainText('Poll saved successfully');
    await expect(page.locator('body')).toContainText(pollTitle);
  });
});
