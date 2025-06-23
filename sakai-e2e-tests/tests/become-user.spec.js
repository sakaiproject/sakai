const { test, expect } = require('@playwright/test');
const { SakaiHelpers } = require('./helpers/sakai-helpers');

/**
 * Sakai Become User Tests - Converted from Cypress
 * 
 * Tests the admin "Become User" functionality for user impersonation
 */

test.describe('Become User', () => {
  const username = 'admin';
  let helpers;

  test.describe('From admin to instructor1 and back', () => {
    test.beforeEach(async ({ page }) => {
      helpers = new SakaiHelpers(page);
      await helpers.sakaiLogin(username);
    });

    test('Administration Workspace - Become User', async ({ page }) => {
      await page.goto('/portal/site/!admin');
      await expect(page.locator('body')).toContainText('Administration Workspace');
      await page.locator('#site-list-recent-item-admin a.btn-nav').filter({ hasText: 'Become User' }).click();
      
      await page.locator('#su input[type="text"]').click();
      await page.locator('#su input[type="text"]').fill('instructor1');
      await expect(page.locator('#su input[type="text"]')).toHaveValue('instructor1');
      
      await page.locator('#su\\:become').click();
      await page.goto('/portal/site/!admin');
      await expect(page.locator('body')).toContainText('Site Unavailable');
      
      await page.locator('.sak-sysInd-account').click();
      await page.locator('a#loginLink1').filter({ hasText: 'Return to' }).click();
      await page.goto('/portal/site/!admin');
      await expect(page.locator('body')).not.toContainText('Site Unavailable');
    });
  });
});