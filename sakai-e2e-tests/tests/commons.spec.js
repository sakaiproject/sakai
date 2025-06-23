const { test, expect } = require('@playwright/test');
const { SakaiHelpers } = require('./helpers/sakai-helpers');

/**
 * Sakai Commons Tests - Converted from Cypress
 * 
 * Tests commons functionality for creating and viewing posts
 */

test.describe('Commons', () => {
  const instructor = 'instructor1';
  const student11 = 'student0011';
  const student12 = 'student0012';
  let sakaiUrl;
  let helpers;

  test.beforeEach(async ({ page }) => {
    helpers = new SakaiHelpers(page);
    
    // Intercept Google Analytics to prevent interference
    await page.route('**/google-analytics.com/**', route => route.abort());
  });

  test.describe('Create a new commons post', () => {
    test('can create a new course', async ({ page }) => {
      await helpers.sakaiLogin(instructor);

      if (sakaiUrl == null) {
        sakaiUrl = await helpers.sakaiCreateCourse(instructor, [
          "sakai\\.commons"
        ]);
      }
    });

    test('can create a commons post as student', async ({ page }) => {
      await helpers.sakaiLogin(student11);
      await page.goto(sakaiUrl);
      await helpers.sakaiToolClick('Commons');

      // Create new commons post
      await page.locator('#commons-post-creator-editor').click();
      await page.locator('#commons-post-creator-editor').fill('This is a student test post');
      await page.locator('#commons-editor-post-button').click();

      // Check for content
      await expect(page.locator('.commons-post-content')).toContainText('This is a student test post');
    });

    test('can create a commons post as instructor', async ({ page }) => {
      await helpers.sakaiLogin(instructor);
      await page.goto(sakaiUrl);
      await helpers.sakaiToolClick('Commons');

      // Check for student post
      await expect(page.locator('.commons-post-content')).toContainText('This is a student test post');

      // Create new commons post
      await page.locator('#commons-post-creator-editor').click();
      await page.locator('#commons-post-creator-editor').fill('This is a test post');
      await page.locator('#commons-editor-post-button').click();

      // Check for content
      await expect(page.locator('.commons-post-content')).toContainText('This is a test post');
    });
  });
});