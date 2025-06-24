const { test, expect } = require('@playwright/test');
const { SakaiHelpers } = require('./helpers/sakai-helpers');

/**
 * Sakai Announcements Tests
 *
 * Tests announcements functionality including creation, visibility, and date restrictions.
 * These tests are independent and can be run in any order.
 */

test.describe('Announcements', () => {
  const instructor = 'instructor1';
  const student11 = 'student0011';
  const futureAnnouncementTitle = 'Future Announcement';
  const pastAnnouncementTitle = 'Past Announcement';
  const currentAnnouncementTitle = 'Current Announcement';
  let sakaiUrl;
  let helpers;

  // Helper function to create an announcement
  const createAnnouncement = async (page, helpers, title, body, startDate, endDate) => {
    await helpers.sakaiToolClick('Announcements');
    await page.locator('.navIntraTool a').filter({ hasText: 'Add' }).click();

    await page.locator('#subject').click();
    await page.locator('#subject').fill(title);
    await helpers.typeCkeditor("body", body);

    if (startDate && endDate) {
      await page.locator('#hidden_specify').click();
      await page.locator('#use_start_date').click();
      await helpers.sakaiDateSelect('#opendate', startDate);
      await page.locator('#use_end_date').click();
      await helpers.sakaiDateSelect('#closedate', endDate);
    }
    await page.locator('.act input.active').first().click();
    // Wait for navigation back to the main announcements list
    await expect(page.locator('.page-header')).toContainText('Announcements');
  };

  test.beforeAll(async ({ browser }) => {
    // Create a single browser context for setup
    const page = await browser.newPage();
    helpers = new SakaiHelpers(page);
    await helpers.sakaiLogin(instructor);

    sakaiUrl = await helpers.sakaiCreateCourse(instructor, [
      "sakai.announcements",
      "sakai.schedule"
    ]);
    await helpers.goto(sakaiUrl);

    // Create all necessary announcements for the tests
    await createAnnouncement(page, helpers, futureAnnouncementTitle,
      "<p>This is a future announcement that should only be visible after the specified date.</p>",
      '06/01/2035 08:30 am', '06/03/2035 08:30 am');

    await createAnnouncement(page, helpers, pastAnnouncementTitle,
      "<p>This is a past announcement that should not be visible anymore.</p>",
      '01/01/2020 08:30 am', '01/03/2020 08:30 am');

    await createAnnouncement(page, helpers, currentAnnouncementTitle,
      "<p>This is a current announcement that should be visible to everyone.</p>");

    await page.close();
  });

  test.beforeEach(async ({ page }) => {
    helpers = new SakaiHelpers(page);
    // Intercept Google Analytics to prevent interference
    await page.route('**/google-analytics.com/**', route => route.abort());
  });

  test('instructor can see all announcements with correct statuses', async ({ page }) => {
    await helpers.sakaiLogin(instructor);
    await helpers.goto(sakaiUrl);
    await helpers.sakaiToolClick('Announcements');

    // Verify we have 2 inactive (past and future) and 1 active (current) announcements
    await expect(page.locator('table tr.inactive')).toHaveCount(2);
    const currentRow = page.locator('tr', { hasText: currentAnnouncementTitle });
    await expect(currentRow).not.toHaveClass(/inactive/);
    await expect(page.locator('text=' + futureAnnouncementTitle)).toBeVisible();
    await expect(page.locator('text=' + pastAnnouncementTitle)).toBeVisible();
  });

  test('student can only see current announcement', async ({ page }) => {
    await helpers.sakaiLogin(student11);
    await helpers.goto(sakaiUrl);
    
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