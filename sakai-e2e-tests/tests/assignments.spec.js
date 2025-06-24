const { test, expect, devices } = require('@playwright/test');
const { SakaiHelpers } = require('./helpers/sakai-helpers');

/**
 * Sakai Assignments Tests 
 *
 * Tests assignment creation, grading, submission, and rubric association.
 * Some tests are independent, and the main workflow is in a serial block.
 */

test.describe('Assignments', () => {
  const instructor = 'instructor1';
  const student11 = 'student0011';
  const student12 = 'student0012';
  const assignTitle = 'Playwright Assignment';
  let sakaiUrl;
  let helpers;

  test.beforeAll(async ({ browser }) => {
    // Create the course once for all tests to share
    const page = await browser.newPage();
    helpers = new SakaiHelpers(page);
    await helpers.sakaiLogin(instructor);
    
    sakaiUrl = await helpers.sakaiCreateCourse(instructor, [
      "sakai.rubrics",
      "sakai.assignment.grades",
      "sakai.gradebookng"
    ]);
    
    await page.close();
  });

  test.beforeEach(async ({ page }) => {
    helpers = new SakaiHelpers(page);
    
    // Intercept Google Analytics to prevent interference
    await page.route('**/google-analytics.com/**', route => route.abort());
  });

  // This test is self-contained and can run independently
  test('can create, grade, and delete a letter grade assignment', async ({ page }) => {
    await helpers.sakaiLogin(instructor);
    await page.goto(sakaiUrl);
    await helpers.sakaiToolClick('Assignments');

    // Create new assignment
    await page.locator('.navIntraTool a').filter({ hasText: 'Add' }).click();

    // Add a title
    await page.locator('#new_assignment_title').click();
    await page.locator('#new_assignment_title').fill('Letter Grades');

    await page.locator('#new_assignment_grade_type').selectOption('Letter grade');

    // Type into the ckeditor instructions field
    await helpers.typeCkeditor("new_assignment_instructions", 
      "<p>What is chiefly responsible for the increase in the average length of life in the USA during the last fifty years?</p>");

    // Save
    await page.locator('div.act input.active').first().click();

    // Confirm can grade it with letters
    await page.locator('.itemAction a').last().click();
    await page.waitForTimeout(10000);
    await page.locator('sakai-grader-toggle input').check();
    await page.locator('#submissionList a').filter({ hasText: 'student0011' }).click();
    await page.locator('#grader-link-block button').click();
    await page.locator('#letter-grade-selector').selectOption('B');
    await page.locator('.act .active').first().click();
    await page.locator('button').filter({ hasText: 'Return to List' }).click();
    await expect(page.locator('table#submissionList tr').nth(1).locator('td[headers="grade"]')).toContainText('B');
    await page.locator('.navIntraTool a').first().click();

    // Now use the old grader
    await page.locator('.itemAction a').last().click();
    await page.waitForTimeout(10000);
    await page.locator('sakai-grader-toggle input').uncheck();
    await page.locator('#submissionList a').filter({ hasText: 'student0011' }).click();
    await expect(page.locator('select#grade option:checked')).toHaveText('B');
    await page.locator('select#grade').selectOption('C');
    await page.locator('.act input#save').click();
    await page.locator('input[name="cancelgradesubmission1"]').click();
    await expect(page.locator('table#submissionList tr').nth(1).locator('td[headers="grade"]')).toContainText('C');
    await page.locator('.navIntraTool a').first().click();

    // Now remove it
    const row = page.locator('tr', { hasText: 'Letter Grades' });
    await row.locator('input[type="checkbox"]').check();
    await page.locator('input#btnRemove').click();
    await expect(page.locator('div')).toContainText('Are you sure you want to delete');
    await page.locator('input').filter({ hasText: 'Delete' }).click();

    // Confirm we are on assignment list
    await expect(page.locator('.portletBody h3')).toContainText('Assignment List');
  });

  // This test is self-contained and can run independently
  test('can create and delete a non-electronic assignment', async ({ page }) => {
    const assignmentTitle = 'Non-electronic Assignment';
    await helpers.sakaiLogin(instructor);
    await page.goto(sakaiUrl);
    await helpers.sakaiToolClick('Assignments');

    // Create new assignment
    await page.locator('.navIntraTool a').filter({ hasText: 'Add' }).click();

    // Add a title
    await page.locator('#new_assignment_title').click();
    await page.locator('#new_assignment_title').fill(assignmentTitle);

    // Need to unset grading
    await page.locator("#gradeAssignment").uncheck();

    // Need to choose Non-electronic from the dropdown
    await page.locator('#subType').selectOption('Non-electronic');

    // Type into the ckeditor instructions field
    await helpers.typeCkeditor("new_assignment_instructions", 
      "<p>Submit a 3000 word essay on the history of the internet to my office in Swanson 201.</p>");

    // Save it with instructions
    await page.locator('div.act input.active').first().click();

    // Check grading view
    await page.locator('.itemAction a').last().click();
    await page.waitForTimeout(10000);
    await page.locator('sakai-grader-toggle input').uncheck();
    await page.locator('#submissionList a').filter({ hasText: 'student0011' }).click();
    await helpers.typeCkeditor("grade_submission_feedback_comment", 
      "<p>Please submit again.</p>");
    await page.locator('input#save-and-return').click();
    await page.locator('.navIntraTool a').first().click();

    // Now remove it
    const row = page.locator('tr', { hasText: assignmentTitle });
    await row.locator('input[type="checkbox"]').check();
    await page.locator('input#btnRemove').click();
    await expect(page.locator('div')).toContainText('Are you sure you want to delete');
    await page.locator('input').filter({ hasText: 'Delete' }).click();

    // Confirm we are on assignment list
    await expect(page.locator('.portletBody h3')).toContainText('Assignment List');
  });

  // The following tests depend on each other and must run in order.
  test.describe.serial('Assignment Submission and Grading Workflow', () => {

    test.afterAll(async ({ page }) => {
      // Cleanup: Remove the assignment created during the workflow
      helpers = new SakaiHelpers(page);
      await helpers.sakaiLogin(instructor);
      await page.goto(sakaiUrl);
      await helpers.sakaiToolClick('Assignments');
      
      const row = page.locator('tr', { hasText: assignTitle });
      await row.locator('input[type="checkbox"]').check();
      await page.locator('input#btnRemove').click();
      await expect(page.locator('div')).toContainText('Are you sure you want to delete');
      await page.locator('input').filter({ hasText: 'Delete' }).click();
      await expect(page.locator('.portletBody h3')).toContainText('Assignment List');
    });

    test('can create a points assignment with a rubric', async ({ page }) => {
      await helpers.sakaiLogin(instructor);
      await page.goto(sakaiUrl);
      await helpers.sakaiToolClick('Assignments');

      // Create new assignment
      await page.locator('.navIntraTool a').filter({ hasText: 'Add' }).click();

      // Add a title and instructions
      await page.locator('input#new_assignment_title').click();
      await page.locator('input#new_assignment_title').fill(assignTitle);
      await helpers.typeCkeditor("new_assignment_instructions", 
        "<p>What is chiefly responsible for the increase in the average length of life in the USA during the last fifty years?</p>");

      // Honor pledge
      await page.locator('#new_assignment_check_add_honor_pledge').click();
      
      // Post without grading to create the assignment shell
      await page.locator('div.act input.active').first().click();

      // Create the rubric
      await helpers.createRubric(instructor, sakaiUrl);
      await expect(page.locator(".modal-dialog")).toBeVisible();
      await page.locator(".modal-footer button").filter({ hasText: 'Save' }).click({ force: true });
      await expect(page.locator(".modal-dialog")).not.toBeVisible();

      // Navigate back to assignments and edit the new one
      await helpers.sakaiToolClick('Assignments');
      const row = page.locator('tr', { hasText: assignTitle });
      await row.locator('.itemAction a').filter({ hasText: 'Edit' }).click();

      // Add points and associate the rubric
      await page.locator("#gradeAssignment").check();
      await page.locator("#new_assignment_grade_points").fill("55.13");
      await page.locator("input[name='rbcs-associate'][value='1']").click();

      // Save
      await page.locator('.act input.active').first().click();

      // Confirm rubric button and score
      await expect(page.locator("a").filter({ hasText: 'Grade' })).toBeVisible();
      await expect(page.locator("sakai-rubric-student-button")).toBeVisible();
      await expect(page.locator('td[headers="maxgrade"]').filter({ hasText: '55.13' })).toBeVisible();
    });

    test('student 1 can submit on desktop', async ({ page }) => {
      await page.setViewportSize(devices['Desktop Chrome'].viewport);
      await helpers.sakaiLogin(student11);
      await page.goto(sakaiUrl);
      await helpers.sakaiToolClick('Assignments');

      await page.locator('a').filter({ hasText: assignTitle }).click();

      // Honor Pledge?
      await page.locator('input[type="submit"]').filter({ hasText: 'Agree' }).click();

      await helpers.typeCkeditor('Assignment.view_submission_text', '<p>This is my submission text</p>');
      await expect(page.locator('.act input.active').first()).toHaveValue('Proceed');
      await page.locator('.act input.active').first().click();

      // Final submit
      await expect(page.locator('.textPanel')).toContainText('This is my submission text');
      await expect(page.locator('.act input.active')).toHaveValue('Submit');
      await page.locator('.act input.active').click();

      // Confirmation page
      await expect(page.locator('h3')).toContainText('Submission Confirm');
    });

    test('student 2 can submit on iphone', async ({ page }) => {
      await page.setViewportSize(devices['iPhone X'].viewport);
      await helpers.sakaiLogin(student12);
      await page.goto(sakaiUrl);
      await page.locator('button.responsive-allsites-button').first().click();
      await page.locator('ul.site-page-list a.btn-nav').filter({ hasText: 'Assignments' }).click();

      await page.locator('a').filter({ hasText: assignTitle }).first().click();

      // Honor Pledge?
      await page.locator('input[type="submit"]').filter({ hasText: 'Agree' }).first().click();

      await helpers.typeCkeditor('Assignment.view_submission_text', '<p>This is my submission text</p>');
      await expect(page.locator('.act input.active').first()).toHaveValue('Proceed');
      await page.locator('.act input.active').first().click();

      // Final submit
      await expect(page.locator('.act input.active').first()).toHaveValue('Submit');
      await page.locator('.act input.active').first().click();

      // Confirmation page
      await expect(page.locator('h3')).toContainText('Submission Confirm');
    });

    test('instructor can grade and allow resubmission for student 2', async ({ page }) => {
      await helpers.sakaiLogin(instructor);
      await page.goto(sakaiUrl);
      await helpers.sakaiToolClick('Assignments');

      await page.locator('.itemAction a').filter({ hasText: 'Grade' }).click();

      await page.waitForTimeout(10000);
      await page.locator('sakai-grader-toggle input').check();
      await page.locator('#submissionList a').filter({ hasText: student12 }).click();

      // Allow student12 to resubmit
      await expect(page.locator('#grader-feedback-text').first()).toContainText('This is my submission text');
      await page.locator("#grader-link-block button").first().click();
      await page.locator('#score-grade-input').fill('50.56');
      await page.locator('.resubmission-checkbox input').click();
      // Save and Release
      await page.locator('#grader-save-buttons button[name="return"]').click();
    });

    test('student 2 can resubmit on iphone', async ({ page }) => {
      await page.setViewportSize(devices['iPhone X'].viewport);
      await helpers.sakaiLogin(student12);
      await page.goto(sakaiUrl);
      await page.locator('button.responsive-allsites-button').first().click();
      await page.locator('ul.site-page-list a.btn-nav').filter({ hasText: 'Assignments' }).click();

      // Click into the assignment
      await page.locator('a').filter({ hasText: assignTitle }).click();

      // Confirm our score is present
      await expect(page.locator('.itemSummaryValue')).toContainText('50.56');

      // Confirm we can re-submit as student
      await expect(page.locator('h3')).toContainText('Resubmission');
      await page.waitForTimeout(5000); // wait for ckeditor to load
      await helpers.typeCkeditor('Assignment.view_submission_text', '<p>This is my re-submission text</p>');
      await page.locator('div.act input.active').first().click();

      // Final resubmit
      await expect(page.locator('.act input.active')).toHaveValue('Submit');
      await page.locator('.act input.active').click();
    });
  });
});