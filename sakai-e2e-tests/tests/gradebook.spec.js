const { test, expect } = require('@playwright/test');
const { SakaiHelpers } = require('./helpers/sakai-helpers');

/**
 * Sakai Gradebook Tests 
 * 
 * Tests gradebook category creation and gradebook item management
 */

test.describe('Gradebook', () => {
  const instructor = 'instructor1';
  const student = 'student0011';
  let sakaiUrl;
  let helpers;
  
  const cats = [
    {letter: "A", percent: 10},
    {letter: "B", percent: 35},
    {letter: "C", percent: 10},
    {letter: "D", percent: 15},
    {letter: "E", percent: 30},
  ];

  test.beforeEach(async ({ page }) => {
    helpers = new SakaiHelpers(page);
    
    // Intercept Google Analytics to prevent interference
    await page.route('**/google-analytics.com/**', route => route.abort());
    
    // Handle uncaught exceptions from Rubrics webcomponent issues
    page.on('pageerror', exception => {
      // Log the error but don't fail the test
      console.log(`Page error: ${exception}`);
    });
  });

  test.beforeAll(async ({ browser }) => {
    // Create the course once for all tests to share
    const page = await browser.newPage();
    helpers = new SakaiHelpers(page);
    await helpers.sakaiLogin(instructor);
    
    sakaiUrl = await helpers.sakaiCreateCourse(instructor, ["sakai.gradebookng"]);
    
    await page.close();
  });

  test.describe('Create site and add gradebook', () => {

    test('Can create gradebook categories', async ({ page }) => {
      await helpers.sakaiLogin(instructor);
      await helpers.goto(sakaiUrl);
      await helpers.sakaiToolClick('Gradebook');

      // DOM is being modified by Wicket so wait for the POST to complete
      // Intercept the category creation requests
      await page.route('**/settings?1*form-categoryPanel-settingsCategoriesPanel-categoriesWrap*', route => route.continue());

      // We want to use categories
      await page.locator('.navIntraTool a').filter({ hasText: 'Settings' }).click();
      await page.locator('.accordion button').filter({ hasText: 'Categories' }).click();
      await page.locator('input[type="radio"]').last().click();

      for (let i = 0; i < cats.length; i++) {
        const cat = cats[i];
        
        await page.locator('.gb-category-row input[name$="name"]').nth(i).fill(cat.letter);
        await page.locator('.gb-category-weight input[name$="weight"]').nth(i).clear();
        await page.locator('.gb-category-weight input[name$="weight"]').nth(i).fill(cat.percent.toString());
        await page.locator('#settingsCategories button').filter({ hasText: 'Add a category' }).click();
        
        // Wait for the request to complete
        await page.waitForTimeout(1000);
      }

      // Save the category modifications
      await page.locator('.act button.active').click();
    });

    test('Can create gradebook items', async ({ page }) => {
      await helpers.sakaiLogin(instructor);
      await helpers.goto(sakaiUrl);
      await helpers.sakaiToolClick('Gradebook');

      for (let i = 0; i < cats.length; i++) {
        const cat = cats[i];
        
        // Ensure no modal is present before proceeding
        await expect(page.locator('.wicket-modal')).toHaveCount(0);
        
        await page.locator("button.gb-add-gradebook-item-button").click();
        
        // Wait for modal to appear
        await expect(page.locator(".wicket-modal")).toBeVisible({ timeout: 15000 });
        
        await page.locator(".wicket-modal input[name$='title']").fill(`Item ${i + 1}`);
        await page.locator(".wicket-modal input[name$='points']").fill('100');
        await page.locator(".wicket-modal select[name$='category']").selectOption(`${cat.letter} (${cat.percent}%)`);
        await page.locator(".wicket-modal button[name$='submit']").click();
        
        await page.waitForTimeout(2000);
        
        // Verify success message appears
        await page.locator(".messageSuccess").scrollIntoView();
        await expect(page.locator(".messageSuccess")).toBeVisible();
      }
    });
  });
});