const { test, expect, devices } = require('@playwright/test');
const { SakaiHelpers } = require('./helpers/sakai-helpers');

/**
 * Sakai Assignments Tests - Converted from Cypress
 * 
 * Tests assignment creation, grading, submission, and rubric association
 */

test.describe('Assignments', () => {
  const instructor = 'instructor1';
  const student11 = 'student0011';
  const student12 = 'student0012';
  const assignTitle = 'Cypress Assignment';
  let sakaiUrl;
  let helpers;

  test.beforeEach(async ({ page }) => {
    helpers = new SakaiHelpers(page);
    
    // Intercept Google Analytics to prevent interference
    await page.route('**/google-analytics.com/**', route => route.abort());
  });

  test.describe('Create a new Assignment', () => {
    test('can create a new course', async ({ page }) => {
      await helpers.sakaiLogin(instructor);

      if (sakaiUrl == null) {
        sakaiUrl = await helpers.sakaiCreateCourse(instructor, [
          "sakai\\.rubrics",
          "sakai\\.assignment\\.grades",
          "sakai\\.gradebookng"
        ]);
      }
    });

    test('can create a letter grade assignment', async ({ page }) => {
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
      await page.locator('input#check_1').check();
      await page.locator('input#btnRemove').click();
      await expect(page.locator('div')).toContainText('Are you sure you want to delete');
      await page.locator('input').filter({ hasText: 'Delete' }).click();

      // Confirm we are on assignment list
      await expect(page.locator('.navIntraTool a')).toContainText('Add');
    });

    test('can create a points assignment', async ({ page }) => {
      await helpers.sakaiLogin(instructor);
      await page.goto(sakaiUrl);
      await helpers.sakaiToolClick('Assignments');

      // Create new assignment
      await page.locator('.navIntraTool a').filter({ hasText: 'Add' }).click();

      // Add a title
      await page.locator('input#new_assignment_title').click();
      await page.locator('input#new_assignment_title').fill(assignTitle);

      // Honor pledge
      await page.locator('#new_assignment_check_add_honor_pledge').click();

      // Need to unset grading
      await page.locator("#gradeAssignment").uncheck();

      // Attempt to save it without instructions
      await page.locator('div.act input.active').first().click();

      // Should be an alert at top
      await expect(page.locator('#generalAlert')).toContainText('Alert');

      // Type into the ckeditor instructions field
      await helpers.typeCkeditor("new_assignment_instructions", 
        "<p>What is chiefly responsible for the increase in the average length of life in the USA during the last fifty years?</p>");

      // Save it with instructions
      await page.locator('div.act input.active').first().click();

      // Confirm it exists but can't grade it
      await expect(page.locator('a').filter({ hasText: 'View Submissions' })).toHaveCount(1);
    });

    test("Can associate a rubric with an assignment", async ({ page }) => {
      await helpers.createRubric(instructor, sakaiUrl);
      await expect(page.locator(".modal-dialog")).toHaveCount(1);
      await page.locator(".modal-footer button").filter({ hasText: 'Save' }).click({ force: true });

      await helpers.sakaiToolClick('Assignments');
      await page.locator('.itemAction').filter({ hasText: 'Edit' }).click();

      // Save assignment with points and a rubric associated
      await page.locator("#gradeAssignment").click();
      await page.locator("#new_assignment_grade_points").fill("55.13");
      await page.locator("input[name='rbcs-associate'][value='1']").click();

      // Again just to make sure editor loaded fully
      await helpers.typeCkeditor("new_assignment_instructions", 
        "<p>What is chiefly responsible for the increase in the average length of life in the USA during the last fifty years?</p>");

      // Save
      await page.locator('.act input.active').first().click();

      // Confirm rubric button
      await expect(page.locator("a").filter({ hasText: 'Grade' })).toHaveCount(1);
      await expect(page.locator("sakai-rubric-student-button")).toHaveCount(1);

      // Confirm score is present on instructor page
      await expect(page.locator('td[headers="maxgrade"]').filter({ hasText: '55.13' })).toHaveCount(1);
    });

    test('can submit as student on desktop', async ({ page }) => {
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
      await expect(page.locator('.act input.active')).toHaveValue('Back to list');
      await page.locator('.act input.active').click();
    });

    test('can submit as student on iphone', async ({ page }) => {
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
      await expect(page.locator('.act input.active')).toHaveValue('Back to list');
      await page.locator('.act input.active').first().click();

      // Try to submit again
      await page.locator('a').filter({ hasText: assignTitle }).click();
      await expect(page.locator('.textPanel')).toContainText('This is my submission text');
      await page.locator('form').filter({ hasText: 'Back to list' }).click();
    });

    test('can allow a student to resubmit', async ({ page }) => {
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

    test.skip('can resubmit as student on iphone', async ({ page }) => {
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

    test('can create a non-electronic assignment', async ({ page }) => {
      await helpers.sakaiLogin(instructor);
      await page.goto(sakaiUrl);
      await helpers.sakaiToolClick('Assignments');

      // Create new assignment
      await page.locator('.navIntraTool a').filter({ hasText: 'Add' }).click();

      // Add a title
      await page.locator('#new_assignment_title').click();
      await page.locator('#new_assignment_title').fill('Non-electronic Assignment');

      // Need to unset grading
      await page.locator("#gradeAssignment").uncheck();

      // Need to choose Non-electronic from the dropdown
      await page.locator('#subType').selectOption('Non-electronic');

      // Type into the ckeditor instructions field
      await helpers.typeCkeditor("new_assignment_instructions", 
        "<p>Submit a 3000 word essay on the history of the internet to my office in Swanson 201.</p>");

      // Save it with instructions
      await page.locator('div.act input.active').first().click();

      // Now use the old grader
      await page.locator('.itemAction a').last().click();
      await page.waitForTimeout(10000);
      await page.locator('sakai-grader-toggle input').uncheck();
      await page.locator('#submissionList a').filter({ hasText: 'student0011' }).click();
      await helpers.typeCkeditor("grade_submission_feedback_comment", 
        "<p>Please submit again.</p>");
      await page.locator('input#save-and-return').click();
    });
  });
});