const { test, expect } = require('@playwright/test');
const { SakaiHelpers } = require('./helpers/sakai-helpers');

/**
 * Sakai Lessons Tests 
 * 
 * Tests lesson creation functionality including checklists
 */

test.describe('Lessons', () => {
  const instructor = 'instructor1';
  const student11 = 'student0011';
  const student12 = 'student0012';
  const lessonTitle = 'Playwright Lesson';
  let sakaiUrl;
  let helpers;

  test.beforeEach(async ({ page }) => {
    helpers = new SakaiHelpers(page);
    
    // Intercept Google Analytics to prevent interference
    await page.route('**/google-analytics.com/**', route => route.abort());
  });

  test.beforeAll(async ({ browser }) => {
    // Create the course once for all tests to share
    const page = await browser.newPage();
    helpers = new SakaiHelpers(page);
    await helpers.sakaiLogin(instructor);
    
    sakaiUrl = await helpers.sakaiCreateCourse(instructor, [
      "sakai.rubrics",
      "sakai.assignment.grades",
      "sakai.gradebookng",
      "sakai.lessonbuildertool"
    ]);
    
    await page.close();
  });

  test.describe('Lessons Tests', () => {

    test('create a new lesson item', async ({ page }) => {
      await helpers.sakaiLogin(instructor);
      await page.goto(sakaiUrl);
      
      // Go to lessons tool
      await helpers.sakaiToolClick('Lessons');
      
      // Click on add content button within lessons tool
      await page.locator('button').filter({ hasText: 'Add Content' }).click();
      
      // Click on add checklist link
      await page.locator('.add-checklist-link').click();
      
      // Type in the name of the checklist
      await page.locator('#name').fill('Checklist');
      
      // Add entries for checklist
      await page.locator('#addChecklistItemButton > :nth-child(2)').click();
      await page.locator('#checklistItemDiv1 > .checklist-item-name').fill('A');
      
      await page.locator('#addChecklistItemButton').click();
      await page.locator('#checklistItemDiv2 > .checklist-item-name').fill('B');
      
      await page.locator('#addChecklistItemButton').click();
      await page.locator('#checklistItemDiv3 > .checklist-item-name').fill('C');
      
      await page.locator('#addChecklistItemButton').click();
      await page.locator('#checklistItemDiv4 > .checklist-item-name').fill('D');
      
      // Save checklist
      await page.locator('#save').click();
      
      // Verify a div of checklist is visible
      // Note: Original test had this commented out
      // await expect(page.locator('.checklistDiv')).toBeVisible();
    });
  });
});