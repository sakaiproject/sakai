const { test, expect } = require('../support/fixtures');

test.describe('Forums (sakai.forums)', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;
  const forumTitle = `Playwright Forum ${Date.now()}`;
  const topicTitle = `Playwright Topic ${Date.now()}`;

  test('creates a site with Forums', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', ['sakai\\.forums']);
  });

  test('can create a forum and add a topic', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick(/Forum|Discussion/i);

    await page.locator('.navIntraTool a, .navIntraTool button').filter({ hasText: /New\s+Forum|Add\s+Forum|New/i }).first().click({ force: true });

    await page.locator('form input[type="text"]:visible').first().fill(forumTitle);
    await page.locator('button[type="submit"]:has-text("Save"), button[type="submit"]:has-text("Create"), input[type="submit"][value*="Save"], input[type="submit"][value*="Create"], .act button:has-text("Save"), .act button:has-text("Create"), .act input[value*="Save"], .act input[value*="Create"]').first().click({ force: true });

    await page.getByRole('link', { name: forumTitle }).first().click({ force: true });

    await page.locator('.navIntraTool a, .navIntraTool button').filter({ hasText: /New\s+Topic|Add\s+Topic|New/i }).first().click({ force: true });
    await page.locator('form input[type="text"]:visible').first().fill(topicTitle);
    await page.locator('button[type="submit"]:has-text("Save"), button[type="submit"]:has-text("Create"), input[type="submit"][value*="Save"], input[type="submit"][value*="Create"], .act button:has-text("Save"), .act button:has-text("Create"), .act input[value*="Save"], .act input[value*="Create"]').first().click({ force: true });

    const topicVisible = await page.getByText(topicTitle).first().isVisible({ timeout: 5000 }).catch(() => false);
    if (!topicVisible) {
      await page.goto(sakaiUrl);
      await sakai.toolClick(/Forum|Discussion/i);
      const forumLink = page.getByRole('link', { name: forumTitle }).first();
      if (await forumLink.count()) {
        await forumLink.click({ force: true });
      }
    }

    await expect(page.getByText(topicTitle).first()).toBeVisible();
  });
});
