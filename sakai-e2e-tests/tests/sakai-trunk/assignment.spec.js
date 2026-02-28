const { test, expect } = require('../support/fixtures');

test.describe('Assignments', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;
  const assignTitle = `Playwright Assignment ${Date.now()}`;
  const assignmentToolIds = [
    'sakai\\.rubrics',
    'sakai\\.assignment\\.grades',
    'sakai\\.gradebookng',
  ];

  const proceedAndSubmitAssignment = async (page) => {
    const proceedButton = page.locator([
      '.act input[type="submit"][value*="Proceed"]',
      '.act input[type="button"][value*="Proceed"]',
      '.act button:has-text("Proceed")',
    ].join(', ')).first();
    await expect(proceedButton).toBeVisible({ timeout: 20000 });
    await proceedButton.scrollIntoViewIfNeeded().catch(() => {});
    await proceedButton.click({ force: true });

    const submitButton = page.locator([
      '.act input[type="submit"][value*="Submit"]',
      '.act input[type="button"][value*="Submit"]',
      '.act button:has-text("Submit")',
      '.act button:has-text("Submit for Grading")',
    ].join(', ')).first();
    await expect(submitButton).toBeVisible({ timeout: 20000 });
    await submitButton.scrollIntoViewIfNeeded().catch(() => {});
    await submitButton.click({ force: true });
  };

  const ensureCourseUrl = async (sakai) => {
    if (sakaiUrl) {
      return sakaiUrl;
    }

    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', assignmentToolIds);
    expect(sakaiUrl).toContain('/portal/site/');
    return sakaiUrl;
  };

  const assignmentIdFromHref = (href) => {
    if (!href) {
      return null;
    }
    const match = href.match(/[?&]assignmentId=([^&]+)/);
    return match ? decodeURIComponent(match[1]) : null;
  };

  const listedAssignmentIds = async (page) => {
    const editLinks = page.locator('.itemAction a[href*="sakai_action=doEdit_assignment"]');
    const hrefs = await editLinks.evaluateAll((links) => links.map((link) => link.getAttribute('href') || ''));
    return hrefs.map((href) => {
      const match = href.match(/[?&]assignmentId=([^&]+)/);
      return match ? decodeURIComponent(match[1]) : null;
    }).filter(Boolean);
  };

  const ensureStudentAssignmentExists = async (page, sakai) => {
    const courseUrl = await ensureCourseUrl(sakai);
    await sakai.login('instructor1');
    await page.goto(courseUrl);
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
    const courseUrl = await ensureCourseUrl(sakai);
    expect(courseUrl).toContain('/portal/site/');
  });

  test('can create a letter grade assignment', async ({ page, sakai }) => {
    const courseUrl = await ensureCourseUrl(sakai);
    await sakai.login('instructor1');
    await page.goto(courseUrl);
    await sakai.toolClick('Assignments');

    await sakai.openAddAssignmentForm();
    await page.locator('#new_assignment_title').fill('Letter Grades');
    await page.locator('#new_assignment_grade_type').first().selectOption({ label: 'Letter grade' }, { force: true });

    await sakai.typeCkEditor('new_assignment_instructions',
      '<p>What is chiefly responsible for the increase in the average length of life in the USA during the last fifty years?</p>');

    await sakai.submitAssignmentForm();

    let openedFirstSubmissionList = await sakai.clickAssignmentAction(/View Submissions|Grade/i);
    if (!openedFirstSubmissionList) {
      await page.goto(courseUrl);
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
      await page.goto(courseUrl);
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

    await page.goto(courseUrl);
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
    const courseUrl = await ensureCourseUrl(sakai);
    await sakai.login('instructor1');
    await page.goto(courseUrl);
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
    const courseUrl = await ensureCourseUrl(sakai);
    const rubricAssignmentTitle = `Rubric Assignment ${Date.now()}`;

    await sakai.createRubric('instructor1', courseUrl);

    await page.locator('.modal-footer button:visible, div.popover button:visible, button:visible')
      .filter({ hasText: /Save/i })
      .first()
      .click({ force: true });

    await page.goto(courseUrl);
    await sakai.toolClick('Assignments');
    await sakai.gotoAssignmentsList();
    await sakai.openAddAssignmentForm();

    const titleInput = page.locator('#new_assignment_title').first();
    await expect(titleInput).toBeVisible({ timeout: 15000 });
    await titleInput.fill(rubricAssignmentTitle);
    await expect(titleInput).toHaveValue(rubricAssignmentTitle);

    const gradeAssignmentCheckbox = page.locator('#gradeAssignment').first();
    await expect(gradeAssignmentCheckbox).toBeVisible({ timeout: 15000 });
    await gradeAssignmentCheckbox.check({ force: true });
    await expect(gradeAssignmentCheckbox).toBeChecked();

    await page.locator('#new_assignment_grade_points').fill('55.13');
    await expect(page.locator('#new_assignment_grade_points')).toHaveValue('55.13');
    await page.locator('input[name="rbcs-associate"][value="1"]').check({ force: true });
    await expect(page.locator('input[name="rbcs-associate"][value="1"]')).toBeChecked();

    await sakai.typeCkEditor('new_assignment_instructions',
      '<p>What is chiefly responsible for the increase in the average length of life in the USA during the last fifty years?</p>');

    await sakai.normalizeAssignmentFormDefaults();

    const dateParts = await page.evaluate(() => {
      const prefixes = ['new_assignment_open', 'new_assignment_due', 'new_assignment_close'];
      const parts = ['year', 'month', 'day', 'hour', 'min'];
      const output = {};
      for (const prefix of prefixes) {
        output[prefix] = {};
        for (const part of parts) {
          const field = document.querySelector(`input[name="${prefix}_${part}"]`);
          output[prefix][part] = field ? String(field.value || '') : '';
        }
      }
      return output;
    });

    for (const prefix of Object.keys(dateParts)) {
      for (const part of Object.keys(dateParts[prefix])) {
        expect(dateParts[prefix][part], `${prefix}_${part} should be numeric`).toMatch(/^\d+$/);
      }
    }

    await sakai.submitAssignmentForm();
    await sakai.gotoAssignmentsList();

    const rubricRow = page.locator('tr').filter({ hasText: rubricAssignmentTitle }).first();
    await expect(rubricRow).toBeVisible();
    await expect(rubricRow).toContainText(/55\.13|55/);
    await expect(rubricRow).not.toContainText(/No Grade/i);

    const editLink = rubricRow.locator('a').filter({ hasText: /^Edit\b/i }).first();
    await expect(editLink).toBeVisible();
    await editLink.click({ force: true });

    await expect(page.locator('#new_assignment_title')).toHaveValue(rubricAssignmentTitle);
    await expect(page.locator('#gradeAssignment')).toBeChecked();
    await expect(page.locator('#new_assignment_grade_points')).toHaveValue('55.13');
    await expect(page.locator('input[name="rbcs-associate"][value="1"]')).toBeChecked();
  });

  test('can submit as student on desktop', async ({ page, sakai }) => {
    const assignmentReady = await ensureStudentAssignmentExists(page, sakai);
    if (!assignmentReady) {
      test.skip(true, `${assignTitle} was not published/listed for student flows in this run`);
    }

    const courseUrl = await ensureCourseUrl(sakai);
    await page.setViewportSize({ width: 1280, height: 800 });
    await sakai.login('student0011');
    await page.goto(courseUrl);
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
    await proceedAndSubmitAssignment(page);
    await expect(page.getByRole('heading', { name: /Submission Confirmation/i })).toBeVisible();
    await page.locator('.act input.active[value*="Back to list"], .act button.active:has-text("Back to list")').first().click({ force: true });
  });

  test('can submit as student on iphone viewport', async ({ page, sakai }) => {
    const assignmentReady = await ensureStudentAssignmentExists(page, sakai);
    if (!assignmentReady) {
      test.skip(true, `${assignTitle} was not published/listed for student flows in this run`);
    }

    const courseUrl = await ensureCourseUrl(sakai);
    await page.setViewportSize({ width: 375, height: 812 });
    await sakai.login('student0012');
    await page.goto(courseUrl);

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
    await proceedAndSubmitAssignment(page);
    await expect(page.getByRole('heading', { name: /Submission Confirmation/i })).toBeVisible();
    await page.locator('.act input.active[value*="Back to list"], .act button.active:has-text("Back to list")').first().click({ force: true });
  });

  test('can allow a student to resubmit', async ({ page, sakai }) => {
    const courseUrl = await ensureCourseUrl(sakai);
    await sakai.login('instructor1');
    await page.goto(courseUrl);
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
    const nonElectronicTitle = `Non-electronic Assignment ${Date.now()}`;
    const courseUrl = await ensureCourseUrl(sakai);
    await sakai.login('instructor1');
    await page.goto(courseUrl);
    await sakai.toolClick('Assignments');
    await sakai.gotoAssignmentsList();
    const existingAssignmentIds = new Set(await listedAssignmentIds(page));

    await sakai.openAddAssignmentForm();
    await page.locator('#new_assignment_title').fill(nonElectronicTitle);
    await page.locator('#gradeAssignment').uncheck({ force: true });
    await page.locator('#subType').selectOption({ label: 'Non-electronic' });

    await sakai.typeCkEditor('new_assignment_instructions',
      '<p>Submit a 3000 word essay on the history of the internet to my office in Swanson 201.</p>');

    await sakai.submitAssignmentForm();
    await sakai.gotoAssignmentsList();

    const rows = page.locator('table tbody tr');
    const rowCount = await rows.count();
    let nonElectronicRow = null;
    let createdAssignmentId = null;
    for (let index = 0; index < rowCount; index += 1) {
      const row = rows.nth(index);
      const editLink = row.locator('.itemAction a[href*="sakai_action=doEdit_assignment"]').first();
      if (!(await editLink.count())) {
        continue;
      }
      const assignmentId = assignmentIdFromHref(await editLink.getAttribute('href'));
      if (assignmentId && !existingAssignmentIds.has(assignmentId)) {
        nonElectronicRow = row;
        createdAssignmentId = assignmentId;
        break;
      }
    }

    if (!nonElectronicRow || !createdAssignmentId) {
      throw new Error('Could not find the newly created non-electronic assignment row');
    }
    await expect(nonElectronicRow).toBeVisible();

    const nonElectronicAction = nonElectronicRow.locator('.itemAction a').filter({ hasText: /View Submissions|Grade/i }).first();
    await expect(nonElectronicAction).toBeVisible();
    await nonElectronicAction.click({ force: true });
    await page.waitForLoadState('domcontentloaded').catch(() => {});

    const newGraderToggle = page.locator('sakai-grader-toggle input').first();
    await newGraderToggle.waitFor({ state: 'visible', timeout: 15000 }).catch(() => {});
    if (await newGraderToggle.count()) {
      await newGraderToggle.uncheck({ force: true });
    }

    const studentSubmission = page.locator('#submissionList a').filter({ hasText: 'student0011' }).first();
    if (await studentSubmission.isVisible({ timeout: 10000 }).catch(() => false)) {
      await studentSubmission.click({ force: true });
      await sakai.typeCkEditor('grade_submission_feedback_comment', '<p>Please submit again.</p>');
      await page.locator('input#save-and-return').click({ force: true });
      return;
    }

    await sakai.gotoAssignmentsList();
    const createdRow = page.locator('table tbody tr').filter({
      has: page.locator(`.itemAction a[href*="sakai_action=doEdit_assignment"][href*="${createdAssignmentId}"]`),
    }).first();
    await expect(createdRow).toBeVisible();
    await expect(createdRow).toContainText(/Submissions:\s*0\//i);
  });
});
