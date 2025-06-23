const { test, expect } = require('@playwright/test');
const { SakaiHelpers } = require('./helpers/sakai-helpers');

/**
 * Sakai Announcements Tests - Converted from Cypress
 * 
 * Tests announcements functionality including creation, visibility, and date restrictions
 */

test.describe('Announcements', () => {
  const instructor = 'instructor1';
  const student11 = 'student0011';
  const futureAnnouncementTitle = 'Future Announcement';
  const pastAnnouncementTitle = 'Past Announcement';
  const currentAnnouncementTitle = 'Current Announcement';
  let sakaiUrl;
  let helpers;

  test.beforeAll(async ({ browser }) => {
    // Create the course once for all tests to share
    const page = await browser.newPage();
    helpers = new SakaiHelpers(page);
    await helpers.sakaiLogin(instructor);
    
    sakaiUrl = await helpers.sakaiCreateCourse(instructor, [
      "sakai\\.announcements", 
      "sakai\\.schedule"
    ]);
    
    await page.close();
  });

  test.beforeEach(async ({ page }) => {
    helpers = new SakaiHelpers(page);
    
    // Intercept Google Analytics to prevent interference
    await page.route('**/google-analytics.com/**', route => route.abort());
  });

  test.describe('Create a new Announcement', () => {

    test('can create a future announcement', async ({ page }) => {
      await helpers.sakaiLogin(instructor);
      await page.goto(sakaiUrl);
      await helpers.sakaiToolClick('Announcements');

      // Create new announcement
      await page.locator('.navIntraTool a').filter({ hasText: 'Add' }).click();

      // Add a title
      await page.locator('#subject').click();
      await page.locator('#subject').fill(futureAnnouncementTitle);

      // Type into the ckeditor instructions field
      await helpers.typeCkeditor("body", 
        "<p>This is a future announcement that should only be visible after the specified date.</p>");

      // Set future dates
      await page.locator('#hidden_specify').click();
      await page.locator('#use_start_date').click();
      await helpers.sakaiDateSelect('#opendate', '06/01/2035 08:30 am');
      await page.locator('#use_end_date').click();
      await helpers.sakaiDateSelect('#closedate', '06/03/2035 08:30 am');
      
      // Save
      await page.locator('.act input.active').first().click();

      // Confirm there is one inactive row
      await expect(page.locator('table tr.inactive')).toHaveCount(1);
    });

    test('can create a past announcement', async ({ page }) => {
      await helpers.sakaiLogin(instructor);
      await page.goto(sakaiUrl);
      await helpers.sakaiToolClick('Announcements');

      // Create new announcement
      await page.locator('.navIntraTool a').filter({ hasText: 'Add' }).click();

      // Add a title
      await page.locator('#subject').click();
      await page.locator('#subject').fill(pastAnnouncementTitle);

      // Type into the ckeditor instructions field
      await helpers.typeCkeditor("body", 
        "<p>This is a past announcement that should not be visible anymore.</p>");

      // Set past dates
      await page.locator('#hidden_specify').click();
      await page.locator('#use_start_date').click();
      await helpers.sakaiDateSelect('#opendate', '01/01/2020 08:30 am');
      await page.locator('#use_end_date').click();
      await helpers.sakaiDateSelect('#closedate', '01/03/2020 08:30 am');
      
      // Save
      await page.locator('.act input.active').first().click();

      // Confirm there is another inactive row (total 2 now)
      await expect(page.locator('table tr.inactive')).toHaveCount(2);
    });

    test('can create a current announcement', async ({ page }) => {
      await helpers.sakaiLogin(instructor);
      await page.goto(sakaiUrl);
      await helpers.sakaiToolClick('Announcements');

      // Create new announcement
      await page.locator('.navIntraTool a').filter({ hasText: 'Add' }).click();

      // Add a title
      await page.locator('#subject').click();
      await page.locator('#subject').fill(currentAnnouncementTitle);

      // Type into the ckeditor instructions field
      await helpers.typeCkeditor("body", 
        "<p>This is a current announcement that should be visible to everyone.</p>");

      // Save without specifying dates (makes it current/active)
      await page.locator('.act input.active').first().click();

      // Verify we have 2 inactive (past and future) and 1 active (current) announcements
      await expect(page.locator('table tr.inactive')).toHaveCount(2);
      await expect(page.locator('table tr:not(.inactive)')).toBeVisible();
    });

    test('student can only see current announcement', async ({ page }) => {
      await helpers.sakaiLogin(student11);
      await page.goto(sakaiUrl);
      
      // Check Announcements tool first
      await helpers.sakaiToolClick('Announcements');
      
      // Verify only the current announcement is visible
      await expect(page.locator('body')).toContainText(currentAnnouncementTitle);
      await expect(page.locator('body')).not.toContainText(futureAnnouncementTitle);
      await expect(page.locator('body')).not.toContainText(pastAnnouncementTitle);
      
      // Now check the Overview page with iframe
      await helpers.sakaiToolClick('Overview');
      
      // Wait for iframe to load and check its contents
      const iframeLocator = page.locator('iframe.portletMainIframe[title*="Recent Announcements"]');
      await expect(iframeLocator).toBeVisible();
      
      const iframe = await helpers.iframeLoaded(iframeLocator);
      
      // Verify announcement visibility within iframe
      await expect(iframe.locator('body')).toContainText(currentAnnouncementTitle);
      await expect(iframe.locator('body')).not.toContainText(futureAnnouncementTitle);
      await expect(iframe.locator('body')).not.toContainText(pastAnnouncementTitle);
    });
  });
});