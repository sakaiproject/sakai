const { test, expect } = require('../support/fixtures');

test.describe('Become User', () => {
  test.describe.configure({ mode: 'serial' });

  test('Administration Workspace - Become User', async ({ page, sakai }) => {
    await sakai.login('admin');
    await page.goto('/portal/site/!admin');

    await expect(page.getByRole('link', { name: /Administration Workspace/i }).first()).toBeVisible();
    await sakai.toolClick(/Become User/i);

    const becomeUserForm = page.locator('#su').first();
    if (!(await becomeUserForm.isVisible({ timeout: 10000 }).catch(() => false))) {
      const becomeUserLink = page.getByRole('link', { name: /^Become User$/i }).first();
      const href = await becomeUserLink.getAttribute('href');
      if (href) {
        await page.goto(href);
      }
    }
    await expect(becomeUserForm).toBeVisible();
    const becomeUserInput = becomeUserForm.locator('input[type="text"]').first();
    await expect(becomeUserInput).toBeVisible();
    await becomeUserInput.fill('instructor1');
    await page.locator('#su\\:become').click();

    await page.goto('/portal/site/!admin');
    await expect(page.getByRole('link', { name: /^Site Unavailable$/i }).first()).toBeVisible();

    await page.locator('.sak-sysInd-account').click();
    await page.locator('a#loginLink1').filter({ hasText: /Return to/i }).click();

    await page.goto('/portal/site/!admin');
    await expect(page.getByRole('link', { name: /^Administration Workspace$/i }).first()).toBeVisible();
  });
});
