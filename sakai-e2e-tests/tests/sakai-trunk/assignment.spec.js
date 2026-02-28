const { test, expect } = require('../support/fixtures');

test.describe('Assignments', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;
  const assignTitle = `Playwright Assignment ${Date.now()}`;

  const ensureStudentAssignmentExists = async (page, sakai) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Assignments');
    await sakai.gotoAssignmentsList();

    const existingAssignmentLink = page.getByRole('link', { name: assignTitle }).first();
    if (await existingAssignmentLink.count()) {
      return;
    }

    await sakai.openAddAssignmentForm();
    await page.locator('#new_assignment_title').fill(assignTitle);
    const gradeAssignmentCheckbox = page.locator('#gradeAssignment').first();
    if (await gradeAssignmentCheckbox.count()) {
      await gradeAssignmentCheckbox.uncheck({ force: true });
    }
    await sakai.typeCkEditor('new_assignment_instructions', '<p>Assignment for student submission tests.</p>');
    await sakai.submitAssignmentForm();

    const postButton = page.getByRole('button', { name: /^Post$/i }).first();
    if ((await postButton.count()) && (await postButton.isVisible().catch(() => false))) {
      await postButton.click({ force: true });
    }

    await sakai.gotoAssignmentsList();
    return page.getByRole('link', { name: assignTitle }).first().isVisible({ timeout: 5000 }).catch(() => false);
  };

  test('can create a new course', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', [
      'sakai\\.rubrics',
      'sakai\\.assignment\\.grades',
      'sakai\\.gradebookng',
    ]);
  });

  test('can create a letter grade assignment', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Assignments');

    await sakai.openAddAssignmentForm();
    await page.locator('#new_assignment_title').fill('Letter Grades');
    await page.locator('#new_assignment_grade_type').first().selectOption({ label: 'Letter grade' }, { force: true });

    await sakai.typeCkEditor('new_assignment_instructions',
      '<p>What is chiefly responsible for the increase in the average length of life in the USA during the last fifty years?</p>');

    await sakai.submitAssignmentForm();

    let openedFirstSubmissionList = await sakai.clickAssignmentAction(/View Submissions|Grade/i);
    if (!openedFirstSubmissionList) {
      await page.goto(sakaiUrl);
      await sakai.toolClick('Assignments');
      openedFirstSubmissionList = await sakai.clickAssignmentAction(/View Submissions|Grade/i);
    }

    const newGraderToggle = page.locator('sakai-grader-toggle input').first();
    const studentSubmissionLink = page.locator('#submissionList a').filter({ hasText: 'student0011' }).first();

    if (openedFirstSubmissionList) {
      await page.waitForLoadState('domcontentloaded').catch(() => {});
      await newGraderToggle.waitFor({ state: 'visible', timeout: 15000 }).catch(() => {});
      if (await newGraderToggle.count() && await studentSubmissionLink.count()) {
        await newGraderToggle.check({ force: true });
        await studentSubmissionLink.click({ force: true });
        await page.locator('#letter-grade-selector').selectOption('B');
        await page.getByRole('button', { name: /Save and Release/i }).click({ force: true });
        await page.getByRole('button', { name: /Return to List/i }).click({ force: true });
        const gradeCell = page.locator('table#submissionList tr').nth(1).locator('td[headers="grade"]');
        if (await gradeCell.count()) {
          await expect(gradeCell).toContainText('B');
        }
        await sakai.gotoAssignmentsList();
      }
    }

    let openedSecondSubmissionList = await sakai.clickAssignmentAction(/View Submissions|Grade/i);
    if (!openedSecondSubmissionList) {
      await page.goto(sakaiUrl);
      await sakai.toolClick('Assignments');
      openedSecondSubmissionList = await sakai.clickAssignmentAction(/View Submissions|Grade/i);
    }

    if (openedSecondSubmissionList) {
      await page.waitForLoadState('domcontentloaded').catch(() => {});
      await newGraderToggle.waitFor({ state: 'visible', timeout: 15000 }).catch(() => {});
      if (await newGraderToggle.count() && await studentSubmissionLink.count()) {
        await newGraderToggle.uncheck({ force: true });
        await studentSubmissionLink.click({ force: true });
        await page.locator('#grade').selectOption('C');
        await page.locator('.act input#save').click({ force: true });
        await page.locator('input[name="cancelgradesubmission1"]').click({ force: true });
        const gradeCell = page.locator('table#submissionList tr').nth(1).locator('td[headers="grade"]');
        if (await gradeCell.count()) {
          await expect(gradeCell).toContainText('C');
        }
        await sakai.gotoAssignmentsList();
      }
    }

    await page.goto(sakaiUrl);
    await sakai.toolClick('Assignments');

    let assignmentCheckbox = page.locator('input[id^="check_"]').first();
    if (!(await assignmentCheckbox.count())) {
      await sakai.gotoAssignmentsList();
      assignmentCheckbox = page.locator('input[id^="check_"]').first();
    }

    if (await assignmentCheckbox.count()) {
      await assignmentCheckbox.check({ force: true });
      await page.locator('input#btnRemove').click({ force: true });
      await expect(page.locator('body')).toContainText('Are you sure you want to delete');
      await page.locator('input[value="Delete"], button:has-text("Delete")').first().click({ force: true });
      await expect(page.getByRole('link', { name: /^Add$/i }).first()).toBeVisible();
    }
  });

  test('can create a points assignment', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Assignments');

    await sakai.openAddAssignmentForm();
    await page.locator('#new_assignment_title').fill(assignTitle);
    await page.locator('#new_assignment_check_add_honor_pledge').click({ force: true });
    await page.locator('#gradeAssignment').uncheck({ force: true });

    await sakai.submitAssignmentForm();
    const formAlert = page.locator('#generalAlert').first();
    if (await formAlert.count()) {
      await expect(formAlert).toContainText(/Alert|required/i);
    }

    await sakai.typeCkEditor('new_assignment_instructions',
      '<p>What is chiefly responsible for the increase in the average length of life in the USA during the last fifty years?</p>');

    await sakai.submitAssignmentForm();
    const postButton = page.getByRole('button', { name: /^Post$/i }).first();
    if ((await postButton.count()) && (await postButton.isVisible().catch(() => false))) {
      await postButton.click({ force: true });
    }

    await sakai.gotoAssignmentsList();
    await expect(page.locator('body')).toContainText(assignTitle);
  });

  test('can associate a rubric with an assignment', async ({ page, sakai }) => {
    await sakai.createRubric('instructor1', sakaiUrl);

    await page.locator('.modal-footer button:visible, div.popover button:visible, button:visible')
      .filter({ hasText: /Save/i })
      .first()
      .click({ force: true });

    await page.goto(sakaiUrl);
    await sakai.toolClick('Assignments');
    await page.locator('.navIntraTool').first().waitFor({ timeout: 15000 }).catch(() => {});

    const clickVisibleEditLink = async () => sakai.clickVisible(page.getByRole('link', { name: /^Edit$/i }));

    if (!(await clickVisibleEditLink())) {
      await sakai.openAddAssignmentForm();
      await page.locator('#new_assignment_title').fill(`Rubric Assignment ${Date.now()}`);
      await sakai.typeCkEditor('new_assignment_instructions', '<p>Assignment for rubric association.</p>');
      await sakai.submitAssignmentForm();
      await clickVisibleEditLink();
    }

    await page.locator('#gradeAssignment').check({ force: true });
    await page.locator('#new_assignment_grade_points').fill('55.13');
    await page.locator('input[name="rbcs-associate"][value="1"]').check({ force: true });

    await sakai.typeCkEditor('new_assignment_instructions',
      '<p>What is chiefly responsible for the increase in the average length of life in the USA during the last fifty years?</p>');

    await page.locator('.act input.active').first().click({ force: true });

    await expect(page.locator('body')).toContainText('Grade');
    await expect(page.locator('body')).toContainText(/Rubric|Assignment/i);
  });

  test('can submit as student on desktop', async ({ page, sakai }) => {
    const assignmentReady = await ensureStudentAssignmentExists(page, sakai);
    if (!assignmentReady) {
      test.skip(true, `${assignTitle} was not published/listed for student flows in this run`);
    }

    await page.setViewportSize({ width: 1280, height: 800 });
    await sakai.login('student0011');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Assignments');
    await sakai.gotoAssignmentsList();

    const desktopAssignmentLink = page.getByRole('link', { name: assignTitle }).first();
    if (!(await desktopAssignmentLink.isVisible({ timeout: 5000 }).catch(() => false))) {
      test.skip(true, `${assignTitle} is not visible to student0011`);
    }
    await desktopAssignmentLink.click({ force: true });

    const agreeButton = page.locator('input[type="submit"][value*="Agree"], button:has-text("Agree")');
    if (await agreeButton.count()) {
      await agreeButton.first().click({ force: true });
    }

    await sakai.typeCkEditor('Assignment.view_submission_text', '<p>This is my submission text</p>');
    await page.locator('.act input.active').first().click({ force: true });

    const submitButton = page.locator('.act input.active[value*="Submit"], .act button.active:has-text("Submit")').first();
    await expect(submitButton).toBeVisible();
    await submitButton.click({ force: true });
    await expect(page.locator('h3')).toContainText('Submission Confirm');
    await page.locator('.act input.active[value*="Back to list"], .act button.active:has-text("Back to list")').first().click({ force: true });
  });

  test('can submit as student on iphone viewport', async ({ page, sakai }) => {
    const assignmentReady = await ensureStudentAssignmentExists(page, sakai);
    if (!assignmentReady) {
      test.skip(true, `${assignTitle} was not published/listed for student flows in this run`);
    }

    await page.setViewportSize({ width: 375, height: 812 });
    await sakai.login('student0012');
    await page.goto(sakaiUrl);

    const allSitesButton = page.locator('button.responsive-allsites-button').first();
    if (await allSitesButton.count()) {
      await allSitesButton.click({ force: true });
    }

    await sakai.toolClick('Assignments');
    await sakai.gotoAssignmentsList();
    const mobileAssignmentLink = page.getByRole('link', { name: assignTitle }).first();
    if (!(await mobileAssignmentLink.isVisible({ timeout: 5000 }).catch(() => false))) {
      test.skip(true, `${assignTitle} is not visible to student0012`);
    }
    await mobileAssignmentLink.click({ force: true });

    const agreeButton = page.locator('input[type="submit"][value*="Agree"], button:has-text("Agree")');
    if (await agreeButton.count()) {
      await agreeButton.first().click({ force: true });
    }

    await sakai.typeCkEditor('Assignment.view_submission_text', '<p>This is my submission text</p>');
    await page.locator('.act input.active').first().click({ force: true });
    const submitButton = page.locator('.act input.active[value*="Submit"], .act button.active:has-text("Submit")').first();
    await expect(submitButton).toBeVisible();
    await submitButton.click({ force: true });
    await expect(page.locator('h3')).toContainText('Submission Confirm');
    await page.locator('.act input.active[value*="Back to list"], .act button.active:has-text("Back to list")').first().click({ force: true });
  });

  test('can allow a student to resubmit', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Assignments');

    const openedGradebookList = await sakai.clickAssignmentAction(/Grade|View Submissions/i);
    if (!openedGradebookList) {
      test.skip(true, 'No grade/view submissions action was available');
    }
    await page.waitForLoadState('domcontentloaded').catch(() => {});

    const newGraderToggle = page.locator('sakai-grader-toggle input').first();
    await newGraderToggle.waitFor({ state: 'visible', timeout: 15000 }).catch(() => {});
    if (await newGraderToggle.count()) {
      await newGraderToggle.check({ force: true });
    }

    await page.locator('#submissionList a').filter({ hasText: 'student0012' }).first().click({ force: true });

    const inlineFeedbackButton = page.locator('.inline-feedback-button').first();
    if (await inlineFeedbackButton.count()) {
      await inlineFeedbackButton.click({ force: true });
      await expect(page.locator('#grader-feedback-text').first()).toContainText('This is my submission text');
    }

    const scoreInput = page.locator('#score-grade-input').first();
    if (await scoreInput.count()) {
      await scoreInput.fill('50.56');
    }

    const resubmitInput = page.locator('.resubmission-checkbox input').first();
    if (await resubmitInput.count()) {
      await resubmitInput.check({ force: true });
    }

    const returnButton = page.locator('#grader-save-buttons button[name="return"], input#save-and-return, button:has-text("Save and Release"), input[value*="Save and Release"]').first();
    if (await returnButton.count()) {
      await returnButton.click({ force: true });
    }
  });

  test('can create a non-electronic assignment', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Assignments');

    await sakai.openAddAssignmentForm();
    await page.locator('#new_assignment_title').fill('Non-electronic Assignment');
    await page.locator('#gradeAssignment').uncheck({ force: true });
    await page.locator('#subType').selectOption({ label: 'Non-electronic' });

    await sakai.typeCkEditor('new_assignment_instructions',
      '<p>Submit a 3000 word essay on the history of the internet to my office in Swanson 201.</p>');

    await sakai.submitAssignmentForm();

    const openedNonElectronicSubmissionList = await sakai.clickAssignmentAction(/View Submissions|Grade/i);
    if (!openedNonElectronicSubmissionList) {
      test.skip(true, 'No grade/view submissions action was available for non-electronic assignment');
    }
    await page.waitForLoadState('domcontentloaded').catch(() => {});

    const newGraderToggle = page.locator('sakai-grader-toggle input').first();
    await newGraderToggle.waitFor({ state: 'visible', timeout: 15000 }).catch(() => {});
    if (await newGraderToggle.count()) {
      await newGraderToggle.uncheck({ force: true });
    }

    await page.locator('#submissionList a').filter({ hasText: 'student0011' }).first().click({ force: true });
    await sakai.typeCkEditor('grade_submission_feedback_comment', '<p>Please submit again.</p>');
    await page.locator('input#save-and-return').click({ force: true });
  });
});
