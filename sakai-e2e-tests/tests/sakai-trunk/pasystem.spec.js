const { test, expect } = require('../support/fixtures');

test.describe('PA System', () => {
  test.describe.configure({ mode: 'serial' });

  test('Administration Workspace - PA System', async ({ page, sakai }) => {
    await sakai.login('admin');

    await page.goto('/portal/site/!admin');
    await expect(page.getByRole('link', { name: /^Administration Workspace$/i }).first()).toBeVisible();

    await page.getByRole('link', { name: /^PA System$/i }).first().click({ force: true });
    await page.getByRole('button', { name: /^Create Banner$/i }).first().click({ force: true });

    await page.locator('form input#message').fill('This is a test');
    await page.locator('#active').click();
    await page.locator('input[value*="Save"], button:has-text("Save")').first().click({ force: true });

    await expect(page.locator('.pasystem-banner-alerts')).toContainText('This is a test');

    await page.getByRole('button', { name: 'Edit' }).click();
    await page.locator('form input#message').fill('This is a test -- 2');
    await page.locator('#active').click();
    await page.locator('input[value*="Save"], button:has-text("Save")').first().click({ force: true });

    await expect(page.locator('.pasystem-banner-alerts')).not.toContainText('This is a test');

    await page.locator('a.pasystem-delete-btn').click({ force: true });
    await page.getByRole('button', { name: 'Delete' }).click({ force: true });
  });
});
